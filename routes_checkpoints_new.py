import os
import json
import logging
from datetime import datetime, date, time, timedelta
from functools import wraps

from flask import Blueprint, render_template, request, redirect, url_for, flash, jsonify, session
from flask import current_app, abort, send_file
from flask_login import login_required, current_user
from sqlalchemy import extract, func
from werkzeug.security import generate_password_hash, check_password_hash
from werkzeug.utils import secure_filename

from app import db
from models import User, Employee, Company, UserRole
from models_checkpoints import CheckPoint, CheckPointRecord, CheckPointIncident, EmployeeContractHours
from models_checkpoints import CheckPointStatus, CheckPointIncidentType, CheckPointOriginalRecord
from models_access import LocationAccessToken, PortalType
from sqlalchemy import or_
from forms_checkpoints import (CheckPointForm, CheckPointLoginForm, CheckPointEmployeePinForm, 
                             ContractHoursForm, CheckPointRecordAdjustmentForm,
                             SignaturePadForm, ExportCheckPointRecordsForm,
                             ManualCheckPointRecordForm)
from utils import log_activity, slugify
from utils_checkpoints import generate_pdf_report, draw_signature


# Crear un Blueprint para las rutas de checkpoints con nombres slugificados
checkpoints_bp = Blueprint('checkpoints_slug', __name__, url_prefix='/fichajes')


# Decoradores personalizados
def admin_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or current_user.role != UserRole.ADMIN:
            flash('Acceso denegado. Se requieren permisos de administrador.', 'danger')
            return redirect(url_for('index'))
        return f(*args, **kwargs)
    return decorated_function


def manager_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or (
            current_user.role != UserRole.ADMIN and 
            current_user.role != UserRole.GERENTE
        ):
            flash('Acceso denegado. Se requieren permisos de gerente o administrador.', 'danger')
            return redirect(url_for('index'))
        return f(*args, **kwargs)
    return decorated_function


def checkpoint_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'checkpoint_id' not in session:
            flash("Debe iniciar sesion como punto de fichaje.", "warning")
            return redirect(url_for('checkpoints_slug.login'))
        return f(*args, **kwargs)
    return decorated_function

# Resto del código...

@checkpoints_bp.route('/company/<slug>/rrrrrr', methods=['GET'])
@login_required
@manager_required
def view_original_records(slug):
    """Página secreta para ver los registros originales antes de ajustes de una empresa específica"""
    from models_checkpoints import CheckPointOriginalRecord
    
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Esta página es solo para administradores
    page = request.args.get('page', 1, type=int)
    start_date = request.args.get('start_date')
    end_date = request.args.get('end_date')
    employee_id = request.args.get('employee_id', type=int)
    show_all = request.args.get('show_all', 'false')
    
    # Obtener los IDs de los empleados de esta empresa
    employee_ids = db.session.query(Employee.id).filter_by(company_id=company_id).all()
    employee_ids = [e[0] for e in employee_ids]
    
    # Construir la consulta base con filtro de empresa
    query = db.session.query(
        CheckPointOriginalRecord, 
        CheckPointRecord, 
        Employee
    ).join(
        CheckPointRecord, 
        CheckPointOriginalRecord.record_id == CheckPointRecord.id
    ).join(
        Employee,
        CheckPointRecord.employee_id == Employee.id
    ).filter(
        Employee.company_id == company_id
    )
    
    # Mostrar todos los registros originales, incluyendo los que solo tienen entrada
    # No filtramos por original_check_out_time para mostrar todos los registros
    
    # Aplicar filtros si los hay
    if start_date:
        try:
            start_date = datetime.strptime(start_date, '%Y-%m-%d').date()
            query = query.filter(func.date(CheckPointOriginalRecord.original_check_in_time) >= start_date)
        except ValueError:
            pass
    
    if end_date:
        try:
            end_date = datetime.strptime(end_date, '%Y-%m-%d').date()
            query = query.filter(func.date(CheckPointOriginalRecord.original_check_in_time) <= end_date)
        except ValueError:
            pass
    
    if employee_id:
        query = query.filter(Employee.id == employee_id)
    
    # Ejecutar la consulta y obtener todos los resultados
    all_records = query.order_by(
        CheckPointOriginalRecord.adjusted_at.desc()
    ).all()
    
    # Filtrar registros duplicados (mismo empleado, misma fecha) manteniendo solo los completos
    filtered_records = []
    seen_records = set()  # Para rastrear combinaciones empleado-fecha ya procesadas
    
    for original, record, employee in all_records:
        # Crear una clave única para identificar registros del mismo empleado en el mismo día
        record_date = original.original_check_in_time.date()
        record_key = (employee.id, record_date)
        
        # Solo incluir cada combinación empleado-fecha una vez
        # Incluimos también los registros con solo hora de entrada
        if record_key not in seen_records:
            filtered_records.append((original, record, employee))
            seen_records.add(record_key)
    
    # Paginar los resultados filtrados manualmente
    total_records = len(filtered_records)
    per_page = 20
    total_pages = (total_records + per_page - 1) // per_page  # Redondear hacia arriba
    
    # Validar número de página
    if page < 1:
        page = 1
    if page > total_pages and total_pages > 0:
        page = total_pages
    
    # Calcular índices de inicio y fin para la página actual
    start_idx = (page - 1) * per_page
    end_idx = min(start_idx + per_page, total_records)
    
    # Obtener los registros para la página actual
    page_records = filtered_records[start_idx:end_idx] if filtered_records else []
    
    # Crear un objeto similar a la paginación de SQLAlchemy para usar en la plantilla
    class Pagination:
        def __init__(self, items, page, per_page, total):
            self.items = items
            self.page = page
            self.per_page = per_page
            self.total = total
            
        @property
        def pages(self):
            return (self.total + self.per_page - 1) // self.per_page
            
        @property
        def has_prev(self):
            return self.page > 1
            
        @property
        def has_next(self):
            return self.page < self.pages
            
        @property
        def prev_num(self):
            return self.page - 1 if self.has_prev else None
            
        @property
        def next_num(self):
            return self.page + 1 if self.has_next else None
    
    # Crear objeto de paginación
    paginated_records = Pagination(page_records, page, per_page, total_records)
    
    # Obtener la lista de empleados para el filtro (todos los empleados de esta empresa, activos e inactivos)
    employees = Employee.query.filter_by(company_id=company_id).order_by(Employee.first_name, Employee.last_name).all()
    logging.debug(f"Encontrados {len(employees)} empleados para el filtro de registros originales")
    
    # Si se solicita exportación
    export_format = request.args.get('export')
    if export_format == 'pdf':
        return export_original_records_pdf(filtered_records, start_date, end_date, company)
    
    return render_template(
        'checkpoints/original_records.html',
        original_records=paginated_records,
        employees=employees,
        company=company,
        company_id=company_id,
        filters={
            'start_date': start_date.strftime('%Y-%m-%d') if isinstance(start_date, date) else None,
            'end_date': end_date.strftime('%Y-%m-%d') if isinstance(end_date, date) else None,
            'employee_id': employee_id
        },
        show_all=show_all,
        title=f"Registros Originales de {company.name if company else ''} (Antes de Ajustes)"
    )

@checkpoints_bp.route('/company/<slug>/manual_record', methods=['GET', 'POST'])
@login_required
@manager_required
def create_manual_record(slug):
    """Página para crear un fichaje manual para una empresa específica"""
    try:
        # Buscar la empresa por slug
        companies = Company.query.all()
        company = None
        company_id = None
        
        for comp in companies:
            if slugify(comp.name) == slug:
                company = comp
                company_id = comp.id
                break
        
        if not company:
            abort(404)
        
        # Verificar permisos
        if not current_user.is_admin() and company not in current_user.companies:
            flash('No tiene permiso para gestionar esta empresa.', 'danger')
            return redirect(url_for('main.dashboard'))
        
        # Crear formulario
        form = ManualCheckPointRecordForm()
        
        # Cargar opciones de empleados y puntos de fichaje
        employees = Employee.query.filter_by(company_id=company.id, is_active=True).order_by(Employee.first_name).all()
        checkpoints = CheckPoint.query.filter_by(company_id=company.id, status=CheckPointStatus.ACTIVE).all()
        
        form.employee_id.choices = [(emp.id, f"{emp.first_name} {emp.last_name}") for emp in employees]
        form.checkpoint_id.choices = [(cp.id, cp.name) for cp in checkpoints]
        
        # Establecer fecha actual por defecto
        if not form.check_in_date.data:
            form.check_in_date.data = datetime.now().strftime('%Y-%m-%d')
        
        # Procesar formulario si se envió
        if form.validate_on_submit():
            # Determinar si se guarda como registro original o ajustado
            is_original = form.is_original.data == '1'
            
            # Procesar fecha y hora de entrada
            check_in_datetime = datetime.strptime(form.check_in_date.data, '%Y-%m-%d')
            check_in_datetime = datetime.combine(check_in_datetime.date(), form.check_in_time.data)
            
            # Procesar hora de salida si se proporcionó
            check_out_datetime = None
            if form.check_out_time.data:
                # Utilizar la misma fecha que la entrada para la salida
                check_out_datetime = datetime.combine(check_in_datetime.date(), form.check_out_time.data)
                
                # Si la hora de salida es anterior a la hora de entrada, asumir que es del día siguiente
                if check_out_datetime < check_in_datetime:
                    check_out_datetime = check_out_datetime + timedelta(days=1)
            
            # Importar los modelos necesarios
            from models_checkpoints import CheckPointOriginalRecord
            
            # Siempre creamos primero un registro en la tabla CheckPointRecord
            record = CheckPointRecord()
            record.checkpoint_id = form.checkpoint_id.data
            record.employee_id = form.employee_id.data
            record.check_in_time = check_in_datetime
            if check_out_datetime:
                record.check_out_time = check_out_datetime
            if form.notes.data:
                record.notes = form.notes.data
            
            # Guardar el registro en la base de datos para obtener su ID
            db.session.add(record)
            db.session.flush()  # Actualiza el objeto con el ID asignado
            
            message_type = 'Fichaje ajustado'
            
            # Si se eligió guardar como registro original, crear también un registro en CheckPointOriginalRecord
            if is_original:
                # Calcular horas trabajadas si hay checkout
                hours_worked = 0.0
                if check_out_datetime:
                    delta = check_out_datetime - check_in_datetime
                    hours_worked = delta.total_seconds() / 3600
                
                # Crear el registro original
                original_record = CheckPointOriginalRecord()
                original_record.record_id = record.id  # Enlaza con el registro recién creado
                original_record.original_check_in_time = check_in_datetime
                original_record.original_check_out_time = check_out_datetime
                original_record.original_notes = form.notes.data
                original_record.hours_worked = hours_worked
                original_record.adjustment_reason = "Registro original creado manualmente"
                
                # Si hay usuario autenticado, asociarlo como el que realizó el ajuste
                if current_user.is_authenticated:
                    original_record.adjusted_by_id = current_user.id
                
                # Añadir a la sesión
                db.session.add(original_record)
                message_type = 'Fichaje original'
            
            # Guardar todos los cambios en la base de datos
            db.session.commit()
            
            # Si hay hora de salida, actualizar las horas trabajadas
            if check_out_datetime:
                # Importar la función update_employee_work_hours
                from utils_work_hours import update_employee_work_hours
                
                # Actualizar las horas trabajadas para este empleado
                update_employee_work_hours(record.employee_id, record.check_in_time, record.check_out_time)
            
            flash(f'{message_type} creado correctamente', 'success')
            # Siempre redirigir a la vista de registros originales después de crear el registro
            return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
        
        return render_template('checkpoints/create_manual_record.html', form=form, company=company)
    
    except Exception as e:
        current_app.logger.error(f"Error en create_manual_record: {e}")
        db.session.rollback()  # Asegurarse de revertir cualquier cambio parcial
        # Detallamos el error para facilitar la depuración
        current_app.logger.error(f"Error detallado: {str(e)}")
        flash(f'Error al crear el fichaje manual: {str(e)}', 'danger')
        return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))

@checkpoints_bp.route('/company/<slug>/rrrrrr/new', methods=['GET', 'POST'])
@login_required
@manager_required
def create_original_record(slug):
    """Crea un nuevo registro original"""
    from models_checkpoints import CheckPointOriginalRecord
    from flask_wtf import FlaskForm
    from wtforms import StringField, TimeField, TextAreaField, SubmitField, SelectField
    from wtforms.validators import DataRequired, Optional, Length
    
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Obtener todos los empleados activos de esta empresa
    employees = Employee.query.filter_by(company_id=company_id, is_active=True).all()
    if not employees:
        flash('No hay empleados activos para esta empresa.', 'warning')
        return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
    
    # Obtener todos los puntos de fichaje activos
    checkpoints = CheckPoint.query.filter_by(company_id=company_id, is_active=True).all()
    if not checkpoints:
        flash('No hay puntos de fichaje activos para esta empresa.', 'warning')
        return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
    
    # Crear un formulario para el nuevo registro
    class CreateOriginalRecordForm(FlaskForm):
        employee_id = SelectField('Empleado', validators=[DataRequired()], coerce=int)
        checkpoint_id = SelectField('Punto de Fichaje', validators=[DataRequired()], coerce=int)
        original_check_in_date = StringField('Fecha de entrada original', validators=[DataRequired()])
        original_check_in_time = TimeField('Hora de entrada original', validators=[DataRequired()])
        original_check_out_time = TimeField('Hora de salida original', validators=[Optional()])
        notes = TextAreaField('Notas', validators=[Optional(), Length(max=500)])
        submit = SubmitField('Crear Registro')
    
    form = CreateOriginalRecordForm()
    
    # Llenar las opciones de los selectores
    form.employee_id.choices = [(emp.id, f"{emp.first_name} {emp.last_name}") for emp in employees]
    form.checkpoint_id.choices = [(cp.id, cp.name) for cp in checkpoints]
    
    # Establecer fecha actual por defecto
    if request.method == 'GET':
        form.original_check_in_date.data = datetime.now().strftime('%Y-%m-%d')
    
    # Procesar el formulario
    if form.validate_on_submit():
        try:
            # Crear un nuevo registro de punto de control
            record = CheckPointRecord()
            record.employee_id = form.employee_id.data
            record.checkpoint_id = form.checkpoint_id.data
            
            # Obtener fecha y hora de entrada
            check_in_date = datetime.strptime(form.original_check_in_date.data, '%Y-%m-%d').date()
            check_in_time = datetime.combine(check_in_date, form.original_check_in_time.data)
            record.check_in_time = check_in_time
            
            # Obtener hora de salida si está presente
            check_out_time = None
            if form.original_check_out_time.data:
                # Si la hora de salida es menor que la de entrada, asumimos que es del día siguiente
                if form.original_check_out_time.data < form.original_check_in_time.data:
                    check_out_date = check_in_date + timedelta(days=1)
                else:
                    check_out_date = check_in_date
                    
                check_out_time = datetime.combine(check_out_date, form.original_check_out_time.data)
                record.check_out_time = check_out_time
                
                # Calcular las horas trabajadas
                from utils_work_hours import update_employee_work_hours
                
                # Calcular la duración en horas
                delta = check_out_time - check_in_time
                hours_worked = delta.total_seconds() / 3600  # Convertir segundos a horas
                
                # Actualizar las horas trabajadas
                update_employee_work_hours(form.employee_id.data, check_in_time, hours_worked)
            
            # Guardar el registro
            db.session.add(record)
            db.session.flush()  # Para obtener el ID del registro
            
            # Crear el registro original correspondiente
            original_record = CheckPointOriginalRecord()
            original_record.record_id = record.id
            original_record.original_check_in_time = check_in_time
            original_record.original_check_out_time = check_out_time
            original_record.original_notes = form.notes.data
            original_record.adjusted_at = datetime.now()
            original_record.adjustment_reason = "Registro creado manualmente"
            
            # Guardar el registro original
            db.session.add(original_record)
            db.session.commit()
            
            flash('Registro original creado con éxito.', 'success')
            return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
            
        except Exception as e:
            db.session.rollback()
            flash(f'Error al crear el registro: {str(e)}', 'danger')
    
    return render_template('checkpoints/create_original_record.html', 
                           form=form, 
                           company=company, 
                           title='Crear Nuevo Registro Original')

@checkpoints_bp.route('/company/<slug>/rrrrrr/edit/<int:id>', methods=['GET', 'POST'])
@login_required
@manager_required
def edit_original_record(slug, id):
    """Edita un registro original"""
    from models_checkpoints import CheckPointOriginalRecord
    from flask_wtf import FlaskForm
    from wtforms import StringField, TimeField, TextAreaField, SubmitField
    from wtforms.validators import DataRequired, Optional, Length
    
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Obtener el registro original
    original_record = CheckPointOriginalRecord.query.get_or_404(id)
    record = CheckPointRecord.query.get_or_404(original_record.record_id)
    
    # Verificar que el registro pertenece a esta empresa
    if record.employee.company_id != company_id:
        flash('Registro no encontrado para esta empresa.', 'warning')
        return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
    
    # Crear un formulario para editar el registro
    class EditOriginalRecordForm(FlaskForm):
        original_check_in_date = StringField('Fecha de entrada original', validators=[DataRequired()])
        original_check_in_time = TimeField('Hora de entrada original', validators=[DataRequired()])
        original_check_out_time = TimeField('Hora de salida original', validators=[Optional()])
        notes = TextAreaField('Notas', validators=[Optional(), Length(max=500)])
        submit = SubmitField('Guardar cambios')
    
    form = EditOriginalRecordForm()
    
    # Pre-llenar el formulario con los datos actuales
    if request.method == 'GET':
        form.original_check_in_date.data = original_record.original_check_in_time.strftime('%Y-%m-%d')
        form.original_check_in_time.data = original_record.original_check_in_time.time()
        if original_record.original_check_out_time:
            form.original_check_out_time.data = original_record.original_check_out_time.time()
        form.notes.data = original_record.original_notes
    
    # Procesar el formulario
    if form.validate_on_submit():
        try:
            # Obtener fecha y hora de entrada
            check_in_date = datetime.strptime(form.original_check_in_date.data, '%Y-%m-%d').date()
            original_record.original_check_in_time = datetime.combine(check_in_date, form.original_check_in_time.data)
            
            # Obtener hora de salida si está presente
            if form.original_check_out_time.data:
                # Si la hora de salida es menor que la de entrada, asumimos que es del día siguiente
                if form.original_check_out_time.data < form.original_check_in_time.data:
                    check_out_date = check_in_date + timedelta(days=1)
                else:
                    check_out_date = check_in_date
                    
                original_record.original_check_out_time = datetime.combine(check_out_date, form.original_check_out_time.data)
            else:
                original_record.original_check_out_time = None
            
            # Actualizar notas
            original_record.original_notes = form.notes.data
            
            # Guardar cambios
            db.session.commit()
            flash('Registro original actualizado con éxito.', 'success')
            return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
            
        except Exception as e:
            db.session.rollback()
            flash(f'Error al actualizar el registro: {str(e)}', 'danger')
    
    return render_template('checkpoints/edit_original_record.html', 
                          form=form, 
                          original_record=original_record,
                          record=record,
                          company=company,
                          company_id=company_id)

@checkpoints_bp.route('/company/<slug>/rrrrrr/restore/<int:id>', methods=['GET'])
@login_required
@manager_required
def restore_original_record(slug, id):
    """Restaura los valores originales en el registro actual"""
    from models_checkpoints import CheckPointOriginalRecord
    
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Obtener el registro original y el registro actual
    original_record = CheckPointOriginalRecord.query.get_or_404(id)
    record = CheckPointRecord.query.get_or_404(original_record.record_id)
    
    # Verificar que el registro pertenece a esta empresa
    if record.employee.company_id != company_id:
        flash('Registro no encontrado para esta empresa.', 'warning')
        return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
    
    try:
        # Restaurar valores originales
        record.check_in_time = original_record.original_check_in_time
        record.check_out_time = original_record.original_check_out_time
        record.signature_data = original_record.original_signature_data
        record.has_signature = original_record.original_has_signature
        record.notes = original_record.original_notes
        
        # Actualizar razón de ajuste
        record.adjustment_reason = "Restaurado a valores originales"
        
        # Guardar cambios
        db.session.commit()
        flash('Registro restaurado a valores originales con éxito.', 'success')
    except Exception as e:
        db.session.rollback()
        flash(f'Error al restaurar el registro: {str(e)}', 'danger')
    
    return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))

@checkpoints_bp.route('/company/<slug>/rrrrrr/delete/<int:id>', methods=['GET'])
@login_required
@manager_required
def delete_original_record(slug, id):
    """Elimina un registro original"""
    from models_checkpoints import CheckPointOriginalRecord
    
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Obtener el registro original
    original_record = CheckPointOriginalRecord.query.get_or_404(id)
    record = CheckPointRecord.query.get_or_404(original_record.record_id)
    
    # Verificar que el registro pertenece a esta empresa
    if record.employee.company_id != company_id:
        flash('Registro no encontrado para esta empresa.', 'warning')
        return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
    
    try:
        # Eliminar el registro
        db.session.delete(original_record)
        db.session.commit()
        flash('Registro original eliminado con éxito.', 'success')
    except Exception as e:
        db.session.rollback()
        flash(f'Error al eliminar el registro: {str(e)}', 'danger')
    
    return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))

@checkpoints_bp.route('/company/<slug>/rrrrrr/export', methods=['GET'])
@login_required
@manager_required
def export_original_records(slug):
    """Exporta los registros originales a PDF"""
    import logging
    from models_checkpoints import CheckPointOriginalRecord
    
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Obtener parámetros de filtro
    start_date = request.args.get('start_date')
    end_date = request.args.get('end_date')
    employee_id = request.args.get('employee_id', type=int)
    
    # Construir la consulta directamente con la tabla CheckPointOriginalRecord y Employee
    query = db.session.query(
        CheckPointOriginalRecord, 
        Employee
    ).join(
        CheckPointRecord, 
        CheckPointOriginalRecord.record_id == CheckPointRecord.id
    ).join(
        Employee,
        CheckPointRecord.employee_id == Employee.id
    ).filter(
        Employee.company_id == company_id
    )
    
    # Aplicar filtros adicionales
    if start_date:
        try:
            start_date = datetime.strptime(start_date, '%Y-%m-%d').date()
            query = query.filter(func.date(CheckPointOriginalRecord.original_check_in_time) >= start_date)
        except ValueError:
            start_date = None
    
    if end_date:
        try:
            end_date = datetime.strptime(end_date, '%Y-%m-%d').date()
            query = query.filter(func.date(CheckPointOriginalRecord.original_check_in_time) <= end_date)
        except ValueError:
            end_date = None
    
    if employee_id:
        query = query.filter(Employee.id == employee_id)
    
    # Ordenar por empleado y fecha
    records = query.order_by(
        Employee.last_name,
        Employee.first_name,
        CheckPointOriginalRecord.original_check_in_time
    ).all()
    
    if not records:
        flash('No se encontraron registros para los filtros seleccionados', 'warning')
        return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
    
    # Generar PDF con los registros
    return export_original_records_pdf(records, start_date, end_date, company)

def export_original_records_pdf(records, start_date=None, end_date=None, company=None):
    """Genera un PDF con los registros originales por empleado agrupados por semanas"""
    import logging
    from fpdf import FPDF
    from tempfile import NamedTemporaryFile
    from utils_checkpoints import get_iso_week_start_end, get_week_description
    
    # Crear un archivo temporal para guardar el PDF
    pdf_file = NamedTemporaryFile(delete=False, suffix='.pdf')
    pdf_file.close()
    
    # Clase PDF personalizada con formato mejorado
    class WeeklyReportPDF(FPDF):
        def __init__(self):
            super().__init__()
            # Colores corporativos
            self.primary_color = (128, 128, 128)  # Gris medio
            self.secondary_color = (169, 169, 169)  # Gris claro
            self.accent_color = (245, 245, 245)  # Gris muy claro para fondos
            
        def header(self):
            # Fondo de cabecera
            self.set_fill_color(*self.primary_color)
            self.rect(0, 0, 210, 15, 'F')
            
            # Título con texto blanco
            self.set_font('Arial', 'B', 15)
            self.set_text_color(255, 255, 255)
            
            title = 'Registros Originales de Fichajes'
            if company:
                title = f'Registros Originales de Fichajes - {company.name}'
            
            self.cell(0, 10, title, 0, 1, 'C')
            
            # Restaurar color de texto
            self.set_text_color(0, 0, 0)
            
            # Período
            self.set_font('Arial', '', 10)
            if start_date and end_date:
                period = f"Período: {start_date.strftime('%d/%m/%Y')} - {end_date.strftime('%d/%m/%Y')}"
            elif start_date:
                period = f"Desde: {start_date.strftime('%d/%m/%Y')}"
            elif end_date:
                period = f"Hasta: {end_date.strftime('%d/%m/%Y')}"
            else:
                period = "Todos los registros"
            
            self.set_y(15)  # Posicionarse después de la barra
            self.cell(0, 10, period, 0, 1, 'C')
            
            # Fecha de generación
            self.set_font('Arial', 'I', 8)
            self.cell(0, 5, f'Generado el: {datetime.now().strftime("%d/%m/%Y %H:%M")}', 0, 1, 'R')
            self.ln(5)
            
        def footer(self):
            # Pie de página
            self.set_y(-15)
            
            # Línea divisoria
            self.set_draw_color(*self.secondary_color)
            self.line(10, self.get_y(), 200, self.get_y())
            
            # Número de página
            self.set_font('Arial', 'I', 8)
            self.cell(0, 10, f'Página {self.page_no()}/{{nb}}', 0, 0, 'C')
    
    try:
        # Crear PDF
        pdf = WeeklyReportPDF()
        pdf.alias_nb_pages()
        pdf.add_page()
        pdf.set_auto_page_break(auto=True, margin=15)
        
        # Agrupar registros por empleado
        employee_records = {}
        for original, employee in records:
            if not original.original_check_in_time:
                continue  # Omitir registros sin hora de entrada original
                
            if employee.id not in employee_records:
                employee_records[employee.id] = {
                    'employee': employee,
                    'records': []
                }
            employee_records[employee.id]['records'].append(original)
        
        # Generar el PDF para cada empleado
        for emp_id, data in employee_records.items():
            employee = data['employee']
            original_records = data['records']
            
            # Encabezado de empleado
            pdf.set_font('Arial', 'B', 12)
            pdf.set_fill_color(*pdf.secondary_color)
            pdf.set_text_color(255, 255, 255)
            pdf.rect(10, pdf.get_y(), 190, 10, 'F')
            pdf.cell(0, 10, f"Empleado: {employee.first_name} {employee.last_name} (DNI: {employee.dni})", 0, 1, 'C')
            pdf.set_text_color(0, 0, 0)  # Restaurar color de texto
            pdf.ln(5)
            
            # Ordenar registros por fecha
            sorted_records = sorted(original_records, 
                                  key=lambda x: x.original_check_in_time if x.original_check_in_time else datetime.min)
            
            # Agrupar registros por semana (de lunes a domingo)
            weeks_records = {}
            for record in sorted_records:
                if not record.original_check_in_time:
                    continue
                    
                # Obtener el lunes de la semana para este registro
                week_start, _ = get_iso_week_start_end(record.original_check_in_time)
                week_key = week_start.strftime('%Y-%m-%d')
                
                if week_key not in weeks_records:
                    weeks_records[week_key] = {
                        'week_start': week_start,
                        'records': [],
                        'total_hours': 0.0
                    }
                
                weeks_records[week_key]['records'].append(record)
                
                # Sumar horas si el registro tiene duración
                hours = record.duration() if record.original_check_out_time and record.original_check_in_time else 0
                if isinstance(hours, (int, float)):
                    weeks_records[week_key]['total_hours'] += hours
            
            # Ordenar las semanas por fecha de inicio
            sorted_weeks = sorted(weeks_records.items(), key=lambda x: x[1]['week_start'])
            
            # Si no hay registros para mostrar
            if not sorted_weeks:
                pdf.set_font('Arial', 'B', 12)
                pdf.ln(5)
                pdf.cell(0, 10, "No se encontraron registros para este empleado", 0, 1, 'C')
                continue
                
            # Para cada semana
            employee_total_hours = 0
            
            for week_key, week_data in sorted_weeks:
                week_start = week_data['week_start']
                week_records = week_data['records']
                week_total_hours = week_data['total_hours']
                employee_total_hours += week_total_hours
                
                # Título de la semana
                pdf.set_font('Arial', 'B', 11)
                pdf.set_fill_color(*pdf.secondary_color)
                pdf.set_text_color(255, 255, 255)
                
                # Asegurarse de que hay espacio para el título de la semana y la tabla
                if pdf.get_y() > pdf.h - 40:  # Si queda poco espacio en la página
                    pdf.add_page()
                
                pdf.rect(10, pdf.get_y(), 190, 8, 'F')
                pdf.cell(0, 8, get_week_description(week_start), 0, 1, 'C', True)
                
                # Restaurar color de texto para la tabla
                pdf.set_text_color(0, 0, 0)
                
                # Encabezados de la tabla
                pdf.set_font('Arial', 'B', 9)
                pdf.set_fill_color(230, 230, 230)
                pdf.cell(45, 7, 'Fecha', 1, 0, 'C', True)
                pdf.cell(35, 7, 'Entrada', 1, 0, 'C', True)
                pdf.cell(35, 7, 'Salida', 1, 0, 'C', True)
                pdf.cell(35, 7, 'H. Totales', 1, 1, 'C', True)
                
                # Datos de fichajes de esta semana
                pdf.set_font('Arial', '', 9)
                
                # Color alternado para las filas (efecto cebra)
                row_count = 0
                
                for record in week_records:
                    # Aplicar color alternado
                    if row_count % 2 == 0:
                        pdf.set_fill_color(255, 255, 255)  # Blanco
                    else:
                        pdf.set_fill_color(*pdf.accent_color)  # Color claro
                        
                    # Fecha
                    fecha = record.original_check_in_time.strftime('%d/%m/%Y') if record.original_check_in_time else '-'
                    pdf.cell(45, 7, fecha, 1, 0, 'C', True)
                    
                    # Hora de entrada original
                    entrada = record.original_check_in_time.strftime('%H:%M') if record.original_check_in_time else '-'
                    pdf.cell(35, 7, entrada, 1, 0, 'C', True)
                    
                    # Hora de salida original
                    if record.original_check_out_time:
                        salida = record.original_check_out_time.strftime('%H:%M')
                        pdf.cell(35, 7, salida, 1, 0, 'C', True)
                    else:
                        pdf.set_text_color(255, 0, 0)  # Rojo para destacar
                        pdf.cell(35, 7, 'SIN SALIDA', 1, 0, 'C', True)
                        pdf.set_text_color(0, 0, 0)  # Restaurar color negro
                    
                    # Horas totales del día con cálculo especial (minutos × 0.60)
                    hours = record.duration() if record.original_check_out_time and record.original_check_in_time else '-'
                    if isinstance(hours, (int, float)):
                        # Convertir horas decimales a minutos totales
                        total_minutes = hours * 60
                        hours_part = int(total_minutes // 60)
                        minutes_part = int(total_minutes % 60)
                        
                        # Aplicar fórmula: minutos × 0.60 / 60 para convertir de vuelta a horas
                        converted_minutes = (minutes_part * 0.60) / 60
                        total_hours_special = hours_part + converted_minutes
                        
                        total_hours_str = f"{total_hours_special:.2f}"
                    else:
                        total_hours_str = '-'
                    pdf.cell(35, 7, total_hours_str, 1, 1, 'C', True)
                    
                    row_count += 1
                
                # Total de horas para esta semana
                pdf.set_font('Arial', 'B', 10)
                pdf.set_fill_color(*pdf.primary_color)
                pdf.set_text_color(255, 255, 255)
                # Ajustamos el ancho total a 150 (suma del ancho de las columnas: 45+35+35+35=150)
                pdf.cell(115, 8, 'TOTAL SEMANA:', 1, 0, 'R', True)
                pdf.cell(35, 8, f"{week_total_hours:.2f} h", 1, 1, 'C', True)
                pdf.set_text_color(0, 0, 0)  # Restaurar color de texto
                
                # Espacio después de cada tabla semanal
                pdf.ln(5)
            
            # Total general para este empleado
            if len(sorted_weeks) > 1:
                pdf.set_font('Arial', 'B', 12)
                pdf.set_fill_color(*pdf.primary_color)
                pdf.set_text_color(255, 255, 255)
                pdf.rect(10, pdf.get_y(), 190, 10, 'F')
                pdf.cell(0, 10, f"TOTAL GENERAL: {employee_total_hours:.2f} HORAS", 0, 1, 'C')
                pdf.set_text_color(0, 0, 0)  # Restaurar color de texto
            
            # Nueva página para el siguiente empleado
            if list(employee_records.keys()).index(emp_id) < len(employee_records) - 1:
                pdf.add_page()
        
        # Si no hay registros para mostrar
        if not employee_records:
            pdf.set_font('Arial', 'B', 12)
            pdf.ln(20)
            pdf.cell(0, 10, "No se encontraron registros válidos para los filtros seleccionados", 0, 1, 'C')
        
        # Guardar el PDF
        pdf.output(pdf_file.name)
        
        # Enviar el archivo
        filename = f"registros_originales_{company.name.replace(' ', '_') if company else 'empresa'}.pdf"
        return send_file(
            pdf_file.name,
            as_attachment=True,
            download_name=filename,
            mimetype='application/pdf'
        )
    except Exception as e:
        logging.error(f"Error al generar PDF: {str(e)}")
        # Intentar crear un PDF de error
        try:
            error_pdf = FPDF()
            error_pdf.add_page()
            error_pdf.set_font('Arial', 'B', 16)
            error_pdf.cell(0, 10, "Error al generar el PDF", 0, 1, 'C')
            error_pdf.set_font('Arial', '', 12)
            error_pdf.cell(0, 10, f"Se produjo un error: {str(e)}", 0, 1, 'L')
            error_pdf.output(pdf_file.name)
            return send_file(
                pdf_file.name,
                as_attachment=True,
                download_name="error_report.pdf",
                mimetype='application/pdf'
            )
        except:
            # Si falla incluso el PDF de error, devuelve un mensaje de error
            return jsonify({'error': f'Error al generar el PDF: {str(e)}'}), 500
    
# Nuevas rutas para ver y exportar ambos tipos de registros (con y sin salida)
@checkpoints_bp.route('/company/<slug>/both', methods=['GET'])
@login_required
@manager_required
def view_both_records(slug):
    """Página para ver todos los registros (con y sin hora de salida) de una empresa específica"""
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Obtener parámetros de filtrado
    page = request.args.get('page', 1, type=int)
    start_date = request.args.get('start_date')
    end_date = request.args.get('end_date')
    employee_id = request.args.get('employee_id', type=int)
    
    # Construir consulta base para todos los registros (con y sin hora de salida)
    query = db.session.query(
        CheckPointRecord, 
        Employee
    ).join(
        Employee,
        CheckPointRecord.employee_id == Employee.id
    ).filter(
        Employee.company_id == company_id
        # No filtramos por check_out_time para mostrar todos los registros
    ).outerjoin(
        CheckPointOriginalRecord,
        CheckPointOriginalRecord.record_id == CheckPointRecord.id
    )
    
    # Aplicar filtros si los hay
    if start_date:
        try:
            start_date = datetime.strptime(start_date, '%Y-%m-%d').date()
            query = query.filter(func.date(CheckPointRecord.check_in_time) >= start_date)
        except ValueError:
            pass
    
    if end_date:
        try:
            end_date = datetime.strptime(end_date, '%Y-%m-%d').date()
            query = query.filter(func.date(CheckPointRecord.check_in_time) <= end_date)
        except ValueError:
            pass
    
    if employee_id:
        query = query.filter(Employee.id == employee_id)
    
    # Ordenar y paginar
    records = query.order_by(
        CheckPointRecord.check_in_time.desc()
    ).paginate(page=page, per_page=20)
    
    # Obtener la lista de empleados para el filtro (todos los empleados de esta empresa, activos e inactivos)
    employees = Employee.query.filter_by(company_id=company_id).order_by(Employee.first_name, Employee.last_name).all()
    logging.debug(f"Encontrados {len(employees)} empleados para el filtro en all_records_by_company")
    
    # Si se solicita exportación
    export_format = request.args.get('export')
    if export_format == 'pdf':
        # Obtener todos los registros para exportar (sin paginación)
        all_records = query.order_by(CheckPointRecord.employee_id, CheckPointRecord.check_in_time).all()
        return export_both_records_pdf(all_records, start_date, end_date, company)
    
    return render_template(
        'checkpoints/both_records.html',
        records=records,
        employees=employees,
        company=company,
        company_id=company_id,
        filters={
            'start_date': start_date.strftime('%Y-%m-%d') if isinstance(start_date, date) else None,
            'end_date': end_date.strftime('%Y-%m-%d') if isinstance(end_date, date) else None,
            'employee_id': employee_id
        },
        title=f"Todos los Registros de {company.name if company else ''}"
    )

@checkpoints_bp.route('/company/<slug>/both/export', methods=['GET'])
@login_required
@manager_required
def export_both_records(slug):
    """Exporta todos los registros (con y sin hora de salida) a PDF"""
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Obtener parámetros de filtro
    start_date = request.args.get('start_date')
    end_date = request.args.get('end_date')
    employee_id = request.args.get('employee_id', type=int)
    adjustment_interval = request.args.get('adjustment_interval', type=int)
    round_to_minutes = request.args.get('round_to_minutes', type=int)
    
    # Construir consulta base para todos los registros
    query = db.session.query(
        CheckPointRecord, 
        Employee
    ).join(
        Employee,
        CheckPointRecord.employee_id == Employee.id
    ).filter(
        Employee.company_id == company_id
    )
    
    # Aplicar filtros
    if start_date:
        try:
            start_date = datetime.strptime(start_date, '%Y-%m-%d').date()
            query = query.filter(func.date(CheckPointRecord.check_in_time) >= start_date)
        except ValueError:
            pass
    
    if end_date:
        try:
            end_date = datetime.strptime(end_date, '%Y-%m-%d').date()
            query = query.filter(func.date(CheckPointRecord.check_in_time) <= end_date)
        except ValueError:
            pass
    
    if employee_id:
        query = query.filter(Employee.id == employee_id)
    
    # Ejecutar consulta
    filtered_records = query.order_by(Employee.last_name, Employee.first_name, CheckPointRecord.check_in_time).all()
    
    if not filtered_records:
        flash('No se encontraron registros para los filtros seleccionados.', 'warning')
        return redirect(url_for('checkpoints_slug.view_both_records', slug=slug))
    
    # Generar PDF con los registros filtrados
    return export_both_records_pdf(filtered_records, start_date, end_date, company, adjustment_interval, round_to_minutes)

def adjust_entry_time(original_time, adjustment_interval, round_to_minutes=5):
    """
    Ajusta la hora de entrada al siguiente múltiplo especificado si está dentro del intervalo de tolerancia.
    Ejemplo: 19:57 con tolerancia de 4+ minutos y redondeo a 5 se ajusta a 20:00
    """
    if not original_time or not adjustment_interval or not round_to_minutes:
        return original_time
    
    # Obtener minutos actuales
    current_minutes = original_time.minute
    current_hour = original_time.hour
    
    # Calcular el siguiente múltiplo de round_to_minutes
    next_multiple = ((current_minutes // round_to_minutes) + 1) * round_to_minutes
    
    # Si el próximo múltiplo excede 60 minutos, ir a la siguiente hora
    if next_multiple >= 60:
        target_minutes = next_multiple - 60
        target_hour = current_hour + 1
        if target_hour >= 24:
            target_hour = 0
    else:
        target_minutes = next_multiple
        target_hour = current_hour
    
    # Calcular diferencia en minutos
    if target_hour > current_hour:
        # Cambio de hora
        difference = (60 - current_minutes) + target_minutes
    else:
        # Misma hora
        difference = target_minutes - current_minutes
    
    # Si la diferencia es menor o igual al intervalo de tolerancia, ajustar
    if difference <= adjustment_interval:
        adjusted_time = original_time.replace(hour=target_hour, minute=target_minutes, second=0, microsecond=0)
        return adjusted_time
    
    return original_time

def export_both_records_pdf(records, start_date=None, end_date=None, company=None, adjustment_interval=None, round_to_minutes=None):
    """Genera un PDF con todos los registros agrupados por empleado"""
    from fpdf import FPDF
    from tempfile import NamedTemporaryFile
    
    class PDF(FPDF):
        def header(self):
            # Logo (si existe) y título
            self.set_font('Arial', 'B', 12)
            self.cell(0, 10, f'Registros de {company.name if company else ""}', 0, 1, 'C')
            
            # Agregar fecha y filtros
            self.set_font('Arial', '', 10)
            filter_text = f"Filtro: "
            if start_date and end_date:
                filter_text += f"Del {start_date.strftime('%d/%m/%Y')} al {end_date.strftime('%d/%m/%Y')}"
            elif start_date:
                filter_text += f"Desde {start_date.strftime('%d/%m/%Y')}"
            elif end_date:
                filter_text += f"Hasta {end_date.strftime('%d/%m/%Y')}"
            else:
                filter_text += "Todos los registros"
                
            self.cell(0, 10, filter_text, 0, 1, 'C')
            
            # Agregar información de ajuste si se aplicó
            if adjustment_interval and round_to_minutes:
                self.set_font('Arial', 'I', 9)
                self.cell(0, 8, f"AJUSTE APLICADO: Tolerancia {adjustment_interval} min - Redondeo a múltiplos de {round_to_minutes} min", 0, 1, 'C')
            
            self.ln(5)
        
        def footer(self):
            # Pie de página
            self.set_y(-15)
            self.set_font('Arial', 'I', 8)
            self.cell(0, 10, f'Página {self.page_no()}/{{nb}}', 0, 0, 'C')
            self.cell(0, 10, f'Generado el {datetime.now().strftime("%d/%m/%Y %H:%M")}', 0, 0, 'R')
    
    # Crear PDF
    pdf = PDF()
    pdf.alias_nb_pages()
    pdf.add_page()
    pdf.set_font('Arial', '', 10)
    
    # Agrupar registros por empleado
    employees_records = {}
    for record, employee in records:
        if employee.id not in employees_records:
            employees_records[employee.id] = {
                'employee': employee,
                'records': []
            }
        employees_records[employee.id]['records'].append(record)
    
    # Iterar sobre cada empleado
    for employee_id, data in employees_records.items():
        employee = data['employee']
        employee_records = data['records']
        
        # Encabezado del empleado
        pdf.set_font('Arial', 'B', 11)
        pdf.cell(0, 10, f"{employee.first_name} {employee.last_name} - {employee.dni}", 0, 1)
        
        # Encabezados de columnas
        pdf.set_font('Arial', 'B', 9)
        pdf.cell(30, 7, 'Fecha', 1)
        pdf.cell(25, 7, 'Entrada', 1)
        pdf.cell(25, 7, 'Salida', 1)
        pdf.cell(20, 7, 'Horas', 1)
        pdf.cell(35, 7, 'Punto de Fichaje', 1)
        pdf.cell(55, 7, 'Estado', 1)
        pdf.ln()
        
        # Filas de registros
        pdf.set_font('Arial', '', 9)
        
        for record in employee_records:
            # Fecha
            pdf.cell(30, 7, record.check_in_time.strftime('%d/%m/%Y') if record.check_in_time else '-', 1)
            
            # Hora entrada (aplicar ajuste si se especifica)
            if record.check_in_time:
                adjusted_time = adjust_entry_time(record.check_in_time, adjustment_interval, round_to_minutes)
                display_time = adjusted_time.strftime('%H:%M:%S')
                # Si se aplicó ajuste, mostrar también la hora original
                if adjustment_interval and round_to_minutes and adjusted_time != record.check_in_time:
                    display_time += f" (Orig: {record.check_in_time.strftime('%H:%M')})"
                pdf.cell(25, 7, display_time, 1)
            else:
                pdf.cell(25, 7, '-', 1)
            
            # Hora salida
            if record.check_out_time:
                pdf.cell(25, 7, record.check_out_time.strftime('%H:%M:%S'), 1)
            else:
                pdf.set_text_color(255, 0, 0)  # Rojo para destacar
                pdf.cell(25, 7, 'SIN SALIDA', 1)
                pdf.set_text_color(0, 0, 0)  # Restaurar color
            
            # Horas
            if record.check_out_time:
                pdf.cell(20, 7, f"{record.duration():.2f} h", 1)
            else:
                pdf.cell(20, 7, '-', 1)
            
            # Punto de fichaje
            checkpoint = CheckPoint.query.get(record.checkpoint_id)
            pdf.cell(35, 7, checkpoint.name if checkpoint else '-', 1)
            
            # Estado
            if record.adjustment_reason and 'AUTO-CLOSE' in record.adjustment_reason:
                pdf.set_text_color(255, 128, 0)  # Naranja para cierre automático
                pdf.cell(55, 7, 'Cierre automático por fin de jornada', 1)
                pdf.set_text_color(0, 0, 0)  # Restaurar color
            elif record.adjusted:
                pdf.set_text_color(0, 0, 255)  # Azul para modificado
                pdf.cell(55, 7, 'Registro modificado manualmente', 1)
                pdf.set_text_color(0, 0, 0)  # Restaurar color
            else:
                pdf.cell(55, 7, 'Original', 1)
            
            pdf.ln()
        
        # Total de horas para este empleado (con ajuste si se especifica)
        total_hours = 0
        for record in employee_records:
            if record.check_out_time:
                # Calcular duración con hora ajustada si se especifica
                if adjustment_interval and round_to_minutes:
                    adjusted_check_in = adjust_entry_time(record.check_in_time, adjustment_interval, round_to_minutes)
                    duration_minutes = (record.check_out_time - adjusted_check_in).total_seconds() / 60
                    total_hours += duration_minutes * 0.60 / 60  # Aplicar la fórmula especial
                else:
                    total_hours += record.duration()
        
        pdf.set_font('Arial', 'B', 9)
        if adjustment_interval and round_to_minutes:
            pdf.cell(80, 7, f"H. Totales: {total_hours:.2f} h (con ajuste)", 1, 1, 'R')
        else:
            pdf.cell(80, 7, f"Total de horas: {total_hours:.2f} h", 1, 1, 'R')
        
        # Espacio entre empleados
        pdf.ln(5)
    
    # Generar archivo PDF en memoria
    pdf_file = NamedTemporaryFile(delete=False)
    pdf.output(pdf_file.name)
    pdf_file.close()
    
    # Devolver archivo
    filename = f"todos_registros_{company.name.replace(' ', '_') if company else 'empresa'}.pdf"
    return send_file(
        pdf_file.name,
        as_attachment=True,
        download_name=filename,
        mimetype='application/pdf'
    )
    for i, (original, record, employee) in enumerate(records[:5]):  # Mostrar solo los primeros 5 para no saturar los logs
        logging.debug(f"Registro {i+1}: Empleado={employee.first_name} {employee.last_name}, " +
                     f"Entrada Original={original.original_check_in_time}, " +
                     f"Salida Original={original.original_check_out_time}")
    
    # Crear un archivo temporal para guardar el PDF
    pdf_file = NamedTemporaryFile(delete=False, suffix='.pdf')
    pdf_file.close()
    
    # Función auxiliar para obtener el lunes de la semana de una fecha
    def get_week_start(date_obj):
        """Retorna la fecha del lunes de la semana a la que pertenece date_obj"""
        # weekday() retorna 0 para lunes, 6 para domingo
        days_to_subtract = date_obj.weekday()
        return date_obj - timedelta(days=days_to_subtract)
    
    # Función auxiliar para obtener el domingo de la semana de una fecha
    def get_week_end(date_obj):
        """Retorna la fecha del domingo de la semana a la que pertenece date_obj"""
        # weekday() retorna 0 para lunes, 6 para domingo
        days_to_add = 6 - date_obj.weekday()
        return date_obj + timedelta(days=days_to_add)
    
    # Crear un PDF personalizado
    class OriginalRecordsPDF(FPDF):
        def header(self):
            # Logo y título
            self.set_font('Arial', 'B', 15)
            title = 'Registro de Ajustes de Fichajes'
            if company:
                title = f'Registro de Ajustes de Fichajes - {company.name}'
            self.cell(0, 10, title, 0, 1, 'C')
            
            # Período
            if start_date and end_date:
                period = f"{start_date.strftime('%d/%m/%Y')} - {end_date.strftime('%d/%m/%Y')}"
            elif start_date:
                period = f"Desde {start_date.strftime('%d/%m/%Y')}"
            elif end_date:
                period = f"Hasta {end_date.strftime('%d/%m/%Y')}"
            else:
                period = "Todos los registros"
                
            self.set_font('Arial', '', 10)
            self.cell(0, 10, f'Período: {period}', 0, 1, 'C')
            
            # Encabezados de la tabla
            self.set_fill_color(200, 220, 255)
            self.set_font('Arial', 'B', 8)
            self.cell(40, 7, 'Empleado', 1, 0, 'C', True)
            self.cell(20, 7, 'Fecha', 1, 0, 'C', True)
            self.cell(15, 7, 'Ent. Orig.', 1, 0, 'C', True)
            self.cell(15, 7, 'Sal. Orig.', 1, 0, 'C', True)
            self.cell(15, 7, 'Ent. Mod.', 1, 0, 'C', True)
            self.cell(15, 7, 'Sal. Mod.', 1, 0, 'C', True)
            self.cell(20, 7, 'Ajustado Por', 1, 0, 'C', True)
            self.cell(50, 7, 'Motivo', 1, 1, 'C', True)
            
        def footer(self):
            # Posición a 1.5 cm del final
            self.set_y(-15)
            # Número de página
            self.set_font('Arial', 'I', 8)
            self.cell(0, 10, f'Página {self.page_no()}/{{nb}}', 0, 0, 'C')
    
    try:
        # Crear PDF
        pdf = OriginalRecordsPDF()
        pdf.alias_nb_pages()
        pdf.add_page()
        pdf.set_auto_page_break(auto=True, margin=15)
        
        # Llenar el PDF con los datos
        pdf.set_font('Arial', '', 8)
        
        # Verificar si hay registros
        if not records:
            pdf.set_font('Arial', 'B', 12)
            pdf.ln(10)
            pdf.cell(0, 10, "No se encontraron registros para los filtros seleccionados", 0, 1, 'C')
            pdf.output(pdf_file.name)
            return send_file(
                pdf_file.name,
                as_attachment=True,
                download_name="registros_originales.pdf",
                mimetype='application/pdf'
            )
        
        # Ordenar registros por empleado, fecha y hora
        sorted_records = sorted(records, key=lambda x: (
            x[2].id,  # employee.id
            x[0].original_check_in_time.date() if x[0].original_check_in_time else datetime.min.date(),  # date
            x[0].original_check_in_time.time() if x[0].original_check_in_time else datetime.min.time()  # time
        ))
        
        # Estructurar registros por empleado
        employee_records = {}
        for original, record, employee in sorted_records:
            # Verificar que los objetos no sean None
            if not original or not record or not employee:
                logging.warning(f"Skipping record with None values: original={original}, record={record}, employee={employee}")
                continue
                
            if employee.id not in employee_records:
                employee_records[employee.id] = {
                    'employee': employee,
                    'weeks': {}
                }
            
            # Validar que original_check_in_time no sea None
            if not original.original_check_in_time:
                logging.warning(f"Skipping record with None original_check_in_time for employee {employee.first_name} {employee.last_name}")
                continue
                
            # Obtener fecha de inicio de la semana (lunes)
            week_start = get_week_start(original.original_check_in_time.date())
            week_end = get_week_end(original.original_check_in_time.date())
            week_key = week_start.strftime('%Y-%m-%d')
            
            if week_key not in employee_records[employee.id]['weeks']:
                employee_records[employee.id]['weeks'][week_key] = {
                    'start_date': week_start,
                    'end_date': week_end,
                    'records': [],
                    'original_hours': 0,
                    'adjusted_hours': 0
                }
            
            try:
                # Calcular horas trabajadas
                hours_original = 0
                if original.original_check_out_time and original.original_check_in_time:
                    # Usando la función duration ya modificada para manejar correctamente los turnos nocturnos
                    hours_original = original.duration()
                    employee_records[employee.id]['weeks'][week_key]['original_hours'] += hours_original
                
                hours_adjusted = 0
                if record.check_out_time and record.check_in_time:
                    # Usando la función duration ya modificada para manejar correctamente los turnos nocturnos
                    hours_adjusted = record.duration()
                    employee_records[employee.id]['weeks'][week_key]['adjusted_hours'] += hours_adjusted
                
                # Guardar registro
                employee_records[employee.id]['weeks'][week_key]['records'].append((original, record, hours_original, hours_adjusted))
            except Exception as e:
                logging.error(f"Error al procesar registro: {str(e)}")
                # Continuar con el siguiente registro
                continue
        
        # Verificar si hay datos para mostrar después de filtrar
        if not employee_records:
            pdf.set_font('Arial', 'B', 12)
            pdf.ln(10)
            pdf.cell(0, 10, "No se encontraron registros válidos para los filtros seleccionados", 0, 1, 'C')
            pdf.output(pdf_file.name)
            return send_file(
                pdf_file.name,
                as_attachment=True,
                download_name="registros_originales.pdf",
                mimetype='application/pdf'
            )
        
        # Generar PDF con los datos estructurados
        for emp_id, emp_data in employee_records.items():
            employee = emp_data['employee']
            employee_name = f"{employee.first_name} {employee.last_name}"
            
            # Imprimir nombre del empleado como encabezado
            pdf.set_font('Arial', 'B', 12)
            pdf.ln(5)
            pdf.cell(0, 10, f"Empleado: {employee_name}", 0, 1, 'L')
            pdf.set_font('Arial', '', 8)
            
            employee_total_original = 0
            employee_total_adjusted = 0
            
            # Verificar si hay semanas para este empleado
            if not emp_data['weeks']:
                pdf.set_font('Arial', 'I', 10)
                pdf.cell(0, 7, "No hay registros para este empleado en el período seleccionado", 0, 1, 'L')
                continue
            
            # Procesar cada semana del empleado
            for week_key in sorted(emp_data['weeks'].keys()):
                week_data = emp_data['weeks'][week_key]
                
                # Imprimir encabezado de la semana
                week_header = f"Semana: {week_data['start_date'].strftime('%d/%m/%Y')} - {week_data['end_date'].strftime('%d/%m/%Y')}"
                pdf.set_font('Arial', 'B', 10)
                pdf.ln(3)
                pdf.cell(0, 7, week_header, 0, 1, 'L')
                pdf.set_font('Arial', '', 8)
                
                # Imprimir registros de la semana
                for original, record, hours_original, hours_adjusted in week_data['records']:
                    try:
                        if not original.original_check_in_time:
                            continue
                            
                        date_str = original.original_check_in_time.strftime('%d/%m/%Y')
                        
                        in_time_orig = original.original_check_in_time.strftime('%H:%M')
                        out_time_orig = original.original_check_out_time.strftime('%H:%M') if original.original_check_out_time else '-'
                        
                        # Verificar si es un cierre automático por olvido
                        is_auto_close = record.notes and "[Cerrado automáticamente por fin de horario de funcionamiento]" in record.notes
                        
                        in_time_mod = record.check_in_time.strftime('%H:%M') if record.check_in_time else '-'
                        
                        # Si fue cerrado automáticamente, mostrar "SIN SALIDA" en vez de la hora
                        if is_auto_close:
                            out_time_mod = "SIN SALIDA"
                        elif record.check_out_time:
                            out_time_mod = record.check_out_time.strftime('%H:%M')
                        else:
                            out_time_mod = '-'
                        
                        adjusted_by = original.adjusted_by.username if original.adjusted_by and hasattr(original.adjusted_by, 'username') else 'Sistema'
                        
                        # Imprimir fila
                        # En lugar de dejar en blanco el nombre del empleado, pon el nombre completo
                        pdf.cell(40, 7, employee_name, 1, 0, 'L')
                        pdf.cell(20, 7, date_str, 1, 0, 'C')
                        pdf.cell(15, 7, in_time_orig, 1, 0, 'C')
                        pdf.cell(15, 7, out_time_orig, 1, 0, 'C')
                        pdf.cell(15, 7, in_time_mod, 1, 0, 'C')
                        pdf.cell(15, 7, out_time_mod, 1, 0, 'C')
                        pdf.cell(20, 7, adjusted_by, 1, 0, 'C')
                        
                        # Ajustar motivo para que quepa en una fila
                        motivo = original.adjustment_reason if hasattr(original, 'adjustment_reason') else ''
                        if motivo and len(motivo) > 30:
                            motivo = motivo[:27] + '...'
                        pdf.cell(50, 7, motivo or '', 1, 1, 'L')
                    except Exception as e:
                        logging.error(f"Error al imprimir registro: {str(e)}")
                        # Continuar con el siguiente registro
                        continue
                
                # Imprimir totales de la semana
                pdf.set_font('Arial', 'B', 8)
                pdf.set_fill_color(230, 230, 230)
                pdf.cell(90, 7, f'Total semana (horas originales): {week_data["original_hours"]:.2f}', 1, 0, 'R', True)
                pdf.cell(90, 7, f'Total semana (horas ajustadas): {week_data["adjusted_hours"]:.2f}', 1, 1, 'R', True)
                pdf.ln(5)
                
                # Acumular totales del empleado
                employee_total_original += week_data['original_hours']
                employee_total_adjusted += week_data['adjusted_hours']
            
            # Imprimir totales del empleado
            pdf.set_font('Arial', 'B', 10)
            pdf.set_fill_color(200, 220, 255)
            pdf.cell(90, 8, f'TOTAL EMPLEADO (horas originales): {employee_total_original:.2f}', 1, 0, 'R', True)
            pdf.cell(90, 8, f'TOTAL EMPLEADO (horas ajustadas): {employee_total_adjusted:.2f}', 1, 1, 'R', True)
            
            # Nueva página para cada empleado, excepto el último
            if list(employee_records.keys()).index(emp_id) < len(employee_records) - 1:
                pdf.add_page()
        
        # Guardar PDF
        pdf.output(pdf_file.name)
        
        # Enviar el archivo
        filename = f"registros_originales.pdf"
        return send_file(
            pdf_file.name,
            as_attachment=True,
            download_name=filename,
            mimetype='application/pdf'
        )
    except Exception as e:
        logging.error(f"Error al generar PDF: {str(e)}")
        # Intentar crear un PDF de error
        try:
            error_pdf = FPDF()
            error_pdf.add_page()
            error_pdf.set_font('Arial', 'B', 16)
            error_pdf.cell(0, 10, "Error al generar el PDF", 0, 1, 'C')
            error_pdf.set_font('Arial', '', 12)
            error_pdf.cell(0, 10, f"Se produjo un error: {str(e)}", 0, 1, 'L')
            error_pdf.output(pdf_file.name)
            return send_file(
                pdf_file.name,
                as_attachment=True,
                download_name="error_report.pdf",
                mimetype='application/pdf'
            )
        except:
            # Si falla incluso el PDF de error, devuelve un mensaje de error
            return jsonify({'error': f'Error al generar el PDF: {str(e)}'}), 500

# Esta ruta ha sido reemplazada por token_direct_access
    
    # Buscar el token en la base de datos
    access_token = LocationAccessToken.get_token_by_value(token)
    
    if not access_token or access_token.portal_type != PortalType.CHECKPOINTS:
        flash('El enlace de acceso directo no es válido o ha sido desactivado.', 'danger')
        return redirect(url_for('checkpoints_slug.select_company'))
    
    # Obtener el punto de fichaje asociado al token
    checkpoint = access_token.location
    
    if not checkpoint or checkpoint.status != CheckPointStatus.ACTIVE:
        flash('El punto de fichaje asociado a este enlace no está disponible.', 'danger')
        return redirect(url_for('checkpoints_slug.select_company'))
    
    # Actualizar fecha de último uso
    access_token.update_last_used()
    
    # Guardar información necesaria en la sesión y marcar como autenticado
    session['checkpoint_id'] = checkpoint.id
    session['company_id'] = checkpoint.company_id
    session['access_method'] = 'direct_token'
    
    # Registrar actividad
    log_activity(f'Acceso directo mediante token al portal de fichajes: {checkpoint.name}')
    
    # Redirigir directamente al formulario de PIN
    return redirect(url_for('checkpoints_slug.employee_pin'))

# Ruta para acceder directamente a un checkpoint específico por ID
@checkpoints_bp.route('/login/<int:checkpoint_id>', methods=['GET', 'POST'])
def login_to_checkpoint(checkpoint_id):
    """Acceso directo a un punto de fichaje específico por ID"""
    # Si ya hay una sesión activa, redirigir al dashboard
    if 'checkpoint_id' in session:
        return redirect(url_for('checkpoints_slug.checkpoint_dashboard'))
    
    # Buscar el checkpoint por ID
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    
    # Si el checkpoint no está activo, mostrar error
    if checkpoint.status != CheckPointStatus.ACTIVE:
        flash('El punto de fichaje no está activo.', 'danger')
        return redirect(url_for('checkpoints_slug.select_company'))
    
    # Comprobar si venimos de un acceso directo por token
    access_method = session.get('access_method', None)
    
    # Crear el formulario
    form = CheckPointLoginForm()
    
    # Procesar el formulario si es una solicitud POST y es válido
    if form.validate_on_submit():
        # Guardar la información del checkpoint en la sesión
        session['checkpoint_id'] = checkpoint.id
        session['company_id'] = checkpoint.company_id
        
        # Redirigir al formulario de ingreso de PIN
        return redirect(url_for('checkpoints_slug.employee_pin'))
    
    # Si venimos de un acceso directo por token, ocultamos el formulario
    show_credentials = access_method != 'direct_token'
    
    return render_template('checkpoints/login.html', form=form, checkpoint=checkpoint, show_credentials=show_credentials)

# Rutas para gestionar tokens de acceso directo

@checkpoints_bp.route('/checkpoint/<int:id>/access-token/create', methods=['POST'])
@login_required
@manager_required
def create_access_token(id):
    """Crea un token de acceso directo para un punto de fichaje"""
    checkpoint = CheckPoint.query.get_or_404(id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != checkpoint.company_id):
        flash('No tienes permiso para gestionar tokens de acceso para este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints_slug.manage_checkpoints'))
    
    # Crear token para el punto de fichaje
    try:
        token = LocationAccessToken.create_for_location(
            location_id=checkpoint.id, 
            portal_type=PortalType.CHECKPOINTS
        )
        flash('Enlace de acceso directo creado correctamente.', 'success')
        log_activity(f'Creado token de acceso directo para punto de fichaje: {checkpoint.name}')
    except Exception as e:
        flash(f'Error al crear token de acceso: {str(e)}', 'danger')
    
    return redirect(url_for('checkpoints_slug.manage_checkpoints'))

@checkpoints_bp.route('/checkpoint/<int:id>/access-token/regenerate', methods=['POST'])
@login_required
@manager_required
def regenerate_access_token(id):
    """Regenera el token de acceso directo para un punto de fichaje"""
    checkpoint = CheckPoint.query.get_or_404(id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != checkpoint.company_id):
        flash('No tienes permiso para gestionar tokens de acceso para este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints_slug.manage_checkpoints'))
    
    # Buscar token existente
    token = LocationAccessToken.query.filter_by(
        location_id=id, 
        portal_type=PortalType.CHECKPOINTS
    ).first()
    
    if not token:
        flash('No existe un token para este punto de fichaje.', 'warning')
        return redirect(url_for('checkpoints_slug.manage_checkpoints'))
    
    # Regenerar token
    try:
        new_token = token.regenerate()
        flash('Enlace de acceso directo regenerado correctamente.', 'success')
        log_activity(f'Regenerado token de acceso directo para punto de fichaje: {checkpoint.name}')
    except Exception as e:
        flash(f'Error al regenerar token de acceso: {str(e)}', 'danger')
    
    return redirect(url_for('checkpoints_slug.manage_checkpoints'))

@checkpoints_bp.route('/checkpoint/<int:id>/access-token/deactivate', methods=['POST'])
@login_required
@manager_required
def deactivate_access_token(id):
    """Desactiva el token de acceso directo para un punto de fichaje"""
    checkpoint = CheckPoint.query.get_or_404(id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != checkpoint.company_id):
        flash('No tienes permiso para gestionar tokens de acceso para este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints_slug.manage_checkpoints'))
    
    # Buscar token existente
    token = LocationAccessToken.query.filter_by(
        location_id=id, 
        portal_type=PortalType.CHECKPOINTS
    ).first()
    
    if not token:
        flash('No existe un token para este punto de fichaje.', 'warning')
        return redirect(url_for('checkpoints_slug.manage_checkpoints'))
    
    # Desactivar token
    try:
        token.deactivate()
        flash('Enlace de acceso directo desactivado correctamente.', 'success')
        log_activity(f'Desactivado token de acceso directo para punto de fichaje: {checkpoint.name}')
    except Exception as e:
        flash(f'Error al desactivar token de acceso: {str(e)}', 'danger')
    
    return redirect(url_for('checkpoints_slug.manage_checkpoints'))

# Endpoint de API para consultar estado del token
@checkpoints_bp.route('/api/checkpoints/<int:id>/token-status', methods=['GET'])
@login_required
@manager_required
def get_token_status(id):
    """API para consultar el estado del token de acceso directo de un punto de fichaje"""
    # Importar modelos necesarios
    from models_access import LocationAccessToken, PortalType
    
    # Verificar que el punto de fichaje existe
    checkpoint = CheckPoint.query.get_or_404(id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != checkpoint.company_id):
        return jsonify({'error': 'No tienes permiso para consultar tokens de acceso para este punto de fichaje'}), 403
    
    # Buscar token activo
    token = LocationAccessToken.query.filter_by(
        location_id=id,
        is_active=True,
        portal_type=PortalType.CHECKPOINTS
    ).first()
    
    if token:
        return jsonify({
            'has_token': True,
            'token': token.token,
            'created_at': token.created_at.strftime('%d-%m-%Y %H:%M'),
            'last_used_at': token.last_used_at.strftime('%d-%m-%Y %H:%M') if token.last_used_at else None
        })
    else:
        return jsonify({'has_token': False})

# Ruta para acceso directo sin login
@checkpoints_bp.route('/token-access/<token>', methods=['GET'])
def token_direct_access(token):
    """Acceso directo al portal de fichajes mediante token sin necesidad de login"""
    # Verificar que el token existe y está activo
    access_token = LocationAccessToken.query.filter_by(
        token=token, 
        is_active=True,
        portal_type=PortalType.CHECKPOINTS
    ).first()
    
    if not access_token:
        flash('El enlace de acceso directo no es válido o ha caducado.', 'danger')
        return redirect(url_for('checkpoints_slug.login'))
    
    # Obtener el punto de fichaje asociado al token
    checkpoint = CheckPoint.query.get(access_token.location_id)
    
    if not checkpoint or checkpoint.status != CheckPointStatus.ACTIVE:
        flash('El punto de fichaje asociado a este acceso no está disponible.', 'danger')
        return redirect(url_for('checkpoints_slug.login'))
    
    # Actualizar última fecha de uso
    access_token.update_last_used()
    
    # Establecer la sesión como si hubiera iniciado sesión normalmente
    session['checkpoint_id'] = checkpoint.id
    session['company_id'] = checkpoint.company_id
    session['access_method'] = 'direct_token'  # Marcamos que vino por acceso directo
    
    # Redirigir directamente a la página de PIN
    return redirect(url_for('checkpoints_slug.employee_pin'))

# Ruta para acceder directamente a un punto de fichaje específico por ID
@checkpoints_bp.route('/checkpoint/<int:checkpoint_id>/direct', methods=['GET', 'POST'])
def direct_login_to_checkpoint(checkpoint_id):
    """Acceso directo a un punto de fichaje específico por ID"""
    # Buscar el punto de fichaje
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    
    # Verificar que el punto de fichaje está activo
    if checkpoint.status != CheckPointStatus.ACTIVE:
        flash('Este punto de fichaje no está disponible actualmente.', 'warning')
        return redirect(url_for('checkpoints_slug.login'))
    
    # Crear el formulario
    form = CheckPointLoginForm()
    
    # Procesar el formulario si es una solicitud POST y es válido
    if form.validate_on_submit():
        # Verificar contraseña
        if check_password_hash(checkpoint.password_hash, form.password.data):
            # Guardar la información del checkpoint en la sesión
            session['checkpoint_id'] = checkpoint.id
            session['company_id'] = checkpoint.company_id
            
            # Redirigir al formulario de ingreso de PIN
            return redirect(url_for('checkpoints_slug.employee_pin'))
        else:
            flash('Contraseña incorrecta. Por favor, inténtelo de nuevo.', 'danger')
    
    return render_template('checkpoints/login.html', form=form, checkpoint=checkpoint)
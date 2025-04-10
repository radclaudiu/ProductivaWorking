import os
import json
import logging
from datetime import datetime, date, time, timedelta
from functools import wraps
from timezone_config import get_current_time, datetime_to_madrid, TIMEZONE

# Configurar logging
logger = logging.getLogger(__name__)

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
from forms_checkpoints import (CheckPointForm, CheckPointLoginForm, CheckPointEmployeePinForm, 
                             ContractHoursForm, CheckPointRecordAdjustmentForm,
                             SignaturePadForm, ExportCheckPointRecordsForm, DeleteCheckPointRecordsForm)
from utils import log_activity
from utils_checkpoints import generate_pdf_report, generate_simple_pdf_report, draw_signature, delete_employee_records


# Crear un Blueprint para las rutas de checkpoints
checkpoints_bp = Blueprint('checkpoints', __name__, url_prefix='/fichajes')


# Decoradores personalizados
def admin_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or not current_user.is_admin():
            flash('Acceso denegado. Se requieren permisos de administrador.', 'danger')
            return redirect(url_for('index'))
        return f(*args, **kwargs)
    return decorated_function


def manager_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or (
            not current_user.is_admin() and 
            not current_user.is_gerente()
        ):
            flash('Acceso denegado. Se requieren permisos de gerente o administrador.', 'danger')
            return redirect(url_for('index'))
        return f(*args, **kwargs)
    return decorated_function


def checkpoint_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'checkpoint_id' not in session:
            flash('Debe iniciar sesión como punto de fichaje.', 'warning')
            return redirect(url_for('checkpoints.login'))
        return f(*args, **kwargs)
    return decorated_function


# Rutas para administración de checkpoints
@checkpoints_bp.route('/')
@login_required
def select_company():
    """Página de selección de empresa para el sistema de fichajes"""
    try:
        # Obtener empresas según los permisos del usuario
        if current_user.is_admin():
            companies = Company.query.filter_by(is_active=True).all()
        else:
            companies = current_user.companies
        
        # Si solo hay una empresa o el usuario solo gestiona una, redirigir directamente
        if len(companies) == 1:
            return redirect(url_for('checkpoints.index_company', slug=companies[0].get_slug()))
        
        return render_template('checkpoints/select_company.html', companies=companies)
    except Exception as e:
        current_app.logger.error(f"Error en select_company: {e}")
        flash("Error al cargar la selección de empresas. Por favor, inténtelo de nuevo.", "danger")
        return redirect(url_for('main.dashboard'))

@checkpoints_bp.route('/company/<string:slug>')
@login_required
def index_company(slug):
    """Página principal del sistema de fichajes para una empresa específica"""
    try:
        # Usar approach más robusto para buscar empresas por slug
        from utils import slugify
        
        # Buscar por ID si es un número
        if slug.isdigit():
            company = Company.query.get_or_404(int(slug))
        else:
            # Buscar todas las empresas y comparar slugs
            all_companies = Company.query.all()
            company = next((c for c in all_companies if slugify(c.name) == slug), None)
            
            if not company:
                flash('Empresa no encontrada', 'danger')
                return redirect(url_for('checkpoints.select_company'))
        
        if not current_user.is_admin() and company not in current_user.companies:
            flash('No tiene permiso para gestionar esta empresa.', 'danger')
            return redirect(url_for('main.dashboard'))
        
        # Guardar la empresa seleccionada en la sesión
        session['selected_company_id'] = company.id
        
        # Inicializar estadísticas con valores por defecto
        stats = {
            'active_checkpoints': 0,
            'maintenance_checkpoints': 0,
            'disabled_checkpoints': 0,
            'employees_with_hours': 0,
            'employees_with_overtime': 0,
            'today_records': 0,
            'pending_checkout': 0,
            'active_incidents': 0
        }
        
        # Obtener estadísticas para el dashboard en bloques separados con manejo de excepciones
        try:
            stats['active_checkpoints'] = CheckPoint.query.filter_by(
                status=CheckPointStatus.ACTIVE, company_id=company.id).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener checkpoints activos: {e}")
            
        try:
            stats['maintenance_checkpoints'] = CheckPoint.query.filter_by(
                status=CheckPointStatus.MAINTENANCE, company_id=company.id).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener checkpoints en mantenimiento: {e}")
            
        try:
            stats['disabled_checkpoints'] = CheckPoint.query.filter_by(
                status=CheckPointStatus.DISABLED, company_id=company.id).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener checkpoints deshabilitados: {e}")
            
        try:
            stats['employees_with_hours'] = db.session.query(EmployeeContractHours)\
                .join(Employee, EmployeeContractHours.employee_id == Employee.id)\
                .filter(Employee.company_id == company.id).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener empleados con horas: {e}")
            
        try:
            stats['employees_with_overtime'] = db.session.query(EmployeeContractHours)\
                .join(Employee, EmployeeContractHours.employee_id == Employee.id)\
                .filter(Employee.company_id == company.id, 
                      EmployeeContractHours.allow_overtime == True).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener empleados con horas extra: {e}")
            
        try:
            stats['today_records'] = db.session.query(CheckPointRecord)\
                .join(CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id)\
                .filter(
                    CheckPoint.company_id == company.id,
                    CheckPointRecord.check_in_time >= datetime.combine(date.today(), time.min)
                ).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener registros de hoy: {e}")
            
        try:
            stats['pending_checkout'] = db.session.query(CheckPointRecord)\
                .join(CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id)\
                .filter(
                    CheckPoint.company_id == company.id,
                    CheckPointRecord.check_out_time.is_(None)
                ).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener registros pendientes de salida: {e}")
            
        try:
            stats['active_incidents'] = db.session.query(CheckPointIncident)\
                .join(CheckPointRecord, CheckPointIncident.record_id == CheckPointRecord.id)\
                .join(CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id)\
                .filter(
                    CheckPoint.company_id == company.id,
                    CheckPointIncident.resolved == False
                ).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener incidencias activas: {e}")
        
        # Inicializar variables con valores vacíos para evitar errores
        latest_records = []
        latest_incidents = []
        checkpoints = []
        employees = []
        week_stats = {}
        
        # Obtener los últimos registros filtrados por empresa
        try:
            latest_records = db.session.query(CheckPointRecord)\
                .join(CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id)\
                .filter(CheckPoint.company_id == company.id)\
                .order_by(CheckPointRecord.check_in_time.desc())\
                .limit(10).all()
        except Exception as e:
            current_app.logger.error(f"Error al obtener últimos registros: {e}")
        
        # Obtener las últimas incidencias filtradas por empresa
        try:
            latest_incidents = db.session.query(CheckPointIncident)\
                .join(CheckPointRecord, CheckPointIncident.record_id == CheckPointRecord.id)\
                .join(CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id)\
                .filter(CheckPoint.company_id == company.id)\
                .order_by(CheckPointIncident.created_at.desc())\
                .limit(10).all()
        except Exception as e:
            current_app.logger.error(f"Error al obtener últimas incidencias: {e}")
        
        # Obtener todos los puntos de fichaje para la empresa
        try:
            checkpoints = CheckPoint.query.filter_by(company_id=company.id).all()
        except Exception as e:
            current_app.logger.error(f"Error al obtener puntos de fichaje: {e}")
        
        # Obtener los empleados activos para la empresa (sin filtrar por is_active para evitar problemas)
        try:
            employees = Employee.query.filter_by(company_id=company.id).order_by(Employee.first_name).all()
        except Exception as e:
            current_app.logger.error(f"Error al obtener empleados: {e}")
        
        # Obtener los tipos de incidencias para el filtrado
        incident_types = []
        try:
            incident_types = [
                {'value': incident_type.value, 'name': incident_type.name}
                for incident_type in CheckPointIncidentType
            ]
        except Exception as e:
            current_app.logger.error(f"Error al obtener tipos de incidencias: {e}")
        
        # Obtener las estadísticas de la última semana para gráficos
        try:
            week_stats = {}
            today = date.today()
            for i in range(7):
                try:
                    day = today - timedelta(days=i)
                    count = db.session.query(CheckPointRecord)\
                        .join(CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id)\
                        .filter(
                            CheckPoint.company_id == company.id,
                            extract('day', CheckPointRecord.check_in_time) == day.day,
                            extract('month', CheckPointRecord.check_in_time) == day.month,
                            extract('year', CheckPointRecord.check_in_time) == day.year
                        ).count()
                    week_stats[day.strftime('%d/%m')] = count
                except Exception as e:
                    current_app.logger.error(f"Error al obtener estadísticas del día {day}: {e}")
                    week_stats[day.strftime('%d/%m')] = 0
        except Exception as e:
            current_app.logger.error(f"Error al obtener estadísticas semanales: {e}")
        
        return render_template('checkpoints/index.html', 
                              stats=stats, 
                              latest_records=latest_records,
                              latest_incidents=latest_incidents,
                              company=company,
                              checkpoints=checkpoints,
                              employees=employees,
                              incident_types=incident_types,
                              week_stats=week_stats)
                              
    except Exception as e:
        current_app.logger.error(f"Error general en index_company: {e}")
        flash('Se produjo un error al cargar la página. Por favor, inténtelo de nuevo.', 'danger')
        return redirect(url_for('main.dashboard'))


@checkpoints_bp.route('/checkpoints')
@login_required
@manager_required
def list_checkpoints():
    """Lista todos los puntos de fichaje disponibles"""
    try:
        # Verificar si hay una empresa seleccionada
        company_id = session.get('selected_company_id')
        if not company_id:
            return redirect(url_for('checkpoints.select_company'))
        
        # Verificar permiso para acceder a esta empresa
        company = Company.query.get_or_404(company_id)
        if not current_user.is_admin() and company not in current_user.companies:
            flash('No tiene permiso para gestionar esta empresa.', 'danger')
            return redirect(url_for('checkpoints.select_company'))
        
        # Filtrar por la empresa seleccionada
        try:
            checkpoints = CheckPoint.query.filter_by(company_id=company_id).all()
        except Exception as e:
            current_app.logger.error(f"Error al obtener puntos de fichaje: {e}")
            checkpoints = []
            flash("Hubo un problema al cargar los puntos de fichaje. Se muestra una lista parcial.", "warning")
        
        return render_template('checkpoints/list_checkpoints.html', checkpoints=checkpoints, company=company)
    except Exception as e:
        current_app.logger.error(f"Error general en list_checkpoints: {e}")
        flash('Se produjo un error al cargar la página. Por favor, inténtelo de nuevo.', 'danger')
        return redirect(url_for('main.dashboard'))


@checkpoints_bp.route('/checkpoints/new', methods=['GET', 'POST'])
@login_required
@manager_required
def create_checkpoint():
    """Crea un nuevo punto de fichaje"""
    # Verificar si hay una empresa seleccionada
    company_id = session.get('selected_company_id')
    if not company_id:
        return redirect(url_for('checkpoints.select_company'))
    
    # Verificar permiso para acceder a esta empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene permiso para gestionar esta empresa.', 'danger')
        return redirect(url_for('checkpoints.select_company'))
    
    form = CheckPointForm()
    
    # Cargar las empresas disponibles para el usuario actual pero preseleccionar la empresa actual
    if current_user.is_admin():
        companies = Company.query.filter_by(is_active=True).all()
    else:
        companies = current_user.companies
        
    form.company_id.choices = [(c.id, c.name) for c in companies]
    
    # Si es GET, preseleccionamos la empresa de la sesión
    if request.method == 'GET':
        form.company_id.data = company_id
    
    if form.validate_on_submit():
        # Convertir el string del status a un objeto de enumeración
        status_value = form.status.data
        status_enum = CheckPointStatus.ACTIVE  # Default
        for status in CheckPointStatus:
            if status.value == status_value:
                status_enum = status
                break
                
        checkpoint = CheckPoint(
            name=form.name.data,
            description=form.description.data,
            location=form.location.data,
            status=status_enum,
            username=form.username.data,
            company_id=form.company_id.data,
            enforce_contract_hours=form.enforce_contract_hours.data,
            auto_adjust_overtime=form.auto_adjust_overtime.data,
            # Nuevos campos para configuración de horario de funcionamiento
            enforce_operation_hours=form.enforce_operation_hours.data,
            operation_start_time=form.operation_start_time.data,
            operation_end_time=form.operation_end_time.data
        )
        
        if form.password.data:
            checkpoint.set_password(form.password.data)
        
        db.session.add(checkpoint)
        
        try:
            db.session.commit()
            flash(f'Punto de fichaje "{checkpoint.name}" creado con éxito.', 'success')
            return redirect(url_for('checkpoints.list_checkpoints'))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al crear el punto de fichaje: {str(e)}', 'danger')
    
    return render_template('checkpoints/checkpoint_form.html', form=form, title='Nuevo Punto de Fichaje', company=company)


@checkpoints_bp.route('/checkpoints/<int:id>/edit', methods=['GET', 'POST'])
@login_required
@manager_required
def edit_checkpoint(id):
    """Edita un punto de fichaje existente"""
    checkpoint = CheckPoint.query.get_or_404(id)
    
    # Verificar permiso (solo admin o gerente de la empresa)
    if not current_user.is_admin() and checkpoint.company_id not in [c.id for c in current_user.companies]:
        flash('No tiene permiso para editar este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints.list_checkpoints'))
    
    form = CheckPointForm(obj=checkpoint)
    
    # Cargar las empresas disponibles para el usuario actual
    if current_user.is_admin():
        companies = Company.query.filter_by(is_active=True).all()
    else:
        companies = current_user.companies
        
    form.company_id.choices = [(c.id, c.name) for c in companies]
    
    if form.validate_on_submit():
        # Convertir el string del status a un objeto de enumeración
        status_value = form.status.data
        status_enum = CheckPointStatus.ACTIVE  # Default
        for status in CheckPointStatus:
            if status.value == status_value:
                status_enum = status
                break
                
        checkpoint.name = form.name.data
        checkpoint.description = form.description.data
        checkpoint.location = form.location.data
        checkpoint.status = status_enum
        checkpoint.username = form.username.data
        checkpoint.company_id = form.company_id.data
        checkpoint.enforce_contract_hours = form.enforce_contract_hours.data
        checkpoint.auto_adjust_overtime = form.auto_adjust_overtime.data
        # Actualizar configuración de horario de funcionamiento
        checkpoint.enforce_operation_hours = form.enforce_operation_hours.data
        checkpoint.operation_start_time = form.operation_start_time.data
        checkpoint.operation_end_time = form.operation_end_time.data
        
        if form.password.data:
            checkpoint.set_password(form.password.data)
        
        try:
            db.session.commit()
            flash(f'Punto de fichaje "{checkpoint.name}" actualizado con éxito.', 'success')
            return redirect(url_for('checkpoints.list_checkpoints'))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al actualizar el punto de fichaje: {str(e)}', 'danger')
    
    return render_template('checkpoints/checkpoint_form.html', form=form, checkpoint=checkpoint, 
                          title='Editar Punto de Fichaje')


@checkpoints_bp.route('/checkpoints/<int:id>/delete', methods=['POST'])
@login_required
@manager_required
def delete_checkpoint(id):
    """Elimina un punto de fichaje con todos sus registros asociados"""
    try:
        checkpoint = CheckPoint.query.get_or_404(id)
        
        # Verificar permiso (solo admin o gerente de la empresa)
        if not current_user.is_admin() and checkpoint.company_id not in [c.id for c in current_user.companies]:
            flash('No tiene permiso para eliminar este punto de fichaje.', 'danger')
            return redirect(url_for('checkpoints.list_checkpoints'))
        
        # Contar registros asociados para el mensaje informativo
        records_count = CheckPointRecord.query.filter_by(checkpoint_id=id).count()
        
        # Guardar el nombre para el mensaje posterior
        checkpoint_name = checkpoint.name
        
        # Iniciar transacción para eliminar todo en cascada
        try:
            # 1. Primero, obtener todos los registros asociados al checkpoint
            checkpoint_records = CheckPointRecord.query.filter_by(checkpoint_id=id).all()
            
            for record in checkpoint_records:
                # 2. Eliminar las incidencias asociadas a cada registro
                CheckPointIncident.query.filter_by(record_id=record.id).delete()
                
                # 3. Eliminar los registros originales (históricos) de cada registro
                CheckPointOriginalRecord.query.filter_by(record_id=record.id).delete()
            
            # 4. Eliminar todos los registros del checkpoint
            CheckPointRecord.query.filter_by(checkpoint_id=id).delete()
            
            # 5. Finalmente eliminar el checkpoint
            db.session.delete(checkpoint)
            
            # Confirmar todos los cambios
            db.session.commit()
            
            if records_count > 0:
                flash(f'Punto de fichaje "{checkpoint_name}" eliminado con éxito junto con {records_count} registros asociados.', 'success')
            else:
                flash(f'Punto de fichaje "{checkpoint_name}" eliminado con éxito.', 'success')
                
        except Exception as e:
            db.session.rollback()
            current_app.logger.error(f"Error al eliminar punto de fichaje: {e}")
            flash(f'Error al eliminar el punto de fichaje: La operación no pudo completarse debido a un error de base de datos.', 'danger')
    except Exception as e:
        current_app.logger.error(f"Error general en delete_checkpoint: {e}")
        flash('Se produjo un error al procesar la solicitud. Por favor, inténtelo de nuevo.', 'danger')
    
    return redirect(url_for('checkpoints.list_checkpoints'))


@checkpoints_bp.route('/checkpoints/<int:id>/records')
@login_required
@manager_required
def list_checkpoint_records(id):
    """Muestra los registros de fichaje de un punto específico"""
    checkpoint = CheckPoint.query.get_or_404(id)
    
    # Verificar permiso (solo admin o gerente de la empresa)
    if not current_user.is_admin() and checkpoint.company_id not in [c.id for c in current_user.companies]:
        flash('No tiene permiso para ver los registros de este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints.list_checkpoints'))
    
    page = request.args.get('page', 1, type=int)
    records = CheckPointRecord.query.filter_by(checkpoint_id=id).order_by(
        CheckPointRecord.check_in_time.desc()
    ).paginate(page=page, per_page=20)
    
    return render_template('checkpoints/list_records.html', 
                          checkpoint=checkpoint, 
                          records=records)


@checkpoints_bp.route('/employees/<int:id>/contract_hours', methods=['GET', 'POST'])
@login_required
@manager_required
def manage_contract_hours(id):
    """Gestiona la configuración de horas por contrato de un empleado"""
    employee = Employee.query.get_or_404(id)
    
    # Verificar permiso (solo admin o gerente de la empresa)
    if not current_user.is_admin() and employee.company_id not in [c.id for c in current_user.companies]:
        flash('No tiene permiso para gestionar las horas de contrato de este empleado.', 'danger')
        return redirect(url_for('checkpoints.index_company', slug=employee.company.get_slug()))
    
    # Buscar o crear configuración de horas de contrato
    contract_hours = EmployeeContractHours.query.filter_by(employee_id=employee.id).first()
    
    if not contract_hours:
        contract_hours = EmployeeContractHours(employee_id=employee.id)
        db.session.add(contract_hours)
        db.session.commit()
    
    form = ContractHoursForm(obj=contract_hours)
    
    if form.validate_on_submit():
        form.populate_obj(contract_hours)
        
        try:
            db.session.commit()
            flash('Configuración de horas de contrato actualizada con éxito.', 'success')
            return redirect(url_for('checkpoints.index_company', slug=employee.company.get_slug()))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al actualizar la configuración: {str(e)}', 'danger')
    
    return render_template('checkpoints/contract_hours_form.html', 
                          form=form, 
                          employee=employee)


@checkpoints_bp.route('/records/<int:id>/adjust', methods=['GET', 'POST'])
@login_required
@manager_required
def adjust_record(id):
    """Ajusta manualmente un registro de fichaje"""
    record = CheckPointRecord.query.get_or_404(id)
    
    # Verificar permiso
    employee_company_id = record.employee.company_id
    if not current_user.is_admin() and employee_company_id not in [c.id for c in current_user.companies]:
        flash('No tiene permiso para ajustar este registro de fichaje.', 'danger')
        return redirect(url_for('checkpoints.list_checkpoints'))
    
    form = CheckPointRecordAdjustmentForm()
    
    # Pre-llenar el formulario con los datos actuales
    if request.method == 'GET':
        form.check_in_date.data = record.check_in_time.strftime('%Y-%m-%d')
        form.check_in_time.data = record.check_in_time.time()
        if record.check_out_time:
            form.check_out_time.data = record.check_out_time.time()
    
    if form.validate_on_submit():
        # Guardar una copia del registro original en la nueva tabla
        from models_checkpoints import CheckPointOriginalRecord
        
        # Crear un registro del estado original antes del ajuste
        original_record = CheckPointOriginalRecord(
            record_id=record.id,
            original_check_in_time=record.check_in_time,
            original_check_out_time=record.check_out_time,
            original_signature_data=record.signature_data,
            original_has_signature=record.has_signature,
            original_notes=record.notes,
            adjusted_by_id=current_user.id,
            adjustment_reason=form.adjustment_reason.data
        )
        db.session.add(original_record)
        
        # Ya no guardamos valores originales en los campos original_* del registro principal
        
        # Actualizar con los nuevos valores
        check_in_date = datetime.strptime(form.check_in_date.data, '%Y-%m-%d').date()
        
        # Combinar fecha y hora para check_in_time
        record.check_in_time = datetime.combine(check_in_date, form.check_in_time.data)
        
        # Combinar fecha y hora para check_out_time si está presente
        if form.check_out_time.data:
            # Si la hora de salida es menor que la de entrada, asumimos que es del día siguiente
            if form.check_out_time.data < form.check_in_time.data:
                check_out_date = check_in_date + timedelta(days=1)
            else:
                check_out_date = check_in_date
                
            record.check_out_time = datetime.combine(check_out_date, form.check_out_time.data)
        
        record.adjusted = True
        record.adjustment_reason = form.adjustment_reason.data
        
        # Si se debe aplicar el límite de horas de contrato
        if form.enforce_contract_hours.data and record.check_out_time:
            contract_hours = EmployeeContractHours.query.filter_by(employee_id=record.employee_id).first()
            
            if contract_hours:
                # Ajustar el fichaje según las horas de contrato
                new_check_in, new_check_out = contract_hours.calculate_adjusted_hours(
                    record.check_in_time, record.check_out_time
                )
                
                if new_check_in and new_check_in != record.check_in_time:
                    record.check_in_time = new_check_in
                    
                    # Marcar con R en lugar de crear incidencia por ajuste de contrato
                    record.notes = (record.notes or "") + " [R] Ajustado para cumplir límite de horas."
        
        try:
            db.session.commit()
            flash('Registro de fichaje ajustado con éxito.', 'success')
            return redirect(url_for('checkpoints.list_checkpoint_records', id=record.checkpoint_id))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al ajustar el registro: {str(e)}', 'danger')
    
    return render_template('checkpoints/adjust_record_form.html', 
                          form=form, 
                          record=record)


@checkpoints_bp.route('/records/<int:id>/signature', methods=['GET', 'POST'])
@login_required
def record_signature(id):
    """Permite al empleado firmar un registro de fichaje"""
    record = CheckPointRecord.query.get_or_404(id)
    
    # Solo el empleado o un administrador pueden firmar
    is_admin_or_manager = current_user.is_admin() or current_user.is_gerente()
    is_employee_user = current_user.employee and current_user.employee.id == record.employee_id
    
    if not is_admin_or_manager and not is_employee_user:
        flash('No tiene permiso para firmar este registro.', 'danger')
        return redirect(url_for('index'))
    
    form = SignaturePadForm()
    
    if form.validate_on_submit():
        record.signature_data = form.signature_data.data
        record.has_signature = True
        
        try:
            db.session.commit()
            flash('Firma guardada con éxito.', 'success')
            
            # Redireccionar según el tipo de usuario
            if is_admin_or_manager:
                return redirect(url_for('checkpoints.list_checkpoint_records', id=record.checkpoint_id))
            else:
                return redirect(url_for('main.dashboard'))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al guardar la firma: {str(e)}', 'danger')
    
    return render_template('checkpoints/signature_pad.html', 
                          form=form, 
                          record=record)


@checkpoints_bp.route('/records')
@login_required
@manager_required
def list_records_all():
    """Muestra todos los registros de fichaje disponibles según permisos"""
    # Determinar los registros a los que tiene acceso el usuario
    if current_user.is_admin():
        base_query = CheckPointRecord.query
    else:
        # Si es gerente, solo registros de las empresas que administra
        company_ids = [company.id for company in current_user.companies]
        
        if not company_ids:
            flash('No tiene empresas asignadas para gestionar registros.', 'warning')
            return redirect(url_for('dashboard'))
        
        # Buscar empleados de esas empresas
        employee_ids = db.session.query(Employee.id).filter(
            Employee.company_id.in_(company_ids)
        ).all()
        employee_ids = [e[0] for e in employee_ids]
        
        if not employee_ids:
            flash('No hay empleados registrados en sus empresas.', 'warning')
            return redirect(url_for('dashboard'))
        
        base_query = CheckPointRecord.query.filter(
            CheckPointRecord.employee_id.in_(employee_ids)
        )
    
    # Filtros adicionales (fecha, empleado, etc.)
    start_date = request.args.get('start_date')
    end_date = request.args.get('end_date')
    employee_id = request.args.get('employee_id', type=int)
    status = request.args.get('status')
    
    if start_date:
        try:
            start_date = datetime.strptime(start_date, '%Y-%m-%d').date()
            base_query = base_query.filter(
                func.date(CheckPointRecord.check_in_time) >= start_date
            )
        except ValueError:
            pass
    
    if end_date:
        try:
            end_date = datetime.strptime(end_date, '%Y-%m-%d').date()
            base_query = base_query.filter(
                func.date(CheckPointRecord.check_in_time) <= end_date
            )
        except ValueError:
            pass
    
    if employee_id:
        base_query = base_query.filter(CheckPointRecord.employee_id == employee_id)
    
    if status:
        if status == 'pending':
            base_query = base_query.filter(CheckPointRecord.check_out_time.is_(None))
        elif status == 'completed':
            base_query = base_query.filter(CheckPointRecord.check_out_time.isnot(None))
        elif status == 'adjusted':
            base_query = base_query.filter(CheckPointRecord.adjusted == True)
        elif status == 'incidents':
            # Registros con incidencias (requiere una subconsulta)
            incident_record_ids = db.session.query(CheckPointIncident.record_id).distinct().subquery()
            base_query = base_query.filter(CheckPointRecord.id.in_(incident_record_ids))
    
    # Ordenar y paginar
    page = request.args.get('page', 1, type=int)
    records = base_query.order_by(CheckPointRecord.check_in_time.desc()).paginate(
        page=page, per_page=20
    )
    
    # Datos adicionales para el filtro - incluir tanto empleados activos como inactivos
    if current_user.is_admin():
        # Para administradores, todos los empleados
        filter_employees = Employee.query.order_by(Employee.first_name).all()
        print(f"Admin (list_records_all): Encontrados {len(filter_employees)} empleados totales")
    else:
        # Para gerentes, los empleados de sus empresas asignadas
        company_ids = [company.id for company in current_user.companies]
        filter_employees = Employee.query.filter(
            Employee.company_id.in_(company_ids)  # Todos los empleados de las empresas asignadas
        ).order_by(Employee.first_name).all()
        print(f"Gerente (list_records_all): Encontrados {len(filter_employees)} empleados para las empresas {company_ids}")
    
    return render_template(
        'checkpoints/all_records.html',
        records=records,
        filter_employees=filter_employees,
        current_filters={
            'start_date': start_date.strftime('%Y-%m-%d') if isinstance(start_date, date) else None,
            'end_date': end_date.strftime('%Y-%m-%d') if isinstance(end_date, date) else None,
            'employee_id': employee_id,
            'status': status
        }
    )


@checkpoints_bp.route('/incidents')
@login_required
@manager_required
def list_incidents():
    """Muestra todas las incidencias de fichaje según permisos"""
    # Determinar las incidencias a las que tiene acceso el usuario
    if current_user.is_admin():
        base_query = CheckPointIncident.query.join(
            CheckPointRecord, CheckPointIncident.record_id == CheckPointRecord.id
        )
    else:
        # Si es gerente, solo incidencias de las empresas que administra
        company_ids = [company.id for company in current_user.companies]
        
        if not company_ids:
            flash('No tiene empresas asignadas para gestionar incidencias.', 'warning')
            return redirect(url_for('dashboard'))
        
        # Buscar empleados de esas empresas
        employee_ids = db.session.query(Employee.id).filter(
            Employee.company_id.in_(company_ids)
        ).all()
        employee_ids = [e[0] for e in employee_ids]
        
        if not employee_ids:
            flash('No hay empleados registrados en sus empresas.', 'warning')
            return redirect(url_for('dashboard'))
        
        base_query = CheckPointIncident.query.join(
            CheckPointRecord, CheckPointIncident.record_id == CheckPointRecord.id
        ).filter(
            CheckPointRecord.employee_id.in_(employee_ids)
        )
    
    # Filtros adicionales
    incident_type = request.args.get('type')
    resolved = request.args.get('resolved')
    employee_id = request.args.get('employee_id', type=int)
    
    if incident_type:
        base_query = base_query.filter(CheckPointIncident.incident_type == incident_type)
    
    if resolved == 'yes':
        base_query = base_query.filter(CheckPointIncident.resolved == True)
    elif resolved == 'no':
        base_query = base_query.filter(CheckPointIncident.resolved.is_(False))
    
    if employee_id:
        base_query = base_query.filter(CheckPointRecord.employee_id == employee_id)
    
    # Ordenar y paginar
    page = request.args.get('page', 1, type=int)
    incidents = base_query.order_by(CheckPointIncident.created_at.desc()).paginate(
        page=page, per_page=20
    )
    
    # Datos adicionales para el filtro - incluir tanto empleados activos como inactivos
    if current_user.is_admin():
        # Para administradores, todos los empleados
        filter_employees = Employee.query.order_by(Employee.first_name).all()
        print(f"Admin (list_incidents): Encontrados {len(filter_employees)} empleados totales")
    else:
        # Para gerentes, los empleados de sus empresas asignadas
        company_ids = [company.id for company in current_user.companies]
        filter_employees = Employee.query.filter(
            Employee.company_id.in_(company_ids)  # Todos los empleados de las empresas asignadas
        ).order_by(Employee.first_name).all()
        print(f"Gerente (list_incidents): Encontrados {len(filter_employees)} empleados para las empresas {company_ids}")
    
    return render_template(
        'checkpoints/incidents.html',
        incidents=incidents,
        filter_employees=filter_employees,
        incident_types=CheckPointIncidentType,
        current_filters={
            'type': incident_type,
            'resolved': resolved,
            'employee_id': employee_id
        }
    )


@checkpoints_bp.route('/incidents/<int:id>/resolve', methods=['POST'])
@login_required
@manager_required
def resolve_incident(id):
    """Marca una incidencia como resuelta"""
    incident = CheckPointIncident.query.get_or_404(id)
    
    # Verificar permisos
    record = incident.record
    employee_company_id = record.employee.company_id
    
    if not current_user.is_admin() and employee_company_id not in [c.id for c in current_user.companies]:
        flash('No tiene permiso para resolver esta incidencia.', 'danger')
        return redirect(url_for('checkpoints.list_incidents'))
    
    # Obtener las notas de resolución
    resolution_notes = request.form.get('resolution_notes', '')
    
    # Resolver la incidencia
    incident.resolve(current_user.id, resolution_notes)
    
    try:
        db.session.commit()
        flash('Incidencia marcada como resuelta con éxito.', 'success')
    except Exception as e:
        db.session.rollback()
        flash(f'Error al resolver la incidencia: {str(e)}', 'danger')
    
    # Redirigir a la página anterior o a la lista de incidencias
    next_page = request.args.get('next') or url_for('checkpoints.list_incidents')
    return redirect(next_page)


# Se ha eliminado la ruta secreta '/company/<slug>/rrrrrr'

# Se ha eliminado la ruta secreta '/company/<slug>/rrrrrr/edit/<int:id>'

# Se ha eliminado la ruta secreta '/company/<slug>/rrrrrr/restore/<int:id>'

# Se ha eliminado la ruta secreta '/company/<slug>/rrrrrr/delete/<int:id>'

# Se ha eliminado la ruta secreta '/company/<slug>/rrrrrr/export' y la función export_original_records_pdf

@checkpoints_bp.route('/company/<slug>/original', methods=['GET'])
@login_required
@admin_required
def view_original_records(slug):
    """Página secreta para ver los registros originales de fichaje para una empresa específica"""
    from models_checkpoints import CheckPointOriginalRecord
    from utils import slugify
    
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
    show_all = request.args.get('show_all', 'true')  # Parámetro para mostrar todos los registros
    
    # Obtener los IDs de los empleados de esta empresa
    employee_ids = db.session.query(Employee.id).filter_by(company_id=company_id).all()
    employee_ids = [e[0] for e in employee_ids]
    
    # Construir la consulta base con filtro de empresa para todos los registros
    if show_all == 'true':
        # Consulta para todos los registros, incluyendo los que solo tienen hora de entrada
        query = db.session.query(
            CheckPointRecord, 
            Employee
        ).join(
            Employee,
            CheckPointRecord.employee_id == Employee.id
        ).filter(
            Employee.company_id == company_id
            # Ya no filtramos por check_out_time para mostrar también registros con solo entrada
        ).outerjoin(
            CheckPointOriginalRecord,
            CheckPointOriginalRecord.record_id == CheckPointRecord.id
        )
    else:
        # Consulta original sólo para registros modificados
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
    
    # Aplicar filtros si los hay
    if start_date:
        try:
            start_date = datetime.strptime(start_date, '%Y-%m-%d').date()
            if show_all == 'true':
                query = query.filter(func.date(CheckPointRecord.check_in_time) >= start_date)
            else:
                query = query.filter(func.date(CheckPointOriginalRecord.original_check_in_time) >= start_date)
        except ValueError:
            pass
    
    if end_date:
        try:
            end_date = datetime.strptime(end_date, '%Y-%m-%d').date()
            if show_all == 'true':
                query = query.filter(func.date(CheckPointRecord.check_in_time) <= end_date)
            else:
                query = query.filter(func.date(CheckPointOriginalRecord.original_check_in_time) <= end_date)
        except ValueError:
            pass
    
    if employee_id:
        query = query.filter(Employee.id == employee_id)
    
    # Ordenar y paginar
    if show_all == 'true':
        all_records = query.order_by(
            CheckPointRecord.check_in_time.desc()
        ).paginate(page=page, per_page=20)
    else:
        all_records = query.order_by(
            CheckPointOriginalRecord.adjusted_at.desc()
        ).paginate(page=page, per_page=20)
    
    # Obtener la lista de empleados para el filtro (todos los empleados de esta empresa, activos e inactivos)
    employees = Employee.query.filter_by(company_id=company_id).order_by(Employee.first_name).all()
    
    return render_template(
        'checkpoints/original_records.html',
        original_records=all_records,
        employees=employees,
        company=company,
        company_id=company_id,
        show_all=show_all,
        filters={
            'start_date': start_date.strftime('%Y-%m-%d') if isinstance(start_date, date) else None,
            'end_date': end_date.strftime('%Y-%m-%d') if isinstance(end_date, date) else None,
            'employee_id': employee_id
        },
        title=f"Registros Originales de {company.name if company else ''} ({('Vista simplificada' if show_all == 'true' else 'Registros modificados')})"
    )

@checkpoints_bp.route('/records/export', methods=['GET', 'POST'])
@login_required
@manager_required
def export_records():
    """Exporta registros de fichaje a PDF"""
    form = ExportCheckPointRecordsForm()
    
    # Cargar todos los empleados activos e inactivos para que funcione el selector
    try:
        # Consulta directa para asegurar que se obtienen todos los empleados
        employees_query = db.session.query(Employee)
        
        # Filtrar empleados según los permisos del usuario
        if not current_user.is_admin():
            # Para gerentes, solo mostrar empleados de sus empresas
            company_ids = [company.id for company in current_user.companies]
            employees_query = employees_query.filter(Employee.company_id.in_(company_ids))
        
        # Ejecutar la consulta (sin filtrar por is_active para obtener todos)
        employees = employees_query.order_by(Employee.first_name).all()
        
        # Imprimir los empleados para debug
        print(f"Empleados encontrados: {len(employees)}")
        for e in employees:
            print(f"ID: {e.id}, Nombre: {e.first_name} {e.last_name}, Empresa: {e.company_id}")
        
        # Configurar las opciones del formulario
        choices = [(0, 'Todos los empleados')]
        for e in employees:
            choices.append((e.id, f"{e.first_name} {e.last_name}"))
        
        form.employee_id.choices = choices
        
    except Exception as e:
        # Manejo de errores para diagnóstico
        print(f"ERROR al cargar los empleados: {str(e)}")
        import traceback
        traceback.print_exc()
        form.employee_id.choices = [(0, 'Todos los empleados')]
    
    if form.validate_on_submit():
        try:
            # Parsear fechas
            start_date = datetime.strptime(form.start_date.data, '%Y-%m-%d').date()
            end_date = datetime.strptime(form.end_date.data, '%Y-%m-%d').date()
            
            # Construir consulta
            query = CheckPointRecord.query.filter(
                func.date(CheckPointRecord.check_in_time) >= start_date,
                func.date(CheckPointRecord.check_in_time) <= end_date
            )
            
            # Filtrar por empleado si se especifica
            if form.employee_id.data != 0:
                query = query.filter(CheckPointRecord.employee_id == form.employee_id.data)
            
            # Ejecutar consulta
            records = query.order_by(
                CheckPointRecord.employee_id,
                CheckPointRecord.check_in_time
            ).all()
            
            if not records:
                flash('No se encontraron registros para el período seleccionado.', 'warning')
                return redirect(url_for('checkpoints.export_records'))
            
            # Generar PDF usando la función simple sin agrupación por semanas ni suma de horas
            pdf_file = generate_simple_pdf_report(
                records=records, 
                start_date=start_date, 
                end_date=end_date,
                include_signature=form.include_signature.data
            )
            
            # Guardar temporalmente y enviar
            filename = f"fichajes_{start_date.strftime('%Y%m%d')}_{end_date.strftime('%Y%m%d')}.pdf"
            return send_file(
                pdf_file,
                as_attachment=True,
                download_name=filename,
                mimetype='application/pdf'
            )
            
        except Exception as e:
            flash(f'Error al generar el informe: {str(e)}', 'danger')
    
    # Establecer fechas predeterminadas si es la primera carga
    if request.method == 'GET':
        today = date.today()
        # Por defecto, mostrar el mes actual
        form.start_date.data = date(today.year, today.month, 1).strftime('%Y-%m-%d')
        form.end_date.data = today.strftime('%Y-%m-%d')
    
    return render_template('checkpoints/export_form.html', form=form)


# Rutas para puntos de fichaje (interfaz tablet/móvil)
@checkpoints_bp.route('/login', methods=['GET', 'POST'])
def login():
    """Página de login para puntos de fichaje"""
    # Si ya hay una sesión activa, redirigir al dashboard
    if 'checkpoint_id' in session:
        return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    # Verificar si se ha pasado un checkpoint_id como parámetro
    checkpoint_id = request.args.get('checkpoint_id')
    checkpoint = None
    if checkpoint_id:
        # Si tenemos un ID de checkpoint, intentamos cargar ese checkpoint específico
        try:
            checkpoint_id = int(checkpoint_id)
            checkpoint = CheckPoint.query.get(checkpoint_id)
            if not checkpoint:
                flash('El punto de fichaje especificado no existe.', 'danger')
        except ValueError:
            flash('ID de punto de fichaje no válido.', 'danger')
    
    form = CheckPointLoginForm()
    
    if form.validate_on_submit():
        # Si se está realizando un login mediante formulario, buscar el checkpoint por username
        login_checkpoint = CheckPoint.query.filter_by(username=form.username.data).first()
        
        if login_checkpoint and login_checkpoint.verify_password(form.password.data):
            if login_checkpoint.status == CheckPointStatus.ACTIVE:
                # Guardar ID del punto de fichaje en la sesión
                session['checkpoint_id'] = login_checkpoint.id
                session['checkpoint_name'] = login_checkpoint.name
                session['company_id'] = login_checkpoint.company_id
                
                flash(f'Bienvenido al punto de fichaje {login_checkpoint.name}', 'success')
                return redirect(url_for('checkpoints.checkpoint_dashboard'))
            else:
                flash('Este punto de fichaje está desactivado o en mantenimiento.', 'warning')
        else:
            flash('Usuario o contraseña incorrectos.', 'danger')
    
    # Si tenemos un checkpoint específico y es la primera carga (GET), prellenamos el formulario
    if checkpoint and request.method == 'GET':
        form.username.data = checkpoint.username
    
    return render_template('checkpoints/login.html', form=form, checkpoint=checkpoint)


@checkpoints_bp.route('/login/<int:checkpoint_id>', methods=['GET', 'POST'])
def login_to_checkpoint(checkpoint_id):
    """Acceso directo a un punto de fichaje específico por ID"""
    # Si ya hay una sesión activa, redirigir al dashboard
    if 'checkpoint_id' in session:
        return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    # Intentar cargar el checkpoint especificado
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    
    form = CheckPointLoginForm()
    
    if form.validate_on_submit():
        if checkpoint and checkpoint.verify_password(form.password.data):
            if checkpoint.status == CheckPointStatus.ACTIVE:
                # Guardar ID del punto de fichaje en la sesión
                session['checkpoint_id'] = checkpoint.id
                session['checkpoint_name'] = checkpoint.name
                session['company_id'] = checkpoint.company_id
                
                flash(f'Bienvenido al punto de fichaje {checkpoint.name}', 'success')
                return redirect(url_for('checkpoints.checkpoint_dashboard'))
            else:
                flash('Este punto de fichaje está desactivado o en mantenimiento.', 'warning')
        else:
            flash('Contraseña incorrecta para este punto de fichaje.', 'danger')
    
    # Prellenar el nombre de usuario
    if request.method == 'GET':
        form.username.data = checkpoint.username
    
    return render_template('checkpoints/login.html', form=form, checkpoint=checkpoint)


@checkpoints_bp.route('/logout')
def logout():
    """Cierra la sesión del punto de fichaje"""
    # Limpiar variables de sesión
    session.pop('checkpoint_id', None)
    session.pop('checkpoint_name', None)
    session.pop('company_id', None)
    session.pop('employee_id', None)
    
    flash('Ha cerrado sesión correctamente.', 'success')
    return redirect(url_for('checkpoints.login'))


@checkpoints_bp.route('/dashboard')
@checkpoint_required
def checkpoint_dashboard():
    """Dashboard principal del punto de fichaje"""
    checkpoint_id = session.get('checkpoint_id')
    # Usamos refresh=True para asegurarnos de obtener los datos más actualizados
    db.session.expire_all()  # Asegurarse de que todas las entidades se refresquen
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    
    # Obtener solo los empleados ACTIVOS de la empresa asociada al punto de fichaje
    # Filtramos por is_active=True para mostrar solo empleados activos en el punto de fichaje
    employees = Employee.query.filter_by(
        company_id=checkpoint.company_id,
        is_active=True
    ).order_by(Employee.first_name, Employee.last_name).all()
    
    return render_template('checkpoints/dashboard.html', 
                          checkpoint=checkpoint,
                          employees=employees)


@checkpoints_bp.route('/employee/<int:id>/pin', methods=['GET', 'POST'])
@checkpoint_required
def employee_pin(id):
    """
    Página para introducir el PIN del empleado
    
    Nueva implementación con separación clara de responsabilidades:
    1. Validación del empleado y permisos
    2. Verificación del PIN
    3. Interfaz de usuario adaptativa según el estado del empleado
    """
    # Obtener información del punto de fichaje y empleado
    checkpoint_id = session.get('checkpoint_id')
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    employee = Employee.query.get_or_404(id)
    
    # Verificar que el empleado pertenece a la empresa del punto de fichaje
    if employee.company_id != checkpoint.company_id:
        flash('Empleado no válido para este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    # Inicializar formulario
    form = CheckPointEmployeePinForm()
    form.employee_id.data = employee.id
    
    # Verificar si existe un registro pendiente de fichaje (entrada sin salida)
    pending_record = CheckPointRecord.query.filter_by(
        employee_id=employee.id,
        checkpoint_id=checkpoint_id,
        check_out_time=None
    ).first()
    
    # Log para debugging
    print(f"Empleado: {employee.id} - {employee.first_name} {employee.last_name}")
    print(f"Estado actual: is_on_shift={employee.is_on_shift}, pending_record={pending_record is not None}")
    if pending_record:
        print(f"Registro pendiente ID: {pending_record.id}, Entrada: {pending_record.check_in_time}")
    
    # Procesar formulario si es submit
    if form.validate_on_submit():
        # Verificar si el PIN ha sido validado por AJAX
        pin_verified = request.form.get('pin_verified') == '1'
        
        # Verificación del PIN (últimos 4 dígitos del DNI, ignorando la letra final en DNI español)
        # Filtrar solo los dígitos del DNI
        dni_digits = ''.join(c for c in employee.dni if c.isdigit())
        pin_from_dni = dni_digits[-4:] if len(dni_digits) >= 4 else dni_digits
        
        # Si el PIN fue validado por AJAX o si es correcto
        if pin_verified or form.pin.data == pin_from_dni:
            # Obtener la acción solicitada
            action = request.form.get('action')
            
            # Procesar según la acción
            if action in ['checkin', 'checkout']:
                # Intentar realizar la acción en una transacción atómica
                return process_employee_action(employee, checkpoint_id, action, pending_record)
            else:
                flash('Acción no reconocida. Por favor, inténtelo de nuevo.', 'danger')
        else:
            flash('PIN incorrecto. Inténtelo de nuevo.', 'danger')
    
    # Renderizar la plantilla con los datos necesarios
    return render_template(
        'checkpoints/employee_pin.html', 
        form=form,
        employee=employee,
        pending_record=pending_record,
        checkpoint=checkpoint
    )


def create_schedule_incident(record, incident_type, description):
    """
    Crea una incidencia relacionada con horarios
    """
    incident = CheckPointIncident(
        record_id=record.id,
        incident_type=incident_type,
        description=description
    )
    db.session.add(incident)
    return incident

def process_employee_action(employee, checkpoint_id, action, pending_record):
    """
    Función auxiliar para procesar las acciones de entrada/salida
    Encapsula la lógica de negocio y el manejo de transacciones
    """
    try:
        # CASO 1: Check-out (finalizar jornada)
        if action == 'checkout' and pending_record:
            # Obtener datos actuales para logging
            old_status = employee.is_on_shift
            record_id = pending_record.id
            
            # Actualizar en una transacción
            db.session.begin_nested()
            
            # 1. Registrar hora de salida
            checkout_time = get_current_time()
            pending_record.check_out_time = checkout_time
            db.session.add(pending_record)
            
            # Guardar siempre el registro original primero al hacer checkout
            from models_checkpoints import CheckPointOriginalRecord
            
            # Capturar los valores reales antes de cualquier ajuste
            # Importante: Guardamos la hora exacta que se introdujo al inicio de la jornada
            original_checkin = pending_record.check_in_time  # Esta es la hora original de entrada
            original_checkout = get_current_time()  # Esta es la hora real de salida antes de ajustes
            
            # Actualizar primero la hora de salida con la hora real
            pending_record.check_out_time = original_checkout
            db.session.add(pending_record)
            db.session.flush()  # Aseguramos que pending_record tenga todos sus campos actualizados
            
            # Buscar si ya existe un registro original al iniciar jornada
            existing_original = CheckPointOriginalRecord.query.filter_by(
                record_id=pending_record.id,
                adjustment_reason="Registro original al iniciar fichaje"
            ).first()
            
            if existing_original:
                # Actualizar el registro existente con los datos de salida
                existing_original.original_check_out_time = original_checkout
                existing_original.original_signature_data = pending_record.signature_data
                existing_original.original_has_signature = pending_record.has_signature
                existing_original.original_notes = pending_record.notes
                existing_original.adjustment_reason = "Registro original completo (ficha entrada/salida)"
                db.session.add(existing_original)
            else:
                # Si no existe (caso poco probable), crear uno nuevo
                original_record = CheckPointOriginalRecord(
                    record_id=pending_record.id,
                    original_check_in_time=original_checkin,
                    original_check_out_time=original_checkout,
                    original_signature_data=pending_record.signature_data,
                    original_has_signature=pending_record.has_signature,
                    original_notes=pending_record.notes,
                    adjustment_reason="Registro original al finalizar fichaje"
                )
                db.session.add(original_record)
            
            # 2. Verificar configuración de horas de contrato
            contract_hours = EmployeeContractHours.query.filter_by(employee_id=employee.id).first()
            if contract_hours:
                # Verificar si se debe ajustar el horario según configuración
                adjusted_checkin, adjusted_checkout = contract_hours.calculate_adjusted_hours(
                    original_checkin, original_checkout
                )
                
                # Verificar si hay ajustes a realizar
                needs_adjustment = (adjusted_checkin and adjusted_checkin != original_checkin) or \
                                  (adjusted_checkout and adjusted_checkout != original_checkout)
                
                # Si hay ajustes, actualizar el registro
                if needs_adjustment:
                    # Actualizar el registro con los valores ajustados
                    if adjusted_checkin and adjusted_checkin != original_checkin:
                        pending_record.check_in_time = adjusted_checkin
                        # Marcar con R en lugar de crear incidencia
                        pending_record.notes = (pending_record.notes or "") + f" [R] Hora entrada ajustada de {original_checkin.strftime('%H:%M')} a {adjusted_checkin.strftime('%H:%M')}"
                    
                    if adjusted_checkout and adjusted_checkout != original_checkout:
                        pending_record.check_out_time = adjusted_checkout
                        # Marcar con R en lugar de crear incidencia
                        pending_record.notes = (pending_record.notes or "") + f" [R] Hora salida ajustada de {original_checkout.strftime('%H:%M:%S')} a {adjusted_checkout.strftime('%H:%M:%S')}"
                    
                    # Marcar como ajustado
                    pending_record.adjusted = True
                    
                    # Actualizar la razón del ajuste en el registro original
                    if existing_original:
                        existing_original.adjustment_reason = "Ajuste automático por límite de horas de contrato"
                        db.session.add(existing_original)
                    elif 'original_record' in locals():
                        original_record.adjustment_reason = "Ajuste automático por límite de horas de contrato"
                        db.session.add(original_record)
                
                # Verificar si hay horas extra
                duration = (pending_record.check_out_time - pending_record.check_in_time).total_seconds() / 3600
                if contract_hours.is_overtime(duration):
                    overtime_hours = duration - contract_hours.daily_hours
                    create_schedule_incident(
                        pending_record, 
                        CheckPointIncidentType.OVERTIME,
                        f"Jornada con {overtime_hours:.2f} horas extra sobre el límite diario de {contract_hours.daily_hours} horas"
                    )
            
            # 3. Actualizar estado del empleado
            employee.is_on_shift = False
            db.session.add(employee)
            
            # Confirmar transacción
            db.session.commit()
            
            # Log detallado post-transacción
            print(f"✅ CHECKOUT: Empleado ID {employee.id} - Estado: {old_status} → {employee.is_on_shift}")
            print(f"   Registro ID: {record_id}, Salida: {pending_record.check_out_time}")
            
            # Notificar al usuario
            flash(f'Jornada finalizada correctamente para {employee.first_name} {employee.last_name}. Por favor, firma tu registro.', 'success')
            
            # Redirigir directamente a la página de firma
            return redirect(url_for('checkpoints.checkpoint_record_signature', id=pending_record.id))
        
        # CASO 2: Check-in (iniciar jornada)
        elif action == 'checkin' and not pending_record:
            # Obtener datos actuales para logging
            old_status = employee.is_on_shift
            
            # Actualizar en una transacción
            db.session.begin_nested()
            
            # 1. Crear nuevo registro de fichaje
            checkin_time = get_current_time()
            new_record = CheckPointRecord(
                employee_id=employee.id,
                checkpoint_id=checkpoint_id,
                check_in_time=checkin_time
            )
            db.session.add(new_record)
            # Hacemos flush para que new_record obtenga un ID
            db.session.flush()
            
            # Guardar siempre el registro original al iniciar jornada
            from models_checkpoints import CheckPointOriginalRecord
            
            # Datos originales en el momento de iniciar la jornada
            original_record = CheckPointOriginalRecord(
                record_id=new_record.id,  # Ahora new_record.id ya tiene un valor
                original_check_in_time=checkin_time,
                original_check_out_time=None,
                original_signature_data=None,
                original_has_signature=False,
                original_notes=None,
                adjustment_reason="Registro original al iniciar fichaje"
            )
            db.session.add(original_record)
            
            # 2. Actualizar estado del empleado
            employee.is_on_shift = True
            db.session.add(employee)
            
            # Confirmar transacción
            db.session.commit()
            db.session.refresh(new_record)
            
            # Log detallado post-transacción
            print(f"✅ CHECKIN: Empleado ID {employee.id} - Estado: {old_status} → {employee.is_on_shift}")
            print(f"   Nuevo registro ID: {new_record.id}, Entrada: {new_record.check_in_time}")
            
            # Redirigir a la pantalla de empleados (dashboard) sin notificación
            return redirect(url_for('checkpoints.checkpoint_dashboard'))
        
        # CASO 3: Estado inconsistente
        else:
            # Detectar y reportar inconsistencias específicamente
            if pending_record and action == 'checkin':
                flash('El empleado ya tiene una entrada activa sin salida registrada.', 'warning')
            elif not pending_record and action == 'checkout':
                flash('El empleado no tiene ninguna entrada activa para registrar salida.', 'warning')
            else:
                flash('Estado inconsistente. Contacte al administrador.', 'danger')
                
            # Intentar corregir inconsistencias entre is_on_shift y pending_record
            fix_employee_state_inconsistency(employee, pending_record)
            
            # Redirigir a la página principal del punto de fichaje
            return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    except Exception as e:
        # Deshacer cambios en caso de error
        db.session.rollback()
        
        # Log detallado del error
        error_details = f"ERROR en fichaje: {str(e)}"
        print(f"❌ {error_details}")
        print(f"   Acción: {action}, Empleado: {employee.id}, Checkpoint: {checkpoint_id}")
        
        # Notificar al usuario
        flash(f'Error al procesar el fichaje: {str(e)}', 'danger')
        
        # Redirigir a la página de PIN
        return redirect(url_for('checkpoints.employee_pin', id=employee.id))


def fix_employee_state_inconsistency(employee, pending_record):
    """
    Función auxiliar para detectar y corregir inconsistencias entre 
    el estado is_on_shift y los registros de fichaje
    """
    try:
        # Caso 1: Tiene registro pendiente pero is_on_shift=False
        if pending_record and not employee.is_on_shift:
            # Corregir: Poner is_on_shift=True
            employee.is_on_shift = True
            db.session.add(employee)
            db.session.commit()
            print(f"⚠️ CORREGIDO: Empleado {employee.id} tiene registro pendiente pero is_on_shift=False")
            
        # Caso 2: No tiene registro pendiente pero is_on_shift=True
        elif not pending_record and employee.is_on_shift:
            # Corregir: Poner is_on_shift=False
            employee.is_on_shift = False
            db.session.add(employee)
            db.session.commit()
            print(f"⚠️ CORREGIDO: Empleado {employee.id} no tiene registro pendiente pero is_on_shift=True")
            
    except Exception as e:
        db.session.rollback()
        print(f"❌ ERROR corrigiendo inconsistencia: {str(e)}")


@checkpoints_bp.route('/record/<int:id>')
@checkpoint_required
def record_details(id):
    """Muestra los detalles de un registro recién creado"""
    record = CheckPointRecord.query.get_or_404(id)
    
    # Verificar que el registro pertenece al punto de fichaje actual
    checkpoint_id = session.get('checkpoint_id')
    if record.checkpoint_id != checkpoint_id:
        flash('Registro no válido para este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    # Obtener el checkpoint para la plantilla
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    
    # Log para depuración
    print(f"Mostrando registro: ID {record.id}, Empleado {record.employee.first_name} {record.employee.last_name}, " +
          f"Entrada: {record.check_in_time}, Salida: {record.check_out_time}, Estado: {record.employee.is_on_shift}")
    
    return render_template('checkpoints/record_details.html', record=record, checkpoint=checkpoint)


@checkpoints_bp.route('/record/<int:id>/checkout', methods=['POST'])
@checkpoint_required
def record_checkout(id):
    """Registra la salida para un fichaje pendiente desde la pantalla de detalles"""
    record = CheckPointRecord.query.get_or_404(id)
    
    # Verificaciones de seguridad
    checkpoint_id = session.get('checkpoint_id')
    if record.checkpoint_id != checkpoint_id:
        flash('Registro no válido para este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    # Verificar que el registro no tenga ya una salida
    if record.check_out_time:
        flash('Este fichaje ya tiene registrada una salida.', 'warning')
        return redirect(url_for('checkpoints.record_details', id=record.id))
    
    # Obtener el empleado asociado al registro
    employee = record.employee
    
    try:
        # Obtener estado actual para logging
        old_status = employee.is_on_shift
        
        # Usar una transacción anidada para mayor seguridad
        db.session.begin_nested()
        
        # 1. Actualizar el registro con la hora de salida
        current_time = get_current_time()
        record.check_out_time = current_time
        db.session.add(record)
        db.session.flush()  # Aseguramos que record tenga todos sus campos actualizados
        
        # Guardar siempre el registro original primero al hacer checkout
        from models_checkpoints import CheckPointOriginalRecord
        
        # Capturar los valores reales antes de cualquier ajuste
        # Importante: Guardamos la hora exacta que se introdujo al inicio de la jornada
        original_checkin = record.check_in_time  # Esta es la hora original de entrada
        original_checkout = current_time  # Esta es la hora real de salida antes de ajustes
        
        # Buscar si ya existe un registro original al iniciar jornada
        existing_original = CheckPointOriginalRecord.query.filter_by(
            record_id=record.id,
            adjustment_reason="Registro original al iniciar fichaje"
        ).first()
        
        if existing_original:
            # Actualizar el registro existente con los datos de salida
            existing_original.original_check_out_time = original_checkout
            existing_original.original_signature_data = record.signature_data
            existing_original.original_has_signature = record.has_signature
            existing_original.original_notes = record.notes
            existing_original.adjustment_reason = "Registro original completo (ficha entrada/salida pantalla detalles)"
            db.session.add(existing_original)
            # Guardar referencia para usar después con ajustes
            original_record = existing_original
        else:
            # Si no existe (caso poco probable), crear uno nuevo
            original_record = CheckPointOriginalRecord(
                record_id=record.id,
                original_check_in_time=original_checkin,
                original_check_out_time=original_checkout,
                original_signature_data=record.signature_data,
                original_has_signature=record.has_signature,
                original_notes=record.notes,
                adjustment_reason="Registro original al finalizar fichaje desde pantalla de detalles"
            )
            db.session.add(original_record)
        
        # 2. Verificar configuración de horas de contrato
        contract_hours = EmployeeContractHours.query.filter_by(employee_id=employee.id).first()
        if contract_hours:
            # Verificar si se debe ajustar el horario según configuración
            adjusted_checkin, adjusted_checkout = contract_hours.calculate_adjusted_hours(
                original_checkin, original_checkout
            )
            
            # Verificar si hay ajustes a realizar
            needs_adjustment = (adjusted_checkin and adjusted_checkin != original_checkin) or \
                              (adjusted_checkout and adjusted_checkout != original_checkout)
            
            # Si hay ajustes, actualizar el registro
            if needs_adjustment:
                # Actualizar el registro con los valores ajustados
                if adjusted_checkin and adjusted_checkin != original_checkin:
                    record.check_in_time = adjusted_checkin
                    # Marcar con R en lugar de crear incidencia
                    record.notes = (record.notes or "") + f" [R] Hora entrada ajustada de {original_checkin.strftime('%H:%M')} a {adjusted_checkin.strftime('%H:%M')}"
                
                if adjusted_checkout and adjusted_checkout != original_checkout:
                    record.check_out_time = adjusted_checkout
                    # Marcar con R en lugar de crear incidencia
                    record.notes = (record.notes or "") + f" [R] Hora salida ajustada de {original_checkout.strftime('%H:%M:%S')} a {adjusted_checkout.strftime('%H:%M:%S')}"
                
                # Marcar como ajustado
                record.adjusted = True
                
                # Actualizar la razón del ajuste en el registro original
                original_record.adjustment_reason = "Ajuste automático por límite de horas de contrato"
                db.session.add(original_record)
            
            # Verificar si hay horas extra
            duration = (record.check_out_time - record.check_in_time).total_seconds() / 3600
            if contract_hours.is_overtime(duration):
                overtime_hours = duration - contract_hours.daily_hours
                create_schedule_incident(
                    record, 
                    CheckPointIncidentType.OVERTIME,
                    f"Jornada con {overtime_hours:.2f} horas extra sobre el límite diario de {contract_hours.daily_hours} horas"
                )
        
        # 3. Actualizar el estado del empleado
        employee.is_on_shift = False
        db.session.add(employee)
        
        # Confirmar la transacción
        db.session.commit()
        
        # Log detallado post-transacción
        print(f"✅ CHECKOUT (pantalla detalles): Empleado ID {employee.id} - Estado: {old_status} → {employee.is_on_shift}")
        print(f"   Registro ID: {record.id}, Entrada: {record.check_in_time}, Salida: {record.check_out_time}")
        
        # Notificar al usuario
        flash(f'Jornada finalizada correctamente para {employee.first_name} {employee.last_name}. Por favor, firma tu registro.', 'success')
        
        # Redirigir directamente a la página de firma
        return redirect(url_for('checkpoints.checkpoint_record_signature', id=record.id))
    except Exception as e:
        # Rollback en caso de error
        db.session.rollback()
        
        # Log detallado del error
        error_details = f"ERROR en checkout desde detalles: {str(e)}"
        print(f"❌ {error_details}")
        print(f"   Registro ID: {record.id}, Empleado ID: {employee.id}")
        
        # Notificar al usuario
        flash(f'Error al registrar la salida: {str(e)}', 'danger')
        
        # En caso de error, redirigir a la página de detalles
        return redirect(url_for('checkpoints.record_details', id=record.id))


@checkpoints_bp.route('/record/<int:id>/signature_pad', methods=['GET', 'POST'])
@checkpoint_required
def checkpoint_record_signature(id):
    """Permite al empleado firmar un registro de fichaje desde el punto de fichaje"""
    record = CheckPointRecord.query.get_or_404(id)
    
    # Verificaciones de seguridad
    checkpoint_id = session.get('checkpoint_id')
    if record.checkpoint_id != checkpoint_id:
        flash('Registro no válido para este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    # Verificar que el registro tenga salida (no se puede firmar sin checkout)
    if not record.check_out_time:
        flash('No se puede firmar un registro sin hora de salida.', 'warning')
        return redirect(url_for('checkpoints.record_details', id=record.id))
    
    # Verificar que no esté ya firmado
    if record.has_signature:
        flash('Este registro ya ha sido firmado anteriormente.', 'info')
        return redirect(url_for('checkpoints.record_details', id=record.id))
    
    # Inicializar formulario
    form = SignaturePadForm()
    form.record_id.data = record.id
    
    # Procesar el formulario si es submit
    if form.validate_on_submit():
        try:
            # Usar una transacción anidada para mayor seguridad
            db.session.begin_nested()
            
            # Guardar los datos de la firma
            signature_data = form.signature_data.data
            
            # Actualizar el registro
            record.signature_data = signature_data
            record.has_signature = True
            
            # Guardar cambios
            db.session.add(record)
            db.session.commit()
            
            # Log detallado post-transacción (solo para el servidor, no visible para el usuario)
            print(f"✅ FIRMA REGISTRADA: Registro ID {record.id}, Empleado ID {record.employee_id}")
            
            # Redirigir directamente a la pantalla de empleados (dashboard) sin mostrar notificación
            return redirect(url_for('checkpoints.checkpoint_dashboard'))
        except Exception as e:
            # Rollback en caso de error
            db.session.rollback()
            
            # Log detallado del error
            error_details = f"ERROR al guardar la firma: {str(e)}"
            print(f"❌ {error_details}")
            print(f"   Registro ID: {record.id}, Empleado ID: {record.employee_id}")
            
            # Notificar al usuario
            flash(f'Error al guardar la firma: {str(e)}', 'danger')
    
    # Renderizar la plantilla con los datos necesarios
    return render_template(
        'checkpoints/signature_pad.html', 
        form=form,
        record=record
    )


@checkpoints_bp.route('/daily-report')
@checkpoint_required
def daily_report():
    """Muestra un informe de fichajes del día actual"""
    checkpoint_id = session.get('checkpoint_id')
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    
    # Obtener fecha actual
    today = date.today()
    
    # Obtener todos los registros del día para este punto de fichaje
    records = CheckPointRecord.query.filter(
        CheckPointRecord.checkpoint_id == checkpoint_id,
        func.date(CheckPointRecord.check_in_time) == today
    ).order_by(CheckPointRecord.check_in_time).all()
    
    # Obtener empleados de la empresa
    company_id = checkpoint.company_id
    all_employees = Employee.query.filter_by(company_id=company_id, is_active=True).all()
    
    # Obtener IDs de empleados que han fichado hoy
    checked_in_ids = [record.employee_id for record in records]
    
    # Obtener empleados que no han fichado hoy
    missing_employees = [emp for emp in all_employees if emp.id not in checked_in_ids]
    
    # Contar registros pendientes de checkout
    pending_checkout = sum(1 for record in records if record.check_out_time is None)
    
    # Preparar estadísticas
    stats = {
        'total_employees': len(all_employees),
        'checked_in': len(checked_in_ids),
        'pending_checkout': pending_checkout
    }
    
    return render_template('checkpoints/daily_report.html', 
                         checkpoint=checkpoint,
                         records=records,
                         today=today,
                         stats=stats,
                         missing_employees=missing_employees)


@checkpoints_bp.route('/api/company-employees', methods=['GET'])
@checkpoint_required
def get_company_employees():
    """Devuelve la lista de empleados ACTIVOS de la empresa en formato JSON"""
    # Obtenemos la compañía del checkpoint actual en lugar de usar session['company_id']
    checkpoint_id = session.get('checkpoint_id')
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    company_id = checkpoint.company_id
    
    if not company_id:
        return jsonify([])
    
    # Obtenemos SOLO los empleados ACTIVOS de la empresa
    # Filtramos por is_active=True para mostrar solo empleados activos en el punto de fichaje
    employees = Employee.query.filter_by(
        company_id=company_id,
        is_active=True
    ).order_by(Employee.first_name, Employee.last_name).all()
    
    employees_data = []
    for employee in employees:
        # Verificar si el empleado tiene un fichaje pendiente (sin check_out)
        pending_record = CheckPointRecord.query.filter_by(
            employee_id=employee.id,
            checkpoint_id=session.get('checkpoint_id'),
            check_out_time=None
        ).first()
        
        employees_data.append({
            'id': employee.id,
            'name': f"{employee.first_name} {employee.last_name}",
            'position': employee.position,
            'has_pending_record': pending_record is not None,
            'is_on_shift': employee.is_on_shift,
            'dni_last_digits': ''.join(c for c in employee.dni if c.isdigit())[-4:] if len(''.join(c for c in employee.dni if c.isdigit())) >= 4 else ''.join(c for c in employee.dni if c.isdigit())
        })
    
    return jsonify(employees_data)


@checkpoints_bp.route('/api/validate-pin', methods=['POST'])
@checkpoint_required
def validate_pin():
    """Validar el PIN del empleado mediante AJAX"""
    if not request.is_json:
        return jsonify({"success": False, "message": "Se requiere un JSON"}), 400
        
    # Obtener datos de la solicitud
    data = request.get_json()
    employee_id = data.get('employee_id')
    pin = data.get('pin')
    
    if not employee_id or not pin:
        return jsonify({"success": False, "message": "Falta ID de empleado o PIN"}), 400
        
    # Obtener el checkpoint activo desde la sesión
    checkpoint_id = session.get('checkpoint_id')
    if not checkpoint_id:
        return jsonify({"success": False, "message": "Sesión de punto de fichaje no iniciada."}), 401
    
    # Verificar que el empleado existe y pertenece a la empresa del checkpoint
    employee = Employee.query.get(employee_id)
    if not employee:
        return jsonify({"success": False, "message": "Empleado no encontrado."}), 404
        
    checkpoint = CheckPoint.query.get(checkpoint_id)
    if not checkpoint:
        return jsonify({"success": False, "message": "Punto de fichaje no encontrado."}), 404
    
    if employee.company_id != checkpoint.company_id:
        return jsonify({"success": False, "message": "Empleado no pertenece a la empresa."}), 403
    
    # Verificar el PIN (últimos 4 dígitos del DNI, ignorando la letra final en DNI español)
    # Filtrar solo los dígitos del DNI
    dni_digits = ''.join(c for c in employee.dni if c.isdigit())
    pin_from_dni = dni_digits[-4:] if len(dni_digits) >= 4 else dni_digits
    
    if pin == pin_from_dni:
        # Verificar si hay un registro pendiente
        pending_record = CheckPointRecord.query.filter_by(
            employee_id=employee.id,
            checkpoint_id=checkpoint_id,
            check_out_time=None
        ).order_by(CheckPointRecord.check_in_time.desc()).first()
        
        action = "checkout" if pending_record else "checkin"
        
        return jsonify({
            "success": True, 
            "action": action,
            "is_on_shift": employee.is_on_shift
        })
    else:
        return jsonify({"success": False, "message": "PIN incorrecto"}), 401


# Endpoint para procesar auto-checkouts
@checkpoints_bp.route('/auto_checkout', methods=['GET', 'POST'])
@login_required
@checkpoint_required
def trigger_auto_checkout():
    """Endpoint para informar que el sistema de auto-checkout ha sido eliminado"""
    # Informar que la funcionalidad ha sido eliminada
    return jsonify({
        'success': False,
        'processed': 0,
        'message': 'El sistema de auto-checkout ha sido eliminado completamente. Ya no se realizarán cierres automáticos de fichajes.'
    }), 404

# Integrar el blueprint en la aplicación principal
@checkpoints_bp.route('/check_credentials', methods=['GET'])
def check_credentials():
    """Endpoint temporal para comprobar credenciales"""
    username = request.args.get('username', '')
    password = request.args.get('password', '')
    
    checkpoint = CheckPoint.query.filter_by(username=username).first()
    
    if checkpoint:
        is_valid = checkpoint.verify_password(password)
        return jsonify({
            'username': username,
            'checkpoint_id': checkpoint.id,
            'checkpoint_name': checkpoint.name,
            'password_valid': is_valid,
            'status': checkpoint.status.value
        })
    else:
        return jsonify({
            'username': username,
            'error': 'Usuario no encontrado'
        })

@checkpoints_bp.route('/run_closer', methods=['GET'])
@login_required
def run_closer():
    """
    Ejecuta manualmente el proceso de cierre automático de fichajes y muestra los resultados
    """
    from close_operation_hours import auto_close_pending_records, STARTUP_FILE
    from app import create_app
    import os
    
    # Solo los administradores pueden ejecutar esta función
    if not current_user.is_admin():
        flash('No tiene permisos para realizar esta acción', 'danger')
        return redirect(url_for('index'))
    
    # Detectar si es el primer inicio después de un redeploy
    is_first_startup = not os.path.exists(STARTUP_FILE)
    
    # Ejecutar el barrido y capturar la salida
    import io
    import sys
    from contextlib import redirect_stdout
    
    buffer = io.StringIO()
    result = "Iniciando barrido...\n"
    
    with redirect_stdout(buffer):
        try:
            # Eliminar el archivo de startup para forzar la detección de redeploy
            if os.path.exists(STARTUP_FILE):
                try:
                    os.remove(STARTUP_FILE)
                    print(f"✓ Archivo de startup eliminado para forzar la detección de redeploy")
                except Exception as e:
                    print(f"No se pudo eliminar el archivo de startup: {e}")
            else:
                print("Primer inicio del servicio (no existe archivo de startup)")
            
            # Crear un app context temporal para la ejecución
            app = create_app()
            with app.app_context():
                success = auto_close_pending_records()
                
            result = buffer.getvalue()
            
            if success is not None:
                if success:
                    flash('Proceso de cierre automático ejecutado correctamente', 'success')
                else:
                    flash('El proceso de cierre finalizó con errores', 'warning')
            else:
                flash('Proceso de cierre completado', 'info')
        except Exception as e:
            db.session.rollback()  # Asegurar rollback en caso de error
            result = f"Error durante el barrido: {str(e)}\n{buffer.getvalue()}"
            flash(f'Error al ejecutar el cierre automático: {str(e)}', 'danger')
    
    # Si no hay salida, proporcionar un mensaje informativo
    if not result or result.strip() == "Iniciando barrido...":
        result += "\nNo hay registros pendientes para cerrar en este momento."
    
    # Mostrar los resultados en una plantilla
    return render_template('checkpoints/closer_results.html', 
                          result=result, 
                          timestamp=datetime.now(),
                          is_first_startup=is_first_startup)

@checkpoints_bp.route('/verify_closures', methods=['GET'])
@login_required
@admin_required
def verify_closures():
    """
    Verifica el estado del sistema de cierre automático y muestra un informe detallado
    """
    from verify_checkpoint_closures import check_pending_records_after_hours
    import io
    import os
    from contextlib import redirect_stdout
    from close_operation_hours import STARTUP_FILE
    
    # Detectar si es el primer inicio después de un redeploy
    is_first_startup = not os.path.exists(STARTUP_FILE)
    
    # Intentar obtener el estado del servicio de cierre automático
    service_status = None
    try:
        from checkpoint_closer_service import get_service_status
        service_status = get_service_status()
    except Exception as e:
        current_app.logger.error(f"Error al obtener el estado del servicio de cierre automático: {str(e)}")
    
    # Ejecutar la verificación y capturar la salida
    buffer = io.StringIO()
    result = "Iniciando verificación...\n"
    
    with redirect_stdout(buffer):
        try:
            # Si existe, eliminar el archivo de startup para forzar la detección de redeploy
            if os.path.exists(STARTUP_FILE):
                try:
                    os.remove(STARTUP_FILE)
                    print(f"✓ Archivo de startup eliminado para forzar la detección de redeploy")
                except Exception as e:
                    print(f"No se pudo eliminar el archivo de startup: {e}")
            else:
                print("Primer inicio después de redeploy (no existe archivo de startup)")
            
            # Mostrar el estado del servicio
            if service_status:
                print(f"\nEstado del servicio de cierre automático:")
                print(f"- Servicio activo: {'Sí' if service_status['active'] else 'No'}")
                print(f"- Servicio en ejecución: {'Sí' if service_status['running'] else 'No'}")
                print(f"- Hilo vivo: {'Sí' if service_status['thread_alive'] else 'No'}")
                print(f"- Última ejecución: {service_status['last_run']}")
                print(f"- Próxima ejecución: {service_status['next_run']}")
                print(f"- Intervalo de verificación: {service_status['check_interval_minutes']} minutos")
                print()
            
            success = check_pending_records_after_hours()
            result = buffer.getvalue()
            
            if success:
                flash('Verificación completada: El sistema de cierre automático está funcionando correctamente', 'success')
            else:
                flash('Verificación completada: Se han detectado problemas en el sistema de cierre automático', 'warning')
        except Exception as e:
            result = f"Error durante la verificación: {str(e)}"
            flash('Error al ejecutar la verificación del sistema de cierre automático', 'danger')
    
    # Mostrar los resultados en la misma plantilla que usamos para el cierre manual
    return render_template('checkpoints/closer_results.html', 
                         result=result, 
                         timestamp=datetime.now(),
                         is_first_startup=is_first_startup,
                         service_status=service_status)

@checkpoints_bp.route('/delete_records', methods=['GET', 'POST'])
@login_required
@admin_required
def delete_records():
    """
    Ruta para eliminar registros de fichaje de un empleado específico en un rango de fechas.
    """
    from utils_checkpoints import delete_employee_records
    from forms_checkpoints import DeleteCheckPointRecordsForm
    
    # Obtener la empresa seleccionada
    company_id = session.get('selected_company_id')
    if not company_id:
        flash('Primero debe seleccionar una empresa.', 'danger')
        return redirect(url_for('checkpoints.select_company'))
        
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene permiso para gestionar esta empresa.', 'danger')
        return redirect(url_for('checkpoints.select_company'))
        
    # Crear el formulario
    form = DeleteCheckPointRecordsForm()
    
    # Obtener los empleados de la empresa para el selector
    employees = []
    try:
        employees = Employee.query.filter_by(company_id=company.id).order_by(Employee.first_name).all()
        form.employee_id.choices = [(e.id, f"{e.first_name} {e.last_name} - {e.dni}") for e in employees]
    except Exception as e:
        flash(f'Error al cargar empleados: {str(e)}', 'danger')
    
    if form.validate_on_submit():
        try:
            # Convertir fechas a objetos date
            start_date = datetime.strptime(form.start_date.data, '%Y-%m-%d').date()
            end_date = datetime.strptime(form.end_date.data, '%Y-%m-%d').date()
            employee_id = form.employee_id.data
            
            # Recuperar el empleado para el mensaje
            employee = Employee.query.get(employee_id)
            
            # Ejecutar la eliminación de registros
            result = delete_employee_records(employee_id, start_date, end_date)
            
            if result["success"]:
                # Si no se eliminó ningún registro
                if result["records_deleted"] == 0:
                    flash(f'No se encontraron registros para eliminar.', 'info')
                else:
                    # Mostrar un mensaje detallado incluyendo las incidencias eliminadas
                    message = (f'Se eliminaron {result["records_deleted"]} registros de fichaje, '
                              f'{result["original_records_deleted"]} registros originales y '
                              f'{result["incidents_deleted"]} incidencias del empleado '
                              f'{employee.first_name} {employee.last_name}.')
                    flash(message, 'success')
            else:
                flash(f'Error al eliminar registros: {result["message"]}', 'danger')
            
            return redirect(url_for('checkpoints.index_company', slug=company.get_slug()))
            
        except Exception as e:
            flash(f'Error al procesar la solicitud: {str(e)}', 'danger')
    
    # Mostrar el formulario
    return render_template('checkpoints/delete_records.html', 
                          form=form,
                          company=company,
                          title='Eliminar Registros de Fichaje')


def init_app(app):
    # Registrar el blueprint
    app.register_blueprint(checkpoints_bp)
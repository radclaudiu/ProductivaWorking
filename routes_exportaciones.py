import os
import logging
from datetime import datetime, timedelta
from math import ceil
from functools import wraps

from flask import Blueprint, render_template, request, jsonify, flash, redirect, url_for, send_file
from flask_login import login_required, current_user
from sqlalchemy import and_, or_, desc, asc
from fpdf import FPDF

from app import db
from models import Company, Employee
from models_checkpoints import CheckPointRecord, CheckPointOriginalRecord
from utils import can_manage_company

# Definir decorator manager_required localmente
def manager_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or (not current_user.is_admin() and not current_user.is_gerente()):
            flash('No tienes permiso para acceder a esta página.', 'danger')
            return redirect(url_for('main.index'))
        return f(*args, **kwargs)
    return decorated_function

logger = logging.getLogger(__name__)

# Crear blueprint para exportaciones
exportaciones_bp = Blueprint('exportaciones', __name__, url_prefix='/exportaciones')

def round_time_entry(time_obj, round_minutes):
    """
    Redondea la hora de entrada hacia arriba según los minutos especificados.
    
    Args:
        time_obj: Objeto time con la hora original
        round_minutes: Minutos para redondear (ej: 10, 15, 30)
        
    Returns:
        Objeto time con la hora redondeada
    """
    if not time_obj or round_minutes <= 0:
        return time_obj
    
    # Convertir a minutos totales
    total_minutes = time_obj.hour * 60 + time_obj.minute
    
    # Calcular el siguiente múltiplo de round_minutes
    if total_minutes % round_minutes != 0:
        rounded_minutes = ceil(total_minutes / round_minutes) * round_minutes
    else:
        rounded_minutes = total_minutes
    
    # Convertir de vuelta a horas y minutos
    hours = rounded_minutes // 60
    minutes = rounded_minutes % 60
    
    # Manejar overflow de 24 horas
    if hours >= 24:
        hours = 23
        minutes = 59
    
    return time_obj.replace(hour=hours, minute=minutes, second=0, microsecond=0)

@exportaciones_bp.route('/')
@login_required
@manager_required
def index():
    """Página principal de exportaciones."""
    return render_template('exportaciones/index.html', title='Exportaciones de Fichajes')

@exportaciones_bp.route('/api/companies')
@login_required
def get_companies():
    """API para obtener las empresas disponibles según el rol del usuario."""
    try:
        if current_user.is_admin():
            companies = Company.query.filter_by(is_active=True).all()
        elif current_user.is_gerente():
            companies = [c for c in current_user.companies if c.is_active]
        else:
            companies = []
        
        return jsonify({
            'success': True,
            'companies': [{'id': c.id, 'name': c.name} for c in companies]
        })
    except Exception as e:
        logger.error(f"Error al obtener empresas: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500

@exportaciones_bp.route('/api/employees/<int:company_id>')
@login_required
def get_employees(company_id):
    """API para obtener los empleados de una empresa específica."""
    try:
        # Verificar permisos
        if not can_manage_company(company_id):
            return jsonify({'success': False, 'error': 'Sin permisos para esta empresa'}), 403
        
        company = Company.query.get_or_404(company_id)
        employees = Employee.query.filter_by(company_id=company_id, is_active=True).order_by(Employee.first_name).all()
        
        return jsonify({
            'success': True,
            'employees': [
                {
                    'id': e.id,
                    'name': f"{e.first_name} {e.last_name}",
                    'dni': e.dni
                } for e in employees
            ]
        })
    except Exception as e:
        logger.error(f"Error al obtener empleados: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500

@exportaciones_bp.route('/generar', methods=['POST'])
@login_required
@manager_required
def generar_exportacion():
    """Genera la exportación de fichajes en PDF."""
    try:
        # Obtener datos del formulario
        company_id = request.form.get('company_id', type=int)
        employee_ids = request.form.getlist('employee_ids')
        record_type = request.form.get('record_type')  # 'original' o 'adjusted'
        start_date_str = request.form.get('start_date')
        end_date_str = request.form.get('end_date')
        apply_rounding = request.form.get('apply_rounding') == 'on'
        round_minutes = int(request.form.get('round_minutes', 0)) if apply_rounding else 0
        
        # Validar y convertir fechas
        if not start_date_str or not end_date_str:
            flash('Las fechas son requeridas', 'danger')
            return redirect(url_for('exportaciones.index'))
            
        start_date = datetime.strptime(start_date_str, '%Y-%m-%d').date()
        end_date = datetime.strptime(end_date_str, '%Y-%m-%d').date()
        
        # Validaciones
        if not company_id or not employee_ids or not record_type:
            flash('Faltan datos requeridos para la exportación', 'danger')
            return redirect(url_for('exportaciones.index'))
        
        # Verificar permisos
        if not can_manage_company(company_id):
            flash('Sin permisos para esta empresa', 'danger')
            return redirect(url_for('exportaciones.index'))
        
        # Obtener empresa
        company = Company.query.get_or_404(company_id)
        
        # Obtener empleados
        employees = Employee.query.filter(
            Employee.id.in_(employee_ids),
            Employee.company_id == company_id
        ).order_by(Employee.first_name).all()
        
        if not employees:
            flash('No se encontraron empleados válidos', 'danger')
            return redirect(url_for('exportaciones.index'))
        
        # Seleccionar tabla según el tipo de registro
        if record_type == 'original':
            table_class = CheckPointOriginalRecord
        else:
            table_class = CheckPointRecord
        
        # Crear PDF
        pdf = FPDF()
        pdf.add_page()
        pdf.set_font('Arial', 'B', 16)
        
        # Título
        title = f"Exportación de Fichajes - {company.name}"
        if apply_rounding:
            title += f" (Redondeo: {round_minutes} min)"
        pdf.cell(0, 10, title.encode('latin-1', 'replace').decode('latin-1'), 0, 1, 'C')
        
        pdf.set_font('Arial', '', 10)
        pdf.cell(0, 5, f"Tipo: {'Fichajes Originales' if record_type == 'original' else 'Fichajes Ajustados'}", 0, 1)
        pdf.cell(0, 5, f"Período: {start_date.strftime('%d/%m/%Y')} - {end_date.strftime('%d/%m/%Y')}", 0, 1)
        pdf.ln(5)
        
        # Procesar cada empleado
        for employee in employees:
            # Obtener registros del empleado (siempre usar CheckPointRecord)
            records = CheckPointRecord.query.filter(
                CheckPointRecord.employee_id == employee.id,
                db.func.date(CheckPointRecord.check_in_time) >= start_date,
                db.func.date(CheckPointRecord.check_in_time) <= end_date
            ).order_by(CheckPointRecord.check_in_time).all()
            
            if not records:
                continue
            
            # Encabezado del empleado
            pdf.set_font('Arial', 'B', 12)
            pdf.cell(0, 8, f"{employee.first_name} {employee.last_name} - DNI: {employee.dni}", 0, 1)
            
            # Encabezados de tabla
            pdf.set_font('Arial', 'B', 9)
            pdf.cell(25, 6, 'Fecha', 1, 0, 'C')
            pdf.cell(20, 6, 'Entrada', 1, 0, 'C')
            pdf.cell(20, 6, 'Salida', 1, 0, 'C')
            pdf.cell(20, 6, 'Horas', 1, 0, 'C')
            pdf.cell(30, 6, 'Ubicación', 1, 0, 'C')
            if apply_rounding:
                pdf.cell(25, 6, 'Entrada Orig.', 1, 0, 'C')
            pdf.cell(40, 6, 'Observaciones', 1, 1, 'C')
            
            # Datos del empleado
            pdf.set_font('Arial', '', 8)
            total_minutes = 0
            
            # Agrupar registros por fecha
            records_by_date = {}
            for record in records:
                date_key = record.check_in_time.date()
                if date_key not in records_by_date:
                    records_by_date[date_key] = {'check_in': None, 'check_out': None}
                
                # Determinar si es entrada o salida basándose en si ya hay check_in
                if records_by_date[date_key]['check_in'] is None:
                    records_by_date[date_key]['check_in'] = record
                elif record.check_out_time:
                    records_by_date[date_key]['check_out'] = record
            
            # Procesar cada día
            for date_key in sorted(records_by_date.keys()):
                day_data = records_by_date[date_key]
                check_in_record = day_data['check_in']
                
                if check_in_record:
                    # Usar horario original o ajustado según selección
                    if record_type == 'original' and check_in_record.original_check_in_time:
                        original_time = check_in_record.original_check_in_time.time()
                        check_out_time = check_in_record.original_check_out_time
                    else:
                        original_time = check_in_record.check_in_time.time()
                        check_out_time = check_in_record.check_out_time
                    
                    # Aplicar redondeo si está habilitado
                    display_time = round_time_entry(original_time, round_minutes) if apply_rounding else original_time
                    
                    # Calcular horas trabajadas
                    if check_out_time:
                        start_datetime = datetime.combine(date_key, display_time)
                        end_datetime = check_out_time if isinstance(check_out_time, datetime) else datetime.combine(date_key, check_out_time.time())
                        
                        if end_datetime > start_datetime:
                            worked_minutes = (end_datetime - start_datetime).total_seconds() / 60
                            total_minutes += worked_minutes
                            hours_str = f"{int(worked_minutes // 60):02d}:{int(worked_minutes % 60):02d}"
                        else:
                            hours_str = "Error"
                    else:
                        hours_str = "Pendiente"
                    
                    # Escribir fila en PDF
                    pdf.cell(25, 5, date_key.strftime('%d/%m/%Y'), 1, 0, 'C')
                    pdf.cell(20, 5, display_time.strftime('%H:%M'), 1, 0, 'C')
                    pdf.cell(20, 5, check_out_time.strftime('%H:%M') if check_out_time else '-', 1, 0, 'C')
                    pdf.cell(20, 5, hours_str, 1, 0, 'C')
                    
                    # Ubicación
                    location = getattr(check_in_record.checkpoint, 'location', 'N/A') or 'N/A'
                    if len(location) > 12:
                        location = location[:12] + '...'
                    pdf.cell(30, 5, location.encode('latin-1', 'replace').decode('latin-1'), 1, 0, 'C')
                    
                    # Mostrar hora original si aplica redondeo
                    if apply_rounding:
                        pdf.cell(25, 5, original_time.strftime('%H:%M'), 1, 0, 'C')
                    
                    # Observaciones
                    obs = ""
                    if apply_rounding and original_time != display_time:
                        diff_minutes = (datetime.combine(date_key, display_time) - datetime.combine(date_key, original_time)).total_seconds() / 60
                        obs = f"Ajuste: +{int(diff_minutes)}min"
                    
                    pdf.cell(40, 5, obs.encode('latin-1', 'replace').decode('latin-1'), 1, 1, 'C')
            
            # Total de horas del empleado
            total_hours = f"{int(total_minutes // 60):02d}:{int(total_minutes % 60):02d}"
            pdf.set_font('Arial', 'B', 9)
            pdf.cell(0, 6, f"Total horas trabajadas: {total_hours}", 0, 1, 'R')
            pdf.ln(3)
        
        # Pie de página
        pdf.ln(5)
        pdf.set_font('Arial', 'I', 8)
        pdf.cell(0, 5, f"Generado el {datetime.now().strftime('%d/%m/%Y a las %H:%M')}", 0, 1, 'C')
        
        # Guardar PDF
        filename = f"fichajes_{company.name.replace(' ', '_')}_{start_date.strftime('%Y%m%d')}_{end_date.strftime('%Y%m%d')}.pdf"
        filepath = os.path.join('temp', filename)
        
        # Crear directorio temp si no existe
        os.makedirs('temp', exist_ok=True)
        
        pdf.output(filepath, 'F')
        
        return send_file(filepath, as_attachment=True, download_name=filename)
        
    except Exception as e:
        logger.error(f"Error al generar exportación: {str(e)}")
        flash(f'Error al generar exportación: {str(e)}', 'danger')
        return redirect(url_for('exportaciones.index'))
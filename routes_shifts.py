"""
Routes para el módulo de sistema de turnos (CrearTurno).

Permite a los usuarios crear, modificar y gestionar programaciones de turnos para empleados.
Proporciona funcionalidad para asignar turnos mediante interfaz de arrastrar y soltar,
generar reportes y exportar los horarios.
"""

import os
import json
from datetime import datetime, timedelta

from flask import Blueprint, render_template, redirect, url_for, request, jsonify, current_app
from flask_login import login_required, current_user
from sqlalchemy import func

from models import Employee, Company
from utils import check_company_access

# Crear blueprint
shifts_bp = Blueprint('shifts', __name__, url_prefix='/shifts')

# Modelo para las programaciones de turnos
class ShiftSchedule:
    def __init__(self, id, name, company_id, start_date, end_date, description=None, status='draft', created_at=None):
        self.id = id
        self.name = name
        self.company_id = company_id
        self.start_date = start_date
        self.end_date = end_date
        self.description = description
        self.status = status
        self.created_at = created_at or datetime.now()

# Modelo para los turnos individuales
class Shift:
    def __init__(self, id, schedule_id, employee_id, date, start_time, end_time, 
                 break_time=0, notes=None, employee_name=None):
        self.id = id
        self.schedule_id = schedule_id
        self.employee_id = employee_id
        self.date = date
        self.start_time = start_time
        self.end_time = end_time
        self.break_time = break_time
        self.notes = notes
        self.employee_name = employee_name

# Lista temporal para almacenar programaciones (esto se reemplazará por base de datos)
schedules = []
shifts = []

# Funciones auxiliares
def format_date(date_str):
    """Formatea una fecha en formato YYYY-MM-DD a DD/MM/YYYY."""
    if not date_str:
        return ""
    parts = date_str.split('-')
    if len(parts) != 3:
        return date_str
    return f"{parts[2]}/{parts[1]}/{parts[0]}"

def init_shifts_module(app):
    """Inicializa el módulo de turnos."""
    # Agregar filtros para Jinja
    @app.template_filter('datetime_from_string')
    def datetime_from_string(value):
        """Convierte un string con formato YYYY-MM-DD a un objeto datetime."""
        if not value:
            return datetime.now()
        try:
            return datetime.strptime(value, '%Y-%m-%d')
        except (ValueError, TypeError):
            return datetime.now()
            
    @app.template_filter('date_format')
    def date_format(value):
        """Formatea un objeto datetime a DD/MM/YYYY."""
        if not value:
            return ""
        try:
            if isinstance(value, str):
                dt = datetime.strptime(value, '%Y-%m-%d')
                return dt.strftime('%d/%m/%Y')
            return value.strftime('%d/%m/%Y')
        except (ValueError, TypeError, AttributeError):
            return str(value)
    
    @app.context_processor
    def inject_now():
        """Inyecta la función now() para acceder a la fecha actual en las plantillas."""
        return {
            'now': datetime.now,
            'timedelta': timedelta
        }
    
    # Añadir datos de ejemplo para desarrollo
    if not schedules:
        schedules.append(ShiftSchedule(
            id=1,
            name="Turnos Semana 25-2023",
            company_id=8,
            start_date="2023-06-19",
            end_date="2023-06-25",
            status="published",
            created_at=datetime.now() - timedelta(days=7)
        ))
        
        # Algunos turnos de ejemplo
        shifts.append(Shift(
            id=1,
            schedule_id=1,
            employee_id=100,
            date="2023-06-19",
            start_time="09:00",
            end_time="18:00",
            break_time=60,
            employee_name="Juan Pérez"
        ))
        
        shifts.append(Shift(
            id=2,
            schedule_id=1,
            employee_id=101,
            date="2023-06-19",
            start_time="14:00",
            end_time="22:00",
            break_time=30,
            employee_name="María López"
        ))

# Rutas
@shifts_bp.route('/')
@login_required
def index():
    """Página principal del módulo de turnos."""
    if current_user.is_admin():
        companies = Company.query.all()
    else:
        companies = current_user.companies
    
    return render_template('shifts/index.html', 
                          title="Sistema de Turnos", 
                          companies=companies)

@shifts_bp.route('/company/<int:company_id>')
@login_required
def company_dashboard(company_id):
    """Dashboard de turnos para una empresa específica."""
    company = check_company_access(company_id)
    
    # Filtrar programaciones por empresa
    company_schedules = [s for s in schedules if s.company_id == company_id]
    
    return render_template('shifts/company_dashboard.html',
                          title=f"Turnos - {company.name}",
                          company=company,
                          schedules=company_schedules)

@shifts_bp.route('/company/<int:company_id>/new', methods=['GET', 'POST'])
@login_required
def new_schedule(company_id):
    """Crear una nueva programación de turnos."""
    global schedules
    
    company = check_company_access(company_id)
    
    if request.method == 'POST':
        name = request.form.get('name')
        description = request.form.get('description')
        start_date = request.form.get('start_date')
        end_date = request.form.get('end_date')
        
        # Crear nueva programación
        schedule_id = len(schedules) + 1
        schedule = ShiftSchedule(
            id=schedule_id,
            name=name,
            company_id=company_id,
            start_date=start_date,
            end_date=end_date,
            description=description,
            status='draft',
            created_at=datetime.now()
        )
        
        schedules.append(schedule)
        
        return redirect(url_for('shifts.edit_schedule', schedule_id=schedule_id))
    
    return render_template('shifts/new_schedule.html',
                          title="Nueva programación de turnos",
                          company=company)

@shifts_bp.route('/schedule/<int:schedule_id>')
@login_required
def view_schedule(schedule_id):
    """Ver una programación de turnos existente."""
    schedule = next((s for s in schedules if s.id == schedule_id), None)
    if not schedule:
        return redirect(url_for('shifts.index'))
    
    company = check_company_access(schedule.company_id)
    
    # Obtener empleados de la empresa
    employees = Employee.query.filter_by(company_id=company.id, is_active=True).all()
    
    # Obtener turnos para esta programación
    schedule_shifts = [s for s in shifts if s.schedule_id == schedule_id]
    
    # Organizar turnos por fecha y empleado
    shifts_by_date = {}
    for shift in schedule_shifts:
        if shift.date not in shifts_by_date:
            shifts_by_date[shift.date] = {}
        shifts_by_date[shift.date][shift.employee_id] = shift
    
    return render_template('shifts/view_schedule.html',
                          title=f"Ver programación - {schedule.name}",
                          schedule=schedule,
                          company=company,
                          employees=employees,
                          shifts_by_date=shifts_by_date)

@shifts_bp.route('/schedule/<int:schedule_id>/edit')
@login_required
def edit_schedule(schedule_id):
    """Editar una programación de turnos existente."""
    schedule = next((s for s in schedules if s.id == schedule_id), None)
    if not schedule:
        return redirect(url_for('shifts.index'))
    
    company = check_company_access(schedule.company_id)
    
    # Obtener empleados de la empresa
    employees = Employee.query.filter_by(company_id=company.id).all()
    
    return render_template('shifts/edit_schedule.html',
                          title=f"Editar programación - {schedule.name}",
                          schedule=schedule,
                          company=company,
                          employees=employees)

@shifts_bp.route('/schedule/<int:schedule_id>/export/<format>')
@login_required
def export_schedule(schedule_id, format):
    """Exportar una programación de turnos en diferentes formatos."""
    schedule = next((s for s in schedules if s.id == schedule_id), None)
    if not schedule:
        return redirect(url_for('shifts.index'))
    
    company = check_company_access(schedule.company_id)
    
    if format == 'pdf':
        # Implementación pendiente - crear PDF
        return "Exportar a PDF - Implementación pendiente", 501
    elif format == 'excel':
        # Implementación pendiente - crear Excel
        return "Exportar a Excel - Implementación pendiente", 501
    else:
        return redirect(url_for('shifts.view_schedule', schedule_id=schedule_id))

# API para interacción con el editor de turnos
@shifts_bp.route('/api/schedules/<int:schedule_id>')
@login_required
def api_get_schedule(schedule_id):
    """API para obtener los datos de una programación y sus turnos."""
    schedule = next((s for s in schedules if s.id == schedule_id), None)
    if not schedule:
        return jsonify({'error': 'Programación no encontrada'}), 404
    
    try:
        company = check_company_access(schedule.company_id)
    except:
        return jsonify({'error': 'No tiene permiso para acceder a esta programación'}), 403
    
    # Obtener turnos para esta programación
    schedule_shifts = [s for s in shifts if s.schedule_id == schedule_id]
    
    # Convertir a diccionarios para serialización JSON
    schedule_dict = {
        'id': schedule.id,
        'name': schedule.name,
        'company_id': schedule.company_id,
        'start_date': schedule.start_date,
        'end_date': schedule.end_date,
        'description': schedule.description,
        'status': schedule.status,
        'created_at': schedule.created_at.strftime('%d/%m/%Y %H:%M')
    }
    
    shifts_dict = []
    for shift in schedule_shifts:
        shifts_dict.append({
            'id': shift.id,
            'schedule_id': shift.schedule_id,
            'employee_id': shift.employee_id,
            'date': shift.date,
            'start_time': shift.start_time,
            'end_time': shift.end_time,
            'break_time': shift.break_time,
            'notes': shift.notes,
            'employee_name': shift.employee_name or 'Empleado'
        })
    
    return jsonify({
        'schedule': schedule_dict,
        'shifts': shifts_dict
    })

@shifts_bp.route('/api/shifts', methods=['POST'])
@login_required
def api_create_shift():
    """API para crear un nuevo turno."""
    global shifts
    
    data = request.json
    if not data:
        return jsonify({'error': 'Datos no válidos'}), 400
    
    schedule_id = data.get('schedule_id')
    schedule = next((s for s in schedules if s.id == schedule_id), None)
    if not schedule:
        return jsonify({'error': 'Programación no encontrada'}), 404
    
    try:
        company = check_company_access(schedule.company_id)
    except:
        return jsonify({'error': 'No tiene permiso para acceder a esta programación'}), 403
    
    # Crear nuevo turno
    shift_id = len(shifts) + 1
    employee_id = data.get('employee_id')
    
    # Obtener nombre del empleado
    employee = Employee.query.get(employee_id)
    employee_name = f"{employee.first_name} {employee.last_name}" if employee else "Empleado desconocido"
    
    shift = Shift(
        id=shift_id,
        schedule_id=schedule_id,
        employee_id=employee_id,
        date=data.get('date'),
        start_time=data.get('start_time'),
        end_time=data.get('end_time'),
        break_time=data.get('break_time', 0),
        notes=data.get('notes'),
        employee_name=employee_name
    )
    
    shifts.append(shift)
    
    return jsonify({
        'id': shift.id,
        'schedule_id': shift.schedule_id,
        'employee_id': shift.employee_id,
        'date': shift.date,
        'start_time': shift.start_time,
        'end_time': shift.end_time,
        'break_time': shift.break_time,
        'notes': shift.notes,
        'employee_name': shift.employee_name
    })

@shifts_bp.route('/api/shifts/<int:shift_id>', methods=['PUT'])
@login_required
def api_update_shift(shift_id):
    """API para actualizar un turno existente."""
    global shifts
    
    shift = next((s for s in shifts if s.id == shift_id), None)
    if not shift:
        return jsonify({'error': 'Turno no encontrado'}), 404
    
    schedule = next((s for s in schedules if s.id == shift.schedule_id), None)
    if not schedule:
        return jsonify({'error': 'Programación no encontrada'}), 404
    
    try:
        company = check_company_access(schedule.company_id)
    except:
        return jsonify({'error': 'No tiene permiso para acceder a esta programación'}), 403
    
    data = request.json
    if not data:
        return jsonify({'error': 'Datos no válidos'}), 400
    
    # Actualizar turno
    shift.start_time = data.get('start_time', shift.start_time)
    shift.end_time = data.get('end_time', shift.end_time)
    shift.break_time = data.get('break_time', shift.break_time)
    shift.notes = data.get('notes', shift.notes)
    
    return jsonify({
        'id': shift.id,
        'schedule_id': shift.schedule_id,
        'employee_id': shift.employee_id,
        'date': shift.date,
        'start_time': shift.start_time,
        'end_time': shift.end_time,
        'break_time': shift.break_time,
        'notes': shift.notes,
        'employee_name': shift.employee_name
    })

@shifts_bp.route('/api/shifts/<int:shift_id>', methods=['DELETE'])
@login_required
def api_delete_shift(shift_id):
    """API para eliminar un turno existente."""
    global shifts
    
    shift = next((s for s in shifts if s.id == shift_id), None)
    if not shift:
        return jsonify({'error': 'Turno no encontrado'}), 404
    
    schedule = next((s for s in schedules if s.id == shift.schedule_id), None)
    if not schedule:
        return jsonify({'error': 'Programación no encontrada'}), 404
    
    try:
        company = check_company_access(schedule.company_id)
    except:
        return jsonify({'error': 'No tiene permiso para acceder a esta programación'}), 403
    
    # Eliminar turno
    shifts = [s for s in shifts if s.id != shift_id]
    
    return jsonify({'success': True})
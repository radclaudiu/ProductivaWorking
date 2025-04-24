"""
Blueprint para el módulo de CrearTurno (Sistema de Creación de Turnos)

Este módulo integra la funcionalidad de CrearTurno en Productiva, permitiendo
la creación y gestión de turnos para empleados mediante una interfaz de arrastrar y soltar.
"""

import os
import json
import logging
from datetime import datetime, timedelta
from flask import Blueprint, render_template, request, jsonify, redirect, url_for, flash, session, current_app
from flask_login import login_required, current_user

from models import Company, Employee, User
from app import db
from utils import check_company_access

# Configurar logging
logger = logging.getLogger(__name__)

# Crear blueprint para el sistema de turnos
shifts_bp = Blueprint('shifts', __name__, url_prefix='/shifts')

# Modelos para el sistema de turnos
class ShiftSchedule(db.Model):
    """Modelo para almacenar programaciones de turnos"""
    __tablename__ = 'shift_schedules'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(128), nullable=False)
    description = db.Column(db.Text)
    company_id = db.Column(db.Integer, db.ForeignKey('companies.id'), nullable=False)
    start_date = db.Column(db.String(20), nullable=False)  # Formato: YYYY-MM-DD
    end_date = db.Column(db.String(20), nullable=False)    # Formato: YYYY-MM-DD
    status = db.Column(db.String(20), default='draft')     # draft, published, archived
    created_by = db.Column(db.Integer, db.ForeignKey('users.id'))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    company = db.relationship('Company', backref=db.backref('shift_schedules', lazy=True))
    creator = db.relationship('User', backref=db.backref('created_schedules', lazy=True))
    shifts = db.relationship('Shift', backref='schedule', lazy=True, cascade='all, delete-orphan')
    
    def to_dict(self):
        """Convierte el modelo a un diccionario para API JSON"""
        return {
            'id': self.id,
            'name': self.name,
            'description': self.description,
            'company_id': self.company_id,
            'company_name': self.company.name if self.company else None,
            'start_date': self.start_date,
            'end_date': self.end_date,
            'status': self.status,
            'created_by': self.created_by,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None,
        }

class Shift(db.Model):
    """Modelo para almacenar turnos individuales de empleados"""
    __tablename__ = 'shifts'
    
    id = db.Column(db.Integer, primary_key=True)
    schedule_id = db.Column(db.Integer, db.ForeignKey('shift_schedules.id'), nullable=False)
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'), nullable=False)
    date = db.Column(db.String(20), nullable=False)       # Formato: YYYY-MM-DD
    start_time = db.Column(db.String(10), nullable=False) # Formato: HH:MM
    end_time = db.Column(db.String(10), nullable=False)   # Formato: HH:MM
    break_time = db.Column(db.Integer, default=0)         # Tiempo de descanso en minutos
    notes = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    employee = db.relationship('Employee', backref=db.backref('shifts', lazy=True))
    
    def to_dict(self):
        """Convierte el modelo a un diccionario para API JSON"""
        return {
            'id': self.id,
            'schedule_id': self.schedule_id,
            'employee_id': self.employee_id,
            'employee_name': f"{self.employee.first_name} {self.employee.last_name}" if self.employee else None,
            'date': self.date,
            'start_time': self.start_time,
            'end_time': self.end_time,
            'break_time': self.break_time,
            'notes': self.notes,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None,
        }

# Rutas del módulo
@shifts_bp.route('/')
@login_required
def index():
    """Página principal del módulo de turnos"""
    # Obtener las empresas a las que tiene acceso el usuario
    companies = []
    
    if current_user.is_admin():
        # Los administradores ven todas las empresas
        companies = Company.query.filter_by(is_active=True).all()
    else:
        # Los gerentes/empleados ven solo sus empresas asignadas
        companies = current_user.companies
    
    return render_template('shifts/index.html', 
                           companies=companies,
                           title="Sistema de Turnos")

@shifts_bp.route('/company/<int:company_id>')
@login_required
def company_dashboard(company_id):
    """Dashboard de turnos para una empresa específica"""
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    check_company_access(company)
    
    # Obtener las programaciones de turnos de la empresa
    schedules = ShiftSchedule.query.filter_by(company_id=company_id).order_by(ShiftSchedule.created_at.desc()).all()
    
    return render_template('shifts/company_dashboard.html', 
                           company=company,
                           schedules=schedules,
                           title=f"Turnos - {company.name}")

@shifts_bp.route('/schedules/new/<int:company_id>', methods=['GET', 'POST'])
@login_required
def new_schedule(company_id):
    """Crear nueva programación de turnos"""
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    check_company_access(company)
    
    if request.method == 'POST':
        # Procesar formulario
        name = request.form.get('name')
        description = request.form.get('description', '')
        start_date = request.form.get('start_date')
        end_date = request.form.get('end_date')
        
        # Validar datos
        if not name or not start_date or not end_date:
            flash('Todos los campos marcados con * son obligatorios', 'danger')
            return redirect(url_for('shifts.new_schedule', company_id=company_id))
        
        # Crear nueva programación
        schedule = ShiftSchedule(
            name=name,
            description=description,
            company_id=company_id,
            start_date=start_date,
            end_date=end_date,
            created_by=current_user.id
        )
        
        try:
            db.session.add(schedule)
            db.session.commit()
            flash('Programación de turnos creada con éxito', 'success')
            return redirect(url_for('shifts.edit_schedule', schedule_id=schedule.id))
        except Exception as e:
            db.session.rollback()
            logger.error(f"Error al crear programación: {str(e)}")
            flash(f'Error al crear la programación: {str(e)}', 'danger')
    
    # GET - Mostrar formulario
    return render_template('shifts/new_schedule.html', 
                           company=company,
                           title="Nueva Programación de Turnos")

@shifts_bp.route('/schedules/<int:schedule_id>')
@login_required
def view_schedule(schedule_id):
    """Ver detalles de una programación de turnos"""
    # Obtener la programación
    schedule = ShiftSchedule.query.get_or_404(schedule_id)
    
    # Verificar acceso a la empresa
    check_company_access(schedule.company)
    
    # Obtener todos los empleados de la empresa para la leyenda
    employees = Employee.query.filter_by(company_id=schedule.company_id, is_active=True).all()
    
    # Obtener todos los turnos de esta programación
    shifts = Shift.query.filter_by(schedule_id=schedule_id).all()
    
    # Organizar los turnos por fecha y empleado para la visualización
    shifts_by_date = {}
    for shift in shifts:
        if shift.date not in shifts_by_date:
            shifts_by_date[shift.date] = {}
        
        shifts_by_date[shift.date][shift.employee_id] = shift
    
    return render_template('shifts/view_schedule.html', 
                           schedule=schedule,
                           company=schedule.company,
                           employees=employees,
                           shifts_by_date=shifts_by_date,
                           title=f"Programación - {schedule.name}")

@shifts_bp.route('/schedules/<int:schedule_id>/edit')
@login_required
def edit_schedule(schedule_id):
    """Editor de turnos (interfaz de arrastrar y soltar)"""
    # Obtener la programación
    schedule = ShiftSchedule.query.get_or_404(schedule_id)
    
    # Verificar acceso a la empresa
    check_company_access(schedule.company)
    
    # Obtener todos los empleados de la empresa
    employees = Employee.query.filter_by(company_id=schedule.company_id, is_active=True).all()
    
    return render_template('shifts/edit_schedule.html', 
                           schedule=schedule,
                           company=schedule.company,
                           employees=employees,
                           title=f"Editar Turnos - {schedule.name}")

# API endpoints para operaciones AJAX
@shifts_bp.route('/api/schedules/<int:schedule_id>', methods=['GET'])
@login_required
def api_get_schedule(schedule_id):
    """API para obtener datos de una programación y sus turnos"""
    schedule = ShiftSchedule.query.get_or_404(schedule_id)
    check_company_access(schedule.company)
    
    # Obtener todos los turnos
    shifts = Shift.query.filter_by(schedule_id=schedule_id).all()
    
    # Convertir a diccionarios para JSON
    schedule_dict = schedule.to_dict()
    shifts_dict = [shift.to_dict() for shift in shifts]
    
    return jsonify({
        'schedule': schedule_dict,
        'shifts': shifts_dict
    })

@shifts_bp.route('/api/shifts', methods=['POST'])
@login_required
def api_create_shift():
    """API para crear un nuevo turno"""
    data = request.json
    
    schedule_id = data.get('schedule_id')
    schedule = ShiftSchedule.query.get_or_404(schedule_id)
    check_company_access(schedule.company)
    
    # Validar que el empleado pertenece a la empresa
    employee_id = data.get('employee_id')
    employee = Employee.query.get_or_404(employee_id)
    if employee.company_id != schedule.company_id:
        return jsonify({'error': 'El empleado no pertenece a esta empresa'}), 400
    
    # Crear el turno
    shift = Shift(
        schedule_id=schedule_id,
        employee_id=employee_id,
        date=data.get('date'),
        start_time=data.get('start_time'),
        end_time=data.get('end_time'),
        break_time=data.get('break_time', 0),
        notes=data.get('notes', '')
    )
    
    try:
        db.session.add(shift)
        db.session.commit()
        return jsonify(shift.to_dict()), 201
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al crear turno: {str(e)}")
        return jsonify({'error': str(e)}), 500

@shifts_bp.route('/api/shifts/<int:shift_id>', methods=['PUT'])
@login_required
def api_update_shift(shift_id):
    """API para actualizar un turno existente"""
    data = request.json
    shift = Shift.query.get_or_404(shift_id)
    
    # Verificar acceso
    schedule = ShiftSchedule.query.get_or_404(shift.schedule_id)
    check_company_access(schedule.company)
    
    # Actualizar campos
    if 'date' in data:
        shift.date = data['date']
    if 'start_time' in data:
        shift.start_time = data['start_time']
    if 'end_time' in data:
        shift.end_time = data['end_time']
    if 'break_time' in data:
        shift.break_time = data['break_time']
    if 'notes' in data:
        shift.notes = data['notes']
    
    try:
        db.session.commit()
        return jsonify(shift.to_dict())
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al actualizar turno: {str(e)}")
        return jsonify({'error': str(e)}), 500

@shifts_bp.route('/api/shifts/<int:shift_id>', methods=['DELETE'])
@login_required
def api_delete_shift(shift_id):
    """API para eliminar un turno"""
    shift = Shift.query.get_or_404(shift_id)
    
    # Verificar acceso
    schedule = ShiftSchedule.query.get_or_404(shift.schedule_id)
    check_company_access(schedule.company)
    
    try:
        db.session.delete(shift)
        db.session.commit()
        return jsonify({'success': True})
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al eliminar turno: {str(e)}")
        return jsonify({'error': str(e)}), 500

@shifts_bp.route('/schedules/<int:schedule_id>/export', methods=['GET'])
@login_required
def export_schedule(schedule_id):
    """Exportar programación de turnos a PDF"""
    schedule = ShiftSchedule.query.get_or_404(schedule_id)
    check_company_access(schedule.company)
    
    # Obtener el formato solicitado (pdf o excel)
    export_format = request.args.get('format', 'pdf')
    
    if export_format == 'pdf':
        # En una implementación real, aquí generaríamos el PDF
        flash('Exportación a PDF implementada en desarrollo futuro', 'info')
    elif export_format == 'excel':
        # En una implementación real, aquí generaríamos el Excel
        flash('Exportación a Excel implementada en desarrollo futuro', 'info')
    
    return redirect(url_for('shifts.view_schedule', schedule_id=schedule_id))

# Función para registrar el blueprint en la aplicación principal
def init_shifts_module(app):
    """Inicializa el módulo de turnos en la aplicación Flask"""
    
    # Crear las tablas si no existen
    with app.app_context():
        db.create_all()
    
    # Añadir al contexto de Jinja2 para uso en plantillas
    @app.context_processor
    def inject_shift_stats():
        """Inyecta estadísticas de turnos en el contexto de Jinja2"""
        def get_employee_shifts_count(employee_id):
            """Retorna el número de turnos asignados a un empleado"""
            return Shift.query.filter_by(employee_id=employee_id).count()
        
        return dict(get_employee_shifts_count=get_employee_shifts_count)
    
    return shifts_bp
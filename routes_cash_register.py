"""
Rutas para el módulo de Arqueos de Caja.

Este módulo contiene las rutas para:
- Dashboard de arqueos
- Gestión de arqueos (crear, editar, eliminar)
- Generación de informes
- Sistema de tokens para empleados
"""

import os
from datetime import datetime, date, timedelta
import uuid
from flask import Blueprint, render_template, redirect, url_for, request, flash, jsonify, current_app, abort
from flask_login import login_required, current_user
from werkzeug.security import generate_password_hash
from app import db
from models import Company, User, Employee
from models_work_hours import CompanyWorkHours
from models_cash_register import CashRegister, CashRegisterSummary, CashRegisterToken
from utils_cash_register import (
    update_register_summary, update_staff_costs_for_summary,
    generate_token, validate_token, mark_token_used
)
from helpers import (
    admin_required, manager_required, company_membership_required, save_activity_log
)
from forms_cash_register import (
    CashRegisterForm, CashRegisterFilterForm, CashRegisterTokenForm,
    PublicCashRegisterForm
)

# Crear el blueprint
cash_register_bp = Blueprint('cash_register', __name__, url_prefix='/arqueos')

@cash_register_bp.route('/')
@login_required
def index():
    """
    Dashboard principal del módulo de arqueos.
    Muestra un resumen de los arqueos por empresa y permite filtrar.
    """
    # Obtener todas las empresas a las que tiene acceso el usuario
    if current_user.is_admin():
        companies = Company.query.all()
    else:
        companies = current_user.companies
    
    if not companies:
        flash('No tiene acceso a ninguna empresa para gestionar arqueos.', 'warning')
        return redirect(url_for('main.index'))
    
    # Si solo tiene acceso a una empresa, redirigir directamente a su dashboard
    if len(companies) == 1:
        return redirect(url_for('cash_register.company_dashboard', company_id=companies[0].id))
    
    # Si tiene acceso a varias, mostrar selector de empresa
    return render_template(
        'cash_register/index.html',
        companies=companies,
        title='Arqueos de Caja - Seleccionar Empresa'
    )

@cash_register_bp.route('/empresa/<int:company_id>')
@login_required
@company_membership_required
def company_dashboard(company_id):
    """
    Dashboard de arqueos para una empresa específica.
    """
    company = Company.query.get_or_404(company_id)
    
    # Obtener fecha actual y determinar rango de fechas para mostrar (por defecto última semana)
    today = date.today()
    end_date = today
    start_date = today - timedelta(days=6)  # Última semana
    
    # Permitir cambiar el rango por parámetros
    if request.args.get('start_date'):
        try:
            start_date = datetime.strptime(request.args.get('start_date'), '%Y-%m-%d').date()
        except ValueError:
            flash('Formato de fecha inválido', 'danger')
    
    if request.args.get('end_date'):
        try:
            end_date = datetime.strptime(request.args.get('end_date'), '%Y-%m-%d').date()
        except ValueError:
            flash('Formato de fecha inválido', 'danger')
    
    # Obtener los arqueos en el rango de fechas
    registers = CashRegister.query.filter(
        CashRegister.company_id == company_id,
        CashRegister.date >= start_date,
        CashRegister.date <= end_date
    ).order_by(CashRegister.date.desc()).all()
    
    # Obtener el resumen semanal actual
    current_week = today.isocalendar()[1]
    current_year = today.isocalendar()[0]
    
    summary = CashRegisterSummary.query.filter_by(
        company_id=company_id,
        year=current_year,
        week_number=current_week
    ).first()
    
    # Si no existe el resumen, crear uno temporal para mostrar
    if not summary:
        summary = CashRegisterSummary(
            company_id=company_id,
            year=current_year,
            week_number=current_week,
            month=today.month
        )
    
    # Obtener datos de coste de personal
    work_hours = CompanyWorkHours.query.filter_by(
        company_id=company_id,
        year=current_year,
        week_number=current_week
    ).first()
    
    # Formulario de filtro
    filter_form = CashRegisterFilterForm()
    
    return render_template(
        'cash_register/dashboard.html',
        company=company,
        registers=registers,
        summary=summary,
        work_hours=work_hours,
        start_date=start_date,
        end_date=end_date,
        filter_form=filter_form,
        title=f'Arqueos - {company.name}'
    )

@cash_register_bp.route('/empresa/<int:company_id>/nuevo', methods=['GET', 'POST'])
@login_required
@company_membership_required
def new_register(company_id):
    """
    Formulario para crear un nuevo arqueo.
    """
    company = Company.query.get_or_404(company_id)
    form = CashRegisterForm()
    
    if form.validate_on_submit():
        # Verificar si ya existe un arqueo para esta fecha y empresa
        existing = CashRegister.query.filter_by(
            company_id=company_id,
            date=form.date.data
        ).first()
        
        if existing:
            flash(f'Ya existe un arqueo para la fecha {form.date.data.strftime("%d/%m/%Y")}', 'danger')
            return redirect(url_for('cash_register.edit_register', register_id=existing.id))
        
        # Crear nuevo arqueo
        register = CashRegister(
            company_id=company_id,
            date=form.date.data,
            total_amount=form.total_amount.data,
            cash_amount=form.cash_amount.data,
            card_amount=form.card_amount.data,
            delivery_cash_amount=form.delivery_cash_amount.data,
            delivery_online_amount=form.delivery_online_amount.data,
            check_amount=form.check_amount.data,
            expenses_amount=form.expenses_amount.data,
            expenses_notes=form.expenses_notes.data,
            notes=form.notes.data,
            created_by_id=current_user.id,
            is_confirmed=True,
            confirmed_at=datetime.utcnow(),
            confirmed_by_id=current_user.id
        )
        
        db.session.add(register)
        
        try:
            db.session.commit()
            
            # Actualizar resúmenes
            update_register_summary(company_id, form.date.data)
            
            flash('Arqueo creado correctamente', 'success')
            save_activity_log('Arqueo creado', f'Arqueo para {form.date.data.strftime("%d/%m/%Y")} - {company.name}')
            
            return redirect(url_for('cash_register.company_dashboard', company_id=company_id))
        except Exception as e:
            db.session.rollback()
            current_app.logger.error(f"Error creando arqueo: {str(e)}")
            flash('Error al crear el arqueo', 'danger')
    
    # Por defecto, la fecha es hoy
    if request.method == 'GET':
        form.date.data = date.today()
    
    return render_template(
        'cash_register/register_form.html',
        form=form,
        company=company,
        title='Nuevo Arqueo',
        is_new=True
    )

@cash_register_bp.route('/editar/<int:register_id>', methods=['GET', 'POST'])
@login_required
def edit_register(register_id):
    """
    Formulario para editar un arqueo existente.
    """
    register = CashRegister.query.get_or_404(register_id)
    
    # Verificar permisos
    if not current_user.is_admin() and register.company_id not in [c.id for c in current_user.companies]:
        flash('No tiene permiso para editar este arqueo', 'danger')
        return redirect(url_for('cash_register.index'))
    
    form = CashRegisterForm(obj=register)
    
    if form.validate_on_submit():
        # Actualizar datos del arqueo
        register.date = form.date.data
        register.total_amount = form.total_amount.data
        register.cash_amount = form.cash_amount.data
        register.card_amount = form.card_amount.data
        register.delivery_cash_amount = form.delivery_cash_amount.data
        register.delivery_online_amount = form.delivery_online_amount.data
        register.check_amount = form.check_amount.data
        register.expenses_amount = form.expenses_amount.data
        register.expenses_notes = form.expenses_notes.data
        register.notes = form.notes.data
        register.updated_at = datetime.utcnow()
        
        try:
            db.session.commit()
            
            # Actualizar resúmenes
            update_register_summary(register.company_id, register.date)
            
            flash('Arqueo actualizado correctamente', 'success')
            save_activity_log('Arqueo actualizado', f'Arqueo ID {register.id}')
            
            return redirect(url_for('cash_register.company_dashboard', company_id=register.company_id))
        except Exception as e:
            db.session.rollback()
            current_app.logger.error(f"Error actualizando arqueo: {str(e)}")
            flash('Error al actualizar el arqueo', 'danger')
    
    # Mostrar la empresa
    company = Company.query.get(register.company_id)
    
    return render_template(
        'cash_register/register_form.html',
        form=form,
        register=register,
        company=company,
        title='Editar Arqueo',
        is_new=False
    )

@cash_register_bp.route('/eliminar/<int:register_id>', methods=['POST'])
@login_required
def delete_register(register_id):
    """
    Eliminar un arqueo existente.
    """
    register = CashRegister.query.get_or_404(register_id)
    
    # Verificar permisos (solo admin o manager)
    if not current_user.is_admin() and not current_user.is_manager():
        flash('No tiene permiso para eliminar arqueos', 'danger')
        return redirect(url_for('cash_register.company_dashboard', company_id=register.company_id))
    
    # Verificar permisos de empresa
    if not current_user.is_admin() and register.company_id not in [c.id for c in current_user.companies]:
        flash('No tiene permiso para eliminar este arqueo', 'danger')
        return redirect(url_for('cash_register.index'))
    
    company_id = register.company_id
    register_date = register.date
    
    try:
        db.session.delete(register)
        db.session.commit()
        
        # Actualizar resúmenes
        update_register_summary(company_id, register_date)
        
        flash('Arqueo eliminado correctamente', 'success')
        save_activity_log('Arqueo eliminado', f'Arqueo para {register_date.strftime("%d/%m/%Y")}')
    except Exception as e:
        db.session.rollback()
        current_app.logger.error(f"Error eliminando arqueo: {str(e)}")
        flash('Error al eliminar el arqueo', 'danger')
    
    return redirect(url_for('cash_register.company_dashboard', company_id=company_id))

@cash_register_bp.route('/empresa/<int:company_id>/tokens')
@login_required
@company_membership_required
@manager_required
def list_tokens(company_id):
    """
    Listar tokens de acceso para empleados.
    """
    company = Company.query.get_or_404(company_id)
    
    # Obtener tokens activos
    active_tokens = CashRegisterToken.query.filter_by(
        company_id=company_id,
        is_active=True
    ).order_by(CashRegisterToken.created_at.desc()).all()
    
    # Obtener tokens usados recientes (últimos 30)
    used_tokens = CashRegisterToken.query.filter_by(
        company_id=company_id,
        is_active=False
    ).order_by(CashRegisterToken.used_at.desc()).limit(30).all()
    
    # Obtener empleados de la empresa para el formulario
    employees = Employee.query.filter_by(company_id=company_id).all()
    
    # Formulario para crear token
    form = CashRegisterTokenForm()
    form.employee_id.choices = [(0, 'Sin asignar')] + [(e.id, f"{e.first_name} {e.last_name}") for e in employees]
    
    return render_template(
        'cash_register/tokens.html',
        company=company,
        active_tokens=active_tokens,
        used_tokens=used_tokens,
        form=form,
        title='Tokens de Arqueo'
    )

@cash_register_bp.route('/empresa/<int:company_id>/tokens/nuevo', methods=['POST'])
@login_required
@company_membership_required
@manager_required
def create_token(company_id):
    """
    Crear un nuevo token para un empleado.
    """
    form = CashRegisterTokenForm()
    
    # Obtener empleados de la empresa para validación
    employees = Employee.query.filter_by(company_id=company_id).all()
    form.employee_id.choices = [(0, 'Sin asignar')] + [(e.id, f"{e.first_name} {e.last_name}") for e in employees]
    
    if form.validate_on_submit():
        # Determinar el ID del empleado (puede ser None)
        employee_id = form.employee_id.data if form.employee_id.data > 0 else None
        
        # Generar el token
        token = generate_token(
            company_id=company_id,
            employee_id=employee_id,
            expiry_days=form.expiry_days.data
        )
        
        if token:
            # Asignar el usuario que lo creó
            token.created_by_id = current_user.id
            db.session.commit()
            
            flash('Token generado correctamente', 'success')
            save_activity_log('Token de arqueo creado', f'Para empresa ID {company_id}')
        else:
            flash('Error al generar el token', 'danger')
    else:
        for field, errors in form.errors.items():
            for error in errors:
                flash(f'Error en el campo {field}: {error}', 'danger')
    
    return redirect(url_for('cash_register.list_tokens', company_id=company_id))

@cash_register_bp.route('/empresa/<int:company_id>/tokens/<int:token_id>/invalidar', methods=['POST'])
@login_required
@company_membership_required
@manager_required
def invalidate_token(company_id, token_id):
    """
    Invalidar un token existente.
    """
    token = CashRegisterToken.query.get_or_404(token_id)
    
    # Verificar que el token pertenece a la empresa
    if token.company_id != company_id:
        flash('Token no válido para esta empresa', 'danger')
        return redirect(url_for('cash_register.list_tokens', company_id=company_id))
    
    token.is_active = False
    db.session.commit()
    
    flash('Token invalidado correctamente', 'success')
    save_activity_log('Token de arqueo invalidado', f'Token ID {token_id}')
    
    return redirect(url_for('cash_register.list_tokens', company_id=company_id))

@cash_register_bp.route('/empresa/<int:company_id>/informes')
@login_required
@company_membership_required
def reports(company_id):
    """
    Generar informes de arqueos y costes de personal.
    """
    company = Company.query.get_or_404(company_id)
    
    # Obtener año y mes actual
    today = date.today()
    current_year = today.year
    current_month = today.month
    
    # Permitir cambiar año y mes por parámetros
    year = request.args.get('year', current_year, type=int)
    month = request.args.get('month', current_month, type=int)
    
    # Obtener todos los resúmenes del mes
    summaries = CashRegisterSummary.query.filter_by(
        company_id=company_id,
        year=year,
        month=month
    ).order_by(CashRegisterSummary.week_number).all()
    
    # Obtener arqueos individuales del mes
    start_date = date(year, month, 1)
    if month < 12:
        end_date = date(year, month + 1, 1) - timedelta(days=1)
    else:
        end_date = date(year + 1, 1, 1) - timedelta(days=1)
    
    registers = CashRegister.query.filter(
        CashRegister.company_id == company_id,
        CashRegister.date >= start_date,
        CashRegister.date <= end_date
    ).order_by(CashRegister.date).all()
    
    # Calcular totales generales
    total_income = sum(r.total_amount for r in registers)
    total_expenses = sum(r.expenses_amount for r in registers)
    
    # Obtener datos de personal del mes
    work_hours = CompanyWorkHours.query.filter_by(
        company_id=company_id,
        year=year,
        month=month
    ).all()
    
    total_hours = sum(wh.monthly_hours for wh in work_hours)
    
    # Calcular coste total de personal
    staff_cost = 0
    if company.hourly_employee_cost and company.hourly_employee_cost > 0:
        staff_cost = total_hours * company.hourly_employee_cost
    
    # Calcular porcentaje de coste de personal sobre facturación
    staff_cost_percentage = 0
    if total_income > 0:
        staff_cost_percentage = (staff_cost / total_income) * 100
    
    return render_template(
        'cash_register/reports.html',
        company=company,
        year=year,
        month=month,
        registers=registers,
        summaries=summaries,
        total_income=total_income,
        total_expenses=total_expenses,
        total_hours=total_hours,
        staff_cost=staff_cost,
        staff_cost_percentage=staff_cost_percentage,
        title=f'Informes de Arqueos - {company.name}'
    )

# Rutas públicas para empleados sin acceso

@cash_register_bp.route('/acceso/<token>')
def public_access(token):
    """
    Acceso público para empleados mediante token.
    """
    # Validar el token
    token_obj = validate_token(token)
    if not token_obj:
        return render_template('cash_register/public/invalid_token.html')
    
    # Si el token es válido, mostrar formulario de arqueo
    company = Company.query.get_or_404(token_obj.company_id)
    
    form = PublicCashRegisterForm()
    
    # Si hay un empleado asociado al token, mostrar su información
    employee = None
    if token_obj.employee_id:
        employee = Employee.query.get(token_obj.employee_id)
    
    return render_template(
        'cash_register/public/register_form.html',
        token=token_obj,
        company=company,
        employee=employee,
        form=form
    )

@cash_register_bp.route('/acceso/<token>/guardar', methods=['POST'])
def public_save_register(token):
    """
    Guardar arqueo desde acceso público.
    """
    # Validar el token
    token_obj = validate_token(token)
    if not token_obj:
        return render_template('cash_register/public/invalid_token.html')
    
    company = Company.query.get_or_404(token_obj.company_id)
    
    form = PublicCashRegisterForm()
    
    if form.validate_on_submit():
        # Verificar si ya existe un arqueo para esta fecha y empresa
        existing = CashRegister.query.filter_by(
            company_id=company.id,
            date=form.date.data
        ).first()
        
        if existing:
            flash(f'Ya existe un arqueo para la fecha {form.date.data.strftime("%d/%m/%Y")}', 'danger')
            return redirect(url_for('cash_register.public_access', token=token))
        
        # Crear nuevo arqueo
        register = CashRegister(
            company_id=company.id,
            date=form.date.data,
            total_amount=form.total_amount.data,
            cash_amount=form.cash_amount.data,
            card_amount=form.card_amount.data,
            delivery_cash_amount=form.delivery_cash_amount.data,
            delivery_online_amount=form.delivery_online_amount.data,
            check_amount=form.check_amount.data,
            expenses_amount=form.expenses_amount.data,
            expenses_notes=form.expenses_notes.data,
            employee_id=token_obj.employee_id,
            is_confirmed=False  # Requiere confirmación de un manager
        )
        
        # Si hay un empleado asociado, usar su nombre
        if token_obj.employee:
            register.employee_name = f"{token_obj.employee.first_name} {token_obj.employee.last_name}"
        else:
            register.employee_name = form.employee_name.data
        
        db.session.add(register)
        
        try:
            db.session.commit()
            
            # Marcar el token como usado
            mark_token_used(token_obj, register.id)
            
            # Actualizar resúmenes (aunque el arqueo no esté confirmado)
            update_register_summary(company.id, form.date.data)
            
            # Mostrar pantalla de confirmación
            return render_template(
                'cash_register/public/success.html',
                company=company,
                register=register
            )
            
        except Exception as e:
            db.session.rollback()
            current_app.logger.error(f"Error en arqueo público: {str(e)}")
            flash('Error al guardar el arqueo', 'danger')
    else:
        for field, errors in form.errors.items():
            for error in errors:
                flash(f'Error en el campo {field}: {error}', 'danger')
    
    # En caso de error, volver al formulario
    employee = None
    if token_obj.employee_id:
        employee = Employee.query.get(token_obj.employee_id)
    
    return render_template(
        'cash_register/public/register_form.html',
        token=token_obj,
        company=company,
        employee=employee,
        form=form
    )

# API AJAX para reportes dinámicos

@cash_register_bp.route('/api/empresa/<int:company_id>/datos_mensuales/<int:year>')
@login_required
@company_membership_required
def api_monthly_data(company_id, year):
    """
    API: Obtener datos mensuales de arqueos y costes para charts.
    """
    # Validar que la empresa existe
    Company.query.get_or_404(company_id)
    
    # Array para almacenar los datos de cada mes
    monthly_data = []
    
    for month in range(1, 13):
        # Obtener todos los arqueos del mes
        start_date = date(year, month, 1)
        if month < 12:
            end_date = date(year, month + 1, 1) - timedelta(days=1)
        else:
            end_date = date(year + 1, 1, 1) - timedelta(days=1)
        
        # Total de ingresos del mes
        monthly_income = db.session.query(db.func.sum(CashRegister.total_amount)).filter(
            CashRegister.company_id == company_id,
            CashRegister.date >= start_date,
            CashRegister.date <= end_date
        ).scalar() or 0
        
        # Horas trabajadas del mes
        monthly_hours = db.session.query(db.func.sum(CompanyWorkHours.monthly_hours)).filter(
            CompanyWorkHours.company_id == company_id,
            CompanyWorkHours.year == year,
            CompanyWorkHours.month == month
        ).scalar() or 0
        
        # Obtener coste por hora de la empresa
        company = Company.query.get(company_id)
        hourly_cost = company.hourly_employee_cost or 0
        
        # Calcular coste de personal
        staff_cost = monthly_hours * hourly_cost
        
        # Calcular porcentaje
        percentage = 0
        if monthly_income > 0:
            percentage = (staff_cost / monthly_income) * 100
        
        # Agregar datos al array
        monthly_data.append({
            'month': month,
            'month_name': date(year, month, 1).strftime('%B'),
            'income': round(monthly_income, 2),
            'hours': round(monthly_hours, 2),
            'staff_cost': round(staff_cost, 2),
            'percentage': round(percentage, 2)
        })
    
    return jsonify({
        'success': True,
        'data': monthly_data
    })

@cash_register_bp.route('/api/check_register_exists')
def api_check_register_exists():
    """
    API: Verificar si ya existe un arqueo para una fecha y empresa.
    """
    # Obtener parámetros
    company_id = request.args.get('company_id', type=int)
    date_str = request.args.get('date')
    
    if not company_id or not date_str:
        return jsonify({'success': False, 'message': 'Parámetros incompletos'})
    
    try:
        register_date = datetime.strptime(date_str, '%Y-%m-%d').date()
    except ValueError:
        return jsonify({'success': False, 'message': 'Formato de fecha inválido'})
    
    # Verificar si existe un arqueo
    existing = CashRegister.query.filter_by(
        company_id=company_id,
        date=register_date
    ).first()
    
    if existing:
        return jsonify({
            'success': True,
            'exists': True,
            'register_id': existing.id
        })
    
    return jsonify({
        'success': True,
        'exists': False
    })
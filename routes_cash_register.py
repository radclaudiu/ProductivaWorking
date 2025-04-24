"""
Rutas para el módulo de Arqueos de Caja.

Este módulo define las rutas y vistas para la gestión de arqueos de caja,
incluyendo el dashboard, CRUD de arqueos, reportes y acceso mediante tokens.
"""

import logging
from datetime import datetime, date, timedelta
import calendar
import os
from decimal import Decimal
import secrets
import json
import sys
import traceback

# Imports de Flask y extensiones
from flask import (
    Blueprint, render_template, request, redirect, url_for, flash, 
    jsonify, current_app, abort, session
)
from flask_login import login_required, current_user
from werkzeug.security import safe_join

# Imports de modelos, formularios y utilidades
from app import db
# Importamos los modelos dentro de las funciones para evitar importaciones circulares
from forms_cash_register import (
    CashRegisterForm, CashRegisterSearchForm, CashRegisterConfirmForm,
    CashRegisterTokenForm, PublicCashRegisterForm, PinVerificationForm
)
from utils_cash_register import (
    calculate_weekly_summary, calculate_staff_cost, calculate_monthly_revenue,
    calculate_yearly_revenue, format_currency, format_percentage,
    get_current_week_number, get_week_dates, get_week_number, get_date_range,
    generate_token_url
)
from app import db

# Definimos las funciones de helpers aquí mismo para evitar importaciones adicionales
def is_valid_url(url):
    """Validar si una URL es válida y segura"""
    import re
    pattern = re.compile(r'^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$')
    return bool(pattern.match(url))
    
def sanitize_filename(filename):
    """Sanitizar nombre de archivo para que sea seguro"""
    import re
    # Reemplazar caracteres no alfanuméricos excepto algunos permitidos
    return re.sub(r'[^a-zA-Z0-9_.-]', '_', filename)

# Definimos los decoradores que necesitamos
def admin_required(f):
    """Decorador que requiere que el usuario sea administrador"""
    from functools import wraps
    from flask import flash, redirect, url_for
    from flask_login import current_user
    
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated:
            return redirect(url_for('auth.login', next=request.url))
        if not current_user.is_admin():
            flash('Acceso restringido. Se requieren permisos de administrador.', 'danger')
            return redirect(url_for('main.index'))
        return f(*args, **kwargs)
    return decorated_function

def gerente_required(f):
    """Decorador que requiere que el usuario sea gerente o administrador"""
    from functools import wraps
    from flask import flash, redirect, url_for
    from flask_login import current_user
    
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated:
            return redirect(url_for('auth.login', next=request.url))
        if not (current_user.is_gerente() or current_user.is_admin()):
            flash('Acceso restringido. Se requieren permisos de gerente.', 'danger')
            return redirect(url_for('main.index'))
        return f(*args, **kwargs)
    return decorated_function

# Configurar logging
logger = logging.getLogger(__name__)

# Crear Blueprint
cash_register_bp = Blueprint('cash_register', __name__, url_prefix='/cash-register')


@cash_register_bp.route('/debug')
def debug_cash_register():
    """
    Ruta de depuración para el módulo de arqueos de caja.
    Muestra información útil para diagnosticar problemas.
    """
    try:
        # Importamos modelos aquí para evitar problemas de importación circular
        from models import Company, Employee, User
        from models_cash_register import CashRegister, CashRegisterSummary, CashRegisterToken
        
        # Información general
        debug_info = {
            'general': {
                'módulo': 'Arqueos de Caja',
                'fecha_hora': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
                'bp_routes': []  # Lo llenaremos manualmente
            }
        }
        
        # Añadir rutas principales del módulo
        debug_info['general']['bp_routes'] = [
            '/cash-register/dashboard',
            '/cash-register/debug',
            '/cash-register/company/1',  # Ejemplo con ID 1
            '/cash-register/company/1/tokens',
            '/cash-register/company/1/register',
            '/cash-register/company/1/report'
        ]
        
        # Comprobamos acceso a modelos
        try:
            num_companies = Company.query.count()
            debug_info['models'] = {
                'Company': f'OK - {num_companies} registros',
            }
        except Exception as e:
            debug_info['models'] = {
                'Company': f'ERROR - {str(e)}',
            }
            
        try:
            num_registers = CashRegister.query.count()
            debug_info['models']['CashRegister'] = f'OK - {num_registers} registros'
        except Exception as e:
            debug_info['models']['CashRegister'] = f'ERROR - {str(e)}'
            
        try:
            num_summaries = CashRegisterSummary.query.count()
            debug_info['models']['CashRegisterSummary'] = f'OK - {num_summaries} registros'
        except Exception as e:
            debug_info['models']['CashRegisterSummary'] = f'ERROR - {str(e)}'
            
        try:
            num_tokens = CashRegisterToken.query.count()
            debug_info['models']['CashRegisterToken'] = f'OK - {num_tokens} registros'
        except Exception as e:
            debug_info['models']['CashRegisterToken'] = f'ERROR - {str(e)}'
            
        # Mostrar errores de importación
        debug_info['imports'] = {}
        try:
            import forms_cash_register
            debug_info['imports']['forms_cash_register'] = 'OK'
        except Exception as e:
            debug_info['imports']['forms_cash_register'] = f'ERROR - {str(e)}'
            
        # Información del entorno
        debug_info['environment'] = {
            'Flask DEBUG': current_app.config.get('DEBUG', 'No configurado'),
            'SQLALCHEMY_DATABASE_URI': current_app.config.get('SQLALCHEMY_DATABASE_URI', 'No configurado'),
            'PROPAGATE_EXCEPTIONS': current_app.config.get('PROPAGATE_EXCEPTIONS', 'No configurado'),
        }
        
        return render_template(
            'debug.html',
            title='Debug - Arqueos de Caja',
            debug_info=debug_info
        )
        
    except Exception as e:
        # Si falla algo, mostrar el error directamente
        import traceback
        error_trace = traceback.format_exc()
        return f'<pre>ERROR EN DEPURACIÓN: {str(e)}\n\n{error_trace}</pre>'

@cash_register_bp.route('/dashboard')
@login_required
def dashboard():
    """
    Dashboard principal de arqueos de caja.
    
    Muestra listado de empresas para acceder a sus datos de arqueo.
    """
    # Importamos Company aquí para evitar importaciones circulares
    from models import Company
    
    # Obtener empresas a las que tiene acceso el usuario
    if current_user.is_admin():
        companies = Company.query.order_by(Company.name).all()
    else:
        companies = current_user.companies
    
    if not companies:
        flash('No tiene acceso a ninguna empresa', 'warning')
        return redirect(url_for('main.index'))
    
    return render_template(
        'cash_register/dashboard.html',
        title='Dashboard de Arqueos de Caja',
        companies=companies
    )



@cash_register_bp.route('/company/<int:company_id>')
@login_required
def company_dashboard(company_id):
    """
    Dashboard de arqueos de caja para una empresa específica.
    
    Muestra resumen de arqueos recientes, totales y gráficos.
    
    Args:
        company_id: ID de la empresa
    """
    # Importamos los modelos dentro de la función para evitar importaciones circulares
    from models import Company
    from models_cash_register import CashRegister, CashRegisterSummary, CashRegisterToken
    
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta empresa', 'danger')
        return redirect(url_for('cash_register.dashboard'))
    
    # Obtener el año y semana actuales
    current_year = datetime.now().year
    current_week = get_current_week_number()
    current_month = datetime.now().month
    
    # Obtener fechas de inicio y fin de la semana actual
    week_start, week_end = get_week_dates(current_year, current_week)
    
    # Calcular o actualizar el resumen semanal
    summary = calculate_weekly_summary(company_id, current_year, current_week)
    
    # Obtener arqueos recientes
    recent_registers = CashRegister.query.filter_by(company_id=company_id)\
        .order_by(CashRegister.date.desc())\
        .limit(10).all()
    
    # Obtener arqueos pendientes (no confirmados)
    pending_registers = CashRegister.query.filter_by(
        company_id=company_id, 
        is_confirmed=False
    ).all()
    
    # Obtener tokens activos
    active_tokens = CashRegisterToken.query.filter_by(
        company_id=company_id,
        is_active=True,
        cash_register_id=None
    ).all()
    
    # Preparar datos para gráfico de métodos de pago de la semana actual
    payment_methods_data = {}
    if summary:
        payment_methods_data = {
            'labels': [
                'Efectivo', 'Tarjeta', 'Delivery - Efectivo', 
                'Delivery - Online', 'Cheque'
            ],
            'data': [
                float(summary.weekly_cash),
                float(summary.weekly_card),
                float(summary.weekly_delivery_cash),
                float(summary.weekly_delivery_online),
                float(summary.weekly_check)
            ]
        }
    
    # Obtener datos de horas trabajadas y calcular coste
    staff_cost = calculate_staff_cost(
        company_id, current_year, current_month, current_week
    )
    
    return render_template(
        'cash_register/company_dashboard.html',
        title=f'Arqueos de Caja - {company.name}',
        company=company,
        summary=summary,
        recent_registers=recent_registers,
        pending_registers=pending_registers,
        active_tokens=active_tokens,
        payment_methods_data=json.dumps(payment_methods_data),
        staff_cost=staff_cost,
        week_start=week_start,
        week_end=week_end,
        current_year=current_year,
        current_week=current_week,
        format_currency=format_currency,
        format_percentage=format_percentage
    )


@cash_register_bp.route('/company/<int:company_id>/register', methods=['GET', 'POST'])
@login_required
def new_register(company_id):
    """
    Crear un nuevo arqueo de caja para una empresa.
    
    Args:
        company_id: ID de la empresa
    """
    # Importamos los modelos dentro de la función para evitar importaciones circulares
    from models import Company, Employee
    from models_cash_register import CashRegister
    
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta empresa', 'danger')
        return redirect(url_for('cash_register.dashboard'))
    
    # Crear formulario
    form = CashRegisterForm()
    
    # Cargar lista de empleados para el selector
    employees = Employee.query.filter_by(company_id=company_id, is_active=True).all()
    employee_choices = [(0, 'Sin asignar')] + [(e.id, f"{e.first_name} {e.last_name}") for e in employees]
    form.employee_id.choices = employee_choices
    
    if form.validate_on_submit():
        try:
            logger.info(f"Iniciando creación de nuevo arqueo para empresa {company_id}")
            
            # Verificar si ya existe un arqueo para esta fecha
            existing_register = CashRegister.query.filter_by(
                company_id=company_id,
                date=form.date.data
            ).first()
            
            if existing_register:
                logger.warning(f"Ya existe un arqueo para la fecha {form.date.data} en empresa {company_id}")
                flash(f'Ya existe un arqueo para la fecha {form.date.data.strftime("%d/%m/%Y")}', 'danger')
                return redirect(url_for('cash_register.company_dashboard', company_id=company_id))
            
            logger.info(f"Creando nuevo arqueo: fecha={form.date.data}, total={form.total_amount.data}")
            
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
                created_by_id=current_user.id
            )
            
            # Asignar empleado si se seleccionó
            if form.employee_id.data != 0:
                logger.info(f"Asignando empleado ID {form.employee_id.data} al arqueo")
                register.employee_id = form.employee_id.data
            
            # Guardar nombre de empleado si se proporciona
            if form.employee_name.data:
                logger.info(f"Guardando nombre de empleado: {form.employee_name.data}")
                register.employee_name = form.employee_name.data
            
            logger.info("Añadiendo registro a la sesión de base de datos")
            db.session.add(register)
            
            logger.info("Ejecutando commit para guardar el arqueo")
            db.session.commit()
            
            # Actualizar resumen semanal y mensual
            year = form.date.data.year
            week_number = get_week_number(form.date.data)
            logger.info(f"Actualizando resumen semanal: año={year}, semana={week_number}")
            calculate_weekly_summary(company_id, year, week_number)
            
            logger.info("Arqueo registrado correctamente")
            flash('Arqueo de caja registrado correctamente', 'success')
            return redirect(url_for('cash_register.company_dashboard', company_id=company_id))
            
        except Exception as e:
            db.session.rollback()
            logger.error(f"Error al crear arqueo: {str(e)}")
            flash(f'Error al crear arqueo: {str(e)}', 'danger')
    
    return render_template(
        'cash_register/register_form.html',
        title='Nuevo Arqueo de Caja',
        form=form,
        company=company,
        is_new=True
    )


@cash_register_bp.route('/register/<int:register_id>', methods=['GET', 'POST'])
@login_required
def edit_register(register_id):
    """
    Editar un arqueo de caja existente.
    
    Args:
        register_id: ID del arqueo a editar
    """
    # Importamos los modelos dentro de la función para evitar importaciones circulares
    from models import Employee
    from models_cash_register import CashRegister
    
    # Obtener arqueo y verificar permisos
    register = CashRegister.query.get_or_404(register_id)
    company = register.company
    
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a este arqueo', 'danger')
        return redirect(url_for('cash_register.dashboard'))
    
    # No permitir editar registros confirmados excepto a administradores
    if register.is_confirmed and not current_user.is_admin():
        flash('No se puede editar un arqueo ya confirmado', 'warning')
        return redirect(url_for('cash_register.company_dashboard', company_id=company.id))
    
    # Crear formulario y poblarlo con datos existentes
    form = CashRegisterForm(obj=register)
    
    # Cargar lista de empleados para el selector
    employees = Employee.query.filter_by(company_id=company.id, is_active=True).all()
    employee_choices = [(0, 'Sin asignar')] + [(e.id, f"{e.first_name} {e.last_name}") for e in employees]
    form.employee_id.choices = employee_choices
    
    if form.validate_on_submit():
        try:
            # Guardar cambios
            form.populate_obj(register)
            
            # Manejar empleado especial "Sin asignar"
            if form.employee_id.data == 0:
                register.employee_id = None
            
            db.session.commit()
            
            # Actualizar resumen semanal y mensual
            year = register.date.year
            week_number = get_week_number(register.date)
            calculate_weekly_summary(company.id, year, week_number)
            
            flash('Arqueo actualizado correctamente', 'success')
            return redirect(url_for('cash_register.company_dashboard', company_id=company.id))
            
        except Exception as e:
            db.session.rollback()
            logger.error(f"Error al actualizar arqueo: {str(e)}")
            flash(f'Error al actualizar arqueo: {str(e)}', 'danger')
    
    return render_template(
        'cash_register/register_form.html',
        title='Editar Arqueo de Caja',
        form=form,
        register=register,
        company=company,
        is_new=False
    )


@cash_register_bp.route('/register/<int:register_id>/confirm', methods=['GET', 'POST'])
@login_required
def confirm_register(register_id):
    """
    Confirmar un arqueo de caja.
    
    Args:
        register_id: ID del arqueo a confirmar
    """
    # Importar modelos para evitar problemas de importación circular
    from models_cash_register import CashRegister
    
    # Obtener arqueo y verificar permisos
    register = CashRegister.query.get_or_404(register_id)
    company = register.company
    
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a este arqueo', 'danger')
        return redirect(url_for('cash_register.dashboard'))
    
    # Verificar si ya está confirmado
    if register.is_confirmed:
        flash('Este arqueo ya ha sido confirmado', 'info')
        return redirect(url_for('cash_register.company_dashboard', company_id=company.id))
    
    # Crear formulario
    form = CashRegisterConfirmForm()
    form.cash_register_id.data = register_id
    
    if form.validate_on_submit():
        if form.cancel.data:
            return redirect(url_for('cash_register.company_dashboard', company_id=company.id))
        
        if form.confirm.data:
            try:
                # Confirmar arqueo
                register.is_confirmed = True
                register.confirmed_at = datetime.now()
                register.confirmed_by_id = current_user.id
                
                db.session.commit()
                
                flash('Arqueo confirmado correctamente', 'success')
                return redirect(url_for('cash_register.company_dashboard', company_id=company.id))
                
            except Exception as e:
                db.session.rollback()
                logger.error(f"Error al confirmar arqueo: {str(e)}")
                flash(f'Error al confirmar arqueo: {str(e)}', 'danger')
    
    return render_template(
        'cash_register/confirm_register.html',
        title='Confirmar Arqueo de Caja',
        form=form,
        register=register,
        company=company,
        format_currency=format_currency
    )


@cash_register_bp.route('/register/<int:register_id>/delete', methods=['POST'])
@login_required
def delete_register(register_id):
    """
    Eliminar un arqueo de caja.
    
    Args:
        register_id: ID del arqueo a eliminar
    """
    # Importar modelos para evitar problemas de importación circular
    from models_cash_register import CashRegister
    
    # Obtener arqueo y verificar permisos
    register = CashRegister.query.get_or_404(register_id)
    company = register.company
    
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a este arqueo', 'danger')
        return redirect(url_for('cash_register.dashboard'))
    
    # No permitir eliminar registros confirmados excepto a administradores
    if register.is_confirmed and not current_user.is_admin():
        flash('No se puede eliminar un arqueo ya confirmado', 'warning')
        return redirect(url_for('cash_register.company_dashboard', company_id=company.id))
    
    try:
        # Obtener datos para actualizar sumarios después
        company_id = register.company_id
        year = register.date.year
        week_number = get_week_number(register.date)
        
        # Eliminar arqueo
        db.session.delete(register)
        db.session.commit()
        
        # Actualizar resumen semanal y mensual
        calculate_weekly_summary(company_id, year, week_number)
        
        flash('Arqueo eliminado correctamente', 'success')
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al eliminar arqueo: {str(e)}")
        flash(f'Error al eliminar arqueo: {str(e)}', 'danger')
    
    return redirect(url_for('cash_register.company_dashboard', company_id=company.id))


@cash_register_bp.route('/company/<int:company_id>/report', methods=['GET', 'POST'])
@login_required
def company_report(company_id):
    """
    Generar informe de arqueos para una empresa.
    
    Args:
        company_id: ID de la empresa
    """
    # Importar modelos para evitar problemas de importación circular
    from models import Company
    from models_cash_register import CashRegister
    
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta empresa', 'danger')
        return redirect(url_for('cash_register.dashboard'))
    
    # Crear formulario de búsqueda
    form = CashRegisterSearchForm()
    form.company_id.data = company_id
    form.company_id.choices = [(company.id, company.name)]
    
    # Establecer valores predeterminados
    current_year = datetime.now().year
    current_month = datetime.now().month
    
    if not form.year.data:
        form.year.data = current_year
    
    if form.month.data is None:
        form.month.data = current_month
    
    # Construir la consulta base
    query = CashRegister.query.filter_by(company_id=company_id)
    
    # Aplicar filtros si se enviaron
    if request.method == 'POST' and form.validate():
        # Filtrar por año
        if form.year.data:
            year = form.year.data
            
            # Filtrar por mes
            if form.month.data > 0:
                month = form.month.data
                start_date, end_date = get_date_range(year, month)
                query = query.filter(
                    CashRegister.date >= start_date,
                    CashRegister.date <= end_date
                )
            # Filtrar por semana
            elif form.week.data:
                week = form.week.data
                start_date, end_date = get_date_range(year, week=week)
                query = query.filter(
                    CashRegister.date >= start_date,
                    CashRegister.date <= end_date
                )
            # Filtrar por año completo
            else:
                start_date, end_date = get_date_range(year)
                query = query.filter(
                    CashRegister.date >= start_date,
                    CashRegister.date <= end_date
                )
        
        # Filtrar por fechas específicas
        elif form.start_date.data or form.end_date.data:
            if form.start_date.data:
                query = query.filter(CashRegister.date >= form.start_date.data)
            if form.end_date.data:
                query = query.filter(CashRegister.date <= form.end_date.data)
        
        # Filtrar por estado (confirmado/pendiente)
        if form.is_confirmed.data != 'all':
            is_confirmed = form.is_confirmed.data == 'true'
            query = query.filter(CashRegister.is_confirmed == is_confirmed)
    else:
        # Por defecto, mostrar el mes actual
        start_date, end_date = get_date_range(current_year, current_month)
        query = query.filter(
            CashRegister.date >= start_date,
            CashRegister.date <= end_date
        )
    
    # Ejecutar la consulta
    registers = query.order_by(CashRegister.date.desc()).all()
    
    # Calcular totales
    totals = {
        'total_amount': sum(r.total_amount for r in registers),
        'cash_amount': sum(r.cash_amount for r in registers),
        'card_amount': sum(r.card_amount for r in registers),
        'delivery_cash_amount': sum(r.delivery_cash_amount for r in registers),
        'delivery_online_amount': sum(r.delivery_online_amount for r in registers),
        'check_amount': sum(r.check_amount for r in registers),
        'expenses_amount': sum(r.expenses_amount for r in registers)
    }
    
    # Calcular datos para gráfico
    payment_methods_data = {
        'labels': [
            'Efectivo', 'Tarjeta', 'Delivery - Efectivo', 
            'Delivery - Online', 'Cheque'
        ],
        'data': [
            float(totals['cash_amount']),
            float(totals['card_amount']),
            float(totals['delivery_cash_amount']),
            float(totals['delivery_online_amount']),
            float(totals['check_amount'])
        ]
    }
    
    # Obtener datos de personal si tenemos año y mes específicos
    staff_cost = None
    if form.year.data and form.month.data > 0:
        staff_cost = calculate_staff_cost(
            company_id, form.year.data, form.month.data, form.week.data or None
        )
    
    return render_template(
        'cash_register/company_report.html',
        title=f'Informe de Arqueos - {company.name}',
        form=form,
        company=company,
        registers=registers,
        totals=totals,
        payment_methods_data=json.dumps(payment_methods_data),
        staff_cost=staff_cost,
        format_currency=format_currency,
        format_percentage=format_percentage
    )


@cash_register_bp.route('/company/<int:company_id>/tokens', methods=['GET', 'POST'])
@login_required
def manage_tokens(company_id):
    """
    Gestionar tokens de acceso para una empresa.
    
    Args:
        company_id: ID de la empresa
    """
    # Importar modelos para evitar problemas de importación circular
    from models import Company, Employee
    from models_cash_register import CashRegisterToken
    
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta empresa', 'danger')
        return redirect(url_for('cash_register.dashboard'))
    
    # Crear formulario para generar tokens
    form = CashRegisterTokenForm()
    form.company_id.data = company_id
    form.company_id.choices = [(company.id, company.name)]
    
    # Cargar lista de empleados para el selector
    employees = Employee.query.filter_by(company_id=company_id, is_active=True).all()
    employee_choices = [(0, 'Sin asignar')] + [(e.id, f"{e.first_name} {e.last_name}") for e in employees]
    form.employee_id.choices = employee_choices
    
    if form.validate_on_submit():
        try:
            # Generar nuevo token
            employee_id = form.employee_id.data if form.employee_id.data != 0 else None
            expiry_days = form.expiry_days.data or 7  # Valor por defecto: 7 días
            pin = form.pin.data if form.pin.data else None
            
            logger.info(f"Generando token para empresa {company_id}, empleado {employee_id}, expiración {expiry_days} días, PIN: {'Sí' if pin else 'No'}")
            
            # Usar método del modelo para generar token
            token = CashRegisterToken.generate_token(
                company_id=company_id,
                employee_id=employee_id,
                created_by_id=current_user.id,
                expiry_days=expiry_days
            )
            
            # Guardar el PIN si se ha proporcionado
            if pin:
                token.pin = pin
                db.session.commit()
            
            # Generar URL para compartir
            base_url = request.host_url.rstrip('/')
            token_url = generate_token_url(token.token, base_url)
            
            flash('Token generado correctamente', 'success')
            
            # Redirigir a la página de tokens con el nuevo token marcado
            return redirect(url_for('cash_register.token_created', 
                                   company_id=company_id, 
                                   token_id=token.id))
            
        except Exception as e:
            db.session.rollback()
            logger.error(f"Error al generar token: {str(e)}")
            flash(f'Error al generar token: {str(e)}', 'danger')
    
    # Obtener tokens activos
    active_tokens = CashRegisterToken.query.filter_by(
        company_id=company_id,
        is_active=True
    ).all()
    
    # Obtener tokens usados
    used_tokens = CashRegisterToken.query.filter_by(
        company_id=company_id,
        is_active=False
    ).filter(CashRegisterToken.used_at.isnot(None)).all()
    
    # Obtener tokens expirados
    expired_tokens = CashRegisterToken.query.filter_by(
        company_id=company_id,
        is_active=True
    ).filter(CashRegisterToken.expires_at < datetime.now()).all()
    
    return render_template(
        'cash_register/manage_tokens.html',
        title=f'Gestión de Tokens - {company.name}',
        form=form,
        company=company,
        active_tokens=active_tokens,
        used_tokens=used_tokens,
        expired_tokens=expired_tokens,
        base_url=request.host_url.rstrip('/'),
        generate_token_url=generate_token_url
    )


@cash_register_bp.route('/company/<int:company_id>/tokens/<int:token_id>')
@login_required
def token_created(company_id, token_id):
    """
    Mostrar detalles de un token recién creado.
    
    Args:
        company_id: ID de la empresa
        token_id: ID del token creado
    """
    # Importar modelos para evitar problemas de importación circular
    from models import Company
    from models_cash_register import CashRegisterToken
    
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta empresa', 'danger')
        return redirect(url_for('cash_register.dashboard'))
    
    # Obtener token
    token = CashRegisterToken.query.get_or_404(token_id)
    
    # Verificar que el token pertenece a la empresa
    if token.company_id != company_id:
        flash('Token no válido para esta empresa', 'danger')
        return redirect(url_for('cash_register.manage_tokens', company_id=company_id))
    
    # Generar URL para compartir
    base_url = request.host_url.rstrip('/')
    # Si tiene PIN, dirigir a la pantalla de verificación; de lo contrario, al formulario directamente
    if token.pin:
        token_url = url_for('cash_register.verify_pin', token_str=token.token, _external=True)
    else:
        token_url = url_for('cash_register.public_register', token_str=token.token, _external=True)
    
    # Obtener tokens activos (para mostrar en la misma página)
    active_tokens = CashRegisterToken.query.filter_by(
        company_id=company_id,
        is_active=True
    ).all()
    
    return render_template(
        'cash_register/token_created.html',
        title=f'Token Generado - {company.name}',
        company=company,
        token=token,
        token_url=token_url,
        active_tokens=active_tokens,
        base_url=request.host_url.rstrip('/'),
        generate_token_url=generate_token_url
    )


@cash_register_bp.route('/token/<string:token_str>', methods=['GET', 'POST'])
def public_register(token_str):
    """
    Acceso público para empleados mediante token.
    
    Args:
        token_str: String del token de acceso
    """
    # Importar modelos para evitar problemas de importación circular
    from models_cash_register import CashRegister, CashRegisterToken
    
    # Verificar token
    token = CashRegisterToken.query.filter_by(token=token_str).first()
    
    if not token:
        flash('Token no válido o expirado', 'danger')
        return redirect(url_for('auth.login'))
    
    # Verificar que el token esté activo y no haya expirado
    if not token.is_active or (token.expires_at and token.expires_at < datetime.now()):
        flash('Token expirado o desactivado', 'danger')
        return redirect(url_for('auth.login'))
    
    # Verificar que el token esté activo (no comprobamos cash_register_id para permitir múltiples envíos)
    # Sólo verificamos su estado y fecha de expiración
    
    # Obtener la empresa
    company = token.company
    
    # Si el token tiene PIN, verificar que el usuario lo haya ingresado
    # El PIN se verifica en la sesión para no tener que pedirlo múltiples veces
    if token.pin and session.get(f'pin_verified_{token_str}') != True:
        return redirect(url_for('cash_register.verify_pin', token_str=token_str))
    
    # Crear formulario
    form = PublicCashRegisterForm()
    form.token.data = token_str
    
    if form.validate_on_submit():
        try:
            logger.info(f"Iniciando creación de arqueo mediante token {token_str} para empresa {company.id}")
            
            # Verificar si ya existe un arqueo para esta fecha
            existing_register = CashRegister.query.filter_by(
                company_id=company.id,
                date=form.date.data
            ).first()
            
            if existing_register:
                logger.warning(f"Ya existe un arqueo para la fecha {form.date.data} en empresa {company.id}")
                flash(f'Ya existe un arqueo para la fecha {form.date.data.strftime("%d/%m/%Y")}', 'danger')
                return redirect(url_for('cash_register.public_register', token_str=token_str))
            
            logger.info(f"Creando nuevo arqueo por token: fecha={form.date.data}, total={form.total_amount.data}")
            
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
                notes=form.notes.data,
                employee_id=token.employee_id,
                employee_name=form.employee_name.data
            )
            
            logger.info("Añadiendo registro a la sesión de base de datos")
            db.session.add(register)
            
            # Actualizar el token para registrar que se ha usado
            logger.info(f"Actualizando estado del token {token.id}")
            token.used_at = datetime.now()
            # Ya no marcamos el token como inactivo para permitir múltiples usos
            # token.is_active = False
            
            # Asignar el token al registro
            logger.info(f"Asignando token {token.id} al arqueo")
            register.token_id = token.id
            
            # Guardar los cambios
            logger.info("Ejecutando commit para guardar el arqueo")
            db.session.commit()
            
            # Actualizar resumen semanal y mensual
            year = form.date.data.year
            week_number = get_week_number(form.date.data)
            logger.info(f"Actualizando resumen semanal: año={year}, semana={week_number}")
            calculate_weekly_summary(company.id, year, week_number)
            
            logger.info("Arqueo por token registrado correctamente")
            
            # Mostrar página de confirmación
            return render_template(
                'cash_register/public_success.html',
                title='Arqueo Enviado',
                company=company,
                register=register,
                format_currency=format_currency
            )
            
        except Exception as e:
            db.session.rollback()
            logger.error(f"Error al crear arqueo: {str(e)}")
            flash(f'Error al crear arqueo: {str(e)}', 'danger')
    
    return render_template(
        'cash_register/public_register.html',
        title='Enviar Arqueo de Caja',
        form=form,
        company=company,
        token=token
    )


@cash_register_bp.route('/token/<int:token_id>/deactivate', methods=['POST'])
@login_required
def deactivate_token(token_id):
    """
    Desactivar un token de acceso.
    
    Args:
        token_id: ID del token a desactivar
    """
    # Importar modelos para evitar problemas de importación circular
    from models_cash_register import CashRegisterToken
    
    # Obtener token
    token = CashRegisterToken.query.get_or_404(token_id)
    
    # Verificar permisos
    company = token.company
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene permisos para esta acción', 'danger')
        return redirect(url_for('cash_register.dashboard'))
    
    try:
        # Desactivar token
        token.is_active = False
        db.session.commit()
        
        flash('Token desactivado correctamente', 'success')
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al desactivar token: {str(e)}")
        flash(f'Error al desactivar token: {str(e)}', 'danger')
    
    return redirect(url_for('cash_register.manage_tokens', company_id=token.company_id))


@cash_register_bp.route('/token/<string:token_str>/verify_pin', methods=['GET', 'POST'])
def verify_pin(token_str):
    """
    Verificar el PIN de acceso para un token.
    
    Args:
        token_str: String del token de acceso
    """
    # Importar modelos para evitar problemas de importación circular
    from models_cash_register import CashRegisterToken
    
    # Verificar token
    token = CashRegisterToken.query.filter_by(token=token_str).first()
    
    if not token:
        flash('Token no válido o expirado', 'danger')
        return redirect(url_for('auth.login'))
    
    # Verificar que el token esté activo y no haya expirado
    if not token.is_active or (token.expires_at and token.expires_at < datetime.now()):
        flash('Token expirado o desactivado', 'danger')
        return redirect(url_for('auth.login'))
    
    # Si no tiene PIN, redirigir directamente al formulario
    if not token.pin:
        session[f'pin_verified_{token_str}'] = True
        return redirect(url_for('cash_register.public_register', token_str=token_str))
    
    # Crear formulario de verificación
    form = PinVerificationForm()
    form.token.data = token_str
    
    # Si envió el formulario, verificar PIN
    if form.validate_on_submit():
        entered_pin = form.pin.data
        if entered_pin == token.pin:
            # PIN correcto, marcar como verificado en la sesión
            session[f'pin_verified_{token_str}'] = True
            return redirect(url_for('cash_register.public_register', token_str=token_str))
        else:
            # PIN incorrecto
            return render_template(
                'cash_register/pin_verification.html',
                title='Verificación de PIN',
                form=form,
                pin_error='PIN incorrecto. Por favor, inténtalo de nuevo.'
            )
    
    # Mostrar formulario de verificación
    return render_template(
        'cash_register/pin_verification.html',
        title='Verificación de PIN',
        form=form
    )


@cash_register_bp.route('/token/<int:token_id>/qr', methods=['GET'])
@login_required
def generate_token_qr(token_id):
    """
    Genera un código QR para un token de acceso.
    
    Args:
        token_id: ID del token para el que generar el QR
    """
    # Importar modelos para evitar problemas de importación circular
    from models_cash_register import CashRegisterToken
    import qrcode
    import io
    from flask import send_file
    
    # Obtener token
    token = CashRegisterToken.query.get_or_404(token_id)
    
    # Verificar permisos
    company = token.company
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene permisos para esta acción', 'danger')
        return redirect(url_for('cash_register.dashboard'))
    
    try:
        # Generar URL del token
        # Si tiene PIN, dirigir a la pantalla de verificación; de lo contrario, al formulario directamente
        if token.pin:
            token_url = url_for('cash_register.verify_pin', token_str=token.token, _external=True)
        else:
            token_url = url_for('cash_register.public_register', token_str=token.token, _external=True)
        
        # Crear código QR
        qr = qrcode.QRCode(
            version=1,
            error_correction=qrcode.constants.ERROR_CORRECT_L,
            box_size=10,
            border=4,
        )
        qr.add_data(token_url)
        qr.make(fit=True)
        
        # Crear imagen
        img = qr.make_image(fill_color="black", back_color="white")
        
        # Guardar imagen en buffer
        img_io = io.BytesIO()
        img.save(img_io, 'PNG')
        img_io.seek(0)
        
        # Enviar imagen como respuesta
        return send_file(img_io, mimetype='image/png', download_name=f'token_qr_{token_id}.png')
    except Exception as e:
        logger.error(f"Error al generar QR para token {token_id}: {str(e)}")
        flash(f'Error al generar código QR: {str(e)}', 'danger')
        return redirect(url_for('cash_register.manage_tokens', company_id=token.company_id))
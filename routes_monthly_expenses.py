"""
Rutas para el módulo de gestión de gastos mensuales.

Este módulo define las rutas y funciones necesarias para el sistema de gastos mensuales,
incluyendo vistas para categorías, gastos fijos, gastos mensuales y reportes.
"""

import os
import datetime
import json
import calendar
from datetime import date

from flask import Blueprint, render_template, redirect, url_for, request, flash, jsonify, abort, current_app
from flask_login import login_required, current_user
from sqlalchemy import extract, func, desc, asc
from sqlalchemy.exc import SQLAlchemyError
from werkzeug.utils import secure_filename

from app import db
from models import Company
from models_monthly_expenses import ExpenseCategory, FixedExpense, MonthlyExpense, MonthlyExpenseSummary, MonthlyExpenseToken
from forms_monthly_expenses import ExpenseCategoryForm, FixedExpenseForm, MonthlyExpenseForm, PeriodSelectorForm, MonthlyExpenseSearchForm
from forms_monthly_expenses import MonthlyExpenseTokenForm, EmployeeExpenseForm


# Crear Blueprint para las rutas de gastos mensuales
monthly_expenses_bp = Blueprint('monthly_expenses', __name__, url_prefix='/monthly-expenses')

# Ruta para seleccionar empresa
@monthly_expenses_bp.route('/')
@login_required
def select_company():
    """
    Permite seleccionar una empresa para acceder a sus gastos mensuales.
    Si el usuario solo tiene una empresa, redirecciona directamente a ella.
    """
    # Obtener las empresas del usuario
    if current_user.is_admin():
        companies = Company.query.all()
    else:
        companies = current_user.companies

    # Si no hay empresas disponibles, mostrar error
    if not companies:
        flash('No tiene empresas asignadas. Contacte con el administrador.', 'danger')
        return redirect(url_for('main.dashboard'))

    # Si solo hay una empresa, redireccionar directamente
    if len(companies) == 1:
        return redirect(url_for('monthly_expenses.company_dashboard', company_id=companies[0].id))

    # Mostrar lista de empresas para seleccionar
    return render_template(
        'monthly_expenses/select_company.html',
        companies=companies,
        title="Seleccionar Empresa"
    )


# Utilidades y funciones auxiliares
def format_currency(amount):
    """Formatea un valor numérico como moneda en formato EUR"""
    if amount is None:
        return "0,00 €"
    return f"{amount:,.2f} €".replace(".", "*").replace(",", ".").replace("*", ",")


def get_or_create_summary(company_id, year, month):
    """
    Obtiene o crea un resumen mensual de gastos para una empresa, año y mes específicos.
    También actualiza los totales si el resumen ya existe.
    """
    # Buscar si ya existe el resumen
    summary = MonthlyExpenseSummary.query.filter_by(
        company_id=company_id,
        year=year,
        month=month
    ).first()
    
    # Si no existe, crearlo
    if not summary:
        summary = MonthlyExpenseSummary(
            company_id=company_id,
            year=year,
            month=month,
            fixed_expenses_total=0.0,
            custom_expenses_total=0.0,
            total_amount=0.0,
            number_of_expenses=0
        )
        db.session.add(summary)
    
    # Calcular los totales
    # 1. Total de gastos fijos incluidos para este mes
    fixed_expenses_query = db.session.query(func.sum(MonthlyExpense.amount)).filter(
        MonthlyExpense.company_id == company_id,
        MonthlyExpense.year == year,
        MonthlyExpense.month == month,
        MonthlyExpense.is_fixed == True
    )
    fixed_total = fixed_expenses_query.scalar() or 0.0
    
    # 2. Total de gastos personalizados (no fijos) para este mes
    custom_expenses_query = db.session.query(func.sum(MonthlyExpense.amount)).filter(
        MonthlyExpense.company_id == company_id,
        MonthlyExpense.year == year,
        MonthlyExpense.month == month,
        MonthlyExpense.is_fixed == False
    )
    custom_total = custom_expenses_query.scalar() or 0.0
    
    # 3. Contar el número total de gastos
    expenses_count = MonthlyExpense.query.filter_by(
        company_id=company_id,
        year=year,
        month=month
    ).count()
    
    # Actualizar el resumen con los nuevos valores
    summary.fixed_expenses_total = fixed_total
    summary.custom_expenses_total = custom_total
    summary.total_amount = fixed_total + custom_total
    summary.number_of_expenses = expenses_count
    summary.updated_at = datetime.datetime.utcnow()
    
    # Guardar cambios
    db.session.commit()
    
    return summary


def update_all_summaries_for_company(company_id):
    """
    Actualiza todos los resúmenes mensuales de gastos para una empresa.
    Útil después de operaciones masivas que afectan múltiples meses.
    """
    # Obtener todos los distintos pares año/mes que tienen gastos para esta empresa
    month_year_pairs = db.session.query(
        MonthlyExpense.year, 
        MonthlyExpense.month
    ).filter_by(
        company_id=company_id
    ).distinct().all()
    
    # Actualizar cada resumen individualmente
    for year, month in month_year_pairs:
        get_or_create_summary(company_id, year, month)
    
    return len(month_year_pairs)


# Rutas principales
@monthly_expenses_bp.route('/company/<int:company_id>')
@login_required
def company_dashboard(company_id):
    """
    Muestra el dashboard de gastos mensuales para una empresa específica.
    Por defecto muestra el mes y año actual, pero permite filtrar por mes/año.
    """
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(company_id):
        flash('No tiene permisos para acceder a esta empresa.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Obtener la empresa
    company = Company.query.get_or_404(company_id)
    
    # Obtener parámetros de filtro
    year = request.args.get('year', type=int)
    month = request.args.get('month', type=int)
    
    # Si no se proporcionan, usar el mes y año actual
    today = datetime.datetime.now()
    if not year:
        year = today.year
    if not month:
        month = today.month
    
    # Obtener el resumen mensual (o crearlo si no existe)
    summary = get_or_create_summary(company_id, year, month)
    
    # Obtener gastos fijos activos desde la tabla FixedExpense
    active_fixed_expenses = FixedExpense.query.filter_by(
        company_id=company_id,
        is_active=True
    ).order_by(FixedExpense.name).all()
    
    # Obtener gastos fijos registrados para este mes específico
    fixed_expenses_this_month = MonthlyExpense.query.filter_by(
        company_id=company_id,
        year=year,
        month=month,
        is_fixed=True
    ).order_by(MonthlyExpense.name).all()
    
    # Combinar ambas listas para el dashboard, priorizando los que ya existen para este mes
    # Primero, vamos a convertir los gastos fijos activos en gastos mensuales si no existen ya
    existing_fixed_names = [expense.name for expense in fixed_expenses_this_month]
    
    # Si no hay gastos fijos convertidos para este mes, crearlos a partir de los gastos fijos activos
    if not fixed_expenses_this_month and active_fixed_expenses:
        for fixed_expense in active_fixed_expenses:
            # Crear un gasto mensual a partir del gasto fijo
            monthly_fixed = MonthlyExpense(
                company_id=company_id,
                year=year,
                month=month,
                name=fixed_expense.name,
                description=fixed_expense.description,
                amount=fixed_expense.amount,
                category_id=fixed_expense.category_id,
                is_fixed=True,
                created_at=datetime.datetime.utcnow(),
                updated_at=datetime.datetime.utcnow()
            )
            db.session.add(monthly_fixed)
        
        # Guardar todos los nuevos gastos fijos convertidos
        try:
            db.session.commit()
            # Refrescar la lista de gastos fijos para este mes
            fixed_expenses_this_month = MonthlyExpense.query.filter_by(
                company_id=company_id,
                year=year,
                month=month,
                is_fixed=True
            ).order_by(MonthlyExpense.name).all()
            
            # Actualizar el resumen con los nuevos gastos
            summary = get_or_create_summary(company_id, year, month)
        except SQLAlchemyError as e:
            db.session.rollback()
            flash(f'Error al crear gastos fijos para este mes: {str(e)}', 'warning')
    
    # Usar los gastos fijos mensuales para mostrar en el dashboard
    fixed_expenses = fixed_expenses_this_month
    
    # Obtener gastos personalizados para este mes
    custom_expenses = MonthlyExpense.query.filter_by(
        company_id=company_id,
        year=year,
        month=month,
        is_fixed=False
    ).order_by(MonthlyExpense.name).all()
    
    # Obtener todos los resúmenes para la navegación entre meses
    all_summaries = MonthlyExpenseSummary.query.filter_by(
        company_id=company_id
    ).order_by(MonthlyExpenseSummary.year, MonthlyExpenseSummary.month).all()
    
    # Preparar formulario de selección de período
    form = PeriodSelectorForm()
    form.year.choices = [(y, str(y)) for y in range(today.year - 5, today.year + 6)]
    form.month.choices = [
        (1, 'Enero'), (2, 'Febrero'), (3, 'Marzo'), (4, 'Abril'),
        (5, 'Mayo'), (6, 'Junio'), (7, 'Julio'), (8, 'Agosto'),
        (9, 'Septiembre'), (10, 'Octubre'), (11, 'Noviembre'), (12, 'Diciembre')
    ]
    form.year.default = year
    form.month.default = month
    form.process()
    
    # Obtener mes anterior y siguiente para navegación
    prev_month = month - 1
    prev_year = year
    if prev_month < 1:
        prev_month = 12
        prev_year -= 1
        
    next_month = month + 1
    next_year = year
    if next_month > 12:
        next_month = 1
        next_year += 1
    
    # Calcular el número de días en el mes para estadísticas
    days_in_month = calendar.monthrange(year, month)[1]
    
    # Calcular promedio diario de gastos
    daily_avg = summary.total_amount / days_in_month if summary.total_amount > 0 else 0
    
    return render_template(
        'monthly_expenses/company_dashboard.html',
        company=company,
        summary=summary,
        fixed_expenses=fixed_expenses,
        custom_expenses=custom_expenses,
        all_summaries=all_summaries,
        form=form,
        current_year=year,
        current_month=month,
        prev_month=prev_month,
        prev_year=prev_year,
        next_month=next_month,
        next_year=next_year,
        days_in_month=days_in_month,
        daily_avg=daily_avg,
        format_currency=format_currency
    )


# Rutas para gestión de categorías
@monthly_expenses_bp.route('/categories/<int:company_id>', methods=['GET', 'POST'])
@login_required
def manage_categories(company_id):
    """
    Gestiona las categorías de gastos para una empresa específica.
    Permite ver, crear, editar y eliminar categorías.
    """
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(company_id):
        flash('No tiene permisos para acceder a esta empresa.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Obtener la empresa
    company = Company.query.get_or_404(company_id)
    
    # Obtener categorías del sistema (predefinidas)
    system_categories = ExpenseCategory.query.filter_by(
        is_system=True
    ).order_by(ExpenseCategory.name).all()
    
    # Obtener categorías personalizadas de la empresa
    custom_categories = ExpenseCategory.query.filter_by(
        company_id=company_id,
        is_system=False
    ).order_by(ExpenseCategory.name).all()
    
    # Preparar formulario de creación de categoría
    form = ExpenseCategoryForm()
    
    # Procesar formulario de creación de categoría
    if form.validate_on_submit():
        try:
            # Crear nueva categoría
            category = ExpenseCategory(
                name=form.name.data,
                description=form.description.data,
                company_id=company_id,
                is_system=False
            )
            db.session.add(category)
            db.session.commit()
            
            flash('Categoría creada correctamente.', 'success')
            return redirect(url_for('monthly_expenses.manage_categories', company_id=company_id))
        except SQLAlchemyError as e:
            db.session.rollback()
            flash(f'Error al crear la categoría: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/categories.html',
        company=company,
        system_categories=system_categories,
        custom_categories=custom_categories,
        form=form
    )


@monthly_expenses_bp.route('/categories/edit/<int:category_id>', methods=['GET', 'POST'])
@login_required
def edit_category(category_id):
    """
    Edita una categoría de gastos existente.
    """
    # Obtener la categoría
    category = ExpenseCategory.query.get_or_404(category_id)
    
    # Verificar que no sea una categoría del sistema
    if category.is_system:
        flash('No se pueden editar las categorías del sistema.', 'warning')
        return redirect(url_for('monthly_expenses.manage_categories', company_id=category.company_id))
    
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(category.company_id):
        flash('No tiene permisos para editar esta categoría.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Obtener la empresa
    company = Company.query.get_or_404(category.company_id)
    
    # Preparar formulario
    form = ExpenseCategoryForm(obj=category)
    
    # Procesar formulario
    if form.validate_on_submit():
        try:
            # Actualizar categoría
            category.name = form.name.data
            category.description = form.description.data
            category.updated_at = datetime.datetime.utcnow()
            
            db.session.commit()
            
            flash('Categoría actualizada correctamente.', 'success')
            return redirect(url_for('monthly_expenses.manage_categories', company_id=category.company_id))
        except SQLAlchemyError as e:
            db.session.rollback()
            flash(f'Error al actualizar la categoría: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/category_edit.html',
        company=company,
        category=category,
        form=form
    )


@monthly_expenses_bp.route('/categories/delete/<int:category_id>', methods=['POST'])
@login_required
def delete_category(category_id):
    """
    Elimina una categoría de gastos existente.
    """
    # Obtener la categoría
    category = ExpenseCategory.query.get_or_404(category_id)
    
    # Verificar que no sea una categoría del sistema
    if category.is_system:
        flash('No se pueden eliminar las categorías del sistema.', 'warning')
        return redirect(url_for('monthly_expenses.manage_categories', company_id=category.company_id))
    
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(category.company_id):
        flash('No tiene permisos para eliminar esta categoría.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Almacenar company_id para redirección después de eliminar
    company_id = category.company_id
    
    # Verificar si hay gastos usando esta categoría
    has_fixed_expenses = FixedExpense.query.filter_by(category_id=category_id).count() > 0
    has_monthly_expenses = MonthlyExpense.query.filter_by(category_id=category_id).count() > 0
    
    if has_fixed_expenses or has_monthly_expenses:
        flash('No se puede eliminar esta categoría porque está siendo utilizada por uno o más gastos.', 'warning')
        return redirect(url_for('monthly_expenses.manage_categories', company_id=company_id))
    
    try:
        # Eliminar la categoría
        db.session.delete(category)
        db.session.commit()
        
        flash('Categoría eliminada correctamente.', 'success')
    except SQLAlchemyError as e:
        db.session.rollback()
        flash(f'Error al eliminar la categoría: {str(e)}', 'danger')
    
    return redirect(url_for('monthly_expenses.manage_categories', company_id=company_id))


# Rutas para gestión de gastos mensuales
@monthly_expenses_bp.route('/new-expense/<int:company_id>', methods=['GET', 'POST'])
@login_required
def new_monthly_expense(company_id):
    """
    Crea un nuevo gasto mensual para una empresa específica.
    """
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(company_id):
        flash('No tiene permisos para acceder a esta empresa.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Obtener la empresa
    company = Company.query.get_or_404(company_id)
    
    # Obtener parámetros de año y mes desde la URL (si se proporcionan)
    year = request.args.get('year', type=int) or datetime.datetime.now().year
    month = request.args.get('month', type=int) or datetime.datetime.now().month
    
    # Preparar formulario
    form = MonthlyExpenseForm()
    form.company_id.data = company_id
    
    # Cargar opciones para los campos select
    form.year.choices = [(y, str(y)) for y in range(year - 5, year + 6)]
    form.month.choices = [
        (1, 'Enero'), (2, 'Febrero'), (3, 'Marzo'), (4, 'Abril'),
        (5, 'Mayo'), (6, 'Junio'), (7, 'Julio'), (8, 'Agosto'),
        (9, 'Septiembre'), (10, 'Octubre'), (11, 'Noviembre'), (12, 'Diciembre')
    ]
    
    # Establecer valores por defecto
    form.year.default = year
    form.month.default = month
    
    # Cargar categorías disponibles
    categories = ExpenseCategory.query.filter(
        (ExpenseCategory.company_id == company_id) | 
        (ExpenseCategory.is_system == True)
    ).order_by(ExpenseCategory.name).all()
    
    form.category_id.choices = [(c.id, c.name) for c in categories]
    
    # Procesar formulario solo si no es la carga inicial
    if request.method == 'GET':
        form.process()
    elif form.validate_on_submit():
        try:
            # Crear nuevo gasto mensual
            expense = MonthlyExpense(
                company_id=company_id,
                year=form.year.data,
                month=form.month.data,
                name=form.name.data,
                description=form.description.data,
                amount=form.amount.data,
                category_id=form.category_id.data,
                is_fixed=form.is_fixed.data
            )
            db.session.add(expense)
            
            # Si se marcó como gasto fijo, también crear un gasto fijo
            if form.is_fixed.data:
                fixed_expense = FixedExpense(
                    company_id=company_id,
                    name=form.name.data,
                    description=form.description.data,
                    amount=form.amount.data,
                    category_id=form.category_id.data,
                    is_active=True
                )
                db.session.add(fixed_expense)
            
            db.session.commit()
            
            # Actualizar resumen mensual
            get_or_create_summary(company_id, form.year.data, form.month.data)
            
            flash('Gasto mensual creado correctamente.', 'success')
            return redirect(url_for(
                'monthly_expenses.company_dashboard', 
                company_id=company_id,
                year=form.year.data,
                month=form.month.data
            ))
        except SQLAlchemyError as e:
            db.session.rollback()
            flash(f'Error al crear el gasto mensual: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/monthly_expense_form.html',
        company=company,
        form=form,
        is_new=True
    )


@monthly_expenses_bp.route('/edit-expense/<int:expense_id>', methods=['GET', 'POST'])
@login_required
def edit_monthly_expense(expense_id):
    """
    Edita un gasto mensual existente.
    """
    # Obtener el gasto
    expense = MonthlyExpense.query.get_or_404(expense_id)
    
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(expense.company_id):
        flash('No tiene permisos para editar este gasto.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Obtener la empresa
    company = Company.query.get_or_404(expense.company_id)
    
    # Preparar formulario
    form = MonthlyExpenseForm(obj=expense)
    form.company_id.data = expense.company_id
    
    # Cargar opciones para los campos select
    form.year.choices = [(y, str(y)) for y in range(expense.year - 5, expense.year + 6)]
    form.month.choices = [
        (1, 'Enero'), (2, 'Febrero'), (3, 'Marzo'), (4, 'Abril'),
        (5, 'Mayo'), (6, 'Junio'), (7, 'Julio'), (8, 'Agosto'),
        (9, 'Septiembre'), (10, 'Octubre'), (11, 'Noviembre'), (12, 'Diciembre')
    ]
    
    # Cargar categorías disponibles
    categories = ExpenseCategory.query.filter(
        (ExpenseCategory.company_id == expense.company_id) | 
        (ExpenseCategory.is_system == True)
    ).order_by(ExpenseCategory.name).all()
    
    form.category_id.choices = [(c.id, c.name) for c in categories]
    
    # Procesar formulario
    if form.validate_on_submit():
        try:
            # Actualizar gasto mensual
            expense.name = form.name.data
            expense.description = form.description.data
            expense.amount = form.amount.data
            expense.category_id = form.category_id.data
            expense.updated_at = datetime.datetime.utcnow()
            
            # Si cambia el año o mes, puede requerir actualizar diferentes resúmenes
            old_year = expense.year
            old_month = expense.month
            
            expense.year = form.year.data
            expense.month = form.month.data
            
            db.session.commit()
            
            # Actualizar resúmenes afectados
            if old_year != form.year.data or old_month != form.month.data:
                # Actualizar tanto el resumen antiguo como el nuevo
                get_or_create_summary(expense.company_id, old_year, old_month)
                get_or_create_summary(expense.company_id, form.year.data, form.month.data)
            else:
                # Solo actualizar el resumen actual
                get_or_create_summary(expense.company_id, form.year.data, form.month.data)
            
            flash('Gasto mensual actualizado correctamente.', 'success')
            return redirect(url_for(
                'monthly_expenses.company_dashboard', 
                company_id=expense.company_id,
                year=form.year.data,
                month=form.month.data
            ))
        except SQLAlchemyError as e:
            db.session.rollback()
            flash(f'Error al actualizar el gasto mensual: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/monthly_expense_form.html',
        company=company,
        expense=expense,
        form=form,
        is_new=False
    )


@monthly_expenses_bp.route('/delete-expense/<int:expense_id>', methods=['POST'])
@login_required
def delete_monthly_expense(expense_id):
    """
    Elimina un gasto mensual existente.
    """
    # Obtener el gasto
    expense = MonthlyExpense.query.get_or_404(expense_id)
    
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(expense.company_id):
        flash('No tiene permisos para eliminar este gasto.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Almacenar datos para redirección después de eliminar
    company_id = expense.company_id
    year = expense.year
    month = expense.month
    
    try:
        # Eliminar el gasto
        db.session.delete(expense)
        db.session.commit()
        
        # Actualizar resumen mensual
        get_or_create_summary(company_id, year, month)
        
        flash('Gasto mensual eliminado correctamente.', 'success')
    except SQLAlchemyError as e:
        db.session.rollback()
        flash(f'Error al eliminar el gasto mensual: {str(e)}', 'danger')
    
    return redirect(url_for(
        'monthly_expenses.company_dashboard', 
        company_id=company_id,
        year=year,
        month=month
    ))


# Rutas para la gestión de tokens de gastos
@monthly_expenses_bp.route('/tokens/<int:company_id>', methods=['GET', 'POST'])
@login_required
def manage_tokens(company_id):
    """
    Gestiona los tokens para que los empleados puedan enviar gastos.
    Permite crear, editar, activar/desactivar tokens.
    """
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(company_id):
        flash('No tiene permisos para acceder a esta empresa.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Obtener la empresa
    company = Company.query.get_or_404(company_id)
    
    # Obtener todos los tokens de la empresa
    tokens = MonthlyExpenseToken.query.filter_by(
        company_id=company_id
    ).order_by(MonthlyExpenseToken.name).all()
    
    # Preparar categorías para el selector
    categories = ExpenseCategory.query.filter(
        (ExpenseCategory.company_id == company_id) | (ExpenseCategory.is_system == True)
    ).order_by(ExpenseCategory.name).all()
    
    # Preparar formulario para nuevo token
    form = MonthlyExpenseTokenForm()
    form.category_id.choices = [(c.id, c.name) for c in categories]
    form.category_id.choices.insert(0, (0, 'Ninguna (el empleado seleccionará)'))
    
    # Procesar formulario
    if form.validate_on_submit():
        try:
            # Crear nuevo token
            token = MonthlyExpenseToken(
                company_id=company_id,
                token=MonthlyExpenseToken.generate_token(),
                name=form.name.data,
                description=form.description.data,
                is_active=form.is_active.data,
                category_id=form.category_id.data if form.category_id.data != 0 else None
            )
            db.session.add(token)
            db.session.commit()
            
            flash(f'Token "{token.name}" creado correctamente: {token.token}', 'success')
            return redirect(url_for('monthly_expenses.manage_tokens', company_id=company_id))
        except SQLAlchemyError as e:
            db.session.rollback()
            flash(f'Error al crear el token: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/tokens.html',
        company=company,
        tokens=tokens,
        form=form
    )


@monthly_expenses_bp.route('/tokens/edit/<int:token_id>', methods=['GET', 'POST'])
@login_required
def edit_token(token_id):
    """
    Edita un token existente.
    """
    # Obtener el token
    token = MonthlyExpenseToken.query.get_or_404(token_id)
    
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(token.company_id):
        flash('No tiene permisos para editar este token.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Obtener la empresa
    company = Company.query.get_or_404(token.company_id)
    
    # Preparar categorías para el selector
    categories = ExpenseCategory.query.filter(
        (ExpenseCategory.company_id == company.id) | (ExpenseCategory.is_system == True)
    ).order_by(ExpenseCategory.name).all()
    
    # Preparar formulario
    form = MonthlyExpenseTokenForm(obj=token)
    form.category_id.choices = [(c.id, c.name) for c in categories]
    form.category_id.choices.insert(0, (0, 'Ninguna (el empleado seleccionará)'))
    
    # Si el token no tiene categoría asignada, seleccionar la opción 0
    if not token.category_id and not form.is_submitted():
        form.category_id.data = 0
    
    # Procesar formulario
    if form.validate_on_submit():
        try:
            # Actualizar token
            token.name = form.name.data
            token.description = form.description.data
            token.is_active = form.is_active.data
            token.category_id = form.category_id.data if form.category_id.data != 0 else None
            token.updated_at = datetime.datetime.utcnow()
            
            db.session.commit()
            
            flash(f'Token "{token.name}" actualizado correctamente.', 'success')
            return redirect(url_for('monthly_expenses.manage_tokens', company_id=token.company_id))
        except SQLAlchemyError as e:
            db.session.rollback()
            flash(f'Error al actualizar el token: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/token_edit.html',
        company=company,
        token=token,
        form=form
    )


@monthly_expenses_bp.route('/tokens/toggle/<int:token_id>')
@login_required
def toggle_token(token_id):
    """
    Activa o desactiva un token.
    """
    # Obtener el token
    token = MonthlyExpenseToken.query.get_or_404(token_id)
    
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(token.company_id):
        flash('No tiene permisos para modificar este token.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    try:
        # Cambiar estado del token
        token.is_active = not token.is_active
        db.session.commit()
        
        status = "activado" if token.is_active else "desactivado"
        flash(f'Token "{token.name}" {status} correctamente.', 'success')
    except SQLAlchemyError as e:
        db.session.rollback()
        flash(f'Error al cambiar el estado del token: {str(e)}', 'danger')
    
    return redirect(url_for('monthly_expenses.manage_tokens', company_id=token.company_id))


@monthly_expenses_bp.route('/tokens/regenerate/<int:token_id>')
@login_required
def regenerate_token(token_id):
    """
    Regenera el código de un token existente.
    """
    # Obtener el token
    token = MonthlyExpenseToken.query.get_or_404(token_id)
    
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(token.company_id):
        flash('No tiene permisos para modificar este token.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    try:
        # Generar nuevo código de token
        token.token = MonthlyExpenseToken.generate_token()
        db.session.commit()
        
        flash(f'Token "{token.name}" regenerado: {token.token}', 'success')
    except SQLAlchemyError as e:
        db.session.rollback()
        flash(f'Error al regenerar el token: {str(e)}', 'danger')
    
    return redirect(url_for('monthly_expenses.manage_tokens', company_id=token.company_id))


@monthly_expenses_bp.route('/employee/submit', methods=['GET', 'POST'])
def employee_submit_expense():
    """
    Página pública para que los empleados envíen gastos usando un token.
    No requiere autenticación, solo un token válido.
    """
    # Preparar formulario
    form = EmployeeExpenseForm()
    
    # Inicializar token_info
    token_info = None
    
    # Verificar si hay un token en los parámetros de la URL
    token_param = request.args.get('token', '')
    if token_param and not form.is_submitted():
        form.token.data = token_param
    
    # Obtener fecha actual para el campo de fecha
    today = datetime.datetime.now().strftime('%d-%m-%Y')
    if not form.expense_date.data:
        form.expense_date.data = today
    
    # Si hay token en el formulario, cargar la información
    if form.token.data:
        token_code = form.token.data.strip().upper() if form.token.data else ""
        token = MonthlyExpenseToken.query.filter_by(
            token=token_code,
            is_active=True
        ).first()
        
        if token:
            # Si el token tiene categoría predeterminada, usarla directamente
            category_name = "Otros (predeterminado)"
            if token.category_id:
                category = ExpenseCategory.query.get(token.category_id)
                if category:
                    category_name = category.name
                    # Asignar la categoría al formulario
                    form.category_id.data = category.id
            
            # Preparar información del token para la plantilla
            token_info = {
                'name': token.name,
                'company_id': token.company_id,
                'category_id': token.category_id,
                'category_name': category_name
            }
    
    # Procesar formulario
    if form.validate_on_submit():
        # Buscar el token nuevamente para asegurarnos de que es válido
        token_code = form.token.data.strip().upper() if form.token.data else ""
        token = MonthlyExpenseToken.query.filter_by(
            token=token_code,
            is_active=True
        ).first()
        
        # Verificar token válido
        if not token:
            flash('El token proporcionado no es válido o está desactivado.', 'danger')
            return render_template('monthly_expenses/employee_submit.html', form=form, token_info=None)
        
        # Determinar categoría - Usar la del token siempre que sea posible
        category_id = token.category_id if token.category_id else form.category_id.data
        
        # Si la categoría no está especificada, usar categoría "Otros" del sistema
        if not category_id:
            category = ExpenseCategory.query.filter_by(name='Otros', is_system=True).first()
            if category:
                category_id = category.id
            else:
                flash('Error: No se pudo determinar la categoría del gasto.', 'danger')
                return render_template('monthly_expenses/employee_submit.html', form=form, token_info=token_info)
        
        try:
            # Determinar el año y mes del gasto (actual si no se especifica)
            expense_date = form.expense_date.data
            today = datetime.datetime.now()
            year = today.year
            month = today.month
            
            # Si se proporcionó una fecha válida, extraer año y mes
            if expense_date:
                try:
                    # Intentar parsear la fecha en formato DD-MM-YYYY
                    expense_date_parts = expense_date.split('-')
                    if len(expense_date_parts) == 3:
                        date_obj = datetime.datetime(
                            int(expense_date_parts[2]),  # Año
                            int(expense_date_parts[1]),  # Mes
                            int(expense_date_parts[0])   # Día
                        )
                        year = date_obj.year
                        month = date_obj.month
                except (ValueError, IndexError):
                    # Si hay error al parsear la fecha, usar fecha actual
                    pass
            
            # Guardar la imagen del recibo si se proporciona
            receipt_image_path = None
            if form.receipt_image.data:
                file = form.receipt_image.data
                filename = secure_filename(file.filename)
                # Crear directorio para gastos si no existe
                receipts_dir = os.path.join(current_app.config['UPLOAD_FOLDER'], 'expense_receipts')
                if not os.path.exists(receipts_dir):
                    os.makedirs(receipts_dir)
                
                # Crear directorio para la empresa si no existe
                company_dir = os.path.join(receipts_dir, str(token.company_id))
                if not os.path.exists(company_dir):
                    os.makedirs(company_dir)
                
                # Guardar el archivo con timestamp para evitar duplicados
                timestamp = datetime.datetime.now().strftime('%Y%m%d%H%M%S')
                filename = f"{timestamp}_{filename}"
                file_path = os.path.join(company_dir, filename)
                file.save(file_path)
                
                # Guardar ruta relativa
                receipt_image_path = os.path.join('expense_receipts', str(token.company_id), filename)
            
            # Crear nuevo gasto
            expense = MonthlyExpense(
                name=form.name.data,
                description=form.description.data,
                amount=form.amount.data,
                company_id=token.company_id,
                category_id=category_id,
                year=year,
                month=month,
                expense_date=form.expense_date.data,
                submitted_by_employee=True,
                employee_name=form.employee_name.data,
                receipt_image=receipt_image_path,
                is_fixed=False
            )
            db.session.add(expense)
            
            # Actualizar información del token
            token.last_used_at = datetime.datetime.utcnow()
            token.total_uses += 1
            
            # Actualizar resumen mensual
            summary = get_or_create_summary(token.company_id, year, month)
            
            db.session.commit()
            
            flash('¡Gasto enviado correctamente! Gracias por tu colaboración.', 'success')
            return redirect(url_for('monthly_expenses.employee_submit_expense', token=token.token))
        
        except SQLAlchemyError as e:
            db.session.rollback()
            flash(f'Error al enviar el gasto: {str(e)}', 'danger')
    
    # Renderizar la plantilla con la información del token
    return render_template('monthly_expenses/employee_submit.html', form=form, token_info=token_info)


# Rutas para gestión de gastos fijos
@monthly_expenses_bp.route('/fixed-expenses/<int:company_id>', methods=['GET', 'POST'])
@login_required
def manage_fixed_expenses(company_id):
    """
    Gestiona los gastos fijos de una empresa.
    Muestra, añade, edita y elimina gastos fijos.
    """
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(company_id):
        flash('No tiene permisos para acceder a esta empresa.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Obtener la empresa
    company = Company.query.get_or_404(company_id)
    
    # Obtener todos los gastos fijos
    fixed_expenses = FixedExpense.query.filter_by(
        company_id=company_id
    ).order_by(FixedExpense.name).all()
    
    # Preparar categorías para el selector
    categories = ExpenseCategory.query.filter(
        (ExpenseCategory.company_id == company_id) | (ExpenseCategory.is_system == True)
    ).order_by(ExpenseCategory.name).all()
    
    # Preparar formulario para nuevo gasto fijo
    form = FixedExpenseForm()
    form.company_id.data = company_id
    form.category_id.choices = [(c.id, c.name) for c in categories]
    
    # Procesar formulario
    if form.validate_on_submit():
        try:
            # Crear nuevo gasto fijo
            fixed_expense = FixedExpense(
                company_id=company_id,
                name=form.name.data,
                description=form.description.data,
                amount=form.amount.data,
                category_id=form.category_id.data,
                is_active=form.is_active.data
            )
            db.session.add(fixed_expense)
            db.session.commit()
            
            flash(f'Gasto fijo "{fixed_expense.name}" creado correctamente.', 'success')
            return redirect(url_for('monthly_expenses.manage_fixed_expenses', company_id=company_id))
        except SQLAlchemyError as e:
            db.session.rollback()
            flash(f'Error al crear el gasto fijo: {str(e)}', 'danger')
    
    # Calcular total gastos fijos activos
    total_active = sum(expense.amount for expense in fixed_expenses if expense.is_active)
    
    return render_template(
        'monthly_expenses/fixed_expenses.html',
        company=company,
        fixed_expenses=fixed_expenses,
        form=form,
        total_active=total_active,
        format_currency=format_currency
    )


@monthly_expenses_bp.route('/fixed-expenses/edit/<int:expense_id>', methods=['GET', 'POST'])
@login_required
def edit_fixed_expense(expense_id):
    """
    Edita un gasto fijo existente.
    """
    # Obtener el gasto fijo
    fixed_expense = FixedExpense.query.get_or_404(expense_id)
    
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(fixed_expense.company_id):
        flash('No tiene permisos para editar este gasto fijo.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Obtener la empresa
    company = Company.query.get_or_404(fixed_expense.company_id)
    
    # Preparar categorías para el selector
    categories = ExpenseCategory.query.filter(
        (ExpenseCategory.company_id == company.id) | (ExpenseCategory.is_system == True)
    ).order_by(ExpenseCategory.name).all()
    
    # Preparar formulario
    form = FixedExpenseForm(obj=fixed_expense)
    form.category_id.choices = [(c.id, c.name) for c in categories]
    
    # Procesar formulario
    if form.validate_on_submit():
        try:
            # Actualizar gasto fijo
            fixed_expense.name = form.name.data
            fixed_expense.description = form.description.data
            fixed_expense.amount = form.amount.data
            fixed_expense.category_id = form.category_id.data
            fixed_expense.is_active = form.is_active.data
            fixed_expense.updated_at = datetime.datetime.utcnow()
            
            db.session.commit()
            
            flash(f'Gasto fijo "{fixed_expense.name}" actualizado correctamente.', 'success')
            return redirect(url_for('monthly_expenses.manage_fixed_expenses', company_id=fixed_expense.company_id))
        except SQLAlchemyError as e:
            db.session.rollback()
            flash(f'Error al actualizar el gasto fijo: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/fixed_expense_edit.html',
        company=company,
        expense=fixed_expense,
        form=form
    )


@monthly_expenses_bp.route('/fixed-expenses/delete/<int:expense_id>', methods=['POST'])
@login_required
def delete_fixed_expense(expense_id):
    """
    Elimina un gasto fijo existente.
    """
    # Obtener el gasto fijo
    fixed_expense = FixedExpense.query.get_or_404(expense_id)
    
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(fixed_expense.company_id):
        flash('No tiene permisos para eliminar este gasto fijo.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Almacenar datos para redirección después de eliminar
    company_id = fixed_expense.company_id
    expense_name = fixed_expense.name
    
    try:
        # Eliminar el gasto fijo
        db.session.delete(fixed_expense)
        db.session.commit()
        
        flash(f'Gasto fijo "{expense_name}" eliminado correctamente.', 'success')
    except SQLAlchemyError as e:
        db.session.rollback()
        flash(f'Error al eliminar el gasto fijo: {str(e)}', 'danger')
    
    return redirect(url_for('monthly_expenses.manage_fixed_expenses', company_id=company_id))


@monthly_expenses_bp.route('/fixed-expenses/toggle/<int:expense_id>')
@login_required
def toggle_fixed_expense(expense_id):
    """
    Activa o desactiva un gasto fijo.
    """
    # Obtener el gasto fijo
    fixed_expense = FixedExpense.query.get_or_404(expense_id)
    
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(fixed_expense.company_id):
        flash('No tiene permisos para modificar este gasto fijo.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    try:
        # Cambiar estado del gasto fijo
        fixed_expense.is_active = not fixed_expense.is_active
        db.session.commit()
        
        status = "activado" if fixed_expense.is_active else "desactivado"
        flash(f'Gasto fijo "{fixed_expense.name}" {status} correctamente.', 'success')
    except SQLAlchemyError as e:
        db.session.rollback()
        flash(f'Error al cambiar el estado del gasto fijo: {str(e)}', 'danger')
    
    return redirect(url_for('monthly_expenses.manage_fixed_expenses', company_id=fixed_expense.company_id))


# Rutas para reportes y estadísticas
@monthly_expenses_bp.route('/report/<int:company_id>')
@login_required
def expenses_report(company_id):
    """
    Muestra un informe anual de gastos mensuales para una empresa específica.
    """
    # Verificar permisos
    if not current_user.is_admin() and not current_user.has_company_access(company_id):
        flash('No tiene permisos para acceder a esta empresa.', 'danger')
        return redirect(url_for('main.dashboard'))
    
    # Obtener la empresa
    company = Company.query.get_or_404(company_id)
    
    # Obtener parámetro de año desde la URL
    year = request.args.get('year', type=int) or datetime.datetime.now().year
    
    # Obtener todos los resúmenes para este año
    summaries = MonthlyExpenseSummary.query.filter_by(
        company_id=company_id,
        year=year
    ).order_by(MonthlyExpenseSummary.month).all()
    
    # Calcular totales por categoría para el año
    category_totals = db.session.query(
        ExpenseCategory.name,
        func.sum(MonthlyExpense.amount).label('total')
    ).join(
        MonthlyExpense, 
        ExpenseCategory.id == MonthlyExpense.category_id
    ).filter(
        MonthlyExpense.company_id == company_id,
        MonthlyExpense.year == year
    ).group_by(
        ExpenseCategory.name
    ).order_by(
        desc('total')
    ).all()
    
    # Calcular total anual
    total_year = sum(summary.total_amount for summary in summaries)
    
    # Preparar datos para el gráfico de evolución mensual
    months = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic']
    fixed_data = [0.0] * 12
    custom_data = [0.0] * 12
    
    for summary in summaries:
        month_idx = summary.month - 1  # Ajustar para índice base 0
        fixed_data[month_idx] = float(summary.fixed_expenses_total or 0.0)
        custom_data[month_idx] = float(summary.custom_expenses_total or 0.0)
    
    chart_data = {
        'labels': months,
        'datasets': [
            {
                'label': 'Gastos Fijos',
                'data': fixed_data,
                'backgroundColor': 'rgba(54, 162, 235, 0.5)',
                'borderColor': 'rgba(54, 162, 235, 1)',
                'borderWidth': 1
            },
            {
                'label': 'Gastos Variables',
                'data': custom_data,
                'backgroundColor': 'rgba(255, 99, 132, 0.5)',
                'borderColor': 'rgba(255, 99, 132, 1)',
                'borderWidth': 1
            }
        ]
    }
    
    # Preparar datos para el gráfico de categorías
    if category_totals:
        category_chart_data = {
            'labels': [cat[0] for cat in category_totals],
            'data': [float(cat[1]) for cat in category_totals]
        }
    else:
        category_chart_data = {
            'labels': [],
            'data': []
        }
    
    return render_template(
        'monthly_expenses/report.html',
        company=company,
        summaries=summaries,
        category_totals=category_totals,
        total_year=total_year,
        current_year=year,
        chart_data=json.dumps(chart_data),
        category_chart_data=json.dumps(category_chart_data),
        format_currency=format_currency
    )
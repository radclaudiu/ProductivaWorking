"""
Rutas para el módulo de gastos mensuales.

Define las rutas web para gestionar categorías de gastos,
gastos fijos y gastos mensuales.
"""

import logging
import json
from datetime import datetime
from decimal import Decimal
from flask import Blueprint, render_template, request, redirect, url_for, flash, jsonify
from flask_login import login_required, current_user
from sqlalchemy import extract
from sqlalchemy.exc import SQLAlchemyError

from app import db
from models import Company, User
from models_monthly_expenses import ExpenseCategory, FixedExpense, MonthlyExpense, MonthlyExpenseSummary
from forms_monthly_expenses import ExpenseCategoryForm, FixedExpenseForm, MonthlyExpenseForm, MonthlyExpenseSearchForm
from utils_cash_register import format_currency, format_percentage


# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Crear Blueprint
monthly_expenses_bp = Blueprint('monthly_expenses', __name__, url_prefix='/monthly-expenses')


@monthly_expenses_bp.route('/dashboard')
@login_required
def dashboard():
    """Página principal del módulo de gastos mensuales."""
    # Obtener empresas accesibles para el usuario
    if current_user.is_admin():
        companies = Company.query.all()
    else:
        companies = current_user.companies
    
    return render_template(
        'monthly_expenses/dashboard.html',
        title='Gestión de Gastos Mensuales',
        companies=companies
    )


@monthly_expenses_bp.route('/company/<int:company_id>')
@login_required
def company_dashboard(company_id):
    """
    Dashboard de gastos mensuales para una empresa específica.
    
    Args:
        company_id: ID de la empresa
    """
    from models import Company
    
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta empresa', 'danger')
        return redirect(url_for('monthly_expenses.dashboard'))
    
    # Obtener año y mes de los parámetros URL o usar los actuales
    current_year = request.args.get('year', type=int, default=datetime.now().year)
    current_month = request.args.get('month', type=int, default=datetime.now().month)
    
    # Obtener lista de gastos fijos activos
    fixed_expenses = FixedExpense.query.filter_by(
        company_id=company_id,
        is_active=True
    ).order_by(FixedExpense.name).all()
    
    # Obtener gastos mensuales para el mes y año seleccionados
    monthly_expenses = MonthlyExpense.query.filter_by(
        company_id=company_id,
        year=current_year,
        month=current_month
    ).order_by(MonthlyExpense.name).all()
    
    # Calcular totales
    fixed_total = sum(expense.amount for expense in monthly_expenses if expense.is_fixed)
    custom_total = sum(expense.amount for expense in monthly_expenses if not expense.is_fixed)
    total_expenses = fixed_total + custom_total
    
    # Obtener resumen si existe
    summary = MonthlyExpenseSummary.query.filter_by(
        company_id=company_id,
        year=current_year,
        month=current_month
    ).first()
    
    # Si no existe el resumen, crearlo
    if not summary:
        summary = MonthlyExpenseSummary(
            company_id=company_id,
            year=current_year,
            month=current_month,
            total_amount=total_expenses,
            fixed_expenses_total=fixed_total,
            custom_expenses_total=custom_total
        )
        db.session.add(summary)
        db.session.commit()
    # Si hay discrepancia entre el resumen y los gastos, actualizar el resumen
    elif summary.total_amount != total_expenses or summary.fixed_expenses_total != fixed_total or summary.custom_expenses_total != custom_total:
        summary.total_amount = total_expenses
        summary.fixed_expenses_total = fixed_total
        summary.custom_expenses_total = custom_total
        summary.updated_at = datetime.utcnow()
        db.session.commit()
    
    # Preparar datos para el gráfico de gastos por categoría
    categories_data = {}
    for expense in monthly_expenses:
        category_name = expense.category.name if expense.category else "Sin categoría"
        if category_name in categories_data:
            categories_data[category_name] += expense.amount
        else:
            categories_data[category_name] = expense.amount
    
    # Convertir a formato para Chart.js
    chart_data = {
        'labels': list(categories_data.keys()),
        'data': list(categories_data.values())
    }
    
    # Obtener los meses como nombres
    month_names = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 
                  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre']
    current_month_name = month_names[current_month-1] if 1 <= current_month <= 12 else ''
    
    return render_template(
        'monthly_expenses/company_dashboard.html',
        title=f'Gastos Mensuales - {company.name}',
        company=company,
        fixed_expenses=fixed_expenses,
        monthly_expenses=monthly_expenses,
        summary=summary,
        current_year=current_year,
        current_month=current_month,
        current_month_name=current_month_name,
        chart_data=json.dumps(chart_data),
        format_currency=format_currency,
        format_percentage=format_percentage
    )


@monthly_expenses_bp.route('/fixed/<int:company_id>', methods=['GET', 'POST'])
@login_required
def manage_fixed_expenses(company_id):
    """
    Gestión de gastos fijos mensuales.
    
    Args:
        company_id: ID de la empresa
    """
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta empresa', 'danger')
        return redirect(url_for('monthly_expenses.dashboard'))
    
    # Obtener lista de gastos fijos
    fixed_expenses = FixedExpense.query.filter_by(
        company_id=company_id
    ).order_by(FixedExpense.name).all()
    
    # Obtener categorías disponibles para el select del formulario
    categories = ExpenseCategory.query.filter(
        (ExpenseCategory.company_id == company_id) | 
        (ExpenseCategory.company_id == None)
    ).order_by(ExpenseCategory.name).all()
    
    category_choices = [(c.id, c.name) for c in categories]
    
    # Crear formulario
    form = FixedExpenseForm()
    form.category_id.choices = category_choices
    form.company_id.data = company_id
    
    if form.validate_on_submit():
        try:
            # Crear nuevo gasto fijo
            fixed_expense = FixedExpense(
                name=form.name.data,
                description=form.description.data,
                amount=form.amount.data,
                category_id=form.category_id.data,
                is_active=form.is_active.data,
                company_id=company_id,
                created_by_id=current_user.id
            )
            
            db.session.add(fixed_expense)
            db.session.commit()
            
            flash('Gasto fijo creado correctamente', 'success')
            return redirect(url_for('monthly_expenses.manage_fixed_expenses', company_id=company_id))
            
        except Exception as e:
            db.session.rollback()
            logger.error(f"Error al crear gasto fijo: {str(e)}")
            flash(f'Error al crear gasto fijo: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/fixed_expenses.html',
        title=f'Gastos Fijos Mensuales - {company.name}',
        company=company,
        fixed_expenses=fixed_expenses,
        form=form,
        format_currency=format_currency
    )


@monthly_expenses_bp.route('/fixed/<int:expense_id>/edit', methods=['GET', 'POST'])
@login_required
def edit_fixed_expense(expense_id):
    """
    Editar un gasto fijo existente.
    
    Args:
        expense_id: ID del gasto fijo a editar
    """
    # Obtener gasto fijo y verificar permisos
    fixed_expense = FixedExpense.query.get_or_404(expense_id)
    company = fixed_expense.company
    
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a este gasto', 'danger')
        return redirect(url_for('monthly_expenses.dashboard'))
    
    # Obtener categorías disponibles para el select del formulario
    categories = ExpenseCategory.query.filter(
        (ExpenseCategory.company_id == company.id) | 
        (ExpenseCategory.company_id == None)
    ).order_by(ExpenseCategory.name).all()
    
    category_choices = [(c.id, c.name) for c in categories]
    
    # Crear formulario y poblarlo con datos existentes
    form = FixedExpenseForm(obj=fixed_expense)
    form.category_id.choices = category_choices
    
    if form.validate_on_submit():
        try:
            # Actualizar datos del gasto fijo
            form.populate_obj(fixed_expense)
            fixed_expense.updated_at = datetime.utcnow()
            
            db.session.commit()
            
            flash('Gasto fijo actualizado correctamente', 'success')
            return redirect(url_for('monthly_expenses.manage_fixed_expenses', company_id=company.id))
            
        except Exception as e:
            db.session.rollback()
            logger.error(f"Error al actualizar gasto fijo: {str(e)}")
            flash(f'Error al actualizar gasto fijo: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/fixed_expense_edit.html',
        title=f'Editar Gasto Fijo - {fixed_expense.name}',
        company=company,
        fixed_expense=fixed_expense,
        form=form
    )


@monthly_expenses_bp.route('/fixed/<int:expense_id>/delete', methods=['POST'])
@login_required
def delete_fixed_expense(expense_id):
    """
    Eliminar un gasto fijo.
    
    Args:
        expense_id: ID del gasto fijo a eliminar
    """
    # Obtener gasto fijo y verificar permisos
    fixed_expense = FixedExpense.query.get_or_404(expense_id)
    company = fixed_expense.company
    
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a este gasto', 'danger')
        return redirect(url_for('monthly_expenses.dashboard'))
    
    try:
        company_id = fixed_expense.company_id
        
        # Eliminar el gasto fijo
        db.session.delete(fixed_expense)
        db.session.commit()
        
        flash('Gasto fijo eliminado correctamente', 'success')
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al eliminar gasto fijo: {str(e)}")
        flash(f'Error al eliminar gasto fijo: {str(e)}', 'danger')
    
    return redirect(url_for('monthly_expenses.manage_fixed_expenses', company_id=company_id))


@monthly_expenses_bp.route('/monthly/<int:company_id>/new', methods=['GET', 'POST'])
@login_required
def new_monthly_expense(company_id):
    """
    Crear un nuevo gasto mensual.
    
    Args:
        company_id: ID de la empresa
    """
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta empresa', 'danger')
        return redirect(url_for('monthly_expenses.dashboard'))
    
    # Obtener categorías disponibles para el select del formulario
    categories = ExpenseCategory.query.filter(
        (ExpenseCategory.company_id == company_id) | 
        (ExpenseCategory.company_id == None)
    ).order_by(ExpenseCategory.name).all()
    
    category_choices = [(c.id, c.name) for c in categories]
    
    # Crear formulario
    form = MonthlyExpenseForm()
    form.category_id.choices = category_choices
    form.company_id.data = company_id
    
    # Establecer valores por defecto para año y mes
    if not form.year.data:
        form.year.data = datetime.now().year
    if not form.month.data:
        form.month.data = datetime.now().month
    
    if form.validate_on_submit():
        try:
            # Crear nuevo gasto mensual
            monthly_expense = MonthlyExpense(
                name=form.name.data,
                description=form.description.data,
                amount=form.amount.data,
                category_id=form.category_id.data,
                year=form.year.data,
                month=form.month.data,
                is_fixed=False,  # Siempre comienza como no fijo
                company_id=company_id,
                created_by_id=current_user.id
            )
            
            db.session.add(monthly_expense)
            
            # Si se marca como gasto fijo, crear también un gasto fijo
            if form.is_fixed.data:
                fixed_expense = FixedExpense(
                    name=form.name.data,
                    description=form.description.data,
                    amount=form.amount.data,
                    category_id=form.category_id.data,
                    is_active=True,
                    company_id=company_id,
                    created_by_id=current_user.id
                )
                db.session.add(fixed_expense)
                db.session.commit()
                
                # Actualizar la referencia en el gasto mensual
                monthly_expense.is_fixed = True
                monthly_expense.fixed_expense_id = fixed_expense.id
                db.session.commit()
            else:
                db.session.commit()
            
            # Actualizar el resumen mensual
            update_monthly_summary(company_id, form.year.data, form.month.data)
            
            flash('Gasto mensual creado correctamente', 'success')
            return redirect(url_for('monthly_expenses.company_dashboard', 
                                    company_id=company_id,
                                    year=form.year.data,
                                    month=form.month.data))
            
        except Exception as e:
            db.session.rollback()
            logger.error(f"Error al crear gasto mensual: {str(e)}")
            flash(f'Error al crear gasto mensual: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/monthly_expense_form.html',
        title=f'Nuevo Gasto Mensual - {company.name}',
        company=company,
        form=form,
        is_new=True
    )


@monthly_expenses_bp.route('/monthly/<int:expense_id>/edit', methods=['GET', 'POST'])
@login_required
def edit_monthly_expense(expense_id):
    """
    Editar un gasto mensual existente.
    
    Args:
        expense_id: ID del gasto mensual a editar
    """
    # Obtener gasto mensual y verificar permisos
    monthly_expense = MonthlyExpense.query.get_or_404(expense_id)
    company = monthly_expense.company
    
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a este gasto', 'danger')
        return redirect(url_for('monthly_expenses.dashboard'))
    
    # Obtener categorías disponibles para el select del formulario
    categories = ExpenseCategory.query.filter(
        (ExpenseCategory.company_id == company.id) | 
        (ExpenseCategory.company_id == None)
    ).order_by(ExpenseCategory.name).all()
    
    category_choices = [(c.id, c.name) for c in categories]
    
    # Crear formulario y poblarlo con datos existentes
    form = MonthlyExpenseForm(obj=monthly_expense)
    form.category_id.choices = category_choices
    
    # Si el gasto ya es fijo, no mostrar la opción de convertir en fijo
    if monthly_expense.is_fixed:
        del form.is_fixed
    
    if form.validate_on_submit():
        try:
            # Guardar valores anteriores para comprobar cambios
            old_year = monthly_expense.year
            old_month = monthly_expense.month
            
            # Actualizar datos del gasto mensual
            if not hasattr(form, 'is_fixed'):  # Si el campo no está en el formulario
                form.populate_obj(monthly_expense)
            else:
                # Actualizar todos los campos excepto is_fixed que requiere tratamiento especial
                monthly_expense.name = form.name.data
                monthly_expense.description = form.description.data
                monthly_expense.amount = form.amount.data
                monthly_expense.category_id = form.category_id.data
                monthly_expense.year = form.year.data
                monthly_expense.month = form.month.data
                
                # Convertir a gasto fijo si se solicita
                if form.is_fixed.data and not monthly_expense.is_fixed:
                    fixed_expense = FixedExpense(
                        name=form.name.data,
                        description=form.description.data,
                        amount=form.amount.data,
                        category_id=form.category_id.data,
                        is_active=True,
                        company_id=company.id,
                        created_by_id=current_user.id
                    )
                    db.session.add(fixed_expense)
                    db.session.commit()
                    
                    monthly_expense.is_fixed = True
                    monthly_expense.fixed_expense_id = fixed_expense.id
            
            monthly_expense.updated_at = datetime.utcnow()
            db.session.commit()
            
            # Actualizar los resúmenes mensuales si cambió el mes/año
            if old_year != monthly_expense.year or old_month != monthly_expense.month:
                update_monthly_summary(company.id, old_year, old_month)
            update_monthly_summary(company.id, monthly_expense.year, monthly_expense.month)
            
            flash('Gasto mensual actualizado correctamente', 'success')
            return redirect(url_for('monthly_expenses.company_dashboard', 
                                    company_id=company.id,
                                    year=monthly_expense.year,
                                    month=monthly_expense.month))
            
        except Exception as e:
            db.session.rollback()
            logger.error(f"Error al actualizar gasto mensual: {str(e)}")
            flash(f'Error al actualizar gasto mensual: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/monthly_expense_form.html',
        title=f'Editar Gasto Mensual - {monthly_expense.name}',
        company=company,
        form=form,
        expense=monthly_expense,
        is_new=False
    )


@monthly_expenses_bp.route('/monthly/<int:expense_id>/delete', methods=['POST'])
@login_required
def delete_monthly_expense(expense_id):
    """
    Eliminar un gasto mensual.
    
    Args:
        expense_id: ID del gasto mensual a eliminar
    """
    # Obtener gasto mensual y verificar permisos
    monthly_expense = MonthlyExpense.query.get_or_404(expense_id)
    company = monthly_expense.company
    
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a este gasto', 'danger')
        return redirect(url_for('monthly_expenses.dashboard'))
    
    try:
        company_id = monthly_expense.company_id
        year = monthly_expense.year
        month = monthly_expense.month
        
        # Eliminar el gasto mensual
        db.session.delete(monthly_expense)
        db.session.commit()
        
        # Actualizar el resumen mensual
        update_monthly_summary(company_id, year, month)
        
        flash('Gasto mensual eliminado correctamente', 'success')
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al eliminar gasto mensual: {str(e)}")
        flash(f'Error al eliminar gasto mensual: {str(e)}', 'danger')
    
    return redirect(url_for('monthly_expenses.company_dashboard', 
                           company_id=company_id,
                           year=year,
                           month=month))


@monthly_expenses_bp.route('/category/<int:company_id>', methods=['GET', 'POST'])
@login_required
def manage_categories(company_id):
    """
    Gestión de categorías de gastos.
    
    Args:
        company_id: ID de la empresa
    """
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta empresa', 'danger')
        return redirect(url_for('monthly_expenses.dashboard'))
    
    # Obtener categorías del sistema (globales)
    system_categories = ExpenseCategory.query.filter_by(is_system=True).order_by(ExpenseCategory.name).all()
    
    # Obtener categorías personalizadas de la empresa
    custom_categories = ExpenseCategory.query.filter_by(
        company_id=company_id,
        is_system=False
    ).order_by(ExpenseCategory.name).all()
    
    # Crear formulario
    form = ExpenseCategoryForm()
    form.company_id.data = company_id
    
    if form.validate_on_submit():
        try:
            # Crear nueva categoría personalizada
            category = ExpenseCategory(
                name=form.name.data,
                description=form.description.data,
                is_system=False,
                company_id=company_id
            )
            
            db.session.add(category)
            db.session.commit()
            
            flash('Categoría creada correctamente', 'success')
            return redirect(url_for('monthly_expenses.manage_categories', company_id=company_id))
            
        except Exception as e:
            db.session.rollback()
            logger.error(f"Error al crear categoría: {str(e)}")
            flash(f'Error al crear categoría: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/categories.html',
        title=f'Categorías de Gastos - {company.name}',
        company=company,
        system_categories=system_categories,
        custom_categories=custom_categories,
        form=form
    )


@monthly_expenses_bp.route('/category/<int:category_id>/edit', methods=['GET', 'POST'])
@login_required
def edit_category(category_id):
    """
    Editar una categoría de gastos personalizada.
    
    Args:
        category_id: ID de la categoría a editar
    """
    # Obtener categoría y verificar permisos
    category = ExpenseCategory.query.get_or_404(category_id)
    
    # No permitir editar categorías del sistema
    if category.is_system:
        flash('No se pueden editar las categorías del sistema', 'danger')
        return redirect(url_for('monthly_expenses.manage_categories', company_id=category.company_id))
    
    company = category.company
    
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta categoría', 'danger')
        return redirect(url_for('monthly_expenses.dashboard'))
    
    # Crear formulario y poblarlo con datos existentes
    form = ExpenseCategoryForm(obj=category)
    
    if form.validate_on_submit():
        try:
            # Actualizar datos de la categoría
            category.name = form.name.data
            category.description = form.description.data
            category.updated_at = datetime.utcnow()
            
            db.session.commit()
            
            flash('Categoría actualizada correctamente', 'success')
            return redirect(url_for('monthly_expenses.manage_categories', company_id=company.id))
            
        except Exception as e:
            db.session.rollback()
            logger.error(f"Error al actualizar categoría: {str(e)}")
            flash(f'Error al actualizar categoría: {str(e)}', 'danger')
    
    return render_template(
        'monthly_expenses/category_edit.html',
        title=f'Editar Categoría - {category.name}',
        company=company,
        category=category,
        form=form
    )


@monthly_expenses_bp.route('/category/<int:category_id>/delete', methods=['POST'])
@login_required
def delete_category(category_id):
    """
    Eliminar una categoría de gastos personalizada.
    
    Args:
        category_id: ID de la categoría a eliminar
    """
    # Obtener categoría y verificar permisos
    category = ExpenseCategory.query.get_or_404(category_id)
    
    # No permitir eliminar categorías del sistema
    if category.is_system:
        flash('No se pueden eliminar las categorías del sistema', 'danger')
        return redirect(url_for('monthly_expenses.manage_categories', company_id=category.company_id))
    
    company = category.company
    
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta categoría', 'danger')
        return redirect(url_for('monthly_expenses.dashboard'))
    
    try:
        company_id = category.company_id
        
        # Verificar si hay gastos asociados a esta categoría
        fixed_count = FixedExpense.query.filter_by(category_id=category_id).count()
        monthly_count = MonthlyExpense.query.filter_by(category_id=category_id).count()
        
        if fixed_count > 0 or monthly_count > 0:
            flash(f'No se puede eliminar la categoría porque tiene {fixed_count + monthly_count} gastos asociados', 'danger')
            return redirect(url_for('monthly_expenses.manage_categories', company_id=company_id))
        
        # Eliminar la categoría
        db.session.delete(category)
        db.session.commit()
        
        flash('Categoría eliminada correctamente', 'success')
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al eliminar categoría: {str(e)}")
        flash(f'Error al eliminar categoría: {str(e)}', 'danger')
    
    return redirect(url_for('monthly_expenses.manage_categories', company_id=company_id))


@monthly_expenses_bp.route('/generate-monthly/<int:company_id>', methods=['POST'])
@login_required
def generate_monthly_from_fixed(company_id):
    """
    Genera gastos mensuales a partir de los gastos fijos activos.
    
    Args:
        company_id: ID de la empresa
    """
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta empresa', 'danger')
        return redirect(url_for('monthly_expenses.dashboard'))
    
    # Obtener año y mes de los parámetros del formulario
    year = request.form.get('year', type=int)
    month = request.form.get('month', type=int)
    
    if not year or not month or month < 1 or month > 12:
        flash('Año o mes no válidos', 'danger')
        return redirect(url_for('monthly_expenses.company_dashboard', company_id=company_id))
    
    try:
        # Obtener gastos fijos activos
        fixed_expenses = FixedExpense.query.filter_by(
            company_id=company_id,
            is_active=True
        ).all()
        
        if not fixed_expenses:
            flash('No hay gastos fijos activos para generar', 'warning')
            return redirect(url_for('monthly_expenses.company_dashboard', 
                                   company_id=company_id,
                                   year=year,
                                   month=month))
        
        # Para cada gasto fijo, crear un gasto mensual si no existe ya
        count = 0
        for fixed in fixed_expenses:
            # Verificar si ya existe
            existing = MonthlyExpense.query.filter_by(
                company_id=company_id,
                fixed_expense_id=fixed.id,
                year=year,
                month=month
            ).first()
            
            if not existing:
                # Crear gasto mensual
                monthly = MonthlyExpense(
                    name=fixed.name,
                    description=fixed.description,
                    amount=fixed.amount,
                    year=year,
                    month=month,
                    is_fixed=True,
                    company_id=company_id,
                    fixed_expense_id=fixed.id,
                    category_id=fixed.category_id,
                    created_by_id=current_user.id
                )
                db.session.add(monthly)
                count += 1
        
        if count > 0:
            db.session.commit()
            # Actualizar el resumen mensual
            update_monthly_summary(company_id, year, month)
            flash(f'Se han generado {count} gastos mensuales a partir de gastos fijos', 'success')
        else:
            flash('No se generaron nuevos gastos mensuales, todos ya existen', 'info')
            
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al generar gastos mensuales: {str(e)}")
        flash(f'Error al generar gastos mensuales: {str(e)}', 'danger')
    
    return redirect(url_for('monthly_expenses.company_dashboard', 
                           company_id=company_id,
                           year=year,
                           month=month))


@monthly_expenses_bp.route('/report/<int:company_id>')
@login_required
def expenses_report(company_id):
    """
    Informe detallado de gastos mensuales.
    
    Args:
        company_id: ID de la empresa
    """
    # Verificar acceso a la empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene acceso a esta empresa', 'danger')
        return redirect(url_for('monthly_expenses.dashboard'))
    
    # Obtener año de los parámetros URL o usar el actual
    current_year = request.args.get('year', type=int, default=datetime.now().year)
    
    # Obtener resúmenes mensuales para el año seleccionado
    summaries = MonthlyExpenseSummary.query.filter_by(
        company_id=company_id,
        year=current_year
    ).order_by(MonthlyExpenseSummary.month).all()
    
    # Preparar datos para gráfico de gastos mensuales
    months = []
    fixed_expenses = []
    custom_expenses = []
    total_expenses = []
    
    for month in range(1, 13):
        summary = next((s for s in summaries if s.month == month), None)
        months.append(month)
        if summary:
            fixed_expenses.append(float(summary.fixed_expenses_total))
            custom_expenses.append(float(summary.custom_expenses_total))
            total_expenses.append(float(summary.total_amount))
        else:
            fixed_expenses.append(0)
            custom_expenses.append(0)
            total_expenses.append(0)
    
    # Convertir a formato para Chart.js
    chart_data = {
        'labels': ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"],
        'datasets': [
            {
                'label': 'Gastos Fijos',
                'data': fixed_expenses,
                'backgroundColor': 'rgba(54, 162, 235, 0.5)',
                'borderColor': 'rgba(54, 162, 235, 1)',
                'borderWidth': 1
            },
            {
                'label': 'Gastos Personalizados',
                'data': custom_expenses,
                'backgroundColor': 'rgba(255, 99, 132, 0.5)',
                'borderColor': 'rgba(255, 99, 132, 1)',
                'borderWidth': 1
            },
            {
                'label': 'Total Gastos',
                'data': total_expenses,
                'backgroundColor': 'rgba(75, 192, 192, 0.5)',
                'borderColor': 'rgba(75, 192, 192, 1)',
                'borderWidth': 1,
                'type': 'line'
            }
        ]
    }
    
    # Obtener totales por categoría para el año seleccionado
    category_totals = db.session.query(
        ExpenseCategory.name,
        db.func.sum(MonthlyExpense.amount).label('total')
    ).join(
        MonthlyExpense, ExpenseCategory.id == MonthlyExpense.category_id
    ).filter(
        MonthlyExpense.company_id == company_id,
        MonthlyExpense.year == current_year
    ).group_by(
        ExpenseCategory.name
    ).order_by(
        db.func.sum(MonthlyExpense.amount).desc()
    ).all()
    
    # Convertir a formato para Chart.js
    category_chart_data = {
        'labels': [item[0] for item in category_totals],
        'data': [float(item[1]) for item in category_totals]
    }
    
    # Calcular total anual
    total_year = sum(float(item[1]) for item in category_totals)
    
    return render_template(
        'monthly_expenses/report.html',
        title=f'Informe de Gastos - {company.name}',
        company=company,
        summaries=summaries,
        current_year=current_year,
        chart_data=json.dumps(chart_data),
        category_chart_data=json.dumps(category_chart_data),
        category_totals=category_totals,
        total_year=total_year,
        format_currency=format_currency
    )


def update_monthly_summary(company_id, year, month):
    """
    Actualiza el resumen mensual para un mes y año específicos.
    
    Args:
        company_id: ID de la empresa
        year: Año
        month: Mes (1-12)
    """
    try:
        # Calcular totales
        expenses = MonthlyExpense.query.filter_by(
            company_id=company_id,
            year=year,
            month=month
        ).all()
        
        fixed_total = sum(expense.amount for expense in expenses if expense.is_fixed)
        custom_total = sum(expense.amount for expense in expenses if not expense.is_fixed)
        total_expenses = fixed_total + custom_total
        
        # Obtener o crear el resumen
        summary = MonthlyExpenseSummary.query.filter_by(
            company_id=company_id,
            year=year,
            month=month
        ).first()
        
        if not summary:
            summary = MonthlyExpenseSummary(
                company_id=company_id,
                year=year,
                month=month,
                total_amount=total_expenses,
                fixed_expenses_total=fixed_total,
                custom_expenses_total=custom_total
            )
            db.session.add(summary)
        else:
            summary.total_amount = total_expenses
            summary.fixed_expenses_total = fixed_total
            summary.custom_expenses_total = custom_total
            summary.updated_at = datetime.utcnow()
        
        db.session.commit()
        return True
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al actualizar resumen mensual: {str(e)}")
        return False
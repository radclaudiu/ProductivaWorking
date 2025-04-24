"""
Utilidades para el módulo de Arqueos de Caja.

Este módulo proporciona funciones para cálculos, estadísticas,
y operaciones comunes del sistema de arqueos de caja.
"""

import logging
from datetime import datetime, date, timedelta
from decimal import Decimal
import calendar
import locale
try:
    locale.setlocale(locale.LC_ALL, 'es_ES.UTF-8')
except:
    locale.setlocale(locale.LC_ALL, '')

from sqlalchemy.exc import SQLAlchemyError
from app import db
from models_cash_register import CashRegister, CashRegisterSummary
from models_work_hours import CompanyWorkHours

# Configurar logging
logger = logging.getLogger(__name__)

# Constantes
DEFAULT_HOURLY_EMPLOYEE_COST = 12.0  # Coste por hora por defecto si no está definido


def get_week_number(date_obj):
    """
    Obtiene el número de semana de una fecha.
    
    Args:
        date_obj: Objeto de fecha
        
    Returns:
        Entero con el número de semana (1-53)
    """
    return date_obj.isocalendar()[1]


def get_date_range(year, month=None, week=None):
    """
    Obtiene rango de fechas para un año, mes o semana específicos.
    
    Args:
        year: Año
        month: Mes (opcional)
        week: Número de semana (opcional)
        
    Returns:
        Tupla con fecha inicial y final del período
    """
    if week:
        # Calcular la fecha inicial de la semana
        first_day = datetime.strptime(f"{year}-{week}-1", "%Y-%W-%w").date()
        last_day = first_day + timedelta(days=6)
        return first_day, last_day
    
    if month:
        # Calcular el primer y último día del mes
        first_day = date(year, month, 1)
        last_day = date(year, month, calendar.monthrange(year, month)[1])
        return first_day, last_day
    
    # Retornar rango del año completo
    first_day = date(year, 1, 1)
    last_day = date(year, 12, 31)
    return first_day, last_day


def calculate_weekly_summary(company_id, year, week_number):
    """
    Calcula y actualiza el resumen semanal para una empresa.
    
    Args:
        company_id: ID de la empresa
        year: Año
        week_number: Número de semana
        
    Returns:
        CashRegisterSummary actualizado o None si hay error
    """
    try:
        # Obtener el mes al que pertenece la semana (usamos el primer día)
        start_date, end_date = get_date_range(year, week=week_number)
        month = start_date.month
        
        # Buscar registros de arqueo para esta semana y empresa
        registers = CashRegister.query.filter(
            CashRegister.company_id == company_id,
            CashRegister.date >= start_date,
            CashRegister.date <= end_date
        ).all()
        
        if not registers:
            logger.info(f"No hay registros para la semana {week_number} de {year} - empresa {company_id}")
            return None
        
        # Buscar o crear resumen para esta semana
        summary = CashRegisterSummary.query.filter_by(
            company_id=company_id,
            year=year,
            month=month,
            week_number=week_number
        ).first()
        
        if not summary:
            summary = CashRegisterSummary(
                company_id=company_id,
                year=year,
                month=month,
                week_number=week_number
            )
            db.session.add(summary)
        
        # Reiniciar contadores semanales
        summary.weekly_total = 0.0
        summary.weekly_cash = 0.0
        summary.weekly_card = 0.0
        summary.weekly_delivery_cash = 0.0
        summary.weekly_delivery_online = 0.0
        summary.weekly_check = 0.0
        summary.weekly_expenses = 0.0
        
        # Calcular totales semanales
        for register in registers:
            # Sumar totales
            summary.weekly_total += register.total_amount
            summary.weekly_cash += register.cash_amount
            summary.weekly_card += register.card_amount
            summary.weekly_delivery_cash += register.delivery_cash_amount
            summary.weekly_delivery_online += register.delivery_online_amount
            summary.weekly_check += register.check_amount
            summary.weekly_expenses += register.expenses_amount
        
        # Obtener datos de horas trabajadas de company_work_hours
        staff_cost = calculate_staff_cost(company_id, year, month, week_number)
        if staff_cost:
            summary.weekly_staff_cost = staff_cost['weekly_cost']
            summary.monthly_staff_cost = staff_cost['monthly_cost']
            
            # Calcular porcentaje de coste de personal
            if summary.weekly_total > 0:
                summary.weekly_staff_cost_percentage = (staff_cost['weekly_cost'] / summary.weekly_total) * 100
            else:
                summary.weekly_staff_cost_percentage = 0
                
            # Actualizar porcentaje mensual
            monthly_revenue = calculate_monthly_revenue(company_id, year, month)
            if monthly_revenue > 0:
                summary.monthly_staff_cost_percentage = (staff_cost['monthly_cost'] / monthly_revenue) * 100
            else:
                summary.monthly_staff_cost_percentage = 0
        
        # Actualizar totales acumulados
        summary.monthly_total = calculate_monthly_revenue(company_id, year, month)
        summary.yearly_total = calculate_yearly_revenue(company_id, year)
        
        # Guardar cambios
        db.session.commit()
        
        return summary
    
    except SQLAlchemyError as e:
        db.session.rollback()
        logger.error(f"Error al calcular resumen semanal: {str(e)}")
        return None


def calculate_staff_cost(company_id, year, month=None, week=None):
    """
    Calcula el coste de personal para una empresa en un período.
    
    Args:
        company_id: ID de la empresa
        year: Año
        month: Mes (opcional)
        week: Número de semana (opcional)
        
    Returns:
        Diccionario con costes semanales y mensuales
    """
    from models import Company
    
    try:
        # Obtener coste por hora de la empresa
        company = Company.query.get(company_id)
        if not company:
            return None
            
        hourly_cost = company.hourly_employee_cost or DEFAULT_HOURLY_EMPLOYEE_COST
        
        # Obtener rango de fechas
        if week:
            start_date, end_date = get_date_range(year, week=week)
            month = start_date.month
        else:
            # Si no se especifica semana, usar el mes completo
            if not month:
                month = 1  # Valor por defecto
            start_date, end_date = get_date_range(year, month)
        
        # Obtener horas trabajadas semanales (si se especificó una semana)
        weekly_hours = 0
        if week:
            # Buscar en company_work_hours para esta semana
            week_record = CompanyWorkHours.query.filter_by(
                company_id=company_id,
                year=year,
                week_number=week
            ).first()
            
            if week_record:
                weekly_hours = week_record.weekly_hours
        
        # Obtener horas trabajadas mensuales
        monthly_hours = 0
        # Sumar todas las horas de las semanas del mes
        month_records = CompanyWorkHours.query.filter(
            CompanyWorkHours.company_id == company_id,
            CompanyWorkHours.year == year,
            CompanyWorkHours.month == month
        ).all()
        
        for record in month_records:
            monthly_hours += record.monthly_hours
        
        # Calcular costes
        weekly_cost = weekly_hours * hourly_cost
        monthly_cost = monthly_hours * hourly_cost
        
        return {
            'weekly_hours': weekly_hours,
            'monthly_hours': monthly_hours,
            'weekly_cost': weekly_cost,
            'monthly_cost': monthly_cost,
            'hourly_cost': hourly_cost
        }
    
    except Exception as e:
        logger.error(f"Error al calcular coste de personal: {str(e)}")
        return {
            'weekly_hours': 0,
            'monthly_hours': 0,
            'weekly_cost': 0,
            'monthly_cost': 0,
            'hourly_cost': DEFAULT_HOURLY_EMPLOYEE_COST
        }


def calculate_monthly_revenue(company_id, year, month):
    """
    Calcula los ingresos totales de un mes para una empresa.
    
    Args:
        company_id: ID de la empresa
        year: Año
        month: Mes
        
    Returns:
        Float con el total de ingresos del mes
    """
    try:
        # Obtener rango de fechas del mes
        start_date, end_date = get_date_range(year, month)
        
        # Sumar todos los arqueos del mes
        result = db.session.query(db.func.sum(CashRegister.total_amount)).filter(
            CashRegister.company_id == company_id,
            CashRegister.date >= start_date,
            CashRegister.date <= end_date
        ).scalar()
        
        return result or 0.0
    
    except Exception as e:
        logger.error(f"Error al calcular ingresos mensuales: {str(e)}")
        return 0.0


def calculate_yearly_revenue(company_id, year):
    """
    Calcula los ingresos totales de un año para una empresa.
    
    Args:
        company_id: ID de la empresa
        year: Año
        
    Returns:
        Float con el total de ingresos del año
    """
    try:
        # Obtener rango de fechas del año
        start_date, end_date = get_date_range(year)
        
        # Sumar todos los arqueos del año
        result = db.session.query(db.func.sum(CashRegister.total_amount)).filter(
            CashRegister.company_id == company_id,
            CashRegister.date >= start_date,
            CashRegister.date <= end_date
        ).scalar()
        
        return result or 0.0
    
    except Exception as e:
        logger.error(f"Error al calcular ingresos anuales: {str(e)}")
        return 0.0


def format_currency(value):
    """
    Formatea un valor como moneda en euros.
    
    Args:
        value: Valor numérico a formatear
        
    Returns:
        String con formato de moneda (ej: "1.234,56 €")
    """
    if value is None:
        return "0,00 €"
    
    try:
        # Convertir a Decimal para precisión
        if not isinstance(value, Decimal):
            value = Decimal(str(value))
        
        # Formatear con separador de miles y decimales en formato español
        return f"{value:,.2f} €".replace(",", "X").replace(".", ",").replace("X", ".")
    
    except:
        return f"{float(value):.2f} €".replace(".", ",")


def format_percentage(value):
    """
    Formatea un valor como porcentaje.
    
    Args:
        value: Valor numérico a formatear
        
    Returns:
        String con formato de porcentaje (ej: "12,34%")
    """
    if value is None:
        return "0,00%"
    
    try:
        return f"{float(value):.2f}%".replace(".", ",")
    except:
        return "0,00%"


def get_current_week_number():
    """
    Obtiene el número de semana actual.
    
    Returns:
        Entero con el número de semana actual
    """
    return datetime.now().isocalendar()[1]


def get_week_dates(year, week_number):
    """
    Obtiene las fechas de inicio y fin de una semana.
    
    Args:
        year: Año
        week_number: Número de semana
        
    Returns:
        Tupla con fecha inicial y final de la semana en formato DD/MM/YYYY
    """
    start_date, end_date = get_date_range(year, week=week_number)
    return start_date.strftime("%d/%m/%Y"), end_date.strftime("%d/%m/%Y")


def generate_token_url(token, base_url):
    """
    Genera la URL para acceso mediante token.
    
    Args:
        token: Token de acceso
        base_url: URL base de la aplicación
        
    Returns:
        URL completa para acceso con token
    """
    return f"{base_url}/cash-register/token/{token}"
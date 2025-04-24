"""
Utilidades para el sistema de Arqueos de Caja.

Este módulo proporciona funciones para:
- Actualizar resúmenes de arqueos (semanal, mensual, anual)
- Calcular costes de personal basados en horas trabajadas
- Generar tokens para acceso de empleados sin registro
"""

from datetime import datetime, timedelta
import logging
import secrets
import string
from sqlalchemy.exc import SQLAlchemyError
from app import db
from models_cash_register import CashRegister, CashRegisterSummary, CashRegisterToken
from models_work_hours import CompanyWorkHours
from models import Company

# Configurar logger
logger = logging.getLogger(__name__)

def get_iso_week_data(date_obj):
    """
    Obtiene el año, semana y día de la semana según el estándar ISO.
    
    Args:
        date_obj (date): Objeto date o datetime
        
    Returns:
        tuple: (año_ISO, semana_ISO, día_semana_ISO)
    """
    iso_calendar = date_obj.isocalendar()
    return iso_calendar

def update_register_summary(company_id, register_date):
    """
    Actualiza los resúmenes de arqueos para una empresa y fecha específica.
    
    Args:
        company_id (int): ID de la empresa
        register_date (date): Fecha del arqueo que dispara la actualización
        
    Returns:
        bool: True si la actualización fue exitosa, False en caso contrario
    """
    if not company_id or not register_date:
        logger.error("Empresa o fecha inválida para actualizar resumen")
        return False
        
    try:
        # Obtener los datos del periodo ISO
        iso_year, iso_week, _ = get_iso_week_data(register_date)
        month = register_date.month
        
        # Buscar o crear el resumen para este periodo
        summary = CashRegisterSummary.query.filter_by(
            company_id=company_id,
            year=iso_year,
            month=month,
            week_number=iso_week
        ).first()
        
        if not summary:
            summary = CashRegisterSummary(
                company_id=company_id,
                year=iso_year,
                month=month,
                week_number=iso_week
            )
            db.session.add(summary)
        
        # Calcular totales semanales
        # Primero día de la semana
        from datetime import datetime, timedelta
        # Calculamos el primer día de la semana (lunes)
        day_of_week = register_date.isocalendar()[2]  # 1=lunes, 7=domingo
        monday = register_date - timedelta(days=day_of_week-1)
        # Último día de la semana (domingo)
        sunday = monday + timedelta(days=6)
        
        # Obtener todos los arqueos de esa semana
        weekly_registers = CashRegister.query.filter(
            CashRegister.company_id == company_id,
            CashRegister.date >= monday,
            CashRegister.date <= sunday
        ).all()
        
        # Reiniciar los totales semanales
        summary.weekly_total = 0
        summary.weekly_cash = 0
        summary.weekly_card = 0
        summary.weekly_delivery_cash = 0
        summary.weekly_delivery_online = 0
        summary.weekly_check = 0
        summary.weekly_expenses = 0
        
        # Sumar todos los arqueos de la semana
        for register in weekly_registers:
            summary.weekly_total += register.total_amount
            summary.weekly_cash += register.cash_amount
            summary.weekly_card += register.card_amount
            summary.weekly_delivery_cash += register.delivery_cash_amount
            summary.weekly_delivery_online += register.delivery_online_amount
            summary.weekly_check += register.check_amount
            summary.weekly_expenses += register.expenses_amount
        
        # Calcular totales mensuales
        # Obtener todos los arqueos del mes
        from datetime import date
        start_of_month = date(iso_year, month, 1)
        if month < 12:
            end_of_month = date(iso_year, month + 1, 1) - timedelta(days=1)
        else:
            end_of_month = date(iso_year + 1, 1, 1) - timedelta(days=1)
        
        monthly_registers = CashRegister.query.filter(
            CashRegister.company_id == company_id,
            CashRegister.date >= start_of_month,
            CashRegister.date <= end_of_month
        ).all()
        
        # Calcular el total mensual
        summary.monthly_total = sum(r.total_amount for r in monthly_registers)
        
        # Actualizar el total anual
        yearly_total = CashRegister.query.with_entities(
            db.func.sum(CashRegister.total_amount)
        ).filter(
            CashRegister.company_id == company_id,
            db.extract('year', CashRegister.date) == iso_year
        ).scalar() or 0
        
        summary.yearly_total = yearly_total
        
        # Calcular costes de personal
        update_staff_costs_for_summary(summary)
        
        db.session.commit()
        logger.info(f"Resumen actualizado para empresa ID {company_id}, semana {iso_week}/{iso_year}")
        return True
        
    except SQLAlchemyError as e:
        db.session.rollback()
        logger.error(f"Error SQL actualizando resumen: {str(e)}")
        return False
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error actualizando resumen: {str(e)}")
        return False

def update_staff_costs_for_summary(summary):
    """
    Actualiza los costes de personal para un resumen de arqueos.
    
    Args:
        summary (CashRegisterSummary): Resumen a actualizar
    """
    try:
        # Obtener la configuración de coste por hora de la empresa
        company = Company.query.get(summary.company_id)
        if not company or not company.hourly_employee_cost:
            logger.warning(f"Empresa {summary.company_id} sin coste por hora configurado")
            return
        
        hourly_cost = company.hourly_employee_cost
        
        # Obtener horas trabajadas de la semana
        weekly_hours = CompanyWorkHours.query.filter_by(
            company_id=summary.company_id,
            year=summary.year,
            week_number=summary.week_number
        ).first()
        
        if weekly_hours:
            # Calcular coste semanal
            summary.weekly_staff_cost = weekly_hours.weekly_hours * hourly_cost
            
            # Calcular porcentaje si hay facturación semanal
            if summary.weekly_total > 0:
                summary.weekly_staff_cost_percentage = (summary.weekly_staff_cost / summary.weekly_total) * 100
            else:
                summary.weekly_staff_cost_percentage = 0
        
        # Obtener horas trabajadas del mes
        monthly_hours = CompanyWorkHours.query.filter_by(
            company_id=summary.company_id,
            year=summary.year,
            month=summary.month
        ).all()
        
        # Sumar todas las horas mensuales (puede haber múltiples registros para el mismo mes)
        total_monthly_hours = sum(h.monthly_hours for h in monthly_hours)
        
        # Calcular coste mensual
        summary.monthly_staff_cost = total_monthly_hours * hourly_cost
        
        # Calcular porcentaje si hay facturación mensual
        if summary.monthly_total > 0:
            summary.monthly_staff_cost_percentage = (summary.monthly_staff_cost / summary.monthly_total) * 100
        else:
            summary.monthly_staff_cost_percentage = 0
            
    except Exception as e:
        logger.error(f"Error calculando costes de personal: {str(e)}")

def generate_token(company_id, employee_id=None, expiry_days=1):
    """
    Genera un token único para permitir a un empleado registrar arqueos.
    
    Args:
        company_id (int): ID de la empresa
        employee_id (int, optional): ID del empleado asignado al token
        expiry_days (int, optional): Días de validez del token (por defecto 1)
        
    Returns:
        CashRegisterToken: El token generado, o None si hubo error
    """
    try:
        # Generamos un token aleatorio seguro
        token_chars = string.ascii_letters + string.digits
        token_value = ''.join(secrets.choice(token_chars) for _ in range(32))
        
        # Fecha de expiración
        expires_at = datetime.utcnow() + timedelta(days=expiry_days)
        
        # Crear el objeto token
        token = CashRegisterToken(
            token=token_value,
            company_id=company_id,
            employee_id=employee_id,
            is_active=True,
            expires_at=expires_at,
            created_by_id=None  # Se debe asignar en la función que llama
        )
        
        db.session.add(token)
        db.session.commit()
        
        return token
        
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error generando token: {str(e)}")
        return None

def validate_token(token_value):
    """
    Valida un token para registro de arqueos.
    
    Args:
        token_value (str): Valor del token a validar
        
    Returns:
        CashRegisterToken: El token si es válido, None en caso contrario
    """
    if not token_value:
        return None
        
    token = CashRegisterToken.query.filter_by(token=token_value).first()
    
    if not token:
        logger.warning(f"Token no encontrado: {token_value[:8]}...")
        return None
        
    if not token.is_valid:
        logger.warning(f"Token no válido: {token_value[:8]}...")
        return None
        
    return token

def mark_token_used(token, cash_register_id):
    """
    Marca un token como utilizado.
    
    Args:
        token (CashRegisterToken): Token a marcar
        cash_register_id (int): ID del arqueo creado con este token
        
    Returns:
        bool: True si se actualizó correctamente, False en caso contrario
    """
    try:
        token.is_active = False
        token.used_at = datetime.utcnow()
        token.cash_register_id = cash_register_id
        
        db.session.add(token)
        db.session.commit()
        
        return True
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error marcando token como usado: {str(e)}")
        return False
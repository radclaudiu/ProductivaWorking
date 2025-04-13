"""
Utilidades para el cálculo de horas trabajadas en los fichajes.
Este módulo implementa funciones para calcular dinámicamente las horas
trabajadas por empleado en diferentes períodos (diario, semanal, mensual).
"""

import logging
from datetime import datetime, timedelta
from sqlalchemy import func
from models_checkpoints import CheckPointRecord, CheckPoint
from app import db
from timezone_config import get_current_time, get_datetime_range_for_week

# Configurar logger
logger = logging.getLogger(__name__)

def calculate_weekly_hours(employee_id, year=None, week_number=None):
    """
    Calcula las horas trabajadas por un empleado en una semana específica.
    Si no se proporciona año y número de semana, se usa la semana actual.
    
    Args:
        employee_id: ID del empleado
        year: Año para el cálculo (opcional, por defecto el año actual)
        week_number: Número de semana (1-53) (opcional, por defecto la semana actual)
        
    Returns:
        Dict con información de horas trabajadas en la semana
    """
    try:
        # Si no se proporciona año y semana, usar la actual
        if year is None or week_number is None:
            now = get_current_time()
            year = now.year
            week_number = now.isocalendar()[1]  # ISO week number
        
        # Obtener rango de fechas para la semana especificada
        start_date, end_date = get_datetime_range_for_week(year, week_number)
        
        logger.debug(f"Calculando horas para empleado {employee_id}, semana {week_number} de {year}")
        logger.debug(f"Rango de fechas: {start_date} a {end_date}")
        
        # Obtener todos los registros completos (con check-out) para esa semana
        records = CheckPointRecord.query.join(
            CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id
        ).filter(
            CheckPointRecord.employee_id == employee_id,
            CheckPointRecord.check_in_time >= start_date,
            CheckPointRecord.check_in_time < end_date,
            CheckPointRecord.check_out_time.isnot(None)  # Solo registros completos
        ).all()
        
        # Calcular horas totales y por día
        total_hours = 0
        days_worked = set()
        daily_hours = {}
        
        for record in records:
            # Calcular duración en horas
            duration = (record.check_out_time - record.check_in_time).total_seconds() / 3600
            
            # Añadir al total
            total_hours += duration
            
            # Añadir al contador por día
            day_key = record.check_in_time.strftime('%Y-%m-%d')
            days_worked.add(day_key)
            
            if day_key in daily_hours:
                daily_hours[day_key] += duration
            else:
                daily_hours[day_key] = duration
        
        # Preparar resultado
        result = {
            'employee_id': employee_id,
            'year': year,
            'week': week_number,
            'total_hours': round(total_hours, 2),
            'days_worked': len(days_worked),
            'daily_hours': {k: round(v, 2) for k, v in daily_hours.items()},
            'start_date': start_date,
            'end_date': end_date
        }
        
        return result
    
    except Exception as e:
        logger.error(f"Error al calcular horas semanales: {e}")
        # Inicializar valores por defecto para el caso de error
        start_date = end_date = None
        
        # Intentar obtener el rango de fechas de la semana de todos modos
        try:
            if year is not None and week_number is not None:
                start_date, end_date = get_datetime_range_for_week(year, week_number)
        except:
            pass
            
        return {
            'employee_id': employee_id,
            'year': year,
            'week': week_number,
            'total_hours': 0,
            'days_worked': 0,
            'daily_hours': {},
            'start_date': start_date,
            'end_date': end_date,
            'error': str(e)
        }


def get_weekly_hours_summary(employee_id, num_weeks=4):
    """
    Obtiene un resumen de las horas trabajadas en las últimas semanas.
    
    Args:
        employee_id: ID del empleado
        num_weeks: Número de semanas a incluir en el resumen
        
    Returns:
        List de diccionarios con el resumen de horas por semana
    """
    try:
        # Obtener fecha actual
        now = get_current_time()
        current_year = now.year
        current_week = now.isocalendar()[1]
        
        # Inicializar resultado
        results = []
        
        # Calcular horas para cada semana
        for i in range(num_weeks):
            # Calcular año y semana
            target_week = current_week - i
            target_year = current_year
            
            # Ajustar si estamos en semanas del año anterior
            if target_week <= 0:
                target_year -= 1
                # Obtener número de semanas en el año anterior
                last_day_prev_year = datetime(target_year, 12, 31)
                target_week = last_day_prev_year.isocalendar()[1]
                # Si es la semana 53 pero el año solo tiene 52, ajustar
                if target_week > 52 and i > 0:
                    target_week = 52
            
            # Calcular horas para esta semana
            weekly_hours = calculate_weekly_hours(employee_id, target_year, target_week)
            results.append(weekly_hours)
        
        return results
    
    except Exception as e:
        logger.error(f"Error al obtener resumen de horas semanales: {e}")
        return []
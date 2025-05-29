"""
Utilidades para el cálculo y gestión de horas trabajadas.

Este módulo proporciona funciones para:
- Calcular horas trabajadas entre dos timestamps
- Actualizar acumulados de horas trabajadas por empleado
- Actualizar acumulados de horas trabajadas por empresa
"""

from datetime import datetime, timedelta, date
import logging
from sqlalchemy.exc import SQLAlchemyError
from app import db
from models_work_hours import EmployeeWorkHours, CompanyWorkHours
from models import Employee

# Configurar logger
logger = logging.getLogger(__name__)

def calculate_hours_worked(check_in_time, check_out_time):
    """
    Calcula las horas trabajadas entre dos timestamps.
    
    Args:
        check_in_time (datetime): Hora de entrada
        check_out_time (datetime): Hora de salida
        
    Returns:
        float: Número de horas trabajadas, redondeado a 2 decimales
    """
    if not check_in_time or not check_out_time:
        return 0.0
    
    # Si la hora de salida es menor que la de entrada, asumimos que es del día siguiente
    # Sin este ajuste, obtendríamos un número negativo de horas
    if check_out_time < check_in_time:
        # Ajustar el cálculo para turnos nocturnos que cruzan días
        seconds_difference = (check_out_time - check_in_time).total_seconds() + (24 * 3600)
    else:
        seconds_difference = (check_out_time - check_in_time).total_seconds()
    
    # Convertir a horas y redondear a 2 decimales
    hours_worked = round(seconds_difference / 3600, 2)
    
    return hours_worked

def get_iso_week_number(dt):
    """
    Obtiene el número de semana ISO (1-53) para una fecha.
    
    Args:
        dt (datetime): Fecha para la que obtener el número de semana
        
    Returns:
        int: Número de semana ISO (1-53)
    """
    return dt.isocalendar()[1]

def get_employee_week_hours(employee_id, date_time):
    """
    Obtiene las horas acumuladas del empleado en la semana actual (excluyendo el fichaje en proceso).
    
    Args:
        employee_id (int): ID del empleado
        date_time (datetime): Fecha para determinar la semana
        
    Returns:
        float: Horas acumuladas en la semana
    """
    try:
        from models_work_hours import EmployeeWorkHours
        
        week_number = get_iso_week_number(date_time)
        year = date_time.year
        
        week_record = EmployeeWorkHours.query.filter_by(
            employee_id=employee_id,
            year=year,
            week_number=week_number
        ).first()
        
        return week_record.weekly_hours if week_record else 0.0
        
    except Exception as e:
        logger.error(f"Error al obtener horas semanales para empleado {employee_id}: {str(e)}")
        return 0.0


def update_employee_work_hours(employee_id, check_in_time, hours_worked):
    """
    Actualiza las horas trabajadas acumuladas para un empleado.
    
    Args:
        employee_id (int): ID del empleado
        check_in_time (datetime): Hora de entrada (determina día/semana/mes)
        hours_worked (float): Horas trabajadas en este fichaje
        
    Returns:
        bool: True si la actualización fue exitosa, False en caso contrario
    """
    if not employee_id or not check_in_time or hours_worked <= 0:
        return False
        
    try:
        # Obtener info del empleado para el company_id
        employee = Employee.query.get(employee_id)
        if not employee:
            logger.error(f"❌ Empleado {employee_id} no encontrado")
            return False
            
        company_id = employee.company_id
        
        # Obtener año, mes y semana del check-in
        year = check_in_time.year
        month = check_in_time.month
        week_number = get_iso_week_number(check_in_time)
        
        # Buscar o crear el registro para el empleado en este periodo
        emp_hours = EmployeeWorkHours.query.filter_by(
            employee_id=employee_id,
            year=year,
            month=month,
            week_number=week_number
        ).first()
        
        if not emp_hours:
            # Crear nuevo registro si no existe
            emp_hours = EmployeeWorkHours(
                employee_id=employee_id,
                company_id=company_id,
                year=year,
                month=month,
                week_number=week_number,
                daily_hours=0.0,
                weekly_hours=0.0,
                monthly_hours=0.0
            )
            db.session.add(emp_hours)
        
        # Actualizar acumulados
        emp_hours.daily_hours += hours_worked
        emp_hours.weekly_hours += hours_worked
        emp_hours.monthly_hours += hours_worked
        
        # También actualizar los acumulados de la empresa
        update_company_work_hours(company_id, check_in_time, hours_worked)
        
        db.session.commit()
        return True
        
    except SQLAlchemyError as e:
        db.session.rollback()
        logger.error(f"❌ Error al actualizar horas del empleado {employee_id}: {str(e)}")
        return False
    except Exception as e:
        db.session.rollback()
        logger.error(f"❌ Error inesperado: {str(e)}")
        return False

def update_company_work_hours(company_id, check_in_time, hours_worked):
    """
    Actualiza las horas trabajadas acumuladas para una empresa.
    
    Args:
        company_id (int): ID de la empresa
        check_in_time (datetime): Hora de entrada (determina semana/mes)
        hours_worked (float): Horas trabajadas en este fichaje
        
    Returns:
        bool: True si la actualización fue exitosa, False en caso contrario
    """
    if not company_id or not check_in_time or hours_worked <= 0:
        return False
        
    try:
        # Obtener año, mes y semana del check-in
        year = check_in_time.year
        month = check_in_time.month
        week_number = get_iso_week_number(check_in_time)
        
        # Buscar o crear el registro para la empresa en este periodo
        comp_hours = CompanyWorkHours.query.filter_by(
            company_id=company_id,
            year=year,
            month=month,
            week_number=week_number
        ).first()
        
        if not comp_hours:
            # Crear nuevo registro si no existe
            comp_hours = CompanyWorkHours(
                company_id=company_id,
                year=year,
                month=month,
                week_number=week_number,
                weekly_hours=0.0,
                monthly_hours=0.0
            )
            db.session.add(comp_hours)
        
        # Actualizar acumulados
        comp_hours.weekly_hours += hours_worked
        comp_hours.monthly_hours += hours_worked
        
        # No hacemos commit aquí, se hace en la función llamadora
        return True
        
    except Exception as e:
        logger.error(f"❌ Error al actualizar horas de la empresa {company_id}: {str(e)}")
        return False

def check_weekly_hours_limit(employee_id, check_in_time, new_hours_worked):
    """
    Verifica si agregar las nuevas horas trabajadas excede el límite semanal del empleado.
    
    Args:
        employee_id (int): ID del empleado
        check_in_time (datetime): Fecha y hora de entrada (para determinar la semana)
        new_hours_worked (float): Nuevas horas a agregar
        
    Returns:
        tuple: (bool, dict) - (cumple_limite, info_detallada)
    """
    logger.info("========== INICIO COMPROBACIÓN HORAS SEMANALES ==========")
    
    try:
        # Obtener información del empleado y su contrato
        from models import Employee
        from models_checkpoints import EmployeeContractHours
        
        employee = Employee.query.get(employee_id)
        if not employee:
            logger.error(f"❌ Empleado {employee_id} no encontrado")
            return False, {"error": "Empleado no encontrado"}
        
        logger.info(f"📋 Empleado: {employee.first_name} {employee.last_name} (ID: {employee_id})")
        
        # Obtener configuración de horas de contrato
        contract_hours = EmployeeContractHours.query.filter_by(employee_id=employee_id).first()
        if not contract_hours or not contract_hours.weekly_hours:
            logger.info("✅ No hay límite semanal configurado - APROBADO")
            return True, {
                "limite_semanal": None,
                "horas_actuales": 0,
                "nuevas_horas": new_hours_worked,
                "total_propuesto": new_hours_worked,
                "cumple": True,
                "razon": "Sin límite semanal configurado"
            }
        
        weekly_limit = contract_hours.weekly_hours
        logger.info(f"⏰ Límite semanal configurado: {weekly_limit} horas")
        
        # Obtener año y semana de la fecha de entrada
        year = check_in_time.year
        week_number = get_iso_week_number(check_in_time)
        logger.info(f"📅 Verificando semana {week_number} del año {year}")
        
        # Buscar horas acumuladas actuales para esta semana
        current_hours = EmployeeWorkHours.query.filter_by(
            employee_id=employee_id,
            year=year,
            week_number=week_number
        ).first()
        
        current_weekly_hours = current_hours.weekly_hours if current_hours else 0.0
        proposed_total = current_weekly_hours + new_hours_worked
        
        logger.info(f"📊 Horas actuales esta semana: {current_weekly_hours}")
        logger.info(f"➕ Nuevas horas a agregar: {new_hours_worked}")
        logger.info(f"🎯 Total propuesto: {proposed_total}")
        logger.info(f"🚦 Límite semanal: {weekly_limit}")
        
        if proposed_total > weekly_limit:
            exceso = proposed_total - weekly_limit
            logger.warning(f"❌ LÍMITE EXCEDIDO: {exceso} horas por encima del límite")
            logger.info("🚫 RESULTADO: NO CUMPLE - Fichaje será rechazado")
            
            return False, {
                "limite_semanal": weekly_limit,
                "horas_actuales": current_weekly_hours,
                "nuevas_horas": new_hours_worked,
                "total_propuesto": proposed_total,
                "exceso": exceso,
                "cumple": False,
                "razon": f"Excede límite semanal por {exceso} horas"
            }
        else:
            margen = weekly_limit - proposed_total
            logger.info(f"✅ DENTRO DEL LÍMITE: {margen} horas de margen restante")
            logger.info("✅ RESULTADO: CUMPLE - Fichaje aprobado")
            
            return True, {
                "limite_semanal": weekly_limit,
                "horas_actuales": current_weekly_hours,
                "nuevas_horas": new_hours_worked,
                "total_propuesto": proposed_total,
                "margen_restante": margen,
                "cumple": True,
                "razon": f"Dentro del límite con {margen} horas de margen"
            }
            
    except Exception as e:
        logger.error(f"❌ Error en verificación de horas semanales: {str(e)}")
        return False, {"error": str(e)}
    
    finally:
        logger.info("========== FIN COMPROBACIÓN HORAS SEMANALES ==========")

def apply_weekly_hours_control(employee_id, check_in_time, check_out_time):
    """
    Aplica el control de horas semanales al cerrar un fichaje.
    
    Args:
        employee_id (int): ID del empleado
        check_in_time (datetime): Hora de entrada
        check_out_time (datetime): Hora de salida
        
    Returns:
        tuple: (bool, dict) - (permitir_fichaje, info_verificacion)
    """
    if not check_out_time:
        return True, {"razon": "Fichaje sin hora de salida, no requiere verificación"}
    
    # Calcular horas trabajadas
    hours_worked = calculate_hours_worked(check_in_time, check_out_time)
    
    # Verificar límite semanal
    cumple_limite, info = check_weekly_hours_limit(employee_id, check_in_time, hours_worked)
    
    return cumple_limite, info
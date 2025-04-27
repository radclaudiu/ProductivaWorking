"""
Script para cerrar automáticamente los fichajes pendientes durante la ventana horaria
configurada en cada punto de fichaje.

Este script debería ejecutarse periódicamente, por ejemplo, cada 10 minutos,
para verificar si es momento de cerrar registros pendientes según la ventana horaria
configurada (por ejemplo, solo entre 02:00-04:00). Solo se cerrarán los fichajes
si la hora actual está dentro del rango definido para cada punto de fichaje.
"""
import sys
import os
import logging
from datetime import datetime, timedelta
from sqlalchemy import func

from app import db, create_app
from models_checkpoints import (
    CheckPoint, CheckPointRecord, CheckPointIncident, 
    CheckPointIncidentType, CheckPointStatus, EmployeeContractHours
)
from models import Employee
from timezone_config import get_current_time, datetime_to_madrid, TIMEZONE
from utils_work_hours import update_employee_work_hours

# Configurar logging
logging.basicConfig(
    filename='checkpoints_closer.log',
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] - %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger('checkpoints_closer')

# Variable para detectar primer inicio después de redeploy
STARTUP_FILE = '.checkpoint_closer_startup'

def auto_close_pending_records():
    """
    Cierra automáticamente todos los registros pendientes de los puntos de fichaje
    cuando la hora actual está dentro de la ventana horaria configurada para el cierre
    (entre operation_start_time y operation_end_time).
    """
    timestamp = datetime.now()
    
    # Detectar si es el primer inicio después de un redeploy
    is_first_startup = not os.path.exists(STARTUP_FILE)
    if is_first_startup:
        # Registrar en el log que es el primer inicio después de un redeploy
        startup_message = f"🚀 PRIMER INICIO DESPUÉS DE REDEPLOY - {timestamp}"
        logger.info(startup_message)
        print(f"\n{'*' * 100}")
        print(f"* {startup_message}")
        print(f"{'*' * 100}\n")
        
        # Crear el archivo para futuras ejecuciones
        with open(STARTUP_FILE, 'w') as f:
            f.write(f"Último inicio: {timestamp}")
    
    print(f"\n{'*' * 100}")
    print(f"* INICIANDO FUNCIÓN DE BARRIDO AUTOMÁTICO")
    print(f"* Fecha/hora: {timestamp}")
    print(f"* Versión: 1.2.0")  # Actualizada la versión con detección de redeploy
    print(f"* Primer inicio después de redeploy: {'Sí' if is_first_startup else 'No'}")
    print(f"{'*' * 100}\n")
    
    # Registrar cada ejecución en el log
    if is_first_startup:
        logger.info(f"Iniciando barrido de cierre automático: {timestamp} [PRIMER INICIO TRAS REDEPLOY]")
    else:
        logger.info(f"Iniciando barrido de cierre automático: {timestamp}")
    
    print(f"========== INICIO BARRIDO DE CIERRE AUTOMÁTICO: {timestamp} ==========")
    print(f"Ejecutando verificación de cierre automático dentro de la ventana horaria configurada: {timestamp}")
    
    try:
        # Obtener hora actual en la zona horaria configurada
        current_time = get_current_time()
        current_hour = current_time.time()
        
        print(f"Hora actual (Madrid): {current_hour}")
        
        # Buscar todos los puntos de fichaje con horario de cierre automático activado
        checkpoints = CheckPoint.query.filter(
            CheckPoint.enforce_operation_hours == True,          # Tiene configuración de horario activada
            CheckPoint.operation_start_time.isnot(None),         # Tiene hora de inicio de ventana configurada
            CheckPoint.operation_end_time.isnot(None),           # Tiene hora de fin de ventana configurada
            CheckPoint.status == CheckPointStatus.ACTIVE          # Está activo
        ).all()
        
        if not checkpoints:
            print("No hay puntos de fichaje con ventana horaria de cierre configurada.")
            print(f"========== FIN BARRIDO DE CIERRE AUTOMÁTICO (sin puntos configurados) ==========")
            return True
            
        print(f"Encontrados {len(checkpoints)} puntos de fichaje con ventana horaria configurada.")
        
        # Inicializamos contadores para el resumen final
        total_checkpoints_processed = 0
        total_records_closed = 0
        
        # Para cada punto de fichaje, verificar si la hora actual está dentro de la ventana de cierre
        for checkpoint in checkpoints:
            # Determinar si la hora actual está dentro del rango especificado
            is_within_window = False
            
            # Caso normal: la ventana no cruza la medianoche (ejemplo: 02:00-04:00)
            if checkpoint.operation_start_time <= checkpoint.operation_end_time:
                is_within_window = (current_hour >= checkpoint.operation_start_time and 
                                   current_hour <= checkpoint.operation_end_time)
            # Caso especial: la ventana cruza la medianoche (ejemplo: 23:00-01:00)
            else:
                is_within_window = (current_hour >= checkpoint.operation_start_time or 
                                   current_hour <= checkpoint.operation_end_time)
            
            # Solo procesar el punto de fichaje si estamos dentro de la ventana horaria de cierre
            if is_within_window:
                print(f"Procesando punto de fichaje: {checkpoint.name} (ID: {checkpoint.id}) - Dentro de ventana de cierre: {checkpoint.operation_start_time} - {checkpoint.operation_end_time}")
                total_checkpoints_processed += 1
                
                # Buscar registros pendientes de este punto de fichaje
                pending_records = CheckPointRecord.query.filter(
                    CheckPointRecord.checkpoint_id == checkpoint.id,
                    CheckPointRecord.check_out_time.is_(None)
                ).all()
                
                if not pending_records:
                    print(f"No hay registros pendientes en el punto de fichaje {checkpoint.name}")
                    continue
                    
                print(f"Encontrados {len(pending_records)} registros pendientes para cerrar.")
                
                # Cerrar cada registro pendiente
                for record in pending_records:
                    # Asegurarse de que la fecha de entrada tenga información de zona horaria
                    check_in_time = record.check_in_time
                    if not check_in_time:
                        print(f"  ⚠️ Advertencia: El registro {record.id} no tiene hora de entrada válida")
                        continue
                        
                    if check_in_time.tzinfo is None:
                        check_in_time = datetime_to_madrid(check_in_time)
                    
                    # Establecer la hora de salida como la hora de fin de funcionamiento
                    check_in_date = check_in_time.date()
                    check_out_time = datetime.combine(check_in_date, checkpoint.operation_end_time)
                    check_out_time = TIMEZONE.localize(check_out_time)
                    
                    # Si la salida queda antes que la entrada (lo cual sería un error), 
                    # establecer la salida para el día siguiente
                    if check_out_time < check_in_time:
                        print(f"  ⚠️  Entrada posterior a hora de cierre: {check_in_time} > {check_out_time}")
                        print(f"  ⚠️  Ajustando la salida para el día siguiente")
                        check_out_date = check_in_date + timedelta(days=1)
                        check_out_time = datetime.combine(check_out_date, checkpoint.operation_end_time)
                        check_out_time = TIMEZONE.localize(check_out_time)
                        
                    # El barrido automático no interactúa con los registros originales
                    # (No crea ni modifica registros en checkpoint_original_records)
                    print(f"  ℹ️ El barrido automático no guardará información en registros originales para registro {record.id}")
                    
                    # Variables para usar más adelante (para compatibilidad con el código existente)
                    existing_original = None
                    original_record = None
                    
                    # Asignar la hora de salida calculada al registro principal
                    record.check_out_time = check_out_time
                    
                    # Marcar que fue cerrado automáticamente
                    record.notes = (record.notes or "") + f" [Cerrado automáticamente durante ventana de cierre {checkpoint.operation_start_time} - {checkpoint.operation_end_time}]"
                    record.adjusted = True
                    
                    # Actualizar el estado del empleado a "fuera de turno"
                    employee = Employee.query.get(record.employee_id)
                    if employee and employee.is_on_shift:
                        # Guardar estado previo para logging
                        prev_state = employee.is_on_shift
                        # Actualizar a "no en turno"
                        employee.is_on_shift = False
                        # Limpiar el ID del registro activo
                        employee.current_record_id = None
                        print(f"  ✓ Empleado ID {employee.id} actualizado a 'fuera de turno' (is_on_shift: {prev_state} → False)")
                    
                    # Comprobar si el empleado tiene configuración de horas por contrato
                    contract_hours = EmployeeContractHours.query.filter_by(employee_id=record.employee_id).first()
                    if contract_hours:
                        # En lugar de calcular las horas reales trabajadas,
                        # asignar directamente las horas diarias del contrato
                        daily_contract_hours = contract_hours.daily_hours
                        
                        # Ya no actualizamos registros originales con el barrido automático
                        print(f"  ✓ Usando {daily_contract_hours} horas contractuales para actualizaciones de acumulados")
                            
                        # Actualizar las tablas de acumulados de horas trabajadas
                        update_success = update_employee_work_hours(
                            employee_id=record.employee_id,
                            check_in_time=record.check_in_time,
                            hours_worked=daily_contract_hours
                        )
                        
                        if update_success:
                            print(f"  ✓ Acumulados de horas actualizados con {daily_contract_hours} horas para empleado ID {record.employee_id}")
                        else:
                            print(f"  ✗ Error al actualizar acumulados de horas para empleado ID {record.employee_id}")
                            
                        # Mantener el ajuste de la hora de salida si es necesario
                        check_in_original = record.check_in_time
                        check_out_original = record.check_out_time
                        
                        adjusted_in, adjusted_out = contract_hours.calculate_adjusted_hours(
                            check_in_original, check_out_original
                        )
                        
                        # Si hay ajuste de salida, aplicarlo
                        if adjusted_out and adjusted_out != check_out_original:
                            record.check_out_time = adjusted_out
                            record.notes += f" [R] Hora de salida ajustada de {check_out_original.strftime('%H:%M:%S')} a {adjusted_out.strftime('%H:%M:%S')} por límite de horas contrato."
                    
                    # Crear una incidencia
                    incident = CheckPointIncident(
                        record_id=record.id,
                        incident_type=CheckPointIncidentType.MISSED_CHECKOUT,
                        description=f"Salida automática durante ventana horaria de cierre ({checkpoint.operation_start_time} - {checkpoint.operation_end_time})"
                    )
                    db.session.add(incident)
                
                # Guardar todos los cambios para este punto de fichaje
                try:
                    records_closed = len(pending_records)
                    db.session.commit()
                    total_records_closed += records_closed
                    print(f"✓ {records_closed} registros cerrados correctamente para el punto {checkpoint.name}")
                except Exception as e:
                    db.session.rollback()
                    print(f"✗ Error al cerrar registros para el punto {checkpoint.name}: {e}")
            else:
                print(f"• No es hora de cerrar los registros para {checkpoint.name}. Hora actual: {current_hour}, Ventana de cierre: {checkpoint.operation_start_time} - {checkpoint.operation_end_time}")
        
        # Mostrar resumen final del barrido
        end_timestamp = datetime.now()
        duration = (end_timestamp - timestamp).total_seconds()
        
        # Crear mensaje de resumen
        summary = f"\n========== RESUMEN DEL BARRIDO DE CIERRE AUTOMÁTICO ==========\n"
        summary += f"Fecha y hora de inicio: {timestamp}\n"
        summary += f"Fecha y hora de fin: {end_timestamp}\n"
        summary += f"Duración: {duration:.2f} segundos\n"
        summary += f"Primer inicio tras redeploy: {'Sí' if is_first_startup else 'No'}\n"
        summary += f"Puntos de fichaje procesados: {total_checkpoints_processed} de {len(checkpoints)}\n"
        summary += f"Registros cerrados: {total_records_closed}\n"
        summary += f"========== FIN BARRIDO DE CIERRE AUTOMÁTICO ==========\n"
        
        # Mostrar en consola y registrar en log
        print(summary)
        logger.info(f"Barrido completado - {total_records_closed} registros cerrados en {duration:.2f} segundos")
        
        return True
        
    except Exception as e:
        end_timestamp = datetime.now()
        
        # Crear mensaje de error
        error_msg = f"\n========== ERROR EN BARRIDO DE CIERRE AUTOMÁTICO ==========\n"
        error_msg += f"Fecha y hora de inicio: {timestamp}\n"
        error_msg += f"Fecha y hora de error: {end_timestamp}\n"
        error_msg += f"Primer inicio tras redeploy: {'Sí' if is_first_startup else 'No'}\n"
        error_msg += f"Error general durante el proceso: {str(e)}\n"
        error_msg += f"========== FIN BARRIDO CON ERROR ==========\n"
        
        # Mostrar en consola y registrar en log
        print(error_msg)
        logger.error(f"Error en barrido: {str(e)}")
        
        return False


if __name__ == "__main__":
    app = create_app()
    with app.app_context():
        success = auto_close_pending_records()
    
    sys.exit(0 if success else 1)
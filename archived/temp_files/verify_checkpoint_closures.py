"""
Script para verificar el estado del sistema de cierre automático de fichajes.

Este script realiza una auditoría del sistema de cierre automático para:
1. Comprobar que no haya fichajes pendientes en puntos de fichaje fuera de horario
2. Generar un informe de estado con las incidencias detectadas
3. Proporcionar estadísticas sobre la efectividad del sistema de cierre
4. Detectar si es la primera ejecución después de un redeploy
"""
import sys
import os
from datetime import datetime, timedelta
from app import db, create_app
from models_checkpoints import (
    CheckPoint, CheckPointRecord, CheckPointIncident, 
    CheckPointIncidentType, CheckPointStatus
)
from timezone_config import get_current_time, datetime_to_madrid, TIMEZONE
from close_operation_hours import STARTUP_FILE

def check_pending_records_after_hours():
    """
    Busca fichajes pendientes en puntos de fichaje que deberían haberse cerrado
    automáticamente por estar fuera del horario de funcionamiento.
    """
    current_time = get_current_time()
    current_time_utc = datetime.utcnow()
    
    # Detectar si es el primer inicio después de un redeploy
    is_first_startup = not os.path.exists(STARTUP_FILE)
    
    print(f"\n{'=' * 100}")
    print(f"VERIFICACIÓN DE FICHAJES PENDIENTES FUERA DE HORARIO")
    print(f"{'=' * 100}")
    print(f"Fecha/hora actual: {current_time} ({TIMEZONE})")
    print(f"Fecha/hora UTC: {current_time_utc}")
    print(f"Primer inicio tras redeploy: {'Sí' if is_first_startup else 'No'}")
    
    # Si es el primer inicio, crear el archivo de startup
    if is_first_startup:
        with open(STARTUP_FILE, 'w') as f:
            f.write(f"Último inicio: {datetime.now()}")
    
    # Buscar puntos de fichaje con horario de funcionamiento configurado
    checkpoints = CheckPoint.query.filter(
        CheckPoint.enforce_operation_hours == True,
        CheckPoint.operation_end_time.isnot(None),
        CheckPoint.status == CheckPointStatus.ACTIVE
    ).all()
    
    if not checkpoints:
        print("No hay puntos de fichaje con horario de funcionamiento configurado.")
        return True
    
    print(f"\nPuntos de fichaje activos con horario: {len(checkpoints)}")
    
    # Estadísticas globales
    total_pending_records = 0
    total_incidents = 0
    
    for checkpoint in checkpoints:
        print(f"\n{'-' * 90}")
        print(f"Punto de fichaje: {checkpoint.name} (ID: {checkpoint.id})")
        print(f"Empresa: {checkpoint.company.name}")
        print(f"Horario: {checkpoint.operation_start_time} - {checkpoint.operation_end_time}")
        
        # Obtener hora de fin del punto de fichaje
        end_time = checkpoint.operation_end_time
        
        # Obtener fecha actual en Madrid
        current_date = get_current_time().date()
        
        # Crear datetime con la fecha actual y la hora de fin
        end_datetime = datetime.combine(current_date, end_time)
        end_datetime = TIMEZONE.localize(end_datetime)
        
        # Calcular si estamos después de la hora de fin
        is_past_end_time = current_time.time() > end_time
        
        print(f"¿Pasada hora de cierre? {'Sí' if is_past_end_time else 'No'}")
        
        # Si estamos pasada la hora de fin, buscar registros pendientes
        if is_past_end_time:
            # Buscar registros pendientes para este punto de fichaje
            pending_records = CheckPointRecord.query.filter(
                CheckPointRecord.checkpoint_id == checkpoint.id,
                CheckPointRecord.check_out_time.is_(None)
            ).all()
            
            if pending_records:
                print(f"⚠️ ALERTA: Se encontraron {len(pending_records)} registros pendientes fuera de horario.")
                print(f"Estos registros deberían haberse cerrado automáticamente.")
                
                for record in pending_records:
                    print(f"  • Empleado: {record.employee.first_name} {record.employee.last_name}")
                    print(f"    Entrada: {record.check_in_time}")
                    print(f"    ID registro: {record.id}")
                
                total_pending_records += len(pending_records)
            else:
                print(f"✅ Correcto: No hay registros pendientes fuera de horario.")
            
            # Verificar incidencias generadas por cierres automáticos en las últimas 24 horas
            yesterday = current_time_utc - timedelta(days=1)
            incidents = CheckPointIncident.query.join(CheckPointRecord).filter(
                CheckPointRecord.checkpoint_id == checkpoint.id,
                CheckPointIncident.incident_type == CheckPointIncidentType.MISSED_CHECKOUT,
                CheckPointIncident.created_at >= yesterday
            ).all()
            
            if incidents:
                print(f"📊 Incidencias de cierre automático en últimas 24h: {len(incidents)}")
                total_incidents += len(incidents)
    
    print(f"\n{'=' * 100}")
    print(f"RESUMEN DE LA VERIFICACIÓN")
    print(f"{'=' * 100}")
    print(f"Puntos de fichaje verificados: {len(checkpoints)}")
    print(f"Registros pendientes fuera de horario: {total_pending_records}")
    print(f"Incidencias de cierre automático en últimas 24h: {total_incidents}")
    print(f"Primer inicio tras redeploy: {'Sí' if is_first_startup else 'No'}")
    
    if total_pending_records > 0:
        print(f"\n⚠️ ATENCIÓN: Se han encontrado {total_pending_records} registros pendientes que deberían haberse cerrado.")
        print(f"Esto puede indicar un problema con el sistema de cierre automático.")
        print(f"Recomendación: Verificar que el servicio 'scheduled_checkpoints_closer.py' esté en ejecución.")
        return False
    else:
        print(f"\n✅ ÉXITO: No se han encontrado registros pendientes fuera de horario.")
        print(f"El sistema de cierre automático está funcionando correctamente.")
        return True

if __name__ == "__main__":
    app = create_app()
    with app.app_context():
        success = check_pending_records_after_hours()
    
    sys.exit(0 if success else 1)
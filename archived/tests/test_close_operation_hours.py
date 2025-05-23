"""
Script para probar manualmente el cierre automático de fichajes pendientes
cuando termina el horario de operación de un punto de fichaje.

Este script permite:
1. Ver los puntos de fichaje con horarios configurados
2. Forzar el cierre de fichajes para un punto específico sin esperar a la hora de fin
"""
import sys
from datetime import datetime, time, timedelta

from app import db, create_app
from models_checkpoints import CheckPoint, CheckPointRecord, CheckPointIncident, CheckPointIncidentType
from timezone_config import TIMEZONE, get_current_time, datetime_to_madrid

def list_checkpoints_with_hours():
    """Muestra todos los puntos de fichaje con horarios configurados"""
    checkpoints = CheckPoint.query.filter(
        CheckPoint.operation_end_time.isnot(None)
    ).all()
    
    print(f"\n{'=' * 100}")
    print(f"PUNTOS DE FICHAJE CON HORARIOS CONFIGURADOS: {len(checkpoints)}")
    print(f"{'=' * 100}")
    
    for i, cp in enumerate(checkpoints, 1):
        status_text = "ACTIVO" if cp.status.name == "ACTIVE" else "INACTIVO"
        enforce_text = "✓ ACTIVADO" if cp.enforce_operation_hours else "✗ DESACTIVADO"
        
        # Contar registros pendientes
        pending_count = CheckPointRecord.query.filter(
            CheckPointRecord.checkpoint_id == cp.id,
            CheckPointRecord.check_out_time.is_(None)
        ).count()
        
        print(f"{i}. ID: {cp.id} - {cp.name} ({cp.company.name})")
        print(f"   Estado: {status_text} - Cierre automático: {enforce_text}")
        print(f"   Horario: {cp.operation_start_time or '??:??'} a {cp.operation_end_time or '??:??'}")
        print(f"   Registros pendientes: {pending_count}")
        print(f"   {'-' * 80}")
    
    return checkpoints

def force_close_checkpoint_records(checkpoint_id):
    """
    Fuerza el cierre de todos los registros pendientes para un punto de fichaje específico
    sin importar la hora actual.
    """
    checkpoint = CheckPoint.query.get(checkpoint_id)
    if not checkpoint:
        print(f"Error: No se encontró un punto de fichaje con ID {checkpoint_id}")
        return False
        
    if not checkpoint.operation_end_time:
        print(f"Error: El punto de fichaje {checkpoint.name} no tiene configurada una hora de fin")
        return False
    
    print(f"\n{'=' * 100}")
    print(f"FORZANDO CIERRE DE FICHAJES PARA: {checkpoint.name}")
    print(f"{'=' * 100}")
    
    # Buscar registros pendientes
    pending_records = CheckPointRecord.query.filter(
        CheckPointRecord.checkpoint_id == checkpoint.id,
        CheckPointRecord.check_out_time.is_(None)
    ).all()
    
    if not pending_records:
        print(f"No hay registros pendientes para cerrar en el punto {checkpoint.name}")
        return True
    
    print(f"Encontrados {len(pending_records)} registros pendientes para cerrar.")
    
    # Cerrar cada registro pendiente
    for record in pending_records:
        # Crear una copia del registro original si es la primera modificación
        if not record.original_check_in_time:
            record.original_check_in_time = record.check_in_time
        
        # Asegurarse de que la fecha de entrada tenga información de zona horaria
        check_in_time = record.check_in_time
        if check_in_time and check_in_time.tzinfo is None:
            check_in_time = datetime_to_madrid(check_in_time)
        
        if not check_in_time:
            print(f"  ⚠️  Advertencia: El registro {record.id} no tiene hora de entrada válida")
            continue
            
        # Establecer la hora de salida como la hora de fin de funcionamiento
        check_in_date = check_in_time.date()
        check_out_time = datetime.combine(check_in_date, checkpoint.operation_end_time)
        check_out_time = TIMEZONE.localize(check_out_time)
        
        # Si la salida queda antes que la entrada, establecer la salida para el día siguiente
        if check_out_time and check_in_time and check_out_time < check_in_time:
            print(f"  ⚠️  Advertencia: Hora de entrada {check_in_time} posterior a hora de cierre {check_out_time}")
            print(f"  ⚠️  Ajustando la salida para el día siguiente")
            check_out_date = check_in_date + timedelta(days=1)
            check_out_time = datetime.combine(check_out_date, checkpoint.operation_end_time)
            check_out_time = TIMEZONE.localize(check_out_time)
        
        print(f"  • Empleado: {record.employee.first_name} {record.employee.last_name}")
        print(f"    Entrada: {record.check_in_time}")
        print(f"    Nueva Salida: {check_out_time}")
        
        # Asignar la hora de salida calculada
        record.check_out_time = check_out_time
        
        # Marcar que fue cerrado automáticamente
        record.notes = (record.notes or "") + " [Forzado manualmente - Cierre por fin de horario]"
        record.adjusted = True
        
        # Crear una incidencia
        incident = CheckPointIncident(
            record_id=record.id,
            incident_type=CheckPointIncidentType.MISSED_CHECKOUT,
            description=f"Salida forzada manualmente por fin de horario de funcionamiento ({checkpoint.operation_end_time})"
        )
        db.session.add(incident)
    
    # Guardar todos los cambios
    try:
        db.session.commit()
        print(f"\n✅ Se cerraron correctamente {len(pending_records)} registros pendientes")
        return True
    except Exception as e:
        db.session.rollback()
        print(f"\n❌ Error al cerrar los registros: {e}")
        return False

def main():
    app = create_app()
    
    with app.app_context():
        # Ejecutar automáticamente la opción de listar puntos de fichaje
        print("\n=== VERIFICACIÓN DE PUNTOS DE FICHAJE CON HORARIOS ===")
        checkpoints = list_checkpoints_with_hours()
        
        if not checkpoints:
            print("\nNo hay puntos de fichaje con horarios configurados")
            return
        
        # Si solo hay un punto de fichaje, preguntar si quiere cerrarlo
        if len(checkpoints) == 1:
            checkpoint = checkpoints[0]
            print(f"\n¿Desea forzar el cierre de los fichajes para {checkpoint.name} (ID: {checkpoint.id})? (s/n)")
            print("Presione Ctrl+C para cancelar")
            
            try:
                # Forzar cierre automáticamente para el punto encontrado
                print(f"\nForzando cierre para el punto de fichaje: {checkpoint.name} (ID: {checkpoint.id})")
                force_close_checkpoint_records(checkpoint.id)
            except KeyboardInterrupt:
                print("\nOperación cancelada por el usuario.")
            except Exception as e:
                print(f"\nError: {e}")
        else:
            # Si hay varios puntos, mostrar el primero
            if checkpoints:
                checkpoint = checkpoints[0]
                print(f"\nForzando cierre para el primer punto de fichaje: {checkpoint.name} (ID: {checkpoint.id})")
                force_close_checkpoint_records(checkpoint.id)

if __name__ == "__main__":
    main()
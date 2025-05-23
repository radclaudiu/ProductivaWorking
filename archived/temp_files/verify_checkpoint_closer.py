"""
Script para verificar el funcionamiento del cierre automático de fichajes pendientes.

Uso:
  python verify_checkpoint_closer.py              # Modo interactivo
  python verify_checkpoint_closer.py --status     # Solo verificar estado
  python verify_checkpoint_closer.py --start      # Iniciar servicio
  python verify_checkpoint_closer.py --stop       # Detener servicio
  python verify_checkpoint_closer.py --run        # Ejecutar cierre manual
  python verify_checkpoint_closer.py --all        # Realizar todas las operaciones
"""
from app import create_app, db
from models import Employee
from models_checkpoints import CheckPoint, CheckPointRecord
import close_operation_hours
from checkpoint_closer_service import get_service_status, start_checkpoint_closer_service, stop_checkpoint_closer_service
import sys
import argparse

app = create_app()

def print_pending_records():
    """Imprime los registros pendientes."""
    pending_records = CheckPointRecord.query.filter_by(check_out_time=None).all()
    print(f"\nRegistros pendientes: {len(pending_records)}")
    
    for record in pending_records:
        print(f"  Registro {record.id} - Empleado: {record.employee.first_name} {record.employee.last_name}")
        print(f"    Entrada: {record.check_in_time.strftime('%Y-%m-%d %H:%M')}")

def check_auto_close_configuration():
    """Verifica la configuración de cierre automático de los puntos de fichaje."""
    checkpoints = CheckPoint.query.all()
    print(f"\nConfiguración de puntos de fichaje: {len(checkpoints)}")
    
    for cp in checkpoints:
        print(f"  {cp.id}. {cp.name} ({cp.company.name})")
        print(f"    Horas de operación: {'SÍ' if cp.enforce_operation_hours else 'NO'}")
        if cp.operation_start_time and cp.operation_end_time:
            print(f"    Ventana horaria: {cp.operation_start_time.strftime('%H:%M')} - {cp.operation_end_time.strftime('%H:%M')}")
        else:
            print("    Ventana horaria: No configurada")

def check_service_status():
    """Verifica el estado del servicio de cierre automático."""
    status = get_service_status()
    
    print("\nEstado del servicio de cierre automático:")
    print(f"  Activo: {'Sí' if status.get('active', False) else 'No'}")
    print(f"  En ejecución: {'Sí' if status.get('running', False) else 'No'}")
    print(f"  Último cierre: {status.get('last_run', 'No ejecutado aún')}")
    print(f"  Próximo cierre: {status.get('next_run', 'Pendiente')}")
    print(f"  Hilo activo: {'Sí' if status.get('thread_alive', False) else 'No'}")
    print(f"  Intervalo de verificación: {status.get('check_interval_minutes', 0)} minutos")
    
    return status

def main():
    """Función principal que ejecuta las verificaciones."""
    with app.app_context():
        # Verificar la configuración de cierre automático
        check_auto_close_configuration()
        
        # Mostrar registros pendientes antes del cierre
        print_pending_records()
        
        # Verificar estado del servicio
        status = check_service_status()
        
        # Preguntar si se desea iniciar/detener el servicio
        if not status.get('active', False):
            response = input("\n¿Desea iniciar el servicio de cierre automático? (s/n): ")
            if response.lower() == 's':
                start_checkpoint_closer_service()
                print("Servicio iniciado. Estado actualizado:")
                check_service_status()
        else:
            response = input("\n¿Desea detener el servicio de cierre automático? (s/n): ")
            if response.lower() == 's':
                stop_checkpoint_closer_service()
                print("Servicio detenido. Estado actualizado:")
                check_service_status()
        
        # Preguntar si se desea ejecutar el cierre manual
        response = input("\n¿Desea ejecutar un cierre manual de registros pendientes? (s/n): ")
        if response.lower() == 's':
            # Contar registros pendientes antes del cierre
            before_count = CheckPointRecord.query.filter_by(check_out_time=None).count()
            
            # Ejecutar el cierre automático
            print("\nEjecutando el cierre automático de registros pendientes...")
            success = close_operation_hours.auto_close_pending_records()
            
            # Contar registros pendientes después del cierre
            after_count = CheckPointRecord.query.filter_by(check_out_time=None).count()
            
            # Calcular cuántos registros se cerraron
            closed_count = before_count - after_count
            
            if success:
                print(f"\nCierre automático completado con éxito.")
                print(f"Registros pendientes antes: {before_count}")
                print(f"Registros pendientes después: {after_count}")
                print(f"Registros cerrados: {closed_count}")
            else:
                print("\nError durante el cierre automático.")
            
            # Mostrar registros pendientes después del cierre
            print_pending_records()

def run_auto_close_manually():
    """Ejecuta el cierre automático de registros pendientes manualmente."""
    with app.app_context():
        # Contar registros pendientes antes del cierre
        before_count = CheckPointRecord.query.filter_by(check_out_time=None).count()
        
        # Ejecutar el cierre automático
        print("\nEjecutando el cierre automático de registros pendientes...")
        success = close_operation_hours.auto_close_pending_records()
        
        # Contar registros pendientes después del cierre
        after_count = CheckPointRecord.query.filter_by(check_out_time=None).count()
        
        # Calcular cuántos registros se cerraron
        closed_count = before_count - after_count
        
        if success:
            print(f"\nCierre automático completado con éxito.")
            print(f"Registros pendientes antes: {before_count}")
            print(f"Registros pendientes después: {after_count}")
            print(f"Registros cerrados: {closed_count}")
        else:
            print("\nError durante el cierre automático.")
        
        # Mostrar registros pendientes después del cierre
        print_pending_records()

def parse_args():
    """Analiza los argumentos de línea de comandos."""
    parser = argparse.ArgumentParser(description='Gestión del servicio de cierre automático de fichajes')
    parser.add_argument('--status', action='store_true', help='Mostrar estado del servicio')
    parser.add_argument('--start', action='store_true', help='Iniciar servicio de cierre automático')
    parser.add_argument('--stop', action='store_true', help='Detener servicio de cierre automático')
    parser.add_argument('--run', action='store_true', help='Ejecutar cierre manual')
    parser.add_argument('--all', action='store_true', help='Realizar todas las verificaciones')
    return parser.parse_args()

if __name__ == "__main__":
    args = parse_args()
    
    with app.app_context():
        # Verificar la configuración de cierre automático
        check_auto_close_configuration()
        
        # Mostrar registros pendientes
        print_pending_records()
        
        # Si no se proporcionó ningún argumento, usar modo interactivo
        if not any(vars(args).values()):
            main()
        else:
            # Verificar estado del servicio
            status = check_service_status()
            
            # Procesar argumentos
            if args.start or args.all:
                if not status.get('active', False):
                    print("\nIniciando servicio de cierre automático...")
                    start_checkpoint_closer_service()
                    print("Servicio iniciado. Estado actualizado:")
                    check_service_status()
                else:
                    print("\nEl servicio ya está activo.")
            
            if args.stop or args.all:
                if status.get('active', False):
                    print("\nDeteniendo servicio de cierre automático...")
                    stop_checkpoint_closer_service()
                    print("Servicio detenido. Estado actualizado:")
                    check_service_status()
                else:
                    print("\nEl servicio no está activo.")
            
            if args.run or args.all:
                run_auto_close_manually()
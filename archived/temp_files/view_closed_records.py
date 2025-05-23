"""
Script para ver los registros de fichaje cerrados y sus registros originales.
"""
from app import create_app, db
from models_checkpoints import CheckPointRecord, CheckPointOriginalRecord
from models import Employee
from timezone_config import format_datetime

app = create_app()

def view_closed_records():
    """Muestra los registros cerrados y sus registros originales."""
    with app.app_context():
        # Obtener los registros cerrados automáticamente (motivo contiene 'cierre automático')
        original_records = (CheckPointOriginalRecord.query
                         .filter(CheckPointOriginalRecord.adjustment_reason.like('%cierre automático%'))
                         .order_by(CheckPointOriginalRecord.id.desc())
                         .all())
        
        if not original_records:
            print("No se encontraron registros cerrados automáticamente")
            
        record_ids = [orig.record_id for orig in original_records]
        
        # Obtener los registros correspondientes
        records = (CheckPointRecord.query
                  .filter(CheckPointRecord.id.in_(record_ids))
                  .order_by(CheckPointRecord.check_out_time.desc())
                  .all())
        
        if not records:
            print("No se encontraron registros cerrados")
            return
        
        print(f"\nRegistros cerrados: {len(records)}")
        print("-" * 80)
        
        for record in records:
            employee = Employee.query.get(record.employee_id)
            
            print(f"Registro {record.id} - Empleado: {employee.first_name} {employee.last_name}")
            print(f"  Entrada: {format_datetime(record.check_in_time)}")
            print(f"  Salida: {format_datetime(record.check_out_time)}")
            print(f"  Duración: {record.duration():.2f} horas")
            
            # Obtener registros originales si existen
            original_records = CheckPointOriginalRecord.query.filter_by(record_id=record.id).all()
            
            if original_records:
                print("  Registros originales:")
                for orig in original_records:
                    print(f"    - ID: {orig.id}")
                    print(f"      Entrada original: {format_datetime(orig.original_check_in_time)}")
                    print(f"      Salida original: {format_datetime(orig.original_check_out_time)}")
                    print(f"      Motivo ajuste: {orig.adjustment_reason}")
            
            print("-" * 80)

if __name__ == "__main__":
    view_closed_records()
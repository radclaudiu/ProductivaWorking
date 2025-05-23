"""
Script para crear algunos registros pendientes para pruebas de cierre automático.
"""
from datetime import datetime, timedelta
from app import create_app, db
from models_checkpoints import CheckPointRecord, CheckPointOriginalRecord
from models import Employee
from timezone_config import get_current_time

app = create_app()

def create_pending_records(num_records=3):
    """
    Crea registros pendientes (solo check-in, sin check-out) para pruebas.
    
    Args:
        num_records: Número de registros a crear
    """
    with app.app_context():
        # Obtener algunos empleados de DNLN
        employees = Employee.query.filter(Employee.company_id == 17).limit(num_records).all()
        
        if not employees:
            print("No se encontraron empleados para la empresa DNLN")
            return
            
        # Hora actual en Madrid
        madrid_now = get_current_time()
        
        # Crear registros pendientes con diferentes horas de entrada
        records_created = 0
        
        for i, employee in enumerate(employees):
            # Crear tiempo de entrada (hace entre 1 y 3 horas)
            hours_ago = i + 1
            check_in_time = madrid_now - timedelta(hours=hours_ago)
            
            # Crear registro
            record = CheckPointRecord(
                employee_id=employee.id,
                checkpoint_id=8,  # tablet (DNLN)
                check_in_time=check_in_time,
                check_out_time=None
            )
            db.session.add(record)
            
            # Actualizar estado del empleado
            employee.is_on_shift = True
            
            records_created += 1
            
        # Guardar cambios en la base de datos
        db.session.commit()
        
        # Crear registros originales
        created_records = CheckPointRecord.query.filter(
            CheckPointRecord.check_out_time.is_(None),
            CheckPointRecord.checkpoint_id == 8
        ).order_by(CheckPointRecord.id.desc()).limit(records_created).all()
        
        for record in created_records:
            # Crear registro original
            original = CheckPointOriginalRecord(
                record_id=record.id,
                original_check_in_time=record.check_in_time,
                original_check_out_time=None,
                adjustment_reason="Registro inicial"
            )
            db.session.add(original)
        
        db.session.commit()
        
        print(f"Se crearon {records_created} registros pendientes:")
        for record in created_records:
            employee = Employee.query.get(record.employee_id)
            print(f"  Registro {record.id} - Empleado: {employee.first_name} {employee.last_name}")
            print(f"    Entrada: {record.check_in_time}")

if __name__ == "__main__":
    create_pending_records(3)
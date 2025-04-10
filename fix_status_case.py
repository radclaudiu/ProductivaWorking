"""
Script para corregir el problema de valor 'activo' en la base de datos.
Cambia todos los valores 'activo' a 'ACTIVO' para que coincidan con el enumerador EmployeeStatus.
"""
from app import app, db
from sqlalchemy import text
from models import Employee, EmployeeStatus
from utils import log_activity

def fix_status_values():
    """
    Corrige los valores de status en la tabla employees,
    cambiando 'activo' a 'ACTIVO' para que coincidan con el enumerador EmployeeStatus.
    """
    print("Buscando registros con status 'activo'...")
    
    # Ejecutar SQL directo para encontrar y corregir los valores
    with app.app_context():
        # Usar sqlalchemy.text para declarar explícitamente consultas de texto
        sql = text("UPDATE employees SET status = 'ACTIVO' WHERE status = 'activo'")
        result = db.session.execute(sql)
        count = result.rowcount
        db.session.commit()
        
        print(f"✅ Actualizados {count} registros con status 'activo' a 'ACTIVO'")
        try:
            log_activity(f"Script de corrección - actualizados {count} registros con status 'activo' a 'ACTIVO'")
        except Exception as e:
            print(f"Nota: No se pudo registrar la actividad: {e}")
    
    return count

if __name__ == "__main__":
    fix_status_values()
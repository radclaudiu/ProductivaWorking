"""
Script para corregir el problema de valor 'activo' en la base de datos.
Cambia todos los valores 'activo' a 'ACTIVO' para que coincidan con el enumerador EmployeeStatus.
Este script corrige todas las tablas que puedan tener relaciones con estos valores.
"""
from app import app, db
from sqlalchemy import text, inspect
from models import Employee, EmployeeStatus
from utils import log_activity

def get_all_tables():
    """Obtiene todas las tablas de la base de datos."""
    with app.app_context():
        inspector = inspect(db.engine)
        return inspector.get_table_names()

def fix_status_values():
    """
    Corrige los valores de status en todas las tablas relevantes,
    cambiando 'activo' a 'ACTIVO' para que coincidan con el enumerador EmployeeStatus.
    """
    print("Buscando y corrigiendo registros con status 'activo' en toda la base de datos...")
    
    total_updated = 0
    
    with app.app_context():
        # Primero actualizar la tabla principal de empleados
        sql = text("UPDATE employees SET status = 'ACTIVO' WHERE status = 'activo'")
        result = db.session.execute(sql)
        count = result.rowcount
        db.session.commit()
        total_updated += count
        print(f"✅ Actualizados {count} registros en tabla employees")
        
        # Buscar todas las columnas status en todas las tablas
        inspector = inspect(db.engine)
        all_tables = inspector.get_table_names()
        
        for table in all_tables:
            if table == 'employees':
                continue  # Ya lo procesamos
                
            columns = inspector.get_columns(table)
            has_status = any(col['name'] == 'status' for col in columns)
            
            if has_status:
                # Comprobar si hay registros con 'activo'
                check_sql = text(f"SELECT COUNT(*) FROM {table} WHERE status = 'activo'")
                try:
                    result = db.session.execute(check_sql)
                    count_activo = result.scalar()
                    
                    if count_activo > 0:
                        update_sql = text(f"UPDATE {table} SET status = 'ACTIVO' WHERE status = 'activo'")
                        result = db.session.execute(update_sql)
                        count = result.rowcount
                        db.session.commit()
                        total_updated += count
                        print(f"✅ Actualizados {count} registros en tabla {table}")
                except Exception as e:
                    print(f"⚠️ Error al procesar tabla {table}: {e}")
        
        # Limpiar la caché de la sesión para asegurar que todos los objetos se recargan correctamente
        db.session.expire_all()
        
        try:
            log_activity(f"Script de corrección - actualizados {total_updated} registros con status 'activo' a 'ACTIVO' en toda la base de datos")
        except Exception as e:
            print(f"Nota: No se pudo registrar la actividad: {e}")
    
    print(f"✅ TOTAL: Actualizados {total_updated} registros en toda la base de datos")
    return total_updated

if __name__ == "__main__":
    fix_status_values()
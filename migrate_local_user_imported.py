"""
Script de migración para añadir los campos 'imported' y 'employee_id' a la tabla local_users.
Estos campos permitirán vincular usuarios locales con empleados de la empresa.
"""

from app import db, app
import sqlalchemy as sa
from sqlalchemy import text, inspect
from sqlalchemy.exc import OperationalError, ProgrammingError

def run_migration():
    print("Iniciando migración para añadir columnas a tabla local_users")
    
    with app.app_context():
        conn = db.engine.connect()
        trans = conn.begin()
        
        try:
            # Método más seguro: verificar si las columnas existen usando inspector
            inspector = inspect(db.engine)
            columns = [column['name'] for column in inspector.get_columns('local_users')]
            
            # Verificar y añadir columna 'imported' si no existe
            if 'imported' not in columns:
                print("La columna 'imported' no existe. Procediendo a crearla...")
                conn.execute(text(
                    "ALTER TABLE local_users ADD COLUMN imported BOOLEAN DEFAULT FALSE"
                ))
                print("Columna 'imported' añadida correctamente")
            else:
                print("La columna 'imported' ya existe en la tabla local_users")
                
            # Verificar y añadir columna 'employee_id' si no existe
            if 'employee_id' not in columns:
                print("La columna 'employee_id' no existe. Procediendo a crearla...")
                conn.execute(text(
                    "ALTER TABLE local_users ADD COLUMN employee_id INTEGER"
                ))
                print("Columna 'employee_id' añadida correctamente")
            else:
                print("La columna 'employee_id' ya existe en la tabla local_users")
            
            # Confirmar los cambios
            trans.commit()
            print("Migración completada con éxito")
            
        except Exception as e:
            trans.rollback()
            print(f"Error durante la migración: {str(e)}")
            raise
        finally:
            conn.close()

if __name__ == "__main__":
    run_migration()
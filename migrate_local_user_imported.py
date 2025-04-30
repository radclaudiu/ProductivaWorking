"""
Script de migración para añadir el campo 'imported' a la tabla local_users.
Este campo indicará si el usuario local fue importado desde un empleado.
"""

from app import db, app
import sqlalchemy as sa
from sqlalchemy import text, inspect
from sqlalchemy.exc import OperationalError, ProgrammingError

def run_migration():
    print("Iniciando migración para añadir columna 'imported' a local_users")
    
    with app.app_context():
        conn = db.engine.connect()
        trans = conn.begin()
        
        try:
            # Método más seguro: verificar si la columna existe usando inspector
            inspector = inspect(db.engine)
            columns = [column['name'] for column in inspector.get_columns('local_users')]
            
            if 'imported' in columns:
                print("La columna 'imported' ya existe en la tabla local_users")
                trans.commit()
                conn.close()
                return
                
            print("La columna 'imported' no existe. Procediendo a crearla...")
            
            # Añadir la columna 'imported' a la tabla local_users
            # Usando SQL directo con execute para mayor compatibilidad
            conn.execute(text(
                "ALTER TABLE local_users ADD COLUMN imported BOOLEAN DEFAULT FALSE"
            ))
            
            # Confirmar los cambios
            trans.commit()
            print("Migración completada con éxito: columna 'imported' añadida a local_users")
            
        except Exception as e:
            trans.rollback()
            print(f"Error durante la migración: {str(e)}")
            raise
        finally:
            conn.close()

if __name__ == "__main__":
    run_migration()
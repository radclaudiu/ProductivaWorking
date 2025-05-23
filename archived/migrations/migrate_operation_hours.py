"""
Migración para agregar los campos de configuración de horario de funcionamiento a los puntos de fichaje
"""
import sys
import time
import os
import psycopg2
from sqlalchemy import text

def migrate():
    """
    Agrega los campos necesarios para la configuración de horario de funcionamiento:
    - operation_start_time: Hora de inicio de funcionamiento
    - operation_end_time: Hora de fin de funcionamiento
    - enforce_operation_hours: Si se debe aplicar el horario de funcionamiento
    """
    print("Iniciando migración para agregar campos de configuración de horario de funcionamiento...")
    
    # Obtener la URL de conexión de la variable de entorno
    db_url = os.environ.get('DATABASE_URL')
    if not db_url:
        print("ERROR: No se encontró la variable de entorno DATABASE_URL")
        return False
    
    # Conectar directamente a PostgreSQL
    try:
        # Crear conexión
        conn = psycopg2.connect(db_url)
        conn.autocommit = False  # Queremos control sobre la transacción
        
        # Crear cursor
        cursor = conn.cursor()
        
        try:
            # Verificar si las columnas ya existen
            cursor.execute("""
                SELECT column_name 
                FROM information_schema.columns 
                WHERE table_name = 'checkpoints' 
                AND (column_name = 'operation_start_time' 
                     OR column_name = 'operation_end_time' 
                     OR column_name = 'enforce_operation_hours')
            """)
            
            existing_columns = [col[0] for col in cursor.fetchall()]
            
            # Si todas las columnas ya existen, la migración ya se realizó
            if len(existing_columns) == 3:
                print("La migración ya ha sido ejecutada anteriormente. No se realizaron cambios.")
                conn.rollback()  # No hay cambios que aplicar
                return True
            
            # Intentar agregar cada columna si no existe
            if 'operation_start_time' not in existing_columns:
                print("Agregando columna operation_start_time...")
                cursor.execute("ALTER TABLE checkpoints ADD COLUMN operation_start_time TIME NULL")
            
            if 'operation_end_time' not in existing_columns:
                print("Agregando columna operation_end_time...")
                cursor.execute("ALTER TABLE checkpoints ADD COLUMN operation_end_time TIME NULL")
            
            if 'enforce_operation_hours' not in existing_columns:
                print("Agregando columna enforce_operation_hours...")
                cursor.execute("ALTER TABLE checkpoints ADD COLUMN enforce_operation_hours BOOLEAN NOT NULL DEFAULT false")
            
            # Confirmar los cambios
            conn.commit()
            print("Migración completada con éxito!")
            return True
            
        except Exception as e:
            conn.rollback()
            print(f"Error durante la migración (transacción revertida): {e}")
            return False
        finally:
            cursor.close()
            conn.close()
    
    except Exception as e:
        print(f"Error al conectar a la base de datos: {e}")
        return False


if __name__ == "__main__":
    success = migrate()
    sys.exit(0 if success else 1)
"""
Script para añadir las columnas status_start_date, status_end_date y status_notes
a la tabla 'employees' que faltan en la base de datos.
"""
import os
import psycopg2
from psycopg2 import sql

def add_missing_columns():
    """
    Añade las columnas status_start_date, status_end_date y status_notes
    a la tabla 'employees' si no existen.
    """
    # Obtener la URL de la base de datos desde la variable de entorno
    database_url = os.environ.get('DATABASE_URL')
    
    if not database_url:
        print("ERROR: La variable de entorno DATABASE_URL no está configurada.")
        return False
    
    conn = None
    try:
        # Conectar a la base de datos
        print("Conectando a la base de datos...")
        conn = psycopg2.connect(database_url)
        cursor = conn.cursor()
        
        # Verificar si las columnas ya existen
        print("Verificando columnas existentes...")
        cursor.execute("""
            SELECT column_name 
            FROM information_schema.columns 
            WHERE table_name = 'employees' AND 
            (column_name = 'status_start_date' OR 
             column_name = 'status_end_date' OR 
             column_name = 'status_notes');
        """)
        
        existing_columns = [row[0] for row in cursor.fetchall()]
        print(f"Columnas existentes: {existing_columns}")
        
        # Añadir columnas que faltan
        if 'status_start_date' not in existing_columns:
            print("Añadiendo columna 'status_start_date'...")
            cursor.execute("""
                ALTER TABLE employees 
                ADD COLUMN status_start_date DATE;
            """)
        
        if 'status_end_date' not in existing_columns:
            print("Añadiendo columna 'status_end_date'...")
            cursor.execute("""
                ALTER TABLE employees 
                ADD COLUMN status_end_date DATE;
            """)
        
        if 'status_notes' not in existing_columns:
            print("Añadiendo columna 'status_notes'...")
            cursor.execute("""
                ALTER TABLE employees 
                ADD COLUMN status_notes TEXT;
            """)
        
        # Confirmar los cambios
        conn.commit()
        print("✅ Migración completada correctamente.")
        return True
        
    except Exception as e:
        print(f"ERROR al añadir columnas: {str(e)}")
        if conn:
            conn.rollback()
        return False
        
    finally:
        if conn:
            conn.close()

if __name__ == "__main__":
    add_missing_columns()
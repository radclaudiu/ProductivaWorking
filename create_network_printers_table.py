#!/usr/bin/env python3
"""
Script para crear la tabla de impresoras de red en la base de datos.
Esta tabla permitirá configurar y gestionar impresoras Brother 
con conectividad WiFi/red para imprimir etiquetas de productos.
"""
import os
import sys
from datetime import datetime
from sqlalchemy import create_engine, inspect
from sqlalchemy.exc import SQLAlchemyError

def get_db_connection():
    """Establece conexión con la base de datos."""
    db_url = os.environ.get('DATABASE_URL')
    if not db_url:
        print("Error: No se ha definido la variable de entorno DATABASE_URL.")
        sys.exit(1)
    
    try:
        from sqlalchemy import text
        engine = create_engine(db_url)
        conn = engine.connect()
        return conn, engine
    except SQLAlchemyError as e:
        print(f"Error al conectar a la base de datos: {e}")
        sys.exit(1)

def check_table_exists(conn, table_name):
    """Verifica si una tabla existe en la base de datos."""
    inspector = inspect(conn.engine)
    return table_name in inspector.get_table_names()

def create_network_printers_table(conn):
    """Crea la tabla network_printers si no existe."""
    if check_table_exists(conn, "network_printers"):
        print("La tabla network_printers ya existe.")
        return
    
    try:
        conn.execute("""
        CREATE TABLE network_printers (
            id SERIAL PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            ip_address VARCHAR(50) NOT NULL,
            model VARCHAR(100),
            api_path VARCHAR(255) DEFAULT '/brother_d/printer/print',
            port INTEGER DEFAULT 80,
            requires_auth BOOLEAN DEFAULT FALSE,
            username VARCHAR(100),
            password VARCHAR(100),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            is_default BOOLEAN DEFAULT FALSE,
            is_active BOOLEAN DEFAULT TRUE,
            last_status VARCHAR(50),
            last_status_check TIMESTAMP,
            location_id INTEGER REFERENCES locations(id) ON DELETE CASCADE
        );
        """)
        print("Tabla network_printers creada correctamente.")
    except SQLAlchemyError as e:
        print(f"Error al crear la tabla network_printers: {e}")
        sys.exit(1)

def main():
    """Función principal que ejecuta la creación de la tabla."""
    conn, engine = get_db_connection()
    try:
        # Crear la tabla de impresoras de red
        create_network_printers_table(conn)
        
        print("¡Proceso completado correctamente!")
    except Exception as e:
        print(f"Error inesperado: {e}")
    finally:
        conn.close()
        engine.dispose()

if __name__ == "__main__":
    main()
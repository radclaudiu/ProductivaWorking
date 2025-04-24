"""
Script para crear las tablas necesarias para el módulo de Arqueos de Caja usando SQL directo.

Este script ejecuta sentencias SQL directamente para crear las tablas requeridas.
"""

import os
import sys
import logging
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

# Configurar logging
logging.basicConfig(level=logging.INFO, format='%(levelname)s: %(message)s')
logger = logging.getLogger(__name__)

def get_db_connection():
    """Establece conexión con la base de datos."""
    try:
        # Obtener URL de conexión desde variables de entorno
        DATABASE_URL = os.environ.get('DATABASE_URL')
        
        if not DATABASE_URL:
            logger.error("No se encontró la variable de entorno DATABASE_URL")
            return None
            
        # Conectar a la base de datos
        conn = psycopg2.connect(DATABASE_URL)
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        
        return conn
    except Exception as e:
        logger.error(f"Error al conectar a la base de datos: {str(e)}")
        return None

def execute_query(conn, query, params=None):
    """Ejecuta una consulta SQL."""
    try:
        cursor = conn.cursor()
        if params:
            cursor.execute(query, params)
        else:
            cursor.execute(query)
        cursor.close()
        return True
    except Exception as e:
        logger.error(f"Error ejecutando consulta: {str(e)}")
        logger.error(f"Query: {query}")
        return False

def check_column_exists(conn, table_name, column_name):
    """Verifica si una columna existe en una tabla."""
    query = """
    SELECT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = %s AND column_name = %s
    )
    """
    cursor = conn.cursor()
    cursor.execute(query, (table_name, column_name))
    exists = cursor.fetchone()[0]
    cursor.close()
    return exists

def check_table_exists(conn, table_name):
    """Verifica si una tabla existe en la base de datos."""
    query = """
    SELECT EXISTS (
        SELECT 1 
        FROM information_schema.tables 
        WHERE table_name = %s
    )
    """
    cursor = conn.cursor()
    cursor.execute(query, (table_name,))
    exists = cursor.fetchone()[0]
    cursor.close()
    return exists

def add_hourly_employee_cost(conn):
    """Añade el campo hourly_employee_cost a la tabla companies si no existe."""
    if check_table_exists(conn, 'companies'):
        if not check_column_exists(conn, 'companies', 'hourly_employee_cost'):
            logger.info("Añadiendo campo hourly_employee_cost a la tabla companies...")
            query = "ALTER TABLE companies ADD COLUMN hourly_employee_cost FLOAT DEFAULT 12.0"
            if execute_query(conn, query):
                logger.info("✓ Campo hourly_employee_cost añadido correctamente")
                return True
        else:
            logger.info("El campo hourly_employee_cost ya existe en la tabla companies")
            return True
    else:
        logger.error("La tabla companies no existe en la base de datos")
        return False
    return False

def create_cash_registers_table(conn):
    """Crea la tabla cash_registers si no existe."""
    if not check_table_exists(conn, 'cash_registers'):
        logger.info("Creando tabla cash_registers...")
        query = """
        CREATE TABLE cash_registers (
            id SERIAL PRIMARY KEY,
            date DATE NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            total_amount FLOAT NOT NULL DEFAULT 0.0,
            cash_amount FLOAT NOT NULL DEFAULT 0.0,
            card_amount FLOAT NOT NULL DEFAULT 0.0,
            delivery_cash_amount FLOAT NOT NULL DEFAULT 0.0,
            delivery_online_amount FLOAT NOT NULL DEFAULT 0.0,
            check_amount FLOAT NOT NULL DEFAULT 0.0,
            expenses_amount FLOAT NOT NULL DEFAULT 0.0,
            expenses_notes TEXT,
            notes TEXT,
            is_confirmed BOOLEAN DEFAULT FALSE,
            confirmed_at TIMESTAMP,
            confirmed_by_id INTEGER REFERENCES users(id),
            company_id INTEGER NOT NULL REFERENCES companies(id),
            created_by_id INTEGER REFERENCES users(id),
            employee_id INTEGER REFERENCES employees(id),
            employee_name VARCHAR(100),
            CONSTRAINT uq_company_date UNIQUE (company_id, date)
        )
        """
        if execute_query(conn, query):
            # Crear índices
            execute_query(conn, "CREATE INDEX idx_cash_register_company ON cash_registers (company_id)")
            execute_query(conn, "CREATE INDEX idx_cash_register_date ON cash_registers (date)")
            logger.info("✓ Tabla cash_registers creada correctamente")
            return True
    else:
        logger.info("La tabla cash_registers ya existe")
        return True
    return False

def create_cash_register_summaries_table(conn):
    """Crea la tabla cash_register_summaries si no existe."""
    if not check_table_exists(conn, 'cash_register_summaries'):
        logger.info("Creando tabla cash_register_summaries...")
        query = """
        CREATE TABLE cash_register_summaries (
            id SERIAL PRIMARY KEY,
            year INTEGER NOT NULL,
            month INTEGER NOT NULL,
            week_number INTEGER NOT NULL,
            weekly_total FLOAT NOT NULL DEFAULT 0.0,
            monthly_total FLOAT NOT NULL DEFAULT 0.0,
            yearly_total FLOAT NOT NULL DEFAULT 0.0,
            weekly_cash FLOAT NOT NULL DEFAULT 0.0,
            weekly_card FLOAT NOT NULL DEFAULT 0.0,
            weekly_delivery_cash FLOAT NOT NULL DEFAULT 0.0,
            weekly_delivery_online FLOAT NOT NULL DEFAULT 0.0,
            weekly_check FLOAT NOT NULL DEFAULT 0.0,
            weekly_expenses FLOAT NOT NULL DEFAULT 0.0,
            weekly_staff_cost FLOAT NOT NULL DEFAULT 0.0,
            monthly_staff_cost FLOAT NOT NULL DEFAULT 0.0,
            weekly_staff_cost_percentage FLOAT NOT NULL DEFAULT 0.0,
            monthly_staff_cost_percentage FLOAT NOT NULL DEFAULT 0.0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            company_id INTEGER NOT NULL REFERENCES companies(id),
            CONSTRAINT uq_summary_period UNIQUE (company_id, year, month, week_number)
        )
        """
        if execute_query(conn, query):
            # Crear índices
            execute_query(conn, "CREATE INDEX idx_summary_company ON cash_register_summaries (company_id)")
            execute_query(conn, "CREATE INDEX idx_summary_year_month ON cash_register_summaries (year, month)")
            logger.info("✓ Tabla cash_register_summaries creada correctamente")
            return True
    else:
        logger.info("La tabla cash_register_summaries ya existe")
        return True
    return False

def create_cash_register_tokens_table(conn):
    """Crea la tabla cash_register_tokens si no existe."""
    if not check_table_exists(conn, 'cash_register_tokens'):
        logger.info("Creando tabla cash_register_tokens...")
        query = """
        CREATE TABLE cash_register_tokens (
            id SERIAL PRIMARY KEY,
            token VARCHAR(64) UNIQUE NOT NULL,
            is_active BOOLEAN DEFAULT TRUE,
            expires_at TIMESTAMP,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            used_at TIMESTAMP,
            company_id INTEGER NOT NULL REFERENCES companies(id),
            created_by_id INTEGER REFERENCES users(id),
            employee_id INTEGER REFERENCES employees(id),
            cash_register_id INTEGER REFERENCES cash_registers(id)
        )
        """
        if execute_query(conn, query):
            logger.info("✓ Tabla cash_register_tokens creada correctamente")
            return True
    else:
        logger.info("La tabla cash_register_tokens ya existe")
        return True
    return False

def main():
    """Función principal que ejecuta todas las operaciones de creación de tablas."""
    logger.info("Iniciando creación de tablas para el módulo de Arqueos de Caja...")
    
    conn = get_db_connection()
    if not conn:
        logger.error("No se pudo establecer conexión con la base de datos")
        return False
    
    try:
        # Añadir campo hourly_employee_cost a la tabla companies
        add_hourly_employee_cost(conn)
        
        # Crear tablas en orden
        create_cash_registers_table(conn)
        create_cash_register_summaries_table(conn)
        create_cash_register_tokens_table(conn)
        
        logger.info("✓ Proceso de creación de tablas completado")
        return True
    finally:
        conn.close()

if __name__ == "__main__":
    main()
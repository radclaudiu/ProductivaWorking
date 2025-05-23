#!/usr/bin/env python
"""
Script para añadir campos adicionales a la tabla monthly_expenses para soportar 
la funcionalidad de envío de gastos por empleados y fechas específicas.
"""

import logging
import os
import sys
from datetime import datetime
from sqlalchemy import create_engine, text
from sqlalchemy.exc import SQLAlchemyError

# Configuración de logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def add_missing_columns():
    """
    Añade las columnas necesarias para la tabla monthly_expenses:
    - expense_date: Fecha específica del gasto (formato VARCHAR para mantener consistencia)
    - submitted_by_employee: Indica si el gasto fue enviado por un empleado
    - employee_name: Nombre del empleado que envió el gasto
    - receipt_image: Ruta a la imagen del recibo/factura
    """
    try:
        # Conectar a la base de datos
        db_url = os.environ.get('DATABASE_URL')
        if not db_url:
            logger.error("No se encontró la variable de entorno DATABASE_URL")
            sys.exit(1)
            
        engine = create_engine(db_url)
        conn = engine.connect()
        
        # Verificar columnas existentes
        columns_query = text("""
            SELECT column_name 
            FROM information_schema.columns 
            WHERE table_name = 'monthly_expenses'
        """)
        existing_columns = [row[0] for row in conn.execute(columns_query)]
        
        # Añadir columnas si no existen
        if 'expense_date' not in existing_columns:
            logger.info("Añadiendo columna 'expense_date'...")
            conn.execute(text("""
                ALTER TABLE monthly_expenses 
                ADD COLUMN expense_date VARCHAR(20)
            """))
            
        if 'submitted_by_employee' not in existing_columns:
            logger.info("Añadiendo columna 'submitted_by_employee'...")
            conn.execute(text("""
                ALTER TABLE monthly_expenses 
                ADD COLUMN submitted_by_employee BOOLEAN DEFAULT FALSE
            """))
            
        if 'employee_name' not in existing_columns:
            logger.info("Añadiendo columna 'employee_name'...")
            conn.execute(text("""
                ALTER TABLE monthly_expenses 
                ADD COLUMN employee_name VARCHAR(100)
            """))
            
        if 'receipt_image' not in existing_columns:
            logger.info("Añadiendo columna 'receipt_image'...")
            conn.execute(text("""
                ALTER TABLE monthly_expenses 
                ADD COLUMN receipt_image VARCHAR(255)
            """))
            
        # Confirmar cambios
        conn.commit()
        logger.info("Migración completada exitosamente.")
        
    except SQLAlchemyError as e:
        logger.error(f"Error al ejecutar la migración: {str(e)}")
        sys.exit(1)
    finally:
        if 'conn' in locals():
            conn.close()

if __name__ == "__main__":
    add_missing_columns()
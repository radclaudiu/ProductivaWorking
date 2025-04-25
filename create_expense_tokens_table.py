"""
Script para crear la tabla de tokens de gastos y actualizar la tabla de gastos mensuales.

Este script añade la tabla para tokens de envío de gastos por empleados y
actualiza la tabla de gastos mensuales con campos para gastos enviados por empleados.
"""

import os
import logging
import datetime
from app import db, create_app
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy import text, inspect

from models_monthly_expenses import MonthlyExpenseToken

# Configurar logging
logging.basicConfig(level=logging.INFO, 
                    format='%(levelname)s:%(name)s:%(message)s')
logger = logging.getLogger('create_expense_tokens_table')

def check_table_exists(table_name):
    """Verifica si una tabla existe en la base de datos."""
    try:
        with db.engine.connect() as conn:
            result = conn.execute(text(f"SELECT to_regclass('public.{table_name}')"))
            exists = result.scalar() is not None
            return exists
    except SQLAlchemyError as e:
        logger.error(f"Error al verificar si existe la tabla {table_name}: {e}")
        return False

def check_column_exists(table_name, column_name):
    """Verifica si una columna existe en una tabla."""
    inspector = inspect(db.engine)
    columns = [c['name'] for c in inspector.get_columns(table_name)]
    return column_name in columns

def create_expense_tokens_table():
    """Crea la tabla de tokens de gastos mensuales si no existe."""
    if not check_table_exists('monthly_expense_tokens'):
        try:
            MonthlyExpenseToken.__table__.create(db.engine)
            logger.info("✓ Tabla monthly_expense_tokens creada correctamente")
            return True
        except SQLAlchemyError as e:
            logger.error(f"✗ Error al crear la tabla monthly_expense_tokens: {e}")
            return False
    else:
        logger.info("⚠ La tabla monthly_expense_tokens ya existe")
        return True

def add_employee_fields_to_monthly_expenses():
    """Añade campos para gastos enviados por empleados a la tabla monthly_expenses."""
    if check_table_exists('monthly_expenses'):
        try:
            with db.engine.connect() as conn:
                # Añadir campo expense_date si no existe
                if not check_column_exists('monthly_expenses', 'expense_date'):
                    conn.execute(text("""
                        ALTER TABLE monthly_expenses 
                        ADD COLUMN expense_date VARCHAR(20)
                    """))
                    logger.info("✓ Campo expense_date añadido a la tabla monthly_expenses")
                else:
                    logger.info("⚠ El campo expense_date ya existe en la tabla monthly_expenses")
                
                # Añadir campo submitted_by_employee si no existe
                if not check_column_exists('monthly_expenses', 'submitted_by_employee'):
                    conn.execute(text("""
                        ALTER TABLE monthly_expenses 
                        ADD COLUMN submitted_by_employee BOOLEAN DEFAULT FALSE
                    """))
                    logger.info("✓ Campo submitted_by_employee añadido a la tabla monthly_expenses")
                else:
                    logger.info("⚠ El campo submitted_by_employee ya existe en la tabla monthly_expenses")
                
                # Añadir campo employee_name si no existe
                if not check_column_exists('monthly_expenses', 'employee_name'):
                    conn.execute(text("""
                        ALTER TABLE monthly_expenses 
                        ADD COLUMN employee_name VARCHAR(100)
                    """))
                    logger.info("✓ Campo employee_name añadido a la tabla monthly_expenses")
                else:
                    logger.info("⚠ El campo employee_name ya existe en la tabla monthly_expenses")
                
                # Añadir campo receipt_image si no existe
                if not check_column_exists('monthly_expenses', 'receipt_image'):
                    conn.execute(text("""
                        ALTER TABLE monthly_expenses 
                        ADD COLUMN receipt_image VARCHAR(255)
                    """))
                    logger.info("✓ Campo receipt_image añadido a la tabla monthly_expenses")
                else:
                    logger.info("⚠ El campo receipt_image ya existe en la tabla monthly_expenses")
                
                conn.commit()
                return True
        except SQLAlchemyError as e:
            logger.error(f"✗ Error al añadir campos a la tabla monthly_expenses: {e}")
            return False
    else:
        logger.error("✗ La tabla monthly_expenses no existe")
        return False

def main():
    """Función principal que ejecuta todas las operaciones."""
    logger.info("===== INICIANDO CREACIÓN DE TABLAS PARA SISTEMA DE TOKENS DE GASTOS =====")
    
    # Crear tabla de tokens de gastos
    if create_expense_tokens_table():
        logger.info("✅ Tabla de tokens de gastos creada o ya existente")
    else:
        logger.error("❌ No se pudo crear la tabla de tokens de gastos")
        return False
    
    # Añadir campos para gastos enviados por empleados
    if add_employee_fields_to_monthly_expenses():
        logger.info("✅ Campos para gastos enviados por empleados añadidos correctamente")
    else:
        logger.error("❌ No se pudieron añadir campos para gastos enviados por empleados")
        return False
    
    logger.info("===== CREACIÓN DE TABLAS PARA SISTEMA DE TOKENS DE GASTOS COMPLETADA =====")
    return True

if __name__ == "__main__":
    app = create_app()
    with app.app_context():
        success = main()
        if success:
            print("✅ Creación de tablas para sistema de tokens de gastos completada con éxito")
        else:
            print("❌ Se produjeron errores durante la creación de tablas")
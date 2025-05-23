"""
Script para crear las tablas necesarias para el módulo de gastos mensuales.

Este script crea las tablas en la base de datos y genera categorías predeterminadas.
"""

import os
import logging
import datetime
from app import db, create_app
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy import text, inspect

from models_monthly_expenses import ExpenseCategory, FixedExpense, MonthlyExpense, MonthlyExpenseSummary

# Configurar logging
logging.basicConfig(level=logging.INFO, 
                    format='%(levelname)s:%(name)s:%(message)s')
logger = logging.getLogger('create_monthly_expenses_tables')

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

def create_expense_categories_table():
    """Crea la tabla de categorías de gastos si no existe."""
    if not check_table_exists('expense_categories'):
        try:
            ExpenseCategory.__table__.create(db.engine)
            logger.info("✓ Tabla expense_categories creada correctamente")
            return True
        except SQLAlchemyError as e:
            logger.error(f"✗ Error al crear la tabla expense_categories: {e}")
            return False
    else:
        logger.info("⚠ La tabla expense_categories ya existe")
        return True

def create_fixed_expenses_table():
    """Crea la tabla de gastos fijos si no existe."""
    if not check_table_exists('fixed_expenses'):
        try:
            FixedExpense.__table__.create(db.engine)
            logger.info("✓ Tabla fixed_expenses creada correctamente")
            return True
        except SQLAlchemyError as e:
            logger.error(f"✗ Error al crear la tabla fixed_expenses: {e}")
            return False
    else:
        logger.info("⚠ La tabla fixed_expenses ya existe")
        return True

def create_monthly_expenses_table():
    """Crea la tabla de gastos mensuales si no existe."""
    if not check_table_exists('monthly_expenses'):
        try:
            MonthlyExpense.__table__.create(db.engine)
            logger.info("✓ Tabla monthly_expenses creada correctamente")
            return True
        except SQLAlchemyError as e:
            logger.error(f"✗ Error al crear la tabla monthly_expenses: {e}")
            return False
    else:
        logger.info("⚠ La tabla monthly_expenses ya existe")
        return True

def create_monthly_expense_summaries_table():
    """Crea la tabla de resúmenes de gastos mensuales si no existe."""
    if not check_table_exists('monthly_expense_summaries'):
        try:
            MonthlyExpenseSummary.__table__.create(db.engine)
            logger.info("✓ Tabla monthly_expense_summaries creada correctamente")
            return True
        except SQLAlchemyError as e:
            logger.error(f"✗ Error al crear la tabla monthly_expense_summaries: {e}")
            return False
    else:
        logger.info("⚠ La tabla monthly_expense_summaries ya existe")
        return True

def create_default_expense_categories():
    """Crea categorías de gastos predeterminadas si no existen."""
    # Verificar si ya existen categorías del sistema
    system_categories_count = ExpenseCategory.query.filter_by(is_system=True).count()
    
    if system_categories_count == 0:
        # Lista de categorías predeterminadas
        default_categories = [
            {
                'name': 'Alquiler',
                'description': 'Gastos relacionados con el alquiler del local o espacio de trabajo',
                'is_system': True
            },
            {
                'name': 'Suministros',
                'description': 'Gastos de electricidad, agua, gas, internet, teléfono, etc.',
                'is_system': True
            },
            {
                'name': 'Nóminas',
                'description': 'Salarios y pagos a empleados',
                'is_system': True
            },
            {
                'name': 'Impuestos',
                'description': 'Pagos de impuestos y tasas municipales',
                'is_system': True
            },
            {
                'name': 'Seguros',
                'description': 'Seguros del local, responsabilidad civil, etc.',
                'is_system': True
            },
            {
                'name': 'Proveedores',
                'description': 'Pagos a proveedores de productos o servicios',
                'is_system': True
            },
            {
                'name': 'Marketing',
                'description': 'Gastos de publicidad, promociones, etc.',
                'is_system': True
            },
            {
                'name': 'Mantenimiento',
                'description': 'Reparaciones y mantenimiento del local o equipos',
                'is_system': True
            },
            {
                'name': 'Software',
                'description': 'Licencias y servicios de software',
                'is_system': True
            },
            {
                'name': 'Otros',
                'description': 'Otros gastos no categorizados',
                'is_system': True
            }
        ]
        
        # Crear las categorías predeterminadas
        try:
            for category_data in default_categories:
                category = ExpenseCategory(**category_data)
                db.session.add(category)
            
            db.session.commit()
            logger.info(f"✓ {len(default_categories)} categorías predeterminadas creadas correctamente")
            return True
        except SQLAlchemyError as e:
            db.session.rollback()
            logger.error(f"✗ Error al crear categorías predeterminadas: {e}")
            return False
    else:
        logger.info(f"⚠ Ya existen {system_categories_count} categorías del sistema")
        return True

def main():
    """Función principal que ejecuta todas las operaciones de creación de tablas."""
    logger.info("===== INICIANDO CREACIÓN DE TABLAS DE GASTOS MENSUALES =====")
    
    # Crear tablas en el orden correcto
    if create_expense_categories_table():
        logger.info("✅ Tabla de categorías de gastos creada o ya existente")
    else:
        logger.error("❌ No se pudo crear la tabla de categorías de gastos")
        return False
    
    if create_fixed_expenses_table():
        logger.info("✅ Tabla de gastos fijos creada o ya existente")
    else:
        logger.error("❌ No se pudo crear la tabla de gastos fijos")
        return False
    
    if create_monthly_expenses_table():
        logger.info("✅ Tabla de gastos mensuales creada o ya existente")
    else:
        logger.error("❌ No se pudo crear la tabla de gastos mensuales")
        return False
    
    if create_monthly_expense_summaries_table():
        logger.info("✅ Tabla de resúmenes de gastos mensuales creada o ya existente")
    else:
        logger.error("❌ No se pudo crear la tabla de resúmenes de gastos mensuales")
        return False
    
    # Crear categorías predeterminadas
    if create_default_expense_categories():
        logger.info("✅ Categorías predeterminadas creadas o ya existentes")
    else:
        logger.error("❌ No se pudieron crear las categorías predeterminadas")
        return False
    
    logger.info("===== CREACIÓN DE TABLAS DE GASTOS MENSUALES COMPLETADA =====")
    return True

if __name__ == "__main__":
    app = create_app()
    with app.app_context():
        success = main()
        if success:
            print("✅ Creación de tablas de gastos mensuales completada con éxito")
        else:
            print("❌ Se produjeron errores durante la creación de tablas")
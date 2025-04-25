"""
Script para crear las tablas necesarias para el módulo de gastos mensuales.

Este script crea las tablas en la base de datos y genera categorías predeterminadas.
"""

import logging
from datetime import datetime
from app import app, db
from models_monthly_expenses import ExpenseCategory, FixedExpense, MonthlyExpense, MonthlyExpenseSummary
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy import inspect

# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def check_table_exists(table_name):
    """Verifica si una tabla existe en la base de datos."""
    with app.app_context():
        inspector = inspect(db.engine)
        return table_name in inspector.get_table_names()

def create_expense_categories_table():
    """Crea la tabla de categorías de gastos si no existe."""
    if not check_table_exists('expense_categories'):
        logger.info("Creando tabla 'expense_categories'...")
        try:
            with app.app_context():
                db.create_all(tables=[ExpenseCategory.__table__])
            logger.info("Tabla 'expense_categories' creada correctamente.")
            return True
        except SQLAlchemyError as e:
            logger.error(f"Error al crear tabla 'expense_categories': {str(e)}")
            return False
    else:
        logger.info("La tabla 'expense_categories' ya existe.")
        return True

def create_fixed_expenses_table():
    """Crea la tabla de gastos fijos si no existe."""
    if not check_table_exists('fixed_expenses'):
        logger.info("Creando tabla 'fixed_expenses'...")
        try:
            with app.app_context():
                db.create_all(tables=[FixedExpense.__table__])
            logger.info("Tabla 'fixed_expenses' creada correctamente.")
            return True
        except SQLAlchemyError as e:
            logger.error(f"Error al crear tabla 'fixed_expenses': {str(e)}")
            return False
    else:
        logger.info("La tabla 'fixed_expenses' ya existe.")
        return True

def create_monthly_expenses_table():
    """Crea la tabla de gastos mensuales si no existe."""
    if not check_table_exists('monthly_expenses'):
        logger.info("Creando tabla 'monthly_expenses'...")
        try:
            with app.app_context():
                db.create_all(tables=[MonthlyExpense.__table__])
            logger.info("Tabla 'monthly_expenses' creada correctamente.")
            return True
        except SQLAlchemyError as e:
            logger.error(f"Error al crear tabla 'monthly_expenses': {str(e)}")
            return False
    else:
        logger.info("La tabla 'monthly_expenses' ya existe.")
        return True

def create_monthly_expense_summaries_table():
    """Crea la tabla de resúmenes de gastos mensuales si no existe."""
    if not check_table_exists('monthly_expense_summaries'):
        logger.info("Creando tabla 'monthly_expense_summaries'...")
        try:
            with app.app_context():
                db.create_all(tables=[MonthlyExpenseSummary.__table__])
            logger.info("Tabla 'monthly_expense_summaries' creada correctamente.")
            return True
        except SQLAlchemyError as e:
            logger.error(f"Error al crear tabla 'monthly_expense_summaries': {str(e)}")
            return False
    else:
        logger.info("La tabla 'monthly_expense_summaries' ya existe.")
        return True

def create_default_expense_categories():
    """Crea categorías de gastos predeterminadas si no existen."""
    default_categories = [
        {"name": "Alquiler", "description": "Alquiler de local o instalaciones", "is_system": True},
        {"name": "Agua", "description": "Consumo de agua", "is_system": True},
        {"name": "Luz", "description": "Consumo de electricidad", "is_system": True},
        {"name": "Gas", "description": "Consumo de gas", "is_system": True},
        {"name": "Teléfono", "description": "Telefonía e internet", "is_system": True},
        {"name": "Seguros", "description": "Seguros de local, responsabilidad civil, etc.", "is_system": True},
        {"name": "Asesoría", "description": "Servicios de gestoría, contabilidad y asesoría", "is_system": True},
        {"name": "Comisiones bancarias", "description": "Comisiones de TPV y servicios bancarios", "is_system": True},
        {"name": "Bebidas", "description": "Compra de bebidas", "is_system": True},
        {"name": "Comida", "description": "Compra de alimentos", "is_system": True},
        {"name": "Impuestos", "description": "Impuestos municipales, estatales, etc.", "is_system": True},
        {"name": "Mantenimiento", "description": "Reparaciones y mantenimiento", "is_system": True},
        {"name": "Personal", "description": "Gastos relacionados con el personal", "is_system": True},
        {"name": "Limpieza", "description": "Productos y servicios de limpieza", "is_system": True},
        {"name": "Otros", "description": "Otros gastos no clasificados", "is_system": True}
    ]
    
    with app.app_context():
        try:
            # Verificar si ya existen categorías del sistema
            existing_system_categories = ExpenseCategory.query.filter_by(is_system=True).count()
            
            if existing_system_categories > 0:
                logger.info(f"Ya existen {existing_system_categories} categorías predeterminadas.")
                return True
            
            # Crear categorías predeterminadas
            for category_data in default_categories:
                category = ExpenseCategory(**category_data)
                db.session.add(category)
            
            db.session.commit()
            logger.info(f"Se crearon {len(default_categories)} categorías predeterminadas.")
            return True
            
        except SQLAlchemyError as e:
            db.session.rollback()
            logger.error(f"Error al crear categorías predeterminadas: {str(e)}")
            return False

def main():
    """Función principal que ejecuta todas las operaciones de creación de tablas."""
    logger.info("Iniciando creación de tablas para el módulo de gastos mensuales...")
    
    # Crear las tablas en orden correcto (respetando dependencias)
    if not create_expense_categories_table():
        logger.error("No se pudo continuar con la creación de tablas.")
        return False
    
    if not create_fixed_expenses_table():
        logger.error("No se pudo continuar con la creación de tablas.")
        return False
    
    if not create_monthly_expenses_table():
        logger.error("No se pudo continuar con la creación de tablas.")
        return False
    
    if not create_monthly_expense_summaries_table():
        logger.error("No se pudo continuar con la creación de tablas.")
        return False
    
    # Crear datos predeterminados
    if not create_default_expense_categories():
        logger.error("No se pudieron crear las categorías predeterminadas.")
        return False
    
    logger.info("Creación de tablas para el módulo de gastos mensuales completada con éxito.")
    return True

if __name__ == "__main__":
    main()
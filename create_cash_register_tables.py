"""
Script para crear las tablas necesarias para el módulo de Arqueos de Caja.

Este script crea directamente las tablas en la base de datos sin usar migraciones.
También añade el campo hourly_employee_cost a la tabla companies si no existe.
"""

import os
import sys
import logging
from datetime import datetime
from sqlalchemy import Column, Integer, Float, String, Boolean, Date, DateTime, Text, ForeignKey
from sqlalchemy import UniqueConstraint, Index, MetaData, Table, inspect
from sqlalchemy.sql import text

# Configurar logging
logging.basicConfig(level=logging.INFO, format='%(levelname)s: %(message)s')
logger = logging.getLogger(__name__)

# Importar la aplicación
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from app import app, db

def check_column_exists(table_name, column_name):
    """Verifica si una columna existe en una tabla."""
    inspector = inspect(db.engine)
    columns = inspector.get_columns(table_name)
    return any(column['name'] == column_name for column in columns)

def check_table_exists(table_name):
    """Verifica si una tabla existe en la base de datos."""
    inspector = inspect(db.engine)
    return table_name in inspector.get_table_names()

def add_hourly_employee_cost():
    """Añade el campo hourly_employee_cost a la tabla companies si no existe."""
    if check_table_exists('companies') and not check_column_exists('companies', 'hourly_employee_cost'):
        logger.info("Añadiendo campo hourly_employee_cost a la tabla companies...")
        try:
            with db.engine.connect() as conn:
                conn.execute(text("ALTER TABLE companies ADD COLUMN hourly_employee_cost FLOAT DEFAULT 12.0"))
                conn.commit()
            logger.info("✓ Campo hourly_employee_cost añadido correctamente")
            return True
        except Exception as e:
            logger.error(f"❌ Error al añadir campo hourly_employee_cost: {str(e)}")
            return False
    else:
        logger.info("El campo hourly_employee_cost ya existe o la tabla companies no existe")
        return True

def create_cash_registers_table():
    """Crea la tabla cash_registers si no existe."""
    if not check_table_exists('cash_registers'):
        logger.info("Creando tabla cash_registers...")
        try:
            metadata = MetaData()
            Table('cash_registers', metadata,
                Column('id', Integer, primary_key=True),
                Column('date', Date, nullable=False),
                Column('created_at', DateTime, default=datetime.utcnow),
                Column('updated_at', DateTime, default=datetime.utcnow, onupdate=datetime.utcnow),
                Column('total_amount', Float, nullable=False, default=0.0),
                Column('cash_amount', Float, nullable=False, default=0.0),
                Column('card_amount', Float, nullable=False, default=0.0),
                Column('delivery_cash_amount', Float, nullable=False, default=0.0),
                Column('delivery_online_amount', Float, nullable=False, default=0.0),
                Column('check_amount', Float, nullable=False, default=0.0),
                Column('expenses_amount', Float, nullable=False, default=0.0),
                Column('expenses_notes', Text),
                Column('notes', Text),
                Column('is_confirmed', Boolean, default=False),
                Column('confirmed_at', DateTime),
                Column('confirmed_by_id', Integer, ForeignKey('users.id')),
                Column('company_id', Integer, ForeignKey('companies.id'), nullable=False),
                Column('created_by_id', Integer, ForeignKey('users.id')),
                Column('employee_id', Integer, ForeignKey('employees.id')),
                Column('employee_name', String(100)),
                UniqueConstraint('company_id', 'date', name='uq_company_date')
            )
            
            metadata.create_all(db.engine)
            
            # Crear índices
            with db.engine.connect() as conn:
                conn.execute(text("CREATE INDEX idx_cash_register_company ON cash_registers (company_id)"))
                conn.execute(text("CREATE INDEX idx_cash_register_date ON cash_registers (date)"))
                conn.commit()
                
            logger.info("✓ Tabla cash_registers creada correctamente")
            return True
        except Exception as e:
            logger.error(f"❌ Error al crear tabla cash_registers: {str(e)}")
            return False
    else:
        logger.info("La tabla cash_registers ya existe")
        return True

def create_cash_register_summaries_table():
    """Crea la tabla cash_register_summaries si no existe."""
    if not check_table_exists('cash_register_summaries'):
        logger.info("Creando tabla cash_register_summaries...")
        try:
            metadata = MetaData()
            Table('cash_register_summaries', metadata,
                Column('id', Integer, primary_key=True),
                Column('year', Integer, nullable=False),
                Column('month', Integer, nullable=False),
                Column('week_number', Integer, nullable=False),
                Column('weekly_total', Float, nullable=False, default=0.0),
                Column('monthly_total', Float, nullable=False, default=0.0),
                Column('yearly_total', Float, nullable=False, default=0.0),
                Column('weekly_cash', Float, nullable=False, default=0.0),
                Column('weekly_card', Float, nullable=False, default=0.0),
                Column('weekly_delivery_cash', Float, nullable=False, default=0.0),
                Column('weekly_delivery_online', Float, nullable=False, default=0.0),
                Column('weekly_check', Float, nullable=False, default=0.0),
                Column('weekly_expenses', Float, nullable=False, default=0.0),
                Column('weekly_staff_cost', Float, nullable=False, default=0.0),
                Column('monthly_staff_cost', Float, nullable=False, default=0.0),
                Column('weekly_staff_cost_percentage', Float, nullable=False, default=0.0),
                Column('monthly_staff_cost_percentage', Float, nullable=False, default=0.0),
                Column('created_at', DateTime, default=datetime.utcnow),
                Column('updated_at', DateTime, default=datetime.utcnow, onupdate=datetime.utcnow),
                Column('company_id', Integer, ForeignKey('companies.id'), nullable=False),
                UniqueConstraint('company_id', 'year', 'month', 'week_number', name='uq_summary_period')
            )
            
            metadata.create_all(db.engine)
            
            # Crear índices
            with db.engine.connect() as conn:
                conn.execute(text("CREATE INDEX idx_summary_company ON cash_register_summaries (company_id)"))
                conn.execute(text("CREATE INDEX idx_summary_year_month ON cash_register_summaries (year, month)"))
                conn.commit()
                
            logger.info("✓ Tabla cash_register_summaries creada correctamente")
            return True
        except Exception as e:
            logger.error(f"❌ Error al crear tabla cash_register_summaries: {str(e)}")
            return False
    else:
        logger.info("La tabla cash_register_summaries ya existe")
        return True

def create_cash_register_tokens_table():
    """Crea la tabla cash_register_tokens si no existe."""
    if not check_table_exists('cash_register_tokens'):
        logger.info("Creando tabla cash_register_tokens...")
        try:
            metadata = MetaData()
            Table('cash_register_tokens', metadata,
                Column('id', Integer, primary_key=True),
                Column('token', String(64), unique=True, nullable=False),
                Column('is_active', Boolean, default=True),
                Column('expires_at', DateTime),
                Column('created_at', DateTime, default=datetime.utcnow),
                Column('used_at', DateTime),
                Column('company_id', Integer, ForeignKey('companies.id'), nullable=False),
                Column('created_by_id', Integer, ForeignKey('users.id')),
                Column('employee_id', Integer, ForeignKey('employees.id')),
                Column('cash_register_id', Integer, ForeignKey('cash_registers.id'))
            )
            
            metadata.create_all(db.engine)
            logger.info("✓ Tabla cash_register_tokens creada correctamente")
            return True
        except Exception as e:
            logger.error(f"❌ Error al crear tabla cash_register_tokens: {str(e)}")
            return False
    else:
        logger.info("La tabla cash_register_tokens ya existe")
        return True

def main():
    """Función principal que ejecuta todas las operaciones de creación de tablas."""
    logger.info("Iniciando creación de tablas para el módulo de Arqueos de Caja...")
    
    with app.app_context():
        # Añadir campo hourly_employee_cost a la tabla companies
        add_hourly_employee_cost()
        
        # Crear tablas en orden
        create_cash_registers_table()
        create_cash_register_summaries_table()
        create_cash_register_tokens_table()
        
        logger.info("✓ Proceso de creación de tablas completado")

if __name__ == "__main__":
    main()
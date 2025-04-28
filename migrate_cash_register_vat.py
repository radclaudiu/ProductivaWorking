"""
Script de migración para añadir campos de IVA a los modelos de arqueos de caja.

Este script añade campos para el porcentaje de IVA, importe del IVA y importe neto
a las tablas de arqueos de caja y resúmenes acumulados.
"""

import os
import sys
import logging
from datetime import datetime

# Configurar logging
logging.basicConfig(level=logging.INFO, 
                   format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Añadir el directorio raíz al path para poder importar los módulos
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app import db, create_app
from models_cash_register import CashRegister, CashRegisterSummary

def migrate_tables():
    """
    Añade los campos de IVA a las tablas de arqueos de caja.
    """
    logger.info("Iniciando migración de tablas de arqueos para añadir campos de IVA...")
    
    app = create_app()
    with app.app_context():
        try:
            # Comprobar si las columnas ya existen
            inspector = db.inspect(db.engine)
            
            cash_register_columns = [col['name'] for col in inspector.get_columns('cash_registers')]
            cash_register_summary_columns = [col['name'] for col in inspector.get_columns('cash_register_summaries')]
            
            # Añadir columnas a cash_registers si no existen
            columns_to_add_cash_register = []
            
            if 'vat_percentage' not in cash_register_columns:
                columns_to_add_cash_register.append('vat_percentage FLOAT DEFAULT 21.0 NOT NULL')
                
            if 'vat_amount' not in cash_register_columns:
                columns_to_add_cash_register.append('vat_amount FLOAT DEFAULT 0.0 NOT NULL')
                
            if 'net_amount' not in cash_register_columns:
                columns_to_add_cash_register.append('net_amount FLOAT DEFAULT 0.0 NOT NULL')
            
            # Añadir columnas a cash_register_summaries si no existen
            columns_to_add_summary = []
            
            if 'weekly_vat_amount' not in cash_register_summary_columns:
                columns_to_add_summary.append('weekly_vat_amount FLOAT DEFAULT 0.0 NOT NULL')
                
            if 'weekly_net_amount' not in cash_register_summary_columns:
                columns_to_add_summary.append('weekly_net_amount FLOAT DEFAULT 0.0 NOT NULL')
                
            if 'monthly_vat_amount' not in cash_register_summary_columns:
                columns_to_add_summary.append('monthly_vat_amount FLOAT DEFAULT 0.0 NOT NULL')
                
            if 'monthly_net_amount' not in cash_register_summary_columns:
                columns_to_add_summary.append('monthly_net_amount FLOAT DEFAULT 0.0 NOT NULL')
            
            # Ejecutar la migración para cada tabla
            if columns_to_add_cash_register:
                with db.engine.connect() as conn:
                    for column_def in columns_to_add_cash_register:
                        conn.execute(f"ALTER TABLE cash_registers ADD COLUMN {column_def}")
                logger.info(f"Añadidas columnas a cash_registers: {', '.join(columns_to_add_cash_register)}")
            else:
                logger.info("No es necesario añadir columnas a cash_registers, ya existen")
                
            if columns_to_add_summary:
                with db.engine.connect() as conn:
                    for column_def in columns_to_add_summary:
                        conn.execute(f"ALTER TABLE cash_register_summaries ADD COLUMN {column_def}")
                logger.info(f"Añadidas columnas a cash_register_summaries: {', '.join(columns_to_add_summary)}")
            else:
                logger.info("No es necesario añadir columnas a cash_register_summaries, ya existen")
            
            # Actualizar registros existentes
            logger.info("Actualizando valores de IVA en registros existentes...")
            
            # Para cada arqueo, calcular el IVA (21% por defecto) y el importe neto
            cash_registers = CashRegister.query.all()
            for reg in cash_registers:
                # Por defecto, establecemos 21% de IVA
                reg.vat_percentage = 21.0
                # Calculamos el importe del IVA y el neto
                reg.vat_amount = round(reg.total_amount - (reg.total_amount / (1 + (reg.vat_percentage / 100))), 2)
                reg.net_amount = round(reg.total_amount - reg.vat_amount, 2)
            
            # Actualizar los resúmenes acumulados
            summaries = CashRegisterSummary.query.all()
            for summary in summaries:
                # Sumar todos los arqueos de la semana para esta empresa
                week_registers = CashRegister.query.filter(
                    CashRegister.company_id == summary.company_id,
                    db.extract('year', CashRegister.date) == summary.year,
                    db.extract('month', CashRegister.date) == summary.month,
                    db.extract('week', CashRegister.date) == summary.week_number
                ).all()
                
                summary.weekly_vat_amount = sum(reg.vat_amount for reg in week_registers)
                summary.weekly_net_amount = sum(reg.net_amount for reg in week_registers)
                
                # Sumar todos los arqueos del mes para esta empresa
                month_registers = CashRegister.query.filter(
                    CashRegister.company_id == summary.company_id,
                    db.extract('year', CashRegister.date) == summary.year,
                    db.extract('month', CashRegister.date) == summary.month
                ).all()
                
                summary.monthly_vat_amount = sum(reg.vat_amount for reg in month_registers)
                summary.monthly_net_amount = sum(reg.net_amount for reg in month_registers)
            
            # Guardar cambios
            db.session.commit()
            logger.info("Valores de IVA actualizados con éxito en todos los registros")
            
            logger.info("Migración completada con éxito")
            return True
            
        except Exception as e:
            db.session.rollback()
            logger.error(f"Error durante la migración: {e}")
            return False

if __name__ == "__main__":
    migrate_tables()
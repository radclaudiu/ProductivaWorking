"""
Script para migrar la tabla cash_registers eliminando la restricción única en company_id y date.

Este script elimina la restricción que permite solo un arqueo por empresa y fecha para permitir
múltiples arqueos por día.
"""

from flask import Flask
from sqlalchemy import create_engine, text
import os
import logging

# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Crear app Flask mínima para acceder a la configuración
app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = os.environ.get('DATABASE_URL')

def migrate_cash_register_constraint():
    """
    Eliminar la restricción única de la tabla cash_registers.
    """
    try:
        # Conectar a la base de datos
        engine = create_engine(app.config['SQLALCHEMY_DATABASE_URI'])
        conn = engine.connect()
        
        # Verificar si la restricción existe
        logger.info("Verificando si existe la restricción uq_company_date...")
        
        # Consulta para comprobar si existe la restricción
        check_query = text("""
            SELECT COUNT(*) FROM pg_constraint 
            WHERE conname = 'uq_company_date' 
            AND conrelid = 'cash_registers'::regclass;
        """)
        
        result = conn.execute(check_query)
        constraint_exists = result.scalar() > 0
        
        if constraint_exists:
            logger.info("La restricción uq_company_date existe. Eliminándola...")
            
            # Eliminar la restricción
            drop_query = text("ALTER TABLE cash_registers DROP CONSTRAINT IF EXISTS uq_company_date;")
            conn.execute(drop_query)
            
            logger.info("Restricción eliminada correctamente.")
        else:
            logger.info("La restricción uq_company_date no existe o ya ha sido eliminada.")
        
        conn.close()
        return True
        
    except Exception as e:
        logger.error(f"Error al migrar la restricción de cash_registers: {str(e)}")
        return False

if __name__ == "__main__":
    logger.info("Iniciando migración de la tabla cash_registers...")
    success = migrate_cash_register_constraint()
    
    if success:
        logger.info("Migración completada exitosamente.")
    else:
        logger.error("La migración ha fallado. Revisa los logs para más detalles.")
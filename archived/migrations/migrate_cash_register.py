"""
Script para aplicar la migración de las tablas de arqueos de caja.

Este script aplica la migración que añade el campo hourly_employee_cost a la tabla companies
y crea las tablas necesarias para el sistema de arqueos de caja.
"""

import os
import sys
import logging
from flask_migrate import Migrate, upgrade

# Configurar logging
logging.basicConfig(level=logging.INFO, format='%(levelname)s: %(message)s')
logger = logging.getLogger(__name__)

# Importar la aplicación
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from app import app, db

def run_migrations():
    """Ejecuta las migraciones pendientes."""
    logger.info("Aplicando migraciones del sistema de arqueos de caja...")
    
    try:
        # Inicializar el objeto Migrate con la aplicación y la base de datos
        migrate = Migrate(app, db)
        
        # Aplicar las migraciones pendientes
        with app.app_context():
            upgrade()
            
        logger.info("✓ Migraciones aplicadas correctamente")
        return True
    except Exception as e:
        logger.error(f"❌ Error al aplicar migraciones: {str(e)}")
        return False

if __name__ == "__main__":
    run_migrations()
#!/usr/bin/env python
"""
Script para añadir la columna 'requires_pin' a la tabla 'locations'.
Esta columna permite configurar si se requiere PIN para los empleados en el portal.
"""
import logging
from sqlalchemy import Column, Boolean
from sqlalchemy.exc import OperationalError, ProgrammingError

from app import db, create_app
from models_tasks import Location

# Configurar logging
logging.basicConfig(level=logging.INFO, 
                    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def add_requires_pin_column():
    """
    Añade la columna 'requires_pin' a la tabla 'locations' si no existe.
    Por defecto, se establece en True para mantener el comportamiento actual.
    """
    app = create_app()
    with app.app_context():
        connection = db.engine.connect()
        try:
            # Verificar si la columna ya existe
            inspector = db.inspect(db.engine)
            columns = [column['name'] for column in inspector.get_columns('locations')]
            
            if 'requires_pin' not in columns:
                logger.info("Añadiendo columna 'requires_pin' a la tabla 'locations'")
                
                # Añadir la columna a través de una sentencia SQL
                connection.execute('ALTER TABLE locations ADD COLUMN requires_pin BOOLEAN DEFAULT TRUE')
                
                # Actualizar todos los registros existentes para establecer requires_pin en True
                connection.execute('UPDATE locations SET requires_pin = TRUE')
                
                logger.info("Columna 'requires_pin' añadida correctamente")
            else:
                logger.info("La columna 'requires_pin' ya existe en la tabla 'locations'")
                
        except (OperationalError, ProgrammingError) as e:
            logger.error(f"Error al añadir columna: {str(e)}")
            return False
        finally:
            connection.close()
            
        return True

if __name__ == "__main__":
    if add_requires_pin_column():
        print("✓ Migración completada: Se ha añadido la columna 'requires_pin' a la tabla 'locations'")
    else:
        print("✗ Error al ejecutar la migración")
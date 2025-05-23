# Script para migrar la tabla network_printers añadiendo soporte para impresoras Raspberry Pi

import os
import sys
import logging
from datetime import datetime

import sqlalchemy as sa
from sqlalchemy import create_engine, MetaData, Table, Column, Enum, String
from sqlalchemy.orm import sessionmaker

# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def run_migration():
    try:
        # Obtener la URL de la base de datos desde la variable de entorno
        database_url = os.environ.get('DATABASE_URL')
        if not database_url:
            logger.error("La variable de entorno DATABASE_URL no está definida")
            return False

        # Crear motor de base de datos
        engine = create_engine(database_url)
        Session = sessionmaker(bind=engine)
        session = Session()

        # Crear metadata
        metadata = MetaData()
        metadata.bind = engine

        # Verificar si las columnas ya existen
        inspector = sa.inspect(engine)
        columns = inspector.get_columns('network_printers')
        column_names = [col['name'] for col in columns]

        # Lista de cambios realizados
        changes = []

        # Añadir columna printer_type si no existe
        if 'printer_type' not in column_names:
            logger.info("Añadiendo columna printer_type a la tabla network_printers")
            session.execute(sa.text(
                "ALTER TABLE network_printers ADD COLUMN printer_type VARCHAR(20) DEFAULT 'direct_network' NOT NULL"
            ))
            changes.append("Añadida columna printer_type")

        # Añadir columna usb_port si no existe
        if 'usb_port' not in column_names:
            logger.info("Añadiendo columna usb_port a la tabla network_printers")
            session.execute(sa.text(
                "ALTER TABLE network_printers ADD COLUMN usb_port VARCHAR(100)"
            ))
            changes.append("Añadida columna usb_port")

        # Comprobar si se realizaron cambios
        if changes:
            session.commit()
            logger.info("Migración completada con éxito. Cambios: %s", ", ".join(changes))
            print("Migración completada con éxito.")
            print("Cambios realizados:")
            for change in changes:
                print(f" - {change}")
        else:
            logger.info("No se necesitaron cambios en la tabla network_printers")
            print("No se necesitaron cambios. La estructura de la tabla ya está actualizada.")

        return True

    except Exception as e:
        logger.error(f"Error durante la migración: {str(e)}")
        if session:
            session.rollback()
        print(f"Error durante la migración: {str(e)}")
        return False

if __name__ == "__main__":
    print("Ejecutando migración para añadir soporte de impresoras Raspberry Pi...")
    success = run_migration()
    sys.exit(0 if success else 1)

"""
Script para inicializar las tablas de CreaTurno en la base de datos de Productiva.
Este script crea las tablas necesarias para integrar CreaTurno con Productiva.
"""

import os
import psycopg2
from flask import Flask
from sqlalchemy import text
from app import db, create_app
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def init_creaturno_tables():
    """
    Crea las tablas específicas de CreaTurno en la base de datos de Productiva.
    """
    try:
        # Inicializar la aplicación Flask para tener acceso a la base de datos
        app = create_app()
        
        # Leer el script SQL desde el archivo
        script_path = os.path.join('CreaTurno', 'create_tables_productiva.sql')
        
        with open(script_path, 'r') as file:
            sql_script = file.read()
            
        logger.info("Ejecutando script para crear tablas de CreaTurno")
        
        # Ejecutar el script SQL directamente con SQLAlchemy
        with app.app_context():
            db.session.execute(text(sql_script))
            db.session.commit()
            
        logger.info("Tablas de CreaTurno creadas correctamente")
        return True
        
    except Exception as e:
        logger.error(f"Error al crear tablas de CreaTurno: {str(e)}")
        return False

if __name__ == "__main__":
    success = init_creaturno_tables()
    if success:
        print("✅ Tablas de CreaTurno creadas correctamente")
    else:
        print("❌ Error al crear tablas de CreaTurno")
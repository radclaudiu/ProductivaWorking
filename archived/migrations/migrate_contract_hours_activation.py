"""
Script para añadir columnas use_normal_schedule y use_flexibility a la tabla employee_contract_hours
"""

import sys
import os
from datetime import datetime
from flask import Flask, current_app
from sqlalchemy import Column, Boolean, create_engine, text
from sqlalchemy.ext.declarative import declarative_base

# Añadir el directorio actual al path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

# Importar la aplicación
from app import create_app, db

def add_activation_columns():
    """Añade las columnas use_normal_schedule y use_flexibility a la tabla employee_contract_hours"""
    
    # Crear la aplicación
    app = create_app()
    
    # Comprobar si las columnas ya existen
    with app.app_context():
        engine = db.engine
        inspector = db.inspect(engine)
        columns = [c['name'] for c in inspector.get_columns('employee_contract_hours')]
        
        if 'use_normal_schedule' not in columns or 'use_flexibility' not in columns:
            print("Añadiendo nuevas columnas a la tabla employee_contract_hours...")
            
            try:
                # Añadir columna use_normal_schedule si no existe
                if 'use_normal_schedule' not in columns:
                    db.session.execute(text(
                        "ALTER TABLE employee_contract_hours ADD COLUMN use_normal_schedule BOOLEAN DEFAULT FALSE"
                    ))
                
                # Añadir columna use_flexibility si no existe
                if 'use_flexibility' not in columns:
                    db.session.execute(text(
                        "ALTER TABLE employee_contract_hours ADD COLUMN use_flexibility BOOLEAN DEFAULT FALSE"
                    ))
                
                db.session.commit()
                print("Columnas añadidas con éxito.")
            except Exception as e:
                db.session.rollback()
                print(f"Error al añadir las columnas: {str(e)}")
        else:
            print("Las columnas ya existen en la tabla employee_contract_hours.")

if __name__ == '__main__':
    add_activation_columns()
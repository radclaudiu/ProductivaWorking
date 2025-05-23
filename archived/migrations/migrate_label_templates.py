"""
Script para crear las tablas relacionadas con las etiquetas y plantillas
"""
from datetime import datetime
import os
import sys

from flask import Flask
from flask_sqlalchemy import SQLAlchemy

# Añadir el directorio actual al path para poder importar los modelos
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app import db, create_app
from models_tasks import LabelTemplate, ProductLabel

def create_label_tables():
    """Crea las tablas relacionadas con las etiquetas si no existen"""
    app = create_app()
    
    with app.app_context():
        # Verificar si las tablas ya existen
        inspector = db.inspect(db.engine)
        tables = inspector.get_table_names()
        
        # Crear tablas si no existen
        if 'label_templates' not in tables:
            print("Creando tabla label_templates...")
            db.metadata.tables['label_templates'].create(db.engine)
            print("Tabla label_templates creada exitosamente.")
        else:
            print("La tabla label_templates ya existe.")
            
        if 'product_labels' not in tables:
            print("Creando tabla product_labels...")
            db.metadata.tables['product_labels'].create(db.engine)
            print("Tabla product_labels creada exitosamente.")
        else:
            print("La tabla product_labels ya existe.")
            
        # Comprobar si hay plantillas por defecto existentes
        if 'label_templates' in tables:
            from models_tasks import Location
            
            # Para cada ubicación, crear una plantilla por defecto si no existe
            locations = Location.query.all()
            for location in locations:
                default_template = LabelTemplate.query.filter_by(
                    location_id=location.id, 
                    is_default=True
                ).first()
                
                if not default_template:
                    print(f"Creando plantilla por defecto para ubicación {location.name}...")
                    default_template = LabelTemplate(
                        name=f"Plantilla por defecto - {location.name}",
                        is_default=True,
                        location_id=location.id
                    )
                    db.session.add(default_template)
                    
            db.session.commit()
            print("Plantillas por defecto verificadas.")

if __name__ == "__main__":
    create_label_tables()
    print("Migración completada.")
#!/usr/bin/env python3
"""
Script para crear la tabla de impresoras de red en la base de datos
usando el contexto de la aplicación Flask.
"""
import os
import sys
from datetime import datetime
from sqlalchemy import MetaData, Table, Column, Integer, String, Boolean, DateTime, ForeignKey, inspect
from app import app, db

def create_network_printers_table():
    """Crea la tabla network_printers si no existe."""
    with app.app_context():
        # Verificar si la tabla ya existe
        inspector = inspect(db.engine)
        if "network_printers" in inspector.get_table_names():
            print("La tabla network_printers ya existe.")
            return
        
        try:
            # Crear tabla usando SQLAlchemy Core
            metadata = MetaData()
            
            # Crear la definición de la tabla
            network_printers = Table(
                'network_printers', 
                metadata,
                Column('id', Integer, primary_key=True),
                Column('name', String(100), nullable=False),
                Column('ip_address', String(50), nullable=False),
                Column('model', String(100)),
                Column('api_path', String(255), default='/brother_d/printer/print'),
                Column('port', Integer, default=80),
                Column('requires_auth', Boolean, default=False),
                Column('username', String(100)),
                Column('password', String(100)),
                Column('created_at', DateTime, default=datetime.utcnow),
                Column('updated_at', DateTime, default=datetime.utcnow, onupdate=datetime.utcnow),
                Column('is_default', Boolean, default=False),
                Column('is_active', Boolean, default=True),
                Column('last_status', String(50)),
                Column('last_status_check', DateTime),
                Column('location_id', Integer, ForeignKey('locations.id', ondelete='CASCADE'))
            )
            
            # Crear la tabla en la base de datos
            metadata.create_all(db.engine, tables=[network_printers])
            print("Tabla network_printers creada correctamente.")
            
            return True
        except Exception as e:
            print(f"Error al crear la tabla network_printers: {e}")
            return False

if __name__ == "__main__":
    success = create_network_printers_table()
    if success:
        print("¡Proceso completado correctamente!")
    else:
        print("Error en el proceso de creación de la tabla.")
        sys.exit(1)
"""
Script para crear la tabla de tokens de acceso directo para ubicaciones.
"""
import os
import sys
from datetime import datetime
from sqlalchemy import text

from app import create_app, db
from models_checkpoints import CheckPoint

# Crear la tabla access_tokens
SQL_CREATE_TABLE = """
CREATE TABLE IF NOT EXISTS location_access_tokens (
    id SERIAL PRIMARY KEY,
    location_id INTEGER NOT NULL,
    token VARCHAR(128) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    FOREIGN KEY (location_id) REFERENCES checkpoints (id) ON DELETE CASCADE,
    UNIQUE (location_id),
    UNIQUE (token)
);
"""

def create_access_tokens_table():
    """
    Crea la tabla de tokens de acceso directo para ubicaciones.
    """
    try:
        app = create_app()
        with app.app_context():
            # Crear la tabla usando SQL directo con text() para prepararlo correctamente
            result = db.session.execute(text(SQL_CREATE_TABLE))
            db.session.commit()
            print(f"✅ Tabla location_access_tokens creada correctamente")
            
            # Verificar si hay ubicaciones sin token
            checkpoints = CheckPoint.query.all()
            print(f"Se encontraron {len(checkpoints)} ubicaciones")
            
            return True
    except Exception as e:
        print(f"❌ Error al crear la tabla: {str(e)}")
        return False

if __name__ == "__main__":
    create_access_tokens_table()
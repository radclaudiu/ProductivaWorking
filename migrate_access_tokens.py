"""
Script para migrar la tabla de tokens de acceso.

Este script actualiza la estructura de la tabla 'location_access_tokens' para añadir
el campo portal_type y convertir la columna location_id para permitir múltiples tokens
por ubicación (eliminando la restricción UNIQUE).
"""
import sys
import logging
from datetime import datetime

from sqlalchemy import text
from sqlalchemy.exc import SQLAlchemyError, ProgrammingError

from app import db, create_app
from models_access import PortalType, LocationAccessToken

# Configurar logging
logging.basicConfig(level=logging.INFO, 
                    format='%(asctime)s - %(levelname)s - %(message)s')

def backup_tokens():
    """Realiza una copia de seguridad de los tokens existentes."""
    try:
        tokens = db.session.query(LocationAccessToken).all()
        return [(token.id, token.location_id, token.token, token.is_active, 
                token.created_at, token.last_used_at) for token in tokens]
    except Exception as e:
        logging.error(f"Error al hacer copia de seguridad de tokens: {e}")
        return []

def migrate_access_tokens():
    """
    Migra la tabla de tokens de acceso para añadir el campo portal_type
    y permitir múltiples tokens por ubicación.
    """
    try:
        # Hacer copia de seguridad de los tokens existentes
        logging.info("Haciendo copia de seguridad de tokens existentes...")
        tokens_backup = backup_tokens()
        logging.info(f"Se hizo copia de seguridad de {len(tokens_backup)} tokens")
        
        # Comprobar si la columna portal_type ya existe
        try:
            # Intentar seleccionar la columna portal_type
            db.session.execute(text("SELECT portal_type FROM location_access_tokens LIMIT 1"))
            logging.info("La columna portal_type ya existe. No se requiere migración.")
            return False
        except (SQLAlchemyError, ProgrammingError):
            # La columna no existe, continuamos con la migración
            logging.info("La columna portal_type no existe. Procediendo con la migración...")
        
        # Eliminar la tabla actual y recrearla con la nueva estructura
        db.session.execute(text("DROP TABLE IF EXISTS location_access_tokens"))
        db.session.commit()
        
        # Crear la tabla con la nueva estructura
        db.metadata.create_all(db.engine, tables=[LocationAccessToken.__table__])
        db.session.commit()
        
        # Restaurar los tokens de la copia de seguridad, asignándoles el tipo TASKS por defecto
        for token_id, location_id, token_value, is_active, created_at, last_used_at in tokens_backup:
            new_token = LocationAccessToken(
                id=token_id,
                location_id=location_id,
                token=token_value,
                is_active=is_active,
                created_at=created_at,
                last_used_at=last_used_at,
                portal_type=PortalType.TASKS
            )
            db.session.add(new_token)
        
        db.session.commit()
        logging.info(f"Se restauraron {len(tokens_backup)} tokens con tipo TASKS por defecto")
        
        return True
    except Exception as e:
        db.session.rollback()
        logging.error(f"Error en la migración: {e}")
        import traceback
        logging.error(traceback.format_exc())
        return False

if __name__ == "__main__":
    app = create_app()
    with app.app_context():
        try:
            if migrate_access_tokens():
                print("✅ Migración de tokens de acceso completada con éxito")
                sys.exit(0)
            else:
                print("⚠️ No fue necesario realizar la migración o se encontraron errores")
                sys.exit(1)
        except Exception as e:
            print(f"❌ Error durante la migración: {e}")
            sys.exit(1)
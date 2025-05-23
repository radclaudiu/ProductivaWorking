import os
from main import app
from app import db
from models_checkpoints import *
from models_tasks import *
from models import *

def create_checkpoint_tables():
    """Función para crear las tablas de puntos de fichaje"""
    print("Creando tablas de puntos de fichaje...")
    
    # Crear el contexto de la aplicación
    with app.app_context():
        # Importar los modelos para asegurar que SQLAlchemy los conozca
        import models_checkpoints
        import models_tasks
        import models
        
        # Asegurarse de que las tablas existen
        db.create_all()
        
        print("Tablas creadas correctamente.")
    
if __name__ == "__main__":
    create_checkpoint_tables()
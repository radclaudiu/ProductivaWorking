"""
Script para crear la tabla checkpoint_original_records para almacenar los registros
originales de fichajes antes de ser ajustados.
"""
from app import db, create_app
from models_checkpoints import CheckPointOriginalRecord

def create_original_records_table():
    """Crea la tabla para almacenar registros originales de fichajes"""
    app = create_app()
    with app.app_context():
        # Verificar si la tabla ya existe
        inspector = db.inspect(db.engine)
        if 'checkpoint_original_records' not in inspector.get_table_names():
            print("Creando tabla 'checkpoint_original_records'...")
            CheckPointOriginalRecord.__table__.create(db.engine)
            print("Tabla creada exitosamente.")
        else:
            print("La tabla 'checkpoint_original_records' ya existe.")

if __name__ == "__main__":
    create_original_records_table()
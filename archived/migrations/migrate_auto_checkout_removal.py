"""
Script de migración para eliminar la columna auto_checkout_time de la tabla checkpoints
"""
from app import db
from sqlalchemy import text
from flask import Flask, current_app

def run_migration():
    """
    Elimina la columna auto_checkout_time de la tabla checkpoints
    que fue parte del sistema de auto-checkout que ha sido eliminado.
    """
    print("Iniciando migración para eliminar columna auto_checkout_time...")
    
    try:
        # Verificar si la columna existe antes de intentar eliminarla
        result = db.session.execute(text(
            "SELECT column_name FROM information_schema.columns "
            "WHERE table_name = 'checkpoints' AND column_name = 'auto_checkout_time'"
        )).fetchone()
        
        if result:
            print("La columna auto_checkout_time existe, procediendo a eliminarla...")
            # Eliminar la columna
            db.session.execute(text(
                "ALTER TABLE checkpoints DROP COLUMN auto_checkout_time"
            ))
            db.session.commit()
            print("✅ Columna auto_checkout_time eliminada correctamente")
        else:
            print("⚠️ La columna auto_checkout_time no existe, no es necesario eliminarla")
        
    except Exception as e:
        db.session.rollback()
        print(f"❌ Error al eliminar la columna auto_checkout_time: {e}")
    
    print("Migración completada")

if __name__ == "__main__":
    # Crear aplicación Flask para contexto
    app = Flask(__name__)
    app.config.from_object('config.Config')
    db.init_app(app)
    
    with app.app_context():
        run_migration()
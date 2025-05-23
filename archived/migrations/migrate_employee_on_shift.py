"""
Script para añadir columna is_on_shift a la tabla employees
"""
from app import db
from flask import Flask
import os
from config import Config
from sqlalchemy import text

def add_is_on_shift_column():
    """Añade la columna is_on_shift a la tabla employees"""
    try:
        # Verificar si la columna ya existe
        check_query = text("SELECT column_name FROM information_schema.columns WHERE table_name='employees' AND column_name='is_on_shift'")
        result = db.session.execute(check_query).fetchall()
        
        if not result:
            # La columna no existe, la añadimos
            print("Añadiendo columna is_on_shift a la tabla employees...")
            alter_query = text("ALTER TABLE employees ADD COLUMN is_on_shift BOOLEAN DEFAULT FALSE")
            db.session.execute(alter_query)
            db.session.commit()
            print("Columna is_on_shift añadida correctamente.")
        else:
            print("La columna is_on_shift ya existe en la tabla employees.")
        
        return True
    except Exception as e:
        db.session.rollback()
        print(f"Error al añadir la columna is_on_shift: {str(e)}")
        return False

if __name__ == "__main__":
    app = Flask(__name__)
    app.config.from_object(Config)
    db.init_app(app)
    
    with app.app_context():
        add_is_on_shift_column()
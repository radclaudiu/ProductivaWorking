"""
Script para modificar los campos de fecha en la tabla de empleados
para que acepten cadenas de texto en lugar de fechas.
Esto resolverá el problema de conversión de tipos al editar empleados.
"""
import logging
from app import db
from models import Employee
from sqlalchemy import text

def modify_employee_date_fields():
    """
    Modifica los campos de fecha en la tabla de empleados para que sean
    de tipo String(20) en lugar de Date.
    """
    try:
        # Ejecutamos SQL directo para modificar la columna end_date
        db.session.execute(text("ALTER TABLE employees ALTER COLUMN end_date TYPE VARCHAR(20)"))
        print("✓ Columna end_date modificada a VARCHAR(20)")
        
        # Modificamos también start_date
        db.session.execute(text("ALTER TABLE employees ALTER COLUMN start_date TYPE VARCHAR(20)"))
        print("✓ Columna start_date modificada a VARCHAR(20)")
        
        # Modificamos status_start_date
        db.session.execute(text("ALTER TABLE employees ALTER COLUMN status_start_date TYPE VARCHAR(20)"))
        print("✓ Columna status_start_date modificada a VARCHAR(20)")
        
        # Modificamos status_end_date
        db.session.execute(text("ALTER TABLE employees ALTER COLUMN status_end_date TYPE VARCHAR(20)"))
        print("✓ Columna status_end_date modificada a VARCHAR(20)")
        
        # Commit de los cambios
        db.session.commit()
        print("✓ Todos los cambios guardados correctamente")
        
        return {
            "success": True,
            "message": "Campos de fecha de empleados modificados correctamente"
        }
    except Exception as e:
        db.session.rollback()
        error_msg = f"Error al modificar campos de fecha: {str(e)}"
        print(f"✗ {error_msg}")
        logging.error(error_msg)
        return {
            "success": False,
            "message": error_msg
        }

if __name__ == "__main__":
    from app import app
    with app.app_context():
        result = modify_employee_date_fields()
        print(result["message"])
"""
Migración para añadir los campos de información adicional de empleados:
- Número de Seguridad Social
- Email de contacto
- Dirección postal
- Número de teléfono
"""
from app import app, db
from flask.cli import with_appcontext
from sqlalchemy import Column, String

def run_migration():
    """Ejecuta la migración para añadir los campos de información adicional de empleados"""
    
    print("Iniciando migración para añadir campos de información adicional de empleados...")
    
    try:
        # Añadir las columnas si no existen
        with app.app_context():
            # Comprobar si las columnas ya existen
            inspector = db.inspect(db.engine)
            columns = [column['name'] for column in inspector.get_columns('employees')]
            
            # Crear la migración solo si es necesario
            connection = db.engine.raw_connection()
            cursor = connection.cursor()
            
            if 'social_security_number' not in columns:
                print("Añadiendo columna 'social_security_number'...")
                cursor.execute('ALTER TABLE employees ADD COLUMN social_security_number VARCHAR(20)')
            else:
                print("La columna 'social_security_number' ya existe.")
                
            if 'email' not in columns:
                print("Añadiendo columna 'email'...")
                cursor.execute('ALTER TABLE employees ADD COLUMN email VARCHAR(120)')
            else:
                print("La columna 'email' ya existe.")
                
            if 'address' not in columns:
                print("Añadiendo columna 'address'...")
                cursor.execute('ALTER TABLE employees ADD COLUMN address VARCHAR(200)')
            else:
                print("La columna 'address' ya existe.")
                
            if 'phone' not in columns:
                print("Añadiendo columna 'phone'...")
                cursor.execute('ALTER TABLE employees ADD COLUMN phone VARCHAR(20)')
            else:
                print("La columna 'phone' ya existe.")
                
            connection.commit()
            cursor.close()
            connection.close()
            
            print("Migración completada con éxito.")
            
    except Exception as e:
        print(f"Error durante la migración: {str(e)}")
        return False
        
    return True

if __name__ == "__main__":
    with app.app_context():
        run_migration()
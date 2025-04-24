"""
Script para migrar la base de datos añadiendo el campo hours_worked a checkpoint_original_records
y calculando sus valores para los registros existentes.
"""

import sys
from app import db, create_app
from sqlalchemy import text, inspect

def column_exists(table_name, column_name):
    """Verifica si una columna existe en una tabla"""
    with db.engine.connect() as connection:
        insp = inspect(db.engine)
        columns = [col['name'] for col in insp.get_columns(table_name)]
        return column_name in columns

def add_hours_worked_column():
    """
    Añade la columna hours_worked a la tabla checkpoint_original_records si no existe.
    """
    app = create_app()
    with app.app_context():
        # 1. Comprobar si la columna ya existe sin usar ORM
        exists = column_exists('checkpoint_original_records', 'hours_worked')
        
        if exists:
            print("✅ La columna 'hours_worked' ya existe en la tabla")
            return True
        
        # 2. Si no existe, la añadimos usando SQL directo
        try:
            print("➕ Añadiendo columna 'hours_worked' a la tabla checkpoint_original_records...")
            # Usar text() de SQLAlchemy para la sentencia SQL y una conexión nueva
            sql = text("ALTER TABLE checkpoint_original_records ADD COLUMN hours_worked FLOAT DEFAULT 0.0 NOT NULL")
            with db.engine.begin() as connection:
                connection.execute(sql)
            print("✅ Columna añadida correctamente")
            return True
        except Exception as e:
            print(f"❌ Error al añadir la columna: {str(e)}")
            return False

def update_hours_worked_values():
    """
    Calcula y actualiza los valores de hours_worked para todos los registros existentes.
    """
    app = create_app()
    with app.app_context():
        from models_checkpoints import CheckPointOriginalRecord
        
        try:
            print("🔄 Calculando horas trabajadas para registros existentes...")
            # Obtenemos todos los registros con check-out (para poder calcular horas)
            records = CheckPointOriginalRecord.query.filter(
                CheckPointOriginalRecord.original_check_out_time.isnot(None)
            ).all()
            
            count = 0
            for record in records:
                # Usamos el método duration() que ya existe en el modelo
                hours = record.duration()
                if hours is not None:
                    record.hours_worked = round(hours, 2)  # Redondeamos a 2 decimales
                    count += 1
            
            # Guardamos los cambios
            db.session.commit()
            print(f"✅ Se actualizaron {count} registros con sus horas trabajadas")
            return True
            
        except Exception as e:
            db.session.rollback()
            print(f"❌ Error al actualizar los registros: {str(e)}")
            return False

def create_work_hours_tables():
    """
    Crea las tablas para almacenar los acumulados de horas trabajadas:
    - employee_work_hours: Acumulados por empleado
    - company_work_hours: Acumulados por empresa
    """
    app = create_app()
    with app.app_context():
        # Importar los modelos aquí para evitar problemas de inicialización
        from models_work_hours import EmployeeWorkHours, CompanyWorkHours

        try:
            # Crear las tablas
            print("🔄 Creando tablas para acumulados de horas trabajadas...")
            db.create_all()
            print("✅ Tablas creadas correctamente")
            return True
        except Exception as e:
            print(f"❌ Error al crear las tablas: {str(e)}")
            return False

def run_migration():
    """Ejecuta la migración completa"""
    print("\n====== MIGRACIÓN SISTEMA DE HORAS TRABAJADAS ======\n")
    
    # 1. Añadir columna hours_worked
    print("Paso 1: Añadir columna hours_worked a checkpoint_original_records")
    if not add_hours_worked_column():
        print("❌ No se pudo añadir la columna hours_worked. Abortando.")
        return False
    
    # 2. Crear tablas para acumulados
    print("\nPaso 2: Crear tablas para acumulados de horas trabajadas")
    if not create_work_hours_tables():
        print("❌ No se pudieron crear las tablas. Abortando.")
        return False
    
    # 3. Actualizar valores de horas trabajadas
    print("\nPaso 3: Calcular y actualizar horas trabajadas en registros existentes")
    if not update_hours_worked_values():
        print("❌ No se pudieron actualizar los valores. La migración está incompleta.")
        return False
    
    print("\n✅ MIGRACIÓN COMPLETADA EXITOSAMENTE")
    return True

if __name__ == "__main__":
    success = run_migration()
    sys.exit(0 if success else 1)
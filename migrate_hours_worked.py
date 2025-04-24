"""
Script para migrar la base de datos añadiendo el campo hours_worked a checkpoint_original_records
y calculando sus valores para los registros existentes.
"""

from app import db, create_app
from models_checkpoints import CheckPointOriginalRecord

def add_hours_worked_column():
    """
    Añade la columna hours_worked a la tabla checkpoint_original_records si no existe.
    También calcula y actualiza el valor de hours_worked para todos los registros existentes.
    """
    app = create_app()
    with app.app_context():
        # 1. Comprobar si la columna ya existe
        try:
            # Intentamos acceder a la columna para verificar si existe
            CheckPointOriginalRecord.query.first().hours_worked
            print("✅ La columna 'hours_worked' ya existe en la tabla")
            return True
        except Exception as e:
            print(f"ℹ️ La columna 'hours_worked' no existe aún: {str(e)}")
        
        # 2. Si no existe, la añadimos usando SQL directo
        try:
            print("➕ Añadiendo columna 'hours_worked' a la tabla checkpoint_original_records...")
            # Usar text() de SQLAlchemy para la sentencia SQL
            from sqlalchemy import text
            sql = text("ALTER TABLE checkpoint_original_records ADD COLUMN hours_worked FLOAT DEFAULT 0.0 NOT NULL")
            db.session.execute(sql)
            db.session.commit()
            print("✅ Columna añadida correctamente")
        except Exception as e:
            db.session.rollback()
            print(f"❌ Error al añadir la columna: {str(e)}")
            return False
        
        # 3. Actualizar valores existentes calculando las horas trabajadas
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

if __name__ == "__main__":
    add_hours_worked_column()
"""Script para agregar el campo current_month_completed a las tareas.

Este script agrega el campo current_month_completed a la tabla tasks para soportar
la funcionalidad de tareas mensuales personalizadas con días específicos del mes.
"""

import logging
from sqlalchemy import Column, Boolean, Table, MetaData
from app import db, create_app
from models_tasks import Task

app = create_app()

def migrate_monthly_tasks_flag():
    """Agrega el campo current_month_completed a la tabla tasks."""
    # Configurar logging
    logging.basicConfig(level=logging.INFO, 
                       format='[%(asctime)s] [%(levelname)s] %(message)s')
    logger = logging.getLogger(__name__)
    
    with app.app_context():
        # Utilizamos la API de SQL de bajo nivel para realizar la migración
        try:
            # Verificar si la columna ya existe
            inspected_columns = [c.get('name') for c in db.inspect(db.engine).get_columns('tasks')]
            if 'current_month_completed' in inspected_columns:
                logger.info("La columna 'current_month_completed' ya existe en la tabla 'tasks'")
                return True
                
            # Crear la migración si no existe la columna
            meta = MetaData()
            meta.reflect(bind=db.engine)
            tasks = Table('tasks', meta)
            
            # Definir la columna a agregar
            new_column = Column('current_month_completed', Boolean, default=False)
            
            # Realizar la migración
            logger.info("Agregando columna 'current_month_completed' a la tabla 'tasks'...")
            with db.engine.begin() as conn:
                conn.execute(
                    db.text(
                        "ALTER TABLE tasks ADD COLUMN current_month_completed BOOLEAN DEFAULT false"
                    )
                )
                
            logger.info("Columna 'current_month_completed' agregada correctamente")
            return True
            
        except Exception as e:
            logger.error(f"Error al agregar columna 'current_month_completed': {str(e)}")
            return False

# Si se ejecuta este script directamente, realizar la migración
if __name__ == '__main__':
    migrate_monthly_tasks_flag()

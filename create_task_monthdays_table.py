"""Script para crear la tabla task_monthdays para las tareas mensuales personalizadas

Este script crea la tabla task_monthdays que permite asignar tareas mensuales
a días específicos del mes (1-31).
"""

import logging
from app import db, create_app
from models_tasks import TaskMonthDay

app = create_app()

def create_task_monthdays_table():
    """Crea la tabla task_monthdays si no existe."""
    # Configurar logging
    logging.basicConfig(level=logging.INFO, 
                       format='[%(asctime)s] [%(levelname)s] %(message)s')
    logger = logging.getLogger(__name__)
    
    with app.app_context():
        # Utilizar metadata para verificar si la tabla existe
        inspector = db.inspect(db.engine)
        existing_tables = inspector.get_table_names()
        
        if 'task_monthdays' in existing_tables:
            logger.info("La tabla 'task_monthdays' ya existe en la base de datos")
            return True
        
        try:
            # Crear la tabla usando la definición del modelo
            db.create_all()
            logger.info("Tabla 'task_monthdays' creada exitosamente")
            return True
        except Exception as e:
            logger.error(f"Error al crear la tabla 'task_monthdays': {str(e)}")
            return False

# Si se ejecuta este script directamente, crear la tabla
if __name__ == '__main__':
    create_task_monthdays_table()

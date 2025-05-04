import os
import sys
import logging
from datetime import datetime

# Configuración de logging
logging.basicConfig(level=logging.INFO, 
                   format='[%(asctime)s] [%(levelname)s] %(message)s',
                   datefmt='%Y-%m-%d %H:%M:%S')
logger = logging.getLogger(__name__)

# Asegurar que el script se ejecuta en el directorio del proyecto
script_dir = os.path.dirname(os.path.abspath(__file__))
os.chdir(script_dir)

# Importar la aplicación Flask y configuración de base de datos
from app import db, create_app
from models_tasks import Task, TaskFrequency

# Crear la aplicación Flask para tener contexto
app = create_app()

def migrate_task_weekly_completion():
    """Migración para añadir el campo current_week_completed a la tabla de tareas."""
    try:
        # Obtener el esquema de la tabla Task
        inspector = db.inspect(db.engine)
        columns = [col['name'] for col in inspector.get_columns('tasks')]
        
        # Verificar si el campo ya existe
        if 'current_week_completed' in columns:
            logger.info("El campo 'current_week_completed' ya existe en la tabla de tareas.")
            return True
            
        # Crear la columna con un valor predeterminado de False
        logger.info("Añadiendo campo 'current_week_completed' a la tabla de tareas...")
        db.engine.execute('ALTER TABLE tasks ADD COLUMN current_week_completed BOOLEAN DEFAULT FALSE')
        
        # Actualizar el valor para todas las tareas semanales ya completadas
        tasks_updated = db.session.query(Task).filter_by(
            frequency=TaskFrequency.SEMANAL,
            status='completada'
        ).update({Task.current_week_completed: True})
        
        db.session.commit()
        
        logger.info(f"Migración completada: {tasks_updated} tareas semanales actualizadas")
        return True
        
    except Exception as e:
        logger.error(f"Error en la migración: {str(e)}")
        db.session.rollback()
        return False

# Ejecutar la migración dentro del contexto de la aplicación
if __name__ == '__main__':
    with app.app_context():
        logger.info("Iniciando migración de tareas semanales...")
        success = migrate_task_weekly_completion()
        if success:
            logger.info("La migración se completó correctamente")
            sys.exit(0)
        else:
            logger.error("La migración falló")
            sys.exit(1)

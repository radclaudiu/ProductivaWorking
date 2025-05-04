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
from models_tasks import Task, TaskFrequency, TaskStatus
from sqlalchemy import text

# Crear la aplicación Flask para tener contexto
app = create_app()

def update_task_schema():
    """Actualiza el esquema de la tabla de tareas para asegurar que tiene el campo current_week_completed."""
    try:
        with app.app_context():
            # Verificar si la tabla existe
            inspector = db.inspect(db.engine)
            tables = inspector.get_table_names()
            
            if 'tasks' in tables:
                # Verificar si el campo ya existe
                columns = [col['name'] for col in inspector.get_columns('tasks')]
                
                if 'current_week_completed' not in columns:
                    logger.info("Actualizando esquema de la tabla 'tasks'...")
                    
                    # Crear la columna con SQL directo para evitar el problema de circular
                    sql = text("ALTER TABLE tasks ADD COLUMN current_week_completed BOOLEAN DEFAULT FALSE")
                    logger.info(f"Ejecutando SQL: {sql}")
                    
                    # Ejecutar el SQL directo
                    with db.engine.begin() as conn:
                        conn.execute(sql)
                    
                    logger.info("Columna current_week_completed añadida correctamente")
                    
                    # Actualizar el estado para las tareas semanales completadas
                    tasks = db.session.query(Task).filter(
                        Task.frequency == TaskFrequency.SEMANAL,
                        Task.status == TaskStatus.COMPLETADA
                    ).all()
                    
                    count = 0
                    for task in tasks:
                        task.current_week_completed = True
                        count += 1
                    
                    db.session.commit()
                    
                    logger.info(f"Esquema actualizado y {count} tareas semanales marcadas como completadas")
                    return True
                else:
                    logger.info("La columna 'current_week_completed' ya existe en la tabla de tareas")
                    return True
            else:
                logger.warning("La tabla 'tasks' no existe en la base de datos")
                return False
                
    except Exception as e:
        logger.error(f"Error al actualizar el esquema: {str(e)}")
        return False

# Ejecutar la actualización
if __name__ == '__main__':
    with app.app_context():
        logger.info("Iniciando actualización del esquema de tareas...")
        success = update_task_schema()
        
        if success:
            logger.info("Actualización completada correctamente")
            sys.exit(0)
        else:
            logger.error("La actualización falló")
            sys.exit(1)

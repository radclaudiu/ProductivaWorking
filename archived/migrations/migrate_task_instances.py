"""
Script para crear la tabla task_instances para almacenar instancias de tareas programadas.
"""
from datetime import datetime
from app import db, create_app
from sqlalchemy import inspect
from models_tasks import Task, TaskInstance, TaskStatus, LocalUser

def create_task_instances_table():
    """Crea la tabla para almacenar instancias de tareas programadas"""
    print("Iniciando migración de instancias de tareas...")
    app = create_app()
    
    with app.app_context():
        # Comprobar si la tabla ya existe
        inspector = inspect(db.engine)
        if 'task_instances' in inspector.get_table_names():
            print("La tabla task_instances ya existe. Saltando creación.")
            return
            
        # Crear la tabla
        db.create_all()
        
        print("Tabla task_instances creada correctamente.")
        
if __name__ == "__main__":
    create_task_instances_table()
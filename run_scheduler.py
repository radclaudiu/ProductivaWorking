# Script para ejecutar manualmente el programador de tareas

from app import create_app
from task_scheduler_service import run_task_scheduler

app = create_app()
with app.app_context():
    print("Iniciando ejecución manual del programador de tareas...")
    try:
        run_task_scheduler()
        print("\n✅ Programador de tareas ejecutado correctamente")
    except Exception as e:
        print(f"\n❌ Error al ejecutar el programador de tareas: {str(e)}")

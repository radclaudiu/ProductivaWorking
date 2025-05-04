#!/usr/bin/env python
'''
Script para ejecutar manualmente el programador de tareas

Este script ejecuta el programador de tareas que genera instancias
para el día actual y los próximos 7 días.
'''

import time
import logging
from datetime import datetime
from app import app
from task_scheduler_service import run_task_scheduler

# Configurar logging
logging.basicConfig(level=logging.INFO, 
                    format='[%(asctime)s] [%(levelname)s] %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')
logger = logging.getLogger("task_scheduler_manual")

# Banner visual
def print_banner():
    print("\n" + "=" * 80)
    print("   PROGRAMADOR DE TAREAS - EJECUCIÓN MANUAL")
    print(f"   Fecha/hora: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 80)

def run_manual_scheduler():
    print_banner()
    
    logger.info("INICIANDO EJECUCIÓN MANUAL DEL PROGRAMADOR DE TAREAS")
    print("\nIniciando ejecución manual del programador de tareas...")
    
    start_time = time.time()
    
    # Usar la instancia global de la aplicación
    with app.app_context():
        try:
            # Ejecutar el programador de tareas
            run_task_scheduler()
            
            # Calcular duración
            duration = time.time() - start_time
            
            # Mostrar resumen
            print("\n" + "=" * 80)
            print(f"\n✅ PROGRAMADOR DE TAREAS COMPLETADO EXITOSAMENTE")
            print(f"   Duración: {duration:.2f} segundos")
            print(f"   Fecha/hora finalización: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
            print("\n" + "=" * 80)
            
            logger.info(f"PROGRAMADOR DE TAREAS COMPLETADO EXITOSAMENTE EN {duration:.2f} SEGUNDOS")
            
        except Exception as e:
            print(f"\n❌ ERROR AL EJECUTAR EL PROGRAMADOR DE TAREAS: {str(e)}")
            logger.error(f"ERROR AL EJECUTAR EL PROGRAMADOR DE TAREAS: {str(e)}")

if __name__ == "__main__":
    run_manual_scheduler()

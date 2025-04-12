"""
Servicio de cierre automático de fichajes como proceso en segundo plano.

Este módulo proporciona funciones para iniciar un hilo (thread) que ejecuta
periódicamente el cierre automático de fichajes pendientes.
"""
import threading
import time
import logging
import os
from datetime import datetime, timedelta
from close_operation_hours import auto_close_pending_records, STARTUP_FILE

# Configuración de logging
logging.basicConfig(level=logging.INFO, 
                   format='[%(asctime)s] [%(levelname)s] %(message)s',
                   datefmt='%Y-%m-%d %H:%M:%S')
logger = logging.getLogger(__name__)

# Intervalo entre ejecuciones (en segundos)
CHECK_INTERVAL = 40 * 60  # 40 minutos

# Variable global para controlar el estado del servicio
service_thread = None
service_running = False
last_run_time = None
checkpoint_closer_active = False

def checkpoint_closer_worker():
    """
    Función que ejecuta el cierre automático de fichajes periódicamente.
    """
    global service_running, last_run_time, checkpoint_closer_active
    
    # Importar la aplicación Flask
    from app import create_app
    app = create_app()
    
    logger.info("Iniciando servicio de cierre automático de fichajes")
    checkpoint_closer_active = True
    
    # Detectar si es la primera ejecución después de un redeploy
    is_first_startup = not os.path.exists(STARTUP_FILE)
    if is_first_startup:
        logger.info("Primera ejecución después de redeploy - Archivo de startup no encontrado")
    else:
        logger.info("Servicio en ejecución continua - Archivo de startup encontrado")
    
    try:
        while service_running:
            try:
                logger.info("Ejecutando verificación de cierre automático de fichajes")
                
                # Usar el contexto de la aplicación para operaciones de base de datos
                with app.app_context():
                    success = auto_close_pending_records()
                
                if success is not None:
                    if success:
                        logger.info("Cierre automático ejecutado correctamente")
                    else:
                        logger.warning("El cierre automático finalizó con errores")
                else:
                    logger.info("Cierre automático completado sin cambios")
                
                # Actualizar el tiempo de la última ejecución
                last_run_time = datetime.now()
                
            except Exception as e:
                logger.error(f"Error durante la ejecución del cierre automático: {str(e)}")
            
            # Esperar hasta la próxima ejecución
            logger.info(f"Próxima ejecución en {CHECK_INTERVAL/60} minutos")
            time.sleep(CHECK_INTERVAL)
    
    except Exception as e:
        logger.error(f"Error fatal en el servicio de cierre automático: {str(e)}")
    finally:
        checkpoint_closer_active = False
        logger.info("Servicio de cierre automático detenido")


def start_checkpoint_closer_service():
    """
    Inicia el servicio de cierre automático de fichajes en un hilo separado.
    
    Returns:
        bool: True si el servicio se inició correctamente, False en caso contrario.
    """
    global service_thread, service_running, checkpoint_closer_active
    
    if service_thread is not None and service_thread.is_alive():
        logger.info("El servicio de cierre automático ya está en ejecución")
        return False
    
    # Si el hilo anterior existe pero está muerto, limpiar la referencia
    if service_thread is not None and not service_thread.is_alive():
        service_thread = None
        logger.warning("Se detectó un hilo anterior muerto - Limpiando referencia")
    
    # Iniciar el servicio
    service_running = True
    service_thread = threading.Thread(target=checkpoint_closer_worker, daemon=True)
    service_thread.start()
    
    # Esperar a que el servicio se inicie completamente
    timeout = 5  # 5 segundos máximo de espera
    start_time = time.time()
    while not checkpoint_closer_active and time.time() - start_time < timeout:
        time.sleep(0.1)
    
    if checkpoint_closer_active:
        logger.info("Servicio de cierre automático iniciado correctamente")
        return True
    else:
        logger.error("No se pudo iniciar el servicio de cierre automático")
        return False


def stop_checkpoint_closer_service():
    """
    Detiene el servicio de cierre automático de fichajes.
    
    Returns:
        bool: True si el servicio se detuvo correctamente, False en caso contrario.
    """
    global service_thread, service_running, checkpoint_closer_active
    
    if service_thread is None or not service_thread.is_alive():
        logger.info("El servicio de cierre automático no está en ejecución")
        service_running = False
        checkpoint_closer_active = False
        return False
    
    # Detener el hilo
    service_running = False
    
    # Esperar a que el hilo termine
    timeout = 5  # 5 segundos máximo de espera
    start_time = time.time()
    while checkpoint_closer_active and time.time() - start_time < timeout:
        time.sleep(0.1)
    
    if not checkpoint_closer_active:
        logger.info("Servicio de cierre automático detenido correctamente")
        return True
    else:
        logger.warning("No se pudo detener el servicio correctamente")
        return False


def get_service_status():
    """
    Obtiene el estado actual del servicio de cierre automático.
    
    Returns:
        dict: Diccionario con información sobre el estado del servicio.
    """
    global service_thread, service_running, last_run_time, checkpoint_closer_active
    
    is_alive = service_thread is not None and service_thread.is_alive()
    
    # Formatear los tiempos para mostrarlos de forma amigable
    formatted_last_run = "No ejecutado aún" if last_run_time is None else last_run_time.strftime('%Y-%m-%d %H:%M:%S')
    
    next_run = None
    if last_run_time is not None:
        next_run = last_run_time + timedelta(seconds=CHECK_INTERVAL)
        formatted_next_run = next_run.strftime('%Y-%m-%d %H:%M:%S')
    else:
        formatted_next_run = "Pendiente de primera ejecución"
    
    return {
        'active': is_alive and checkpoint_closer_active,
        'running': service_running,
        'last_run': formatted_last_run,
        'next_run': formatted_next_run,
        'thread_alive': is_alive,
        'check_interval_minutes': CHECK_INTERVAL / 60
    }
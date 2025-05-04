"""Servicio de reinicio de tareas diarias como proceso independiente.

Este módulo proporciona funciones para iniciar un hilo (thread) que ejecuta
el reinicio de tareas diarias automáticamente todos los días a las 05:00 AM.
"""
import threading
import time
import logging
import os
import fcntl
import socket
from datetime import datetime, timedelta
from models_tasks import Task, TaskFrequency, TaskStatus, TaskInstance, WeekDay, TaskMonthDay
from app import db

# Configuración de logging
logging.basicConfig(level=logging.INFO, 
                   format='[%(asctime)s] [%(levelname)s] %(message)s',
                   datefmt='%Y-%m-%d %H:%M:%S')
logger = logging.getLogger(__name__)

# Archivo de bloqueo para evitar múltiples instancias
LOCK_FILE = "/tmp/daily_tasks_reset_service.lock"

# Hora del día para ejecutar el reinicio (formato 24h)
RESET_HOUR = 5  # 05:00 AM
RESET_MINUTE = 0

# Variables globales para controlar el estado del servicio
service_thread = None
service_running = False
last_run_time = None
service_active = False
last_reset_date = None
lock_file_handle = None


def reset_monthly_tasks():
    """
    Reinicia el estado de las tareas mensuales cuando inicia un nuevo mes.
    Actualiza el campo current_month_completed para que las tareas mensuales 
    puedan volver a mostrarse según los días configurados.
    
    Returns:
        int: Número de tareas actualizadas
    """
    try:
        today = datetime.now().date()
        # Si es el primer día del mes, reiniciamos todas las tareas mensuales
        # marcando current_month_completed como False
        if today.day == 1:
            # Obtener todas las tareas mensuales
            monthly_tasks = Task.query.filter_by(
                frequency=TaskFrequency.MENSUAL,
                current_month_completed=True,
                status=TaskStatus.PENDIENTE
            ).all()
            
            if not monthly_tasks:
                logger.info("No hay tareas mensuales para reiniciar")
                return 0
                
            # Reiniciar el estado de completed para el nuevo mes
            for task in monthly_tasks:
                task.current_month_completed = False
                logger.debug(f"Tarea mensual '{task.title}' reiniciada para el nuevo mes")
            
            # Guardar los cambios
            db.session.commit()
            logger.info(f"Se reiniciaron {len(monthly_tasks)} tareas mensuales para el nuevo mes")
            return len(monthly_tasks)
            
        # Si no es el primer día del mes, verificamos si hay tareas mensuales
        # asignadas a este día del mes específico
        else:
            # Procesamos tareas mensuales con días específicos
            day_of_month = today.day
            
            # Obtener tareas mensuales con días específicos para hoy
            # y que no estén completadas para este mes
            monthly_tasks_for_today = Task.query.join(
                TaskMonthDay,
                Task.id == TaskMonthDay.task_id
            ).filter(
                Task.frequency == TaskFrequency.MENSUAL,
                Task.current_month_completed == False,
                Task.status == TaskStatus.PENDIENTE,
                TaskMonthDay.day_of_month == day_of_month
            ).all()
            
            if not monthly_tasks_for_today:
                logger.info(f"No hay tareas mensuales para el día {day_of_month} del mes")
                return 0
                
            # Crear instancias para las tareas de hoy
            instances_created = 0
            
            for task in monthly_tasks_for_today:
                # Verificar si ya existe una instancia para hoy
                existing_instance = TaskInstance.query.filter_by(
                    task_id=task.id,
                    scheduled_date=today
                ).first()
                
                # Si no existe, crear una nueva instancia para hoy
                if not existing_instance:
                    new_instance = TaskInstance(
                        task_id=task.id,
                        scheduled_date=today,
                        status=TaskStatus.PENDIENTE
                    )
                    db.session.add(new_instance)
                    instances_created += 1
                    logger.debug(f"Creada instancia de tarea mensual '{task.title}' para hoy (día {day_of_month})")
            
            # Guardar los cambios
            if instances_created > 0:
                db.session.commit()
                logger.info(f"Se crearon {instances_created} instancias de tareas mensuales para hoy (día {day_of_month})")
            
            return instances_created
            
    except Exception as e:
        logger.error(f"Error al reiniciar tareas mensuales: {str(e)}")
        db.session.rollback()
        return 0


def reset_daily_tasks():
    """
    Reinicia el estado de las tareas diarias.
    Permite que las tareas diarias vuelvan a estar activas para el nuevo día.
    
    Returns:
        int: Número de tareas actualizadas
    """
    global last_reset_date
    
    try:
        # Obtener todas las tareas diarias
        tasks = Task.query.filter_by(
            frequency=TaskFrequency.DIARIA,
            status=TaskStatus.PENDIENTE
        ).all()
        
        if not tasks:
            logger.info("No hay tareas diarias para reiniciar")
            last_reset_date = datetime.now().date()
            return 0
            
        # Para tareas diarias, no necesitamos cambiar ninguna propiedad de las tareas,
        # pero podemos crear o actualizar instancias diarias
        instances_created = 0
        today = datetime.now().date()
        
        for task in tasks:
            # Verificar si ya existe una instancia para hoy
            existing_instance = TaskInstance.query.filter_by(
                task_id=task.id,
                scheduled_date=today
            ).first()
            
            # Si no existe, crear una nueva instancia para hoy
            if not existing_instance:
                new_instance = TaskInstance(
                    task_id=task.id,
                    scheduled_date=today,
                    status=TaskStatus.PENDIENTE
                )
                db.session.add(new_instance)
                instances_created += 1
                logger.debug(f"Creada instancia de tarea diaria '{task.title}' para hoy")
        
        # Guardar los cambios
        if instances_created > 0:
            db.session.commit()
            logger.info(f"Se crearon {instances_created} instancias de tareas diarias para hoy")
        
        # Actualizar la fecha de último reinicio
        last_reset_date = datetime.now().date()
        
        logger.info(f"Se reiniciaron {len(tasks)} tareas diarias para el nuevo día")
        return len(tasks)
        
    except Exception as e:
        logger.error(f"Error al reiniciar tareas diarias: {str(e)}")
        db.session.rollback()
        return 0


def should_run_reset():
    """
    Determina si es momento de ejecutar el reinicio de tareas diarias.
    El reinicio debe ejecutarse todos los días a la hora configurada (RESET_HOUR:RESET_MINUTE).
    Además, se verifica que no se haya ejecutado ya hoy.
    
    Returns:
        bool: True si se debe ejecutar el reinicio, False en caso contrario.
    """
    now = datetime.now()
    today = now.date()
    
    # Si ya lo hicimos hoy, no volver a ejecutar
    if last_reset_date == today:
        return False
    
    # Solo a partir de la hora configurada
    target_time = now.replace(hour=RESET_HOUR, minute=RESET_MINUTE, second=0, microsecond=0)
    return now >= target_time


def calculate_sleep_time():
    """
    Calcula el tiempo exacto que debe dormir el servicio hasta la próxima hora de ejecución.
    
    Returns:
        float: Tiempo en segundos hasta la próxima ejecución a las 05:00 AM
    """
    now = datetime.now()
    today = now.date()
    
    # Si hoy aún no llega la hora de reinicio
    if now.hour < RESET_HOUR or (now.hour == RESET_HOUR and now.minute < RESET_MINUTE):
        target_time = now.replace(hour=RESET_HOUR, minute=RESET_MINUTE, second=0, microsecond=0)
        return (target_time - now).total_seconds()
    
    # Si ya pasó la hora de reinicio hoy, calcular para mañana
    tomorrow = today + timedelta(days=1)
    target_time = datetime.combine(tomorrow, datetime.min.time())
    target_time = target_time.replace(hour=RESET_HOUR, minute=RESET_MINUTE, second=0, microsecond=0)
    
    return (target_time - now).total_seconds()


def daily_tasks_reset_worker():
    """
    Función que ejecuta el reinicio de tareas diarias.
    """
    global service_running, last_run_time, service_active, lock_file_handle
    
    # Importar la aplicación Flask
    from app import create_app
    app = create_app()
    
    logger.info("Iniciando servicio de reinicio de tareas diarias")
    service_active = True
    
    try:
        while service_running:
            try:
                # Verificar si es momento de ejecutar el reinicio
                if should_run_reset():
                    logger.info(f"Es hora de reiniciar tareas diarias ({RESET_HOUR}:{RESET_MINUTE}) - Ejecutando reinicio")
                    
                    # Usar el contexto de la aplicación para operaciones de base de datos
                    with app.app_context():
                        # Realizar el reinicio de tareas diarias
                        tasks_reset_count = reset_daily_tasks()
                        if tasks_reset_count > 0:
                            logger.info(f"Reinicio exitoso: {tasks_reset_count} tareas diarias actualizadas")
                        else:
                            logger.info("No se reiniciaron tareas diarias (no hay tareas diarias o ya se hizo hoy)")
                            
                        # Realizar el reinicio de tareas mensuales (personalizadas o por primer día del mes)
                        monthly_tasks_reset_count = reset_monthly_tasks()
                        if monthly_tasks_reset_count > 0:
                            logger.info(f"Reinicio exitoso: {monthly_tasks_reset_count} tareas mensuales actualizadas")
                        else:
                            logger.info("No se reiniciaron tareas mensuales (no hay tareas mensuales para hoy)")
                        
                else:
                    logger.debug("No es momento de ejecutar el reinicio de tareas diarias")
                
                # Actualizar el tiempo de la última ejecución
                last_run_time = datetime.now()
                
                # Calcular el tiempo de espera hasta la próxima verificación
                sleep_time = calculate_sleep_time()
                next_check_time = datetime.now() + timedelta(seconds=sleep_time)
                logger.info(f"Próxima verificación en {sleep_time/60:.1f} minutos ({next_check_time.strftime('%Y-%m-%d %H:%M:%S')})")
                time.sleep(sleep_time)
                
            except Exception as e:
                logger.error(f"Error durante la ejecución del reinicio de tareas diarias: {str(e)}")
                # Dormir 1 hora en caso de error
                time.sleep(60 * 60)
    
    except Exception as e:
        logger.error(f"Error fatal en el servicio de reinicio de tareas diarias: {str(e)}")
    finally:
        service_active = False
        
        # Liberar el archivo de bloqueo al finalizar
        if lock_file_handle:
            try:
                logger.info("Liberando archivo de bloqueo al finalizar worker")
                fcntl.lockf(lock_file_handle, fcntl.LOCK_UN)
                lock_file_handle.close()
                lock_file_handle = None
            except Exception as e:
                logger.error(f"Error al liberar bloqueo en worker: {str(e)}")
                
        logger.info("Servicio de reinicio de tareas diarias detenido")


def start_daily_tasks_reset_service():
    """
    Inicia el servicio de reinicio de tareas diarias en un hilo separado.
    Utiliza un archivo de bloqueo para evitar múltiples ejecuciones con varios workers.
    
    Returns:
        bool: True si el servicio se inició correctamente, False en caso contrario.
    """
    global service_thread, service_running, service_active, lock_file_handle
    
    # Verificar si ya hay una instancia en ejecución (incluso en otro worker)
    try:
        # Intentar obtener un bloqueo exclusivo (no bloqueante)
        lock_file_handle = open(LOCK_FILE, 'w')
        fcntl.lockf(lock_file_handle, fcntl.LOCK_EX | fcntl.LOCK_NB)
        
        # Escribir información en el archivo de bloqueo
        pid = os.getpid()
        host = socket.gethostname()
        lock_info = f"{pid}@{host} {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"
        lock_file_handle.write(lock_info)
        lock_file_handle.flush()
        
        logger.info(f"Adquirido bloqueo exclusivo para el servicio (PID: {pid})")
    except IOError:
        # No se pudo obtener el bloqueo, otro proceso ya lo tiene
        logger.info("Otra instancia del servicio ya está en ejecución")
        return False
        
    if service_thread is not None and service_thread.is_alive():
        logger.info("El servicio de reinicio de tareas ya está en ejecución en este worker")
        return False
    
    # Si el hilo anterior existe pero está muerto, limpiar la referencia
    if service_thread is not None and not service_thread.is_alive():
        service_thread = None
        logger.warning("Se detectó un hilo anterior muerto - Limpiando referencia")
    
    # Iniciar el servicio
    service_running = True
    service_thread = threading.Thread(target=daily_tasks_reset_worker, daemon=True)
    service_thread.start()
    
    # Esperar a que el servicio se inicie completamente
    timeout = 5  # 5 segundos máximo de espera
    start_time = time.time()
    while not service_active and time.time() - start_time < timeout:
        time.sleep(0.1)
    
    if service_active:
        logger.info("Servicio de reinicio de tareas diarias iniciado correctamente")
        return True
    else:
        logger.error("No se pudo iniciar el servicio de reinicio de tareas diarias")
        # Liberar el bloqueo
        if lock_file_handle:
            fcntl.lockf(lock_file_handle, fcntl.LOCK_UN)
            lock_file_handle.close()
            lock_file_handle = None
        return False


def stop_daily_tasks_reset_service():
    """
    Detiene el servicio de reinicio de tareas diarias.
    Libera el archivo de bloqueo si está en uso.
    
    Returns:
        bool: True si el servicio se detuvo correctamente, False en caso contrario.
    """
    global service_thread, service_running, service_active, lock_file_handle
    
    if service_thread is None or not service_thread.is_alive():
        logger.info("El servicio de reinicio de tareas diarias no está en ejecución")
        service_running = False
        service_active = False
        
        # Liberar el bloqueo si está activo
        if lock_file_handle:
            try:
                logger.info("Liberando archivo de bloqueo")
                fcntl.lockf(lock_file_handle, fcntl.LOCK_UN)
                lock_file_handle.close()
                lock_file_handle = None
            except Exception as e:
                logger.error(f"Error al liberar bloqueo: {str(e)}")
        return False
    
    # Detener el hilo
    service_running = False
    
    # Esperar a que el hilo termine
    timeout = 5  # 5 segundos máximo de espera
    start_time = time.time()
    while service_active and time.time() - start_time < timeout:
        time.sleep(0.1)
    
    # Liberar el bloqueo si está activo
    if lock_file_handle:
        try:
            logger.info("Liberando archivo de bloqueo")
            fcntl.lockf(lock_file_handle, fcntl.LOCK_UN)
            lock_file_handle.close()
            lock_file_handle = None
        except Exception as e:
            logger.error(f"Error al liberar bloqueo: {str(e)}")
            
    if not service_active:
        logger.info("Servicio de reinicio de tareas diarias detenido correctamente")
        return True
    else:
        logger.warning("No se pudo detener el servicio correctamente")
        return False


def get_service_status():
    """
    Obtiene el estado actual del servicio de reinicio de tareas diarias.
    
    Returns:
        dict: Diccionario con información sobre el estado del servicio.
    """
    global service_thread, service_running, last_run_time, service_active, last_reset_date
    
    is_alive = service_thread is not None and service_thread.is_alive()
    
    # Formatear los tiempos para mostrarlos de forma amigable
    formatted_last_run = "No ejecutado aún" if last_run_time is None else last_run_time.strftime('%Y-%m-%d %H:%M:%S')
    formatted_last_reset = "No reiniciado aún" if last_reset_date is None else last_reset_date.strftime('%Y-%m-%d')
    
    # Calcular la próxima ejecución
    now = datetime.now()
    target_time = now.replace(hour=RESET_HOUR, minute=RESET_MINUTE, second=0, microsecond=0)
    if now >= target_time:  # Si ya pasó la hora para hoy, calcular para mañana
        target_time = target_time + timedelta(days=1)
    
    formatted_next_reset = target_time.strftime('%Y-%m-%d %H:%M:%S')
    
    return {
        'active': is_alive and service_active,
        'running': service_running,
        'last_run': formatted_last_run,
        'next_reset': formatted_next_reset,
        'last_reset_date': formatted_last_reset,
        'thread_alive': is_alive,
        'reset_hour': f"{RESET_HOUR:02d}:{RESET_MINUTE:02d}"
    }

"""Servicio de reinicio de tareas semanales como proceso independiente.

Este módulo proporciona funciones para iniciar un hilo (thread) que ejecuta
el reinicio de tareas semanales automáticamente los lunes a las 04:00 AM.
"""
import threading
import time
import logging
from datetime import datetime, timedelta
from models_tasks import Task, TaskFrequency, TaskStatus, TaskInstance, WeekDay
from app import db

# Configuración de logging
logging.basicConfig(level=logging.INFO, 
                   format='[%(asctime)s] [%(levelname)s] %(message)s',
                   datefmt='%Y-%m-%d %H:%M:%S')
logger = logging.getLogger(__name__)

# No necesitamos un intervalo fijo de verificación, ya que calcularemos
# exactamente cuánto tiempo falta hasta el próximo lunes

# Hora del día para ejecutar el reinicio (formato 24h)
RESET_HOUR = 4  # 04:00 AM
RESET_MINUTE = 0

# Variables globales para controlar el estado del servicio
service_thread = None
service_running = False
last_run_time = None
service_active = False
last_reset_date = None

def reset_weekly_tasks():
    """
    Reinicia el estado de las tareas semanales.
    Modifica el campo 'current_week_completed' a False para todas las tareas semanales.
    
    Returns:
        int: Número de tareas actualizadas
    """
    global last_reset_date
    
    try:
        # Obtener todas las tareas semanales marcadas como completadas
        tasks = Task.query.filter_by(
            frequency=TaskFrequency.SEMANAL,
            current_week_completed=True
        ).all()
        
        if not tasks:
            logger.info("No hay tareas semanales completadas para reiniciar")
            last_reset_date = datetime.now().date()
            return 0
            
        count = 0
        # Reiniciar el estado de cada tarea
        for task in tasks:
            task.current_week_completed = False
            count += 1
            
        # Guardar los cambios
        db.session.commit()
        
        # Actualizar la fecha de último reinicio
        last_reset_date = datetime.now().date()
        
        logger.info(f"Se reiniciaron {count} tareas semanales para la nueva semana")
        return count
        
    except Exception as e:
        logger.error(f"Error al reiniciar tareas semanales: {str(e)}")
        db.session.rollback()
        return 0

def process_custom_tasks_for_week():
    """
    Procesa las tareas personalizadas para la semana entrante.
    Crea instancias de tareas para los días correspondientes de la semana.
    
    Returns:
        int: Número de instancias de tareas creadas
    """
    try:
        # Obtener la fecha actual y fechas de la semana
        today = datetime.now().date()
        # Si hoy es lunes, start_of_week es hoy
        # Si no, calculamos la fecha del lunes de esta semana
        if today.weekday() == 0:  # 0 = lunes
            start_of_week = today
        else:
            # No debería ocurrir porque solo ejecutamos los lunes, pero por si acaso
            days_since_monday = today.weekday()
            start_of_week = today - timedelta(days=days_since_monday)
        
        # Calcular las fechas para cada día de la semana
        week_dates = {
            0: start_of_week,                       # Lunes
            1: start_of_week + timedelta(days=1),   # Martes
            2: start_of_week + timedelta(days=2),   # Miércoles
            3: start_of_week + timedelta(days=3),   # Jueves
            4: start_of_week + timedelta(days=4),   # Viernes
            5: start_of_week + timedelta(days=5),   # Sábado
            6: start_of_week + timedelta(days=6)    # Domingo
        }
        
        # Obtener todas las tareas personalizadas activas
        custom_tasks = Task.query.filter_by(
            frequency=TaskFrequency.PERSONALIZADA,
            status=TaskStatus.PENDIENTE
        ).filter(
            (Task.end_date.is_(None)) | (Task.end_date >= today)
        ).filter(
            Task.start_date <= today
        ).all()
        
        if not custom_tasks:
            logger.info("No hay tareas personalizadas para procesar esta semana")
            return 0
        
        instances_created = 0
        # Para cada tarea personalizada
        for task in custom_tasks:
            # Obtener los días configurados
            weekdays = task.weekdays
            if not weekdays:
                continue
                
            # Para cada día configurado
            for weekday_entry in weekdays:
                # Obtener el índice del día de la semana (0-6 donde 0 es lunes)
                day_index = {
                    WeekDay.LUNES: 0,
                    WeekDay.MARTES: 1,
                    WeekDay.MIERCOLES: 2,
                    WeekDay.JUEVES: 3,
                    WeekDay.VIERNES: 4,
                    WeekDay.SABADO: 5,
                    WeekDay.DOMINGO: 6
                }[weekday_entry.day_of_week]
                
                # Obtener la fecha correspondiente
                target_date = week_dates[day_index]
                
                # Verificar si ya existe una instancia para esta tarea en esta fecha
                existing_instance = TaskInstance.query.filter_by(
                    task_id=task.id,
                    scheduled_date=target_date
                ).first()
                
                # Si no existe, crear una nueva instancia
                if not existing_instance:
                    new_instance = TaskInstance(
                        task_id=task.id,
                        scheduled_date=target_date,
                        status=TaskStatus.PENDIENTE
                    )
                    db.session.add(new_instance)
                    instances_created += 1
                    logger.debug(f"Creada instancia de tarea '{task.title}' para {target_date}")
        
        # Guardar todos los cambios
        if instances_created > 0:
            db.session.commit()
            logger.info(f"Se crearon {instances_created} instancias de tareas personalizadas para esta semana")
        
        return instances_created
        
    except Exception as e:
        logger.error(f"Error al procesar tareas personalizadas: {str(e)}")
        db.session.rollback()
        return 0

def should_run_reset():
    """
    Determina si es momento de ejecutar el reinicio de tareas semanales.
    El reinicio debe ejecutarse si es lunes y la hora actual es igual o posterior a RESET_HOUR:RESET_MINUTE.
    Además, se verifica que no se haya ejecutado ya hoy.
    
    Returns:
        bool: True si se debe ejecutar el reinicio, False en caso contrario.
    """
    now = datetime.now()
    today = now.date()
    
    # Si ya lo hicimos hoy, no volver a ejecutar
    if last_reset_date == today:
        return False
    
    # Solo los lunes (0 = lunes en Python)
    if today.weekday() != 0:
        return False
    
    # Solo a partir de la hora configurada
    target_time = now.replace(hour=RESET_HOUR, minute=RESET_MINUTE, second=0, microsecond=0)
    return now >= target_time

def calculate_sleep_time():
    """
    Calcula el tiempo exacto que debe dormir el servicio hasta el próximo lunes a las 04:00 AM.
    
    Returns:
        float: Tiempo en segundos hasta el próximo lunes a las 04:00 AM
    """
    now = datetime.now()
    today = now.date()
    
    # Si hoy es lunes y aún no llega la hora de reinicio
    if today.weekday() == 0 and now.hour < RESET_HOUR:
        target_time = now.replace(hour=RESET_HOUR, minute=RESET_MINUTE, second=0, microsecond=0)
        return (target_time - now).total_seconds()
    
    # En cualquier otro caso, calcular días hasta el próximo lunes
    days_until_monday = (7 - today.weekday()) % 7
    if days_until_monday == 0:  # Hoy es lunes pero ya pasó la hora de reinicio
        days_until_monday = 7
    
    # Calcular el próximo lunes a las 04:00 AM
    next_reset = now + timedelta(days=days_until_monday)
    next_reset = next_reset.replace(hour=RESET_HOUR, minute=RESET_MINUTE, second=0, microsecond=0)
    
    # Devolver el tiempo exacto en segundos
    return (next_reset - now).total_seconds()

def weekly_tasks_reset_worker():
    """
    Función que ejecuta el reinicio de tareas semanales los lunes.
    """
    global service_running, last_run_time, service_active
    
    # Importar la aplicación Flask
    from app import create_app
    app = create_app()
    
    logger.info("Iniciando servicio de reinicio de tareas semanales")
    service_active = True
    
    try:
        while service_running:
            try:
                # Verificar si es momento de ejecutar el reinicio
                if should_run_reset():
                    logger.info(f"Es lunes después de las {RESET_HOUR}:{RESET_MINUTE} - Ejecutando reinicio de tareas semanales")
                    
                    # Usar el contexto de la aplicación para operaciones de base de datos
                    with app.app_context():
                        # Realizar el reinicio de tareas semanales
                        tasks_reset_count = reset_weekly_tasks()
                        if tasks_reset_count > 0:
                            logger.info(f"Reinicio exitoso: {tasks_reset_count} tareas semanales actualizadas")
                        else:
                            logger.info("No se reiniciaron tareas (no hay tareas completadas o ya se hizo hoy)")
                        
                        # Procesar tareas personalizadas para la semana
                        custom_tasks_count = process_custom_tasks_for_week()
                        if custom_tasks_count > 0:
                            logger.info(f"Procesamiento exitoso: {custom_tasks_count} instancias de tareas personalizadas creadas")
                        else:
                            logger.info("No se crearon instancias de tareas personalizadas para esta semana")
                else:
                    logger.debug("No es momento de ejecutar el reinicio de tareas semanales")
                
                # Actualizar el tiempo de la última ejecución
                last_run_time = datetime.now()
                
                # Calcular el tiempo de espera hasta la próxima verificación
                sleep_time = calculate_sleep_time()
                next_check_time = datetime.now() + timedelta(seconds=sleep_time)
                logger.info(f"Próxima verificación en {sleep_time/60:.1f} minutos ({next_check_time.strftime('%Y-%m-%d %H:%M:%S')})")
                time.sleep(sleep_time)
                
            except Exception as e:
                logger.error(f"Error durante la ejecución del reinicio de tareas: {str(e)}")
                # Dormir 1 hora en caso de error
                time.sleep(60 * 60)
    
    except Exception as e:
        logger.error(f"Error fatal en el servicio de reinicio de tareas: {str(e)}")
    finally:
        service_active = False
        logger.info("Servicio de reinicio de tareas semanales detenido")


def start_weekly_tasks_reset_service():
    """
    Inicia el servicio de reinicio de tareas semanales en un hilo separado.
    
    Returns:
        bool: True si el servicio se inició correctamente, False en caso contrario.
    """
    global service_thread, service_running, service_active
    
    if service_thread is not None and service_thread.is_alive():
        logger.info("El servicio de reinicio de tareas ya está en ejecución")
        return False
    
    # Si el hilo anterior existe pero está muerto, limpiar la referencia
    if service_thread is not None and not service_thread.is_alive():
        service_thread = None
        logger.warning("Se detectó un hilo anterior muerto - Limpiando referencia")
    
    # Iniciar el servicio
    service_running = True
    service_thread = threading.Thread(target=weekly_tasks_reset_worker, daemon=True)
    service_thread.start()
    
    # Esperar a que el servicio se inicie completamente
    timeout = 5  # 5 segundos máximo de espera
    start_time = time.time()
    while not service_active and time.time() - start_time < timeout:
        time.sleep(0.1)
    
    if service_active:
        logger.info("Servicio de reinicio de tareas semanales iniciado correctamente")
        return True
    else:
        logger.error("No se pudo iniciar el servicio de reinicio de tareas semanales")
        return False


def stop_weekly_tasks_reset_service():
    """
    Detiene el servicio de reinicio de tareas semanales.
    
    Returns:
        bool: True si el servicio se detuvo correctamente, False en caso contrario.
    """
    global service_thread, service_running, service_active
    
    if service_thread is None or not service_thread.is_alive():
        logger.info("El servicio de reinicio de tareas no está en ejecución")
        service_running = False
        service_active = False
        return False
    
    # Detener el hilo
    service_running = False
    
    # Esperar a que el hilo termine
    timeout = 5  # 5 segundos máximo de espera
    start_time = time.time()
    while service_active and time.time() - start_time < timeout:
        time.sleep(0.1)
    
    if not service_active:
        logger.info("Servicio de reinicio de tareas detenido correctamente")
        return True
    else:
        logger.warning("No se pudo detener el servicio correctamente")
        return False


def get_service_status():
    """
    Obtiene el estado actual del servicio de reinicio de tareas semanales.
    
    Returns:
        dict: Diccionario con información sobre el estado del servicio.
    """
    global service_thread, service_running, last_run_time, service_active, last_reset_date
    
    is_alive = service_thread is not None and service_thread.is_alive()
    
    # Formatear los tiempos para mostrarlos de forma amigable
    formatted_last_run = "No ejecutado aún" if last_run_time is None else last_run_time.strftime('%Y-%m-%d %H:%M:%S')
    formatted_last_reset = "No reiniciado aún" if last_reset_date is None else last_reset_date.strftime('%Y-%m-%d')
    
    # Calcular el próximo lunes 04:00
    now = datetime.now()
    days_until_monday = (7 - now.weekday()) % 7
    if days_until_monday == 0 and now.hour >= RESET_HOUR:  # Es lunes y ya pasó la hora
        days_until_monday = 7
    
    next_reset = now + timedelta(days=days_until_monday)
    next_reset = next_reset.replace(hour=RESET_HOUR, minute=RESET_MINUTE, second=0, microsecond=0)
    formatted_next_reset = next_reset.strftime('%Y-%m-%d %H:%M:%S')
    
    return {
        'active': is_alive and service_active,
        'running': service_running,
        'last_run': formatted_last_run,
        'next_reset': formatted_next_reset,
        'last_reset_date': formatted_last_reset,
        'thread_alive': is_alive,
        'reset_hour': f"{RESET_HOUR:02d}:{RESET_MINUTE:02d}"
    }

import os
import time
import threading
from datetime import datetime, date, timedelta
import logging
from flask import current_app
from models_tasks import Task, TaskInstance, TaskCompletion, TaskFrequency, TaskStatus, Location
from calendar import monthrange
import random

# Evitamos la importación circular
from app import db

# Configuración de logging
logging.basicConfig(
    filename='task_scheduler.log',
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger('task_scheduler')

# Archivo para indicar que el servicio está en ejecución
STARTUP_FILE = '.task_scheduler_startup'

# Variable para controlar si el servicio sigue ejecutándose
keep_running = True

# Señalización de inicialización completada
initialization_complete = False

def create_startup_file():
    """Crea el archivo que indica que el servicio está en ejecución"""
    with open(STARTUP_FILE, 'w') as f:
        f.write(str(datetime.now()))

def check_startup_file():
    """Verifica si el archivo de inicio existe"""
    return os.path.exists(STARTUP_FILE)

def create_task_instance(task, scheduled_date):
    """Crea una instancia de tarea para una fecha específica si no existe"""
    # Verificar si ya existe una instancia para esta tarea y fecha
    existing = TaskInstance.query.filter_by(
        task_id=task.id, 
        scheduled_date=scheduled_date
    ).first()
    
    if not existing:
        # Crear nueva instancia
        instance = TaskInstance(
            task_id=task.id,
            scheduled_date=scheduled_date,
            status=TaskStatus.PENDIENTE
        )
        db.session.add(instance)
        db.session.commit()
        logger.info(f"✅ TAREA INSTANCIADA: '{task.title}' (ID: {task.id}) para fecha {scheduled_date}")
        return True
    return False

def process_tasks_for_location(location, target_date=None):
    """Procesa todas las tareas de una ubicación para las próximas 4 semanas
    
    Args:
        location: Objeto Location a procesar
        target_date: Fecha de inicio para generar instancias (por defecto, hoy)
    """
    location_id = location.id
    location_name = location.name
    
    # Si no se especifica fecha, usamos la fecha actual
    start_date = target_date if target_date else date.today()
    
    # Generamos tareas para las próximas 4 semanas (28 días)
    num_days = 28
    
    logger.info(f"🔄 PROCESANDO UBICACIÓN: {location_name} (ID: {location_id}) para los próximos {num_days} días")
    
    # Contador para cada tipo de tarea
    counters = {
        'diaria': 0,
        'dia_lunes': 0,
        'dia_martes': 0,
        'dia_miercoles': 0,
        'dia_jueves': 0,
        'dia_viernes': 0,
        'dia_sabado': 0,
        'dia_domingo': 0,
        'semanal': 0,
        'quincenal': 0,
        'mensual': 0,
        'personalizada': 0
    }
    
    try:
        # Generamos tareas para cada día en las próximas 4 semanas
        for day_offset in range(num_days):
            process_date = start_date + timedelta(days=day_offset)
            # Día de la semana (0=Lunes, 6=Domingo)
            weekday = process_date.weekday()
            
            # Procesar tareas DIARIAS para este día
            daily_tasks = Task.query.filter_by(frequency=TaskFrequency.DIARIA, location_id=location_id).filter(
                (Task.end_date.is_(None)) | (Task.end_date >= process_date)
            ).filter(Task.start_date <= process_date).all()
            
            for task in daily_tasks:
                # Siempre creamos una instancia para tareas diarias
                if create_task_instance(task, process_date):
                    counters['diaria'] += 1
            
            # Procesar tareas para DÍAS ESPECÍFICOS (lunes, martes, etc.)
            # Mapeo de día de semana (0-6) a enum TaskFrequency
            day_specific_map = {
                0: TaskFrequency.DIA_LUNES,
                1: TaskFrequency.DIA_MARTES,
                2: TaskFrequency.DIA_MIERCOLES,
                3: TaskFrequency.DIA_JUEVES,
                4: TaskFrequency.DIA_VIERNES,
                5: TaskFrequency.DIA_SABADO,
                6: TaskFrequency.DIA_DOMINGO
            }
            
            # Mapeo de día de semana (0-6) a contador
            counter_map = {
                0: 'dia_lunes',
                1: 'dia_martes',
                2: 'dia_miercoles',
                3: 'dia_jueves',
                4: 'dia_viernes',
                5: 'dia_sabado',
                6: 'dia_domingo'
            }
            
            # Obtener tareas para el día específico de la semana
            day_specific_freq = day_specific_map[weekday]
            day_specific_tasks = Task.query.filter_by(frequency=day_specific_freq, location_id=location_id).filter(
                (Task.end_date.is_(None)) | (Task.end_date >= process_date)
            ).filter(Task.start_date <= process_date).all()
            
            for task in day_specific_tasks:
                # Crear instancia para tareas de día específico
                if create_task_instance(task, process_date):
                    counters[counter_map[weekday]] += 1
                    
            # Procesar tareas SEMANALES
            # Las tareas semanales deben aparecer todos los días hasta que se completen en esa semana
            weekly_tasks = Task.query.filter_by(frequency=TaskFrequency.SEMANAL, location_id=location_id).filter(
                (Task.end_date.is_(None)) | (Task.end_date >= process_date)
            ).filter(Task.start_date <= process_date).all()
            
            for task in weekly_tasks:
                # Calcular el inicio de la semana actual (lunes)
                monday_of_week = process_date - timedelta(days=process_date.weekday())
                
                # Verificar si ya hay una tarea completada en esta semana
                completed_this_week = TaskInstance.query.filter(
                    TaskInstance.task_id == task.id,
                    TaskInstance.status == TaskStatus.COMPLETADA,
                    TaskInstance.scheduled_date >= monday_of_week,
                    TaskInstance.scheduled_date <= monday_of_week + timedelta(days=6)
                ).first()
                
                # Si ya está completada para esta semana, no crear nuevas instancias
                if completed_this_week:
                    continue
                    
                # Si no está completada en esta semana, crear instancia para hoy
                if create_task_instance(task, process_date):
                    counters['semanal'] += 1
        
            # Procesar tareas QUINCENALES
            # Las tareas quincenales deben aparecer todos los días hasta que se completen en esa quincena
            biweekly_tasks = Task.query.filter_by(frequency=TaskFrequency.QUINCENAL, location_id=location_id).filter(
                (Task.end_date.is_(None)) | (Task.end_date >= process_date)
            ).filter(Task.start_date <= process_date).all()
            
            for task in biweekly_tasks:
                # Determinar si estamos en la primera o segunda quincena
                if process_date.day <= 15:
                    # Primera quincena: días 1-15
                    period_start = date(process_date.year, process_date.month, 1)
                    period_end = date(process_date.year, process_date.month, 15)
                else:
                    # Segunda quincena: días 16-fin de mes
                    period_start = date(process_date.year, process_date.month, 16)
                    # Último día del mes
                    next_month = process_date.month + 1 if process_date.month < 12 else 1
                    next_month_year = process_date.year if process_date.month < 12 else process_date.year + 1
                    period_end = date(next_month_year, next_month, 1) - timedelta(days=1)
                
                # Verificar si ya hay una tarea completada en esta quincena
                completed_this_period = TaskInstance.query.filter(
                    TaskInstance.task_id == task.id,
                    TaskInstance.status == TaskStatus.COMPLETADA,
                    TaskInstance.scheduled_date >= period_start,
                    TaskInstance.scheduled_date <= period_end
                ).first()
                
                # Si ya está completada para esta quincena, no crear nuevas instancias
                if completed_this_period:
                    continue
                    
                # Si no está completada en esta quincena, crear instancia para hoy
                if create_task_instance(task, process_date):
                    counters['quincenal'] += 1
        
            # Procesar tareas MENSUALES
            # Las tareas mensuales deben aparecer todos los días hasta que se completen en ese mes
            monthly_tasks = Task.query.filter_by(frequency=TaskFrequency.MENSUAL, location_id=location_id).filter(
                (Task.end_date.is_(None)) | (Task.end_date >= process_date)
            ).filter(Task.start_date <= process_date).all()
            
            for task in monthly_tasks:
                # Determinar el periodo del mes actual
                month_start = date(process_date.year, process_date.month, 1)
                # Último día del mes
                next_month = process_date.month + 1 if process_date.month < 12 else 1
                next_month_year = process_date.year if process_date.month < 12 else process_date.year + 1
                month_end = date(next_month_year, next_month, 1) - timedelta(days=1)
                
                # Verificar si ya hay una tarea completada en este mes
                completed_this_month = TaskInstance.query.filter(
                    TaskInstance.task_id == task.id,
                    TaskInstance.status == TaskStatus.COMPLETADA,
                    TaskInstance.scheduled_date >= month_start,
                    TaskInstance.scheduled_date <= month_end
                ).first()
                
                # Si ya está completada para este mes, no crear nuevas instancias
                if completed_this_month:
                    continue
                    
                # Si no está completada en este mes, crear instancia para hoy
                if create_task_instance(task, process_date):
                    counters['mensual'] += 1
        
            # Procesar tareas PERSONALIZADAS (con días de la semana específicos)
            custom_tasks = Task.query.filter_by(frequency=TaskFrequency.PERSONALIZADA, location_id=location_id).filter(
                (Task.end_date.is_(None)) | (Task.end_date >= process_date)
            ).filter(Task.start_date <= process_date).all()
            
            for task in custom_tasks:
                if not task.weekdays:
                    continue
                    
                # Verificar si alguno de los días configurados coincide con la fecha de proceso
                process_date_weekday = process_date.weekday()  # 0=Lunes, 6=Domingo
                
                # Crear mapeo de nombres de días a números
                day_map = {
                    'lunes': 0, 'martes': 1, 'miercoles': 2, 'jueves': 3,
                    'viernes': 4, 'sabado': 5, 'domingo': 6
                }
                
                task_matches_today = False
                for weekday in task.weekdays:
                    weekday_value = weekday.day_of_week.value.lower()
                    if day_map.get(weekday_value) == process_date_weekday:
                        task_matches_today = True
                        break
                
                # Si no coincide con el día actual, no crear instancia
                if not task_matches_today:
                    continue
                
                # Calcular el inicio de la semana actual (lunes)
                monday_of_week = process_date - timedelta(days=process_date.weekday())
                
                # Verificar si ya hay una tarea completada en esta semana
                completed_this_week = TaskInstance.query.filter(
                    TaskInstance.task_id == task.id,
                    TaskInstance.status == TaskStatus.COMPLETADA,
                    TaskInstance.scheduled_date >= monday_of_week,
                    TaskInstance.scheduled_date <= monday_of_week + timedelta(days=6)
                ).first()
                
                # Si ya está completada para esta semana, no crear nuevas instancias
                if completed_this_week:
                    continue
                    
                # Si no está completada en esta semana, crear instancia para hoy
                if create_task_instance(task, process_date):
                    counters['personalizada'] += 1
        
        # Mostrar resultados de procesamiento
        total_instances = sum(counters.values())
        if total_instances > 0:
            logger.info(f"✅ UBICACIÓN PROCESADA: {location_name} (ID: {location_id}) - Creadas {total_instances} instancias:")
            logger.info(f"   ○ Diarias: {counters['diaria']}")
            logger.info(f"   ○ Semanales: {counters['semanal']}")
            logger.info(f"   ○ Quincenales: {counters['biweekly']}")
            logger.info(f"   ○ Mensuales: {counters['monthly']}")
            logger.info(f"   ○ Personalizadas: {counters['custom']}")
        else:
            logger.info(f"⚠️ UBICACIÓN SIN CAMBIOS: {location_name} (ID: {location_id}) - No se crearon nuevas instancias")
        
        return counters
    
    except Exception as e:
        logger.error(f"Error procesando ubicación {location_name} (ID: {location_id}): {str(e)}")
        db.session.rollback()
        return counters

def run_task_scheduler_for_location(location_id=None):
    """Función principal que ejecuta el programador de tareas, opcionalmente solo para una ubicación específica
    
    Esta función genera instancias de tareas para:
    1. El día actual
    2. Los próximos 7 días
    
    Args:
        location_id: ID de la ubicación para la que ejecutar el programador. Si es None, se ejecuta para todas.
    """
    # Importar Flask para obtener app
    from app import app
    
    # Banner de inicio en el log con estilo mejorado
    logger.info("\n" + "=" * 100)
    if location_id:
        title = "PROGRAMADOR DE TAREAS - EJECUCIÓN PARA UBICACIÓN ESPECÍFICA"
        logger.info(f"{'=' * 10} 🔄 {title} 🔄 {'=' * 10}")
    else:
        title = "PROGRAMADOR DE TAREAS - EJECUCIÓN GLOBAL"
        logger.info(f"{'=' * 15} 🔄 {title} 🔄 {'=' * 15}")
    logger.info("=" * 100 + "\n")
    
    start_time = datetime.now()
    
    with app.app_context():
        # Si se especifica una ubicación, indicarlo en el log
        if location_id:
            location = Location.query.get(location_id)
            if not location:
                logger.error(f"❌ ERROR: No se encontró la ubicación con ID {location_id}")
                print(f"\n❌ Error: No se encontró la ubicación con ID {location_id}\n")
                return
            location_info = f"{location.name} (ID: {location_id})"
            logger.info(f"✅ INICIANDO PROGRAMADOR ESPECÍFICO para ubicación: {location_info}")
            logger.info(f"Hora de inicio: {start_time}")
        else:
            logger.info(f"✅ INICIANDO PROGRAMADOR GLOBAL para TODAS las ubicaciones")
            logger.info(f"Hora de inicio: {start_time}")
        
        # Variables para contador de tareas
        total_tasks_created = 0
        total_tasks_by_type = {
            'daily': 0,
            'weekly': 0,
            'biweekly': 0,
            'monthly': 0,
            'custom': 0
        }
        total_locations_processed = 0
        total_locations_with_tasks = 0
        
        # Obtener fecha actual y próximos 7 días
        today = date.today()
        dates_to_process = [today + timedelta(days=i) for i in range(8)]  # Hoy + 7 días
        
        logger.info(f"Procesando {len(dates_to_process)} fechas: de {dates_to_process[0]} a {dates_to_process[-1]}")
        
        # Determinar qué ubicaciones procesar
        if location_id:
            locations = [location]
        else:
            # Obtener todas las ubicaciones activas
            locations = Location.query.filter_by(is_active=True).all()
            
        logger.info(f"Procesando {len(locations)} ubicaciones")
        
        # Procesar cada ubicación para el día actual
        logger.info(f"Procesando tareas para el día actual: {today}")
        for location in locations:
            try:
                # Añadir una pequeña espera aleatoria para evitar concurrencia
                time.sleep(random.uniform(0.1, 0.5))
                
                location_has_tasks = False
                
                for process_date in dates_to_process:
                    counters = process_tasks_for_location(location, process_date)
                    
                    # Actualizar contadores
                    task_count = sum(counters.values())
                    total_tasks_created += task_count
                    
                    for task_type, count in counters.items():
                        total_tasks_by_type[task_type] += count
                    
                    if task_count > 0:
                        location_has_tasks = True
                
                total_locations_processed += 1
                if location_has_tasks:
                    total_locations_with_tasks += 1
                    
            except Exception as e:
                logger.error(f"Error procesando ubicación {location.name}: {str(e)}")
                continue
        
        # Mostrar resumen general
        end_time = datetime.now()
        duration = end_time - start_time
        duration_seconds = duration.total_seconds()
        
        logger.info("\n" + "=" * 100)
        logger.info("RESUMEN DE EJECUCIÓN DEL PROGRAMADOR DE TAREAS")
        logger.info("=" * 100)
        logger.info(f"Fecha de inicio: {start_time}")
        logger.info(f"Fecha de fin: {end_time}")
        logger.info(f"Duración: {duration_seconds:.2f} segundos")
        logger.info(f"Ubicaciones procesadas: {total_locations_processed} de {len(locations)}")
        logger.info(f"Ubicaciones con tareas: {total_locations_with_tasks}")
        logger.info(f"Total de instancias de tareas creadas: {total_tasks_created}")
        logger.info(f"  ○ Tareas diarias: {total_tasks_by_type['daily']}")
        logger.info(f"  ○ Tareas semanales: {total_tasks_by_type['weekly']}")
        logger.info(f"  ○ Tareas quincenales: {total_tasks_by_type['biweekly']}")
        logger.info(f"  ○ Tareas mensuales: {total_tasks_by_type['monthly']}")
        logger.info(f"  ○ Tareas personalizadas: {total_tasks_by_type['custom']}")
        logger.info("=" * 100 + "\n")
        
        return total_tasks_created

def run_task_scheduler():
    """Función que ejecuta el programador de tareas para todas las ubicaciones (para compatibilidad)"""
    return run_task_scheduler_for_location()

def schedule_next_run():
    """Programa la próxima ejecución para las 6:00 AM"""
    now = datetime.now()
    tomorrow = now.replace(hour=6, minute=0, second=0, microsecond=0) + timedelta(days=1)
    seconds_until = (tomorrow - now).total_seconds()
    hours_until = seconds_until / 3600
    
    logger.info(f"Próxima ejecución programada para: {tomorrow} (en {hours_until:.2f} horas)")
    return seconds_until

def service_loop():
    """Bucle principal del servicio"""
    global keep_running, initialization_complete
    
    logger.info("\n" + "=" * 100)
    logger.info("==================== 🕐 INICIANDO SERVICIO DE PROGRAMADOR DE TAREAS 🕐 ====================")
    logger.info("=" * 100 + "\n")
    
    logger.info("Iniciando servicio de programación de tareas")
    
    # Verificar si el servicio ya estaba en ejecución
    if check_startup_file():
        logger.info("Continuando ejecución del servicio - archivo de inicio encontrado")
    else:
        logger.info("Primera ejecución - creando archivo de inicio")
        create_startup_file()
    
    # Programar el tiempo para la próxima ejecución
    seconds_until_next_run = schedule_next_run()
    
    # Señalizar que la inicialización está completa
    initialization_complete = True
    
    # Ejecutar el programa principal
    while keep_running:
        try:
            # Dormir hasta la próxima ejecución programada
            time.sleep(seconds_until_next_run)
            
            if not keep_running:
                break
            
            # Ejecutar el programador de tareas para todas las ubicaciones
            logger.info("Ejecutando programador de tareas programado")
            run_task_scheduler_for_location()
            
            # Reprogramar para la próxima ejecución
            seconds_until_next_run = schedule_next_run()
            
        except Exception as e:
            logger.error(f"Error en el bucle de servicio: {str(e)}")
            seconds_until_next_run = 3600  # Si hay un error, intentar de nuevo en 1 hora
    
    logger.info("Servicio de programación de tareas detenido")

def start_service():
    """Inicia el servicio en un hilo separado"""
    global keep_running
    keep_running = True
    
    # Iniciar el servicio en un hilo separado
    service_thread = threading.Thread(target=service_loop, daemon=True)
    service_thread.start()
    
    # Esperar a que la inicialización se complete
    while not initialization_complete:
        time.sleep(0.1)
    
    logger.info("Servicio de programación de tareas iniciado")
    
    return True

def stop_service():
    """Detiene el servicio"""
    global keep_running
    keep_running = False
    
    logger.info("Deteniendo servicio de programación de tareas")
    
    return True

# Si se ejecuta directamente, iniciar el servicio
if __name__ == "__main__":
    # Configurar logging para la consola
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)
    logger.addHandler(console_handler)
    
    # Iniciar el servicio
    start_service()
    
    # Mantener el proceso principal activo
    try:
        while True:
            time.sleep(60)
    except KeyboardInterrupt:
        stop_service()
        print("Servicio detenido por el usuario")

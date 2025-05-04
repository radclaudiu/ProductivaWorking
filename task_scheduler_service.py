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
        logger.info(f"Nueva instancia creada para tarea {task.id} ({task.title}) en fecha {scheduled_date}")
        return True
    return False

def process_tasks_for_location(location, target_date=None):
    """Procesa todas las tareas de una ubicación para una fecha específica
    
    Args:
        location: Objeto Location a procesar
        target_date: Fecha objetivo para la que generar instancias (por defecto, hoy)
    """
    location_id = location.id
    location_name = location.name
    
    # Si no se especifica fecha, usamos la fecha actual
    process_date = target_date if target_date else date.today()
    
    logger.info(f"Procesando ubicación: {location_name} (ID: {location_id}) para fecha {process_date}")
    
    # Contador para cada tipo de tarea
    counters = {
        'daily': 0,
        'weekly': 0,
        'biweekly': 0,
        'monthly': 0,
        'custom': 0
    }
    
    try:
        # Procesar tareas diarias
        daily_tasks = Task.query.filter_by(frequency=TaskFrequency.DIARIA, location_id=location_id).filter(
            (Task.end_date.is_(None)) | (Task.end_date >= process_date)
        ).filter(Task.start_date <= process_date).all()
        
        for task in daily_tasks:
            if create_task_instance(task, process_date):
                counters['daily'] += 1
        
        # Procesar tareas semanales
        weekly_tasks = Task.query.filter_by(frequency=TaskFrequency.SEMANAL, location_id=location_id).filter(
            (Task.end_date.is_(None)) | (Task.end_date >= process_date)
        ).filter(Task.start_date <= process_date).all()
        
        for task in weekly_tasks:
            # Calcular el lunes y domingo de la semana actual
            monday_of_week = process_date - timedelta(days=process_date.weekday())
            sunday_of_week = monday_of_week + timedelta(days=6)
            
            # Verificar si ya existe una instancia activa para esta semana
            existing_this_week = TaskInstance.query.filter_by(task_id=task.id).filter(
                TaskInstance.scheduled_date >= monday_of_week,
                TaskInstance.scheduled_date <= sunday_of_week
            ).first()
            
            if not existing_this_week:
                # Obtener la última completación
                last_completion = TaskCompletion.query.filter_by(task_id=task.id).order_by(
                    TaskCompletion.completion_date.desc()
                ).first()
                
                should_create_instance = False
                
                if not last_completion:
                    # Si nunca se ha completado, crear instancia
                    should_create_instance = True
                else:
                    # Si la última completación fue en una semana anterior, crear instancia
                    last_date = last_completion.completion_date.date()
                    last_monday = last_date - timedelta(days=last_date.weekday())
                    
                    if last_monday < monday_of_week:
                        should_create_instance = True
                
                if should_create_instance and create_task_instance(task, process_date):
                    counters['weekly'] += 1
        
        # Procesar tareas quincenales
        biweekly_tasks = Task.query.filter_by(frequency=TaskFrequency.QUINCENAL, location_id=location_id).filter(
            (Task.end_date.is_(None)) | (Task.end_date >= process_date)
        ).filter(Task.start_date <= process_date).all()
        
        for task in biweekly_tasks:
            # Determinar la quincena actual
            if process_date.day < 16:
                # Primera quincena (1-15)
                fortnight_start = date(process_date.year, process_date.month, 1)
                fortnight_end = date(process_date.year, process_date.month, 15)
            else:
                # Segunda quincena (16-fin)
                fortnight_start = date(process_date.year, process_date.month, 16)
                last_day = monthrange(process_date.year, process_date.month)[1]
                fortnight_end = date(process_date.year, process_date.month, last_day)
            
            # Verificar si ya existe una instancia activa para esta quincena
            existing_this_fortnight = TaskInstance.query.filter_by(task_id=task.id).filter(
                TaskInstance.scheduled_date >= fortnight_start,
                TaskInstance.scheduled_date <= fortnight_end
            ).first()
            
            if not existing_this_fortnight:
                # Obtener la última completación
                last_completion = TaskCompletion.query.filter_by(task_id=task.id).order_by(
                    TaskCompletion.completion_date.desc()
                ).first()
                
                should_create_instance = False
                
                if not last_completion:
                    # Si nunca se ha completado, crear instancia
                    should_create_instance = True
                else:
                    # Determinar la quincena de la última completación
                    last_date = last_completion.completion_date.date()
                    if last_date.day < 16:
                        last_fortnight_start = date(last_date.year, last_date.month, 1)
                    else:
                        last_fortnight_start = date(last_date.year, last_date.month, 16)
                    
                    # Si la última completación fue en una quincena anterior, crear instancia
                    if last_fortnight_start < fortnight_start:
                        should_create_instance = True
                
                if should_create_instance and create_task_instance(task, process_date):
                    counters['biweekly'] += 1
        
        # Procesar tareas mensuales
        monthly_tasks = Task.query.filter_by(frequency=TaskFrequency.MENSUAL, location_id=location_id).filter(
            (Task.end_date.is_(None)) | (Task.end_date >= process_date)
        ).filter(Task.start_date <= process_date).all()
        
        for task in monthly_tasks:
            # Determinar el mes actual
            month_start = date(process_date.year, process_date.month, 1)
            last_day = monthrange(process_date.year, process_date.month)[1]
            month_end = date(process_date.year, process_date.month, last_day)
            
            # Verificar si ya existe una instancia activa para este mes
            existing_this_month = TaskInstance.query.filter_by(task_id=task.id).filter(
                TaskInstance.scheduled_date >= month_start,
                TaskInstance.scheduled_date <= month_end
            ).first()
            
            if not existing_this_month:
                # Obtener la última completación
                last_completion = TaskCompletion.query.filter_by(task_id=task.id).order_by(
                    TaskCompletion.completion_date.desc()
                ).first()
                
                should_create_instance = False
                
                if not last_completion:
                    # Si nunca se ha completado, crear instancia
                    should_create_instance = True
                else:
                    # Determinar el mes de la última completación
                    last_date = last_completion.completion_date.date()
                    last_month_start = date(last_date.year, last_date.month, 1)
                    
                    # Si la última completación fue en un mes anterior, crear instancia
                    if last_month_start < month_start:
                        should_create_instance = True
                
                if should_create_instance and create_task_instance(task, process_date):
                    counters['monthly'] += 1
        
        # Procesar tareas personalizadas
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
            
            for weekday in task.weekdays:
                weekday_value = weekday.day_of_week.value.lower()
                if day_map.get(weekday_value) == process_date_weekday:
                    if create_task_instance(task, process_date):
                        counters['custom'] += 1
                    break
        
        logger.info(f"Ubicación {location_name} (ID: {location_id}) procesada: "
                   f"Diarias={counters['daily']}, "
                   f"Semanales={counters['weekly']}, "
                   f"Quincenales={counters['biweekly']}, "
                   f"Mensuales={counters['monthly']}, "
                   f"Personalizadas={counters['custom']}")
        
        return counters
    
    except Exception as e:
        logger.error(f"Error procesando ubicación {location_name} (ID: {location_id}): {str(e)}")
        db.session.rollback()
        return counters

def run_task_scheduler():
    """Función principal que ejecuta el programador de tareas
    
    Esta función genera instancias de tareas para:
    1. El día actual
    2. Los próximos 7 días
    """
    # Banner de inicio en el log
    logger.info("*" * 80)
    logger.info("*" + " " * 28 + "PROGRAMADOR DE TAREAS" + " " * 28 + "*")
    logger.info("*" * 80)
    
    start_time = datetime.now()
    logger.info(f"Iniciando programador de tareas: {start_time}")
    
    # Contadores globales
    total_counters = {
        'daily': 0,
        'weekly': 0,
        'biweekly': 0,
        'monthly': 0,
        'custom': 0,
        'locations_processed': 0,
        'locations_with_errors': 0,
        'future_dates_processed': 0
    }
    
    try:
        # Obtener todas las ubicaciones activas
        locations = Location.query.filter_by(is_active=True).all()
        logger.info(f"Se encontraron {len(locations)} ubicaciones activas")
        
        # Fechas para las que generar tareas
        today = date.today()
        future_dates = [today + timedelta(days=i) for i in range(1, 8)]  # Próximos 7 días
        
        # Procesar cada ubicación para el día actual
        logger.info(f"Procesando tareas para el día actual: {today}")
        for location in locations:
            try:
                # Añadir una pequeña espera aleatoria para evitar concurrencia
                time.sleep(random.uniform(0.1, 0.5))
                
                # Procesar tareas para esta ubicación (día actual)
                counters = process_tasks_for_location(location, today)
                
                # Actualizar contadores globales
                total_counters['daily'] += counters['daily']
                total_counters['weekly'] += counters['weekly']
                total_counters['biweekly'] += counters['biweekly']
                total_counters['monthly'] += counters['monthly']
                total_counters['custom'] += counters['custom']
                total_counters['locations_processed'] += 1
                
            except Exception as e:
                logger.error(f"Error procesando ubicación {location.name} (ID: {location.id}): {str(e)}")
                total_counters['locations_with_errors'] += 1
                continue
        
        # Procesar cada ubicación para los próximos 7 días
        logger.info(f"Procesando tareas para los próximos 7 días ({future_dates[0]} - {future_dates[-1]})")
        for future_date in future_dates:
            logger.info(f"Procesando tareas para fecha futura: {future_date}")
            total_counters['future_dates_processed'] += 1
            
            for location in locations:
                try:
                    # Añadir una pequeña espera aleatoria para evitar concurrencia
                    time.sleep(random.uniform(0.1, 0.5))
                    
                    # Procesar tareas para esta ubicación (fecha futura)
                    future_counters = process_tasks_for_location(location, future_date)
                    
                    # Actualizar contadores globales (no sumamos al total principal)
                    logger.info(f"Creadas {sum(future_counters.values())} instancias para fecha {future_date}")
                    
                except Exception as e:
                    logger.error(f"Error procesando ubicación {location.name} para fecha futura {future_date}: {str(e)}")
                    continue
        
        end_time = datetime.now()
        duration = (end_time - start_time).total_seconds()
        
        # Resumen final
        total_tasks = (total_counters['daily'] + total_counters['weekly'] + 
                       total_counters['biweekly'] + total_counters['monthly'] + 
                       total_counters['custom'])
        
        # Banner de finalización en el log
        logger.info("*" * 80)
        logger.info("*" + " " * 23 + "PROGRAMADOR DE TAREAS COMPLETADO" + " " * 23 + "*")
        logger.info("*" * 80)
        
        logger.info(f"Programador de tareas completado en {duration:.2f} segundos")
        logger.info(f"Ubicaciones procesadas: {total_counters['locations_processed']} "
                   f"(Errores: {total_counters['locations_with_errors']})")
        logger.info(f"Total de tareas programadas: {total_tasks}")
        logger.info(f"Desglose: Diarias={total_counters['daily']}, "
                   f"Semanales={total_counters['weekly']}, "
                   f"Quincenales={total_counters['biweekly']}, "
                   f"Mensuales={total_counters['monthly']}, "
                   f"Personalizadas={total_counters['custom']}")
        
        logger.info("*" * 80)
        
        # Imprimir también en la consola para depuración
        print(f"\n✅ Programador de tareas completado - {total_tasks} tareas programadas")
        print(f"   • Ubicaciones: {total_counters['locations_processed']} (Errores: {total_counters['locations_with_errors']})")
        print(f"   • Tareas: Diarias={total_counters['daily']}, "
              f"Semanales={total_counters['weekly']}, "
              f"Quincenales={total_counters['biweekly']}, "
              f"Mensuales={total_counters['monthly']}, "
              f"Personalizadas={total_counters['custom']}")
        print(f"   • Tiempo: {duration:.2f} segundos\n")
    
    except Exception as e:
        logger.error(f"Error general en el programador de tareas: {str(e)}")
        print(f"\n❌ Error en el programador de tareas: {str(e)}\n")

def schedule_next_run():
    """Programa la próxima ejecución para las 6:00 AM"""
    now = datetime.now()
    
    # Definir el próximo tiempo objetivo (6:00 AM)
    if now.hour < 6:
        # Si es antes de las 6 AM, programar para hoy a las 6 AM
        target_time = datetime(now.year, now.month, now.day, 6, 0, 0)
    else:
        # Si es después de las 6 AM, programar para mañana a las 6 AM
        tomorrow = now + timedelta(days=1)
        target_time = datetime(tomorrow.year, tomorrow.month, tomorrow.day, 6, 0, 0)
    
    seconds_until_target = (target_time - now).total_seconds()
    
    logger.info(f"Próxima ejecución programada para: {target_time} "
               f"(en {seconds_until_target/3600:.2f} horas)")
    
    return seconds_until_target

def service_loop():
    """Bucle principal del servicio"""
    global initialization_complete
    
    # Verificar si el servicio debe arrancar desde cero o ya estaba ejecutándose
    first_run = not check_startup_file()
    if first_run:
        logger.info("Primera ejecución del servicio después del inicio")
        create_startup_file()
    else:
        logger.info("Continuando ejecución del servicio - archivo de inicio encontrado")
    
    # Ejecutar inmediatamente si es la primera vez o si se está recuperando de un reinicio
    if first_run:
        logger.info("Ejecutando programador de tareas en el primer inicio")
        try:
            # Utilizamos la función sin contexto ya que podría no estar disponible
            run_task_scheduler()
        except Exception as e:
            logger.error(f"Error al ejecutar programador de tareas: {str(e)}")
            print(f"Error en programador de tareas: {str(e)}")
    
    # Indicar que la inicialización está completa
    initialization_complete = True
    
    # Bucle principal del servicio
    while keep_running:
        try:
            # Calcular tiempo hasta la próxima ejecución
            sleep_time = schedule_next_run()
            
            # Dormir hasta el próximo tiempo programado
            time.sleep(sleep_time)
            
            # Ejecutar el programador de tareas
            logger.info("Ejecutando programador de tareas programado")
            try:
                run_task_scheduler()
            except Exception as e:
                logger.error(f"Error al ejecutar programador de tareas programado: {str(e)}")
                print(f"Error en programador programado: {str(e)}")
        
        except Exception as e:
            logger.error(f"Error en el bucle del servicio: {str(e)}")
            # En caso de error, esperar 30 minutos antes de reintentar
            time.sleep(1800)

def start_service():
    """Inicia el servicio en un hilo separado"""
    try:
        # Banner de inicio del servicio
        logger.info("*" * 80)
        logger.info("*" + " " * 20 + "INICIANDO SERVICIO DE PROGRAMADOR DE TAREAS" + " " * 20 + "*")
        logger.info("*" * 80)
        
        logger.info("Iniciando servicio de programación de tareas")
        
        # Crear y arrancar el hilo del servicio
        service_thread = threading.Thread(target=service_loop)
        service_thread.daemon = True
        service_thread.start()
        
        # Esperar a que la inicialización esté completa
        timeout = 30  # 30 segundos de timeout
        start_time = time.time()
        while not initialization_complete and time.time() - start_time < timeout:
            time.sleep(0.1)
        
        if not initialization_complete:
            logger.warning("Timeout esperando la inicialización del servicio")
        
        logger.info("Servicio de programación de tareas iniciado")
        return service_thread
    
    except Exception as e:
        logger.error(f"Error al iniciar el servicio: {str(e)}")
        raise

def stop_service():
    """Detiene el servicio"""
    global keep_running
    
    # Banner de detención del servicio
    logger.info("*" * 80)
    logger.info("*" + " " * 20 + "DETENIENDO SERVICIO DE PROGRAMADOR DE TAREAS" + " " * 20 + "*")
    logger.info("*" * 80)
    
    logger.info("Deteniendo servicio de programación de tareas")
    keep_running = False
    # Eliminar el archivo de inicio si existe
    if os.path.exists(STARTUP_FILE):
        os.remove(STARTUP_FILE)
    
    logger.info("Servicio de programación de tareas detenido")
    
    # Banner de confirmación
    logger.info("*" * 80)
    logger.info("*" + " " * 21 + "SERVICIO DE PROGRAMADOR DE TAREAS DETENIDO" + " " * 21 + "*")
    logger.info("*" * 80)

# Código para ejecutar el servicio directamente
if __name__ == "__main__":
    # Este bloque se ejecuta cuando se inicia el script directamente
    import sys
    from flask import Flask
    from flask_sqlalchemy import SQLAlchemy
    
    # Crear una aplicación Flask mínima para pruebas en modo standalone
    app = Flask(__name__)
    app.config['SQLALCHEMY_DATABASE_URI'] = os.environ.get("DATABASE_URL")
    app.config["SQLALCHEMY_ENGINE_OPTIONS"] = {
        "pool_recycle": 300,
        "pool_pre_ping": True,
    }
    
    if len(sys.argv) > 1 and sys.argv[1] == "--run-once":
        # Ejecutar una sola vez y salir
        print("Ejecutando programador de tareas una sola vez...")
        try:
            run_task_scheduler()
            print("Programador de tareas completado.")
        except Exception as e:
            print(f"Error al ejecutar programador: {str(e)}")
    else:
        # Iniciar como servicio
        print("Iniciando servicio de programación de tareas...")
        try:
            # En modo script, ejecutar directamente en el hilo principal
            service_loop()
        except KeyboardInterrupt:
            print("\nServicio interrumpido por el usuario.")
            stop_service()
            print("Servicio detenido.")

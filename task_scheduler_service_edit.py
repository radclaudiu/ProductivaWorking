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

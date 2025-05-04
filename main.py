import logging
from app import app

# Configurar logging
logging.basicConfig(level=logging.INFO, 
                    format='[%(asctime)s] [%(levelname)s] %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')
logger = logging.getLogger(__name__)

# Iniciar servicio de cierre automático de fichajes cuando se inicia la aplicación
try:
    logger.info("Iniciando servicio de cierre automático de fichajes...")
    from checkpoint_closer_service import start_checkpoint_closer_service
    
    # Iniciar el servicio
    success = start_checkpoint_closer_service()
    if success:
        logger.info("✓ Servicio de cierre automático iniciado correctamente")
    else:
        logger.warning("⚠ No se pudo iniciar el servicio de cierre automático")
except Exception as e:
    logger.error(f"❌ Error al iniciar el servicio de cierre automático: {str(e)}")

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
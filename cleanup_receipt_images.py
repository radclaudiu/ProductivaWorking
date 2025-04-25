#!/usr/bin/env python3
"""
Script para limpiar imágenes de recibos antiguas.

Este script elimina las imágenes de recibos de gastos que tienen más de 10 días
de antigüedad. Debe ejecutarse periódicamente mediante un cron job o similar.
"""

import os
import time
import datetime
import logging
import re
from config import Config
from app import create_app, db
from models_monthly_expenses import MonthlyExpense

# Configurar logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(name)s: %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger('receipt_cleaner')

# Días para mantener las imágenes - se obtiene de la configuración
# pero definimos un valor por defecto aquí por si acaso
DAYS_TO_KEEP = 10

def get_file_upload_date(filename):
    """
    Extrae la fecha de un nombre de archivo con formato timestamp.
    Los archivos se guardan con un prefijo de timestamp (YYYYMMDDHHMMSS_).
    
    Args:
        filename: Nombre del archivo.
        
    Returns:
        Un objeto datetime con la fecha de subida o None si no se puede extraer.
    """
    try:
        # Buscar un patrón de timestamp al inicio del nombre de archivo
        match = re.match(r'^(\d{14})_', filename)
        if match:
            timestamp = match.group(1)
            # Convertir el timestamp a fecha
            return datetime.datetime.strptime(timestamp, '%Y%m%d%H%M%S')
    except Exception as e:
        logger.error(f"Error al extraer fecha del archivo {filename}: {str(e)}")
    
    return None

def is_file_old(file_path, days=DAYS_TO_KEEP):
    """
    Determina si un archivo tiene más de X días de antigüedad.
    
    Args:
        file_path: Ruta completa al archivo.
        days: Número de días antes de considerar un archivo antiguo.
        
    Returns:
        bool: True si el archivo es antiguo, False en caso contrario.
    """
    try:
        # Primero intentar extraer la fecha del nombre del archivo
        filename = os.path.basename(file_path)
        upload_date = get_file_upload_date(filename)
        
        if upload_date:
            # Calcular si la fecha extraída del nombre es antigua
            now = datetime.datetime.now()
            file_age_days = (now - upload_date).days
            return file_age_days > days
        
        # Si no se puede extraer del nombre, usar la fecha de modificación del sistema
        file_stat = os.stat(file_path)
        file_mtime = file_stat.st_mtime
        current_time = time.time()
        file_age_days = (current_time - file_mtime) / (24 * 3600)  # Convertir segundos a días
        
        return file_age_days > days
    except Exception as e:
        logger.error(f"Error al verificar antigüedad del archivo {file_path}: {str(e)}")
        # En caso de error, mejor no eliminar el archivo
        return False

def is_file_referenced(relative_path):
    """
    Verifica si un archivo está referenciado en la base de datos.
    
    Args:
        relative_path: Ruta relativa al archivo en la tabla de gastos.
        
    Returns:
        bool: True si el archivo está en uso, False en caso contrario.
    """
    try:
        # Contar cuántos registros usan esta imagen
        count = MonthlyExpense.query.filter_by(receipt_image=relative_path).count()
        return count > 0
    except Exception as e:
        logger.error(f"Error al verificar si el archivo está en uso: {str(e)}")
        # En caso de duda, mejor no eliminar
        return True

def cleanup_receipt_images():
    """
    Elimina imágenes de recibos antiguas que ya no están en uso.
    """
    app = create_app()
    with app.app_context():
        # Obtener la configuración de días de retención
        global DAYS_TO_KEEP
        DAYS_TO_KEEP = Config.RECEIPT_IMAGES_RETENTION_DAYS
        
        uploads_dir = Config.UPLOAD_FOLDER
        receipts_dir = os.path.join(uploads_dir, 'expense_receipts')
        
        if not os.path.exists(receipts_dir):
            logger.info("El directorio de recibos no existe. Nada que limpiar.")
            return
        
        # Registrar información inicial
        logger.info(f"Iniciando limpieza de imágenes de recibos antiguas (> {DAYS_TO_KEEP} días)")
        
        files_checked = 0
        files_deleted = 0
        errors = 0
        
        # Recorrer directorios de empresas
        for company_dir in os.listdir(receipts_dir):
            company_path = os.path.join(receipts_dir, company_dir)
            if not os.path.isdir(company_path):
                continue
            
            # Procesar todos los archivos en el directorio de la empresa
            for filename in os.listdir(company_path):
                file_path = os.path.join(company_path, filename)
                if not os.path.isfile(file_path):
                    continue
                
                files_checked += 1
                
                # Comprobar si el archivo es antiguo
                if is_file_old(file_path):
                    # Comprobar si el archivo está en uso en la base de datos
                    relative_path = os.path.join('expense_receipts', company_dir, filename)
                    if not is_file_referenced(relative_path):
                        try:
                            # Eliminar el archivo
                            os.remove(file_path)
                            files_deleted += 1
                            logger.info(f"Eliminado archivo antiguo: {file_path}")
                        except Exception as e:
                            logger.error(f"Error al eliminar archivo {file_path}: {str(e)}")
                            errors += 1
        
        # Registrar resumen
        logger.info(f"Limpieza completada. Archivos revisados: {files_checked}, eliminados: {files_deleted}, errores: {errors}")

if __name__ == "__main__":
    cleanup_receipt_images()
"""
Configuración de zona horaria para la aplicación
"""
import os
import time
from datetime import datetime, timezone
import pytz

# Configurar zona horaria de Madrid
TIMEZONE = pytz.timezone('Europe/Madrid')

def get_current_time():
    """
    Obtiene la hora actual en la zona horaria configurada (Madrid)
    """
    # Obtener la hora UTC
    utc_now = datetime.now(timezone.utc)
    # Convertir a la hora de Madrid
    return utc_now.astimezone(TIMEZONE)

def datetime_to_madrid(dt):
    """
    Convierte un objeto datetime a la zona horaria de Madrid
    Si el datetime no tiene zona horaria, asume que es UTC
    """
    if dt is None:
        return None
        
    # Si el datetime no tiene zona horaria (naive), asumimos que es UTC
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
        
    # Convertir a la zona horaria de Madrid
    return dt.astimezone(TIMEZONE)
    
def format_datetime(dt, format_str='%Y-%m-%d %H:%M:%S'):
    """
    Formatea un datetime en la zona horaria de Madrid
    """
    if dt is None:
        return ""
    
    try:
        madrid_dt = datetime_to_madrid(dt)
        return madrid_dt.strftime(format_str)
    except Exception as e:
        print(f"Error al formatear fecha: {e}")
        return ""
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

def parse_client_timestamp(timestamp_str):
    """
    Convierte una cadena ISO 8601 de timestamp del cliente a un objeto datetime
    en la zona horaria configurada (Madrid)
    
    Args:
        timestamp_str: Cadena ISO 8601 (formato: YYYY-MM-DDTHH:MM:SS.sssZ)
        
    Returns:
        Objeto datetime en zona horaria de Madrid o None si hay error
    """
    if not timestamp_str:
        print("ℹ️ parse_client_timestamp: No se recibió timestamp (valor vacío)")
        return None
    
    print(f"🔍 Intentando parsear timestamp del cliente: {timestamp_str}")
        
    try:
        # Parsear la cadena ISO a datetime con zona horaria
        dt = datetime.fromisoformat(timestamp_str.replace('Z', '+00:00'))
        print(f"✓ Timestamp parseado correctamente: {dt}")
        
        # Asegurar que tiene zona horaria UTC si no la tiene
        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=timezone.utc)
            print(f"ℹ️ Timestamp no tenía zona horaria, asignado UTC: {dt}")
            
        # Convertir a la zona horaria de Madrid
        madrid_dt = dt.astimezone(TIMEZONE)
        print(f"✓ Timestamp convertido a hora de Madrid: {madrid_dt}")
        print(f"✓ Timestamp como epoch: {madrid_dt.timestamp()}")
        print(f"✓ Timestamp UTC: {dt}, Timestamp Madrid: {madrid_dt}")
        
        return madrid_dt
    except ValueError as e:
        print(f"❌ Error ValueError al parsear timestamp del cliente: {e}")
        print(f"   Formato recibido: {timestamp_str}")
        print(f"   Formato esperado: ISO 8601 (YYYY-MM-DDTHH:MM:SS.sssZ)")
        return None
    except Exception as e:
        print(f"❌ Error inesperado al parsear timestamp del cliente: {e}")
        print(f"   Timestamp: {timestamp_str}")
        print(f"   Tipo de dato: {type(timestamp_str)}")
        return None

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
        if madrid_dt is not None:
            return madrid_dt.strftime(format_str)
        return ""
    except Exception as e:
        print(f"Error al formatear fecha: {e}")
        return ""
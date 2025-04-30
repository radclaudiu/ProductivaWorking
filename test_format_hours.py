#!/usr/bin/env python3
# Script para probar la función format_hours_minutes

def format_hours_minutes(hours_decimal):
    """
    Convierte horas en formato decimal (0-100) a formato horario estándar (0-60).
    Por ejemplo, 1.75 (1h 75m) se convierte a "1h 45m".
    
    Args:
        hours_decimal: Número de horas en formato decimal
        
    Returns:
        String con el formato "XXh YYm" donde YY es en base 60
    """
    if hours_decimal is None:
        return "-"
        
    # Separar las horas enteras de la parte decimal
    hours_whole = int(hours_decimal)
    minutes_decimal = hours_decimal - hours_whole
    
    # Convertir la parte decimal a minutos en base 60
    # Primero, obtenemos los minutos decimales (como 0.75) y luego multiplicamos por 100 
    # para obtener el número en base 100 (75), y luego multiplicamos por 0.6 
    # para convertir a base 60 (45).
    minutes = int(minutes_decimal * 100 * 0.6)
    
    return f"{hours_whole}h {minutes:02}m"

# Probar con varios valores
test_values = [
    1.75,   # Esperado: 1h 45m
    2.50,   # Esperado: 2h 30m
    8.40,   # Esperado: 8h 24m
    0.25,   # Esperado: 0h 15m
    0.33,   # Esperado: 0h 20m (aproximado)
    0.99,   # Esperado: 0h 59m (aproximado)
    4.20,   # Esperado: 4h 12m
    3.67,   # Esperado: 3h 40m (aproximado)
]

for value in test_values:
    print(f"{value} horas -> {format_hours_minutes(value)}")
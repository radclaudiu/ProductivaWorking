"""
Filtros personalizados para Jinja2 y funciones de utilidad para las plantillas.

Este módulo define filtros y funciones que pueden ser utilizados en las plantillas
Jinja2 para formatear datos, realizar cálculos o mostrar información de forma más
amigable.
"""

def month_name_filter(month_number):
    """
    Convierte un número de mes a su nombre en español.
    
    Args:
        month_number (int): Número del mes (1-12)
        
    Returns:
        str: Nombre del mes en español
    """
    month_names = {
        1: 'Enero',
        2: 'Febrero',
        3: 'Marzo',
        4: 'Abril',
        5: 'Mayo',
        6: 'Junio',
        7: 'Julio',
        8: 'Agosto',
        9: 'Septiembre',
        10: 'Octubre',
        11: 'Noviembre',
        12: 'Diciembre'
    }
    
    if isinstance(month_number, str):
        try:
            month_number = int(month_number)
        except ValueError:
            return 'Mes inválido'
            
    return month_names.get(month_number, 'Mes inválido')
    
def format_currency_filter(value, decimals=2):
    """
    Formatea un valor como moneda (€).
    
    Args:
        value (float): Valor a formatear
        decimals (int): Número de decimales a mostrar
        
    Returns:
        str: Valor formateado como moneda
    """
    if value is None:
        return '0,00 €'
        
    try:
        value = float(value)
        formatted = '{:,.{prec}f}'.format(value, prec=decimals)
        formatted = formatted.replace(',', 'X').replace('.', ',').replace('X', '.')
        return f'{formatted} €'
    except (ValueError, TypeError):
        return '0,00 €'
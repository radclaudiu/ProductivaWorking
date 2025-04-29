import os
import tempfile
import base64
import logging
from datetime import datetime, timedelta
from io import BytesIO
from PIL import Image
from fpdf import FPDF
from sqlalchemy import and_, func
from app import db
from models_checkpoints import CheckPointRecord, CheckPointOriginalRecord
from timezone_config import get_current_time, datetime_to_madrid, TIMEZONE

logger = logging.getLogger(__name__)

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
    minutes = int(minutes_decimal * 60)
    
    return f"{hours_whole}h {minutes:02}m"

class CheckPointPDF(FPDF):
    """Clase personalizada para generar PDFs de fichajes con cabecera y pie de página"""
    
    def __init__(self, title='Registro de Fichajes', *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.title = title
        self.set_title(title)
        self.set_author('Sistema de Fichajes')
        
        # Colores corporativos
        self.primary_color = (31, 79, 121)  # Azul corporativo oscuro
        self.secondary_color = (82, 125, 162)  # Azul corporativo medio
        self.accent_color = (236, 240, 245)  # Azul muy claro para fondos
        
    def header(self):
        # Rectángulo superior de color
        self.set_fill_color(*self.primary_color)
        self.rect(0, 0, 210, 20, 'F')
        
        # Logo (si existe)
        logo_path = os.path.join(os.getcwd(), 'static', 'img', 'logo.png')
        if os.path.exists(logo_path):
            self.image(logo_path, 10, 3, 15)
        
        # Título con texto blanco (centrado verticalmente)
        self.set_font('Arial', 'B', 16)
        self.set_text_color(255, 255, 255)
        self.set_y(7)  # Centrar verticalmente el texto en la barra de 20mm
        self.cell(0, 6, self.title, 0, 1, 'C')
        
        # Restablecer color de texto
        self.set_text_color(0, 0, 0)
        
        # Línea de separación con color corporativo
        self.set_draw_color(*self.secondary_color)
        self.set_line_width(0.5)
        self.line(10, 25, 200, 25)
        
        # Restablecer color de texto
        self.set_text_color(0, 0, 0)
        self.ln(5)
        
    def footer(self):
        # Posición a 2 cm desde el final
        self.set_y(-20)
        
        # Línea de separación con color corporativo
        self.set_draw_color(*self.secondary_color)
        self.line(10, self.get_y(), 200, self.get_y())
        
        # Rectángulo inferior de color
        self.set_fill_color(*self.primary_color)
        self.rect(0, self.h - 12, 210, 12, 'F')
        
        # Número de página con texto blanco (centrado verticalmente)
        self.set_font('Arial', 'B', 9)
        self.set_text_color(255, 255, 255)
        self.set_y(self.h - 8)  # Centrar verticalmente en la barra de 12mm
        self.cell(0, 4, f'Página {self.page_no()}', 0, 0, 'C')


def set_font_safely(pdf, family, style='', size=12):
    """
    Función auxiliar para establecer la fuente de manera segura en el PDF.
    Normaliza los valores de estilo para evitar errores de tipado.
    
    Args:
        pdf: Objeto FPDF
        family: Nombre de la familia de fuente (string)
        style: Estilo de la fuente ('', 'B', 'I', 'U', etc.)
        size: Tamaño de la fuente (integer)
    """
    # Mapa de estilos válidos
    style_map = {
        '': '', 
        'B': 'B', 
        'I': 'I', 
        'U': 'U', 
        'BU': 'BU', 
        'UB': 'UB', 
        'BI': 'BI', 
        'IB': 'BI', 
        'UI': 'UI', 
        'IU': 'UI', 
        'BUI': 'BUI', 
        'BIU': 'BUI', 
        'UBI': 'BUI', 
        'UIB': 'BUI', 
        'IBU': 'BUI', 
        'IUB': 'BUI'
    }
    
    # Normalizar el estilo
    normalized_style = style_map.get(str(style), '')
    
    # Asegurar que el tamaño sea un entero
    normalized_size = int(size)
    
    # Establecer la fuente
    pdf.set_font(family, normalized_style, normalized_size)


def draw_signature(pdf, signature_data, x, y, width=50, height=20):
    """Dibuja la firma en el PDF desde datos base64"""
    if not signature_data:
        return
    
    try:
        # Procesar los datos base64
        if 'data:image/png;base64,' in signature_data:
            signature_data = signature_data.split('data:image/png;base64,')[1]
        
        # Decodificar los datos
        decoded_data = base64.b64decode(signature_data)
        
        # Crear una imagen temporal
        with tempfile.NamedTemporaryFile(delete=False, suffix='.png') as temp_file:
            temp_file.write(decoded_data)
            temp_file_path = temp_file.name
        
        # Dibujar la imagen en el PDF
        pdf.image(temp_file_path, x, y, width, height)
        
        # Eliminar el archivo temporal
        os.unlink(temp_file_path)
        
        return True  # Indicar que se dibujó la firma correctamente
    except Exception as e:
        print(f"Error al dibujar la firma: {str(e)}")
        return False  # Indicar que hubo un error al dibujar la firma


def get_iso_week_start_end(date_obj):
    """
    Obtiene el inicio (lunes) y fin (domingo) de la semana ISO para una fecha dada.
    
    Args:
        date_obj: Objeto datetime.date o datetime.datetime
        
    Returns:
        Tupla con (fecha_inicio_semana, fecha_fin_semana)
    """
    # Si es un datetime, convertir a date
    if isinstance(date_obj, datetime):
        date_obj = date_obj.date()
    
    # El día de la semana en formato ISO (1=lunes, 7=domingo)
    iso_day = date_obj.isoweekday()
    
    # Calcular el lunes de esa semana
    week_start = date_obj - timedelta(days=iso_day - 1)
    
    # El domingo es 6 días después del lunes
    week_end = week_start + timedelta(days=6)
    
    return (week_start, week_end)

def get_week_description(week_start):
    """
    Genera una descripción de la semana basada en su fecha de inicio.
    
    Args:
        week_start: Fecha de inicio de la semana (lunes)
        
    Returns:
        String con descripción de la semana (ej: "Semana 15 (10/04 - 16/04)")
    """
    week_end = week_start + timedelta(days=6)
    week_num = week_start.isocalendar()[1]
    return f"Semana {week_num} ({week_start.strftime('%d/%m')} - {week_end.strftime('%d/%m')})"

def generate_simple_pdf_report(records, start_date, end_date, include_signature=True):
    """
    Genera un informe PDF simple de los registros de fichaje sin agrupar por semanas y sin sumar horas.
    Este formato es específico para la ruta 'fichajes/records/export'.
    """
    # Agrupar registros por empleado
    employees_records = {}
    
    for record in records:
        employee_id = record.employee_id
        
        if employee_id not in employees_records:
            employees_records[employee_id] = {
                'employee': record.employee,
                'records': []
            }
            
        employees_records[employee_id]['records'].append(record)
        
    # Crear PDF
    pdf = CheckPointPDF(title=f'Informe de Fichajes: {start_date.strftime("%d/%m/%Y")} - {end_date.strftime("%d/%m/%Y")}')
    pdf.set_auto_page_break(auto=True, margin=15)
    
    # Para cada empleado
    for employee_id, data in employees_records.items():
        employee = data['employee']
        records = data['records']
        company = employee.company
        
        # Añadir una nueva página para cada empleado
        pdf.add_page()
        
        # Añadir espacio adicional después del encabezado para evitar solapamiento con la barra
        pdf.ln(8)  # 8mm de espacio adicional
        
        # Calcular posiciones centradas (el ancho de la página es 210mm)
        # Usaremos un margen de 20mm a cada lado
        left_margin = 20
        right_margin = 20
        usable_width = 210 - left_margin - right_margin
        block_width = 85  # Cada bloque tendrá 85mm de ancho
        gap = 10          # 10mm de separación entre bloques
        
        # Posición X del primer bloque
        block1_x = left_margin
        # Posición X del segundo bloque
        block2_x = block1_x + block_width + gap
        
        # Crear fondos de color para los bloques de información
        # Bloque empleado (izquierda)
        pdf.set_fill_color(*pdf.accent_color)
        pdf.rect(block1_x, pdf.get_y(), block_width, 30, 'F')
        
        # Bloque empresa (derecha)
        pdf.rect(block2_x, pdf.get_y(), block_width, 30, 'F')
        
        # Líneas de contorno con color corporativo
        pdf.set_draw_color(*pdf.secondary_color)
        pdf.rect(block1_x, pdf.get_y(), block_width, 30, 'D')
        pdf.rect(block2_x, pdf.get_y(), block_width, 30, 'D')
        
        initial_y = pdf.get_y()
        
        # Títulos con fondo de color primario
        pdf.set_fill_color(*pdf.secondary_color)
        pdf.set_text_color(255, 255, 255)
        pdf.set_font('Arial', 'B', 10)
        
        # Título empleado
        pdf.rect(block1_x, pdf.get_y(), block_width, 6, 'F')
        pdf.set_xy(block1_x, pdf.get_y())
        pdf.cell(block_width, 6, 'DATOS DEL EMPLEADO', 0, 0, 'C')
        
        # Título empresa
        pdf.rect(block2_x, pdf.get_y(), block_width, 6, 'F')
        pdf.set_xy(block2_x, pdf.get_y())
        pdf.cell(block_width, 6, 'DATOS DE LA EMPRESA', 0, 1, 'C')
        
        # Restaurar color de texto
        pdf.set_text_color(0, 0, 0)
        
        # Espacio para etiquetas y valores
        label_width = 25
        value_width = block_width - label_width - 5  # 5mm de margen interno
        label_x1 = block1_x + 2  # 2mm de margen interno
        value_x1 = label_x1 + label_width
        label_x2 = block2_x + 2
        value_x2 = label_x2 + label_width
        
        # Etiquetas en negrita
        pdf.set_font('Arial', 'B', 9)
        pdf.set_xy(label_x1, pdf.get_y() + 2)
        pdf.cell(label_width, 5, 'Nombre:', 0, 0)
        
        pdf.set_xy(label_x2, pdf.get_y())
        pdf.cell(label_width, 5, 'Nombre:', 0, 1)
        
        # Datos en texto normal
        pdf.set_font('Arial', '', 9)
        pdf.set_xy(value_x1, pdf.get_y() - 5)
        pdf.cell(value_width, 5, f"{employee.first_name} {employee.last_name}", 0, 0)
        
        pdf.set_xy(value_x2, pdf.get_y())
        pdf.cell(value_width, 5, f"{company.name}", 0, 1)
        
        # DNI
        pdf.set_font('Arial', 'B', 9)
        pdf.set_xy(label_x1, pdf.get_y() + 2)
        pdf.cell(label_width, 5, 'DNI/NIE:', 0, 0)
        
        # CIF
        pdf.set_xy(label_x2, pdf.get_y())
        pdf.cell(label_width, 5, 'CIF:', 0, 1)
        
        pdf.set_font('Arial', '', 9)
        pdf.set_xy(value_x1, pdf.get_y() - 5)
        pdf.cell(value_width, 5, f"{employee.dni}", 0, 0)
        
        pdf.set_xy(value_x2, pdf.get_y())
        pdf.cell(value_width, 5, f"{company.tax_id}", 0, 1)
        
        # Puesto
        pdf.set_font('Arial', 'B', 9)
        pdf.set_xy(label_x1, pdf.get_y() + 2)
        pdf.cell(label_width, 5, 'Puesto:', 0, 0)
        
        # Dirección
        pdf.set_xy(label_x2, pdf.get_y())
        pdf.cell(label_width, 5, 'Dirección:', 0, 1)
        
        pdf.set_font('Arial', '', 9)
        pdf.set_xy(value_x1, pdf.get_y() - 5)
        pdf.cell(value_width, 5, f"{employee.position or '-'}", 0, 0)
        
        empresa_direccion = f"{company.address or ''}, {company.city or ''}"
        if empresa_direccion.strip() == ',':
            empresa_direccion = '-'
        pdf.set_xy(value_x2, pdf.get_y())
        pdf.cell(value_width, 5, empresa_direccion, 0, 1)
        
        # Asegurar que avanzamos correctamente después de los bloques
        pdf.set_y(initial_y + 35)  # Espacio antes de la tabla
        
        # Título de la tabla con fondo corporativo
        pdf.set_fill_color(*pdf.primary_color)
        pdf.set_text_color(255, 255, 255)
        pdf.set_font('Arial', 'B', 12)
        
        # Centrar el título de la tabla usando los mismos márgenes
        title_width = 170  # Ancho del título
        title_x = (210 - title_width) / 2  # Centrado en la página
        
        # Dibujar el rectángulo para el título
        pdf.rect(title_x, pdf.get_y(), title_width, 10, 'F')
        
        # Guardar la posición Y actual
        current_y = pdf.get_y()
        
        # Posicionar el texto verticalmente centrado dentro de la barra (subir un poco)
        pdf.set_y(current_y + 2.5)  # 2.5mm para centrar el texto en la barra de 10mm de alto
        pdf.cell(0, 5, 'REGISTROS DE FICHAJE', 0, 1, 'C')
        
        # Volver a la posición después del rectángulo y añadir espacio
        pdf.set_y(current_y + 10 + 5)  # 5mm de espacio adicional entre la barra y la tabla
        
        # Restaurar color de texto
        pdf.set_text_color(0, 0, 0)
        
        # Tabla de registros
        pdf.set_font('Arial', 'B', 10)
        
        # Cabecera de tabla
        col_widths = [35, 30, 30, 40]
        header = ['Fecha', 'Entrada', 'Salida', 'Firma']
        table_width = sum(col_widths)
        
        # Calcular posición X para centrar la tabla
        table_x = (210 - table_width) / 2
        
        # Ordenar los registros por fecha de check-in
        sorted_records = sorted(records, key=lambda r: r.check_in_time)
        
        # Cabecera de la tabla
        pdf.set_y(pdf.get_y() + 2)  # Espacio antes de la tabla
        pdf.set_x(table_x)
        
        # Dibujar cabecera con color corporativo
        pdf.set_fill_color(*pdf.secondary_color)
        pdf.set_text_color(255, 255, 255)
        pdf.set_draw_color(*pdf.primary_color)
        pdf.set_line_width(0.3)
        
        for i, col in enumerate(header):
            pdf.cell(col_widths[i], 10, col, 1, 0, 'C', True)
        pdf.ln()
        
        # Restaurar color de texto para las filas
        pdf.set_text_color(0, 0, 0)
        pdf.set_font('Arial', '', 10)
        
        # Color alternado para las filas (efecto cebra)
        row_count = 0
        
        for record in sorted_records:
            # Verificar si hay espacio en la página actual
            if pdf.get_y() > pdf.h - 30:  # Si queda poco espacio para esta fila
                pdf.add_page()
                pdf.ln(8)  # Espacio después del encabezado
                
                # Volver a dibujar la cabecera de la tabla
                pdf.set_x(table_x)
                pdf.set_fill_color(*pdf.secondary_color)
                pdf.set_text_color(255, 255, 255)
                
                for i, col in enumerate(header):
                    pdf.cell(col_widths[i], 10, col, 1, 0, 'C', True)
                pdf.ln()
                
                # Restaurar color de texto
                pdf.set_text_color(0, 0, 0)
            
            # Posicionar al inicio de la fila, centrado
            pdf.set_x(table_x)
            
            # Cambiar color de fondo según fila par/impar
            if row_count % 2 == 0:
                pdf.set_fill_color(255, 255, 255)  # Blanco
            else:
                pdf.set_fill_color(*pdf.accent_color)  # Color claro
            
            # Fecha
            pdf.cell(col_widths[0], 10, record.check_in_time.strftime('%d/%m/%Y'), 1, 0, 'C', True)
            
            # Hora entrada
            pdf.cell(col_widths[1], 10, record.check_in_time.strftime('%H:%M'), 1, 0, 'C', True)
            
            # Hora salida
            if record.check_out_time:
                pdf.cell(col_widths[2], 10, record.check_out_time.strftime('%H:%M'), 1, 0, 'C', True)
            else:
                pdf.cell(col_widths[2], 10, '-', 1, 0, 'C', True)
            
            # Firma si está habilitado
            if include_signature:
                # Dibujar la firma o un espacio para la firma manual
                if hasattr(record, 'signature_data') and record.signature_data:
                    # Guardar posición actual
                    firma_x = pdf.get_x()
                    firma_y = pdf.get_y()
                    
                    # Crear celda para la firma
                    pdf.cell(col_widths[3], 10, '', 1, 1, 'C', True)
                    
                    # Si hay firma digital, intentar dibujarla
                    result = draw_signature(pdf, record.signature_data, firma_x + 5, firma_y + 1, 30, 8)
                    if not result:
                        # Si falla, indicar que hay un problema con la firma
                        current_y = pdf.get_y()
                        pdf.set_xy(firma_x, firma_y)
                        pdf.set_font('Arial', 'I', 8)
                        pdf.cell(col_widths[3], 10, 'Error firma', 1, 1, 'C', True)
                        pdf.set_font('Arial', '', 10)
                else:
                    # Espacio para firma con texto indicativo
                    pdf.cell(col_widths[3], 10, 'Sin firma', 1, 1, 'C', True)
            else:
                # Si no se incluye firma, simplemente dejar un espacio o algún texto
                pdf.cell(col_widths[3], 10, 'No requerido', 1, 1, 'C', True)
            
            row_count += 1
        
        # Espacio después de cada empleado
        pdf.ln(10)
    
    # Crear archivo temporal para el PDF
    pdf_file = tempfile.NamedTemporaryFile(delete=False, suffix='.pdf')
    pdf.output(pdf_file.name)
    
    return pdf_file.name

def generate_pdf_report(records, start_date, end_date, include_signature=True):
    """Genera un informe PDF de los registros de fichaje agrupados por semanas"""
    # Agrupar registros por empleado
    employees_records = {}
    
    for record in records:
        employee_id = record.employee_id
        
        if employee_id not in employees_records:
            employees_records[employee_id] = {
                'employee': record.employee,
                'records': []
            }
            
        employees_records[employee_id]['records'].append(record)
    
    # Crear PDF
    pdf = CheckPointPDF(title=f'Informe de Fichajes: {start_date.strftime("%d/%m/%Y")} - {end_date.strftime("%d/%m/%Y")}')
    pdf.set_auto_page_break(auto=True, margin=15)
    
    # Para cada empleado
    for employee_id, data in employees_records.items():
        employee = data['employee']
        records = data['records']
        company = employee.company
        
        # Añadir una nueva página para cada empleado
        pdf.add_page()
        
        # Añadir espacio adicional después del encabezado para evitar solapamiento con la barra
        pdf.ln(8)  # 8mm de espacio adicional
        
        # Calcular posiciones centradas (el ancho de la página es 210mm)
        # Usaremos un margen de 20mm a cada lado
        left_margin = 20
        right_margin = 20
        usable_width = 210 - left_margin - right_margin
        block_width = 85  # Cada bloque tendrá 85mm de ancho
        gap = 10          # 10mm de separación entre bloques
        
        # Posición X del primer bloque
        block1_x = left_margin
        # Posición X del segundo bloque
        block2_x = block1_x + block_width + gap
        
        # Crear fondos de color para los bloques de información
        # Bloque empleado (izquierda)
        pdf.set_fill_color(*pdf.accent_color)
        pdf.rect(block1_x, pdf.get_y(), block_width, 30, 'F')
        
        # Bloque empresa (derecha)
        pdf.rect(block2_x, pdf.get_y(), block_width, 30, 'F')
        
        # Líneas de contorno con color corporativo
        pdf.set_draw_color(*pdf.secondary_color)
        pdf.rect(block1_x, pdf.get_y(), block_width, 30, 'D')
        pdf.rect(block2_x, pdf.get_y(), block_width, 30, 'D')
        
        initial_y = pdf.get_y()
        
        # Títulos con fondo de color primario
        pdf.set_fill_color(*pdf.secondary_color)
        pdf.set_text_color(255, 255, 255)
        pdf.set_font('Arial', 'B', 10)
        
        # Título empleado
        pdf.rect(block1_x, pdf.get_y(), block_width, 6, 'F')
        pdf.set_xy(block1_x, pdf.get_y())
        pdf.cell(block_width, 6, 'DATOS DEL EMPLEADO', 0, 0, 'C')
        
        # Título empresa
        pdf.rect(block2_x, pdf.get_y(), block_width, 6, 'F')
        pdf.set_xy(block2_x, pdf.get_y())
        pdf.cell(block_width, 6, 'DATOS DE LA EMPRESA', 0, 1, 'C')
        
        # Restaurar color de texto
        pdf.set_text_color(0, 0, 0)
        
        # Espacio para etiquetas y valores
        label_width = 25
        value_width = block_width - label_width - 5  # 5mm de margen interno
        label_x1 = block1_x + 2  # 2mm de margen interno
        value_x1 = label_x1 + label_width
        label_x2 = block2_x + 2
        value_x2 = label_x2 + label_width
        
        # Etiquetas en negrita
        pdf.set_font('Arial', 'B', 9)
        pdf.set_xy(label_x1, pdf.get_y() + 2)
        pdf.cell(label_width, 5, 'Nombre:', 0, 0)
        
        pdf.set_xy(label_x2, pdf.get_y())
        pdf.cell(label_width, 5, 'Nombre:', 0, 1)
        
        # Datos en texto normal
        pdf.set_font('Arial', '', 9)
        pdf.set_xy(value_x1, pdf.get_y() - 5)
        pdf.cell(value_width, 5, f"{employee.first_name} {employee.last_name}", 0, 0)
        
        pdf.set_xy(value_x2, pdf.get_y())
        pdf.cell(value_width, 5, f"{company.name}", 0, 1)
        
        # DNI
        pdf.set_font('Arial', 'B', 9)
        pdf.set_xy(label_x1, pdf.get_y() + 2)
        pdf.cell(label_width, 5, 'DNI/NIE:', 0, 0)
        
        # CIF
        pdf.set_xy(label_x2, pdf.get_y())
        pdf.cell(label_width, 5, 'CIF:', 0, 1)
        
        pdf.set_font('Arial', '', 9)
        pdf.set_xy(value_x1, pdf.get_y() - 5)
        pdf.cell(value_width, 5, f"{employee.dni}", 0, 0)
        
        pdf.set_xy(value_x2, pdf.get_y())
        pdf.cell(value_width, 5, f"{company.tax_id}", 0, 1)
        
        # Puesto
        pdf.set_font('Arial', 'B', 9)
        pdf.set_xy(label_x1, pdf.get_y() + 2)
        pdf.cell(label_width, 5, 'Puesto:', 0, 0)
        
        # Dirección
        pdf.set_xy(label_x2, pdf.get_y())
        pdf.cell(label_width, 5, 'Dirección:', 0, 1)
        
        pdf.set_font('Arial', '', 9)
        pdf.set_xy(value_x1, pdf.get_y() - 5)
        pdf.cell(value_width, 5, f"{employee.position or '-'}", 0, 0)
        
        empresa_direccion = f"{company.address or ''}, {company.city or ''}"
        if empresa_direccion.strip() == ',':
            empresa_direccion = '-'
        pdf.set_xy(value_x2, pdf.get_y())
        pdf.cell(value_width, 5, empresa_direccion, 0, 1)
        
        # Asegurar que avanzamos correctamente después de los bloques
        pdf.set_y(initial_y + 35)  # Espacio antes de la tabla
        
        # Título de la tabla con fondo corporativo
        pdf.set_fill_color(*pdf.primary_color)
        pdf.set_text_color(255, 255, 255)
        pdf.set_font('Arial', 'B', 12)
        
        # Centrar el título de la tabla usando los mismos márgenes
        title_width = 170  # Ancho del título
        title_x = (210 - title_width) / 2  # Centrado en la página
        
        # Dibujar el rectángulo para el título
        pdf.rect(title_x, pdf.get_y(), title_width, 10, 'F')
        
        # Guardar la posición Y actual
        current_y = pdf.get_y()
        
        # Posicionar el texto verticalmente centrado dentro de la barra (subir un poco)
        pdf.set_y(current_y + 2.5)  # 2.5mm para centrar el texto en la barra de 10mm de alto
        pdf.cell(0, 5, 'REGISTROS DE FICHAJE POR SEMANAS', 0, 1, 'C')
        
        # Volver a la posición después del rectángulo y añadir espacio
        pdf.set_y(current_y + 10 + 5)  # 5mm de espacio adicional entre la barra y la tabla
        
        # Restaurar color de texto
        pdf.set_text_color(0, 0, 0)
        
        # Tabla de registros
        pdf.set_font('Arial', 'B', 10)
        
        # Cabecera de tabla
        col_widths = [35, 30, 30, 30, 40]
        header = ['Fecha', 'Entrada', 'Salida', 'Horas', 'Firma']
        table_width = sum(col_widths)
        
        # Calcular posición X para centrar la tabla
        table_x = (210 - table_width) / 2
        
        # Ordenar los registros por fecha de check-in
        sorted_records = sorted(records, key=lambda r: r.check_in_time)
        
        # Agrupar registros por semana (de lunes a domingo)
        weeks_records = {}
        for record in sorted_records:
            # Obtener el lunes de la semana para este registro
            week_start, _ = get_iso_week_start_end(record.check_in_time)
            week_key = week_start.strftime('%Y-%m-%d')
            
            if week_key not in weeks_records:
                weeks_records[week_key] = {
                    'week_start': week_start,
                    'records': [],
                    'total_hours': 0.0
                }
            
            weeks_records[week_key]['records'].append(record)
            
            # Sumar horas si el registro tiene duración
            duration = record.duration()
            if duration is not None:
                weeks_records[week_key]['total_hours'] += duration
        
        # Ordenar las semanas por fecha de inicio
        sorted_weeks = sorted(weeks_records.items(), key=lambda x: x[1]['week_start'])
        
        # Para cada semana
        for week_key, week_data in sorted_weeks:
            week_start = week_data['week_start']
            week_records = week_data['records']
            week_total_hours = week_data['total_hours']
            
            # Título de la semana
            pdf.set_font('Arial', 'B', 11)
            pdf.set_fill_color(*pdf.secondary_color)
            pdf.set_text_color(255, 255, 255)
            
            # Calcular posición X para centrar el título de la semana
            week_title_width = 170  # Ancho del título de la semana
            week_title_x = (210 - week_title_width) / 2  # Centrado en la página
            
            # Asegurarse de que hay espacio para el título de la semana y la tabla
            if pdf.get_y() > pdf.h - 60:  # Si queda poco espacio en la página
                pdf.add_page()
                pdf.ln(8)  # 8mm de espacio después del encabezado
            
            pdf.set_y(pdf.get_y() + 5)  # Espacio antes del título de la semana
            pdf.set_x(week_title_x)
            pdf.rect(week_title_x, pdf.get_y(), week_title_width, 8, 'F')
            pdf.cell(week_title_width, 8, get_week_description(week_start), 0, 1, 'C', True)
            
            # Restaurar color de texto para la tabla
            pdf.set_text_color(0, 0, 0)
            pdf.set_font('Arial', 'B', 10)
            
            # Cabecera de la tabla para esta semana
            pdf.set_y(pdf.get_y() + 2)  # Espacio antes de la tabla
            pdf.set_x(table_x)
            
            # Dibujar cabecera con color corporativo
            pdf.set_fill_color(*pdf.secondary_color)
            pdf.set_text_color(255, 255, 255)
            pdf.set_draw_color(*pdf.primary_color)
            pdf.set_line_width(0.3)
            
            for i, col in enumerate(header):
                pdf.cell(col_widths[i], 10, col, 1, 0, 'C', True)
            pdf.ln()
            
            # Restaurar color de texto para las filas
            pdf.set_text_color(0, 0, 0)
            pdf.set_font('Arial', '', 10)
            
            # Color alternado para las filas (efecto cebra)
            row_count = 0
            
            for record in week_records:
                # Posicionar al inicio de la fila, centrado
                pdf.set_x(table_x)
                
                # Cambiar color de fondo según fila par/impar
                if row_count % 2 == 0:
                    pdf.set_fill_color(255, 255, 255)  # Blanco
                else:
                    pdf.set_fill_color(*pdf.accent_color)  # Color claro
                
                # Fecha
                pdf.cell(col_widths[0], 10, record.check_in_time.strftime('%d/%m/%Y'), 1, 0, 'C', True)
                
                # Hora entrada
                pdf.cell(col_widths[1], 10, record.check_in_time.strftime('%H:%M'), 1, 0, 'C', True)
                
                # Hora salida
                if record.check_out_time:
                    pdf.cell(col_widths[2], 10, record.check_out_time.strftime('%H:%M'), 1, 0, 'C', True)
                else:
                    pdf.cell(col_widths[2], 10, '-', 1, 0, 'C', True)
                
                # Horas trabajadas
                duration = record.duration()
                hours_str = format_hours_minutes(duration)
                pdf.cell(col_widths[3], 10, hours_str, 1, 0, 'C', True)
                
                # Celda para firma
                y_pos_before = pdf.get_y()
                pdf.cell(col_widths[4], 10, '', 1, 0, 'C', True)
                
                # Dibujar firma en la celda si existe
                if include_signature:
                    if record.has_signature and record.signature_data:
                        # Guardar posición actual
                        x_pos = pdf.get_x() - col_widths[4]
                        # Guardar la fuente actual
                        # Guardar el estado actual de la fuente
                        current_font = pdf.font_family
                        current_style = pdf.font_style
                        current_size = int(pdf.font_size_pt)
                        
                        # Dibujar la firma dentro de la celda
                        result = draw_signature(pdf, record.signature_data, x_pos + 2, y_pos_before + 1, col_widths[4] - 4, 8)
                        
                        # Si falla, retroceder y mostrar texto indicativo
                        if not result:
                            # Retroceder a la posición de la celda
                            pdf.set_xy(x_pos, y_pos_before)
                            # Cambiar a fuente cursiva pequeña
                            set_font_safely(pdf, 'Arial', 'I', 8)
                            # Mostrar mensaje de error
                            pdf.cell(col_widths[4], 10, 'Error firma', 0, 0, 'C')
                            # Restaurar fuente original con los valores normalizados
                            set_font_safely(pdf, current_font, current_style, current_size)
                    else:
                        # Si no hay firma, retroceder y mostrar texto indicativo
                        x_pos = pdf.get_x() - col_widths[4]
                        # Guardar la fuente actual
                        current_font = pdf.font_family
                        current_style = pdf.font_style
                        current_size = int(pdf.font_size_pt)
                        # Cambiar a fuente cursiva pequeña
                        pdf.set_xy(x_pos, y_pos_before)
                        set_font_safely(pdf, 'Arial', 'I', 8)
                        # Mostrar texto de sin firma
                        pdf.cell(col_widths[4], 10, 'Sin firma', 0, 0, 'C')
                        # Restaurar fuente original con los valores normalizados
                        set_font_safely(pdf, current_font, current_style, current_size)
                
                pdf.ln()
                row_count += 1
            
            # Añadir fila de total semanal
            pdf.set_x(table_x)
            pdf.set_fill_color(*pdf.primary_color)
            pdf.set_text_color(255, 255, 255)
            set_font_safely(pdf, 'Arial', 'B', 10)
            
            # Columnas de fecha, entrada, salida combinadas para el total
            combined_width = col_widths[0] + col_widths[1] + col_widths[2]
            pdf.cell(combined_width, 10, 'TOTAL SEMANA', 1, 0, 'C', True)
            
            # Horas totales
            pdf.cell(col_widths[3], 10, format_hours_minutes(week_total_hours), 1, 0, 'C', True)
            
            # Celda vacía para la firma
            pdf.cell(col_widths[4], 10, '', 1, 1, 'C', True)
            
            # Restaurar color de texto
            pdf.set_text_color(0, 0, 0)
            set_font_safely(pdf, 'Arial', '', 10)
            
            # Espacio después de cada tabla semanal
            pdf.ln(5)
        
        # Añadir total general al final
        if len(sorted_weeks) > 1:
            total_hours = sum(week_data['total_hours'] for _, week_data in sorted_weeks)
            
            # Título del total general
            set_font_safely(pdf, 'Arial', 'B', 12)
            pdf.set_fill_color(*pdf.primary_color)
            pdf.set_text_color(255, 255, 255)
            
            # Calcular posición X para centrar el total general
            total_title_width = 170  # Ancho del título del total
            total_title_x = (210 - total_title_width) / 2  # Centrado en la página
            
            pdf.set_y(pdf.get_y() + 2)  # Espacio antes del total general
            pdf.set_x(total_title_x)
            pdf.rect(total_title_x, pdf.get_y(), total_title_width, 10, 'F')
            pdf.cell(total_title_width, 10, f"TOTAL GENERAL: {format_hours_minutes(total_hours)}", 0, 1, 'C', True)
            
            # Restaurar color de texto
            pdf.set_text_color(0, 0, 0)
            set_font_safely(pdf, 'Arial', '', 10)
    
    # Crear un archivo temporal en disco
    temp_file = tempfile.NamedTemporaryFile(delete=False, suffix='.pdf')
    temp_filename = temp_file.name
    temp_file.close()
    
    # Guardar el PDF en el archivo temporal
    pdf.output(temp_filename)
    
    return temp_filename


# Variable global para control de ejecuciones simultáneas
# Cada vez que process_auto_checkouts() se está ejecutando, este valor es True
# SISTEMA DE AUTO-CHECKOUT ELIMINADO
# El sistema de auto-checkout ha sido eliminado por completo para evitar procesamiento
# automático de los registros sin fichaje de salida.


def delete_employee_records(employee_id, start_date, end_date, checkpoint_id=None):
    """
    Elimina los registros de fichaje de un empleado específico dentro de un rango de fechas.
    
    Args:
        employee_id: ID del empleado cuyos registros se eliminarán
        start_date: Fecha de inicio del rango (datetime.date)
        end_date: Fecha de fin del rango (datetime.date)
        checkpoint_id: ID del punto de fichaje (opcional, si None se eliminan de todos los puntos)
        
    Returns:
        dict: Diccionario con el resultado de la operación y estadísticas
    """
    timestamp = datetime.now()
    logger.warning(f"ELIMINANDO REGISTROS DE EMPLEADO ID {employee_id} - PERIODO: {start_date} - {end_date}")
    
    try:
        # Construir la consulta básica para encontrar registros de este empleado en el rango de fechas
        records_query = db.session.query(CheckPointRecord).filter(
            CheckPointRecord.employee_id == employee_id,
            func.date(CheckPointRecord.check_in_time) >= start_date,
            func.date(CheckPointRecord.check_in_time) <= end_date
        )
        
        # Si se especifica un punto de fichaje, filtrar por él
        if checkpoint_id:
            records_query = records_query.filter(CheckPointRecord.checkpoint_id == checkpoint_id)
        
        # Obtener IDs de registros que se eliminarán (para eliminar también registros originales y incidencias)
        record_ids = [record.id for record in records_query.all()]
        
        # Número de registros encontrados
        records_count = len(record_ids)
        
        # Si no hay registros, devolver resultado
        if records_count == 0:
            return {
                "success": True,
                "message": "No se encontraron registros para eliminar",
                "records_deleted": 0,
                "original_records_deleted": 0,
                "incidents_deleted": 0
            }
            
        incidents_deleted = 0
        original_deleted = 0
        
        # Iniciar una transacción para que todas las operaciones sean atómicas
        try:
            # 1. Primero eliminar las incidencias asociadas a estos registros
            if record_ids:
                # Importar aquí para evitar problemas de dependencia circular
                from models_checkpoints import CheckPointIncident
                
                incidents_deleted = db.session.query(CheckPointIncident).filter(
                    CheckPointIncident.record_id.in_(record_ids)
                ).delete(synchronize_session=False)
                
                logger.info(f"Eliminadas {incidents_deleted} incidencias asociadas a los registros")
                
                # 2. Luego eliminar los registros originales asociados
                original_deleted = db.session.query(CheckPointOriginalRecord).filter(
                    CheckPointOriginalRecord.record_id.in_(record_ids)
                ).delete(synchronize_session=False)
                
                logger.info(f"Eliminados {original_deleted} registros originales")
                
                # 3. Finalmente eliminar los registros principales
                records_deleted = records_query.delete(synchronize_session=False)
                
                logger.info(f"Eliminados {records_deleted} registros principales")
                
                # Confirmar todas las eliminaciones
                db.session.commit()
                
                end_timestamp = datetime.now()
                duration = (end_timestamp - timestamp).total_seconds()
                
                logger.warning(
                    f"ELIMINACIÓN COMPLETADA - {records_deleted} registros, "
                    f"{original_deleted} registros originales y {incidents_deleted} incidencias "
                    f"eliminados en {duration:.2f} segundos"
                )
                
                return {
                    "success": True,
                    "timestamp": timestamp.strftime('%Y-%m-%d %H:%M:%S'),
                    "duration_seconds": duration,
                    "records_deleted": records_deleted,
                    "original_records_deleted": original_deleted,
                    "incidents_deleted": incidents_deleted
                }
        except Exception as inner_e:
            # Si ocurre un error durante la eliminación, hacer rollback y relanzar
            db.session.rollback()
            raise inner_e
            
    except Exception as e:
        # Hacer rollback en caso de error
        db.session.rollback()
        
        error_msg = f"ERROR CRÍTICO durante la eliminación de registros: {str(e)}"
        logger.critical(error_msg)
        
        return {
            "success": False,
            "message": f"Error durante la eliminación: {str(e)}",
            "records_deleted": 0,
            "original_records_deleted": 0,
            "incidents_deleted": 0
        }
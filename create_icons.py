from PIL import Image, ImageDraw, ImageFont
import os

# Crear directorio si no existe
if not os.path.exists('static/pwa'):
    os.makedirs('static/pwa')

# Función para crear icono
def create_icon(size):
    # Crear imagen con fondo azul
    img = Image.new('RGB', (size, size), (52, 152, 219))  # Azul (#3498db)
    draw = ImageDraw.Draw(img)
    
    # Intentar cargar una fuente, si no está disponible usar la predeterminada
    try:
        # Ajustar tamaño de fuente según dimensiones
        font_size = int(size * 0.5)
        font = ImageFont.truetype('Arial', font_size)
    except IOError:
        font = ImageFont.load_default()
    
    # Dibujar letra "P" en el centro
    text = "P"
    # Obtener tamaño de texto para centrarlo
    try:
        text_width, text_height = draw.textsize(text, font=font)
    except:
        text_width, text_height = font_size, font_size  # Estimación aproximada
    
    # Calcular posición para centrar
    position = ((size - text_width) // 2, (size - text_height) // 2 - int(size * 0.05))
    
    # Dibujar texto
    draw.text(position, text, (255, 255, 255), font=font)
    
    # Dibujar líneas decorativas
    line_y1 = int(size * 0.7)
    line_y2 = int(size * 0.8)
    
    # Línea principal
    draw.line([(int(size * 0.25), line_y1), (int(size * 0.75), line_y1)], 
              fill=(255, 255, 255), width=int(size * 0.04))
    
    # Línea secundaria
    draw.line([(int(size * 0.2), line_y2), (int(size * 0.8), line_y2)], 
              fill=(255, 255, 255), width=int(size * 0.02))
    
    return img

# Crear iconos de diferentes tamaños
sizes = [192, 512]
for size in sizes:
    icon = create_icon(size)
    icon.save(f'static/pwa/icon-{size}.png')
    print(f"Created icon-{size}.png")

print("Icons created successfully!")
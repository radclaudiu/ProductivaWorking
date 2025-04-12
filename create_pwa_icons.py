from PIL import Image, ImageDraw, ImageFont
import os

def create_pwa_icon(size):
    """
    Crea un icono para PWA con fondo azul y la letra 'P' en color blanco
    
    Args:
        size: Tamaño del icono (ancho y alto en píxeles)
    
    Returns:
        Una imagen PIL del icono
    """
    # Crear una imagen con fondo azul
    background_color = (51, 103, 145)  # Color azul de Productiva (#336791)
    image = Image.new('RGB', (size, size), color=background_color)
    draw = ImageDraw.Draw(image)
    
    # Determinar el tamaño de la fuente basado en el tamaño del icono
    font_size = int(size * 0.6)
    
    try:
        # Intenta usar una fuente del sistema
        font = ImageFont.truetype("Arial", font_size)
    except IOError:
        # Si no encuentra la fuente, usa la fuente por defecto
        font = ImageFont.load_default()
    
    # Dibujar la letra 'P' en blanco centrada
    text = "P"
    text_color = (255, 255, 255)  # Blanco
    
    # Para centrar el texto, primero calculamos su ancho y alto
    # Usa textbbox en lugar de textsize que está obsoleto
    left, top, right, bottom = draw.textbbox((0, 0), text, font=font)
    text_width = right - left
    text_height = bottom - top
    
    # Posición para centrar
    position = ((size - text_width) // 2, (size - text_height) // 2 - int(size * 0.05))
    
    # Dibujar el texto
    draw.text(position, text, fill=text_color, font=font)
    
    return image

def save_pwa_icons():
    """
    Crea y guarda los iconos para PWA en diferentes tamaños
    """
    # Asegurarse de que el directorio exista
    os.makedirs('static/pwa', exist_ok=True)
    
    # Crear y guardar iconos en diferentes tamaños
    sizes = [192, 512]
    
    for size in sizes:
        icon = create_pwa_icon(size)
        icon_path = f'static/pwa/icon-{size}.png'
        icon.save(icon_path)
        print(f"Icono creado: {icon_path}")

if __name__ == "__main__":
    save_pwa_icons()
import os
from PIL import Image, ImageDraw, ImageFont

def create_pwa_icon(size, text="P"):
    """
    Crea un icono de PWA cuadrado con un fondo azul y una letra
    
    Args:
        size: Tamaño del icono en píxeles (ancho y alto)
        text: Texto a mostrar en el icono (por defecto "P" para Productiva)
    
    Returns:
        Una imagen PIL del icono
    """
    # Crear una imagen cuadrada
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Dibujar un círculo azul como fondo
    background_color = (52, 152, 219)  # Azul similar al tema de la aplicación
    draw.ellipse((0, 0, size, size), fill=background_color)
    
    # Añadir texto
    # Determinar el tamaño de fuente proporcional al icono
    font_size = int(size * 0.6)
    try:
        # Intentar cargar una fuente sans-serif
        font = ImageFont.truetype("arial.ttf", font_size)
    except IOError:
        # Si la fuente no está disponible, usar la fuente por defecto
        font = ImageFont.load_default()
    
    # Centrar el texto
    text_width, text_height = draw.textsize(text, font=font) if hasattr(draw, 'textsize') else (font_size, font_size)
    position = ((size - text_width) // 2, (size - text_height) // 2)
    
    # Dibujar el texto
    draw.text(position, text, fill="white", font=font)
    
    return img

def generate_pwa_icons():
    """
    Genera los iconos de diferentes tamaños para la PWA
    """
    icon_sizes = [72, 96, 128, 144, 152, 192, 384, 512]
    icon_dir = os.path.join("static", "icons")
    
    # Asegurarse de que el directorio existe
    os.makedirs(icon_dir, exist_ok=True)
    
    # Generar cada icono
    for size in icon_sizes:
        icon = create_pwa_icon(size)
        icon_path = os.path.join(icon_dir, f"icon-{size}x{size}.png")
        icon.save(icon_path)
        print(f"Icono generado: {icon_path}")
    
    # Generar un favicon.ico (16x16)
    favicon = create_pwa_icon(16)
    favicon_path = os.path.join("static", "favicon.ico")
    favicon.save(favicon_path)
    print(f"Favicon generado: {favicon_path}")

if __name__ == "__main__":
    generate_pwa_icons()
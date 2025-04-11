import os
import logging
from datetime import timedelta

# Configure logging
# Cambiar a DEBUG para depuración
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s [%(levelname)s] %(name)s: %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)

class Config:
    """Base configuration."""
    # Base configuration
    SECRET_KEY = os.environ.get('SESSION_SECRET', 'dev-key-for-development')
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    
    # Configure SQLAlchemy database connection
    SQLALCHEMY_DATABASE_URI = os.environ.get('DATABASE_URL', 'sqlite:///app.db')
    SQLALCHEMY_ENGINE_OPTIONS = {
        "pool_recycle": 120,         # Reciclar conexiones cada 2 minutos
        "pool_pre_ping": True,       # Verificar conexiones antes de usarlas
        "pool_size": 20,             # Aumentar el tamaño del pool de conexiones
        "max_overflow": 15,          # Permitir 15 conexiones adicionales si el pool está lleno
        "pool_timeout": 15,          # Timeout para obtener una conexión del pool (segundos)
        "connect_args": {
            "connect_timeout": 10,   # Timeout para establecer la conexión (segundos)
            "keepalives": 1,         # Activar keepalives
            "keepalives_idle": 30,   # Tiempo de inactividad antes de enviar un keepalive (segundos)
            "keepalives_interval": 10, # Intervalo entre keepalives (segundos)
            "keepalives_count": 5,   # Número de keepalives fallidos antes de cerrar la conexión
        },
        "echo": False,               # Deshabilitar echo de las consultas SQL
        "echo_pool": False,          # Deshabilitar echo del pool
    }
    
    # File upload configuration
    UPLOAD_FOLDER = os.path.join(os.getcwd(), 'uploads')
    MAX_CONTENT_LENGTH = 16 * 1024 * 1024  # 16MB max upload size
    ALLOWED_EXTENSIONS = {'pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png'}
    
    # JWT configuration
    JWT_SECRET_KEY = os.environ.get('SESSION_SECRET', 'dev-key-for-development')
    JWT_ACCESS_TOKEN_EXPIRES = timedelta(hours=1)
    
    # Session configuration
    PERMANENT_SESSION_LIFETIME = timedelta(days=7)
    
    # Ensure upload directory exists
    @staticmethod
    def init_app(app):
        os.makedirs(Config.UPLOAD_FOLDER, exist_ok=True)

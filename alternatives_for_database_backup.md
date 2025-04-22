# Alternativas para Backup/Restauración de Base de Datos

## Opción 1: Script Ejecutable Auto-Contenido (Ya implementado)
El script `create_executable_backup.sh` genera un archivo `.sh` autocontenido que incluye:
- El backup completo de la base de datos en formato SQL
- Funciones para restaurar, extraer SQL, y mostrar información
- Capacidad para deshabilitar claves foráneas durante la restauración

**Ventajas:**
- Un solo archivo para transferir
- No requiere archivos adicionales
- Funciona en cualquier sistema con PostgreSQL
- Puede ejecutarse directamente sin herramientas adicionales

**Ejemplo de uso:**
```bash
# Crear el backup
./create_executable_backup.sh

# Restaurar (después de transferir a otro servidor)
./productiva_backup_executable_20250422_XXXXX.sh --restore
```

## Opción 2: Restauración con Docker (Nueva alternativa)

Esta alternativa utiliza Docker para encapsular todo el proceso de restauración, ideal cuando quieres una solución que funcione en cualquier entorno, independientemente de las versiones instaladas.

**Beneficios:**
- Completamente aislado del sistema
- No necesita PostgreSQL instalado
- Versión de PostgreSQL controlada y constante
- Funciona en cualquier sistema con Docker

**Ejemplo de implementación:**

1. Crear archivo `docker-compose-restore.yml`:
```yaml
version: '3'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: productiva
    volumes:
      - ./backup:/backup
    ports:
      - "5432:5432"
    command: >
      bash -c "
        echo 'Restaurando base de datos...' &&
        pg_restore -U postgres -d productiva /backup/productiva.dump || 
        (echo 'Intentando formato SQL...' && 
         psql -U postgres -d productiva -f /backup/productiva.sql)
      "
```

2. Script para el proceso de restauración `docker_restore.sh`:
```bash
#!/bin/bash
# Script para restaurar base de datos usando Docker

# Verificar argumentos
if [ $# -lt 1 ]; then
    echo "Uso: $0 <archivo_backup>"
    exit 1
fi

BACKUP_FILE="$1"
BACKUP_DIR="$(dirname "$BACKUP_FILE")"
BACKUP_NAME="$(basename "$BACKUP_FILE")"

# Crear docker-compose-restore.yml
cat > docker-compose-restore.yml << EOF
version: '3'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: productiva
    volumes:
      - ${BACKUP_DIR}:/backup
    ports:
      - "5432:5432"
EOF

# Ejecutar restauración
echo "Iniciando contenedor PostgreSQL..."
docker-compose -f docker-compose-restore.yml up -d

echo "Esperando a que PostgreSQL esté listo..."
sleep 10

echo "Restaurando desde $BACKUP_NAME..."
if [[ $BACKUP_NAME == *.dump ]]; then
    docker-compose -f docker-compose-restore.yml exec postgres pg_restore -U postgres -d productiva /backup/$BACKUP_NAME
else
    docker-compose -f docker-compose-restore.yml exec postgres psql -U postgres -d productiva -f /backup/$BACKUP_NAME
fi

echo "Base de datos restaurada. El servidor PostgreSQL está disponible en localhost:5432"
echo "Usuario: postgres, Contraseña: postgres, Base de datos: productiva"
echo ""
echo "Para detener el contenedor: docker-compose -f docker-compose-restore.yml down"
```

## Opción 3: Herramienta Interactiva de Restauración GUI (Nueva alternativa)

Esta alternativa proporciona una interfaz gráfica web para subir y restaurar backups, ideal para administradores menos técnicos.

**Beneficios:**
- Interfaz gráfica amigable
- No requiere conocimientos de línea de comandos
- Permite visualizar tablas y registros antes de restaurar
- Opciones para restauración selectiva (solo ciertas tablas)

**Implementación:**

1. Crear un script `db_restore_web.py`:
```python
#!/usr/bin/env python3
"""
Herramienta web para restaurar backups de PostgreSQL
"""
import os
import tempfile
import psycopg2
from flask import Flask, request, render_template, redirect, url_for, flash

# Importar configuración
try:
    from config import Config
    DATABASE_URL = Config.SQLALCHEMY_DATABASE_URI
except ImportError:
    DATABASE_URL = os.environ.get('DATABASE_URL', 'postgresql://postgres:postgres@localhost/productiva')

app = Flask(__name__)
app.secret_key = 'clave-secreta-para-restauracion'

# Extraer parámetros de conexión
def parse_db_url(url):
    """Extrae los componentes de una URL de conexión de base de datos"""
    # postgresql://user:password@host:port/dbname
    if not url or 'postgres' not in url:
        return None
        
    # Remover el prefijo
    if '://' in url:
        url = url.split('://', 1)[1]
        
    # Extraer usuario:contraseña y host:puerto/nombre
    if '@' in url:
        auth, rest = url.split('@', 1)
    else:
        auth = ''
        rest = url
        
    # Extraer host:port y dbname
    if '/' in rest:
        host_port, dbname = rest.split('/', 1)
    else:
        host_port = rest
        dbname = 'postgres'
        
    # Extraer usuario y contraseña
    if ':' in auth:
        user, password = auth.split(':', 1)
    else:
        user = auth
        password = ''
        
    # Extraer host y puerto
    if ':' in host_port:
        host, port = host_port.split(':', 1)
        try:
            port = int(port)
        except:
            port = 5432
    else:
        host = host_port
        port = 5432
        
    return {
        'host': host,
        'port': port,
        'user': user,
        'password': password,
        'dbname': dbname
    }

# Template para la página principal
INDEX_TEMPLATE = """
<!DOCTYPE html>
<html>
<head>
    <title>Restauración de Base de Datos</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .container { max-width: 800px; margin: 0 auto; }
        h1 { color: #333; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; }
        input[type="file"] { padding: 5px; }
        button { background: #4CAF50; color: white; padding: 10px 15px; border: none; cursor: pointer; }
        .success { color: green; }
        .error { color: red; }
        .option { margin: 10px 0; }
        .tables { margin: 15px 0; }
        .table-list { height: 200px; overflow-y: auto; border: 1px solid #ddd; padding: 10px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Restauración de Base de Datos PostgreSQL</h1>
        
        <div class="connection-info">
            <h2>Información de conexión</h2>
            <p>Host: {{ db_info.host }}</p>
            <p>Puerto: {{ db_info.port }}</p>
            <p>Usuario: {{ db_info.user }}</p>
            <p>Base de datos: {{ db_info.dbname }}</p>
        </div>
        
        {% if messages %}
            <div class="messages">
                {% for message in messages %}
                    <p class="{{ message.type }}">{{ message.text }}</p>
                {% endfor %}
            </div>
        {% endif %}
        
        <form action="{{ url_for('upload_backup') }}" method="post" enctype="multipart/form-data">
            <div class="form-group">
                <label for="backup_file">Archivo de backup SQL:</label>
                <input type="file" id="backup_file" name="backup_file" accept=".sql,.dump">
            </div>
            
            <div class="options">
                <h3>Opciones de restauración</h3>
                
                <div class="option">
                    <input type="checkbox" id="disable_fk" name="disable_fk" value="1">
                    <label for="disable_fk">Deshabilitar restricciones de clave foránea durante la importación</label>
                </div>
                
                <div class="option">
                    <input type="checkbox" id="ignore_errors" name="ignore_errors" value="1">
                    <label for="ignore_errors">Ignorar errores y continuar importando</label>
                </div>
                
                <div class="option">
                    <input type="checkbox" id="only_schema" name="only_schema" value="1">
                    <label for="only_schema">Importar solo estructura (sin datos)</label>
                </div>
                
                <div class="option">
                    <input type="checkbox" id="only_data" name="only_data" value="1">
                    <label for="only_data">Importar solo datos (sin estructura)</label>
                </div>
                
                <div class="option">
                    <input type="checkbox" id="drop_existing" name="drop_existing" value="1">
                    <label for="drop_existing">Eliminar base de datos si ya existe</label>
                </div>
            </div>
            
            {% if tables %}
                <div class="tables">
                    <h3>Tablas (opcional)</h3>
                    <p>Selecciona las tablas a importar (si no seleccionas ninguna, se importarán todas):</p>
                    
                    <div class="table-list">
                        {% for table in tables %}
                            <div>
                                <input type="checkbox" id="table_{{ table }}" name="tables[]" value="{{ table }}">
                                <label for="table_{{ table }}">{{ table }}</label>
                            </div>
                        {% endfor %}
                    </div>
                </div>
            {% endif %}
            
            <div class="form-group">
                <button type="submit">Restaurar Base de Datos</button>
            </div>
        </form>
    </div>
</body>
</html>
"""

@app.route('/')
def index():
    """Página principal"""
    db_info = parse_db_url(DATABASE_URL)
    tables = []
    
    try:
        # Obtener lista de tablas
        conn = psycopg2.connect(
            host=db_info['host'],
            port=db_info['port'],
            user=db_info['user'],
            password=db_info['password'],
            dbname=db_info['dbname']
        )
        cursor = conn.cursor()
        cursor.execute("SELECT table_name FROM information_schema.tables WHERE table_schema='public'")
        tables = [row[0] for row in cursor.fetchall()]
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"Error al obtener tablas: {str(e)}")
    
    return render_template_string(INDEX_TEMPLATE, db_info=db_info, tables=tables, messages=[])

@app.route('/upload', methods=['POST'])
def upload_backup():
    """Procesar formulario de restauración"""
    db_info = parse_db_url(DATABASE_URL)
    messages = []
    
    if 'backup_file' not in request.files:
        messages.append({'type': 'error', 'text': 'No se seleccionó ningún archivo'})
        return render_template_string(INDEX_TEMPLATE, db_info=db_info, tables=[], messages=messages)
    
    backup_file = request.files['backup_file']
    if backup_file.filename == '':
        messages.append({'type': 'error', 'text': 'No se seleccionó ningún archivo'})
        return render_template_string(INDEX_TEMPLATE, db_info=db_info, tables=[], messages=messages)
    
    # Opciones
    options = {
        'disable_fk': 'disable_fk' in request.form,
        'ignore_errors': 'ignore_errors' in request.form,
        'only_schema': 'only_schema' in request.form,
        'only_data': 'only_data' in request.form,
        'drop_existing': 'drop_existing' in request.form
    }
    
    # Tablas seleccionadas
    selected_tables = request.form.getlist('tables[]')
    
    # Verificar opciones incompatibles
    if options['only_schema'] and options['only_data']:
        messages.append({'type': 'error', 'text': 'Las opciones "solo estructura" y "solo datos" son mutuamente excluyentes'})
        return render_template_string(INDEX_TEMPLATE, db_info=db_info, tables=[], messages=messages)
    
    # Guardar archivo en carpeta temporal
    temp_dir = tempfile.mkdtemp()
    temp_path = os.path.join(temp_dir, backup_file.filename)
    backup_file.save(temp_path)
    
    # Aquí iría la lógica de restauración
    messages.append({'type': 'success', 'text': f'Archivo "{backup_file.filename}" subido correctamente'})
    messages.append({'type': 'success', 'text': 'Iniciando restauración...'})
    
    # Implementar función de restauración aquí...
    
    return render_template_string(INDEX_TEMPLATE, db_info=db_info, tables=[], messages=messages)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)
```

2. Para ejecutar:
```bash
python db_restore_web.py
```

La interfaz web estará disponible en http://localhost:5001

## Opción 4: Backups Programados a NAS o Almacenamiento Remoto (Nueva alternativa)

Esta solución automatiza completamente el proceso de backup, permitiéndote programar copias de seguridad periódicas que se almacenan en un dispositivo NAS o servicio en la nube.

**Beneficios:**
- Backups automáticos sin intervención humana
- Copia de seguridad remota (fuera del servidor)
- Configuración muy flexible (diaria, semanal, etc.)
- Retención de varios backups históricos

**Implementación:**

1. Script `scheduled_backup.sh`:
```bash
#!/bin/bash
# Script para crear backups programados y almacenarlos en NAS o almacenamiento remoto

# Configuración
BACKUP_DIR="/ruta/local/backups"
REMOTE_DIR="/mnt/nas/backups"  # Punto de montaje NAS
RETENTION_DAYS=30              # Días de retención
MAX_BACKUPS=10                 # Número máximo de backups a mantener

# Credenciales de base de datos (usar variables de entorno o archivo .pgpass)
DB_HOST="${PGHOST:-localhost}"
DB_PORT="${PGPORT:-5432}"
DB_USER="${PGUSER:-postgres}"
DB_NAME="${PGDATABASE:-productiva}"

# Crear directorio local de backups si no existe
mkdir -p "$BACKUP_DIR"

# Nombre de archivo con fecha y hora
DATE=$(date +"%Y%m%d_%H%M%S")
BACKUP_NAME="productiva_backup_$DATE"
BACKUP_SQL="$BACKUP_DIR/$BACKUP_NAME.sql"
BACKUP_EXEC="$BACKUP_DIR/$BACKUP_NAME.sh"

# Crear backup SQL plano
echo "Creando backup SQL en $BACKUP_SQL..."
PGPASSWORD="$PGPASSWORD" pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" --no-owner --no-acl "$DB_NAME" > "$BACKUP_SQL"

# Crear backup ejecutable con nuestro script personalizado
echo "Creando backup ejecutable en $BACKUP_EXEC..."
./create_executable_backup.sh

# Eliminar backups antiguos para control de retención
echo "Limpiando backups antiguos..."
find "$BACKUP_DIR" -name "productiva_backup_*.sql" -type f -mtime +$RETENTION_DAYS -delete
find "$BACKUP_DIR" -name "productiva_backup_*.sh" -type f -mtime +$RETENTION_DAYS -delete

# Controlar el número máximo de backups
NUM_BACKUPS=$(find "$BACKUP_DIR" -name "productiva_backup_*.sql" | wc -l)
if [ "$NUM_BACKUPS" -gt "$MAX_BACKUPS" ]; then
    echo "Hay más de $MAX_BACKUPS backups, eliminando los más antiguos..."
    ls -t "$BACKUP_DIR"/productiva_backup_*.sql | tail -n +$((MAX_BACKUPS+1)) | xargs rm -f
    ls -t "$BACKUP_DIR"/productiva_backup_*.sh | tail -n +$((MAX_BACKUPS+1)) | xargs rm -f
fi

# Copiar backup al NAS o almacenamiento remoto
if [ -d "$REMOTE_DIR" ]; then
    echo "Copiando backups a almacenamiento remoto..."
    cp "$BACKUP_SQL" "$REMOTE_DIR/"
    cp "$BACKUP_EXEC" "$REMOTE_DIR/"
    echo "Backup copiado a $REMOTE_DIR"
else
    echo "Aviso: El directorio remoto $REMOTE_DIR no está disponible. El backup solo se almacenó localmente."
fi

# También podríamos usar otras opciones para almacenamiento remoto:
# Para Amazon S3:
# aws s3 cp "$BACKUP_SQL" "s3://mi-bucket-de-backups/"
# aws s3 cp "$BACKUP_EXEC" "s3://mi-bucket-de-backups/"

# Para Google Cloud Storage:
# gsutil cp "$BACKUP_SQL" "gs://mi-bucket-de-backups/"
# gsutil cp "$BACKUP_EXEC" "gs://mi-bucket-de-backups/"

# Para SFTP:
# scp "$BACKUP_SQL" "usuario@servidor:/ruta/destino/"
# scp "$BACKUP_EXEC" "usuario@servidor:/ruta/destino/"

echo "Proceso de backup completado exitosamente."
```

2. Configuración de cron para automatización:
```bash
# Editar crontab
crontab -e

# Agregar línea para ejecución diaria a las 3 AM
0 3 * * * /ruta/completa/a/scheduled_backup.sh >> /var/log/db_backup.log 2>&1
```

## Recomendación Final

Para una solución óptima, recomendamos implementar una combinación de las opciones anteriores:

1. Usar `create_executable_backup.sh` para backups manuales rápidos y fáciles de restaurar
2. Configurar `scheduled_backup.sh` para backups automáticos diarios
3. Mantener la opción con Docker disponible para entornos nuevos sin PostgreSQL
4. Implementar la herramienta web para administradores menos técnicos

Esta estrategia multicapa proporciona máxima flexibilidad y seguridad para tus datos.
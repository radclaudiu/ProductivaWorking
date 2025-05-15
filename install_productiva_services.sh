#!/bin/bash

# ============================================================================
# Script de instalación de servicios automáticos para Productiva
# ============================================================================
# Este script configura todos los servicios automáticos (cron jobs) necesarios
# para que la aplicación Productiva funcione correctamente.
# 
# Servicios instalados:
# 1. Cierre automático de fichajes pendientes
# 2. Reinicio de tareas diarias/mensuales
# 3. Reinicio de tareas semanales
# 4. Limpieza de imágenes de recibos antiguas
# 5. Backup automático de la base de datos
# ============================================================================

# Colores para mensajes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para mostrar mensajes de información
info() {
    echo -e "${BLUE}[INFO] $1${NC}"
}

# Función para mostrar mensajes de éxito
success() {
    echo -e "${GREEN}[OK] $1${NC}"
}

# Función para mostrar mensajes de advertencia
warning() {
    echo -e "${YELLOW}[ADVERTENCIA] $1${NC}"
}

# Función para mostrar mensajes de error
error() {
    echo -e "${RED}[ERROR] $1${NC}"
}

# Verificar que el script se ejecute como root
if [ "$(id -u)" != "0" ]; then
   error "Este script debe ejecutarse como root o con sudo"
   exit 1
fi

# Obtener el directorio actual (donde está instalada la aplicación)
APP_DIR=$(pwd)
info "Directorio de la aplicación: $APP_DIR"

# Verificar que los archivos necesarios existan
if [ ! -f "$APP_DIR/checkpoint_closer_service.py" ]; then
    error "No se encontró el archivo checkpoint_closer_service.py"
    exit 1
fi

if [ ! -f "$APP_DIR/daily_tasks_reset_service.py" ]; then
    error "No se encontró el archivo daily_tasks_reset_service.py"
    exit 1
fi

if [ ! -f "$APP_DIR/weekly_tasks_reset_service.py" ]; then
    error "No se encontró el archivo weekly_tasks_reset_service.py"
    exit 1
fi

if [ ! -f "$APP_DIR/cleanup_receipt_images.py" ]; then
    error "No se encontró el archivo cleanup_receipt_images.py"
    exit 1
fi

# Crear directorio para los logs
LOG_DIR="$APP_DIR/logs"
if [ ! -d "$LOG_DIR" ]; then
    info "Creando directorio para logs: $LOG_DIR"
    mkdir -p "$LOG_DIR"
fi

# Crear directorio para backups
BACKUP_DIR="$APP_DIR/backups"
if [ ! -d "$BACKUP_DIR" ]; then
    info "Creando directorio para backups: $BACKUP_DIR"
    mkdir -p "$BACKUP_DIR"
fi

# Determinar el intérprete de Python
if command -v python3 &> /dev/null; then
    PYTHON_CMD="python3"
elif command -v python &> /dev/null; then
    PYTHON_CMD="python"
else
    error "No se encontró un intérprete de Python. Por favor, instala Python 3."
    exit 1
fi

info "Usando intérprete de Python: $PYTHON_CMD"

# Crear los scripts ejecutables para cada servicio
info "Creando scripts ejecutables para los servicios..."

# 1. Script para cierre automático de fichajes pendientes
cat > "$APP_DIR/run_checkpoint_closer.sh" << EOF
#!/bin/bash
cd "$APP_DIR"
$PYTHON_CMD checkpoint_closer_service.py --run_once > "$LOG_DIR/checkpoint_closer_\$(date +%Y%m%d).log" 2>&1
EOF
chmod +x "$APP_DIR/run_checkpoint_closer.sh"
success "Creado script para cierre automático de fichajes"

# 2. Script para reinicio de tareas diarias/mensuales
cat > "$APP_DIR/run_daily_tasks_reset.sh" << EOF
#!/bin/bash
cd "$APP_DIR"
$PYTHON_CMD daily_tasks_reset_service.py --run_once > "$LOG_DIR/daily_tasks_reset_\$(date +%Y%m%d).log" 2>&1
EOF
chmod +x "$APP_DIR/run_daily_tasks_reset.sh"
success "Creado script para reinicio de tareas diarias/mensuales"

# 3. Script para reinicio de tareas semanales
cat > "$APP_DIR/run_weekly_tasks_reset.sh" << EOF
#!/bin/bash
cd "$APP_DIR"
$PYTHON_CMD weekly_tasks_reset_service.py --run_once > "$LOG_DIR/weekly_tasks_reset_\$(date +%Y%m%d).log" 2>&1
EOF
chmod +x "$APP_DIR/run_weekly_tasks_reset.sh"
success "Creado script para reinicio de tareas semanales"

# 4. Script para limpieza de imágenes de recibos
cat > "$APP_DIR/run_cleanup_receipts.sh" << EOF
#!/bin/bash
cd "$APP_DIR"
$PYTHON_CMD cleanup_receipt_images.py > "$LOG_DIR/cleanup_receipts_\$(date +%Y%m%d).log" 2>&1
EOF
chmod +x "$APP_DIR/run_cleanup_receipts.sh"
success "Creado script para limpieza de imágenes de recibos"

# 5. Script para backup diario de la base de datos
cat > "$APP_DIR/run_daily_backup.sh" << EOF
#!/bin/bash
cd "$APP_DIR"
./create_full_backup.sh > "$LOG_DIR/backup_\$(date +%Y%m%d).log" 2>&1
# Mover el backup generado a la carpeta de backups
mv productiva_backup_*.sql "$BACKUP_DIR/" 2>/dev/null
mv productiva_backup_executable_*.sh "$BACKUP_DIR/" 2>/dev/null
# Eliminar backups antiguos (más de 7 días)
find "$BACKUP_DIR" -name "productiva_backup_*.sql" -type f -mtime +7 -delete
find "$BACKUP_DIR" -name "productiva_backup_executable_*.sh" -type f -mtime +7 -delete
EOF
chmod +x "$APP_DIR/run_daily_backup.sh"
success "Creado script para backup diario de la base de datos"

# Verificar si el archivo crontab temporal ya existe y eliminarlo si es así
if [ -f /tmp/productiva_crontab ]; then
    rm /tmp/productiva_crontab
fi

# Obtener el crontab actual y guardarlo en un archivo temporal
crontab -l > /tmp/productiva_crontab 2>/dev/null || echo "" > /tmp/productiva_crontab

# Verificar si las entradas ya existen en el crontab
ALREADY_INSTALLED=0
if grep -q "run_checkpoint_closer.sh" /tmp/productiva_crontab; then
    warning "Las entradas de cron para Productiva ya parecen estar instaladas"
    ALREADY_INSTALLED=1
fi

if [ $ALREADY_INSTALLED -eq 1 ]; then
    read -p "¿Desea reinstalar las entradas de cron? (s/n): " REINSTALL
    if [[ $REINSTALL != "s" && $REINSTALL != "S" ]]; then
        info "Operación cancelada. No se modificaron las entradas de cron."
        exit 0
    fi
    # Eliminar las entradas existentes
    sed -i '/run_checkpoint_closer.sh/d' /tmp/productiva_crontab
    sed -i '/run_daily_tasks_reset.sh/d' /tmp/productiva_crontab
    sed -i '/run_weekly_tasks_reset.sh/d' /tmp/productiva_crontab
    sed -i '/run_cleanup_receipts.sh/d' /tmp/productiva_crontab
    sed -i '/run_daily_backup.sh/d' /tmp/productiva_crontab
    info "Entradas de cron antiguas eliminadas"
fi

# Agregar las nuevas entradas de cron
info "Agregando tareas programadas al crontab..."

# Cierre automático de fichajes: cada 10 minutos
echo "*/10 * * * * $APP_DIR/run_checkpoint_closer.sh" >> /tmp/productiva_crontab

# Reinicio de tareas diarias: todos los días a las 5:00 AM
echo "0 5 * * * $APP_DIR/run_daily_tasks_reset.sh" >> /tmp/productiva_crontab

# Reinicio de tareas semanales: todos los lunes a las 4:00 AM
echo "0 4 * * 1 $APP_DIR/run_weekly_tasks_reset.sh" >> /tmp/productiva_crontab

# Limpieza de imágenes de recibos: todos los días a las 3:00 AM
echo "0 3 * * * $APP_DIR/run_cleanup_receipts.sh" >> /tmp/productiva_crontab

# Backup diario: todos los días a las 2:00 AM
echo "0 2 * * * $APP_DIR/run_daily_backup.sh" >> /tmp/productiva_crontab

# Actualizar el crontab
crontab /tmp/productiva_crontab
rm /tmp/productiva_crontab

success "Tareas programadas instaladas correctamente"

# Mostrar información sobre las tareas instaladas
echo ""
echo "========================================================"
echo "SERVICIOS PROGRAMADOS PARA PRODUCTIVA"
echo "========================================================"
echo "1. Cierre automático de fichajes pendientes"
echo "   - Ejecución: Cada 10 minutos"
echo "   - Script: $APP_DIR/run_checkpoint_closer.sh"
echo "   - Log: $LOG_DIR/checkpoint_closer_AAAAMMDD.log"
echo ""
echo "2. Reinicio de tareas diarias/mensuales"
echo "   - Ejecución: Todos los días a las 5:00 AM"
echo "   - Script: $APP_DIR/run_daily_tasks_reset.sh"
echo "   - Log: $LOG_DIR/daily_tasks_reset_AAAAMMDD.log"
echo ""
echo "3. Reinicio de tareas semanales"
echo "   - Ejecución: Todos los lunes a las 4:00 AM"
echo "   - Script: $APP_DIR/run_weekly_tasks_reset.sh"
echo "   - Log: $LOG_DIR/weekly_tasks_reset_AAAAMMDD.log"
echo ""
echo "4. Limpieza de imágenes de recibos antiguas"
echo "   - Ejecución: Todos los días a las 3:00 AM"
echo "   - Script: $APP_DIR/run_cleanup_receipts.sh"
echo "   - Log: $LOG_DIR/cleanup_receipts_AAAAMMDD.log"
echo ""
echo "5. Backup diario de la base de datos"
echo "   - Ejecución: Todos los días a las 2:00 AM"
echo "   - Script: $APP_DIR/run_daily_backup.sh"
echo "   - Log: $LOG_DIR/backup_AAAAMMDD.log"
echo "   - Ubicación de backups: $BACKUP_DIR"
echo "   - Retención: 7 días"
echo "========================================================"
echo ""

# Instrucciones para verificar la instalación
echo "Para verificar que las tareas se han instalado correctamente, ejecute:"
echo "crontab -l"
echo ""
echo "Para probar la ejecución de un servicio manualmente, ejecute (por ejemplo):"
echo "$APP_DIR/run_checkpoint_closer.sh"
echo ""
echo "Para ver los logs generados, consulte los archivos en:"
echo "$LOG_DIR"
echo ""

success "Instalación completada exitosamente"
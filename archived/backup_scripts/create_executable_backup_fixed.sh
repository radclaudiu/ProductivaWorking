#!/bin/bash
# ============================================================================
# SCRIPT MEJORADO PARA CREAR BACKUP EJECUTABLE DE PRODUCTIVA
# Versión corregida: 23-04-2025
# ============================================================================
# Este script genera un archivo ejecutable que contiene:
# 1. El dump completo de la base de datos PostgreSQL
# 2. Instrucciones y comandos para restaurar la base de datos
#
# El archivo resultante puede restaurarse fácilmente usando:
# ./nombre_del_backup.sh --restore [opciones]
#

# Colores para mensajes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Obtener fecha y hora para el nombre del archivo
DATE_STAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="productiva_backup_executable_${DATE_STAMP}.sh"

# Verificar si se está pidiendo ayuda
if [ "$1" = "--help" ]; then
  cat << 'HELP_TEXT'
Script para crear un backup ejecutable de la base de datos.

Uso: ./create_executable_backup_fixed.sh [DB_NAME] [DB_HOST] [DB_PORT] [DB_USER]

Parámetros (opcionales):
  DB_NAME    Nombre de la base de datos (predeterminado: productiva)
  DB_HOST    Host del servidor (predeterminado: localhost)
  DB_PORT    Puerto del servidor (predeterminado: 5432)
  DB_USER    Usuario (predeterminado: postgres)

Ejemplo:
  ./create_executable_backup_fixed.sh productiva localhost 5432 postgres
HELP_TEXT
  exit 0
fi

# Parámetros de conexión de PostgreSQL
DB_NAME=${1:-${PGDATABASE:-"productiva"}}
DB_HOST=${2:-${PGHOST:-"localhost"}}
DB_PORT=${3:-${PGPORT:-"5432"}}
DB_USER=${4:-${PGUSER:-"postgres"}}
DB_PASSWORD=${PGPASSWORD:-""}

# Verificar si hay variable de entorno DATABASE_URL y extraer los datos
if [ -n "$DATABASE_URL" ]; then
  echo " - Usando DATABASE_URL para conexión"
  # Extraer componentes de DATABASE_URL
  if [[ "$DATABASE_URL" =~ postgres://([^:]+):([^@]+)@([^:]+):([0-9]+)/([^?]+) ]]; then
    DB_USER="${BASH_REMATCH[1]}"
    DB_PASSWORD="${BASH_REMATCH[2]}"
    DB_HOST="${BASH_REMATCH[3]}"
    DB_PORT="${BASH_REMATCH[4]}"
    DB_NAME="${BASH_REMATCH[5]}"
  fi
fi

# Si aún no hay contraseña, pedirla
if [ -z "$DB_PASSWORD" ]; then
  echo -n " - Ingrese la contraseña para $DB_USER: "
  read -s DB_PASSWORD
  echo ""
fi

# Archivo temporal para .pgpass
PGPASS_FILE="${HOME}/.pgpass"
PGPASS_EXISTS=0

# Guardar .pgpass existente si hay uno
if [ -f "$PGPASS_FILE" ]; then
  PGPASS_EXISTS=1
  cp "$PGPASS_FILE" "${PGPASS_FILE}.bak"
fi

# Crear o modificar .pgpass temporalmente para evitar prompt de contraseña
echo "${DB_HOST}:${DB_PORT}:${DB_NAME}:${DB_USER}:${DB_PASSWORD}" > "$PGPASS_FILE"
chmod 600 "$PGPASS_FILE"

# Crear archivo temporal para el dump SQL
TEMP_SQL=$(mktemp)

echo -e "${BLUE}=== Generando backup ejecutable de PostgreSQL ===${NC}"
echo -e "Base de datos: ${GREEN}$DB_NAME${NC}"
echo -e "Servidor: ${GREEN}$DB_HOST:$DB_PORT${NC}"
echo -e "Usuario: ${GREEN}$DB_USER${NC}"
echo -e "Archivo de salida: ${GREEN}$BACKUP_FILE${NC}"
echo ""

# Obtener y mostrar información general
TABLE_COUNT=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public'" | tr -d ' ')
RECORD_COUNT=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT SUM(n_live_tup) FROM pg_stat_user_tables" | tr -d ' ')
PG_VERSION=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SHOW server_version" | tr -d ' ')

echo -e "Tablas encontradas: ${GREEN}$TABLE_COUNT${NC}"
echo -e "Registros totales: ${GREEN}$RECORD_COUNT${NC}"
echo -e "Versión PostgreSQL: ${GREEN}$PG_VERSION${NC}"
echo ""

# Crear script SQL inicial con función para tipos enumerados
echo "Preparando creación de tipos enumerados..."
cat > "$TEMP_SQL" << 'EOL'
-- Script SQL generado para restauración de PRODUCTIVA
-- Con soporte mejorado para tipos enumerados

BEGIN;

-- Función para crear tipos enumerados si no existen
DO $$
BEGIN
    -- Tipo user_role
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('admin', 'gerente', 'empleado');
    END IF;
    
    -- Tipo contract_type
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contract_type') THEN
        CREATE TYPE contract_type AS ENUM ('INDEFINIDO', 'TEMPORAL', 'PRACTICAS', 'FORMACION', 'OBRA');
    END IF;
    
    -- Tipo employee_status
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'employee_status') THEN
        CREATE TYPE employee_status AS ENUM ('activo', 'baja_medica', 'excedencia', 'vacaciones', 'inactivo');
    END IF;
    
    -- Tipo week_day
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'week_day') THEN
        CREATE TYPE week_day AS ENUM ('lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo');
    END IF;
    
    -- Tipo vacation_status
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vacation_status') THEN
        CREATE TYPE vacation_status AS ENUM ('REGISTRADA', 'DISFRUTADA');
    END IF;
    
    -- Tipo checkpoint_status
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_status') THEN
        CREATE TYPE checkpoint_status AS ENUM ('active', 'disabled', 'maintenance');
    END IF;
    
    -- Tipo checkpoint_incident_type
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_incident_type') THEN
        CREATE TYPE checkpoint_incident_type AS ENUM ('missed_checkout', 'late_checkin', 'early_checkout', 'overtime', 'manual_adjustment', 'contract_hours_adjustment');
    END IF;
    
    -- Tipo task_priority
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_priority') THEN
        CREATE TYPE task_priority AS ENUM ('alta', 'media', 'baja');
    END IF;
    
    -- Tipo task_frequency
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_frequency') THEN
        CREATE TYPE task_frequency AS ENUM ('diaria', 'semanal', 'mensual', 'unica');
    END IF;
    
    -- Tipo task_status
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_status') THEN
        CREATE TYPE task_status AS ENUM ('pendiente', 'completada', 'cancelada');
    END IF;
END $$;

EOL

# Añadir información de fecha y origen al SQL
cat >> "$TEMP_SQL" << EOL
-- Fecha: $(date '+%Y-%m-%d %H:%M:%S')
-- Base de datos origen: $DB_NAME
-- Servidor origen: $DB_HOST:$DB_PORT
-- Versión PostgreSQL: $PG_VERSION

EOL

# Exportar la estructura y datos (sin tipos enumerados, ya definidos)
echo "Exportando estructura de tablas y secuencias..."
PGPASSWORD="$DB_PASSWORD" pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
  --no-owner --no-privileges \
  --schema-only --no-comments \
  | grep -v "CREATE TYPE" | grep -v "AS ENUM" >> "$TEMP_SQL"

echo "Exportando datos..."
PGPASSWORD="$DB_PASSWORD" pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
  --no-owner --no-privileges \
  --data-only --inserts \
  --column-inserts \
  --rows-per-insert=100 \
  | grep -v "CREATE TYPE" | grep -v "AS ENUM" >> "$TEMP_SQL"

# Añadir bloque para actualizar secuencias al final del backup
cat >> "$TEMP_SQL" << 'EOL'

-- Actualizar secuencias después de restaurar datos
DO $$
DECLARE
    seq_record RECORD;
BEGIN
    FOR seq_record IN 
        SELECT 
            sch.nspname as seq_schema,
            seq.relname as seq_name,
            tab.relname as table_name,
            attr.attname as column_name
        FROM 
            pg_class seq
            JOIN pg_namespace sch ON sch.oid = seq.relnamespace
            JOIN pg_depend dep ON dep.objid = seq.oid
            JOIN pg_class tab ON dep.refobjid = tab.oid
            JOIN pg_attribute attr ON attr.attrelid = tab.oid AND attr.attnum = dep.refobjsubid
        WHERE 
            seq.relkind = 'S' AND
            sch.nspname = 'public'
    LOOP
        EXECUTE format('SELECT setval(''%I.%I'', COALESCE((SELECT MAX(%I) FROM %I.%I), 1), true)',
            seq_record.seq_schema, seq_record.seq_name,
            seq_record.column_name, seq_record.seq_schema, seq_record.table_name);
    END LOOP;
END $$;

COMMIT;
EOL

# Calcular tamaño del archivo SQL
FILE_SIZE=$(du -h "$TEMP_SQL" | cut -f1)

echo -e "SQL generado correctamente (${GREEN}$FILE_SIZE${NC})"

# Crear la cabecera del script ejecutable
echo "Generando script ejecutable..."
cat > "$BACKUP_FILE" << 'HEADER'
#!/bin/bash
# ============================================================================
# BACKUP EJECUTABLE DE BASE DE DATOS PRODUCTIVA 
# ============================================================================
# Este archivo es un script ejecutable que contiene un backup completo de la
# base de datos PostgreSQL y las instrucciones para restaurarla.
#
# Modos de uso:
#
# 1. INFORMACIÓN (no realiza cambios):
#    ./$(basename $0) --info
#
# 2. RESTAURAR (crea una nueva base de datos y restaura todo):
#    ./$(basename $0) --restore [--db DB_NAME] [--host HOST] [--port PORT] [--user USER]
#
# 3. EXTRAER SQL (solo extrae el SQL sin ejecutarlo):
#    ./$(basename $0) --extract [output_file.sql]
#
# Opciones:
#   --db NAME     Nombre de la base de datos (predeterminado: productiva)
#   --host HOST   Host del servidor PostgreSQL (predeterminado: localhost)
#   --port PORT   Puerto del servidor PostgreSQL (predeterminado: 5432)
#   --user USER   Usuario PostgreSQL (predeterminado: productiva)
#   --no-data     Omitir los datos, restaurar solo la estructura
#   --no-privs    Omitir los privilegios de usuarios
#   --force       Sobrescribir la base de datos si ya existe
#   --help        Mostrar esta ayuda
# ============================================================================

# Colores para los mensajes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para mostrar información del backup
show_info() {
    echo -e "${BLUE}================ INFORMACIÓN DEL BACKUP ================${NC}"
    echo -e "${GREEN}Fecha de creación:${NC} $(grep "# Fecha de backup:" "$0" | cut -d: -f2-)"
    echo -e "${GREEN}Base de datos origen:${NC} $(grep "# Base de datos origen:" "$0" | cut -d: -f2-)"
    echo -e "${GREEN}Servidor origen:${NC} $(grep "# Servidor origen:" "$0" | cut -d: -f2-)"
    echo -e "${GREEN}Versión PostgreSQL:${NC} $(grep "# Versión PostgreSQL:" "$0" | cut -d: -f2-)"
    echo -e "${GREEN}Tablas incluidas:${NC} $(grep "# Número de tablas:" "$0" | cut -d: -f2-)"
    echo -e "${GREEN}Registros totales:${NC} $(grep "# Registros totales:" "$0" | cut -d: -f2-)"
    echo
    echo -e "${YELLOW}Para restaurar este backup, ejecute:${NC}"
    echo -e "  $0 --restore [--db DB_NAME] [--host HOST] [--user USER]"
    echo
    echo -e "${YELLOW}Para extraer el SQL sin ejecutarlo:${NC}"
    echo -e "  $0 --extract [archivo_salida.sql]"
    echo
}

# Función para extraer el SQL sin ejecutarlo
extract_sql() {
    output_file="$1"
    if [ -z "$output_file" ]; then
        output_file="productiva_backup_extracted.sql"
    fi
    
    echo -e "${BLUE}Extrayendo SQL a ${output_file}...${NC}"
    
    # Extraer la parte SQL del script (después de la línea __SQL_DUMP_BELOW__)
    sed -n '/^__SQL_DUMP_BELOW__$/,/^__SQL_DUMP_ABOVE__$/p' "$0" | sed '1d;$d' > "$output_file"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}SQL extraído correctamente a ${output_file}${NC}"
        echo -e "Tamaño del archivo: $(du -h "$output_file" | cut -f1)"
    else
        echo -e "${RED}Error al extraer SQL${NC}"
        exit 1
    fi
}

# Función para restaurar el backup
restore_backup() {
    DB_NAME="productiva"
    DB_HOST="localhost"
    DB_PORT="5432"
    DB_USER="productiva"
    SKIP_DATA=0
    SKIP_PRIVS=0
    FORCE=0
    
    # Procesar argumentos
    while [[ "$#" -gt 0 ]]; do
        case $1 in
            --db) DB_NAME="$2"; shift ;;
            --db=*) DB_NAME="${1#*=}" ;;
            --host) DB_HOST="$2"; shift ;;
            --host=*) DB_HOST="${1#*=}" ;;
            --port) DB_PORT="$2"; shift ;;
            --port=*) DB_PORT="${1#*=}" ;;
            --user) DB_USER="$2"; shift ;;
            --user=*) DB_USER="${1#*=}" ;;
            --no-data) SKIP_DATA=1 ;;
            --no-privs) SKIP_PRIVS=1 ;;
            --force) FORCE=1 ;;
            --help) show_usage; exit 0 ;;
            *) echo -e "${RED}Opción desconocida: $1${NC}"; show_usage; exit 1 ;;
        esac
        shift
    done
    
    echo -e "${BLUE}================== RESTAURACIÓN ====================${NC}"
    echo -e "Base de datos destino: ${GREEN}$DB_NAME${NC}"
    echo -e "Servidor destino: ${GREEN}$DB_HOST:$DB_PORT${NC}"
    echo -e "Usuario: ${GREEN}$DB_USER${NC}"
    echo ""
    
    # Verificar si la base de datos ya existe
    if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -lqt | cut -d \| -f 1 | grep -qw "$DB_NAME"; then
        if [ $FORCE -eq 1 ]; then
            echo -e "${YELLOW}La base de datos '$DB_NAME' ya existe. Se eliminará...${NC}"
            
            # Cerrar conexiones activas a la base de datos
            echo "Cerrando conexiones activas a la base de datos '$DB_NAME'..."
            PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "
                SELECT pg_terminate_backend(pg_stat_activity.pid)
                FROM pg_stat_activity
                WHERE pg_stat_activity.datname = '$DB_NAME'
                AND pid <> pg_backend_pid();
            " postgres
            
            # Eliminar la base de datos
            PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "DROP DATABASE \"$DB_NAME\";" postgres
            if [ $? -eq 0 ]; then
                echo "Base de datos eliminada exitosamente."
            else
                echo -e "${RED}Error al eliminar la base de datos${NC}"
                exit 1
            fi
        else
            echo -e "${RED}Error: La base de datos '$DB_NAME' ya existe.${NC}"
            echo "Use --force para sobrescribir la base de datos existente."
            exit 1
        fi
    fi
    
    # Crear la base de datos
    echo "Creando base de datos '$DB_NAME'..."
    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "CREATE DATABASE \"$DB_NAME\" ENCODING 'UTF8' LC_COLLATE 'en_US.UTF-8' LC_CTYPE 'en_US.UTF-8' TEMPLATE template0;" postgres
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Error al crear la base de datos${NC}"
        exit 1
    fi
    
    # Extraer SQL a un archivo temporal
    temp_sql=$(mktemp)
    sed -n '/^__SQL_DUMP_BELOW__$/,/^__SQL_DUMP_ABOVE__$/p' "$0" | sed '1d;$d' > "$temp_sql"
    
    # Modificar el SQL si es necesario
    if [ $SKIP_DATA -eq 1 ]; then
        echo -e "${YELLOW}Omitiendo datos, solo se restaurará la estructura...${NC}"
        grep -v "^INSERT INTO" "$temp_sql" > "${temp_sql}.tmp"
        mv "${temp_sql}.tmp" "$temp_sql"
    fi
    
    if [ $SKIP_PRIVS -eq 1 ]; then
        echo -e "${YELLOW}Omitiendo privilegios...${NC}"
        grep -v "^GRANT " "$temp_sql" > "${temp_sql}.tmp"
        mv "${temp_sql}.tmp" "$temp_sql"
    fi
    
    # Restaurar la base de datos
    echo -e "${BLUE}Restaurando backup a '$DB_NAME'...${NC}"
    if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -f "$temp_sql" "$DB_NAME"; then
        echo -e "${GREEN}¡Backup restaurado exitosamente!${NC}"
    else
        echo -e "${RED}Error al restaurar el backup${NC}"
        rm -f "$temp_sql"
        exit 1
    fi
    
    # Limpiar
    rm -f "$temp_sql"
    echo -e "${GREEN}Proceso completado correctamente.${NC}"
}

# Función para mostrar la ayuda
show_usage() {
    echo -e "Uso: $0 [OPCIÓN]"
    echo ""
    echo -e "Opciones:"
    echo -e "  --info                  Mostrar información del backup"
    echo -e "  --extract [FILE]        Extraer SQL sin ejecutarlo"
    echo -e "  --restore [OPTIONS]     Restaurar el backup"
    echo -e "  --help                  Mostrar esta ayuda"
    echo ""
    echo -e "Opciones de restauración:"
    echo -e "  --db NAME               Nombre de base de datos (default: productiva)"
    echo -e "  --host HOST             Host del servidor (default: localhost)"
    echo -e "  --port PORT             Puerto del servidor (default: 5432)"
    echo -e "  --user USER             Usuario PostgreSQL (default: productiva)"
    echo -e "  --no-data               Omitir datos, restaurar solo estructura"
    echo -e "  --no-privs              Omitir privilegios"
    echo -e "  --force                 Sobrescribir base de datos si existe"
}

# Procesar argumentos
if [ $# -eq 0 ]; then
    show_info
    exit 0
fi

case "$1" in
    --info)
        show_info
        ;;
    --extract)
        shift
        extract_sql "$1"
        ;;
    --restore)
        shift
        restore_backup "$@"
        ;;
    --help)
        show_usage
        ;;
    *)
        echo -e "${RED}Opción desconocida: $1${NC}"
        show_usage
        exit 1
        ;;
esac

exit 0

# Fecha de backup: $(date '+%Y-%m-%d %H:%M:%S')
# Base de datos origen: $DB_NAME
# Servidor origen: $DB_HOST:$DB_PORT
# Versión PostgreSQL: $PG_VERSION
# Número de tablas: $TABLE_COUNT
# Registros totales: $RECORD_COUNT

__SQL_DUMP_BELOW__
HEADER

# Añadir el contenido SQL al archivo ejecutable
cat "$TEMP_SQL" >> "$BACKUP_FILE"

# Finalizar el archivo ejecutable
cat >> "$BACKUP_FILE" << 'FOOTER'
__SQL_DUMP_ABOVE__
FOOTER

# Hacer ejecutable el archivo
chmod +x "$BACKUP_FILE"

# Limpiar archivos temporales
rm -f "$TEMP_SQL"

# Restaurar .pgpass original si existía
if [ $PGPASS_EXISTS -eq 1 ]; then
  mv "${PGPASS_FILE}.bak" "$PGPASS_FILE"
else
  rm -f "$PGPASS_FILE"
fi

echo -e "${GREEN}¡Backup ejecutable creado correctamente!${NC}"
echo -e "Archivo: ${YELLOW}$BACKUP_FILE${NC}"
echo -e "Tamaño: ${YELLOW}$(du -h "$BACKUP_FILE" | cut -f1)${NC}"
echo
echo -e "Para restaurar este backup, ejecute:"
echo -e "  ${YELLOW}./$BACKUP_FILE --restore [--db DB_NAME] [--host HOST] [--user USER]${NC}"
echo
echo -e "Para ver más opciones, ejecute:"
echo -e "  ${YELLOW}./$BACKUP_FILE --help${NC}"
#!/bin/bash

# =============================================================================
# Script para crear un backup ejecutable completo de una base de datos PostgreSQL
# =============================================================================
# 
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

# Crear un archivo .pgpass temporal para no pedir contraseña varias veces
PGPASS_FILE="$HOME/.pgpass.temp"
if [ -f "$PGPASS_FILE" ]; then
  mv "$PGPASS_FILE" "${PGPASS_FILE}.bak"
fi

# Guardar credenciales en .pgpass
echo "$DB_HOST:$DB_PORT:$DB_NAME:$DB_USER:$DB_PASSWORD" > "$PGPASS_FILE"
echo "$DB_HOST:$DB_PORT:postgres:$DB_USER:$DB_PASSWORD" >> "$PGPASS_FILE"
chmod 600 "$PGPASS_FILE"

# Usar PGPASSFILE para evitar preguntar contraseña
export PGPASSFILE="$PGPASS_FILE"

echo -e "${GREEN}=== Generando backup ejecutable de PostgreSQL ===${NC}"
echo "Base de datos: $DB_NAME"
echo "Servidor: $DB_HOST:$DB_PORT"
echo "Usuario: $DB_USER"
echo "Archivo de salida: $BACKUP_FILE"

# Verificar conexión
if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1" &> /dev/null; then
  echo -e "${RED}Error: No se puede conectar a la base de datos.${NC}"
  echo "Verifique los parámetros de conexión e intente nuevamente."
  rm -f "$PGPASS_FILE"
  if [ -f "${PGPASS_FILE}.bak" ]; then
    mv "${PGPASS_FILE}.bak" "$PGPASS_FILE"
  fi
  exit 1
fi

# Obtener versión de PostgreSQL
PG_VERSION=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT version();" -t | sed 's/^[[:space:]]*//')

# Contar tablas
TABLE_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT COUNT(*) FROM pg_tables WHERE schemaname='public';" -t | sed 's/^[[:space:]]*//')

# Archivo temporal para el dump SQL
TEMP_SQL=$(mktemp)

# Generar el dump completo de la base de datos
echo "Exportando estructura y datos de la base de datos..."
pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" --no-owner --no-acl "$DB_NAME" --format=plain > "$TEMP_SQL"

# Verificar si el dump fue exitoso
if [ $? -ne 0 ]; then
  echo -e "${RED}Error al exportar la base de datos.${NC}"
  rm -f "$TEMP_SQL" "$PGPASS_FILE"
  if [ -f "${PGPASS_FILE}.bak" ]; then
    mv "${PGPASS_FILE}.bak" "$PGPASS_FILE"
  fi
  exit 1
fi

# Contar registros totales
TOTAL_RECORDS=$(grep -c "^INSERT INTO" "$TEMP_SQL")

# Crear la cabecera del script ejecutable
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
#   --user USER   Usuario PostgreSQL (predeterminado: postgres)
#   --no-data     Omitir los datos, restaurar solo la estructura
#   --no-privs    Omitir los privilegios de usuarios
#   --force       Sobrescribir la base de datos si ya existe (recomendado para actualizaciones)
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
            --host) DB_HOST="$2"; shift ;;
            --port) DB_PORT="$2"; shift ;;
            --user) DB_USER="$2"; shift ;;
            --no-data) SKIP_DATA=1 ;;
            --no-privs) SKIP_PRIVS=1 ;;
            --force) FORCE=1 ;;
            *) echo "Opción desconocida: $1"; exit 1 ;;
        esac
        shift
    done
    
    echo -e "${BLUE}================== RESTAURACIÓN ====================${NC}"
    echo -e "${GREEN}Base de datos destino:${NC} $DB_NAME"
    echo -e "${GREEN}Servidor destino:${NC} $DB_HOST:$DB_PORT"
    echo -e "${GREEN}Usuario:${NC} $DB_USER"
    echo
    
    # Verificar si psql está disponible
    if ! command -v psql &> /dev/null; then
        echo -e "${RED}Error: psql no está instalado o no está en el PATH${NC}"
        echo "Por favor, instale PostgreSQL client utilities"
        exit 1
    fi
    
    # Crear archivo .pgpass temporal para restauración (se elimina al final)
    PGPASS_FILE="$HOME/.pgpass.temp"
    if [ -f "$PGPASS_FILE" ]; then
        mv "$PGPASS_FILE" "${PGPASS_FILE}.bak"
    fi
    
    # Preguntar contraseña una sola vez
    read -s -p "Ingrese la contraseña para el usuario $DB_USER: " DB_PASSWORD
    echo ""
    
    # Crear archivo pgpass para evitar preguntar la contraseña múltiples veces
    echo "$DB_HOST:$DB_PORT:postgres:$DB_USER:$DB_PASSWORD" > "$PGPASS_FILE"
    echo "$DB_HOST:$DB_PORT:$DB_NAME:$DB_USER:$DB_PASSWORD" >> "$PGPASS_FILE"
    chmod 600 "$PGPASS_FILE"
    
    # Usar PGPASSFILE para evitar preguntar contraseña
    export PGPASSFILE="$PGPASS_FILE"
    
    # Verificar si se puede conectar al servidor
    if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "SELECT 1" postgres &> /dev/null; then
        echo -e "${RED}Error al conectar con el servidor PostgreSQL${NC}"
        echo "Verifique los parámetros de conexión y que el servidor esté en ejecución"
        # Limpiar archivos temporales
        rm -f "$PGPASS_FILE"
        if [ -f "${PGPASS_FILE}.bak" ]; then
            mv "${PGPASS_FILE}.bak" "$PGPASS_FILE"
        fi
        exit 1
    fi
    
    # Verificar si la base de datos ya existe
    if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -lqt | grep -w "$DB_NAME" &> /dev/null; then
        if [ $FORCE -eq 1 ]; then
            echo -e "${YELLOW}La base de datos '$DB_NAME' ya existe. Se eliminará...${NC}"

            # Intentar cerrar conexiones activas antes de eliminar la base de datos
            echo -e "Cerrando conexiones activas a la base de datos '$DB_NAME'..."
            psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "
                SELECT pg_terminate_backend(pg_stat_activity.pid) 
                FROM pg_stat_activity 
                WHERE pg_stat_activity.datname = '$DB_NAME'
                AND pid <> pg_backend_pid();" postgres &> /dev/null
            
            # Esperar un momento para que las conexiones se cierren
            sleep 2

            # Intentar eliminar la base de datos
            if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "DROP DATABASE IF EXISTS \"$DB_NAME\";" postgres; then
                echo -e "${RED}Error al eliminar la base de datos existente.${NC}"
                echo -e "${YELLOW}La base de datos puede estar en uso por otras aplicaciones.${NC}"
                echo -e "Sugerencias:"
                echo -e " 1. Detenga todas las aplicaciones que estén usando la base de datos"
                echo -e " 2. Intente nuevamente el comando de restauración"
                echo -e " 3. Si el problema persiste, puede crear una base de datos con otro nombre"
                exit 1
            fi
            echo -e "${GREEN}Base de datos eliminada exitosamente.${NC}"
        else
            echo -e "${RED}Error: La base de datos '$DB_NAME' ya existe.${NC}"
            echo "Use --force para sobrescribirla o elija otro nombre con --db"
            exit 1
        fi
    fi
    
    # Crear base de datos
    echo -e "${BLUE}Creando base de datos '$DB_NAME'...${NC}"
    if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "CREATE DATABASE \"$DB_NAME\" WITH ENCODING='UTF8';" postgres; then
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
    if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -f "$temp_sql" "$DB_NAME"; then
        echo -e "${GREEN}¡Backup restaurado exitosamente!${NC}"
    else
        echo -e "${RED}Error al restaurar el backup${NC}"
        rm -f "$temp_sql"
        exit 1
    fi
    
    # Actualizar secuencias si hay datos
    if [ $SKIP_DATA -eq 0 ]; then
        echo -e "${BLUE}Actualizando secuencias...${NC}"
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "
            DO \$\$
            DECLARE
                seq_record RECORD;
            BEGIN
                FOR seq_record IN 
                    SELECT
                        n.nspname AS schema_name,
                        s.relname AS seq_name,
                        t.relname AS table_name,
                        a.attname AS column_name
                    FROM
                        pg_class s
                        JOIN pg_namespace n ON s.relnamespace = n.oid
                        JOIN pg_depend d ON d.objid = s.oid
                        JOIN pg_class t ON d.refobjid = t.oid
                        JOIN pg_attribute a ON (d.refobjid, d.refobjsubid) = (a.attrelid, a.attnum)
                    WHERE
                        s.relkind = 'S'
                        AND n.nspname = 'public'
                LOOP
                    EXECUTE 'SELECT setval(''public.' || quote_ident(seq_record.seq_name) || ''', COALESCE((SELECT MAX(' || quote_ident(seq_record.column_name) || ') FROM ' || quote_ident(seq_record.table_name) || '), 1), true)';
                END LOOP;
            END \$\$;
        " "$DB_NAME" &> /dev/null
    fi
    
    # Limpiar archivos temporales
    rm -f "$temp_sql"
    rm -f "$PGPASS_FILE"
    if [ -f "${PGPASS_FILE}.bak" ]; then
        mv "${PGPASS_FILE}.bak" "$PGPASS_FILE"
    fi
    
    echo -e "${GREEN}Restauración completada exitosamente.${NC}"
    echo -e "${BLUE}=================== ESTADÍSTICAS ===================${NC}"
    echo -e "Tablas restauradas: $(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "SELECT COUNT(*) FROM pg_tables WHERE schemaname='public';" -t "$DB_NAME" | tr -d ' ')"
    
    if [ $SKIP_DATA -eq 0 ]; then
        echo -e "Información de registros por tabla:"
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "
            SELECT tablename, pg_size_pretty(pg_relation_size(quote_ident(tablename)::text)) as size,
            pg_total_relation_size(quote_ident(tablename)::text) as total_size, 
            (SELECT count(*) FROM ONLY \"\"\"||tablename||\"\"\") as records
            FROM pg_tables
            WHERE schemaname = 'public'
            ORDER BY records DESC;
        " "$DB_NAME"
    fi
}

# Ejecutar esta parte si se usa el comando --help
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    show_info
    exit 0
fi

# Ejecutar esta parte si se usa el comando --info
if [[ "$1" == "--info" ]]; then
    show_info
    exit 0
fi

# Ejecutar esta parte si se usa el comando --extract
if [[ "$1" == "--extract" ]]; then
    output_file="$2"
    extract_sql "$output_file"
    exit 0
fi

# Ejecutar esta parte si se usa el comando --restore
if [[ "$1" == "--restore" ]]; then
    shift
    restore_backup "$@"
    exit 0
fi

# Si no se especificó ningún comando, mostrar la ayuda
if [[ $# -eq 0 ]]; then
    show_info
    exit 0
fi

echo "Comando desconocido: $1"
echo "Use --help para ver las opciones disponibles."
exit 1

# Dump de la base de datos PostgreSQL comienza aquí
__SQL_DUMP_BELOW__
HEADER

# Añadir información del backup
cat >> "$BACKUP_FILE" << EOF
-- Generado por pg_dump con modificaciones para mayor compatibilidad
-- Fecha de backup: $(date '+%Y-%m-%d %H:%M:%S')
-- Base de datos origen: $DB_NAME
-- Servidor origen: $DB_HOST:$DB_PORT
-- Versión PostgreSQL: $PG_VERSION
-- Número de tablas: $TABLE_COUNT
-- Registros totales: $TOTAL_RECORDS


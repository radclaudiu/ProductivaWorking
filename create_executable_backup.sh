#!/bin/bash
# Script para generar un backup ejecutable de PostgreSQL
# Este script combina el backup de la base de datos con un script shell
# que permite restaurar la base de datos directamente

# Verificar que pg_dump esté disponible
if ! command -v pg_dump &> /dev/null; then
    echo "Error: pg_dump no está instalado o no está en el PATH"
    exit 1
fi

# Obtener fecha y hora actual para el nombre del archivo
DATE_TIME=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="productiva_backup_executable_$DATE_TIME.sh"

# Colores para mensajes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Variables de conexión (por defecto usar variables de entorno)
DB_HOST=${PGHOST:-"localhost"}
DB_PORT=${PGPORT:-"5432"}
DB_USER=${PGUSER:-"postgres"}
DB_NAME=${PGDATABASE:-"neondb"}

# Mensaje de inicio
echo -e "${GREEN}=== Generando backup ejecutable de PostgreSQL ===${NC}"
echo "Base de datos: $DB_NAME"
echo "Servidor: $DB_HOST:$DB_PORT"
echo "Usuario: $DB_USER"
echo "Archivo de salida: $BACKUP_FILE"
echo ""

# Obtener información para incluir en el backup
TABLE_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public'" | tr -d ' ')
PG_VERSION=$(psql -V | head -1)

# Crear archivo temporal para el dump SQL
TEMP_SQL=$(mktemp)

echo "Exportando estructura y datos de la base de datos..."

# Generar encabezado para el SQL
cat > "$TEMP_SQL" << 'EOL'
-- Script SQL generado mediante pg_dump con mejoras para compatibilidad

BEGIN;

-- Creación de tipos enumerados necesarios
CREATE OR REPLACE FUNCTION create_types_if_not_exist() RETURNS void AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('admin', 'gerente', 'empleado');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contract_type') THEN
        CREATE TYPE contract_type AS ENUM ('INDEFINIDO', 'TEMPORAL', 'PRACTICAS', 'FORMACION', 'OBRA');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'employee_status') THEN
        CREATE TYPE employee_status AS ENUM ('activo', 'baja_medica', 'excedencia', 'vacaciones', 'inactivo');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'week_day') THEN
        CREATE TYPE week_day AS ENUM ('lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vacation_status') THEN
        CREATE TYPE vacation_status AS ENUM ('REGISTRADA', 'DISFRUTADA');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_status') THEN
        CREATE TYPE checkpoint_status AS ENUM ('active', 'disabled', 'maintenance');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_incident_type') THEN
        CREATE TYPE checkpoint_incident_type AS ENUM ('missed_checkout', 'late_checkin', 'early_checkout', 'overtime', 'manual_adjustment', 'contract_hours_adjustment');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_priority') THEN
        CREATE TYPE task_priority AS ENUM ('alta', 'media', 'baja');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_frequency') THEN
        CREATE TYPE task_frequency AS ENUM ('diaria', 'semanal', 'mensual', 'unica');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_status') THEN
        CREATE TYPE task_status AS ENUM ('pendiente', 'completada', 'cancelada');
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Ejecutar la función
SELECT create_types_if_not_exist();

-- Eliminar la función temporal
DROP FUNCTION IF EXISTS create_types_if_not_exist();

EOL

# Añadir información de fecha y origen justo después
cat >> "$TEMP_SQL" << EOL
-- Fecha: $(date '+%Y-%m-%d %H:%M:%S')
-- Base de datos origen: $DB_NAME
-- Servidor origen: $DB_HOST:$DB_PORT

EOL

# Generar dump de la estructura de la base de datos (sin los tipos enumerados que ya se definieron arriba)
pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" --no-owner --no-acl --schema-only "$DB_NAME" | \
  grep -v "CREATE TYPE" | \
  grep -v "ALTER TYPE" | \
  sed 's/USER-DEFINED/VARCHAR(100)/g' >> "$TEMP_SQL"

# Agregar datos de las tablas en orden específico para respetar dependencias
TABLES=(
  "users"
  "companies" 
  "locations"
  "user_companies"
  "employees"
  "checkpoints"
  "local_users"
  "task_groups"
  "products"
  "employee_documents"
  "employee_notes"
  "employee_history"
  "employee_schedules"
  "employee_check_ins"
  "employee_vacations"
  "checkpoint_records"
  "checkpoint_incidents"
  "checkpoint_original_records"
  "employee_contract_hours"
  "tasks"
  "task_weekdays"
  "task_schedules"
  "task_instances"
  "task_completions"
  "product_conservations"
  "product_labels"
  "label_templates"
  "activity_logs"
)

echo "Exportando datos de las tablas en orden controlado..."

# Obtener lista de todas las tablas para procesar las que no están en la lista explícita
ALL_TABLES=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT tablename FROM pg_tables WHERE schemaname='public'" | tr -d ' ')

# Archivo temporal para los datos
DATA_TEMP=$(mktemp)
PROCESSED_SQL=$(mktemp)

echo "" > "$PROCESSED_SQL"

echo "-- Creando tipo de datos enumerados personalizados" >> "$PROCESSED_SQL"
cat >> "$PROCESSED_SQL" << 'EOL'
DO $$
DECLARE
BEGIN
    -- Verificar y crear tipos ENUM si no existen
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('admin', 'gerente', 'empleado');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contract_type') THEN
        CREATE TYPE contract_type AS ENUM ('INDEFINIDO', 'TEMPORAL', 'PRACTICAS', 'FORMACION', 'OBRA');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'employee_status') THEN
        CREATE TYPE employee_status AS ENUM ('activo', 'baja_medica', 'excedencia', 'vacaciones', 'inactivo');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'week_day') THEN
        CREATE TYPE week_day AS ENUM ('lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vacation_status') THEN
        CREATE TYPE vacation_status AS ENUM ('REGISTRADA', 'DISFRUTADA');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_status') THEN
        CREATE TYPE checkpoint_status AS ENUM ('active', 'disabled', 'maintenance');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_incident_type') THEN
        CREATE TYPE checkpoint_incident_type AS ENUM ('missed_checkout', 'late_checkin', 'early_checkout', 'overtime', 'manual_adjustment', 'contract_hours_adjustment');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_priority') THEN
        CREATE TYPE task_priority AS ENUM ('alta', 'media', 'baja');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_frequency') THEN
        CREATE TYPE task_frequency AS ENUM ('diaria', 'semanal', 'mensual', 'unica');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_status') THEN
        CREATE TYPE task_status AS ENUM ('pendiente', 'completada', 'cancelada');
    END IF;
END;
$$;
EOL

# Configuración para la conexión sin pedir contraseña varias veces
# Crear archivo .pgpass temporal (se elimina al final)
PGPASS_FILE="$HOME/.pgpass.temp"
echo "$DB_HOST:$DB_PORT:$DB_NAME:$DB_USER:$DB_PASSWORD" > "$PGPASS_FILE"
chmod 600 "$PGPASS_FILE"

# Variable de entorno PGPASSFILE para usar nuestro archivo temporal
export PGPASSFILE="$PGPASS_FILE"

# Generar estructura de tablas (sin los tipos enumerados que ya se definieron arriba)
echo "-- Generando estructura de tablas" >> "$PROCESSED_SQL"
pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" --no-owner --no-acl --schema-only "$DB_NAME" | \
  grep -v "CREATE TYPE" | \
  grep -v "ALTER TYPE" | \
  grep -v "^--" | \
  grep -v "^SET " | \
  grep -v "SELECT pg_catalog" >> "$PROCESSED_SQL"

# Exportar las tablas de un modo más fiable
echo " - Exportando datos de todas las tablas..."

# Función para exportar tabla con formato COPY
export_table_to_sql() {
  local TABLE=$1
  local OUTPUT_FILE=$2
  
  echo " - Exportando tabla $TABLE"
  
  # Añadir un encabezado para esta tabla
  echo "-- Datos para tabla $TABLE" >> "$OUTPUT_FILE"
  
  # Usar pg_dump con formato COPY
  pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" --no-owner --no-acl --data-only --table="$TABLE" "$DB_NAME" --format=plain >> "$OUTPUT_FILE"
  
  # Añadir un separador
  echo "" >> "$OUTPUT_FILE"
}

# Primero las tablas en orden específico
for TABLE in ${TABLES[@]}; do
  export_table_to_sql "$TABLE" "$PROCESSED_SQL" 
done

# Ahora las tablas adicionales
for TABLE in $ALL_TABLES; do
  if [[ ! " ${TABLES[@]} " =~ " $TABLE " ]]; then
    export_table_to_sql "$TABLE" "$PROCESSED_SQL"
  fi
done

# Eliminar el archivo .pgpass temporal
rm -f "$PGPASS_FILE"

# Agregar secuencias
echo "-- Actualizar secuencias" >> "$PROCESSED_SQL"
cat >> "$PROCESSED_SQL" << 'EOL'
DO $$
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
END;
$$;
EOL

# Mover el SQL procesado al archivo temporal final
cat "$PROCESSED_SQL" > "$TEMP_SQL"

# Limpiar archivos temporales
rm -f "$DATA_TEMP" "$PROCESSED_SQL"

# Crear el script ejecutable
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
# Cuando quieras actualizar la base de datos de desarrollo con #nuevos datos de producción:
./backup_produccion_fecha.sh --restore --db nombre_bd_dev --force
============================================================================
#Cuando quieras actualizar la base de datos de desarrollo con nuevos #datos de producción:
#./backup_produccion_fecha.sh --restore --db nombre_bd_dev --force
#Esta aproximación:

#Es más confiable (evita problemas de sincronización parcial)
#Es más rápida de implementar
#Evita inconsistencias en los datos
#Garantiza que la base de datos de desarrollo sea una copia exacta #de producción
#Para tu caso específico, cuando necesites actualizar una base de #datos DEV que ya tiene el 99% de los datos de ayer con nuevos datos #de producción, simplemente usarías:

#./productiva_backup_executable_YYYYMMDD.sh --restore --db nombre_base_dev --force
#El parámetro --force hará que se elimine la base de datos existente y se cree una nueva con los datos actualizados, garantizando que tengas una copia exacta de producción.

#¿Te gustaría que mejore algo más en el script o está bien así?
#
#Claro, entiendo. Tienes razón que a veces la opción más simple es #simplemente borr#ar y volver a insertar todo, especialmente #considerando que# las opciones de actualización incremental pueden #ser muy compleja#s de implementar correctamente y podrían causar #inconsistencias.#
#
#Vamos a mantener la funcionalidad actual que tienes con --force que #elimina la base de datos existente y crea una nueva. Esta es #realmente la forma más segura y fiable de hacerlo:
#=====================================================================#=======

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
-- Generado por pg_dump con modificaciones para mayor compatibilidad
-- Fecha de backup: $(date '+%Y-%m-%d %H:%M:%S')
-- Base de datos origen: $DB_NAME
-- Servidor origen: $DB_HOST:$DB_PORT
-- Versión PostgreSQL: $PG_VERSION
-- Número de tablas: $TABLE_COUNT

BEGIN;

-- Creación de tipos enumerados necesarios
CREATE OR REPLACE FUNCTION create_types_if_not_exist() RETURNS void AS $create_types$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('admin', 'gerente', 'empleado');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contract_type') THEN
        CREATE TYPE contract_type AS ENUM ('INDEFINIDO', 'TEMPORAL', 'PRACTICAS', 'FORMACION', 'OBRA');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'employee_status') THEN
        CREATE TYPE employee_status AS ENUM ('activo', 'baja_medica', 'excedencia', 'vacaciones', 'inactivo');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'week_day') THEN
        CREATE TYPE week_day AS ENUM ('lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vacation_status') THEN
        CREATE TYPE vacation_status AS ENUM ('REGISTRADA', 'DISFRUTADA');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_status') THEN
        CREATE TYPE checkpoint_status AS ENUM ('active', 'disabled', 'maintenance');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_incident_type') THEN
        CREATE TYPE checkpoint_incident_type AS ENUM ('missed_checkout', 'late_checkin', 'early_checkout', 'overtime', 'manual_adjustment', 'contract_hours_adjustment');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_priority') THEN
        CREATE TYPE task_priority AS ENUM ('alta', 'media', 'baja');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_frequency') THEN
        CREATE TYPE task_frequency AS ENUM ('diaria', 'semanal', 'mensual', 'unica');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_status') THEN
        CREATE TYPE task_status AS ENUM ('pendiente', 'completada', 'cancelada');
    END IF;
END;
$create_types$ LANGUAGE plpgsql;

-- Ejecutar la función
SELECT create_types_if_not_exist();

-- Eliminar la función temporal
DROP FUNCTION IF EXISTS create_types_if_not_exist();

HEADER

# Añadir el contenido SQL al archivo ejecutable
cat "$TEMP_SQL" >> "$BACKUP_FILE"

# Finalizar el archivo ejecutable
cat >> "$BACKUP_FILE" << 'FOOTER'

COMMIT;
__SQL_DUMP_ABOVE__
FOOTER

# Hacer el archivo ejecutable
chmod +x "$BACKUP_FILE"

# Eliminar archivo temporal
rm -f "$TEMP_SQL"

# Mostrar información del backup creado
echo -e "${GREEN}Backup ejecutable creado exitosamente: ${BACKUP_FILE}${NC}"
echo "Tamaño del archivo: $(du -h "$BACKUP_FILE" | cut -f1)"
echo ""
echo -e "${YELLOW}Para restaurar este backup, use:${NC}"
echo "  ./$BACKUP_FILE --restore [--db NOMBRE_BD] [--host HOST] [--user USUARIO]"
echo ""
echo -e "${YELLOW}Para ver información del backup:${NC}"
echo "  ./$BACKUP_FILE --info"
echo ""
echo -e "${YELLOW}Para extraer el SQL sin ejecutarlo:${NC}"
echo "  ./$BACKUP_FILE --extract [archivo_salida.sql]"
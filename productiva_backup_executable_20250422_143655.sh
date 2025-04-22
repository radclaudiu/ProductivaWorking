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


-- Creando tipo de datos enumerados personalizados
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
-- Generando estructura de tablas




    'missed_checkout',
    'late_checkin',
    'early_checkout',
    'overtime',
    'manual_adjustment',
    'contract_hours_adjustment'
);



    'active',
    'disabled',
    'maintenance'
);



    'MISSED_CHECKOUT',
    'LATE_CHECKIN',
    'EARLY_CHECKOUT',
    'OVERTIME',
    'MANUAL_ADJUSTMENT',
    'CONTRACT_HOURS_ADJUSTMENT'
);



    'ACTIVE',
    'DISABLED',
    'MAINTENANCE'
);



    'DESCONGELACION',
    'REFRIGERACION',
    'GASTRO',
    'CALIENTE',
    'SECO'
);



    'INDEFINIDO',
    'TEMPORAL',
    'PRACTICAS',
    'FORMACION',
    'OBRA'
);



    'INDEFINIDO',
    'TEMPORAL',
    'PRACTICAS',
    'FORMACION',
    'OBRA'
);



    'activo',
    'baja_medica',
    'excedencia',
    'vacaciones',
    'inactivo'
);



    'ACTIVO',
    'BAJA_MEDICA',
    'EXCEDENCIA',
    'VACACIONES',
    'INACTIVO'
);



    'diaria',
    'semanal',
    'mensual',
    'unica'
);



    'alta',
    'media',
    'baja'
);



    'pendiente',
    'completada',
    'cancelada'
);



    'DIARIA',
    'SEMANAL',
    'QUINCENAL',
    'MENSUAL',
    'PERSONALIZADA'
);



    'BAJA',
    'MEDIA',
    'ALTA',
    'URGENTE'
);



    'PENDIENTE',
    'COMPLETADA',
    'VENCIDA',
    'CANCELADA'
);



    'admin',
    'gerente',
    'empleado'
);



    'ADMIN',
    'GERENTE',
    'EMPLEADO'
);



    'REGISTRADA',
    'DISFRUTADA'
);



    'REGISTRADA',
    'DISFRUTADA'
);



    'lunes',
    'martes',
    'miercoles',
    'jueves',
    'viernes',
    'sabado',
    'domingo'
);



    'LUNES',
    'MARTES',
    'MIERCOLES',
    'JUEVES',
    'VIERNES',
    'SABADO',
    'DOMINGO'
);





CREATE TABLE public.activity_logs (
    id integer NOT NULL,
    action character varying(256) NOT NULL,
    ip_address character varying(64),
    "timestamp" timestamp without time zone,
    user_id integer
);



CREATE SEQUENCE public.activity_logs_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.activity_logs_id_seq OWNED BY public.activity_logs.id;



CREATE TABLE public.checkpoint_incidents (
    id integer NOT NULL,
    incident_type public.checkpointincidenttype,
    description text,
    created_at timestamp without time zone,
    resolved boolean,
    resolved_at timestamp without time zone,
    resolution_notes text,
    record_id integer NOT NULL,
    resolved_by_id integer
);



CREATE SEQUENCE public.checkpoint_incidents_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.checkpoint_incidents_id_seq OWNED BY public.checkpoint_incidents.id;



CREATE TABLE public.checkpoint_original_records (
    id integer NOT NULL,
    record_id integer NOT NULL,
    original_check_in_time timestamp without time zone NOT NULL,
    original_check_out_time timestamp without time zone,
    original_signature_data text,
    original_has_signature boolean,
    original_notes text,
    adjusted_at timestamp without time zone,
    adjusted_by_id integer,
    adjustment_reason character varying(256),
    created_at timestamp without time zone
);



CREATE SEQUENCE public.checkpoint_original_records_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.checkpoint_original_records_id_seq OWNED BY public.checkpoint_original_records.id;



CREATE TABLE public.checkpoint_records (
    id integer NOT NULL,
    check_in_time timestamp without time zone NOT NULL,
    check_out_time timestamp without time zone,
    original_check_in_time timestamp without time zone,
    original_check_out_time timestamp without time zone,
    adjusted boolean,
    adjustment_reason character varying(256),
    notes text,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    employee_id integer NOT NULL,
    checkpoint_id integer NOT NULL,
    signature_data text,
    has_signature boolean
);



CREATE SEQUENCE public.checkpoint_records_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.checkpoint_records_id_seq OWNED BY public.checkpoint_records.id;



CREATE TABLE public.checkpoints (
    id integer NOT NULL,
    name character varying(128) NOT NULL,
    description text,
    location character varying(256),
    status public.checkpointstatus,
    username character varying(64) NOT NULL,
    password_hash character varying(256) NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    company_id integer NOT NULL,
    enforce_contract_hours boolean,
    auto_adjust_overtime boolean,
    operation_start_time time without time zone,
    operation_end_time time without time zone,
    enforce_operation_hours boolean
);



CREATE SEQUENCE public.checkpoints_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.checkpoints_id_seq OWNED BY public.checkpoints.id;



CREATE TABLE public.companies (
    id integer NOT NULL,
    name character varying(128) NOT NULL,
    address character varying(256),
    city character varying(64),
    postal_code character varying(16),
    country character varying(64),
    sector character varying(64),
    tax_id character varying(32),
    phone character varying(13),
    email character varying(120),
    website character varying(128),
    bank_account character varying(24),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_active boolean DEFAULT true
);



CREATE SEQUENCE public.companies_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.companies_id_seq OWNED BY public.companies.id;



CREATE TABLE public.employee_check_ins (
    id integer NOT NULL,
    employee_id integer NOT NULL,
    check_in_time timestamp without time zone NOT NULL,
    check_out_time timestamp without time zone,
    is_generated boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    notes text
);



CREATE SEQUENCE public.employee_check_ins_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.employee_check_ins_id_seq OWNED BY public.employee_check_ins.id;



CREATE TABLE public.employee_contract_hours (
    id integer NOT NULL,
    daily_hours double precision,
    weekly_hours double precision,
    allow_overtime boolean,
    max_overtime_daily double precision,
    use_normal_schedule boolean,
    normal_start_time time without time zone,
    normal_end_time time without time zone,
    use_flexibility boolean,
    checkin_flexibility integer,
    checkout_flexibility integer,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    employee_id integer NOT NULL
);



CREATE SEQUENCE public.employee_contract_hours_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.employee_contract_hours_id_seq OWNED BY public.employee_contract_hours.id;



CREATE TABLE public.employee_documents (
    id integer NOT NULL,
    filename character varying(256) NOT NULL,
    original_filename character varying(256) NOT NULL,
    file_path character varying(512) NOT NULL,
    file_type character varying(64),
    file_size integer,
    description character varying(256),
    uploaded_at timestamp without time zone,
    employee_id integer NOT NULL
);



CREATE SEQUENCE public.employee_documents_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.employee_documents_id_seq OWNED BY public.employee_documents.id;



CREATE TABLE public.employee_history (
    id integer NOT NULL,
    field_name character varying(64) NOT NULL,
    old_value character varying(256),
    new_value character varying(256),
    changed_at timestamp without time zone,
    employee_id integer NOT NULL,
    changed_by_id integer
);



CREATE SEQUENCE public.employee_history_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.employee_history_id_seq OWNED BY public.employee_history.id;



CREATE TABLE public.employee_notes (
    id integer NOT NULL,
    content text NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    employee_id integer NOT NULL,
    created_by_id integer
);



CREATE SEQUENCE public.employee_notes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.employee_notes_id_seq OWNED BY public.employee_notes.id;



CREATE TABLE public.employee_schedules (
    id integer NOT NULL,
    employee_id integer NOT NULL,
    day_of_week character varying(20) NOT NULL,
    start_time time without time zone NOT NULL,
    end_time time without time zone NOT NULL,
    is_working_day boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);



CREATE SEQUENCE public.employee_schedules_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.employee_schedules_id_seq OWNED BY public.employee_schedules.id;



CREATE TABLE public.employee_vacations (
    id integer NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    status public.vacationstatus,
    is_signed boolean,
    is_enjoyed boolean,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    notes text,
    employee_id integer NOT NULL
);



CREATE SEQUENCE public.employee_vacations_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.employee_vacations_id_seq OWNED BY public.employee_vacations.id;



CREATE TABLE public.employees (
    id integer NOT NULL,
    first_name character varying(64) NOT NULL,
    last_name character varying(64) NOT NULL,
    dni character varying(16) NOT NULL,
    social_security_number character varying(20),
    email character varying(120),
    address character varying(200),
    phone character varying(20),
    "position" character varying(64),
    contract_type character varying(20) DEFAULT 'INDEFINIDO'::character varying,
    bank_account character varying(64),
    start_date character varying(20),
    end_date character varying(20),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_active boolean DEFAULT true,
    status character varying(20) DEFAULT 'activo'::character varying,
    company_id integer NOT NULL,
    user_id integer,
    is_on_shift boolean DEFAULT false,
    status_start_date character varying(20),
    status_end_date character varying(20),
    status_notes text
);



CREATE SEQUENCE public.employees_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.employees_id_seq OWNED BY public.employees.id;



CREATE TABLE public.label_templates (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    is_default boolean,
    titulo_x integer,
    titulo_y integer,
    titulo_size integer,
    titulo_bold boolean,
    conservacion_x integer,
    conservacion_y integer,
    conservacion_size integer,
    conservacion_bold boolean,
    preparador_x integer,
    preparador_y integer,
    preparador_size integer,
    preparador_bold boolean,
    fecha_x integer,
    fecha_y integer,
    fecha_size integer,
    fecha_bold boolean,
    caducidad_x integer,
    caducidad_y integer,
    caducidad_size integer,
    caducidad_bold boolean,
    caducidad2_x integer,
    caducidad2_y integer,
    caducidad2_size integer,
    caducidad2_bold boolean,
    location_id integer NOT NULL
);



CREATE SEQUENCE public.label_templates_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.label_templates_id_seq OWNED BY public.label_templates.id;



CREATE TABLE public.local_users (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    last_name character varying(64) NOT NULL,
    username character varying(128) NOT NULL,
    pin character varying(256) NOT NULL,
    photo_path character varying(256),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    is_active boolean,
    location_id integer NOT NULL
);



CREATE SEQUENCE public.local_users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.local_users_id_seq OWNED BY public.local_users.id;



CREATE TABLE public.locations (
    id integer NOT NULL,
    name character varying(128) NOT NULL,
    address character varying(256),
    city character varying(64),
    postal_code character varying(16),
    description text,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    is_active boolean,
    portal_username character varying(64),
    portal_password_hash character varying(256),
    company_id integer NOT NULL
);



CREATE SEQUENCE public.locations_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.locations_id_seq OWNED BY public.locations.id;



CREATE TABLE public.product_conservations (
    id integer NOT NULL,
    conservation_type public.conservationtype NOT NULL,
    hours_valid integer NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    product_id integer NOT NULL
);



CREATE SEQUENCE public.product_conservations_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.product_conservations_id_seq OWNED BY public.product_conservations.id;



CREATE TABLE public.product_labels (
    id integer NOT NULL,
    created_at timestamp without time zone,
    expiry_date date NOT NULL,
    product_id integer NOT NULL,
    local_user_id integer NOT NULL,
    conservation_type public.conservationtype NOT NULL
);



CREATE SEQUENCE public.product_labels_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.product_labels_id_seq OWNED BY public.product_labels.id;



CREATE TABLE public.products (
    id integer NOT NULL,
    name character varying(128) NOT NULL,
    description text,
    shelf_life_days integer NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    is_active boolean,
    location_id integer NOT NULL
);



CREATE SEQUENCE public.products_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.products_id_seq OWNED BY public.products.id;



CREATE TABLE public.task_completions (
    id integer NOT NULL,
    completion_date timestamp without time zone,
    notes text,
    task_id integer NOT NULL,
    local_user_id integer NOT NULL
);



CREATE SEQUENCE public.task_completions_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.task_completions_id_seq OWNED BY public.task_completions.id;



CREATE TABLE public.task_groups (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    description text,
    color character varying(7),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    location_id integer NOT NULL
);



CREATE SEQUENCE public.task_groups_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.task_groups_id_seq OWNED BY public.task_groups.id;



CREATE TABLE public.task_instances (
    id integer NOT NULL,
    scheduled_date date NOT NULL,
    status public.taskstatus,
    notes text,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    task_id integer NOT NULL,
    completed_by_id integer
);



CREATE SEQUENCE public.task_instances_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.task_instances_id_seq OWNED BY public.task_instances.id;



CREATE TABLE public.task_schedules (
    id integer NOT NULL,
    day_of_week public.weekday,
    day_of_month integer,
    start_time time without time zone,
    end_time time without time zone,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    task_id integer NOT NULL
);



CREATE SEQUENCE public.task_schedules_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.task_schedules_id_seq OWNED BY public.task_schedules.id;



CREATE TABLE public.task_weekdays (
    id integer NOT NULL,
    day_of_week public.weekday NOT NULL,
    task_id integer NOT NULL
);



CREATE SEQUENCE public.task_weekdays_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.task_weekdays_id_seq OWNED BY public.task_weekdays.id;



CREATE TABLE public.tasks (
    id integer NOT NULL,
    title character varying(128) NOT NULL,
    description text,
    priority public.taskpriority,
    frequency public.taskfrequency,
    status public.taskstatus,
    start_date date NOT NULL,
    end_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    location_id integer NOT NULL,
    created_by_id integer,
    group_id integer
);



CREATE SEQUENCE public.tasks_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.tasks_id_seq OWNED BY public.tasks.id;



CREATE TABLE public.user_companies (
    user_id integer NOT NULL,
    company_id integer NOT NULL
);



CREATE TABLE public.users (
    id integer NOT NULL,
    username character varying(64) NOT NULL,
    email character varying(120) NOT NULL,
    password_hash character varying(256) NOT NULL,
    role character varying(20) DEFAULT 'empleado'::character varying NOT NULL,
    first_name character varying(64),
    last_name character varying(64),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_active boolean DEFAULT true
);



CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;



ALTER TABLE ONLY public.activity_logs ALTER COLUMN id SET DEFAULT nextval('public.activity_logs_id_seq'::regclass);



ALTER TABLE ONLY public.checkpoint_incidents ALTER COLUMN id SET DEFAULT nextval('public.checkpoint_incidents_id_seq'::regclass);



ALTER TABLE ONLY public.checkpoint_original_records ALTER COLUMN id SET DEFAULT nextval('public.checkpoint_original_records_id_seq'::regclass);



ALTER TABLE ONLY public.checkpoint_records ALTER COLUMN id SET DEFAULT nextval('public.checkpoint_records_id_seq'::regclass);



ALTER TABLE ONLY public.checkpoints ALTER COLUMN id SET DEFAULT nextval('public.checkpoints_id_seq'::regclass);



ALTER TABLE ONLY public.companies ALTER COLUMN id SET DEFAULT nextval('public.companies_id_seq'::regclass);



ALTER TABLE ONLY public.employee_check_ins ALTER COLUMN id SET DEFAULT nextval('public.employee_check_ins_id_seq'::regclass);



ALTER TABLE ONLY public.employee_contract_hours ALTER COLUMN id SET DEFAULT nextval('public.employee_contract_hours_id_seq'::regclass);



ALTER TABLE ONLY public.employee_documents ALTER COLUMN id SET DEFAULT nextval('public.employee_documents_id_seq'::regclass);



ALTER TABLE ONLY public.employee_history ALTER COLUMN id SET DEFAULT nextval('public.employee_history_id_seq'::regclass);



ALTER TABLE ONLY public.employee_notes ALTER COLUMN id SET DEFAULT nextval('public.employee_notes_id_seq'::regclass);



ALTER TABLE ONLY public.employee_schedules ALTER COLUMN id SET DEFAULT nextval('public.employee_schedules_id_seq'::regclass);



ALTER TABLE ONLY public.employee_vacations ALTER COLUMN id SET DEFAULT nextval('public.employee_vacations_id_seq'::regclass);



ALTER TABLE ONLY public.employees ALTER COLUMN id SET DEFAULT nextval('public.employees_id_seq'::regclass);



ALTER TABLE ONLY public.label_templates ALTER COLUMN id SET DEFAULT nextval('public.label_templates_id_seq'::regclass);



ALTER TABLE ONLY public.local_users ALTER COLUMN id SET DEFAULT nextval('public.local_users_id_seq'::regclass);



ALTER TABLE ONLY public.locations ALTER COLUMN id SET DEFAULT nextval('public.locations_id_seq'::regclass);



ALTER TABLE ONLY public.product_conservations ALTER COLUMN id SET DEFAULT nextval('public.product_conservations_id_seq'::regclass);



ALTER TABLE ONLY public.product_labels ALTER COLUMN id SET DEFAULT nextval('public.product_labels_id_seq'::regclass);



ALTER TABLE ONLY public.products ALTER COLUMN id SET DEFAULT nextval('public.products_id_seq'::regclass);



ALTER TABLE ONLY public.task_completions ALTER COLUMN id SET DEFAULT nextval('public.task_completions_id_seq'::regclass);



ALTER TABLE ONLY public.task_groups ALTER COLUMN id SET DEFAULT nextval('public.task_groups_id_seq'::regclass);



ALTER TABLE ONLY public.task_instances ALTER COLUMN id SET DEFAULT nextval('public.task_instances_id_seq'::regclass);



ALTER TABLE ONLY public.task_schedules ALTER COLUMN id SET DEFAULT nextval('public.task_schedules_id_seq'::regclass);



ALTER TABLE ONLY public.task_weekdays ALTER COLUMN id SET DEFAULT nextval('public.task_weekdays_id_seq'::regclass);



ALTER TABLE ONLY public.tasks ALTER COLUMN id SET DEFAULT nextval('public.tasks_id_seq'::regclass);



ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);



ALTER TABLE ONLY public.activity_logs
    ADD CONSTRAINT activity_logs_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.checkpoint_incidents
    ADD CONSTRAINT checkpoint_incidents_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.checkpoint_original_records
    ADD CONSTRAINT checkpoint_original_records_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.checkpoint_records
    ADD CONSTRAINT checkpoint_records_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.checkpoints
    ADD CONSTRAINT checkpoints_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.checkpoints
    ADD CONSTRAINT checkpoints_username_key UNIQUE (username);



ALTER TABLE ONLY public.companies
    ADD CONSTRAINT companies_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.companies
    ADD CONSTRAINT companies_tax_id_key UNIQUE (tax_id);



ALTER TABLE ONLY public.employee_check_ins
    ADD CONSTRAINT employee_check_ins_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.employee_contract_hours
    ADD CONSTRAINT employee_contract_hours_employee_id_key UNIQUE (employee_id);



ALTER TABLE ONLY public.employee_contract_hours
    ADD CONSTRAINT employee_contract_hours_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.employee_documents
    ADD CONSTRAINT employee_documents_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.employee_history
    ADD CONSTRAINT employee_history_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.employee_notes
    ADD CONSTRAINT employee_notes_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.employee_schedules
    ADD CONSTRAINT employee_schedules_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.employee_vacations
    ADD CONSTRAINT employee_vacations_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_dni_key UNIQUE (dni);



ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_user_id_key UNIQUE (user_id);



ALTER TABLE ONLY public.label_templates
    ADD CONSTRAINT label_templates_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.local_users
    ADD CONSTRAINT local_users_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_portal_username_key UNIQUE (portal_username);



ALTER TABLE ONLY public.product_conservations
    ADD CONSTRAINT product_conservations_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.product_labels
    ADD CONSTRAINT product_labels_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.task_completions
    ADD CONSTRAINT task_completions_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.task_groups
    ADD CONSTRAINT task_groups_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.task_instances
    ADD CONSTRAINT task_instances_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.task_schedules
    ADD CONSTRAINT task_schedules_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.task_weekdays
    ADD CONSTRAINT task_weekdays_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.user_companies
    ADD CONSTRAINT user_companies_pkey PRIMARY KEY (user_id, company_id);



ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);



ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);



CREATE INDEX idx_checkpoint_records_checkpoint ON public.checkpoint_records USING btree (checkpoint_id);



CREATE INDEX idx_checkpoint_records_dates ON public.checkpoint_records USING btree (check_in_time, check_out_time);



CREATE INDEX idx_checkpoint_records_employee ON public.checkpoint_records USING btree (employee_id);



CREATE INDEX idx_employee_checkins_employee ON public.employee_check_ins USING btree (employee_id);



CREATE INDEX idx_employee_schedules_employee ON public.employee_schedules USING btree (employee_id);



CREATE INDEX idx_employees_company ON public.employees USING btree (company_id);



CREATE INDEX idx_employees_user ON public.employees USING btree (user_id);



CREATE INDEX idx_products_location ON public.products USING btree (location_id);



CREATE INDEX idx_task_instances_status ON public.task_instances USING btree (status);



CREATE INDEX idx_task_instances_task ON public.task_instances USING btree (task_id);



CREATE INDEX idx_tasks_group ON public.tasks USING btree (group_id);



CREATE INDEX idx_tasks_location ON public.tasks USING btree (location_id);



ALTER TABLE ONLY public.activity_logs
    ADD CONSTRAINT activity_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);



ALTER TABLE ONLY public.checkpoint_incidents
    ADD CONSTRAINT checkpoint_incidents_record_id_fkey FOREIGN KEY (record_id) REFERENCES public.checkpoint_records(id);



ALTER TABLE ONLY public.checkpoint_incidents
    ADD CONSTRAINT checkpoint_incidents_resolved_by_id_fkey FOREIGN KEY (resolved_by_id) REFERENCES public.users(id);



ALTER TABLE ONLY public.checkpoint_original_records
    ADD CONSTRAINT checkpoint_original_records_adjusted_by_id_fkey FOREIGN KEY (adjusted_by_id) REFERENCES public.users(id);



ALTER TABLE ONLY public.checkpoint_original_records
    ADD CONSTRAINT checkpoint_original_records_record_id_fkey FOREIGN KEY (record_id) REFERENCES public.checkpoint_records(id);



ALTER TABLE ONLY public.checkpoint_records
    ADD CONSTRAINT checkpoint_records_checkpoint_id_fkey FOREIGN KEY (checkpoint_id) REFERENCES public.checkpoints(id);



ALTER TABLE ONLY public.checkpoint_records
    ADD CONSTRAINT checkpoint_records_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);



ALTER TABLE ONLY public.checkpoints
    ADD CONSTRAINT checkpoints_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);



ALTER TABLE ONLY public.employee_check_ins
    ADD CONSTRAINT employee_check_ins_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);



ALTER TABLE ONLY public.employee_contract_hours
    ADD CONSTRAINT employee_contract_hours_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);



ALTER TABLE ONLY public.employee_documents
    ADD CONSTRAINT employee_documents_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);



ALTER TABLE ONLY public.employee_history
    ADD CONSTRAINT employee_history_changed_by_id_fkey FOREIGN KEY (changed_by_id) REFERENCES public.users(id);



ALTER TABLE ONLY public.employee_history
    ADD CONSTRAINT employee_history_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);



ALTER TABLE ONLY public.employee_notes
    ADD CONSTRAINT employee_notes_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.users(id);



ALTER TABLE ONLY public.employee_notes
    ADD CONSTRAINT employee_notes_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);



ALTER TABLE ONLY public.employee_schedules
    ADD CONSTRAINT employee_schedules_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);



ALTER TABLE ONLY public.employee_vacations
    ADD CONSTRAINT employee_vacations_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);



ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);



ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);



ALTER TABLE ONLY public.label_templates
    ADD CONSTRAINT label_templates_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);



ALTER TABLE ONLY public.local_users
    ADD CONSTRAINT local_users_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);



ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);



ALTER TABLE ONLY public.product_conservations
    ADD CONSTRAINT product_conservations_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);



ALTER TABLE ONLY public.product_labels
    ADD CONSTRAINT product_labels_local_user_id_fkey FOREIGN KEY (local_user_id) REFERENCES public.local_users(id);



ALTER TABLE ONLY public.product_labels
    ADD CONSTRAINT product_labels_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);



ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);



ALTER TABLE ONLY public.task_completions
    ADD CONSTRAINT task_completions_local_user_id_fkey FOREIGN KEY (local_user_id) REFERENCES public.local_users(id);



ALTER TABLE ONLY public.task_completions
    ADD CONSTRAINT task_completions_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);



ALTER TABLE ONLY public.task_groups
    ADD CONSTRAINT task_groups_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);



ALTER TABLE ONLY public.task_instances
    ADD CONSTRAINT task_instances_completed_by_id_fkey FOREIGN KEY (completed_by_id) REFERENCES public.local_users(id);



ALTER TABLE ONLY public.task_instances
    ADD CONSTRAINT task_instances_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);



ALTER TABLE ONLY public.task_schedules
    ADD CONSTRAINT task_schedules_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);



ALTER TABLE ONLY public.task_weekdays
    ADD CONSTRAINT task_weekdays_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);



ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.users(id);



ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.task_groups(id);



ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);



ALTER TABLE ONLY public.user_companies
    ADD CONSTRAINT user_companies_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);



ALTER TABLE ONLY public.user_companies
    ADD CONSTRAINT user_companies_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);



-- Datos para tabla users
COPY users FROM stdin;
\.

-- Datos para tabla companies
COPY companies FROM stdin;
\.

-- Datos para tabla locations
COPY locations FROM stdin;
\.

-- Datos para tabla user_companies
COPY user_companies FROM stdin;
\.

-- Datos para tabla employees
COPY employees FROM stdin;
\.

-- Datos para tabla checkpoints
COPY checkpoints FROM stdin;
\.

-- Datos para tabla local_users
COPY local_users FROM stdin;
\.

-- Datos para tabla task_groups
COPY task_groups FROM stdin;
\.

-- Datos para tabla products
COPY products FROM stdin;
\.

-- Datos para tabla employee_documents
COPY employee_documents FROM stdin;
\.

-- Datos para tabla employee_notes
COPY employee_notes FROM stdin;
\.

-- Datos para tabla employee_history
COPY employee_history FROM stdin;
\.

-- Datos para tabla employee_schedules
COPY employee_schedules FROM stdin;
\.

-- Datos para tabla employee_check_ins
COPY employee_check_ins FROM stdin;
\.

-- Datos para tabla employee_vacations
COPY employee_vacations FROM stdin;
\.

-- Datos para tabla checkpoint_records
COPY checkpoint_records FROM stdin;
\.

-- Datos para tabla checkpoint_incidents
COPY checkpoint_incidents FROM stdin;
\.

-- Datos para tabla checkpoint_original_records
COPY checkpoint_original_records FROM stdin;
\.

-- Datos para tabla employee_contract_hours
COPY employee_contract_hours FROM stdin;
\.

-- Datos para tabla tasks
COPY tasks FROM stdin;
\.

-- Datos para tabla task_weekdays
COPY task_weekdays FROM stdin;
\.

-- Datos para tabla task_schedules
COPY task_schedules FROM stdin;
\.

-- Datos para tabla task_instances
COPY task_instances FROM stdin;
\.

-- Datos para tabla task_completions
COPY task_completions FROM stdin;
\.

-- Datos para tabla product_conservations
COPY product_conservations FROM stdin;
\.

-- Datos para tabla product_labels
COPY product_labels FROM stdin;
\.

-- Datos para tabla label_templates
COPY label_templates FROM stdin;
\.

-- Datos para tabla activity_logs
COPY activity_logs FROM stdin;
\.

-- Actualizar secuencias
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

COMMIT;
__SQL_DUMP_ABOVE__

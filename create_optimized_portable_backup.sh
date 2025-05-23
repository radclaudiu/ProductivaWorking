#!/bin/bash
# Script optimizado para crear una copia de seguridad ejecutable de la base de datos Productiva
# Fecha: 23 de Mayo de 2025
# Este script genera un archivo .sh que puede ejecutarse en un servidor local
# para restaurar completamente la base de datos

# Colores para mensajes
ROJO='\033[0;31m'
VERDE='\033[0;32m'
AMARILLO='\033[0;33m'
AZUL='\033[0;34m'
RESET='\033[0m'

# Obtener las variables de entorno de PostgreSQL
DB_HOST=${PGHOST}
DB_PORT=${PGPORT}
DB_USER=${PGUSER}
DB_PASSWORD=${PGPASSWORD}
DB_NAME=${PGDATABASE}
FECHA_ACTUAL=$(date +"%Y%m%d_%H%M%S")
FILENAME="${DB_NAME}_portable_backup_${FECHA_ACTUAL}.sh"

echo -e "${AZUL}Generando copia de seguridad ejecutable optimizada...${RESET}"
echo -e "${AZUL}Conexión: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}${RESET}"

# Verificar conexión a la base de datos
export PGPASSWORD="${DB_PASSWORD}"
if ! psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "\conninfo" > /dev/null 2>&1; then
    echo -e "${ROJO}Error: No se puede conectar a la base de datos.${RESET}"
    exit 1
fi

# Obtener tablas esenciales de la base de datos
echo -e "${AZUL}Identificando tablas en la base de datos...${RESET}"
TABLAS=$(psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -t -d "${DB_NAME}" -c "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name;" | grep -v "^$")
TOTAL_TABLAS=$(echo "$TABLAS" | wc -l)
FECHA_BACKUP=$(date +"%d-%m-%Y %H:%M:%S")

echo -e "${VERDE}Se encontraron ${TOTAL_TABLAS} tablas en la base de datos.${RESET}"

# Crear el encabezado del script ejecutable
cat > ${FILENAME} << 'HEREDOC'
#!/bin/bash
# Script ejecutable para restauración de base de datos Productiva
# Generado automáticamente - NO MODIFICAR

# Colores para mensajes
ROJO='\033[0;31m'
VERDE='\033[0;32m'
AMARILLO='\033[0;33m'
AZUL='\033[0;34m'
RESET='\033[0m'

# Parámetros por defecto
DB_HOST="localhost"
DB_PORT="5432"
DB_USER="postgres"
DB_PASSWORD=""
DB_NAME="productiva"
FECHA_EJECUCION=$(date +"%d/%m/%Y %H:%M:%S")

# Función para mostrar ayuda
mostrar_ayuda() {
    echo -e "${AZUL}===========================================================${RESET}"
    echo -e "${AZUL}  Restauración de Base de Datos Productiva${RESET}"
    echo -e "${AZUL}===========================================================${RESET}"
    echo ""
    echo -e "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  -h, --host HOST      Host de la base de datos (por defecto: localhost)"
    echo "  -p, --port PUERTO    Puerto de la base de datos (por defecto: 5432)"
    echo "  -u, --user USUARIO   Usuario de la base de datos (por defecto: postgres)"
    echo "  -d, --db NOMBRE_DB   Nombre de la base de datos (por defecto: productiva)"
    echo "  --password PASS      Contraseña de la base de datos (evitar en producción)"
    echo "  --help               Muestra esta ayuda"
    echo ""
    echo -e "${AMARILLO}ADVERTENCIA: Este script eliminará la base de datos especificada si ya existe.${RESET}"
    echo ""
    exit 0
}

# Procesar parámetros de línea de comandos
while [[ $# -gt 0 ]]; do
    case "$1" in
        -h|--host)
            DB_HOST="$2"
            shift 2
            ;;
        -p|--port)
            DB_PORT="$2"
            shift 2
            ;;
        -u|--user)
            DB_USER="$2"
            shift 2
            ;;
        -d|--db)
            DB_NAME="$2"
            shift 2
            ;;
        --password)
            DB_PASSWORD="$2"
            shift 2
            ;;
        --help)
            mostrar_ayuda
            ;;
        *)
            echo -e "${ROJO}Error: Parámetro desconocido: $1${RESET}"
            mostrar_ayuda
            ;;
    esac
done

# Solicitar contraseña si no se proporcionó
if [[ -z "$DB_PASSWORD" ]]; then
    read -sp "Contraseña para usuario ${DB_USER}: " DB_PASSWORD
    echo ""
fi

# Exportar variables para uso con psql
export PGPASSWORD="$DB_PASSWORD"

# Verificar conexión a PostgreSQL
echo -e "${AZUL}Verificando conexión a PostgreSQL...${RESET}"
if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "\conninfo" postgres > /dev/null 2>&1; then
    echo -e "${ROJO}Error: No se puede conectar a PostgreSQL. Verifique las credenciales.${RESET}"
    exit 1
fi
echo -e "${VERDE}Conexión establecida correctamente.${RESET}"

# Verificar si la base de datos existe
DB_EXISTS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -t -c "SELECT 1 FROM pg_database WHERE datname='$DB_NAME';" postgres | grep -c 1)

# Información sobre la copia de seguridad
echo -e "${AZUL}===========================================================${RESET}"
echo -e "${AZUL}  RESTAURACIÓN DE BASE DE DATOS PRODUCTIVA${RESET}"
echo -e "${AZUL}===========================================================${RESET}"
echo -e "${AZUL}Fecha de creación:${RESET} BACKUP_DATE"
echo -e "${AZUL}Fecha de ejecución:${RESET} $FECHA_EJECUCION"
echo -e "${AZUL}Servidor destino:${RESET} $DB_HOST:$DB_PORT"
echo -e "${AZUL}Base de datos:${RESET} $DB_NAME"
echo -e "${AZUL}Tablas incluidas:${RESET} TOTAL_TABLES"
echo -e "${AZUL}===========================================================${RESET}"

# Confirmación antes de borrar la base de datos existente
if [[ $DB_EXISTS -eq 1 ]]; then
    echo -e "${AMARILLO}ADVERTENCIA: La base de datos '$DB_NAME' ya existe.${RESET}"
    read -p "¿Desea eliminarla y reemplazarla con esta copia de seguridad? (s/n): " CONFIRMAR
    if [[ ! "$CONFIRMAR" =~ ^[Ss]$ ]]; then
        echo -e "${ROJO}Operación cancelada por el usuario.${RESET}"
        exit 1
    fi
    
    echo -e "${AZUL}Eliminando base de datos existente...${RESET}"
    if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "DROP DATABASE IF EXISTS $DB_NAME;" postgres; then
        echo -e "${ROJO}Error al eliminar la base de datos existente.${RESET}"
        exit 1
    fi
fi

# Crear base de datos nueva
echo -e "${AZUL}Creando base de datos nueva...${RESET}"
if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "CREATE DATABASE $DB_NAME;" postgres; then
    echo -e "${ROJO}Error al crear la base de datos nueva.${RESET}"
    exit 1
fi

echo -e "${VERDE}Base de datos creada correctamente.${RESET}"
echo -e "${AZUL}Iniciando restauración de datos...${RESET}"

# Función para ejecutar consultas SQL
ejecutar_sql() {
    local sql="$1"
    echo "$sql" | psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME" >/dev/null 2>&1
    return $?
}

# Crear esquema
echo -e "${AZUL}Creando estructura de base de datos...${RESET}"

# Esquema SQL de la base de datos (generado automáticamente)
HEREDOC

# Añadir marcadores al script
sed -i "s|BACKUP_DATE|${FECHA_BACKUP}|g" ${FILENAME}
sed -i "s|TOTAL_TABLES|${TOTAL_TABLAS}|g" ${FILENAME}

# Generar script SQL para estructura de tablas
echo -e "${AZUL}Generando sentencias para estructura de tablas...${RESET}"
SCHEMA_SQL=$(psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -t -c "
    SELECT 'CREATE SCHEMA IF NOT EXISTS public;';
    SELECT 'SET search_path TO public;';
    SELECT 'BEGIN;';
    SELECT pg_get_ddl(format('%I.%I', schemaname, tablename), true)
    FROM pg_tables
    WHERE schemaname = 'public'
    ORDER BY tablename;
    SELECT 'COMMIT;';
")

# Añadir script para tipos enumerados
ENUM_SQL=$(psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -t -c "
    SELECT format(
        'DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = ''%s'') THEN CREATE TYPE %s AS ENUM (%s); END IF; END $$;',
        t.typname,
        t.typname,
        string_agg(format('''%s''', e.enumlabel), ', ' ORDER BY e.enumsortorder)
    )
    FROM pg_type t
    JOIN pg_enum e ON t.oid = e.enumtypid
    JOIN pg_namespace n ON n.oid = t.typnamespace
    WHERE n.nspname = 'public'
    GROUP BY t.typname;
")

# Añadir estructura al script ejecutable
echo "${ENUM_SQL}" >> ${FILENAME}
echo "${SCHEMA_SQL}" >> ${FILENAME}

# Generar script para insertar datos en cada tabla
echo -e "${AZUL}Generando sentencias para datos...${RESET}"

# Obtener lista de tablas
for tabla in $TABLAS; do
    echo -e "${AZUL}Procesando tabla: ${tabla}${RESET}"
    
    # Verificar si la tabla tiene datos
    COUNT=$(psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -t -d "${DB_NAME}" -c "SELECT COUNT(*) FROM ${tabla};" | tr -d '[:space:]')
    
    if [ "$COUNT" -gt 0 ]; then
        # Generar sentencias INSERT para tabla
        echo -e "${VERDE}Generando ${COUNT} registros para tabla ${tabla}${RESET}"
        
        # Añadir un comando para truncar la tabla antes de insertar
        echo "-- Truncar tabla ${tabla}" >> ${FILENAME}
        echo "TRUNCATE TABLE ${tabla} CASCADE;" >> ${FILENAME}
        
        # Generar sentencias COPY o INSERT según sea más eficiente
        if [ "$COUNT" -gt 1000 ]; then
            # Para tablas grandes, usar sentencias COPY
            echo "\\COPY ${tabla} FROM STDIN WITH CSV DELIMITER ',' NULL 'NULL';" >> ${FILENAME}
            psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "\COPY ${tabla} TO STDOUT WITH CSV DELIMITER ',' NULL 'NULL'" >> ${FILENAME}
            echo "\\." >> ${FILENAME}
        else
            # Para tablas pequeñas, generar INSERT
            INSERT_STMT=$(psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -t -c "
                SELECT format(
                    'INSERT INTO ${tabla} (%s) VALUES (%s);',
                    string_agg(format('%I', column_name), ', ' ORDER BY ordinal_position),
                    string_agg(
                        CASE 
                            WHEN data_type LIKE '%char%' OR data_type = 'text' THEN
                                format('''''' || REPLACE(COALESCE(%I::text, ''), '''', '''''') || '''''', column_name)
                            WHEN data_type = 'boolean' THEN
                                format('COALESCE(%I::text, ''false'')::boolean', column_name)
                            WHEN data_type LIKE '%int%' OR data_type LIKE '%float%' OR data_type LIKE '%double%' OR data_type = 'numeric' THEN
                                format('NULLIF(COALESCE(%I::text, ''NULL''), ''NULL'')::%s', column_name, data_type)
                            WHEN data_type = 'date' THEN
                                format('NULLIF(COALESCE(%I::text, ''NULL''), ''NULL'')::date', column_name)
                            WHEN data_type LIKE '%timestamp%' THEN
                                format('NULLIF(COALESCE(%I::text, ''NULL''), ''NULL'')::timestamp', column_name)
                            ELSE
                                format('NULLIF(COALESCE(%I::text, ''NULL''), ''NULL'')::%s', column_name, data_type)
                        END,
                        ', ' ORDER BY ordinal_position
                    )
                )
                FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = '${tabla}'
                GROUP BY table_name;
            ")
            
            # Generar datos para cada fila
            DATA_INSERT=$(psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -t -c "
                WITH col_names AS (
                    SELECT string_agg(format('%I', column_name), ', ' ORDER BY ordinal_position) as cols
                    FROM information_schema.columns
                    WHERE table_schema = 'public' AND table_name = '${tabla}'
                    GROUP BY table_name
                )
                SELECT format('INSERT INTO ${tabla} (%s) VALUES (%s);',
                    cols,
                    string_agg(
                        CASE 
                            WHEN col IS NULL THEN 'NULL'
                            WHEN col_type LIKE '%char%' OR col_type = 'text' THEN
                                '''' || REPLACE(col, '''', '''''') || ''''
                            ELSE col
                        END,
                        ', '
                    )
                )
                FROM (
                    SELECT c.cols, t.col, i.data_type as col_type
                    FROM col_names c
                    CROSS JOIN LATERAL (
                        SELECT *
                        FROM ${tabla}
                    ) t
                    JOIN information_schema.columns i ON 
                        i.table_schema = 'public' AND 
                        i.table_name = '${tabla}' AND
                        i.ordinal_position = row_number() OVER ()
                ) s;
            ")
            
            echo "${DATA_INSERT}" >> ${FILENAME}
        fi
    else
        echo -e "${AMARILLO}La tabla ${tabla} está vacía, saltando...${RESET}"
        echo "-- Tabla ${tabla} estaba vacía en origen" >> ${FILENAME}
    fi
done

# Añadir sección final al script ejecutable
cat >> ${FILENAME} << 'HEREDOC'

# Actualizar secuencias
echo -e "${AZUL}Actualizando secuencias...${RESET}"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME" << EOF
DO \$\$
DECLARE
    seq_name text;
    tbl_name text;
    col_name text;
    max_val bigint;
    seq_val bigint;
BEGIN
    FOR seq_name, tbl_name, col_name IN
        SELECT
            s.relname AS seq_name,
            t.relname AS tbl_name,
            a.attname AS col_name
        FROM pg_class s
        JOIN pg_depend d ON d.objid = s.oid
        JOIN pg_class t ON d.refobjid = t.oid
        JOIN pg_attribute a ON (d.refobjid = a.attrelid AND d.refobjsubid = a.attnum)
        JOIN pg_namespace n ON n.oid = s.relnamespace
        WHERE s.relkind = 'S'
        AND n.nspname = 'public'
    LOOP
        EXECUTE format('SELECT COALESCE(MAX(%I), 0) + 1 FROM %I', col_name, tbl_name) INTO max_val;
        EXECUTE format('SELECT last_value FROM %I', seq_name) INTO seq_val;
        
        IF max_val > seq_val THEN
            EXECUTE format('ALTER SEQUENCE %I RESTART WITH %s', seq_name, max_val);
            RAISE NOTICE 'Secuencia % actualizada a %', seq_name, max_val;
        END IF;
    END LOOP;
END;
\$\$;
EOF

# Verificar la restauración
echo -e "${AZUL}Verificando restauración...${RESET}"
TABLAS_RESTAURADAS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';" "$DB_NAME" | tr -d '[:space:]')

# Resultado final
echo -e "${AZUL}===========================================================${RESET}"
echo -e "${VERDE}RESTAURACIÓN COMPLETADA EXITOSAMENTE${RESET}"
echo -e "${AZUL}===========================================================${RESET}"
echo -e "${AZUL}Tablas restauradas:${RESET} $TABLAS_RESTAURADAS"
echo -e "${AZUL}Base de datos:${RESET} $DB_NAME@$DB_HOST"
echo -e "${AZUL}===========================================================${RESET}"

exit 0
HEREDOC

# Hacer ejecutable
chmod +x ${FILENAME}

echo -e "${VERDE}¡Copia de seguridad ejecutable optimizada creada con éxito!${RESET}"
echo -e "${AZUL}Archivo:${RESET} ${FILENAME}"
echo -e "${AZUL}Tamaño:${RESET} $(du -h ${FILENAME} | cut -f1)"
echo -e "${AZUL}Tablas incluidas:${RESET} ${TOTAL_TABLAS}"

exit 0
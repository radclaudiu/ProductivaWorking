#!/bin/bash
# Script para crear una copia de seguridad de tablas esenciales
# que puede ser ejecutada en un servidor local
# Fecha: 23 de Mayo de 2025

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
FILENAME="productiva_portable_backup_${FECHA_ACTUAL}.sh"

echo -e "${AZUL}Generando copia de seguridad portable para las tablas esenciales...${RESET}"
echo -e "${AZUL}Conexión: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}${RESET}"

# Verificar conexión a la base de datos
export PGPASSWORD="${DB_PASSWORD}"
if ! psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "\conninfo" > /dev/null 2>&1; then
    echo -e "${ROJO}Error: No se puede conectar a la base de datos.${RESET}"
    exit 1
fi

# Definir las tablas esenciales
TABLAS_ESENCIALES=(
    "users"
    "companies"
    "locations"
    "checkpoints"
    "checkpoint_records"
    "employees"
    "employee_contract_hours"
    "employee_work_hours"
    "task_templates"
    "task_instances"
    "products"
    "labels"
    "cash_registers"
    "cash_register_summaries"
    "monthly_expenses"
    "monthly_expense_categories"
)

# Verificar que las tablas existan
TABLAS_ENCONTRADAS=()
for tabla in "${TABLAS_ESENCIALES[@]}"; do
    if psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -t -c "SELECT to_regclass('public.${tabla}');" | grep -q "${tabla}"; then
        TABLAS_ENCONTRADAS+=("${tabla}")
        echo -e "${VERDE}✓ Tabla encontrada: ${tabla}${RESET}"
    else
        echo -e "${AMARILLO}⚠ Tabla no encontrada: ${tabla}${RESET}"
    fi
done

TOTAL_TABLAS=${#TABLAS_ENCONTRADAS[@]}
if [ $TOTAL_TABLAS -eq 0 ]; then
    echo -e "${ROJO}Error: No se encontraron tablas esenciales en la base de datos.${RESET}"
    exit 1
fi

FECHA_BACKUP=$(date +"%d-%m-%Y %H:%M:%S")

# Crear el encabezado del script ejecutable
cat > ${FILENAME} << 'EOF'
#!/bin/bash
# Script ejecutable para restauración de la base de datos Productiva
# Tablas esenciales para el funcionamiento del sistema
# IMPORTANTE: Este script destruirá la base de datos de destino y la reemplazará

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
    echo "  --password PASS      Contraseña para el usuario (no recomendado por seguridad)"
    echo "  --help               Muestra esta ayuda"
    echo ""
    echo -e "${AMARILLO}ADVERTENCIA: Este script eliminará la base de datos si ya existe.${RESET}"
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
EOF

# Añadir los datos de la copia al script
cat >> ${FILENAME} << EOF
echo -e "\${AZUL}Fecha de creación de backup:${RESET} ${FECHA_BACKUP}"
echo -e "\${AZUL}Fecha de restauración:${RESET} \$FECHA_EJECUCION"
echo -e "\${AZUL}Servidor destino:${RESET} \$DB_HOST:\$DB_PORT"
echo -e "\${AZUL}Base de datos:${RESET} \$DB_NAME"
echo -e "\${AZUL}Tablas incluidas:${RESET} ${TOTAL_TABLAS}"
echo -e "\${AZUL}===========================================================${RESET}"
EOF

# Continuar con el script ejecutable
cat >> ${FILENAME} << 'EOF'

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
echo -e "${AZUL}Restaurando estructura y datos esenciales...${RESET}"

# Contenido SQL insertado aquí
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME" << 'SQLCONTENT'
EOF

# Obtener y crear los tipos ENUM primero
echo -e "${AZUL}Generando tipos ENUM...${RESET}"
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

echo "${ENUM_SQL}" >> ${FILENAME}

# Para cada tabla, generar CREATE TABLE y datos
for tabla in "${TABLAS_ENCONTRADAS[@]}"; do
    echo -e "${AZUL}Procesando tabla: ${tabla}${RESET}"
    
    # Obtener la estructura de la tabla
    echo -e "-- Estructura para la tabla ${tabla}" >> ${FILENAME}
    pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -t "public.${tabla}" --schema-only --no-owner --no-privileges "${DB_NAME}" >> ${FILENAME}
    
    # Obtener cantidad de registros
    NUM_RECORDS=$(psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -t -c "SELECT COUNT(*) FROM ${tabla};" "${DB_NAME}" | tr -d '[:space:]')
    
    if [ "$NUM_RECORDS" -gt 0 ]; then
        echo -e "${VERDE}Exportando ${NUM_RECORDS} registros de ${tabla}${RESET}"
        echo -e "-- Datos para la tabla ${tabla}" >> ${FILENAME}
        echo -e "TRUNCATE TABLE ${tabla} CASCADE;" >> ${FILENAME}
        
        # Usar COPY para datos (más eficiente que INSERT para muchos registros)
        echo "\\COPY ${tabla} FROM STDIN WITH DELIMITER ',' CSV NULL 'NULL';" >> ${FILENAME}
        psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -c "\COPY ${tabla} TO STDOUT WITH DELIMITER ',' CSV NULL 'NULL'" "${DB_NAME}" >> ${FILENAME}
        echo "\\." >> ${FILENAME}
    else
        echo -e "${AMARILLO}La tabla ${tabla} está vacía${RESET}"
        echo -e "-- La tabla ${tabla} está vacía" >> ${FILENAME}
    fi
done

# Añadir sentencias para actualizar las secuencias
echo -e "${AZUL}Generando sentencias para actualizar secuencias...${RESET}"
cat >> ${FILENAME} << 'EOF'

-- Actualizar todas las secuencias basadas en los valores máximos de las tablas
DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN
        SELECT
            n.nspname AS schema_name,
            s.relname AS sequence_name,
            t.relname AS table_name,
            a.attname AS column_name
        FROM
            pg_class s
            JOIN pg_depend d ON (d.objid = s.oid AND d.classid = 'pg_class'::regclass AND d.refclassid = 'pg_class'::regclass)
            JOIN pg_class t ON (t.oid = d.refobjid)
            JOIN pg_namespace n ON (n.oid = t.relnamespace)
            JOIN pg_attribute a ON (a.attrelid = t.oid AND a.attnum = d.refobjsubid)
        WHERE
            s.relkind = 'S'
            AND n.nspname = 'public'
    LOOP
        EXECUTE format('SELECT setval(''%I.%I'', COALESCE((SELECT MAX(%I) FROM %I.%I), 1), true)',
            rec.schema_name, rec.sequence_name, rec.column_name, rec.schema_name, rec.table_name);
    END LOOP;
END;
$$;
EOF

# Cerrar el bloque SQL y finalizar el script
cat >> ${FILENAME} << 'EOF'
SQLCONTENT

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
EOF

# Hacer ejecutable
chmod +x ${FILENAME}

echo -e "${VERDE}¡Copia de seguridad ejecutable creada correctamente!${RESET}"
echo -e "${AZUL}Archivo:${RESET} ${FILENAME}"
echo -e "${AZUL}Tamaño:${RESET} $(du -h ${FILENAME} | cut -f1)"
echo -e "${AZUL}Tablas incluidas:${RESET} ${TOTAL_TABLAS}"
echo -e ""
echo -e "${AZUL}Para restaurar esta copia en otro servidor, simplemente transfiera el archivo y ejecútelo:${RESET}"
echo -e "  ${VERDE}scp ${FILENAME} usuario@servidor:/ruta/destino/${RESET}"
echo -e "  ${VERDE}ssh usuario@servidor 'bash /ruta/destino/${FILENAME}'${RESET}"
echo -e ""
echo -e "${AZUL}Para especificar diferentes parámetros, ejecute:${RESET}"
echo -e "  ${VERDE}./${FILENAME} --host localhost --port 5432 --user postgres --db productiva${RESET}"
echo -e ""

exit 0
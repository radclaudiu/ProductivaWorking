#!/bin/bash
# Script para crear una copia de seguridad ejecutable de la base de datos Productiva
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

echo -e "${AZUL}Generando copia de seguridad ejecutable de la base de datos ${DB_NAME}...${RESET}"
echo -e "${AZUL}Utilizando conexión: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}${RESET}"

# Verificar conexión a la base de datos
export PGPASSWORD="${DB_PASSWORD}"
if ! psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "\dt" > /dev/null 2>&1; then
    echo -e "${ROJO}Error: No se puede conectar a la base de datos ${DB_NAME}${RESET}"
    echo -e "${ROJO}Verifique las credenciales de conexión.${RESET}"
    exit 1
fi

# Generar información sobre la copia de seguridad
echo -e "${AZUL}Analizando estructura de la base de datos...${RESET}"
TABLAS=$(psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -t -d "${DB_NAME}" -c "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name;")
TOTAL_TABLAS=$(echo "$TABLAS" | wc -l)
FECHA_BACKUP=$(date +"%d-%m-%Y %H:%M:%S")

# Crear archivo ejecutable
cat > ${FILENAME} << 'HEREDOC'
#!/bin/bash
# Script ejecutable para restauración de base de datos Productiva
# Generado automáticamente - NO MODIFICAR EL CONTENIDO

# Colores para mensajes
ROJO='\033[0;31m'
VERDE='\033[0;32m'
AMARILLO='\033[0;33m'
AZUL='\033[0;34m'
RESET='\033[0m'

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
    echo "  --help               Muestra esta ayuda"
    echo ""
    echo -e "${AMARILLO}ADVERTENCIA: Este script eliminará la base de datos especificada si ya existe.${RESET}"
    echo ""
    exit 0
}

# Parámetros por defecto
DB_HOST="localhost"
DB_PORT="5432"
DB_USER="postgres"
DB_PASSWORD=""
DB_NAME="productiva"
FECHA_EJECUCION=$(date +"%d/%m/%Y %H:%M:%S")

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
if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "\conninfo" > /dev/null 2>&1; then
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
echo -e "${AZUL}Fecha de creación de la copia:${RESET} BACKUP_DATE"
echo -e "${AZUL}Fecha de restauración:${RESET} $FECHA_EJECUCION"
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

# Restauración del esquema y datos (autogenerado)
echo -e "${AZUL}Restaurando estructura y datos...${RESET}"

# Aquí se insertará el SQL completo de la base de datos
SQL_DUMP_CONTENT

# Verificación final
echo -e "${AZUL}Verificando la restauración...${RESET}"
TABLAS_RESTAURADAS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';" "$DB_NAME")
echo -e "${VERDE}Restauración completada exitosamente.${RESET}"
echo -e "${VERDE}Tablas restauradas: $TABLAS_RESTAURADAS${RESET}"
echo -e "${AZUL}===========================================================${RESET}"
echo -e "${VERDE}LA BASE DE DATOS HA SIDO RESTAURADA CORRECTAMENTE.${RESET}"
echo -e "${AZUL}===========================================================${RESET}"

exit 0
HEREDOC

# Sustituir marcadores de posición en el script
sed -i "s|BACKUP_DATE|${FECHA_BACKUP}|g" ${FILENAME}
sed -i "s|TOTAL_TABLES|${TOTAL_TABLAS}|g" ${FILENAME}

# Obtener el dump SQL
echo -e "${AZUL}Generando dump SQL completo...${RESET}"
pg_dump --clean --if-exists --schema=public --no-owner --no-privileges -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" "${DB_NAME}" > temp_dump.sql

# Escapar caracteres especiales en el dump SQL
sed -i 's/\$/\\$/g' temp_dump.sql
sed -i 's/`/\\`/g' temp_dump.sql

# Insertar el contenido SQL en el script
SQL_CONTENT=$(cat temp_dump.sql)
sed -i "s|SQL_DUMP_CONTENT|cat << 'SQLCONTENT' | psql -h \"\$DB_HOST\" -p \"\$DB_PORT\" -U \"\$DB_USER\" \"\$DB_NAME\"\n${SQL_CONTENT}\nSQLCONTENT|g" ${FILENAME}

# Limpiar archivos temporales
rm temp_dump.sql

# Hacer el script ejecutable
chmod +x ${FILENAME}

echo -e "${VERDE}¡Copia de seguridad ejecutable creada correctamente!${RESET}"
echo -e "${AZUL}Archivo:${RESET} ${FILENAME}"
echo -e "${AZUL}Tamaño:${RESET} $(du -h ${FILENAME} | cut -f1)"
echo -e "${AZUL}Fecha:${RESET} ${FECHA_BACKUP}"
echo -e "${AZUL}Tablas incluidas:${RESET} ${TOTAL_TABLAS}"
echo -e ""
echo -e "${AZUL}Para restaurar esta copia en otro servidor, simplemente transfiera el archivo y ejecútelo:${RESET}"
echo -e "  ${VERDE}scp ${FILENAME} usuario@servidor:/ruta/destino/${RESET}"
echo -e "  ${VERDE}ssh usuario@servidor 'bash /ruta/destino/${FILENAME}'${RESET}"
echo -e ""
echo -e "${AMARILLO}NOTA: El script solicitará la contraseña de PostgreSQL al ejecutarse.${RESET}"
echo -e "${AMARILLO}      Para especificar diferentes parámetros use:${RESET}"
echo -e "  ${VERDE}./${FILENAME} --host servidor --port 5432 --user postgres --db productiva${RESET}"
echo -e ""

exit 0
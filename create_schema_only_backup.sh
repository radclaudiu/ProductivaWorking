#!/bin/bash
# Script para crear un backup que contiene solo el esquema de la base de datos
# Este backup puede ser ejecutado en un servidor local para crear la estructura
# necesaria sin los datos, ideal para bases de datos nuevas
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
FILENAME="productiva_schema_${FECHA_ACTUAL}.sh"

echo -e "${AZUL}Generando script de creación de estructura de base de datos...${RESET}"
echo -e "${AZUL}Conexión: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}${RESET}"

# Verificar conexión a la base de datos
export PGPASSWORD="${DB_PASSWORD}"
if ! psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "\conninfo" > /dev/null 2>&1; then
    echo -e "${ROJO}Error: No se puede conectar a la base de datos.${RESET}"
    exit 1
fi

# Contar las tablas en la base de datos
TOTAL_TABLAS=$(psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -t -d "${DB_NAME}" -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';" | tr -d '[:space:]')
FECHA_BACKUP=$(date +"%d-%m-%Y %H:%M:%S")

echo -e "${VERDE}Se encontraron ${TOTAL_TABLAS} tablas en la base de datos.${RESET}"

# Crear el encabezado del script ejecutable
cat > ${FILENAME} << 'EOF'
#!/bin/bash
# Script ejecutable para crear la estructura de base de datos Productiva
# Este script creará SOLO LA ESTRUCTURA - SIN DATOS
# Ideal para servidores locales o de desarrollo

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
    echo -e "${AZUL}  Creación de Estructura de Base de Datos Productiva${RESET}"
    echo -e "${AZUL}  IMPORTANTE: SOLO ESTRUCTURA - NO INCLUYE DATOS${RESET}"
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
echo -e "${AZUL}  CREACIÓN DE ESTRUCTURA DE BASE DE DATOS PRODUCTIVA${RESET}"
echo -e "${AZUL}===========================================================${RESET}"
EOF

# Añadir los datos de la copia al script
cat >> ${FILENAME} << EOF
echo -e "\${AZUL}Fecha de generación:${RESET} ${FECHA_BACKUP}"
echo -e "\${AZUL}Fecha de ejecución:${RESET} \$FECHA_EJECUCION"
echo -e "\${AZUL}Servidor destino:${RESET} \$DB_HOST:\$DB_PORT"
echo -e "\${AZUL}Base de datos:${RESET} \$DB_NAME"
echo -e "\${AZUL}Total de tablas:${RESET} ${TOTAL_TABLAS}"
echo -e "\${AZUL}===========================================================${RESET}"
EOF

# Continuar con el script ejecutable
cat >> ${FILENAME} << 'EOF'

# Confirmación antes de borrar la base de datos existente
if [[ $DB_EXISTS -eq 1 ]]; then
    echo -e "${AMARILLO}ADVERTENCIA: La base de datos '$DB_NAME' ya existe.${RESET}"
    read -p "¿Desea eliminarla y reemplazarla con esta estructura nueva? (s/n): " CONFIRMAR
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
echo -e "${AZUL}Creando estructura de la base de datos...${RESET}"

# Ejecutar script SQL con la estructura
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME" << 'SQLCONTENT'
EOF

# Obtener y añadir los tipos ENUM primero
echo -e "${AZUL}Exportando tipos ENUM...${RESET}"
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

# Añadir los tipos ENUM al script
echo "${ENUM_SQL}" >> ${FILENAME}

# Obtener el esquema completo de la base de datos (solo estructura, sin datos)
echo -e "${AZUL}Exportando estructura de tablas...${RESET}"
pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" --schema-only --no-owner --no-privileges "${DB_NAME}" >> ${FILENAME}

# Cerrar el bloque SQL y finalizar el script
cat >> ${FILENAME} << 'EOF'
SQLCONTENT

# Verificar la creación de la estructura
echo -e "${AZUL}Verificando estructura creada...${RESET}"
TABLAS_CREADAS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';" "$DB_NAME" | tr -d '[:space:]')

# Resultado final
echo -e "${AZUL}===========================================================${RESET}"
echo -e "${VERDE}ESTRUCTURA DE BASE DE DATOS CREADA EXITOSAMENTE${RESET}"
echo -e "${AZUL}===========================================================${RESET}"
echo -e "${AZUL}Tablas creadas:${RESET} $TABLAS_CREADAS"
echo -e "${AZUL}Base de datos:${RESET} $DB_NAME@$DB_HOST"
echo -e "${AZUL}===========================================================${RESET}"
echo -e ""
echo -e "${AMARILLO}NOTA IMPORTANTE:${RESET}"
echo -e "Esta estructura de base de datos NO incluye datos. Para completar la"
echo -e "instalación, necesitará insertar datos manualmente o mediante scripts"
echo -e "adicionales para que la aplicación funcione correctamente."
echo -e ""
echo -e "${AZUL}Sugerencia:${RESET} Puede crear un usuario administrador con el siguiente comando:"
echo -e "psql -h $DB_HOST -p $DB_PORT -U $DB_USER $DB_NAME -c \"INSERT INTO users (username, email, password_hash, role) VALUES ('admin', 'admin@example.com', 'pbkdf2:sha256:150000$abcd1234$abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890', 'admin');\""
echo -e ""

exit 0
EOF

# Hacer ejecutable
chmod +x ${FILENAME}

echo -e "${VERDE}¡Script de creación de estructura generado correctamente!${RESET}"
echo -e "${AZUL}Archivo:${RESET} ${FILENAME}"
echo -e "${AZUL}Tamaño:${RESET} $(du -h ${FILENAME} | cut -f1)"
echo -e "${AZUL}Tablas incluidas:${RESET} ${TOTAL_TABLAS}"
echo -e ""
echo -e "${AZUL}Para ejecutar este script en un servidor local:${RESET}"
echo -e "  ${VERDE}scp ${FILENAME} usuario@servidor:/ruta/destino/${RESET}"
echo -e "  ${VERDE}ssh usuario@servidor 'bash /ruta/destino/${FILENAME}'${RESET}"
echo -e ""
echo -e "${AMARILLO}NOTA: Este script creará SOLO la estructura de tablas, sin datos.${RESET}"
echo -e "${AMARILLO}      Ideal para servidores de desarrollo o pruebas.${RESET}"
echo -e ""
echo -e "${AZUL}Para especificar diferentes parámetros, ejecute:${RESET}"
echo -e "  ${VERDE}./${FILENAME} --host localhost --port 5432 --user postgres --db productiva${RESET}"
echo -e ""

exit 0
#!/bin/bash

# Script para migrar una base de datos Neon DB a una nueva instancia PostgreSQL
# Este script puede ejecutarse en la máquina destino para migrar la base de datos

# Colores para mensajes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuración de la base de datos origen (Neon DB)
SOURCE_DB_HOST="ep-crimson-grass-a4jo4xfu.us-east-1.aws.neon.tech"
SOURCE_DB_PORT="5432"
SOURCE_DB_NAME="neondb"
SOURCE_DB_USER="neondb_owner"
SOURCE_DB_PASS="npg_3SbGfUue5LCT"
SOURCE_DB_SSL="require"

# Obtener fecha actual para el nombre del archivo
FECHA=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="./backups"
BACKUP_FILE="${BACKUP_DIR}/neondb_backup_${FECHA}.sql"

# Crear directorio de backups si no existe
mkdir -p $BACKUP_DIR

echo -e "${YELLOW}=== MIGRACIÓN DE BASE DE DATOS NEON DB ===${NC}"
echo -e "${YELLOW}Este script migra una base de datos desde Neon DB a otra instancia PostgreSQL${NC}"

# Verificar si pg_dump y psql están instalados
if ! command -v pg_dump &> /dev/null || ! command -v psql &> /dev/null; then
    echo -e "${RED}Error: Se requiere PostgreSQL client (pg_dump, psql).${NC}"
    echo -e "${YELLOW}Por favor instala las herramientas de cliente PostgreSQL:${NC}"
    echo -e "${GREEN}sudo apt update && sudo apt install -y postgresql-client${NC}"
    exit 1
fi

echo -e "\n${YELLOW}=== FASE 1: EXTRACCIÓN DE DATOS DESDE NEON DB ===${NC}"
echo -e "${GREEN}Conectando a: ${SOURCE_DB_HOST}${NC}"

# Crear archivo de credenciales temporales para pg_dump
PGPASSFILE=$(mktemp)
echo "${SOURCE_DB_HOST}:${SOURCE_DB_PORT}:${SOURCE_DB_NAME}:${SOURCE_DB_USER}:${SOURCE_DB_PASS}" > $PGPASSFILE
chmod 600 $PGPASSFILE
export PGPASSFILE

# Ejecutar pg_dump para crear la copia de seguridad
echo -e "${YELLOW}Extrayendo datos a ${BACKUP_FILE}...${NC}"
echo -e "${YELLOW}Este proceso puede tomar varios minutos dependiendo del tamaño de la base de datos.${NC}"

# Usar pg_dump con opciones específicas para mayor compatibilidad
pg_dump -h ${SOURCE_DB_HOST} -p ${SOURCE_DB_PORT} -U ${SOURCE_DB_USER} -d ${SOURCE_DB_NAME} \
    --format=plain \
    --no-owner \
    --no-acl \
    --clean \
    --if-exists \
    --create \
    --inserts \
    --sslmode=${SOURCE_DB_SSL} \
    > ${BACKUP_FILE}

# Verificar si pg_dump se ejecutó correctamente
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Datos extraídos con éxito en: ${BACKUP_FILE}${NC}"
    
    # Mostrar tamaño del archivo
    FILE_SIZE=$(du -h ${BACKUP_FILE} | cut -f1)
    echo -e "${GREEN}Tamaño del archivo de backup: ${FILE_SIZE}${NC}"
    
    echo -e "\n${YELLOW}=== FASE 2: IMPORTACIÓN DE DATOS A NUEVA INSTANCIA ===${NC}"
    
    # Solicitar información de la base de datos de destino
    echo -e "${YELLOW}Ingresa la información de la base de datos de destino:${NC}"
    
    echo -e "${YELLOW}Host (dejar en blanco para usar 'localhost'):${NC}"
    read TARGET_DB_HOST
    TARGET_DB_HOST=${TARGET_DB_HOST:-localhost}
    
    echo -e "${YELLOW}Puerto (dejar en blanco para usar '5432'):${NC}"
    read TARGET_DB_PORT
    TARGET_DB_PORT=${TARGET_DB_PORT:-5432}
    
    echo -e "${YELLOW}Nombre de la base de datos destino (dejar en blanco para usar '${SOURCE_DB_NAME}'):${NC}"
    read TARGET_DB_NAME
    TARGET_DB_NAME=${TARGET_DB_NAME:-${SOURCE_DB_NAME}}
    
    echo -e "${YELLOW}Usuario (dejar en blanco para usar el usuario actual '$(whoami)'):${NC}"
    read TARGET_DB_USER
    TARGET_DB_USER=${TARGET_DB_USER:-$(whoami)}
    
    echo -e "${YELLOW}Contraseña (déjala en blanco si usas autenticación peer):${NC}"
    read -s TARGET_DB_PASS
    echo ""  # Salto de línea después de la contraseña
    
    # Resumen de la configuración
    echo -e "\n${GREEN}Resumen de la migración:${NC}"
    echo -e "- Base de datos origen: ${SOURCE_DB_NAME} (${SOURCE_DB_HOST})"
    echo -e "- Base de datos destino: ${TARGET_DB_NAME} (${TARGET_DB_HOST})"
    
    echo -e "\n${YELLOW}¿Crear/reemplazar la base de datos en el servidor destino? (y/n, por defecto: y):${NC}"
    read CREATE_DB
    CREATE_DB=${CREATE_DB:-y}
    
    if [ "$CREATE_DB" = "y" ]; then
        echo -e "${YELLOW}Creando/reemplazando base de datos ${TARGET_DB_NAME} en ${TARGET_DB_HOST}...${NC}"
        
        # Crear archivo temporal para credenciales
        TARGET_PGPASSFILE=$(mktemp)
        echo "${TARGET_DB_HOST}:${TARGET_DB_PORT}:postgres:${TARGET_DB_USER}:${TARGET_DB_PASS}" > $TARGET_PGPASSFILE
        chmod 600 $TARGET_PGPASSFILE
        export PGPASSFILE=$TARGET_PGPASSFILE
        
        # Eliminar y crear la base de datos
        PSQL_DROP_CMD="DROP DATABASE IF EXISTS \"${TARGET_DB_NAME}\";"
        PSQL_CREATE_CMD="CREATE DATABASE \"${TARGET_DB_NAME}\";"
        
        if psql -h $TARGET_DB_HOST -p $TARGET_DB_PORT -U $TARGET_DB_USER -d postgres -c "$PSQL_DROP_CMD" && \
           psql -h $TARGET_DB_HOST -p $TARGET_DB_PORT -U $TARGET_DB_USER -d postgres -c "$PSQL_CREATE_CMD"; then
            echo -e "${GREEN}✅ Base de datos creada con éxito.${NC}"
        else
            echo -e "${RED}❌ Error al crear la base de datos.${NC}"
            echo -e "${YELLOW}Verifica tus permisos y que la información de conexión sea correcta.${NC}"
            
            echo -e "\n${YELLOW}¿Deseas continuar con la importación de todas formas? (y/n):${NC}"
            read CONTINUE
            
            if [ "$CONTINUE" != "y" ]; then
                echo -e "${RED}Migración cancelada.${NC}"
                rm -f $TARGET_PGPASSFILE
                exit 1
            fi
        fi
        
        rm -f $TARGET_PGPASSFILE
    fi
    
    # Restaurar la base de datos
    echo -e "${YELLOW}Importando datos a ${TARGET_DB_NAME}...${NC}"
    echo -e "${YELLOW}Este proceso puede tomar varios minutos.${NC}"
    
    # Crear un nuevo archivo de credenciales para la base de datos de destino
    TARGET_PGPASSFILE=$(mktemp)
    echo "${TARGET_DB_HOST}:${TARGET_DB_PORT}:${TARGET_DB_NAME}:${TARGET_DB_USER}:${TARGET_DB_PASS}" > $TARGET_PGPASSFILE
    chmod 600 $TARGET_PGPASSFILE
    export PGPASSFILE=$TARGET_PGPASSFILE
    
    # Ejecutar la importación
    if psql -h $TARGET_DB_HOST -p $TARGET_DB_PORT -U $TARGET_DB_USER -d $TARGET_DB_NAME -f $BACKUP_FILE; then
        echo -e "${GREEN}✅ Base de datos migrada con éxito.${NC}"
        
        # Verificar la migración
        echo -e "${YELLOW}Verificando migración...${NC}"
        TABLE_COUNT=$(psql -h $TARGET_DB_HOST -p $TARGET_DB_PORT -U $TARGET_DB_USER -d $TARGET_DB_NAME -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")
        TABLE_COUNT=$(echo $TABLE_COUNT | xargs)  # Eliminar espacios en blanco
        
        if [ "$TABLE_COUNT" -gt 0 ]; then
            echo -e "${GREEN}Verificación exitosa: ${TABLE_COUNT} tablas importadas.${NC}"
            
            # Mostrar las tablas importadas
            echo -e "${YELLOW}Tablas importadas:${NC}"
            psql -h $TARGET_DB_HOST -p $TARGET_DB_PORT -U $TARGET_DB_USER -d $TARGET_DB_NAME -c "\dt" | grep -v "relations found"
            
            echo -e "\n${GREEN}🎉 ¡MIGRACIÓN COMPLETADA CON ÉXITO! 🎉${NC}"
            echo -e "${YELLOW}Puedes conectarte a tu nueva base de datos con:${NC}"
            echo -e "${GREEN}psql -h ${TARGET_DB_HOST} -p ${TARGET_DB_PORT} -U ${TARGET_DB_USER} -d ${TARGET_DB_NAME}${NC}"
            
            # Crear script de conexión para facilitar el acceso futuro
            CONNECT_SCRIPT="connect_to_${TARGET_DB_NAME}.sh"
            echo '#!/bin/bash' > $CONNECT_SCRIPT
            echo "# Script para conectar a la base de datos migrada" >> $CONNECT_SCRIPT
            echo "export PGPASSWORD='${TARGET_DB_PASS}'" >> $CONNECT_SCRIPT
            echo "psql -h ${TARGET_DB_HOST} -p ${TARGET_DB_PORT} -U ${TARGET_DB_USER} -d ${TARGET_DB_NAME}" >> $CONNECT_SCRIPT
            chmod +x $CONNECT_SCRIPT
            
            echo -e "${YELLOW}Se ha creado un script de conexión: ${CONNECT_SCRIPT}${NC}"
        else
            echo -e "${RED}⚠️ Advertencia: No se encontraron tablas en la base de datos migrada.${NC}"
            echo -e "${YELLOW}Verifica el archivo de backup y la importación.${NC}"
        fi
    else
        echo -e "${RED}❌ Error al importar la base de datos.${NC}"
        echo -e "${YELLOW}Por favor revisa los mensajes de error anteriores para más información.${NC}"
    fi
    
    # Eliminar archivo temporal de credenciales
    rm -f $TARGET_PGPASSFILE
else
    echo -e "${RED}❌ Error al extraer datos de Neon DB.${NC}"
    echo -e "${YELLOW}Verifica que la información de conexión sea correcta y que tengas permisos suficientes.${NC}"
    echo -e "${YELLOW}Asegúrate de tener una conexión estable a Internet.${NC}"
fi

# Eliminar el archivo temporal de credenciales original
rm -f $PGPASSFILE

echo -e "\n${GREEN}=== PROCESO DE MIGRACIÓN FINALIZADO ===${NC}"
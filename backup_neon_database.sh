#!/bin/bash

# Script para hacer una copia de seguridad de una base de datos Neon DB
# Configurado específicamente para la base de datos:
# postgresql://neondb_owner:npg_3SbGfUue5LCT@ep-crimson-grass-a4jo4xfu.us-east-1.aws.neon.tech/neondb?sslmode=require

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

echo -e "${YELLOW}Iniciando copia de seguridad de la base de datos Neon...${NC}"

# Crear archivo de credenciales temporales para pg_dump
PGPASSFILE=$(mktemp)
echo "${SOURCE_DB_HOST}:${SOURCE_DB_PORT}:${SOURCE_DB_NAME}:${SOURCE_DB_USER}:${SOURCE_DB_PASS}" > $PGPASSFILE
chmod 600 $PGPASSFILE
export PGPASSFILE

# Mostrar información de conexión
echo -e "${GREEN}Información de conexión:${NC}"
echo -e "- Host: ${SOURCE_DB_HOST}"
echo -e "- Puerto: ${SOURCE_DB_PORT}"
echo -e "- Base de datos: ${SOURCE_DB_NAME}"
echo -e "- Usuario: ${SOURCE_DB_USER}"
echo -e "- SSL: ${SOURCE_DB_SSL}"

# Ejecutar pg_dump para crear la copia de seguridad
echo -e "${YELLOW}Creando copia de seguridad en ${BACKUP_FILE}...${NC}"
echo -e "${YELLOW}Este proceso puede tomar varios minutos dependiendo del tamaño de la base de datos.${NC}"

# Usar pg_dump con opciones para crear un archivo de backup completo y compatible
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
    echo -e "${GREEN}✅ Copia de seguridad creada con éxito en: ${BACKUP_FILE}${NC}"
    
    # Añadir instrucciones al archivo para facilitar la restauración
    echo -e "\n-- INSTRUCCIONES PARA RESTAURAR ESTA BASE DE DATOS:" >> ${BACKUP_FILE}
    echo -e "-- 1. Crea una base de datos vacía: CREATE DATABASE destino;" >> ${BACKUP_FILE}
    echo -e "-- 2. Conéctate a la base de datos: \\c destino" >> ${BACKUP_FILE}
    echo -e "-- 3. Ejecuta este archivo: psql -d destino -f ${BACKUP_FILE}" >> ${BACKUP_FILE}
    echo -e "-- 4. O simplemente: psql -f ${BACKUP_FILE} postgres" >> ${BACKUP_FILE}
    
    # Mostrar tamaño del archivo
    FILE_SIZE=$(du -h ${BACKUP_FILE} | cut -f1)
    echo -e "${GREEN}Tamaño del archivo de backup: ${FILE_SIZE}${NC}"
    
    echo -e "\n${YELLOW}=== PARTE 2: RESTAURAR LA BASE DE DATOS EN OTRA INSTANCIA ===${NC}"
    echo -e "${YELLOW}Ahora puedes ejecutar el script en tu instancia de destino.${NC}"
    
    # Preguntar si quiere restaurar en una base de datos de destino ahora
    echo -e "\n${YELLOW}¿Deseas restaurar esta copia en otra base de datos ahora? (y/n):${NC}"
    read RESTORE_NOW
    
    if [ "$RESTORE_NOW" = "y" ]; then
        # Solicitar información de la base de datos de destino
        echo -e "\n${YELLOW}Ingresa la información de la base de datos de destino:${NC}"
        
        echo -e "${YELLOW}Host (dejar en blanco para usar 'localhost'):${NC}"
        read TARGET_DB_HOST
        TARGET_DB_HOST=${TARGET_DB_HOST:-localhost}
        
        echo -e "${YELLOW}Puerto (dejar en blanco para usar '5432'):${NC}"
        read TARGET_DB_PORT
        TARGET_DB_PORT=${TARGET_DB_PORT:-5432}
        
        echo -e "${YELLOW}Nombre de la base de datos (dejar en blanco para usar '${SOURCE_DB_NAME}'):${NC}"
        read TARGET_DB_NAME
        TARGET_DB_NAME=${TARGET_DB_NAME:-${SOURCE_DB_NAME}}
        
        echo -e "${YELLOW}Usuario (dejar en blanco para usar el usuario actual '$(whoami)'):${NC}"
        read TARGET_DB_USER
        TARGET_DB_USER=${TARGET_DB_USER:-$(whoami)}
        
        echo -e "${YELLOW}Contraseña (déjala en blanco si usas autenticación peer):${NC}"
        read -s TARGET_DB_PASS
        echo ""  # Salto de línea después de la contraseña
        
        # Crear la base de datos de destino si no existe
        echo -e "${YELLOW}¿Crear la base de datos en el servidor destino? (y/n, por defecto: y):${NC}"
        read CREATE_DB
        CREATE_DB=${CREATE_DB:-y}
        
        if [ "$CREATE_DB" = "y" ]; then
            echo -e "${YELLOW}Creando base de datos ${TARGET_DB_NAME} en ${TARGET_DB_HOST}...${NC}"
            
            # Crear archivo temporal para credenciales
            TARGET_PGPASSFILE=$(mktemp)
            echo "${TARGET_DB_HOST}:${TARGET_DB_PORT}:postgres:${TARGET_DB_USER}:${TARGET_DB_PASS}" > $TARGET_PGPASSFILE
            chmod 600 $TARGET_PGPASSFILE
            export PGPASSFILE=$TARGET_PGPASSFILE
            
            if psql -h $TARGET_DB_HOST -p $TARGET_DB_PORT -U $TARGET_DB_USER postgres -c "DROP DATABASE IF EXISTS \"${TARGET_DB_NAME}\";"; then
                if psql -h $TARGET_DB_HOST -p $TARGET_DB_PORT -U $TARGET_DB_USER postgres -c "CREATE DATABASE \"${TARGET_DB_NAME}\";"; then
                    echo -e "${GREEN}Base de datos creada con éxito.${NC}"
                else
                    echo -e "${RED}No se pudo crear la base de datos.${NC}"
                    exit 1
                fi
            else
                echo -e "${RED}No se pudo eliminar la base de datos existente.${NC}"
                exit 1
            fi
            
            rm -f $TARGET_PGPASSFILE
        fi
        
        # Restaurar la base de datos
        echo -e "${YELLOW}Restaurando la base de datos desde ${BACKUP_FILE}...${NC}"
        echo -e "${YELLOW}Este proceso puede tomar varios minutos.${NC}"
        
        # Crear un nuevo archivo de credenciales para la base de datos de destino
        TARGET_PGPASSFILE=$(mktemp)
        echo "${TARGET_DB_HOST}:${TARGET_DB_PORT}:${TARGET_DB_NAME}:${TARGET_DB_USER}:${TARGET_DB_PASS}" > $TARGET_PGPASSFILE
        chmod 600 $TARGET_PGPASSFILE
        export PGPASSFILE=$TARGET_PGPASSFILE
        
        if psql -h $TARGET_DB_HOST -p $TARGET_DB_PORT -U $TARGET_DB_USER -d $TARGET_DB_NAME -f $BACKUP_FILE; then
            echo -e "${GREEN}✅ Base de datos restaurada con éxito.${NC}"
            
            # Contar tablas en la base de datos para verificar
            TABLE_COUNT=$(psql -h $TARGET_DB_HOST -p $TARGET_DB_PORT -U $TARGET_DB_USER -d $TARGET_DB_NAME -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")
            echo -e "${GREEN}Número de tablas en la base de datos: ${TABLE_COUNT}${NC}"
            
            echo -e "\n${GREEN}¡Migración completada con éxito!${NC}"
            echo -e "${YELLOW}Ahora puedes conectarte a tu nueva base de datos:${NC}"
            echo -e "${GREEN}psql -h ${TARGET_DB_HOST} -p ${TARGET_DB_PORT} -U ${TARGET_DB_USER} -d ${TARGET_DB_NAME}${NC}"
        else
            echo -e "${RED}❌ Error al restaurar la base de datos.${NC}"
            echo -e "${YELLOW}Revisa los mensajes de error anteriores para más información.${NC}"
        fi
        
        # Eliminar archivo temporal de credenciales
        rm -f $TARGET_PGPASSFILE
    else
        echo -e "\n${GREEN}La copia de seguridad está lista para ser usada en otro momento.${NC}"
        echo -e "${YELLOW}Para restaurarla, copia el archivo ${BACKUP_FILE} a tu nueva instancia y ejecuta:${NC}"
        echo -e "${GREEN}psql -h <host> -p <puerto> -U <usuario> -d <base_datos> -f ${BACKUP_FILE}${NC}"
    fi
else
    echo -e "${RED}❌ Error al crear la copia de seguridad.${NC}"
    echo -e "${YELLOW}Verifica que la información de conexión sea correcta y que tengas permisos suficientes.${NC}"
fi

# Eliminar el archivo temporal de credenciales
rm -f $PGPASSFILE

echo -e "\n${GREEN}¡Proceso completado!${NC}"
#!/bin/bash

# Script para hacer una copia de seguridad de la base de datos y crear un archivo SQL
# que pueda ser importado en una base de datos local

# Colores para mensajes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Obtener fecha actual para el nombre del archivo
FECHA=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="./backups"
BACKUP_FILE="${BACKUP_DIR}/backup_${FECHA}.sql"

# Crear directorio de backups si no existe
mkdir -p $BACKUP_DIR

echo -e "${YELLOW}Iniciando copia de seguridad de la base de datos...${NC}"

# Verificar que DATABASE_URL existe
if [ -z "$DATABASE_URL" ]; then
    echo -e "${RED}Error: La variable DATABASE_URL no está definida.${NC}"
    echo -e "${YELLOW}Intentando buscar la configuración en otro lugar...${NC}"
    
    # Verificar si está en un archivo .env
    if [ -f .env ]; then
        echo -e "${GREEN}Encontrado archivo .env${NC}"
        export $(grep -v '^#' .env | xargs)
    fi
    
    # Verificar nuevamente
    if [ -z "$DATABASE_URL" ]; then
        echo -e "${RED}No se pudo encontrar la variable DATABASE_URL. Por favor defínela antes de ejecutar este script.${NC}"
        exit 1
    fi
fi

# Extraer los componentes de la URL de la base de datos
DB_USER=$(echo $DATABASE_URL | sed -E 's/^postgres:\/\/([^:]+):.*/\1/')
DB_PASS=$(echo $DATABASE_URL | sed -E 's/^postgres:\/\/[^:]+:([^@]+).*/\1/')
DB_HOST=$(echo $DATABASE_URL | sed -E 's/^postgres:\/\/[^@]+@([^:]+):.*/\1/')
DB_PORT=$(echo $DATABASE_URL | sed -E 's/^postgres:\/\/[^:]+:[^@]+@[^:]+:([0-9]+).*/\1/')
DB_NAME=$(echo $DATABASE_URL | sed -E 's/^postgres:\/\/[^:]+:[^@]+@[^:]+:[0-9]+\/([^?]+).*/\1/')

echo -e "${GREEN}Información de conexión:${NC}"
echo -e "- Host: ${DB_HOST}"
echo -e "- Puerto: ${DB_PORT}"
echo -e "- Base de datos: ${DB_NAME}"
echo -e "- Usuario: ${DB_USER}"

# Crear archivo de credenciales temporales para pg_dump
PGPASSFILE=$(mktemp)
echo "${DB_HOST}:${DB_PORT}:${DB_NAME}:${DB_USER}:${DB_PASS}" > $PGPASSFILE
chmod 600 $PGPASSFILE
export PGPASSFILE

# Ejecutar pg_dump para crear la copia de seguridad
echo -e "${YELLOW}Creando copia de seguridad en ${BACKUP_FILE}...${NC}"

# Usar pg_dump con opciones para crear un archivo de backup completo y compatible
pg_dump -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} \
    --format=plain \
    --no-owner \
    --no-acl \
    --clean \
    --if-exists \
    --create \
    > ${BACKUP_FILE}

# Verificar si pg_dump se ejecutó correctamente
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Copia de seguridad creada con éxito en: ${BACKUP_FILE}${NC}"
    
    # Añadir instrucciones al archivo para facilitar la restauración
    echo -e "\n-- INSTRUCCIONES PARA RESTAURAR ESTA BASE DE DATOS LOCALMENTE:" >> ${BACKUP_FILE}
    echo -e "-- 1. Crea una base de datos vacía: CREATE DATABASE ${DB_NAME};" >> ${BACKUP_FILE}
    echo -e "-- 2. Conéctate a la base de datos: \\c ${DB_NAME}" >> ${BACKUP_FILE}
    echo -e "-- 3. Ejecuta este archivo: psql -d ${DB_NAME} -f ${BACKUP_FILE}" >> ${BACKUP_FILE}
    echo -e "-- 4. O simplemente: psql -f ${BACKUP_FILE} postgres" >> ${BACKUP_FILE}
    
    # Mostrar tamaño del archivo
    FILE_SIZE=$(du -h ${BACKUP_FILE} | cut -f1)
    echo -e "${GREEN}Tamaño del archivo de backup: ${FILE_SIZE}${NC}"
    
    echo -e "\n${YELLOW}Instrucciones para usar esta copia de seguridad en tu base de datos local:${NC}"
    echo -e "${GREEN}1.${NC} Copia este archivo a tu computadora local"
    echo -e "${GREEN}2.${NC} Crea una base de datos vacía en PostgreSQL:"
    echo -e "   ${YELLOW}CREATE DATABASE ${DB_NAME};${NC}"
    echo -e "${GREEN}3.${NC} Importa el archivo con este comando:"
    echo -e "   ${YELLOW}psql -d ${DB_NAME} -f ${BACKUP_FILE}${NC}"
    echo -e "   O simplemente:"
    echo -e "   ${YELLOW}psql -f ${BACKUP_FILE} postgres${NC}"
else
    echo -e "${RED}❌ Error al crear la copia de seguridad.${NC}"
fi

# Eliminar el archivo temporal de credenciales
rm -f $PGPASSFILE

echo -e "\n${GREEN}¡Proceso completado!${NC}"
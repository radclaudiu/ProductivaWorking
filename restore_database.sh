#!/bin/bash

# Script para restaurar una copia de seguridad de la base de datos en un entorno local
# Complemento del script backup_database.sh

# Colores para mensajes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Directorio donde se almacenan los backups
BACKUP_DIR="./backups"

# Verificar que el directorio de backups existe
if [ ! -d "$BACKUP_DIR" ]; then
    echo -e "${RED}Error: El directorio de backups no existe.${NC}"
    echo -e "${YELLOW}Primero debes ejecutar backup_database.sh para crear una copia de seguridad.${NC}"
    exit 1
fi

# Verificar que hay archivos de backup disponibles
BACKUP_COUNT=$(ls -1 ${BACKUP_DIR}/*.sql 2>/dev/null | wc -l)
if [ $BACKUP_COUNT -eq 0 ]; then
    echo -e "${RED}Error: No se encontraron archivos de backup en ${BACKUP_DIR}${NC}"
    echo -e "${YELLOW}Primero debes ejecutar backup_database.sh para crear una copia de seguridad.${NC}"
    exit 1
fi

# Listar backups disponibles
echo -e "${GREEN}Backups disponibles:${NC}"
ls -1t ${BACKUP_DIR}/*.sql | nl

# Solicitar al usuario que elija un backup
echo -e "${YELLOW}Ingresa el número del backup que deseas restaurar:${NC}"
read BACKUP_NUMBER

# Obtener la ruta del backup seleccionado
SELECTED_BACKUP=$(ls -1t ${BACKUP_DIR}/*.sql | sed -n "${BACKUP_NUMBER}p")

if [ -z "$SELECTED_BACKUP" ]; then
    echo -e "${RED}Error: Selección inválida.${NC}"
    exit 1
fi

echo -e "${GREEN}Has seleccionado: ${SELECTED_BACKUP}${NC}"

# Solicitar información de conexión para la base de datos local
echo -e "${YELLOW}Ingresa la información de tu base de datos local:${NC}"
echo -e "${YELLOW}Nombre de la base de datos (dejar en blanco para usar 'productiva'):${NC}"
read DB_NAME
DB_NAME=${DB_NAME:-productiva}

echo -e "${YELLOW}Host (dejar en blanco para usar 'localhost'):${NC}"
read DB_HOST
DB_HOST=${DB_HOST:-localhost}

echo -e "${YELLOW}Puerto (dejar en blanco para usar '5432'):${NC}"
read DB_PORT
DB_PORT=${DB_PORT:-5432}

echo -e "${YELLOW}Usuario (dejar en blanco para usar el usuario actual '$(whoami)'):${NC}"
read DB_USER
DB_USER=${DB_USER:-$(whoami)}

echo -e "${YELLOW}¿Crear la base de datos si no existe? (y/n, por defecto: y):${NC}"
read CREATE_DB
CREATE_DB=${CREATE_DB:-y}

# Resumen de la configuración
echo -e "\n${GREEN}Resumen de la restauración:${NC}"
echo -e "- Archivo de backup: ${SELECTED_BACKUP}"
echo -e "- Base de datos: ${DB_NAME}"
echo -e "- Host: ${DB_HOST}"
echo -e "- Puerto: ${DB_PORT}"
echo -e "- Usuario: ${DB_USER}"
echo -e "- Crear base de datos: ${CREATE_DB}"

echo -e "\n${YELLOW}¿Deseas continuar con esta configuración? (y/n):${NC}"
read CONFIRM

if [ "$CONFIRM" != "y" ]; then
    echo -e "${RED}Restauración cancelada.${NC}"
    exit 0
fi

# Crear la base de datos si es necesario
if [ "$CREATE_DB" = "y" ]; then
    echo -e "${YELLOW}Creando base de datos ${DB_NAME}...${NC}"
    if PGPASSWORD=$DB_PASS psql -h $DB_HOST -p $DB_PORT -U $DB_USER -c "CREATE DATABASE $DB_NAME;" postgres; then
        echo -e "${GREEN}Base de datos creada con éxito.${NC}"
    else
        echo -e "${YELLOW}No se pudo crear la base de datos. Es posible que ya exista o no tengas permisos suficientes.${NC}"
        
        echo -e "${YELLOW}¿Deseas continuar con la restauración de todas formas? (y/n):${NC}"
        read CONTINUE
        
        if [ "$CONTINUE" != "y" ]; then
            echo -e "${RED}Restauración cancelada.${NC}"
            exit 0
        fi
    fi
fi

# Restaurar la base de datos
echo -e "${YELLOW}Restaurando la base de datos desde ${SELECTED_BACKUP}...${NC}"
echo -e "${YELLOW}Este proceso puede tomar varios minutos dependiendo del tamaño de la copia de seguridad.${NC}"

if PGPASSWORD=$DB_PASS psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f $SELECTED_BACKUP; then
    echo -e "${GREEN}✅ Base de datos restaurada con éxito.${NC}"
    
    # Contar tablas en la base de datos para verificar
    TABLE_COUNT=$(PGPASSWORD=$DB_PASS psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")
    echo -e "${GREEN}Número de tablas en la base de datos: ${TABLE_COUNT}${NC}"
    
    echo -e "\n${GREEN}¡Restauración completada con éxito!${NC}"
    echo -e "${YELLOW}Ahora puedes conectarte a tu base de datos local:${NC}"
    echo -e "${GREEN}psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME}${NC}"
else
    echo -e "${RED}❌ Error al restaurar la base de datos.${NC}"
    echo -e "${YELLOW}Revisa los mensajes de error anteriores para más información.${NC}"
fi
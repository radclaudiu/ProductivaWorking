#!/bin/bash

# ============================================================================
# Script para crear un backup ejecutable rápido (solo tablas esenciales)
# ============================================================================

# Colores para mensajes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Obtener fecha y hora para el nombre del archivo
DATE_STAMP=$(date +%Y%m%d_%H%M%S)
EXECUTABLE_BACKUP_FILE="productiva_quick_backup_${DATE_STAMP}.sh"

# Usar variables de entorno si existen
if [ -n "$DATABASE_URL" ]; then
  echo -e "${BLUE}Usando DATABASE_URL para la conexión${NC}"
else
  echo -e "${RED}No se encontró DATABASE_URL. Por favor configura esta variable de entorno.${NC}"
  exit 1
fi

echo -e "${GREEN}=== Creando backup rápido de PostgreSQL ===${NC}"

# Crear el script ejecutable sin SQL inicialmente
cat > "$EXECUTABLE_BACKUP_FILE" << HEADER
#!/bin/bash
# ============================================================================
# BACKUP EJECUTABLE RÁPIDO DE PRODUCTIVA
# ============================================================================
# Este archivo es un script ejecutable que contiene un backup de las tablas
# esenciales de la base de datos PostgreSQL.
#
# Modo de uso:
#
# 1. Ver información:
#    ./\$(basename \$0) --info
#
# 2. Restaurar (borrará la DB existente si se usa --force):
#    ./\$(basename \$0) --restore [--db DB_NAME] [--host HOST] [--user USER] [--force]
#
# 3. Extraer SQL:
#    ./\$(basename \$0) --extract [archivo.sql]
# ============================================================================
# Fecha de backup: $(date '+%Y-%m-%d %H:%M:%S')
# ============================================================================

# Colores para los mensajes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para mostrar información del backup
show_info() {
    echo -e "\${BLUE}================ INFORMACIÓN DEL BACKUP ================${NC}"
    echo -e "\${GREEN}Fecha de creación:${NC} $(date '+%Y-%m-%d %H:%M:%S')"
    echo -e "\${GREEN}Tamaño:${NC} \$(du -h "\$0" | cut -f1)"
    echo -e "\${GREEN}Tipo:${NC} Backup rápido (tablas esenciales)"
    echo
    echo -e "\${YELLOW}Para restaurar este backup, ejecute:${NC}"
    echo -e "  \$0 --restore [--db DB_NAME] [--host HOST] [--user USER] [--force]"
    echo
    echo -e "\${YELLOW}Para extraer el SQL sin ejecutarlo:${NC}"
    echo -e "  \$0 --extract [archivo_salida.sql]"
    echo
}

# Función para extraer el SQL sin ejecutarlo
extract_sql() {
    output_file="\$1"
    if [ -z "\$output_file" ]; then
        output_file="productiva_backup_extracted.sql"
    fi
    
    echo -e "\${BLUE}Extrayendo SQL a \${output_file}...${NC}"
    sed -n '/^__SQL_DUMP_BELOW__$/,/^__SQL_DUMP_ABOVE__$/p' "\$0" | sed '1d;\$d' > "\$output_file"
    
    if [ \$? -eq 0 ]; then
        echo -e "\${GREEN}SQL extraído correctamente a \${output_file}${NC}"
        echo -e "Tamaño del archivo: \$(du -h "\$output_file" | cut -f1)"
    else
        echo -e "\${RED}Error al extraer SQL${NC}"
        exit 1
    fi
}

# Función para restaurar el backup
restore_backup() {
    DB_NAME="productiva"
    DB_HOST="localhost"
    DB_PORT="5432"
    DB_USER="postgres"
    FORCE=0
    
    # Procesar argumentos
    while [[ "\$#" -gt 0 ]]; do
        case \$1 in
            --db) DB_NAME="\$2"; shift ;;
            --host) DB_HOST="\$2"; shift ;;
            --port) DB_PORT="\$2"; shift ;;
            --user) DB_USER="\$2"; shift ;;
            --force) FORCE=1 ;;
            *) echo "Opción desconocida: \$1"; exit 1 ;;
        esac
        shift
    done
    
    echo -e "\${BLUE}================== RESTAURACIÓN ====================${NC}"
    echo -e "\${GREEN}Base de datos destino:${NC} \$DB_NAME"
    echo -e "\${GREEN}Servidor destino:${NC} \$DB_HOST:\$DB_PORT"
    echo -e "\${GREEN}Usuario:${NC} \$DB_USER"
    echo
    
    # Verificar si psql está disponible
    if ! command -v psql &> /dev/null; then
        echo -e "\${RED}Error: psql no está instalado${NC}"
        exit 1
    fi
    
    # Preguntar contraseña
    read -s -p "Ingrese la contraseña para el usuario \$DB_USER: " DB_PASSWORD
    echo ""
    export PGPASSWORD="\$DB_PASSWORD"
    
    # Verificar si se puede conectar al servidor
    if ! psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "SELECT 1" postgres &> /dev/null; then
        echo -e "\${RED}Error al conectar con el servidor PostgreSQL${NC}"
        exit 1
    fi
    
    # Verificar si la base de datos ya existe
    if psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -lqt | grep -w "\$DB_NAME" &> /dev/null; then
        if [ \$FORCE -eq 1 ]; then
            echo -e "\${YELLOW}La base de datos '\$DB_NAME' ya existe. Se eliminará...${NC}"

            # Cerrar conexiones activas
            echo -e "Cerrando conexiones activas..."
            psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "
                SELECT pg_terminate_backend(pg_stat_activity.pid) 
                FROM pg_stat_activity 
                WHERE pg_stat_activity.datname = '\$DB_NAME'
                AND pid <> pg_backend_pid();" postgres &> /dev/null
            
            sleep 2

            # Eliminar la base de datos
            if ! psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "DROP DATABASE IF EXISTS \\"\$DB_NAME\\";" postgres; then
                echo -e "\${RED}Error al eliminar la base de datos existente.${NC}"
                exit 1
            fi
            echo -e "\${GREEN}Base de datos eliminada exitosamente.${NC}"
        else
            echo -e "\${RED}Error: La base de datos '\$DB_NAME' ya existe.${NC}"
            echo "Use --force para sobrescribirla o elija otro nombre con --db"
            exit 1
        fi
    fi
    
    # Crear base de datos
    echo -e "\${BLUE}Creando base de datos '\$DB_NAME'...${NC}"
    if ! psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "CREATE DATABASE \\"\$DB_NAME\\" WITH ENCODING='UTF8';" postgres; then
        echo -e "\${RED}Error al crear la base de datos${NC}"
        exit 1
    fi
    
    # Extraer SQL a un archivo temporal
    echo -e "\${BLUE}Extrayendo SQL...${NC}"
    TEMP_SQL=\$(mktemp)
    sed -n '/^__SQL_DUMP_BELOW__$/,/^__SQL_DUMP_ABOVE__$/p' "\$0" | sed '1d;\$d' > "\$TEMP_SQL"
    
    # Restaurar el backup
    echo -e "\${BLUE}Restaurando datos a la base de datos '\$DB_NAME'...${NC}"
    if ! psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -d "\$DB_NAME" -f "\$TEMP_SQL"; then
        echo -e "\${RED}Error al restaurar la base de datos${NC}"
        rm -f "\$TEMP_SQL"
        exit 1
    fi
    
    rm -f "\$TEMP_SQL"
    echo -e "\${GREEN}Restauración completada exitosamente${NC}"
}

# Procesar argumentos de línea de comandos
case "\$1" in
    --info)
        show_info
        ;;
    --extract)
        if [ -n "\$2" ] && [[ "\$2" != --* ]]; then
            extract_sql "\$2"
        else
            extract_sql
        fi
        ;;
    --restore)
        shift
        restore_backup "\$@"
        ;;
    *)
        echo "Uso: \$0 [--info|--extract|--restore]"
        echo "Opciones de restauración: [--db DB_NAME] [--host HOST] [--user USER] [--force]"
        exit 1
        ;;
esac

exit 0

__SQL_DUMP_BELOW__
HEADER

# Ejecutar pg_dump para las tablas esenciales directamente en el archivo
echo -e "${BLUE}Exportando tablas esenciales...${NC}"

# Lista de tablas esenciales que queremos incluir
ESSENTIAL_TABLES=(
  "users"
  "companies"
  "employees"
  "locations"
  "checkpoints"
  "checkpoint_records"
  "checkpoint_original_records"
)

# Crear la lista de opciones para pg_dump
TABLE_OPTIONS=""
for table in "${ESSENTIAL_TABLES[@]}"; do
  TABLE_OPTIONS="$TABLE_OPTIONS --table=public.$table"
done

# Ejecutar pg_dump solo para las tablas especificadas
pg_dump --format=plain --no-owner --no-acl $TABLE_OPTIONS >> "$EXECUTABLE_BACKUP_FILE"

# Verificar si pg_dump fue exitoso
if [ $? -ne 0 ]; then
  echo -e "${RED}Error al exportar las tablas.${NC}"
  rm -f "$EXECUTABLE_BACKUP_FILE"
  exit 1
fi

# Añadir el marcador de fin de SQL
echo "__SQL_DUMP_ABOVE__" >> "$EXECUTABLE_BACKUP_FILE"

# Hacer el script ejecutable
chmod +x "$EXECUTABLE_BACKUP_FILE"

echo -e "${GREEN}Backup rápido creado: $EXECUTABLE_BACKUP_FILE${NC}"
echo -e "${GREEN}Tamaño del archivo: $(du -h "$EXECUTABLE_BACKUP_FILE" | cut -f1)${NC}"
echo -e ""
echo -e "Puede ejecutar este script de las siguientes maneras:"
echo -e ""
echo -e "${BLUE}Ver información:${NC}"
echo -e "  ./$EXECUTABLE_BACKUP_FILE --info"
echo -e ""
echo -e "${BLUE}Restaurar (borrará la DB existente con --force):${NC}"
echo -e "  ./$EXECUTABLE_BACKUP_FILE --restore --force"
echo -e ""
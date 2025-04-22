#!/bin/bash

# =============================================================================
# Script para crear un backup completo de PostgreSQL con verificación de tamaño
# =============================================================================

# Colores para mensajes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Obtener fecha y hora para el nombre del archivo
DATE_STAMP=$(date +%Y%m%d_%H%M%S)
SQL_BACKUP_FILE="productiva_backup_${DATE_STAMP}.sql"
EXECUTABLE_BACKUP_FILE="productiva_backup_executable_${DATE_STAMP}.sh"

# Usar variables de entorno si existen
if [ -n "$DATABASE_URL" ]; then
  echo -e "${BLUE}Usando DATABASE_URL para la conexión${NC}"
  # No necesitamos extraer los componentes, pg_dump puede usar DATABASE_URL directamente
else
  # Usar variables de entorno de PostgreSQL si están definidas
  if [ -n "$PGDATABASE" ] && [ -n "$PGHOST" ] && [ -n "$PGUSER" ]; then
    echo -e "${BLUE}Usando variables PGDATABASE, PGHOST, etc.${NC}"
  else
    # Solicitar información de conexión
    echo -e "${YELLOW}Por favor ingrese la información de conexión:${NC}"
    
    # Solicitar host
    read -p "Host (default: localhost): " DB_HOST
    DB_HOST=${DB_HOST:-localhost}
    export PGHOST="$DB_HOST"
    
    # Solicitar puerto
    read -p "Puerto (default: 5432): " DB_PORT
    DB_PORT=${DB_PORT:-5432}
    export PGPORT="$DB_PORT"
    
    # Solicitar nombre de base de datos
    read -p "Nombre de base de datos: " DB_NAME
    if [ -z "$DB_NAME" ]; then
      echo -e "${RED}Error: Debe especificar un nombre de base de datos${NC}"
      exit 1
    fi
    export PGDATABASE="$DB_NAME"
    
    # Solicitar usuario
    read -p "Usuario (default: postgres): " DB_USER
    DB_USER=${DB_USER:-postgres}
    export PGUSER="$DB_USER"
    
    # Solicitar contraseña
    read -s -p "Contraseña: " DB_PASSWORD
    echo ""
    if [ -n "$DB_PASSWORD" ]; then
      export PGPASSWORD="$DB_PASSWORD"
    fi
  fi
fi

echo -e "${GREEN}=== Creando backup completo de PostgreSQL ===${NC}"

# Paso 1: Crear un backup SQL plano completo
echo -e "${BLUE}Exportando la base de datos a $SQL_BACKUP_FILE...${NC}"
pg_dump --format=plain --no-owner --no-acl > "$SQL_BACKUP_FILE"

# Verificar si pg_dump fue exitoso
if [ $? -ne 0 ]; then
  echo -e "${RED}Error al exportar la base de datos.${NC}"
  exit 1
fi

# Verificar el tamaño del archivo
FILE_SIZE=$(du -h "$SQL_BACKUP_FILE" | cut -f1)
FILE_SIZE_BYTES=$(du -b "$SQL_BACKUP_FILE" | cut -f1)

echo -e "${GREEN}Backup SQL creado exitosamente: $SQL_BACKUP_FILE${NC}"
echo -e "Tamaño del archivo: $FILE_SIZE"

# Verificar si el archivo es demasiado pequeño
MIN_SIZE_BYTES=10000 # 10 KB como mínimo
if [ "$FILE_SIZE_BYTES" -lt "$MIN_SIZE_BYTES" ]; then
  echo -e "${YELLOW}Advertencia: El archivo de backup parece demasiado pequeño (menos de 10 KB).${NC}"
  echo -e "${YELLOW}Esto puede indicar que no se exportaron todos los datos.${NC}"
  
  echo -e "${BLUE}Contando tablas y registros para verificar...${NC}"
  TABLE_COUNT=$(grep -c "CREATE TABLE" "$SQL_BACKUP_FILE")
  RECORD_COUNT=$(grep -c "INSERT INTO" "$SQL_BACKUP_FILE")
  
  echo -e "Tablas encontradas: $TABLE_COUNT"
  echo -e "Registros (INSERT) encontrados: $RECORD_COUNT"
  
  if [ "$TABLE_COUNT" -eq 0 ] || [ "$RECORD_COUNT" -eq 0 ]; then
    echo -e "${RED}Error: El backup no contiene tablas o datos.${NC}"
    echo -e "${RED}Verifique sus credenciales y permisos de base de datos.${NC}"
    exit 1
  fi
else
  echo -e "${GREEN}El tamaño del archivo parece razonable.${NC}"
fi

# Paso 2: Crear un backup ejecutable
echo -e "${BLUE}Creando backup ejecutable $EXECUTABLE_BACKUP_FILE...${NC}"

# Crear el script ejecutable con el SQL incrustado
cat > "$EXECUTABLE_BACKUP_FILE" << HEADER
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
#    ./\$(basename \$0) --info
#
# 2. RESTAURAR (crea una nueva base de datos y restaura todo):
#    ./\$(basename \$0) --restore [--db DB_NAME] [--host HOST] [--port PORT] [--user USER]
#
# 3. EXTRAER SQL (solo extrae el SQL sin ejecutarlo):
#    ./\$(basename \$0) --extract [output_file.sql]
#
# Opciones:
#   --db NAME     Nombre de la base de datos (predeterminado: productiva)
#   --host HOST   Host del servidor PostgreSQL (predeterminado: localhost)
#   --port PORT   Puerto del servidor PostgreSQL (predeterminado: 5432)
#   --user USER   Usuario PostgreSQL (predeterminado: postgres)
#   --force       Sobrescribir la base de datos si ya existe
#   --help        Mostrar esta ayuda
# ============================================================================
# Fecha de backup: $(date '+%Y-%m-%d %H:%M:%S')
# Tamaño del backup SQL: $FILE_SIZE
# Tablas: $TABLE_COUNT
# Registros: $RECORD_COUNT
# ============================================================================

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
    echo -e "${GREEN}Tamaño del backup SQL:${NC} $(grep "# Tamaño del backup SQL:" "$0" | cut -d: -f2-)"
    echo -e "${GREEN}Tablas:${NC} $(grep "# Tablas:" "$0" | cut -d: -f2-)"
    echo -e "${GREEN}Registros:${NC} $(grep "# Registros:" "$0" | cut -d: -f2-)"
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
    output_file="\$1"
    if [ -z "\$output_file" ]; then
        output_file="productiva_backup_extracted.sql"
    fi
    
    echo -e "\${BLUE}Extrayendo SQL a \${output_file}...\${NC}"
    
    # Extraer la parte SQL del script (después de la línea __SQL_DUMP_BELOW__)
    sed -n '/^__SQL_DUMP_BELOW__$/,/^__SQL_DUMP_ABOVE__$/p' "\$0" | sed '1d;\$d' > "\$output_file"
    
    if [ \$? -eq 0 ]; then
        echo -e "\${GREEN}SQL extraído correctamente a \${output_file}\${NC}"
        echo -e "Tamaño del archivo: \$(du -h "\$output_file" | cut -f1)"
    else
        echo -e "\${RED}Error al extraer SQL\${NC}"
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
    
    echo -e "\${BLUE}================== RESTAURACIÓN ====================\${NC}"
    echo -e "\${GREEN}Base de datos destino:\${NC} \$DB_NAME"
    echo -e "\${GREEN}Servidor destino:\${NC} \$DB_HOST:\$DB_PORT"
    echo -e "\${GREEN}Usuario:\${NC} \$DB_USER"
    echo
    
    # Verificar si psql está disponible
    if ! command -v psql &> /dev/null; then
        echo -e "\${RED}Error: psql no está instalado o no está en el PATH\${NC}"
        echo "Por favor, instale PostgreSQL client utilities"
        exit 1
    fi
    
    # Preguntar contraseña
    read -s -p "Ingrese la contraseña para el usuario \$DB_USER: " DB_PASSWORD
    echo ""
    
    # Usar variable PGPASSWORD para evitar preguntar contraseña múltiples veces
    export PGPASSWORD="\$DB_PASSWORD"
    
    # Verificar si se puede conectar al servidor
    if ! psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "SELECT 1" postgres &> /dev/null; then
        echo -e "\${RED}Error al conectar con el servidor PostgreSQL\${NC}"
        echo "Verifique los parámetros de conexión y que el servidor esté en ejecución"
        exit 1
    fi
    
    # Verificar si la base de datos ya existe
    if psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -lqt | grep -w "\$DB_NAME" &> /dev/null; then
        if [ \$FORCE -eq 1 ]; then
            echo -e "\${YELLOW}La base de datos '\$DB_NAME' ya existe. Se eliminará...\${NC}"

            # Intentar cerrar conexiones activas antes de eliminar la base de datos
            echo -e "Cerrando conexiones activas a la base de datos '\$DB_NAME'..."
            psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "
                SELECT pg_terminate_backend(pg_stat_activity.pid) 
                FROM pg_stat_activity 
                WHERE pg_stat_activity.datname = '\$DB_NAME'
                AND pid <> pg_backend_pid();" postgres &> /dev/null
            
            # Esperar un momento para que las conexiones se cierren
            sleep 2

            # Intentar eliminar la base de datos
            if ! psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "DROP DATABASE IF EXISTS \\"\$DB_NAME\\";" postgres; then
                echo -e "\${RED}Error al eliminar la base de datos existente.\${NC}"
                echo -e "\${YELLOW}La base de datos puede estar en uso por otras aplicaciones.\${NC}"
                echo -e "Sugerencias:"
                echo -e " 1. Detenga todas las aplicaciones que estén usando la base de datos"
                echo -e " 2. Intente nuevamente el comando de restauración"
                echo -e " 3. Si el problema persiste, puede crear una base de datos con otro nombre"
                exit 1
            fi
            echo -e "\${GREEN}Base de datos eliminada exitosamente.\${NC}"
        else
            echo -e "\${RED}Error: La base de datos '\$DB_NAME' ya existe.\${NC}"
            echo "Use --force para sobrescribirla o elija otro nombre con --db"
            exit 1
        fi
    fi
    
    # Crear base de datos
    echo -e "\${BLUE}Creando base de datos '\$DB_NAME'...\${NC}"
    if ! psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "CREATE DATABASE \\"\$DB_NAME\\" WITH ENCODING='UTF8';" postgres; then
        echo -e "\${RED}Error al crear la base de datos\${NC}"
        exit 1
    fi
    
    # Extraer SQL a un archivo temporal
    echo -e "\${BLUE}Preparando SQL para restauración...\${NC}"
    temp_sql=\$(mktemp)
    sed -n '/^__SQL_DUMP_BELOW__$/,/^__SQL_DUMP_ABOVE__$/p' "\$0" | sed '1d;\$d' > "\$temp_sql"
    
    # Restaurar la base de datos
    echo -e "\${BLUE}Restaurando backup a '\$DB_NAME'...\${NC}"
    if psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -f "\$temp_sql" "\$DB_NAME"; then
        echo -e "\${GREEN}¡Backup restaurado exitosamente!\${NC}"
    else
        echo -e "\${RED}Error al restaurar el backup\${NC}"
        rm -f "\$temp_sql"
        exit 1
    fi
    
    # Limpiar archivos temporales
    rm -f "\$temp_sql"
    
    echo -e "\${GREEN}Restauración completada exitosamente.\${NC}"
    echo -e "\${BLUE}=================== ESTADÍSTICAS ===================\${NC}"
    echo -e "Tablas restauradas: \$(psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "SELECT COUNT(*) FROM pg_tables WHERE schemaname='public';" -t "\$DB_NAME" | tr -d ' ')"
    
    echo -e "Información de registros por tabla:"
    # Usar una consulta más simple y compatible para evitar errores de sintaxis
    psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "
        SELECT 
            tablename, 
            pg_size_pretty(pg_relation_size(quote_ident(tablename)::text)) as size,
            pg_total_relation_size(quote_ident(tablename)::text) as total_size
        FROM 
            pg_tables
        WHERE 
            schemaname = 'public'
        ORDER BY 
            total_size DESC;
    " "\$DB_NAME"
    
    # Mostrar las 10 tablas con más registros (se calcula dinámicamente)
    echo -e "\nTablas con más registros:"
    psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -t -c "
        SELECT 'SELECT '''||tablename||''' AS tabla, COUNT(*) AS registros FROM '||tablename||' UNION ALL'
        FROM pg_tables 
        WHERE schemaname='public'
        ORDER BY tablename
    " "\$DB_NAME" | head -n 10 > /tmp/count_query.sql
    
    # Ajustar la última línea para quitar el UNION ALL
    sed -i '\$ s/UNION ALL$//' /tmp/count_query.sql
    
    # Añadir ORDER BY
    echo "ORDER BY registros DESC;" >> /tmp/count_query.sql
    
    # Ejecutar la consulta
    psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "$(cat /tmp/count_query.sql)" "\$DB_NAME"
    
    # Limpiar archivos temporales
    rm -f /tmp/count_query.sql
}

# Ejecutar esta parte si se usa el comando --help
if [[ "\$1" == "--help" || "\$1" == "-h" ]]; then
    show_info
    exit 0
fi

# Ejecutar esta parte si se usa el comando --info
if [[ "\$1" == "--info" ]]; then
    show_info
    exit 0
fi

# Ejecutar esta parte si se usa el comando --extract
if [[ "\$1" == "--extract" ]]; then
    output_file="\$2"
    extract_sql "\$output_file"
    exit 0
fi

# Ejecutar esta parte si se usa el comando --restore
if [[ "\$1" == "--restore" ]]; then
    shift
    restore_backup "\$@"
    exit 0
fi

# Si no se especificó ningún comando, mostrar la ayuda
if [[ \$# -eq 0 ]]; then
    show_info
    exit 0
fi

echo "Comando desconocido: \$1"
echo "Use --help para ver las opciones disponibles."
exit 1

# Dump de la base de datos PostgreSQL comienza aquí
__SQL_DUMP_BELOW__
HEADER

# Añadir el contenido SQL al archivo ejecutable
cat "$SQL_BACKUP_FILE" >> "$EXECUTABLE_BACKUP_FILE"

# Finalizar el archivo ejecutable
echo "__SQL_DUMP_ABOVE__" >> "$EXECUTABLE_BACKUP_FILE"

# Hacer el archivo ejecutable
chmod +x "$EXECUTABLE_BACKUP_FILE"

# Mostrar información del backup creado
echo -e "${GREEN}Backup ejecutable creado exitosamente: ${EXECUTABLE_BACKUP_FILE}${NC}"
echo "Tamaño del archivo: $(du -h "$EXECUTABLE_BACKUP_FILE" | cut -f1)"
echo ""
echo -e "${YELLOW}Para restaurar este backup, use:${NC}"
echo "  ./$EXECUTABLE_BACKUP_FILE --restore [--db NOMBRE_BD] [--host HOST] [--user USUARIO]"
echo ""
echo -e "${YELLOW}Para ver información del backup:${NC}"
echo "  ./$EXECUTABLE_BACKUP_FILE --info"
echo ""
echo -e "${YELLOW}Para extraer el SQL sin ejecutarlo:${NC}"
echo "  ./$EXECUTABLE_BACKUP_FILE --extract [archivo_salida.sql]"

# Crear un archivo informativo
INFO_FILE="backup_info_${DATE_STAMP}.txt"
echo "Información del backup" > "$INFO_FILE"
echo "====================" >> "$INFO_FILE"
echo "Fecha: $(date '+%Y-%m-%d %H:%M:%S')" >> "$INFO_FILE"
echo "Archivo SQL: $SQL_BACKUP_FILE ($(du -h "$SQL_BACKUP_FILE" | cut -f1))" >> "$INFO_FILE"
echo "Archivo Ejecutable: $EXECUTABLE_BACKUP_FILE ($(du -h "$EXECUTABLE_BACKUP_FILE" | cut -f1))" >> "$INFO_FILE"
echo "Tablas: $TABLE_COUNT" >> "$INFO_FILE"
echo "Registros: $RECORD_COUNT" >> "$INFO_FILE"
echo "" >> "$INFO_FILE"
echo "Para restaurar, ejecute:" >> "$INFO_FILE"
echo "  ./$EXECUTABLE_BACKUP_FILE --restore" >> "$INFO_FILE"

echo -e "${BLUE}Se ha creado un archivo informativo: ${INFO_FILE}${NC}"

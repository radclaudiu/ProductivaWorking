#!/bin/bash
# =============================================================================
# SCRIPT SIMPLIFICADO PARA CREAR BACKUP EJECUTABLE DE PRODUCTIVA
# =============================================================================
# Este script genera un backup confiable que usa comandos estándar de PostgreSQL
# y minimiza las manipulaciones de texto para evitar problemas de sintaxis.
#
# Características principales:
# 1. Separa la exportación de estructura y datos para evitar problemas con tipos enum
# 2. Utiliza opciones estándar de pg_dump sin manipulación compleja
# 3. Genera un script ejecutable para fácil restauración
# 4. Maneja nombres de bases de datos personalizados durante la restauración
#
# Versión: 1.1.0 (2025-04-24)
# Autor: Daniel Clemente
#

# Colores para mensajes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Obtener fecha y hora para el nombre del archivo
DATE_STAMP=$(date +%Y%m%d_%H%M%S)
DUMP_FILE="productiva_backup_${DATE_STAMP}.sql"
BACKUP_SCRIPT="productiva_backup_${DATE_STAMP}.sh"

# Verificar argumentos
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
  echo "Script simplificado para crear backup de Productiva"
  echo ""
  echo "Uso: $0 [DBNAME] [HOST] [PORT] [USER]"
  echo ""
  echo "Parámetros (opcionales):"
  echo "  DBNAME  Nombre de la base de datos (predeterminado: productiva)"
  echo "  HOST    Host del servidor (predeterminado: localhost)"
  echo "  PORT    Puerto del servidor (predeterminado: 5432)"
  echo "  USER    Usuario (predeterminado: postgres)"
  echo ""
  echo "Ejemplo: $0 productiva localhost 5432 postgres"
  exit 0
fi

# Parámetros de conexión
DB_NAME=${1:-${PGDATABASE:-"productiva"}}
DB_HOST=${2:-${PGHOST:-"localhost"}}
DB_PORT=${3:-${PGPORT:-"5432"}}
DB_USER=${4:-${PGUSER:-"postgres"}}
DB_PASSWORD=${PGPASSWORD:-""}

# Usar variables de entorno si están disponibles
if [ -n "$DATABASE_URL" ]; then
  echo -e "${BLUE}Usando DATABASE_URL para conexión${NC}"
  if [[ "$DATABASE_URL" =~ postgres://([^:]+):([^@]+)@([^:]+):([0-9]+)/([^?]+) ]]; then
    DB_USER="${BASH_REMATCH[1]}"
    DB_PASSWORD="${BASH_REMATCH[2]}"
    DB_HOST="${BASH_REMATCH[3]}"
    DB_PORT="${BASH_REMATCH[4]}"
    DB_NAME="${BASH_REMATCH[5]}"
  fi
fi

# Solicitar contraseña si no está definida
if [ -z "$DB_PASSWORD" ]; then
  echo -n "Ingrese contraseña para $DB_USER: "
  read -s DB_PASSWORD
  echo ""
fi

# Mostrar información de conexión
echo -e "${BLUE}=== Creando backup de PostgreSQL ===${NC}"
echo -e "Base de datos: ${GREEN}$DB_NAME${NC}"
echo -e "Servidor: ${GREEN}$DB_HOST:$DB_PORT${NC}"
echo -e "Usuario: ${GREEN}$DB_USER${NC}"
echo -e "Archivo SQL: ${GREEN}$DUMP_FILE${NC}"
echo -e "Script ejecutable: ${GREEN}$BACKUP_SCRIPT${NC}"
echo ""

# Usar temporalmente archivo pgpass para evitar prompt de contraseña
PGPASS_FILE="${HOME}/.pgpass"
PGPASS_EXISTS=0

if [ -f "$PGPASS_FILE" ]; then
  PGPASS_EXISTS=1
  cp "$PGPASS_FILE" "${PGPASS_FILE}.bak"
fi

# Crear o modificar .pgpass
echo "${DB_HOST}:${DB_PORT}:${DB_NAME}:${DB_USER}:${DB_PASSWORD}" > "$PGPASS_FILE"
chmod 600 "$PGPASS_FILE"

# Obtener información de la base de datos
echo -e "${BLUE}Obteniendo información de la base de datos...${NC}"
TABLE_COUNT=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public'" | tr -d ' ')
RECORD_COUNT=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT SUM(n_live_tup) FROM pg_stat_user_tables" | tr -d ' ')
PG_VERSION=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SHOW server_version" | tr -d ' ')

echo -e "Tablas encontradas: ${GREEN}$TABLE_COUNT${NC}"
echo -e "Registros totales: ${GREEN}$RECORD_COUNT${NC}"
echo -e "Versión PostgreSQL: ${GREEN}$PG_VERSION${NC}"
echo ""

# Crear el dump SQL utilizando opciones estándar (sin manipulación compleja)
echo -e "${BLUE}Creando dump SQL...${NC}"
PGPASSWORD="$DB_PASSWORD" pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME" \
  --no-owner --no-privileges \
  --schema-only \
  --format=plain \
  --encoding=UTF8 \
  --no-comments > "$DUMP_FILE"

# Ahora añadir solo los datos (sin esquema)
echo "Exportando datos..."
PGPASSWORD="$DB_PASSWORD" pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME" \
  --no-owner --no-privileges \
  --data-only \
  --column-inserts \
  --inserts \
  --format=plain \
  --encoding=UTF8 \
  --no-comments >> "$DUMP_FILE"

if [ $? -ne 0 ]; then
  echo -e "${RED}Error al crear el dump SQL${NC}"
  
  # Restaurar .pgpass original si existía
  if [ $PGPASS_EXISTS -eq 1 ]; then
    mv "${PGPASS_FILE}.bak" "$PGPASS_FILE"
  else
    rm -f "$PGPASS_FILE"
  fi
  
  exit 1
fi

# Tamaño del dump SQL
DUMP_SIZE=$(du -h "$DUMP_FILE" | cut -f1)
echo -e "Dump SQL creado exitosamente: ${GREEN}$DUMP_SIZE${NC}"

# Crear el script ejecutable
echo -e "${BLUE}Creando script ejecutable para restauración...${NC}"

cat > "$BACKUP_SCRIPT" << SCRIPT_HEADER
#!/bin/bash
# =============================================================================
# BACKUP EJECUTABLE DE PRODUCTIVA (Versión Simplificada)
# =============================================================================
# Fecha de creación: $(date '+%Y-%m-%d %H:%M:%S')
# Base de datos origen: $DB_NAME
# Servidor origen: $DB_HOST:$DB_PORT
# Versión PostgreSQL: $PG_VERSION
# Tablas: $TABLE_COUNT
# Registros: $RECORD_COUNT
# Tamaño del SQL: $DUMP_SIZE
# =============================================================================
#
# Este script contiene un backup completo de PostgreSQL y comandos para restaurarlo.
#
# MODO DE USO:
#
# 1. Ver información (sin cambios):
#    ./\$(basename \$0) --info
#
# 2. Restaurar (requiere PostgreSQL):
#    ./\$(basename \$0) --restore [opciones]
#
#    Opciones de restauración:
#      --db NOMBRE     Nombre de base de datos (default: productiva)
#      --host HOST     Host del servidor (default: localhost) 
#      --port PUERTO   Puerto del servidor (default: 5432)
#      --user USUARIO  Usuario PostgreSQL (default: postgres)
#      --force         Sobrescribir la base de datos si existe
#
# 3. Extraer SQL (sin ejecutarlo):
#    ./\$(basename \$0) --extract [archivo.sql]
#

# Colores para mensajes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para mostrar información
show_info() {
  echo -e "\${BLUE}============ INFORMACIÓN DEL BACKUP ============\${NC}"
  echo -e "\${GREEN}Fecha de creación:\${NC} $(date '+%Y-%m-%d %H:%M:%S')"
  echo -e "\${GREEN}Base de datos origen:\${NC} $DB_NAME"
  echo -e "\${GREEN}Servidor origen:\${NC} $DB_HOST:$DB_PORT" 
  echo -e "\${GREEN}Versión PostgreSQL:\${NC} $PG_VERSION"
  echo -e "\${GREEN}Tablas:\${NC} $TABLE_COUNT"
  echo -e "\${GREEN}Registros:\${NC} $RECORD_COUNT"
  echo -e "\${GREEN}Tamaño SQL:\${NC} $DUMP_SIZE"
  echo ""
  echo -e "\${YELLOW}Para restaurar el backup:\${NC}"
  echo -e "  \$0 --restore [--db NOMBRE] [--host HOST] [--user USUARIO] [--force]"
  echo ""
  echo -e "\${YELLOW}Para extraer el SQL:\${NC}"
  echo -e "  \$0 --extract [archivo.sql]"
}

# Función para extraer el SQL
extract_sql() {
  output_file="\$1"
  if [ -z "\$output_file" ]; then
    output_file="productiva_backup_extracted.sql"
  fi
  
  echo -e "\${BLUE}Extrayendo SQL a \${output_file}...\${NC}"
  awk '/^__SQL_DUMP_BELOW__$/,/^__SQL_DUMP_ABOVE__$/' "\$0" | sed '1d;$d' > "\$output_file"
  
  if [ \$? -eq 0 ]; then
    echo -e "\${GREEN}SQL extraído correctamente: \$(du -h "\$output_file" | cut -f1)\${NC}"
  else
    echo -e "\${RED}Error al extraer SQL\${NC}"
    exit 1
  fi
}

# Función para restaurar el backup
restore_backup() {
  # Valores predeterminados
  DB_NAME="productiva"
  DB_HOST="localhost"
  DB_PORT="5432"
  DB_USER="postgres"
  FORCE=0
  
  # Procesar argumentos
  while [ "\$#" -gt 0 ]; do
    case "\$1" in
      --db) DB_NAME="\$2"; shift ;;
      --db=*) DB_NAME="\${1#*=}" ;;
      --host) DB_HOST="\$2"; shift ;;
      --host=*) DB_HOST="\${1#*=}" ;;
      --port) DB_PORT="\$2"; shift ;;
      --port=*) DB_PORT="\${1#*=}" ;;
      --user) DB_USER="\$2"; shift ;;
      --user=*) DB_USER="\${1#*=}" ;;
      --force) FORCE=1 ;;
      *) echo -e "\${RED}Opción desconocida: \$1\${NC}"; exit 1 ;;
    esac
    shift
  done
  
  echo -e "\${BLUE}============ RESTAURACIÓN ============\${NC}"
  echo -e "Base de datos: \${GREEN}\$DB_NAME\${NC}"
  echo -e "Servidor: \${GREEN}\$DB_HOST:\$DB_PORT\${NC}"
  echo -e "Usuario: \${GREEN}\$DB_USER\${NC}"
  echo ""
  
  # Verificar si la base de datos existe
  if psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -lqt | cut -d \| -f 1 | grep -qw "\$DB_NAME"; then
    if [ \$FORCE -eq 1 ]; then
      echo -e "\${YELLOW}La base de datos '\$DB_NAME' ya existe. Se eliminará...\${NC}"
      
      # Cerrar conexiones activas
      echo "Cerrando conexiones activas..."
      psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "
        SELECT pg_terminate_backend(pid) 
        FROM pg_stat_activity 
        WHERE datname = '\$DB_NAME' AND pid <> pg_backend_pid();
      " postgres
      
      # Eliminar base de datos
      psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "DROP DATABASE \"\$DB_NAME\";" postgres
      if [ \$? -ne 0 ]; then
        echo -e "\${RED}Error al eliminar la base de datos\${NC}"
        exit 1
      fi
    else
      echo -e "\${RED}Error: La base de datos '\$DB_NAME' ya existe\${NC}"
      echo "Use --force para sobrescribir la base de datos existente"
      exit 1
    fi
  fi
  
  # Crear la base de datos vacía
  echo "Creando nueva base de datos '\$DB_NAME'..."
  psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -c "CREATE DATABASE \"\$DB_NAME\" ENCODING='UTF8' LC_COLLATE='en_US.UTF-8' LC_CTYPE='en_US.UTF-8' TEMPLATE=template0;" postgres
  
  if [ \$? -ne 0 ]; then
    echo -e "\${RED}Error al crear la base de datos\${NC}"
    exit 1
  fi
  
  # Extraer SQL a archivo temporal
  temp_sql=\$(mktemp)
  awk '/^__SQL_DUMP_BELOW__$/,/^__SQL_DUMP_ABOVE__$/' "\$0" | sed '1d;$d' > "\$temp_sql"
  
  # Restaurar la base de datos
  echo -e "\${BLUE}Restaurando backup a '\$DB_NAME'...\${NC}"
  
  # Modificar el archivo SQL para reemplazar el nombre de la base de datos original con el nuevo
  echo "Adaptando el SQL para usar la base de datos '\$DB_NAME'..."
  # Eliminar CREATE DATABASE y \connect, ya que hemos creado la base de datos manualmente
  sed -i '/^CREATE DATABASE/d' "\$temp_sql"
  sed -i '/^\\connect/d' "\$temp_sql"
  sed -i '/^ALTER DATABASE/d' "\$temp_sql"
  
  # Eliminar opciones incompatibles
  sed -i '/locale_provider/d' "\$temp_sql"
  
  # Ejecutar el script SQL directamente en la base de datos creada
  echo "Restaurando estructura y datos..."
  psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -d "\$DB_NAME" -f "\$temp_sql"
  
  if [ \$? -eq 0 ]; then
    echo -e "\${GREEN}Backup restaurado exitosamente\${NC}"
  else
    echo -e "\${RED}Error al restaurar el backup\${NC}"
    rm -f "\$temp_sql"
    exit 1
  fi
  
  # Limpiar
  rm -f "\$temp_sql"
}

# Procesar argumentos
if [ \$# -eq 0 ]; then
  show_info
  exit 0
fi

case "\$1" in
  --info)
    show_info
    ;;
  --extract)
    shift
    extract_sql "\$1"
    ;;
  --restore)
    shift
    restore_backup "\$@"
    ;;
  --help)
    echo "Uso: \$0 [--info|--extract|--restore] [opciones]"
    echo "Ejecute \$0 sin argumentos para más información"
    ;;
  *)
    echo -e "\${RED}Opción desconocida: \$1\${NC}"
    echo "Uso: \$0 [--info|--extract|--restore] [opciones]"
    exit 1
    ;;
esac

exit 0

__SQL_DUMP_BELOW__
SCRIPT_HEADER

# Añadir el dump SQL al script ejecutable
cat "$DUMP_FILE" >> "$BACKUP_SCRIPT"

# Cerrar el script ejecutable
echo "__SQL_DUMP_ABOVE__" >> "$BACKUP_SCRIPT"

# Hacer ejecutable el script
chmod +x "$BACKUP_SCRIPT"

# Limpiar archivos temporales
rm -f "$DUMP_FILE"

# Restaurar .pgpass original si existía
if [ $PGPASS_EXISTS -eq 1 ]; then
  mv "${PGPASS_FILE}.bak" "$PGPASS_FILE"
else
  rm -f "$PGPASS_FILE"
fi

echo -e "${GREEN}¡Backup ejecutable creado correctamente!${NC}"
echo -e "Archivo: ${YELLOW}$BACKUP_SCRIPT${NC}"
echo -e "Tamaño: ${YELLOW}$(du -h "$BACKUP_SCRIPT" | cut -f1)${NC}"
echo ""
echo -e "Para ver información del backup, ejecute:"
echo -e "  ${YELLOW}./$BACKUP_SCRIPT --info${NC}"
echo ""
echo -e "Para restaurar el backup, ejecute:"
echo -e "  ${YELLOW}./$BACKUP_SCRIPT --restore [--db NOMBRE] [--host HOST] [--user USUARIO]${NC}"
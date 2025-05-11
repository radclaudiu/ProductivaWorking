#!/bin/bash

# =============================================================================
# Script para crear un backup completo de PostgreSQL con verificación de tamaño
# Actualizado: 02/05/2025 - Soporta nuevas tablas y columnas
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

# Verificar que existan todas las tablas críticas
echo -e "${BLUE}Verificando tablas en el backup...${NC}"

# Lista de tablas críticas del sistema que deben estar presentes
CRITICAL_TABLES=(
  "users" "companies" "employees" "locations" 
  "checkpoints" "checkpoint_records" "checkpoint_incidents" "checkpoint_original_records" "employee_contract_hours"
  "cash_registers" "cash_register_summaries" "cash_register_tokens"
  "expense_categories" "fixed_expenses" "monthly_expenses" "monthly_expense_summaries" "monthly_expense_tokens"
  "task_templates" "task_instances" "network_printers"
)

# Verificar cada tabla crítica
MISSING_TABLES=()
for table in "${CRITICAL_TABLES[@]}"; do
  if ! grep -q "CREATE TABLE public.${table}" "$SQL_BACKUP_FILE"; then
    MISSING_TABLES+=("$table")
    echo -e "${YELLOW}⚠️ Advertencia: No se encontró la tabla '${table}' en el backup${NC}"
  fi
done

# Informar sobre las tablas encontradas
FOUND_TABLES=$(grep -c "CREATE TABLE public" "$SQL_BACKUP_FILE")
echo -e "${GREEN}✓ Se encontraron $FOUND_TABLES tablas en el backup${NC}"

# Advertir si faltan tablas críticas
if [ ${#MISSING_TABLES[@]} -gt 0 ]; then
  echo -e "${YELLOW}⚠️ Atención: Faltan ${#MISSING_TABLES[@]} tablas críticas en el backup${NC}"
  echo -e "${YELLOW}  Esto puede indicar que el esquema de la base de datos está incompleto${NC}"
  echo -e "${YELLOW}  o que las tablas fueron agregadas después de este script.${NC}"
fi

# Verificar columnas para tablas críticas

# Verificar que la tabla 'companies' tenga el campo 'hourly_employee_cost'
if grep -q "CREATE TABLE public.companies" "$SQL_BACKUP_FILE"; then
  if ! grep -q "hourly_employee_cost" "$SQL_BACKUP_FILE"; then
    echo -e "${YELLOW}⚠️ Advertencia: No se encontró la columna 'hourly_employee_cost' en la tabla 'companies'${NC}"
    echo -e "${YELLOW}  Esta columna es necesaria para el módulo de Arqueos de Caja${NC}"
  fi
fi

# Verificar campos del módulo de IVA en la tabla 'cash_registers'
if grep -q "CREATE TABLE public.cash_registers" "$SQL_BACKUP_FILE"; then
  if ! grep -q "vat_percentage" "$SQL_BACKUP_FILE"; then
    echo -e "${YELLOW}⚠️ Advertencia: No se encontró la columna 'vat_percentage' en la tabla 'cash_registers'${NC}"
    echo -e "${YELLOW}  Esta columna es necesaria para el cálculo de IVA en Arqueos de Caja${NC}"
  fi
fi

# Verificar campos para la ventana horaria de cierre en la tabla 'checkpoints'
if grep -q "CREATE TABLE public.checkpoints" "$SQL_BACKUP_FILE"; then
  if ! grep -q "operation_start_time" "$SQL_BACKUP_FILE" || ! grep -q "operation_end_time" "$SQL_BACKUP_FILE"; then
    echo -e "${YELLOW}⚠️ Advertencia: No se encontraron las columnas de ventana horaria en la tabla 'checkpoints'${NC}"
    echo -e "${YELLOW}  Estas columnas son necesarias para el cierre automático programado${NC}"
  fi
fi

# Actualizar nombre del script ejecutable para incluir información sobre las tablas
if [ ${#MISSING_TABLES[@]} -gt 0 ]; then
  MISSING_SUFFIX="_faltantes_$(echo ${MISSING_TABLES[@]} | tr ' ' '_' | cut -c1-50)"
  NEW_EXECUTABLE_BACKUP_FILE="productiva_backup_executable_${DATE_STAMP}${MISSING_SUFFIX}.sh"
  echo -e "${YELLOW}Cambiando nombre del archivo de backup para indicar tablas faltantes: ${NEW_EXECUTABLE_BACKUP_FILE}${NC}"
  EXECUTABLE_BACKUP_FILE="$NEW_EXECUTABLE_BACKUP_FILE"
fi

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
# Actualizado para módulos: Arqueos de Caja, Gastos Mensuales, IVA y Ventana Horaria
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
    echo -e "${GREEN}Fecha de creación:${NC} $(date '+%Y-%m-%d %H:%M:%S')"
    echo -e "${GREEN}Tamaño:${NC} $(du -h "$0" | cut -f1)"
    echo -e "${GREEN}Módulos actualizados:${NC} Arqueos de Caja, Gastos Mensuales, IVA y Ventana Horaria"
    echo

    # Verificar módulos presentes extrayendo primero la parte SQL
    echo -e "${BLUE}Analizando módulos incluidos en este backup...${NC}"
    
    # Extraer SQL a un archivo temporal para analizar contenido
    temp_sql=\$(mktemp)
    sed -n '/^__SQL_DUMP_BELOW__$/,/^__SQL_DUMP_ABOVE__$/p' "\$0" | sed '1d;\$d' > "\$temp_sql"
    
    # Verificar sistema base
    if grep -q "CREATE TABLE public.users" "\$temp_sql" && grep -q "CREATE TABLE public.companies" "\$temp_sql"; then
        echo -e "${GREEN}\u2713 Sistema Base${NC}"
    else
        echo -e "${YELLOW}\u26a0\ufe0f Sistema Base (incompleto)${NC}"
    fi
    
    # Verificar control horario
    if grep -q "CREATE TABLE public.checkpoints" "\$temp_sql" && grep -q "CREATE TABLE public.checkpoint_records" "\$temp_sql"; then
        echo -e "${GREEN}\u2713 Control Horario${NC}"
    else
        echo -e "${YELLOW}\u26a0\ufe0f Control Horario (incompleto)${NC}"
    fi
    
    # Verificar arqueos de caja
    if grep -q "CREATE TABLE public.cash_registers" "\$temp_sql"; then
        echo -e "${GREEN}\u2713 Arqueos de Caja${NC}"
    else
        echo -e "${RED}\u2717 Arqueos de Caja (no incluido)${NC}"
    fi
    
    # Verificar gastos mensuales
    if grep -q "CREATE TABLE public.monthly_expenses" "\$temp_sql"; then
        echo -e "${GREEN}\u2713 Gastos Mensuales${NC}"
    else
        echo -e "${RED}\u2717 Gastos Mensuales (no incluido)${NC}"
    fi

    # Verificar funcionalidades especiales
    echo -e "\n${BLUE}Funcionalidades adicionales:${NC}"
    
    # Verificar soporte de IVA
    if grep -q "vat_percentage" "\$temp_sql"; then
        echo -e "${GREEN}\u2713 Soporte para IVA${NC}"
    else
        echo -e "${RED}\u2717 Soporte para IVA (no incluido)${NC}"
    fi
    
    # Verificar soporte de ventana horaria
    if grep -q "operation_start_time" "\$temp_sql" && grep -q "operation_end_time" "\$temp_sql"; then
        echo -e "${GREEN}\u2713 Ventana horaria para cierres automáticos${NC}"
    else
        echo -e "${RED}\u2717 Ventana horaria para cierres automáticos (no incluido)${NC}"
    fi
    
    # Limpiar archivo temporal
    rm -f "\$temp_sql"
    
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
    DB_EXISTS=0
    if psql -h "\$DB_HOST" -p "\$DB_PORT" -U "\$DB_USER" -lqt | grep -w "\$DB_NAME" &> /dev/null; then
        DB_EXISTS=1
        echo -e "\${YELLOW}La base de datos '\$DB_NAME' ya existe. Se actualizará sin eliminarla...\${NC}"
    fi
    
    # Si la base de datos no existe, crearla
    if [ \$DB_EXISTS -eq 0 ]; then
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

# Crear un archivo informativo detallado
INFO_FILE="backup_info_${DATE_STAMP}.txt"
echo "Información del backup de Productiva" > "$INFO_FILE"
echo "===================================" >> "$INFO_FILE"
echo "Fecha: $(date '+%Y-%m-%d %H:%M:%S')" >> "$INFO_FILE"
echo "Archivo SQL: $SQL_BACKUP_FILE ($(du -h "$SQL_BACKUP_FILE" | cut -f1))" >> "$INFO_FILE"
echo "Archivo Ejecutable: $EXECUTABLE_BACKUP_FILE ($(du -h "$EXECUTABLE_BACKUP_FILE" | cut -f1))" >> "$INFO_FILE"
echo "Tablas: $TABLE_COUNT" >> "$INFO_FILE"
echo "Registros: $RECORD_COUNT" >> "$INFO_FILE"
echo "" >> "$INFO_FILE"

# Añadir información sobre tablas críticas encontradas/faltantes
echo "Módulos encontrados:" >> "$INFO_FILE"

# Verificar módulo base
if grep -q "CREATE TABLE public.users" "$SQL_BACKUP_FILE" && \
   grep -q "CREATE TABLE public.companies" "$SQL_BACKUP_FILE" && \
   grep -q "CREATE TABLE public.employees" "$SQL_BACKUP_FILE"; then
   echo "- Sistema Base: SÍ" >> "$INFO_FILE"
else
   echo "- Sistema Base: INCOMPLETO" >> "$INFO_FILE"
fi

# Verificar módulo de Control Horario
if grep -q "CREATE TABLE public.checkpoints" "$SQL_BACKUP_FILE" && \
   grep -q "CREATE TABLE public.checkpoint_records" "$SQL_BACKUP_FILE"; then
   echo "- Control Horario: SÍ" >> "$INFO_FILE"
else
   echo "- Control Horario: INCOMPLETO" >> "$INFO_FILE"
fi

# Verificar módulo de Arqueos de Caja
if grep -q "CREATE TABLE public.cash_registers" "$SQL_BACKUP_FILE"; then
   echo "- Arqueos de Caja: SÍ" >> "$INFO_FILE"
else
   echo "- Arqueos de Caja: NO" >> "$INFO_FILE"
fi

# Verificar módulo de Gastos Mensuales
if grep -q "CREATE TABLE public.expense_categories" "$SQL_BACKUP_FILE" && \
   grep -q "CREATE TABLE public.monthly_expenses" "$SQL_BACKUP_FILE"; then
   echo "- Gastos Mensuales: SÍ" >> "$INFO_FILE"
else
   echo "- Gastos Mensuales: INCOMPLETO O AUSENTE" >> "$INFO_FILE"
fi

# Verificar módulo de Tareas
if grep -q "CREATE TABLE public.task_templates" "$SQL_BACKUP_FILE" && \
   grep -q "CREATE TABLE public.task_instances" "$SQL_BACKUP_FILE"; then
   echo "- Sistema de Tareas: SÍ" >> "$INFO_FILE"
else
   echo "- Sistema de Tareas: INCOMPLETO O AUSENTE" >> "$INFO_FILE"
fi

# Añadir funcionalidades especiales
echo "" >> "$INFO_FILE"
echo "Funcionalidades especiales:" >> "$INFO_FILE"

# Verificar soporte de IVA en arqueos
if grep -q "vat_percentage" "$SQL_BACKUP_FILE"; then
   echo "- Cálculo de IVA en Arqueos: SÍ" >> "$INFO_FILE"
else
   echo "- Cálculo de IVA en Arqueos: NO" >> "$INFO_FILE"
fi

# Verificar soporte de ventana horaria para cierres automáticos
if grep -q "operation_start_time" "$SQL_BACKUP_FILE" && grep -q "operation_end_time" "$SQL_BACKUP_FILE"; then
   echo "- Ventana horaria para cierres automáticos: SÍ" >> "$INFO_FILE"
else
   echo "- Ventana horaria para cierres automáticos: NO" >> "$INFO_FILE"
fi

# Verificar soporte de impresoras de red
if grep -q "CREATE TABLE public.network_printers" "$SQL_BACKUP_FILE"; then
   echo "- Soporte para impresoras de red: SÍ" >> "$INFO_FILE"
else
   echo "- Soporte para impresoras de red: NO" >> "$INFO_FILE"
fi

echo "" >> "$INFO_FILE"
echo "Para restaurar, ejecute:" >> "$INFO_FILE"
echo "  ./$EXECUTABLE_BACKUP_FILE --restore" >> "$INFO_FILE"

echo -e "${BLUE}Se ha creado un archivo informativo detallado: ${INFO_FILE}${NC}"

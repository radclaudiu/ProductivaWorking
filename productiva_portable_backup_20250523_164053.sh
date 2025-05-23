#!/bin/bash
# Script ejecutable para restauración de la base de datos Productiva
# Tablas esenciales para el funcionamiento del sistema
# IMPORTANTE: Este script destruirá la base de datos de destino y la reemplazará

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
echo -e "${AZUL}  RESTAURACIÓN DE BASE DE DATOS PRODUCTIVA${RESET}"
echo -e "${AZUL}===========================================================${RESET}"
echo -e "${AZUL}Fecha de creación de backup:\033[0m 23-05-2025 16:40:58"
echo -e "${AZUL}Fecha de restauración:\033[0m $FECHA_EJECUCION"
echo -e "${AZUL}Servidor destino:\033[0m $DB_HOST:$DB_PORT"
echo -e "${AZUL}Base de datos:\033[0m $DB_NAME"
echo -e "${AZUL}Tablas incluidas:\033[0m 13"
echo -e "${AZUL}===========================================================\033[0m"

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
echo -e "${AZUL}Restaurando estructura y datos esenciales...${RESET}"

# Contenido SQL insertado aquí
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME" << 'SQLCONTENT'
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_incident_type') THEN CREATE TYPE checkpoint_incident_type AS ENUM ('missed_checkout', 'late_checkin', 'early_checkout', 'overtime', 'manual_adjustment', 'contract_hours_adjustment'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_status') THEN CREATE TYPE checkpoint_status AS ENUM ('active', 'disabled', 'maintenance'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpointincidenttype') THEN CREATE TYPE checkpointincidenttype AS ENUM ('MISSED_CHECKOUT', 'LATE_CHECKIN', 'EARLY_CHECKOUT', 'OVERTIME', 'MANUAL_ADJUSTMENT', 'CONTRACT_HOURS_ADJUSTMENT'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpointstatus') THEN CREATE TYPE checkpointstatus AS ENUM ('ACTIVE', 'DISABLED', 'MAINTENANCE'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'conservationtype') THEN CREATE TYPE conservationtype AS ENUM ('DESCONGELACION', 'REFRIGERACION', 'GASTRO', 'CALIENTE', 'SECO'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contract_type') THEN CREATE TYPE contract_type AS ENUM ('INDEFINIDO', 'TEMPORAL', 'PRACTICAS', 'FORMACION', 'OBRA'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contracttype') THEN CREATE TYPE contracttype AS ENUM ('INDEFINIDO', 'TEMPORAL', 'PRACTICAS', 'FORMACION', 'OBRA'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'employee_status') THEN CREATE TYPE employee_status AS ENUM ('activo', 'baja_medica', 'excedencia', 'vacaciones', 'inactivo'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'employeestatus') THEN CREATE TYPE employeestatus AS ENUM ('ACTIVO', 'BAJA_MEDICA', 'EXCEDENCIA', 'VACACIONES', 'INACTIVO'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_frequency') THEN CREATE TYPE task_frequency AS ENUM ('diaria', 'semanal', 'mensual', 'unica'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_priority') THEN CREATE TYPE task_priority AS ENUM ('alta', 'media', 'baja'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_status') THEN CREATE TYPE task_status AS ENUM ('pendiente', 'completada', 'cancelada'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'taskfrequency') THEN CREATE TYPE taskfrequency AS ENUM ('DIARIA', 'SEMANAL', 'QUINCENAL', 'MENSUAL', 'PERSONALIZADA'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'taskpriority') THEN CREATE TYPE taskpriority AS ENUM ('BAJA', 'MEDIA', 'ALTA', 'URGENTE'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'taskstatus') THEN CREATE TYPE taskstatus AS ENUM ('PENDIENTE', 'COMPLETADA', 'VENCIDA', 'CANCELADA'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN CREATE TYPE user_role AS ENUM ('admin', 'gerente', 'empleado'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'userrole') THEN CREATE TYPE userrole AS ENUM ('ADMIN', 'GERENTE', 'EMPLEADO'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vacation_status') THEN CREATE TYPE vacation_status AS ENUM ('REGISTRADA', 'DISFRUTADA'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vacationstatus') THEN CREATE TYPE vacationstatus AS ENUM ('REGISTRADA', 'DISFRUTADA'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'week_day') THEN CREATE TYPE week_day AS ENUM ('lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo'); END IF; END 1388;
 DO 1388 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'weekday') THEN CREATE TYPE weekday AS ENUM ('LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'); END IF; END 1388;
-- Estructura para la tabla users
--
-- PostgreSQL database dump
--

-- Dumped from database version 16.9
-- Dumped by pg_dump version 16.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id integer NOT NULL,
    username character varying(64) NOT NULL,
    email character varying(120) NOT NULL,
    password_hash character varying(256) NOT NULL,
    role character varying(20) DEFAULT 'empleado'::character varying NOT NULL,
    first_name character varying(64),
    last_name character varying(64),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_active boolean DEFAULT true
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- PostgreSQL database dump complete
--

-- Datos para la tabla users
TRUNCATE TABLE users CASCADE;
\COPY users FROM STDIN WITH DELIMITER ',' CSV NULL 'NULL';
1,admin,admin@example.com,scrypt:32768:8:1$iqZQqUfLcn3F3OGT$e85d4367bb3f7248a25abb3668b4f057b7830c6a02cb3578f1a9c666ec6a5113a71cee5b7ce5bcb673c0cff14626391c46669c0e766d754a7ea5c3483c3ad28f,ADMIN,Admin,User,2025-04-09 20:44:47.924417,2025-04-09 20:44:47.92442,t
2,lucia,luciamendez@gruporad.com,scrypt:32768:8:1$efnGnbbuer71cBLW$c5e5a4040cf8697d75b8599e1f634ad3ae865df3e222996b7167954ab3f8efc9cb2be59142430cda4caeb8f2ff326c6302ef25191d58be4356c4d4fbe09ca328,ADMIN,Lucia ,Mendez,2025-04-10 13:34:57.699453,2025-04-15 18:18:20.483229,t
\.
-- Estructura para la tabla companies

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
echo -e "${AZUL}Fecha de generación:\033[0m 23-05-2025 16:42:01"
echo -e "${AZUL}Fecha de ejecución:\033[0m $FECHA_EJECUCION"
echo -e "${AZUL}Servidor destino:\033[0m $DB_HOST:$DB_PORT"
echo -e "${AZUL}Base de datos:\033[0m $DB_NAME"
echo -e "${AZUL}Total de tablas:\033[0m 41"
echo -e "${AZUL}===========================================================\033[0m"

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
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_incident_type') THEN CREATE TYPE checkpoint_incident_type AS ENUM ('missed_checkout', 'late_checkin', 'early_checkout', 'overtime', 'manual_adjustment', 'contract_hours_adjustment'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_status') THEN CREATE TYPE checkpoint_status AS ENUM ('active', 'disabled', 'maintenance'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpointincidenttype') THEN CREATE TYPE checkpointincidenttype AS ENUM ('MISSED_CHECKOUT', 'LATE_CHECKIN', 'EARLY_CHECKOUT', 'OVERTIME', 'MANUAL_ADJUSTMENT', 'CONTRACT_HOURS_ADJUSTMENT'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpointstatus') THEN CREATE TYPE checkpointstatus AS ENUM ('ACTIVE', 'DISABLED', 'MAINTENANCE'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'conservationtype') THEN CREATE TYPE conservationtype AS ENUM ('DESCONGELACION', 'REFRIGERACION', 'GASTRO', 'CALIENTE', 'SECO'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contract_type') THEN CREATE TYPE contract_type AS ENUM ('INDEFINIDO', 'TEMPORAL', 'PRACTICAS', 'FORMACION', 'OBRA'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contracttype') THEN CREATE TYPE contracttype AS ENUM ('INDEFINIDO', 'TEMPORAL', 'PRACTICAS', 'FORMACION', 'OBRA'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'employee_status') THEN CREATE TYPE employee_status AS ENUM ('activo', 'baja_medica', 'excedencia', 'vacaciones', 'inactivo'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'employeestatus') THEN CREATE TYPE employeestatus AS ENUM ('ACTIVO', 'BAJA_MEDICA', 'EXCEDENCIA', 'VACACIONES', 'INACTIVO'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_frequency') THEN CREATE TYPE task_frequency AS ENUM ('diaria', 'semanal', 'mensual', 'unica'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_priority') THEN CREATE TYPE task_priority AS ENUM ('alta', 'media', 'baja'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_status') THEN CREATE TYPE task_status AS ENUM ('pendiente', 'completada', 'cancelada'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'taskfrequency') THEN CREATE TYPE taskfrequency AS ENUM ('DIARIA', 'SEMANAL', 'QUINCENAL', 'MENSUAL', 'PERSONALIZADA'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'taskpriority') THEN CREATE TYPE taskpriority AS ENUM ('BAJA', 'MEDIA', 'ALTA', 'URGENTE'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'taskstatus') THEN CREATE TYPE taskstatus AS ENUM ('PENDIENTE', 'COMPLETADA', 'VENCIDA', 'CANCELADA'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN CREATE TYPE user_role AS ENUM ('admin', 'gerente', 'empleado'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'userrole') THEN CREATE TYPE userrole AS ENUM ('ADMIN', 'GERENTE', 'EMPLEADO'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vacation_status') THEN CREATE TYPE vacation_status AS ENUM ('REGISTRADA', 'DISFRUTADA'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vacationstatus') THEN CREATE TYPE vacationstatus AS ENUM ('REGISTRADA', 'DISFRUTADA'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'week_day') THEN CREATE TYPE week_day AS ENUM ('lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo'); END IF; END 1540;
 DO 1540 BEGIN IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'weekday') THEN CREATE TYPE weekday AS ENUM ('LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'); END IF; END 1540;
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

--
-- Name: checkpoint_incident_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.checkpoint_incident_type AS ENUM (
    'missed_checkout',
    'late_checkin',
    'early_checkout',
    'overtime',
    'manual_adjustment',
    'contract_hours_adjustment'
);


--
-- Name: checkpoint_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.checkpoint_status AS ENUM (
    'active',
    'disabled',
    'maintenance'
);


--
-- Name: checkpointincidenttype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.checkpointincidenttype AS ENUM (
    'MISSED_CHECKOUT',
    'LATE_CHECKIN',
    'EARLY_CHECKOUT',
    'OVERTIME',
    'MANUAL_ADJUSTMENT',
    'CONTRACT_HOURS_ADJUSTMENT'
);


--
-- Name: checkpointstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.checkpointstatus AS ENUM (
    'ACTIVE',
    'DISABLED',
    'MAINTENANCE'
);


--
-- Name: conservationtype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.conservationtype AS ENUM (
    'DESCONGELACION',
    'REFRIGERACION',
    'GASTRO',
    'CALIENTE',
    'SECO'
);


--
-- Name: contract_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.contract_type AS ENUM (
    'INDEFINIDO',
    'TEMPORAL',
    'PRACTICAS',
    'FORMACION',
    'OBRA'
);


--
-- Name: contracttype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.contracttype AS ENUM (
    'INDEFINIDO',
    'TEMPORAL',
    'PRACTICAS',
    'FORMACION',
    'OBRA'
);


--
-- Name: employee_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.employee_status AS ENUM (
    'activo',
    'baja_medica',
    'excedencia',
    'vacaciones',
    'inactivo'
);


--
-- Name: employeestatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.employeestatus AS ENUM (
    'ACTIVO',
    'BAJA_MEDICA',
    'EXCEDENCIA',
    'VACACIONES',
    'INACTIVO'
);


--
-- Name: task_frequency; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.task_frequency AS ENUM (
    'diaria',
    'semanal',
    'mensual',
    'unica'
);


--
-- Name: task_priority; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.task_priority AS ENUM (
    'alta',
    'media',
    'baja'
);


--
-- Name: task_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.task_status AS ENUM (
    'pendiente',
    'completada',
    'cancelada'
);


--
-- Name: taskfrequency; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.taskfrequency AS ENUM (
    'DIARIA',
    'SEMANAL',
    'QUINCENAL',
    'MENSUAL',
    'PERSONALIZADA'
);


--
-- Name: taskpriority; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.taskpriority AS ENUM (
    'BAJA',
    'MEDIA',
    'ALTA',
    'URGENTE'
);


--
-- Name: taskstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.taskstatus AS ENUM (
    'PENDIENTE',
    'COMPLETADA',
    'VENCIDA',
    'CANCELADA'
);


--
-- Name: user_role; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.user_role AS ENUM (
    'admin',
    'gerente',
    'empleado'
);


--
-- Name: userrole; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.userrole AS ENUM (
    'ADMIN',
    'GERENTE',
    'EMPLEADO'
);


--
-- Name: vacation_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.vacation_status AS ENUM (
    'REGISTRADA',
    'DISFRUTADA'
);


--
-- Name: vacationstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.vacationstatus AS ENUM (
    'REGISTRADA',
    'DISFRUTADA'
);


--
-- Name: week_day; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.week_day AS ENUM (
    'lunes',
    'martes',
    'miercoles',
    'jueves',
    'viernes',
    'sabado',
    'domingo'
);


--
-- Name: weekday; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.weekday AS ENUM (
    'LUNES',
    'MARTES',
    'MIERCOLES',
    'JUEVES',
    'VIERNES',
    'SABADO',
    'DOMINGO'
);


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: activity_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.activity_logs (
    id integer NOT NULL,
    action character varying(256) NOT NULL,
    ip_address character varying(64),
    "timestamp" timestamp without time zone,
    user_id integer
);


--
-- Name: activity_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.activity_logs_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: activity_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.activity_logs_id_seq OWNED BY public.activity_logs.id;


--
-- Name: cash_register_summaries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cash_register_summaries (
    id integer NOT NULL,
    year integer NOT NULL,
    month integer NOT NULL,
    week_number integer NOT NULL,
    weekly_total double precision DEFAULT 0.0 NOT NULL,
    monthly_total double precision DEFAULT 0.0 NOT NULL,
    yearly_total double precision DEFAULT 0.0 NOT NULL,
    weekly_cash double precision DEFAULT 0.0 NOT NULL,
    weekly_card double precision DEFAULT 0.0 NOT NULL,
    weekly_delivery_cash double precision DEFAULT 0.0 NOT NULL,
    weekly_delivery_online double precision DEFAULT 0.0 NOT NULL,
    weekly_check double precision DEFAULT 0.0 NOT NULL,
    weekly_expenses double precision DEFAULT 0.0 NOT NULL,
    weekly_staff_cost double precision DEFAULT 0.0 NOT NULL,
    monthly_staff_cost double precision DEFAULT 0.0 NOT NULL,
    weekly_staff_cost_percentage double precision DEFAULT 0.0 NOT NULL,
    monthly_staff_cost_percentage double precision DEFAULT 0.0 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    company_id integer NOT NULL,
    weekly_vat_amount double precision DEFAULT 0.0 NOT NULL,
    weekly_net_amount double precision DEFAULT 0.0 NOT NULL,
    monthly_vat_amount double precision DEFAULT 0.0 NOT NULL,
    monthly_net_amount double precision DEFAULT 0.0 NOT NULL
);


--
-- Name: cash_register_summaries_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cash_register_summaries_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cash_register_summaries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cash_register_summaries_id_seq OWNED BY public.cash_register_summaries.id;


--
-- Name: cash_register_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cash_register_tokens (
    id integer NOT NULL,
    token character varying(64) NOT NULL,
    is_active boolean DEFAULT true,
    expires_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    used_at timestamp without time zone,
    company_id integer NOT NULL,
    created_by_id integer,
    employee_id integer,
    cash_register_id integer,
    pin character varying(10)
);


--
-- Name: cash_register_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cash_register_tokens_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cash_register_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cash_register_tokens_id_seq OWNED BY public.cash_register_tokens.id;


--
-- Name: cash_registers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cash_registers (
    id integer NOT NULL,
    date date NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    total_amount double precision DEFAULT 0.0 NOT NULL,
    cash_amount double precision DEFAULT 0.0 NOT NULL,
    card_amount double precision DEFAULT 0.0 NOT NULL,
    delivery_cash_amount double precision DEFAULT 0.0 NOT NULL,
    delivery_online_amount double precision DEFAULT 0.0 NOT NULL,
    check_amount double precision DEFAULT 0.0 NOT NULL,
    expenses_amount double precision DEFAULT 0.0 NOT NULL,
    expenses_notes text,
    notes text,
    is_confirmed boolean DEFAULT false,
    confirmed_at timestamp without time zone,
    confirmed_by_id integer,
    company_id integer NOT NULL,
    created_by_id integer,
    employee_id integer,
    employee_name character varying(100),
    token_id integer,
    vat_percentage double precision DEFAULT 21.0 NOT NULL,
    vat_amount double precision DEFAULT 0.0 NOT NULL,
    net_amount double precision DEFAULT 0.0 NOT NULL
);


--
-- Name: cash_registers_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cash_registers_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cash_registers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cash_registers_id_seq OWNED BY public.cash_registers.id;


--
-- Name: checkpoint_incidents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.checkpoint_incidents (
    id integer NOT NULL,
    incident_type public.checkpointincidenttype,
    description text,
    created_at timestamp without time zone,
    resolved boolean,
    resolved_at timestamp without time zone,
    resolution_notes text,
    record_id integer NOT NULL,
    resolved_by_id integer
);


--
-- Name: checkpoint_incidents_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.checkpoint_incidents_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: checkpoint_incidents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.checkpoint_incidents_id_seq OWNED BY public.checkpoint_incidents.id;


--
-- Name: checkpoint_original_records; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.checkpoint_original_records (
    id integer NOT NULL,
    record_id integer NOT NULL,
    original_check_in_time timestamp without time zone NOT NULL,
    original_check_out_time timestamp without time zone,
    original_signature_data text,
    original_has_signature boolean,
    original_notes text,
    adjusted_at timestamp without time zone,
    adjusted_by_id integer,
    adjustment_reason character varying(256),
    created_at timestamp without time zone,
    hours_worked double precision DEFAULT 0.0 NOT NULL
);


--
-- Name: checkpoint_original_records_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.checkpoint_original_records_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: checkpoint_original_records_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.checkpoint_original_records_id_seq OWNED BY public.checkpoint_original_records.id;


--
-- Name: checkpoint_records; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.checkpoint_records (
    id integer NOT NULL,
    check_in_time timestamp without time zone NOT NULL,
    check_out_time timestamp without time zone,
    original_check_in_time timestamp without time zone,
    original_check_out_time timestamp without time zone,
    adjusted boolean,
    adjustment_reason character varying(256),
    notes text,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    employee_id integer NOT NULL,
    checkpoint_id integer NOT NULL,
    signature_data text,
    has_signature boolean
);


--
-- Name: checkpoint_records_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.checkpoint_records_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: checkpoint_records_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.checkpoint_records_id_seq OWNED BY public.checkpoint_records.id;


--
-- Name: checkpoints; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.checkpoints (
    id integer NOT NULL,
    name character varying(128) NOT NULL,
    description text,
    location character varying(256),
    status public.checkpointstatus,
    username character varying(64) NOT NULL,
    password_hash character varying(256) NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    company_id integer NOT NULL,
    enforce_contract_hours boolean,
    auto_adjust_overtime boolean,
    operation_start_time time without time zone,
    operation_end_time time without time zone,
    enforce_operation_hours boolean
);


--
-- Name: checkpoints_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.checkpoints_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: checkpoints_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.checkpoints_id_seq OWNED BY public.checkpoints.id;


--
-- Name: companies; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.companies (
    id integer NOT NULL,
    name character varying(128) NOT NULL,
    address character varying(256),
    city character varying(64),
    postal_code character varying(16),
    country character varying(64),
    sector character varying(64),
    tax_id character varying(32),
    phone character varying(13),
    email character varying(120),
    website character varying(128),
    bank_account character varying(24),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_active boolean DEFAULT true,
    hourly_employee_cost double precision DEFAULT 12.0
);


--
-- Name: companies_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.companies_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: companies_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.companies_id_seq OWNED BY public.companies.id;


--
-- Name: company_work_hours; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.company_work_hours (
    id integer NOT NULL,
    year integer NOT NULL,
    month integer NOT NULL,
    week_number integer NOT NULL,
    weekly_hours double precision NOT NULL,
    monthly_hours double precision NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    company_id integer NOT NULL
);


--
-- Name: company_work_hours_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.company_work_hours_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: company_work_hours_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.company_work_hours_id_seq OWNED BY public.company_work_hours.id;


--
-- Name: employee_check_ins; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_check_ins (
    id integer NOT NULL,
    employee_id integer NOT NULL,
    check_in_time timestamp without time zone NOT NULL,
    check_out_time timestamp without time zone,
    is_generated boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    notes text
);


--
-- Name: employee_check_ins_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_check_ins_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_check_ins_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_check_ins_id_seq OWNED BY public.employee_check_ins.id;


--
-- Name: employee_contract_hours; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_contract_hours (
    id integer NOT NULL,
    daily_hours double precision,
    weekly_hours double precision,
    allow_overtime boolean,
    max_overtime_daily double precision,
    use_normal_schedule boolean,
    normal_start_time time without time zone,
    normal_end_time time without time zone,
    use_flexibility boolean,
    checkin_flexibility integer,
    checkout_flexibility integer,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    employee_id integer NOT NULL
);


--
-- Name: employee_contract_hours_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_contract_hours_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_contract_hours_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_contract_hours_id_seq OWNED BY public.employee_contract_hours.id;


--
-- Name: employee_documents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_documents (
    id integer NOT NULL,
    filename character varying(256) NOT NULL,
    original_filename character varying(256) NOT NULL,
    file_path character varying(512) NOT NULL,
    file_type character varying(64),
    file_size integer,
    description character varying(256),
    uploaded_at timestamp without time zone,
    employee_id integer NOT NULL
);


--
-- Name: employee_documents_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_documents_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_documents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_documents_id_seq OWNED BY public.employee_documents.id;


--
-- Name: employee_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_history (
    id integer NOT NULL,
    field_name character varying(64) NOT NULL,
    old_value character varying(256),
    new_value character varying(256),
    changed_at timestamp without time zone,
    employee_id integer NOT NULL,
    changed_by_id integer
);


--
-- Name: employee_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_history_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_history_id_seq OWNED BY public.employee_history.id;


--
-- Name: employee_notes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_notes (
    id integer NOT NULL,
    content text NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    employee_id integer NOT NULL,
    created_by_id integer
);


--
-- Name: employee_notes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_notes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_notes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_notes_id_seq OWNED BY public.employee_notes.id;


--
-- Name: employee_schedules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_schedules (
    id integer NOT NULL,
    employee_id integer NOT NULL,
    day_of_week character varying(20) NOT NULL,
    start_time time without time zone NOT NULL,
    end_time time without time zone NOT NULL,
    is_working_day boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: employee_schedules_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_schedules_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_schedules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_schedules_id_seq OWNED BY public.employee_schedules.id;


--
-- Name: employee_vacations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_vacations (
    id integer NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    status public.vacationstatus,
    is_signed boolean,
    is_enjoyed boolean,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    notes text,
    employee_id integer NOT NULL
);


--
-- Name: employee_vacations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_vacations_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_vacations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_vacations_id_seq OWNED BY public.employee_vacations.id;


--
-- Name: employee_work_hours; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_work_hours (
    id integer NOT NULL,
    year integer NOT NULL,
    month integer NOT NULL,
    week_number integer NOT NULL,
    daily_hours double precision NOT NULL,
    weekly_hours double precision NOT NULL,
    monthly_hours double precision NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    employee_id integer NOT NULL,
    company_id integer NOT NULL
);


--
-- Name: employee_work_hours_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_work_hours_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_work_hours_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_work_hours_id_seq OWNED BY public.employee_work_hours.id;


--
-- Name: employees; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employees (
    id integer NOT NULL,
    first_name character varying(64) NOT NULL,
    last_name character varying(64) NOT NULL,
    dni character varying(16) NOT NULL,
    social_security_number character varying(20),
    email character varying(120),
    address character varying(200),
    phone character varying(20),
    "position" character varying(64),
    contract_type character varying(20) DEFAULT 'INDEFINIDO'::character varying,
    bank_account character varying(64),
    start_date character varying(20),
    end_date character varying(20),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_active boolean DEFAULT true,
    status character varying(20) DEFAULT 'activo'::character varying,
    company_id integer NOT NULL,
    user_id integer,
    is_on_shift boolean DEFAULT false,
    status_start_date character varying(20),
    status_end_date character varying(20),
    status_notes text
);


--
-- Name: employees_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employees_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employees_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employees_id_seq OWNED BY public.employees.id;


--
-- Name: expense_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.expense_categories (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    company_id integer,
    is_system boolean,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


--
-- Name: expense_categories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.expense_categories_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: expense_categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.expense_categories_id_seq OWNED BY public.expense_categories.id;


--
-- Name: fixed_expenses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fixed_expenses (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    amount double precision NOT NULL,
    company_id integer NOT NULL,
    category_id integer NOT NULL,
    is_active boolean,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


--
-- Name: fixed_expenses_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fixed_expenses_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fixed_expenses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fixed_expenses_id_seq OWNED BY public.fixed_expenses.id;


--
-- Name: label_templates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.label_templates (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    is_default boolean,
    titulo_x integer,
    titulo_y integer,
    titulo_size integer,
    titulo_bold boolean,
    conservacion_x integer,
    conservacion_y integer,
    conservacion_size integer,
    conservacion_bold boolean,
    preparador_x integer,
    preparador_y integer,
    preparador_size integer,
    preparador_bold boolean,
    fecha_x integer,
    fecha_y integer,
    fecha_size integer,
    fecha_bold boolean,
    caducidad_x integer,
    caducidad_y integer,
    caducidad_size integer,
    caducidad_bold boolean,
    caducidad2_x integer,
    caducidad2_y integer,
    caducidad2_size integer,
    caducidad2_bold boolean,
    location_id integer NOT NULL
);


--
-- Name: label_templates_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.label_templates_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: label_templates_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.label_templates_id_seq OWNED BY public.label_templates.id;


--
-- Name: local_users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.local_users (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    last_name character varying(64) NOT NULL,
    username character varying(128) NOT NULL,
    pin character varying(256) NOT NULL,
    photo_path character varying(256),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    is_active boolean,
    location_id integer NOT NULL,
    imported boolean DEFAULT false,
    employee_id integer
);


--
-- Name: local_users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.local_users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: local_users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.local_users_id_seq OWNED BY public.local_users.id;


--
-- Name: location_access_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.location_access_tokens (
    id integer NOT NULL,
    location_id integer NOT NULL,
    token character varying(128) NOT NULL,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    last_used_at timestamp without time zone
);


--
-- Name: location_access_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.location_access_tokens_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: location_access_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.location_access_tokens_id_seq OWNED BY public.location_access_tokens.id;


--
-- Name: locations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.locations (
    id integer NOT NULL,
    name character varying(128) NOT NULL,
    address character varying(256),
    city character varying(64),
    postal_code character varying(16),
    description text,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    is_active boolean,
    portal_username character varying(64),
    portal_password_hash character varying(256),
    company_id integer NOT NULL,
    requires_pin boolean DEFAULT true
);


--
-- Name: locations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.locations_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: locations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.locations_id_seq OWNED BY public.locations.id;


--
-- Name: monthly_expense_summaries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.monthly_expense_summaries (
    id integer NOT NULL,
    company_id integer NOT NULL,
    year integer NOT NULL,
    month integer NOT NULL,
    fixed_expenses_total double precision NOT NULL,
    custom_expenses_total double precision NOT NULL,
    total_amount double precision NOT NULL,
    number_of_expenses integer NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


--
-- Name: monthly_expense_summaries_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.monthly_expense_summaries_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: monthly_expense_summaries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.monthly_expense_summaries_id_seq OWNED BY public.monthly_expense_summaries.id;


--
-- Name: monthly_expense_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.monthly_expense_tokens (
    id integer NOT NULL,
    company_id integer NOT NULL,
    token character varying(20) NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    is_active boolean,
    category_id integer,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    last_used_at timestamp without time zone,
    total_uses integer
);


--
-- Name: monthly_expense_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.monthly_expense_tokens_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: monthly_expense_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.monthly_expense_tokens_id_seq OWNED BY public.monthly_expense_tokens.id;


--
-- Name: monthly_expenses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.monthly_expenses (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    amount double precision NOT NULL,
    company_id integer NOT NULL,
    category_id integer NOT NULL,
    year integer NOT NULL,
    month integer NOT NULL,
    is_fixed boolean,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    expense_date character varying(20),
    submitted_by_employee boolean DEFAULT false,
    employee_name character varying(100),
    receipt_image character varying(255)
);


--
-- Name: monthly_expenses_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.monthly_expenses_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: monthly_expenses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.monthly_expenses_id_seq OWNED BY public.monthly_expenses.id;


--
-- Name: network_printers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.network_printers (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    ip_address character varying(50) NOT NULL,
    model character varying(100),
    api_path character varying(255) DEFAULT '/brother_d/printer/print'::character varying,
    port integer DEFAULT 80,
    requires_auth boolean DEFAULT false,
    username character varying(100),
    password character varying(100),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_default boolean DEFAULT false,
    is_active boolean DEFAULT true,
    last_status character varying(50),
    last_status_check timestamp without time zone,
    location_id integer,
    usb_port character varying(100),
    printer_type character varying(20) DEFAULT 'DIRECT_NETWORK'::character varying NOT NULL
);


--
-- Name: network_printers_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.network_printers_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: network_printers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.network_printers_id_seq OWNED BY public.network_printers.id;


--
-- Name: product_conservations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_conservations (
    id integer NOT NULL,
    conservation_type public.conservationtype NOT NULL,
    hours_valid integer NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    product_id integer NOT NULL
);


--
-- Name: product_conservations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_conservations_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: product_conservations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_conservations_id_seq OWNED BY public.product_conservations.id;


--
-- Name: product_labels; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_labels (
    id integer NOT NULL,
    created_at timestamp without time zone,
    expiry_date date NOT NULL,
    product_id integer NOT NULL,
    local_user_id integer NOT NULL,
    conservation_type public.conservationtype NOT NULL
);


--
-- Name: product_labels_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_labels_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: product_labels_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_labels_id_seq OWNED BY public.product_labels.id;


--
-- Name: products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.products (
    id integer NOT NULL,
    name character varying(128) NOT NULL,
    description text,
    shelf_life_days integer NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    is_active boolean,
    location_id integer NOT NULL
);


--
-- Name: products_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.products_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.products_id_seq OWNED BY public.products.id;


--
-- Name: task_completions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_completions (
    id integer NOT NULL,
    completion_date timestamp without time zone,
    notes text,
    task_id integer NOT NULL,
    local_user_id integer NOT NULL
);


--
-- Name: task_completions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_completions_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_completions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_completions_id_seq OWNED BY public.task_completions.id;


--
-- Name: task_groups; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_groups (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    description text,
    color character varying(7),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    location_id integer NOT NULL
);


--
-- Name: task_groups_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_groups_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_groups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_groups_id_seq OWNED BY public.task_groups.id;


--
-- Name: task_instances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_instances (
    id integer NOT NULL,
    scheduled_date date NOT NULL,
    status public.taskstatus,
    notes text,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    task_id integer NOT NULL,
    completed_by_id integer
);


--
-- Name: task_instances_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_instances_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_instances_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_instances_id_seq OWNED BY public.task_instances.id;


--
-- Name: task_monthdays; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_monthdays (
    id integer NOT NULL,
    day_of_month integer NOT NULL,
    task_id integer NOT NULL
);


--
-- Name: task_monthdays_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_monthdays_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_monthdays_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_monthdays_id_seq OWNED BY public.task_monthdays.id;


--
-- Name: task_schedules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_schedules (
    id integer NOT NULL,
    day_of_week public.weekday,
    day_of_month integer,
    start_time time without time zone,
    end_time time without time zone,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    task_id integer NOT NULL
);


--
-- Name: task_schedules_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_schedules_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_schedules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_schedules_id_seq OWNED BY public.task_schedules.id;


--
-- Name: task_weekdays; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_weekdays (
    id integer NOT NULL,
    day_of_week public.weekday NOT NULL,
    task_id integer NOT NULL
);


--
-- Name: task_weekdays_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_weekdays_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_weekdays_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_weekdays_id_seq OWNED BY public.task_weekdays.id;


--
-- Name: tasks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tasks (
    id integer NOT NULL,
    title character varying(128) NOT NULL,
    description text,
    priority public.taskpriority,
    frequency public.taskfrequency,
    status public.taskstatus,
    start_date date NOT NULL,
    end_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    location_id integer NOT NULL,
    created_by_id integer,
    group_id integer,
    current_week_completed boolean DEFAULT false,
    current_month_completed boolean DEFAULT false
);


--
-- Name: tasks_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tasks_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tasks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tasks_id_seq OWNED BY public.tasks.id;


--
-- Name: user_companies; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_companies (
    user_id integer NOT NULL,
    company_id integer NOT NULL
);


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
-- Name: activity_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activity_logs ALTER COLUMN id SET DEFAULT nextval('public.activity_logs_id_seq'::regclass);


--
-- Name: cash_register_summaries id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_register_summaries ALTER COLUMN id SET DEFAULT nextval('public.cash_register_summaries_id_seq'::regclass);


--
-- Name: cash_register_tokens id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_register_tokens ALTER COLUMN id SET DEFAULT nextval('public.cash_register_tokens_id_seq'::regclass);


--
-- Name: cash_registers id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_registers ALTER COLUMN id SET DEFAULT nextval('public.cash_registers_id_seq'::regclass);


--
-- Name: checkpoint_incidents id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoint_incidents ALTER COLUMN id SET DEFAULT nextval('public.checkpoint_incidents_id_seq'::regclass);


--
-- Name: checkpoint_original_records id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoint_original_records ALTER COLUMN id SET DEFAULT nextval('public.checkpoint_original_records_id_seq'::regclass);


--
-- Name: checkpoint_records id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoint_records ALTER COLUMN id SET DEFAULT nextval('public.checkpoint_records_id_seq'::regclass);


--
-- Name: checkpoints id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoints ALTER COLUMN id SET DEFAULT nextval('public.checkpoints_id_seq'::regclass);


--
-- Name: companies id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.companies ALTER COLUMN id SET DEFAULT nextval('public.companies_id_seq'::regclass);


--
-- Name: company_work_hours id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.company_work_hours ALTER COLUMN id SET DEFAULT nextval('public.company_work_hours_id_seq'::regclass);


--
-- Name: employee_check_ins id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_check_ins ALTER COLUMN id SET DEFAULT nextval('public.employee_check_ins_id_seq'::regclass);


--
-- Name: employee_contract_hours id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_contract_hours ALTER COLUMN id SET DEFAULT nextval('public.employee_contract_hours_id_seq'::regclass);


--
-- Name: employee_documents id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_documents ALTER COLUMN id SET DEFAULT nextval('public.employee_documents_id_seq'::regclass);


--
-- Name: employee_history id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_history ALTER COLUMN id SET DEFAULT nextval('public.employee_history_id_seq'::regclass);


--
-- Name: employee_notes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_notes ALTER COLUMN id SET DEFAULT nextval('public.employee_notes_id_seq'::regclass);


--
-- Name: employee_schedules id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_schedules ALTER COLUMN id SET DEFAULT nextval('public.employee_schedules_id_seq'::regclass);


--
-- Name: employee_vacations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_vacations ALTER COLUMN id SET DEFAULT nextval('public.employee_vacations_id_seq'::regclass);


--
-- Name: employee_work_hours id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_work_hours ALTER COLUMN id SET DEFAULT nextval('public.employee_work_hours_id_seq'::regclass);


--
-- Name: employees id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees ALTER COLUMN id SET DEFAULT nextval('public.employees_id_seq'::regclass);


--
-- Name: expense_categories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expense_categories ALTER COLUMN id SET DEFAULT nextval('public.expense_categories_id_seq'::regclass);


--
-- Name: fixed_expenses id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_expenses ALTER COLUMN id SET DEFAULT nextval('public.fixed_expenses_id_seq'::regclass);


--
-- Name: label_templates id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.label_templates ALTER COLUMN id SET DEFAULT nextval('public.label_templates_id_seq'::regclass);


--
-- Name: local_users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.local_users ALTER COLUMN id SET DEFAULT nextval('public.local_users_id_seq'::regclass);


--
-- Name: location_access_tokens id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_access_tokens ALTER COLUMN id SET DEFAULT nextval('public.location_access_tokens_id_seq'::regclass);


--
-- Name: locations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.locations ALTER COLUMN id SET DEFAULT nextval('public.locations_id_seq'::regclass);


--
-- Name: monthly_expense_summaries id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_expense_summaries ALTER COLUMN id SET DEFAULT nextval('public.monthly_expense_summaries_id_seq'::regclass);


--
-- Name: monthly_expense_tokens id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_expense_tokens ALTER COLUMN id SET DEFAULT nextval('public.monthly_expense_tokens_id_seq'::regclass);


--
-- Name: monthly_expenses id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_expenses ALTER COLUMN id SET DEFAULT nextval('public.monthly_expenses_id_seq'::regclass);


--
-- Name: network_printers id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.network_printers ALTER COLUMN id SET DEFAULT nextval('public.network_printers_id_seq'::regclass);


--
-- Name: product_conservations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_conservations ALTER COLUMN id SET DEFAULT nextval('public.product_conservations_id_seq'::regclass);


--
-- Name: product_labels id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_labels ALTER COLUMN id SET DEFAULT nextval('public.product_labels_id_seq'::regclass);


--
-- Name: products id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products ALTER COLUMN id SET DEFAULT nextval('public.products_id_seq'::regclass);


--
-- Name: task_completions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_completions ALTER COLUMN id SET DEFAULT nextval('public.task_completions_id_seq'::regclass);


--
-- Name: task_groups id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_groups ALTER COLUMN id SET DEFAULT nextval('public.task_groups_id_seq'::regclass);


--
-- Name: task_instances id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_instances ALTER COLUMN id SET DEFAULT nextval('public.task_instances_id_seq'::regclass);


--
-- Name: task_monthdays id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_monthdays ALTER COLUMN id SET DEFAULT nextval('public.task_monthdays_id_seq'::regclass);


--
-- Name: task_schedules id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_schedules ALTER COLUMN id SET DEFAULT nextval('public.task_schedules_id_seq'::regclass);


--
-- Name: task_weekdays id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_weekdays ALTER COLUMN id SET DEFAULT nextval('public.task_weekdays_id_seq'::regclass);


--
-- Name: tasks id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tasks ALTER COLUMN id SET DEFAULT nextval('public.tasks_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: activity_logs activity_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activity_logs
    ADD CONSTRAINT activity_logs_pkey PRIMARY KEY (id);


--
-- Name: cash_register_summaries cash_register_summaries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_register_summaries
    ADD CONSTRAINT cash_register_summaries_pkey PRIMARY KEY (id);


--
-- Name: cash_register_tokens cash_register_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_register_tokens
    ADD CONSTRAINT cash_register_tokens_pkey PRIMARY KEY (id);


--
-- Name: cash_register_tokens cash_register_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_register_tokens
    ADD CONSTRAINT cash_register_tokens_token_key UNIQUE (token);


--
-- Name: cash_registers cash_registers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_registers
    ADD CONSTRAINT cash_registers_pkey PRIMARY KEY (id);


--
-- Name: checkpoint_incidents checkpoint_incidents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoint_incidents
    ADD CONSTRAINT checkpoint_incidents_pkey PRIMARY KEY (id);


--
-- Name: checkpoint_original_records checkpoint_original_records_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoint_original_records
    ADD CONSTRAINT checkpoint_original_records_pkey PRIMARY KEY (id);


--
-- Name: checkpoint_records checkpoint_records_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoint_records
    ADD CONSTRAINT checkpoint_records_pkey PRIMARY KEY (id);


--
-- Name: checkpoints checkpoints_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoints
    ADD CONSTRAINT checkpoints_pkey PRIMARY KEY (id);


--
-- Name: checkpoints checkpoints_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoints
    ADD CONSTRAINT checkpoints_username_key UNIQUE (username);


--
-- Name: companies companies_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.companies
    ADD CONSTRAINT companies_pkey PRIMARY KEY (id);


--
-- Name: companies companies_tax_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.companies
    ADD CONSTRAINT companies_tax_id_key UNIQUE (tax_id);


--
-- Name: company_work_hours company_work_hours_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.company_work_hours
    ADD CONSTRAINT company_work_hours_pkey PRIMARY KEY (id);


--
-- Name: employee_check_ins employee_check_ins_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_check_ins
    ADD CONSTRAINT employee_check_ins_pkey PRIMARY KEY (id);


--
-- Name: employee_contract_hours employee_contract_hours_employee_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_contract_hours
    ADD CONSTRAINT employee_contract_hours_employee_id_key UNIQUE (employee_id);


--
-- Name: employee_contract_hours employee_contract_hours_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_contract_hours
    ADD CONSTRAINT employee_contract_hours_pkey PRIMARY KEY (id);


--
-- Name: employee_documents employee_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_documents
    ADD CONSTRAINT employee_documents_pkey PRIMARY KEY (id);


--
-- Name: employee_history employee_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_history
    ADD CONSTRAINT employee_history_pkey PRIMARY KEY (id);


--
-- Name: employee_notes employee_notes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_notes
    ADD CONSTRAINT employee_notes_pkey PRIMARY KEY (id);


--
-- Name: employee_schedules employee_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_schedules
    ADD CONSTRAINT employee_schedules_pkey PRIMARY KEY (id);


--
-- Name: employee_vacations employee_vacations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_vacations
    ADD CONSTRAINT employee_vacations_pkey PRIMARY KEY (id);


--
-- Name: employee_work_hours employee_work_hours_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_work_hours
    ADD CONSTRAINT employee_work_hours_pkey PRIMARY KEY (id);


--
-- Name: employees employees_dni_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_dni_key UNIQUE (dni);


--
-- Name: employees employees_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_pkey PRIMARY KEY (id);


--
-- Name: employees employees_user_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_user_id_key UNIQUE (user_id);


--
-- Name: expense_categories expense_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expense_categories
    ADD CONSTRAINT expense_categories_pkey PRIMARY KEY (id);


--
-- Name: fixed_expenses fixed_expenses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_expenses
    ADD CONSTRAINT fixed_expenses_pkey PRIMARY KEY (id);


--
-- Name: label_templates label_templates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.label_templates
    ADD CONSTRAINT label_templates_pkey PRIMARY KEY (id);


--
-- Name: local_users local_users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.local_users
    ADD CONSTRAINT local_users_pkey PRIMARY KEY (id);


--
-- Name: location_access_tokens location_access_tokens_location_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_access_tokens
    ADD CONSTRAINT location_access_tokens_location_id_key UNIQUE (location_id);


--
-- Name: location_access_tokens location_access_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_access_tokens
    ADD CONSTRAINT location_access_tokens_pkey PRIMARY KEY (id);


--
-- Name: location_access_tokens location_access_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_access_tokens
    ADD CONSTRAINT location_access_tokens_token_key UNIQUE (token);


--
-- Name: locations locations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_pkey PRIMARY KEY (id);


--
-- Name: locations locations_portal_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_portal_username_key UNIQUE (portal_username);


--
-- Name: monthly_expense_summaries monthly_expense_summaries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_expense_summaries
    ADD CONSTRAINT monthly_expense_summaries_pkey PRIMARY KEY (id);


--
-- Name: monthly_expense_tokens monthly_expense_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_expense_tokens
    ADD CONSTRAINT monthly_expense_tokens_pkey PRIMARY KEY (id);


--
-- Name: monthly_expense_tokens monthly_expense_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_expense_tokens
    ADD CONSTRAINT monthly_expense_tokens_token_key UNIQUE (token);


--
-- Name: monthly_expenses monthly_expenses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_expenses
    ADD CONSTRAINT monthly_expenses_pkey PRIMARY KEY (id);


--
-- Name: network_printers network_printers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.network_printers
    ADD CONSTRAINT network_printers_pkey PRIMARY KEY (id);


--
-- Name: product_conservations product_conservations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_conservations
    ADD CONSTRAINT product_conservations_pkey PRIMARY KEY (id);


--
-- Name: product_labels product_labels_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_labels
    ADD CONSTRAINT product_labels_pkey PRIMARY KEY (id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: task_completions task_completions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_completions
    ADD CONSTRAINT task_completions_pkey PRIMARY KEY (id);


--
-- Name: task_groups task_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_groups
    ADD CONSTRAINT task_groups_pkey PRIMARY KEY (id);


--
-- Name: task_instances task_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_instances
    ADD CONSTRAINT task_instances_pkey PRIMARY KEY (id);


--
-- Name: task_monthdays task_monthdays_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_monthdays
    ADD CONSTRAINT task_monthdays_pkey PRIMARY KEY (id);


--
-- Name: task_schedules task_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_schedules
    ADD CONSTRAINT task_schedules_pkey PRIMARY KEY (id);


--
-- Name: task_weekdays task_weekdays_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_weekdays
    ADD CONSTRAINT task_weekdays_pkey PRIMARY KEY (id);


--
-- Name: tasks tasks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_pkey PRIMARY KEY (id);


--
-- Name: company_work_hours uq_company_period; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.company_work_hours
    ADD CONSTRAINT uq_company_period UNIQUE (company_id, year, month, week_number);


--
-- Name: employee_work_hours uq_employee_period; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_work_hours
    ADD CONSTRAINT uq_employee_period UNIQUE (employee_id, year, month, week_number);


--
-- Name: cash_register_summaries uq_summary_period; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_register_summaries
    ADD CONSTRAINT uq_summary_period UNIQUE (company_id, year, month, week_number);


--
-- Name: user_companies user_companies_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_companies
    ADD CONSTRAINT user_companies_pkey PRIMARY KEY (user_id, company_id);


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
-- Name: idx_cash_register_company; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cash_register_company ON public.cash_registers USING btree (company_id);


--
-- Name: idx_cash_register_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cash_register_date ON public.cash_registers USING btree (date);


--
-- Name: idx_checkpoint_records_checkpoint; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkpoint_records_checkpoint ON public.checkpoint_records USING btree (checkpoint_id);


--
-- Name: idx_checkpoint_records_dates; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkpoint_records_dates ON public.checkpoint_records USING btree (check_in_time, check_out_time);


--
-- Name: idx_checkpoint_records_employee; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkpoint_records_employee ON public.checkpoint_records USING btree (employee_id);


--
-- Name: idx_company_month; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_company_month ON public.company_work_hours USING btree (company_id, year, month);


--
-- Name: idx_company_week; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_company_week ON public.company_work_hours USING btree (company_id, year, week_number);


--
-- Name: idx_company_year; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_company_year ON public.company_work_hours USING btree (company_id, year);


--
-- Name: idx_employee_checkins_employee; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_checkins_employee ON public.employee_check_ins USING btree (employee_id);


--
-- Name: idx_employee_month; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_month ON public.employee_work_hours USING btree (employee_id, year, month);


--
-- Name: idx_employee_schedules_employee; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_schedules_employee ON public.employee_schedules USING btree (employee_id);


--
-- Name: idx_employee_week; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_week ON public.employee_work_hours USING btree (employee_id, year, week_number);


--
-- Name: idx_employee_year; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_year ON public.employee_work_hours USING btree (employee_id, year);


--
-- Name: idx_employees_company; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employees_company ON public.employees USING btree (company_id);


--
-- Name: idx_employees_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employees_user ON public.employees USING btree (user_id);


--
-- Name: idx_products_location; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_location ON public.products USING btree (location_id);


--
-- Name: idx_summary_company; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_summary_company ON public.cash_register_summaries USING btree (company_id);


--
-- Name: idx_summary_year_month; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_summary_year_month ON public.cash_register_summaries USING btree (year, month);


--
-- Name: idx_task_instances_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_task_instances_status ON public.task_instances USING btree (status);


--
-- Name: idx_task_instances_task; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_task_instances_task ON public.task_instances USING btree (task_id);


--
-- Name: idx_tasks_group; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tasks_group ON public.tasks USING btree (group_id);


--
-- Name: idx_tasks_location; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tasks_location ON public.tasks USING btree (location_id);


--
-- Name: activity_logs activity_logs_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activity_logs
    ADD CONSTRAINT activity_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: cash_register_summaries cash_register_summaries_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_register_summaries
    ADD CONSTRAINT cash_register_summaries_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: cash_register_tokens cash_register_tokens_cash_register_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_register_tokens
    ADD CONSTRAINT cash_register_tokens_cash_register_id_fkey FOREIGN KEY (cash_register_id) REFERENCES public.cash_registers(id);


--
-- Name: cash_register_tokens cash_register_tokens_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_register_tokens
    ADD CONSTRAINT cash_register_tokens_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: cash_register_tokens cash_register_tokens_created_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_register_tokens
    ADD CONSTRAINT cash_register_tokens_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.users(id);


--
-- Name: cash_register_tokens cash_register_tokens_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_register_tokens
    ADD CONSTRAINT cash_register_tokens_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: cash_registers cash_registers_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_registers
    ADD CONSTRAINT cash_registers_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: cash_registers cash_registers_confirmed_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_registers
    ADD CONSTRAINT cash_registers_confirmed_by_id_fkey FOREIGN KEY (confirmed_by_id) REFERENCES public.users(id);


--
-- Name: cash_registers cash_registers_created_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_registers
    ADD CONSTRAINT cash_registers_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.users(id);


--
-- Name: cash_registers cash_registers_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_registers
    ADD CONSTRAINT cash_registers_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: cash_registers cash_registers_token_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cash_registers
    ADD CONSTRAINT cash_registers_token_id_fkey FOREIGN KEY (token_id) REFERENCES public.cash_register_tokens(id);


--
-- Name: checkpoint_incidents checkpoint_incidents_record_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoint_incidents
    ADD CONSTRAINT checkpoint_incidents_record_id_fkey FOREIGN KEY (record_id) REFERENCES public.checkpoint_records(id);


--
-- Name: checkpoint_incidents checkpoint_incidents_resolved_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoint_incidents
    ADD CONSTRAINT checkpoint_incidents_resolved_by_id_fkey FOREIGN KEY (resolved_by_id) REFERENCES public.users(id);


--
-- Name: checkpoint_original_records checkpoint_original_records_adjusted_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoint_original_records
    ADD CONSTRAINT checkpoint_original_records_adjusted_by_id_fkey FOREIGN KEY (adjusted_by_id) REFERENCES public.users(id);


--
-- Name: checkpoint_original_records checkpoint_original_records_record_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoint_original_records
    ADD CONSTRAINT checkpoint_original_records_record_id_fkey FOREIGN KEY (record_id) REFERENCES public.checkpoint_records(id);


--
-- Name: checkpoint_records checkpoint_records_checkpoint_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoint_records
    ADD CONSTRAINT checkpoint_records_checkpoint_id_fkey FOREIGN KEY (checkpoint_id) REFERENCES public.checkpoints(id);


--
-- Name: checkpoint_records checkpoint_records_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoint_records
    ADD CONSTRAINT checkpoint_records_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: checkpoints checkpoints_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.checkpoints
    ADD CONSTRAINT checkpoints_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: company_work_hours company_work_hours_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.company_work_hours
    ADD CONSTRAINT company_work_hours_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: employee_check_ins employee_check_ins_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_check_ins
    ADD CONSTRAINT employee_check_ins_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_contract_hours employee_contract_hours_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_contract_hours
    ADD CONSTRAINT employee_contract_hours_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_documents employee_documents_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_documents
    ADD CONSTRAINT employee_documents_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_history employee_history_changed_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_history
    ADD CONSTRAINT employee_history_changed_by_id_fkey FOREIGN KEY (changed_by_id) REFERENCES public.users(id);


--
-- Name: employee_history employee_history_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_history
    ADD CONSTRAINT employee_history_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_notes employee_notes_created_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_notes
    ADD CONSTRAINT employee_notes_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.users(id);


--
-- Name: employee_notes employee_notes_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_notes
    ADD CONSTRAINT employee_notes_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_schedules employee_schedules_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_schedules
    ADD CONSTRAINT employee_schedules_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_vacations employee_vacations_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_vacations
    ADD CONSTRAINT employee_vacations_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_work_hours employee_work_hours_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_work_hours
    ADD CONSTRAINT employee_work_hours_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: employee_work_hours employee_work_hours_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_work_hours
    ADD CONSTRAINT employee_work_hours_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employees employees_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: employees employees_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: expense_categories expense_categories_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expense_categories
    ADD CONSTRAINT expense_categories_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: fixed_expenses fixed_expenses_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_expenses
    ADD CONSTRAINT fixed_expenses_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.expense_categories(id);


--
-- Name: fixed_expenses fixed_expenses_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_expenses
    ADD CONSTRAINT fixed_expenses_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: label_templates label_templates_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.label_templates
    ADD CONSTRAINT label_templates_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);


--
-- Name: local_users local_users_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.local_users
    ADD CONSTRAINT local_users_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);


--
-- Name: location_access_tokens location_access_tokens_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_access_tokens
    ADD CONSTRAINT location_access_tokens_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.checkpoints(id) ON DELETE CASCADE;


--
-- Name: locations locations_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: monthly_expense_summaries monthly_expense_summaries_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_expense_summaries
    ADD CONSTRAINT monthly_expense_summaries_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: monthly_expense_tokens monthly_expense_tokens_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_expense_tokens
    ADD CONSTRAINT monthly_expense_tokens_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.expense_categories(id);


--
-- Name: monthly_expense_tokens monthly_expense_tokens_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_expense_tokens
    ADD CONSTRAINT monthly_expense_tokens_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: monthly_expenses monthly_expenses_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_expenses
    ADD CONSTRAINT monthly_expenses_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.expense_categories(id);


--
-- Name: monthly_expenses monthly_expenses_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_expenses
    ADD CONSTRAINT monthly_expenses_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: network_printers network_printers_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.network_printers
    ADD CONSTRAINT network_printers_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id) ON DELETE CASCADE;


--
-- Name: product_conservations product_conservations_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_conservations
    ADD CONSTRAINT product_conservations_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: product_labels product_labels_local_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_labels
    ADD CONSTRAINT product_labels_local_user_id_fkey FOREIGN KEY (local_user_id) REFERENCES public.local_users(id);


--
-- Name: product_labels product_labels_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_labels
    ADD CONSTRAINT product_labels_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: products products_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);


--
-- Name: task_completions task_completions_local_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_completions
    ADD CONSTRAINT task_completions_local_user_id_fkey FOREIGN KEY (local_user_id) REFERENCES public.local_users(id);


--
-- Name: task_completions task_completions_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_completions
    ADD CONSTRAINT task_completions_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);


--
-- Name: task_groups task_groups_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_groups
    ADD CONSTRAINT task_groups_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);


--
-- Name: task_instances task_instances_completed_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_instances
    ADD CONSTRAINT task_instances_completed_by_id_fkey FOREIGN KEY (completed_by_id) REFERENCES public.local_users(id);


--
-- Name: task_instances task_instances_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_instances
    ADD CONSTRAINT task_instances_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);


--
-- Name: task_monthdays task_monthdays_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_monthdays
    ADD CONSTRAINT task_monthdays_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);


--
-- Name: task_schedules task_schedules_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_schedules
    ADD CONSTRAINT task_schedules_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);


--
-- Name: task_weekdays task_weekdays_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_weekdays
    ADD CONSTRAINT task_weekdays_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);


--
-- Name: tasks tasks_created_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.users(id);


--
-- Name: tasks tasks_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.task_groups(id);


--
-- Name: tasks tasks_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);


--
-- Name: user_companies user_companies_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_companies
    ADD CONSTRAINT user_companies_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: user_companies user_companies_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_companies
    ADD CONSTRAINT user_companies_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

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

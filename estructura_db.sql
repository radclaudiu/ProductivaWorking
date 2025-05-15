--
-- PostgreSQL database dump
--

-- Dumped from database version 16.8
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
-- Name: checkpoint_incident_type; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.checkpoint_incident_type AS ENUM (
    'missed_checkout',
    'late_checkin',
    'early_checkout',
    'overtime',
    'manual_adjustment',
    'contract_hours_adjustment'
);


ALTER TYPE public.checkpoint_incident_type OWNER TO neondb_owner;

--
-- Name: checkpoint_status; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.checkpoint_status AS ENUM (
    'active',
    'disabled',
    'maintenance'
);


ALTER TYPE public.checkpoint_status OWNER TO neondb_owner;

--
-- Name: checkpointincidenttype; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.checkpointincidenttype AS ENUM (
    'MISSED_CHECKOUT',
    'LATE_CHECKIN',
    'EARLY_CHECKOUT',
    'OVERTIME',
    'MANUAL_ADJUSTMENT',
    'CONTRACT_HOURS_ADJUSTMENT'
);


ALTER TYPE public.checkpointincidenttype OWNER TO neondb_owner;

--
-- Name: checkpointstatus; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.checkpointstatus AS ENUM (
    'ACTIVE',
    'DISABLED',
    'MAINTENANCE'
);


ALTER TYPE public.checkpointstatus OWNER TO neondb_owner;

--
-- Name: conservationtype; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.conservationtype AS ENUM (
    'DESCONGELACION',
    'REFRIGERACION',
    'GASTRO',
    'CALIENTE',
    'SECO'
);


ALTER TYPE public.conservationtype OWNER TO neondb_owner;

--
-- Name: contract_type; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.contract_type AS ENUM (
    'INDEFINIDO',
    'TEMPORAL',
    'PRACTICAS',
    'FORMACION',
    'OBRA'
);


ALTER TYPE public.contract_type OWNER TO neondb_owner;

--
-- Name: contracttype; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.contracttype AS ENUM (
    'INDEFINIDO',
    'TEMPORAL',
    'PRACTICAS',
    'FORMACION',
    'OBRA'
);


ALTER TYPE public.contracttype OWNER TO neondb_owner;

--
-- Name: employee_status; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.employee_status AS ENUM (
    'activo',
    'baja_medica',
    'excedencia',
    'vacaciones',
    'inactivo'
);


ALTER TYPE public.employee_status OWNER TO neondb_owner;

--
-- Name: employeestatus; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.employeestatus AS ENUM (
    'ACTIVO',
    'BAJA_MEDICA',
    'EXCEDENCIA',
    'VACACIONES',
    'INACTIVO'
);


ALTER TYPE public.employeestatus OWNER TO neondb_owner;

--
-- Name: task_frequency; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.task_frequency AS ENUM (
    'diaria',
    'semanal',
    'mensual',
    'unica'
);


ALTER TYPE public.task_frequency OWNER TO neondb_owner;

--
-- Name: task_priority; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.task_priority AS ENUM (
    'alta',
    'media',
    'baja'
);


ALTER TYPE public.task_priority OWNER TO neondb_owner;

--
-- Name: task_status; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.task_status AS ENUM (
    'pendiente',
    'completada',
    'cancelada'
);


ALTER TYPE public.task_status OWNER TO neondb_owner;

--
-- Name: taskfrequency; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.taskfrequency AS ENUM (
    'DIARIA',
    'SEMANAL',
    'QUINCENAL',
    'MENSUAL',
    'PERSONALIZADA'
);


ALTER TYPE public.taskfrequency OWNER TO neondb_owner;

--
-- Name: taskpriority; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.taskpriority AS ENUM (
    'BAJA',
    'MEDIA',
    'ALTA',
    'URGENTE'
);


ALTER TYPE public.taskpriority OWNER TO neondb_owner;

--
-- Name: taskstatus; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.taskstatus AS ENUM (
    'PENDIENTE',
    'COMPLETADA',
    'VENCIDA',
    'CANCELADA'
);


ALTER TYPE public.taskstatus OWNER TO neondb_owner;

--
-- Name: user_role; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.user_role AS ENUM (
    'admin',
    'gerente',
    'empleado'
);


ALTER TYPE public.user_role OWNER TO neondb_owner;

--
-- Name: userrole; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.userrole AS ENUM (
    'ADMIN',
    'GERENTE',
    'EMPLEADO'
);


ALTER TYPE public.userrole OWNER TO neondb_owner;

--
-- Name: vacation_status; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.vacation_status AS ENUM (
    'REGISTRADA',
    'DISFRUTADA'
);


ALTER TYPE public.vacation_status OWNER TO neondb_owner;

--
-- Name: vacationstatus; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.vacationstatus AS ENUM (
    'REGISTRADA',
    'DISFRUTADA'
);


ALTER TYPE public.vacationstatus OWNER TO neondb_owner;

--
-- Name: week_day; Type: TYPE; Schema: public; Owner: neondb_owner
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


ALTER TYPE public.week_day OWNER TO neondb_owner;

--
-- Name: weekday; Type: TYPE; Schema: public; Owner: neondb_owner
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


ALTER TYPE public.weekday OWNER TO neondb_owner;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: activity_logs; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.activity_logs (
    id integer NOT NULL,
    action character varying(256) NOT NULL,
    ip_address character varying(64),
    "timestamp" timestamp without time zone,
    user_id integer
);


ALTER TABLE public.activity_logs OWNER TO neondb_owner;

--
-- Name: activity_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.activity_logs_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.activity_logs_id_seq OWNER TO neondb_owner;

--
-- Name: activity_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.activity_logs_id_seq OWNED BY public.activity_logs.id;


--
-- Name: cash_register_summaries; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.cash_register_summaries OWNER TO neondb_owner;

--
-- Name: cash_register_summaries_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.cash_register_summaries_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cash_register_summaries_id_seq OWNER TO neondb_owner;

--
-- Name: cash_register_summaries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.cash_register_summaries_id_seq OWNED BY public.cash_register_summaries.id;


--
-- Name: cash_register_tokens; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.cash_register_tokens OWNER TO neondb_owner;

--
-- Name: cash_register_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.cash_register_tokens_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cash_register_tokens_id_seq OWNER TO neondb_owner;

--
-- Name: cash_register_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.cash_register_tokens_id_seq OWNED BY public.cash_register_tokens.id;


--
-- Name: cash_registers; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.cash_registers OWNER TO neondb_owner;

--
-- Name: cash_registers_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.cash_registers_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cash_registers_id_seq OWNER TO neondb_owner;

--
-- Name: cash_registers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.cash_registers_id_seq OWNED BY public.cash_registers.id;


--
-- Name: checkpoint_incidents; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.checkpoint_incidents OWNER TO neondb_owner;

--
-- Name: checkpoint_incidents_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.checkpoint_incidents_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.checkpoint_incidents_id_seq OWNER TO neondb_owner;

--
-- Name: checkpoint_incidents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.checkpoint_incidents_id_seq OWNED BY public.checkpoint_incidents.id;


--
-- Name: checkpoint_original_records; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.checkpoint_original_records OWNER TO neondb_owner;

--
-- Name: checkpoint_original_records_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.checkpoint_original_records_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.checkpoint_original_records_id_seq OWNER TO neondb_owner;

--
-- Name: checkpoint_original_records_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.checkpoint_original_records_id_seq OWNED BY public.checkpoint_original_records.id;


--
-- Name: checkpoint_records; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.checkpoint_records OWNER TO neondb_owner;

--
-- Name: checkpoint_records_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.checkpoint_records_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.checkpoint_records_id_seq OWNER TO neondb_owner;

--
-- Name: checkpoint_records_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.checkpoint_records_id_seq OWNED BY public.checkpoint_records.id;


--
-- Name: checkpoints; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.checkpoints OWNER TO neondb_owner;

--
-- Name: checkpoints_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.checkpoints_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.checkpoints_id_seq OWNER TO neondb_owner;

--
-- Name: checkpoints_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.checkpoints_id_seq OWNED BY public.checkpoints.id;


--
-- Name: companies; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.companies OWNER TO neondb_owner;

--
-- Name: companies_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.companies_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.companies_id_seq OWNER TO neondb_owner;

--
-- Name: companies_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.companies_id_seq OWNED BY public.companies.id;


--
-- Name: company_work_hours; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.company_work_hours OWNER TO neondb_owner;

--
-- Name: company_work_hours_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.company_work_hours_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.company_work_hours_id_seq OWNER TO neondb_owner;

--
-- Name: company_work_hours_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.company_work_hours_id_seq OWNED BY public.company_work_hours.id;


--
-- Name: employee_check_ins; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.employee_check_ins OWNER TO neondb_owner;

--
-- Name: employee_check_ins_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.employee_check_ins_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employee_check_ins_id_seq OWNER TO neondb_owner;

--
-- Name: employee_check_ins_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.employee_check_ins_id_seq OWNED BY public.employee_check_ins.id;


--
-- Name: employee_contract_hours; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.employee_contract_hours OWNER TO neondb_owner;

--
-- Name: employee_contract_hours_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.employee_contract_hours_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employee_contract_hours_id_seq OWNER TO neondb_owner;

--
-- Name: employee_contract_hours_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.employee_contract_hours_id_seq OWNED BY public.employee_contract_hours.id;


--
-- Name: employee_documents; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.employee_documents OWNER TO neondb_owner;

--
-- Name: employee_documents_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.employee_documents_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employee_documents_id_seq OWNER TO neondb_owner;

--
-- Name: employee_documents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.employee_documents_id_seq OWNED BY public.employee_documents.id;


--
-- Name: employee_history; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.employee_history OWNER TO neondb_owner;

--
-- Name: employee_history_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.employee_history_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employee_history_id_seq OWNER TO neondb_owner;

--
-- Name: employee_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.employee_history_id_seq OWNED BY public.employee_history.id;


--
-- Name: employee_notes; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.employee_notes (
    id integer NOT NULL,
    content text NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    employee_id integer NOT NULL,
    created_by_id integer
);


ALTER TABLE public.employee_notes OWNER TO neondb_owner;

--
-- Name: employee_notes_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.employee_notes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employee_notes_id_seq OWNER TO neondb_owner;

--
-- Name: employee_notes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.employee_notes_id_seq OWNED BY public.employee_notes.id;


--
-- Name: employee_schedules; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.employee_schedules OWNER TO neondb_owner;

--
-- Name: employee_schedules_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.employee_schedules_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employee_schedules_id_seq OWNER TO neondb_owner;

--
-- Name: employee_schedules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.employee_schedules_id_seq OWNED BY public.employee_schedules.id;


--
-- Name: employee_vacations; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.employee_vacations OWNER TO neondb_owner;

--
-- Name: employee_vacations_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.employee_vacations_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employee_vacations_id_seq OWNER TO neondb_owner;

--
-- Name: employee_vacations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.employee_vacations_id_seq OWNED BY public.employee_vacations.id;


--
-- Name: employee_work_hours; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.employee_work_hours OWNER TO neondb_owner;

--
-- Name: employee_work_hours_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.employee_work_hours_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employee_work_hours_id_seq OWNER TO neondb_owner;

--
-- Name: employee_work_hours_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.employee_work_hours_id_seq OWNED BY public.employee_work_hours.id;


--
-- Name: employees; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.employees OWNER TO neondb_owner;

--
-- Name: employees_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.employees_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employees_id_seq OWNER TO neondb_owner;

--
-- Name: employees_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.employees_id_seq OWNED BY public.employees.id;


--
-- Name: expense_categories; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.expense_categories OWNER TO neondb_owner;

--
-- Name: expense_categories_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.expense_categories_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.expense_categories_id_seq OWNER TO neondb_owner;

--
-- Name: expense_categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.expense_categories_id_seq OWNED BY public.expense_categories.id;


--
-- Name: fixed_expenses; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.fixed_expenses OWNER TO neondb_owner;

--
-- Name: fixed_expenses_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.fixed_expenses_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fixed_expenses_id_seq OWNER TO neondb_owner;

--
-- Name: fixed_expenses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.fixed_expenses_id_seq OWNED BY public.fixed_expenses.id;


--
-- Name: label_templates; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.label_templates OWNER TO neondb_owner;

--
-- Name: label_templates_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.label_templates_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.label_templates_id_seq OWNER TO neondb_owner;

--
-- Name: label_templates_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.label_templates_id_seq OWNED BY public.label_templates.id;


--
-- Name: local_users; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.local_users OWNER TO neondb_owner;

--
-- Name: local_users_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.local_users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.local_users_id_seq OWNER TO neondb_owner;

--
-- Name: local_users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.local_users_id_seq OWNED BY public.local_users.id;


--
-- Name: location_access_tokens; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.location_access_tokens (
    id integer NOT NULL,
    location_id integer NOT NULL,
    token character varying(128) NOT NULL,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    last_used_at timestamp without time zone
);


ALTER TABLE public.location_access_tokens OWNER TO neondb_owner;

--
-- Name: location_access_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.location_access_tokens_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.location_access_tokens_id_seq OWNER TO neondb_owner;

--
-- Name: location_access_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.location_access_tokens_id_seq OWNED BY public.location_access_tokens.id;


--
-- Name: locations; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.locations OWNER TO neondb_owner;

--
-- Name: locations_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.locations_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.locations_id_seq OWNER TO neondb_owner;

--
-- Name: locations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.locations_id_seq OWNED BY public.locations.id;


--
-- Name: monthly_expense_summaries; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.monthly_expense_summaries OWNER TO neondb_owner;

--
-- Name: monthly_expense_summaries_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.monthly_expense_summaries_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.monthly_expense_summaries_id_seq OWNER TO neondb_owner;

--
-- Name: monthly_expense_summaries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.monthly_expense_summaries_id_seq OWNED BY public.monthly_expense_summaries.id;


--
-- Name: monthly_expense_tokens; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.monthly_expense_tokens OWNER TO neondb_owner;

--
-- Name: monthly_expense_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.monthly_expense_tokens_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.monthly_expense_tokens_id_seq OWNER TO neondb_owner;

--
-- Name: monthly_expense_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.monthly_expense_tokens_id_seq OWNED BY public.monthly_expense_tokens.id;


--
-- Name: monthly_expenses; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.monthly_expenses OWNER TO neondb_owner;

--
-- Name: monthly_expenses_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.monthly_expenses_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.monthly_expenses_id_seq OWNER TO neondb_owner;

--
-- Name: monthly_expenses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.monthly_expenses_id_seq OWNED BY public.monthly_expenses.id;


--
-- Name: network_printers; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.network_printers OWNER TO neondb_owner;

--
-- Name: network_printers_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.network_printers_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.network_printers_id_seq OWNER TO neondb_owner;

--
-- Name: network_printers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.network_printers_id_seq OWNED BY public.network_printers.id;


--
-- Name: product_conservations; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.product_conservations (
    id integer NOT NULL,
    conservation_type public.conservationtype NOT NULL,
    hours_valid integer NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    product_id integer NOT NULL
);


ALTER TABLE public.product_conservations OWNER TO neondb_owner;

--
-- Name: product_conservations_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.product_conservations_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.product_conservations_id_seq OWNER TO neondb_owner;

--
-- Name: product_conservations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.product_conservations_id_seq OWNED BY public.product_conservations.id;


--
-- Name: product_labels; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.product_labels (
    id integer NOT NULL,
    created_at timestamp without time zone,
    expiry_date date NOT NULL,
    product_id integer NOT NULL,
    local_user_id integer NOT NULL,
    conservation_type public.conservationtype NOT NULL
);


ALTER TABLE public.product_labels OWNER TO neondb_owner;

--
-- Name: product_labels_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.product_labels_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.product_labels_id_seq OWNER TO neondb_owner;

--
-- Name: product_labels_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.product_labels_id_seq OWNED BY public.product_labels.id;


--
-- Name: products; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.products OWNER TO neondb_owner;

--
-- Name: products_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.products_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.products_id_seq OWNER TO neondb_owner;

--
-- Name: products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.products_id_seq OWNED BY public.products.id;


--
-- Name: task_completions; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.task_completions (
    id integer NOT NULL,
    completion_date timestamp without time zone,
    notes text,
    task_id integer NOT NULL,
    local_user_id integer NOT NULL
);


ALTER TABLE public.task_completions OWNER TO neondb_owner;

--
-- Name: task_completions_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.task_completions_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.task_completions_id_seq OWNER TO neondb_owner;

--
-- Name: task_completions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.task_completions_id_seq OWNED BY public.task_completions.id;


--
-- Name: task_groups; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.task_groups OWNER TO neondb_owner;

--
-- Name: task_groups_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.task_groups_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.task_groups_id_seq OWNER TO neondb_owner;

--
-- Name: task_groups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.task_groups_id_seq OWNED BY public.task_groups.id;


--
-- Name: task_instances; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.task_instances OWNER TO neondb_owner;

--
-- Name: task_instances_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.task_instances_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.task_instances_id_seq OWNER TO neondb_owner;

--
-- Name: task_instances_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.task_instances_id_seq OWNED BY public.task_instances.id;


--
-- Name: task_monthdays; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.task_monthdays (
    id integer NOT NULL,
    day_of_month integer NOT NULL,
    task_id integer NOT NULL
);


ALTER TABLE public.task_monthdays OWNER TO neondb_owner;

--
-- Name: task_monthdays_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.task_monthdays_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.task_monthdays_id_seq OWNER TO neondb_owner;

--
-- Name: task_monthdays_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.task_monthdays_id_seq OWNED BY public.task_monthdays.id;


--
-- Name: task_schedules; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.task_schedules OWNER TO neondb_owner;

--
-- Name: task_schedules_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.task_schedules_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.task_schedules_id_seq OWNER TO neondb_owner;

--
-- Name: task_schedules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.task_schedules_id_seq OWNED BY public.task_schedules.id;


--
-- Name: task_weekdays; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.task_weekdays (
    id integer NOT NULL,
    day_of_week public.weekday NOT NULL,
    task_id integer NOT NULL
);


ALTER TABLE public.task_weekdays OWNER TO neondb_owner;

--
-- Name: task_weekdays_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.task_weekdays_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.task_weekdays_id_seq OWNER TO neondb_owner;

--
-- Name: task_weekdays_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.task_weekdays_id_seq OWNED BY public.task_weekdays.id;


--
-- Name: tasks; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.tasks OWNER TO neondb_owner;

--
-- Name: tasks_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.tasks_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tasks_id_seq OWNER TO neondb_owner;

--
-- Name: tasks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.tasks_id_seq OWNED BY public.tasks.id;


--
-- Name: user_companies; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.user_companies (
    user_id integer NOT NULL,
    company_id integer NOT NULL
);


ALTER TABLE public.user_companies OWNER TO neondb_owner;

--
-- Name: users; Type: TABLE; Schema: public; Owner: neondb_owner
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


ALTER TABLE public.users OWNER TO neondb_owner;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO neondb_owner;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: activity_logs id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.activity_logs ALTER COLUMN id SET DEFAULT nextval('public.activity_logs_id_seq'::regclass);


--
-- Name: cash_register_summaries id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_register_summaries ALTER COLUMN id SET DEFAULT nextval('public.cash_register_summaries_id_seq'::regclass);


--
-- Name: cash_register_tokens id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_register_tokens ALTER COLUMN id SET DEFAULT nextval('public.cash_register_tokens_id_seq'::regclass);


--
-- Name: cash_registers id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_registers ALTER COLUMN id SET DEFAULT nextval('public.cash_registers_id_seq'::regclass);


--
-- Name: checkpoint_incidents id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoint_incidents ALTER COLUMN id SET DEFAULT nextval('public.checkpoint_incidents_id_seq'::regclass);


--
-- Name: checkpoint_original_records id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoint_original_records ALTER COLUMN id SET DEFAULT nextval('public.checkpoint_original_records_id_seq'::regclass);


--
-- Name: checkpoint_records id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoint_records ALTER COLUMN id SET DEFAULT nextval('public.checkpoint_records_id_seq'::regclass);


--
-- Name: checkpoints id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoints ALTER COLUMN id SET DEFAULT nextval('public.checkpoints_id_seq'::regclass);


--
-- Name: companies id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.companies ALTER COLUMN id SET DEFAULT nextval('public.companies_id_seq'::regclass);


--
-- Name: company_work_hours id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.company_work_hours ALTER COLUMN id SET DEFAULT nextval('public.company_work_hours_id_seq'::regclass);


--
-- Name: employee_check_ins id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_check_ins ALTER COLUMN id SET DEFAULT nextval('public.employee_check_ins_id_seq'::regclass);


--
-- Name: employee_contract_hours id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_contract_hours ALTER COLUMN id SET DEFAULT nextval('public.employee_contract_hours_id_seq'::regclass);


--
-- Name: employee_documents id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_documents ALTER COLUMN id SET DEFAULT nextval('public.employee_documents_id_seq'::regclass);


--
-- Name: employee_history id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_history ALTER COLUMN id SET DEFAULT nextval('public.employee_history_id_seq'::regclass);


--
-- Name: employee_notes id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_notes ALTER COLUMN id SET DEFAULT nextval('public.employee_notes_id_seq'::regclass);


--
-- Name: employee_schedules id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_schedules ALTER COLUMN id SET DEFAULT nextval('public.employee_schedules_id_seq'::regclass);


--
-- Name: employee_vacations id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_vacations ALTER COLUMN id SET DEFAULT nextval('public.employee_vacations_id_seq'::regclass);


--
-- Name: employee_work_hours id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_work_hours ALTER COLUMN id SET DEFAULT nextval('public.employee_work_hours_id_seq'::regclass);


--
-- Name: employees id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employees ALTER COLUMN id SET DEFAULT nextval('public.employees_id_seq'::regclass);


--
-- Name: expense_categories id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.expense_categories ALTER COLUMN id SET DEFAULT nextval('public.expense_categories_id_seq'::regclass);


--
-- Name: fixed_expenses id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.fixed_expenses ALTER COLUMN id SET DEFAULT nextval('public.fixed_expenses_id_seq'::regclass);


--
-- Name: label_templates id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.label_templates ALTER COLUMN id SET DEFAULT nextval('public.label_templates_id_seq'::regclass);


--
-- Name: local_users id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.local_users ALTER COLUMN id SET DEFAULT nextval('public.local_users_id_seq'::regclass);


--
-- Name: location_access_tokens id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.location_access_tokens ALTER COLUMN id SET DEFAULT nextval('public.location_access_tokens_id_seq'::regclass);


--
-- Name: locations id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.locations ALTER COLUMN id SET DEFAULT nextval('public.locations_id_seq'::regclass);


--
-- Name: monthly_expense_summaries id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.monthly_expense_summaries ALTER COLUMN id SET DEFAULT nextval('public.monthly_expense_summaries_id_seq'::regclass);


--
-- Name: monthly_expense_tokens id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.monthly_expense_tokens ALTER COLUMN id SET DEFAULT nextval('public.monthly_expense_tokens_id_seq'::regclass);


--
-- Name: monthly_expenses id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.monthly_expenses ALTER COLUMN id SET DEFAULT nextval('public.monthly_expenses_id_seq'::regclass);


--
-- Name: network_printers id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.network_printers ALTER COLUMN id SET DEFAULT nextval('public.network_printers_id_seq'::regclass);


--
-- Name: product_conservations id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.product_conservations ALTER COLUMN id SET DEFAULT nextval('public.product_conservations_id_seq'::regclass);


--
-- Name: product_labels id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.product_labels ALTER COLUMN id SET DEFAULT nextval('public.product_labels_id_seq'::regclass);


--
-- Name: products id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.products ALTER COLUMN id SET DEFAULT nextval('public.products_id_seq'::regclass);


--
-- Name: task_completions id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_completions ALTER COLUMN id SET DEFAULT nextval('public.task_completions_id_seq'::regclass);


--
-- Name: task_groups id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_groups ALTER COLUMN id SET DEFAULT nextval('public.task_groups_id_seq'::regclass);


--
-- Name: task_instances id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_instances ALTER COLUMN id SET DEFAULT nextval('public.task_instances_id_seq'::regclass);


--
-- Name: task_monthdays id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_monthdays ALTER COLUMN id SET DEFAULT nextval('public.task_monthdays_id_seq'::regclass);


--
-- Name: task_schedules id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_schedules ALTER COLUMN id SET DEFAULT nextval('public.task_schedules_id_seq'::regclass);


--
-- Name: task_weekdays id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_weekdays ALTER COLUMN id SET DEFAULT nextval('public.task_weekdays_id_seq'::regclass);


--
-- Name: tasks id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tasks ALTER COLUMN id SET DEFAULT nextval('public.tasks_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: activity_logs activity_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.activity_logs
    ADD CONSTRAINT activity_logs_pkey PRIMARY KEY (id);


--
-- Name: cash_register_summaries cash_register_summaries_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_register_summaries
    ADD CONSTRAINT cash_register_summaries_pkey PRIMARY KEY (id);


--
-- Name: cash_register_tokens cash_register_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_register_tokens
    ADD CONSTRAINT cash_register_tokens_pkey PRIMARY KEY (id);


--
-- Name: cash_register_tokens cash_register_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_register_tokens
    ADD CONSTRAINT cash_register_tokens_token_key UNIQUE (token);


--
-- Name: cash_registers cash_registers_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_registers
    ADD CONSTRAINT cash_registers_pkey PRIMARY KEY (id);


--
-- Name: checkpoint_incidents checkpoint_incidents_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoint_incidents
    ADD CONSTRAINT checkpoint_incidents_pkey PRIMARY KEY (id);


--
-- Name: checkpoint_original_records checkpoint_original_records_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoint_original_records
    ADD CONSTRAINT checkpoint_original_records_pkey PRIMARY KEY (id);


--
-- Name: checkpoint_records checkpoint_records_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoint_records
    ADD CONSTRAINT checkpoint_records_pkey PRIMARY KEY (id);


--
-- Name: checkpoints checkpoints_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoints
    ADD CONSTRAINT checkpoints_pkey PRIMARY KEY (id);


--
-- Name: checkpoints checkpoints_username_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoints
    ADD CONSTRAINT checkpoints_username_key UNIQUE (username);


--
-- Name: companies companies_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.companies
    ADD CONSTRAINT companies_pkey PRIMARY KEY (id);


--
-- Name: companies companies_tax_id_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.companies
    ADD CONSTRAINT companies_tax_id_key UNIQUE (tax_id);


--
-- Name: company_work_hours company_work_hours_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.company_work_hours
    ADD CONSTRAINT company_work_hours_pkey PRIMARY KEY (id);


--
-- Name: employee_check_ins employee_check_ins_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_check_ins
    ADD CONSTRAINT employee_check_ins_pkey PRIMARY KEY (id);


--
-- Name: employee_contract_hours employee_contract_hours_employee_id_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_contract_hours
    ADD CONSTRAINT employee_contract_hours_employee_id_key UNIQUE (employee_id);


--
-- Name: employee_contract_hours employee_contract_hours_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_contract_hours
    ADD CONSTRAINT employee_contract_hours_pkey PRIMARY KEY (id);


--
-- Name: employee_documents employee_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_documents
    ADD CONSTRAINT employee_documents_pkey PRIMARY KEY (id);


--
-- Name: employee_history employee_history_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_history
    ADD CONSTRAINT employee_history_pkey PRIMARY KEY (id);


--
-- Name: employee_notes employee_notes_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_notes
    ADD CONSTRAINT employee_notes_pkey PRIMARY KEY (id);


--
-- Name: employee_schedules employee_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_schedules
    ADD CONSTRAINT employee_schedules_pkey PRIMARY KEY (id);


--
-- Name: employee_vacations employee_vacations_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_vacations
    ADD CONSTRAINT employee_vacations_pkey PRIMARY KEY (id);


--
-- Name: employee_work_hours employee_work_hours_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_work_hours
    ADD CONSTRAINT employee_work_hours_pkey PRIMARY KEY (id);


--
-- Name: employees employees_dni_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_dni_key UNIQUE (dni);


--
-- Name: employees employees_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_pkey PRIMARY KEY (id);


--
-- Name: employees employees_user_id_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_user_id_key UNIQUE (user_id);


--
-- Name: expense_categories expense_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.expense_categories
    ADD CONSTRAINT expense_categories_pkey PRIMARY KEY (id);


--
-- Name: fixed_expenses fixed_expenses_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.fixed_expenses
    ADD CONSTRAINT fixed_expenses_pkey PRIMARY KEY (id);


--
-- Name: label_templates label_templates_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.label_templates
    ADD CONSTRAINT label_templates_pkey PRIMARY KEY (id);


--
-- Name: local_users local_users_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.local_users
    ADD CONSTRAINT local_users_pkey PRIMARY KEY (id);


--
-- Name: location_access_tokens location_access_tokens_location_id_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.location_access_tokens
    ADD CONSTRAINT location_access_tokens_location_id_key UNIQUE (location_id);


--
-- Name: location_access_tokens location_access_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.location_access_tokens
    ADD CONSTRAINT location_access_tokens_pkey PRIMARY KEY (id);


--
-- Name: location_access_tokens location_access_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.location_access_tokens
    ADD CONSTRAINT location_access_tokens_token_key UNIQUE (token);


--
-- Name: locations locations_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_pkey PRIMARY KEY (id);


--
-- Name: locations locations_portal_username_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_portal_username_key UNIQUE (portal_username);


--
-- Name: monthly_expense_summaries monthly_expense_summaries_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.monthly_expense_summaries
    ADD CONSTRAINT monthly_expense_summaries_pkey PRIMARY KEY (id);


--
-- Name: monthly_expense_tokens monthly_expense_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.monthly_expense_tokens
    ADD CONSTRAINT monthly_expense_tokens_pkey PRIMARY KEY (id);


--
-- Name: monthly_expense_tokens monthly_expense_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.monthly_expense_tokens
    ADD CONSTRAINT monthly_expense_tokens_token_key UNIQUE (token);


--
-- Name: monthly_expenses monthly_expenses_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.monthly_expenses
    ADD CONSTRAINT monthly_expenses_pkey PRIMARY KEY (id);


--
-- Name: network_printers network_printers_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.network_printers
    ADD CONSTRAINT network_printers_pkey PRIMARY KEY (id);


--
-- Name: product_conservations product_conservations_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.product_conservations
    ADD CONSTRAINT product_conservations_pkey PRIMARY KEY (id);


--
-- Name: product_labels product_labels_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.product_labels
    ADD CONSTRAINT product_labels_pkey PRIMARY KEY (id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: task_completions task_completions_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_completions
    ADD CONSTRAINT task_completions_pkey PRIMARY KEY (id);


--
-- Name: task_groups task_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_groups
    ADD CONSTRAINT task_groups_pkey PRIMARY KEY (id);


--
-- Name: task_instances task_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_instances
    ADD CONSTRAINT task_instances_pkey PRIMARY KEY (id);


--
-- Name: task_monthdays task_monthdays_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_monthdays
    ADD CONSTRAINT task_monthdays_pkey PRIMARY KEY (id);


--
-- Name: task_schedules task_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_schedules
    ADD CONSTRAINT task_schedules_pkey PRIMARY KEY (id);


--
-- Name: task_weekdays task_weekdays_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_weekdays
    ADD CONSTRAINT task_weekdays_pkey PRIMARY KEY (id);


--
-- Name: tasks tasks_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_pkey PRIMARY KEY (id);


--
-- Name: company_work_hours uq_company_period; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.company_work_hours
    ADD CONSTRAINT uq_company_period UNIQUE (company_id, year, month, week_number);


--
-- Name: employee_work_hours uq_employee_period; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_work_hours
    ADD CONSTRAINT uq_employee_period UNIQUE (employee_id, year, month, week_number);


--
-- Name: cash_register_summaries uq_summary_period; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_register_summaries
    ADD CONSTRAINT uq_summary_period UNIQUE (company_id, year, month, week_number);


--
-- Name: user_companies user_companies_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.user_companies
    ADD CONSTRAINT user_companies_pkey PRIMARY KEY (user_id, company_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: idx_cash_register_company; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_cash_register_company ON public.cash_registers USING btree (company_id);


--
-- Name: idx_cash_register_date; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_cash_register_date ON public.cash_registers USING btree (date);


--
-- Name: idx_checkpoint_records_checkpoint; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_checkpoint_records_checkpoint ON public.checkpoint_records USING btree (checkpoint_id);


--
-- Name: idx_checkpoint_records_dates; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_checkpoint_records_dates ON public.checkpoint_records USING btree (check_in_time, check_out_time);


--
-- Name: idx_checkpoint_records_employee; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_checkpoint_records_employee ON public.checkpoint_records USING btree (employee_id);


--
-- Name: idx_company_month; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_company_month ON public.company_work_hours USING btree (company_id, year, month);


--
-- Name: idx_company_week; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_company_week ON public.company_work_hours USING btree (company_id, year, week_number);


--
-- Name: idx_company_year; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_company_year ON public.company_work_hours USING btree (company_id, year);


--
-- Name: idx_employee_checkins_employee; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_employee_checkins_employee ON public.employee_check_ins USING btree (employee_id);


--
-- Name: idx_employee_month; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_employee_month ON public.employee_work_hours USING btree (employee_id, year, month);


--
-- Name: idx_employee_schedules_employee; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_employee_schedules_employee ON public.employee_schedules USING btree (employee_id);


--
-- Name: idx_employee_week; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_employee_week ON public.employee_work_hours USING btree (employee_id, year, week_number);


--
-- Name: idx_employee_year; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_employee_year ON public.employee_work_hours USING btree (employee_id, year);


--
-- Name: idx_employees_company; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_employees_company ON public.employees USING btree (company_id);


--
-- Name: idx_employees_user; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_employees_user ON public.employees USING btree (user_id);


--
-- Name: idx_products_location; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_products_location ON public.products USING btree (location_id);


--
-- Name: idx_summary_company; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_summary_company ON public.cash_register_summaries USING btree (company_id);


--
-- Name: idx_summary_year_month; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_summary_year_month ON public.cash_register_summaries USING btree (year, month);


--
-- Name: idx_task_instances_status; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_task_instances_status ON public.task_instances USING btree (status);


--
-- Name: idx_task_instances_task; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_task_instances_task ON public.task_instances USING btree (task_id);


--
-- Name: idx_tasks_group; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_tasks_group ON public.tasks USING btree (group_id);


--
-- Name: idx_tasks_location; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_tasks_location ON public.tasks USING btree (location_id);


--
-- Name: activity_logs activity_logs_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.activity_logs
    ADD CONSTRAINT activity_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: cash_register_summaries cash_register_summaries_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_register_summaries
    ADD CONSTRAINT cash_register_summaries_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: cash_register_tokens cash_register_tokens_cash_register_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_register_tokens
    ADD CONSTRAINT cash_register_tokens_cash_register_id_fkey FOREIGN KEY (cash_register_id) REFERENCES public.cash_registers(id);


--
-- Name: cash_register_tokens cash_register_tokens_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_register_tokens
    ADD CONSTRAINT cash_register_tokens_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: cash_register_tokens cash_register_tokens_created_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_register_tokens
    ADD CONSTRAINT cash_register_tokens_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.users(id);


--
-- Name: cash_register_tokens cash_register_tokens_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_register_tokens
    ADD CONSTRAINT cash_register_tokens_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: cash_registers cash_registers_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_registers
    ADD CONSTRAINT cash_registers_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: cash_registers cash_registers_confirmed_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_registers
    ADD CONSTRAINT cash_registers_confirmed_by_id_fkey FOREIGN KEY (confirmed_by_id) REFERENCES public.users(id);


--
-- Name: cash_registers cash_registers_created_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_registers
    ADD CONSTRAINT cash_registers_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.users(id);


--
-- Name: cash_registers cash_registers_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_registers
    ADD CONSTRAINT cash_registers_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: cash_registers cash_registers_token_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cash_registers
    ADD CONSTRAINT cash_registers_token_id_fkey FOREIGN KEY (token_id) REFERENCES public.cash_register_tokens(id);


--
-- Name: checkpoint_incidents checkpoint_incidents_record_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoint_incidents
    ADD CONSTRAINT checkpoint_incidents_record_id_fkey FOREIGN KEY (record_id) REFERENCES public.checkpoint_records(id);


--
-- Name: checkpoint_incidents checkpoint_incidents_resolved_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoint_incidents
    ADD CONSTRAINT checkpoint_incidents_resolved_by_id_fkey FOREIGN KEY (resolved_by_id) REFERENCES public.users(id);


--
-- Name: checkpoint_original_records checkpoint_original_records_adjusted_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoint_original_records
    ADD CONSTRAINT checkpoint_original_records_adjusted_by_id_fkey FOREIGN KEY (adjusted_by_id) REFERENCES public.users(id);


--
-- Name: checkpoint_original_records checkpoint_original_records_record_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoint_original_records
    ADD CONSTRAINT checkpoint_original_records_record_id_fkey FOREIGN KEY (record_id) REFERENCES public.checkpoint_records(id);


--
-- Name: checkpoint_records checkpoint_records_checkpoint_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoint_records
    ADD CONSTRAINT checkpoint_records_checkpoint_id_fkey FOREIGN KEY (checkpoint_id) REFERENCES public.checkpoints(id);


--
-- Name: checkpoint_records checkpoint_records_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoint_records
    ADD CONSTRAINT checkpoint_records_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: checkpoints checkpoints_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.checkpoints
    ADD CONSTRAINT checkpoints_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: company_work_hours company_work_hours_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.company_work_hours
    ADD CONSTRAINT company_work_hours_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: employee_check_ins employee_check_ins_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_check_ins
    ADD CONSTRAINT employee_check_ins_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_contract_hours employee_contract_hours_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_contract_hours
    ADD CONSTRAINT employee_contract_hours_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_documents employee_documents_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_documents
    ADD CONSTRAINT employee_documents_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_history employee_history_changed_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_history
    ADD CONSTRAINT employee_history_changed_by_id_fkey FOREIGN KEY (changed_by_id) REFERENCES public.users(id);


--
-- Name: employee_history employee_history_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_history
    ADD CONSTRAINT employee_history_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_notes employee_notes_created_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_notes
    ADD CONSTRAINT employee_notes_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.users(id);


--
-- Name: employee_notes employee_notes_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_notes
    ADD CONSTRAINT employee_notes_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_schedules employee_schedules_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_schedules
    ADD CONSTRAINT employee_schedules_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_vacations employee_vacations_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_vacations
    ADD CONSTRAINT employee_vacations_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employee_work_hours employee_work_hours_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_work_hours
    ADD CONSTRAINT employee_work_hours_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: employee_work_hours employee_work_hours_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employee_work_hours
    ADD CONSTRAINT employee_work_hours_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employees employees_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: employees employees_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: expense_categories expense_categories_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.expense_categories
    ADD CONSTRAINT expense_categories_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: fixed_expenses fixed_expenses_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.fixed_expenses
    ADD CONSTRAINT fixed_expenses_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.expense_categories(id);


--
-- Name: fixed_expenses fixed_expenses_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.fixed_expenses
    ADD CONSTRAINT fixed_expenses_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: label_templates label_templates_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.label_templates
    ADD CONSTRAINT label_templates_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);


--
-- Name: local_users local_users_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.local_users
    ADD CONSTRAINT local_users_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);


--
-- Name: location_access_tokens location_access_tokens_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.location_access_tokens
    ADD CONSTRAINT location_access_tokens_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.checkpoints(id) ON DELETE CASCADE;


--
-- Name: locations locations_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: monthly_expense_summaries monthly_expense_summaries_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.monthly_expense_summaries
    ADD CONSTRAINT monthly_expense_summaries_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: monthly_expense_tokens monthly_expense_tokens_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.monthly_expense_tokens
    ADD CONSTRAINT monthly_expense_tokens_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.expense_categories(id);


--
-- Name: monthly_expense_tokens monthly_expense_tokens_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.monthly_expense_tokens
    ADD CONSTRAINT monthly_expense_tokens_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: monthly_expenses monthly_expenses_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.monthly_expenses
    ADD CONSTRAINT monthly_expenses_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.expense_categories(id);


--
-- Name: monthly_expenses monthly_expenses_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.monthly_expenses
    ADD CONSTRAINT monthly_expenses_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: network_printers network_printers_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.network_printers
    ADD CONSTRAINT network_printers_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id) ON DELETE CASCADE;


--
-- Name: product_conservations product_conservations_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.product_conservations
    ADD CONSTRAINT product_conservations_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: product_labels product_labels_local_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.product_labels
    ADD CONSTRAINT product_labels_local_user_id_fkey FOREIGN KEY (local_user_id) REFERENCES public.local_users(id);


--
-- Name: product_labels product_labels_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.product_labels
    ADD CONSTRAINT product_labels_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: products products_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);


--
-- Name: task_completions task_completions_local_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_completions
    ADD CONSTRAINT task_completions_local_user_id_fkey FOREIGN KEY (local_user_id) REFERENCES public.local_users(id);


--
-- Name: task_completions task_completions_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_completions
    ADD CONSTRAINT task_completions_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);


--
-- Name: task_groups task_groups_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_groups
    ADD CONSTRAINT task_groups_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);


--
-- Name: task_instances task_instances_completed_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_instances
    ADD CONSTRAINT task_instances_completed_by_id_fkey FOREIGN KEY (completed_by_id) REFERENCES public.local_users(id);


--
-- Name: task_instances task_instances_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_instances
    ADD CONSTRAINT task_instances_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);


--
-- Name: task_monthdays task_monthdays_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_monthdays
    ADD CONSTRAINT task_monthdays_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);


--
-- Name: task_schedules task_schedules_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_schedules
    ADD CONSTRAINT task_schedules_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);


--
-- Name: task_weekdays task_weekdays_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.task_weekdays
    ADD CONSTRAINT task_weekdays_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id);


--
-- Name: tasks tasks_created_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.users(id);


--
-- Name: tasks tasks_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.task_groups(id);


--
-- Name: tasks tasks_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_location_id_fkey FOREIGN KEY (location_id) REFERENCES public.locations(id);


--
-- Name: user_companies user_companies_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.user_companies
    ADD CONSTRAINT user_companies_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: user_companies user_companies_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.user_companies
    ADD CONSTRAINT user_companies_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: cloud_admin
--

ALTER DEFAULT PRIVILEGES FOR ROLE cloud_admin IN SCHEMA public GRANT ALL ON SEQUENCES TO neon_superuser WITH GRANT OPTION;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: cloud_admin
--

ALTER DEFAULT PRIVILEGES FOR ROLE cloud_admin IN SCHEMA public GRANT ALL ON TABLES TO neon_superuser WITH GRANT OPTION;


--
-- PostgreSQL database dump complete
--


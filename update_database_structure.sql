-- ============================================================================
-- Script de actualización de estructura de base de datos Productiva
-- Fecha: 21/05/2025
-- ============================================================================
-- Este script actualiza la estructura de una base de datos existente de Productiva
-- Está diseñado para:
--   1. Verificar si cada elemento existe antes de intentar crearlo
--   2. Saltar elementos que ya existen (tablas, columnas, etc.)
--   3. Mantener la integridad de los datos existentes
--   4. Validar las restricciones y relaciones
-- ============================================================================

-- Función auxiliar para verificar si existe una columna
CREATE OR REPLACE FUNCTION column_exists(tbl text, col text) RETURNS boolean AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = tbl
          AND column_name = col
    );
END;
$$ LANGUAGE plpgsql;

-- Función auxiliar para verificar si existe una tabla
CREATE OR REPLACE FUNCTION table_exists(tbl text) RETURNS boolean AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = tbl
    );
END;
$$ LANGUAGE plpgsql;

-- Función auxiliar para verificar si existe un tipo enum
CREATE OR REPLACE FUNCTION enum_exists(enum_name text) RETURNS boolean AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_namespace n ON t.typnamespace = n.oid
        WHERE t.typtype = 'e'
          AND n.nspname = 'public'
          AND t.typname = enum_name
    );
END;
$$ LANGUAGE plpgsql;

-- Iniciar transacción
BEGIN;

-- Mensaje informativo
DO $$
BEGIN
    RAISE NOTICE '=== Iniciando actualización de estructura de base de datos Productiva ===';
END $$;

-- ============================================================================
-- 1. TIPOS ENUMERADOS (ENUMS)
-- ============================================================================

-- CHECKPOINT_STATUS enum (ACTIVE, DISABLED, MAINTENANCE)
DO $$
BEGIN
    IF NOT enum_exists('checkpointstatus') THEN
        RAISE NOTICE 'Creando tipo enum checkpointstatus';
        CREATE TYPE public.checkpointstatus AS ENUM (
            'ACTIVE',
            'DISABLED',
            'MAINTENANCE'
        );
    ELSE
        RAISE NOTICE 'El tipo enum checkpointstatus ya existe, saltando...';
    END IF;
END $$;

-- CHECKPOINT_INCIDENT_TYPE enum
DO $$
BEGIN
    IF NOT enum_exists('checkpointincidenttype') THEN
        RAISE NOTICE 'Creando tipo enum checkpointincidenttype';
        CREATE TYPE public.checkpointincidenttype AS ENUM (
            'MISSED_CHECKOUT',
            'LATE_CHECKIN',
            'EARLY_CHECKOUT',
            'OVERTIME',
            'MANUAL_ADJUSTMENT',
            'CONTRACT_HOURS_ADJUSTMENT'
        );
    ELSE
        RAISE NOTICE 'El tipo enum checkpointincidenttype ya existe, saltando...';
    END IF;
END $$;

-- EMPLOYEE_STATUS enum
DO $$
BEGIN
    IF NOT enum_exists('employeestatus') THEN
        RAISE NOTICE 'Creando tipo enum employeestatus';
        CREATE TYPE public.employeestatus AS ENUM (
            'ACTIVO',
            'BAJA_MEDICA',
            'EXCEDENCIA',
            'VACACIONES',
            'INACTIVO'
        );
    ELSE
        RAISE NOTICE 'El tipo enum employeestatus ya existe, saltando...';
    END IF;
END $$;

-- TASK_FREQUENCY enum
DO $$
BEGIN
    IF NOT enum_exists('taskfrequency') THEN
        RAISE NOTICE 'Creando tipo enum taskfrequency';
        CREATE TYPE public.taskfrequency AS ENUM (
            'DIARIA',
            'SEMANAL',
            'QUINCENAL',
            'MENSUAL',
            'PERSONALIZADA'
        );
    ELSE
        RAISE NOTICE 'El tipo enum taskfrequency ya existe, saltando...';
    END IF;
END $$;

-- WEEKDAY enum
DO $$
BEGIN
    IF NOT enum_exists('weekday') THEN
        RAISE NOTICE 'Creando tipo enum weekday';
        CREATE TYPE public.weekday AS ENUM (
            'LUNES',
            'MARTES',
            'MIERCOLES',
            'JUEVES',
            'VIERNES',
            'SABADO',
            'DOMINGO'
        );
    ELSE
        RAISE NOTICE 'El tipo enum weekday ya existe, saltando...';
    END IF;
END $$;

-- ============================================================================
-- 2. TABLAS PRINCIPALES
-- ============================================================================

-- Tabla USERS
DO $$
BEGIN
    IF NOT table_exists('users') THEN
        RAISE NOTICE 'Creando tabla users';
        CREATE TABLE public.users (
            id SERIAL PRIMARY KEY,
            username VARCHAR(64) NOT NULL UNIQUE,
            email VARCHAR(120) NOT NULL UNIQUE,
            password_hash VARCHAR(256) NOT NULL,
            first_name VARCHAR(64),
            last_name VARCHAR(64),
            active BOOLEAN DEFAULT TRUE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            role VARCHAR(20) DEFAULT 'user'
        );
    ELSE
        RAISE NOTICE 'La tabla users ya existe, verificando columnas...';
        
        -- Verificar y añadir columnas que podrían faltar
        IF NOT column_exists('users', 'first_name') THEN
            RAISE NOTICE 'Añadiendo columna first_name a users';
            ALTER TABLE public.users ADD COLUMN first_name VARCHAR(64);
        END IF;
        
        IF NOT column_exists('users', 'last_name') THEN
            RAISE NOTICE 'Añadiendo columna last_name a users';
            ALTER TABLE public.users ADD COLUMN last_name VARCHAR(64);
        END IF;
        
        IF NOT column_exists('users', 'active') THEN
            RAISE NOTICE 'Añadiendo columna active a users';
            ALTER TABLE public.users ADD COLUMN active BOOLEAN DEFAULT TRUE;
        END IF;
        
        IF NOT column_exists('users', 'created_at') THEN
            RAISE NOTICE 'Añadiendo columna created_at a users';
            ALTER TABLE public.users ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        END IF;
        
        IF NOT column_exists('users', 'updated_at') THEN
            RAISE NOTICE 'Añadiendo columna updated_at a users';
            ALTER TABLE public.users ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        END IF;
        
        IF NOT column_exists('users', 'role') THEN
            RAISE NOTICE 'Añadiendo columna role a users';
            ALTER TABLE public.users ADD COLUMN role VARCHAR(20) DEFAULT 'user';
        END IF;
    END IF;
END $$;

-- Tabla COMPANIES
DO $$
BEGIN
    IF NOT table_exists('companies') THEN
        RAISE NOTICE 'Creando tabla companies';
        CREATE TABLE public.companies (
            id SERIAL PRIMARY KEY,
            name VARCHAR(128) NOT NULL,
            address TEXT,
            logo_url VARCHAR(256),
            contact_email VARCHAR(120),
            contact_phone VARCHAR(20),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            hourly_employee_cost DOUBLE PRECISION DEFAULT 0.0
        );
    ELSE
        RAISE NOTICE 'La tabla companies ya existe, verificando columnas...';
        
        -- Verificar columna hourly_employee_cost (importante para el módulo de Arqueos de Caja)
        IF NOT column_exists('companies', 'hourly_employee_cost') THEN
            RAISE NOTICE 'Añadiendo columna hourly_employee_cost a companies';
            ALTER TABLE public.companies ADD COLUMN hourly_employee_cost DOUBLE PRECISION DEFAULT 0.0;
        END IF;
    END IF;
END $$;

-- Tabla EMPLOYEES
DO $$
BEGIN
    IF NOT table_exists('employees') THEN
        RAISE NOTICE 'Creando tabla employees';
        CREATE TABLE public.employees (
            id SERIAL PRIMARY KEY,
            first_name VARCHAR(64) NOT NULL,
            last_name VARCHAR(64) NOT NULL,
            email VARCHAR(120),
            phone VARCHAR(20),
            dni VARCHAR(20),
            position VARCHAR(64),
            hire_date DATE,
            status employeestatus DEFAULT 'ACTIVO',
            bank_account VARCHAR(50),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            company_id INTEGER NOT NULL,
            on_shift BOOLEAN DEFAULT FALSE,
            status_start_date VARCHAR(20),
            status_end_date VARCHAR(20),
            status_notes TEXT,
            FOREIGN KEY (company_id) REFERENCES public.companies(id)
        );
    ELSE
        RAISE NOTICE 'La tabla employees ya existe, verificando columnas...';
        
        -- Verificar columnas relacionadas con el estado del empleado
        IF NOT column_exists('employees', 'status_start_date') THEN
            RAISE NOTICE 'Añadiendo columna status_start_date a employees';
            ALTER TABLE public.employees ADD COLUMN status_start_date VARCHAR(20);
        END IF;
        
        IF NOT column_exists('employees', 'status_end_date') THEN
            RAISE NOTICE 'Añadiendo columna status_end_date a employees';
            ALTER TABLE public.employees ADD COLUMN status_end_date VARCHAR(20);
        END IF;
        
        IF NOT column_exists('employees', 'status_notes') THEN
            RAISE NOTICE 'Añadiendo columna status_notes a employees';
            ALTER TABLE public.employees ADD COLUMN status_notes TEXT;
        END IF;
        
        IF NOT column_exists('employees', 'on_shift') THEN
            RAISE NOTICE 'Añadiendo columna on_shift a employees';
            ALTER TABLE public.employees ADD COLUMN on_shift BOOLEAN DEFAULT FALSE;
        END IF;
    END IF;
END $$;

-- Tabla LOCATIONS
DO $$
BEGIN
    IF NOT table_exists('locations') THEN
        RAISE NOTICE 'Creando tabla locations';
        CREATE TABLE public.locations (
            id SERIAL PRIMARY KEY,
            name VARCHAR(128) NOT NULL,
            address TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            company_id INTEGER NOT NULL,
            username VARCHAR(64),
            password_hash VARCHAR(256),
            requires_pin BOOLEAN DEFAULT TRUE,
            FOREIGN KEY (company_id) REFERENCES public.companies(id)
        );
    ELSE
        RAISE NOTICE 'La tabla locations ya existe, verificando columnas...';
        
        -- Verificar columna requires_pin (importante para la autenticación)
        IF NOT column_exists('locations', 'requires_pin') THEN
            RAISE NOTICE 'Añadiendo columna requires_pin a locations';
            ALTER TABLE public.locations ADD COLUMN requires_pin BOOLEAN DEFAULT TRUE;
        END IF;
    END IF;
END $$;

-- Tabla CHECKPOINTS (puntos de fichaje)
DO $$
BEGIN
    IF NOT table_exists('checkpoints') THEN
        RAISE NOTICE 'Creando tabla checkpoints';
        CREATE TABLE public.checkpoints (
            id SERIAL PRIMARY KEY,
            name VARCHAR(128) NOT NULL,
            description TEXT,
            location VARCHAR(256),
            status checkpointstatus DEFAULT 'ACTIVE',
            username VARCHAR(64) NOT NULL,
            password_hash VARCHAR(256) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            company_id INTEGER NOT NULL,
            enforce_contract_hours BOOLEAN DEFAULT FALSE,
            auto_adjust_overtime BOOLEAN DEFAULT FALSE,
            operation_start_time TIME,
            operation_end_time TIME,
            enforce_operation_hours BOOLEAN DEFAULT FALSE,
            FOREIGN KEY (company_id) REFERENCES public.companies(id)
        );
    ELSE
        RAISE NOTICE 'La tabla checkpoints ya existe, verificando columnas...';
        
        -- Verificar columnas para ventana horaria
        IF NOT column_exists('checkpoints', 'operation_start_time') THEN
            RAISE NOTICE 'Añadiendo columna operation_start_time a checkpoints';
            ALTER TABLE public.checkpoints ADD COLUMN operation_start_time TIME;
        END IF;
        
        IF NOT column_exists('checkpoints', 'operation_end_time') THEN
            RAISE NOTICE 'Añadiendo columna operation_end_time a checkpoints';
            ALTER TABLE public.checkpoints ADD COLUMN operation_end_time TIME;
        END IF;
        
        IF NOT column_exists('checkpoints', 'enforce_operation_hours') THEN
            RAISE NOTICE 'Añadiendo columna enforce_operation_hours a checkpoints';
            ALTER TABLE public.checkpoints ADD COLUMN enforce_operation_hours BOOLEAN DEFAULT FALSE;
        END IF;
    END IF;
END $$;

-- Tabla CHECKPOINT_RECORDS (registros de fichaje)
DO $$
BEGIN
    IF NOT table_exists('checkpoint_records') THEN
        RAISE NOTICE 'Creando tabla checkpoint_records';
        CREATE TABLE public.checkpoint_records (
            id SERIAL PRIMARY KEY,
            check_in_time TIMESTAMP NOT NULL,
            check_out_time TIMESTAMP,
            original_check_in_time TIMESTAMP,
            original_check_out_time TIMESTAMP,
            adjusted BOOLEAN DEFAULT FALSE,
            adjustment_reason VARCHAR(256),
            notes TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            employee_id INTEGER NOT NULL,
            checkpoint_id INTEGER NOT NULL,
            signature_data TEXT,
            has_signature BOOLEAN DEFAULT FALSE,
            FOREIGN KEY (employee_id) REFERENCES public.employees(id),
            FOREIGN KEY (checkpoint_id) REFERENCES public.checkpoints(id)
        );
    ELSE
        RAISE NOTICE 'La tabla checkpoint_records ya existe, verificando columnas...';
        
        -- Verificar columnas para firmas
        IF NOT column_exists('checkpoint_records', 'signature_data') THEN
            RAISE NOTICE 'Añadiendo columna signature_data a checkpoint_records';
            ALTER TABLE public.checkpoint_records ADD COLUMN signature_data TEXT;
        END IF;
        
        IF NOT column_exists('checkpoint_records', 'has_signature') THEN
            RAISE NOTICE 'Añadiendo columna has_signature a checkpoint_records';
            ALTER TABLE public.checkpoint_records ADD COLUMN has_signature BOOLEAN DEFAULT FALSE;
        END IF;
    END IF;
END $$;

-- Tabla CHECKPOINT_ORIGINAL_RECORDS (registros originales antes de ajustes)
DO $$
BEGIN
    IF NOT table_exists('checkpoint_original_records') THEN
        RAISE NOTICE 'Creando tabla checkpoint_original_records';
        CREATE TABLE public.checkpoint_original_records (
            id SERIAL PRIMARY KEY,
            record_id INTEGER NOT NULL,
            original_check_in_time TIMESTAMP NOT NULL,
            original_check_out_time TIMESTAMP,
            original_signature_data TEXT,
            original_has_signature BOOLEAN DEFAULT FALSE,
            original_notes TEXT,
            adjusted_at TIMESTAMP,
            adjusted_by_id INTEGER,
            adjustment_reason VARCHAR(256),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            hours_worked DOUBLE PRECISION DEFAULT 0.0,
            FOREIGN KEY (record_id) REFERENCES public.checkpoint_records(id)
        );
    ELSE
        RAISE NOTICE 'La tabla checkpoint_original_records ya existe, verificando columnas...';
        
        -- Verificar columna hours_worked
        IF NOT column_exists('checkpoint_original_records', 'hours_worked') THEN
            RAISE NOTICE 'Añadiendo columna hours_worked a checkpoint_original_records';
            ALTER TABLE public.checkpoint_original_records ADD COLUMN hours_worked DOUBLE PRECISION DEFAULT 0.0;
        END IF;
    END IF;
END $$;

-- Tabla EMPLOYEE_WORK_HOURS (horas trabajadas por empleado)
DO $$
BEGIN
    IF NOT table_exists('employee_work_hours') THEN
        RAISE NOTICE 'Creando tabla employee_work_hours';
        CREATE TABLE public.employee_work_hours (
            id SERIAL PRIMARY KEY,
            employee_id INTEGER NOT NULL,
            date VARCHAR(20) NOT NULL,
            hours_worked DOUBLE PRECISION DEFAULT 0.0,
            week_number INTEGER,
            month_number INTEGER,
            year_number INTEGER,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (employee_id) REFERENCES public.employees(id)
        );
    ELSE
        RAISE NOTICE 'La tabla employee_work_hours ya existe, verificando columnas...';
    END IF;
END $$;

-- Tabla EMPLOYEE_CONTRACT_HOURS (horas contratadas por empleado)
DO $$
BEGIN
    IF NOT table_exists('employee_contract_hours') THEN
        RAISE NOTICE 'Creando tabla employee_contract_hours';
        CREATE TABLE public.employee_contract_hours (
            id SERIAL PRIMARY KEY,
            employee_id INTEGER NOT NULL,
            weekly_hours DOUBLE PRECISION DEFAULT 40.0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (employee_id) REFERENCES public.employees(id)
        );
    ELSE
        RAISE NOTICE 'La tabla employee_contract_hours ya existe, verificando columnas...';
    END IF;
END $$;

-- ============================================================================
-- 3. TABLAS DEL MÓDULO DE ARQUEO DE CAJA
-- ============================================================================

-- Tabla CASH_REGISTERS
DO $$
BEGIN
    IF NOT table_exists('cash_registers') THEN
        RAISE NOTICE 'Creando tabla cash_registers';
        CREATE TABLE public.cash_registers (
            id SERIAL PRIMARY KEY,
            date DATE NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            total_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            cash_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            card_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            delivery_cash_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            delivery_online_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            check_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            expenses_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            expenses_notes TEXT,
            notes TEXT,
            is_confirmed BOOLEAN DEFAULT FALSE,
            confirmed_at TIMESTAMP,
            confirmed_by_id INTEGER,
            company_id INTEGER NOT NULL,
            created_by_id INTEGER,
            employee_id INTEGER,
            employee_name VARCHAR(100),
            token_id INTEGER,
            vat_percentage DOUBLE PRECISION DEFAULT 21.0 NOT NULL,
            vat_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            net_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            FOREIGN KEY (company_id) REFERENCES public.companies(id)
        );
    ELSE
        RAISE NOTICE 'La tabla cash_registers ya existe, verificando columnas...';
        
        -- Verificar columnas para IVA
        IF NOT column_exists('cash_registers', 'vat_percentage') THEN
            RAISE NOTICE 'Añadiendo columna vat_percentage a cash_registers';
            ALTER TABLE public.cash_registers ADD COLUMN vat_percentage DOUBLE PRECISION DEFAULT 21.0 NOT NULL;
        END IF;
        
        IF NOT column_exists('cash_registers', 'vat_amount') THEN
            RAISE NOTICE 'Añadiendo columna vat_amount a cash_registers';
            ALTER TABLE public.cash_registers ADD COLUMN vat_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL;
        END IF;
        
        IF NOT column_exists('cash_registers', 'net_amount') THEN
            RAISE NOTICE 'Añadiendo columna net_amount a cash_registers';
            ALTER TABLE public.cash_registers ADD COLUMN net_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL;
        END IF;
    END IF;
END $$;

-- Tabla CASH_REGISTER_SUMMARIES
DO $$
BEGIN
    IF NOT table_exists('cash_register_summaries') THEN
        RAISE NOTICE 'Creando tabla cash_register_summaries';
        CREATE TABLE public.cash_register_summaries (
            id SERIAL PRIMARY KEY,
            year INTEGER NOT NULL,
            month INTEGER NOT NULL,
            week_number INTEGER NOT NULL,
            weekly_total DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            monthly_total DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            yearly_total DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            weekly_cash DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            weekly_card DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            weekly_delivery_cash DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            weekly_delivery_online DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            weekly_check DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            weekly_expenses DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            weekly_staff_cost DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            monthly_staff_cost DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            weekly_staff_cost_percentage DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            monthly_staff_cost_percentage DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            company_id INTEGER NOT NULL,
            weekly_vat_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            weekly_net_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            monthly_vat_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            monthly_net_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
            FOREIGN KEY (company_id) REFERENCES public.companies(id)
        );
    ELSE
        RAISE NOTICE 'La tabla cash_register_summaries ya existe, verificando columnas...';
        
        -- Verificar columnas para IVA
        IF NOT column_exists('cash_register_summaries', 'weekly_vat_amount') THEN
            RAISE NOTICE 'Añadiendo columna weekly_vat_amount a cash_register_summaries';
            ALTER TABLE public.cash_register_summaries ADD COLUMN weekly_vat_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL;
        END IF;
        
        IF NOT column_exists('cash_register_summaries', 'weekly_net_amount') THEN
            RAISE NOTICE 'Añadiendo columna weekly_net_amount a cash_register_summaries';
            ALTER TABLE public.cash_register_summaries ADD COLUMN weekly_net_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL;
        END IF;
        
        IF NOT column_exists('cash_register_summaries', 'monthly_vat_amount') THEN
            RAISE NOTICE 'Añadiendo columna monthly_vat_amount a cash_register_summaries';
            ALTER TABLE public.cash_register_summaries ADD COLUMN monthly_vat_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL;
        END IF;
        
        IF NOT column_exists('cash_register_summaries', 'monthly_net_amount') THEN
            RAISE NOTICE 'Añadiendo columna monthly_net_amount a cash_register_summaries';
            ALTER TABLE public.cash_register_summaries ADD COLUMN monthly_net_amount DOUBLE PRECISION DEFAULT 0.0 NOT NULL;
        END IF;
    END IF;
END $$;

-- Tabla CASH_REGISTER_TOKENS
DO $$
BEGIN
    IF NOT table_exists('cash_register_tokens') THEN
        RAISE NOTICE 'Creando tabla cash_register_tokens';
        CREATE TABLE public.cash_register_tokens (
            id SERIAL PRIMARY KEY,
            token VARCHAR(64) NOT NULL,
            is_active BOOLEAN DEFAULT TRUE,
            expires_at TIMESTAMP,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            used_at TIMESTAMP,
            company_id INTEGER NOT NULL,
            created_by_id INTEGER,
            employee_id INTEGER,
            cash_register_id INTEGER,
            pin VARCHAR(10),
            FOREIGN KEY (company_id) REFERENCES public.companies(id)
        );
    ELSE
        RAISE NOTICE 'La tabla cash_register_tokens ya existe, verificando columnas...';
    END IF;
END $$;

-- ============================================================================
-- 4. TABLAS DEL MÓDULO DE ACCESO DIRECTO
-- ============================================================================

-- Tabla LOCATION_ACCESS_TOKENS
DO $$
BEGIN
    IF NOT table_exists('location_access_tokens') THEN
        RAISE NOTICE 'Creando tabla location_access_tokens';
        CREATE TABLE public.location_access_tokens (
            id SERIAL PRIMARY KEY,
            location_id INTEGER NOT NULL,
            token VARCHAR(64) NOT NULL,
            description TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            created_by_id INTEGER,
            last_used_at TIMESTAMP,
            is_active BOOLEAN DEFAULT TRUE,
            portal_type VARCHAR(20) NOT NULL,
            FOREIGN KEY (location_id) REFERENCES public.locations(id)
        );
    ELSE
        RAISE NOTICE 'La tabla location_access_tokens ya existe, verificando columnas...';
        
        -- Verificar columna portal_type
        IF NOT column_exists('location_access_tokens', 'portal_type') THEN
            RAISE NOTICE 'Añadiendo columna portal_type a location_access_tokens';
            ALTER TABLE public.location_access_tokens ADD COLUMN portal_type VARCHAR(20) NOT NULL DEFAULT 'tasks';
        END IF;
    END IF;
END $$;

-- ============================================================================
-- 5. TABLAS DEL MÓDULO DE IMPRESORAS DE RED
-- ============================================================================

-- Tabla NETWORK_PRINTERS
DO $$
BEGIN
    IF NOT table_exists('network_printers') THEN
        RAISE NOTICE 'Creando tabla network_printers';
        CREATE TABLE public.network_printers (
            id SERIAL PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            printer_type VARCHAR(20) NOT NULL,
            ip_address VARCHAR(50),
            port INTEGER DEFAULT 9100,
            model VARCHAR(50),
            location_id INTEGER NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            raspberry_pi_address VARCHAR(50),
            raspberry_pi_port INTEGER DEFAULT 5555,
            raspberry_pi_api_path VARCHAR(50) DEFAULT '/print',
            usb_port VARCHAR(50),
            FOREIGN KEY (location_id) REFERENCES public.locations(id)
        );
    ELSE
        RAISE NOTICE 'La tabla network_printers ya existe, verificando columnas...';
        
        -- Verificar columnas para Raspberry Pi
        IF NOT column_exists('network_printers', 'raspberry_pi_address') THEN
            RAISE NOTICE 'Añadiendo columna raspberry_pi_address a network_printers';
            ALTER TABLE public.network_printers ADD COLUMN raspberry_pi_address VARCHAR(50);
        END IF;
        
        IF NOT column_exists('network_printers', 'raspberry_pi_port') THEN
            RAISE NOTICE 'Añadiendo columna raspberry_pi_port a network_printers';
            ALTER TABLE public.network_printers ADD COLUMN raspberry_pi_port INTEGER DEFAULT 5555;
        END IF;
        
        IF NOT column_exists('network_printers', 'raspberry_pi_api_path') THEN
            RAISE NOTICE 'Añadiendo columna raspberry_pi_api_path a network_printers';
            ALTER TABLE public.network_printers ADD COLUMN raspberry_pi_api_path VARCHAR(50) DEFAULT '/print';
        END IF;
        
        IF NOT column_exists('network_printers', 'usb_port') THEN
            RAISE NOTICE 'Añadiendo columna usb_port a network_printers';
            ALTER TABLE public.network_printers ADD COLUMN usb_port VARCHAR(50);
        END IF;
    END IF;
END $$;

-- ============================================================================
-- 6. TABLAS DEL MÓDULO DE TAREAS
-- ============================================================================

-- Tabla TASKS
DO $$
BEGIN
    IF NOT table_exists('tasks') THEN
        RAISE NOTICE 'Creando tabla tasks';
        CREATE TABLE public.tasks (
            id SERIAL PRIMARY KEY,
            title VARCHAR(100) NOT NULL,
            description TEXT,
            frequency VARCHAR(20) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            location_id INTEGER NOT NULL,
            last_completed_at TIMESTAMP,
            current_week_completed BOOLEAN DEFAULT FALSE,
            current_month_completed BOOLEAN DEFAULT FALSE,
            group_id INTEGER,
            FOREIGN KEY (location_id) REFERENCES public.locations(id)
        );
    ELSE
        RAISE NOTICE 'La tabla tasks ya existe, verificando columnas...';
        
        -- Verificar columna current_month_completed
        IF NOT column_exists('tasks', 'current_month_completed') THEN
            RAISE NOTICE 'Añadiendo columna current_month_completed a tasks';
            ALTER TABLE public.tasks ADD COLUMN current_month_completed BOOLEAN DEFAULT FALSE;
        END IF;
    END IF;
END $$;

-- Tabla TASK_MONTHDAYS (para tareas mensuales en días específicos)
DO $$
BEGIN
    IF NOT table_exists('task_monthdays') THEN
        RAISE NOTICE 'Creando tabla task_monthdays';
        CREATE TABLE public.task_monthdays (
            id SERIAL PRIMARY KEY,
            task_id INTEGER NOT NULL,
            month_day INTEGER NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (task_id) REFERENCES public.tasks(id) ON DELETE CASCADE
        );
    ELSE
        RAISE NOTICE 'La tabla task_monthdays ya existe, verificando columnas...';
    END IF;
END $$;

-- Mensaje de finalización
DO $$
BEGIN
    RAISE NOTICE '=== Actualización de estructura de base de datos completada ===';
END $$;

-- Commit de transacción
COMMIT;

-- Eliminar funciones auxiliares
DROP FUNCTION IF EXISTS column_exists;
DROP FUNCTION IF EXISTS table_exists;
DROP FUNCTION IF EXISTS enum_exists;
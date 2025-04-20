#!/usr/bin/env python3
"""
Script autónomo para configurar una base de datos PostgreSQL para Productiva

Este script:
1. Contiene toda la configuración de conexión a la base de datos (no usa variables de entorno)
2. Crea todas las tablas necesarias (basado en create_tables.sql y migraciones)
3. Importa datos desde un archivo de backup
4. No depende de ningún otro archivo de la aplicación

Uso:
    python database_setup.py [--host HOSTNAME] [--port PORT] [--user USERNAME] 
                             [--password PASSWORD] [--dbname DBNAME] 
                             [--backup-file BACKUP_FILE]

Ejemplos:
    # Usar configuración por defecto
    python database_setup.py
    
    # Especificar parámetros de conexión
    python database_setup.py --host localhost --port 5432 --user postgres --password mypass --dbname productiva
    
    # Usar un archivo de backup específico
    python database_setup.py --backup-file my_backup.sql
"""

import argparse
import os
import sys
import time
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT


# Obtener configuración desde variables de entorno
def get_env_config():
    """Obtiene la configuración de conexión desde variables de entorno"""
    return {
        'host': os.environ.get('PGHOST', 'localhost'),
        'port': int(os.environ.get('PGPORT', 5432)),
        'user': os.environ.get('PGUSER', 'postgres'),
        'password': os.environ.get('PGPASSWORD', 'postgres'),
        'dbname': os.environ.get('PGDATABASE', 'productiva'),
        'backup_file': None  # Si es None, solo se crean las tablas sin importar datos
    }

# Configuración por defecto
DEFAULT_CONFIG = get_env_config()


def parse_arguments():
    """Procesa los argumentos de línea de comandos"""
    parser = argparse.ArgumentParser(description='Configurar base de datos PostgreSQL para Productiva')
    
    parser.add_argument('--host', default=DEFAULT_CONFIG['host'],
                        help=f'Hostname del servidor PostgreSQL (default: {DEFAULT_CONFIG["host"]})')
    
    parser.add_argument('--port', type=int, default=DEFAULT_CONFIG['port'],
                        help=f'Puerto del servidor PostgreSQL (default: {DEFAULT_CONFIG["port"]})')
    
    parser.add_argument('--user', default=DEFAULT_CONFIG['user'],
                        help=f'Usuario PostgreSQL (default: {DEFAULT_CONFIG["user"]})')
    
    parser.add_argument('--password', default=DEFAULT_CONFIG['password'],
                        help=f'Contraseña PostgreSQL (default: {DEFAULT_CONFIG["password"]})')
    
    parser.add_argument('--dbname', default=DEFAULT_CONFIG['dbname'],
                        help=f'Nombre de la base de datos (default: {DEFAULT_CONFIG["dbname"]})')
    
    parser.add_argument('--backup-file', default=DEFAULT_CONFIG['backup_file'],
                        help='Archivo SQL de backup para importar datos (opcional)')
    
    parser.add_argument('--drop-existing', action='store_true',
                        help='Eliminar la base de datos si ya existe')
    
    parser.add_argument('--skip-tables', action='store_true',
                        help='Omitir la creación de tablas (útil si las tablas ya existen)')
    
    parser.add_argument('--only-data', action='store_true',
                        help='Importar solo datos, omitir creación de tablas y estructura')
    
    parser.add_argument('--only-schema', action='store_true',
                        help='Importar solo esquema, omitir importación de datos')
    
    parser.add_argument('--disable-fk', action='store_true',
                        help='Deshabilitar temporalmente las restricciones de clave foránea durante la importación de datos')
    
    parser.add_argument('--ignore-errors', action='store_true',
                        help='Continuar importando incluso si hay errores en algunos comandos')
    
    return parser.parse_args()


def get_connection_string(config, include_dbname=True):
    """Construye la cadena de conexión a partir de la configuración"""
    if include_dbname:
        return f"host={config['host']} port={config['port']} user={config['user']} password={config['password']} dbname={config['dbname']}"
    else:
        return f"host={config['host']} port={config['port']} user={config['user']} password={config['password']}"


def connect_to_database(config, include_dbname=True):
    """Establece una conexión a la base de datos"""
    try:
        if include_dbname:
            conn = psycopg2.connect(
                host=config['host'],
                port=config['port'],
                user=config['user'],
                password=config['password'],
                dbname=config['dbname']
            )
        else:
            # Conectar sin especificar la base de datos (para poder crearla)
            conn = psycopg2.connect(
                host=config['host'],
                port=config['port'],
                user=config['user'],
                password=config['password']
            )
        
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        return conn
    except Exception as e:
        print(f"Error al conectar a la base de datos: {str(e)}")
        return None


def database_exists(config):
    """Verifica si la base de datos ya existe"""
    try:
        # Conectar a postgres (base de datos por defecto)
        conn = connect_to_database({**config, 'dbname': 'postgres'})
        if not conn:
            return False
        
        cursor = conn.cursor()
        
        # Verificar si la base de datos existe
        cursor.execute("SELECT 1 FROM pg_database WHERE datname = %s", (config['dbname'],))
        exists = cursor.fetchone() is not None
        
        cursor.close()
        conn.close()
        
        return exists
    except Exception as e:
        print(f"Error al verificar existencia de base de datos: {str(e)}")
        return False


def create_database(config):
    """Crea la base de datos si no existe"""
    try:
        # Primero verificamos si la base de datos ya existe
        if database_exists(config):
            print(f"La base de datos '{config['dbname']}' ya existe.")
            return True
        
        # Conectar a postgres para poder crear la nueva base de datos
        conn = connect_to_database({**config, 'dbname': 'postgres'})
        if not conn:
            return False
        
        cursor = conn.cursor()
        
        # Crear la base de datos
        print(f"Creando base de datos '{config['dbname']}'...")
        cursor.execute(f"CREATE DATABASE {config['dbname']} WITH ENCODING='UTF8'")
        
        cursor.close()
        conn.close()
        
        print(f"Base de datos '{config['dbname']}' creada exitosamente.")
        return True
    except Exception as e:
        print(f"Error al crear la base de datos: {str(e)}")
        return False


def drop_database(config):
    """Elimina la base de datos si existe"""
    try:
        # Verificar si la base de datos existe
        if not database_exists(config):
            print(f"La base de datos '{config['dbname']}' no existe.")
            return True
        
        # Conectar a postgres para poder eliminar la base de datos
        conn = connect_to_database({**config, 'dbname': 'postgres'})
        if not conn:
            return False
        
        cursor = conn.cursor()
        
        # Intentar cerrar todas las conexiones a la base de datos
        print(f"Cerrando conexiones a la base de datos '{config['dbname']}'...")
        cursor.execute(f"""
            SELECT pg_terminate_backend(pg_stat_activity.pid)
            FROM pg_stat_activity
            WHERE pg_stat_activity.datname = '{config['dbname']}'
            AND pid <> pg_backend_pid()
        """)
        
        # Eliminar la base de datos
        print(f"Eliminando base de datos '{config['dbname']}'...")
        cursor.execute(f"DROP DATABASE {config['dbname']}")
        
        cursor.close()
        conn.close()
        
        print(f"Base de datos '{config['dbname']}' eliminada exitosamente.")
        return True
    except Exception as e:
        print(f"Error al eliminar la base de datos: {str(e)}")
        return False


def create_tables(config):
    """Crea todas las tablas necesarias para la aplicación"""
    try:
        # Conectar a la base de datos
        conn = connect_to_database(config)
        if not conn:
            return False
        
        cursor = conn.cursor()
        
        # Definición completa de todas las tablas, basada en create_tables.sql y migraciones
        # Esta es una versión consolidada que incluye todas las tablas y columnas
        sql_create_tables = """
-- Definición de tipos enumerados
DO $$
BEGIN
    -- Verificar si el tipo existe antes de crearlo
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('admin', 'gerente', 'empleado');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contract_type') THEN
        CREATE TYPE contract_type AS ENUM ('INDEFINIDO', 'TEMPORAL', 'PRACTICAS', 'FORMACION', 'OBRA');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'employee_status') THEN
        CREATE TYPE employee_status AS ENUM ('activo', 'baja_medica', 'excedencia', 'vacaciones', 'inactivo');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'week_day') THEN
        CREATE TYPE week_day AS ENUM ('lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vacation_status') THEN
        CREATE TYPE vacation_status AS ENUM ('REGISTRADA', 'DISFRUTADA');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_status') THEN
        CREATE TYPE checkpoint_status AS ENUM ('active', 'disabled', 'maintenance');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkpoint_incident_type') THEN
        CREATE TYPE checkpoint_incident_type AS ENUM ('missed_checkout', 'late_checkin', 'early_checkout', 'overtime', 'manual_adjustment', 'contract_hours_adjustment');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_priority') THEN
        CREATE TYPE task_priority AS ENUM ('alta', 'media', 'baja');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_frequency') THEN
        CREATE TYPE task_frequency AS ENUM ('diaria', 'semanal', 'mensual', 'unica');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_status') THEN
        CREATE TYPE task_status AS ENUM ('pendiente', 'completada', 'cancelada');
    END IF;
END $$;

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(64) UNIQUE NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'empleado',
    first_name VARCHAR(64),
    last_name VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Tabla de empresas
CREATE TABLE IF NOT EXISTS companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(256),
    city VARCHAR(64),
    postal_code VARCHAR(16),
    country VARCHAR(64),
    sector VARCHAR(64),
    tax_id VARCHAR(32) UNIQUE,
    phone VARCHAR(13),
    email VARCHAR(120),
    website VARCHAR(128),
    bank_account VARCHAR(24),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Tabla de relación usuarios-empresas
CREATE TABLE IF NOT EXISTS user_companies (
    user_id INTEGER REFERENCES users(id),
    company_id INTEGER REFERENCES companies(id),
    PRIMARY KEY (user_id, company_id)
);

-- Tabla de empleados
CREATE TABLE IF NOT EXISTS employees (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    dni VARCHAR(16) UNIQUE NOT NULL,
    social_security_number VARCHAR(20),
    email VARCHAR(120),
    address VARCHAR(200),
    phone VARCHAR(20),
    position VARCHAR(64),
    contract_type VARCHAR(20) DEFAULT 'INDEFINIDO',
    bank_account VARCHAR(64),
    start_date VARCHAR(20),
    end_date VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    status VARCHAR(20) DEFAULT 'activo',
    company_id INTEGER REFERENCES companies(id) NOT NULL,
    user_id INTEGER REFERENCES users(id) UNIQUE,
    is_on_shift BOOLEAN DEFAULT FALSE,
    status_start_date VARCHAR(20),
    status_end_date VARCHAR(20),
    status_notes TEXT
);

-- Tabla de documentos de empleados
CREATE TABLE IF NOT EXISTS employee_documents (
    id SERIAL PRIMARY KEY,
    filename VARCHAR(256) NOT NULL,
    original_filename VARCHAR(256) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_type VARCHAR(64),
    file_size INTEGER,
    description VARCHAR(256),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    employee_id INTEGER REFERENCES employees(id) NOT NULL
);

-- Tabla de notas de empleados
CREATE TABLE IF NOT EXISTS employee_notes (
    id SERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    employee_id INTEGER REFERENCES employees(id) NOT NULL,
    created_by_id INTEGER REFERENCES users(id)
);

-- Tabla de historial de cambios de empleados
CREATE TABLE IF NOT EXISTS employee_history (
    id SERIAL PRIMARY KEY,
    field_name VARCHAR(64) NOT NULL,
    old_value VARCHAR(256),
    new_value VARCHAR(256),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    employee_id INTEGER REFERENCES employees(id) NOT NULL,
    changed_by_id INTEGER REFERENCES users(id)
);

-- Tabla de horarios de empleados
CREATE TABLE IF NOT EXISTS employee_schedules (
    id SERIAL PRIMARY KEY,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_working_day BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    employee_id INTEGER REFERENCES employees(id) NOT NULL
);

-- Tabla de fichajes de empleados
CREATE TABLE IF NOT EXISTS employee_check_ins (
    id SERIAL PRIMARY KEY,
    check_in_time TIMESTAMP NOT NULL,
    check_out_time TIMESTAMP,
    is_generated BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    employee_id INTEGER REFERENCES employees(id) NOT NULL
);

-- Tabla de vacaciones de empleados
CREATE TABLE IF NOT EXISTS employee_vacations (
    id SERIAL PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'REGISTRADA',
    is_signed BOOLEAN DEFAULT FALSE,
    is_enjoyed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    employee_id INTEGER REFERENCES employees(id) NOT NULL
);

-- Tabla de actividad (logs)
CREATE TABLE IF NOT EXISTS activity_logs (
    id SERIAL PRIMARY KEY,
    action VARCHAR(256) NOT NULL,
    ip_address VARCHAR(64),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INTEGER REFERENCES users(id)
);

-- Tabla de puntos de fichaje
CREATE TABLE IF NOT EXISTS checkpoints (
    id SERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    location VARCHAR(256),
    status VARCHAR(20) DEFAULT 'active',
    username VARCHAR(64) UNIQUE NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    company_id INTEGER REFERENCES companies(id) NOT NULL,
    enforce_contract_hours BOOLEAN DEFAULT FALSE,
    auto_adjust_overtime BOOLEAN DEFAULT FALSE,
    operation_start_time TIME,
    operation_end_time TIME,
    enforce_operation_hours BOOLEAN DEFAULT FALSE
);

-- Tabla de registros de fichajes
CREATE TABLE IF NOT EXISTS checkpoint_records (
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
    employee_id INTEGER REFERENCES employees(id) NOT NULL,
    checkpoint_id INTEGER REFERENCES checkpoints(id) NOT NULL,
    signature_data TEXT,
    has_signature BOOLEAN DEFAULT FALSE
);

-- Tabla de incidencias de fichajes
CREATE TABLE IF NOT EXISTS checkpoint_incidents (
    id SERIAL PRIMARY KEY,
    incident_type VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    record_id INTEGER REFERENCES checkpoint_records(id) NOT NULL,
    resolved_by_id INTEGER REFERENCES users(id)
);

-- Tabla de registros originales de fichajes
CREATE TABLE IF NOT EXISTS checkpoint_original_records (
    id SERIAL PRIMARY KEY,
    record_id INTEGER REFERENCES checkpoint_records(id) NOT NULL,
    original_check_in_time TIMESTAMP NOT NULL,
    original_check_out_time TIMESTAMP,
    original_signature_data TEXT,
    original_has_signature BOOLEAN DEFAULT FALSE,
    original_notes TEXT,
    adjusted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    adjusted_by_id INTEGER REFERENCES users(id),
    adjustment_reason VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de configuración de horas por contrato
CREATE TABLE IF NOT EXISTS employee_contract_hours (
    id SERIAL PRIMARY KEY,
    daily_hours FLOAT DEFAULT 8.0,
    weekly_hours FLOAT DEFAULT 40.0,
    allow_overtime BOOLEAN DEFAULT FALSE,
    max_overtime_daily FLOAT DEFAULT 2.0,
    use_normal_schedule BOOLEAN DEFAULT FALSE,
    normal_start_time TIME,
    normal_end_time TIME,
    use_flexibility BOOLEAN DEFAULT FALSE,
    checkin_flexibility INTEGER DEFAULT 15,
    checkout_flexibility INTEGER DEFAULT 15,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    employee_id INTEGER REFERENCES employees(id) NOT NULL UNIQUE
);

-- Tabla de ubicaciones
CREATE TABLE IF NOT EXISTS locations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(256),
    city VARCHAR(64),
    postal_code VARCHAR(16),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    portal_username VARCHAR(64),
    portal_password_hash VARCHAR(256),
    company_id INTEGER REFERENCES companies(id)
);

-- Tabla de usuarios locales
CREATE TABLE IF NOT EXISTS local_users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    username VARCHAR(128) NOT NULL,
    pin VARCHAR(256) NOT NULL,
    photo_path VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    location_id INTEGER REFERENCES locations(id) NOT NULL
);

-- Tablas para el sistema de tareas

-- Grupos de tareas
CREATE TABLE IF NOT EXISTS task_groups (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    description TEXT,
    color VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    location_id INTEGER REFERENCES locations(id) NOT NULL
);

-- Tareas
CREATE TABLE IF NOT EXISTS tasks (
    id SERIAL PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    priority VARCHAR(20),
    frequency VARCHAR(20),
    status VARCHAR(20) DEFAULT 'pendiente',
    start_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    group_id INTEGER REFERENCES task_groups(id),
    location_id INTEGER REFERENCES locations(id) NOT NULL,
    created_by_id INTEGER REFERENCES users(id)
);

-- Días de la semana para tareas
CREATE TABLE IF NOT EXISTS task_weekdays (
    id SERIAL PRIMARY KEY,
    day_of_week VARCHAR(20) NOT NULL,
    task_id INTEGER REFERENCES tasks(id) NOT NULL
);

-- Programación de tareas
CREATE TABLE IF NOT EXISTS task_schedules (
    id SERIAL PRIMARY KEY,
    day_of_week VARCHAR(20),
    day_of_month INTEGER,
    start_time TIME,
    end_time TIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    task_id INTEGER REFERENCES tasks(id) NOT NULL
);

-- Instancias de tareas
CREATE TABLE IF NOT EXISTS task_instances (
    id SERIAL PRIMARY KEY,
    scheduled_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'pendiente',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    task_id INTEGER REFERENCES tasks(id) NOT NULL,
    completed_by_id INTEGER REFERENCES local_users(id)
);

-- Completación de tareas
CREATE TABLE IF NOT EXISTS task_completions (
    id SERIAL PRIMARY KEY,
    completion_date TIMESTAMP,
    notes TEXT,
    task_id INTEGER REFERENCES tasks(id) NOT NULL,
    local_user_id INTEGER REFERENCES local_users(id) NOT NULL
);

-- Tablas para el sistema de productos y etiquetas

-- Productos
CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    shelf_life_days INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    location_id INTEGER REFERENCES locations(id) NOT NULL
);

-- Conservación de productos
CREATE TABLE IF NOT EXISTS product_conservations (
    id SERIAL PRIMARY KEY,
    conservation_type VARCHAR(50) NOT NULL,
    hours_valid INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    product_id INTEGER REFERENCES products(id) NOT NULL
);

-- Plantillas de etiquetas
CREATE TABLE IF NOT EXISTS label_templates (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_default BOOLEAN DEFAULT FALSE,
    titulo_x INTEGER,
    titulo_y INTEGER,
    titulo_size INTEGER,
    titulo_bold BOOLEAN,
    conservacion_x INTEGER,
    conservacion_y INTEGER,
    conservacion_size INTEGER,
    conservacion_bold BOOLEAN,
    preparador_x INTEGER,
    preparador_y INTEGER,
    preparador_size INTEGER,
    preparador_bold BOOLEAN,
    fecha_x INTEGER,
    fecha_y INTEGER,
    fecha_size INTEGER,
    fecha_bold BOOLEAN,
    caducidad_x INTEGER,
    caducidad_y INTEGER,
    caducidad_size INTEGER,
    caducidad_bold BOOLEAN,
    caducidad2_x INTEGER,
    caducidad2_y INTEGER,
    caducidad2_size INTEGER,
    caducidad2_bold BOOLEAN,
    location_id INTEGER REFERENCES locations(id) NOT NULL
);

-- Etiquetas de productos
CREATE TABLE IF NOT EXISTS product_labels (
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATE NOT NULL,
    product_id INTEGER REFERENCES products(id) NOT NULL,
    local_user_id INTEGER REFERENCES local_users(id) NOT NULL,
    conservation_type VARCHAR(50) NOT NULL
);

-- Índices para mejora de rendimiento
CREATE INDEX IF NOT EXISTS idx_employees_company ON employees(company_id);
CREATE INDEX IF NOT EXISTS idx_employees_user ON employees(user_id);
CREATE INDEX IF NOT EXISTS idx_checkpoint_records_employee ON checkpoint_records(employee_id);
CREATE INDEX IF NOT EXISTS idx_checkpoint_records_checkpoint ON checkpoint_records(checkpoint_id);
CREATE INDEX IF NOT EXISTS idx_checkpoint_records_dates ON checkpoint_records(check_in_time, check_out_time);
CREATE INDEX IF NOT EXISTS idx_employee_checkins_employee ON employee_check_ins(employee_id);
CREATE INDEX IF NOT EXISTS idx_employee_schedules_employee ON employee_schedules(employee_id);
CREATE INDEX IF NOT EXISTS idx_task_instances_task ON task_instances(task_id);
CREATE INDEX IF NOT EXISTS idx_task_instances_status ON task_instances(status);
CREATE INDEX IF NOT EXISTS idx_tasks_location ON tasks(location_id);
CREATE INDEX IF NOT EXISTS idx_tasks_group ON tasks(group_id);
CREATE INDEX IF NOT EXISTS idx_products_location ON products(location_id);
"""
        
        # Ejecutar el script SQL para crear las tablas
        print("Creando tablas en la base de datos...")
        cursor.execute(sql_create_tables)
        
        cursor.close()
        conn.close()
        
        print("Tablas creadas exitosamente.")
        return True
    except Exception as e:
        print(f"Error al crear las tablas: {str(e)}")
        return False


def import_schema_from_backup(config):
    """Importa solo la estructura de tablas desde un archivo de backup"""
    if not config['backup_file']:
        print("No se especificó archivo de backup, omitiendo importación de esquema.")
        return True
    
    try:
        # Leer el archivo de backup
        with open(config['backup_file'], 'r') as f:
            sql_backup = f.read()
        
        # Extraer solo los comandos CREATE TABLE y ALTER TABLE
        schema_commands = []
        
        # Dividir el script en líneas
        lines = sql_backup.split('\n')
        
        # Identificar y extraer comandos de esquema (CREATE TABLE, ALTER TABLE, etc.)
        current_command = ""
        in_create_table = False
        
        for line in lines:
            line = line.strip()
            
            # Ignorar comentarios y líneas vacías
            if not line or line.startswith('--'):
                continue
            
            # Detectar inicio de comandos para crear esquema
            if line.startswith('CREATE TABLE') or line.startswith('ALTER TABLE') or line.startswith('CREATE INDEX'):
                in_create_table = True
                current_command = line
            elif in_create_table:
                current_command += " " + line
                
                # Si la línea termina con punto y coma, finaliza el comando
                if line.endswith(';'):
                    schema_commands.append(current_command)
                    current_command = ""
                    in_create_table = False
        
        # Conectar a la base de datos
        conn = connect_to_database(config)
        if not conn:
            return False
        
        cursor = conn.cursor()
        
        # Ejecutar los comandos de esquema
        print("Importando esquema (CREATE TABLE, ALTER TABLE) desde el backup...")
        for i, command in enumerate(schema_commands):
            command = command.replace('USER-DEFINED', 'VARCHAR(100)')
            try:
                cursor.execute(command)
            except Exception as e:
                print(f"Error al importar esquema, comando {i+1}: {str(e)}")
                # No abortamos, seguimos con el siguiente comando
        
        cursor.close()
        conn.close()
        
        print("Esquema importado exitosamente.")
        return True
        
    except Exception as e:
        print(f"Error al importar esquema: {str(e)}")
        return False


def import_data_from_backup(config):
    """Importa solo los datos (INSERT) desde un archivo de backup"""
    if not config['backup_file']:
        print("No se especificó archivo de backup, omitiendo importación de datos.")
        return True
    
    try:
        # Leer el archivo de backup
        with open(config['backup_file'], 'r') as f:
            sql_backup = f.read()
        
        # Extraer los comandos INSERT y organizarlos por tabla
        inserts_by_table = {}
        
        # Dividir el script en líneas y buscar INSERT
        for line in sql_backup.split('\n'):
            line = line.strip()
            if line.startswith('INSERT INTO'):
                # Extraer el nombre de la tabla
                table_name = line.split('INSERT INTO ')[1].split(' ')[0].strip('"')
                
                # Agregar a la lista de inserts para esta tabla
                if table_name not in inserts_by_table:
                    inserts_by_table[table_name] = []
                
                inserts_by_table[table_name].append(line)
        
        if not inserts_by_table:
            print("No se encontraron comandos INSERT en el archivo de backup.")
            return False
        
        # Definir el orden de importación para respetar las dependencias de clave foránea
        # Primero las tablas que no dependen de otras, luego las que dependen
        table_import_order = [
            # Primero las tablas sin dependencias
            "users",
            "companies",
            "locations",
            # Luego las tablas con dependencias simples
            "user_companies",
            "employees",
            "checkpoints",
            "local_users",
            "task_groups",
            "products",
            # Luego las tablas con dependencias más complejas
            "employee_documents",
            "employee_notes",
            "employee_history",
            "employee_schedules",
            "employee_check_ins",
            "employee_vacations",
            "checkpoint_records",
            "checkpoint_incidents",
            "checkpoint_original_records",
            "employee_contract_hours",
            "tasks",
            "task_weekdays",
            "task_schedules",
            "task_instances",
            "task_completions",
            "product_conservations",
            "product_labels",
            "label_templates",
            "activity_logs"
        ]
        
        # Agregar cualquier tabla que esté en inserts_by_table pero no en table_import_order
        for table in inserts_by_table.keys():
            if table not in table_import_order:
                table_import_order.append(table)
        
        # Conectar a la base de datos
        conn = connect_to_database(config)
        if not conn:
            return False
        
        cursor = conn.cursor()
        
        # Si se especificó deshabilitar las restricciones de clave foránea
        if config.get('disable_fk'):
            print("Deshabilitando temporalmente las restricciones de clave foránea...")
            try:
                cursor.execute("SET session_replication_role = 'replica';")
                print("✓ Restricciones de clave foránea deshabilitadas correctamente.")
            except Exception as fk_error:
                print(f"Error al deshabilitar restricciones de clave foránea: {str(fk_error)}")
                print("Continuando con las restricciones activas...")
        
        # Estadísticas globales
        total_inserts = sum(len(cmds) for cmds in inserts_by_table.values())
        successful_inserts = 0
        failed_inserts = 0
        
        print(f"Importando datos de {len(inserts_by_table)} tablas ({total_inserts} INSERTs totales)...")
        
        # Crear conexión para secuencias
        seq_conn = connect_to_database(config)
        if not seq_conn:
            print("Error al conectar para actualizar secuencias.")
            if conn:
                conn.close()
            return False
        
        seq_cursor = seq_conn.cursor()
        
        # Lista para almacenar errores únicos
        unique_errors = set()
        
        # Para cada tabla en el orden definido
        for table_index, table_name in enumerate(table_import_order):
            if table_name not in inserts_by_table:
                continue
            
            commands = inserts_by_table[table_name]
            
            print(f"\nImportando tabla {table_index+1}/{len(table_import_order)}: {table_name} ({len(commands)} registros)...")
            
            # Crear conexión dedicada para esta tabla
            table_conn = connect_to_database(config)
            if not table_conn:
                print(f"Error al conectar para la tabla {table_name}.")
                continue
                
            table_cursor = table_conn.cursor()
            
            # Deshabilitar FK en la conexión de esta tabla si se especificó
            if config.get('disable_fk'):
                try:
                    table_cursor.execute("SET session_replication_role = 'replica';")
                except Exception:
                    pass
            
            # Ejecutar los INSERT para esta tabla
            table_success = 0
            table_fail = 0
            
            # Actualizar la secuencia para esta tabla antes de insertar datos
            try:
                seq_cursor.execute(f"SELECT pg_get_serial_sequence('{table_name}', 'id');")
                seq_result = seq_cursor.fetchone()
                if seq_result and seq_result[0]:
                    seq_cursor.execute(f"SELECT setval('{seq_result[0]}', 1, false);")
            except Exception as seq_error:
                print(f"Aviso: No se pudo reiniciar la secuencia para {table_name}: {str(seq_error)}")
            
            # Para cada comando INSERT
            for cmd_index, command in enumerate(commands):
                try:
                    # Cada INSERT en su propia transacción
                    table_cursor.execute("BEGIN;")
                    table_cursor.execute(command)
                    table_cursor.execute("COMMIT;")
                    table_success += 1
                    successful_inserts += 1
                except Exception as cmd_error:
                    table_cursor.execute("ROLLBACK;")
                    table_fail += 1
                    failed_inserts += 1
                    error_msg = str(cmd_error)
                    
                    # Almacenar errores únicos
                    error_type = error_msg.split(':')[0] if ':' in error_msg else error_msg
                    if error_type not in unique_errors:
                        unique_errors.add(error_type)
                        print(f"Nuevo tipo de error en {table_name}: {error_msg}")
                    
                    # Mostrar algunos errores para no llenar la consola
                    if cmd_index < 3 or cmd_index % 50 == 0:
                        print(f"Error en {table_name} #{cmd_index+1}/{len(commands)}: {error_msg[:100]}...")
                    
                    # Si no se especificó ignorar errores y hay un fallo, detener la importación de esta tabla
                    if not config.get('ignore_errors'):
                        print(f"Deteniendo importación de {table_name} debido a errores (use --ignore-errors para continuar).")
                        break
            
            # Cerrar la conexión de esta tabla
            table_cursor.close()
            table_conn.close()
            
            # Actualizar la secuencia para esta tabla después de insertar datos
            try:
                seq_cursor.execute(f"SELECT pg_get_serial_sequence('{table_name}', 'id');")
                seq_result = seq_cursor.fetchone()
                if seq_result and seq_result[0]:
                    seq_cursor.execute(f"""
                    SELECT setval('{seq_result[0]}', COALESCE((SELECT MAX(id) FROM {table_name}), 1));
                    """)
            except Exception as seq_error:
                print(f"Aviso: No se pudo actualizar la secuencia para {table_name}: {str(seq_error)}")
            
            print(f"Tabla {table_name}: {table_success} exitosos, {table_fail} fallidos")
        
        # Si se deshabilitaron las FK, volver a habilitarlas
        if config.get('disable_fk'):
            print("Reactivando las restricciones de clave foránea...")
            try:
                cursor.execute("SET session_replication_role = 'origin';")
                print("✓ Restricciones de clave foránea reactivadas correctamente.")
            except Exception as fk_error:
                print(f"Error al reactivar restricciones de clave foránea: {str(fk_error)}")
        
        # Cerrar conexiones
        cursor.close()
        seq_cursor.close()
        seq_conn.close()
        
        if conn:
            conn.close()
        
        # Resumen de la importación
        print("\n=== RESUMEN DE IMPORTACIÓN DE DATOS ===")
        print(f"Total de comandos INSERT: {total_inserts}")
        print(f"Comandos exitosos: {successful_inserts} ({successful_inserts/total_inserts*100:.1f}%)")
        print(f"Comandos fallidos: {failed_inserts} ({failed_inserts/total_inserts*100:.1f}%)")
        print("=======================================\n")
        
        if successful_inserts > 0:
            completion_status = "parcialmente" if failed_inserts > 0 else "exitosamente"
            print(f"Datos importados {completion_status}.")
            return True
        else:
            print("Error: No se pudieron importar datos.")
            return False
        
    except Exception as e:
        print(f"Error al importar datos: {str(e)}")
        return False


def import_from_backup(config):
    """Importa datos desde un archivo de backup SQL en dos fases: esquema y datos"""
    if not config['backup_file']:
        print("No se especificó archivo de backup, omitiendo importación.")
        return True
    
    if not os.path.exists(config['backup_file']):
        print(f"El archivo de backup '{config['backup_file']}' no existe.")
        return False
    
    # Fase 1: Importar esquema
    schema_success = import_schema_from_backup(config)
    if not schema_success:
        print("Error al importar el esquema de la base de datos.")
        return False
    
    # Fase 2: Importar datos
    data_success = import_data_from_backup(config)
    if not data_success:
        print("Error al importar los datos. La estructura fue creada pero los datos pueden estar incompletos.")
        return False
    
    return True


def setup_database(config):
    """Configura la base de datos completa"""
    print("\n====== CONFIGURACIÓN DE BASE DE DATOS PRODUCTIVA ======\n")
    print(f"Configuración de conexión:")
    print(f"  Host: {config['host']}")
    print(f"  Puerto: {config['port']}")
    print(f"  Usuario: {config['user']}")
    print(f"  Base de datos: {config['dbname']}")
    if config['backup_file']:
        print(f"  Archivo de backup: {config['backup_file']}")
    if config.get('skip_tables'):
        print("  Modo: Omitir creación de tablas")
    if config.get('only_data'):
        print("  Modo: Solo importar datos")
    if config.get('only_schema'):
        print("  Modo: Solo importar esquema")
    print("")
    
    # Si se especificó, eliminar la base de datos existente
    if config.get('drop_existing') and database_exists(config):
        if not drop_database(config):
            print("No se pudo eliminar la base de datos existente.")
            return False
    
    # Crear la base de datos (si no existe)
    if not create_database(config):
        print("No se pudo crear la base de datos.")
        return False
    
    # Si hay un archivo de backup
    if config['backup_file']:
        # Determinar qué fases ejecutar según los parámetros
        import_schema = not config.get('only_data') and not config.get('skip_tables')
        import_data = not config.get('only_schema')
        
        if import_schema:
            print("Importando esquema desde archivo de backup...")
            if not import_schema_from_backup(config):
                print("Error al importar esquema desde el backup.")
                
                # Solo intentamos crear tablas desde el script si no se especificó omitir tablas
                if not config.get('skip_tables'):
                    print("Intentando crear tablas desde el script...")
                    if not create_tables(config):
                        print("No se pudieron crear las tablas.")
                        # Si no se pudieron crear tablas y no se específicó solo datos,
                        # consideramos que ha fallado
                        if not config.get('only_data'):
                            return False
        
        if import_data:
            print("Importando datos desde archivo de backup...")
            if not import_data_from_backup(config):
                print("Error al importar datos desde el backup.")
                # Si solo se especificó importar datos y falló, consideramos que ha fallado
                if config.get('only_data'):
                    return False
    
    else:
        # Si no hay backup y no se especificó omitir tablas, crear tablas desde el script
        if not config.get('skip_tables') and not config.get('only_data'):
            print("Creando tablas desde el script (no se especificó archivo de backup)...")
            if not create_tables(config):
                print("No se pudieron crear las tablas.")
                return False
    
    print("\n====== CONFIGURACIÓN COMPLETADA EXITOSAMENTE ======\n")
    return True


def check_environment():
    """Verifica que estén disponibles las dependencias necesarias"""
    try:
        # Verificar que psycopg2 está instalado
        import psycopg2
        print("✓ psycopg2 está instalado")
        
        return True
    except ImportError:
        print("Error: Este script requiere psycopg2.")
        print("Puedes instalarlo con: pip install psycopg2-binary")
        return False


def search_for_backup_files():
    """Busca archivos de backup SQL en el directorio actual"""
    backup_files = []
    
    # Buscar archivos con extensión .sql
    for file in os.listdir('.'):
        if file.endswith('.sql') and (
            'backup' in file.lower() or 
            'dump' in file.lower() or 
            'productiva' in file.lower()
        ):
            backup_files.append(file)
    
    return backup_files


def main():
    """Función principal"""
    if not check_environment():
        return False
    
    # Procesar argumentos de línea de comandos
    args = parse_arguments()
    
    # Configuración
    config = {
        'host': args.host,
        'port': args.port,
        'user': args.user,
        'password': args.password,
        'dbname': args.dbname,
        'backup_file': args.backup_file,
        'drop_existing': args.drop_existing,
        'skip_tables': args.skip_tables,
        'only_data': args.only_data,
        'only_schema': args.only_schema,
        'disable_fk': args.disable_fk,
        'ignore_errors': args.ignore_errors
    }
    
    # Verificar opciones incompatibles
    if config['only_data'] and config['only_schema']:
        print("Error: Las opciones --only-data y --only-schema son mutuamente excluyentes.")
        return False
    
    if config['skip_tables'] and config['only_schema']:
        print("Error: Las opciones --skip-tables y --only-schema son mutuamente excluyentes.")
        return False
    
    # Mostrar advertencias para opciones potencialmente peligrosas
    if config['disable_fk']:
        print("\n⚠️ ADVERTENCIA: Has especificado --disable-fk, esto puede comprometer la integridad referencial de la base de datos.")
        print("   Usa esta opción solo cuando sea necesario para resolver problemas de dependencias circulares.\n")
    
    if config['ignore_errors']:
        print("\n⚠️ ADVERTENCIA: Has especificado --ignore-errors, esto continuará importando incluso si hay errores.")
        print("   Los datos resultantes pueden estar incompletos o ser inconsistentes.\n")
    
    # Si no se especificó un archivo de backup, buscar automáticamente
    if not config['backup_file']:
        backup_files = search_for_backup_files()
        if backup_files:
            print("Se encontraron los siguientes archivos de backup:")
            for i, file in enumerate(backup_files):
                print(f"  {i+1}. {file}")
            
            choice = input("\n¿Desea importar alguno de estos archivos? (Número/n): ")
            if choice.lower() != 'n' and choice.strip():
                try:
                    index = int(choice) - 1
                    if 0 <= index < len(backup_files):
                        config['backup_file'] = backup_files[index]
                        print(f"Se usará el archivo: {backup_files[index]}")
                    else:
                        print("Opción inválida, no se importarán datos.")
                except ValueError:
                    print("Entrada inválida, no se importarán datos.")
    
    # Si se especificó only_data o only_schema pero no hay archivo de backup
    if (config['only_data'] or config['only_schema']) and not config['backup_file']:
        print("Error: Las opciones --only-data y --only-schema requieren un archivo de backup.")
        return False
    
    # Configurar la base de datos
    return setup_database(config)


if __name__ == "__main__":
    main()
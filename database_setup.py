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


# Configuración por defecto
DEFAULT_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'user': 'postgres',
    'password': 'postgres',
    'dbname': 'productiva',
    'backup_file': None  # Si es None, solo se crean las tablas sin importar datos
}


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


def import_from_backup(config):
    """Importa datos desde un archivo de backup SQL"""
    if not config['backup_file']:
        print("No se especificó archivo de backup, omitiendo importación de datos.")
        return True
    
    if not os.path.exists(config['backup_file']):
        print(f"El archivo de backup '{config['backup_file']}' no existe.")
        return False
    
    try:
        # Conectar a la base de datos
        conn = connect_to_database(config)
        if not conn:
            return False
        
        cursor = conn.cursor()
        
        # Leer el archivo de backup
        print(f"Importando datos desde '{config['backup_file']}'...")
        with open(config['backup_file'], 'r') as f:
            sql_backup = f.read()
        
        # Ejecutar el script SQL
        cursor.execute(sql_backup)
        
        cursor.close()
        conn.close()
        
        print("Datos importados exitosamente.")
        return True
    except Exception as e:
        print(f"Error al importar datos: {str(e)}")
        return False


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
    print("")
    
    # Si se especificó, eliminar la base de datos existente
    if config.get('drop_existing') and database_exists(config):
        if not drop_database(config):
            print("No se pudo eliminar la base de datos existente.")
            return False
    
    # Crear la base de datos
    if not create_database(config):
        print("No se pudo crear la base de datos.")
        return False
    
    # Crear las tablas
    if not create_tables(config):
        print("No se pudieron crear las tablas.")
        return False
    
    # Importar datos si se especificó un archivo de backup
    if config['backup_file']:
        if not import_from_backup(config):
            print("No se pudieron importar los datos.")
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
        'drop_existing': args.drop_existing
    }
    
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
    
    # Configurar la base de datos
    return setup_database(config)


if __name__ == "__main__":
    main()
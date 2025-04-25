"""
Script para inicializar las tablas de CreaTurno en la base de datos de Productiva.
Este script crea las tablas necesarias para integrar CreaTurno con Productiva.
"""

import os
import logging
from flask import current_app
from sqlalchemy import text
from app import db

# Configurar logging
logger = logging.getLogger(__name__)

def init_creaturno_tables():
    """
    Crea las tablas específicas de CreaTurno en la base de datos de Productiva.
    """
    try:
        # Verificar si las tablas ya existen
        result = db.session.execute(text("""
            SELECT to_regclass('creaturno_shifts');
        """))
        
        table_exists = result.scalar() is not None
        
        if table_exists:
            logger.info("Las tablas de CreaTurno ya existen en la base de datos")
            return True
            
        # Las tablas no existen, crearlas
        logger.info("Creando tablas de CreaTurno en la base de datos")
        
        # Ejecutar las sentencias SQL para crear las tablas
        # Estas sentencias están basadas en el esquema usado por CreaTurno
        # pero adaptadas para usar los datos existentes en Productiva
        
        # Tabla creaturno_shifts (turnos)
        db.session.execute(text("""
            CREATE TABLE IF NOT EXISTS creaturno_shifts (
                id SERIAL PRIMARY KEY,
                employee_id INTEGER NOT NULL,
                location_id INTEGER NOT NULL,
                start_time TIMESTAMP WITH TIME ZONE NOT NULL,
                end_time TIMESTAMP WITH TIME ZONE NOT NULL,
                role VARCHAR(100),
                color VARCHAR(50),
                notes TEXT,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
                FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE
            );
        """))
        
        # Tabla creaturno_shift_templates (plantillas de turnos)
        db.session.execute(text("""
            CREATE TABLE IF NOT EXISTS creaturno_shift_templates (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                company_id INTEGER NOT NULL,
                start_time TIME NOT NULL,
                end_time TIME NOT NULL,
                days_of_week INTEGER[] NOT NULL,
                role VARCHAR(100),
                color VARCHAR(50),
                notes TEXT,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
            );
        """))
        
        # Tabla creaturno_shift_roles (roles de turnos)
        db.session.execute(text("""
            CREATE TABLE IF NOT EXISTS creaturno_shift_roles (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                company_id INTEGER NOT NULL,
                color VARCHAR(50),
                description TEXT,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
            );
        """))
        
        # Tabla creaturno_shift_assignments (asignaciones de turnos)
        db.session.execute(text("""
            CREATE TABLE IF NOT EXISTS creaturno_shift_assignments (
                id SERIAL PRIMARY KEY,
                shift_id INTEGER NOT NULL,
                employee_id INTEGER NOT NULL,
                status VARCHAR(50) DEFAULT 'pending',
                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                FOREIGN KEY (shift_id) REFERENCES creaturno_shifts(id) ON DELETE CASCADE,
                FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
            );
        """))
        
        # Tabla creaturno_shift_swap_requests (solicitudes de cambio de turno)
        db.session.execute(text("""
            CREATE TABLE IF NOT EXISTS creaturno_shift_swap_requests (
                id SERIAL PRIMARY KEY,
                requester_shift_id INTEGER NOT NULL,
                target_shift_id INTEGER,
                requester_employee_id INTEGER NOT NULL,
                target_employee_id INTEGER,
                status VARCHAR(50) DEFAULT 'pending',
                notes TEXT,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                FOREIGN KEY (requester_shift_id) REFERENCES creaturno_shifts(id) ON DELETE CASCADE,
                FOREIGN KEY (target_shift_id) REFERENCES creaturno_shifts(id) ON DELETE SET NULL,
                FOREIGN KEY (requester_employee_id) REFERENCES employees(id) ON DELETE CASCADE,
                FOREIGN KEY (target_employee_id) REFERENCES employees(id) ON DELETE SET NULL
            );
        """))
        
        # Tabla creaturno_shift_preference_rules (reglas de preferencia de turno)
        db.session.execute(text("""
            CREATE TABLE IF NOT EXISTS creaturno_shift_preference_rules (
                id SERIAL PRIMARY KEY,
                employee_id INTEGER NOT NULL,
                rule_type VARCHAR(50) NOT NULL,
                value JSONB NOT NULL,
                priority INTEGER DEFAULT 1,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
            );
        """))
        
        # Tabla creaturno_location_staffing_requirements (requisitos de personal por ubicación)
        db.session.execute(text("""
            CREATE TABLE IF NOT EXISTS creaturno_location_staffing_requirements (
                id SERIAL PRIMARY KEY,
                location_id INTEGER NOT NULL,
                day_of_week INTEGER NOT NULL,
                time_slot TIME NOT NULL,
                min_employees INTEGER NOT NULL DEFAULT 1,
                preferred_employees INTEGER,
                role_requirements JSONB,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE
            );
        """))
        
        # Tabla creaturno_shift_schedule_settings (configuración de programación de turnos)
        db.session.execute(text("""
            CREATE TABLE IF NOT EXISTS creaturno_shift_schedule_settings (
                id SERIAL PRIMARY KEY,
                company_id INTEGER NOT NULL,
                auto_approve_swaps BOOLEAN DEFAULT false,
                advance_schedule_days INTEGER DEFAULT 14,
                min_rest_hours INTEGER DEFAULT 8,
                max_daily_hours INTEGER DEFAULT 8,
                max_weekly_hours INTEGER DEFAULT 40,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
            );
        """))
        
        # Crear índices para mejorar el rendimiento
        db.session.execute(text("""
            CREATE INDEX IF NOT EXISTS idx_creaturno_shifts_employee_id ON creaturno_shifts(employee_id);
            CREATE INDEX IF NOT EXISTS idx_creaturno_shifts_location_id ON creaturno_shifts(location_id);
            CREATE INDEX IF NOT EXISTS idx_creaturno_shifts_start_time ON creaturno_shifts(start_time);
            CREATE INDEX IF NOT EXISTS idx_creaturno_shift_templates_company_id ON creaturno_shift_templates(company_id);
            CREATE INDEX IF NOT EXISTS idx_creaturno_shift_roles_company_id ON creaturno_shift_roles(company_id);
            CREATE INDEX IF NOT EXISTS idx_creaturno_shift_assignments_shift_id ON creaturno_shift_assignments(shift_id);
            CREATE INDEX IF NOT EXISTS idx_creaturno_shift_assignments_employee_id ON creaturno_shift_assignments(employee_id);
            CREATE INDEX IF NOT EXISTS idx_creaturno_shift_swap_requests_requester_id ON creaturno_shift_swap_requests(requester_employee_id);
            CREATE INDEX IF NOT EXISTS idx_creaturno_shift_preference_rules_employee_id ON creaturno_shift_preference_rules(employee_id);
            CREATE INDEX IF NOT EXISTS idx_creaturno_location_staffing_requirements_location_id ON creaturno_location_staffing_requirements(location_id);
            CREATE INDEX IF NOT EXISTS idx_creaturno_shift_schedule_settings_company_id ON creaturno_shift_schedule_settings(company_id);
        """))
        
        # Commit para guardar los cambios
        db.session.commit()
        
        logger.info("Tablas de CreaTurno creadas correctamente")
        return True
        
    except Exception as e:
        logger.error(f"Error al crear tablas de CreaTurno: {str(e)}")
        db.session.rollback()
        return False

if __name__ == "__main__":
    # Si se ejecuta directamente, importar la aplicación Flask y crear un contexto
    from app import app
    with app.app_context():
        success = init_creaturno_tables()
        print(f"Inicialización de tablas de CreaTurno: {'Exitosa' if success else 'Fallida'}")
-- SQL para actualizar el esquema de tareas con todos los cambios necesarios
-- Ejecutar en tu fork para que funcionen las tareas nuevas

-- 1. Actualizar los valores del enum TaskFrequency para que coincidan con el código
ALTER TYPE taskfrequency RENAME TO taskfrequency_old;

CREATE TYPE taskfrequency AS ENUM ('DIARIA', 'SEMANAL', 'PERSONALIZADA', 'FECHA_ESPECIFICA');

-- Actualizar la columna frequency en la tabla tasks
ALTER TABLE tasks ALTER COLUMN frequency TYPE taskfrequency USING frequency::text::taskfrequency;

-- Eliminar el tipo antiguo
DROP TYPE taskfrequency_old;

-- 2. Verificar que existe la tabla task_groups
CREATE TABLE IF NOT EXISTS task_groups (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    description TEXT,
    color VARCHAR(7) DEFAULT '#17a2b8',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    location_id INTEGER NOT NULL REFERENCES locations(id)
);

-- 3. Agregar columna group_id a tasks si no existe
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'tasks' AND column_name = 'group_id') THEN
        ALTER TABLE tasks ADD COLUMN group_id INTEGER REFERENCES task_groups(id);
    END IF;
END $$;

-- 4. Verificar que existe la tabla task_weekdays
CREATE TABLE IF NOT EXISTS task_weekdays (
    id SERIAL PRIMARY KEY,
    day_of_week VARCHAR(20) NOT NULL,
    task_id INTEGER NOT NULL REFERENCES tasks(id) ON DELETE CASCADE
);

-- 5. Verificar que existe la tabla task_month_days
CREATE TABLE IF NOT EXISTS task_month_days (
    id SERIAL PRIMARY KEY,
    day_of_month INTEGER NOT NULL CHECK (day_of_month >= 1 AND day_of_month <= 31),
    task_id INTEGER NOT NULL REFERENCES tasks(id) ON DELETE CASCADE
);

-- 6. Verificar que existen las columnas de estado semanal y mensual en tasks
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'tasks' AND column_name = 'current_week_completed') THEN
        ALTER TABLE tasks ADD COLUMN current_week_completed BOOLEAN DEFAULT FALSE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'tasks' AND column_name = 'current_month_completed') THEN
        ALTER TABLE tasks ADD COLUMN current_month_completed BOOLEAN DEFAULT FALSE;
    END IF;
END $$;

-- 7. Verificar que existe la tabla task_instances
CREATE TABLE IF NOT EXISTS task_instances (
    id SERIAL PRIMARY KEY,
    scheduled_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'pendiente',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    task_id INTEGER NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    completed_by_id INTEGER REFERENCES local_users(id)
);

-- 8. Verificar estructura de task_completions
CREATE TABLE IF NOT EXISTS task_completions (
    id SERIAL PRIMARY KEY,
    completion_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    task_id INTEGER NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    local_user_id INTEGER NOT NULL REFERENCES local_users(id)
);

-- 9. Crear índices para mejor rendimiento
CREATE INDEX IF NOT EXISTS idx_tasks_frequency ON tasks(frequency);
CREATE INDEX IF NOT EXISTS idx_tasks_location_id ON tasks(location_id);
CREATE INDEX IF NOT EXISTS idx_tasks_group_id ON tasks(group_id);
CREATE INDEX IF NOT EXISTS idx_task_weekdays_task_id ON task_weekdays(task_id);
CREATE INDEX IF NOT EXISTS idx_task_month_days_task_id ON task_month_days(task_id);
CREATE INDEX IF NOT EXISTS idx_task_instances_task_id ON task_instances(task_id);
CREATE INDEX IF NOT EXISTS idx_task_instances_scheduled_date ON task_instances(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_task_completions_task_id ON task_completions(task_id);

-- 10. Actualizar función de trigger para updated_at si no existe
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 11. Crear triggers para updated_at en las tablas que lo necesiten
DO $$
BEGIN
    -- Para task_groups
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_task_groups_updated_at') THEN
        CREATE TRIGGER update_task_groups_updated_at 
        BEFORE UPDATE ON task_groups 
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    
    -- Para task_instances
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_task_instances_updated_at') THEN
        CREATE TRIGGER update_task_instances_updated_at 
        BEFORE UPDATE ON task_instances 
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
END $$;

-- 12. Verificar que existen las tablas de productos y etiquetas
CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    shelf_life_days INTEGER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    location_id INTEGER NOT NULL REFERENCES locations(id)
);

-- 13. Crear enum para tipos de conservación
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'conservationtype') THEN
        CREATE TYPE conservationtype AS ENUM ('descongelacion', 'refrigeracion', 'gastro', 'caliente', 'seco');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS product_conservations (
    id SERIAL PRIMARY KEY,
    conservation_type conservationtype NOT NULL,
    hours_valid INTEGER NOT NULL DEFAULT 24,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS network_printers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    model VARCHAR(100),
    api_path VARCHAR(255) DEFAULT '/print',
    port INTEGER DEFAULT 5000,
    printer_type VARCHAR(20) DEFAULT 'DIRECT_NETWORK',
    usb_port VARCHAR(100),
    requires_auth BOOLEAN DEFAULT FALSE,
    username VARCHAR(100),
    password VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    last_status VARCHAR(50),
    last_status_check TIMESTAMP,
    location_id INTEGER REFERENCES locations(id)
);

CREATE TABLE IF NOT EXISTS label_templates (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_default BOOLEAN DEFAULT FALSE,
    titulo_x INTEGER DEFAULT 50,
    titulo_y INTEGER DEFAULT 10,
    titulo_size INTEGER DEFAULT 11,
    titulo_bold BOOLEAN DEFAULT TRUE,
    conservacion_x INTEGER DEFAULT 50,
    conservacion_y INTEGER DEFAULT 25,
    conservacion_size INTEGER DEFAULT 9,
    conservacion_bold BOOLEAN DEFAULT TRUE,
    preparador_x INTEGER DEFAULT 50,
    preparador_y INTEGER DEFAULT 40,
    preparador_size INTEGER DEFAULT 7,
    preparador_bold BOOLEAN DEFAULT FALSE,
    fecha_x INTEGER DEFAULT 50,
    fecha_y INTEGER DEFAULT 50,
    fecha_size INTEGER DEFAULT 7,
    fecha_bold BOOLEAN DEFAULT FALSE,
    caducidad_x INTEGER DEFAULT 50,
    caducidad_y INTEGER DEFAULT 65,
    caducidad_size INTEGER DEFAULT 9,
    caducidad_bold BOOLEAN DEFAULT TRUE,
    caducidad2_x INTEGER DEFAULT 50,
    caducidad2_y INTEGER DEFAULT 80,
    caducidad2_size INTEGER DEFAULT 8,
    caducidad2_bold BOOLEAN DEFAULT FALSE,
    location_id INTEGER NOT NULL REFERENCES locations(id)
);

CREATE TABLE IF NOT EXISTS product_labels (
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATE NOT NULL,
    product_id INTEGER NOT NULL REFERENCES products(id),
    local_user_id INTEGER NOT NULL REFERENCES local_users(id),
    conservation_type conservationtype NOT NULL
);

-- 14. Verificar que la tabla locations tiene las columnas necesarias
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'locations' AND column_name = 'requires_pin') THEN
        ALTER TABLE locations ADD COLUMN requires_pin BOOLEAN DEFAULT TRUE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'locations' AND column_name = 'portal_username') THEN
        ALTER TABLE locations ADD COLUMN portal_username VARCHAR(64) UNIQUE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'locations' AND column_name = 'portal_password_hash') THEN
        ALTER TABLE locations ADD COLUMN portal_password_hash VARCHAR(256);
    END IF;
END $$;

-- 15. Verificar que la tabla local_users tiene las columnas necesarias
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'local_users' AND column_name = 'imported') THEN
        ALTER TABLE local_users ADD COLUMN imported BOOLEAN DEFAULT FALSE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'local_users' AND column_name = 'employee_id') THEN
        ALTER TABLE local_users ADD COLUMN employee_id INTEGER REFERENCES employees(id);
    END IF;
END $$;

-- 16. Mostrar resumen de cambios aplicados
SELECT 'Schema actualizado correctamente. Verificando estructura...' as status;

-- Verificar que todo está correcto
SELECT 
    'tasks' as tabla,
    COUNT(*) as registros,
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'tasks') as columnas
FROM tasks
UNION ALL
SELECT 
    'task_groups' as tabla,
    COUNT(*) as registros,
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'task_groups') as columnas
FROM task_groups
UNION ALL
SELECT 
    'task_weekdays' as tabla,
    COUNT(*) as registros,
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'task_weekdays') as columnas
FROM task_weekdays
UNION ALL
SELECT 
    'task_month_days' as tabla,
    COUNT(*) as registros,
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'task_month_days') as columnas
FROM task_month_days;

SELECT 'Actualización completada exitosamente!' as resultado;
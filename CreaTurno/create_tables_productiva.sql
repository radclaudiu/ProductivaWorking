-- Script para crear solo las tablas específicas de CreaTurno
-- Las tablas de usuarios, empresas y empleados ya existen en Productiva

-- Tabla de plantillas de horario
CREATE TABLE IF NOT EXISTS schedule_templates (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT,
  is_default BOOLEAN DEFAULT FALSE,
  start_hour INTEGER NOT NULL DEFAULT 8,
  end_hour INTEGER NOT NULL DEFAULT 20,
  time_increment INTEGER NOT NULL DEFAULT 15,
  is_global BOOLEAN DEFAULT FALSE,
  company_id INTEGER REFERENCES companies(id),
  created_by INTEGER REFERENCES users(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP
);

-- Tabla de horarios (schedules)
CREATE TABLE IF NOT EXISTS schedules (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT DEFAULT '',
  company_id INTEGER NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
  template_id INTEGER REFERENCES schedule_templates(id),
  start_date TEXT,
  end_date TEXT,
  status TEXT DEFAULT 'draft',
  department TEXT,
  created_by INTEGER REFERENCES users(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP
);

-- Tabla de turnos (shifts)
CREATE TABLE IF NOT EXISTS shifts (
  id SERIAL PRIMARY KEY,
  employee_id INTEGER NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
  date TEXT NOT NULL,
  start_time TEXT NOT NULL,
  end_time TEXT NOT NULL,
  notes TEXT DEFAULT '',
  status TEXT DEFAULT 'scheduled',
  break_time INTEGER,
  actual_start_time TEXT,
  actual_end_time TEXT,
  total_hours INTEGER,
  schedule_id INTEGER REFERENCES schedules(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP
);

-- Añadir índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_shifts_date ON shifts(date);
CREATE INDEX IF NOT EXISTS idx_shifts_employee ON shifts(employee_id);
CREATE INDEX IF NOT EXISTS idx_shifts_schedule ON shifts(schedule_id);

-- Agregar comentarios a las tablas para documentación
COMMENT ON TABLE schedule_templates IS 'Plantillas para crear horarios con configuraciones predefinidas';
COMMENT ON TABLE schedules IS 'Horarios de trabajo para empresas';
COMMENT ON TABLE shifts IS 'Turnos de trabajo asignados a empleados';
"""
Modelos para el sistema de seguimiento de horas trabajadas.
Estos modelos permiten almacenar y acumular horas trabajadas por:
- Empleado: a nivel diario, semanal y mensual
- Empresa: a nivel semanal y mensual
"""

from datetime import datetime, timedelta, date
from sqlalchemy import UniqueConstraint, Index
from app import db


class EmployeeWorkHours(db.Model):
    """
    Registro de horas trabajadas acumuladas por empleado (diaria, semanal y mensual).
    Se crea un registro por cada combinación única de empleado, año, semana y mes.
    """
    __tablename__ = 'employee_work_hours'
    
    id = db.Column(db.Integer, primary_key=True)
    
    # Periodo al que corresponden las horas acumuladas
    year = db.Column(db.Integer, nullable=False)
    month = db.Column(db.Integer, nullable=False)  # 1-12
    week_number = db.Column(db.Integer, nullable=False)  # 1-53 (semana ISO)
    
    # Horas acumuladas en cada periodo
    daily_hours = db.Column(db.Float, default=0.0, nullable=False)
    weekly_hours = db.Column(db.Float, default=0.0, nullable=False)
    monthly_hours = db.Column(db.Float, default=0.0, nullable=False)
    
    # Metadata
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'), nullable=False)
    company_id = db.Column(db.Integer, db.ForeignKey('companies.id'), nullable=False)
    
    # Relaciones con SQLAlchemy
    employee = db.relationship('Employee', backref=db.backref('work_hours', lazy=True))
    company = db.relationship('Company')
    
    # Restricciones e índices para optimizar consultas
    __table_args__ = (
        # Clave única para evitar duplicados
        UniqueConstraint('employee_id', 'year', 'month', 'week_number', name='uq_employee_period'),
        
        # Índices para acelerar las consultas más comunes
        Index('idx_employee_week', employee_id, year, week_number),
        Index('idx_employee_month', employee_id, year, month),
        Index('idx_employee_year', employee_id, year),
    )
    
    def __repr__(self):
        return f'<EmployeeWorkHours {self.employee_id} - Y:{self.year} M:{self.month} W:{self.week_number} - {self.weekly_hours}h/sem, {self.monthly_hours}h/mes>'


class CompanyWorkHours(db.Model):
    """
    Registro de horas trabajadas acumuladas por empresa (semanal y mensual).
    Se crea un registro por cada combinación única de empresa, año, semana y mes.
    """
    __tablename__ = 'company_work_hours'
    
    id = db.Column(db.Integer, primary_key=True)
    
    # Periodo al que corresponden las horas acumuladas
    year = db.Column(db.Integer, nullable=False)
    month = db.Column(db.Integer, nullable=False)  # 1-12
    week_number = db.Column(db.Integer, nullable=False)  # 1-53 (semana ISO)
    
    # Horas acumuladas en cada periodo
    weekly_hours = db.Column(db.Float, default=0.0, nullable=False)
    monthly_hours = db.Column(db.Float, default=0.0, nullable=False)
    
    # Metadata
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    company_id = db.Column(db.Integer, db.ForeignKey('companies.id'), nullable=False)
    
    # Relaciones con SQLAlchemy
    company = db.relationship('Company', backref=db.backref('company_work_hours', lazy=True))
    
    # Restricciones e índices para optimizar consultas
    __table_args__ = (
        # Clave única para evitar duplicados
        UniqueConstraint('company_id', 'year', 'month', 'week_number', name='uq_company_period'),
        
        # Índices para acelerar las consultas más comunes
        Index('idx_company_week', company_id, year, week_number),
        Index('idx_company_month', company_id, year, month),
        Index('idx_company_year', company_id, year),
    )
    
    def __repr__(self):
        return f'<CompanyWorkHours {self.company_id} - Y:{self.year} M:{self.month} W:{self.week_number} - {self.weekly_hours}h/sem, {self.monthly_hours}h/mes>'
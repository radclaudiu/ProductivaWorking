"""
Modelos para el sistema de Arqueos de Caja.

Este módulo contiene los modelos necesarios para gestionar arqueos diarios,
incluyendo desglose de ingresos, gastos y estadísticas asociadas.
"""

from datetime import datetime
from sqlalchemy import UniqueConstraint, Index
from app import db
from models import Employee

class CashRegister(db.Model):
    """
    Modelo principal para los arqueos de caja.
    Almacena un registro por cada arqueo diario realizado en una empresa.
    """
    __tablename__ = 'cash_registers'
    
    id = db.Column(db.Integer, primary_key=True)
    
    # Fecha y hora del arqueo
    date = db.Column(db.Date, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Desglose de importes
    total_amount = db.Column(db.Float, nullable=False, default=0.0)
    cash_amount = db.Column(db.Float, nullable=False, default=0.0)
    card_amount = db.Column(db.Float, nullable=False, default=0.0)
    delivery_cash_amount = db.Column(db.Float, nullable=False, default=0.0)
    delivery_online_amount = db.Column(db.Float, nullable=False, default=0.0)
    check_amount = db.Column(db.Float, nullable=False, default=0.0)
    
    # Información de gastos para controlar salidas de efectivo
    expenses_amount = db.Column(db.Float, nullable=False, default=0.0)
    expenses_notes = db.Column(db.Text)
    
    # Notas generales del arqueo
    notes = db.Column(db.Text)
    
    # Estado del arqueo
    is_confirmed = db.Column(db.Boolean, default=False)
    confirmed_at = db.Column(db.DateTime)
    confirmed_by_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    
    # Relaciones
    company_id = db.Column(db.Integer, db.ForeignKey('companies.id'), nullable=False)
    created_by_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    
    # En caso de ser creado por un empleado sin registro
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'))
    employee_name = db.Column(db.String(100))  # Para casos donde no hay ID de empleado
    
    # Relaciones con SQLAlchemy
    company = db.relationship('Company', backref=db.backref('cash_registers', lazy=True))
    created_by = db.relationship('User', foreign_keys=[created_by_id])
    confirmed_by = db.relationship('User', foreign_keys=[confirmed_by_id])
    employee = db.relationship('Employee', foreign_keys=[employee_id])
    
    # Restricciones e índices
    __table_args__ = (
        # No puede haber más de un arqueo por día y empresa
        UniqueConstraint('company_id', 'date', name='uq_company_date'),
        # Índices para búsquedas comunes
        Index('idx_cash_register_company', 'company_id'),
        Index('idx_cash_register_date', 'date'),
    )
    
    def __repr__(self):
        return f'<Arqueo {self.id}: {self.date} - {self.total_amount}€>'
    
    @property
    def week_number(self):
        """Devuelve el número de semana ISO del arqueo"""
        return self.date.isocalendar()[1]
    
    @property
    def year(self):
        """Devuelve el año del arqueo"""
        return self.date.year
    
    @property
    def month(self):
        """Devuelve el mes del arqueo"""
        return self.date.month
    
    @property
    def verified_amounts(self):
        """
        Verifica que el desglose de importes cuadre con el total.
        Retorna True si el total es igual a la suma de los importes desglosados menos gastos.
        """
        sum_amounts = (
            self.cash_amount +
            self.card_amount +
            self.delivery_cash_amount +
            self.delivery_online_amount +
            self.check_amount
        )
        return abs(self.total_amount - sum_amounts) < 0.01  # Tolerancia de 1 céntimo
    
    @property
    def net_cash(self):
        """
        Calcula el efectivo neto después de descontar gastos.
        """
        return self.cash_amount - self.expenses_amount


class CashRegisterSummary(db.Model):
    """
    Almacena resúmenes acumulados de arqueos por periodo (semana, mes, año).
    Se crea un registro por cada combinación única de empresa, año, mes y semana.
    """
    __tablename__ = 'cash_register_summaries'
    
    id = db.Column(db.Integer, primary_key=True)
    
    # Periodo al que corresponde el resumen
    year = db.Column(db.Integer, nullable=False)
    month = db.Column(db.Integer, nullable=False)  # 1-12
    week_number = db.Column(db.Integer, nullable=False)  # 1-53 (semana ISO)
    
    # Acumulados por periodo
    weekly_total = db.Column(db.Float, default=0.0, nullable=False)
    monthly_total = db.Column(db.Float, default=0.0, nullable=False)
    yearly_total = db.Column(db.Float, default=0.0, nullable=False)
    
    # Desglose de acumulados semanales
    weekly_cash = db.Column(db.Float, default=0.0, nullable=False)
    weekly_card = db.Column(db.Float, default=0.0, nullable=False)
    weekly_delivery_cash = db.Column(db.Float, default=0.0, nullable=False)
    weekly_delivery_online = db.Column(db.Float, default=0.0, nullable=False)
    weekly_check = db.Column(db.Float, default=0.0, nullable=False)
    weekly_expenses = db.Column(db.Float, default=0.0, nullable=False)
    
    # Costes de personal (se calculan a partir de las horas trabajadas)
    weekly_staff_cost = db.Column(db.Float, default=0.0, nullable=False)
    monthly_staff_cost = db.Column(db.Float, default=0.0, nullable=False)
    
    # Porcentajes de coste de personal sobre facturación
    weekly_staff_cost_percentage = db.Column(db.Float, default=0.0, nullable=False)
    monthly_staff_cost_percentage = db.Column(db.Float, default=0.0, nullable=False)
    
    # Metadata
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    company_id = db.Column(db.Integer, db.ForeignKey('companies.id'), nullable=False)
    
    # Relaciones con SQLAlchemy
    company = db.relationship('Company', backref=db.backref('cash_register_summaries', lazy=True))
    
    # Restricciones e índices
    __table_args__ = (
        # Clave única para evitar duplicados
        UniqueConstraint('company_id', 'year', 'month', 'week_number', name='uq_summary_period'),
        # Índices para búsquedas comunes
        Index('idx_summary_company', 'company_id'),
        Index('idx_summary_year_month', 'year', 'month'),
    )
    
    def __repr__(self):
        return f'<Resumen Arqueo: {self.company.name} - {self.year}-W{self.week_number:02d}>'
    
    @property
    def staff_cost_warning(self):
        """
        Indica si el porcentaje de coste de personal está por encima del umbral recomendado.
        Por lo general, se considera alto un porcentaje > 30% de la facturación.
        """
        return self.weekly_staff_cost_percentage > 30.0


class CashRegisterToken(db.Model):
    """
    Tokens para permitir a empleados sin acceso a la plataforma registrar arqueos.
    """
    __tablename__ = 'cash_register_tokens'
    
    id = db.Column(db.Integer, primary_key=True)
    token = db.Column(db.String(64), unique=True, nullable=False)
    
    # Validez del token
    is_active = db.Column(db.Boolean, default=True)
    expires_at = db.Column(db.DateTime)
    
    # Metadatos
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    used_at = db.Column(db.DateTime)
    
    # Relaciones
    company_id = db.Column(db.Integer, db.ForeignKey('companies.id'), nullable=False)
    created_by_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'))
    
    # Si se ha usado para crear un arqueo
    cash_register_id = db.Column(db.Integer, db.ForeignKey('cash_registers.id'))
    
    # Relaciones con SQLAlchemy
    company = db.relationship('Company', backref=db.backref('cash_register_tokens', lazy=True))
    created_by = db.relationship('User', foreign_keys=[created_by_id])
    employee = db.relationship('Employee', foreign_keys=[employee_id])
    cash_register = db.relationship('CashRegister', backref=db.backref('token', uselist=False))
    
    def __repr__(self):
        status = "activo" if self.is_active else "inactivo"
        return f'<Token Arqueo: {self.token[:8]}... ({status})>'
    
    @property
    def is_valid(self):
        """
        Comprueba si el token está activo y no ha expirado.
        """
        if not self.is_active:
            return False
        if self.expires_at and self.expires_at < datetime.utcnow():
            return False
        return True
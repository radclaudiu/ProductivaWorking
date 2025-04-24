"""
Modelos para el sistema de Arqueos de Caja.

Este módulo define los modelos para gestionar los arqueos de caja diarios,
resúmenes acumulados y tokens de acceso para empleados.
"""

import secrets
from datetime import datetime, timedelta
from sqlalchemy import Column, Integer, Float, String, Boolean, Date, Text, ForeignKey, UniqueConstraint
from sqlalchemy.orm import relationship
from app import db


class CashRegister(db.Model):
    """
    Modelo para los arqueos de caja diarios.
    
    Cada registro representa un arqueo de caja para una empresa en una fecha específica.
    Registra los importes por método de pago, gastos y notas.
    """
    __tablename__ = 'cash_registers'
    
    id = Column(Integer, primary_key=True)
    date = Column(Date, nullable=False)
    created_at = Column(db.DateTime, default=datetime.utcnow)
    updated_at = Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Campos de importes
    total_amount = Column(Float, nullable=False, default=0.0)
    cash_amount = Column(Float, nullable=False, default=0.0)
    card_amount = Column(Float, nullable=False, default=0.0)
    delivery_cash_amount = Column(Float, nullable=False, default=0.0)
    delivery_online_amount = Column(Float, nullable=False, default=0.0)
    check_amount = Column(Float, nullable=False, default=0.0)
    expenses_amount = Column(Float, nullable=False, default=0.0)
    
    # Campos de notas
    expenses_notes = Column(Text)
    notes = Column(Text)
    
    # Campos de confirmación
    is_confirmed = Column(Boolean, default=False)
    confirmed_at = Column(db.DateTime)
    confirmed_by_id = Column(Integer, ForeignKey('users.id'))
    confirmed_by = relationship('User', foreign_keys=[confirmed_by_id])
    
    # Campos de relación
    company_id = Column(Integer, ForeignKey('companies.id'), nullable=False)
    company = relationship('Company', backref='cash_registers')
    
    created_by_id = Column(Integer, ForeignKey('users.id'))
    created_by = relationship('User', foreign_keys=[created_by_id])
    
    employee_id = Column(Integer, ForeignKey('employees.id'))
    employee = relationship('Employee')
    
    employee_name = Column(String(100))
    
    # Token que creó este arqueo (puede ser nulo si se creó manualmente)
    token_id = Column(Integer, ForeignKey('cash_register_tokens.id'))
    
    # Restricción única: una empresa solo puede tener un arqueo por fecha
    __table_args__ = (
        UniqueConstraint('company_id', 'date', name='uq_company_date'),
    )
    
    def __repr__(self):
        return f'<CashRegister {self.date} - {self.company.name if self.company else "Unknown"}>'


class CashRegisterSummary(db.Model):
    """
    Modelo para los resúmenes acumulados de arqueos de caja.
    
    Almacena los totales acumulados semanales, mensuales y anuales para facilitar
    la generación de informes y evitar recálculos.
    """
    __tablename__ = 'cash_register_summaries'
    
    id = Column(Integer, primary_key=True)
    year = Column(Integer, nullable=False)
    month = Column(Integer, nullable=False)
    week_number = Column(Integer, nullable=False)
    
    # Totales acumulados
    weekly_total = Column(Float, nullable=False, default=0.0)
    monthly_total = Column(Float, nullable=False, default=0.0)
    yearly_total = Column(Float, nullable=False, default=0.0)
    
    # Desglose por tipo de pago (semanal)
    weekly_cash = Column(Float, nullable=False, default=0.0)
    weekly_card = Column(Float, nullable=False, default=0.0)
    weekly_delivery_cash = Column(Float, nullable=False, default=0.0)
    weekly_delivery_online = Column(Float, nullable=False, default=0.0)
    weekly_check = Column(Float, nullable=False, default=0.0)
    weekly_expenses = Column(Float, nullable=False, default=0.0)
    
    # Datos de coste de personal
    weekly_staff_cost = Column(Float, nullable=False, default=0.0)
    monthly_staff_cost = Column(Float, nullable=False, default=0.0)
    weekly_staff_cost_percentage = Column(Float, nullable=False, default=0.0)
    monthly_staff_cost_percentage = Column(Float, nullable=False, default=0.0)
    
    # Metadatos
    created_at = Column(db.DateTime, default=datetime.utcnow)
    updated_at = Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relación con la empresa
    company_id = Column(Integer, ForeignKey('companies.id'), nullable=False)
    company = relationship('Company', backref='cash_register_summaries')
    
    # Restricción única: solo un resumen por empresa, año, mes y semana
    __table_args__ = (
        UniqueConstraint('company_id', 'year', 'month', 'week_number', name='uq_summary_period'),
    )
    
    def __repr__(self):
        return f'<CashRegisterSummary {self.company.name if self.company else "Unknown"} - W{self.week_number}/{self.month}/{self.year}>'


class CashRegisterToken(db.Model):
    """
    Modelo para los tokens de acceso de empleados a arqueos de caja.
    
    Permite a empleados sin acceso completo al sistema enviar datos
    de arqueos de caja mediante un token temporal.
    """
    __tablename__ = 'cash_register_tokens'
    
    id = Column(Integer, primary_key=True)
    token = Column(String(64), unique=True, nullable=False)
    is_active = Column(Boolean, default=True)
    expires_at = Column(db.DateTime)
    created_at = Column(db.DateTime, default=datetime.utcnow)
    used_at = Column(db.DateTime)
    
    # PIN de acceso simple (opcional)
    pin = Column(String(10), nullable=True)
    
    # Relaciones
    company_id = Column(Integer, ForeignKey('companies.id'), nullable=False)
    company = relationship('Company', backref='cash_register_tokens')
    
    created_by_id = Column(Integer, ForeignKey('users.id'))
    created_by = relationship('User')
    
    employee_id = Column(Integer, ForeignKey('employees.id'))
    employee = relationship('Employee')
    
    # Relación con arqueos enviados por este token
    # No es un campo en la base de datos, sino una relación inversa
    registers = relationship('CashRegister', backref='token', foreign_keys='CashRegister.token_id')
    
    def __repr__(self):
        return f'<CashRegisterToken {self.token[:8]}... - {self.company.name if self.company else "Unknown"}>'
    
    @staticmethod
    def generate_token(company_id, employee_id=None, created_by_id=None, expiry_days=7):
        """
        Genera un nuevo token para acceso a arqueos de caja.
        
        Args:
            company_id: ID de la empresa
            employee_id: ID del empleado asignado (opcional)
            created_by_id: ID del usuario que crea el token
            expiry_days: Días hasta la expiración del token (valor por defecto: 7)
            
        Returns:
            Instancia de CashRegisterToken
        """
        import logging
        logger = logging.getLogger(__name__)
        
        # Asegurar que expiry_days sea un entero válido
        if expiry_days is None or not isinstance(expiry_days, int) or expiry_days <= 0:
            logger.warning(f"expiry_days inválido: {expiry_days}. Usando valor por defecto (7).")
            expiry_days = 7
            
        token = secrets.token_hex(32)  # 64 caracteres
        expires_at = datetime.utcnow() + timedelta(days=expiry_days)
        
        logger.info(f"Generando token para empresa {company_id}, expira el {expires_at}")
        
        new_token = CashRegisterToken(
            token=token,
            company_id=company_id,
            employee_id=employee_id,
            created_by_id=created_by_id,
            expires_at=expires_at
        )
        
        db.session.add(new_token)
        db.session.commit()
        
        logger.info(f"Token generado correctamente: {new_token.id}")
        return new_token
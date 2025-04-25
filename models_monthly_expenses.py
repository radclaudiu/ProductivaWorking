"""
Modelos para el sistema de gestión de gastos mensuales.

Este módulo define los modelos necesarios para el sistema de gastos mensuales,
incluyendo categorías de gastos, gastos fijos, gastos mensuales y resúmenes.
"""

import datetime
import secrets
import string
from app import db
from sqlalchemy import Column, Integer, String, Float, Boolean, ForeignKey, Text, DateTime, func
from sqlalchemy.orm import relationship

class ExpenseCategory(db.Model):
    """
    Modelo para las categorías de gastos.
    
    Las categorías pueden ser del sistema (predefinidas) o personalizadas por cada empresa.
    """
    __tablename__ = 'expense_categories'
    
    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    description = Column(Text, nullable=True)
    company_id = Column(Integer, ForeignKey('companies.id'), nullable=True)
    is_system = Column(Boolean, default=False)  # True para categorías predefinidas del sistema
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.datetime.utcnow, onupdate=datetime.datetime.utcnow)
    
    # Relaciones
    company = relationship('Company', backref='expense_categories')
    fixed_expenses = relationship('FixedExpense', back_populates='category', cascade='all, delete-orphan')
    monthly_expenses = relationship('MonthlyExpense', back_populates='category', cascade='all, delete-orphan')
    
    def __repr__(self):
        return f"<ExpenseCategory {self.name}>"


class FixedExpense(db.Model):
    """
    Modelo para los gastos fijos mensuales.
    
    Los gastos fijos son aquellos que se repiten cada mes (ej. alquiler, servicios).
    """
    __tablename__ = 'fixed_expenses'
    
    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    description = Column(Text, nullable=True)
    amount = Column(Float, nullable=False, default=0.0)
    company_id = Column(Integer, ForeignKey('companies.id'), nullable=False)
    category_id = Column(Integer, ForeignKey('expense_categories.id'), nullable=False)
    is_active = Column(Boolean, default=True)  # Para deshabilitar sin eliminar
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.datetime.utcnow, onupdate=datetime.datetime.utcnow)
    
    # Relaciones
    company = relationship('Company', backref='fixed_expenses')
    category = relationship('ExpenseCategory', back_populates='fixed_expenses')
    
    def __repr__(self):
        return f"<FixedExpense {self.name} - {self.amount}€>"


class MonthlyExpense(db.Model):
    """
    Modelo para los gastos mensuales personalizados.
    
    Los gastos mensuales son aquellos que se registran específicamente para un mes
    y pueden variar en cada período.
    """
    __tablename__ = 'monthly_expenses'
    
    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    description = Column(Text, nullable=True)
    amount = Column(Float, nullable=False, default=0.0)
    company_id = Column(Integer, ForeignKey('companies.id'), nullable=False)
    category_id = Column(Integer, ForeignKey('expense_categories.id'), nullable=False)
    year = Column(Integer, nullable=False)
    month = Column(Integer, nullable=False)  # 1-12
    is_fixed = Column(Boolean, default=False)  # Indica si también es un gasto fijo
    expense_date = Column(String(20), nullable=True)  # Fecha del gasto en formato DD-MM-YYYY
    submitted_by_employee = Column(Boolean, default=False)  # Indica si fue enviado por un empleado
    employee_name = Column(String(100), nullable=True)  # Nombre del empleado que reportó el gasto
    receipt_image = Column(String(255), nullable=True)  # Ruta a la imagen del recibo/factura (opcional)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.datetime.utcnow, onupdate=datetime.datetime.utcnow)
    
    # Relaciones
    company = relationship('Company', backref='monthly_expenses')
    category = relationship('ExpenseCategory', back_populates='monthly_expenses')
    
    def __repr__(self):
        return f"<MonthlyExpense {self.name} - {self.month}/{self.year} - {self.amount}€>"


class MonthlyExpenseSummary(db.Model):
    """
    Modelo para los resúmenes mensuales de gastos.
    
    Almacena los totales calculados para facilitar la generación de informes
    sin necesidad de recalcular los totales cada vez.
    """
    __tablename__ = 'monthly_expense_summaries'
    
    id = Column(Integer, primary_key=True)
    company_id = Column(Integer, ForeignKey('companies.id'), nullable=False)
    year = Column(Integer, nullable=False)
    month = Column(Integer, nullable=False)  # 1-12
    fixed_expenses_total = Column(Float, nullable=False, default=0.0)
    custom_expenses_total = Column(Float, nullable=False, default=0.0)
    total_amount = Column(Float, nullable=False, default=0.0)  # fixed + custom
    number_of_expenses = Column(Integer, nullable=False, default=0)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.datetime.utcnow, onupdate=datetime.datetime.utcnow)
    
    # Relaciones
    company = relationship('Company', backref='monthly_expense_summaries')
    
    def __repr__(self):
        return f"<MonthlyExpenseSummary {self.month}/{self.year} - {self.total_amount}€>"


class MonthlyExpenseToken(db.Model):
    """
    Modelo para tokens de envío de gastos por empleados.
    
    Estos tokens permiten a los empleados enviar gastos sin necesidad
    de acceder al sistema completo.
    """
    __tablename__ = 'monthly_expense_tokens'
    
    id = Column(Integer, primary_key=True)
    company_id = Column(Integer, ForeignKey('companies.id'), nullable=False)
    token = Column(String(20), nullable=False, unique=True)
    name = Column(String(100), nullable=False)  # Nombre descriptivo del token
    description = Column(Text, nullable=True)
    is_active = Column(Boolean, default=True)
    category_id = Column(Integer, ForeignKey('expense_categories.id'), nullable=True)  # Categoría predeterminada (opcional)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.datetime.utcnow, onupdate=datetime.datetime.utcnow)
    last_used_at = Column(DateTime, nullable=True)
    total_uses = Column(Integer, default=0)
    
    # Relaciones
    company = relationship('Company', backref='expense_tokens')
    category = relationship('ExpenseCategory', backref='expense_tokens')
    
    @staticmethod
    def generate_token(length=10):
        """Genera un token aleatorio para envío de gastos."""
        characters = string.ascii_uppercase + string.digits
        token = ''.join(secrets.choice(characters) for _ in range(length))
        return token
    
    def __repr__(self):
        return f"<MonthlyExpenseToken {self.name} - {self.token}>"
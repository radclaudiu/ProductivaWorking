"""
Modelos para el sistema de gestión de gastos mensuales.

Este módulo define los modelos necesarios para el sistema de gastos mensuales,
incluyendo categorías de gastos, gastos fijos, gastos mensuales y resúmenes.
"""

import datetime
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
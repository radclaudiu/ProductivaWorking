"""
Modelos para el sistema de gestión de gastos mensuales.

Este módulo define los modelos para gestionar los gastos mensuales fijos y personalizados
de cada empresa para uso en el módulo de arqueos de caja y reportes.
"""

from datetime import datetime
from sqlalchemy import Column, Integer, Float, String, Boolean, Date, Text, ForeignKey, UniqueConstraint
from sqlalchemy.orm import relationship
from app import db


class ExpenseCategory(db.Model):
    """
    Modelo para categorías de gastos.
    
    Define categorías predeterminadas y personalizadas para clasificar gastos.
    """
    __tablename__ = 'expense_categories'
    
    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    description = Column(Text)
    is_system = Column(Boolean, default=False)  # True para categorías del sistema, False para personalizadas
    company_id = Column(Integer, ForeignKey('companies.id'), nullable=True)  # Null para categorías globales
    company = relationship('Company', backref='expense_categories')
    
    created_at = Column(db.DateTime, default=datetime.utcnow)
    updated_at = Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    def __repr__(self):
        return f'<ExpenseCategory {self.name}>'


class FixedExpense(db.Model):
    """
    Modelo para gastos fijos mensuales.
    
    Almacena la definición de gastos fijos que se replican cada mes
    automáticamente.
    """
    __tablename__ = 'fixed_expenses'
    
    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    description = Column(Text)
    amount = Column(Float, nullable=False, default=0.0)
    is_active = Column(Boolean, default=True)
    
    # Relaciones
    company_id = Column(Integer, ForeignKey('companies.id'), nullable=False)
    company = relationship('Company', backref='fixed_expenses')
    
    category_id = Column(Integer, ForeignKey('expense_categories.id'))
    category = relationship('ExpenseCategory', backref='fixed_expenses')
    
    created_by_id = Column(Integer, ForeignKey('users.id'))
    created_by = relationship('User', foreign_keys=[created_by_id])
    
    created_at = Column(db.DateTime, default=datetime.utcnow)
    updated_at = Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    def __repr__(self):
        return f'<FixedExpense {self.name} ({self.amount})>'


class MonthlyExpense(db.Model):
    """
    Modelo para registro de gastos mensuales.
    
    Cada registro representa un gasto asignado a un mes específico,
    ya sea un gasto fijo replicado automáticamente o un gasto personalizado.
    """
    __tablename__ = 'monthly_expenses'
    
    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    description = Column(Text)
    amount = Column(Float, nullable=False, default=0.0)
    year = Column(Integer, nullable=False)
    month = Column(Integer, nullable=False)  # 1-12
    
    # True si proviene de un gasto fijo, False si es personalizado para este mes
    is_fixed = Column(Boolean, default=False)
    
    # Relaciones
    company_id = Column(Integer, ForeignKey('companies.id'), nullable=False)
    company = relationship('Company', backref='monthly_expenses')
    
    # Relación opcional con el gasto fijo de origen (si proviene de uno)
    fixed_expense_id = Column(Integer, ForeignKey('fixed_expenses.id'), nullable=True)
    fixed_expense = relationship('FixedExpense', backref='monthly_instances')
    
    category_id = Column(Integer, ForeignKey('expense_categories.id'))
    category = relationship('ExpenseCategory', backref='monthly_expenses')
    
    created_by_id = Column(Integer, ForeignKey('users.id'))
    created_by = relationship('User', foreign_keys=[created_by_id])
    
    created_at = Column(db.DateTime, default=datetime.utcnow)
    updated_at = Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Solo un registro por gasto fijo por mes/año/empresa
    __table_args__ = (
        UniqueConstraint('company_id', 'fixed_expense_id', 'year', 'month', 
                         name='uq_monthly_fixed_expense'),
    )
    
    def __repr__(self):
        expense_type = "Fijo" if self.is_fixed else "Personalizado"
        return f'<MonthlyExpense {self.name} ({self.amount}) - {expense_type} - {self.month}/{self.year}>'


class MonthlyExpenseSummary(db.Model):
    """
    Modelo para el resumen mensual de gastos.
    
    Almacena totales acumulados de gastos por mes para cada empresa,
    para facilitar la generación de informes y reportes.
    """
    __tablename__ = 'monthly_expense_summaries'
    
    id = Column(Integer, primary_key=True)
    year = Column(Integer, nullable=False)
    month = Column(Integer, nullable=False)  # 1-12
    
    # Totales acumulados
    total_amount = Column(Float, nullable=False, default=0.0)
    fixed_expenses_total = Column(Float, nullable=False, default=0.0)
    custom_expenses_total = Column(Float, nullable=False, default=0.0)
    
    # Metadatos
    created_at = Column(db.DateTime, default=datetime.utcnow)
    updated_at = Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relación con la empresa
    company_id = Column(Integer, ForeignKey('companies.id'), nullable=False)
    company = relationship('Company', backref='monthly_expense_summaries')
    
    # Solo un resumen por empresa, año y mes
    __table_args__ = (
        UniqueConstraint('company_id', 'year', 'month', name='uq_monthly_expense_summary'),
    )
    
    def __repr__(self):
        return f'<MonthlyExpenseSummary {self.company.name if self.company else "Unknown"} - {self.month}/{self.year}>'
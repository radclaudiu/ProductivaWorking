"""add cash register tables

Revision ID: a1b2c3d4e5f6
Revises: 6a9d8f1a6e1d
Create Date: 2025-04-24 14:46:00.000000

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = 'a1b2c3d4e5f6'
down_revision = '6a9d8f1a6e1d'
branch_labels = None
depends_on = None


def upgrade():
    # Añadir campo hourly_employee_cost a la tabla companies
    op.add_column('companies', sa.Column('hourly_employee_cost', sa.Float(), nullable=True, default=12.0))
    
    # Crear tabla para arqueos de caja
    op.create_table('cash_registers',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('date', sa.Date(), nullable=False),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.Column('total_amount', sa.Float(), nullable=False, default=0.0),
        sa.Column('cash_amount', sa.Float(), nullable=False, default=0.0),
        sa.Column('card_amount', sa.Float(), nullable=False, default=0.0),
        sa.Column('delivery_cash_amount', sa.Float(), nullable=False, default=0.0),
        sa.Column('delivery_online_amount', sa.Float(), nullable=False, default=0.0),
        sa.Column('check_amount', sa.Float(), nullable=False, default=0.0),
        sa.Column('expenses_amount', sa.Float(), nullable=False, default=0.0),
        sa.Column('expenses_notes', sa.Text(), nullable=True),
        sa.Column('notes', sa.Text(), nullable=True),
        sa.Column('is_confirmed', sa.Boolean(), nullable=True, default=False),
        sa.Column('confirmed_at', sa.DateTime(), nullable=True),
        sa.Column('confirmed_by_id', sa.Integer(), nullable=True),
        sa.Column('company_id', sa.Integer(), nullable=False),
        sa.Column('created_by_id', sa.Integer(), nullable=True),
        sa.Column('employee_id', sa.Integer(), nullable=True),
        sa.Column('employee_name', sa.String(length=100), nullable=True),
        sa.ForeignKeyConstraint(['company_id'], ['companies.id'], ),
        sa.ForeignKeyConstraint(['confirmed_by_id'], ['users.id'], ),
        sa.ForeignKeyConstraint(['created_by_id'], ['users.id'], ),
        sa.ForeignKeyConstraint(['employee_id'], ['employees.id'], ),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('company_id', 'date', name='uq_company_date')
    )
    
    # Crear índices para búsquedas comunes
    op.create_index('idx_cash_register_company', 'cash_registers', ['company_id'], unique=False)
    op.create_index('idx_cash_register_date', 'cash_registers', ['date'], unique=False)
    
    # Crear tabla para resúmenes acumulados por períodos
    op.create_table('cash_register_summaries',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('year', sa.Integer(), nullable=False),
        sa.Column('month', sa.Integer(), nullable=False),
        sa.Column('week_number', sa.Integer(), nullable=False),
        sa.Column('weekly_total', sa.Float(), nullable=False, default=0.0),
        sa.Column('monthly_total', sa.Float(), nullable=False, default=0.0),
        sa.Column('yearly_total', sa.Float(), nullable=False, default=0.0),
        sa.Column('weekly_cash', sa.Float(), nullable=False, default=0.0),
        sa.Column('weekly_card', sa.Float(), nullable=False, default=0.0),
        sa.Column('weekly_delivery_cash', sa.Float(), nullable=False, default=0.0),
        sa.Column('weekly_delivery_online', sa.Float(), nullable=False, default=0.0),
        sa.Column('weekly_check', sa.Float(), nullable=False, default=0.0),
        sa.Column('weekly_expenses', sa.Float(), nullable=False, default=0.0),
        sa.Column('weekly_staff_cost', sa.Float(), nullable=False, default=0.0),
        sa.Column('monthly_staff_cost', sa.Float(), nullable=False, default=0.0),
        sa.Column('weekly_staff_cost_percentage', sa.Float(), nullable=False, default=0.0),
        sa.Column('monthly_staff_cost_percentage', sa.Float(), nullable=False, default=0.0),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.Column('company_id', sa.Integer(), nullable=False),
        sa.ForeignKeyConstraint(['company_id'], ['companies.id'], ),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('company_id', 'year', 'month', 'week_number', name='uq_summary_period')
    )
    
    # Crear índices para búsquedas comunes en resúmenes
    op.create_index('idx_summary_company', 'cash_register_summaries', ['company_id'], unique=False)
    op.create_index('idx_summary_year_month', 'cash_register_summaries', ['year', 'month'], unique=False)
    
    # Crear tabla para tokens de acceso
    op.create_table('cash_register_tokens',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('token', sa.String(length=64), nullable=False),
        sa.Column('is_active', sa.Boolean(), nullable=True, default=True),
        sa.Column('expires_at', sa.DateTime(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('used_at', sa.DateTime(), nullable=True),
        sa.Column('company_id', sa.Integer(), nullable=False),
        sa.Column('created_by_id', sa.Integer(), nullable=True),
        sa.Column('employee_id', sa.Integer(), nullable=True),
        sa.Column('cash_register_id', sa.Integer(), nullable=True),
        sa.ForeignKeyConstraint(['cash_register_id'], ['cash_registers.id'], ),
        sa.ForeignKeyConstraint(['company_id'], ['companies.id'], ),
        sa.ForeignKeyConstraint(['created_by_id'], ['users.id'], ),
        sa.ForeignKeyConstraint(['employee_id'], ['employees.id'], ),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('token')
    )


def downgrade():
    # Eliminar tablas en orden inverso
    op.drop_table('cash_register_tokens')
    op.drop_index('idx_summary_year_month', table_name='cash_register_summaries')
    op.drop_index('idx_summary_company', table_name='cash_register_summaries')
    op.drop_table('cash_register_summaries')
    op.drop_index('idx_cash_register_date', table_name='cash_registers')
    op.drop_index('idx_cash_register_company', table_name='cash_registers')
    op.drop_table('cash_registers')
    op.drop_column('companies', 'hourly_employee_cost')
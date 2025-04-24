"""
Formularios para el módulo de Arqueos de Caja.

Este módulo define los formularios necesarios para la gestión de arqueos de caja,
incluyendo el formulario de arqueo diario, búsqueda y filtrado, y generación de tokens.
"""

from datetime import datetime, date
from decimal import Decimal
from wtforms import (
    StringField, DateField, FloatField, TextAreaField, BooleanField,
    SelectField, HiddenField, SubmitField, IntegerField, validators
)
from flask_wtf import FlaskForm
from wtforms.validators import DataRequired, Optional, ValidationError, Length


class CashRegisterForm(FlaskForm):
    """
    Formulario para registrar un arqueo de caja diario.
    
    Los gastos se registran como información adicional pero no afectan
    al total del arqueo, que solo incluye los ingresos por diferentes
    métodos de pago.
    """
    company_id = HiddenField('ID de Empresa', validators=[DataRequired()])
    date = DateField('Fecha', validators=[DataRequired()], format='%Y-%m-%d', default=date.today)
    
    # Campos de importes
    total_amount = FloatField('Importe Total (€)', default=0, 
                              validators=[validators.NumberRange(min=0)])
    
    cash_amount = FloatField('Efectivo (€)', default=0, 
                             validators=[validators.NumberRange(min=0)])
    
    card_amount = FloatField('Tarjeta (€)', default=0, 
                             validators=[validators.NumberRange(min=0)])
    
    delivery_cash_amount = FloatField('Delivery - Efectivo (€)', default=0, 
                                     validators=[validators.NumberRange(min=0)])
    
    delivery_online_amount = FloatField('Delivery - Online (€)', default=0, 
                                       validators=[validators.NumberRange(min=0)])
    
    check_amount = FloatField('Cheque (€)', default=0, 
                             validators=[validators.NumberRange(min=0)])
    
    expenses_amount = FloatField('Gastos (€)', default=0, 
                                validators=[validators.NumberRange(min=0)])
    
    # Campos de notas
    expenses_notes = TextAreaField('Detalle de Gastos', validators=[Optional(), Length(max=500)])
    notes = TextAreaField('Notas', validators=[Optional(), Length(max=500)])
    
    # Campos adicionales
    employee_id = SelectField('Empleado', coerce=int, validators=[Optional()])
    employee_name = StringField('Nombre del Empleado', validators=[Optional(), Length(max=100)])
    
    submit = SubmitField('Guardar Arqueo')
    
    def validate_total_amount(self, field):
        """Validación del importe total."""
        # Calcular la suma de todos los métodos de pago (no incluye gastos)
        # Los gastos se registran como información adicional pero no afectan al total
        total_payments = (
            self.cash_amount.data +
            self.card_amount.data +
            self.delivery_cash_amount.data +
            self.delivery_online_amount.data +
            self.check_amount.data
        )
        
        # Verificar que coincida con el total declarado
        if abs(field.data - total_payments) > 0.01:  # Permitir pequeñas diferencias por redondeo
            field.errors = list(field.errors)
            field.errors.append(
                f"El importe total ({field.data:.2f} €) no coincide con la suma de los métodos de pago ({total_payments:.2f} €)"
            )
            return False
        
        return True


class CashRegisterSearchForm(FlaskForm):
    """
    Formulario para búsqueda y filtrado de arqueos de caja.
    """
    company_id = SelectField('Empresa', coerce=int, validators=[DataRequired()])
    start_date = DateField('Fecha Desde', format='%Y-%m-%d', validators=[Optional()])
    end_date = DateField('Fecha Hasta', format='%Y-%m-%d', validators=[Optional()])
    is_confirmed = SelectField('Estado', choices=[
        ('all', 'Todos'),
        ('true', 'Confirmados'),
        ('false', 'Pendientes')
    ], default='all')
    
    year = IntegerField('Año', validators=[Optional()])
    month = SelectField('Mes', choices=[
        (0, 'Todos'),
        (1, 'Enero'),
        (2, 'Febrero'),
        (3, 'Marzo'),
        (4, 'Abril'),
        (5, 'Mayo'),
        (6, 'Junio'),
        (7, 'Julio'),
        (8, 'Agosto'),
        (9, 'Septiembre'),
        (10, 'Octubre'),
        (11, 'Noviembre'),
        (12, 'Diciembre')
    ], coerce=int, default=0)
    
    week = IntegerField('Semana', validators=[Optional()])
    
    submit = SubmitField('Buscar')
    
    def validate_start_date(self, field):
        """Validación de fecha inicio < fecha fin."""
        if field.data and self.end_date.data and field.data > self.end_date.data:
            raise ValidationError('La fecha de inicio no puede ser posterior a la fecha de fin')


# Eliminado el formulario CashRegisterConfirmForm ya que ya no es necesario


class CashRegisterTokenForm(FlaskForm):
    """
    Formulario para generar tokens de acceso para empleados.
    """
    company_id = SelectField('Empresa', coerce=int, validators=[DataRequired()])
    employee_id = SelectField('Empleado', coerce=int, validators=[Optional()])
    expiry_days = IntegerField('Días de validez', validators=[Optional()], default=7,
                              description="Deja en 0 para crear un token sin caducidad")
    pin = StringField('PIN de acceso (opcional)', validators=[Optional(), Length(min=4, max=6)],
                     description="PIN numérico simple para acceder al formulario (4-6 dígitos)")
    
    submit = SubmitField('Generar Token')


class PinVerificationForm(FlaskForm):
    """
    Formulario para verificar el PIN de acceso de un token.
    """
    token = HiddenField('Token', validators=[DataRequired()])
    pin = StringField('PIN de acceso', validators=[DataRequired(), Length(min=4, max=6)],
                     description="Introduce el PIN que te han proporcionado para acceder al formulario")
    
    submit = SubmitField('Verificar')


class PublicCashRegisterForm(FlaskForm):
    """
    Formulario público para que empleados envíen datos de arqueo mediante token.
    
    Los gastos se registran como información adicional pero no afectan
    al total del arqueo, que solo incluye los ingresos por diferentes
    métodos de pago.
    """
    token = HiddenField('Token', validators=[DataRequired()])
    date = DateField('Fecha', validators=[DataRequired()], format='%Y-%m-%d', default=date.today)
    
    # Campos de importes
    total_amount = FloatField('Importe Total (€)', default=0, 
                              validators=[validators.NumberRange(min=0)])
    
    cash_amount = FloatField('Efectivo (€)', default=0, 
                             validators=[validators.NumberRange(min=0)])
    
    card_amount = FloatField('Tarjeta (€)', default=0, 
                             validators=[validators.NumberRange(min=0)])
    
    delivery_cash_amount = FloatField('Delivery - Efectivo (€)', default=0, 
                                     validators=[validators.NumberRange(min=0)])
    
    delivery_online_amount = FloatField('Delivery - Online (€)', default=0, 
                                       validators=[validators.NumberRange(min=0)])
    
    check_amount = FloatField('Cheque (€)', default=0, 
                             validators=[validators.NumberRange(min=0)])
    
    expenses_amount = FloatField('Gastos (€)', default=0, 
                                validators=[validators.NumberRange(min=0)])
    
    # Campos de notas
    expenses_notes = TextAreaField('Detalle de Gastos', validators=[Optional(), Length(max=500)])
    notes = TextAreaField('Notas', validators=[Optional(), Length(max=500)])
    
    # Nombre del empleado (por si no tiene cuenta)
    employee_name = StringField('Tu Nombre', validators=[DataRequired(), Length(max=100)])
    
    submit = SubmitField('Enviar Arqueo')
    def validate_total_amount(self, field):
        """Validación del importe total."""
        # Calcular la suma de todos los métodos de pago (no incluye gastos)
        # Los gastos se registran como información adicional pero no afectan al total
        total_payments = (
            self.cash_amount.data +
            self.card_amount.data +
            self.delivery_cash_amount.data +
            self.delivery_online_amount.data +
            self.check_amount.data
        )
        
        # Verificar que coincida con el total declarado
        if abs(field.data - total_payments) > 0.01:  # Permitir pequeñas diferencias por redondeo
            field.errors = list(field.errors)
            field.errors.append(
                f"El importe total ({field.data:.2f} €) no coincide con la suma de los métodos de pago ({total_payments:.2f} €)"
            )
            return False
        
        return True
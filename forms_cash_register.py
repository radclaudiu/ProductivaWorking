"""
Formularios para el módulo de Arqueos de Caja.

Este módulo contiene los formularios necesarios para:
- Crear y editar arqueos
- Filtrar listados de arqueos
- Generar tokens para empleados
- Formulario público para empleados sin registro
"""

from datetime import date, datetime, timedelta
from flask_wtf import FlaskForm
from wtforms import (
    StringField, DecimalField, DateField, TextAreaField, SubmitField,
    SelectField, BooleanField, IntegerField, HiddenField
)
from wtforms.validators import DataRequired, Optional, Length, NumberRange, ValidationError

class CashRegisterForm(FlaskForm):
    """
    Formulario para crear y editar arqueos de caja.
    """
    date = DateField('Fecha', validators=[DataRequired()], format='%Y-%m-%d')
    
    total_amount = DecimalField('Importe Total (€)', 
                              validators=[DataRequired(), NumberRange(min=0)],
                              places=2, default=0)
    
    cash_amount = DecimalField('Efectivo (€)', 
                            validators=[DataRequired(), NumberRange(min=0)],
                            places=2, default=0)
    
    card_amount = DecimalField('Tarjeta (€)', 
                            validators=[Optional(), NumberRange(min=0)],
                            places=2, default=0)
    
    delivery_cash_amount = DecimalField('Delivery Efectivo (€)', 
                                     validators=[Optional(), NumberRange(min=0)],
                                     places=2, default=0)
    
    delivery_online_amount = DecimalField('Delivery Online (€)', 
                                       validators=[Optional(), NumberRange(min=0)],
                                       places=2, default=0)
    
    check_amount = DecimalField('Cheques (€)', 
                             validators=[Optional(), NumberRange(min=0)],
                             places=2, default=0)
    
    expenses_amount = DecimalField('Gastos (€)', 
                                validators=[Optional(), NumberRange(min=0)],
                                places=2, default=0)
    
    expenses_notes = TextAreaField('Notas de Gastos', 
                                validators=[Optional(), Length(max=500)])
    
    notes = TextAreaField('Notas Generales', 
                        validators=[Optional(), Length(max=500)])
    
    submit = SubmitField('Guardar Arqueo')
    
    def validate(self, extra_validators=None):
        """Validación personalizada para verificar que la suma de los importes coincide con el total."""
        if not super().validate(extra_validators):
            return False
            
        total = self.total_amount.data
        sum_parts = (self.cash_amount.data + 
                    self.card_amount.data + 
                    self.delivery_cash_amount.data + 
                    self.delivery_online_amount.data + 
                    self.check_amount.data)
                    
        # Permitir una pequeña diferencia por redondeo (máximo 1 céntimo)
        if abs(total - sum_parts) > 0.01:
            self.total_amount.errors.append(
                f'El importe total ({total:.2f}€) debe ser igual a la suma de los desgloses ({sum_parts:.2f}€)')
            return False
            
        return True

class CashRegisterFilterForm(FlaskForm):
    """
    Formulario para filtrar arqueos en el dashboard.
    """
    start_date = DateField('Desde', format='%Y-%m-%d', 
                        validators=[Optional()],
                        default=lambda: date.today() - timedelta(days=7))
    
    end_date = DateField('Hasta', format='%Y-%m-%d', 
                       validators=[Optional()],
                       default=date.today)
    
    submit = SubmitField('Filtrar')
    
    def validate(self, extra_validators=None):
        """Validación personalizada para asegurar que la fecha de inicio no es posterior a la fecha de fin."""
        if not super().validate(extra_validators):
            return False
            
        if self.start_date.data and self.end_date.data and self.start_date.data > self.end_date.data:
            self.start_date.errors.append('La fecha de inicio no puede ser posterior a la fecha de fin')
            return False
            
        return True

class CashRegisterTokenForm(FlaskForm):
    """
    Formulario para crear tokens de acceso para empleados sin registro.
    """
    employee_id = SelectField('Empleado', coerce=int, validators=[Optional()])
    
    expiry_days = IntegerField('Días de validez', 
                            validators=[DataRequired(), NumberRange(min=1, max=30)],
                            default=1)
    
    submit = SubmitField('Generar Token')

class PublicCashRegisterForm(FlaskForm):
    """
    Formulario público para que empleados sin acceso puedan registrar arqueos.
    """
    employee_name = StringField('Tu nombre', 
                             validators=[Optional(), Length(max=100)],
                             render_kw={"placeholder": "Opcional si ya estás asignado al token"})
    
    date = DateField('Fecha', validators=[DataRequired()], format='%Y-%m-%d',
                  default=date.today)
    
    total_amount = DecimalField('Importe Total (€)', 
                             validators=[DataRequired(), NumberRange(min=0)],
                             places=2, default=0)
    
    cash_amount = DecimalField('Efectivo (€)', 
                            validators=[DataRequired(), NumberRange(min=0)],
                            places=2, default=0)
    
    card_amount = DecimalField('Tarjeta (€)', 
                            validators=[Optional(), NumberRange(min=0)],
                            places=2, default=0)
    
    delivery_cash_amount = DecimalField('Delivery Efectivo (€)', 
                                     validators=[Optional(), NumberRange(min=0)],
                                     places=2, default=0)
    
    delivery_online_amount = DecimalField('Delivery Online (€)', 
                                       validators=[Optional(), NumberRange(min=0)],
                                       places=2, default=0)
    
    check_amount = DecimalField('Cheques (€)', 
                             validators=[Optional(), NumberRange(min=0)],
                             places=2, default=0)
    
    expenses_amount = DecimalField('Gastos (€)', 
                               validators=[Optional(), NumberRange(min=0)],
                               places=2, default=0)
    
    expenses_notes = TextAreaField('Notas de Gastos (explicar para qué se ha usado el dinero)', 
                                validators=[Optional(), Length(max=500)])
    
    confirm = BooleanField('Confirmo que los datos son correctos', validators=[DataRequired()])
    
    submit = SubmitField('Enviar Arqueo')
    
    def validate(self, extra_validators=None):
        """Validación personalizada para verificar que la suma de los importes coincide con el total."""
        if not super().validate(extra_validators):
            return False
            
        total = self.total_amount.data
        sum_parts = (self.cash_amount.data + 
                    self.card_amount.data + 
                    self.delivery_cash_amount.data + 
                    self.delivery_online_amount.data + 
                    self.check_amount.data)
                    
        # Permitir una pequeña diferencia por redondeo (máximo 1 céntimo)
        if abs(total - sum_parts) > 0.01:
            self.total_amount.errors.append(
                f'El importe total ({total:.2f}€) debe ser igual a la suma de los desgloses ({sum_parts:.2f}€)')
            return False
            
        return True
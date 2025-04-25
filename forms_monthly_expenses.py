"""
Formularios para el módulo de gastos mensuales.

Define los formularios WTForms para la gestión de categorías,
gastos fijos y gastos mensuales.
"""

from flask_wtf import FlaskForm
from wtforms import StringField, TextAreaField, FloatField, BooleanField, IntegerField
from wtforms import SelectField, HiddenField, SubmitField
from wtforms.validators import DataRequired, Length, NumberRange, Optional


class ExpenseCategoryForm(FlaskForm):
    """Formulario para crear o editar categorías de gastos."""
    name = StringField('Nombre', validators=[
        DataRequired(message="El nombre es obligatorio"),
        Length(min=2, max=100, message="El nombre debe tener entre 2 y 100 caracteres")
    ])
    description = TextAreaField('Descripción', validators=[
        Optional(),
        Length(max=500, message="La descripción no puede exceder los 500 caracteres")
    ])
    company_id = HiddenField('ID de Empresa')
    submit = SubmitField('Guardar')


class FixedExpenseForm(FlaskForm):
    """Formulario para crear o editar gastos fijos mensuales."""
    name = StringField('Nombre', validators=[
        DataRequired(message="El nombre es obligatorio"),
        Length(min=2, max=100, message="El nombre debe tener entre 2 y 100 caracteres")
    ])
    description = TextAreaField('Descripción', validators=[
        Optional(),
        Length(max=500, message="La descripción no puede exceder los 500 caracteres")
    ])
    amount = FloatField('Importe (€)', validators=[
        DataRequired(message="El importe es obligatorio"),
        NumberRange(min=0, message="El importe debe ser mayor o igual a 0")
    ])
    category_id = SelectField('Categoría', coerce=int, validators=[
        DataRequired(message="La categoría es obligatoria")
    ])
    is_active = BooleanField('Activo', default=True)
    company_id = HiddenField('ID de Empresa')
    submit = SubmitField('Guardar')


class MonthlyExpenseForm(FlaskForm):
    """Formulario para crear o editar gastos mensuales."""
    name = StringField('Nombre', validators=[
        DataRequired(message="El nombre es obligatorio"),
        Length(min=2, max=100, message="El nombre debe tener entre 2 y 100 caracteres")
    ])
    description = TextAreaField('Descripción', validators=[
        Optional(),
        Length(max=500, message="La descripción no puede exceder los 500 caracteres")
    ])
    amount = FloatField('Importe (€)', validators=[
        DataRequired(message="El importe es obligatorio"),
        NumberRange(min=0, message="El importe debe ser mayor o igual a 0")
    ])
    category_id = SelectField('Categoría', coerce=int, validators=[
        DataRequired(message="La categoría es obligatoria")
    ])
    is_fixed = BooleanField('Convertir en gasto fijo mensual', default=False)
    year = IntegerField('Año', validators=[
        DataRequired(message="El año es obligatorio"),
        NumberRange(min=2020, max=2099, message="El año debe estar entre 2020 y 2099")
    ])
    month = SelectField('Mes', coerce=int, validators=[
        DataRequired(message="El mes es obligatorio")
    ])
    company_id = HiddenField('ID de Empresa')
    submit = SubmitField('Guardar')
    
    def __init__(self, *args, **kwargs):
        super(MonthlyExpenseForm, self).__init__(*args, **kwargs)
        self.month.choices = [
            (1, 'Enero'), (2, 'Febrero'), (3, 'Marzo'), (4, 'Abril'),
            (5, 'Mayo'), (6, 'Junio'), (7, 'Julio'), (8, 'Agosto'),
            (9, 'Septiembre'), (10, 'Octubre'), (11, 'Noviembre'), (12, 'Diciembre')
        ]


class MonthlyExpenseSearchForm(FlaskForm):
    """Formulario para buscar y filtrar gastos mensuales."""
    year = IntegerField('Año', validators=[
        Optional(),
        NumberRange(min=2020, max=2099, message="El año debe estar entre 2020 y 2099")
    ])
    month = SelectField('Mes', coerce=int, validators=[Optional()])
    category_id = SelectField('Categoría', coerce=int, validators=[Optional()])
    company_id = SelectField('Empresa', coerce=int, validators=[Optional()])
    is_fixed = SelectField('Tipo', coerce=int, validators=[Optional()])
    submit = SubmitField('Buscar')
    
    def __init__(self, *args, **kwargs):
        super(MonthlyExpenseSearchForm, self).__init__(*args, **kwargs)
        self.month.choices = [
            (0, 'Todos'),
            (1, 'Enero'), (2, 'Febrero'), (3, 'Marzo'), (4, 'Abril'),
            (5, 'Mayo'), (6, 'Junio'), (7, 'Julio'), (8, 'Agosto'),
            (9, 'Septiembre'), (10, 'Octubre'), (11, 'Noviembre'), (12, 'Diciembre')
        ]
        self.is_fixed.choices = [
            (0, 'Todos'),
            (1, 'Gastos Fijos'),
            (2, 'Gastos Personalizados')
        ]
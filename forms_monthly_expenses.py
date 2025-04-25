"""
Formularios para el sistema de gestión de gastos mensuales.

Este módulo define los formularios necesarios para el sistema de gastos mensuales,
incluyendo formularios para categorías, gastos fijos y gastos mensuales.
"""

import datetime
from flask_wtf import FlaskForm
from flask_wtf.file import FileField, FileAllowed
from wtforms import StringField, TextAreaField, FloatField, BooleanField, SubmitField
from wtforms import SelectField, HiddenField, IntegerField
from wtforms.validators import DataRequired, Length, Optional, NumberRange

# Para obtener lista de meses
def get_months_list():
    return [
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
    ]

# Para obtener lista de años (actual y 5 años atrás y adelante)
def get_years_list():
    current_year = datetime.datetime.now().year
    return [(year, str(year)) for year in range(current_year - 5, current_year + 6)]


class ExpenseCategoryForm(FlaskForm):
    """
    Formulario para crear y editar categorías de gastos.
    """
    name = StringField('Nombre', validators=[
        DataRequired(message="El nombre es obligatorio."),
        Length(min=2, max=100, message="El nombre debe tener entre 2 y 100 caracteres.")
    ])
    
    description = TextAreaField('Descripción', validators=[
        Optional(),
        Length(max=500, message="La descripción no puede exceder los 500 caracteres.")
    ])
    
    submit = SubmitField('Guardar Categoría')


class FixedExpenseForm(FlaskForm):
    """
    Formulario para crear y editar gastos fijos.
    """
    company_id = HiddenField('ID Empresa')
    
    name = StringField('Nombre', validators=[
        DataRequired(message="El nombre es obligatorio."),
        Length(min=2, max=100, message="El nombre debe tener entre 2 y 100 caracteres.")
    ])
    
    description = TextAreaField('Descripción', validators=[
        Optional(),
        Length(max=500, message="La descripción no puede exceder los 500 caracteres.")
    ])
    
    amount = FloatField('Importe (€)', validators=[
        DataRequired(message="El importe es obligatorio."),
        NumberRange(min=0, message="El importe no puede ser negativo.")
    ])
    
    category_id = SelectField('Categoría', coerce=int, validators=[
        DataRequired(message="La categoría es obligatoria.")
    ])
    
    is_active = BooleanField('Activo')
    
    submit = SubmitField('Guardar Gasto Fijo')


class MonthlyExpenseForm(FlaskForm):
    """
    Formulario para crear y editar gastos mensuales.
    """
    company_id = HiddenField('ID Empresa')
    
    year = SelectField('Año', coerce=int, validators=[
        DataRequired(message="El año es obligatorio.")
    ])
    
    month = SelectField('Mes', coerce=int, validators=[
        DataRequired(message="El mes es obligatorio.")
    ])
    
    name = StringField('Nombre', validators=[
        DataRequired(message="El nombre es obligatorio."),
        Length(min=2, max=100, message="El nombre debe tener entre 2 y 100 caracteres.")
    ])
    
    description = TextAreaField('Descripción', validators=[
        Optional(),
        Length(max=500, message="La descripción no puede exceder los 500 caracteres.")
    ])
    
    amount = FloatField('Importe (€)', validators=[
        DataRequired(message="El importe es obligatorio."),
        NumberRange(min=0, message="El importe no puede ser negativo.")
    ])
    
    category_id = SelectField('Categoría', coerce=int, validators=[
        DataRequired(message="La categoría es obligatoria.")
    ])
    
    is_fixed = BooleanField('Crear también como gasto fijo')
    
    submit = SubmitField('Guardar Gasto Mensual')


class PeriodSelectorForm(FlaskForm):
    """
    Formulario para seleccionar un período (mes y año) para los informes.
    """
    year = SelectField('Año', coerce=int, validators=[
        DataRequired(message="El año es obligatorio.")
    ])
    
    month = SelectField('Mes', coerce=int, validators=[
        DataRequired(message="El mes es obligatorio.")
    ])
    
    submit = SubmitField('Filtrar')


class MonthlyExpenseSearchForm(FlaskForm):
    """
    Formulario para buscar gastos mensuales.
    """
    query = StringField('Buscar', validators=[Optional()])
    
    category_id = SelectField('Categoría', coerce=int, validators=[Optional()])
    
    start_date = StringField('Desde (MM/YYYY)', validators=[Optional()])
    
    end_date = StringField('Hasta (MM/YYYY)', validators=[Optional()])
    
    submit = SubmitField('Buscar')


class MonthlyExpenseTokenForm(FlaskForm):
    """
    Formulario para crear o editar tokens de envío de gastos por empleados.
    """
    name = StringField('Nombre descriptivo', validators=[
        DataRequired(message="El nombre es obligatorio."),
        Length(min=2, max=100, message="El nombre debe tener entre 2 y 100 caracteres.")
    ])
    
    description = TextAreaField('Descripción', validators=[
        Optional(),
        Length(max=500, message="La descripción no puede exceder los 500 caracteres.")
    ])
    
    category_id = SelectField('Categoría predeterminada', coerce=int, validators=[Optional()])
    
    is_active = BooleanField('Activo', default=True)
    
    submit = SubmitField('Guardar Token')


class EmployeeExpenseForm(FlaskForm):
    """
    Formulario para que los empleados envíen gastos usando un token.
    """
    token = StringField('Token de Acceso', validators=[
        DataRequired(message="El token es obligatorio."),
        Length(max=20, message="El token no puede exceder los 20 caracteres.")
    ])
    
    name = StringField('Concepto del Gasto', validators=[
        DataRequired(message="El concepto es obligatorio."),
        Length(min=2, max=100, message="El concepto debe tener entre 2 y 100 caracteres.")
    ])
    
    description = TextAreaField('Descripción o Detalles', validators=[
        Optional(),
        Length(max=500, message="La descripción no puede exceder los 500 caracteres.")
    ])
    
    amount = FloatField('Importe (€)', validators=[
        DataRequired(message="El importe es obligatorio."),
        NumberRange(min=0, message="El importe no puede ser negativo.")
    ])
    
    employee_name = StringField('Tu Nombre', validators=[
        DataRequired(message="Tu nombre es obligatorio."),
        Length(min=2, max=100, message="El nombre debe tener entre 2 y 100 caracteres.")
    ])
    
    category_id = SelectField('Categoría', coerce=int, validators=[Optional()])
    
    expense_date = StringField('Fecha del Gasto (DD-MM-YYYY)', validators=[Optional()])
    
    receipt_image = FileField('Imagen del Recibo/Factura (opcional)', 
                             validators=[
                                 Optional(),
                                 FileAllowed(['jpg', 'jpeg', 'png', 'pdf'], 
                                            'Solo se permiten archivos de imagen (JPG, PNG) o PDF')
                             ])
    
    submit = SubmitField('Enviar Gasto')
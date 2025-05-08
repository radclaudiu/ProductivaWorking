from flask_wtf import FlaskForm
from wtforms import StringField, TextAreaField, SelectField, BooleanField, SubmitField, TimeField
from wtforms import FloatField, PasswordField, IntegerField, HiddenField
from wtforms.validators import DataRequired, Length, Optional, ValidationError, NumberRange
from flask_wtf.file import FileField, FileAllowed
from models_checkpoints import CheckPointStatus, CheckPointIncidentType
from datetime import datetime, time


class CheckPointForm(FlaskForm):
    """Formulario para crear y editar puntos de fichaje"""
    name = StringField('Nombre', validators=[DataRequired(), Length(max=128)])
    description = TextAreaField('Descripción', validators=[Optional(), Length(max=500)])
    location = StringField('Ubicación', validators=[Optional(), Length(max=256)])
    status = SelectField('Estado', choices=[(status.value, status.name.capitalize()) for status in CheckPointStatus])
    
    username = StringField('Usuario', validators=[DataRequired(), Length(min=4, max=64)])
    password = PasswordField('Contraseña', validators=[Length(min=6, max=64)])
    # Campo de confirmación de contraseña eliminado
    
    # El campo auto_checkout_time ha sido eliminado
    enforce_contract_hours = BooleanField('Aplicar límite horas contrato', default=False)
    auto_adjust_overtime = BooleanField('Ajustar automáticamente horas extra', default=False)
    
    # Configuración de horario de cierre automático
    enforce_operation_hours = BooleanField('Aplicar horario de cierre automático', default=False)
    operation_start_time = TimeField('Hora de inicio de cierre automático', validators=[Optional()])
    operation_end_time = TimeField('Hora de fin de cierre automático', validators=[Optional()])
    
    company_id = SelectField('Empresa', coerce=int, validators=[DataRequired()])
    
    submit = SubmitField('Guardar')
    
    # La validación de la contraseña se realiza a través del validator Length en el campo
            
    def validate_operation_end_time(self, field):
        """Verifica que la hora de fin de cierre automático sea posterior a la de inicio"""
        if field.data and self.operation_start_time.data and field.data <= self.operation_start_time.data:
            raise ValidationError('La hora de fin de cierre automático debe ser posterior a la hora de inicio')


class CheckPointEmployeePinForm(FlaskForm):
    """Formulario para introducir el PIN del empleado en el punto de fichaje"""
    pin = StringField('PIN (4 dígitos)', validators=[
        DataRequired(), 
        Length(min=4, max=4, message='El PIN debe tener exactamente 4 dígitos')
    ])
    employee_id = HiddenField('ID Empleado', validators=[DataRequired()])
    submit = SubmitField('Confirmar')


class CheckPointLoginForm(FlaskForm):
    """Formulario para iniciar sesión en un punto de fichaje"""
    username = StringField('Usuario', validators=[DataRequired()])
    password = PasswordField('Contraseña', validators=[DataRequired()])
    submit = SubmitField('Iniciar Sesión')


class ContractHoursForm(FlaskForm):
    """Formulario para configurar las horas de contrato de un empleado"""
    daily_hours = FloatField('Horas diarias', validators=[
        DataRequired(), 
        NumberRange(min=0.5, max=24, message='Las horas diarias deben estar entre 0.5 y 24')
    ])
    weekly_hours = FloatField('Horas semanales', validators=[
        DataRequired(), 
        NumberRange(min=1, max=168, message='Las horas semanales deben estar entre 1 y 168')
    ])
    
    allow_overtime = BooleanField('Permitir horas extra', default=False)
    max_overtime_daily = FloatField('Máximo horas extra diarias', validators=[
        Optional(), 
        NumberRange(min=0, max=12, message='Las horas extra deben estar entre 0 y 12')
    ])
    
    use_normal_schedule = BooleanField('Aplicar horario normal', default=False)
    normal_start_time = TimeField('Hora normal de entrada', validators=[Optional()])
    normal_end_time = TimeField('Hora normal de salida', validators=[Optional()])
    
    use_flexibility = BooleanField('Aplicar margen de flexibilidad', default=False)
    checkin_flexibility = IntegerField('Flexibilidad entrada (minutos)', validators=[
        Optional(),
        NumberRange(min=0, max=120, message='La flexibilidad debe estar entre 0 y 120 minutos')
    ])
    checkout_flexibility = IntegerField('Flexibilidad salida (minutos)', validators=[
        Optional(),
        NumberRange(min=0, max=120, message='La flexibilidad debe estar entre 0 y 120 minutos')
    ])
    
    submit = SubmitField('Guardar Configuración')
    
    def validate_weekly_hours(self, field):
        """Verifica que las horas semanales sean coherentes con las diarias"""
        if field.data < self.daily_hours.data:
            raise ValidationError('Las horas semanales no pueden ser menos que las diarias')
        
    def validate_normal_end_time(self, field):
        """Verifica que la hora de fin sea posterior a la de inicio"""
        if field.data and self.normal_start_time.data and field.data <= self.normal_start_time.data:
            raise ValidationError('La hora de fin debe ser posterior a la hora de inicio')


class CheckPointRecordAdjustmentForm(FlaskForm):
    """Formulario para ajustar manualmente un fichaje"""
    check_in_date = StringField('Fecha', validators=[DataRequired()])
    check_in_time = TimeField('Hora de entrada', validators=[DataRequired()])
    check_out_time = TimeField('Hora de salida', validators=[Optional()])
    
    adjustment_reason = TextAreaField('Motivo del ajuste', validators=[DataRequired(), Length(max=500)])
    
    enforce_contract_hours = BooleanField('Aplicar límite de horas de contrato', default=False)
    
    submit = SubmitField('Guardar Ajuste')
    
    def validate_check_out_time(self, field):
        """Verifica que la hora de salida sea posterior a la de entrada"""
        if field.data and field.data <= self.check_in_time.data:
            raise ValidationError('La hora de salida debe ser posterior a la hora de entrada')


class SignaturePadForm(FlaskForm):
    """Formulario para la firma digital en los fichajes"""
    signature_data = HiddenField('Firma', validators=[DataRequired()])
    record_id = HiddenField('ID del Registro', validators=[DataRequired()])
    submit = SubmitField('Confirmar Firma')


class ExportCheckPointRecordsForm(FlaskForm):
    """Formulario para exportar registros de fichaje"""
    start_date = StringField('Fecha de inicio', validators=[DataRequired()])
    end_date = StringField('Fecha de fin', validators=[DataRequired()])
    employee_id = SelectField('Empleado', coerce=int, validators=[Optional()])
    include_signature = BooleanField('Incluir firma', default=True)
    
    submit = SubmitField('Exportar')
    
    def validate_end_date(self, field):
        """Verifica que la fecha de fin sea posterior a la de inicio"""
        try:
            start = datetime.strptime(self.start_date.data, '%Y-%m-%d')
            end = datetime.strptime(field.data, '%Y-%m-%d')
            
            if end < start:
                raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio')
        except ValueError:
            raise ValidationError('Formato de fecha incorrecto. Use YYYY-MM-DD')


class DeleteCheckPointRecordsForm(FlaskForm):
    """Formulario para eliminar registros de fichaje de un empleado en un rango de fechas"""
    start_date = StringField('Fecha de inicio', validators=[DataRequired()])
    end_date = StringField('Fecha de fin', validators=[DataRequired()])
    employee_id = SelectField('Empleado', coerce=int, validators=[DataRequired()])
    confirmation = StringField('Escriba CONFIRMAR para eliminar', validators=[
        DataRequired(),
        Length(min=9, max=9, message='Debe escribir exactamente CONFIRMAR')
    ])
    
    submit = SubmitField('Eliminar Fichajes')
    
    def validate_confirmation(self, field):
        """Verifica que se haya escrito la confirmación correctamente"""
        if field.data != 'CONFIRMAR':
            raise ValidationError('Debe escribir exactamente CONFIRMAR para continuar')
    
    def validate_end_date(self, field):
        """Verifica que la fecha de fin sea posterior a la de inicio"""
        try:
            start = datetime.strptime(self.start_date.data, '%Y-%m-%d')
            end = datetime.strptime(field.data, '%Y-%m-%d')
            
            if end < start:
                raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio')
        except ValueError:
            raise ValidationError('Formato de fecha incorrecto. Use YYYY-MM-DD')

class ManualCheckPointRecordForm(FlaskForm):
    """Formulario para crear un fichaje manual directamente en la tabla CheckPointRecord"""
    employee_id = SelectField('Empleado', coerce=int, validators=[DataRequired()])
    checkpoint_id = SelectField('Punto de Fichaje', coerce=int, validators=[DataRequired()])
    check_in_date = StringField('Fecha de entrada', validators=[DataRequired()])
    check_in_time = TimeField('Hora de entrada', validators=[DataRequired()])
    check_out_time = TimeField('Hora de salida', validators=[Optional()])
    notes = TextAreaField('Notas', validators=[Optional(), Length(max=500)])
    submit = SubmitField('Crear Fichaje')
    
    def validate_check_out_time(self, field):
        """Verifica que la hora de salida sea posterior a la de entrada si está presente"""
        if field.data and self.check_in_time.data:
            if field.data < self.check_in_time.data:
                # No hacemos validación, ya que podría ser del día siguiente
                pass

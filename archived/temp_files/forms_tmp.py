from flask_wtf import FlaskForm
from flask_wtf.file import FileField, FileAllowed, FileRequired
from wtforms import StringField, PasswordField, SubmitField, TextAreaField, SelectField, widgets
from wtforms import BooleanField, DateField, HiddenField, EmailField, TelField, URLField, TimeField
from wtforms import SelectMultipleField
from wtforms.validators import DataRequired, Email, EqualTo, Length, ValidationError, Optional
from datetime import date, datetime
from sqlalchemy import func

from models import User, ContractType, UserRole, EmployeeStatus, WeekDay, VacationStatus

class MultiCheckboxField(SelectMultipleField):
    """Campo personalizado para mostrar múltiples opciones como checkboxes."""
    widget = widgets.ListWidget(prefix_label=False)
    option_widget = widgets.CheckboxInput()

class LoginForm(FlaskForm):
    username = StringField('Usuario', validators=[DataRequired(), Length(min=3, max=64)])
    password = PasswordField('Contraseña', validators=[DataRequired()])
    remember_me = BooleanField('Recordarme')
    submit = SubmitField('Iniciar Sesión')

class RegistrationForm(FlaskForm):
    username = StringField('Usuario', validators=[DataRequired(), Length(min=3, max=64)])
    email = EmailField('Email', validators=[DataRequired(), Email()])
    password = PasswordField('Contraseña', validators=[DataRequired(), Length(min=8)])
    password2 = PasswordField('Repetir Contraseña', validators=[DataRequired(), EqualTo('password')])
    first_name = StringField('Nombre', validators=[DataRequired(), Length(max=64)])
    last_name = StringField('Apellidos', validators=[DataRequired(), Length(max=64)])
    role = SelectField('Rol', choices=[])  # Se llenará dinámicamente en __init__
    # Nuevo campo para selección múltiple de empresas
    companies = MultiCheckboxField('Empresas', coerce=int, 
                                 description='Selecciona una o más empresas para este usuario')
    submit = SubmitField('Registrar')
    
    def __init__(self, *args, **kwargs):
        super(RegistrationForm, self).__init__(*args, **kwargs)
        # Solo permitimos asignar el rol ADMIN si el usuario actual es "admin"
        from flask_login import current_user
        
        # Configurar las opciones de rol disponibles
        if current_user.is_authenticated and current_user.username == 'admin':
            # El usuario admin puede asignar cualquier rol
            self.role.choices = [(role.value, role.name.capitalize()) for role in UserRole]
        else:
            # Otros usuarios solo pueden asignar roles que no sean ADMIN
            self.role.choices = [(role.value, role.name.capitalize()) 
                                for role in UserRole if role != UserRole.ADMIN]
        
    def validate_username(self, username):
        user = User.query.filter_by(username=username.data).first()
        if user is not None:
            raise ValidationError('Por favor, usa un nombre de usuario diferente.')
            
    def validate_email(self, email):
        user = User.query.filter_by(email=email.data).first()
        if user is not None:
            raise ValidationError('Por favor, usa un email diferente.')
            
    def validate_role(self, role):
        from flask_login import current_user
        
        # Validar que solo el usuario "admin" pueda asignar el rol ADMIN
        if role.data == UserRole.ADMIN.value and (not current_user.is_authenticated or current_user.username != 'admin'):
            raise ValidationError('Solo el usuario "admin" puede asignar el rol de administrador.')

class UserUpdateForm(FlaskForm):
    username = StringField('Usuario', validators=[DataRequired(), Length(min=3, max=64)])
    email = EmailField('Email', validators=[DataRequired(), Email()])
    first_name = StringField('Nombre', validators=[DataRequired(), Length(max=64)])
    last_name = StringField('Apellidos', validators=[DataRequired(), Length(max=64)])
    role = SelectField('Rol', choices=[])  # Se llenará dinámicamente en __init__
    # Actualizado para usar selección múltiple de empresas
    companies = MultiCheckboxField('Empresas', coerce=int, 
                                 description='Selecciona una o más empresas para este usuario')
    is_active = BooleanField('Usuario Activo')
    submit = SubmitField('Actualizar Usuario')
    
    def __init__(self, original_username, original_email, *args, **kwargs):
        super(UserUpdateForm, self).__init__(*args, **kwargs)
        self.original_username = original_username
        self.original_email = original_email
        
        # Solo permitimos asignar el rol ADMIN si el usuario actual es "admin"
        from flask_login import current_user
        
        # Configurar las opciones de rol disponibles
        if current_user.is_authenticated and current_user.username == 'admin':
            # El usuario admin puede asignar cualquier rol
            self.role.choices = [(role.value, role.name.capitalize()) for role in UserRole]
        else:
            # Otros usuarios solo pueden asignar roles que no sean ADMIN
            self.role.choices = [(role.value, role.name.capitalize()) 
                               for role in UserRole if role != UserRole.ADMIN]
        
    def validate_username(self, username):
        if username.data != self.original_username:
            user = User.query.filter_by(username=username.data).first()
            if user is not None:
                raise ValidationError('Por favor, usa un nombre de usuario diferente.')
                
    def validate_email(self, email):
        if email.data != self.original_email:
            user = User.query.filter_by(email=email.data).first()
            if user is not None:
                raise ValidationError('Por favor, usa un email diferente.')
                
    def validate_role(self, role):
        from flask_login import current_user
        
        # Validar que solo el usuario "admin" pueda asignar el rol ADMIN
        if role.data == UserRole.ADMIN.value and (not current_user.is_authenticated or current_user.username != 'admin'):
            raise ValidationError('Solo el usuario "admin" puede asignar el rol de administrador.')

class PasswordChangeForm(FlaskForm):
    current_password = PasswordField('Contraseña Actual', validators=[DataRequired()])
    new_password = PasswordField('Nueva Contraseña', validators=[DataRequired(), Length(min=8)])
    confirm_password = PasswordField('Confirmar Contraseña', validators=[
        DataRequired(), EqualTo('new_password', message='Las contraseñas deben coincidir')
    ])
    submit = SubmitField('Cambiar Contraseña')

class CompanyForm(FlaskForm):
    name = StringField('Nombre', validators=[DataRequired(), Length(max=128)])
    address = StringField('Dirección', validators=[Length(max=256)])
    city = StringField('Ciudad', validators=[Length(max=64)])
    postal_code = StringField('Código Postal', validators=[Length(max=16)])
    country = StringField('País', validators=[Length(max=64)])
    sector = StringField('Sector', validators=[Length(max=64)])
    tax_id = StringField('CIF/NIF', validators=[DataRequired(), Length(max=32)])
    phone = TelField('Teléfono', validators=[Length(max=32)])
    email = EmailField('Email', validators=[Email(), Length(max=120)])
    website = URLField('Sitio Web', validators=[Length(max=128)])
    is_active = BooleanField('Empresa Activa')
    submit = SubmitField('Guardar')
    
    def __init__(self, original_tax_id=None, *args, **kwargs):
        super(CompanyForm, self).__init__(*args, **kwargs)
        self.original_tax_id = original_tax_id
        
    def validate_tax_id(self, tax_id):
        from models import Company
        # Si estamos editando y el CIF/NIF no ha cambiado, no hacemos validación adicional
        if self.original_tax_id and tax_id.data.lower() == self.original_tax_id.lower():
            return
            
        # Buscamos si existe alguna empresa con el mismo CIF/NIF (sin distinguir mayúsculas/minúsculas)
        company = Company.query.filter(func.lower(Company.tax_id) == func.lower(tax_id.data)).first()
        if company is not None:
            raise ValidationError('Ya existe una empresa con este CIF/NIF. Por favor, verifica los datos.')

class EmployeeForm(FlaskForm):
    first_name = StringField('Nombre', validators=[DataRequired(), Length(max=64)])
    last_name = StringField('Apellidos', validators=[DataRequired(), Length(max=64)])
    dni = StringField('DNI/NIE', validators=[DataRequired(), Length(max=16)])
    position = StringField('Puesto', validators=[Length(max=64)])
    contract_type = SelectField('Tipo de Contrato', 
                               choices=[(ct.value, ct.name.capitalize()) for ct in ContractType])
    bank_account = StringField('Cuenta Bancaria', validators=[Length(max=64)])
    start_date = DateField('Fecha de Inicio', validators=[Optional()], default=date.today)
    end_date = DateField('Fecha de Fin', validators=[Optional()])
    company_id = SelectField('Empresa', coerce=int, validators=[DataRequired()])
    is_active = BooleanField('Empleado Activo')
    status = SelectField('Estado', choices=[(status.value, status.name.capitalize()) for status in EmployeeStatus], 
                        default=EmployeeStatus.ACTIVO.value)
    submit = SubmitField('Guardar')
    
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

class EmployeeDocumentForm(FlaskForm):
    file = FileField('Documento', validators=[
        FileRequired(),
        FileAllowed(['pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png'], 'Solo se permiten archivos PDF, DOC, DOCX, JPG, JPEG y PNG')
    ])
    description = StringField('Descripción', validators=[Length(max=256)])
    submit = SubmitField('Subir')

class EmployeeNoteForm(FlaskForm):
    content = TextAreaField('Nota', validators=[DataRequired()])
    submit = SubmitField('Guardar')

class EmployeeStatusForm(FlaskForm):
    status = SelectField('Estado', choices=[(status.value, status.name.capitalize()) for status in EmployeeStatus])
    status_start_date = DateField('Fecha de Inicio', validators=[DataRequired()], default=date.today)
    status_end_date = DateField('Fecha de Fin Prevista', validators=[Optional()])
    status_notes = TextAreaField('Notas', validators=[Optional(), Length(max=500)])
    submit = SubmitField('Actualizar Estado')
    
    def validate_status_end_date(form, field):
        if field.data and form.status_start_date.data and field.data < form.status_start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

class SearchForm(FlaskForm):
    query = StringField('Buscar', validators=[DataRequired()])
    submit = SubmitField('Buscar')

class EmployeeScheduleForm(FlaskForm):
    day_of_week = SelectField('Día de la Semana', choices=[(day.value, day.name.capitalize()) for day in WeekDay])
    start_time = TimeField('Hora de Entrada', validators=[DataRequired()])
    end_time = TimeField('Hora de Salida', validators=[DataRequired()])
    is_working_day = BooleanField('Día Laborable', default=True)
    submit = SubmitField('Guardar Horario')
    
    def validate_end_time(form, field):
        if form.start_time.data and field.data and field.data <= form.start_time.data:
            raise ValidationError('La hora de salida debe ser posterior a la hora de entrada.')

class EmployeeWeeklyScheduleForm(FlaskForm):
    # Lunes
    lunes_is_working_day = BooleanField('Laborable', default=True)
    lunes_start_time = TimeField('Entrada', validators=[Optional()])
    lunes_end_time = TimeField('Salida', validators=[Optional()])
    
    # Martes
    martes_is_working_day = BooleanField('Laborable', default=True)
    martes_start_time = TimeField('Entrada', validators=[Optional()])
    martes_end_time = TimeField('Salida', validators=[Optional()])
    
    # Miércoles
    miercoles_is_working_day = BooleanField('Laborable', default=True)
    miercoles_start_time = TimeField('Entrada', validators=[Optional()])
    miercoles_end_time = TimeField('Salida', validators=[Optional()])
    
    # Jueves
    jueves_is_working_day = BooleanField('Laborable', default=True)
    jueves_start_time = TimeField('Entrada', validators=[Optional()])
    jueves_end_time = TimeField('Salida', validators=[Optional()])
    
    # Viernes
    viernes_is_working_day = BooleanField('Laborable', default=True)
    viernes_start_time = TimeField('Entrada', validators=[Optional()])
    viernes_end_time = TimeField('Salida', validators=[Optional()])
    
    # Sábado
    sabado_is_working_day = BooleanField('Laborable', default=False)
    sabado_start_time = TimeField('Entrada', validators=[Optional()])
    sabado_end_time = TimeField('Salida', validators=[Optional()])
    
    # Domingo
    domingo_is_working_day = BooleanField('Laborable', default=False)
    domingo_start_time = TimeField('Entrada', validators=[Optional()])
    domingo_end_time = TimeField('Salida', validators=[Optional()])
    
    submit = SubmitField('Guardar Horarios')
    
    def validate(self, **kwargs):
        if not super().validate():
            return False
        
        # Verificar que para cada día marcado como laborable se hayan introducido horas
        for day in ["lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"]:
            is_working_day = getattr(self, f"{day}_is_working_day").data
            start_time = getattr(self, f"{day}_start_time").data
            end_time = getattr(self, f"{day}_end_time").data
            
            if is_working_day:
                if not start_time:
                    field = getattr(self, f"{day}_start_time")
                    field.errors = ["Este campo es obligatorio para días laborables."]
                    return False
                
                if not end_time:
                    field = getattr(self, f"{day}_end_time")
                    field.errors = ["Este campo es obligatorio para días laborables."]
                    return False
                
                if end_time <= start_time:
                    field = getattr(self, f"{day}_end_time")
                    field.errors = ["La hora de salida debe ser posterior a la hora de entrada."]
                    return False
        
        return True

class EmployeeCheckInForm(FlaskForm):
    check_in_date = DateField('Fecha', validators=[DataRequired()], default=date.today)
    check_in_time = TimeField('Hora de Entrada', validators=[DataRequired()], default=lambda: datetime.now().time().replace(second=0, microsecond=0))
    check_out_time = TimeField('Hora de Salida', validators=[Optional()])
    notes = TextAreaField('Notas', validators=[Optional(), Length(max=500)])
    submit = SubmitField('Registrar Fichaje')
    
    def validate_check_out_time(form, field):
        if field.data and form.check_in_time.data and field.data < form.check_in_time.data:
            raise ValidationError('La hora de salida debe ser posterior a la hora de entrada.')

class EmployeeVacationForm(FlaskForm):
    start_date = DateField('Fecha de Inicio', validators=[DataRequired()], default=date.today)
    end_date = DateField('Fecha de Fin', validators=[DataRequired()])
    notes = TextAreaField('Notas', validators=[Optional(), Length(max=500)])
    submit = SubmitField('Registrar Vacaciones')
    
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

# Clase de aprobación de vacaciones eliminada, ya que no se requiere aprobación

class GenerateCheckInsForm(FlaskForm):
    start_date = DateField('Fecha de Inicio', validators=[DataRequired()], default=date.today)
    end_date = DateField('Fecha de Fin', validators=[DataRequired()])
    submit = SubmitField('Generar Fichajes')
    
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

class ExportCheckInsForm(FlaskForm):
    start_date = DateField('Fecha de Inicio', validators=[Optional()], default=lambda: date.today().replace(day=1))
    end_date = DateField('Fecha de Fin', validators=[Optional()], default=date.today)
    submit = SubmitField('Exportar a PDF')
    
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

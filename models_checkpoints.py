import enum
import random
from datetime import datetime, date, time, timedelta
from sqlalchemy import Enum
from werkzeug.security import generate_password_hash, check_password_hash
from app import db
from models import Employee, Company


class CheckPointStatus(enum.Enum):
    ACTIVE = "active"
    DISABLED = "disabled"
    MAINTENANCE = "maintenance"


class CheckPointIncidentType(enum.Enum):
    MISSED_CHECKOUT = "missed_checkout"
    LATE_CHECKIN = "late_checkin"
    EARLY_CHECKOUT = "early_checkout"
    OVERTIME = "overtime"
    MANUAL_ADJUSTMENT = "manual_adjustment"
    CONTRACT_HOURS_ADJUSTMENT = "contract_hours_adjustment"


class CheckPoint(db.Model):
    """Modelo para puntos de fichaje físicos (tablet, móvil, etc.)"""
    __tablename__ = 'checkpoints'

    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(128), nullable=False)
    description = db.Column(db.Text)
    location = db.Column(db.String(256))
    status = db.Column(Enum(CheckPointStatus), default=CheckPointStatus.ACTIVE)
    
    # Credenciales para el punto de fichaje
    username = db.Column(db.String(64), unique=True, nullable=False)
    password_hash = db.Column(db.String(256), nullable=False)
    
    # Configuración del punto de fichaje
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    company_id = db.Column(db.Integer, db.ForeignKey('companies.id'), nullable=False)
    company = db.relationship('Company', backref=db.backref('checkpoints', lazy=True))
    
    # Configuración de los ajustes automáticos
    enforce_contract_hours = db.Column(db.Boolean, default=False)
    auto_adjust_overtime = db.Column(db.Boolean, default=False)
    
    # Configuración de ventana horaria para el cierre automático
    # Estas horas definen el rango dentro del cual se ejecutará la función de cierre
    # Por ejemplo, de 02:00 a 04:00 significa que solo se cerrarán fichajes si la hora actual está en ese rango
    operation_start_time = db.Column(db.Time, nullable=True)
    operation_end_time = db.Column(db.Time, nullable=True)
    enforce_operation_hours = db.Column(db.Boolean, default=False)
    
    # Fichajes realizados desde este punto
    check_ins = db.relationship('CheckPointRecord', back_populates='checkpoint', cascade='all, delete-orphan')
    
    def __repr__(self):
        return f'<CheckPoint {self.name} ({self.company.name})>'
    
    def set_password(self, password):
        self.password_hash = generate_password_hash(password)
    
    def verify_password(self, password):
        return check_password_hash(self.password_hash, password)


class CheckPointRecord(db.Model):
    """Registro de fichajes realizados desde un punto de fichaje"""
    __tablename__ = 'checkpoint_records'
    
    id = db.Column(db.Integer, primary_key=True)
    check_in_time = db.Column(db.DateTime, nullable=False)
    check_out_time = db.Column(db.DateTime)
    
    # Fichaje original antes de ajustes
    original_check_in_time = db.Column(db.DateTime)
    original_check_out_time = db.Column(db.DateTime)
    
    # Ajustes aplicados
    adjusted = db.Column(db.Boolean, default=False)
    adjustment_reason = db.Column(db.String(256))
    
    notes = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'), nullable=False)
    employee = db.relationship('Employee', backref=db.backref('checkpoint_records', lazy=True))
    
    checkpoint_id = db.Column(db.Integer, db.ForeignKey('checkpoints.id'), nullable=False)
    checkpoint = db.relationship('CheckPoint', back_populates='check_ins')
    
    # Incidencias relacionadas con este fichaje
    incidents = db.relationship('CheckPointIncident', back_populates='record', cascade='all, delete-orphan')
    
    # Firma del empleado (si se requiere)
    signature_data = db.Column(db.Text)
    has_signature = db.Column(db.Boolean, default=False)
    
    def __repr__(self):
        from timezone_config import datetime_to_madrid
        
        status = "Completado" if self.check_out_time else "Pendiente de salida"
        
        # Convertir la hora de check-in a zona horaria de Madrid para mostrarla
        if self.check_in_time:
            madrid_time = datetime_to_madrid(self.check_in_time)
            formatted_time = madrid_time.strftime("%d/%m/%Y %H:%M")
        else:
            formatted_time = "Sin fecha"
            
        return f'<Fichaje {self.employee.first_name} {formatted_time} - {status}>'
    
    def duration(self):
        """Calcula la duración del fichaje en horas"""
        if not self.check_out_time:
            return None
        
        # Asegurarse de que ambas fechas tengan la misma información de zona horaria
        from timezone_config import datetime_to_madrid
        
        # Convertir ambas fechas a aware con la misma zona horaria
        check_in = datetime_to_madrid(self.check_in_time)
        check_out = datetime_to_madrid(self.check_out_time)
        
        # Si la hora de salida es anterior a la de entrada, podría ser un turno nocturno
        # En ese caso, asumimos que la salida corresponde al día siguiente
        if check_out < check_in:
            # Ajustar manualmente el día para calcular correctamente
            delta = (check_out - check_in).total_seconds() + (24 * 3600)  # Sumar 24 horas en segundos
            return delta / 3600  # Convertir a horas
        
        delta = check_out - check_in
        return delta.total_seconds() / 3600  # Convertir a horas (formato decimal)
    
    @property
    def has_original_record(self):
        """Comprueba si este registro tiene un registro original asociado"""
        # Evitamos la importación cíclica usando el nombre de la tabla directamente
        return db.session.query(db.Model.metadata.tables['checkpoint_original_records']).filter_by(record_id=self.id).count() > 0
    
    def to_dict(self):
        """Convierte el registro a un diccionario para serialización"""
        # Importar la función de formateo para mostrar la duración correctamente
        from utils_checkpoints import format_hours_minutes
        
        # Calcular la duración
        duration_value = self.duration()
        formatted_duration = format_hours_minutes(duration_value) if duration_value is not None else None
        
        result = {
            'id': self.id,
            'employee_id': self.employee_id,
            'employee_name': f"{self.employee.first_name} {self.employee.last_name}",
            'check_in_time': self.check_in_time.isoformat() if self.check_in_time else None,
            'check_out_time': self.check_out_time.isoformat() if self.check_out_time else None,
            'duration': formatted_duration,
            'duration_decimal': None if duration_value is None else round(duration_value, 2),
            'adjusted': self.adjusted,
            'has_signature': self.has_signature,
            'has_original_record': self.has_original_record
        }
        
        if self.adjusted and self.original_check_in_time:
            result['original_check_in_time'] = self.original_check_in_time.isoformat()
            if self.original_check_out_time:
                result['original_check_out_time'] = self.original_check_out_time.isoformat()
                
        return result


class CheckPointIncident(db.Model):
    """Incidencias relacionadas con fichajes"""
    __tablename__ = 'checkpoint_incidents'
    
    id = db.Column(db.Integer, primary_key=True)
    incident_type = db.Column(Enum(CheckPointIncidentType))
    description = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    resolved = db.Column(db.Boolean, default=False)
    resolved_at = db.Column(db.DateTime)
    resolution_notes = db.Column(db.Text)
    
    # Relaciones
    record_id = db.Column(db.Integer, db.ForeignKey('checkpoint_records.id'), nullable=False)
    record = db.relationship('CheckPointRecord', back_populates='incidents')
    
    resolved_by_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    resolved_by = db.relationship('User')
    
    def __repr__(self):
        from timezone_config import datetime_to_madrid
        
        # Convertir la fecha de creación a zona horaria de Madrid
        if self.created_at:
            madrid_time = datetime_to_madrid(self.created_at)
            formatted_date = madrid_time.strftime("%d/%m/%Y")
        else:
            formatted_date = "Sin fecha"
            
        return f'<Incidencia {self.incident_type.value} - {formatted_date}>'
    
    def resolve(self, user_id, notes=None):
        """Marca la incidencia como resuelta"""
        self.resolved = True
        self.resolved_at = datetime.utcnow()
        self.resolved_by_id = user_id
        if notes:
            self.resolution_notes = notes


class CheckPointOriginalRecord(db.Model):
    """Registro original de fichajes antes de cualquier ajuste"""
    __tablename__ = 'checkpoint_original_records'
    
    id = db.Column(db.Integer, primary_key=True)
    # ID del registro original que se modificó
    record_id = db.Column(db.Integer, db.ForeignKey('checkpoint_records.id'), nullable=False)
    # Horas originales
    original_check_in_time = db.Column(db.DateTime, nullable=False)
    original_check_out_time = db.Column(db.DateTime)
    # Horas trabajadas calculadas en este fichaje
    hours_worked = db.Column(db.Float, default=0.0, nullable=False)
    # Firma original si existía
    original_signature_data = db.Column(db.Text)
    original_has_signature = db.Column(db.Boolean, default=False)
    # Notas originales 
    original_notes = db.Column(db.Text)
    # Metadatos del cambio
    adjusted_at = db.Column(db.DateTime, default=datetime.utcnow)
    adjusted_by_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    adjustment_reason = db.Column(db.String(256))
    # Metadata
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Relaciones
    record = db.relationship('CheckPointRecord', backref=db.backref('original_records', lazy=True))
    adjusted_by = db.relationship('User')
    
    # Método para calcular la duración con las horas originales
    def duration(self):
        """Calcula la duración del fichaje original en horas"""
        if not self.original_check_out_time:
            return None
            
        # Asegurarse de que ambas fechas tengan la misma información de zona horaria
        from timezone_config import datetime_to_madrid
        
        # Convertir ambas fechas a aware con la misma zona horaria
        check_in = datetime_to_madrid(self.original_check_in_time)
        check_out = datetime_to_madrid(self.original_check_out_time)
        
        # Si la hora de salida es anterior a la de entrada, podría ser un turno nocturno
        # En ese caso, asumimos que la salida corresponde al día siguiente
        if check_out < check_in:
            # Ajustar manualmente el día para calcular correctamente
            delta = (check_out - check_in).total_seconds() + (24 * 3600)  # Sumar 24 horas en segundos
            return delta / 3600  # Convertir a horas (formato decimal)
        
        delta = check_out - check_in
        return delta.total_seconds() / 3600  # Convertir segundos a horas (formato decimal)
    
    def __repr__(self):
        return f"<CheckPointOriginalRecord {self.id} - Record {self.record_id}>"
    
    def to_dict(self):
        """Convierte el registro a un diccionario para serialización"""
        # Importar la función de formateo para mostrar la duración correctamente
        from utils_checkpoints import format_hours_minutes
        
        # Calcular la duración
        duration_value = self.duration()
        formatted_duration = format_hours_minutes(duration_value) if duration_value is not None else None
        
        result = {
            'id': self.id,
            'record_id': self.record_id,
            'original_check_in_time': self.original_check_in_time.isoformat() if self.original_check_in_time else None,
            'original_check_out_time': self.original_check_out_time.isoformat() if self.original_check_out_time else None,
            'duration': formatted_duration,
            'duration_decimal': None if duration_value is None else round(duration_value, 2),
            'original_has_signature': self.original_has_signature,
            'original_notes': self.original_notes,
            'adjusted_at': self.adjusted_at.isoformat() if self.adjusted_at else None,
            'adjusted_by_id': self.adjusted_by_id,
            'adjustment_reason': self.adjustment_reason
        }
        return result


class EmployeeContractHours(db.Model):
    """Configuración de horas por contrato para cada empleado"""
    __tablename__ = 'employee_contract_hours'
    
    id = db.Column(db.Integer, primary_key=True)
    
    # Horas diarias máximas según contrato
    daily_hours = db.Column(db.Float, default=8.0)
    weekly_hours = db.Column(db.Float, default=40.0)
    
    # Configuración de márgenes
    allow_overtime = db.Column(db.Boolean, default=False)
    max_overtime_daily = db.Column(db.Float, default=2.0)  # Horas extra máximas diarias permitidas
    
    # Horario normal de trabajo (para cálculo de retrasos)
    use_normal_schedule = db.Column(db.Boolean, default=False)  # Activar horario normal
    normal_start_time = db.Column(db.Time)
    normal_end_time = db.Column(db.Time)
    
    # Margen de flexibilidad (en minutos)
    use_flexibility = db.Column(db.Boolean, default=False)  # Activar margen de flexibilidad
    checkin_flexibility = db.Column(db.Integer, default=15)  # Minutos de flexibilidad en la entrada
    checkout_flexibility = db.Column(db.Integer, default=15)  # Minutos de flexibilidad en la salida
    
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'), nullable=False, unique=True)
    employee = db.relationship('Employee', backref=db.backref('contract_hours', uselist=False))
    
    def __repr__(self):
        return f'<ContractHours {self.employee.first_name} - {self.daily_hours}h/día, {self.weekly_hours}h/semana>'
    
    def is_overtime(self, duration_hours):
        """Comprueba si una duración de horas supera el máximo diario"""
        return duration_hours > self.daily_hours
    
    def calculate_adjusted_hours(self, check_in_time, check_out_time):
        """Calcula el tiempo ajustado según el contrato y configuración"""
        if not check_out_time:
            return None, None
            
        # Asegurarse de que ambas fechas tengan la misma información de zona horaria
        from timezone_config import datetime_to_madrid, TIMEZONE
        
        # Convertir ambas fechas a aware con la misma zona horaria
        check_in_time = datetime_to_madrid(check_in_time)
        check_out_time = datetime_to_madrid(check_out_time)
            
        adjusted_in = check_in_time
        adjusted_out = check_out_time
        
        # 1. Verificar si se debe aplicar horario normal
        if self.use_normal_schedule and self.normal_start_time and self.normal_end_time:
            # Si la hora de entrada está fuera del horario normal (demasiado temprano), ajustar
            # Creamos un datetime aware con la fecha de check_in y la hora de normal_start
            check_in_date = check_in_time.date()
            normal_start_datetime = datetime.combine(check_in_date, self.normal_start_time)
            # Convertir a aware con zona horaria
            normal_start_datetime = TIMEZONE.localize(normal_start_datetime)
            
            # Permitimos margen de flexibilidad si está configurado
            if self.use_flexibility and self.checkin_flexibility:
                early_limit = normal_start_datetime - timedelta(minutes=self.checkin_flexibility)
                # Si la entrada es antes del límite de flexibilidad, ajustamos
                if check_in_time < early_limit:
                    adjusted_in = normal_start_datetime
            else:
                # Sin flexibilidad, ajustamos al horario normal exacto
                if check_in_time < normal_start_datetime:
                    adjusted_in = normal_start_datetime
            
            # Si la hora de salida es después del horario normal, ajustar (solo si no se permiten horas extra)
            if not self.allow_overtime and self.normal_end_time:
                check_out_date = check_out_time.date()
                normal_end_datetime = datetime.combine(check_out_date, self.normal_end_time)
                # Convertir a aware con zona horaria
                normal_end_datetime = TIMEZONE.localize(normal_end_datetime)
                
                # Considerar si la salida es al día siguiente
                if normal_end_datetime < normal_start_datetime:
                    next_day = check_out_time.date() + timedelta(days=1)
                    normal_end_datetime = datetime.combine(next_day, self.normal_end_time)
                    # Convertir a aware con zona horaria
                    normal_end_datetime = TIMEZONE.localize(normal_end_datetime)
                
                # Permitimos margen de flexibilidad si está configurado
                if self.use_flexibility and self.checkout_flexibility:
                    late_limit = normal_end_datetime + timedelta(minutes=self.checkout_flexibility)
                    # Si la salida es después del límite de flexibilidad, ajustamos
                    if check_out_time > late_limit:
                        adjusted_out = normal_end_datetime
                else:
                    # Sin flexibilidad, ajustamos al horario normal exacto
                    if check_out_time > normal_end_datetime:
                        adjusted_out = normal_end_datetime
        
        # 2. Calcular duración ajustada con los posibles cambios anteriores
        duration = (adjusted_out - adjusted_in).total_seconds() / 3600
        
        # 3. Verificar límite de horas diarias
        if duration <= self.daily_hours:
            return adjusted_in, adjusted_out
            
        # 4. Si excede las horas y no se permiten horas extra (o excede el límite de horas extra), ajustamos
        if not self.allow_overtime or (self.allow_overtime and duration > (self.daily_hours + self.max_overtime_daily)):
            # Determinamos cuál es el límite máximo
            max_hours = self.daily_hours
            if self.allow_overtime:
                max_hours = self.daily_hours + self.max_overtime_daily
                
            # Ajustamos la hora de salida para que la duración sea igual a las horas permitidas
            # En lugar de recortar la entrada, recortamos desde la salida
            
            # Añadir entre 1 y 5 minutos aleatorios para que la hora de salida parezca más natural
            random_minutes = random.randint(1, 5)
            
            # Calcular la nueva hora de salida con el tiempo exacto de horas máximas + minutos aleatorios
            new_check_out_time = adjusted_in + timedelta(hours=max_hours, minutes=random_minutes)
            
            return adjusted_in, new_check_out_time
            
        return adjusted_in, adjusted_out
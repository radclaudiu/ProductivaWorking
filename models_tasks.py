from app import db
from datetime import datetime, date, time, timedelta
import enum
from sqlalchemy import Enum
from werkzeug.security import generate_password_hash, check_password_hash
from models import User, Company
import socket

class TaskPriority(enum.Enum):
    BAJA = "baja"
    MEDIA = "media"
    ALTA = "alta"
    URGENTE = "urgente"

class TaskFrequency(enum.Enum):
    DIARIA = "diaria"
    SEMANAL = "semanal"
    QUINCENAL = "quincenal"
    MENSUAL = "mensual"
    PERSONALIZADA = "personalizada"

class TaskStatus(enum.Enum):
    PENDIENTE = "pendiente"
    COMPLETADA = "completada"
    VENCIDA = "vencida"
    CANCELADA = "cancelada"

class WeekDay(enum.Enum):
    LUNES = "lunes"
    MARTES = "martes"
    MIERCOLES = "miercoles"
    JUEVES = "jueves"
    VIERNES = "viernes"
    SABADO = "sabado"
    DOMINGO = "domingo"

class TaskGroup(db.Model):
    __tablename__ = 'task_groups'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(64), nullable=False)
    description = db.Column(db.Text)
    color = db.Column(db.String(7), default="#17a2b8")  # Color para identificar visualmente el grupo
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    location_id = db.Column(db.Integer, db.ForeignKey('locations.id'), nullable=False)
    location = db.relationship('Location', backref=db.backref('task_groups', lazy=True))
    
    # Tareas en este grupo
    tasks = db.relationship('Task', back_populates='group')
    
    def __repr__(self):
        return f'<TaskGroup {self.name}>'
    
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'description': self.description,
            'color': self.color,
            'location_id': self.location_id,
            'location_name': self.location.name if self.location else None
        }
    
class Location(db.Model):
    __tablename__ = 'locations'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(128), nullable=False)
    address = db.Column(db.String(256))
    city = db.Column(db.String(64))
    postal_code = db.Column(db.String(16))
    description = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    is_active = db.Column(db.Boolean, default=True)
    
    # Configuración del portal
    requires_pin = db.Column(db.Boolean, default=True, 
                             info={'description': 'Si se requiere PIN para empleados'})
    
    # Credenciales de acceso al portal
    portal_username = db.Column(db.String(64), unique=True)
    portal_password_hash = db.Column(db.String(256))
    
    # Relaciones
    company_id = db.Column(db.Integer, db.ForeignKey('companies.id'), nullable=False)
    company = db.relationship('Company', backref=db.backref('locations', lazy=True))
    
    # Relación con las tareas
    tasks = db.relationship('Task', back_populates='location', cascade='all, delete-orphan')
    
    # Relación con usuarios locales
    local_users = db.relationship('LocalUser', back_populates='location', cascade='all, delete-orphan')
    
    def __repr__(self):
        return f'<Location {self.name}>'
    
    def set_portal_password(self, password):
        """Establece una contraseña encriptada para el portal"""
        self.portal_password_hash = generate_password_hash(password)
        
    def check_portal_password(self, password):
        """Verifica si la contraseña proporcionada coincide con la almacenada"""
        # Si hay hash de contraseña, verificamos contra esa
        if self.portal_password_hash:
            return check_password_hash(self.portal_password_hash, password)
        # Si no, comparamos con la contraseña fija
        return password == self.portal_fixed_password
    
    @property
    def portal_fixed_username(self):
        """Retorna el nombre de usuario para este local"""
        # Si hay un usuario personalizado, lo devolvemos
        if self.portal_username:
            return self.portal_username
        # Si no, usamos el formato predeterminado
        return f"portal_{self.id}"
        
    def set_portal_username(self, username):
        """Establece un nombre de usuario personalizado para el portal"""
        # Verificar que el nombre de usuario no exista ya para otro local
        existing = Location.query.filter(Location.portal_username == username, Location.id != self.id).first()
        if existing:
            raise ValueError(f"El nombre de usuario '{username}' ya está en uso por otro local")
        self.portal_username = username
        
    @property
    def portal_fixed_password(self):
        """Retorna la contraseña para este local"""
        # Si hay contraseña personalizada (hash), significará que el usuario ha establecido una contraseña personalizada
        if self.portal_password_hash:
            # No podemos recuperar la contraseña real (solo tenemos el hash)
            # Usaremos una función específica para validar la contraseña en lugar de mostrarla
            return None  # No podemos mostrar la contraseña real
        # Si no hay contraseña personalizada, usamos el formato predeterminado
        return f"Portal{self.id}2025!"
    
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'address': self.address,
            'city': self.city,
            'postal_code': self.postal_code,
            'description': self.description,
            'company_id': self.company_id,
            'company_name': self.company.name if self.company else None,
            'is_active': self.is_active,
            'has_portal_credentials': True  # Siempre tiene credenciales fijas
        }

class LocalUser(db.Model):
    __tablename__ = 'local_users'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(64), nullable=False)
    last_name = db.Column(db.String(64), nullable=False)
    username = db.Column(db.String(128), nullable=False)  # Se generará automáticamente
    pin = db.Column(db.String(256), nullable=False)  # PIN de 4 dígitos (almacenado como hash)
    photo_path = db.Column(db.String(256))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    is_active = db.Column(db.Boolean, default=True)
    imported = db.Column(db.Boolean, default=False)  # Indica si el usuario fue importado de Employee
    
    # Relaciones
    location_id = db.Column(db.Integer, db.ForeignKey('locations.id'), nullable=False)
    location = db.relationship('Location', back_populates='local_users')
    
    # Relación con el empleado (si fue importado)
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'), nullable=True)
    employee = db.relationship('Employee', backref=db.backref('local_user', uselist=False))
    
    # Relación con las tareas completadas
    completed_tasks = db.relationship('TaskCompletion', back_populates='local_user')
    
    def __repr__(self):
        return f'<LocalUser {self.name} {self.last_name}>'
    
    def set_pin(self, pin):
        # Almacenamos el PIN como hash por seguridad
        self.pin = generate_password_hash(pin)
        
    def check_pin(self, pin):
        return check_password_hash(self.pin, pin)
    
    def get_full_name(self):
        return f"{self.name} {self.last_name}"
    
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'username': self.username,
            'photo_path': self.photo_path,
            'location_id': self.location_id,
            'location_name': self.location.name if self.location else None,
            'is_active': self.is_active,
            'imported': self.imported,
            'employee_id': self.employee_id,
            'employee_name': f"{self.employee.first_name} {self.employee.last_name}" if self.employee else None
        }

class Task(db.Model):
    __tablename__ = 'tasks'
    
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(128), nullable=False)
    description = db.Column(db.Text)
    priority = db.Column(Enum(TaskPriority), default=TaskPriority.MEDIA)
    frequency = db.Column(Enum(TaskFrequency), default=TaskFrequency.DIARIA)
    status = db.Column(Enum(TaskStatus), default=TaskStatus.PENDIENTE)
    start_date = db.Column(db.Date, nullable=False, default=date.today)
    end_date = db.Column(db.Date)  # Fecha final para tareas recurrentes, NULL si no caduca
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    location_id = db.Column(db.Integer, db.ForeignKey('locations.id'), nullable=False)
    location = db.relationship('Location', back_populates='tasks')
    created_by_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    created_by = db.relationship('User')
    
    # Grupo de tareas
    group_id = db.Column(db.Integer, db.ForeignKey('task_groups.id'), nullable=True)
    group = db.relationship('TaskGroup', back_populates='tasks')
    
    # Programación
    schedule_details = db.relationship('TaskSchedule', back_populates='task', cascade='all, delete-orphan')
    weekdays = db.relationship('TaskWeekday', back_populates='task', cascade='all, delete-orphan')
    
    # Historial de completado
    completions = db.relationship('TaskCompletion', back_populates='task', cascade='all, delete-orphan')
    
    def __repr__(self):
        return f'<Task {self.title}>'
    
    def to_dict(self):
        return {
            'id': self.id,
            'title': self.title,
            'description': self.description,
            'priority': self.priority.value if self.priority else None,
            'frequency': self.frequency.value if self.frequency else None,
            'status': self.status.value if self.status else None,
            'start_date': self.start_date.isoformat() if self.start_date else None,
            'end_date': self.end_date.isoformat() if self.end_date else None,
            'location_id': self.location_id,
            'location_name': self.location.name if self.location else None
        }
    
    def is_due_today(self):
        """Comprueba si la tarea está programada para hoy según su programación."""
        today = date.today()
        today_weekday = today.weekday()  # 0 es lunes, 6 es domingo
        
        # Si la tarea tiene fecha de fin y ya ha pasado, no está activa
        if self.end_date and today > self.end_date:
            return False
        
        # Si la tarea tiene fecha de inicio y aún no ha llegado, no está activa
        if self.start_date and today < self.start_date:
            return False
        
        # Para tareas personalizadas con múltiples días, verificamos los días configurados
        if self.frequency == TaskFrequency.PERSONALIZADA and self.weekdays:
            for weekday_entry in self.weekdays:
                if TaskWeekday.day_matches_today(weekday_entry.day_of_week):
                    return True
            # Si llegamos aquí, es que hoy no es uno de los días configurados
            return False
            
        # Si no hay programación específica (schedule_details está vacío),
        # consideramos que la tarea está activa según su frecuencia
        if not self.schedule_details:
            # Para tareas diarias, siempre están activas
            if self.frequency == TaskFrequency.DIARIA:
                return True
                
            # Para tareas semanales, verificamos si today es el mismo día de la semana que start_date
            elif self.frequency == TaskFrequency.SEMANAL and self.start_date:
                return today.weekday() == self.start_date.weekday()
                
            # Para tareas mensuales, verificamos si today es el mismo día del mes que start_date
            elif self.frequency == TaskFrequency.MENSUAL and self.start_date:
                return today.day == self.start_date.day
                
            # Para tareas quincenales, verificamos si han pasado múltiplos de 15 días desde start_date
            elif self.frequency == TaskFrequency.QUINCENAL and self.start_date:
                delta = (today - self.start_date).days
                return delta % 15 == 0
            
            # Para cualquier otro caso, mostramos la tarea
            return True
        
        # Comprobamos la programación específica
        for schedule in self.schedule_details:
            if schedule.is_active_for_date(today):
                return True
                
        return False

class TaskSchedule(db.Model):
    __tablename__ = 'task_schedules'
    
    id = db.Column(db.Integer, primary_key=True)
    day_of_week = db.Column(Enum(WeekDay), nullable=True)  # Día de la semana para tareas semanales
    day_of_month = db.Column(db.Integer, nullable=True)    # Día del mes para tareas mensuales
    start_time = db.Column(db.Time, nullable=True)         # Hora de inicio
    end_time = db.Column(db.Time, nullable=True)           # Hora de finalización
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    task_id = db.Column(db.Integer, db.ForeignKey('tasks.id'), nullable=False)
    task = db.relationship('Task', back_populates='schedule_details')
    
    def __repr__(self):
        if self.day_of_week:
            return f'<TaskSchedule {self.task.title} - {self.day_of_week.value}>'
        elif self.day_of_month:
            return f'<TaskSchedule {self.task.title} - Day {self.day_of_month}>'
        else:
            return f'<TaskSchedule {self.task.title}>'
    
    def is_active_for_date(self, check_date):
        """Comprueba si este horario está activo para una fecha determinada."""
        # Si es una tarea diaria, siempre está activa
        if self.task.frequency == TaskFrequency.DIARIA:
            return True
            
        # Para tareas semanales, comprobamos el día de la semana
        if self.task.frequency == TaskFrequency.SEMANAL and self.day_of_week:
            day_map = {
                WeekDay.LUNES: 0,
                WeekDay.MARTES: 1,
                WeekDay.MIERCOLES: 2,
                WeekDay.JUEVES: 3,
                WeekDay.VIERNES: 4,
                WeekDay.SABADO: 5,
                WeekDay.DOMINGO: 6
            }
            return check_date.weekday() == day_map[self.day_of_week]
            
        # Para tareas mensuales, comprobamos el día del mes
        if self.task.frequency == TaskFrequency.MENSUAL and self.day_of_month:
            return check_date.day == self.day_of_month
            
        # Para tareas quincenales (cada 15 días)
        if self.task.frequency == TaskFrequency.QUINCENAL:
            if not self.task.start_date:
                return False
                
            delta = (check_date - self.task.start_date).days
            return delta % 15 == 0
            
        # Si llegamos aquí y no hemos retornado, no está activa
        return False
        
class TaskWeekday(db.Model):
    """Modelo para almacenar los días de la semana en los que una tarea debe ejecutarse"""
    __tablename__ = 'task_weekdays'
    
    id = db.Column(db.Integer, primary_key=True)
    day_of_week = db.Column(Enum(WeekDay), nullable=False)
    
    # Relaciones
    task_id = db.Column(db.Integer, db.ForeignKey('tasks.id'), nullable=False)
    task = db.relationship('Task', back_populates='weekdays')
    
    def __repr__(self):
        return f'<TaskWeekday {self.task.title} - {self.day_of_week.value}>'
        
    @classmethod
    def day_matches_today(cls, weekday):
        """Comprueba si el día de la semana corresponde al día actual"""
        day_map = {
            WeekDay.LUNES: 0,
            WeekDay.MARTES: 1,
            WeekDay.MIERCOLES: 2,
            WeekDay.JUEVES: 3,
            WeekDay.VIERNES: 4,
            WeekDay.SABADO: 5,
            WeekDay.DOMINGO: 6
        }
        return date.today().weekday() == day_map[weekday]

class TaskInstance(db.Model):
    """Instancia de tarea programada para una fecha específica."""
    __tablename__ = 'task_instances'
    
    id = db.Column(db.Integer, primary_key=True)
    scheduled_date = db.Column(db.Date, nullable=False)
    status = db.Column(Enum(TaskStatus), default=TaskStatus.PENDIENTE)
    notes = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    task_id = db.Column(db.Integer, db.ForeignKey('tasks.id'), nullable=False)
    task = db.relationship('Task')
    completed_by_id = db.Column(db.Integer, db.ForeignKey('local_users.id'), nullable=True)
    completed_by = db.relationship('LocalUser')
    
    def __repr__(self):
        return f'<TaskInstance {self.task.title} on {self.scheduled_date}>'
    
    def to_dict(self):
        return {
            'id': self.id,
            'task_id': self.task_id,
            'task_title': self.task.title if self.task else None,
            'scheduled_date': self.scheduled_date.isoformat() if self.scheduled_date else None,
            'status': self.status.value if self.status else None,
            'notes': self.notes,
            'completed_by': self.completed_by.name if self.completed_by else None
        }

class TaskCompletion(db.Model):
    __tablename__ = 'task_completions'
    
    id = db.Column(db.Integer, primary_key=True)
    completion_date = db.Column(db.DateTime, default=datetime.utcnow)
    notes = db.Column(db.Text)
    
    # Relaciones
    task_id = db.Column(db.Integer, db.ForeignKey('tasks.id'), nullable=False)
    task = db.relationship('Task', back_populates='completions')
    local_user_id = db.Column(db.Integer, db.ForeignKey('local_users.id'), nullable=False)
    local_user = db.relationship('LocalUser', back_populates='completed_tasks')
    
    def __repr__(self):
        return f'<TaskCompletion {self.task.title} by {self.local_user.name}>'
    
    def to_dict(self):
        return {
            'id': self.id,
            'task_id': self.task_id,
            'task_title': self.task.title if self.task else None,
            'local_user_id': self.local_user_id,
            'local_user_name': self.local_user.name if self.local_user else None,
            'completion_date': self.completion_date.isoformat() if self.completion_date else None,
            'notes': self.notes
        }

# Modelos para el sistema de etiquetas

class ConservationType(enum.Enum):
    DESCONGELACION = "descongelacion"
    REFRIGERACION = "refrigeracion"
    GASTRO = "gastro"
    CALIENTE = "caliente"
    SECO = "seco"

class Product(db.Model):
    """Modelo para productos alimenticios que pueden ser etiquetados"""
    __tablename__ = 'products'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(128), nullable=False)
    description = db.Column(db.Text)
    shelf_life_days = db.Column(db.Integer, default=0, nullable=False)  # Vida útil en días (0 = no aplicable)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    is_active = db.Column(db.Boolean, default=True)
    
    # Relaciones
    location_id = db.Column(db.Integer, db.ForeignKey('locations.id'), nullable=False)
    location = db.relationship('Location', backref=db.backref('products', lazy=True))
    
    # Relación con los tipos de conservación
    conservation_types = db.relationship('ProductConservation', back_populates='product', cascade='all, delete-orphan')
    
    # Historial de etiquetas generadas
    labels = db.relationship('ProductLabel', back_populates='product', cascade='all, delete-orphan')
    
    def __repr__(self):
        return f'<Product {self.name}>'
    
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'description': self.description,
            'shelf_life_days': self.shelf_life_days,
            'location_id': self.location_id,
            'location_name': self.location.name if self.location else None,
            'is_active': self.is_active
        }
        
    def get_shelf_life_expiry(self, from_date=None):
        """Calcula la fecha de caducidad secundaria basada en la vida útil en días"""
        if self.shelf_life_days <= 0:
            return None
            
        if from_date is None:
            from_date = datetime.now()
            
        # Asegurar que trabajamos con un datetime
        if isinstance(from_date, date) and not isinstance(from_date, datetime):
            from_date = datetime.combine(from_date, datetime.min.time())
            
        # Calcular fecha de caducidad secundaria (solo fecha, sin hora)
        return (from_date + timedelta(days=self.shelf_life_days)).date()

class ProductConservation(db.Model):
    """Modelo para definir los tiempos de conservación de un producto según el tipo"""
    __tablename__ = 'product_conservations'
    
    id = db.Column(db.Integer, primary_key=True)
    conservation_type = db.Column(Enum(ConservationType), nullable=False)
    hours_valid = db.Column(db.Integer, nullable=False, default=24)  # Horas que dura el producto en este tipo de conservación
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relaciones
    product_id = db.Column(db.Integer, db.ForeignKey('products.id'), nullable=False)
    product = db.relationship('Product', back_populates='conservation_types')
    
    def __repr__(self):
        return f'<ProductConservation {self.product.name} - {self.conservation_type.value}>'
    
    def to_dict(self):
        return {
            'id': self.id,
            'product_id': self.product_id,
            'product_name': self.product.name if self.product else None,
            'conservation_type': self.conservation_type.value,
            'hours_valid': self.hours_valid
        }
    
    def get_expiry_date(self, from_date=None):
        """Calcula la fecha de caducidad basada en horas"""
        expiry_datetime = self.get_expiry_datetime(from_date)
        return expiry_datetime.date()
        
    def get_expiry_datetime(self, from_date=None):
        """Calcula el datetime exacto de caducidad, incluyendo la hora"""
        if from_date is None:
            from_date = datetime.now()
            
        # Asegurar que trabajamos con un datetime
        if isinstance(from_date, date) and not isinstance(from_date, datetime):
            from_date = datetime.combine(from_date, datetime.min.time())
            
        # Retornar el datetime completo con hora exacta
        return from_date + timedelta(hours=self.hours_valid)
        
class PrinterType(enum.Enum):
    DIRECT_NETWORK = "direct_network"     # Conexión directa a impresora en red (Brother, etc)
    RASPBERRY_PI = "raspberry_pi"        # Conexión a Raspberry Pi que controla la impresora

class NetworkPrinter(db.Model):
    """Modelo para almacenar las impresoras de red para imprimir etiquetas"""
    __tablename__ = 'network_printers'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), nullable=False)
    ip_address = db.Column(db.String(50), nullable=False)
    model = db.Column(db.String(100))
    api_path = db.Column(db.String(255), default='/print')
    port = db.Column(db.Integer, default=5000, nullable=True)  # Cambiado a 5000 por defecto para Raspberry Pi
    printer_type = db.Column(Enum(PrinterType), default=PrinterType.DIRECT_NETWORK)
    usb_port = db.Column(db.String(100))  # Puerto USB para Raspberry Pi (e.g., /dev/usb/lp0)
    requires_auth = db.Column(db.Boolean, default=False)
    username = db.Column(db.String(100))
    password = db.Column(db.String(100))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    is_default = db.Column(db.Boolean, default=False)
    is_active = db.Column(db.Boolean, default=True)
    last_status = db.Column(db.String(50))
    last_status_check = db.Column(db.DateTime)
    
    # Relaciones
    location_id = db.Column(db.Integer, db.ForeignKey('locations.id'))
    location = db.relationship('Location', backref=db.backref('printers', lazy=True))
    
    def __repr__(self):
        return f'<NetworkPrinter {self.name} - {self.ip_address}>'
    
    def get_full_url(self):
        """Retorna la URL completa de la API de la impresora"""
        # Determinar el puerto por defecto según el tipo de impresora
        default_port = 5000 if self.printer_type == PrinterType.RASPBERRY_PI else 80
        
        # Si hay un puerto especificado, usarlo; de lo contrario, usar el predeterminado
        port_to_use = self.port if self.port else default_port
        
        # Para impresoras Raspberry Pi, usar el endpoint específico para imprimir etiquetas
        if self.printer_type == PrinterType.RASPBERRY_PI:
            # El endpoint predeterminado para Raspberry Pi es /print
            path = self.api_path if self.api_path else '/print'
            return f"http://{self.ip_address}:{port_to_use}{path}"
        else:  # Para impresoras de red directas (Brother, etc.)
            # El endpoint predeterminado para Brother es /brother_d/printer/print
            path = self.api_path if self.api_path else '/brother_d/printer/print'
            return f"http://{self.ip_address}:{port_to_use}{path}"
    
    def check_status(self):
        """Verifica si la impresora está en línea"""
        try:
            # Intenta conectarse al puerto de la impresora
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.settimeout(2.0)  # Timeout de 2 segundos
            
            # Si no hay puerto especificado, usar el puerto predeterminado según el tipo
            if self.printer_type == PrinterType.RASPBERRY_PI:
                port_to_check = self.port if self.port else 5000  # Puerto predeterminado para Flask en Raspberry Pi
            else:  # DIRECT_NETWORK
                port_to_check = self.port if self.port else 80    # Puerto predeterminado para impresoras Brother
            
            result = s.connect_ex((self.ip_address, port_to_check))
            s.close()
            
            if result == 0:
                # Para impresoras Raspberry Pi, verificar también el endpoint específico
                if self.printer_type == PrinterType.RASPBERRY_PI:
                    import requests
                    try:
                        # Verificar que el servicio está respondiendo correctamente
                        check_url = f"http://{self.ip_address}:{port_to_check}/status"
                        response = requests.get(check_url, timeout=3.0)
                        if response.status_code == 200:
                            status_data = response.json()
                            if status_data.get('success'):
                                self.last_status = "online: " + status_data.get('printer_status', 'ready')
                                self.last_status_check = datetime.utcnow()
                                return True
                    except Exception as inner_e:
                        # La conexión TCP funciona pero el servicio no responde correctamente
                        self.last_status = f"error in service: {str(inner_e)}"
                        self.last_status_check = datetime.utcnow()
                        return False
                
                # Para impresoras de red directas, basta con la conexión TCP
                self.last_status = "online"
                self.last_status_check = datetime.utcnow()
                return True
            else:
                self.last_status = "offline"
                self.last_status_check = datetime.utcnow()
                return False
        except Exception as e:
            self.last_status = f"error: {str(e)}"
            self.last_status_check = datetime.utcnow()
            return False
    
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'ip_address': self.ip_address,
            'model': self.model,
            'api_path': self.api_path,
            'port': self.port,
            'printer_type': self.printer_type.value if self.printer_type else PrinterType.DIRECT_NETWORK.value,
            'usb_port': self.usb_port,
            'requires_auth': self.requires_auth,
            'username': self.username if self.requires_auth else None,
            'is_default': self.is_default,
            'is_active': self.is_active,
            'last_status': self.last_status,
            'last_status_check': self.last_status_check.isoformat() if self.last_status_check else None,
            'location_id': self.location_id,
            'location_name': self.location.name if self.location else None
        }


class LabelTemplate(db.Model):
    """Modelo para almacenar las plantillas de etiquetas personalizadas"""
    __tablename__ = 'label_templates'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(64), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    is_default = db.Column(db.Boolean, default=False)
    
    # Posiciones para el título
    titulo_x = db.Column(db.Integer, default=50)
    titulo_y = db.Column(db.Integer, default=10)
    titulo_size = db.Column(db.Integer, default=11)
    titulo_bold = db.Column(db.Boolean, default=True)
    
    # Posiciones para el tipo de conservación
    conservacion_x = db.Column(db.Integer, default=50)
    conservacion_y = db.Column(db.Integer, default=25)
    conservacion_size = db.Column(db.Integer, default=9)
    conservacion_bold = db.Column(db.Boolean, default=True)
    
    # Posiciones para el preparador
    preparador_x = db.Column(db.Integer, default=50)
    preparador_y = db.Column(db.Integer, default=40)
    preparador_size = db.Column(db.Integer, default=7)
    preparador_bold = db.Column(db.Boolean, default=False)
    
    # Posiciones para la fecha
    fecha_x = db.Column(db.Integer, default=50)
    fecha_y = db.Column(db.Integer, default=50)
    fecha_size = db.Column(db.Integer, default=7)
    fecha_bold = db.Column(db.Boolean, default=False)
    
    # Posiciones para la caducidad
    caducidad_x = db.Column(db.Integer, default=50)
    caducidad_y = db.Column(db.Integer, default=65)
    caducidad_size = db.Column(db.Integer, default=9)
    caducidad_bold = db.Column(db.Boolean, default=True)
    
    # Posiciones para la caducidad secundaria
    caducidad2_x = db.Column(db.Integer, default=50)
    caducidad2_y = db.Column(db.Integer, default=80)
    caducidad2_size = db.Column(db.Integer, default=8)
    caducidad2_bold = db.Column(db.Boolean, default=False)
    
    location_id = db.Column(db.Integer, db.ForeignKey('locations.id'), nullable=False)
    location = db.relationship('Location', backref=db.backref('label_templates', lazy=True))
    
    def __repr__(self):
        return f'<LabelTemplate {self.id} - {self.name}>'
    
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None,
            'is_default': self.is_default,
            'titulo_x': self.titulo_x,
            'titulo_y': self.titulo_y,
            'titulo_size': self.titulo_size,
            'titulo_bold': self.titulo_bold,
            'conservacion_x': self.conservacion_x,
            'conservacion_y': self.conservacion_y,
            'conservacion_size': self.conservacion_size,
            'conservacion_bold': self.conservacion_bold,
            'preparador_x': self.preparador_x,
            'preparador_y': self.preparador_y,
            'preparador_size': self.preparador_size,
            'preparador_bold': self.preparador_bold,
            'fecha_x': self.fecha_x,
            'fecha_y': self.fecha_y,
            'fecha_size': self.fecha_size,
            'fecha_bold': self.fecha_bold,
            'caducidad_x': self.caducidad_x,
            'caducidad_y': self.caducidad_y,
            'caducidad_size': self.caducidad_size,
            'caducidad_bold': self.caducidad_bold,
            'caducidad2_x': self.caducidad2_x,
            'caducidad2_y': self.caducidad2_y,
            'caducidad2_size': self.caducidad2_size,
            'caducidad2_bold': self.caducidad2_bold,
            'location_id': self.location_id
        }

class ProductLabel(db.Model):
    """Modelo para registrar las etiquetas generadas"""
    __tablename__ = 'product_labels'
    
    id = db.Column(db.Integer, primary_key=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    expiry_date = db.Column(db.Date, nullable=False)
    
    # Relaciones
    product_id = db.Column(db.Integer, db.ForeignKey('products.id'), nullable=False)
    product = db.relationship('Product', back_populates='labels')
    local_user_id = db.Column(db.Integer, db.ForeignKey('local_users.id'), nullable=False)
    local_user = db.relationship('LocalUser', backref=db.backref('generated_labels', lazy=True))
    conservation_type = db.Column(Enum(ConservationType), nullable=False)
    
    def __repr__(self):
        return f'<ProductLabel {self.product.name} - {self.conservation_type.value} - {self.expiry_date}>'
    
    def to_dict(self):
        return {
            'id': self.id,
            'product_id': self.product_id,
            'product_name': self.product.name if self.product else None,
            'local_user_id': self.local_user_id,
            'local_user_name': self.local_user.name if self.local_user else None,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'expiry_date': self.expiry_date.isoformat() if self.expiry_date else None,
            'conservation_type': self.conservation_type.value
        }
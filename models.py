from datetime import datetime, time, timedelta
import enum
import random
from flask_login import UserMixin
from sqlalchemy import Enum
from werkzeug.security import generate_password_hash, check_password_hash

from app import db, login_manager

class UserRole(enum.Enum):
    ADMIN = "admin"
    GERENTE = "gerente"
    EMPLEADO = "empleado"

class User(UserMixin, db.Model):
    __tablename__ = 'users'
    
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(64), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(256), nullable=False)
    role = db.Column(Enum(UserRole), default=UserRole.EMPLEADO, nullable=False)
    first_name = db.Column(db.String(64))
    last_name = db.Column(db.String(64))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    is_active = db.Column(db.Boolean, default=True)
    
    # Relationships
    company_id = db.Column(db.Integer, db.ForeignKey('companies.id'))
    company = db.relationship('Company', back_populates='users')
    employee = db.relationship('Employee', back_populates='user', uselist=False)
    activity_logs = db.relationship('ActivityLog', back_populates='user', cascade='all, delete-orphan')
    
    def set_password(self, password):
        self.password_hash = generate_password_hash(password)
        
    def check_password(self, password):
        return check_password_hash(self.password_hash, password)
    
    def is_admin(self):
        return self.role == UserRole.ADMIN
        
    def is_gerente(self):
        return self.role == UserRole.GERENTE
        
    def is_empleado(self):
        return self.role == UserRole.EMPLEADO
    
    def __repr__(self):
        return f'<User {self.username}>'
        
    def to_dict(self):
        return {
            'id': self.id,
            'username': self.username,
            'email': self.email,
            'role': self.role.value,
            'full_name': f"{self.first_name} {self.last_name}",
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'company_id': self.company_id
        }
        
@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))

class Company(db.Model):
    __tablename__ = 'companies'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(128), nullable=False)
    address = db.Column(db.String(256))
    city = db.Column(db.String(64))
    postal_code = db.Column(db.String(16))
    country = db.Column(db.String(64))
    sector = db.Column(db.String(64))
    tax_id = db.Column(db.String(32), unique=True)
    phone = db.Column(db.String(32))
    email = db.Column(db.String(120))
    website = db.Column(db.String(128))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    is_active = db.Column(db.Boolean, default=True)
    
    # Relationships
    employees = db.relationship('Employee', back_populates='company', cascade='all, delete-orphan')
    users = db.relationship('User', back_populates='company')
    
    def __repr__(self):
        return f'<Company {self.name}>'
        
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'address': self.address,
            'city': self.city,
            'postal_code': self.postal_code,
            'country': self.country,
            'sector': self.sector,
            'tax_id': self.tax_id,
            'phone': self.phone,
            'email': self.email,
            'website': self.website,
            'is_active': self.is_active,
            'employee_count': len(self.employees)
        }

class ContractType(enum.Enum):
    INDEFINIDO = "INDEFINIDO"
    TEMPORAL = "TEMPORAL"
    PRACTICAS = "PRACTICAS"
    FORMACION = "FORMACION"
    OBRA = "OBRA"

class EmployeeStatus(enum.Enum):
    ACTIVO = "activo"
    BAJA_MEDICA = "baja_medica"
    EXCEDENCIA = "excedencia"
    VACACIONES = "vacaciones"
    INACTIVO = "inactivo"

class WeekDay(enum.Enum):
    LUNES = "lunes"
    MARTES = "martes"
    MIERCOLES = "miercoles"
    JUEVES = "jueves"
    VIERNES = "viernes"
    SABADO = "sabado"
    DOMINGO = "domingo"

class VacationStatus(enum.Enum):
    REGISTRADA = "REGISTRADA"
    DISFRUTADA = "DISFRUTADA"

class Employee(db.Model):
    __tablename__ = 'employees'
    
    id = db.Column(db.Integer, primary_key=True)
    first_name = db.Column(db.String(64), nullable=False)
    last_name = db.Column(db.String(64), nullable=False)
    dni = db.Column(db.String(16), unique=True, nullable=False)
    # Nuevos campos adicionales
    email = db.Column(db.String(120))
    social_security_number = db.Column(db.String(32))
    phone = db.Column(db.String(32))
    address = db.Column(db.String(256))
    contract_hours = db.Column(db.String(64))  # Horario en contrato
    # Campos existentes
    position = db.Column(db.String(64))
    contract_type = db.Column(Enum(ContractType), default=ContractType.INDEFINIDO)
    bank_account = db.Column(db.String(64))
    start_date = db.Column(db.Date)
    end_date = db.Column(db.Date)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    is_active = db.Column(db.Boolean, default=True)
    status = db.Column(Enum(EmployeeStatus), default=EmployeeStatus.ACTIVO)
    status_start_date = db.Column(db.Date)
    status_end_date = db.Column(db.Date)
    status_notes = db.Column(db.Text)
    
    # Relationships
    company_id = db.Column(db.Integer, db.ForeignKey('companies.id'), nullable=False)
    company = db.relationship('Company', back_populates='employees')
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), unique=True)
    user = db.relationship('User', back_populates='employee')
    documents = db.relationship('EmployeeDocument', back_populates='employee', cascade='all, delete-orphan')
    notes = db.relationship('EmployeeNote', back_populates='employee', cascade='all, delete-orphan')
    history = db.relationship('EmployeeHistory', back_populates='employee', cascade='all, delete-orphan')
    schedules = db.relationship('EmployeeSchedule', back_populates='employee', cascade='all, delete-orphan')
    check_ins = db.relationship('EmployeeCheckIn', back_populates='employee', cascade='all, delete-orphan')
    vacations = db.relationship('EmployeeVacation', back_populates='employee', cascade='all, delete-orphan')
    
    def __repr__(self):
        return f'<Employee {self.first_name} {self.last_name}>'
        
    def to_dict(self):
        return {
            'id': self.id,
            'first_name': self.first_name,
            'last_name': self.last_name,
            'full_name': f"{self.first_name} {self.last_name}",
            'dni': self.dni,
            # Nuevos campos en el diccionario
            'email': self.email,
            'social_security_number': self.social_security_number,
            'phone': self.phone,
            'address': self.address,
            'contract_hours': self.contract_hours,
            # Campos anteriores
            'position': self.position,
            'contract_type': self.contract_type.value if self.contract_type else None,
            'bank_account': self.bank_account,
            'start_date': self.start_date.isoformat() if self.start_date else None,
            'end_date': self.end_date.isoformat() if self.end_date else None,
            'is_active': self.is_active,
            'status': self.status.value if self.status else 'activo',
            'status_start_date': self.status_start_date.isoformat() if self.status_start_date else None,
            'status_end_date': self.status_end_date.isoformat() if self.status_end_date else None,
            'company_id': self.company_id,
            'company_name': self.company.name if self.company else None
        }

class EmployeeDocument(db.Model):
    __tablename__ = 'employee_documents'
    
    id = db.Column(db.Integer, primary_key=True)
    filename = db.Column(db.String(256), nullable=False)
    original_filename = db.Column(db.String(256), nullable=False)
    file_path = db.Column(db.String(512), nullable=False)
    file_type = db.Column(db.String(64))
    file_size = db.Column(db.Integer)  # Size in bytes
    description = db.Column(db.String(256))
    uploaded_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Relationships
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'), nullable=False)
    employee = db.relationship('Employee', back_populates='documents')
    
    def __repr__(self):
        return f'<EmployeeDocument {self.original_filename}>'

class EmployeeNote(db.Model):
    __tablename__ = 'employee_notes'
    
    id = db.Column(db.Integer, primary_key=True)
    content = db.Column(db.Text, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'), nullable=False)
    employee = db.relationship('Employee', back_populates='notes')
    created_by_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    created_by = db.relationship('User')
    
    def __repr__(self):
        return f'<EmployeeNote {self.id}>'

class EmployeeHistory(db.Model):
    __tablename__ = 'employee_history'
    
    id = db.Column(db.Integer, primary_key=True)
    field_name = db.Column(db.String(64), nullable=False)
    old_value = db.Column(db.String(256))
    new_value = db.Column(db.String(256))
    changed_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Relationships
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'), nullable=False)
    employee = db.relationship('Employee', back_populates='history')
    changed_by_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    changed_by = db.relationship('User')
    
    def __repr__(self):
        return f'<EmployeeHistory {self.field_name}>'

class EmployeeSchedule(db.Model):
    __tablename__ = 'employee_schedules'
    
    id = db.Column(db.Integer, primary_key=True)
    day_of_week = db.Column(Enum(WeekDay), nullable=False)
    start_time = db.Column(db.Time, nullable=False)
    end_time = db.Column(db.Time, nullable=False)
    is_working_day = db.Column(db.Boolean, default=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'), nullable=False)
    employee = db.relationship('Employee', back_populates='schedules')
    
    def __repr__(self):
        return f'<EmployeeSchedule {self.employee.last_name} - {self.day_of_week.value}>'
    
    def to_dict(self):
        return {
            'id': self.id,
            'day_of_week': self.day_of_week.value,
            'start_time': self.start_time.strftime('%H:%M') if self.start_time else None,
            'end_time': self.end_time.strftime('%H:%M') if self.end_time else None,
            'is_working_day': self.is_working_day,
            'employee_id': self.employee_id
        }

class EmployeeCheckIn(db.Model):
    __tablename__ = 'employee_check_ins'
    
    id = db.Column(db.Integer, primary_key=True)
    check_in_time = db.Column(db.DateTime, nullable=False)
    check_out_time = db.Column(db.DateTime)
    is_generated = db.Column(db.Boolean, default=False)  # True if automatically generated
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    notes = db.Column(db.Text)
    
    # Relationships
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'), nullable=False)
    employee = db.relationship('Employee', back_populates='check_ins')
    
    def __repr__(self):
        return f'<EmployeeCheckIn {self.employee.last_name} - {self.check_in_time}>'
    
    def to_dict(self):
        return {
            'id': self.id,
            'check_in_time': self.check_in_time.strftime('%Y-%m-%d %H:%M:%S'),
            'check_out_time': self.check_out_time.strftime('%Y-%m-%d %H:%M:%S') if self.check_out_time else None,
            'is_generated': self.is_generated,
            'notes': self.notes,
            'employee_id': self.employee_id,
            'employee_name': f"{self.employee.first_name} {self.employee.last_name}" if self.employee else None
        }
    
    @staticmethod
    def generate_realistic_time(base_time, minutes_range=4):
        """Generate a realistic check-in or check-out time with random variation."""
        # For check-in: ensure it's 1-4 minutes before scheduled time
        # For check-out: ensure it's 1-4 minutes after scheduled time
        
        if 'check_in' in str(base_time):
            # Llegada anticipada: entre 1-4 minutos antes (siempre por debajo de la hora programada)
            minutes_variation = random.randint(1, minutes_range)
            seconds_variation = random.randint(0, 59)
            return base_time - timedelta(minutes=minutes_variation, seconds=seconds_variation)
        else:
            # Salida posterior: entre 1-4 minutos después
            minutes_variation = random.randint(1, minutes_range)
            seconds_variation = random.randint(0, 59)
            return base_time + timedelta(minutes=minutes_variation, seconds=seconds_variation)
            
    @classmethod
    def generate_check_ins_for_schedule(cls, employee, start_date, end_date):
        """Generate check-ins for an employee based on their schedule."""
        
        if not employee.schedules:
            return []
            
        # Create a map of weekday to schedule
        weekday_map = {
            0: WeekDay.LUNES,
            1: WeekDay.MARTES,
            2: WeekDay.MIERCOLES,
            3: WeekDay.JUEVES,
            4: WeekDay.VIERNES,
            5: WeekDay.SABADO,
            6: WeekDay.DOMINGO
        }
        
        # Map employee schedules by day of week
        schedule_map = {}
        for schedule in employee.schedules:
            for i, day in enumerate(weekday_map.values()):
                if schedule.day_of_week == day:
                    schedule_map[i] = schedule
                    
        # Check if employee is on vacation for any days in range
        vacation_days = set()
        for vacation in employee.vacations:
            if vacation.status == VacationStatus.REGISTRADA or vacation.status == VacationStatus.DISFRUTADA:
                current_day = vacation.start_date
                while current_day <= vacation.end_date:
                    vacation_days.add(current_day)
                    current_day += timedelta(days=1)
        
        # Generate check-ins for each day
        generated_check_ins = []
        current_date = start_date
        while current_date <= end_date:
            # Skip if employee is on vacation
            if current_date in vacation_days:
                current_date += timedelta(days=1)
                continue
                
            # Get weekday index (0=Monday, 6=Sunday)
            weekday = current_date.weekday()
            
            # Skip if no schedule for this day
            if weekday not in schedule_map:
                current_date += timedelta(days=1)
                continue
                
            schedule = schedule_map[weekday]
            
            # Skip if not a working day
            if not schedule.is_working_day:
                current_date += timedelta(days=1)
                continue
                
            # Create base check-in and check-out times
            check_in_base = datetime.combine(current_date, schedule.start_time)
            check_out_base = datetime.combine(current_date, schedule.end_time)
            
            # Generate realistic times
            check_in_time = cls.generate_realistic_time(check_in_base, 4)
            check_out_time = cls.generate_realistic_time(check_out_base, 4)
            
            # Create check-in record
            check_in = cls(
                employee_id=employee.id,
                check_in_time=check_in_time,
                check_out_time=check_out_time,
                is_generated=True,
                notes="Generado automáticamente"
            )
            
            generated_check_ins.append(check_in)
            current_date += timedelta(days=1)
            
        return generated_check_ins

class EmployeeVacation(db.Model):
    __tablename__ = 'employee_vacations'
    
    id = db.Column(db.Integer, primary_key=True)
    start_date = db.Column(db.Date, nullable=False)
    end_date = db.Column(db.Date, nullable=False)
    status = db.Column(Enum(VacationStatus), default=VacationStatus.REGISTRADA)
    is_signed = db.Column(db.Boolean, default=False)
    is_enjoyed = db.Column(db.Boolean, default=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    notes = db.Column(db.Text)
    
    # Relationships
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'), nullable=False)
    employee = db.relationship('Employee', back_populates='vacations')
    # Removed approved_by references as part of vacation workflow simplification
    
    def __repr__(self):
        return f'<EmployeeVacation {self.employee.last_name} - {self.start_date} to {self.end_date}>'
    
    def to_dict(self):
        return {
            'id': self.id,
            'start_date': self.start_date.isoformat(),
            'end_date': self.end_date.isoformat(),
            'status': self.status.value,
            'is_signed': self.is_signed,
            'is_enjoyed': self.is_enjoyed,
            'notes': self.notes,
            'employee_id': self.employee_id,
            'employee_name': f"{self.employee.first_name} {self.employee.last_name}" if self.employee else None,
            'duration_days': (self.end_date - self.start_date).days + 1 if self.start_date and self.end_date else 0
        }
    
    def total_days(self):
        """Calculate the total number of days in this vacation period."""
        if not self.start_date or not self.end_date:
            return 0
        return (self.end_date - self.start_date).days + 1
    
    def mark_as_signed(self):
        """Mark the vacation as signed by the employee."""
        self.is_signed = True
        return self
        
    def mark_as_enjoyed(self):
        """Mark the vacation as enjoyed after the date has passed."""
        self.is_enjoyed = True
        return self
        
    def overlaps_with(self, start_date, end_date):
        """Check if this vacation period overlaps with the given dates."""
        return (self.start_date <= end_date and self.end_date >= start_date)

class ActivityLog(db.Model):
    __tablename__ = 'activity_logs'
    
    id = db.Column(db.Integer, primary_key=True)
    action = db.Column(db.String(256), nullable=False)
    ip_address = db.Column(db.String(64))
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Relationships
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    user = db.relationship('User', back_populates='activity_logs')
    
    def __repr__(self):
        return f'<ActivityLog {self.action}>'

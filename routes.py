import os
import sqlalchemy
from datetime import datetime, time
from functools import wraps

from flask import Blueprint, render_template, redirect, url_for, flash, request, abort, jsonify, send_from_directory, send_file
from flask_login import login_user, logout_user, login_required, current_user
from urllib.parse import urlparse
from werkzeug.utils import secure_filename
from sqlalchemy import func, text

from app import db
from models import (User, Company, Employee, EmployeeDocument, EmployeeNote, UserRole, 
                   ContractType, EmployeeStatus, EmployeeSchedule, EmployeeCheckIn, 
                   EmployeeVacation, VacationStatus, WeekDay)
from models_checkpoints import CheckPoint
from forms import (LoginForm, RegistrationForm, UserUpdateForm, PasswordChangeForm, 
                  CompanyForm, EmployeeForm, EmployeeDocumentForm, EmployeeNoteForm, SearchForm,
                  EmployeeStatusForm, EmployeeScheduleForm, EmployeeWeeklyScheduleForm, EmployeeCheckInForm, 
                  EmployeeVacationForm, GenerateCheckInsForm, ExportCheckInsForm)
from utils import (save_file, log_employee_change, log_activity, can_manage_company, 
                  can_manage_employee, can_view_employee, get_dashboard_stats, generate_checkins_pdf,
                  export_company_employees_zip, create_database_backup)
from clean_database import clean_database

# Create blueprints
auth_bp = Blueprint('auth', __name__)
main_bp = Blueprint('main', __name__)
company_bp = Blueprint('company', __name__, url_prefix='/companies')
employee_bp = Blueprint('employee', __name__, url_prefix='/employees')
user_bp = Blueprint('user', __name__, url_prefix='/users')
schedule_bp = Blueprint('schedule', __name__, url_prefix='/schedules')
checkin_bp = Blueprint('checkin', __name__, url_prefix='/checkins')
vacation_bp = Blueprint('vacation', __name__, url_prefix='/vacations')
ui_bp = Blueprint('ui', __name__, url_prefix='/ui')

# Decorator for admin-only routes
def admin_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or not current_user.is_admin():
            flash('No tienes permiso para acceder a esta página.', 'danger')
            return redirect(url_for('main.index'))
        return f(*args, **kwargs)
    return decorated_function

# Decorator for manager-only routes
def manager_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or (not current_user.is_admin() and not current_user.is_gerente()):
            flash('No tienes permiso para acceder a esta página.', 'danger')
            return redirect(url_for('main.index'))
        return f(*args, **kwargs)
    return decorated_function

# Authentication routes
@auth_bp.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('main.dashboard'))
    
    form = LoginForm()
    if form.validate_on_submit():
        user = User.query.filter_by(username=form.username.data).first()
        if user is None or not user.check_password(form.password.data) or not user.is_active:
            flash('Usuario o contraseña invalidos.', 'danger')
            return redirect(url_for('auth.login'))
        
        login_user(user, remember=form.remember_me.data)
        log_activity(f'Login exitoso: {user.username}', user.id)
        
        next_page = request.args.get('next')
        if not next_page or urlparse(next_page).netloc != '':
            next_page = url_for('main.dashboard')
        
        flash(f'Bienvenido, {user.username}!', 'success')
        return redirect(next_page)
    
    return render_template('login.html', title='Iniciar Sesión', form=form)

@auth_bp.route('/logout')
@login_required
def logout():
    log_activity(f'Logout: {current_user.username}')
    logout_user()
    flash('Has cerrado sesión correctamente.', 'success')
    return redirect(url_for('auth.login'))

@auth_bp.route('/register', methods=['GET', 'POST'])
@admin_required
def register():
    form = RegistrationForm()
    
    # Get list of companies for the checkbox field
    companies = Company.query.all()
    form.companies.choices = [(c.id, c.name) for c in companies]
    
    if form.validate_on_submit():
        # Validación adicional: solo el usuario "admin" puede crear usuarios con rol de administrador
        if form.role.data == UserRole.ADMIN.value and current_user.username != 'admin':
            flash('Solo el usuario "admin" puede crear usuarios con rol de administrador.', 'danger')
            return render_template('register.html', title='Registrar Usuario', form=form)
        
        user = User(
            username=form.username.data,
            email=form.email.data,
            first_name=form.first_name.data,
            last_name=form.last_name.data,
            role=UserRole(form.role.data)
        )
        user.set_password(form.password.data)
        
        # Añadir las empresas seleccionadas al usuario
        if form.companies.data:
            selected_companies = Company.query.filter(Company.id.in_(form.companies.data)).all()
            user.companies = selected_companies
            
        db.session.add(user)
        db.session.commit()
        
        log_activity(f'Usuario registrado: {user.username}')
        flash('¡Usuario registrado correctamente!', 'success')
        return redirect(url_for('user.list_users'))
    
    return render_template('register.html', title='Registrar Usuario', form=form)

# Main routes
@main_bp.route('/')
def index():
    if current_user.is_authenticated:
        return redirect(url_for('main.dashboard'))
    return redirect(url_for('auth.login'))

@main_bp.route('/dashboard')
@login_required
def dashboard():
    stats = get_dashboard_stats()
    return render_template('dashboard_modern.html', title='Dashboard', stats=stats, datetime=datetime)

@main_bp.route('/profile', methods=['GET', 'POST'])
@login_required
def profile():
    form = PasswordChangeForm()
    
    if form.validate_on_submit():
        if not current_user.check_password(form.current_password.data):
            flash('La contraseña actual es incorrecta.', 'danger')
            return redirect(url_for('main.profile'))
            
        current_user.set_password(form.new_password.data)
        db.session.commit()
        log_activity('Cambio de contraseña')
        flash('Contraseña actualizada correctamente.', 'success')
        return redirect(url_for('main.profile'))
    
    return render_template('profile.html', title='Mi Perfil', form=form)

@main_bp.route('/backup/')
@login_required
@admin_required
def backup_panel():
    """Panel de opciones de backup y exportación de datos."""
    return render_template('backup_panel.html', title='Panel de Backup')

@main_bp.route('/search')
@login_required
def search():
    query = request.args.get('query', '')
    if not query:
        return redirect(url_for('main.dashboard'))
    
    # Search for companies (admin and gerentes only)
    companies = []
    if current_user.is_admin():
        companies = Company.query.filter(
            (Company.name.ilike(f'%{query}%')) |
            (Company.tax_id.ilike(f'%{query}%'))
        ).all()
    elif current_user.is_gerente():
        # Buscar solo entre las empresas del usuario
        company_ids = [company.id for company in current_user.companies]
        if company_ids:
            companies = Company.query.filter(
                Company.id.in_(company_ids)
            ).filter(
                (Company.name.ilike(f'%{query}%')) |
                (Company.tax_id.ilike(f'%{query}%'))
            ).all()
    
    # Search for employees based on user role
    employees = []
    if current_user.is_admin():
        employees = Employee.query.filter(
            (Employee.first_name.ilike(f'%{query}%')) |
            (Employee.last_name.ilike(f'%{query}%')) |
            (Employee.dni.ilike(f'%{query}%'))
        ).all()
    elif current_user.is_gerente():
        # Buscar empleados de todas las empresas asignadas al gerente
        company_ids = [company.id for company in current_user.companies]
        if company_ids:
            employees = Employee.query.filter(
                Employee.company_id.in_(company_ids)
            ).filter(
                (Employee.first_name.ilike(f'%{query}%')) |
                (Employee.last_name.ilike(f'%{query}%')) |
                (Employee.dni.ilike(f'%{query}%'))
            ).all()
    elif current_user.is_empleado() and current_user.employee:
        # Empleados can only see themselves in search results
        if (query.lower() in current_user.employee.first_name.lower() or
            query.lower() in current_user.employee.last_name.lower() or
            query.lower() in current_user.employee.dni.lower()):
            employees = [current_user.employee]
    
    return render_template('search_results.html', 
                          title='Resultados de Búsqueda',
                          query=query,
                          companies=companies,
                          employees=employees)

# Company routes
@company_bp.route('/')
@login_required
def list_companies():
    # Admin puede ver todas las empresas
    if current_user.is_admin():
        companies = Company.query.all()
    # Gerente solo puede ver sus empresas asignadas
    elif current_user.is_gerente():
        companies = current_user.companies
    # Empleado solo puede ver sus empresas asignadas
    elif current_user.is_empleado():
        companies = current_user.companies
    else:
        companies = []
    
    return render_template('company_list.html', title='Empresas', companies=companies)

@company_bp.route('/<string:slug>')
@login_required
def view_company(slug):
    # Usar approach más robusto para buscar empresas por slug
    from utils import slugify, can_manage_company
    
    # Buscar por ID si es un número
    if slug.isdigit():
        company = Company.query.get_or_404(int(slug))
    else:
        # Buscar todas las empresas y comparar slugs
        all_companies = Company.query.all()
        company = next((c for c in all_companies if slugify(c.name) == slug), None)
        
        if not company:
            flash('Empresa no encontrada', 'danger')
            return redirect(url_for('company.list_companies'))
    
    # Check if user has permission to view this company - using the can_manage_company function
    if not can_manage_company(company.id):
        flash('No tienes permiso para ver esta empresa.', 'danger')
        return redirect(url_for('company.list_companies'))
    
    # Ordenar empleados: primero activos, luego inactivos
    sorted_employees = sorted(company.employees, key=lambda e: (not e.is_active, e.first_name.lower()))
    
    return render_template('company_detail.html', 
                          title=company.name, 
                          company=company,
                          sorted_employees=sorted_employees)

@company_bp.route('/new', methods=['GET', 'POST'])
@admin_required
def create_company():
    form = CompanyForm()
    
    if form.validate_on_submit():
        company = Company(
            name=form.name.data,
            address=form.address.data,
            city=form.city.data,
            postal_code=form.postal_code.data,
            country=form.country.data,
            sector=form.sector.data,
            tax_id=form.tax_id.data,
            phone=form.phone.data,
            email=form.email.data,
            website=form.website.data,
            is_active=form.is_active.data
        )
        db.session.add(company)
        db.session.commit()
        
        log_activity(f'Empresa creada: {company.name}')
        flash(f'Empresa "{company.name}" creada correctamente.', 'success')
        return redirect(url_for('company.list_companies'))
    
    return render_template('company_form.html', title='Nueva Empresa', form=form)

@company_bp.route('/<string:slug>/edit', methods=['GET', 'POST'])
@login_required
def edit_company(slug):
    # Usar approach más robusto para buscar empresas por slug
    from utils import slugify
    
    # Buscar por ID si es un número
    if slug.isdigit():
        company = Company.query.get_or_404(int(slug))
    else:
        # Buscar todas las empresas y comparar slugs
        all_companies = Company.query.all()
        company = next((c for c in all_companies if slugify(c.name) == slug), None)
        
        if not company:
            flash('Empresa no encontrada', 'danger')
            return redirect(url_for('company.list_companies'))
    
    # Check if user has permission to edit this company
    if not can_manage_company(company.id):
        flash('No tienes permiso para editar esta empresa.', 'danger')
        return redirect(url_for('company.list_companies'))
    
    # Pasamos el tax_id original para poder validar correctamente
    form = CompanyForm(original_tax_id=company.tax_id, obj=company)
    
    if form.validate_on_submit():
        company.name = form.name.data
        company.address = form.address.data
        company.city = form.city.data
        company.postal_code = form.postal_code.data
        company.country = form.country.data
        company.sector = form.sector.data
        company.tax_id = form.tax_id.data
        company.phone = form.phone.data
        company.email = form.email.data
        company.website = form.website.data
        company.is_active = form.is_active.data
        company.updated_at = datetime.utcnow()
        
        db.session.commit()
        
        log_activity(f'Empresa actualizada: {company.name}')
        flash(f'Empresa "{company.name}" actualizada correctamente.', 'success')
        return redirect(url_for('company.view_company', slug=company.get_slug()))
    
    return render_template('company_form.html', title=f'Editar {company.name}', form=form, company=company)

@company_bp.route('/<string:slug>/export', methods=['GET'])
@admin_required
def export_company_data(slug):
    # Usar approach más robusto para buscar empresas por slug
    from utils import slugify
    
    # Buscar por ID si es un número
    if slug.isdigit():
        company = Company.query.get_or_404(int(slug))
    else:
        # Buscar todas las empresas y comparar slugs
        all_companies = Company.query.all()
        company = next((c for c in all_companies if slugify(c.name) == slug), None)
        
        if not company:
            flash('Empresa no encontrada', 'danger')
            return redirect(url_for('company.list_companies'))
    
    # Export company data as ZIP
    export_data = export_company_employees_zip(company.id)
    if not export_data:
        flash('Error al exportar los datos de la empresa.', 'danger')
        return redirect(url_for('company.view_company', slug=company.get_slug()))
    
    log_activity(f'Datos de empresa exportados: {company.name}')
    return send_file(
        export_data['buffer'],
        as_attachment=True,
        download_name=export_data['filename'],
        mimetype='application/zip'
    )

@company_bp.route('/<string:slug>/delete', methods=['POST'])
@admin_required
def delete_company(slug):
    # Usar approach más robusto para buscar empresas por slug
    from utils import slugify
    
    # Buscar por ID si es un número
    if slug.isdigit():
        company = Company.query.get_or_404(int(slug))
    else:
        # Buscar todas las empresas y comparar slugs
        all_companies = Company.query.all()
        company = next((c for c in all_companies if slugify(c.name) == slug), None)
        
        if not company:
            flash('Empresa no encontrada', 'danger')
            return redirect(url_for('company.list_companies'))
    
    # Proceed with deletion
    company_name = company.name
    
    # Delete all related entities
    try:
        # Step 1: Delete checkpoints and related records
        from models_checkpoints import CheckPoint, CheckPointRecord, CheckPointIncident, EmployeeContractHours
        
        # First, get employee IDs for this company (needed for multiple operations)
        employee_ids = [emp.id for emp in company.employees]
        
        # Delete checkpoint incidents and records first
        checkpoints = CheckPoint.query.filter_by(company_id=company.id).all()
        checkpoint_ids = [checkpoint.id for checkpoint in checkpoints]
        
        # Locate all checkpoint records for all employees of this company first
        # (this covers both records from company checkpoints and from other checkpoints)
        all_records = []
        all_record_ids = []
        
        if employee_ids:
            # Get employee checkpoint records
            employee_records = CheckPointRecord.query.filter(CheckPointRecord.employee_id.in_(employee_ids)).all()
            all_records.extend(employee_records)
            all_record_ids.extend([r.id for r in employee_records])
        
        if checkpoint_ids:
            # Also get checkpoint records by checkpoint_id
            checkpoint_records = CheckPointRecord.query.filter(CheckPointRecord.checkpoint_id.in_(checkpoint_ids)).all()
            
            # Add only records not already included
            new_records = [r for r in checkpoint_records if r.id not in all_record_ids]
            all_records.extend(new_records)
            all_record_ids.extend([r.id for r in new_records])
            
        # Delete all checkpoint incidents for all identified records
        if all_record_ids:
            CheckPointIncident.query.filter(CheckPointIncident.record_id.in_(all_record_ids)).delete(synchronize_session=False)
            
            # Delete all checkpoint records
            CheckPointRecord.query.filter(CheckPointRecord.id.in_(all_record_ids)).delete(synchronize_session=False)
        
        # Delete all checkpoints for this company
        if checkpoint_ids:
            CheckPoint.query.filter(CheckPoint.company_id == company.id).delete(synchronize_session=False)
        
        # Step 2: Delete all task completions for tasks related to this company's locations
        from models_tasks import TaskCompletion, Task, Location, LocalUser, TaskSchedule, TaskWeekday, TaskGroup
        
        # Get all locations for this company
        locations = Location.query.filter_by(company_id=company.id).all()
        location_ids = [loc.id for loc in locations]
        
        # Delete task completions for tasks in these locations
        if location_ids:
            # Get all tasks for these locations
            tasks = Task.query.filter(Task.location_id.in_(location_ids)).all()
            task_ids = [task.id for task in tasks]
            
            if task_ids:
                # Delete all task completions
                TaskCompletion.query.filter(TaskCompletion.task_id.in_(task_ids)).delete(synchronize_session=False)
            
            # Delete all task related records (schedules and weekdays)
            TaskSchedule.query.filter(TaskSchedule.task_id.in_(task_ids)).delete(synchronize_session=False)
            TaskWeekday.query.filter(TaskWeekday.task_id.in_(task_ids)).delete(synchronize_session=False)
            
            # Delete all tasks for these locations
            Task.query.filter(Task.location_id.in_(location_ids)).delete(synchronize_session=False)
            
            # Delete task groups
            TaskGroup.query.filter(TaskGroup.location_id.in_(location_ids)).delete(synchronize_session=False)
            
            # Primero necesitamos obtener todos los productos para poder eliminar sus etiquetas
            from models_tasks import Product, ProductLabel, ProductConservation, LabelTemplate
            
            # Get all products for these locations
            products = Product.query.filter(Product.location_id.in_(location_ids)).all()
            product_ids = [product.id for product in products]
            
            # Delete all product labels first
            if product_ids:
                ProductLabel.query.filter(ProductLabel.product_id.in_(product_ids)).delete(synchronize_session=False)
                
                # Delete all product conservation settings
                ProductConservation.query.filter(ProductConservation.product_id.in_(product_ids)).delete(synchronize_session=False)
            
            # Get all local users for these locations to delete their product labels
            local_users = LocalUser.query.filter(LocalUser.location_id.in_(location_ids)).all()
            local_user_ids = [user.id for user in local_users]
            
            # Delete any product labels created by these local users (even for products outside these locations)
            if local_user_ids:
                ProductLabel.query.filter(ProductLabel.local_user_id.in_(local_user_ids)).delete(synchronize_session=False)
            
            # Delete all label templates for these locations
            LabelTemplate.query.filter(LabelTemplate.location_id.in_(location_ids)).delete(synchronize_session=False)
            
            # Delete all products
            Product.query.filter(Product.location_id.in_(location_ids)).delete(synchronize_session=False)
            
            # Delete all local users for these locations
            LocalUser.query.filter(LocalUser.location_id.in_(location_ids)).delete(synchronize_session=False)
            
            # Delete all locations
            Location.query.filter(Location.company_id == company.id).delete(synchronize_session=False)
        
        # Step 2: Delete all employee related records (documents, notes, history, schedules, etc.)
        from models import EmployeeDocument, EmployeeNote, EmployeeHistory, EmployeeSchedule, EmployeeCheckIn, EmployeeVacation
        
        employee_ids = [emp.id for emp in company.employees]
        
        if employee_ids:
            # Delete employee contract hours (from checkpoints module)
            EmployeeContractHours.query.filter(EmployeeContractHours.employee_id.in_(employee_ids)).delete(synchronize_session=False)
            
            # Delete documents
            EmployeeDocument.query.filter(EmployeeDocument.employee_id.in_(employee_ids)).delete(synchronize_session=False)
            
            # Delete notes
            EmployeeNote.query.filter(EmployeeNote.employee_id.in_(employee_ids)).delete(synchronize_session=False)
            
            # Delete history
            EmployeeHistory.query.filter(EmployeeHistory.employee_id.in_(employee_ids)).delete(synchronize_session=False)
            
            # Delete schedules
            EmployeeSchedule.query.filter(EmployeeSchedule.employee_id.in_(employee_ids)).delete(synchronize_session=False)
            
            # Delete check-ins
            EmployeeCheckIn.query.filter(EmployeeCheckIn.employee_id.in_(employee_ids)).delete(synchronize_session=False)
            
            # Delete vacations
            EmployeeVacation.query.filter(EmployeeVacation.employee_id.in_(employee_ids)).delete(synchronize_session=False)
        
        # Delete all employees
        for employee in company.employees:
            db.session.delete(employee)
        
        # En lugar de eliminar los usuarios, solo eliminaremos la relación en la tabla asociativa
        # La tabla asociativa user_companies se eliminará automáticamente cuando se elimine la empresa
        # debido a que la relación tiene cascade="all, delete-orphan"
        
        # También necesitamos establecer el company_id en NULL para los usuarios referenciando esta empresa
        db.session.execute(
            db.text("UPDATE users SET company_id = NULL WHERE company_id = :company_id"),
            {"company_id": company.id}
        )
        
        # Finally delete the company
        db.session.delete(company)
        db.session.commit()
        
        log_activity(f'Empresa eliminada: {company_name}')
        flash(f'Empresa "{company_name}" y todos sus datos relacionados han sido eliminados correctamente.', 'success')
        
    except Exception as e:
        db.session.rollback()
        log_activity(f'Error al eliminar empresa {company_name}: {str(e)}')
        flash(f'Error al eliminar la empresa: {str(e)}', 'danger')
    
    return redirect(url_for('company.list_companies'))

# Employee routes
@employee_bp.route('/')
@login_required
def list_employees():
    # Obtener parámetro de página, por defecto 1
    page = request.args.get('page', 1, type=int)
    per_page = 20  # Número de empleados por página
    
    # Preparar la consulta según el rol del usuario
    if current_user.is_admin():
        # Para administradores, usar paginación para evitar cargar todos los empleados a la vez
        query = Employee.query.order_by(Employee.last_name, Employee.first_name)
    
    # Gerente puede ver solo empleados de sus empresas
    elif current_user.is_gerente():
        # Obtener IDs de todas las empresas asignadas al gerente
        company_ids = [company.id for company in current_user.companies]
        if company_ids:
            query = Employee.query.filter(
                Employee.company_id.in_(company_ids)
            ).order_by(Employee.last_name, Employee.first_name)
        else:
            employees = []
            employees_by_company = {}
            return render_template('employee_list.html', title='Empleados', 
                                  employees=employees, employees_by_company=employees_by_company, pagination=None)
    
    # Empleado solo puede verse a sí mismo
    elif current_user.is_empleado() and current_user.employee:
        # Para un solo empleado no necesitamos paginación
        employees = [current_user.employee]
        # Agrupar empleado por empresa para visualización consistente
        company_name = current_user.employee.company.name if current_user.employee.company else "Sin Empresa Asignada"
        employees_by_company = {company_name: [current_user.employee]}
        return render_template('employee_list.html', title='Empleados', 
                              employees=employees, employees_by_company=employees_by_company, pagination=None)
    else:
        employees = []
        employees_by_company = {}
        return render_template('employee_list.html', title='Empleados', 
                              employees=employees, employees_by_company=employees_by_company, pagination=None)
    
    # Ejecutar consulta paginada
    pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    employees = pagination.items
    
    # Agrupar empleados por empresa
    employees_by_company = {}
    for employee in employees:
        company_name = employee.company.name if employee.company else "Sin Empresa Asignada"
        if company_name not in employees_by_company:
            employees_by_company[company_name] = []
        employees_by_company[company_name].append(employee)
    
    # Ordenar empresas alfabéticamente
    sorted_employees_by_company = {}
    for company_name in sorted(employees_by_company.keys()):
        sorted_employees_by_company[company_name] = employees_by_company[company_name]
    
    return render_template('employee_list.html', title='Empleados', 
                          employees=employees, 
                          employees_by_company=sorted_employees_by_company,
                          pagination=pagination)

@employee_bp.route('/<int:id>')
@login_required
def view_employee(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to view this employee
    if not can_view_employee(employee):
        flash('No tienes permiso para ver este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Determinar si el usuario puede administrar la activación del empleado
    can_manage_activation = False
    if current_user.is_admin():
        can_manage_activation = True
    elif current_user.is_gerente():
        # Convertir ambos IDs a string para comparar
        company_ids = [str(c.id) for c in current_user.companies]
        if str(employee.company_id) in company_ids:
            can_manage_activation = True
    
    # Verificación de permisos completada
    
    return render_template('employee_detail.html', 
                          title=f'{employee.first_name} {employee.last_name}', 
                          employee=employee,
                          can_manage_activation=can_manage_activation)

@employee_bp.route('/new', methods=['GET', 'POST'])
@manager_required
def create_employee():
    form = EmployeeForm()
    
    # Get list of companies for the dropdown
    if current_user.is_admin():
        companies = Company.query.all()
        form.company_id.choices = [(c.id, c.name) for c in companies]
    else:
        # Para gerentes, mostrar todas las empresas a las que tienen acceso
        companies = current_user.companies
        if companies:
            form.company_id.choices = [(c.id, c.name) for c in companies]
    
    if form.validate_on_submit():
        employee = Employee(
            first_name=form.first_name.data,
            last_name=form.last_name.data,
            dni=form.dni.data,
            social_security_number=form.social_security_number.data,
            email=form.email.data,
            address=form.address.data,
            phone=form.phone.data,
            position=form.position.data,
            contract_type=ContractType(form.contract_type.data) if form.contract_type.data else None,
            bank_account=form.bank_account.data,
            start_date=form.start_date.data,
            end_date=form.end_date.data,
            company_id=form.company_id.data,
            is_active=form.is_active.data,
            status=EmployeeStatus(form.status.data) if form.status.data else EmployeeStatus.ACTIVO,
            status_start_date=form.start_date.data  # Por defecto, la fecha de inicio de estado es la misma de contratación
        )
        db.session.add(employee)
        db.session.commit()
        
        # Asignar automáticamente el empleado a todos los puntos de fichaje de su empresa
        try:
            # Obtener todos los puntos de fichaje de la empresa
            checkpoints = CheckPoint.query.filter_by(company_id=employee.company_id).all()
            if checkpoints:
                log_activity(f'Asignando empleado a {len(checkpoints)} puntos de fichaje')
                # No se necesita hacer ninguna asignación explícita, ya que los puntos de fichaje
                # acceden a los empleados a través de su relación con la empresa
                
                # Podríamos usar este espacio para configuración adicional si fuera necesario en el futuro
                # Por ejemplo, inicializar ContractHours para cada empleado
                pass
        except Exception as e:
            # No detenemos la creación del empleado si hay algún error en esta parte
            print(f"Error al asignar empleado a puntos de fichaje: {str(e)}")
            log_activity(f'Error al asignar empleado a puntos de fichaje: {str(e)}')
        
        log_activity(f'Empleado creado: {employee.first_name} {employee.last_name}')
        flash(f'Empleado "{employee.first_name} {employee.last_name}" creado correctamente.', 'success')
        return redirect(url_for('employee.view_employee', id=employee.id))
    
    return render_template('employee_form.html', title='Nuevo Empleado', form=form)

@employee_bp.route('/<int:id>/edit', methods=['GET', 'POST'])
@login_required
def edit_employee(id):
    # Obtener el empleado directamente de la base de datos
    employee = Employee.query.get_or_404(id)
    
    # Verificar permisos
    if not can_manage_employee(employee):
        flash('No tienes permiso para editar este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Método GET: Preparar el formulario con los datos del empleado
    if request.method == 'GET':
        form = EmployeeForm(obj=employee)
        
        # Configurar las opciones de empresas según el rol del usuario
        if current_user.is_admin():
            companies = Company.query.all()
            form.company_id.choices = [(c.id, c.name) for c in companies]
        else:
            companies = current_user.companies
            if companies:
                form.company_id.choices = [(c.id, c.name) for c in companies]
                
        # Log para depuración
        log_activity(f"Form loaded for Employee {employee.id}: end_date = {employee.end_date}")
        
        return render_template('employee_form.html', 
                             title=f'Editar {employee.first_name} {employee.last_name}', 
                             form=form, 
                             employee=employee)
    
    # Método POST: Procesar el formulario
    elif request.method == 'POST':
        # Inicializar el formulario con los datos del POST
        form = EmployeeForm(formdata=request.form)
        
        # Configurar las opciones de empresas nuevamente
        if current_user.is_admin():
            companies = Company.query.all()
            form.company_id.choices = [(c.id, c.name) for c in companies]
        else:
            companies = current_user.companies
            if companies:
                form.company_id.choices = [(c.id, c.name) for c in companies]
        
        # Validar el formulario
        if form.validate():
            try:
                # Registrar los cambios para auditoría (solo registra, no afecta a la operación)
                # Si hay algún cambio, lo registramos en el historial
                old_values = {
                    'first_name': employee.first_name,
                    'last_name': employee.last_name,
                    'dni': employee.dni,
                    'social_security_number': employee.social_security_number,
                    'email': employee.email,
                    'address': employee.address,
                    'phone': employee.phone,
                    'position': employee.position,
                    'contract_type': employee.contract_type.value if employee.contract_type else None,
                    'bank_account': employee.bank_account,
                    'start_date': employee.start_date,
                    'end_date': employee.end_date,
                    'is_active': employee.is_active,
                    'status': employee.status.value if employee.status else 'activo',
                    'company_id': employee.company_id
                }
                
                # Asignar los valores del formulario directamente al objeto employee
                # sin comparaciones intermedias ni lógica compleja
                employee.first_name = form.first_name.data
                employee.last_name = form.last_name.data
                employee.dni = form.dni.data
                employee.social_security_number = form.social_security_number.data
                employee.email = form.email.data
                employee.address = form.address.data
                employee.phone = form.phone.data
                employee.position = form.position.data
                employee.contract_type = ContractType(form.contract_type.data) if form.contract_type.data else None
                employee.bank_account = form.bank_account.data
                employee.start_date = form.start_date.data
                
                # IMPORTANTE: Procesar y limpiar las fechas
                # Asignar fechas directamente, limpiando valores vacíos
                end_date = form.end_date.data.strip() if form.end_date.data and form.end_date.data.strip() else None
                employee.end_date = end_date
                
                employee.company_id = form.company_id.data
                employee.is_active = form.is_active.data
                employee.status = EmployeeStatus(form.status.data)
                employee.updated_at = datetime.utcnow()
                
                # Ejecutar actualización directamente en SQL para asegurar que los datos se guardan
                # Esta consulta asegura que los datos se escriben correctamente, incluso si hay algún 
                # problema con el ORM
                if end_date is not None:
                    db.session.execute(text(
                        "UPDATE employees SET end_date = :end_date WHERE id = :id"),
                        {"end_date": end_date, "id": employee.id}
                    )
                else:
                    db.session.execute(text(
                        "UPDATE employees SET end_date = NULL WHERE id = :id"),
                        {"id": employee.id}
                    )
                
                # Aplicar los cambios a la base de datos
                db.session.commit()
                
                # Registrar los cambios en el historial (para auditoría)
                # Solo después de que la operación haya sido exitosa
                for field in old_values:
                    old_value = old_values[field]
                    if field == 'company_id':
                        # Caso especial para company_id: registrar los nombres
                        if old_value != form.company_id.data:
                            old_company = Company.query.get(old_value).name if old_value else 'Ninguna'
                            new_company = Company.query.get(form.company_id.data).name
                            log_employee_change(employee, 'company', old_company, new_company)
                    else:
                        # Para el resto de campos
                        new_value = getattr(employee, field)
                        if field == 'contract_type' or field == 'status':
                            new_value = new_value.value if new_value else None
                        
                        if str(old_value) != str(new_value):
                            log_employee_change(employee, field, str(old_value) if old_value is not None else None, 
                                             str(new_value) if new_value is not None else None)
                
                # Verificar que los cambios se guardaron correctamente
                db.session.refresh(employee)
                
                # Notificar al usuario
                log_activity(f'Empleado actualizado: {employee.first_name} {employee.last_name}')
                flash(f'Empleado "{employee.first_name} {employee.last_name}" actualizado correctamente.', 'success')
                
                # Redirigir a la página de detalle del empleado
                return redirect(url_for('employee.view_employee', id=employee.id))
                
            except Exception as e:
                # Manejar cualquier error que pueda ocurrir durante el proceso
                db.session.rollback()
                log_activity(f"Error al actualizar empleado: {str(e)}")
                flash(f'Error al actualizar empleado: {str(e)}', 'danger')
                
                # Mostrar el formulario nuevamente con los datos proporcionados
                return render_template('employee_form.html', 
                                     title=f'Editar {employee.first_name} {employee.last_name}', 
                                     form=form, 
                                     employee=employee)
        else:
            # Si el formulario no es válido, mostrar errores
            for field, errors in form.errors.items():
                for error in errors:
                    flash(f"Error en {getattr(form, field).label.text}: {error}", 'danger')
            
            # Volver a mostrar el formulario con los errores
            return render_template('employee_form.html', 
                                 title=f'Editar {employee.first_name} {employee.last_name}', 
                                 form=form, 
                                 employee=employee)
            
    # Nunca debería llegar aquí, pero por si acaso
    return redirect(url_for('employee.list_employees'))

@employee_bp.route('/<int:id>/delete', methods=['POST'])
@manager_required
def delete_employee(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to delete this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Verificar si el empleado tiene registros de fichaje
    from models_checkpoints import CheckPointRecord
    checkpoint_records = CheckPointRecord.query.filter_by(employee_id=employee.id).count()
    
    if checkpoint_records > 0:
        employee_name = f"{employee.first_name} {employee.last_name}"
        message = f'No se puede eliminar al empleado "{employee_name}" porque tiene {checkpoint_records} registros de fichaje asociados. '
        message += 'Para eliminar este empleado, primero debes eliminar todos sus registros de fichaje o desactivarlo en lugar de eliminarlo.'
        log_activity(f'Intento fallido de eliminar empleado con registros: {employee_name}')
        flash(message, 'warning')
        return redirect(url_for('employee.view_employee', id=employee.id))
    
    try:
        employee_name = f"{employee.first_name} {employee.last_name}"
        db.session.delete(employee)
        db.session.commit()
        
        log_activity(f'Empleado eliminado: {employee_name}')
        flash(f'Empleado "{employee_name}" eliminado correctamente.', 'success')
        return redirect(url_for('employee.list_employees'))
    except Exception as e:
        db.session.rollback()
        log_activity(f'Error al eliminar empleado {employee.id}: {str(e)}')
        flash(f'Error al eliminar empleado: {str(e)}', 'danger')
        return redirect(url_for('employee.list_employees'))
    
@employee_bp.route('/<int:id>/status', methods=['GET', 'POST'])
@login_required
def manage_status(id):
    # Obtener el empleado directamente de la base de datos
    employee = Employee.query.get_or_404(id)
    
    # Verificar permisos
    if not can_manage_employee(employee):
        flash('No tienes permiso para gestionar el estado de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Método GET: Preparar el formulario con los datos del empleado
    if request.method == 'GET':
        form = EmployeeStatusForm(obj=employee)
        
        # Registrar actividad
        log_activity(f"Formulario de estado del empleado cargado: {employee.first_name} {employee.last_name}")
        
        return render_template('employee_status.html', 
                             title=f'Gestionar Estado - {employee.first_name} {employee.last_name}', 
                             form=form, 
                             employee=employee)
    
    # Método POST: Procesar el formulario
    elif request.method == 'POST':
        form = EmployeeStatusForm(formdata=request.form)
        
        # Validar el formulario
        if form.validate():
            try:
                # Registrar los cambios para auditoría
                old_values = {
                    'status': employee.status.value if employee.status else 'activo',
                    'status_start_date': employee.status_start_date,
                    'status_end_date': employee.status_end_date,
                    'status_notes': employee.status_notes
                }
                
                # Actualizar el estado
                employee.status = EmployeeStatus(form.status.data)
                
                # IMPORTANTE: Procesar y limpiar las fechas
                status_start_date = form.status_start_date.data.strip() if form.status_start_date.data and form.status_start_date.data.strip() else None
                status_end_date = form.status_end_date.data.strip() if form.status_end_date.data and form.status_end_date.data.strip() else None
                
                # Asignar los valores procesados
                employee.status_start_date = status_start_date
                employee.status_end_date = status_end_date
                employee.status_notes = form.status_notes.data
                employee.updated_at = datetime.utcnow()
                
                # Garantizar la persistencia de los datos incluso con SQL directo
                if status_start_date is not None or status_end_date is not None:
                    db.session.execute(text(
                        """UPDATE employees 
                           SET status_start_date = :start_date,
                               status_end_date = :end_date,
                               status = :status,
                               status_notes = :notes
                           WHERE id = :id"""),
                        {
                            "start_date": status_start_date,
                            "end_date": status_end_date,
                            "status": form.status.data,
                            "notes": form.status_notes.data,
                            "id": employee.id
                        }
                    )
                
                # Guardar los cambios
                db.session.commit()
                
                # Verificar que los cambios se guardaron correctamente
                updated_employee = Employee.query.get(employee.id)
                
                # Registrar cambios en el historial después de la operación exitosa
                for field in old_values:
                    old_value = old_values[field]
                    new_value = getattr(employee, field)
                    
                    if field == 'status':
                        new_value = new_value.value if new_value else None
                    
                    if str(old_value) != str(new_value):
                        log_employee_change(employee, field, 
                                          str(old_value) if old_value is not None else None, 
                                          str(new_value) if new_value is not None else None)
                
                # Registrar en el log
                log_activity(f'Estado de empleado actualizado: {employee.first_name} {employee.last_name}')
                flash(f'Estado del empleado "{employee.first_name} {employee.last_name}" actualizado correctamente.', 'success')
                
                # Redirigir a la página de detalle del empleado
                return redirect(url_for('employee.view_employee', id=employee.id))
                
            except Exception as e:
                # Manejar cualquier error
                db.session.rollback()
                log_activity(f"Error al actualizar estado de empleado: {str(e)}")
                flash(f'Error al actualizar estado: {str(e)}', 'danger')
        else:
            # Si el formulario no es válido, mostrar errores
            for field, errors in form.errors.items():
                for error in errors:
                    flash(f"Error en {getattr(form, field).label.text}: {error}", 'danger')
        
        # Si hay errores, volver a mostrar el formulario con los mensajes
        return render_template('employee_status.html', 
                             title=f'Gestionar Estado - {employee.first_name} {employee.last_name}', 
                             form=form, 
                             employee=employee)

@employee_bp.route('/<int:id>/documents')
@login_required
def list_documents(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to view this employee's documents
    if not can_view_employee(employee):
        flash('No tienes permiso para ver los documentos de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    documents = EmployeeDocument.query.filter_by(employee_id=employee.id).all()
    return render_template('employee_documents.html', 
                          title=f'Documentos de {employee.first_name} {employee.last_name}', 
                          employee=employee, 
                          documents=documents)

@employee_bp.route('/<int:id>/documents/upload', methods=['GET', 'POST'])
@login_required
def upload_document(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to upload documents for this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para subir documentos para este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeDocumentForm()
    
    if form.validate_on_submit():
        file_data = save_file(form.file.data, form.description.data)
        if file_data:
            document = EmployeeDocument(
                employee_id=employee.id,
                filename=file_data['filename'],
                original_filename=file_data['original_filename'],
                file_path=file_data['file_path'],
                file_type=file_data['file_type'],
                file_size=file_data['file_size'],
                description=file_data['description']
            )
            db.session.add(document)
            db.session.commit()
            
            log_activity(f'Documento subido para {employee.first_name} {employee.last_name}: {file_data["original_filename"]}')
            flash(f'Documento "{file_data["original_filename"]}" subido correctamente.', 'success')
            return redirect(url_for('employee.list_documents', id=employee.id))
        else:
            flash('Error al subir el documento. Formato no permitido.', 'danger')
    
    return render_template('employee_document_upload.html', 
                          title=f'Subir Documento para {employee.first_name} {employee.last_name}', 
                          employee=employee, 
                          form=form)

@employee_bp.route('/documents/<int:doc_id>')
@login_required
def download_document(doc_id):
    document = EmployeeDocument.query.get_or_404(doc_id)
    employee = Employee.query.get_or_404(document.employee_id)
    
    # Check if user has permission to download this document
    if not can_view_employee(employee):
        flash('No tienes permiso para descargar este documento.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    log_activity(f'Documento descargado: {document.original_filename}')
    return send_from_directory(
        os.path.dirname(document.file_path),
        os.path.basename(document.file_path),
        as_attachment=True,
        download_name=document.original_filename
    )

@employee_bp.route('/documents/<int:doc_id>/delete', methods=['POST'])
@login_required
def delete_document(doc_id):
    document = EmployeeDocument.query.get_or_404(doc_id)
    employee = Employee.query.get_or_404(document.employee_id)
    
    # Check if user has permission to delete this document
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar este documento.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Delete file from filesystem
    if os.path.exists(document.file_path):
        os.remove(document.file_path)
    
    document_name = document.original_filename
    db.session.delete(document)
    db.session.commit()
    
    log_activity(f'Documento eliminado: {document_name}')
    flash(f'Documento "{document_name}" eliminado correctamente.', 'success')
    return redirect(url_for('employee.list_documents', id=employee.id))

@employee_bp.route('/<int:id>/notes', methods=['GET', 'POST'])
@login_required
def manage_notes(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to manage notes for this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para gestionar notas de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeNoteForm()
    
    if form.validate_on_submit():
        note = EmployeeNote(
            employee_id=employee.id,
            content=form.content.data,
            created_by_id=current_user.id
        )
        db.session.add(note)
        db.session.commit()
        
        log_activity(f'Nota añadida para {employee.first_name} {employee.last_name}')
        flash('Nota añadida correctamente.', 'success')
        return redirect(url_for('employee.manage_notes', id=employee.id))
    
    notes = EmployeeNote.query.filter_by(employee_id=employee.id).order_by(EmployeeNote.created_at.desc()).all()
    return render_template('employee_notes.html', 
                          title=f'Notas de {employee.first_name} {employee.last_name}', 
                          employee=employee, 
                          notes=notes, 
                          form=form)

@employee_bp.route('/backup/database', methods=['GET', 'POST'])
@login_required
@admin_required
def backup_database():
    """Create a database backup"""
    if request.method == 'POST':
        result = create_database_backup()
        if result['success']:
            flash('Copia de seguridad creada con éxito', 'success')
            return send_file(
                result['file'],
                as_attachment=True,
                download_name=f"backup_{result['timestamp']}.zip",
                mimetype='application/zip'
            )
        else:
            error_message = result.get("error", "Error desconocido")
            flash(f'Error al crear la copia de seguridad: {error_message}', 'danger')
            
    return render_template('backup_form.html', title='Copia de Seguridad de Base de Datos')

@employee_bp.route('/notes/<int:note_id>/delete', methods=['POST'])
@login_required
def delete_note(note_id):
    note = EmployeeNote.query.get_or_404(note_id)
    employee = Employee.query.get_or_404(note.employee_id)
    
    # Check if user has permission to delete this note
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar esta nota.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    db.session.delete(note)
    db.session.commit()
    
    log_activity(f'Nota eliminada para {employee.first_name} {employee.last_name}')
    flash('Nota eliminada correctamente.', 'success')
    return redirect(url_for('employee.manage_notes', id=employee.id))

@employee_bp.route('/<int:id>/toggle-active', methods=['POST'])
@login_required
def toggle_employee_activation(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para cambiar el estado de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Toggle the is_active status
    old_status = employee.is_active
    employee.is_active = not employee.is_active
    
    # Log the change
    log_employee_change(employee, 'is_active', str(old_status), str(employee.is_active))
    employee.updated_at = datetime.utcnow()
    
    db.session.commit()
    
    status = 'activado' if employee.is_active else 'desactivado'
    log_activity(f'Empleado {status}: {employee.first_name} {employee.last_name}')
    flash(f'Empleado "{employee.first_name} {employee.last_name}" {status} correctamente.', 'success')
    
    # Redirect back to the appropriate page
    return redirect(url_for('employee.view_employee', id=employee.id))

@employee_bp.route('/<int:id>/history')
@login_required
def view_history(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to view this employee's history
    if not can_view_employee(employee):
        flash('No tienes permiso para ver el historial de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    history = employee.history
    return render_template('employee_history.html', 
                          title=f'Historial de {employee.first_name} {employee.last_name}', 
                          employee=employee, 
                          history=history)

@employee_bp.route('/<int:id>/vacations')
@login_required
def view_vacations(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to view this employee's vacations
    if not can_view_employee(employee):
        flash('No tienes permiso para ver las vacaciones de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Redirect to the vacation blueprint's list view
    return redirect(url_for('vacation.list_vacations', employee_id=employee.id))

@employee_bp.route('/<int:id>/vacations/manage')
@login_required
@manager_required
def manage_vacations(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to manage this employee's vacations
    if not current_user.is_admin() and (current_user.company_id != employee.company_id):
        flash('No tienes permiso para gestionar las vacaciones de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Redirect to the vacation list with management capabilities
    return redirect(url_for('vacation.list_vacations', employee_id=employee.id))

# User routes
@user_bp.route('/')
@admin_required
def list_users():
    users = User.query.all()
    return render_template('user_management.html', title='Gestión de Usuarios', users=users)

@user_bp.route('/<int:id>/edit', methods=['GET', 'POST'])
@admin_required
def edit_user(id):
    user = User.query.get_or_404(id)
    form = UserUpdateForm(user.username, user.email, obj=user)
    
    # Get list of companies for the checkbox field
    companies = Company.query.all()
    form.companies.choices = [(c.id, c.name) for c in companies]
    
    # Si es una solicitud GET, establecer los valores iniciales de las empresas
    if request.method == 'GET':
        form.companies.data = [company.id for company in user.companies]
    
    if form.validate_on_submit():
        # Validación adicional: solo el usuario "admin" puede asignar el rol de administrador
        # o cambiar el rol del usuario "admin"
        if (form.role.data == UserRole.ADMIN.value and current_user.username != 'admin') or \
           (user.username == 'admin' and current_user.username != 'admin'):
            flash('Solo el usuario "admin" puede asignar el rol de administrador o modificar al usuario "admin".', 'danger')
            return render_template('user_form.html', title=f'Editar Usuario {user.username}', form=form, user=user)
        
        # Protección adicional: Si el usuario a editar es "admin", no permitir cambios en su rol
        if user.username == 'admin' and form.role.data != UserRole.ADMIN.value:
            flash('No se puede cambiar el rol del usuario "admin".', 'danger')
            return render_template('user_form.html', title=f'Editar Usuario {user.username}', form=form, user=user)
        
        user.username = form.username.data
        user.email = form.email.data
        user.first_name = form.first_name.data
        user.last_name = form.last_name.data
        user.role = UserRole(form.role.data)
        user.is_active = form.is_active.data
        
        # Actualizar las empresas del usuario
        if form.companies.data:
            selected_companies = Company.query.filter(Company.id.in_(form.companies.data)).all()
            user.companies = selected_companies
        else:
            user.companies = []
        
        db.session.commit()
        
        log_activity(f'Usuario actualizado: {user.username}')
        flash(f'Usuario "{user.username}" actualizado correctamente.', 'success')
        return redirect(url_for('user.list_users'))
    
    return render_template('user_form.html', title=f'Editar Usuario {user.username}', form=form, user=user)

@user_bp.route('/<int:id>/reset-password', methods=['POST'])
@admin_required
def reset_password(id):
    user = User.query.get_or_404(id)
    
    # No permitir restablecer la contraseña del usuario "admin" a menos que el usuario actual sea "admin"
    if user.username == 'admin' and current_user.username != 'admin':
        flash('No tienes permiso para restablecer la contraseña del usuario "admin".', 'danger')
        return redirect(url_for('user.list_users'))
    
    # Generate a temporary password
    temp_password = f"temp{id}pwd{int(datetime.utcnow().timestamp())}"[:12]
    user.set_password(temp_password)
    db.session.commit()
    
    log_activity(f'Contraseña restablecida para: {user.username}')
    flash(f'Contraseña de "{user.username}" restablecida a: {temp_password}', 'success')
    return redirect(url_for('user.list_users'))

@user_bp.route('/<int:id>/activate', methods=['POST'])
@admin_required
def toggle_activation(id):
    user = User.query.get_or_404(id)
    
    # Don't allow deactivating own account
    if user.id == current_user.id:
        flash('No puedes desactivar tu propia cuenta.', 'danger')
        return redirect(url_for('user.list_users'))
    
    # No permitir desactivar al usuario "admin"
    if user.username == 'admin':
        flash('No se puede desactivar al usuario "admin".', 'danger')
        return redirect(url_for('user.list_users'))
    
    user.is_active = not user.is_active
    db.session.commit()
    
    status = 'activado' if user.is_active else 'desactivado'
    log_activity(f'Usuario {status}: {user.username}')
    flash(f'Usuario "{user.username}" {status} correctamente.', 'success')
    return redirect(url_for('user.list_users'))

@user_bp.route('/<int:id>/delete', methods=['POST'])
@admin_required
def delete_user(id):
    user = User.query.get_or_404(id)
    
    # Don't allow deleting own account
    if user.id == current_user.id:
        flash('No puedes eliminar tu propia cuenta.', 'danger')
        return redirect(url_for('user.list_users'))
    
    # No permitir eliminar al usuario "admin"
    if user.username == 'admin':
        flash('No se puede eliminar al usuario "admin".', 'danger')
        return redirect(url_for('user.list_users'))
    
    username = user.username
    db.session.delete(user)
    db.session.commit()
    
    log_activity(f'Usuario eliminado: {username}')
    flash(f'Usuario "{username}" eliminado correctamente.', 'success')
    return redirect(url_for('user.list_users'))

# Schedules routes
@schedule_bp.route('/employee/<int:employee_id>')
@login_required
def list_schedules(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to view this employee
    if not can_view_employee(employee):
        flash('No tienes permiso para ver los horarios de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    schedules = EmployeeSchedule.query.filter_by(employee_id=employee_id).all()
    
    return render_template('schedule_list.html', 
                          title=f'Horarios de {employee.first_name} {employee.last_name}', 
                          employee=employee,
                          schedules=schedules)

@schedule_bp.route('/employee/<int:employee_id>/new', methods=['GET', 'POST'])
@manager_required
def create_schedule(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para gestionar los horarios de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeScheduleForm()
    
    if form.validate_on_submit():
        schedule = EmployeeSchedule(
            day_of_week=WeekDay(form.day_of_week.data),
            start_time=form.start_time.data,
            end_time=form.end_time.data,
            is_working_day=form.is_working_day.data,
            employee_id=employee_id
        )
        db.session.add(schedule)
        db.session.commit()
        
        log_activity(f'Horario creado para {employee.first_name} {employee.last_name}')
        flash('Horario creado correctamente.', 'success')
        return redirect(url_for('schedule.list_schedules', employee_id=employee_id))
    
    return render_template('schedule_form.html', 
                          title=f'Nuevo Horario para {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee)

@schedule_bp.route('/employee/<int:employee_id>/weekly', methods=['GET', 'POST'])
@manager_required
def weekly_schedule(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para gestionar los horarios de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeWeeklyScheduleForm()
    
    # Si es una petición GET, cargar los horarios existentes
    if request.method == 'GET':
        # Obtener los horarios existentes para cada día
        schedules = EmployeeSchedule.query.filter_by(employee_id=employee_id).all()
        day_schedules = {schedule.day_of_week.value: schedule for schedule in schedules}
        
        # Cargar los datos en el formulario
        for day in ["lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"]:
            if day in day_schedules:
                schedule = day_schedules[day]
                getattr(form, f"{day}_is_working_day").data = schedule.is_working_day
                getattr(form, f"{day}_start_time").data = schedule.start_time
                getattr(form, f"{day}_end_time").data = schedule.end_time
    
    if form.validate_on_submit():
        # Para cada día de la semana, crear o actualizar el horario
        for day_name in ["lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"]:
            # Obtener los datos del formulario para este día
            is_working_day = getattr(form, f"{day_name}_is_working_day").data
            start_time = getattr(form, f"{day_name}_start_time").data
            end_time = getattr(form, f"{day_name}_end_time").data
            
            # Valores por defecto seguros para evitar errores NULL
            if not start_time:
                start_time = time(9, 0)  # 9:00 AM por defecto
            
            if not end_time:
                end_time = time(18, 0)  # 6:00 PM por defecto
            
            # Convertir el nombre del día a un WeekDay
            day_enum = WeekDay(day_name)
            
            # Buscar un horario existente para este día
            schedule = EmployeeSchedule.query.filter_by(
                employee_id=employee_id, 
                day_of_week=day_enum
            ).first()
            
            # Si no existe horario para este día, crearlo independientemente de si es laborable o no
            if not schedule:
                schedule = EmployeeSchedule(
                    day_of_week=day_enum,
                    start_time=start_time,
                    end_time=end_time,
                    is_working_day=is_working_day,
                    employee_id=employee_id
                )
                db.session.add(schedule)
            # Si existe, actualizarlo en todos los casos
            else:
                schedule.is_working_day = is_working_day
                schedule.start_time = start_time
                schedule.end_time = end_time
        
        db.session.commit()
        log_activity(f'Horarios semanales actualizados para {employee.first_name} {employee.last_name}')
        flash('Horarios semanales actualizados correctamente.', 'success')
        return redirect(url_for('schedule.list_schedules', employee_id=employee_id))
    
    return render_template('weekly_schedule_form.html', 
                          title=f'Horario Semanal para {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee)

@schedule_bp.route('/<int:id>/edit', methods=['GET', 'POST'])
@manager_required
def edit_schedule(id):
    schedule = EmployeeSchedule.query.get_or_404(id)
    employee = Employee.query.get_or_404(schedule.employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para editar los horarios de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeScheduleForm(obj=schedule)
    
    if form.validate_on_submit():
        schedule.day_of_week = WeekDay(form.day_of_week.data)
        schedule.start_time = form.start_time.data
        schedule.end_time = form.end_time.data
        schedule.is_working_day = form.is_working_day.data
        schedule.updated_at = datetime.utcnow()
        
        db.session.commit()
        
        log_activity(f'Horario actualizado para {employee.first_name} {employee.last_name}')
        flash('Horario actualizado correctamente.', 'success')
        return redirect(url_for('schedule.list_schedules', employee_id=schedule.employee_id))
    
    return render_template('schedule_form.html', 
                          title=f'Editar Horario de {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee,
                          schedule=schedule)

@schedule_bp.route('/<int:id>/delete', methods=['POST'])
@manager_required
def delete_schedule(id):
    schedule = EmployeeSchedule.query.get_or_404(id)
    employee = Employee.query.get_or_404(schedule.employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar los horarios de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    employee_id = schedule.employee_id
    db.session.delete(schedule)
    db.session.commit()
    
    log_activity(f'Horario eliminado para {employee.first_name} {employee.last_name}')
    flash('Horario eliminado correctamente.', 'success')
    return redirect(url_for('schedule.list_schedules', employee_id=employee_id))

# Check-ins routes
@checkin_bp.route('/employee/<int:employee_id>')
@login_required
def list_checkins(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to view this employee
    if not can_view_employee(employee):
        flash('No tienes permiso para ver los fichajes de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Form for exporting check-ins to PDF
    export_form = ExportCheckInsForm()
    
    # Obtener todos los fichajes ordenados por fecha
    all_checkins = EmployeeCheckIn.query.filter_by(employee_id=employee_id).order_by(EmployeeCheckIn.check_in_time.desc()).all()
    
    # Agrupar fichajes por mes
    from datetime import datetime
    
    # Función para obtener el mes y año
    def get_month_year(check_in_time):
        return (check_in_time.year, check_in_time.month)
    
    # Crear un diccionario para agrupar por meses
    checkins_by_month = {}
    
    for checkin in all_checkins:
        year_month = get_month_year(checkin.check_in_time)
        year = year_month[0]
        month = year_month[1]
        
        # Obtener el nombre del mes en español
        month_names = [
            'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 
            'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
        ]
        month_name = month_names[month - 1]
        month_label = f"{month_name} {year}"
        
        if month_label not in checkins_by_month:
            checkins_by_month[month_label] = []
        
        checkins_by_month[month_label].append(checkin)
    
    return render_template('checkin_list.html', 
                          title=f'Fichajes de {employee.first_name} {employee.last_name}', 
                          employee=employee,
                          checkins=all_checkins,
                          checkins_by_month=checkins_by_month,
                          export_form=export_form)

@checkin_bp.route('/employee/<int:employee_id>/new', methods=['GET', 'POST'])
@manager_required
def create_checkin(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para gestionar los fichajes de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeCheckInForm()
    
    if form.validate_on_submit():
        # Combinar fecha y hora para la entrada
        check_in_time = datetime.combine(form.check_in_date.data, form.check_in_time.data)
        
        # Combinar fecha y hora para la salida (si existe)
        check_out_time = None
        if form.check_out_time.data:
            check_out_time = datetime.combine(form.check_in_date.data, form.check_out_time.data)
        
        checkin = EmployeeCheckIn(
            check_in_time=check_in_time,
            check_out_time=check_out_time,
            notes=form.notes.data,
            employee_id=employee_id
        )
        db.session.add(checkin)
        db.session.commit()
        
        log_activity(f'Fichaje creado para {employee.first_name} {employee.last_name}')
        flash('Fichaje creado correctamente.', 'success')
        return redirect(url_for('checkin.list_checkins', employee_id=employee_id))
    
    return render_template('checkin_form.html', 
                          title=f'Nuevo Fichaje para {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee)

@checkin_bp.route('/<int:id>/edit', methods=['GET', 'POST'])
@manager_required
def edit_checkin(id):
    checkin = EmployeeCheckIn.query.get_or_404(id)
    employee = Employee.query.get_or_404(checkin.employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para editar los fichajes de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeCheckInForm()
    
    if request.method == 'GET':
        form.check_in_date.data = checkin.check_in_time.date()
        form.check_in_time.data = checkin.check_in_time.time()
        form.check_out_time.data = checkin.check_out_time.time() if checkin.check_out_time else None
        form.notes.data = checkin.notes
    
    if form.validate_on_submit():
        # Combinar fecha y hora para la entrada
        check_in_time = datetime.combine(form.check_in_date.data, form.check_in_time.data)
        
        # Combinar fecha y hora para la salida (si existe)
        check_out_time = None
        if form.check_out_time.data:
            check_out_time = datetime.combine(form.check_in_date.data, form.check_out_time.data)
        
        checkin.check_in_time = check_in_time
        checkin.check_out_time = check_out_time
        checkin.notes = form.notes.data
        checkin.updated_at = datetime.utcnow()
        
        db.session.commit()
        
        log_activity(f'Fichaje actualizado para {employee.first_name} {employee.last_name}')
        flash('Fichaje actualizado correctamente.', 'success')
        return redirect(url_for('checkin.list_checkins', employee_id=checkin.employee_id))
    
    return render_template('checkin_form.html', 
                          title=f'Editar Fichaje de {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee,
                          checkin=checkin)

@checkin_bp.route('/<int:id>/delete', methods=['POST'])
@manager_required
def delete_checkin(id):
    checkin = EmployeeCheckIn.query.get_or_404(id)
    employee = Employee.query.get_or_404(checkin.employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar los fichajes de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    employee_id = checkin.employee_id
    db.session.delete(checkin)
    db.session.commit()
    
    log_activity(f'Fichaje eliminado para {employee.first_name} {employee.last_name}')
    flash('Fichaje eliminado correctamente.', 'success')
    return redirect(url_for('checkin.list_checkins', employee_id=employee_id))

@checkin_bp.route('/employee/<int:employee_id>/export', methods=['POST'])
@login_required
def export_checkins(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to view this employee
    if not can_view_employee(employee):
        flash('No tienes permiso para exportar los fichajes de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = ExportCheckInsForm()
    
    if form.validate_on_submit():
        start_date = form.start_date.data
        end_date = form.end_date.data
        
        # Generate PDF
        pdf_file = generate_checkins_pdf(employee, start_date, end_date)
        
        if not pdf_file:
            flash('No se han podido generar el PDF de fichajes.', 'warning')
            return redirect(url_for('checkin.list_checkins', employee_id=employee_id))
        
        # Prepare filename for download
        filename = f"fichajes_{employee.first_name.lower()}_{employee.last_name.lower()}_{start_date.strftime('%Y%m%d')}_{end_date.strftime('%Y%m%d')}.pdf"
        
        log_activity(f'Exportados fichajes a PDF para {employee.first_name} {employee.last_name}')
        
        # Send the file to the user
        return send_file(pdf_file, as_attachment=True, download_name=filename, mimetype='application/pdf')
    
    flash('Error al validar el formulario.', 'danger')
    return redirect(url_for('checkin.list_checkins', employee_id=employee_id))

@checkin_bp.route('/employee/<int:employee_id>/delete-by-date', methods=['GET', 'POST'])
@manager_required
def delete_checkins_by_date(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar los fichajes de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = ExportCheckInsForm()  # Reutilizamos el formulario de exportación para las fechas
    form.submit.label.text = 'Eliminar Fichajes'  # Cambiamos el texto del botón
    
    if form.validate_on_submit():
        start_date = form.start_date.data
        end_date = form.end_date.data
        
        if not start_date or not end_date:
            flash('Debe seleccionar fechas de inicio y fin.', 'warning')
            return render_template('delete_checkins_form.html', 
                                title=f'Eliminar Fichajes de {employee.first_name} {employee.last_name}', 
                                form=form,
                                employee=employee)
        
        # Crear objetos datetime para la comparación
        start_datetime = datetime.combine(start_date, datetime.min.time())
        end_datetime = datetime.combine(end_date, datetime.max.time())
        
        # Buscar fichajes en el rango de fechas
        checkins_to_delete = EmployeeCheckIn.query.filter(
            EmployeeCheckIn.employee_id == employee_id,
            EmployeeCheckIn.check_in_time >= start_datetime,
            EmployeeCheckIn.check_in_time <= end_datetime
        ).all()
        
        if not checkins_to_delete:
            flash('No se encontraron fichajes en el periodo seleccionado.', 'warning')
        else:
            count = len(checkins_to_delete)
            for checkin in checkins_to_delete:
                db.session.delete(checkin)
            db.session.commit()
            
            log_activity(f'Eliminados {count} fichajes para {employee.first_name} {employee.last_name}')
            flash(f'Se han eliminado {count} fichajes correctamente.', 'success')
        
        return redirect(url_for('checkin.list_checkins', employee_id=employee_id))
    
    return render_template('delete_checkins_form.html', 
                          title=f'Eliminar Fichajes de {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee)

@checkin_bp.route('/employee/<int:employee_id>/generate', methods=['GET', 'POST'])
@manager_required
def generate_checkins(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para generar fichajes para este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Check if employee has schedules
    if not employee.schedules:
        flash('El empleado no tiene horarios definidos. Por favor, defina los horarios primero.', 'warning')
        return redirect(url_for('schedule.list_schedules', employee_id=employee_id))
    
    form = GenerateCheckInsForm()
    
    if form.validate_on_submit():
        start_date = form.start_date.data
        end_date = form.end_date.data
        
        # Generate check-ins
        check_ins = EmployeeCheckIn.generate_check_ins_for_schedule(employee, start_date, end_date)
        
        if not check_ins:
            flash('No se han podido generar fichajes para el periodo seleccionado.', 'warning')
        else:
            # Save check-ins to database
            for check_in in check_ins:
                db.session.add(check_in)
            db.session.commit()
            
            log_activity(f'Fichajes generados para {employee.first_name} {employee.last_name}')
            flash(f'Se han generado {len(check_ins)} fichajes correctamente.', 'success')
        
        return redirect(url_for('checkin.list_checkins', employee_id=employee_id))
    
    return render_template('generate_checkins_form.html', 
                          title=f'Generar Fichajes para {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee)

# Vacations routes
@vacation_bp.route('/employee/<int:employee_id>')
@login_required
def list_vacations(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to view this employee
    if not can_view_employee(employee):
        flash('No tienes permiso para ver las vacaciones de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    vacations = EmployeeVacation.query.filter_by(employee_id=employee_id).order_by(EmployeeVacation.start_date.desc()).all()
    
    return render_template('vacation_list.html', 
                          title=f'Vacaciones de {employee.first_name} {employee.last_name}', 
                          employee=employee,
                          vacations=vacations)

@vacation_bp.route('/employee/<int:employee_id>/new', methods=['GET', 'POST'])
@login_required
def create_vacation(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to view this employee
    if not (can_manage_employee(employee) or 
            (current_user.is_empleado() and current_user.employee and current_user.employee.id == employee_id)):
        flash('No tienes permiso para gestionar las vacaciones de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeVacationForm()
    
    if form.validate_on_submit():
        # Check if the vacation period overlaps with existing vacations
        overlapping_vacations = EmployeeVacation.query.filter_by(employee_id=employee_id).all()
        for v in overlapping_vacations:
            if v.overlaps_with(form.start_date.data, form.end_date.data):
                flash('El periodo de vacaciones se solapa con otro existente.', 'danger')
                return render_template('vacation_form.html', 
                                      title=f'Nuevas Vacaciones para {employee.first_name} {employee.last_name}', 
                                      form=form,
                                      employee=employee)
        
        # Simplificado: siempre REGISTRADA
        status = VacationStatus.REGISTRADA
        
        vacation = EmployeeVacation(
            start_date=form.start_date.data,
            end_date=form.end_date.data,
            status=status,
            notes=form.notes.data,
            employee_id=employee_id
        )
        
        # Ya no se requiere asignar aprobador en el flujo simplificado
        
        db.session.add(vacation)
        db.session.commit()
        
        log_activity(f'Vacaciones creadas para {employee.first_name} {employee.last_name}')
        flash('Vacaciones creadas correctamente.', 'success')
        return redirect(url_for('vacation.list_vacations', employee_id=employee_id))
    
    return render_template('vacation_form.html', 
                          title=f'Nuevas Vacaciones para {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee)

# Se eliminó la ruta de aprobación de vacaciones, ya no es necesaria con el flujo simplificado

@vacation_bp.route('/<int:id>/delete', methods=['POST'])
@manager_required
def delete_vacation(id):
    vacation = EmployeeVacation.query.get_or_404(id)
    employee = Employee.query.get_or_404(vacation.employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar vacaciones de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    employee_id = vacation.employee_id
    db.session.delete(vacation)
    db.session.commit()
    
    log_activity(f'Vacaciones eliminadas para {employee.first_name} {employee.last_name}')
    flash('Vacaciones eliminadas correctamente.', 'success')
    return redirect(url_for('vacation.list_vacations', employee_id=employee_id))

# Ruta para limpiar la base de datos (solo administradores)
@main_bp.route('/clean-database', methods=['GET', 'POST'])
@admin_required
def clean_database_route():
    if request.method == 'POST':
        # Verificar la confirmación del formulario
        confirmation = request.form.get('confirmation')
        
        if confirmation == 'CONFIRMAR BORRAR TODO':
            # Ejecutar la limpieza de base de datos
            result = clean_database(confirm=True)
            
            if result['success']:
                log_activity('Base de datos limpiada completamente')
                flash('Base de datos limpiada completamente. Se han eliminado todos los registros.', 'success')
            else:
                flash(f'Error al limpiar la base de datos: {result.get("message", "Error desconocido")}', 'danger')
                
            return redirect(url_for('main.dashboard'))
        else:
            flash('La confirmación no es correcta. La base de datos NO ha sido limpiada.', 'warning')
    
    return render_template('clean_database.html', title='Limpiar Base de Datos')

# Ruta para exportar datos de todas las empresas (para el panel de backup)
@company_bp.route('/export/all', methods=['GET'])
@login_required
@admin_required
def export_all_companies():
    """Export all companies data to Excel file"""
    from io import BytesIO
    from openpyxl import Workbook
    from openpyxl.styles import Font, PatternFill
    from openpyxl.utils import get_column_letter
    
    # Obtener todas las empresas
    companies = Company.query.all()
    
    if not companies:
        flash('No hay empresas para exportar.', 'info')
        return redirect(url_for('main.dashboard'))
    
    # Crear archivo Excel
    output = BytesIO()
    workbook = Workbook()
    
    # Hoja de empresas
    companies_sheet = workbook.active
    companies_sheet.title = "Empresas"
    
    # Encabezados para empresas
    company_headers = ["ID", "Nombre", "CIF/NIF", "Dirección", "Ciudad", "Código Postal", 
                      "País", "Sector", "Teléfono", "Email", "Sitio Web", "Activa", 
                      "Fecha de Creación", "Última Actualización"]
    
    for col_num, header in enumerate(company_headers, 1):
        col_letter = get_column_letter(col_num)
        companies_sheet[f'{col_letter}1'] = header
        companies_sheet[f'{col_letter}1'].font = Font(bold=True)
        companies_sheet[f'{col_letter}1'].fill = PatternFill(start_color="DDDDDD", end_color="DDDDDD", fill_type="solid")
    
    # Datos de empresas
    for row_num, company in enumerate(companies, 2):
        created_at = company.created_at.strftime('%d-%m-%Y %H:%M') if company.created_at else ''
        updated_at = company.updated_at.strftime('%d-%m-%Y %H:%M') if company.updated_at else ''
        
        row = [
            company.id,
            company.name,
            company.tax_id,
            company.address,
            company.city,
            company.postal_code,
            company.country,
            company.sector,
            company.phone,
            company.email,
            company.website,
            "Sí" if company.is_active else "No",
            created_at,
            updated_at
        ]
        
        for col_num, cell_value in enumerate(row, 1):
            col_letter = get_column_letter(col_num)
            companies_sheet[f'{col_letter}{row_num}'] = cell_value
    
    # Ajustar anchos de columna
    for column in companies_sheet.columns:
        max_length = 0
        column_letter = column[0].column_letter
        for cell in column:
            if cell.value:
                max_length = max(max_length, len(str(cell.value)))
        adjusted_width = (max_length + 2)
        companies_sheet.column_dimensions[column_letter].width = adjusted_width
    
    # Hoja de empleados
    employees_sheet = workbook.create_sheet(title="Empleados")
    
    # Encabezados para empleados
    employee_headers = ["ID", "Empresa", "Nombre", "Apellidos", "DNI/NIE", "Email", 
                       "Teléfono", "Dirección", "Fecha Nacimiento", "Fecha Inicio", 
                       "Fecha Fin", "Posición", "Tipo Contrato", "Estado"]
    
    for col_num, header in enumerate(employee_headers, 1):
        col_letter = get_column_letter(col_num)
        employees_sheet[f'{col_letter}1'] = header
        employees_sheet[f'{col_letter}1'].font = Font(bold=True)
        employees_sheet[f'{col_letter}1'].fill = PatternFill(start_color="DDDDDD", end_color="DDDDDD", fill_type="solid")
    
    # Obtener todos los empleados
    employees = Employee.query.all()
    
    # Datos de empleados
    for row_num, employee in enumerate(employees, 2):
        company_name = employee.company.name if employee.company else "No asignada"
        contract_type = employee.contract_type.name if employee.contract_type else "No especificado"
        status_name = employee.status.name if employee.status else "No especificado"
        
        row = [
            employee.id,
            company_name,
            employee.first_name,
            employee.last_name,
            employee.identity_document,
            employee.email,
            employee.phone,
            employee.address,
            employee.birth_date,
            employee.start_date,
            employee.end_date,
            employee.position,
            contract_type,
            status_name
        ]
        
        for col_num, cell_value in enumerate(row, 1):
            col_letter = get_column_letter(col_num)
            employees_sheet[f'{col_letter}{row_num}'] = cell_value
    
    # Ajustar anchos de columna
    for column in employees_sheet.columns:
        max_length = 0
        column_letter = column[0].column_letter
        for cell in column:
            if cell.value:
                max_length = max(max_length, len(str(cell.value)))
        adjusted_width = (max_length + 2)
        employees_sheet.column_dimensions[column_letter].width = adjusted_width
    
    workbook.save(output)
    output.seek(0)
    
    # Generar nombre de archivo
    now = datetime.now().strftime('%Y%m%d_%H%M%S')
    filename = f"datos_empresas_empleados_{now}.xlsx"
    
    # Registrar actividad
    log_activity('Exportados datos de todas las empresas y empleados a Excel')
    
    return send_file(
        output,
        mimetype='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        as_attachment=True,
        download_name=filename
    )

# Ruta para exportar todos los fichajes (para el panel de backup)
@checkin_bp.route('/export/all', methods=['GET'])
@login_required
@admin_required
def export_all_checkins():
    """Export all check-ins to Excel file"""
    from io import BytesIO
    from openpyxl import Workbook
    from openpyxl.styles import Font, PatternFill
    from openpyxl.utils import get_column_letter
    
    # Get all checkins, join with employee to get names
    query = db.session.query(EmployeeCheckIn, Employee)\
        .join(Employee, EmployeeCheckIn.employee_id == Employee.id)\
        .order_by(EmployeeCheckIn.check_in_time.desc())
    
    # If not admin, filter by companies the user has access to
    if not current_user.is_admin():
        company_ids = [c.id for c in current_user.companies]
        query = query.filter(Employee.company_id.in_(company_ids))
    
    results = query.all()
    
    if not results:
        flash('No hay fichajes para exportar.', 'info')
        return redirect(url_for('main.dashboard'))
    
    # Create Excel file
    output = BytesIO()
    workbook = Workbook()
    worksheet = workbook.active
    worksheet.title = "Todos los Fichajes"
    
    # Headers
    headers = ["ID", "Empleado", "Empresa", "Fecha", "Hora Entrada", "Hora Salida", "Duración", "Estado", "Notas"]
    for col_num, header in enumerate(headers, 1):
        col_letter = get_column_letter(col_num)
        worksheet[f'{col_letter}1'] = header
        # Apply style to header
        worksheet[f'{col_letter}1'].font = Font(bold=True)
        worksheet[f'{col_letter}1'].fill = PatternFill(start_color="DDDDDD", end_color="DDDDDD", fill_type="solid")
    
    # Data
    for row_num, (checkin, employee) in enumerate(results, 2):
        check_in_date = checkin.check_in_time.strftime('%d-%m-%Y') if checkin.check_in_time else ''
        check_in_time = checkin.check_in_time.strftime('%H:%M:%S') if checkin.check_in_time else ''
        check_out_time = checkin.check_out_time.strftime('%H:%M:%S') if checkin.check_out_time else ''
        
        duration = ''
        if checkin.check_in_time and checkin.check_out_time:
            duration_seconds = (checkin.check_out_time - checkin.check_in_time).total_seconds()
            hours, remainder = divmod(duration_seconds, 3600)
            minutes, seconds = divmod(remainder, 60)
            duration = f"{int(hours):02d}:{int(minutes):02d}:{int(seconds):02d}"
        
        status = "Completo" if checkin.check_out_time else "Pendiente"
        
        # Get company name
        company_name = employee.company.name if employee.company else "No asignada"
        
        row = [
            checkin.id,
            f"{employee.first_name} {employee.last_name}",
            company_name,
            check_in_date,
            check_in_time,
            check_out_time,
            duration,
            status,
            checkin.notes or ''
        ]
        
        for col_num, cell_value in enumerate(row, 1):
            col_letter = get_column_letter(col_num)
            worksheet[f'{col_letter}{row_num}'] = cell_value
    
    # Adjust column widths
    for column in worksheet.columns:
        max_length = 0
        column_letter = column[0].column_letter
        for cell in column:
            if cell.value:
                max_length = max(max_length, len(str(cell.value)))
        adjusted_width = (max_length + 2)
        worksheet.column_dimensions[column_letter].width = adjusted_width
    
    workbook.save(output)
    output.seek(0)
    
    # Generate filename
    now = datetime.now().strftime('%Y%m%d_%H%M%S')
    filename = f"todos_los_fichajes_{now}.xlsx"
    
    # Log activity
    log_activity('Exportados todos los fichajes a Excel')
    
    return send_file(
        output,
        mimetype='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        as_attachment=True,
        download_name=filename
    )

from datetime import datetime
from flask import session, current_app
from app import db
from models import Employee
from models_tasks import LocalUser, Location
import random
import string

def create_default_local_user():
    """Crea un usuario local por defecto si no existe ninguno para la ubicación."""
    # Comprobamos si existen ubicaciones
    locations = Location.query.filter_by(is_active=True).all()
    if not locations:
        return None
    
    # Para cada ubicación, comprobamos si tiene usuarios locales
    for location in locations:
        local_users = LocalUser.query.filter_by(location_id=location.id).count()
        
        if local_users == 0:
            # Crear usuario admin por defecto para esta ubicación
            default_user = LocalUser(
                name="Admin",
                last_name="Local",
                username=f"admin_{location.id}",
                pin="1234",  # Se establecerá el hash más adelante
                is_active=True,
                location_id=location.id,
                created_at=datetime.utcnow(),
                updated_at=datetime.utcnow()
            )
            
            # Establecer el PIN real
            default_user.set_pin("1234")
            
            db.session.add(default_user)
            db.session.commit()
            
            return default_user
    
    return None

def get_portal_session():
    """Obtiene información de la sesión del portal."""
    return {
        'location_id': session.get('portal_location_id'),
        'location_name': session.get('portal_location_name'),
        'user_id': session.get('local_user_id'),
        'user_name': session.get('local_user_name')
    }
    
def clear_portal_session():
    """Limpia la sesión del portal."""
    if 'portal_location_id' in session:
        session.pop('portal_location_id')
    if 'portal_location_name' in session:
        session.pop('portal_location_name')
    if 'local_user_id' in session:
        session.pop('local_user_id')
    if 'local_user_name' in session:
        session.pop('local_user_name')
        
def generate_secure_password(location_id=None):
    """Genera una contraseña segura con el formato estandarizado 'Portal[ID]2025!'."""
    if location_id:
        # Utilizamos un formato estándar para facilitar recordar la contraseña
        # Formato: Portal[ID]2025!
        return f"Portal{location_id}2025!"
    else:
        # Si no se proporciona ID, generamos una contraseña aleatoria más compleja
        # Asegurarnos de incluir al menos: una mayúscula, una minúscula, un número y un carácter especial
        uppercase = random.choice(string.ascii_uppercase)
        lowercase = random.choice(string.ascii_lowercase)
        digit = random.choice(string.digits)
        special = random.choice("!@#$%&*")
        
        # El resto de caracteres aleatorios
        remaining_length = 8  # Longitud total 12
        all_chars = string.ascii_letters + string.digits + "!@#$%&*"
        rest = ''.join(random.choice(all_chars) for _ in range(remaining_length))
        
        # Combinar todos los caracteres y mezclar
        password = uppercase + lowercase + digit + special + rest
        password_list = list(password)
        random.shuffle(password_list)
        
        return ''.join(password_list)

def regenerate_portal_password(location_id, only_return=False):
    """Regenera y actualiza la contraseña del portal de una ubicación.
    
    Args:
        location_id: ID de la ubicación
        only_return: Si es True, solo devuelve la contraseña actual sin regenerarla
    
    Returns:
        La contraseña actual o la nueva contraseña regenerada
    """
    try:
        location = Location.query.get(location_id)
        if not location:
            return None
            
        # Utilizamos un formato fijo para las contraseñas del portal
        # Formato estandarizado: Portal[ID]2025!
        # Esto permite que siempre podamos recuperar la contraseña sin almacenarla en texto plano
        fixed_password = generate_secure_password(location_id)
            
        if only_return:
            # Solo devolvemos la contraseña actual sin cambiar nada
            # Como usamos un formato fijo, siempre podemos reconstruirla
            return fixed_password
            
        # Actualizamos la contraseña en la base de datos
        location.set_portal_password(fixed_password)
        
        db.session.commit()
        return fixed_password
    except Exception as e:
        if not only_return:  # Solo hacemos rollback si estábamos modificando la BD
            db.session.rollback()
        print(f"Error al manipular contraseña: {str(e)}")
        return None
        
def count_available_employees(location_id):
    """
    Cuenta cuántos empleados de la empresa asociada a la ubicación están
    disponibles para ser sincronizados (no tienen un usuario local asociado).
    
    Args:
        location_id: ID de la ubicación
        
    Returns:
        Número de empleados disponibles para sincronización
    """
    try:
        # Obtenemos la ubicación para verificar su empresa
        location = Location.query.get(location_id)
        if not location:
            return 0
            
        # Obtenemos IDs de los empleados que ya están importados como usuarios locales
        imported_employee_ids = db.session.query(LocalUser.employee_id).filter(
            LocalUser.location_id == location_id,
            LocalUser.employee_id.isnot(None)
        ).all()
        
        # Convertir lista de tuplas a lista simple
        imported_ids = [item[0] for item in imported_employee_ids if item[0] is not None]
        
        # Consultar empleados activos de la empresa que no estén en la lista de importados
        company_id = location.company_id
        query = Employee.query.filter(
            Employee.company_id == company_id,
            Employee.is_active == True,
            ~Employee.id.in_(imported_ids) if imported_ids else True
        )
        
        # Contar empleados disponibles
        available_count = query.count()
        return available_count
        
    except Exception as e:
        current_app.logger.error(f"Error al contar empleados disponibles: {str(e)}")
        return 0
        
def sync_employees_to_local_users(location_id):
    """
    Sincroniza empleados de la empresa como usuarios locales.
    
    Args:
        location_id: ID de la ubicación
        
    Returns:
        Tupla con (total_empleados, creados, actualizados)
    """
    try:
        # Obtenemos la ubicación para verificar su empresa
        location = Location.query.get(location_id)
        if not location:
            raise ValueError(f"Ubicación no encontrada: {location_id}")
        
        company_id = location.company_id
        
        # Obtener IDs de empleados ya importados
        imported_employees = LocalUser.query.filter(
            LocalUser.location_id == location_id,
            LocalUser.employee_id.isnot(None)
        ).all()
        
        imported_ids = {user.employee_id: user for user in imported_employees if user.employee_id is not None}
        
        # Consultar todos los empleados activos de la empresa
        employees = Employee.query.filter_by(
            company_id=company_id,
            is_active=True
        ).all()
        
        if not employees:
            return 0, 0, 0
            
        total_created = 0
        total_updated = 0
        
        for employee in employees:
            # Si el empleado ya tiene un usuario local, actualizamos sus datos
            if employee.id in imported_ids:
                local_user = imported_ids[employee.id]
                # Actualizamos los datos básicos
                local_user.name = employee.first_name
                local_user.last_name = employee.last_name
                # La foto la dejamos como está en LocalUser
                # El PIN lo dejamos como está
                local_user.updated_at = datetime.utcnow()
                total_updated += 1
            else:
                # Crear nuevo usuario local vinculado al empleado
                username = f"{employee.first_name.lower()}_{employee.last_name.lower()}".replace(" ", "_")
                
                # Usar los últimos 4 dígitos del DNI como PIN
                # Si no hay DNI o no tiene al menos 4 caracteres, usar PIN por defecto
                if employee.dni and len(employee.dni) >= 4:
                    pin = employee.dni[-4:]
                    # Verificar que sean dígitos, si no, usar PIN por defecto
                    if not pin.isdigit():
                        pin = '1234'
                else:
                    pin = '1234'
                
                local_user = LocalUser(
                    name=employee.first_name,
                    last_name=employee.last_name,
                    username=username,
                    is_active=True,
                    imported=True,
                    location_id=location_id,
                    employee_id=employee.id,
                    created_at=datetime.utcnow(),
                    updated_at=datetime.utcnow()
                )
                
                # Establecer el PIN hasheado
                local_user.set_pin(pin)
                
                db.session.add(local_user)
                total_created += 1
                
                # Registrar la creación para fines de auditoría
                current_app.logger.info(f"Usuario {username} creado con PIN: {pin}")
                
        # Guardar cambios en la base de datos
        db.session.commit()
        
        return len(employees), total_created, total_updated
        
    except Exception as e:
        db.session.rollback()
        current_app.logger.error(f"Error en sincronización de empleados: {str(e)}")
        raise
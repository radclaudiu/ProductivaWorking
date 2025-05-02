from functools import wraps
from flask import abort, flash, redirect, url_for
from flask_login import current_user

def admin_required(f):
    """Decorator para requerir que el usuario sea administrador"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or not current_user.is_admin():
            flash('Se requieren permisos de administrador para acceder a esta página.', 'danger')
            return abort(403)
        return f(*args, **kwargs)
    return decorated_function

def gerente_required(f):
    """Decorator para requerir que el usuario sea gerente o administrador"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated:
            flash('Debe iniciar sesión para acceder a esta página.', 'warning')
            return redirect(url_for('auth.login'))
            
        if not (current_user.is_admin() or current_user.is_gerente()):
            flash('Se requieren permisos de gerente para acceder a esta página.', 'danger')
            return abort(403)
            
        return f(*args, **kwargs)
    return decorated_function

def company_access_required(f):
    """Decorator para verificar acceso a una empresa específica"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        company_id = kwargs.get('company_id')
        
        if not company_id:
            flash('ID de empresa no proporcionado.', 'danger')
            return abort(400)
            
        if not current_user.is_authenticated:
            flash('Debe iniciar sesión para acceder a esta página.', 'warning')
            return redirect(url_for('auth.login'))
            
        # Administradores tienen acceso a todas las empresas
        if current_user.is_admin():
            return f(*args, **kwargs)
            
        # Gerentes solo pueden acceder a empresas asignadas
        if current_user.is_gerente():
            # Verificar si la empresa está en la lista de empresas del usuario
            if any(c.id == int(company_id) for c in current_user.companies):
                return f(*args, **kwargs)
        
        flash('No tiene permiso para acceder a esta empresa.', 'danger')
        return abort(403)
        
    return decorated_function

def location_access_required(f):
    """Decorator para verificar acceso a una ubicación específica"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        from models_tasks import Location
        
        location_id = kwargs.get('location_id')
        
        if not location_id:
            flash('ID de ubicación no proporcionado.', 'danger')
            return abort(400)
            
        if not current_user.is_authenticated:
            flash('Debe iniciar sesión para acceder a esta página.', 'warning')
            return redirect(url_for('auth.login'))
        
        # Administradores tienen acceso a todas las ubicaciones
        if current_user.is_admin():
            return f(*args, **kwargs)
            
        # Verificar si la ubicación pertenece a una empresa asignada al usuario
        if current_user.is_gerente():
            try:
                # Obtener la ubicación
                location = Location.query.get(location_id)
                if not location:
                    flash('Ubicación no encontrada.', 'danger')
                    return abort(404)
                    
                # Verificar si la empresa está en la lista de empresas del usuario
                if any(c.id == location.company_id for c in current_user.companies):
                    return f(*args, **kwargs)
            except Exception as e:
                flash(f'Error al verificar acceso: {str(e)}', 'danger')
                return abort(500)
        
        flash('No tiene permiso para acceder a esta ubicación.', 'danger')
        return abort(403)
        
    return decorated_function
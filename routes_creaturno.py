"""
Módulo de rutas para la integración de CreaTurno con Productiva.
Este módulo define las rutas para acceder al módulo de CreaTurno desde Productiva.
"""

import os
import logging
import subprocess
from flask import Blueprint, render_template, redirect, url_for, flash, current_app, request, jsonify
from flask_login import login_required, current_user

# Configurar logging
logger = logging.getLogger(__name__)

# Crear Blueprint
creaturno_bp = Blueprint('creaturno', __name__, url_prefix='/creaturno')

# Variable para guardar el proceso del servidor de CreaTurno
creaturno_process = None

def check_creaturno_tables():
    """
    Verifica si las tablas de CreaTurno están creadas.
    Si no existen, las crea.
    """
    try:
        from init_creaturno_tables import init_creaturno_tables
        
        # Verificar si la tabla shifts existe
        from app import db
        from sqlalchemy import text
        
        # Consultar si existe la tabla shifts
        result = db.session.execute(text("""
            SELECT to_regclass('shifts');
        """))
        
        table_exists = result.scalar() is not None
        
        if not table_exists:
            logger.info("Tablas de CreaTurno no encontradas. Creando tablas...")
            success = init_creaturno_tables()
            if success:
                logger.info("Tablas de CreaTurno creadas correctamente")
                return True
            else:
                logger.error("Error al crear tablas de CreaTurno")
                return False
        else:
            logger.info("Tablas de CreaTurno ya existen")
            return True
            
    except Exception as e:
        logger.error(f"Error al verificar tablas de CreaTurno: {str(e)}")
        return False

def start_creaturno_server():
    """
    Inicia el servidor de CreaTurno si no está en ejecución.
    """
    global creaturno_process
    
    try:
        # Verificar si el proceso está en ejecución
        if creaturno_process and creaturno_process.poll() is None:
            logger.info("Servidor de CreaTurno ya está en ejecución")
            return True
        
        # Iniciar el servidor de CreaTurno en segundo plano
        env = os.environ.copy()
        env["DATABASE_URL"] = os.environ.get("DATABASE_URL")
        env["SESSION_SECRET"] = os.environ.get("SESSION_SECRET", "secret_key_for_creaturno")
        
        # Usar tsx para ejecutar el servidor de CreaTurno de forma directa
        cmd = ["npx", "tsx", "server/index_productiva.ts"]
        
        # Iniciar el proceso
        creaturno_process = subprocess.Popen(
            cmd,
            cwd="./CreaTurno",
            env=env,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        
        logger.info(f"Servidor de CreaTurno iniciado con PID {creaturno_process.pid}")
        return True
        
    except Exception as e:
        logger.error(f"Error al iniciar servidor de CreaTurno: {str(e)}")
        return False

def stop_creaturno_server():
    """
    Detiene el servidor de CreaTurno si está en ejecución.
    """
    global creaturno_process
    
    if creaturno_process and creaturno_process.poll() is None:
        try:
            creaturno_process.terminate()
            creaturno_process.wait(timeout=5)
            logger.info("Servidor de CreaTurno detenido correctamente")
            return True
        except Exception as e:
            logger.error(f"Error al detener servidor de CreaTurno: {str(e)}")
            try:
                creaturno_process.kill()
                logger.info("Servidor de CreaTurno forzado a detenerse")
            except:
                pass
            return False
    
    return True

@creaturno_bp.route('/')
@login_required
def index():
    """
    Página principal del módulo CreaTurno.
    Inicia el servidor y verifica las tablas si es necesario.
    """
    # Verificar que existan las tablas necesarias
    if not check_creaturno_tables():
        flash("Error al verificar tablas de CreaTurno. No se puede continuar.", "danger")
        return redirect(url_for('main.index'))
    
    # Iniciar el servidor de CreaTurno si no está en ejecución
    if not start_creaturno_server():
        flash("Error al iniciar el servidor de CreaTurno. No se puede continuar.", "danger")
        return redirect(url_for('main.index'))
    
    # Pasar información del usuario actual
    user_info = {
        'id': current_user.id,
        'username': current_user.username,
        'full_name': f"{current_user.first_name} {current_user.last_name}",
        'email': current_user.email,
        'is_admin': current_user.is_admin()
    }
    
    # Renderizar la plantilla que muestra la aplicación de CreaTurno
    # El cliente de CreaTurno se cargará en un iframe
    return render_template(
        'creaturno/index.html',
        title='CreaTurno - Gestión de Turnos',
        user_info=user_info
    )

@creaturno_bp.route('/admin')
@login_required
def admin():
    """
    Panel de administración de CreaTurno.
    """
    # Verificar que el usuario sea administrador
    if not current_user.is_admin():
        flash("No tiene permisos para acceder a esta página", "danger")
        return redirect(url_for('creaturno.index'))
    
    # Estado del servidor
    server_status = "En ejecución" if creaturno_process and creaturno_process.poll() is None else "Detenido"
    
    return render_template(
        'creaturno/admin.html',
        title='Administración de CreaTurno',
        server_status=server_status
    )

@creaturno_bp.route('/start_server', methods=['POST'])
@login_required
def start_server():
    """
    Inicia el servidor de CreaTurno manualmente.
    """
    # Verificar que el usuario sea administrador
    if not current_user.is_admin():
        return jsonify({'success': False, 'message': 'No tiene permisos para realizar esta acción'})
    
    success = start_creaturno_server()
    
    return jsonify({
        'success': success,
        'message': 'Servidor iniciado correctamente' if success else 'Error al iniciar el servidor'
    })

@creaturno_bp.route('/stop_server', methods=['POST'])
@login_required
def stop_server():
    """
    Detiene el servidor de CreaTurno manualmente.
    """
    # Verificar que el usuario sea administrador
    if not current_user.is_admin():
        return jsonify({'success': False, 'message': 'No tiene permisos para realizar esta acción'})
    
    success = stop_creaturno_server()
    
    return jsonify({
        'success': success,
        'message': 'Servidor detenido correctamente' if success else 'Error al detener el servidor'
    })

@creaturno_bp.route('/status', methods=['GET'])
@login_required
def server_status():
    """
    Obtiene el estado del servidor de CreaTurno.
    """
    status = "running" if creaturno_process and creaturno_process.poll() is None else "stopped"
    
    return jsonify({
        'status': status,
        'pid': creaturno_process.pid if creaturno_process else None
    })
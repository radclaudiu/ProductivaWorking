"""
Rutas para el módulo CreaTurno integrado con Productiva.

Este módulo maneja la redirección a la interfaz de CreaTurno y la inicialización
del servidor TypeScript que ejecuta la lógica de CreaTurno.
"""

import os
import subprocess
import threading
import time
from flask import Blueprint, render_template, redirect, url_for, flash, session, jsonify, current_app
from flask_login import login_required, current_user
from datetime import datetime
import logging

# Configurar logging
logger = logging.getLogger(__name__)

# Crear blueprint
creaturno_bp = Blueprint('creaturno', __name__, url_prefix='/creaturno')

# Variable global para el proceso del servidor
creaturno_server_process = None

def init_creaturno_tables():
    """
    Inicializa las tablas de CreaTurno en la base de datos si no existen.
    Esta función usa directamente SQL en lugar de migraciones.
    
    Returns:
        bool: True si las tablas ya existen o se crearon correctamente, False en caso contrario.
    """
    from app import db
    from sqlalchemy import text
    
    try:
        # Verificar si las tablas ya existen
        result = db.session.execute(text("""
            SELECT EXISTS (
                SELECT FROM information_schema.tables 
                WHERE table_name = 'creaturno_shift_roles'
            );
        """))
        tables_exist = result.scalar()
        
        if tables_exist:
            logger.info("Tablas de CreaTurno ya existen")
            return True
            
        # Crear las tablas necesarias
        logger.info("Creando tablas de CreaTurno...")
        
        # Ruta al archivo SQL
        sql_file_path = os.path.join('CreaTurno', 'create_tables_productiva.sql')
        
        # Verificar si el archivo existe
        if not os.path.exists(sql_file_path):
            logger.error(f"Archivo SQL de creación de tablas no encontrado: {sql_file_path}")
            return False
            
        # Leer y ejecutar el archivo SQL
        with open(sql_file_path, 'r') as f:
            sql_commands = f.read()
            
        # Ejecutar los comandos SQL
        db.session.execute(text(sql_commands))
        db.session.commit()
        
        logger.info("Tablas de CreaTurno creadas correctamente")
        return True
        
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al crear tablas de CreaTurno: {str(e)}")
        return False

def start_creaturno_server():
    """
    Inicia el servidor de CreaTurno en segundo plano.
    
    Returns:
        bool: True si el servidor se inició correctamente, False en caso contrario.
    """
    global creaturno_server_process
    
    try:
        # Verificar si el servidor ya está en ejecución
        if creaturno_server_process is not None and creaturno_server_process.poll() is None:
            logger.info("El servidor de CreaTurno ya está en ejecución")
            return True
            
        # Ruta al directorio de CreaTurno
        creaturno_dir = os.path.join(os.getcwd(), 'CreaTurno')
        
        # Comando para iniciar el servidor
        node_executable = os.path.join(os.getcwd(), 'node_modules', '.bin', 'tsx')
        server_script = os.path.join(creaturno_dir, 'server', 'index_productiva.ts')
        
        # Verificar si los archivos existen
        if not os.path.exists(server_script):
            logger.error(f"Script del servidor no encontrado: {server_script}")
            return False
            
        # Iniciar el servidor en un nuevo proceso
        if os.path.exists(node_executable):
            cmd = [node_executable, server_script]
        else:
            # Intentar con npx si tsx no está instalado directamente
            cmd = ['node', server_script]
            
        # Configure environment variables
        env = os.environ.copy()
        env['DATABASE_URL'] = current_app.config['SQLALCHEMY_DATABASE_URI']
        
        logger.info(f"Iniciando servidor de CreaTurno con comando: {' '.join(cmd)}")
        
        creaturno_server_process = subprocess.Popen(
            cmd,
            cwd=creaturno_dir,
            env=env,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            universal_newlines=True
        )
        
        # Esperar un momento para ver si el servidor inicia correctamente
        time.sleep(2)
        
        # Verificar si el proceso sigue en ejecución
        if creaturno_server_process.poll() is None:
            logger.info("Servidor de CreaTurno iniciado correctamente")
            
            # Iniciar un hilo para capturar la salida del servidor
            def log_output():
                for line in creaturno_server_process.stdout:
                    logger.info(f"CreaTurno Server: {line.strip()}")
                for line in creaturno_server_process.stderr:
                    logger.error(f"CreaTurno Server Error: {line.strip()}")
            
            threading.Thread(target=log_output, daemon=True).start()
            
            return True
        else:
            # El proceso terminó rápidamente, lo que indica un error
            stdout, stderr = creaturno_server_process.communicate()
            logger.error(f"Error al iniciar servidor de CreaTurno: {stderr}")
            creaturno_server_process = None
            return False
            
    except Exception as e:
        logger.error(f"Error al iniciar servidor de CreaTurno: {str(e)}")
        return False

# Función para inicializar CreaTurno, será llamada desde app.py
def setup_creaturno():
    """Configuración inicial de CreaTurno."""
    if init_creaturno_tables():
        return start_creaturno_server()
    return False

@creaturno_bp.route('/')
@login_required
def index():
    """Página principal de CreaTurno."""
    try:
        # Asegurarse de que las tablas estén creadas y el servidor esté en ejecución
        if not init_creaturno_tables() or not start_creaturno_server():
            flash('Error al inicializar el módulo CreaTurno. Por favor, contacte con el administrador.', 'danger')
            return redirect(url_for('main.dashboard'))
            
        # Guardar información del usuario en la sesión para que CreaTurno pueda utilizarla
        session['user_id'] = current_user.id
        session['username'] = current_user.username
        session['role'] = current_user.role.name
        session['company_id'] = current_user.company_id if hasattr(current_user, 'company_id') else None
        
        # Renderizar la plantilla del cliente de CreaTurno
        return render_template('creaturno/index.html', title='CreaTurno')
        
    except Exception as e:
        logger.error(f"Error al cargar página de CreaTurno: {str(e)}")
        flash(f'Error al cargar CreaTurno: {str(e)}', 'danger')
        return redirect(url_for('main.dashboard'))

@creaturno_bp.route('/admin')
@login_required
def admin():
    """Página de administración de CreaTurno."""
    # Verificar si el usuario es administrador
    if not current_user.is_admin():
        flash('Acceso denegado. Se requieren permisos de administrador.', 'danger')
        return redirect(url_for('creaturno.index'))
        
    try:
        # Renderizar la plantilla de administración de CreaTurno
        return render_template('creaturno/admin.html', title='Administración de CreaTurno')
        
    except Exception as e:
        logger.error(f"Error al cargar página de administración de CreaTurno: {str(e)}")
        flash(f'Error al cargar administración de CreaTurno: {str(e)}', 'danger')
        return redirect(url_for('creaturno.index'))

@creaturno_bp.route('/status')
@login_required
def status():
    """Verificar el estado del servidor de CreaTurno."""
    global creaturno_server_process
    
    is_running = creaturno_server_process is not None and creaturno_server_process.poll() is None
    
    return jsonify({
        'status': 'running' if is_running else 'stopped',
        'timestamp': datetime.now().isoformat()
    })

@creaturno_bp.route('/restart')
@login_required
def restart():
    """Reiniciar el servidor de CreaTurno."""
    global creaturno_server_process
    
    # Verificar si el usuario es administrador
    if not current_user.is_admin():
        flash('Acceso denegado. Se requieren permisos de administrador.', 'danger')
        return redirect(url_for('creaturno.index'))
        
    try:
        # Detener el servidor si está en ejecución
        if creaturno_server_process is not None:
            creaturno_server_process.terminate()
            creaturno_server_process.wait(timeout=5)
            creaturno_server_process = None
            
        # Iniciar el servidor nuevamente
        if start_creaturno_server():
            flash('Servidor de CreaTurno reiniciado correctamente.', 'success')
        else:
            flash('Error al reiniciar el servidor de CreaTurno.', 'danger')
            
        return redirect(url_for('creaturno.admin'))
        
    except Exception as e:
        logger.error(f"Error al reiniciar servidor de CreaTurno: {str(e)}")
        flash(f'Error al reiniciar servidor: {str(e)}', 'danger')
        return redirect(url_for('creaturno.admin'))
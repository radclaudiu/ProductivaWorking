from flask import Blueprint, render_template, jsonify, request, current_app
from flask_login import login_required, current_user
import json
import os
from utils import log_activity
from decorators import admin_required

# Crear el blueprint para el módulo de ayuda
help_bp = Blueprint('help', __name__, url_prefix='/help')

@help_bp.route('/', methods=['GET'])
def help_index():
    """Página principal del centro de ayuda"""
    return render_template('help.html')

@help_bp.route('/api/update', methods=['POST'])
@login_required
@admin_required
def update_help_content():
    """API para actualizar contenido de ayuda (solo administradores)"""
    try:
        # Verificar que la petición contiene datos en formato JSON
        if not request.is_json:
            return jsonify({'success': False, 'error': 'La petición debe ser JSON'}), 400
            
        data = request.get_json()
        if not data or 'itemId' not in data or 'content' not in data:
            return jsonify({'success': False, 'error': 'Faltan datos requeridos (itemId o content)'}), 400
        
        item_id = data['itemId']
        content = data['content']
        
        # En una implementación real, aquí guardaríamos el contenido en la base de datos
        # Por ahora, simularemos el guardado y registraremos la actividad
        
        # Registro de la actividad para auditoría
        log_activity(f'Contenido de ayuda actualizado: Item {item_id}')
        
        # Ruta para el archivo de ayuda (si quisiéramos implementar guardado en archivos)
        # help_file_path = os.path.join(current_app.root_path, 'static', 'data', 'help_content.json')
        
        return jsonify({
            'success': True,
            'message': 'Contenido actualizado correctamente'
        })
    
    except Exception as e:
        current_app.logger.error(f"Error al actualizar contenido de ayuda: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500

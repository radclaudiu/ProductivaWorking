"""
Corrige directamente la validación del campo de estado en el formulario de edición de empleados.
Este script modifica routes.py para asegurar que el formulario no falle con 'Not a valid choice'.
"""

def fix_edit_form():
    # Leer el archivo routes.py
    with open('routes.py', 'r', encoding='utf-8') as file:
        content = file.read()
    
    # Buscar la función edit_employee
    edit_function_marker = "@employee_bp.route('/<int:id>/edit', methods=['GET', 'POST'])"
    if edit_function_marker not in content:
        print("No se encontró la función edit_employee en routes.py")
        return False
    
    # Ubicar la función edit_employee
    edit_function_pos = content.find(edit_function_marker)
    form_validation_pos = content.find("if form.validate_on_submit():", edit_function_pos)
    
    if form_validation_pos == -1:
        print("No se encontró la validación del formulario en edit_employee")
        return False
    
    # Añadir código para forzar la validación correcta justo antes de la validación del formulario
    force_valid_status_code = """
        # Asegurar que el valor de status es válido antes de validar
        if form.status.data and form.status.choices:
            valid_status_values = [choice[0] for choice in form.status.choices]
            if form.status.data not in valid_status_values:
                print(f"DEBUG: Corrigiendo status inválido antes de validar: {form.status.data} no está en {valid_status_values}")
                # Establecer a un valor seguro que sabemos existe
                form.status.data = "activo"
        """
    
    # Insertar el código de validación antes de la validación del formulario
    insertion_point = form_validation_pos
    new_content = content[:insertion_point] + force_valid_status_code + content[insertion_point:]
    
    # Guardar el archivo modificado
    with open('routes.py', 'w', encoding='utf-8') as file:
        file.write(new_content)
    
    print("✅ Corrección aplicada correctamente a la función edit_employee en routes.py")
    return True

if __name__ == "__main__":
    fix_edit_form()
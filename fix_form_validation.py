"""
Script para corregir el problema de validación en el formulario de edición de empleados.
Este script busca específicamente el problema de 'Not a valid choice' en el campo status.
"""
from models import EmployeeStatus

def direct_fix_for_edit_form():
    """
    Implementa una corrección directa para el formulario de edición de empleados.
    Modifica la función edit_employee para corregir el problema de validación del campo status.
    """
    # Verificar los posibles valores de EmployeeStatus
    status_values = [status.value for status in EmployeeStatus]
    print(f"Valores válidos para EmployeeStatus: {status_values}")
    
    # Definir la corrección - adición de código después de la inicialización del formulario
    # pero antes de la validación
    form_init_marker = "form = EmployeeForm(obj=employee)"
    fix_code = """
    # Asegurar que los campos SelectField tienen valores válidos
    if hasattr(form, 'status') and form.status.data is not None:
        # Si el valor actual no está entre las opciones válidas
        status_choices = [choice[0] for choice in form.status.choices] if form.status.choices else []
        if form.status.data not in status_choices:
            # Resetear a un valor válido por defecto - ACTIVO
            print(f"DEBUG: Corrigiendo status inválido: {form.status.data} no está en {status_choices}")
            form.status.data = "activo"  # valor predeterminado seguro que sabemos existe
    
    # Asegurar choices para todos los SelectFields
    form.status.choices = [(s.value, s.name.replace('_', ' ').capitalize()) for s in EmployeeStatus]
    form.contract_type.choices = [(ct.value, ct.name.replace('_', ' ').capitalize()) for ct in ContractType]
    
    # Establecer company_id choices correctamente
    if current_user.is_admin():
        companies = Company.query.all()
        form.company_id.choices = [(c.id, c.name) for c in companies] if companies else [(employee.company_id, "Empresa actual")]
    else:
        companies = current_user.companies
        form.company_id.choices = [(c.id, c.name) for c in companies] if companies else [(employee.company_id, "Empresa actual")]
"""
    
    # Leer el archivo routes.py
    with open('routes.py', 'r', encoding='utf-8') as file:
        content = file.read()
    
    # Encontrar la función edit_employee
    edit_function_start = content.find('@employee_bp.route(\'/<int:id>/edit\'')
    if edit_function_start == -1:
        print("No se pudo encontrar la función edit_employee")
        return False
    
    # Encontrar dónde se inicializa el formulario
    form_init_pos = content.find(form_init_marker, edit_function_start)
    if form_init_pos == -1:
        print("No se pudo encontrar la inicialización del formulario")
        return False
    
    # Insertar el código de corrección después de la inicialización del formulario
    insertion_point = form_init_pos + len(form_init_marker)
    new_content = content[:insertion_point] + fix_code + content[insertion_point:]
    
    # Guardar el archivo modificado
    with open('routes.py', 'w', encoding='utf-8') as file:
        file.write(new_content)
    
    print("✅ Corrección aplicada correctamente al archivo routes.py")
    return True

if __name__ == "__main__":
    direct_fix_for_edit_form()
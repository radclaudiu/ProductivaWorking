"""
Script para corregir el problema de edición de empleados,
asegurando que todos los SelectFields tengan opciones válidas.
"""
import os
import re

def create_patch_for_edit_employee():
    """
    Crea un parche para la función edit_employee en routes.py.
    """
    # Leer el contenido de forms.py para identificar los SelectFields
    with open('forms.py', 'r') as f:
        forms_content = f.read()
    
    # Buscar todos los SelectFields en EmployeeForm
    employee_form_pattern = r'class EmployeeForm\(.*?\):(.*?)class'
    employee_form_match = re.search(employee_form_pattern, forms_content, re.DOTALL)
    
    if not employee_form_match:
        print("No se pudo encontrar la clase EmployeeForm en forms.py")
        return False
    
    employee_form_code = employee_form_match.group(1)
    select_fields = []
    
    # Buscar líneas que definan SelectFields
    select_field_pattern = r'(\w+)\s*=\s*SelectField'
    for match in re.finditer(select_field_pattern, employee_form_code):
        select_fields.append(match.group(1))
    
    print(f"SelectFields encontrados en EmployeeForm: {select_fields}")
    
    # Leer routes.py para modificar la función edit_employee
    with open('routes.py', 'r') as f:
        routes_content = f.read()
    
    # Buscar la función edit_employee
    edit_employee_pattern = r'@employee_bp\.route\(\'/<int:id>/edit\',.*?\)\ndef edit_employee\(.*?\):(.*?)@employee_bp\.route'
    edit_employee_match = re.search(edit_employee_pattern, routes_content, re.DOTALL)
    
    if not edit_employee_match:
        print("No se pudo encontrar la función edit_employee en routes.py")
        return False
    
    edit_employee_code = edit_employee_match.group(1)
    
    # Preparar el código para asegurar que todos los SelectFields tengan opciones
    choices_code = """
    # Asegurar que todos los SelectFields tengan opciones válidas
    # Establecer opciones para contract_type si es necesario
    if not hasattr(form.contract_type, 'choices') or not form.contract_type.choices:
        form.contract_type.choices = [(ct.value, ct.name.capitalize()) for ct in ContractType]
    
    # Establecer opciones para status si es necesario
    if not hasattr(form.status, 'choices') or not form.status.choices:
        form.status.choices = [(status.value, status.name.capitalize()) for status in EmployeeStatus]
    
    # Establecer opciones para company_id si es necesario
    if not hasattr(form.company_id, 'choices') or not form.company_id.choices:
        # Si no hay opciones, usar la empresa actual como única opción
        form.company_id.choices = [(employee.company_id, "Empresa actual")]
    """
    
    # Buscar el punto justo después de inicializar el formulario para insertar nuestro código
    form_init_pattern = r'form\s*=\s*EmployeeForm\(.*?\)'
    form_init_match = re.search(form_init_pattern, edit_employee_code)
    
    if not form_init_match:
        print("No se pudo encontrar la inicialización del formulario en edit_employee")
        return False
    
    # Crear el código modificado
    form_init_end = form_init_match.end()
    modified_edit_employee_code = edit_employee_code[:form_init_end] + "\n" + choices_code + edit_employee_code[form_init_end:]
    
    # Reemplazar en el código completo
    modified_routes_content = routes_content.replace(edit_employee_code, modified_edit_employee_code)
    
    # Guardar el archivo modificado
    with open('routes.py', 'w') as f:
        f.write(modified_routes_content)
    
    print("✅ Se ha modificado correctamente la función edit_employee")
    return True

if __name__ == "__main__":
    create_patch_for_edit_employee()
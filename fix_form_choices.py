"""
Script para corregir el problema de edición de empleados
asegurando que todos los SelectFields tengan opciones válidas.
"""
import re

def fix_employee_edit_form():
    # Leer el archivo routes.py
    with open('routes.py', 'r') as f:
        lines = f.readlines()
    
    # Buscar la línea donde se inicializa el formulario
    form_init_line_idx = None
    for i, line in enumerate(lines):
        if "form = EmployeeForm(obj=employee)" in line:
            form_init_line_idx = i
            break
    
    if form_init_line_idx is None:
        print("No se encontró la inicialización del formulario")
        return False
    
    # Nuevo código a insertar después de la inicialización del formulario
    new_code = """    
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
    
    # Insertar el nuevo código
    lines.insert(form_init_line_idx + 1, new_code)
    
    # Guardar el archivo modificado
    with open('routes.py', 'w') as f:
        f.writelines(lines)
    
    print("✅ Se ha añadido código para asegurar que todos los SelectFields tengan opciones válidas")
    return True

if __name__ == "__main__":
    fix_employee_edit_form()

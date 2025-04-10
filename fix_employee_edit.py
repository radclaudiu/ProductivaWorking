"""
Script para corregir el problema de edición de empleados.
Este script modifica la función edit_employee en routes.py.
"""
import os

def fix_employee_edit_route():
    """
    Modifica la función edit_employee en routes.py para corregir el problema
    con los campos SelectField que no tienen opciones.
    """
    # Leer el archivo routes.py
    with open('routes.py', 'r') as f:
        content = f.read()
    
    # Buscar la función edit_employee
    start_marker = '@employee_bp.route(\'/<int:id>/edit\', methods=[\'GET\', \'POST\'])'
    if start_marker not in content:
        print("No se encontró la función edit_employee en routes.py.")
        return False
    
    # Dividir el contenido antes y después de la función
    parts = content.split(start_marker)
    if len(parts) != 2:
        print("No se pudo dividir correctamente el archivo.")
        return False
    
    before_function = parts[0]
    function_and_after = parts[1]
    
    # Buscar el siguiente decorador para saber dónde termina la función
    next_marker = '@employee_bp.route'
    function_parts = function_and_after.split(next_marker, 1)
    if len(function_parts) != 2:
        print("No se pudo encontrar el final de la función.")
        return False
    
    function_content = function_parts[0]
    after_function = next_marker + function_parts[1]
    
    # Modificar el código que establece las opciones para el campo company_id
    old_code = """    # Get list of companies for the dropdown
    if current_user.is_admin():
        companies = Company.query.all()
        form.company_id.choices = [(c.id, c.name) for c in companies]
    else:
        # Para gerentes, mostrar todas las empresas a las que tienen acceso
        companies = current_user.companies
        if companies:
            form.company_id.choices = [(c.id, c.name) for c in companies]"""
    
    new_code = """    # Get list of companies for the dropdown - con fallback seguro
    companies_found = False
    if current_user.is_admin():
        companies = Company.query.all()
        if companies:
            form.company_id.choices = [(c.id, c.name) for c in companies]
            companies_found = True
    else:
        # Para gerentes, mostrar todas las empresas a las que tienen acceso
        companies = current_user.companies
        if companies:
            form.company_id.choices = [(c.id, c.name) for c in companies]
            companies_found = True
    
    # Si no se encontraron empresas, establecer la empresa actual como única opción
    if not companies_found:
        form.company_id.choices = [(employee.company_id, "Empresa actual")]
        
    # Asegurar que todos los SelectFields tienen opciones válidas
    # Estos campos ya deberían tener opciones predefinidas, pero lo verificamos por seguridad
    if not form.contract_type.choices:
        form.contract_type.choices = [(ct.value, ct.name.capitalize()) for ct in ContractType]
    if not form.status.choices:
        form.status.choices = [(status.value, status.name.capitalize()) for status in EmployeeStatus]"""
    
    if old_code not in function_content:
        print("No se encontró el código a reemplazar.")
        return False
    
    new_function_content = function_content.replace(old_code, new_code)
    
    # Escribir el archivo modificado
    with open('routes.py', 'w') as f:
        f.write(before_function + start_marker + new_function_content + after_function)
    
    print("✅ Se ha modificado correctamente la función edit_employee en routes.py.")
    return True

if __name__ == "__main__":
    fix_employee_edit_route()
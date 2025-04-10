def edit_employee(id):
    employee = Employee.query.get_or_404(id)
    
    # Verificar permisos para editar este empleado
    if not current_user.is_admin() and employee.company_id not in [c.id for c in current_user.companies]:
        flash("No tienes permiso para editar este empleado.", "danger")
        return redirect(url_for('employee_bp.index'))
    
    # Guardar fecha de fin antes de inicializar el formulario (podría ser None)
    print(f"DEBUG pre-form - Employee {id}: end_date = {employee.end_date}, tipo {type(employee.end_date)}")
    
    form = EmployeeForm(obj=employee)
    
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
    
    # Get list of companies for the dropdown
    if current_user.is_admin():
        companies = Company.query.all()
        form.company_id.choices = [(c.id, c.name) for c in companies]
    else:
        # Para gerentes, mostrar todas las empresas a las que tienen acceso
        companies = current_user.companies
        if companies:
            form.company_id.choices = [(c.id, c.name) for c in companies]
    
    # Verificar que el valor actual de status esté entre las opciones
    # Si no está, establecerlo a un valor válido (el primero de la lista)
    status_values = [s.value for s in EmployeeStatus]
    if employee.status and employee.status not in status_values:
        # Corregir el status del empleado a un valor válido
        print(f"DEBUG: Corrigiendo status de empleado de {employee.status} a {EmployeeStatus.ACTIVE.value}")
        employee.status = EmployeeStatus.ACTIVE.value
    
    # Verificar que el valor actual de status del formulario esté entre las opciones
    status_choices = [choice[0] for choice in form.status.choices]
    if form.status.data and form.status.data not in status_choices:
        # Corregir el status del formulario a un valor válido
        print(f"DEBUG: Corrigiendo status del formulario de {form.status.data} a {EmployeeStatus.ACTIVE.value}")
        form.status.data = EmployeeStatus.ACTIVE.value
    
    # Verificar si el formulario fue enviado y es válido
    print(f"DEBUG form submitted: {'POST' if request.method == 'POST' else 'GET'}")
    if form.is_submitted():
        print("DEBUG form is_submitted: True")
        print(f"DEBUG form validate: {form.validate()}")
        if not form.validate():
            print(f"DEBUG form errors: {form.errors}")
        
        if form.validate_on_submit():
            # Actualizar todos los campos del formulario al objeto employee
            form.populate_obj(employee)
            
            # Para fechas, manejar específicamente porque puede haber problemas con None
            print(f"DEBUG post-form - Form end_date = {form.end_date.data}, tipo {type(form.end_date.data)}")
            employee.end_date = form.end_date.data
            
            # Guardar los cambios en la base de datos
            db.session.commit()
            
            # Registrar la actividad
            record_activity(f"Editó al empleado {employee.full_name}")
            
            flash(f"Empleado {employee.full_name} actualizado correctamente.", "success")
            return redirect(url_for('employee_bp.view', id=employee.id))
    
    return render_template('employee_form.html', form=form, employee=employee, edit=True)



def edit_employee(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to edit this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para editar este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Debug información del empleado antes de inicializar el formulario
    print(f"DEBUG pre-form - Employee {employee.id}: end_date = {employee.end_date}, tipo {type(employee.end_date)}")
    log_activity(f"DEBUG pre-form - Employee {employee.id}: end_date = {employee.end_date}, tipo {type(employee.end_date)}")
    
    form = EmployeeForm(obj=employee)
    
    # Debug valores del formulario después de inicializar
    print(f"DEBUG post-form - Form end_date = {form.end_date.data}, tipo {type(form.end_date.data)}")
    log_activity(f"DEBUG post-form - Form end_date = {form.end_date.data}, tipo {type(form.end_date.data)}")
    
    # Revisar si el formulario fue enviado o no
    print(f"DEBUG form submitted: {request.method}")
    log_activity(f"DEBUG form submitted: {request.method}")
    if request.method == 'POST':
        print(f"DEBUG form is_submitted: {form.is_submitted()}")
        log_activity(f"DEBUG form is_submitted: {form.is_submitted()}")
        
    # Get list of companies for the dropdown
    if current_user.is_admin():
        companies = Company.query.all()
        form.company_id.choices = [(c.id, c.name) for c in companies] if companies else [(employee.company_id, "Empresa actual")]
    else:
        # Para gerentes, mostrar todas las empresas a las que tienen acceso
        companies = current_user.companies
        if companies:
            form.company_id.choices = [(c.id, c.name) for c in companies]
        else:
            # Si no hay empresas asignadas al gerente, mostrar un valor por defecto
            # para evitar el error "Choices cannot be None"
            form.company_id.choices = [(employee.company_id, "Empresa actual")]
    
    # Ahora sí podemos validar el formulario
    if request.method == 'POST':
        print(f"DEBUG form validate (después de asignar choices): {form.validate()}")
        log_activity(f"DEBUG form validate (después de asignar choices): {form.validate()}")
        if not form.validate():
            print(f"DEBUG form errors: {form.errors}")
            log_activity(f"DEBUG form errors: {form.errors}")
    
    if form.validate_on_submit():
        # Log de depuración para validar que el formulario pasó la validación
        print(f"DEBUG form validate_on_submit: PASÓ LA VALIDACIÓN. Datos end_date={form.end_date.data}, tipo {type(form.end_date.data)}")
        log_activity(f"DEBUG form validate_on_submit: PASÓ LA VALIDACIÓN. Datos end_date={form.end_date.data}, tipo {type(form.end_date.data)}")
        
        # Track changes for employee history
        if employee.first_name != form.first_name.data:
            log_employee_change(employee, 'first_name', employee.first_name, form.first_name.data)
            employee.first_name = form.first_name.data
            
        if employee.last_name != form.last_name.data:
            log_employee_change(employee, 'last_name', employee.last_name, form.last_name.data)
            employee.last_name = form.last_name.data
            
        if employee.dni != form.dni.data:
            log_employee_change(employee, 'dni', employee.dni, form.dni.data)
            employee.dni = form.dni.data
            
        if employee.social_security_number != form.social_security_number.data:
            log_employee_change(employee, 'social_security_number', employee.social_security_number, form.social_security_number.data)
            employee.social_security_number = form.social_security_number.data
            
        if employee.email != form.email.data:
            log_employee_change(employee, 'email', employee.email, form.email.data)
            employee.email = form.email.data
            
        if employee.address != form.address.data:
            log_employee_change(employee, 'address', employee.address, form.address.data)
            employee.address = form.address.data
            
        if employee.phone != form.phone.data:
            log_employee_change(employee, 'phone', employee.phone, form.phone.data)
            employee.phone = form.phone.data
            
        if employee.position != form.position.data:
            log_employee_change(employee, 'position', employee.position, form.position.data)
            employee.position = form.position.data
            
        if str(employee.contract_type.value if employee.contract_type else None) != form.contract_type.data:
            log_employee_change(employee, 'contract_type', 
                              employee.contract_type.value if employee.contract_type else None, 
                              form.contract_type.data)
            employee.contract_type = ContractType(form.contract_type.data) if form.contract_type.data else None
            
        if employee.bank_account != form.bank_account.data:
            log_employee_change(employee, 'bank_account', employee.bank_account, form.bank_account.data)
            employee.bank_account = form.bank_account.data
            
        if employee.start_date != form.start_date.data:
            log_employee_change(employee, 'start_date', 
                              employee.start_date.isoformat() if employee.start_date else None, 
                              form.start_date.data.isoformat() if form.start_date.data else None)
            employee.start_date = form.start_date.data
            
        # Manejar la fecha de baja (end_date)
        old_end_date = employee.end_date.isoformat() if employee.end_date else None
        new_end_date = form.end_date.data
        
        # Log de depuración para verfificar el valor de end_date
        print(f"DEBUG end_date procesando - old: {old_end_date}, new: {new_end_date}, tipo_new: {type(new_end_date)}")
        log_activity(f"DEBUG end_date procesando - old: {old_end_date}, new: {new_end_date}, tipo_new: {type(new_end_date)}")
        
        # Cambiamos la comparación para asegurar que funcione correctamente
        # Verificamos si hay diferencias entre el valor actual y el nuevo valor
        # independientemente de su formato de representación
        old_date_str = old_end_date if old_end_date else None
        new_date_str = new_end_date.isoformat() if new_end_date else None
        
        if old_date_str != new_date_str:
            print(f"DEBUG end_date CAMBIO DETECTADO - old: {old_date_str}, new: {new_date_str}")
            log_activity(f"DEBUG end_date CAMBIO DETECTADO - old: {old_date_str}, new: {new_date_str}")
            
            log_employee_change(employee, 'end_date', old_date_str, new_date_str)
            
            # Asignar directamente el valor con ORM
            employee.end_date = new_end_date
            
            # También ejecutar SQL directo para garantizar que se aplica el cambio
            if new_end_date is not None:
                sql = "UPDATE employees SET end_date = :end_date WHERE id = :id"
                db.session.execute(sql, {"end_date": new_end_date, "id": employee.id})
                print(f"Fecha de baja establecida a {new_end_date} para empleado {employee.id}")
            else:
                sql = "UPDATE employees SET end_date = NULL WHERE id = :id"
                db.session.execute(sql, {"id": employee.id})
                print(f"Fecha de baja eliminada (NULL) para empleado {employee.id}")
            
            # Forzar que se apliquen los cambios inmediatamente
            db.session.flush()
            
            # Verificar inmediatamente si el cambio fue efectivo
            db.session.refresh(employee)
            print(f"DEBUG end_date post-refresh: {employee.end_date}")
            log_activity(f"DEBUG end_date post-refresh: {employee.end_date}")
            
        if employee.company_id != form.company_id.data:
            old_company = Company.query.get(employee.company_id).name if employee.company_id else 'Ninguna'
            new_company = Company.query.get(form.company_id.data).name
            log_employee_change(employee, 'company', old_company, new_company)
            employee.company_id = form.company_id.data
            
        if employee.is_active != form.is_active.data:
            log_employee_change(employee, 'is_active', str(employee.is_active), str(form.is_active.data))
            employee.is_active = form.is_active.data
            
        if str(employee.status.value if employee.status else 'activo') != form.status.data:
            log_employee_change(employee, 'status', 
                              employee.status.value if employee.status else 'activo', 
                              form.status.data)
            employee.status = EmployeeStatus(form.status.data)
            
        employee.updated_at = datetime.utcnow()
        
        # Log de depuración justo antes del commit
        print(f"DEBUG pre-commit - Employee {employee.id}: end_date = {employee.end_date}, tipo {type(employee.end_date)}")
        log_activity(f"DEBUG pre-commit - Employee {employee.id}: end_date = {employee.end_date}, tipo {type(employee.end_date)}")
        
        db.session.commit()
        
        # Log de depuración justo después del commit - recargamos el empleado para verificar
        employee_post_commit = Employee.query.get(employee.id)
        print(f"DEBUG post-commit - Employee {employee_post_commit.id}: end_date = {employee_post_commit.end_date}, tipo {type(employee_post_commit.end_date)}")
        log_activity(f"DEBUG post-commit - Employee {employee_post_commit.id}: end_date = {employee_post_commit.end_date}, tipo {type(employee_post_commit.end_date)}")
        
        log_activity(f'Empleado actualizado: {employee.first_name} {employee.last_name}')
        flash(f'Empleado "{employee.first_name} {employee.last_name}" actualizado correctamente.', 'success')
        return redirect(url_for('employee.view_employee', id=employee.id))
    
    return render_template('employee_form.html', title=f'Editar {employee.first_name} {employee.last_name}', form=form, employee=employee)
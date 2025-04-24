# Implementación del Sistema de Seguimiento de Horas Trabajadas y Coste Horario

## Índice

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Archivos Modificados/Creados](#archivos-modificadoscreados)
   1. [Modelos de Datos y Formularios](#1-modelos-de-datos-y-formularios)
      1. [Coste Horario de Empleados](#10-modificación-en-modelspy---coste-horario-de-empleados)
      2. [Validación de Coste Horario](#11-modificación-en-formspy---validación-de-coste-horario)
      3. [Gestión de Coste Horario](#12-modificación-en-routespy---gestión-de-coste-horario)
      4. [Modelos para Seguimiento de Horas](#11-modificación-en-models_checkpointspy)
   2. [Utilidades para Cálculo de Horas](#12-creación-de-utils_work_hourspy)
   3. [Script de Migración](#13-script-de-migración-migrate_work_hourspy)
   4. [Script de Pruebas](#14-script-de-pruebas-test_work_hourspy)
3. [Modificaciones en Flujos de Fichaje](#2-modificaciones-en-flujos-de-fichaje)
   1. [Función process_employee_action](#21-actualización-del-método-process_employee_action-en-routes_checkpointspy)
   2. [Función record_checkout](#22-actualización-del-método-record_checkout-en-routes_checkpointspy)
   3. [Función adjust_record](#23-actualización-del-método-adjust_record-en-routes_checkpointspy)
4. [Sistema de Resolución de Incidencias Masivas](#5-sistema-de-resolución-de-incidencias-masivas)
   1. [Ruta de Resolución Masiva](#51-modificación-en-routes_checkpointspy---resolución-masiva)
   2. [Interfaz de Resolución Masiva](#52-interfaz-en-templatescheckpointsincidentshtml)
   3. [JavaScript para Gestión de Selección](#53-javascript-para-gestión-de-selección)
5. [Cambios en la Interfaz](#cambios-en-la-interfaz)
   1. [Visualización de Coste Horario](#1-modificación-en-templatescompany_detailhtml---visualización-de-coste-horario)
   2. [Formulario de Coste Horario](#2-modificación-en-templatescompany_formhtml---formulario-de-coste-horario)
   3. [Integración de Checkboxes](#3-modificación-en-templatescheckpointsincidentshtml---integración-de-checkboxes)
6. [Consideraciones Adicionales](#consideraciones-adicionales)
   1. [Script de Migración para Coste Horario](#1-script-de-migración-para-coste-horario)
   2. [Funciones Auxiliares para Resolución Masiva](#2-funciones-auxiliares-para-resolución-masiva)
   3. [Información sobre el Script de Pruebas](#3-actualización-de-información-sobre-script-test_work_hourspy)
7. [Instrucciones para Migración/Fusión](#instrucciones-para-migraciónfusión)
   1. [Para el Sistema de Seguimiento de Horas](#1-para-el-sistema-de-seguimiento-de-horas)
   2. [Para el Coste Horario de Empleados](#2-para-el-coste-horario-de-empleados)
   3. [Para el Sistema de Resolución de Incidencias Masivas](#3-para-el-sistema-de-resolución-de-incidencias-masivas)
8. [Conclusión](#conclusión)

## Resumen Ejecutivo

Este documento detalla los cambios realizados para implementar un sistema completo de seguimiento y acumulación de horas trabajadas en la aplicación de gestión de fichajes, así como la implementación del coste horario de empleados a nivel de empresa. El objetivo principal ha sido desarrollar un mecanismo que permita:

1. Calcular y almacenar las horas trabajadas en cada registro de fichaje
2. Mantener acumulados de horas trabajadas a nivel diario, semanal y mensual por empleado
3. Mantener acumulados de horas trabajadas a nivel mensual por empresa
4. Gestionar correctamente ajustes manuales (restando horas originales y sumando nuevas)
5. Optimizar el rendimiento mediante índices adecuados
6. **Configurar y almacenar el coste horario por empleado a nivel de empresa**

## Archivos Modificados/Creados

### 1. Modelos de Datos y Formularios

#### 1.0. Modificación en `models.py` - Coste Horario de Empleados

Se ha añadido el campo `hourly_employee_cost` a la clase `Company` para permitir la configuración del coste por hora de los empleados a nivel de empresa:

```python
class Company(db.Model):
    """Modelo para las empresas"""
    __tablename__ = 'companies'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), nullable=False)
    # ... otros campos existentes ...
    
    # Nuevo campo para el coste por hora de los empleados
    hourly_employee_cost = db.Column(db.Numeric(10, 2), default=0.00, nullable=False,
                                    comment='Coste por hora de los empleados de la empresa')
```

#### 1.1. Modificación en `forms.py` - Validación de Coste Horario

Se ha modificado el formulario `CompanyForm` para incluir el campo de coste por hora con validación específica para formato decimal español:

```python
class CompanyForm(FlaskForm):
    """Formulario para empresas"""
    name = StringField('Nombre', validators=[DataRequired()])
    # ... otros campos existentes ...
    
    # Nuevo campo para el coste por hora con validación específica para formato decimal español
    hourly_employee_cost = StringField('Coste por Hora (€)',
                                     validators=[Optional()],
                                     default='0,00',
                                     description='Coste por hora de los empleados (formato: 12,50)')
    
    def validate_hourly_employee_cost(self, field):
        """Valida que el coste por hora tenga formato decimal válido en formato español"""
        if field.data:
            # Reemplaza comas por puntos para procesamiento interno
            value = field.data.replace(',', '.')
            try:
                # Intenta convertir a decimal y verifica que sea positivo
                hourly_cost = Decimal(value)
                if hourly_cost < 0:
                    raise ValidationError('El coste por hora no puede ser negativo')
            except:
                raise ValidationError('Formato inválido. Use el formato: 12,50')
```

#### 1.2. Modificación en `routes.py` - Gestión de Coste Horario

Se ha actualizado el controlador de empresas para manejar el campo de coste por hora:

```python
@app.route('/companies/<int:company_id>', methods=['GET', 'POST'])
@login_required
@admin_required
def edit_company(company_id):
    company = Company.query.get_or_404(company_id)
    form = CompanyForm(obj=company)
    
    if form.validate_on_submit():
        form.populate_obj(company)
        
        # Procesar el coste por hora en formato español
        if form.hourly_employee_cost.data:
            hourly_cost = form.hourly_employee_cost.data.replace(',', '.')
            company.hourly_employee_cost = Decimal(hourly_cost)
        
        db.session.commit()
        flash('Empresa actualizada correctamente', 'success')
        return redirect(url_for('companies'))
    
    # Formatear el coste por hora para mostrar en el formulario
    if company.hourly_employee_cost is not None:
        form.hourly_employee_cost.data = str(company.hourly_employee_cost).replace('.', ',')
    
    return render_template('company_form.html', form=form, company=company)
```

#### 1.1. Modificación en `models_checkpoints.py`

- Añadido campo `hours_worked` en la clase `CheckPointOriginalRecord` para almacenar el cálculo de horas trabajadas
- Creadas dos nuevas clases:
  - `EmployeeWorkHours`: Para almacenar y acumular horas a nivel de empleado
  - `CompanyWorkHours`: Para almacenar y acumular horas a nivel de empresa

```python
class EmployeeWorkHours(db.Model):
    """Registro de horas trabajadas acumuladas por empleado (diaria, semanal y mensual)"""
    __tablename__ = 'employee_work_hours'
    
    id = db.Column(db.Integer, primary_key=True)
    year = db.Column(db.Integer, nullable=False)
    month = db.Column(db.Integer, nullable=False)  # 1-12
    week_number = db.Column(db.Integer, nullable=False)  # 1-53 (ISO)
    day = db.Column(db.Integer, nullable=True)  # 1-31
    
    daily_hours = db.Column(db.Float, default=0.0, nullable=False)  # Horas del día específico
    weekly_hours = db.Column(db.Float, default=0.0, nullable=False)  # Horas de la semana
    monthly_hours = db.Column(db.Float, default=0.0, nullable=False)  # Horas del mes
    
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    employee_id = db.Column(db.Integer, db.ForeignKey('employees.id'), nullable=False)
    employee = db.relationship('Employee', backref=db.backref('work_hours', lazy=True))
    
    company_id = db.Column(db.Integer, db.ForeignKey('companies.id'))
    
    __table_args__ = (
        db.Index('idx_employee_date', employee_id, year, month, day),
        db.Index('idx_employee_week', employee_id, year, week_number),
        db.Index('idx_employee_month', employee_id, year, month),
        db.Index('idx_company_month', company_id, year, month),
    )

class CompanyWorkHours(db.Model):
    """Registro de horas trabajadas acumuladas por empresa (mensual)"""
    __tablename__ = 'company_work_hours'
    
    id = db.Column(db.Integer, primary_key=True)
    
    year = db.Column(db.Integer, nullable=False)
    month = db.Column(db.Integer, nullable=False)  # 1-12
    
    monthly_hours = db.Column(db.Float, default=0.0, nullable=False)
    
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    company_id = db.Column(db.Integer, db.ForeignKey('companies.id'), nullable=False)
    company = db.relationship('Company', backref=db.backref('work_hours', lazy=True))
    
    __table_args__ = (
        db.Index('idx_company_year_month', company_id, year, month, unique=True),
        db.Index('idx_company_year', company_id, year),
        db.Index('idx_year_month', year, month),
    )
```

#### 1.2. Creación de `utils_work_hours.py`

Este nuevo archivo contiene las funciones necesarias para:

- Calcular horas trabajadas entre dos timestamps
- Obtener el número de semana ISO
- Actualizar acumulados de horas por empleado
- Actualizar acumulados de horas por empresa

```python
def calculate_hours_worked(check_in_time, check_out_time):
    """
    Calcula las horas trabajadas entre dos timestamps.
    
    Args:
        check_in_time (datetime): Hora de entrada
        check_out_time (datetime): Hora de salida
        
    Returns:
        float: Número de horas trabajadas, redondeado a 2 decimales
    """
    if not check_in_time or not check_out_time:
        return 0.0
    
    # Si la hora de salida es menor que la de entrada, asumimos que es del día siguiente
    # Sin este ajuste, obtendríamos un número negativo de horas
    if check_out_time < check_in_time:
        # Ajustar el cálculo para turnos nocturnos que cruzan días
        check_out_time += timedelta(days=1)
    
    # Calcular la diferencia en segundos y convertir a horas
    seconds_difference = (check_out_time - check_in_time).total_seconds()
    hours_worked = seconds_difference / 3600
    
    # Redondear a 2 decimales para mejor legibilidad
    return round(hours_worked, 2)

def update_employee_work_hours(employee_id, check_in_time, hours_worked):
    """
    Actualiza las horas trabajadas acumuladas para un empleado.
    
    Args:
        employee_id (int): ID del empleado
        check_in_time (datetime): Hora de entrada (determina día/semana/mes)
        hours_worked (float): Horas trabajadas en este fichaje
        
    Returns:
        bool: True si la actualización fue exitosa, False en caso contrario
    """
    # Lógica compleja para actualizar todos los acumulados
    # Incluye manejo de registros diarios, semanales y mensuales
    # Ver archivo completo para detalles
```

#### 1.3. Script de Migración `migrate_work_hours.py`

Este script se encarga de:

- Añadir la columna `hours_worked` a la tabla `checkpoint_original_records`
- Crear la tabla `employee_work_hours` con sus índices
- Crear la tabla `company_work_hours` con sus índices

```python
def add_hours_worked_to_checkpoint_original_records():
    """Añade la columna hours_worked a la tabla checkpoint_original_records si no existe"""
    # Implementación...

def create_employee_work_hours_table():
    """Crea la tabla employee_work_hours si no existe"""
    # Implementación...

def create_company_work_hours_table():
    """Crea la tabla company_work_hours si no existe"""
    # Implementación...
```

#### 1.4. Script de Pruebas `test_work_hours.py`

Script para verificar:
- La correcta creación de las tablas en la base de datos
- El funcionamiento de las funciones de cálculo de horas
- Los acumulados de horas trabajadas

```python
def verify_models():
    """Verificar que los modelos necesarios existen en la base de datos"""
    # Implementación...

def test_calculate_hours():
    """Probar la función de cálculo de horas entre dos timestamps"""
    # Prueba diferentes escenarios (jornadas normales, cortas, nocturnas, etc.)
```

### 2. Modificaciones en Flujos de Fichaje

#### 2.1. Actualización del Método `process_employee_action` en `routes_checkpoints.py`

Se modificó esta función para que cuando un empleado realiza un check-out:

1. Calcule las horas trabajadas: `hours_worked = calculate_hours_worked(original_checkin, original_checkout)`
2. Actualice los acumulados: `update_employee_work_hours(employee.id, original_checkin, hours_worked)`
3. Guarde el cálculo en `CheckPointOriginalRecord`

```python
# Calcular las horas trabajadas con los valores reales (originales)
hours_worked = calculate_hours_worked(original_checkin, original_checkout)

# Actualizar los acumulados de horas trabajadas
update_result = update_employee_work_hours(employee.id, original_checkin, hours_worked)
if update_result:
    print(f"✅ Horas trabajadas ({hours_worked:.2f}h) actualizadas correctamente para empleado {employee.id}")
else:
    print(f"⚠️ No se pudieron actualizar las horas trabajadas para empleado {employee.id}")
```

#### 2.2. Actualización del Método `record_checkout` en `routes_checkpoints.py`

Similar a la modificación anterior, pero para la función que maneja checkouts desde la pantalla de detalles:

1. Calcule las horas trabajadas: `hours_worked = calculate_hours_worked(original_checkin, original_checkout)`
2. Actualice los acumulados: `update_employee_work_hours(employee.id, original_checkin, hours_worked)`
3. Guarde el cálculo en `CheckPointOriginalRecord`

#### 2.3. Actualización del Método `adjust_record` en `routes_checkpoints.py`

Esta función ya estaba preparada para manejar ajustes de horas, pero se aseguró que:

1. Se guarde el valor de horas originales
2. Se resten las horas originales del acumulado cuando se hace un ajuste
3. Se sumen las nuevas horas ajustadas al acumulado

```python
# Si las horas originales eran > 0, restar del acumulado (necesario para evitar horas duplicadas)
if original_hours > 0:
    # Restar las horas originales (mandar un valor negativo)
    update_employee_work_hours(record.employee_id, original_record.original_check_in_time, -original_hours)
    print(f"✓ Restando horas originales: {original_hours:.2f}h (Empleado: {record.employee_id})")

# Si las nuevas horas son > 0, añadir al acumulado
if new_hours > 0:
    # Sumar las nuevas horas ajustadas
    update_employee_work_hours(record.employee_id, record.check_in_time, new_hours)
    print(f"✓ Sumando horas ajustadas: {new_hours:.2f}h (Empleado: {record.employee_id})")
```

## Consideraciones Importantes

### 1. Diseño de Base de Datos

- Se utilizan **índices optimizados** para mejorar el rendimiento en tablas con muchos registros
- Se mantienen relaciones consistentes con cascadas de eliminación donde corresponde
- Se implementan mecanismos para manejar transiciones de días, semanas y meses
  
### 2. Manejo de Turnos Nocturnos

- Se detectan y manejan correctamente turnos que cruzan días (ej: 22:00 → 06:00)
- Las horas se asignan al día/mes/semana en que empezó el turno

### 3. Gestión de Transacciones

- Se utilizan transacciones anidadas para garantizar la consistencia
- Se implementa manejo de excepciones con rollback para evitar datos inconsistentes
- Se añaden logs detallados para depuración

### 4. Acumulados Inteligentes

- El sistema gestiona correctamente registros existentes y nuevos
- Evita duplicaciones al actualizar registros
- Mantiene consistencia entre diferentes niveles de acumulación

## Detalles de Implementación

### Cálculo de Horas Trabajadas

El sistema utiliza un enfoque robusto para calcular horas:

```python
def calculate_hours_worked(check_in_time, check_out_time):
    if not check_in_time or not check_out_time:
        return 0.0
    
    # Manejo de turnos nocturnos
    if check_out_time < check_in_time:
        check_out_time += timedelta(days=1)
    
    seconds_difference = (check_out_time - check_in_time).total_seconds()
    hours_worked = seconds_difference / 3600
    
    return round(hours_worked, 2)
```

### Actualización de Acumulados

La lógica para actualizar acumulados maneja varios casos:

1. **Registro diario**: Si existe, se actualiza; si no, se crea
2. **Registro semanal**: Se actualiza o crea un registro sin día específico
3. **Registro mensual**: Se actualiza o crea un registro sin día ni semana específicos
4. **Registro de empresa**: Se actualiza el acumulado mensual a nivel de empresa

### Estrategia de Índices

Se han creado los siguientes índices para optimizar el rendimiento:

```python
__table_args__ = (
    db.Index('idx_employee_date', employee_id, year, month, day),
    db.Index('idx_employee_week', employee_id, year, week_number),
    db.Index('idx_employee_month', employee_id, year, month),
    db.Index('idx_company_month', company_id, year, month),
)
```

### 5. Sistema de Resolución de Incidencias Masivas

Se ha implementado un sistema para permitir la resolución de múltiples incidencias de fichajes simultáneamente, mejorando la eficiencia de los administradores cuando necesitan procesar muchos registros pendientes.

#### 5.1. Modificación en `routes_checkpoints.py` - Resolución Masiva

```python
@app.route('/checkpoints/resolve_multiple', methods=['POST'])
@login_required
@admin_required
def resolve_multiple_incidents():
    """
    Ruta para resolver múltiples incidencias simultáneamente.
    Recibe una lista de IDs de registros a resolver y los datos de resolución comunes.
    """
    data = request.form
    record_ids = request.form.getlist('record_ids')
    
    if not record_ids:
        flash('No se seleccionaron registros para resolver', 'warning')
        return redirect(url_for('checkpoint_incidents'))
    
    # Obtener los datos comunes para todos los registros
    resolution_type = data.get('resolution_type')
    resolution_notes = data.get('resolution_notes', '')
    
    # Configuración de fechas/horas según el tipo de resolución
    resolution_date = data.get('resolution_date')
    resolution_time = data.get('resolution_time')
    
    # Contador de registros resueltos correctamente
    success_count = 0
    error_count = 0
    
    for record_id in record_ids:
        try:
            record = CheckPointRecord.query.get(record_id)
            if not record:
                continue
                
            # Aplicar la resolución según el tipo seleccionado
            if resolution_type == 'auto_checkout':
                # Implementación de cierre automático con la fecha/hora especificada
                success = apply_auto_checkout(record, resolution_date, resolution_time, resolution_notes)
            elif resolution_type == 'delete_record':
                # Implementación de eliminación de registro con notas
                success = delete_incident_record(record, resolution_notes)
            elif resolution_type == 'mark_as_resolved':
                # Implementación de marcar como resuelto sin cambios
                success = mark_incident_as_resolved(record, resolution_notes)
            else:
                success = False
                
            if success:
                success_count += 1
            else:
                error_count += 1
                
        except Exception as e:
            error_count += 1
            app.logger.error(f"Error al resolver incidencia ID {record_id}: {str(e)}")
    
    # Mensaje de resultado
    if success_count > 0:
        flash(f'Se resolvieron correctamente {success_count} incidencias', 'success')
    if error_count > 0:
        flash(f'No se pudieron resolver {error_count} incidencias', 'danger')
    
    return redirect(url_for('checkpoint_incidents'))
```

#### 5.2. Interfaz en `templates/checkpoints/incidents.html`

Se ha añadido un formulario modal para la resolución masiva de incidencias:

```html
<!-- Modal para Resolución Masiva -->
<div class="modal fade" id="masiveResolveModal" tabindex="-1" role="dialog" aria-labelledby="masiveResolveModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="masiveResolveModalLabel">Resolución Masiva de Incidencias</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <form action="{{ url_for('resolve_multiple_incidents') }}" method="post">
        <div class="modal-body">
          <!-- Campos ocultos para IDs de registros seleccionados -->
          <div id="selected-records-container"></div>
          
          <div class="form-group">
            <label>Tipo de Resolución:</label>
            <select class="form-control" name="resolution_type" id="mass-resolution-type" required>
              <option value="">Seleccione una opción</option>
              <option value="auto_checkout">Generar Fichaje de Salida Automático</option>
              <option value="delete_record">Eliminar Registro (Fichaje Inválido)</option>
              <option value="mark_as_resolved">Marcar como Resuelto (Sin Cambios)</option>
            </select>
          </div>
          
          <!-- Opciones específicas para cada tipo de resolución -->
          <div id="auto-checkout-options" style="display:none;">
            <div class="row">
              <div class="col-md-6">
                <div class="form-group">
                  <label>Fecha de Salida:</label>
                  <input type="date" class="form-control" name="resolution_date">
                </div>
              </div>
              <div class="col-md-6">
                <div class="form-group">
                  <label>Hora de Salida:</label>
                  <input type="time" class="form-control" name="resolution_time">
                </div>
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label>Notas de Resolución:</label>
            <textarea class="form-control" name="resolution_notes" rows="3" 
                     placeholder="Explicación de la resolución aplicada"></textarea>
          </div>
          
          <div class="alert alert-info">
            Se resolverán <span id="selected-count">0</span> incidencias con esta configuración.
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
          <button type="submit" class="btn btn-primary">Resolver Incidencias</button>
        </div>
      </form>
    </div>
  </div>
</div>
```

#### 5.3. JavaScript para Gestión de Selección

```javascript
// Código para gestionar la selección y procesamiento de registros para resolución masiva
$(document).ready(function() {
    // Variable para almacenar IDs seleccionados
    let selectedRecords = [];
    
    // Checkbox de selección de todos los registros
    $("#select-all").change(function() {
        const isChecked = $(this).prop('checked');
        $(".record-checkbox").prop('checked', isChecked);
        
        // Actualizar lista de seleccionados
        selectedRecords = [];
        if (isChecked) {
            $(".record-checkbox").each(function() {
                selectedRecords.push($(this).val());
            });
        }
        
        updateSelectedCount();
        toggleMassActionButton();
    });
    
    // Checkboxes individuales
    $(document).on('change', '.record-checkbox', function() {
        const recordId = $(this).val();
        
        if ($(this).prop('checked')) {
            // Añadir a seleccionados si no está ya
            if (!selectedRecords.includes(recordId)) {
                selectedRecords.push(recordId);
            }
        } else {
            // Quitar de seleccionados
            const index = selectedRecords.indexOf(recordId);
            if (index > -1) {
                selectedRecords.splice(index, 1);
            }
        }
        
        updateSelectedCount();
        toggleMassActionButton();
    });
    
    // Función para actualizar contador y campos ocultos
    function updateSelectedCount() {
        $("#selected-count").text(selectedRecords.length);
        
        // Actualizar campos ocultos para el formulario
        $("#selected-records-container").empty();
        selectedRecords.forEach(function(id) {
            $("#selected-records-container").append(
                `<input type="hidden" name="record_ids" value="${id}">`
            );
        });
    }
    
    // Mostrar/ocultar botón de acción masiva según haya seleccionados
    function toggleMassActionButton() {
        if (selectedRecords.length > 0) {
            $("#mass-action-btn").removeClass('d-none');
        } else {
            $("#mass-action-btn").addClass('d-none');
        }
    }
    
    // Cambiar opciones según tipo de resolución
    $("#mass-resolution-type").change(function() {
        const type = $(this).val();
        
        if (type === 'auto_checkout') {
            $("#auto-checkout-options").show();
        } else {
            $("#auto-checkout-options").hide();
        }
    });
});
```

## Instrucciones para Migración/Fusión

Para implementar estos cambios en otro fork, siga estos pasos:

### 1. Para el Sistema de Seguimiento de Horas
1. Añadir el campo `hours_worked` a la clase `CheckPointOriginalRecord` en `models_checkpoints.py`
2. Añadir las clases `EmployeeWorkHours` y `CompanyWorkHours` en `models_checkpoints.py`
3. Crear el archivo `utils_work_hours.py` con todas las funciones auxiliares
4. Crear el script de migración `migrate_work_hours.py`
5. Modificar la función `process_employee_action` para calcular y actualizar horas
6. Modificar la función `record_checkout` para calcular y actualizar horas
7. Verificar que la función `adjust_record` maneje correctamente los ajustes
8. Ejecutar el script de migración para crear las tablas y columnas necesarias
9. Ejecutar el script de prueba para verificar la implementación

### 2. Para el Coste Horario de Empleados
1. Añadir el campo `hourly_employee_cost` a la clase `Company` en `models.py`
2. Modificar el formulario `CompanyForm` en `forms.py` para incluir el nuevo campo con validación
3. Actualizar el controlador de empresas para manejar el formato decimal español
4. Actualizar las plantillas para mostrar el coste por hora en las vistas de empresa

### 3. Para el Sistema de Resolución de Incidencias Masivas
1. Añadir la ruta `resolve_multiple_incidents` en `routes_checkpoints.py`
2. Crear las funciones auxiliares para cada tipo de resolución
3. Actualizar la plantilla `incidents.html` para incluir el formulario modal y checkboxes
4. Añadir el código JavaScript para gestionar la selección y procesamiento

## Cambios en la Interfaz

### 1. Modificación en `templates/company_detail.html` - Visualización de Coste Horario

Se ha actualizado la plantilla de detalle de empresa para mostrar el coste por hora de los empleados:

```html
<div class="row">
  <div class="col-md-6">
    <div class="card mb-4">
      <div class="card-header">
        <h5>Información General</h5>
      </div>
      <div class="card-body">
        <ul class="list-group list-group-flush">
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span>Nombre:</span>
            <span class="font-weight-bold">{{ company.name }}</span>
          </li>
          
          <!-- Nuevos campos -->
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span>Coste por Hora de Empleados:</span>
            <span class="font-weight-bold">{{ company.hourly_employee_cost|replace('.', ',') }} €</span>
          </li>
          
          <!-- Otros campos existentes -->
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span>Número de Empleados:</span>
            <span class="badge badge-primary badge-pill">{{ company.employees|length }}</span>
          </li>
        </ul>
      </div>
    </div>
  </div>
  
  <!-- Contenido adicional -->
</div>
```

### 2. Modificación en `templates/company_form.html` - Formulario de Coste Horario

Se ha actualizado el formulario de empresa para incluir el campo de coste por hora con instrucciones de formato:

```html
<div class="form-group">
  {{ form.hourly_employee_cost.label(class="form-control-label") }}
  {% if form.hourly_employee_cost.description %}
    <small class="form-text text-muted">{{ form.hourly_employee_cost.description }}</small>
  {% endif %}
  {{ form.hourly_employee_cost(class="form-control") }}
  {% if form.hourly_employee_cost.errors %}
    <div class="invalid-feedback d-block">
      {% for error in form.hourly_employee_cost.errors %}
        <span>{{ error }}</span>
      {% endfor %}
    </div>
  {% endif %}
</div>
```

### 3. Modificación en `templates/checkpoints/incidents.html` - Integración de Checkboxes

Se ha actualizado la tabla de incidencias para incluir las casillas de selección necesarias para la resolución masiva:

```html
<table class="table table-striped table-bordered">
  <thead class="thead-dark">
    <tr>
      <th style="width: 30px">
        <input type="checkbox" id="select-all" title="Seleccionar todos">
      </th>
      <th>Empleado</th>
      <th>Localización</th>
      <th>Fecha/Hora</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Acciones</th>
    </tr>
  </thead>
  <tbody>
    {% for record in records %}
    <tr>
      <td class="text-center">
        <input type="checkbox" class="record-checkbox" value="{{ record.id }}">
      </td>
      <td>{{ record.employee.full_name }}</td>
      <td>{{ record.checkpoint.name }}</td>
      <td>{{ record.check_in_time.strftime('%d/%m/%Y %H:%M') }}</td>
      <td>
        {% if record.missed_checkout %}
          <span class="badge badge-warning">Sin salida</span>
        {% else %}
          <span class="badge badge-info">{{ record.incident_type }}</span>
        {% endif %}
      </td>
      <td>
        {% if record.is_resolved %}
          <span class="badge badge-success">Resuelto</span>
        {% else %}
          <span class="badge badge-danger">Pendiente</span>
        {% endif %}
      </td>
      <td class="text-center">
        <a href="{{ url_for('view_record', record_id=record.id) }}" class="btn btn-sm btn-info">
          <i class="fas fa-eye"></i>
        </a>
        <a href="{{ url_for('resolve_incident', record_id=record.id) }}" class="btn btn-sm btn-primary">
          <i class="fas fa-check"></i>
        </a>
      </td>
    </tr>
    {% endfor %}
  </tbody>
</table>

<!-- Botón de acción masiva (inicialmente oculto) -->
<div class="fixed-bottom p-3 bg-light border-top d-none" id="mass-action-btn">
  <div class="container">
    <div class="d-flex justify-content-between align-items-center">
      <span><strong><span id="selected-records-count">0</span> registros</strong> seleccionados</span>
      <button type="button" class="btn btn-primary" data-toggle="modal" data-target="#masiveResolveModal">
        <i class="fas fa-tasks"></i> Resolver seleccionados
      </button>
    </div>
  </div>
</div>
```

## Consideraciones Adicionales

### 1. Script de Migración para Coste Horario

Para añadir el campo de coste horario a la base de datos existente, se ha creado un script de migración:

```python
"""
Script para añadir la columna hourly_employee_cost a la tabla companies
"""
from app import db, create_app
from sqlalchemy import text
import os

def add_hourly_employee_cost_column():
    """
    Añade la columna hourly_employee_cost a la tabla companies
    """
    try:
        app = create_app()
        with app.app_context():
            # Verificar si la columna ya existe
            query = text("SELECT column_name FROM information_schema.columns WHERE table_name='companies' AND column_name='hourly_employee_cost'")
            result = db.session.execute(query).fetchone()
            
            if result:
                print("✓ La columna 'hourly_employee_cost' ya existe en la tabla 'companies'")
                return True
            
            # Añadir la columna
            query = text("ALTER TABLE companies ADD COLUMN hourly_employee_cost NUMERIC(10,2) DEFAULT 0.00 NOT NULL")
            db.session.execute(query)
            db.session.commit()
            
            # Verificar que se haya creado
            query = text("SELECT column_name FROM information_schema.columns WHERE table_name='companies' AND column_name='hourly_employee_cost'")
            result = db.session.execute(query).fetchone()
            
            if result:
                print("✓ Columna 'hourly_employee_cost' añadida exitosamente a la tabla 'companies'")
                return True
            else:
                print("✗ Error al añadir la columna 'hourly_employee_cost'")
                return False
                
    except Exception as e:
        print(f"✗ Error al modificar la tabla: {str(e)}")
        return False

if __name__ == "__main__":
    add_hourly_employee_cost_column()
```

### 2. Funciones Auxiliares para Resolución Masiva

```python
def apply_auto_checkout(record, checkout_date, checkout_time, notes):
    """
    Aplica un checkout automático a un registro de fichaje
    
    Args:
        record: Registro de fichaje a procesar
        checkout_date: Fecha del checkout en formato 'YYYY-MM-DD'
        checkout_time: Hora del checkout en formato 'HH:MM'
        notes: Notas de resolución
        
    Returns:
        bool: True si se aplicó correctamente, False en caso contrario
    """
    try:
        # Convertir fecha y hora a datetime
        if not checkout_date or not checkout_time:
            # Si no se especificó fecha/hora, usar la fecha de entrada + 8 horas
            checkout_datetime = record.check_in_time + timedelta(hours=8)
        else:
            checkout_date_obj = datetime.strptime(checkout_date, '%Y-%m-%d').date()
            checkout_time_obj = datetime.strptime(checkout_time, '%H:%M').time()
            checkout_datetime = datetime.combine(checkout_date_obj, checkout_time_obj)
        
        # Verificar que la fecha de salida sea posterior a la de entrada
        if checkout_datetime <= record.check_in_time:
            checkout_datetime = record.check_in_time + timedelta(hours=8)
        
        # Actualizar el registro
        record.check_out_time = checkout_datetime
        record.resolution_notes = notes
        record.is_resolved = True
        record.is_auto_resolved = True
        record.resolved_at = datetime.now()
        record.resolved_by_id = current_user.id
        
        # Calcular horas trabajadas y actualizar acumulados
        hours_worked = calculate_hours_worked(record.check_in_time, checkout_datetime)
        update_employee_work_hours(record.employee_id, record.check_in_time, hours_worked)
        
        db.session.commit()
        return True
    except Exception as e:
        db.session.rollback()
        app.logger.error(f"Error al aplicar auto-checkout: {str(e)}")
        return False
        
def delete_incident_record(record, notes):
    """
    Marca un registro como eliminado (sin eliminarlo físicamente)
    
    Args:
        record: Registro de fichaje a procesar
        notes: Notas de resolución
        
    Returns:
        bool: True si se marcó correctamente, False en caso contrario
    """
    try:
        # Marcar como eliminado pero mantener en base de datos para auditoría
        record.is_deleted = True
        record.is_resolved = True
        record.resolution_notes = notes
        record.resolved_at = datetime.now()
        record.resolved_by_id = current_user.id
        
        db.session.commit()
        return True
    except Exception as e:
        db.session.rollback()
        app.logger.error(f"Error al eliminar registro: {str(e)}")
        return False
        
def mark_incident_as_resolved(record, notes):
    """
    Marca un registro como resuelto sin realizar cambios
    
    Args:
        record: Registro de fichaje a procesar
        notes: Notas de resolución
        
    Returns:
        bool: True si se marcó correctamente, False en caso contrario
    """
    try:
        record.is_resolved = True
        record.resolution_notes = notes
        record.resolved_at = datetime.now()
        record.resolved_by_id = current_user.id
        
        db.session.commit()
        return True
    except Exception as e:
        db.session.rollback()
        app.logger.error(f"Error al marcar como resuelto: {str(e)}")
        return False
```

### 3. Actualización de Información Sobre Script `test_work_hours.py`

El script `test_work_hours.py` se ha mejorado para verificar:

- La existencia de las tablas y columnas necesarias
- El cálculo de horas en diferentes escenarios (jornadas normales, nocturnas, con minutos extra)
- La funcionalidad de actualización de acumulados
- La visualización de los registros actuales de horas trabajadas

Este script es útil tanto para verificar la instalación como para probar la funcionalidad después de hacer cambios.

## Conclusión

Esta implementación proporciona un sistema completo con las siguientes características:

### 1. Sistema de Seguimiento de Horas Trabajadas
- Cálculo preciso de horas en distintos escenarios de fichajes 
- Acumulados automáticos a diferentes niveles (diario, semanal, mensual)
- Gestión adecuada de ajustes manuales
- Alto rendimiento gracias a la estrategia de índices optimizados

### 2. Gestión de Coste Horario de Empleados
- Configuración del coste por hora a nivel de empresa
- Validación de formato decimal español (con comas)
- Interfaz intuitiva para administrar los costes
- Migración automática para añadir el nuevo campo

### 3. Sistema de Resolución de Incidencias Masivas
- Interfaz para seleccionar múltiples incidencias simultáneamente
- Opciones configurables según el tipo de resolución
- Procesamiento eficiente para administradores
- Funciones auxiliares para diferentes tipos de resolución

Todo el código está debidamente documentado y sigue las mejores prácticas de programación, con énfasis en:

- Manejo seguro de transacciones
- Validación adecuada de datos
- Interfaz de usuario intuitiva
- Rendimiento optimizado para grandes volúmenes de datos
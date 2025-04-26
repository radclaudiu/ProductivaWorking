# Auditoría de Seguridad - Productiva

Este documento contiene un análisis exhaustivo de seguridad para todos los archivos del proyecto Productiva. El análisis se centra en identificar posibles vulnerabilidades, problemas de seguridad y mejores prácticas que deberían aplicarse.

## Metodología

Cada archivo ha sido examinado para detectar:
- Vulnerabilidades de seguridad comunes (inyección SQL, XSS, CSRF, etc.)
- Exposición de información sensible (credenciales, tokens, etc.)
- Configuraciones inseguras
- Uso de bibliotecas o métodos obsoletos
- Falta de validación de entrada o sanitización

## Resumen Ejecutivo

Se ha realizado un examen de los archivos clave del proyecto y se han identificado varias vulnerabilidades y problemas de seguridad que requieren atención.

## Hallazgos de Seguridad

### 1. Configuración y Arranque de la Aplicación

#### 1.1 `config.py`

**Severidad: Alta**
- **Problema**: Claves secretas hardcodeadas para desarrollo.
  - Línea 16: `SECRET_KEY = os.environ.get('SESSION_SECRET', 'dev-key-for-development')`
  - Línea 47: `JWT_SECRET_KEY = os.environ.get('SESSION_SECRET', 'dev-key-for-development')`
- **Riesgo**: Usar claves default conocidas en producción podría comprometer la seguridad de sesiones y tokens JWT.
- **Recomendación**: Asegurar que las claves secretas sean generadas aleatoriamente y nunca hardcodeadas, ni siquiera para desarrollo.

**Severidad: Baja**
- **Problema**: Logging configurado en nivel DEBUG.
  - Línea 8: `level=logging.DEBUG`
- **Riesgo**: En producción, podría revelar información sensible en los logs.
- **Recomendación**: Configurar el nivel de logging basado en el entorno (producción/desarrollo).

#### 1.2 `app.py`

**Severidad: Crítica**
- **Problema**: CSRF protección deshabilitada.
  - Línea 42: `app.config['WTF_CSRF_ENABLED'] = False`
- **Riesgo**: La aplicación es vulnerable a ataques CSRF, permitiendo a atacantes ejecutar acciones no autorizadas en nombre del usuario.
- **Recomendación**: Habilitar CSRF protection en todos los entornos.

**Severidad: Alta**
- **Problema**: Claves secretas hardcodeadas.
  - Línea 35: `app.secret_key = os.environ.get('SESSION_SECRET', 'dev-key-for-development-2025-secure')`
  - Línea 215: `app.secret_key = 'development-secret-key-2025-secure'`
- **Riesgo**: Compromiso de la seguridad de sesiones de usuario.
- **Recomendación**: Eliminar claves hardcodeadas y usar variables de entorno.

**Severidad: Alta**
- **Problema**: Configuración de modo DEBUG y TESTING en producción.
  - Línea 220-224: Configuraciones de depuración activas (`DEBUG = True`, `TESTING = True`)
- **Riesgo**: Expone información detallada de errores y stack traces a usuarios maliciosos.
- **Recomendación**: Desactivar estas opciones en producción.

**Severidad: Alta**
- **Problema**: Endpoint para ejecutar consultas SQL arbitrarias.
  - Función `db_query()` (línea 268)
- **Riesgo**: Si este endpoint no está adecuadamente protegido, podría permitir inyección SQL.
- **Recomendación**: Eliminar o restringir acceso a este endpoint a solo administradores autorizados.

**Severidad: Media**
- **Problema**: Endpoint expone traceback completo de errores.
  - Línea 136: `return render_template('errors/500.html', error=error, traceback=error_traceback)`
- **Riesgo**: Expone información interna del sistema a cualquier usuario.
- **Recomendación**: No mostrar tracebacks en producción, solo en logs del servidor.

### 2. Gestión de Exportación y Backup

#### 2.1 `export_database.py`

**Severidad: Alta**
- **Problema**: Uso inseguro de `subprocess` para ejecutar comandos de sistema.
  - Línea 93-98: `subprocess.Popen(pg_dump_cmd, env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE)`
- **Riesgo**: Posible inyección de comandos si los parámetros de la base de datos no están adecuadamente sanitizados.
- **Recomendación**: Validar y sanitizar todos los parámetros que se pasan a `pg_dump`.

**Severidad: Alta**
- **Problema**: Exposición de contraseñas en variables de entorno.
  - Línea 90: `env["PGPASSWORD"] = db_params['password']`
- **Riesgo**: Las contraseñas podrían quedar expuestas en logs de sistema o en la salida de comandos de debugging.
- **Recomendación**: Utilizar métodos más seguros para proporcionar credenciales, como archivos pgpass.

#### 2.2 `create_executable_backup.sh`

**Severidad: Media**
- **Problema**: Scripts de shell con posibles vulnerabilidades de inyección.
- **Riesgo**: Si los parámetros no son correctamente sanitizados, podría permitir inyección de comandos.
- **Recomendación**: Verificar que todos los parámetros estén correctamente entrecomillados y escapados.

### 3. Gestión de Archivos

#### 3.1 `utils.py` - Función `save_file`

**Severidad: Media**
- **Problema**: Potencial vulnerabilidad en la gestión de archivos subidos.
  - Línea 53: `file.save(file_path)`
- **Riesgo**: Aunque se utiliza `secure_filename`, podría haber riesgos de path traversal o tipos de archivo maliciosos.
- **Recomendación**: Reforzar la validación de tipos de archivo, implementar escaneo de contenido malicioso.

**Severidad: Alta**
- **Problema**: Credenciales hardcodeadas para usuario administrador.
  - Línea 25-31: Creación de usuario admin con contraseña fija.
- **Riesgo**: Acceso no autorizado a la aplicación con credenciales conocidas.
- **Recomendación**: Eliminar la creación automática de usuarios admin con contraseñas predefinidas.

### 4. Control de Acceso e Inyección SQL

#### 4.1 `app.py` - Función `db_query`

**Severidad: Crítica**
- **Problema**: Ejecución directa de consultas SQL sin sanitizar.
  - Línea 290: `cursor.execute(query_text)`
- **Riesgo**: Vulnerabilidad de inyección SQL severa, permitiendo a atacantes ejecutar consultas arbitrarias.
- **Recomendación**: Eliminar esta funcionalidad o implementar estrictos controles de acceso y sanitización.

**Severidad: Alta**
- **Problema**: Ausencia de restricciones de acceso al endpoint SQL.
  - No se verifica si el usuario tiene permisos de administrador.
- **Riesgo**: Cualquier usuario autenticado podría ejecutar consultas SQL arbitrarias.
- **Recomendación**: Restringir acceso solo a administradores verificados y registrar todas las consultas ejecutadas.

#### 4.2 `routes.py` - Funciones de control de acceso

**Severidad: Media**
- **Problema**: Verificaciones de acceso inconsistentes en varias rutas.
- **Riesgo**: Posibles escalaciones de privilegios o acceso no autorizado a ciertas funcionalidades.
- **Recomendación**: Revisar todas las rutas y aplicar consistentemente verificaciones de permisos.

### 5. Gestión de Autenticación y Sesiones

#### 5.1 `routes.py` - Funciones de autenticación

**Severidad: Alta**
- **Problema**: Contraseñas almacenadas usando hash sin sal (salt) específica.
  - Línea 57 en `models.py`: `self.password_hash = generate_password_hash(password)`
- **Riesgo**: Aunque se usa el método `generate_password_hash`, no se especifica un algoritmo de hashing fuerte ni un factor de costo adecuado.
- **Recomendación**: Especificar método `pbkdf2:sha256` con un coste alto (mínimo 160000 iteraciones).

**Severidad: Media**
- **Problema**: Mensajes de error demasiado explícitos en login.
  - Línea 67: `flash('Usuario o contraseña invalidos.', 'danger')`
- **Riesgo**: Permite a atacantes diferenciar entre usuarios existentes y contraseñas incorrectas (timing attacks).
- **Recomendación**: Usar mensajes de error genéricos como "Credenciales incorrectas".

#### 5.2 `app.py` - Configuración de sesiones

**Severidad: Alta**
- **Problema**: Duración de sesión muy prolongada sin renovación.
  - Línea 49 en `config.py`: `PERMANENT_SESSION_LIFETIME = timedelta(days=7)`
- **Riesgo**: Exposición prolongada en caso de robo de cookies de sesión.
- **Recomendación**: Reducir la duración de sesión e implementar renovación de tokens y cierre por inactividad.

#### 5.3 Gestión de permisos

**Severidad: Media**
- **Problema**: Decoradores de protección de rutas definidos en múltiples lugares.
  - Decoradores como `@admin_required` y `@manager_required` en `routes.py`.
  - Función `check_company_access` en `routes_cash_register_additional.py`.
- **Riesgo**: Inconsistencia en la aplicación de controles de acceso.
- **Recomendación**: Centralizar las verificaciones de permisos en un único módulo.

### 6. Cross-Site Scripting (XSS) y Exposición de Datos

#### 6.1 Templates HTML - Ausencia de Escape

**Severidad: Alta**
- **Problema**: No se usa el filtro `|escape` de forma consistente en las plantillas.
  - Múltiples archivos como `company_delete_confirm.html` y `employee_detail.html` muestran datos sin escape.
  - Ejemplo: `{{ company.name }}` en lugar de `{{ company.name|escape }}`
- **Riesgo**: Vulnerabilidad a ataques XSS si datos de entrada contienen código malicioso.
- **Recomendación**: Aplicar escape de forma predeterminada y usar `|safe` solo cuando sea absolutamente necesario.

**Severidad: Media**
- **Problema**: Posible vulnerabilidad XSS en serialización de datos a JavaScript.
  - En templates como `employee_history.html`: `{{ history|map(attribute='changed_at')|list|tojson }}`
  - En `dashboard.html`: `{{ stats.employees_by_contract | tojson }}`
- **Riesgo**: Aunque `tojson` ayuda a serializar datos, no garantiza protección completa contra XSS.
- **Recomendación**: Validar que todos los datos expuestos a JavaScript estén sanitizados.

#### 6.2 APIs y Respuestas JSON

**Severidad: Media**
- **Problema**: Errores de sistema expuestos directamente en respuestas JSON.
  - En `routes_checkpoints_new.py` (línea 762): `jsonify({'error': f'Error al generar el PDF: {str(e)}'}), 500`
- **Riesgo**: Exposición de detalles de implementación y posible información confidencial en errores.
- **Recomendación**: Usar mensajes de error genéricos y registrar los detalles en logs internos.

#### 6.3 Gestión de Datos Sensibles

**Severidad: Alta**
- **Problema**: Datos sensibles de empleados expuestos en múltiples vistas sin control adecuado.
  - Ejemplo en `checkin_form.html` (línea 84): `<p><strong>DNI/NIE:</strong> {{ employee.dni }}</p>`
- **Riesgo**: Exposición no autorizada de información personal identificable (PII).
- **Recomendación**: Implementar control de acceso granular a datos sensibles y enmascarar información cuando corresponda.

### 7. Validación de Entrada y Protección CSRF

#### 7.1 Protección CSRF Deshabilitada

**Severidad: Crítica**
- **Problema**: A pesar de tener tokens CSRF en los formularios, la protección está deshabilitada.
  - Línea 42 en `app.py`: `app.config['WTF_CSRF_ENABLED'] = False`
  - La mayoría de formularios incluyen tokens: `<input type="hidden" name="csrf_token" value="{{ csrf_token() }}" />`
- **Riesgo**: Vulnerabilidad a ataques CSRF en todas las operaciones POST de la aplicación.
- **Recomendación**: Habilitar la protección CSRF y aplicarla consistentemente en todos los formularios.

#### 7.2 Validación Insuficiente en Entradas de Usuario

**Severidad: Alta**
- **Problema**: Validación insuficiente de entradas de usuario en rutas API.
  - Múltiples endpoints aceptan datos directamente sin validación adicional más allá de WTForms.
- **Riesgo**: Posibles vulnerabilidades de inyección y bypass de validación del lado del cliente.
- **Recomendación**: Implementar validación de datos en el lado del servidor, independiente de WTForms.

### 8. Conclusiones y Recomendaciones Generales

A continuación se presentan las recomendaciones prioritarias para mejorar la seguridad de la aplicación:

1. **Prioridad Crítica**:
   - Habilitar protección CSRF inmediatamente
   - Eliminar la función de ejecución de consultas SQL arbitrarias
   - Implementar sanitización consistente contra XSS

2. **Prioridad Alta**:
   - Eliminar todas las credenciales hardcodeadas
   - Revisar y fortalecer los mecanismos de hash de contraseñas
   - Implementar validación más estricta para todas las entradas de usuario
   - Limitar la exposición de información sensible en templates

3. **Prioridad Media**:
   - Centralizar la gestión de permisos
   - Mejorar la gestión de errores para evitar exposición de información
   - Implementar timeout de sesiones más corto con renovación segura
   - Revisar y mejorar las prácticas de logging y auditoría

La aplicación contiene múltiples vulnerabilidades críticas que podrían comprometer datos sensibles y la integridad del sistema. Se recomienda abordar estos problemas antes de cualquier despliegue en producción.

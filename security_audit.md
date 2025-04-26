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

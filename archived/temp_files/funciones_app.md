# Funciones de la Aplicación

Esta tabla contiene todas las funciones definidas en la aplicación, junto con su ubicación y una breve descripción.

| Función | Archivo | Línea | Descripción |
|---------|---------|-------|-------------|
| `create_app` | app.py | 25 | Create and configure the Flask application. |
| `forbidden_page` | app.py | 87 |  |
| `page_not_found` | app.py | 91 |  |
| `server_error_page` | app.py | 95 |  |
| `load_locations` | app.py | 100 |  |
| `log_activity` | app.py | 137 |  |
| `after_request_handler` | app.py | 156 |  |
| `auto_close_pending_records` | close_operation_hours.py | 16 | Cierra automáticamente todos los registros pendientes de los puntos de fichaje |
| `init_app` | config.py | 52 |  |
| `generate_dni` | create_employees.py | 33 | Genera un DNI ficticio con formato español válido (8 números + letra) |
| `generate_bank_account` | create_employees.py | 41 | Genera un número de cuenta bancaria ficticio |
| `create_employees` | create_employees.py | 51 | Crea 30 empleados ficticios para la empresa 100RCS (ID 8) |
| `generate_dni` | create_remaining_employees.py | 24 | Genera un DNI ficticio con formato español válido (8 números + letra) |
| `generate_bank_account` | create_remaining_employees.py | 32 | Genera un número de cuenta bancaria ficticio |
| `create_remaining_employees` | create_remaining_employees.py | 42 | Crea los empleados ficticios restantes para la empresa 100RCS (ID 8) |
| `extract_functions_from_file` | extract_functions.py | 5 | Extrae todas las funciones definidas en un archivo Python, junto con sus docstrings. |
| `find_all_py_files` | extract_functions.py | 85 | Encuentra todos los archivos Python en un directorio y sus subdirectorios, |
| `create_functions_table` | extract_functions.py | 110 | Genera un archivo markdown con una tabla que contiene información sobre todas las funciones |
| `__init__` | forms.py | 36 |  |
| `validate_username` | forms.py | 50 |  |
| `validate_email` | forms.py | 55 |  |
| `validate_role` | forms.py | 60 |  |
| `__init__` | forms.py | 79 |  |
| `validate_username` | forms.py | 96 |  |
| `validate_email` | forms.py | 102 |  |
| `validate_role` | forms.py | 108 |  |
| `__init__` | forms.py | 152 |  |
| `validate_tax_id` | forms.py | 156 |  |
| `validate_name` | forms.py | 167 | Validar que el nombre no tenga caracteres especiales |
| `validate_end_date` | forms.py | 189 |  |
| `validate_status_end_date` | forms.py | 212 |  |
| `validate_end_time` | forms.py | 227 |  |
| `validate` | forms.py | 269 |  |
| `validate_check_out_time` | forms.py | 304 |  |
| `validate_end_date` | forms.py | 314 |  |
| `validate_end_date` | forms.py | 325 |  |
| `validate_end_date` | forms.py | 334 |  |
| `validate_password` | forms_checkpoints.py | 35 | Asegura que la contraseña y la confirmación coincidan |
| `validate_operation_end_time` | forms_checkpoints.py | 40 | Verifica que la hora de fin de operación sea posterior a la de inicio |
| `validate_weekly_hours` | forms_checkpoints.py | 96 | Verifica que las horas semanales sean coherentes con las diarias |
| `validate_normal_end_time` | forms_checkpoints.py | 101 | Verifica que la hora de fin sea posterior a la de inicio |
| `validate_check_out_time` | forms_checkpoints.py | 119 | Verifica que la hora de salida sea posterior a la de entrada |
| `validate_end_date` | forms_checkpoints.py | 141 | Verifica que la fecha de fin sea posterior a la de inicio |
| `validate_pin` | forms_tasks.py | 46 |  |
| `__init__` | forms_tasks.py | 71 |  |
| `validate_end_date` | forms_tasks.py | 77 |  |
| `validate` | forms_tasks.py | 81 |  |
| `validate_end_time` | forms_tasks.py | 100 |  |
| `validate_end_time` | forms_tasks.py | 110 |  |
| `validate_end_time` | forms_tasks.py | 123 |  |
| `validate_end_time` | forms_tasks.py | 132 |  |
| `validate_pin` | forms_tasks.py | 149 |  |
| `validate_end_time` | forms_tasks.py | 168 |  |
| `__init__` | forms_tasks.py | 204 |  |
| `__init__` | forms_tasks.py | 219 |  |
| `__init__` | forms_tmp.py | 36 |  |
| `validate_username` | forms_tmp.py | 50 |  |
| `validate_email` | forms_tmp.py | 55 |  |
| `validate_role` | forms_tmp.py | 60 |  |
| `__init__` | forms_tmp.py | 79 |  |
| `validate_username` | forms_tmp.py | 96 |  |
| `validate_email` | forms_tmp.py | 102 |  |
| `validate_role` | forms_tmp.py | 108 |  |
| `__init__` | forms_tmp.py | 137 |  |
| `validate_tax_id` | forms_tmp.py | 141 |  |
| `validate_end_date` | forms_tmp.py | 168 |  |
| `validate_status_end_date` | forms_tmp.py | 191 |  |
| `validate_end_time` | forms_tmp.py | 206 |  |
| `validate` | forms_tmp.py | 248 |  |
| `validate_check_out_time` | forms_tmp.py | 283 |  |
| `validate_end_date` | forms_tmp.py | 293 |  |
| `validate_end_date` | forms_tmp.py | 304 |  |
| `validate_end_date` | forms_tmp.py | 313 |  |
| `run_migration` | migrate_auto_checkout_removal.py | 8 | Elimina la columna auto_checkout_time de la tabla checkpoints |
| `create_checkpoint_tables` | migrate_checkpoints.py | 8 | Función para crear las tablas de puntos de fichaje |
| `add_activation_columns` | migrate_contract_hours_activation.py | 18 | Añade las columnas use_normal_schedule y use_flexibility a la tabla employee_contract_hours |
| `add_is_on_shift_column` | migrate_employee_on_shift.py | 10 | Añade la columna is_on_shift a la tabla employees |
| `create_label_tables` | migrate_label_templates.py | 17 | Crea las tablas relacionadas con las etiquetas si no existen |
| `migrate` | migrate_operation_hours.py | 10 | Agrega los campos necesarios para la configuración de horario de funcionamiento: |
| `create_original_records_table` | migrate_original_records.py | 8 | Crea la tabla para almacenar registros originales de fichajes |
| `create_task_instances_table` | migrate_task_instances.py | 9 | Crea la tabla para almacenar instancias de tareas programadas |
| `get_engine` | migrations/env.py | 18 |  |
| `get_engine_url` | migrations/env.py | 27 |  |
| `get_metadata` | migrations/env.py | 48 |  |
| `run_migrations_offline` | migrations/env.py | 54 | Run migrations in 'offline' mode. |
| `run_migrations_online` | migrations/env.py | 75 | Run migrations in 'online' mode. |
| `process_revision_directives` | migrations/env.py | 86 |  |
| `upgrade` | migrations/versions/add_bank_account_to_companies.py | 19 |  |
| `downgrade` | migrations/versions/add_bank_account_to_companies.py | 40 |  |
| `company_id` | models.py | 41 |  |
| `company` | models.py | 47 |  |
| `set_password` | models.py | 56 |  |
| `check_password` | models.py | 59 |  |
| `is_admin` | models.py | 62 |  |
| `is_gerente` | models.py | 65 |  |
| `is_empleado` | models.py | 68 |  |
| `__repr__` | models.py | 71 |  |
| `to_dict` | models.py | 74 |  |
| `load_user` | models.py | 87 |  |
| `__repr__` | models.py | 114 |  |
| `get_slug` | models.py | 117 | Obtiene el slug (URL amigable) del nombre de la empresa |
| `to_dict` | models.py | 122 |  |
| `__repr__` | models.py | 201 |  |
| `to_dict` | models.py | 204 |  |
| `__repr__` | models.py | 241 |  |
| `__repr__` | models.py | 258 |  |
| `__repr__` | models.py | 276 |  |
| `__repr__` | models.py | 294 |  |
| `to_dict` | models.py | 297 |  |
| `__repr__` | models.py | 322 |  |
| `to_dict` | models.py | 325 |  |
| `generate_realistic_time` | models.py | 337 | Generate a realistic check-in or check-out time with random variation. |
| `generate_check_ins_for_schedule` | models.py | 354 | Generate check-ins for an employee based on their schedule. |
| `__repr__` | models.py | 451 |  |
| `to_dict` | models.py | 454 |  |
| `total_days` | models.py | 468 | Calculate the total number of days in this vacation period. |
| `mark_as_signed` | models.py | 474 | Mark the vacation as signed by the employee. |
| `mark_as_enjoyed` | models.py | 479 | Mark the vacation as enjoyed after the date has passed. |
| `overlaps_with` | models.py | 484 | Check if this vacation period overlaps with the given dates. |
| `__repr__` | models.py | 500 |  |
| `__repr__` | models_checkpoints.py | 58 |  |
| `set_password` | models_checkpoints.py | 61 |  |
| `verify_password` | models_checkpoints.py | 64 |  |
| `__repr__` | models_checkpoints.py | 102 |  |
| `duration` | models_checkpoints.py | 106 | Calcula la duración del fichaje en horas |
| `has_original_record` | models_checkpoints.py | 129 | Comprueba si este registro tiene un registro original asociado |
| `to_dict` | models_checkpoints.py | 134 | Convierte el registro a un diccionario para serialización |
| `__repr__` | models_checkpoints.py | 175 |  |
| `resolve` | models_checkpoints.py | 178 | Marca la incidencia como resuelta |
| `duration` | models_checkpoints.py | 214 | Calcula la duración del fichaje original en horas |
| `__repr__` | models_checkpoints.py | 236 |  |
| `to_dict` | models_checkpoints.py | 239 | Convierte el registro a un diccionario para serialización |
| `__repr__` | models_checkpoints.py | 287 |  |
| `is_overtime` | models_checkpoints.py | 290 | Comprueba si una duración de horas supera el máximo diario |
| `calculate_adjusted_hours` | models_checkpoints.py | 294 | Calcula el tiempo ajustado según el contrato y configuración |
| `__repr__` | models_tasks.py | 53 |  |
| `to_dict` | models_tasks.py | 56 |  |
| `__repr__` | models_tasks.py | 93 |  |
| `set_portal_password` | models_tasks.py | 96 | Establece una contraseña encriptada para el portal |
| `check_portal_password` | models_tasks.py | 100 | Verifica si la contraseña proporcionada coincide con la almacenada |
| `portal_fixed_username` | models_tasks.py | 109 | Retorna el nombre de usuario para este local |
| `portal_fixed_password` | models_tasks.py | 115 | Retorna la contraseña para este local |
| `to_dict` | models_tasks.py | 125 |  |
| `__repr__` | models_tasks.py | 159 |  |
| `set_pin` | models_tasks.py | 162 |  |
| `check_pin` | models_tasks.py | 166 |  |
| `get_full_name` | models_tasks.py | 169 |  |
| `to_dict` | models_tasks.py | 172 |  |
| `__repr__` | models_tasks.py | 214 |  |
| `to_dict` | models_tasks.py | 217 |  |
| `is_due_today` | models_tasks.py | 231 | Comprueba si la tarea está programada para hoy según su programación. |
| `__repr__` | models_tasks.py | 297 |  |
| `is_active_for_date` | models_tasks.py | 305 | Comprueba si este horario está activo para una fecha determinada. |
| `__repr__` | models_tasks.py | 350 |  |
| `day_matches_today` | models_tasks.py | 354 | Comprueba si el día de la semana corresponde al día actual |
| `__repr__` | models_tasks.py | 384 |  |
| `to_dict` | models_tasks.py | 387 |  |
| `__repr__` | models_tasks.py | 411 |  |
| `to_dict` | models_tasks.py | 414 |  |
| `__repr__` | models_tasks.py | 456 |  |
| `to_dict` | models_tasks.py | 459 |  |
| `get_shelf_life_expiry` | models_tasks.py | 470 | Calcula la fecha de caducidad secundaria basada en la vida útil en días |
| `__repr__` | models_tasks.py | 499 |  |
| `to_dict` | models_tasks.py | 502 |  |
| `get_expiry_date` | models_tasks.py | 511 | Calcula la fecha de caducidad basada en horas |
| `get_expiry_datetime` | models_tasks.py | 516 | Calcula el datetime exacto de caducidad, incluyendo la hora |
| `__repr__` | models_tasks.py | 577 |  |
| `to_dict` | models_tasks.py | 580 |  |
| `__repr__` | models_tasks.py | 629 |  |
| `to_dict` | models_tasks.py | 632 |  |
| `reset_checkpoint_password` | reset_checkpoint_password.py | 5 | Resetea la contraseña del punto de fichaje con username 'movil' a 'movil' |
| `admin_required` | routes.py | 36 |  |
| `decorated_function` | routes.py | 38 |  |
| `manager_required` | routes.py | 46 |  |
| `decorated_function` | routes.py | 48 |  |
| `login` | routes.py | 57 |  |
| `logout` | routes.py | 82 |  |
| `register` | routes.py | 90 |  |
| `index` | routes.py | 128 |  |
| `dashboard` | routes.py | 135 |  |
| `profile` | routes.py | 141 |  |
| `search` | routes.py | 159 |  |
| `list_companies` | routes.py | 217 |  |
| `view_company` | routes.py | 234 |  |
| `create_company` | routes.py | 259 |  |
| `edit_company` | routes.py | 287 |  |
| `export_company_data` | routes.py | 335 |  |
| `delete_company` | routes.py | 367 |  |
| `list_employees` | routes.py | 548 |  |
| `view_employee` | routes.py | 611 |  |
| `create_employee` | routes.py | 623 |  |
| `edit_employee` | routes.py | 682 |  |
| `delete_employee` | routes.py | 769 |  |
| `manage_status` | routes.py | 805 |  |
| `list_documents` | routes.py | 855 |  |
| `upload_document` | routes.py | 871 |  |
| `download_document` | routes.py | 909 |  |
| `delete_document` | routes.py | 928 |  |
| `manage_notes` | routes.py | 951 |  |
| `backup_database` | routes.py | 984 | Create a database backup |
| `delete_note` | routes.py | 1002 |  |
| `view_history` | routes.py | 1020 |  |
| `list_users` | routes.py | 1037 |  |
| `edit_user` | routes.py | 1043 |  |
| `reset_password` | routes.py | 1092 |  |
| `toggle_activation` | routes.py | 1111 |  |
| `delete_user` | routes.py | 1134 |  |
| `list_schedules` | routes.py | 1158 |  |
| `create_schedule` | routes.py | 1175 |  |
| `weekly_schedule` | routes.py | 1207 |  |
| `edit_schedule` | routes.py | 1283 |  |
| `delete_schedule` | routes.py | 1315 |  |
| `list_checkins` | routes.py | 1335 |  |
| `get_month_year` | routes.py | 1353 |  |
| `create_checkin` | routes.py | 1386 |  |
| `edit_checkin` | routes.py | 1425 |  |
| `delete_checkin` | routes.py | 1470 |  |
| `export_checkins` | routes.py | 1489 |  |
| `delete_checkins_by_date` | routes.py | 1523 |  |
| `generate_checkins` | routes.py | 1576 |  |
| `list_vacations` | routes.py | 1619 |  |
| `create_vacation` | routes.py | 1636 |  |
| `delete_vacation` | routes.py | 1687 |  |
| `admin_required` | routes_checkpoints.py | 30 |  |
| `decorated_function` | routes_checkpoints.py | 32 |  |
| `manager_required` | routes_checkpoints.py | 40 |  |
| `decorated_function` | routes_checkpoints.py | 42 |  |
| `checkpoint_required` | routes_checkpoints.py | 53 |  |
| `decorated_function` | routes_checkpoints.py | 55 |  |
| `select_company` | routes_checkpoints.py | 66 | Página de selección de empresa para el sistema de fichajes |
| `index_company` | routes_checkpoints.py | 87 | Página principal del sistema de fichajes para una empresa específica |
| `list_checkpoints` | routes_checkpoints.py | 280 | Lista todos los puntos de fichaje disponibles |
| `create_checkpoint` | routes_checkpoints.py | 312 | Crea un nuevo punto de fichaje |
| `edit_checkpoint` | routes_checkpoints.py | 382 | Edita un punto de fichaje existente |
| `delete_checkpoint` | routes_checkpoints.py | 441 | Elimina un punto de fichaje con todos sus registros asociados |
| `list_checkpoint_records` | routes_checkpoints.py | 497 | Muestra los registros de fichaje de un punto específico |
| `manage_contract_hours` | routes_checkpoints.py | 519 | Gestiona la configuración de horas por contrato de un empleado |
| `adjust_record` | routes_checkpoints.py | 557 | Ajusta manualmente un registro de fichaje |
| `record_signature` | routes_checkpoints.py | 648 | Permite al empleado firmar un registro de fichaje |
| `list_records_all` | routes_checkpoints.py | 687 | Muestra todos los registros de fichaje disponibles según permisos |
| `list_incidents` | routes_checkpoints.py | 785 | Muestra todas las incidencias de fichaje según permisos |
| `resolve_incident` | routes_checkpoints.py | 864 | Marca una incidencia como resuelta |
| `view_original_records` | routes_checkpoints.py | 907 | Página secreta para ver los registros originales de fichaje para una empresa específica |
| `export_records` | routes_checkpoints.py | 1024 | Exporta registros de fichaje a PDF |
| `login` | routes_checkpoints.py | 1098 | Página de login para puntos de fichaje |
| `login_to_checkpoint` | routes_checkpoints.py | 1145 | Acceso directo a un punto de fichaje específico por ID |
| `logout` | routes_checkpoints.py | 1179 | Cierra la sesión del punto de fichaje |
| `checkpoint_dashboard` | routes_checkpoints.py | 1193 | Dashboard principal del punto de fichaje |
| `employee_pin` | routes_checkpoints.py | 1214 | Página para introducir el PIN del empleado |
| `create_schedule_incident` | routes_checkpoints.py | 1284 | Crea una incidencia relacionada con horarios |
| `process_employee_action` | routes_checkpoints.py | 1296 | Función auxiliar para procesar las acciones de entrada/salida |
| `fix_employee_state_inconsistency` | routes_checkpoints.py | 1484 | Función auxiliar para detectar y corregir inconsistencias entre |
| `record_details` | routes_checkpoints.py | 1513 | Muestra los detalles de un registro recién creado |
| `record_checkout` | routes_checkpoints.py | 1535 | Registra la salida para un fichaje pendiente desde la pantalla de detalles |
| `checkpoint_record_signature` | routes_checkpoints.py | 1662 | Permite al empleado firmar un registro de fichaje desde el punto de fichaje |
| `daily_report` | routes_checkpoints.py | 1733 | Muestra un informe de fichajes del día actual |
| `get_company_employees` | routes_checkpoints.py | 1777 | Devuelve la lista de empleados de la empresa en formato JSON |
| `validate_pin` | routes_checkpoints.py | 1816 | Validar el PIN del empleado mediante AJAX |
| `trigger_auto_checkout` | routes_checkpoints.py | 1874 | Endpoint para informar que el sistema de auto-checkout ha sido eliminado |
| `check_credentials` | routes_checkpoints.py | 1885 | Endpoint temporal para comprobar credenciales |
| `init_app` | routes_checkpoints.py | 1907 |  |
| `admin_required` | routes_checkpoints_new.py | 29 |  |
| `decorated_function` | routes_checkpoints_new.py | 31 |  |
| `manager_required` | routes_checkpoints_new.py | 39 |  |
| `decorated_function` | routes_checkpoints_new.py | 41 |  |
| `checkpoint_required` | routes_checkpoints_new.py | 52 |  |
| `decorated_function` | routes_checkpoints_new.py | 54 |  |
| `view_original_records` | routes_checkpoints_new.py | 66 | Página secreta para ver los registros originales antes de ajustes de una empresa específica |
| `__init__` | routes_checkpoints_new.py | 171 |  |
| `pages` | routes_checkpoints_new.py | 178 |  |
| `has_prev` | routes_checkpoints_new.py | 182 |  |
| `has_next` | routes_checkpoints_new.py | 186 |  |
| `prev_num` | routes_checkpoints_new.py | 190 |  |
| `next_num` | routes_checkpoints_new.py | 194 |  |
| `edit_original_record` | routes_checkpoints_new.py | 226 | Edita un registro original |
| `restore_original_record` | routes_checkpoints_new.py | 315 | Restaura los valores originales en el registro actual |
| `delete_original_record` | routes_checkpoints_new.py | 365 | Elimina un registro original |
| `export_original_records` | routes_checkpoints_new.py | 406 | Exporta los registros originales a PDF |
| `export_original_records_pdf` | routes_checkpoints_new.py | 502 | Genera un PDF con los registros originales agrupados por semanas (lunes a domingo) |
| `get_week_start` | routes_checkpoints_new.py | 514 | Retorna la fecha del lunes de la semana a la que pertenece date_obj |
| `get_week_end` | routes_checkpoints_new.py | 521 | Retorna la fecha del domingo de la semana a la que pertenece date_obj |
| `header` | routes_checkpoints_new.py | 529 |  |
| `footer` | routes_checkpoints_new.py | 562 |  |
| `login_to_checkpoint` | routes_checkpoints_new.py | 719 | Acceso directo a un punto de fichaje específico por ID |
| `manager_required` | routes_tasks.py | 27 |  |
| `decorated_function` | routes_tasks.py | 29 |  |
| `local_user_required` | routes_tasks.py | 41 |  |
| `decorated_function` | routes_tasks.py | 43 |  |
| `index` | routes_tasks.py | 53 | Dashboard principal del módulo de tareas |
| `list_locations` | routes_tasks.py | 79 | Lista de locales disponibles |
| `create_location` | routes_tasks.py | 95 | Crear un nuevo local |
| `edit_location` | routes_tasks.py | 140 | Editar un local existente |
| `delete_location` | routes_tasks.py | 195 | Eliminar un local |
| `list_task_groups` | routes_tasks.py | 220 | Lista de grupos de tareas para un local |
| `create_task_group` | routes_tasks.py | 240 | Crear un nuevo grupo de tareas |
| `edit_task_group` | routes_tasks.py | 276 | Editar un grupo de tareas existente |
| `delete_task_group` | routes_tasks.py | 309 | Eliminar un grupo de tareas |
| `view_location` | routes_tasks.py | 338 | Ver detalles de un local |
| `list_local_users` | routes_tasks.py | 421 | Lista de usuarios de un local |
| `create_local_user` | routes_tasks.py | 440 | Crear un nuevo usuario local |
| `edit_local_user` | routes_tasks.py | 496 | Editar un usuario local existente |
| `delete_local_user` | routes_tasks.py | 562 | Eliminar un usuario local |
| `list_tasks` | routes_tasks.py | 599 | Lista de tareas de un local |
| `create_task` | routes_tasks.py | 636 | Crear una nueva tarea |
| `configure_daily_schedule` | routes_tasks.py | 717 | Configurar horario diario para una tarea |
| `configure_weekly_schedule` | routes_tasks.py | 765 | Configurar horario semanal para una tarea |
| `configure_monthly_schedule` | routes_tasks.py | 815 | Configurar horario mensual para una tarea |
| `configure_biweekly_schedule` | routes_tasks.py | 865 | Configurar horario quincenal para una tarea |
| `edit_task` | routes_tasks.py | 913 | Editar una tarea existente |
| `delete_task` | routes_tasks.py | 1025 | Eliminar una tarea |
| `view_task` | routes_tasks.py | 1050 | Ver detalles de una tarea |
| `portal_selection` | routes_tasks.py | 1077 | Página de selección de portal |
| `portal_test` | routes_tasks.py | 1094 | Ruta de prueba para diagnóstico |
| `portal_login` | routes_tasks.py | 1126 | Página de login para acceder al portal de un local |
| `local_login` | routes_tasks.py | 1159 | Página de login para usuarios locales |
| `local_portal` | routes_tasks.py | 1182 | Portal de acceso para un local |
| `local_user_login` | routes_tasks.py | 1230 | Login con PIN para empleado local |
| `local_logout` | routes_tasks.py | 1261 | Cerrar sesión de usuario local |
| `portal_logout` | routes_tasks.py | 1273 | Cerrar sesión de portal local |
| `local_user_tasks` | routes_tasks.py | 1290 | Panel de tareas para usuario local |
| `task_is_due_on_date` | routes_tasks.py | 1411 |  |
| `complete_task` | routes_tasks.py | 1515 | Marcar una tarea como completada (versión con formulario) |
| `ajax_complete_task` | routes_tasks.py | 1563 | Marcar una tarea como completada (versión AJAX) |
| `regenerate_password` | routes_tasks.py | 1618 | Devuelve la contraseña fija del portal de un local |
| `update_portal_credentials` | routes_tasks.py | 1635 | Actualiza la contraseña personalizada del portal para un local |
| `get_portal_credentials` | routes_tasks.py | 1669 | Obtiene las credenciales fijas del portal mediante AJAX de forma segura |
| `task_stats` | routes_tasks.py | 1701 | API para obtener estadísticas de tareas |
| `local_user_labels` | routes_tasks.py | 1775 | Generador de etiquetas para productos - Lista de productos disponibles |
| `product_conservation_selection` | routes_tasks.py | 1799 | Selección de tipo de conservación para un producto específico |
| `manage_labels` | routes_tasks.py | 1828 | Gestor de etiquetas para la página de tareas, filtrado por ubicación si se especifica |
| `label_editor` | routes_tasks.py | 1893 | Editor de diseño de etiquetas para un local |
| `list_label_templates` | routes_tasks.py | 1937 | Lista de plantillas de etiquetas para un local |
| `create_label_template` | routes_tasks.py | 1956 | Crear una nueva plantilla de etiquetas |
| `edit_label_template` | routes_tasks.py | 2014 | Editar una plantilla de etiquetas existente |
| `delete_label_template` | routes_tasks.py | 2046 | Eliminar una plantilla de etiquetas |
| `set_default_label_template` | routes_tasks.py | 2074 | Establecer una plantilla como predeterminada |
| `download_excel_template` | routes_tasks.py | 2100 | Descargar plantilla vacía en Excel para importación de productos |
| `export_labels_excel` | routes_tasks.py | 2145 | Exportar lista de productos y tipos de conservación a Excel |
| `import_labels_excel` | routes_tasks.py | 2217 | Importar lista de productos y tipos de conservación desde Excel |
| `generate_labels` | routes_tasks.py | 2356 | Endpoint simplificado para generar e imprimir etiquetas directamente |
| `list_products` | routes_tasks.py | 2523 | Lista de productos, filtrada por ubicación si se especifica |
| `create_product` | routes_tasks.py | 2574 | Crear nuevo producto, opcionalmente preseleccionando una ubicación |
| `edit_product` | routes_tasks.py | 2631 | Editar producto existente |
| `manage_product_conservations` | routes_tasks.py | 2681 | Gestionar tipos de conservación para un producto |
| `verificar_acceso_bd` | run_checkpoint_closer.py | 27 | Verifica que podemos acceder a la base de datos y que las tablas necesarias están configuradas. |
| `run_once` | run_checkpoint_closer.py | 64 | Ejecuta una verificación única de puntos de fichaje fuera de horario |
| `verificar_sistema` | scheduled_checkpoints_closer.py | 38 | Verifica que el sistema está correctamente configurado y puede acceder a la base de datos. |
| `run_service` | scheduled_checkpoints_closer.py | 62 | Ejecuta el servicio de verificación periódica para el cierre de fichajes |
| `list_checkpoints_with_hours` | test_close_operation_hours.py | 16 | Muestra todos los puntos de fichaje con horarios configurados |
| `force_close_checkpoint_records` | test_close_operation_hours.py | 44 | Fuerza el cierre de todos los registros pendientes para un punto de fichaje específico |
| `main` | test_close_operation_hours.py | 131 |  |
| `get_current_time` | timezone_config.py | 12 | Obtiene la hora actual en la zona horaria configurada (Madrid) |
| `datetime_to_madrid` | timezone_config.py | 21 | Convierte un objeto datetime a la zona horaria de Madrid |
| `format_datetime` | timezone_config.py | 36 | Formatea un datetime en la zona horaria de Madrid |
| `add_shelf_life_days_column` | update_db.py | 5 | Añade la columna shelf_life_days a la tabla products si no existe. |
| `create_admin_user` | utils.py | 19 | Create admin user if not exists. |
| `allowed_file` | utils.py | 36 | Check if file extension is allowed. |
| `save_file` | utils.py | 41 | Save uploaded file to filesystem and return path. |
| `log_employee_change` | utils.py | 65 | Log changes to employee data. |
| `log_activity` | utils.py | 77 | Log user activity. |
| `can_manage_company` | utils.py | 98 | Check if current user can manage the company. |
| `can_manage_employee` | utils.py | 114 | Check if current user can manage the employee. |
| `can_view_employee` | utils.py | 133 | Check if current user can view the employee. |
| `generate_checkins_pdf` | utils.py | 152 | Generate a PDF with employee check-ins between dates. |
| `export_company_employees_zip` | utils.py | 284 | Export all employees and their documents in a ZIP file. |
| `create_employee_summary_pdf` | utils.py | 415 | Create a PDF summary of an employee. |
| `slugify` | utils.py | 499 | Convierte un texto a formato slug (URL amigable) |
| `get_dashboard_stats` | utils.py | 528 | Get statistics for dashboard (optimizado). |
| `get_task_stats` | utils.py | 666 |  |
| `create_database_backup` | utils.py | 724 | Create a backup of the database |
| `__init__` | utils_checkpoints.py | 13 |  |
| `header` | utils_checkpoints.py | 24 |  |
| `footer` | utils_checkpoints.py | 52 |  |
| `draw_signature` | utils_checkpoints.py | 71 | Dibuja la firma en el PDF desde datos base64 |
| `generate_pdf_report` | utils_checkpoints.py | 99 | Genera un informe PDF de los registros de fichaje |
| `create_default_local_user` | utils_tasks.py | 8 | Crea un usuario local por defecto si no existe ninguno para la ubicación. |
| `get_portal_session` | utils_tasks.py | 42 | Obtiene información de la sesión del portal. |
| `clear_portal_session` | utils_tasks.py | 51 | Limpia la sesión del portal. |
| `generate_secure_password` | utils_tasks.py | 62 | Genera una contraseña segura con el formato estandarizado 'Portal[ID]2025!'. |
| `regenerate_portal_password` | utils_tasks.py | 88 | Regenera y actualiza la contraseña del portal de una ubicación. |
| `check_pending_records_after_hours` | verify_checkpoint_closures.py | 18 | Busca fichajes pendientes en puntos de fichaje que deberían haberse cerrado |


## Detalles de las Funciones


### app.py

#### `create_app` (línea 25)

**Docstring:**
```
Create and configure the Flask application.
```

**Código:**
```python
def create_app(config_class='config.Config'):
    """Create and configure the Flask application."""
    app = Flask(__name__)
    
    # Load configuration
    app.config.from_object(config_class)
    
    # Initialize extensions
    db.init_app(app)
    login_manager.init_app(app)
    # Temporarily disable CSRF for debugging
    app.config['WTF_CSRF_ENABLED'] = False
    csrf.init_app(app)
    migrate.init_app(app, db)
    
    # Configure login
    login_manager.login_view = 'auth.login'
    login_manager.login_message = 'Por favor, inicie sesión para acceder a esta página.'
    login_manager.login_message_category = 'warning'
    
    # Import models to ensure they're registered with SQLAlchemy
    with app.app_context():
        from models import (User, Company, Employee, ActivityLog, EmployeeDocument,
                           EmployeeNote, EmployeeHistory, EmployeeSchedule, 
                           EmployeeCheckIn, EmployeeVacation)
        # Import task models
        from models_tasks import (Location, LocalUser, Task, TaskSchedule, TaskCompletion, 
                                TaskPriority, TaskFrequency, TaskStatus, WeekDay, TaskGroup,
                                Product, ProductConservation, ProductLabel, ConservationType)
                                
        # Import checkpoint models
        from models_checkpoints import (CheckPoint, CheckPointRecord, CheckPointIncident, 
                                      EmployeeContractHours, CheckPointStatus, CheckPointIncidentType)
        
        # Create admin user if it doesn't exist
        from utils import create_admin_user
        create_admin_user()
    
    # Register blueprints
    from routes import (auth_bp, main_bp, company_bp, employee_bp, user_bp, 
                       schedule_bp, checkin_bp, vacation_bp, ui_bp)
    from routes_tasks import tasks_bp
    from routes_checkpoints import init_app as init_checkpoints_app
    from routes_checkpoints_new import checkpoints_bp as checkpoints_new_bp
    
    app.register_blueprint(auth_bp)
    app.register_blueprint(main_bp)
    app.register_blueprint(company_bp)
    app.register_blueprint(employee_bp)
    app.register_blueprint(user_bp)
    app.register_blueprint(schedule_bp)
    app.register_blueprint(checkin_bp)
    app.register_blueprint(vacation_bp)
    app.register_blueprint(ui_bp)
    app.register_blueprint(tasks_bp, url_prefix='/tasks')
    app.register_blueprint(checkpoints_new_bp)
    
    # Inicializar el sistema de puntos de fichaje
    init_checkpoints_app(app)
    
    # Register error handlers
    @app.errorhandler(403)
    def forbidden_page(error):
        return render_template('errors/403.html'), 403
        
    @app.errorhandler(404)
    def page_not_found(error):
        return render_template('errors/404.html'), 404
        
    @app.errorhandler(500)
    def server_error_page(error):
        return render_template('errors/500.html'), 500
    
    # Cargar ubicaciones disponibles para menú de navegación
    @app.before_request
    def load_locations():
        from flask import g
        from models_tasks import Location
        from flask_login import current_user
        from sqlalchemy import or_
        
        # Inicializar la lista de ubicaciones vacía por defecto
        g.locations = []
        
        # Solo cargar ubicaciones si el usuario está autenticado
        if current_user.is_authenticated:
            try:
                if current_user.is_admin():
                    # Administradores ven todas las ubicaciones
                    g.locations = Location.query.filter_by(is_active=True).all()
                elif current_user.is_gerente():
                    # Gerentes ven ubicaciones de todas sus empresas
                    if not current_user.companies:
                        return  # No hay empresas asignadas
                    
                    # Obtener todos los IDs de las empresas del usuario
                    company_ids = [company.id for company in current_user.companies]
                    
                    # Buscar ubicaciones de todas las empresas asignadas al gerente
                    g.locations = Location.query.filter(
                        Location.company_id.in_(company_ids),
                        Location.is_active == True
                    ).all()
            except Exception as e:
                # Si hay un error, no mostrar ubicaciones
                g.locations = []
                logger.error(f"Error al cargar ubicaciones: {e}")
    
    # Log activity middleware (optimizado para reducir DB commits)
    # Usar before_request y after_request para solo hacer commit después de la solicitud
    
    @app.before_request
    def log_activity():
        # Ignorar solicitudes estáticas y de monitoreo frecuentes
        if (request.path.startswith('/static') or 
            request.path == '/favicon.ico' or 
            request.path == '/health'):
            return  # Skip logging
            
        if current_user.is_authenticated:
            try:
                # Solo registrar acciones interesantes (no GETs a páginas comunes)
                if request.method != 'GET' or not request.path.startswith(('/dashboard', '/employees')):
                    # Usar la función de utils.py para registrar en lugar de hacerlo directamente aquí
                    from utils import log_activity as log_activity_util
                    log_activity_util(f"{request.method} {request.path}")
            except Exception as e:
                # Si hay un error, registrar pero no interrumpir el flujo
                logger.error(f"Error al registrar actividad: {e}")
    
    @app.after_request
    def after_request_handler(response):
        # Mantenemos esto por compatibilidad pero la lógica se movió a utils.py
        return response
    
    # Initialize app config
    from config import Config
    Config.init_app(app)
    
    return app

# Import these here to avoid circular imports
from flask import render_template
from flask_login import current_user

# Create the application instance
app = create_app()

```

#### `forbidden_page` (línea 87)

**Código:**
```python
    def forbidden_page(error):
        return render_template('errors/403.html'), 403
        
    @app.errorhandler(404)
```

#### `page_not_found` (línea 91)

**Código:**
```python
    def page_not_found(error):
        return render_template('errors/404.html'), 404
        
    @app.errorhandler(500)
```

#### `server_error_page` (línea 95)

**Código:**
```python
    def server_error_page(error):
        return render_template('errors/500.html'), 500
    
    # Cargar ubicaciones disponibles para menú de navegación
    @app.before_request
```

#### `load_locations` (línea 100)

**Código:**
```python
    def load_locations():
        from flask import g
        from models_tasks import Location
        from flask_login import current_user
        from sqlalchemy import or_
        
        # Inicializar la lista de ubicaciones vacía por defecto
        g.locations = []
        
        # Solo cargar ubicaciones si el usuario está autenticado
        if current_user.is_authenticated:
            try:
                if current_user.is_admin():
                    # Administradores ven todas las ubicaciones
                    g.locations = Location.query.filter_by(is_active=True).all()
                elif current_user.is_gerente():
                    # Gerentes ven ubicaciones de todas sus empresas
                    if not current_user.companies:
                        return  # No hay empresas asignadas
                    
                    # Obtener todos los IDs de las empresas del usuario
                    company_ids = [company.id for company in current_user.companies]
                    
                    # Buscar ubicaciones de todas las empresas asignadas al gerente
                    g.locations = Location.query.filter(
                        Location.company_id.in_(company_ids),
                        Location.is_active == True
                    ).all()
            except Exception as e:
                # Si hay un error, no mostrar ubicaciones
                g.locations = []
                logger.error(f"Error al cargar ubicaciones: {e}")
    
    # Log activity middleware (optimizado para reducir DB commits)
    # Usar before_request y after_request para solo hacer commit después de la solicitud
    
    @app.before_request
```

#### `log_activity` (línea 137)

**Código:**
```python
    def log_activity():
        # Ignorar solicitudes estáticas y de monitoreo frecuentes
        if (request.path.startswith('/static') or 
            request.path == '/favicon.ico' or 
            request.path == '/health'):
            return  # Skip logging
            
        if current_user.is_authenticated:
            try:
                # Solo registrar acciones interesantes (no GETs a páginas comunes)
                if request.method != 'GET' or not request.path.startswith(('/dashboard', '/employees')):
                    # Usar la función de utils.py para registrar en lugar de hacerlo directamente aquí
                    from utils import log_activity as log_activity_util
                    log_activity_util(f"{request.method} {request.path}")
            except Exception as e:
                # Si hay un error, registrar pero no interrumpir el flujo
                logger.error(f"Error al registrar actividad: {e}")
    
    @app.after_request
```

#### `after_request_handler` (línea 156)

**Código:**
```python
    def after_request_handler(response):
        # Mantenemos esto por compatibilidad pero la lógica se movió a utils.py
        return response
    
    # Initialize app config
    from config import Config
    Config.init_app(app)
    
    return app

# Import these here to avoid circular imports
```


### close_operation_hours.py

#### `auto_close_pending_records` (línea 16)

**Docstring:**
```
Cierra automáticamente todos los registros pendientes de los puntos de fichaje 
que han llegado a su hora de fin de funcionamiento configurada.
```

**Código:**
```python
def auto_close_pending_records():
    """
    Cierra automáticamente todos los registros pendientes de los puntos de fichaje 
    que han llegado a su hora de fin de funcionamiento configurada.
    """
    timestamp = datetime.now()
    print(f"\n{'*' * 100}")
    print(f"* INICIANDO FUNCIÓN DE BARRIDO AUTOMÁTICO")
    print(f"* Fecha/hora: {timestamp}")
    print(f"* Versión: 1.1.0")
    print(f"{'*' * 100}\n")
    
    print(f"========== INICIO BARRIDO DE CIERRE AUTOMÁTICO: {timestamp} ==========")
    print(f"Ejecutando verificación de cierre automático por fin de horario de funcionamiento: {timestamp}")
    
    try:
        # Obtener hora actual en la zona horaria configurada
        current_time = get_current_time()
        current_hour = current_time.time()
        
        print(f"Hora actual (Madrid): {current_hour}")
        
        # Buscar todos los puntos de fichaje con horario de funcionamiento activado
        checkpoints = CheckPoint.query.filter(
            CheckPoint.enforce_operation_hours == True,  # Tiene configuración de horario activada
            CheckPoint.operation_end_time.isnot(None),   # Tiene hora de fin configurada
            CheckPoint.status == CheckPointStatus.ACTIVE  # Está activo
        ).all()
        
        if not checkpoints:
            print("No hay puntos de fichaje con horario de funcionamiento configurado.")
            print(f"========== FIN BARRIDO DE CIERRE AUTOMÁTICO (sin puntos configurados) ==========")
            return True
            
        print(f"Encontrados {len(checkpoints)} puntos de fichaje con horario configurado.")
        
        # Inicializamos contadores para el resumen final
        total_checkpoints_processed = 0
        total_records_closed = 0
        
        # Para cada punto de fichaje, verificar si es hora de cerrar sus registros
        for checkpoint in checkpoints:
            # Si la hora actual es mayor o igual a la hora de fin configurada, cerrar registros
            if current_hour >= checkpoint.operation_end_time:
                print(f"Procesando punto de fichaje: {checkpoint.name} (ID: {checkpoint.id}) - Hora de fin: {checkpoint.operation_end_time}")
                total_checkpoints_processed += 1
                
                # Buscar registros pendientes de este punto de fichaje
                pending_records = CheckPointRecord.query.filter(
                    CheckPointRecord.checkpoint_id == checkpoint.id,
                    CheckPointRecord.check_out_time.is_(None)
                ).all()
                
                if not pending_records:
                    print(f"No hay registros pendientes en el punto de fichaje {checkpoint.name}")
                    continue
                    
                print(f"Encontrados {len(pending_records)} registros pendientes para cerrar.")
                
                # Cerrar cada registro pendiente
                for record in pending_records:
                    # Crear una copia del registro original si es la primera modificación
                    if not record.original_check_in_time:
                        record.original_check_in_time = record.check_in_time
                    
                    # Asegurarse de que la fecha de entrada tenga información de zona horaria
                    check_in_time = record.check_in_time
                    if not check_in_time:
                        print(f"  ⚠️ Advertencia: El registro {record.id} no tiene hora de entrada válida")
                        continue
                        
                    if check_in_time.tzinfo is None:
                        check_in_time = datetime_to_madrid(check_in_time)
                    
                    # Establecer la hora de salida como la hora de fin de funcionamiento
                    check_in_date = check_in_time.date()
                    check_out_time = datetime.combine(check_in_date, checkpoint.operation_end_time)
                    check_out_time = TIMEZONE.localize(check_out_time)
                    
                    # Si la salida queda antes que la entrada (lo cual sería un error), 
                    # establecer la salida para el día siguiente
                    if check_out_time < check_in_time:
                        print(f"  ⚠️  Entrada posterior a hora de cierre: {check_in_time} > {check_out_time}")
                        print(f"  ⚠️  Ajustando la salida para el día siguiente")
                        check_out_date = check_in_date + timedelta(days=1)
                        check_out_time = datetime.combine(check_out_date, checkpoint.operation_end_time)
                        check_out_time = TIMEZONE.localize(check_out_time)
                    
                    # Asignar la hora de salida calculada
                    record.check_out_time = check_out_time
                    
                    # Marcar que fue cerrado automáticamente
                    record.notes = (record.notes or "") + " [Cerrado automáticamente por fin de horario de funcionamiento]"
                    record.adjusted = True
                    
                    # Crear una incidencia
                    incident = CheckPointIncident(
                        record_id=record.id,
                        incident_type=CheckPointIncidentType.MISSED_CHECKOUT,
                        description=f"Salida automática por fin de horario de funcionamiento ({checkpoint.operation_end_time})"
                    )
                    db.session.add(incident)
                
                # Guardar todos los cambios para este punto de fichaje
                try:
                    records_closed = len(pending_records)
                    db.session.commit()
                    total_records_closed += records_closed
                    print(f"✓ {records_closed} registros cerrados correctamente para el punto {checkpoint.name}")
                except Exception as e:
                    db.session.rollback()
                    print(f"✗ Error al cerrar registros para el punto {checkpoint.name}: {e}")
            else:
                print(f"• No es hora de cerrar los registros para {checkpoint.name}. Hora actual: {current_hour}, Hora fin: {checkpoint.operation_end_time}")
        
        # Mostrar resumen final del barrido
        end_timestamp = datetime.now()
        duration = (end_timestamp - timestamp).total_seconds()
        print(f"\n========== RESUMEN DEL BARRIDO DE CIERRE AUTOMÁTICO ==========")
        print(f"Fecha y hora de inicio: {timestamp}")
        print(f"Fecha y hora de fin: {end_timestamp}")
        print(f"Duración: {duration:.2f} segundos")
        print(f"Puntos de fichaje procesados: {total_checkpoints_processed} de {len(checkpoints)}")
        print(f"Registros cerrados: {total_records_closed}")
        print(f"========== FIN BARRIDO DE CIERRE AUTOMÁTICO ==========\n")
        
        return True
        
    except Exception as e:
        end_timestamp = datetime.now()
        print(f"\n========== ERROR EN BARRIDO DE CIERRE AUTOMÁTICO ==========")
        print(f"Fecha y hora de inicio: {timestamp}")
        print(f"Fecha y hora de error: {end_timestamp}")
        print(f"Error general durante el proceso: {e}")
        print(f"========== FIN BARRIDO CON ERROR ==========\n")
        return False


if __name__ == "__main__":
    app = create_app()
    with app.app_context():
        success = auto_close_pending_records()
    
    sys.exit(0 if success else 1)
```


### config.py

#### `init_app` (línea 52)

**Código:**
```python
    def init_app(app):
        os.makedirs(Config.UPLOAD_FOLDER, exist_ok=True)

```


### create_employees.py

#### `generate_dni` (línea 33)

**Docstring:**
```
Genera un DNI ficticio con formato español válido (8 números + letra)
```

**Código:**
```python
def generate_dni():
    """Genera un DNI ficticio con formato español válido (8 números + letra)"""
    number = random.randint(10000000, 99999999)
    # Letras válidas para DNI español
    letters = "TRWAGMYFPDXBNJZSQVHLCKE"
    letter = letters[number % 23]
    return f"{number}{letter}"

```

#### `generate_bank_account` (línea 41)

**Docstring:**
```
Genera un número de cuenta bancaria ficticio
```

**Código:**
```python
def generate_bank_account():
    """Genera un número de cuenta bancaria ficticio"""
    country = "ES"
    control = f"{random.randint(10, 99)}"
    entity = f"{random.randint(1000, 9999)}"
    office = f"{random.randint(1000, 9999)}"
    dc = f"{random.randint(10, 99)}"
    account = f"{random.randint(1000000000, 9999999999)}"
    return f"{country}{control} {entity} {office} {dc} {account}"

```

#### `create_employees` (línea 51)

**Docstring:**
```
Crea 30 empleados ficticios para la empresa 100RCS (ID 8)
```

**Código:**
```python
def create_employees():
    """
    Crea 30 empleados ficticios para la empresa 100RCS (ID 8)
    """
    app = create_app()
    with app.app_context():
        company_id = 8  # ID de la empresa 100RCS
        
        print("Iniciando creación de empleados ficticios...")
        
        # Verificar cuántos empleados ya tiene la empresa
        existing_count = Employee.query.filter_by(company_id=company_id).count()
        print(f"La empresa ya tiene {existing_count} empleados.")
        
        # Crear 30 empleados ficticios
        employees_added = 0
        for i in range(30):
            first_name = random.choice(first_names)
            last_name = f"{random.choice(last_names)} {random.choice(last_names)}"
            
            # Verificar si el empleado ya existe
            dni = generate_dni()
            if Employee.query.filter_by(dni=dni).first():
                print(f"DNI {dni} ya existe. Generando otro...")
                continue
                
            # Generar fechas aleatorias para el contrato
            start_date = date.today() - timedelta(days=random.randint(30, 730))
            
            # Decidir si el contrato tiene fecha de fin
            if random.choice([True, False]):
                end_date = start_date + timedelta(days=random.randint(180, 1095))
            else:
                end_date = None
                
            # Crear el empleado
            employee = Employee(
                first_name=first_name,
                last_name=last_name,
                dni=dni,
                position=random.choice(positions),
                contract_type=random.choice(list(ContractType)),
                bank_account=generate_bank_account(),
                start_date=start_date,
                end_date=end_date,
                company_id=company_id,
                is_active=random.choice([True, True, True, False]),  # 75% activos
                status=random.choice(list(EmployeeStatus)),
                status_start_date=date.today() - timedelta(days=random.randint(0, 90)),
                created_at=datetime.now(),
                updated_at=datetime.now()
            )
            
            db.session.add(employee)
            try:
                db.session.commit()
                employees_added += 1
                print(f"Empleado {employees_added}/30 añadido: {first_name} {last_name}")
            except Exception as e:
                db.session.rollback()
                print(f"Error al añadir empleado: {e}")
        
        print(f"Proceso completado. Se han añadido {employees_added} empleados.")

if __name__ == "__main__":
    create_employees()
```


### create_remaining_employees.py

#### `generate_dni` (línea 24)

**Docstring:**
```
Genera un DNI ficticio con formato español válido (8 números + letra)
```

**Código:**
```python
def generate_dni():
    """Genera un DNI ficticio con formato español válido (8 números + letra)"""
    number = random.randint(10000000, 99999999)
    # Letras válidas para DNI español
    letters = "TRWAGMYFPDXBNJZSQVHLCKE"
    letter = letters[number % 23]
    return f"{number}{letter}"

```

#### `generate_bank_account` (línea 32)

**Docstring:**
```
Genera un número de cuenta bancaria ficticio
```

**Código:**
```python
def generate_bank_account():
    """Genera un número de cuenta bancaria ficticio"""
    country = "ES"
    control = f"{random.randint(10, 99)}"
    entity = f"{random.randint(1000, 9999)}"
    office = f"{random.randint(1000, 9999)}"
    dc = f"{random.randint(10, 99)}"
    account = f"{random.randint(1000000000, 9999999999)}"
    return f"{country}{control} {entity} {office} {dc} {account}"

```

#### `create_remaining_employees` (línea 42)

**Docstring:**
```
Crea los empleados ficticios restantes para la empresa 100RCS (ID 8)
```

**Código:**
```python
def create_remaining_employees():
    """
    Crea los empleados ficticios restantes para la empresa 100RCS (ID 8)
    """
    app = create_app()
    with app.app_context():
        company_id = 8  # ID de la empresa 100RCS
        
        print("Iniciando creación de empleados ficticios restantes...")
        
        # Verificar cuántos empleados ya tiene la empresa
        existing_count = Employee.query.filter_by(company_id=company_id).count()
        print(f"La empresa ya tiene {existing_count} empleados.")
        
        # Determinar cuántos empleados más hay que crear
        employees_to_add = 31 - existing_count  # 30 nuevos + 1 existente = 31
        print(f"Se crearán {employees_to_add} empleados adicionales.")
        
        # Crear los empleados restantes
        employees_added = 0
        for i in range(employees_to_add):
            first_name = random.choice(first_names)
            last_name = f"{random.choice(last_names)} {random.choice(last_names)}"
            
            # Verificar si el empleado ya existe
            dni = generate_dni()
            if Employee.query.filter_by(dni=dni).first():
                print(f"DNI {dni} ya existe. Generando otro...")
                continue
                
            # Generar fechas aleatorias para el contrato
            start_date = date.today() - timedelta(days=random.randint(30, 730))
            
            # Decidir si el contrato tiene fecha de fin
            if random.choice([True, False]):
                end_date = start_date + timedelta(days=random.randint(180, 1095))
            else:
                end_date = None
                
            # Crear el empleado
            employee = Employee(
                first_name=first_name,
                last_name=last_name,
                dni=dni,
                position=random.choice(positions),
                contract_type=random.choice(list(ContractType)),
                bank_account=generate_bank_account(),
                start_date=start_date,
                end_date=end_date,
                company_id=company_id,
                is_active=random.choice([True, True, True, False]),  # 75% activos
                status=random.choice(list(EmployeeStatus)),
                status_start_date=date.today() - timedelta(days=random.randint(0, 90)),
                created_at=datetime.now(),
                updated_at=datetime.now()
            )
            
            db.session.add(employee)
            try:
                db.session.commit()
                employees_added += 1
                print(f"Empleado {employees_added}/{employees_to_add} añadido: {first_name} {last_name}")
            except Exception as e:
                db.session.rollback()
                print(f"Error al añadir empleado: {e}")
        
        print(f"Proceso completado. Se han añadido {employees_added} empleados adicionales.")
        print(f"Total de empleados en la empresa: {existing_count + employees_added}")

if __name__ == "__main__":
    create_remaining_employees()
```


### extract_functions.py

#### `extract_functions_from_file` (línea 5)

**Docstring:**
```
Extrae todas las funciones definidas en un archivo Python, junto con sus docstrings.

Args:
    file_path: Ruta al archivo Python
    
Returns:
    Una lista de tuplas que contienen (nombre_función, línea_inicio, docstring, código_completo)
```

**Código:**
```python
def extract_functions_from_file(file_path):
    """
    Extrae todas las funciones definidas en un archivo Python, junto con sus docstrings.
    
    Args:
        file_path: Ruta al archivo Python
        
    Returns:
        Una lista de tuplas que contienen (nombre_función, línea_inicio, docstring, código_completo)
    """
    functions = []
    
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            content = file.read()
            source_lines = content.split('\n')
        
        # Analizar el código usando AST
        tree = ast.parse(content)
        
        # Extraer funciones a nivel de módulo y de clases
        for node in ast.walk(tree):
            if isinstance(node, ast.FunctionDef):
                # Extraer docstring si existe
                docstring = ast.get_docstring(node) or ""
                
                # Obtener línea de inicio
                start_line = node.lineno
                
                # Estimamos la línea de fin usando el source_code
                # Esto es una simplificación, pero funcionará para nuestra necesidad
                # de documentación básica
                
                # Buscamos la definición de la función
                func_lines = []
                indentation = None
                i = start_line - 1  # Ajustar a índice base 0
                
                # Obtener la indentación de la línea de definición
                if i < len(source_lines):
                    line = source_lines[i]
                    # Encontrar la indentación inicial
                    spaces = len(line) - len(line.lstrip())
                    indentation = spaces
                    func_lines.append(line)
                    i += 1
                
                # Seguir leyendo líneas mientras tengan la misma o mayor indentación
                # hasta encontrar otra definición de función o clase
                while i < len(source_lines):
                    line = source_lines[i]
                    if line.strip() == '':
                        func_lines.append(line)
                        i += 1
                        continue
                    
                    current_spaces = len(line) - len(line.lstrip())
                    
                    # Si encontramos una línea con menor indentación, hemos salido de la función
                    if current_spaces <= indentation and line.lstrip().startswith(('def ', 'class ')):
                        break
                    
                    # Verificar si todavía estamos dentro de la función
                    if current_spaces < indentation and not line.lstrip().startswith('#'):
                        break
                    
                    func_lines.append(line)
                    i += 1
                
                # Unir las líneas para obtener el código completo de la función
                func_code = '\n'.join(func_lines)
                
                # Añadir a la lista de funciones
                functions.append((node.name, start_line, docstring, func_code))
                
        return functions
    except Exception as e:
        print(f"Error al analizar {file_path}: {e}")
        return []

```

#### `find_all_py_files` (línea 85)

**Docstring:**
```
Encuentra todos los archivos Python en un directorio y sus subdirectorios,
excluyendo directorios de cache y librerías.

Args:
    directory: Directorio raíz para la búsqueda
    
Returns:
    Lista de rutas a archivos Python
```

**Código:**
```python
def find_all_py_files(directory):
    """
    Encuentra todos los archivos Python en un directorio y sus subdirectorios,
    excluyendo directorios de cache y librerías.
    
    Args:
        directory: Directorio raíz para la búsqueda
        
    Returns:
        Lista de rutas a archivos Python
    """
    py_files = []
    
    exclude_dirs = ['.cache', '__pycache__', '.pythonlibs']
    
    for root, dirs, files in os.walk(directory):
        # Excluir directorios no deseados
        dirs[:] = [d for d in dirs if d not in exclude_dirs]
        
        for file in files:
            if file.endswith('.py'):
                py_files.append(os.path.join(root, file))
    
    return py_files

```

#### `create_functions_table` (línea 110)

**Docstring:**
```
Genera un archivo markdown con una tabla que contiene información sobre todas las funciones
de la aplicación, incluyendo el nombre de la función, el archivo donde está definida,
la línea de inicio y una descripción basada en su docstring.

Args:
    output_file: Ruta donde se guardará el archivo de salida
```

**Código:**
```python
def create_functions_table(output_file="funciones_app.md"):
    """
    Genera un archivo markdown con una tabla que contiene información sobre todas las funciones
    de la aplicación, incluyendo el nombre de la función, el archivo donde está definida,
    la línea de inicio y una descripción basada en su docstring.
    
    Args:
        output_file: Ruta donde se guardará el archivo de salida
    """
    function_data = []
    
    # Encontrar todos los archivos Python
    py_files = find_all_py_files('.')
    
    # Extraer funciones de cada archivo
    for file_path in py_files:
        # Convertir la ruta a un formato más legible
        relative_path = os.path.relpath(file_path, '.')
        
        # Extraer funciones de este archivo
        functions = extract_functions_from_file(file_path)
        
        for func_name, line_num, docstring, func_code in functions:
            # Preparar una descripción basada en el docstring
            description = ""
            if docstring:
                # Tomar solo la primera línea del docstring como descripción breve
                first_line = docstring.strip().split('\n')[0]
                description = first_line.strip()
            
            function_data.append((func_name, relative_path, line_num, description, docstring, func_code))
    
    # Ordenar la información por nombre de archivo y luego por línea
    function_data.sort(key=lambda x: (x[1], x[2]))
    
    # Generar el archivo markdown con la tabla principal
    with open(output_file, 'w', encoding='utf-8') as out_file:
        out_file.write("# Funciones de la Aplicación\n\n")
        out_file.write("Esta tabla contiene todas las funciones definidas en la aplicación, junto con su ubicación y una breve descripción.\n\n")
        out_file.write("| Función | Archivo | Línea | Descripción |\n")
        out_file.write("|---------|---------|-------|-------------|\n")
        
        for func_name, file_path, line_num, description, _, _ in function_data:
            # Escapar posibles pipes en la descripción para no romper la tabla markdown
            if description:
                description = description.replace('|', '\\|')
            
            # Añadir cada función a la tabla
            out_file.write(f"| `{func_name}` | {file_path} | {line_num} | {description} |\n")
        
        # Añadir sección detallada con código fuente y docstrings completos
        out_file.write("\n\n## Detalles de las Funciones\n\n")
        
        current_file = ""
        for func_name, file_path, line_num, _, docstring, func_code in function_data:
            # Añadir separadores de archivos para mayor claridad
            if file_path != current_file:
                out_file.write(f"\n### {file_path}\n\n")
                current_file = file_path
            
            # Escribir detalles de la función
            out_file.write(f"#### `{func_name}` (línea {line_num})\n\n")
            
            if docstring:
                out_file.write("**Docstring:**\n")
                out_file.write("```\n")
                out_file.write(docstring)
                out_file.write("\n```\n\n")
            
            out_file.write("**Código:**\n")
            out_file.write("```python\n")
            out_file.write(func_code)
            out_file.write("\n```\n\n")
    
    print(f"Archivo generado: {output_file}")

if __name__ == "__main__":
    create_functions_table()
```


### forms.py

#### `__init__` (línea 36)

**Código:**
```python
    def __init__(self, *args, **kwargs):
        super(RegistrationForm, self).__init__(*args, **kwargs)
        # Solo permitimos asignar el rol ADMIN si el usuario actual es "admin"
        from flask_login import current_user
        
        # Configurar las opciones de rol disponibles
        if current_user.is_authenticated and current_user.username == 'admin':
            # El usuario admin puede asignar cualquier rol
            self.role.choices = [(role.value, role.name.capitalize()) for role in UserRole]
        else:
            # Otros usuarios solo pueden asignar roles que no sean ADMIN
            self.role.choices = [(role.value, role.name.capitalize()) 
                                for role in UserRole if role != UserRole.ADMIN]
        
```

#### `validate_username` (línea 50)

**Código:**
```python
    def validate_username(self, username):
        user = User.query.filter_by(username=username.data).first()
        if user is not None:
            raise ValidationError('Por favor, usa un nombre de usuario diferente.')
            
```

#### `validate_email` (línea 55)

**Código:**
```python
    def validate_email(self, email):
        user = User.query.filter_by(email=email.data).first()
        if user is not None:
            raise ValidationError('Por favor, usa un email diferente.')
            
```

#### `validate_role` (línea 60)

**Código:**
```python
    def validate_role(self, role):
        from flask_login import current_user
        
        # No permitir crear usuarios con rol "admin" excepto por el propio admin
        if role.data == UserRole.ADMIN.value and (not current_user.is_authenticated or current_user.username != 'admin'):
            raise ValidationError('Solo el usuario "admin" puede asignar el rol de administrador.')

```

#### `__init__` (línea 79)

**Código:**
```python
    def __init__(self, original_username, original_email, *args, **kwargs):
        super(UserUpdateForm, self).__init__(*args, **kwargs)
        self.original_username = original_username
        self.original_email = original_email
        
        # Solo permitimos asignar el rol ADMIN si el usuario actual es "admin"
        from flask_login import current_user
        
        # Configurar las opciones de rol disponibles
        if current_user.is_authenticated and current_user.username == 'admin':
            # El usuario admin puede asignar cualquier rol
            self.role.choices = [(role.value, role.name.capitalize()) for role in UserRole]
        else:
            # Otros usuarios solo pueden asignar roles que no sean ADMIN
            self.role.choices = [(role.value, role.name.capitalize()) 
                               for role in UserRole if role != UserRole.ADMIN]
        
```

#### `validate_username` (línea 96)

**Código:**
```python
    def validate_username(self, username):
        if username.data != self.original_username:
            user = User.query.filter_by(username=username.data).first()
            if user is not None:
                raise ValidationError('Por favor, usa un nombre de usuario diferente.')
                
```

#### `validate_email` (línea 102)

**Código:**
```python
    def validate_email(self, email):
        if email.data != self.original_email:
            user = User.query.filter_by(email=email.data).first()
            if user is not None:
                raise ValidationError('Por favor, usa un email diferente.')
                
```

#### `validate_role` (línea 108)

**Código:**
```python
    def validate_role(self, role):
        from flask_login import current_user
        
        # Proteger cambio de rol para el usuario "admin"
        if self.original_username == 'admin':
            # No permitir cambiar el rol del usuario "admin" bajo ninguna circunstancia
            if role.data != UserRole.ADMIN.value:
                raise ValidationError('No se puede cambiar el rol del usuario "admin".')
            return

        # Validar que solo el usuario "admin" pueda asignar el rol ADMIN a otros usuarios
        if role.data == UserRole.ADMIN.value and (not current_user.is_authenticated or current_user.username != 'admin'):
            raise ValidationError('Solo el usuario "admin" puede asignar el rol de administrador.')
```

#### `__init__` (línea 152)

**Código:**
```python
    def __init__(self, original_tax_id=None, *args, **kwargs):
        super(CompanyForm, self).__init__(*args, **kwargs)
        self.original_tax_id = original_tax_id
        
```

#### `validate_tax_id` (línea 156)

**Código:**
```python
    def validate_tax_id(self, tax_id):
        from models import Company
        # Si estamos editando y el CIF/NIF no ha cambiado, no hacemos validación adicional
        if self.original_tax_id and tax_id.data.lower() == self.original_tax_id.lower():
            return
            
        # Buscamos si existe alguna empresa con el mismo CIF/NIF (sin distinguir mayúsculas/minúsculas)
        company = Company.query.filter(func.lower(Company.tax_id) == func.lower(tax_id.data)).first()
        if company is not None:
            raise ValidationError('Ya existe una empresa con este CIF/NIF. Por favor, verifica los datos.')
    
```

#### `validate_name` (línea 167)

**Docstring:**
```
Validar que el nombre no tenga caracteres especiales
```

**Código:**
```python
    def validate_name(self, name):
        """Validar que el nombre no tenga caracteres especiales"""
        import re
        if not re.match(r'^[a-zA-Z0-9áéíóúÁÉÍÓÚüÜñÑ\s&-]+$', name.data):
            raise ValidationError('El nombre no debe contener caracteres especiales como puntos (.), comas, etc.')

```

#### `validate_end_date` (línea 189)

**Código:**
```python
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

```

#### `validate_status_end_date` (línea 212)

**Código:**
```python
    def validate_status_end_date(form, field):
        if field.data and form.status_start_date.data and field.data < form.status_start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

```

#### `validate_end_time` (línea 227)

**Código:**
```python
    def validate_end_time(form, field):
        if form.start_time.data and field.data and field.data <= form.start_time.data:
            raise ValidationError('La hora de salida debe ser posterior a la hora de entrada.')

```

#### `validate` (línea 269)

**Código:**
```python
    def validate(self, **kwargs):
        if not super().validate():
            return False
        
        # Verificar que para cada día marcado como laborable se hayan introducido horas
        for day in ["lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"]:
            is_working_day = getattr(self, f"{day}_is_working_day").data
            start_time = getattr(self, f"{day}_start_time").data
            end_time = getattr(self, f"{day}_end_time").data
            
            if is_working_day:
                if not start_time:
                    field = getattr(self, f"{day}_start_time")
                    field.errors = ["Este campo es obligatorio para días laborables."]
                    return False
                
                if not end_time:
                    field = getattr(self, f"{day}_end_time")
                    field.errors = ["Este campo es obligatorio para días laborables."]
                    return False
                
                if end_time <= start_time:
                    field = getattr(self, f"{day}_end_time")
                    field.errors = ["La hora de salida debe ser posterior a la hora de entrada."]
                    return False
        
        return True

```

#### `validate_check_out_time` (línea 304)

**Código:**
```python
    def validate_check_out_time(form, field):
        if field.data and form.check_in_time.data and field.data < form.check_in_time.data:
            raise ValidationError('La hora de salida debe ser posterior a la hora de entrada.')

```

#### `validate_end_date` (línea 314)

**Código:**
```python
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

# Clase de aprobación de vacaciones eliminada, ya que no se requiere aprobación

```

#### `validate_end_date` (línea 325)

**Código:**
```python
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

```

#### `validate_end_date` (línea 334)

**Código:**
```python
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

```


### forms_checkpoints.py

#### `validate_password` (línea 35)

**Docstring:**
```
Asegura que la contraseña y la confirmación coincidan
```

**Código:**
```python
    def validate_password(self, field):
        """Asegura que la contraseña y la confirmación coincidan"""
        if self.password.data and self.password.data != self.confirm_password.data:
            raise ValidationError('Las contraseñas no coinciden')
            
```

#### `validate_operation_end_time` (línea 40)

**Docstring:**
```
Verifica que la hora de fin de operación sea posterior a la de inicio
```

**Código:**
```python
    def validate_operation_end_time(self, field):
        """Verifica que la hora de fin de operación sea posterior a la de inicio"""
        if field.data and self.operation_start_time.data and field.data <= self.operation_start_time.data:
            raise ValidationError('La hora de fin de operación debe ser posterior a la hora de inicio')


```

#### `validate_weekly_hours` (línea 96)

**Docstring:**
```
Verifica que las horas semanales sean coherentes con las diarias
```

**Código:**
```python
    def validate_weekly_hours(self, field):
        """Verifica que las horas semanales sean coherentes con las diarias"""
        if field.data < self.daily_hours.data:
            raise ValidationError('Las horas semanales no pueden ser menos que las diarias')
        
```

#### `validate_normal_end_time` (línea 101)

**Docstring:**
```
Verifica que la hora de fin sea posterior a la de inicio
```

**Código:**
```python
    def validate_normal_end_time(self, field):
        """Verifica que la hora de fin sea posterior a la de inicio"""
        if field.data and self.normal_start_time.data and field.data <= self.normal_start_time.data:
            raise ValidationError('La hora de fin debe ser posterior a la hora de inicio')


```

#### `validate_check_out_time` (línea 119)

**Docstring:**
```
Verifica que la hora de salida sea posterior a la de entrada
```

**Código:**
```python
    def validate_check_out_time(self, field):
        """Verifica que la hora de salida sea posterior a la de entrada"""
        if field.data and field.data <= self.check_in_time.data:
            raise ValidationError('La hora de salida debe ser posterior a la hora de entrada')


```

#### `validate_end_date` (línea 141)

**Docstring:**
```
Verifica que la fecha de fin sea posterior a la de inicio
```

**Código:**
```python
    def validate_end_date(self, field):
        """Verifica que la fecha de fin sea posterior a la de inicio"""
        try:
            start = datetime.strptime(self.start_date.data, '%Y-%m-%d')
            end = datetime.strptime(field.data, '%Y-%m-%d')
            
            if end < start:
                raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio')
        except ValueError:
            raise ValidationError('Formato de fecha incorrecto. Use YYYY-MM-DD')
```


### forms_tasks.py

#### `validate_pin` (línea 46)

**Código:**
```python
    def validate_pin(form, field):
        if not field.data.isdigit():
            raise ValidationError('El PIN debe contener solo números')

```

#### `__init__` (línea 71)

**Código:**
```python
    def __init__(self, *args, **kwargs):
        super(TaskForm, self).__init__(*args, **kwargs)
        # Añadir opción para "Sin grupo"
        if self.group_id.choices and self.group_id.choices[0][0] != 0:
            self.group_id.choices.insert(0, (0, 'Sin grupo'))
    
```

#### `validate_end_date` (línea 77)

**Código:**
```python
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio')
            
```

#### `validate` (línea 81)

**Código:**
```python
    def validate(self, **kwargs):
        if not super(TaskForm, self).validate(**kwargs):
            return False
            
        # Verificar que si la frecuencia es personalizada, al menos un día está marcado
        if self.frequency.data == TaskFrequency.PERSONALIZADA.value:
            if not (self.monday.data or self.tuesday.data or self.wednesday.data or 
                    self.thursday.data or self.friday.data or self.saturday.data or 
                    self.sunday.data):
                self.frequency.errors.append('Debes seleccionar al menos un día de la semana para tareas personalizadas')
                return False
                
        return True

```

#### `validate_end_time` (línea 100)

**Código:**
```python
    def validate_end_time(form, field):
        if field.data and form.start_time.data and field.data < form.start_time.data:
            raise ValidationError('La hora de fin debe ser posterior a la hora de inicio')

```

#### `validate_end_time` (línea 110)

**Código:**
```python
    def validate_end_time(form, field):
        if field.data and form.start_time.data and field.data < form.start_time.data:
            raise ValidationError('La hora de fin debe ser posterior a la hora de inicio')

```

#### `validate_end_time` (línea 123)

**Código:**
```python
    def validate_end_time(form, field):
        if field.data and form.start_time.data and field.data < form.start_time.data:
            raise ValidationError('La hora de fin debe ser posterior a la hora de inicio')

```

#### `validate_end_time` (línea 132)

**Código:**
```python
    def validate_end_time(form, field):
        if field.data and form.start_time.data and field.data < form.start_time.data:
            raise ValidationError('La hora de fin debe ser posterior a la hora de inicio')

```

#### `validate_pin` (línea 149)

**Código:**
```python
    def validate_pin(form, field):
        if not field.data.isdigit():
            raise ValidationError('El PIN debe contener solo números')

```

#### `validate_end_time` (línea 168)

**Código:**
```python
    def validate_end_time(form, field):
        if field.data and form.start_time.data and field.data < form.start_time.data:
            raise ValidationError('La hora de fin debe ser posterior a la hora de inicio')

```

#### `__init__` (línea 204)

**Código:**
```python
    def __init__(self, *args, **kwargs):
        super(ProductConservationForm, self).__init__(*args, **kwargs)
        from models_tasks import ConservationType
        self.conservation_type.choices = [(ct.value, ct.name.capitalize()) for ct in ConservationType]

```

#### `__init__` (línea 219)

**Código:**
```python
    def __init__(self, *args, **kwargs):
        super(GenerateLabelForm, self).__init__(*args, **kwargs)
        from models_tasks import ConservationType
        self.conservation_type.choices = [(ct.value, ct.name.capitalize()) for ct in ConservationType]
        
```


### forms_tmp.py

#### `__init__` (línea 36)

**Código:**
```python
    def __init__(self, *args, **kwargs):
        super(RegistrationForm, self).__init__(*args, **kwargs)
        # Solo permitimos asignar el rol ADMIN si el usuario actual es "admin"
        from flask_login import current_user
        
        # Configurar las opciones de rol disponibles
        if current_user.is_authenticated and current_user.username == 'admin':
            # El usuario admin puede asignar cualquier rol
            self.role.choices = [(role.value, role.name.capitalize()) for role in UserRole]
        else:
            # Otros usuarios solo pueden asignar roles que no sean ADMIN
            self.role.choices = [(role.value, role.name.capitalize()) 
                                for role in UserRole if role != UserRole.ADMIN]
        
```

#### `validate_username` (línea 50)

**Código:**
```python
    def validate_username(self, username):
        user = User.query.filter_by(username=username.data).first()
        if user is not None:
            raise ValidationError('Por favor, usa un nombre de usuario diferente.')
            
```

#### `validate_email` (línea 55)

**Código:**
```python
    def validate_email(self, email):
        user = User.query.filter_by(email=email.data).first()
        if user is not None:
            raise ValidationError('Por favor, usa un email diferente.')
            
```

#### `validate_role` (línea 60)

**Código:**
```python
    def validate_role(self, role):
        from flask_login import current_user
        
        # Validar que solo el usuario "admin" pueda asignar el rol ADMIN
        if role.data == UserRole.ADMIN.value and (not current_user.is_authenticated or current_user.username != 'admin'):
            raise ValidationError('Solo el usuario "admin" puede asignar el rol de administrador.')

```

#### `__init__` (línea 79)

**Código:**
```python
    def __init__(self, original_username, original_email, *args, **kwargs):
        super(UserUpdateForm, self).__init__(*args, **kwargs)
        self.original_username = original_username
        self.original_email = original_email
        
        # Solo permitimos asignar el rol ADMIN si el usuario actual es "admin"
        from flask_login import current_user
        
        # Configurar las opciones de rol disponibles
        if current_user.is_authenticated and current_user.username == 'admin':
            # El usuario admin puede asignar cualquier rol
            self.role.choices = [(role.value, role.name.capitalize()) for role in UserRole]
        else:
            # Otros usuarios solo pueden asignar roles que no sean ADMIN
            self.role.choices = [(role.value, role.name.capitalize()) 
                               for role in UserRole if role != UserRole.ADMIN]
        
```

#### `validate_username` (línea 96)

**Código:**
```python
    def validate_username(self, username):
        if username.data != self.original_username:
            user = User.query.filter_by(username=username.data).first()
            if user is not None:
                raise ValidationError('Por favor, usa un nombre de usuario diferente.')
                
```

#### `validate_email` (línea 102)

**Código:**
```python
    def validate_email(self, email):
        if email.data != self.original_email:
            user = User.query.filter_by(email=email.data).first()
            if user is not None:
                raise ValidationError('Por favor, usa un email diferente.')
                
```

#### `validate_role` (línea 108)

**Código:**
```python
    def validate_role(self, role):
        from flask_login import current_user
        
        # Validar que solo el usuario "admin" pueda asignar el rol ADMIN
        if role.data == UserRole.ADMIN.value and (not current_user.is_authenticated or current_user.username != 'admin'):
            raise ValidationError('Solo el usuario "admin" puede asignar el rol de administrador.')

```

#### `__init__` (línea 137)

**Código:**
```python
    def __init__(self, original_tax_id=None, *args, **kwargs):
        super(CompanyForm, self).__init__(*args, **kwargs)
        self.original_tax_id = original_tax_id
        
```

#### `validate_tax_id` (línea 141)

**Código:**
```python
    def validate_tax_id(self, tax_id):
        from models import Company
        # Si estamos editando y el CIF/NIF no ha cambiado, no hacemos validación adicional
        if self.original_tax_id and tax_id.data.lower() == self.original_tax_id.lower():
            return
            
        # Buscamos si existe alguna empresa con el mismo CIF/NIF (sin distinguir mayúsculas/minúsculas)
        company = Company.query.filter(func.lower(Company.tax_id) == func.lower(tax_id.data)).first()
        if company is not None:
            raise ValidationError('Ya existe una empresa con este CIF/NIF. Por favor, verifica los datos.')

```

#### `validate_end_date` (línea 168)

**Código:**
```python
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

```

#### `validate_status_end_date` (línea 191)

**Código:**
```python
    def validate_status_end_date(form, field):
        if field.data and form.status_start_date.data and field.data < form.status_start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

```

#### `validate_end_time` (línea 206)

**Código:**
```python
    def validate_end_time(form, field):
        if form.start_time.data and field.data and field.data <= form.start_time.data:
            raise ValidationError('La hora de salida debe ser posterior a la hora de entrada.')

```

#### `validate` (línea 248)

**Código:**
```python
    def validate(self, **kwargs):
        if not super().validate():
            return False
        
        # Verificar que para cada día marcado como laborable se hayan introducido horas
        for day in ["lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"]:
            is_working_day = getattr(self, f"{day}_is_working_day").data
            start_time = getattr(self, f"{day}_start_time").data
            end_time = getattr(self, f"{day}_end_time").data
            
            if is_working_day:
                if not start_time:
                    field = getattr(self, f"{day}_start_time")
                    field.errors = ["Este campo es obligatorio para días laborables."]
                    return False
                
                if not end_time:
                    field = getattr(self, f"{day}_end_time")
                    field.errors = ["Este campo es obligatorio para días laborables."]
                    return False
                
                if end_time <= start_time:
                    field = getattr(self, f"{day}_end_time")
                    field.errors = ["La hora de salida debe ser posterior a la hora de entrada."]
                    return False
        
        return True

```

#### `validate_check_out_time` (línea 283)

**Código:**
```python
    def validate_check_out_time(form, field):
        if field.data and form.check_in_time.data and field.data < form.check_in_time.data:
            raise ValidationError('La hora de salida debe ser posterior a la hora de entrada.')

```

#### `validate_end_date` (línea 293)

**Código:**
```python
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

# Clase de aprobación de vacaciones eliminada, ya que no se requiere aprobación

```

#### `validate_end_date` (línea 304)

**Código:**
```python
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

```

#### `validate_end_date` (línea 313)

**Código:**
```python
    def validate_end_date(form, field):
        if field.data and form.start_date.data and field.data < form.start_date.data:
            raise ValidationError('La fecha de fin debe ser posterior a la fecha de inicio.')

```


### migrate_auto_checkout_removal.py

#### `run_migration` (línea 8)

**Docstring:**
```
Elimina la columna auto_checkout_time de la tabla checkpoints
que fue parte del sistema de auto-checkout que ha sido eliminado.
```

**Código:**
```python
def run_migration():
    """
    Elimina la columna auto_checkout_time de la tabla checkpoints
    que fue parte del sistema de auto-checkout que ha sido eliminado.
    """
    print("Iniciando migración para eliminar columna auto_checkout_time...")
    
    try:
        # Verificar si la columna existe antes de intentar eliminarla
        result = db.session.execute(text(
            "SELECT column_name FROM information_schema.columns "
            "WHERE table_name = 'checkpoints' AND column_name = 'auto_checkout_time'"
        )).fetchone()
        
        if result:
            print("La columna auto_checkout_time existe, procediendo a eliminarla...")
            # Eliminar la columna
            db.session.execute(text(
                "ALTER TABLE checkpoints DROP COLUMN auto_checkout_time"
            ))
            db.session.commit()
            print("✅ Columna auto_checkout_time eliminada correctamente")
        else:
            print("⚠️ La columna auto_checkout_time no existe, no es necesario eliminarla")
        
    except Exception as e:
        db.session.rollback()
        print(f"❌ Error al eliminar la columna auto_checkout_time: {e}")
    
    print("Migración completada")

if __name__ == "__main__":
    # Crear aplicación Flask para contexto
    app = Flask(__name__)
    app.config.from_object('config.Config')
    db.init_app(app)
    
    with app.app_context():
        run_migration()
```


### migrate_checkpoints.py

#### `create_checkpoint_tables` (línea 8)

**Docstring:**
```
Función para crear las tablas de puntos de fichaje
```

**Código:**
```python
def create_checkpoint_tables():
    """Función para crear las tablas de puntos de fichaje"""
    print("Creando tablas de puntos de fichaje...")
    
    # Crear el contexto de la aplicación
    with app.app_context():
        # Importar los modelos para asegurar que SQLAlchemy los conozca
        import models_checkpoints
        import models_tasks
        import models
        
        # Asegurarse de que las tablas existen
        db.create_all()
        
        print("Tablas creadas correctamente.")
    
if __name__ == "__main__":
    create_checkpoint_tables()
```


### migrate_contract_hours_activation.py

#### `add_activation_columns` (línea 18)

**Docstring:**
```
Añade las columnas use_normal_schedule y use_flexibility a la tabla employee_contract_hours
```

**Código:**
```python
def add_activation_columns():
    """Añade las columnas use_normal_schedule y use_flexibility a la tabla employee_contract_hours"""
    
    # Crear la aplicación
    app = create_app()
    
    # Comprobar si las columnas ya existen
    with app.app_context():
        engine = db.engine
        inspector = db.inspect(engine)
        columns = [c['name'] for c in inspector.get_columns('employee_contract_hours')]
        
        if 'use_normal_schedule' not in columns or 'use_flexibility' not in columns:
            print("Añadiendo nuevas columnas a la tabla employee_contract_hours...")
            
            try:
                # Añadir columna use_normal_schedule si no existe
                if 'use_normal_schedule' not in columns:
                    db.session.execute(text(
                        "ALTER TABLE employee_contract_hours ADD COLUMN use_normal_schedule BOOLEAN DEFAULT FALSE"
                    ))
                
                # Añadir columna use_flexibility si no existe
                if 'use_flexibility' not in columns:
                    db.session.execute(text(
                        "ALTER TABLE employee_contract_hours ADD COLUMN use_flexibility BOOLEAN DEFAULT FALSE"
                    ))
                
                db.session.commit()
                print("Columnas añadidas con éxito.")
            except Exception as e:
                db.session.rollback()
                print(f"Error al añadir las columnas: {str(e)}")
        else:
            print("Las columnas ya existen en la tabla employee_contract_hours.")

if __name__ == '__main__':
    add_activation_columns()
```


### migrate_employee_on_shift.py

#### `add_is_on_shift_column` (línea 10)

**Docstring:**
```
Añade la columna is_on_shift a la tabla employees
```

**Código:**
```python
def add_is_on_shift_column():
    """Añade la columna is_on_shift a la tabla employees"""
    try:
        # Verificar si la columna ya existe
        check_query = text("SELECT column_name FROM information_schema.columns WHERE table_name='employees' AND column_name='is_on_shift'")
        result = db.session.execute(check_query).fetchall()
        
        if not result:
            # La columna no existe, la añadimos
            print("Añadiendo columna is_on_shift a la tabla employees...")
            alter_query = text("ALTER TABLE employees ADD COLUMN is_on_shift BOOLEAN DEFAULT FALSE")
            db.session.execute(alter_query)
            db.session.commit()
            print("Columna is_on_shift añadida correctamente.")
        else:
            print("La columna is_on_shift ya existe en la tabla employees.")
        
        return True
    except Exception as e:
        db.session.rollback()
        print(f"Error al añadir la columna is_on_shift: {str(e)}")
        return False

if __name__ == "__main__":
    app = Flask(__name__)
    app.config.from_object(Config)
    db.init_app(app)
    
    with app.app_context():
        add_is_on_shift_column()
```


### migrate_label_templates.py

#### `create_label_tables` (línea 17)

**Docstring:**
```
Crea las tablas relacionadas con las etiquetas si no existen
```

**Código:**
```python
def create_label_tables():
    """Crea las tablas relacionadas con las etiquetas si no existen"""
    app = create_app()
    
    with app.app_context():
        # Verificar si las tablas ya existen
        inspector = db.inspect(db.engine)
        tables = inspector.get_table_names()
        
        # Crear tablas si no existen
        if 'label_templates' not in tables:
            print("Creando tabla label_templates...")
            db.metadata.tables['label_templates'].create(db.engine)
            print("Tabla label_templates creada exitosamente.")
        else:
            print("La tabla label_templates ya existe.")
            
        if 'product_labels' not in tables:
            print("Creando tabla product_labels...")
            db.metadata.tables['product_labels'].create(db.engine)
            print("Tabla product_labels creada exitosamente.")
        else:
            print("La tabla product_labels ya existe.")
            
        # Comprobar si hay plantillas por defecto existentes
        if 'label_templates' in tables:
            from models_tasks import Location
            
            # Para cada ubicación, crear una plantilla por defecto si no existe
            locations = Location.query.all()
            for location in locations:
                default_template = LabelTemplate.query.filter_by(
                    location_id=location.id, 
                    is_default=True
                ).first()
                
                if not default_template:
                    print(f"Creando plantilla por defecto para ubicación {location.name}...")
                    default_template = LabelTemplate(
                        name=f"Plantilla por defecto - {location.name}",
                        is_default=True,
                        location_id=location.id
                    )
                    db.session.add(default_template)
                    
            db.session.commit()
            print("Plantillas por defecto verificadas.")

if __name__ == "__main__":
    create_label_tables()
    print("Migración completada.")
```


### migrate_operation_hours.py

#### `migrate` (línea 10)

**Docstring:**
```
Agrega los campos necesarios para la configuración de horario de funcionamiento:
- operation_start_time: Hora de inicio de funcionamiento
- operation_end_time: Hora de fin de funcionamiento
- enforce_operation_hours: Si se debe aplicar el horario de funcionamiento
```

**Código:**
```python
def migrate():
    """
    Agrega los campos necesarios para la configuración de horario de funcionamiento:
    - operation_start_time: Hora de inicio de funcionamiento
    - operation_end_time: Hora de fin de funcionamiento
    - enforce_operation_hours: Si se debe aplicar el horario de funcionamiento
    """
    print("Iniciando migración para agregar campos de configuración de horario de funcionamiento...")
    
    # Obtener la URL de conexión de la variable de entorno
    db_url = os.environ.get('DATABASE_URL')
    if not db_url:
        print("ERROR: No se encontró la variable de entorno DATABASE_URL")
        return False
    
    # Conectar directamente a PostgreSQL
    try:
        # Crear conexión
        conn = psycopg2.connect(db_url)
        conn.autocommit = False  # Queremos control sobre la transacción
        
        # Crear cursor
        cursor = conn.cursor()
        
        try:
            # Verificar si las columnas ya existen
            cursor.execute("""
                SELECT column_name 
                FROM information_schema.columns 
                WHERE table_name = 'checkpoints' 
                AND (column_name = 'operation_start_time' 
                     OR column_name = 'operation_end_time' 
                     OR column_name = 'enforce_operation_hours')
            """)
            
            existing_columns = [col[0] for col in cursor.fetchall()]
            
            # Si todas las columnas ya existen, la migración ya se realizó
            if len(existing_columns) == 3:
                print("La migración ya ha sido ejecutada anteriormente. No se realizaron cambios.")
                conn.rollback()  # No hay cambios que aplicar
                return True
            
            # Intentar agregar cada columna si no existe
            if 'operation_start_time' not in existing_columns:
                print("Agregando columna operation_start_time...")
                cursor.execute("ALTER TABLE checkpoints ADD COLUMN operation_start_time TIME NULL")
            
            if 'operation_end_time' not in existing_columns:
                print("Agregando columna operation_end_time...")
                cursor.execute("ALTER TABLE checkpoints ADD COLUMN operation_end_time TIME NULL")
            
            if 'enforce_operation_hours' not in existing_columns:
                print("Agregando columna enforce_operation_hours...")
                cursor.execute("ALTER TABLE checkpoints ADD COLUMN enforce_operation_hours BOOLEAN NOT NULL DEFAULT false")
            
            # Confirmar los cambios
            conn.commit()
            print("Migración completada con éxito!")
            return True
            
        except Exception as e:
            conn.rollback()
            print(f"Error durante la migración (transacción revertida): {e}")
            return False
        finally:
            cursor.close()
            conn.close()
    
    except Exception as e:
        print(f"Error al conectar a la base de datos: {e}")
        return False


if __name__ == "__main__":
    success = migrate()
    sys.exit(0 if success else 1)
```


### migrate_original_records.py

#### `create_original_records_table` (línea 8)

**Docstring:**
```
Crea la tabla para almacenar registros originales de fichajes
```

**Código:**
```python
def create_original_records_table():
    """Crea la tabla para almacenar registros originales de fichajes"""
    app = create_app()
    with app.app_context():
        # Verificar si la tabla ya existe
        inspector = db.inspect(db.engine)
        if 'checkpoint_original_records' not in inspector.get_table_names():
            print("Creando tabla 'checkpoint_original_records'...")
            CheckPointOriginalRecord.__table__.create(db.engine)
            print("Tabla creada exitosamente.")
        else:
            print("La tabla 'checkpoint_original_records' ya existe.")

if __name__ == "__main__":
    create_original_records_table()
```


### migrate_task_instances.py

#### `create_task_instances_table` (línea 9)

**Docstring:**
```
Crea la tabla para almacenar instancias de tareas programadas
```

**Código:**
```python
def create_task_instances_table():
    """Crea la tabla para almacenar instancias de tareas programadas"""
    print("Iniciando migración de instancias de tareas...")
    app = create_app()
    
    with app.app_context():
        # Comprobar si la tabla ya existe
        inspector = inspect(db.engine)
        if 'task_instances' in inspector.get_table_names():
            print("La tabla task_instances ya existe. Saltando creación.")
            return
            
        # Crear la tabla
        db.create_all()
        
        print("Tabla task_instances creada correctamente.")
        
if __name__ == "__main__":
    create_task_instances_table()
```


### migrations/env.py

#### `get_engine` (línea 18)

**Código:**
```python
def get_engine():
    try:
        # this works with Flask-SQLAlchemy<3 and Alchemical
        return current_app.extensions['migrate'].db.get_engine()
    except (TypeError, AttributeError):
        # this works with Flask-SQLAlchemy>=3
        return current_app.extensions['migrate'].db.engine


```

#### `get_engine_url` (línea 27)

**Código:**
```python
def get_engine_url():
    try:
        return get_engine().url.render_as_string(hide_password=False).replace(
            '%', '%%')
    except AttributeError:
        return str(get_engine().url).replace('%', '%%')


# add your model's MetaData object here
# for 'autogenerate' support
# from myapp import mymodel
# target_metadata = mymodel.Base.metadata
config.set_main_option('sqlalchemy.url', get_engine_url())
target_db = current_app.extensions['migrate'].db

# other values from the config, defined by the needs of env.py,
# can be acquired:
# my_important_option = config.get_main_option("my_important_option")
# ... etc.


```

#### `get_metadata` (línea 48)

**Código:**
```python
def get_metadata():
    if hasattr(target_db, 'metadatas'):
        return target_db.metadatas[None]
    return target_db.metadata


```

#### `run_migrations_offline` (línea 54)

**Docstring:**
```
Run migrations in 'offline' mode.

This configures the context with just a URL
and not an Engine, though an Engine is acceptable
here as well.  By skipping the Engine creation
we don't even need a DBAPI to be available.

Calls to context.execute() here emit the given string to the
script output.
```

**Código:**
```python
def run_migrations_offline():
    """Run migrations in 'offline' mode.

    This configures the context with just a URL
    and not an Engine, though an Engine is acceptable
    here as well.  By skipping the Engine creation
    we don't even need a DBAPI to be available.

    Calls to context.execute() here emit the given string to the
    script output.

    """
    url = config.get_main_option("sqlalchemy.url")
    context.configure(
        url=url, target_metadata=get_metadata(), literal_binds=True
    )

    with context.begin_transaction():
        context.run_migrations()


```

#### `run_migrations_online` (línea 75)

**Docstring:**
```
Run migrations in 'online' mode.

In this scenario we need to create an Engine
and associate a connection with the context.
```

**Código:**
```python
def run_migrations_online():
    """Run migrations in 'online' mode.

    In this scenario we need to create an Engine
    and associate a connection with the context.

    """

    # this callback is used to prevent an auto-migration from being generated
    # when there are no changes to the schema
    # reference: http://alembic.zzzcomputing.com/en/latest/cookbook.html
    def process_revision_directives(context, revision, directives):
        if getattr(config.cmd_opts, 'autogenerate', False):
            script = directives[0]
            if script.upgrade_ops.is_empty():
                directives[:] = []
                logger.info('No changes in schema detected.')

    conf_args = current_app.extensions['migrate'].configure_args
    if conf_args.get("process_revision_directives") is None:
        conf_args["process_revision_directives"] = process_revision_directives

    connectable = get_engine()

    with connectable.connect() as connection:
        context.configure(
            connection=connection,
            target_metadata=get_metadata(),
            **conf_args
        )

        with context.begin_transaction():
            context.run_migrations()


if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()

```

#### `process_revision_directives` (línea 86)

**Código:**
```python
    def process_revision_directives(context, revision, directives):
        if getattr(config.cmd_opts, 'autogenerate', False):
            script = directives[0]
            if script.upgrade_ops.is_empty():
                directives[:] = []
                logger.info('No changes in schema detected.')

    conf_args = current_app.extensions['migrate'].configure_args
    if conf_args.get("process_revision_directives") is None:
        conf_args["process_revision_directives"] = process_revision_directives

    connectable = get_engine()

    with connectable.connect() as connection:
        context.configure(
            connection=connection,
            target_metadata=get_metadata(),
            **conf_args
        )

        with context.begin_transaction():
            context.run_migrations()


```


### migrations/versions/add_bank_account_to_companies.py

#### `upgrade` (línea 19)

**Código:**
```python
def upgrade():
    # Añadir columna bank_account a la tabla companies
    op.add_column('companies', sa.Column('bank_account', sa.String(24), nullable=True))
    
    # Cambiar el tamaño de la columna phone de 32 a 13 caracteres
    # Primero obtener el tipo de dato existente para preservar el resto de propiedades
    conn = op.get_bind()
    inspector = sa.inspect(conn)
    columns = inspector.get_columns('companies')
    column_info = next((c for c in columns if c['name'] == 'phone'), None)
    
    if column_info:
        # Solo actualizamos si la columna existe y tiene un tamaño diferente
        existing_type = column_info.get('type', None)
        if existing_type and hasattr(existing_type, 'length') and existing_type.length != 13:
            op.alter_column('companies', 'phone',
                           existing_type=sa.String(existing_type.length),
                           type_=sa.String(13),
                           existing_nullable=True)


```

#### `downgrade` (línea 40)

**Código:**
```python
def downgrade():
    # Eliminar la columna bank_account de la tabla companies
    op.drop_column('companies', 'bank_account')
    
    # Restaurar el tamaño de la columna phone a 32 caracteres
    op.alter_column('companies', 'phone',
                   existing_type=sa.String(13),
                   type_=sa.String(32),
                   existing_nullable=True)
```


### models.py

#### `company_id` (línea 41)

**Código:**
```python
    def company_id(self):
        if self.companies and len(self.companies) > 0:
            return self.companies[0].id
        return None
        
    @property
```

#### `company` (línea 47)

**Código:**
```python
    def company(self):
        if self.companies and len(self.companies) > 0:
            return self.companies[0]
        return None
    
    # Otras relaciones
    employee = db.relationship('Employee', back_populates='user', uselist=False)
    activity_logs = db.relationship('ActivityLog', back_populates='user', cascade='all, delete-orphan')
    
```

#### `set_password` (línea 56)

**Código:**
```python
    def set_password(self, password):
        self.password_hash = generate_password_hash(password)
        
```

#### `check_password` (línea 59)

**Código:**
```python
    def check_password(self, password):
        return check_password_hash(self.password_hash, password)
    
```

#### `is_admin` (línea 62)

**Código:**
```python
    def is_admin(self):
        return self.role == UserRole.ADMIN
        
```

#### `is_gerente` (línea 65)

**Código:**
```python
    def is_gerente(self):
        return self.role == UserRole.GERENTE
        
```

#### `is_empleado` (línea 68)

**Código:**
```python
    def is_empleado(self):
        return self.role == UserRole.EMPLEADO
    
```

#### `__repr__` (línea 71)

**Código:**
```python
    def __repr__(self):
        return f'<User {self.username}>'
        
```

#### `to_dict` (línea 74)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'username': self.username,
            'email': self.email,
            'role': self.role.value,
            'full_name': f"{self.first_name} {self.last_name}",
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'company_id': self.company_id,  # Para compatibilidad
            'companies': [{'id': c.id, 'name': c.name} for c in self.companies] if self.companies else []
        }
        
```

#### `load_user` (línea 87)

**Código:**
```python
def load_user(user_id):
    return User.query.get(int(user_id))

```

#### `__repr__` (línea 114)

**Código:**
```python
    def __repr__(self):
        return f'<Company {self.name}>'
        
```

#### `get_slug` (línea 117)

**Docstring:**
```
Obtiene el slug (URL amigable) del nombre de la empresa
```

**Código:**
```python
    def get_slug(self):
        """Obtiene el slug (URL amigable) del nombre de la empresa"""
        from utils import slugify
        return slugify(self.name)
        
```

#### `to_dict` (línea 122)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'slug': self.get_slug(),
            'address': self.address,
            'city': self.city,
            'postal_code': self.postal_code,
            'country': self.country,
            'sector': self.sector,
            'tax_id': self.tax_id,
            'phone': self.phone,
            'email': self.email,
            'website': self.website,
            'bank_account': self.bank_account,
            'is_active': self.is_active,
            'employee_count': len(self.employees)
        }

```

#### `__repr__` (línea 201)

**Código:**
```python
    def __repr__(self):
        return f'<Employee {self.first_name} {self.last_name}>'
        
```

#### `to_dict` (línea 204)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'first_name': self.first_name,
            'last_name': self.last_name,
            'full_name': f"{self.first_name} {self.last_name}",
            'dni': self.dni,
            'position': self.position,
            'contract_type': self.contract_type.value if self.contract_type else None,
            'bank_account': self.bank_account,
            'start_date': self.start_date.isoformat() if self.start_date else None,
            'end_date': self.end_date.isoformat() if self.end_date else None,
            'is_active': self.is_active,
            'is_on_shift': self.is_on_shift,  # Agregar estado de jornada
            'status': self.status.value if self.status else 'activo',
            'status_start_date': self.status_start_date.isoformat() if self.status_start_date else None,
            'status_end_date': self.status_end_date.isoformat() if self.status_end_date else None,
            'company_id': self.company_id,
            'company_name': self.company.name if self.company else None
        }

```

#### `__repr__` (línea 241)

**Código:**
```python
    def __repr__(self):
        return f'<EmployeeDocument {self.original_filename}>'

```

#### `__repr__` (línea 258)

**Código:**
```python
    def __repr__(self):
        return f'<EmployeeNote {self.id}>'

```

#### `__repr__` (línea 276)

**Código:**
```python
    def __repr__(self):
        return f'<EmployeeHistory {self.field_name}>'

```

#### `__repr__` (línea 294)

**Código:**
```python
    def __repr__(self):
        return f'<EmployeeSchedule {self.employee.last_name} - {self.day_of_week.value}>'
    
```

#### `to_dict` (línea 297)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'day_of_week': self.day_of_week.value,
            'start_time': self.start_time.strftime('%H:%M') if self.start_time else None,
            'end_time': self.end_time.strftime('%H:%M') if self.end_time else None,
            'is_working_day': self.is_working_day,
            'employee_id': self.employee_id
        }

```

#### `__repr__` (línea 322)

**Código:**
```python
    def __repr__(self):
        return f'<EmployeeCheckIn {self.employee.last_name} - {self.check_in_time}>'
    
```

#### `to_dict` (línea 325)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'check_in_time': self.check_in_time.strftime('%Y-%m-%d %H:%M:%S'),
            'check_out_time': self.check_out_time.strftime('%Y-%m-%d %H:%M:%S') if self.check_out_time else None,
            'is_generated': self.is_generated,
            'notes': self.notes,
            'employee_id': self.employee_id,
            'employee_name': f"{self.employee.first_name} {self.employee.last_name}" if self.employee else None
        }
    
    @staticmethod
```

#### `generate_realistic_time` (línea 337)

**Docstring:**
```
Generate a realistic check-in or check-out time with random variation.
```

**Código:**
```python
    def generate_realistic_time(base_time, minutes_range=4):
        """Generate a realistic check-in or check-out time with random variation."""
        # For check-in: ensure it's 1-4 minutes before scheduled time
        # For check-out: ensure it's 1-4 minutes after scheduled time
        
        if 'check_in' in str(base_time):
            # Llegada anticipada: entre 1-4 minutos antes (siempre por debajo de la hora programada)
            minutes_variation = random.randint(1, minutes_range)
            seconds_variation = random.randint(0, 59)
            return base_time - timedelta(minutes=minutes_variation, seconds=seconds_variation)
        else:
            # Salida posterior: entre 1-4 minutos después
            minutes_variation = random.randint(1, minutes_range)
            seconds_variation = random.randint(0, 59)
            return base_time + timedelta(minutes=minutes_variation, seconds=seconds_variation)
            
    @classmethod
```

#### `generate_check_ins_for_schedule` (línea 354)

**Docstring:**
```
Generate check-ins for an employee based on their schedule.
```

**Código:**
```python
    def generate_check_ins_for_schedule(cls, employee, start_date, end_date):
        """Generate check-ins for an employee based on their schedule."""
        
        if not employee.schedules:
            return []
            
        # Create a map of weekday to schedule
        weekday_map = {
            0: WeekDay.LUNES,
            1: WeekDay.MARTES,
            2: WeekDay.MIERCOLES,
            3: WeekDay.JUEVES,
            4: WeekDay.VIERNES,
            5: WeekDay.SABADO,
            6: WeekDay.DOMINGO
        }
        
        # Map employee schedules by day of week
        schedule_map = {}
        for schedule in employee.schedules:
            for i, day in enumerate(weekday_map.values()):
                if schedule.day_of_week == day:
                    schedule_map[i] = schedule
                    
        # Check if employee is on vacation for any days in range
        vacation_days = set()
        for vacation in employee.vacations:
            if vacation.status == VacationStatus.REGISTRADA or vacation.status == VacationStatus.DISFRUTADA:
                current_day = vacation.start_date
                while current_day <= vacation.end_date:
                    vacation_days.add(current_day)
                    current_day += timedelta(days=1)
        
        # Generate check-ins for each day
        generated_check_ins = []
        current_date = start_date
        while current_date <= end_date:
            # Skip if employee is on vacation
            if current_date in vacation_days:
                current_date += timedelta(days=1)
                continue
                
            # Get weekday index (0=Monday, 6=Sunday)
            weekday = current_date.weekday()
            
            # Skip if no schedule for this day
            if weekday not in schedule_map:
                current_date += timedelta(days=1)
                continue
                
            schedule = schedule_map[weekday]
            
            # Skip if not a working day
            if not schedule.is_working_day:
                current_date += timedelta(days=1)
                continue
                
            # Create base check-in and check-out times
            check_in_base = datetime.combine(current_date, schedule.start_time)
            check_out_base = datetime.combine(current_date, schedule.end_time)
            
            # Generate realistic times
            check_in_time = cls.generate_realistic_time(check_in_base, 4)
            check_out_time = cls.generate_realistic_time(check_out_base, 4)
            
            # Create check-in record
            check_in = cls(
                employee_id=employee.id,
                check_in_time=check_in_time,
                check_out_time=check_out_time,
                is_generated=True,
                notes="Generado automáticamente"
            )
            
            generated_check_ins.append(check_in)
            current_date += timedelta(days=1)
            
        return generated_check_ins

```

#### `__repr__` (línea 451)

**Código:**
```python
    def __repr__(self):
        return f'<EmployeeVacation {self.employee.last_name} - {self.start_date} to {self.end_date}>'
    
```

#### `to_dict` (línea 454)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'start_date': self.start_date.isoformat(),
            'end_date': self.end_date.isoformat(),
            'status': self.status.value,
            'is_signed': self.is_signed,
            'is_enjoyed': self.is_enjoyed,
            'notes': self.notes,
            'employee_id': self.employee_id,
            'employee_name': f"{self.employee.first_name} {self.employee.last_name}" if self.employee else None,
            'duration_days': (self.end_date - self.start_date).days + 1 if self.start_date and self.end_date else 0
        }
    
```

#### `total_days` (línea 468)

**Docstring:**
```
Calculate the total number of days in this vacation period.
```

**Código:**
```python
    def total_days(self):
        """Calculate the total number of days in this vacation period."""
        if not self.start_date or not self.end_date:
            return 0
        return (self.end_date - self.start_date).days + 1
    
```

#### `mark_as_signed` (línea 474)

**Docstring:**
```
Mark the vacation as signed by the employee.
```

**Código:**
```python
    def mark_as_signed(self):
        """Mark the vacation as signed by the employee."""
        self.is_signed = True
        return self
        
```

#### `mark_as_enjoyed` (línea 479)

**Docstring:**
```
Mark the vacation as enjoyed after the date has passed.
```

**Código:**
```python
    def mark_as_enjoyed(self):
        """Mark the vacation as enjoyed after the date has passed."""
        self.is_enjoyed = True
        return self
        
```

#### `overlaps_with` (línea 484)

**Docstring:**
```
Check if this vacation period overlaps with the given dates.
```

**Código:**
```python
    def overlaps_with(self, start_date, end_date):
        """Check if this vacation period overlaps with the given dates."""
        return (self.start_date <= end_date and self.end_date >= start_date)

```

#### `__repr__` (línea 500)

**Código:**
```python
    def __repr__(self):
        return f'<ActivityLog {self.action}>'

```


### models_checkpoints.py

#### `__repr__` (línea 58)

**Código:**
```python
    def __repr__(self):
        return f'<CheckPoint {self.name} ({self.company.name})>'
    
```

#### `set_password` (línea 61)

**Código:**
```python
    def set_password(self, password):
        self.password_hash = generate_password_hash(password)
    
```

#### `verify_password` (línea 64)

**Código:**
```python
    def verify_password(self, password):
        return check_password_hash(self.password_hash, password)


```

#### `__repr__` (línea 102)

**Código:**
```python
    def __repr__(self):
        status = "Completado" if self.check_out_time else "Pendiente de salida"
        return f'<Fichaje {self.employee.first_name} {self.check_in_time.strftime("%d/%m/%Y %H:%M")} - {status}>'
    
```

#### `duration` (línea 106)

**Docstring:**
```
Calcula la duración del fichaje en horas
```

**Código:**
```python
    def duration(self):
        """Calcula la duración del fichaje en horas"""
        if not self.check_out_time:
            return None
        
        # Asegurarse de que ambas fechas tengan la misma información de zona horaria
        from timezone_config import datetime_to_madrid
        
        # Convertir ambas fechas a aware con la misma zona horaria
        check_in = datetime_to_madrid(self.check_in_time)
        check_out = datetime_to_madrid(self.check_out_time)
        
        # Si la hora de salida es anterior a la de entrada, podría ser un turno nocturno
        # En ese caso, asumimos que la salida corresponde al día siguiente
        if check_out < check_in:
            # Ajustar manualmente el día para calcular correctamente
            delta = (check_out - check_in).total_seconds() + (24 * 3600)  # Sumar 24 horas en segundos
            return delta / 3600  # Convertir a horas
        
        delta = check_out - check_in
        return delta.total_seconds() / 3600  # Convertir a horas
    
    @property
```

#### `has_original_record` (línea 129)

**Docstring:**
```
Comprueba si este registro tiene un registro original asociado
```

**Código:**
```python
    def has_original_record(self):
        """Comprueba si este registro tiene un registro original asociado"""
        # Evitamos la importación cíclica usando el nombre de la tabla directamente
        return db.session.query(db.Model.metadata.tables['checkpoint_original_records']).filter_by(record_id=self.id).count() > 0
    
```

#### `to_dict` (línea 134)

**Docstring:**
```
Convierte el registro a un diccionario para serialización
```

**Código:**
```python
    def to_dict(self):
        """Convierte el registro a un diccionario para serialización"""
        result = {
            'id': self.id,
            'employee_id': self.employee_id,
            'employee_name': f"{self.employee.first_name} {self.employee.last_name}",
            'check_in_time': self.check_in_time.isoformat() if self.check_in_time else None,
            'check_out_time': self.check_out_time.isoformat() if self.check_out_time else None,
            'duration': None if self.duration() is None else round(self.duration(), 2),
            'adjusted': self.adjusted,
            'has_signature': self.has_signature,
            'has_original_record': self.has_original_record
        }
        
        if self.adjusted and self.original_check_in_time:
            result['original_check_in_time'] = self.original_check_in_time.isoformat()
            if self.original_check_out_time:
                result['original_check_out_time'] = self.original_check_out_time.isoformat()
                
        return result


```

#### `__repr__` (línea 175)

**Código:**
```python
    def __repr__(self):
        return f'<Incidencia {self.incident_type.value} - {self.created_at.strftime("%d/%m/%Y")}>'
    
```

#### `resolve` (línea 178)

**Docstring:**
```
Marca la incidencia como resuelta
```

**Código:**
```python
    def resolve(self, user_id, notes=None):
        """Marca la incidencia como resuelta"""
        self.resolved = True
        self.resolved_at = datetime.utcnow()
        self.resolved_by_id = user_id
        if notes:
            self.resolution_notes = notes


```

#### `duration` (línea 214)

**Docstring:**
```
Calcula la duración del fichaje original en horas
```

**Código:**
```python
    def duration(self):
        """Calcula la duración del fichaje original en horas"""
        if not self.original_check_out_time:
            return None
            
        # Asegurarse de que ambas fechas tengan la misma información de zona horaria
        from timezone_config import datetime_to_madrid
        
        # Convertir ambas fechas a aware con la misma zona horaria
        check_in = datetime_to_madrid(self.original_check_in_time)
        check_out = datetime_to_madrid(self.original_check_out_time)
        
        # Si la hora de salida es anterior a la de entrada, podría ser un turno nocturno
        # En ese caso, asumimos que la salida corresponde al día siguiente
        if check_out < check_in:
            # Ajustar manualmente el día para calcular correctamente
            delta = (check_out - check_in).total_seconds() + (24 * 3600)  # Sumar 24 horas en segundos
            return delta / 3600  # Convertir a horas
        
        delta = check_out - check_in
        return delta.total_seconds() / 3600  # Convertir segundos a horas
    
```

#### `__repr__` (línea 236)

**Código:**
```python
    def __repr__(self):
        return f"<CheckPointOriginalRecord {self.id} - Record {self.record_id}>"
    
```

#### `to_dict` (línea 239)

**Docstring:**
```
Convierte el registro a un diccionario para serialización
```

**Código:**
```python
    def to_dict(self):
        """Convierte el registro a un diccionario para serialización"""
        result = {
            'id': self.id,
            'record_id': self.record_id,
            'original_check_in_time': self.original_check_in_time.isoformat() if self.original_check_in_time else None,
            'original_check_out_time': self.original_check_out_time.isoformat() if self.original_check_out_time else None,
            'duration': self.duration(),
            'original_has_signature': self.original_has_signature,
            'original_notes': self.original_notes,
            'adjusted_at': self.adjusted_at.isoformat() if self.adjusted_at else None,
            'adjusted_by_id': self.adjusted_by_id,
            'adjustment_reason': self.adjustment_reason
        }
        return result


```

#### `__repr__` (línea 287)

**Código:**
```python
    def __repr__(self):
        return f'<ContractHours {self.employee.first_name} - {self.daily_hours}h/día, {self.weekly_hours}h/semana>'
    
```

#### `is_overtime` (línea 290)

**Docstring:**
```
Comprueba si una duración de horas supera el máximo diario
```

**Código:**
```python
    def is_overtime(self, duration_hours):
        """Comprueba si una duración de horas supera el máximo diario"""
        return duration_hours > self.daily_hours
    
```

#### `calculate_adjusted_hours` (línea 294)

**Docstring:**
```
Calcula el tiempo ajustado según el contrato y configuración
```

**Código:**
```python
    def calculate_adjusted_hours(self, check_in_time, check_out_time):
        """Calcula el tiempo ajustado según el contrato y configuración"""
        if not check_out_time:
            return None, None
            
        # Asegurarse de que ambas fechas tengan la misma información de zona horaria
        from timezone_config import datetime_to_madrid, TIMEZONE
        
        # Convertir ambas fechas a aware con la misma zona horaria
        check_in_time = datetime_to_madrid(check_in_time)
        check_out_time = datetime_to_madrid(check_out_time)
            
        adjusted_in = check_in_time
        adjusted_out = check_out_time
        
        # 1. Verificar si se debe aplicar horario normal
        if self.use_normal_schedule and self.normal_start_time and self.normal_end_time:
            # Si la hora de entrada está fuera del horario normal (demasiado temprano), ajustar
            # Creamos un datetime aware con la fecha de check_in y la hora de normal_start
            check_in_date = check_in_time.date()
            normal_start_datetime = datetime.combine(check_in_date, self.normal_start_time)
            # Convertir a aware con zona horaria
            normal_start_datetime = TIMEZONE.localize(normal_start_datetime)
            
            # Permitimos margen de flexibilidad si está configurado
            if self.use_flexibility and self.checkin_flexibility:
                early_limit = normal_start_datetime - timedelta(minutes=self.checkin_flexibility)
                # Si la entrada es antes del límite de flexibilidad, ajustamos
                if check_in_time < early_limit:
                    adjusted_in = normal_start_datetime
            else:
                # Sin flexibilidad, ajustamos al horario normal exacto
                if check_in_time < normal_start_datetime:
                    adjusted_in = normal_start_datetime
            
            # Si la hora de salida es después del horario normal, ajustar (solo si no se permiten horas extra)
            if not self.allow_overtime and self.normal_end_time:
                check_out_date = check_out_time.date()
                normal_end_datetime = datetime.combine(check_out_date, self.normal_end_time)
                # Convertir a aware con zona horaria
                normal_end_datetime = TIMEZONE.localize(normal_end_datetime)
                
                # Considerar si la salida es al día siguiente
                if normal_end_datetime < normal_start_datetime:
                    next_day = check_out_time.date() + timedelta(days=1)
                    normal_end_datetime = datetime.combine(next_day, self.normal_end_time)
                    # Convertir a aware con zona horaria
                    normal_end_datetime = TIMEZONE.localize(normal_end_datetime)
                
                # Permitimos margen de flexibilidad si está configurado
                if self.use_flexibility and self.checkout_flexibility:
                    late_limit = normal_end_datetime + timedelta(minutes=self.checkout_flexibility)
                    # Si la salida es después del límite de flexibilidad, ajustamos
                    if check_out_time > late_limit:
                        adjusted_out = normal_end_datetime
                else:
                    # Sin flexibilidad, ajustamos al horario normal exacto
                    if check_out_time > normal_end_datetime:
                        adjusted_out = normal_end_datetime
        
        # 2. Calcular duración ajustada con los posibles cambios anteriores
        duration = (adjusted_out - adjusted_in).total_seconds() / 3600
        
        # 3. Verificar límite de horas diarias
        if duration <= self.daily_hours:
            return adjusted_in, adjusted_out
            
        # 4. Si excede las horas y no se permiten horas extra (o excede el límite de horas extra), ajustamos
        if not self.allow_overtime or (self.allow_overtime and duration > (self.daily_hours + self.max_overtime_daily)):
            # Determinamos cuál es el límite máximo
            max_hours = self.daily_hours
            if self.allow_overtime:
                max_hours = self.daily_hours + self.max_overtime_daily
                
            # Ajustamos la hora de entrada para que la duración sea igual a las horas permitidas
            new_check_in_time = adjusted_out - timedelta(hours=max_hours)
            return new_check_in_time, adjusted_out
            
        return adjusted_in, adjusted_out
```


### models_tasks.py

#### `__repr__` (línea 53)

**Código:**
```python
    def __repr__(self):
        return f'<TaskGroup {self.name}>'
    
```

#### `to_dict` (línea 56)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'description': self.description,
            'color': self.color,
            'location_id': self.location_id,
            'location_name': self.location.name if self.location else None
        }
    
```

#### `__repr__` (línea 93)

**Código:**
```python
    def __repr__(self):
        return f'<Location {self.name}>'
    
```

#### `set_portal_password` (línea 96)

**Docstring:**
```
Establece una contraseña encriptada para el portal
```

**Código:**
```python
    def set_portal_password(self, password):
        """Establece una contraseña encriptada para el portal"""
        self.portal_password_hash = generate_password_hash(password)
        
```

#### `check_portal_password` (línea 100)

**Docstring:**
```
Verifica si la contraseña proporcionada coincide con la almacenada
```

**Código:**
```python
    def check_portal_password(self, password):
        """Verifica si la contraseña proporcionada coincide con la almacenada"""
        # Si hay hash de contraseña, verificamos contra esa
        if self.portal_password_hash:
            return check_password_hash(self.portal_password_hash, password)
        # Si no, comparamos con la contraseña fija
        return password == self.portal_fixed_password
    
    @property
```

#### `portal_fixed_username` (línea 109)

**Docstring:**
```
Retorna el nombre de usuario para este local
```

**Código:**
```python
    def portal_fixed_username(self):
        """Retorna el nombre de usuario para este local"""
        # Siempre usamos el formato predeterminado
        return f"portal_{self.id}"
        
    @property
```

#### `portal_fixed_password` (línea 115)

**Docstring:**
```
Retorna la contraseña para este local
```

**Código:**
```python
    def portal_fixed_password(self):
        """Retorna la contraseña para este local"""
        # Si hay contraseña personalizada (hash), significará que el usuario ha establecido una contraseña personalizada
        if self.portal_password_hash:
            # No podemos recuperar la contraseña real (solo tenemos el hash)
            # Usaremos una función específica para validar la contraseña en lugar de mostrarla
            return None  # No podemos mostrar la contraseña real
        # Si no hay contraseña personalizada, usamos el formato predeterminado
        return f"Portal{self.id}2025!"
    
```

#### `to_dict` (línea 125)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'address': self.address,
            'city': self.city,
            'postal_code': self.postal_code,
            'description': self.description,
            'company_id': self.company_id,
            'company_name': self.company.name if self.company else None,
            'is_active': self.is_active,
            'has_portal_credentials': True  # Siempre tiene credenciales fijas
        }

```

#### `__repr__` (línea 159)

**Código:**
```python
    def __repr__(self):
        return f'<LocalUser {self.name} {self.last_name}>'
    
```

#### `set_pin` (línea 162)

**Código:**
```python
    def set_pin(self, pin):
        # Almacenamos el PIN como hash por seguridad
        self.pin = generate_password_hash(pin)
        
```

#### `check_pin` (línea 166)

**Código:**
```python
    def check_pin(self, pin):
        return check_password_hash(self.pin, pin)
    
```

#### `get_full_name` (línea 169)

**Código:**
```python
    def get_full_name(self):
        return f"{self.name} {self.last_name}"
    
```

#### `to_dict` (línea 172)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'username': self.username,
            'photo_path': self.photo_path,
            'location_id': self.location_id,
            'location_name': self.location.name if self.location else None,
            'is_active': self.is_active
        }

```

#### `__repr__` (línea 214)

**Código:**
```python
    def __repr__(self):
        return f'<Task {self.title}>'
    
```

#### `to_dict` (línea 217)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'title': self.title,
            'description': self.description,
            'priority': self.priority.value if self.priority else None,
            'frequency': self.frequency.value if self.frequency else None,
            'status': self.status.value if self.status else None,
            'start_date': self.start_date.isoformat() if self.start_date else None,
            'end_date': self.end_date.isoformat() if self.end_date else None,
            'location_id': self.location_id,
            'location_name': self.location.name if self.location else None
        }
    
```

#### `is_due_today` (línea 231)

**Docstring:**
```
Comprueba si la tarea está programada para hoy según su programación.
```

**Código:**
```python
    def is_due_today(self):
        """Comprueba si la tarea está programada para hoy según su programación."""
        today = date.today()
        today_weekday = today.weekday()  # 0 es lunes, 6 es domingo
        
        # Si la tarea tiene fecha de fin y ya ha pasado, no está activa
        if self.end_date and today > self.end_date:
            return False
        
        # Si la tarea tiene fecha de inicio y aún no ha llegado, no está activa
        if self.start_date and today < self.start_date:
            return False
        
        # Para tareas personalizadas con múltiples días, verificamos los días configurados
        if self.frequency == TaskFrequency.PERSONALIZADA and self.weekdays:
            for weekday_entry in self.weekdays:
                if TaskWeekday.day_matches_today(weekday_entry.day_of_week):
                    return True
            # Si llegamos aquí, es que hoy no es uno de los días configurados
            return False
            
        # Si no hay programación específica (schedule_details está vacío),
        # consideramos que la tarea está activa según su frecuencia
        if not self.schedule_details:
            # Para tareas diarias, siempre están activas
            if self.frequency == TaskFrequency.DIARIA:
                return True
                
            # Para tareas semanales, verificamos si today es el mismo día de la semana que start_date
            elif self.frequency == TaskFrequency.SEMANAL and self.start_date:
                return today.weekday() == self.start_date.weekday()
                
            # Para tareas mensuales, verificamos si today es el mismo día del mes que start_date
            elif self.frequency == TaskFrequency.MENSUAL and self.start_date:
                return today.day == self.start_date.day
                
            # Para tareas quincenales, verificamos si han pasado múltiplos de 15 días desde start_date
            elif self.frequency == TaskFrequency.QUINCENAL and self.start_date:
                delta = (today - self.start_date).days
                return delta % 15 == 0
            
            # Para cualquier otro caso, mostramos la tarea
            return True
        
        # Comprobamos la programación específica
        for schedule in self.schedule_details:
            if schedule.is_active_for_date(today):
                return True
                
        return False

```

#### `__repr__` (línea 297)

**Código:**
```python
    def __repr__(self):
        if self.day_of_week:
            return f'<TaskSchedule {self.task.title} - {self.day_of_week.value}>'
        elif self.day_of_month:
            return f'<TaskSchedule {self.task.title} - Day {self.day_of_month}>'
        else:
            return f'<TaskSchedule {self.task.title}>'
    
```

#### `is_active_for_date` (línea 305)

**Docstring:**
```
Comprueba si este horario está activo para una fecha determinada.
```

**Código:**
```python
    def is_active_for_date(self, check_date):
        """Comprueba si este horario está activo para una fecha determinada."""
        # Si es una tarea diaria, siempre está activa
        if self.task.frequency == TaskFrequency.DIARIA:
            return True
            
        # Para tareas semanales, comprobamos el día de la semana
        if self.task.frequency == TaskFrequency.SEMANAL and self.day_of_week:
            day_map = {
                WeekDay.LUNES: 0,
                WeekDay.MARTES: 1,
                WeekDay.MIERCOLES: 2,
                WeekDay.JUEVES: 3,
                WeekDay.VIERNES: 4,
                WeekDay.SABADO: 5,
                WeekDay.DOMINGO: 6
            }
            return check_date.weekday() == day_map[self.day_of_week]
            
        # Para tareas mensuales, comprobamos el día del mes
        if self.task.frequency == TaskFrequency.MENSUAL and self.day_of_month:
            return check_date.day == self.day_of_month
            
        # Para tareas quincenales (cada 15 días)
        if self.task.frequency == TaskFrequency.QUINCENAL:
            if not self.task.start_date:
                return False
                
            delta = (check_date - self.task.start_date).days
            return delta % 15 == 0
            
        # Si llegamos aquí y no hemos retornado, no está activa
        return False
        
```

#### `__repr__` (línea 350)

**Código:**
```python
    def __repr__(self):
        return f'<TaskWeekday {self.task.title} - {self.day_of_week.value}>'
        
    @classmethod
```

#### `day_matches_today` (línea 354)

**Docstring:**
```
Comprueba si el día de la semana corresponde al día actual
```

**Código:**
```python
    def day_matches_today(cls, weekday):
        """Comprueba si el día de la semana corresponde al día actual"""
        day_map = {
            WeekDay.LUNES: 0,
            WeekDay.MARTES: 1,
            WeekDay.MIERCOLES: 2,
            WeekDay.JUEVES: 3,
            WeekDay.VIERNES: 4,
            WeekDay.SABADO: 5,
            WeekDay.DOMINGO: 6
        }
        return date.today().weekday() == day_map[weekday]

```

#### `__repr__` (línea 384)

**Código:**
```python
    def __repr__(self):
        return f'<TaskInstance {self.task.title} on {self.scheduled_date}>'
    
```

#### `to_dict` (línea 387)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'task_id': self.task_id,
            'task_title': self.task.title if self.task else None,
            'scheduled_date': self.scheduled_date.isoformat() if self.scheduled_date else None,
            'status': self.status.value if self.status else None,
            'notes': self.notes,
            'completed_by': self.completed_by.name if self.completed_by else None
        }

```

#### `__repr__` (línea 411)

**Código:**
```python
    def __repr__(self):
        return f'<TaskCompletion {self.task.title} by {self.local_user.name}>'
    
```

#### `to_dict` (línea 414)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'task_id': self.task_id,
            'task_title': self.task.title if self.task else None,
            'local_user_id': self.local_user_id,
            'local_user_name': self.local_user.name if self.local_user else None,
            'completion_date': self.completion_date.isoformat() if self.completion_date else None,
            'notes': self.notes
        }

# Modelos para el sistema de etiquetas

```

#### `__repr__` (línea 456)

**Código:**
```python
    def __repr__(self):
        return f'<Product {self.name}>'
    
```

#### `to_dict` (línea 459)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'description': self.description,
            'shelf_life_days': self.shelf_life_days,
            'location_id': self.location_id,
            'location_name': self.location.name if self.location else None,
            'is_active': self.is_active
        }
        
```

#### `get_shelf_life_expiry` (línea 470)

**Docstring:**
```
Calcula la fecha de caducidad secundaria basada en la vida útil en días
```

**Código:**
```python
    def get_shelf_life_expiry(self, from_date=None):
        """Calcula la fecha de caducidad secundaria basada en la vida útil en días"""
        if self.shelf_life_days <= 0:
            return None
            
        if from_date is None:
            from_date = datetime.now()
            
        # Asegurar que trabajamos con un datetime
        if isinstance(from_date, date) and not isinstance(from_date, datetime):
            from_date = datetime.combine(from_date, datetime.min.time())
            
        # Calcular fecha de caducidad secundaria (solo fecha, sin hora)
        return (from_date + timedelta(days=self.shelf_life_days)).date()

```

#### `__repr__` (línea 499)

**Código:**
```python
    def __repr__(self):
        return f'<ProductConservation {self.product.name} - {self.conservation_type.value}>'
    
```

#### `to_dict` (línea 502)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'product_id': self.product_id,
            'product_name': self.product.name if self.product else None,
            'conservation_type': self.conservation_type.value,
            'hours_valid': self.hours_valid
        }
    
```

#### `get_expiry_date` (línea 511)

**Docstring:**
```
Calcula la fecha de caducidad basada en horas
```

**Código:**
```python
    def get_expiry_date(self, from_date=None):
        """Calcula la fecha de caducidad basada en horas"""
        expiry_datetime = self.get_expiry_datetime(from_date)
        return expiry_datetime.date()
        
```

#### `get_expiry_datetime` (línea 516)

**Docstring:**
```
Calcula el datetime exacto de caducidad, incluyendo la hora
```

**Código:**
```python
    def get_expiry_datetime(self, from_date=None):
        """Calcula el datetime exacto de caducidad, incluyendo la hora"""
        if from_date is None:
            from_date = datetime.now()
            
        # Asegurar que trabajamos con un datetime
        if isinstance(from_date, date) and not isinstance(from_date, datetime):
            from_date = datetime.combine(from_date, datetime.min.time())
            
        # Retornar el datetime completo con hora exacta
        return from_date + timedelta(hours=self.hours_valid)
        
```

#### `__repr__` (línea 577)

**Código:**
```python
    def __repr__(self):
        return f'<LabelTemplate {self.id} - {self.name}>'
    
```

#### `to_dict` (línea 580)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None,
            'is_default': self.is_default,
            'titulo_x': self.titulo_x,
            'titulo_y': self.titulo_y,
            'titulo_size': self.titulo_size,
            'titulo_bold': self.titulo_bold,
            'conservacion_x': self.conservacion_x,
            'conservacion_y': self.conservacion_y,
            'conservacion_size': self.conservacion_size,
            'conservacion_bold': self.conservacion_bold,
            'preparador_x': self.preparador_x,
            'preparador_y': self.preparador_y,
            'preparador_size': self.preparador_size,
            'preparador_bold': self.preparador_bold,
            'fecha_x': self.fecha_x,
            'fecha_y': self.fecha_y,
            'fecha_size': self.fecha_size,
            'fecha_bold': self.fecha_bold,
            'caducidad_x': self.caducidad_x,
            'caducidad_y': self.caducidad_y,
            'caducidad_size': self.caducidad_size,
            'caducidad_bold': self.caducidad_bold,
            'caducidad2_x': self.caducidad2_x,
            'caducidad2_y': self.caducidad2_y,
            'caducidad2_size': self.caducidad2_size,
            'caducidad2_bold': self.caducidad2_bold,
            'location_id': self.location_id
        }

```

#### `__repr__` (línea 629)

**Código:**
```python
    def __repr__(self):
        return f'<ProductLabel {self.product.name} - {self.conservation_type.value} - {self.expiry_date}>'
    
```

#### `to_dict` (línea 632)

**Código:**
```python
    def to_dict(self):
        return {
            'id': self.id,
            'product_id': self.product_id,
            'product_name': self.product.name if self.product else None,
            'local_user_id': self.local_user_id,
            'local_user_name': self.local_user.name if self.local_user else None,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'expiry_date': self.expiry_date.isoformat() if self.expiry_date else None,
            'conservation_type': self.conservation_type.value
        }
```


### reset_checkpoint_password.py

#### `reset_checkpoint_password` (línea 5)

**Docstring:**
```
Resetea la contraseña del punto de fichaje con username 'movil' a 'movil'
```

**Código:**
```python
def reset_checkpoint_password():
    """Resetea la contraseña del punto de fichaje con username 'movil' a 'movil'"""
    with app.app_context():
        checkpoint = CheckPoint.query.filter_by(username='movil').first()
        if checkpoint:
            checkpoint.set_password('movil')
            db.session.commit()
            print(f"Contraseña actualizada para el punto de fichaje '{checkpoint.name}'")
        else:
            print("No se encontró el punto de fichaje con username 'movil'")

if __name__ == "__main__":
    reset_checkpoint_password()
```


### routes.py

#### `admin_required` (línea 36)

**Código:**
```python
def admin_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or not current_user.is_admin():
            flash('No tienes permiso para acceder a esta página.', 'danger')
            return redirect(url_for('main.index'))
        return f(*args, **kwargs)
    return decorated_function

# Decorator for manager-only routes
```

#### `decorated_function` (línea 38)

**Código:**
```python
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or not current_user.is_admin():
            flash('No tienes permiso para acceder a esta página.', 'danger')
            return redirect(url_for('main.index'))
        return f(*args, **kwargs)
    return decorated_function

# Decorator for manager-only routes
```

#### `manager_required` (línea 46)

**Código:**
```python
def manager_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or (not current_user.is_admin() and not current_user.is_gerente()):
            flash('No tienes permiso para acceder a esta página.', 'danger')
            return redirect(url_for('main.index'))
        return f(*args, **kwargs)
    return decorated_function

# Authentication routes
@auth_bp.route('/login', methods=['GET', 'POST'])
```

#### `decorated_function` (línea 48)

**Código:**
```python
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or (not current_user.is_admin() and not current_user.is_gerente()):
            flash('No tienes permiso para acceder a esta página.', 'danger')
            return redirect(url_for('main.index'))
        return f(*args, **kwargs)
    return decorated_function

# Authentication routes
```

#### `login` (línea 57)

**Código:**
```python
def login():
    if current_user.is_authenticated:
        return redirect(url_for('main.dashboard'))
    
    form = LoginForm()
    if form.validate_on_submit():
        user = User.query.filter_by(username=form.username.data).first()
        if user is None or not user.check_password(form.password.data) or not user.is_active:
            flash('Usuario o contraseña invalidos.', 'danger')
            return redirect(url_for('auth.login'))
        
        login_user(user, remember=form.remember_me.data)
        log_activity(f'Login exitoso: {user.username}', user.id)
        
        next_page = request.args.get('next')
        if not next_page or urlparse(next_page).netloc != '':
            next_page = url_for('main.dashboard')
        
        flash(f'Bienvenido, {user.username}!', 'success')
        return redirect(next_page)
    
    return render_template('login.html', title='Iniciar Sesión', form=form)

@auth_bp.route('/logout')
@login_required
```

#### `logout` (línea 82)

**Código:**
```python
def logout():
    log_activity(f'Logout: {current_user.username}')
    logout_user()
    flash('Has cerrado sesión correctamente.', 'success')
    return redirect(url_for('auth.login'))

@auth_bp.route('/register', methods=['GET', 'POST'])
@admin_required
```

#### `register` (línea 90)

**Código:**
```python
def register():
    form = RegistrationForm()
    
    # Get list of companies for the checkbox field
    companies = Company.query.all()
    form.companies.choices = [(c.id, c.name) for c in companies]
    
    if form.validate_on_submit():
        # Validación adicional: solo el usuario "admin" puede crear usuarios con rol de administrador
        if form.role.data == UserRole.ADMIN.value and current_user.username != 'admin':
            flash('Solo el usuario "admin" puede crear usuarios con rol de administrador.', 'danger')
            return render_template('register.html', title='Registrar Usuario', form=form)
        
        user = User(
            username=form.username.data,
            email=form.email.data,
            first_name=form.first_name.data,
            last_name=form.last_name.data,
            role=UserRole(form.role.data)
        )
        user.set_password(form.password.data)
        
        # Añadir las empresas seleccionadas al usuario
        if form.companies.data:
            selected_companies = Company.query.filter(Company.id.in_(form.companies.data)).all()
            user.companies = selected_companies
            
        db.session.add(user)
        db.session.commit()
        
        log_activity(f'Usuario registrado: {user.username}')
        flash('¡Usuario registrado correctamente!', 'success')
        return redirect(url_for('user.list_users'))
    
    return render_template('register.html', title='Registrar Usuario', form=form)

# Main routes
@main_bp.route('/')
```

#### `index` (línea 128)

**Código:**
```python
def index():
    if current_user.is_authenticated:
        return redirect(url_for('main.dashboard'))
    return redirect(url_for('auth.login'))

@main_bp.route('/dashboard')
@login_required
```

#### `dashboard` (línea 135)

**Código:**
```python
def dashboard():
    stats = get_dashboard_stats()
    return render_template('dashboard.html', title='Dashboard', stats=stats, datetime=datetime)

@main_bp.route('/profile', methods=['GET', 'POST'])
@login_required
```

#### `profile` (línea 141)

**Código:**
```python
def profile():
    form = PasswordChangeForm()
    
    if form.validate_on_submit():
        if not current_user.check_password(form.current_password.data):
            flash('La contraseña actual es incorrecta.', 'danger')
            return redirect(url_for('main.profile'))
            
        current_user.set_password(form.new_password.data)
        db.session.commit()
        log_activity('Cambio de contraseña')
        flash('Contraseña actualizada correctamente.', 'success')
        return redirect(url_for('main.profile'))
    
    return render_template('profile.html', title='Mi Perfil', form=form)

@main_bp.route('/search')
@login_required
```

#### `search` (línea 159)

**Código:**
```python
def search():
    query = request.args.get('query', '')
    if not query:
        return redirect(url_for('main.dashboard'))
    
    # Search for companies (admin and gerentes only)
    companies = []
    if current_user.is_admin():
        companies = Company.query.filter(
            (Company.name.ilike(f'%{query}%')) |
            (Company.tax_id.ilike(f'%{query}%'))
        ).all()
    elif current_user.is_gerente():
        # Buscar solo entre las empresas del usuario
        company_ids = [company.id for company in current_user.companies]
        if company_ids:
            companies = Company.query.filter(
                Company.id.in_(company_ids)
            ).filter(
                (Company.name.ilike(f'%{query}%')) |
                (Company.tax_id.ilike(f'%{query}%'))
            ).all()
    
    # Search for employees based on user role
    employees = []
    if current_user.is_admin():
        employees = Employee.query.filter(
            (Employee.first_name.ilike(f'%{query}%')) |
            (Employee.last_name.ilike(f'%{query}%')) |
            (Employee.dni.ilike(f'%{query}%'))
        ).all()
    elif current_user.is_gerente():
        # Buscar empleados de todas las empresas asignadas al gerente
        company_ids = [company.id for company in current_user.companies]
        if company_ids:
            employees = Employee.query.filter(
                Employee.company_id.in_(company_ids)
            ).filter(
                (Employee.first_name.ilike(f'%{query}%')) |
                (Employee.last_name.ilike(f'%{query}%')) |
                (Employee.dni.ilike(f'%{query}%'))
            ).all()
    elif current_user.is_empleado() and current_user.employee:
        # Empleados can only see themselves in search results
        if (query.lower() in current_user.employee.first_name.lower() or
            query.lower() in current_user.employee.last_name.lower() or
            query.lower() in current_user.employee.dni.lower()):
            employees = [current_user.employee]
    
    return render_template('search_results.html', 
                          title='Resultados de Búsqueda',
                          query=query,
                          companies=companies,
                          employees=employees)

# Company routes
@company_bp.route('/')
@login_required
```

#### `list_companies` (línea 217)

**Código:**
```python
def list_companies():
    # Admin puede ver todas las empresas
    if current_user.is_admin():
        companies = Company.query.all()
    # Gerente solo puede ver sus empresas asignadas
    elif current_user.is_gerente():
        companies = current_user.companies
    # Empleado solo puede ver sus empresas asignadas
    elif current_user.is_empleado():
        companies = current_user.companies
    else:
        companies = []
    
    return render_template('company_list.html', title='Empresas', companies=companies)

@company_bp.route('/<string:slug>')
@login_required
```

#### `view_company` (línea 234)

**Código:**
```python
def view_company(slug):
    # Usar approach más robusto para buscar empresas por slug
    from utils import slugify
    
    # Buscar por ID si es un número
    if slug.isdigit():
        company = Company.query.get_or_404(int(slug))
    else:
        # Buscar todas las empresas y comparar slugs
        all_companies = Company.query.all()
        company = next((c for c in all_companies if slugify(c.name) == slug), None)
        
        if not company:
            flash('Empresa no encontrada', 'danger')
            return redirect(url_for('company.list_companies'))
    
    # Check if user has permission to view this company
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tienes permiso para ver esta empresa.', 'danger')
        return redirect(url_for('company.list_companies'))
    
    return render_template('company_detail.html', title=company.name, company=company)

@company_bp.route('/new', methods=['GET', 'POST'])
@admin_required
```

#### `create_company` (línea 259)

**Código:**
```python
def create_company():
    form = CompanyForm()
    
    if form.validate_on_submit():
        company = Company(
            name=form.name.data,
            address=form.address.data,
            city=form.city.data,
            postal_code=form.postal_code.data,
            country=form.country.data,
            sector=form.sector.data,
            tax_id=form.tax_id.data,
            phone=form.phone.data,
            email=form.email.data,
            website=form.website.data,
            is_active=form.is_active.data
        )
        db.session.add(company)
        db.session.commit()
        
        log_activity(f'Empresa creada: {company.name}')
        flash(f'Empresa "{company.name}" creada correctamente.', 'success')
        return redirect(url_for('company.list_companies'))
    
    return render_template('company_form.html', title='Nueva Empresa', form=form)

@company_bp.route('/<string:slug>/edit', methods=['GET', 'POST'])
@login_required
```

#### `edit_company` (línea 287)

**Código:**
```python
def edit_company(slug):
    # Usar approach más robusto para buscar empresas por slug
    from utils import slugify
    
    # Buscar por ID si es un número
    if slug.isdigit():
        company = Company.query.get_or_404(int(slug))
    else:
        # Buscar todas las empresas y comparar slugs
        all_companies = Company.query.all()
        company = next((c for c in all_companies if slugify(c.name) == slug), None)
        
        if not company:
            flash('Empresa no encontrada', 'danger')
            return redirect(url_for('company.list_companies'))
    
    # Check if user has permission to edit this company
    if not can_manage_company(company.id):
        flash('No tienes permiso para editar esta empresa.', 'danger')
        return redirect(url_for('company.list_companies'))
    
    # Pasamos el tax_id original para poder validar correctamente
    form = CompanyForm(original_tax_id=company.tax_id, obj=company)
    
    if form.validate_on_submit():
        company.name = form.name.data
        company.address = form.address.data
        company.city = form.city.data
        company.postal_code = form.postal_code.data
        company.country = form.country.data
        company.sector = form.sector.data
        company.tax_id = form.tax_id.data
        company.phone = form.phone.data
        company.email = form.email.data
        company.website = form.website.data
        company.is_active = form.is_active.data
        company.updated_at = datetime.utcnow()
        
        db.session.commit()
        
        log_activity(f'Empresa actualizada: {company.name}')
        flash(f'Empresa "{company.name}" actualizada correctamente.', 'success')
        return redirect(url_for('company.view_company', slug=company.get_slug()))
    
    return render_template('company_form.html', title=f'Editar {company.name}', form=form, company=company)

@company_bp.route('/<string:slug>/export', methods=['GET'])
@admin_required
```

#### `export_company_data` (línea 335)

**Código:**
```python
def export_company_data(slug):
    # Usar approach más robusto para buscar empresas por slug
    from utils import slugify
    
    # Buscar por ID si es un número
    if slug.isdigit():
        company = Company.query.get_or_404(int(slug))
    else:
        # Buscar todas las empresas y comparar slugs
        all_companies = Company.query.all()
        company = next((c for c in all_companies if slugify(c.name) == slug), None)
        
        if not company:
            flash('Empresa no encontrada', 'danger')
            return redirect(url_for('company.list_companies'))
    
    # Export company data as ZIP
    export_data = export_company_employees_zip(company.id)
    if not export_data:
        flash('Error al exportar los datos de la empresa.', 'danger')
        return redirect(url_for('company.view_company', slug=company.get_slug()))
    
    log_activity(f'Datos de empresa exportados: {company.name}')
    return send_file(
        export_data['buffer'],
        as_attachment=True,
        download_name=export_data['filename'],
        mimetype='application/zip'
    )

@company_bp.route('/<string:slug>/delete', methods=['POST'])
@admin_required
```

#### `delete_company` (línea 367)

**Código:**
```python
def delete_company(slug):
    # Usar approach más robusto para buscar empresas por slug
    from utils import slugify
    
    # Buscar por ID si es un número
    if slug.isdigit():
        company = Company.query.get_or_404(int(slug))
    else:
        # Buscar todas las empresas y comparar slugs
        all_companies = Company.query.all()
        company = next((c for c in all_companies if slugify(c.name) == slug), None)
        
        if not company:
            flash('Empresa no encontrada', 'danger')
            return redirect(url_for('company.list_companies'))
    
    # Proceed with deletion
    company_name = company.name
    
    # Delete all related entities
    try:
        # Step 1: Delete checkpoints and related records
        from models_checkpoints import CheckPoint, CheckPointRecord, CheckPointIncident, EmployeeContractHours
        
        # First, get employee IDs for this company (needed for multiple operations)
        employee_ids = [emp.id for emp in company.employees]
        
        # Delete checkpoint incidents and records first
        checkpoints = CheckPoint.query.filter_by(company_id=company.id).all()
        checkpoint_ids = [checkpoint.id for checkpoint in checkpoints]
        
        # Locate all checkpoint records for all employees of this company first
        # (this covers both records from company checkpoints and from other checkpoints)
        all_records = []
        all_record_ids = []
        
        if employee_ids:
            # Get employee checkpoint records
            employee_records = CheckPointRecord.query.filter(CheckPointRecord.employee_id.in_(employee_ids)).all()
            all_records.extend(employee_records)
            all_record_ids.extend([r.id for r in employee_records])
        
        if checkpoint_ids:
            # Also get checkpoint records by checkpoint_id
            checkpoint_records = CheckPointRecord.query.filter(CheckPointRecord.checkpoint_id.in_(checkpoint_ids)).all()
            
            # Add only records not already included
            new_records = [r for r in checkpoint_records if r.id not in all_record_ids]
            all_records.extend(new_records)
            all_record_ids.extend([r.id for r in new_records])
            
        # Delete all checkpoint incidents for all identified records
        if all_record_ids:
            CheckPointIncident.query.filter(CheckPointIncident.record_id.in_(all_record_ids)).delete(synchronize_session=False)
            
            # Delete all checkpoint records
            CheckPointRecord.query.filter(CheckPointRecord.id.in_(all_record_ids)).delete(synchronize_session=False)
        
        # Delete all checkpoints for this company
        if checkpoint_ids:
            CheckPoint.query.filter(CheckPoint.company_id == company.id).delete(synchronize_session=False)
        
        # Step 2: Delete all task completions for tasks related to this company's locations
        from models_tasks import TaskCompletion, Task, Location, LocalUser, TaskSchedule, TaskWeekday, TaskGroup
        
        # Get all locations for this company
        locations = Location.query.filter_by(company_id=company.id).all()
        location_ids = [loc.id for loc in locations]
        
        # Delete task completions for tasks in these locations
        if location_ids:
            # Get all tasks for these locations
            tasks = Task.query.filter(Task.location_id.in_(location_ids)).all()
            task_ids = [task.id for task in tasks]
            
            if task_ids:
                # Delete all task completions
                TaskCompletion.query.filter(TaskCompletion.task_id.in_(task_ids)).delete(synchronize_session=False)
            
            # Delete all task related records (schedules and weekdays)
            TaskSchedule.query.filter(TaskSchedule.task_id.in_(task_ids)).delete(synchronize_session=False)
            TaskWeekday.query.filter(TaskWeekday.task_id.in_(task_ids)).delete(synchronize_session=False)
            
            # Delete all tasks for these locations
            Task.query.filter(Task.location_id.in_(location_ids)).delete(synchronize_session=False)
            
            # Delete task groups
            TaskGroup.query.filter(TaskGroup.location_id.in_(location_ids)).delete(synchronize_session=False)
            
            # Primero necesitamos obtener todos los productos para poder eliminar sus etiquetas
            from models_tasks import Product, ProductLabel, ProductConservation, LabelTemplate
            
            # Get all products for these locations
            products = Product.query.filter(Product.location_id.in_(location_ids)).all()
            product_ids = [product.id for product in products]
            
            # Delete all product labels first
            if product_ids:
                ProductLabel.query.filter(ProductLabel.product_id.in_(product_ids)).delete(synchronize_session=False)
                
                # Delete all product conservation settings
                ProductConservation.query.filter(ProductConservation.product_id.in_(product_ids)).delete(synchronize_session=False)
            
            # Get all local users for these locations to delete their product labels
            local_users = LocalUser.query.filter(LocalUser.location_id.in_(location_ids)).all()
            local_user_ids = [user.id for user in local_users]
            
            # Delete any product labels created by these local users (even for products outside these locations)
            if local_user_ids:
                ProductLabel.query.filter(ProductLabel.local_user_id.in_(local_user_ids)).delete(synchronize_session=False)
            
            # Delete all label templates for these locations
            LabelTemplate.query.filter(LabelTemplate.location_id.in_(location_ids)).delete(synchronize_session=False)
            
            # Delete all products
            Product.query.filter(Product.location_id.in_(location_ids)).delete(synchronize_session=False)
            
            # Delete all local users for these locations
            LocalUser.query.filter(LocalUser.location_id.in_(location_ids)).delete(synchronize_session=False)
            
            # Delete all locations
            Location.query.filter(Location.company_id == company.id).delete(synchronize_session=False)
        
        # Step 2: Delete all employee related records (documents, notes, history, schedules, etc.)
        from models import EmployeeDocument, EmployeeNote, EmployeeHistory, EmployeeSchedule, EmployeeCheckIn, EmployeeVacation
        
        employee_ids = [emp.id for emp in company.employees]
        
        if employee_ids:
            # Delete employee contract hours (from checkpoints module)
            EmployeeContractHours.query.filter(EmployeeContractHours.employee_id.in_(employee_ids)).delete(synchronize_session=False)
            
            # Delete documents
            EmployeeDocument.query.filter(EmployeeDocument.employee_id.in_(employee_ids)).delete(synchronize_session=False)
            
            # Delete notes
            EmployeeNote.query.filter(EmployeeNote.employee_id.in_(employee_ids)).delete(synchronize_session=False)
            
            # Delete history
            EmployeeHistory.query.filter(EmployeeHistory.employee_id.in_(employee_ids)).delete(synchronize_session=False)
            
            # Delete schedules
            EmployeeSchedule.query.filter(EmployeeSchedule.employee_id.in_(employee_ids)).delete(synchronize_session=False)
            
            # Delete check-ins
            EmployeeCheckIn.query.filter(EmployeeCheckIn.employee_id.in_(employee_ids)).delete(synchronize_session=False)
            
            # Delete vacations
            EmployeeVacation.query.filter(EmployeeVacation.employee_id.in_(employee_ids)).delete(synchronize_session=False)
        
        # Delete all employees
        for employee in company.employees:
            db.session.delete(employee)
        
        # En lugar de eliminar los usuarios, solo eliminaremos la relación en la tabla asociativa
        # La tabla asociativa user_companies se eliminará automáticamente cuando se elimine la empresa
        # debido a que la relación tiene cascade="all, delete-orphan"
        
        # También necesitamos establecer el company_id en NULL para los usuarios referenciando esta empresa
        db.session.execute(
            db.text("UPDATE users SET company_id = NULL WHERE company_id = :company_id"),
            {"company_id": company.id}
        )
        
        # Finally delete the company
        db.session.delete(company)
        db.session.commit()
        
        log_activity(f'Empresa eliminada: {company_name}')
        flash(f'Empresa "{company_name}" y todos sus datos relacionados han sido eliminados correctamente.', 'success')
        
    except Exception as e:
        db.session.rollback()
        log_activity(f'Error al eliminar empresa {company_name}: {str(e)}')
        flash(f'Error al eliminar la empresa: {str(e)}', 'danger')
    
    return redirect(url_for('company.list_companies'))

# Employee routes
@employee_bp.route('/')
@login_required
```

#### `list_employees` (línea 548)

**Código:**
```python
def list_employees():
    # Obtener parámetro de página, por defecto 1
    page = request.args.get('page', 1, type=int)
    per_page = 20  # Número de empleados por página
    
    # Preparar la consulta según el rol del usuario
    if current_user.is_admin():
        # Para administradores, usar paginación para evitar cargar todos los empleados a la vez
        query = Employee.query.order_by(Employee.last_name, Employee.first_name)
    
    # Gerente puede ver solo empleados de sus empresas
    elif current_user.is_gerente():
        # Obtener IDs de todas las empresas asignadas al gerente
        company_ids = [company.id for company in current_user.companies]
        if company_ids:
            query = Employee.query.filter(
                Employee.company_id.in_(company_ids)
            ).order_by(Employee.last_name, Employee.first_name)
        else:
            employees = []
            employees_by_company = {}
            return render_template('employee_list.html', title='Empleados', 
                                  employees=employees, employees_by_company=employees_by_company, pagination=None)
    
    # Empleado solo puede verse a sí mismo
    elif current_user.is_empleado() and current_user.employee:
        # Para un solo empleado no necesitamos paginación
        employees = [current_user.employee]
        # Agrupar empleado por empresa para visualización consistente
        company_name = current_user.employee.company.name if current_user.employee.company else "Sin Empresa Asignada"
        employees_by_company = {company_name: [current_user.employee]}
        return render_template('employee_list.html', title='Empleados', 
                              employees=employees, employees_by_company=employees_by_company, pagination=None)
    else:
        employees = []
        employees_by_company = {}
        return render_template('employee_list.html', title='Empleados', 
                              employees=employees, employees_by_company=employees_by_company, pagination=None)
    
    # Ejecutar consulta paginada
    pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    employees = pagination.items
    
    # Agrupar empleados por empresa
    employees_by_company = {}
    for employee in employees:
        company_name = employee.company.name if employee.company else "Sin Empresa Asignada"
        if company_name not in employees_by_company:
            employees_by_company[company_name] = []
        employees_by_company[company_name].append(employee)
    
    # Ordenar empresas alfabéticamente
    sorted_employees_by_company = {}
    for company_name in sorted(employees_by_company.keys()):
        sorted_employees_by_company[company_name] = employees_by_company[company_name]
    
    return render_template('employee_list.html', title='Empleados', 
                          employees=employees, 
                          employees_by_company=sorted_employees_by_company,
                          pagination=pagination)

@employee_bp.route('/<int:id>')
@login_required
```

#### `view_employee` (línea 611)

**Código:**
```python
def view_employee(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to view this employee
    if not can_view_employee(employee):
        flash('No tienes permiso para ver este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    return render_template('employee_detail.html', title=f'{employee.first_name} {employee.last_name}', employee=employee)

@employee_bp.route('/new', methods=['GET', 'POST'])
@manager_required
```

#### `create_employee` (línea 623)

**Código:**
```python
def create_employee():
    form = EmployeeForm()
    
    # Get list of companies for the dropdown
    if current_user.is_admin():
        companies = Company.query.all()
        form.company_id.choices = [(c.id, c.name) for c in companies]
    else:
        # Para gerentes, mostrar todas las empresas a las que tienen acceso
        companies = current_user.companies
        if companies:
            form.company_id.choices = [(c.id, c.name) for c in companies]
            # Si no hay datos seleccionados, use la primera empresa por defecto
            if not form.company_id.data and companies:
                form.company_id.data = companies[0].id
    
    if form.validate_on_submit():
        employee = Employee(
            first_name=form.first_name.data,
            last_name=form.last_name.data,
            dni=form.dni.data,
            position=form.position.data,
            contract_type=ContractType(form.contract_type.data) if form.contract_type.data else None,
            bank_account=form.bank_account.data,
            start_date=form.start_date.data,
            end_date=form.end_date.data,
            company_id=form.company_id.data,
            is_active=form.is_active.data,
            status=EmployeeStatus(form.status.data) if form.status.data else EmployeeStatus.ACTIVO,
            status_start_date=form.start_date.data  # Por defecto, la fecha de inicio de estado es la misma de contratación
        )
        db.session.add(employee)
        db.session.commit()
        
        # Asignar automáticamente el empleado a todos los puntos de fichaje de su empresa
        try:
            # Obtener todos los puntos de fichaje de la empresa
            checkpoints = CheckPoint.query.filter_by(company_id=employee.company_id).all()
            if checkpoints:
                log_activity(f'Asignando empleado a {len(checkpoints)} puntos de fichaje')
                # No se necesita hacer ninguna asignación explícita, ya que los puntos de fichaje
                # acceden a los empleados a través de su relación con la empresa
                
                # Podríamos usar este espacio para configuración adicional si fuera necesario en el futuro
                # Por ejemplo, inicializar ContractHours para cada empleado
                pass
        except Exception as e:
            # No detenemos la creación del empleado si hay algún error en esta parte
            print(f"Error al asignar empleado a puntos de fichaje: {str(e)}")
            log_activity(f'Error al asignar empleado a puntos de fichaje: {str(e)}')
        
        log_activity(f'Empleado creado: {employee.first_name} {employee.last_name}')
        flash(f'Empleado "{employee.first_name} {employee.last_name}" creado correctamente.', 'success')
        return redirect(url_for('employee.view_employee', id=employee.id))
    
    return render_template('employee_form.html', title='Nuevo Empleado', form=form)

@employee_bp.route('/<int:id>/edit', methods=['GET', 'POST'])
@login_required
```

#### `edit_employee` (línea 682)

**Código:**
```python
def edit_employee(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to edit this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para editar este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeForm(obj=employee)
    
    # Get list of companies for the dropdown
    if current_user.is_admin():
        companies = Company.query.all()
        form.company_id.choices = [(c.id, c.name) for c in companies]
    else:
        # Para gerentes, mostrar todas las empresas a las que tienen acceso
        companies = current_user.companies
        if companies:
            form.company_id.choices = [(c.id, c.name) for c in companies]
    
    if form.validate_on_submit():
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
            
        if employee.end_date != form.end_date.data:
            log_employee_change(employee, 'end_date', 
                              employee.end_date.isoformat() if employee.end_date else None, 
                              form.end_date.data.isoformat() if form.end_date.data else None)
            employee.end_date = form.end_date.data
            
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
        db.session.commit()
        
        log_activity(f'Empleado actualizado: {employee.first_name} {employee.last_name}')
        flash(f'Empleado "{employee.first_name} {employee.last_name}" actualizado correctamente.', 'success')
        return redirect(url_for('employee.view_employee', id=employee.id))
    
    return render_template('employee_form.html', title=f'Editar {employee.first_name} {employee.last_name}', form=form, employee=employee)

@employee_bp.route('/<int:id>/delete', methods=['POST'])
@manager_required
```

#### `delete_employee` (línea 769)

**Código:**
```python
def delete_employee(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to delete this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Verificar si el empleado tiene registros de fichaje
    from models_checkpoints import CheckPointRecord
    checkpoint_records = CheckPointRecord.query.filter_by(employee_id=employee.id).count()
    
    if checkpoint_records > 0:
        employee_name = f"{employee.first_name} {employee.last_name}"
        message = f'No se puede eliminar al empleado "{employee_name}" porque tiene {checkpoint_records} registros de fichaje asociados. '
        message += 'Para eliminar este empleado, primero debes eliminar todos sus registros de fichaje o desactivarlo en lugar de eliminarlo.'
        log_activity(f'Intento fallido de eliminar empleado con registros: {employee_name}')
        flash(message, 'warning')
        return redirect(url_for('employee.view_employee', id=employee.id))
    
    try:
        employee_name = f"{employee.first_name} {employee.last_name}"
        db.session.delete(employee)
        db.session.commit()
        
        log_activity(f'Empleado eliminado: {employee_name}')
        flash(f'Empleado "{employee_name}" eliminado correctamente.', 'success')
        return redirect(url_for('employee.list_employees'))
    except Exception as e:
        db.session.rollback()
        log_activity(f'Error al eliminar empleado {employee.id}: {str(e)}')
        flash(f'Error al eliminar empleado: {str(e)}', 'danger')
        return redirect(url_for('employee.list_employees'))
    
@employee_bp.route('/<int:id>/status', methods=['GET', 'POST'])
@login_required
```

#### `manage_status` (línea 805)

**Código:**
```python
def manage_status(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to manage this employee's status
    if not can_manage_employee(employee):
        flash('No tienes permiso para gestionar el estado de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeStatusForm(obj=employee)
    
    if form.validate_on_submit():
        old_status = employee.status.value if employee.status else 'activo'
        new_status = form.status.data
        
        if old_status != new_status:
            log_employee_change(employee, 'status', old_status, new_status)
            employee.status = EmployeeStatus(new_status)
        
        # Log changes to dates and notes if they've changed
        if employee.status_start_date != form.status_start_date.data:
            log_employee_change(employee, 'status_start_date', 
                              employee.status_start_date.isoformat() if employee.status_start_date else None, 
                              form.status_start_date.data.isoformat() if form.status_start_date.data else None)
        
        if employee.status_end_date != form.status_end_date.data:
            log_employee_change(employee, 'status_end_date', 
                              employee.status_end_date.isoformat() if employee.status_end_date else None, 
                              form.status_end_date.data.isoformat() if form.status_end_date.data else None)
        
        if employee.status_notes != form.status_notes.data:
            log_employee_change(employee, 'status_notes', employee.status_notes, form.status_notes.data)
        
        # Update the employee record with the new status information
        employee.status = EmployeeStatus(form.status.data)
        employee.status_start_date = form.status_start_date.data
        employee.status_end_date = form.status_end_date.data
        employee.status_notes = form.status_notes.data
        employee.updated_at = datetime.utcnow()
        
        db.session.commit()
        
        log_activity(f'Estado de empleado actualizado: {employee.first_name} {employee.last_name}')
        flash(f'Estado del empleado "{employee.first_name} {employee.last_name}" actualizado correctamente.', 'success')
        return redirect(url_for('employee.view_employee', id=employee.id))
    
    return render_template('employee_status.html', title=f'Gestionar Estado - {employee.first_name} {employee.last_name}', 
                          form=form, employee=employee)

@employee_bp.route('/<int:id>/documents')
@login_required
```

#### `list_documents` (línea 855)

**Código:**
```python
def list_documents(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to view this employee's documents
    if not can_view_employee(employee):
        flash('No tienes permiso para ver los documentos de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    documents = EmployeeDocument.query.filter_by(employee_id=employee.id).all()
    return render_template('employee_documents.html', 
                          title=f'Documentos de {employee.first_name} {employee.last_name}', 
                          employee=employee, 
                          documents=documents)

@employee_bp.route('/<int:id>/documents/upload', methods=['GET', 'POST'])
@login_required
```

#### `upload_document` (línea 871)

**Código:**
```python
def upload_document(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to upload documents for this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para subir documentos para este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeDocumentForm()
    
    if form.validate_on_submit():
        file_data = save_file(form.file.data, form.description.data)
        if file_data:
            document = EmployeeDocument(
                employee_id=employee.id,
                filename=file_data['filename'],
                original_filename=file_data['original_filename'],
                file_path=file_data['file_path'],
                file_type=file_data['file_type'],
                file_size=file_data['file_size'],
                description=file_data['description']
            )
            db.session.add(document)
            db.session.commit()
            
            log_activity(f'Documento subido para {employee.first_name} {employee.last_name}: {file_data["original_filename"]}')
            flash(f'Documento "{file_data["original_filename"]}" subido correctamente.', 'success')
            return redirect(url_for('employee.list_documents', id=employee.id))
        else:
            flash('Error al subir el documento. Formato no permitido.', 'danger')
    
    return render_template('employee_document_upload.html', 
                          title=f'Subir Documento para {employee.first_name} {employee.last_name}', 
                          employee=employee, 
                          form=form)

@employee_bp.route('/documents/<int:doc_id>')
@login_required
```

#### `download_document` (línea 909)

**Código:**
```python
def download_document(doc_id):
    document = EmployeeDocument.query.get_or_404(doc_id)
    employee = Employee.query.get_or_404(document.employee_id)
    
    # Check if user has permission to download this document
    if not can_view_employee(employee):
        flash('No tienes permiso para descargar este documento.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    log_activity(f'Documento descargado: {document.original_filename}')
    return send_from_directory(
        os.path.dirname(document.file_path),
        os.path.basename(document.file_path),
        as_attachment=True,
        download_name=document.original_filename
    )

@employee_bp.route('/documents/<int:doc_id>/delete', methods=['POST'])
@login_required
```

#### `delete_document` (línea 928)

**Código:**
```python
def delete_document(doc_id):
    document = EmployeeDocument.query.get_or_404(doc_id)
    employee = Employee.query.get_or_404(document.employee_id)
    
    # Check if user has permission to delete this document
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar este documento.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Delete file from filesystem
    if os.path.exists(document.file_path):
        os.remove(document.file_path)
    
    document_name = document.original_filename
    db.session.delete(document)
    db.session.commit()
    
    log_activity(f'Documento eliminado: {document_name}')
    flash(f'Documento "{document_name}" eliminado correctamente.', 'success')
    return redirect(url_for('employee.list_documents', id=employee.id))

@employee_bp.route('/<int:id>/notes', methods=['GET', 'POST'])
@login_required
```

#### `manage_notes` (línea 951)

**Código:**
```python
def manage_notes(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to manage notes for this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para gestionar notas de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeNoteForm()
    
    if form.validate_on_submit():
        note = EmployeeNote(
            employee_id=employee.id,
            content=form.content.data,
            created_by_id=current_user.id
        )
        db.session.add(note)
        db.session.commit()
        
        log_activity(f'Nota añadida para {employee.first_name} {employee.last_name}')
        flash('Nota añadida correctamente.', 'success')
        return redirect(url_for('employee.manage_notes', id=employee.id))
    
    notes = EmployeeNote.query.filter_by(employee_id=employee.id).order_by(EmployeeNote.created_at.desc()).all()
    return render_template('employee_notes.html', 
                          title=f'Notas de {employee.first_name} {employee.last_name}', 
                          employee=employee, 
                          notes=notes, 
                          form=form)

@employee_bp.route('/backup/database', methods=['GET', 'POST'])
@login_required
@admin_required
```

#### `backup_database` (línea 984)

**Docstring:**
```
Create a database backup
```

**Código:**
```python
def backup_database():
    """Create a database backup"""
    if request.method == 'POST':
        result = create_database_backup()
        if result['success']:
            flash('Copia de seguridad creada con éxito', 'success')
            return send_file(
                result['file'],
                as_attachment=True,
                download_name=f"backup_{result['timestamp']}.sql"
            )
        else:
            flash(f'Error al crear la copia de seguridad: {result["error"]}', 'danger')
            
    return render_template('backup_form.html', title='Copia de Seguridad de Base de Datos')

@employee_bp.route('/notes/<int:note_id>/delete', methods=['POST'])
@login_required
```

#### `delete_note` (línea 1002)

**Código:**
```python
def delete_note(note_id):
    note = EmployeeNote.query.get_or_404(note_id)
    employee = Employee.query.get_or_404(note.employee_id)
    
    # Check if user has permission to delete this note
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar esta nota.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    db.session.delete(note)
    db.session.commit()
    
    log_activity(f'Nota eliminada para {employee.first_name} {employee.last_name}')
    flash('Nota eliminada correctamente.', 'success')
    return redirect(url_for('employee.manage_notes', id=employee.id))

@employee_bp.route('/<int:id>/history')
@login_required
```

#### `view_history` (línea 1020)

**Código:**
```python
def view_history(id):
    employee = Employee.query.get_or_404(id)
    
    # Check if user has permission to view this employee's history
    if not can_view_employee(employee):
        flash('No tienes permiso para ver el historial de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    history = employee.history
    return render_template('employee_history.html', 
                          title=f'Historial de {employee.first_name} {employee.last_name}', 
                          employee=employee, 
                          history=history)

# User routes
@user_bp.route('/')
@admin_required
```

#### `list_users` (línea 1037)

**Código:**
```python
def list_users():
    users = User.query.all()
    return render_template('user_management.html', title='Gestión de Usuarios', users=users)

@user_bp.route('/<int:id>/edit', methods=['GET', 'POST'])
@admin_required
```

#### `edit_user` (línea 1043)

**Código:**
```python
def edit_user(id):
    user = User.query.get_or_404(id)
    form = UserUpdateForm(user.username, user.email, obj=user)
    
    # Get list of companies for the checkbox field
    companies = Company.query.all()
    form.companies.choices = [(c.id, c.name) for c in companies]
    
    # Si es una solicitud GET, establecer los valores iniciales de las empresas
    if request.method == 'GET':
        form.companies.data = [company.id for company in user.companies]
    
    if form.validate_on_submit():
        # Validación adicional: solo el usuario "admin" puede asignar el rol de administrador
        # o cambiar el rol del usuario "admin"
        if (form.role.data == UserRole.ADMIN.value and current_user.username != 'admin') or \
           (user.username == 'admin' and current_user.username != 'admin'):
            flash('Solo el usuario "admin" puede asignar el rol de administrador o modificar al usuario "admin".', 'danger')
            return render_template('user_form.html', title=f'Editar Usuario {user.username}', form=form, user=user)
        
        # Protección adicional: Si el usuario a editar es "admin", no permitir cambios en su rol
        if user.username == 'admin' and form.role.data != UserRole.ADMIN.value:
            flash('No se puede cambiar el rol del usuario "admin".', 'danger')
            return render_template('user_form.html', title=f'Editar Usuario {user.username}', form=form, user=user)
        
        user.username = form.username.data
        user.email = form.email.data
        user.first_name = form.first_name.data
        user.last_name = form.last_name.data
        user.role = UserRole(form.role.data)
        user.is_active = form.is_active.data
        
        # Actualizar las empresas del usuario
        if form.companies.data:
            selected_companies = Company.query.filter(Company.id.in_(form.companies.data)).all()
            user.companies = selected_companies
        else:
            user.companies = []
        
        db.session.commit()
        
        log_activity(f'Usuario actualizado: {user.username}')
        flash(f'Usuario "{user.username}" actualizado correctamente.', 'success')
        return redirect(url_for('user.list_users'))
    
    return render_template('user_form.html', title=f'Editar Usuario {user.username}', form=form, user=user)

@user_bp.route('/<int:id>/reset-password', methods=['POST'])
@admin_required
```

#### `reset_password` (línea 1092)

**Código:**
```python
def reset_password(id):
    user = User.query.get_or_404(id)
    
    # No permitir restablecer la contraseña del usuario "admin" a menos que el usuario actual sea "admin"
    if user.username == 'admin' and current_user.username != 'admin':
        flash('No tienes permiso para restablecer la contraseña del usuario "admin".', 'danger')
        return redirect(url_for('user.list_users'))
    
    # Generate a temporary password
    temp_password = f"temp{id}pwd{int(datetime.utcnow().timestamp())}"[:12]
    user.set_password(temp_password)
    db.session.commit()
    
    log_activity(f'Contraseña restablecida para: {user.username}')
    flash(f'Contraseña de "{user.username}" restablecida a: {temp_password}', 'success')
    return redirect(url_for('user.list_users'))

@user_bp.route('/<int:id>/activate', methods=['POST'])
@admin_required
```

#### `toggle_activation` (línea 1111)

**Código:**
```python
def toggle_activation(id):
    user = User.query.get_or_404(id)
    
    # Don't allow deactivating own account
    if user.id == current_user.id:
        flash('No puedes desactivar tu propia cuenta.', 'danger')
        return redirect(url_for('user.list_users'))
    
    # No permitir desactivar al usuario "admin"
    if user.username == 'admin':
        flash('No se puede desactivar al usuario "admin".', 'danger')
        return redirect(url_for('user.list_users'))
    
    user.is_active = not user.is_active
    db.session.commit()
    
    status = 'activado' if user.is_active else 'desactivado'
    log_activity(f'Usuario {status}: {user.username}')
    flash(f'Usuario "{user.username}" {status} correctamente.', 'success')
    return redirect(url_for('user.list_users'))

@user_bp.route('/<int:id>/delete', methods=['POST'])
@admin_required
```

#### `delete_user` (línea 1134)

**Código:**
```python
def delete_user(id):
    user = User.query.get_or_404(id)
    
    # Don't allow deleting own account
    if user.id == current_user.id:
        flash('No puedes eliminar tu propia cuenta.', 'danger')
        return redirect(url_for('user.list_users'))
    
    # No permitir eliminar al usuario "admin"
    if user.username == 'admin':
        flash('No se puede eliminar al usuario "admin".', 'danger')
        return redirect(url_for('user.list_users'))
    
    username = user.username
    db.session.delete(user)
    db.session.commit()
    
    log_activity(f'Usuario eliminado: {username}')
    flash(f'Usuario "{username}" eliminado correctamente.', 'success')
    return redirect(url_for('user.list_users'))

# Schedules routes
@schedule_bp.route('/employee/<int:employee_id>')
@login_required
```

#### `list_schedules` (línea 1158)

**Código:**
```python
def list_schedules(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to view this employee
    if not can_view_employee(employee):
        flash('No tienes permiso para ver los horarios de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    schedules = EmployeeSchedule.query.filter_by(employee_id=employee_id).all()
    
    return render_template('schedule_list.html', 
                          title=f'Horarios de {employee.first_name} {employee.last_name}', 
                          employee=employee,
                          schedules=schedules)

@schedule_bp.route('/employee/<int:employee_id>/new', methods=['GET', 'POST'])
@manager_required
```

#### `create_schedule` (línea 1175)

**Código:**
```python
def create_schedule(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para gestionar los horarios de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeScheduleForm()
    
    if form.validate_on_submit():
        schedule = EmployeeSchedule(
            day_of_week=WeekDay(form.day_of_week.data),
            start_time=form.start_time.data,
            end_time=form.end_time.data,
            is_working_day=form.is_working_day.data,
            employee_id=employee_id
        )
        db.session.add(schedule)
        db.session.commit()
        
        log_activity(f'Horario creado para {employee.first_name} {employee.last_name}')
        flash('Horario creado correctamente.', 'success')
        return redirect(url_for('schedule.list_schedules', employee_id=employee_id))
    
    return render_template('schedule_form.html', 
                          title=f'Nuevo Horario para {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee)

@schedule_bp.route('/employee/<int:employee_id>/weekly', methods=['GET', 'POST'])
@manager_required
```

#### `weekly_schedule` (línea 1207)

**Código:**
```python
def weekly_schedule(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para gestionar los horarios de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeWeeklyScheduleForm()
    
    # Si es una petición GET, cargar los horarios existentes
    if request.method == 'GET':
        # Obtener los horarios existentes para cada día
        schedules = EmployeeSchedule.query.filter_by(employee_id=employee_id).all()
        day_schedules = {schedule.day_of_week.value: schedule for schedule in schedules}
        
        # Cargar los datos en el formulario
        for day in ["lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"]:
            if day in day_schedules:
                schedule = day_schedules[day]
                getattr(form, f"{day}_is_working_day").data = schedule.is_working_day
                getattr(form, f"{day}_start_time").data = schedule.start_time
                getattr(form, f"{day}_end_time").data = schedule.end_time
    
    if form.validate_on_submit():
        # Para cada día de la semana, crear o actualizar el horario
        for day_name in ["lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"]:
            # Obtener los datos del formulario para este día
            is_working_day = getattr(form, f"{day_name}_is_working_day").data
            start_time = getattr(form, f"{day_name}_start_time").data
            end_time = getattr(form, f"{day_name}_end_time").data
            
            # Valores por defecto seguros para evitar errores NULL
            if not start_time:
                start_time = time(9, 0)  # 9:00 AM por defecto
            
            if not end_time:
                end_time = time(18, 0)  # 6:00 PM por defecto
            
            # Convertir el nombre del día a un WeekDay
            day_enum = WeekDay(day_name)
            
            # Buscar un horario existente para este día
            schedule = EmployeeSchedule.query.filter_by(
                employee_id=employee_id, 
                day_of_week=day_enum
            ).first()
            
            # Si no existe horario para este día, crearlo independientemente de si es laborable o no
            if not schedule:
                schedule = EmployeeSchedule(
                    day_of_week=day_enum,
                    start_time=start_time,
                    end_time=end_time,
                    is_working_day=is_working_day,
                    employee_id=employee_id
                )
                db.session.add(schedule)
            # Si existe, actualizarlo en todos los casos
            else:
                schedule.is_working_day = is_working_day
                schedule.start_time = start_time
                schedule.end_time = end_time
        
        db.session.commit()
        log_activity(f'Horarios semanales actualizados para {employee.first_name} {employee.last_name}')
        flash('Horarios semanales actualizados correctamente.', 'success')
        return redirect(url_for('schedule.list_schedules', employee_id=employee_id))
    
    return render_template('weekly_schedule_form.html', 
                          title=f'Horario Semanal para {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee)

@schedule_bp.route('/<int:id>/edit', methods=['GET', 'POST'])
@manager_required
```

#### `edit_schedule` (línea 1283)

**Código:**
```python
def edit_schedule(id):
    schedule = EmployeeSchedule.query.get_or_404(id)
    employee = Employee.query.get_or_404(schedule.employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para editar los horarios de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeScheduleForm(obj=schedule)
    
    if form.validate_on_submit():
        schedule.day_of_week = WeekDay(form.day_of_week.data)
        schedule.start_time = form.start_time.data
        schedule.end_time = form.end_time.data
        schedule.is_working_day = form.is_working_day.data
        schedule.updated_at = datetime.utcnow()
        
        db.session.commit()
        
        log_activity(f'Horario actualizado para {employee.first_name} {employee.last_name}')
        flash('Horario actualizado correctamente.', 'success')
        return redirect(url_for('schedule.list_schedules', employee_id=schedule.employee_id))
    
    return render_template('schedule_form.html', 
                          title=f'Editar Horario de {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee,
                          schedule=schedule)

@schedule_bp.route('/<int:id>/delete', methods=['POST'])
@manager_required
```

#### `delete_schedule` (línea 1315)

**Código:**
```python
def delete_schedule(id):
    schedule = EmployeeSchedule.query.get_or_404(id)
    employee = Employee.query.get_or_404(schedule.employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar los horarios de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    employee_id = schedule.employee_id
    db.session.delete(schedule)
    db.session.commit()
    
    log_activity(f'Horario eliminado para {employee.first_name} {employee.last_name}')
    flash('Horario eliminado correctamente.', 'success')
    return redirect(url_for('schedule.list_schedules', employee_id=employee_id))

# Check-ins routes
@checkin_bp.route('/employee/<int:employee_id>')
@login_required
```

#### `list_checkins` (línea 1335)

**Código:**
```python
def list_checkins(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to view this employee
    if not can_view_employee(employee):
        flash('No tienes permiso para ver los fichajes de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Form for exporting check-ins to PDF
    export_form = ExportCheckInsForm()
    
    # Obtener todos los fichajes ordenados por fecha
    all_checkins = EmployeeCheckIn.query.filter_by(employee_id=employee_id).order_by(EmployeeCheckIn.check_in_time.desc()).all()
    
    # Agrupar fichajes por mes
    from datetime import datetime
    
    # Función para obtener el mes y año
    def get_month_year(check_in_time):
        return (check_in_time.year, check_in_time.month)
    
    # Crear un diccionario para agrupar por meses
    checkins_by_month = {}
    
    for checkin in all_checkins:
        year_month = get_month_year(checkin.check_in_time)
        year = year_month[0]
        month = year_month[1]
        
        # Obtener el nombre del mes en español
        month_names = [
            'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 
            'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
        ]
        month_name = month_names[month - 1]
        month_label = f"{month_name} {year}"
        
        if month_label not in checkins_by_month:
            checkins_by_month[month_label] = []
        
        checkins_by_month[month_label].append(checkin)
    
    return render_template('checkin_list.html', 
                          title=f'Fichajes de {employee.first_name} {employee.last_name}', 
                          employee=employee,
                          checkins=all_checkins,
                          checkins_by_month=checkins_by_month,
                          export_form=export_form)

@checkin_bp.route('/employee/<int:employee_id>/new', methods=['GET', 'POST'])
@manager_required
```

#### `get_month_year` (línea 1353)

**Código:**
```python
    def get_month_year(check_in_time):
        return (check_in_time.year, check_in_time.month)
    
    # Crear un diccionario para agrupar por meses
    checkins_by_month = {}
    
    for checkin in all_checkins:
        year_month = get_month_year(checkin.check_in_time)
        year = year_month[0]
        month = year_month[1]
        
        # Obtener el nombre del mes en español
        month_names = [
            'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 
            'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
        ]
        month_name = month_names[month - 1]
        month_label = f"{month_name} {year}"
        
        if month_label not in checkins_by_month:
            checkins_by_month[month_label] = []
        
        checkins_by_month[month_label].append(checkin)
    
    return render_template('checkin_list.html', 
                          title=f'Fichajes de {employee.first_name} {employee.last_name}', 
                          employee=employee,
                          checkins=all_checkins,
                          checkins_by_month=checkins_by_month,
                          export_form=export_form)

```

#### `create_checkin` (línea 1386)

**Código:**
```python
def create_checkin(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para gestionar los fichajes de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeCheckInForm()
    
    if form.validate_on_submit():
        # Combinar fecha y hora para la entrada
        check_in_time = datetime.combine(form.check_in_date.data, form.check_in_time.data)
        
        # Combinar fecha y hora para la salida (si existe)
        check_out_time = None
        if form.check_out_time.data:
            check_out_time = datetime.combine(form.check_in_date.data, form.check_out_time.data)
        
        checkin = EmployeeCheckIn(
            check_in_time=check_in_time,
            check_out_time=check_out_time,
            notes=form.notes.data,
            employee_id=employee_id
        )
        db.session.add(checkin)
        db.session.commit()
        
        log_activity(f'Fichaje creado para {employee.first_name} {employee.last_name}')
        flash('Fichaje creado correctamente.', 'success')
        return redirect(url_for('checkin.list_checkins', employee_id=employee_id))
    
    return render_template('checkin_form.html', 
                          title=f'Nuevo Fichaje para {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee)

@checkin_bp.route('/<int:id>/edit', methods=['GET', 'POST'])
@manager_required
```

#### `edit_checkin` (línea 1425)

**Código:**
```python
def edit_checkin(id):
    checkin = EmployeeCheckIn.query.get_or_404(id)
    employee = Employee.query.get_or_404(checkin.employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para editar los fichajes de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeCheckInForm()
    
    if request.method == 'GET':
        form.check_in_date.data = checkin.check_in_time.date()
        form.check_in_time.data = checkin.check_in_time.time()
        form.check_out_time.data = checkin.check_out_time.time() if checkin.check_out_time else None
        form.notes.data = checkin.notes
    
    if form.validate_on_submit():
        # Combinar fecha y hora para la entrada
        check_in_time = datetime.combine(form.check_in_date.data, form.check_in_time.data)
        
        # Combinar fecha y hora para la salida (si existe)
        check_out_time = None
        if form.check_out_time.data:
            check_out_time = datetime.combine(form.check_in_date.data, form.check_out_time.data)
        
        checkin.check_in_time = check_in_time
        checkin.check_out_time = check_out_time
        checkin.notes = form.notes.data
        checkin.updated_at = datetime.utcnow()
        
        db.session.commit()
        
        log_activity(f'Fichaje actualizado para {employee.first_name} {employee.last_name}')
        flash('Fichaje actualizado correctamente.', 'success')
        return redirect(url_for('checkin.list_checkins', employee_id=checkin.employee_id))
    
    return render_template('checkin_form.html', 
                          title=f'Editar Fichaje de {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee,
                          checkin=checkin)

@checkin_bp.route('/<int:id>/delete', methods=['POST'])
@manager_required
```

#### `delete_checkin` (línea 1470)

**Código:**
```python
def delete_checkin(id):
    checkin = EmployeeCheckIn.query.get_or_404(id)
    employee = Employee.query.get_or_404(checkin.employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar los fichajes de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    employee_id = checkin.employee_id
    db.session.delete(checkin)
    db.session.commit()
    
    log_activity(f'Fichaje eliminado para {employee.first_name} {employee.last_name}')
    flash('Fichaje eliminado correctamente.', 'success')
    return redirect(url_for('checkin.list_checkins', employee_id=employee_id))

@checkin_bp.route('/employee/<int:employee_id>/export', methods=['POST'])
@login_required
```

#### `export_checkins` (línea 1489)

**Código:**
```python
def export_checkins(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to view this employee
    if not can_view_employee(employee):
        flash('No tienes permiso para exportar los fichajes de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = ExportCheckInsForm()
    
    if form.validate_on_submit():
        start_date = form.start_date.data
        end_date = form.end_date.data
        
        # Generate PDF
        pdf_file = generate_checkins_pdf(employee, start_date, end_date)
        
        if not pdf_file:
            flash('No se han podido generar el PDF de fichajes.', 'warning')
            return redirect(url_for('checkin.list_checkins', employee_id=employee_id))
        
        # Prepare filename for download
        filename = f"fichajes_{employee.first_name.lower()}_{employee.last_name.lower()}_{start_date.strftime('%Y%m%d')}_{end_date.strftime('%Y%m%d')}.pdf"
        
        log_activity(f'Exportados fichajes a PDF para {employee.first_name} {employee.last_name}')
        
        # Send the file to the user
        return send_file(pdf_file, as_attachment=True, download_name=filename, mimetype='application/pdf')
    
    flash('Error al validar el formulario.', 'danger')
    return redirect(url_for('checkin.list_checkins', employee_id=employee_id))

@checkin_bp.route('/employee/<int:employee_id>/delete-by-date', methods=['GET', 'POST'])
@manager_required
```

#### `delete_checkins_by_date` (línea 1523)

**Código:**
```python
def delete_checkins_by_date(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar los fichajes de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = ExportCheckInsForm()  # Reutilizamos el formulario de exportación para las fechas
    form.submit.label.text = 'Eliminar Fichajes'  # Cambiamos el texto del botón
    
    if form.validate_on_submit():
        start_date = form.start_date.data
        end_date = form.end_date.data
        
        if not start_date or not end_date:
            flash('Debe seleccionar fechas de inicio y fin.', 'warning')
            return render_template('delete_checkins_form.html', 
                                title=f'Eliminar Fichajes de {employee.first_name} {employee.last_name}', 
                                form=form,
                                employee=employee)
        
        # Crear objetos datetime para la comparación
        start_datetime = datetime.combine(start_date, datetime.min.time())
        end_datetime = datetime.combine(end_date, datetime.max.time())
        
        # Buscar fichajes en el rango de fechas
        checkins_to_delete = EmployeeCheckIn.query.filter(
            EmployeeCheckIn.employee_id == employee_id,
            EmployeeCheckIn.check_in_time >= start_datetime,
            EmployeeCheckIn.check_in_time <= end_datetime
        ).all()
        
        if not checkins_to_delete:
            flash('No se encontraron fichajes en el periodo seleccionado.', 'warning')
        else:
            count = len(checkins_to_delete)
            for checkin in checkins_to_delete:
                db.session.delete(checkin)
            db.session.commit()
            
            log_activity(f'Eliminados {count} fichajes para {employee.first_name} {employee.last_name}')
            flash(f'Se han eliminado {count} fichajes correctamente.', 'success')
        
        return redirect(url_for('checkin.list_checkins', employee_id=employee_id))
    
    return render_template('delete_checkins_form.html', 
                          title=f'Eliminar Fichajes de {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee)

@checkin_bp.route('/employee/<int:employee_id>/generate', methods=['GET', 'POST'])
@manager_required
```

#### `generate_checkins` (línea 1576)

**Código:**
```python
def generate_checkins(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para generar fichajes para este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    # Check if employee has schedules
    if not employee.schedules:
        flash('El empleado no tiene horarios definidos. Por favor, defina los horarios primero.', 'warning')
        return redirect(url_for('schedule.list_schedules', employee_id=employee_id))
    
    form = GenerateCheckInsForm()
    
    if form.validate_on_submit():
        start_date = form.start_date.data
        end_date = form.end_date.data
        
        # Generate check-ins
        check_ins = EmployeeCheckIn.generate_check_ins_for_schedule(employee, start_date, end_date)
        
        if not check_ins:
            flash('No se han podido generar fichajes para el periodo seleccionado.', 'warning')
        else:
            # Save check-ins to database
            for check_in in check_ins:
                db.session.add(check_in)
            db.session.commit()
            
            log_activity(f'Fichajes generados para {employee.first_name} {employee.last_name}')
            flash(f'Se han generado {len(check_ins)} fichajes correctamente.', 'success')
        
        return redirect(url_for('checkin.list_checkins', employee_id=employee_id))
    
    return render_template('generate_checkins_form.html', 
                          title=f'Generar Fichajes para {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee)

# Vacations routes
@vacation_bp.route('/employee/<int:employee_id>')
@login_required
```

#### `list_vacations` (línea 1619)

**Código:**
```python
def list_vacations(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to view this employee
    if not can_view_employee(employee):
        flash('No tienes permiso para ver las vacaciones de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    vacations = EmployeeVacation.query.filter_by(employee_id=employee_id).order_by(EmployeeVacation.start_date.desc()).all()
    
    return render_template('vacation_list.html', 
                          title=f'Vacaciones de {employee.first_name} {employee.last_name}', 
                          employee=employee,
                          vacations=vacations)

@vacation_bp.route('/employee/<int:employee_id>/new', methods=['GET', 'POST'])
@login_required
```

#### `create_vacation` (línea 1636)

**Código:**
```python
def create_vacation(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    # Check if user has permission to view this employee
    if not (can_manage_employee(employee) or 
            (current_user.is_empleado() and current_user.employee and current_user.employee.id == employee_id)):
        flash('No tienes permiso para gestionar las vacaciones de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    form = EmployeeVacationForm()
    
    if form.validate_on_submit():
        # Check if the vacation period overlaps with existing vacations
        overlapping_vacations = EmployeeVacation.query.filter_by(employee_id=employee_id).all()
        for v in overlapping_vacations:
            if v.overlaps_with(form.start_date.data, form.end_date.data):
                flash('El periodo de vacaciones se solapa con otro existente.', 'danger')
                return render_template('vacation_form.html', 
                                      title=f'Nuevas Vacaciones para {employee.first_name} {employee.last_name}', 
                                      form=form,
                                      employee=employee)
        
        # Simplificado: siempre REGISTRADA
        status = VacationStatus.REGISTRADA
        
        vacation = EmployeeVacation(
            start_date=form.start_date.data,
            end_date=form.end_date.data,
            status=status,
            notes=form.notes.data,
            employee_id=employee_id
        )
        
        # Ya no se requiere asignar aprobador en el flujo simplificado
        
        db.session.add(vacation)
        db.session.commit()
        
        log_activity(f'Vacaciones creadas para {employee.first_name} {employee.last_name}')
        flash('Vacaciones creadas correctamente.', 'success')
        return redirect(url_for('vacation.list_vacations', employee_id=employee_id))
    
    return render_template('vacation_form.html', 
                          title=f'Nuevas Vacaciones para {employee.first_name} {employee.last_name}', 
                          form=form,
                          employee=employee)

# Se eliminó la ruta de aprobación de vacaciones, ya no es necesaria con el flujo simplificado

@vacation_bp.route('/<int:id>/delete', methods=['POST'])
@manager_required
```

#### `delete_vacation` (línea 1687)

**Código:**
```python
def delete_vacation(id):
    vacation = EmployeeVacation.query.get_or_404(id)
    employee = Employee.query.get_or_404(vacation.employee_id)
    
    # Check if user has permission to manage this employee
    if not can_manage_employee(employee):
        flash('No tienes permiso para eliminar vacaciones de este empleado.', 'danger')
        return redirect(url_for('employee.list_employees'))
    
    employee_id = vacation.employee_id
    db.session.delete(vacation)
    db.session.commit()
    
    log_activity(f'Vacaciones eliminadas para {employee.first_name} {employee.last_name}')
    flash('Vacaciones eliminadas correctamente.', 'success')
    return redirect(url_for('vacation.list_vacations', employee_id=employee_id))

```


### routes_checkpoints.py

#### `admin_required` (línea 30)

**Código:**
```python
def admin_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or current_user.role != UserRole.ADMIN:
            flash('Acceso denegado. Se requieren permisos de administrador.', 'danger')
            return redirect(url_for('index'))
        return f(*args, **kwargs)
    return decorated_function


```

#### `decorated_function` (línea 32)

**Código:**
```python
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or current_user.role != UserRole.ADMIN:
            flash('Acceso denegado. Se requieren permisos de administrador.', 'danger')
            return redirect(url_for('index'))
        return f(*args, **kwargs)
    return decorated_function


```

#### `manager_required` (línea 40)

**Código:**
```python
def manager_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or (
            current_user.role != UserRole.ADMIN and 
            current_user.role != UserRole.GERENTE
        ):
            flash('Acceso denegado. Se requieren permisos de gerente o administrador.', 'danger')
            return redirect(url_for('index'))
        return f(*args, **kwargs)
    return decorated_function


```

#### `decorated_function` (línea 42)

**Código:**
```python
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or (
            current_user.role != UserRole.ADMIN and 
            current_user.role != UserRole.GERENTE
        ):
            flash('Acceso denegado. Se requieren permisos de gerente o administrador.', 'danger')
            return redirect(url_for('index'))
        return f(*args, **kwargs)
    return decorated_function


```

#### `checkpoint_required` (línea 53)

**Código:**
```python
def checkpoint_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'checkpoint_id' not in session:
            flash('Debe iniciar sesión como punto de fichaje.', 'warning')
            return redirect(url_for('checkpoints.login'))
        return f(*args, **kwargs)
    return decorated_function


# Rutas para administración de checkpoints
@checkpoints_bp.route('/')
@login_required
```

#### `decorated_function` (línea 55)

**Código:**
```python
    def decorated_function(*args, **kwargs):
        if 'checkpoint_id' not in session:
            flash('Debe iniciar sesión como punto de fichaje.', 'warning')
            return redirect(url_for('checkpoints.login'))
        return f(*args, **kwargs)
    return decorated_function


# Rutas para administración de checkpoints
```

#### `select_company` (línea 66)

**Docstring:**
```
Página de selección de empresa para el sistema de fichajes
```

**Código:**
```python
def select_company():
    """Página de selección de empresa para el sistema de fichajes"""
    try:
        # Obtener empresas según los permisos del usuario
        if current_user.is_admin():
            companies = Company.query.filter_by(is_active=True).all()
        else:
            companies = current_user.companies
        
        # Si solo hay una empresa o el usuario solo gestiona una, redirigir directamente
        if len(companies) == 1:
            return redirect(url_for('checkpoints.index_company', slug=companies[0].get_slug()))
        
        return render_template('checkpoints/select_company.html', companies=companies)
    except Exception as e:
        current_app.logger.error(f"Error en select_company: {e}")
        flash("Error al cargar la selección de empresas. Por favor, inténtelo de nuevo.", "danger")
        return redirect(url_for('main.dashboard'))

@checkpoints_bp.route('/company/<string:slug>')
@login_required
```

#### `index_company` (línea 87)

**Docstring:**
```
Página principal del sistema de fichajes para una empresa específica
```

**Código:**
```python
def index_company(slug):
    """Página principal del sistema de fichajes para una empresa específica"""
    try:
        # Usar approach más robusto para buscar empresas por slug
        from utils import slugify
        
        # Buscar por ID si es un número
        if slug.isdigit():
            company = Company.query.get_or_404(int(slug))
        else:
            # Buscar todas las empresas y comparar slugs
            all_companies = Company.query.all()
            company = next((c for c in all_companies if slugify(c.name) == slug), None)
            
            if not company:
                flash('Empresa no encontrada', 'danger')
                return redirect(url_for('checkpoints.select_company'))
        
        if not current_user.is_admin() and company not in current_user.companies:
            flash('No tiene permiso para gestionar esta empresa.', 'danger')
            return redirect(url_for('main.dashboard'))
        
        # Guardar la empresa seleccionada en la sesión
        session['selected_company_id'] = company.id
        
        # Inicializar estadísticas con valores por defecto
        stats = {
            'active_checkpoints': 0,
            'maintenance_checkpoints': 0,
            'disabled_checkpoints': 0,
            'employees_with_hours': 0,
            'employees_with_overtime': 0,
            'today_records': 0,
            'pending_checkout': 0,
            'active_incidents': 0
        }
        
        # Obtener estadísticas para el dashboard en bloques separados con manejo de excepciones
        try:
            stats['active_checkpoints'] = CheckPoint.query.filter_by(
                status=CheckPointStatus.ACTIVE, company_id=company.id).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener checkpoints activos: {e}")
            
        try:
            stats['maintenance_checkpoints'] = CheckPoint.query.filter_by(
                status=CheckPointStatus.MAINTENANCE, company_id=company.id).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener checkpoints en mantenimiento: {e}")
            
        try:
            stats['disabled_checkpoints'] = CheckPoint.query.filter_by(
                status=CheckPointStatus.DISABLED, company_id=company.id).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener checkpoints deshabilitados: {e}")
            
        try:
            stats['employees_with_hours'] = db.session.query(EmployeeContractHours)\
                .join(Employee, EmployeeContractHours.employee_id == Employee.id)\
                .filter(Employee.company_id == company.id).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener empleados con horas: {e}")
            
        try:
            stats['employees_with_overtime'] = db.session.query(EmployeeContractHours)\
                .join(Employee, EmployeeContractHours.employee_id == Employee.id)\
                .filter(Employee.company_id == company.id, 
                      EmployeeContractHours.allow_overtime == True).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener empleados con horas extra: {e}")
            
        try:
            stats['today_records'] = db.session.query(CheckPointRecord)\
                .join(CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id)\
                .filter(
                    CheckPoint.company_id == company.id,
                    CheckPointRecord.check_in_time >= datetime.combine(date.today(), time.min)
                ).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener registros de hoy: {e}")
            
        try:
            stats['pending_checkout'] = db.session.query(CheckPointRecord)\
                .join(CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id)\
                .filter(
                    CheckPoint.company_id == company.id,
                    CheckPointRecord.check_out_time.is_(None)
                ).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener registros pendientes de salida: {e}")
            
        try:
            stats['active_incidents'] = db.session.query(CheckPointIncident)\
                .join(CheckPointRecord, CheckPointIncident.record_id == CheckPointRecord.id)\
                .join(CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id)\
                .filter(
                    CheckPoint.company_id == company.id,
                    CheckPointIncident.resolved == False
                ).count()
        except Exception as e:
            current_app.logger.error(f"Error al obtener incidencias activas: {e}")
        
        # Inicializar variables con valores vacíos para evitar errores
        latest_records = []
        latest_incidents = []
        checkpoints = []
        employees = []
        week_stats = {}
        
        # Obtener los últimos registros filtrados por empresa
        try:
            latest_records = db.session.query(CheckPointRecord)\
                .join(CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id)\
                .filter(CheckPoint.company_id == company.id)\
                .order_by(CheckPointRecord.check_in_time.desc())\
                .limit(10).all()
        except Exception as e:
            current_app.logger.error(f"Error al obtener últimos registros: {e}")
        
        # Obtener las últimas incidencias filtradas por empresa
        try:
            latest_incidents = db.session.query(CheckPointIncident)\
                .join(CheckPointRecord, CheckPointIncident.record_id == CheckPointRecord.id)\
                .join(CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id)\
                .filter(CheckPoint.company_id == company.id)\
                .order_by(CheckPointIncident.created_at.desc())\
                .limit(10).all()
        except Exception as e:
            current_app.logger.error(f"Error al obtener últimas incidencias: {e}")
        
        # Obtener todos los puntos de fichaje para la empresa
        try:
            checkpoints = CheckPoint.query.filter_by(company_id=company.id).all()
        except Exception as e:
            current_app.logger.error(f"Error al obtener puntos de fichaje: {e}")
        
        # Obtener los empleados activos para la empresa (sin filtrar por is_active para evitar problemas)
        try:
            employees = Employee.query.filter_by(company_id=company.id).order_by(Employee.first_name).all()
        except Exception as e:
            current_app.logger.error(f"Error al obtener empleados: {e}")
        
        # Obtener los tipos de incidencias para el filtrado
        incident_types = []
        try:
            incident_types = [
                {'value': incident_type.value, 'name': incident_type.name}
                for incident_type in CheckPointIncidentType
            ]
        except Exception as e:
            current_app.logger.error(f"Error al obtener tipos de incidencias: {e}")
        
        # Obtener las estadísticas de la última semana para gráficos
        try:
            week_stats = {}
            today = date.today()
            for i in range(7):
                try:
                    day = today - timedelta(days=i)
                    count = db.session.query(CheckPointRecord)\
                        .join(CheckPoint, CheckPointRecord.checkpoint_id == CheckPoint.id)\
                        .filter(
                            CheckPoint.company_id == company.id,
                            extract('day', CheckPointRecord.check_in_time) == day.day,
                            extract('month', CheckPointRecord.check_in_time) == day.month,
                            extract('year', CheckPointRecord.check_in_time) == day.year
                        ).count()
                    week_stats[day.strftime('%d/%m')] = count
                except Exception as e:
                    current_app.logger.error(f"Error al obtener estadísticas del día {day}: {e}")
                    week_stats[day.strftime('%d/%m')] = 0
        except Exception as e:
            current_app.logger.error(f"Error al obtener estadísticas semanales: {e}")
        
        return render_template('checkpoints/index.html', 
                              stats=stats, 
                              latest_records=latest_records,
                              latest_incidents=latest_incidents,
                              company=company,
                              checkpoints=checkpoints,
                              employees=employees,
                              incident_types=incident_types,
                              week_stats=week_stats)
                              
    except Exception as e:
        current_app.logger.error(f"Error general en index_company: {e}")
        flash('Se produjo un error al cargar la página. Por favor, inténtelo de nuevo.', 'danger')
        return redirect(url_for('main.dashboard'))


@checkpoints_bp.route('/checkpoints')
@login_required
@manager_required
```

#### `list_checkpoints` (línea 280)

**Docstring:**
```
Lista todos los puntos de fichaje disponibles
```

**Código:**
```python
def list_checkpoints():
    """Lista todos los puntos de fichaje disponibles"""
    try:
        # Verificar si hay una empresa seleccionada
        company_id = session.get('selected_company_id')
        if not company_id:
            return redirect(url_for('checkpoints.select_company'))
        
        # Verificar permiso para acceder a esta empresa
        company = Company.query.get_or_404(company_id)
        if not current_user.is_admin() and company not in current_user.companies:
            flash('No tiene permiso para gestionar esta empresa.', 'danger')
            return redirect(url_for('checkpoints.select_company'))
        
        # Filtrar por la empresa seleccionada
        try:
            checkpoints = CheckPoint.query.filter_by(company_id=company_id).all()
        except Exception as e:
            current_app.logger.error(f"Error al obtener puntos de fichaje: {e}")
            checkpoints = []
            flash("Hubo un problema al cargar los puntos de fichaje. Se muestra una lista parcial.", "warning")
        
        return render_template('checkpoints/list_checkpoints.html', checkpoints=checkpoints, company=company)
    except Exception as e:
        current_app.logger.error(f"Error general en list_checkpoints: {e}")
        flash('Se produjo un error al cargar la página. Por favor, inténtelo de nuevo.', 'danger')
        return redirect(url_for('main.dashboard'))


@checkpoints_bp.route('/checkpoints/new', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `create_checkpoint` (línea 312)

**Docstring:**
```
Crea un nuevo punto de fichaje
```

**Código:**
```python
def create_checkpoint():
    """Crea un nuevo punto de fichaje"""
    # Verificar si hay una empresa seleccionada
    company_id = session.get('selected_company_id')
    if not company_id:
        return redirect(url_for('checkpoints.select_company'))
    
    # Verificar permiso para acceder a esta empresa
    company = Company.query.get_or_404(company_id)
    if not current_user.is_admin() and company not in current_user.companies:
        flash('No tiene permiso para gestionar esta empresa.', 'danger')
        return redirect(url_for('checkpoints.select_company'))
    
    form = CheckPointForm()
    
    # Cargar las empresas disponibles para el usuario actual pero preseleccionar la empresa actual
    if current_user.is_admin():
        companies = Company.query.filter_by(is_active=True).all()
    else:
        companies = current_user.companies
        
    form.company_id.choices = [(c.id, c.name) for c in companies]
    
    # Si es GET, preseleccionamos la empresa de la sesión
    if request.method == 'GET':
        form.company_id.data = company_id
    
    if form.validate_on_submit():
        # Convertir el string del status a un objeto de enumeración
        status_value = form.status.data
        status_enum = CheckPointStatus.ACTIVE  # Default
        for status in CheckPointStatus:
            if status.value == status_value:
                status_enum = status
                break
                
        checkpoint = CheckPoint(
            name=form.name.data,
            description=form.description.data,
            location=form.location.data,
            status=status_enum,
            username=form.username.data,
            company_id=form.company_id.data,
            enforce_contract_hours=form.enforce_contract_hours.data,
            auto_adjust_overtime=form.auto_adjust_overtime.data,
            # Nuevos campos para configuración de horario de funcionamiento
            enforce_operation_hours=form.enforce_operation_hours.data,
            operation_start_time=form.operation_start_time.data,
            operation_end_time=form.operation_end_time.data
        )
        
        if form.password.data:
            checkpoint.set_password(form.password.data)
        
        db.session.add(checkpoint)
        
        try:
            db.session.commit()
            flash(f'Punto de fichaje "{checkpoint.name}" creado con éxito.', 'success')
            return redirect(url_for('checkpoints.list_checkpoints'))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al crear el punto de fichaje: {str(e)}', 'danger')
    
    return render_template('checkpoints/checkpoint_form.html', form=form, title='Nuevo Punto de Fichaje', company=company)


@checkpoints_bp.route('/checkpoints/<int:id>/edit', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `edit_checkpoint` (línea 382)

**Docstring:**
```
Edita un punto de fichaje existente
```

**Código:**
```python
def edit_checkpoint(id):
    """Edita un punto de fichaje existente"""
    checkpoint = CheckPoint.query.get_or_404(id)
    
    # Verificar permiso (solo admin o gerente de la empresa)
    if not current_user.is_admin() and checkpoint.company_id not in [c.id for c in current_user.companies]:
        flash('No tiene permiso para editar este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints.list_checkpoints'))
    
    form = CheckPointForm(obj=checkpoint)
    
    # Cargar las empresas disponibles para el usuario actual
    if current_user.is_admin():
        companies = Company.query.filter_by(is_active=True).all()
    else:
        companies = current_user.companies
        
    form.company_id.choices = [(c.id, c.name) for c in companies]
    
    if form.validate_on_submit():
        # Convertir el string del status a un objeto de enumeración
        status_value = form.status.data
        status_enum = CheckPointStatus.ACTIVE  # Default
        for status in CheckPointStatus:
            if status.value == status_value:
                status_enum = status
                break
                
        checkpoint.name = form.name.data
        checkpoint.description = form.description.data
        checkpoint.location = form.location.data
        checkpoint.status = status_enum
        checkpoint.username = form.username.data
        checkpoint.company_id = form.company_id.data
        checkpoint.enforce_contract_hours = form.enforce_contract_hours.data
        checkpoint.auto_adjust_overtime = form.auto_adjust_overtime.data
        # Actualizar configuración de horario de funcionamiento
        checkpoint.enforce_operation_hours = form.enforce_operation_hours.data
        checkpoint.operation_start_time = form.operation_start_time.data
        checkpoint.operation_end_time = form.operation_end_time.data
        
        if form.password.data:
            checkpoint.set_password(form.password.data)
        
        try:
            db.session.commit()
            flash(f'Punto de fichaje "{checkpoint.name}" actualizado con éxito.', 'success')
            return redirect(url_for('checkpoints.list_checkpoints'))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al actualizar el punto de fichaje: {str(e)}', 'danger')
    
    return render_template('checkpoints/checkpoint_form.html', form=form, checkpoint=checkpoint, 
                          title='Editar Punto de Fichaje')


@checkpoints_bp.route('/checkpoints/<int:id>/delete', methods=['POST'])
@login_required
@manager_required
```

#### `delete_checkpoint` (línea 441)

**Docstring:**
```
Elimina un punto de fichaje con todos sus registros asociados
```

**Código:**
```python
def delete_checkpoint(id):
    """Elimina un punto de fichaje con todos sus registros asociados"""
    try:
        checkpoint = CheckPoint.query.get_or_404(id)
        
        # Verificar permiso (solo admin o gerente de la empresa)
        if not current_user.is_admin() and checkpoint.company_id not in [c.id for c in current_user.companies]:
            flash('No tiene permiso para eliminar este punto de fichaje.', 'danger')
            return redirect(url_for('checkpoints.list_checkpoints'))
        
        # Contar registros asociados para el mensaje informativo
        records_count = CheckPointRecord.query.filter_by(checkpoint_id=id).count()
        
        # Guardar el nombre para el mensaje posterior
        checkpoint_name = checkpoint.name
        
        # Iniciar transacción para eliminar todo en cascada
        try:
            # 1. Primero, obtener todos los registros asociados al checkpoint
            checkpoint_records = CheckPointRecord.query.filter_by(checkpoint_id=id).all()
            
            for record in checkpoint_records:
                # 2. Eliminar las incidencias asociadas a cada registro
                CheckPointIncident.query.filter_by(record_id=record.id).delete()
                
                # 3. Eliminar los registros originales (históricos) de cada registro
                CheckPointOriginalRecord.query.filter_by(record_id=record.id).delete()
            
            # 4. Eliminar todos los registros del checkpoint
            CheckPointRecord.query.filter_by(checkpoint_id=id).delete()
            
            # 5. Finalmente eliminar el checkpoint
            db.session.delete(checkpoint)
            
            # Confirmar todos los cambios
            db.session.commit()
            
            if records_count > 0:
                flash(f'Punto de fichaje "{checkpoint_name}" eliminado con éxito junto con {records_count} registros asociados.', 'success')
            else:
                flash(f'Punto de fichaje "{checkpoint_name}" eliminado con éxito.', 'success')
                
        except Exception as e:
            db.session.rollback()
            current_app.logger.error(f"Error al eliminar punto de fichaje: {e}")
            flash(f'Error al eliminar el punto de fichaje: La operación no pudo completarse debido a un error de base de datos.', 'danger')
    except Exception as e:
        current_app.logger.error(f"Error general en delete_checkpoint: {e}")
        flash('Se produjo un error al procesar la solicitud. Por favor, inténtelo de nuevo.', 'danger')
    
    return redirect(url_for('checkpoints.list_checkpoints'))


@checkpoints_bp.route('/checkpoints/<int:id>/records')
@login_required
@manager_required
```

#### `list_checkpoint_records` (línea 497)

**Docstring:**
```
Muestra los registros de fichaje de un punto específico
```

**Código:**
```python
def list_checkpoint_records(id):
    """Muestra los registros de fichaje de un punto específico"""
    checkpoint = CheckPoint.query.get_or_404(id)
    
    # Verificar permiso (solo admin o gerente de la empresa)
    if not current_user.is_admin() and checkpoint.company_id not in [c.id for c in current_user.companies]:
        flash('No tiene permiso para ver los registros de este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints.list_checkpoints'))
    
    page = request.args.get('page', 1, type=int)
    records = CheckPointRecord.query.filter_by(checkpoint_id=id).order_by(
        CheckPointRecord.check_in_time.desc()
    ).paginate(page=page, per_page=20)
    
    return render_template('checkpoints/list_records.html', 
                          checkpoint=checkpoint, 
                          records=records)


@checkpoints_bp.route('/employees/<int:id>/contract_hours', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `manage_contract_hours` (línea 519)

**Docstring:**
```
Gestiona la configuración de horas por contrato de un empleado
```

**Código:**
```python
def manage_contract_hours(id):
    """Gestiona la configuración de horas por contrato de un empleado"""
    employee = Employee.query.get_or_404(id)
    
    # Verificar permiso (solo admin o gerente de la empresa)
    if not current_user.is_admin() and employee.company_id not in [c.id for c in current_user.companies]:
        flash('No tiene permiso para gestionar las horas de contrato de este empleado.', 'danger')
        return redirect(url_for('checkpoints.index_company', slug=employee.company.get_slug()))
    
    # Buscar o crear configuración de horas de contrato
    contract_hours = EmployeeContractHours.query.filter_by(employee_id=employee.id).first()
    
    if not contract_hours:
        contract_hours = EmployeeContractHours(employee_id=employee.id)
        db.session.add(contract_hours)
        db.session.commit()
    
    form = ContractHoursForm(obj=contract_hours)
    
    if form.validate_on_submit():
        form.populate_obj(contract_hours)
        
        try:
            db.session.commit()
            flash('Configuración de horas de contrato actualizada con éxito.', 'success')
            return redirect(url_for('checkpoints.index_company', slug=employee.company.get_slug()))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al actualizar la configuración: {str(e)}', 'danger')
    
    return render_template('checkpoints/contract_hours_form.html', 
                          form=form, 
                          employee=employee)


@checkpoints_bp.route('/records/<int:id>/adjust', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `adjust_record` (línea 557)

**Docstring:**
```
Ajusta manualmente un registro de fichaje
```

**Código:**
```python
def adjust_record(id):
    """Ajusta manualmente un registro de fichaje"""
    record = CheckPointRecord.query.get_or_404(id)
    
    # Verificar permiso
    employee_company_id = record.employee.company_id
    if not current_user.is_admin() and employee_company_id not in [c.id for c in current_user.companies]:
        flash('No tiene permiso para ajustar este registro de fichaje.', 'danger')
        return redirect(url_for('checkpoints.list_checkpoints'))
    
    form = CheckPointRecordAdjustmentForm()
    
    # Pre-llenar el formulario con los datos actuales
    if request.method == 'GET':
        form.check_in_date.data = record.check_in_time.strftime('%Y-%m-%d')
        form.check_in_time.data = record.check_in_time.time()
        if record.check_out_time:
            form.check_out_time.data = record.check_out_time.time()
    
    if form.validate_on_submit():
        # Guardar una copia del registro original en la nueva tabla
        from models_checkpoints import CheckPointOriginalRecord
        
        # Crear un registro del estado original antes del ajuste
        original_record = CheckPointOriginalRecord(
            record_id=record.id,
            original_check_in_time=record.check_in_time,
            original_check_out_time=record.check_out_time,
            original_signature_data=record.signature_data,
            original_has_signature=record.has_signature,
            original_notes=record.notes,
            adjusted_by_id=current_user.id,
            adjustment_reason=form.adjustment_reason.data
        )
        db.session.add(original_record)
        
        # También guardar los valores originales en el registro principal si es el primer ajuste
        if not record.adjusted:
            record.original_check_in_time = record.check_in_time
            record.original_check_out_time = record.check_out_time
        
        # Actualizar con los nuevos valores
        check_in_date = datetime.strptime(form.check_in_date.data, '%Y-%m-%d').date()
        
        # Combinar fecha y hora para check_in_time
        record.check_in_time = datetime.combine(check_in_date, form.check_in_time.data)
        
        # Combinar fecha y hora para check_out_time si está presente
        if form.check_out_time.data:
            # Si la hora de salida es menor que la de entrada, asumimos que es del día siguiente
            if form.check_out_time.data < form.check_in_time.data:
                check_out_date = check_in_date + timedelta(days=1)
            else:
                check_out_date = check_in_date
                
            record.check_out_time = datetime.combine(check_out_date, form.check_out_time.data)
        
        record.adjusted = True
        record.adjustment_reason = form.adjustment_reason.data
        
        # Si se debe aplicar el límite de horas de contrato
        if form.enforce_contract_hours.data and record.check_out_time:
            contract_hours = EmployeeContractHours.query.filter_by(employee_id=record.employee_id).first()
            
            if contract_hours:
                # Ajustar el fichaje según las horas de contrato
                new_check_in, new_check_out = contract_hours.calculate_adjusted_hours(
                    record.check_in_time, record.check_out_time
                )
                
                if new_check_in and new_check_in != record.check_in_time:
                    record.check_in_time = new_check_in
                    
                    # Marcar con R en lugar de crear incidencia por ajuste de contrato
                    record.notes = (record.notes or "") + " [R] Ajustado para cumplir límite de horas."
        
        try:
            db.session.commit()
            flash('Registro de fichaje ajustado con éxito.', 'success')
            return redirect(url_for('checkpoints.list_checkpoint_records', id=record.checkpoint_id))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al ajustar el registro: {str(e)}', 'danger')
    
    return render_template('checkpoints/adjust_record_form.html', 
                          form=form, 
                          record=record)


@checkpoints_bp.route('/records/<int:id>/signature', methods=['GET', 'POST'])
@login_required
```

#### `record_signature` (línea 648)

**Docstring:**
```
Permite al empleado firmar un registro de fichaje
```

**Código:**
```python
def record_signature(id):
    """Permite al empleado firmar un registro de fichaje"""
    record = CheckPointRecord.query.get_or_404(id)
    
    # Solo el empleado o un administrador pueden firmar
    is_admin_or_manager = current_user.is_admin() or current_user.is_gerente()
    is_employee_user = current_user.employee and current_user.employee.id == record.employee_id
    
    if not is_admin_or_manager and not is_employee_user:
        flash('No tiene permiso para firmar este registro.', 'danger')
        return redirect(url_for('index'))
    
    form = SignaturePadForm()
    
    if form.validate_on_submit():
        record.signature_data = form.signature_data.data
        record.has_signature = True
        
        try:
            db.session.commit()
            flash('Firma guardada con éxito.', 'success')
            
            # Redireccionar según el tipo de usuario
            if is_admin_or_manager:
                return redirect(url_for('checkpoints.list_checkpoint_records', id=record.checkpoint_id))
            else:
                return redirect(url_for('main.dashboard'))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al guardar la firma: {str(e)}', 'danger')
    
    return render_template('checkpoints/signature_pad.html', 
                          form=form, 
                          record=record)


@checkpoints_bp.route('/records')
@login_required
@manager_required
```

#### `list_records_all` (línea 687)

**Docstring:**
```
Muestra todos los registros de fichaje disponibles según permisos
```

**Código:**
```python
def list_records_all():
    """Muestra todos los registros de fichaje disponibles según permisos"""
    # Determinar los registros a los que tiene acceso el usuario
    if current_user.is_admin():
        base_query = CheckPointRecord.query
    else:
        # Si es gerente, solo registros de las empresas que administra
        company_ids = [company.id for company in current_user.companies]
        
        if not company_ids:
            flash('No tiene empresas asignadas para gestionar registros.', 'warning')
            return redirect(url_for('dashboard'))
        
        # Buscar empleados de esas empresas
        employee_ids = db.session.query(Employee.id).filter(
            Employee.company_id.in_(company_ids)
        ).all()
        employee_ids = [e[0] for e in employee_ids]
        
        if not employee_ids:
            flash('No hay empleados registrados en sus empresas.', 'warning')
            return redirect(url_for('dashboard'))
        
        base_query = CheckPointRecord.query.filter(
            CheckPointRecord.employee_id.in_(employee_ids)
        )
    
    # Filtros adicionales (fecha, empleado, etc.)
    start_date = request.args.get('start_date')
    end_date = request.args.get('end_date')
    employee_id = request.args.get('employee_id', type=int)
    status = request.args.get('status')
    
    if start_date:
        try:
            start_date = datetime.strptime(start_date, '%Y-%m-%d').date()
            base_query = base_query.filter(
                func.date(CheckPointRecord.check_in_time) >= start_date
            )
        except ValueError:
            pass
    
    if end_date:
        try:
            end_date = datetime.strptime(end_date, '%Y-%m-%d').date()
            base_query = base_query.filter(
                func.date(CheckPointRecord.check_in_time) <= end_date
            )
        except ValueError:
            pass
    
    if employee_id:
        base_query = base_query.filter(CheckPointRecord.employee_id == employee_id)
    
    if status:
        if status == 'pending':
            base_query = base_query.filter(CheckPointRecord.check_out_time.is_(None))
        elif status == 'completed':
            base_query = base_query.filter(CheckPointRecord.check_out_time.isnot(None))
        elif status == 'adjusted':
            base_query = base_query.filter(CheckPointRecord.adjusted == True)
        elif status == 'incidents':
            # Registros con incidencias (requiere una subconsulta)
            incident_record_ids = db.session.query(CheckPointIncident.record_id).distinct().subquery()
            base_query = base_query.filter(CheckPointRecord.id.in_(incident_record_ids))
    
    # Ordenar y paginar
    page = request.args.get('page', 1, type=int)
    records = base_query.order_by(CheckPointRecord.check_in_time.desc()).paginate(
        page=page, per_page=20
    )
    
    # Datos adicionales para el filtro
    if current_user.is_admin():
        filter_employees = Employee.query.filter_by(is_active=True).all()
    else:
        company_ids = [company.id for company in current_user.companies]
        filter_employees = Employee.query.filter(
            Employee.company_id.in_(company_ids),
            Employee.is_active == True
        ).all()
    
    return render_template(
        'checkpoints/all_records.html',
        records=records,
        filter_employees=filter_employees,
        current_filters={
            'start_date': start_date.strftime('%Y-%m-%d') if isinstance(start_date, date) else None,
            'end_date': end_date.strftime('%Y-%m-%d') if isinstance(end_date, date) else None,
            'employee_id': employee_id,
            'status': status
        }
    )


@checkpoints_bp.route('/incidents')
@login_required
@manager_required
```

#### `list_incidents` (línea 785)

**Docstring:**
```
Muestra todas las incidencias de fichaje según permisos
```

**Código:**
```python
def list_incidents():
    """Muestra todas las incidencias de fichaje según permisos"""
    # Determinar las incidencias a las que tiene acceso el usuario
    if current_user.is_admin():
        base_query = CheckPointIncident.query.join(
            CheckPointRecord, CheckPointIncident.record_id == CheckPointRecord.id
        )
    else:
        # Si es gerente, solo incidencias de las empresas que administra
        company_ids = [company.id for company in current_user.companies]
        
        if not company_ids:
            flash('No tiene empresas asignadas para gestionar incidencias.', 'warning')
            return redirect(url_for('dashboard'))
        
        # Buscar empleados de esas empresas
        employee_ids = db.session.query(Employee.id).filter(
            Employee.company_id.in_(company_ids)
        ).all()
        employee_ids = [e[0] for e in employee_ids]
        
        if not employee_ids:
            flash('No hay empleados registrados en sus empresas.', 'warning')
            return redirect(url_for('dashboard'))
        
        base_query = CheckPointIncident.query.join(
            CheckPointRecord, CheckPointIncident.record_id == CheckPointRecord.id
        ).filter(
            CheckPointRecord.employee_id.in_(employee_ids)
        )
    
    # Filtros adicionales
    incident_type = request.args.get('type')
    resolved = request.args.get('resolved')
    employee_id = request.args.get('employee_id', type=int)
    
    if incident_type:
        base_query = base_query.filter(CheckPointIncident.incident_type == incident_type)
    
    if resolved == 'yes':
        base_query = base_query.filter(CheckPointIncident.resolved == True)
    elif resolved == 'no':
        base_query = base_query.filter(CheckPointIncident.resolved == False)
    
    if employee_id:
        base_query = base_query.filter(CheckPointRecord.employee_id == employee_id)
    
    # Ordenar y paginar
    page = request.args.get('page', 1, type=int)
    incidents = base_query.order_by(CheckPointIncident.created_at.desc()).paginate(
        page=page, per_page=20
    )
    
    # Datos adicionales para el filtro
    if current_user.is_admin():
        filter_employees = Employee.query.filter_by(is_active=True).all()
    else:
        company_ids = [company.id for company in current_user.companies]
        filter_employees = Employee.query.filter(
            Employee.company_id.in_(company_ids),
            Employee.is_active == True
        ).all()
    
    return render_template(
        'checkpoints/incidents.html',
        incidents=incidents,
        filter_employees=filter_employees,
        incident_types=CheckPointIncidentType,
        current_filters={
            'type': incident_type,
            'resolved': resolved,
            'employee_id': employee_id
        }
    )


@checkpoints_bp.route('/incidents/<int:id>/resolve', methods=['POST'])
@login_required
@manager_required
```

#### `resolve_incident` (línea 864)

**Docstring:**
```
Marca una incidencia como resuelta
```

**Código:**
```python
def resolve_incident(id):
    """Marca una incidencia como resuelta"""
    incident = CheckPointIncident.query.get_or_404(id)
    
    # Verificar permisos
    record = incident.record
    employee_company_id = record.employee.company_id
    
    if not current_user.is_admin() and employee_company_id not in [c.id for c in current_user.companies]:
        flash('No tiene permiso para resolver esta incidencia.', 'danger')
        return redirect(url_for('checkpoints.list_incidents'))
    
    # Obtener las notas de resolución
    resolution_notes = request.form.get('resolution_notes', '')
    
    # Resolver la incidencia
    incident.resolve(current_user.id, resolution_notes)
    
    try:
        db.session.commit()
        flash('Incidencia marcada como resuelta con éxito.', 'success')
    except Exception as e:
        db.session.rollback()
        flash(f'Error al resolver la incidencia: {str(e)}', 'danger')
    
    # Redirigir a la página anterior o a la lista de incidencias
    next_page = request.args.get('next') or url_for('checkpoints.list_incidents')
    return redirect(next_page)


# Se ha eliminado la ruta secreta '/company/<slug>/rrrrrr'

# Se ha eliminado la ruta secreta '/company/<slug>/rrrrrr/edit/<int:id>'

# Se ha eliminado la ruta secreta '/company/<slug>/rrrrrr/restore/<int:id>'

# Se ha eliminado la ruta secreta '/company/<slug>/rrrrrr/delete/<int:id>'

# Se ha eliminado la ruta secreta '/company/<slug>/rrrrrr/export' y la función export_original_records_pdf

@checkpoints_bp.route('/company/<slug>/original', methods=['GET'])
@login_required
@admin_required
```

#### `view_original_records` (línea 907)

**Docstring:**
```
Página secreta para ver los registros originales de fichaje para una empresa específica
```

**Código:**
```python
def view_original_records(slug):
    """Página secreta para ver los registros originales de fichaje para una empresa específica"""
    from models_checkpoints import CheckPointOriginalRecord
    from utils import slugify
    
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Esta página es solo para administradores
    page = request.args.get('page', 1, type=int)
    start_date = request.args.get('start_date')
    end_date = request.args.get('end_date')
    employee_id = request.args.get('employee_id', type=int)
    show_all = request.args.get('show_all', 'true')  # Parámetro para mostrar todos los registros
    
    # Obtener los IDs de los empleados de esta empresa
    employee_ids = db.session.query(Employee.id).filter_by(company_id=company_id).all()
    employee_ids = [e[0] for e in employee_ids]
    
    # Construir la consulta base con filtro de empresa para todos los registros
    if show_all == 'true':
        # Consulta para todos los registros COMPLETOS, solo aquellos con hora de salida
        query = db.session.query(
            CheckPointRecord, 
            Employee
        ).join(
            Employee,
            CheckPointRecord.employee_id == Employee.id
        ).filter(
            Employee.company_id == company_id,
            CheckPointRecord.check_out_time.isnot(None)  # Solo registros con hora de salida
        ).outerjoin(
            CheckPointOriginalRecord,
            CheckPointOriginalRecord.record_id == CheckPointRecord.id
        )
    else:
        # Consulta original sólo para registros modificados
        query = db.session.query(
            CheckPointOriginalRecord, 
            CheckPointRecord, 
            Employee
        ).join(
            CheckPointRecord, 
            CheckPointOriginalRecord.record_id == CheckPointRecord.id
        ).join(
            Employee,
            CheckPointRecord.employee_id == Employee.id
        ).filter(
            Employee.company_id == company_id
        )
    
    # Aplicar filtros si los hay
    if start_date:
        try:
            start_date = datetime.strptime(start_date, '%Y-%m-%d').date()
            if show_all == 'true':
                query = query.filter(func.date(CheckPointRecord.check_in_time) >= start_date)
            else:
                query = query.filter(func.date(CheckPointOriginalRecord.original_check_in_time) >= start_date)
        except ValueError:
            pass
    
    if end_date:
        try:
            end_date = datetime.strptime(end_date, '%Y-%m-%d').date()
            if show_all == 'true':
                query = query.filter(func.date(CheckPointRecord.check_in_time) <= end_date)
            else:
                query = query.filter(func.date(CheckPointOriginalRecord.original_check_in_time) <= end_date)
        except ValueError:
            pass
    
    if employee_id:
        query = query.filter(Employee.id == employee_id)
    
    # Ordenar y paginar
    if show_all == 'true':
        all_records = query.order_by(
            CheckPointRecord.check_in_time.desc()
        ).paginate(page=page, per_page=20)
    else:
        all_records = query.order_by(
            CheckPointOriginalRecord.adjusted_at.desc()
        ).paginate(page=page, per_page=20)
    
    # Obtener la lista de empleados para el filtro (solo de esta empresa)
    employees = Employee.query.filter_by(company_id=company_id, is_active=True).order_by(Employee.first_name).all()
    
    return render_template(
        'checkpoints/original_records.html',
        original_records=all_records,
        employees=employees,
        company=company,
        company_id=company_id,
        show_all=show_all,
        filters={
            'start_date': start_date.strftime('%Y-%m-%d') if isinstance(start_date, date) else None,
            'end_date': end_date.strftime('%Y-%m-%d') if isinstance(end_date, date) else None,
            'employee_id': employee_id
        },
        title=f"Registros Originales de {company.name if company else ''} ({('Vista simplificada' if show_all == 'true' else 'Registros modificados')})"
    )

@checkpoints_bp.route('/records/export', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `export_records` (línea 1024)

**Docstring:**
```
Exporta registros de fichaje a PDF
```

**Código:**
```python
def export_records():
    """Exporta registros de fichaje a PDF"""
    form = ExportCheckPointRecordsForm()
    
    # Cargar empleados según los permisos del usuario
    if current_user.is_admin():
        employees = Employee.query.filter_by(is_active=True).all()
    else:
        company_ids = [company.id for company in current_user.companies]
        employees = Employee.query.filter(
            Employee.company_id.in_(company_ids), 
            Employee.is_active == True
        ).all()
    
    form.employee_id.choices = [(0, 'Todos los empleados')] + [(e.id, f"{e.first_name} {e.last_name}") for e in employees]
    
    if form.validate_on_submit():
        try:
            # Parsear fechas
            start_date = datetime.strptime(form.start_date.data, '%Y-%m-%d').date()
            end_date = datetime.strptime(form.end_date.data, '%Y-%m-%d').date()
            
            # Construir consulta
            query = CheckPointRecord.query.filter(
                func.date(CheckPointRecord.check_in_time) >= start_date,
                func.date(CheckPointRecord.check_in_time) <= end_date
            )
            
            # Filtrar por empleado si se especifica
            if form.employee_id.data != 0:
                query = query.filter(CheckPointRecord.employee_id == form.employee_id.data)
            
            # Ejecutar consulta
            records = query.order_by(
                CheckPointRecord.employee_id,
                CheckPointRecord.check_in_time
            ).all()
            
            if not records:
                flash('No se encontraron registros para el período seleccionado.', 'warning')
                return redirect(url_for('checkpoints.export_records'))
            
            # Generar PDF
            pdf_file = generate_pdf_report(
                records=records, 
                start_date=start_date, 
                end_date=end_date,
                include_signature=form.include_signature.data
            )
            
            # Guardar temporalmente y enviar
            filename = f"fichajes_{start_date.strftime('%Y%m%d')}_{end_date.strftime('%Y%m%d')}.pdf"
            return send_file(
                pdf_file,
                as_attachment=True,
                download_name=filename,
                mimetype='application/pdf'
            )
            
        except Exception as e:
            flash(f'Error al generar el informe: {str(e)}', 'danger')
    
    # Establecer fechas predeterminadas si es la primera carga
    if request.method == 'GET':
        today = date.today()
        # Por defecto, mostrar el mes actual
        form.start_date.data = date(today.year, today.month, 1).strftime('%Y-%m-%d')
        form.end_date.data = today.strftime('%Y-%m-%d')
    
    return render_template('checkpoints/export_form.html', form=form)


# Rutas para puntos de fichaje (interfaz tablet/móvil)
@checkpoints_bp.route('/login', methods=['GET', 'POST'])
```

#### `login` (línea 1098)

**Docstring:**
```
Página de login para puntos de fichaje
```

**Código:**
```python
def login():
    """Página de login para puntos de fichaje"""
    # Si ya hay una sesión activa, redirigir al dashboard
    if 'checkpoint_id' in session:
        return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    # Verificar si se ha pasado un checkpoint_id como parámetro
    checkpoint_id = request.args.get('checkpoint_id')
    checkpoint = None
    if checkpoint_id:
        # Si tenemos un ID de checkpoint, intentamos cargar ese checkpoint específico
        try:
            checkpoint_id = int(checkpoint_id)
            checkpoint = CheckPoint.query.get(checkpoint_id)
            if not checkpoint:
                flash('El punto de fichaje especificado no existe.', 'danger')
        except ValueError:
            flash('ID de punto de fichaje no válido.', 'danger')
    
    form = CheckPointLoginForm()
    
    if form.validate_on_submit():
        # Si se está realizando un login mediante formulario, buscar el checkpoint por username
        login_checkpoint = CheckPoint.query.filter_by(username=form.username.data).first()
        
        if login_checkpoint and login_checkpoint.verify_password(form.password.data):
            if login_checkpoint.status == CheckPointStatus.ACTIVE:
                # Guardar ID del punto de fichaje en la sesión
                session['checkpoint_id'] = login_checkpoint.id
                session['checkpoint_name'] = login_checkpoint.name
                session['company_id'] = login_checkpoint.company_id
                
                flash(f'Bienvenido al punto de fichaje {login_checkpoint.name}', 'success')
                return redirect(url_for('checkpoints.checkpoint_dashboard'))
            else:
                flash('Este punto de fichaje está desactivado o en mantenimiento.', 'warning')
        else:
            flash('Usuario o contraseña incorrectos.', 'danger')
    
    # Si tenemos un checkpoint específico y es la primera carga (GET), prellenamos el formulario
    if checkpoint and request.method == 'GET':
        form.username.data = checkpoint.username
    
    return render_template('checkpoints/login.html', form=form, checkpoint=checkpoint)


@checkpoints_bp.route('/login/<int:checkpoint_id>', methods=['GET', 'POST'])
```

#### `login_to_checkpoint` (línea 1145)

**Docstring:**
```
Acceso directo a un punto de fichaje específico por ID
```

**Código:**
```python
def login_to_checkpoint(checkpoint_id):
    """Acceso directo a un punto de fichaje específico por ID"""
    # Si ya hay una sesión activa, redirigir al dashboard
    if 'checkpoint_id' in session:
        return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    # Intentar cargar el checkpoint especificado
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    
    form = CheckPointLoginForm()
    
    if form.validate_on_submit():
        if checkpoint and checkpoint.verify_password(form.password.data):
            if checkpoint.status == CheckPointStatus.ACTIVE:
                # Guardar ID del punto de fichaje en la sesión
                session['checkpoint_id'] = checkpoint.id
                session['checkpoint_name'] = checkpoint.name
                session['company_id'] = checkpoint.company_id
                
                flash(f'Bienvenido al punto de fichaje {checkpoint.name}', 'success')
                return redirect(url_for('checkpoints.checkpoint_dashboard'))
            else:
                flash('Este punto de fichaje está desactivado o en mantenimiento.', 'warning')
        else:
            flash('Contraseña incorrecta para este punto de fichaje.', 'danger')
    
    # Prellenar el nombre de usuario
    if request.method == 'GET':
        form.username.data = checkpoint.username
    
    return render_template('checkpoints/login.html', form=form, checkpoint=checkpoint)


@checkpoints_bp.route('/logout')
```

#### `logout` (línea 1179)

**Docstring:**
```
Cierra la sesión del punto de fichaje
```

**Código:**
```python
def logout():
    """Cierra la sesión del punto de fichaje"""
    # Limpiar variables de sesión
    session.pop('checkpoint_id', None)
    session.pop('checkpoint_name', None)
    session.pop('company_id', None)
    session.pop('employee_id', None)
    
    flash('Ha cerrado sesión correctamente.', 'success')
    return redirect(url_for('checkpoints.login'))


@checkpoints_bp.route('/dashboard')
@checkpoint_required
```

#### `checkpoint_dashboard` (línea 1193)

**Docstring:**
```
Dashboard principal del punto de fichaje
```

**Código:**
```python
def checkpoint_dashboard():
    """Dashboard principal del punto de fichaje"""
    checkpoint_id = session.get('checkpoint_id')
    # Usamos refresh=True para asegurarnos de obtener los datos más actualizados
    db.session.expire_all()  # Asegurarse de que todas las entidades se refresquen
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    
    # Obtener todos los empleados de la empresa asociada al punto de fichaje
    # Mostramos todos los empleados sin filtrar por is_active para mantener consistencia
    # con la vista de empleados en la sección de gestión de empresas
    employees = Employee.query.filter_by(
        company_id=checkpoint.company_id
    ).order_by(Employee.first_name, Employee.last_name).all()
    
    return render_template('checkpoints/dashboard.html', 
                          checkpoint=checkpoint,
                          employees=employees)


@checkpoints_bp.route('/employee/<int:id>/pin', methods=['GET', 'POST'])
@checkpoint_required
```

#### `employee_pin` (línea 1214)

**Docstring:**
```
Página para introducir el PIN del empleado

Nueva implementación con separación clara de responsabilidades:
1. Validación del empleado y permisos
2. Verificación del PIN
3. Interfaz de usuario adaptativa según el estado del empleado
```

**Código:**
```python
def employee_pin(id):
    """
    Página para introducir el PIN del empleado
    
    Nueva implementación con separación clara de responsabilidades:
    1. Validación del empleado y permisos
    2. Verificación del PIN
    3. Interfaz de usuario adaptativa según el estado del empleado
    """
    # Obtener información del punto de fichaje y empleado
    checkpoint_id = session.get('checkpoint_id')
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    employee = Employee.query.get_or_404(id)
    
    # Verificar que el empleado pertenece a la empresa del punto de fichaje
    if employee.company_id != checkpoint.company_id:
        flash('Empleado no válido para este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    # Inicializar formulario
    form = CheckPointEmployeePinForm()
    form.employee_id.data = employee.id
    
    # Verificar si existe un registro pendiente de fichaje (entrada sin salida)
    pending_record = CheckPointRecord.query.filter_by(
        employee_id=employee.id,
        checkpoint_id=checkpoint_id,
        check_out_time=None
    ).first()
    
    # Log para debugging
    print(f"Empleado: {employee.id} - {employee.first_name} {employee.last_name}")
    print(f"Estado actual: is_on_shift={employee.is_on_shift}, pending_record={pending_record is not None}")
    if pending_record:
        print(f"Registro pendiente ID: {pending_record.id}, Entrada: {pending_record.check_in_time}")
    
    # Procesar formulario si es submit
    if form.validate_on_submit():
        # Verificar si el PIN ha sido validado por AJAX
        pin_verified = request.form.get('pin_verified') == '1'
        
        # Verificación del PIN (últimos 4 dígitos del DNI, ignorando la letra final en DNI español)
        # Filtrar solo los dígitos del DNI
        dni_digits = ''.join(c for c in employee.dni if c.isdigit())
        pin_from_dni = dni_digits[-4:] if len(dni_digits) >= 4 else dni_digits
        
        # Si el PIN fue validado por AJAX o si es correcto
        if pin_verified or form.pin.data == pin_from_dni:
            # Obtener la acción solicitada
            action = request.form.get('action')
            
            # Procesar según la acción
            if action in ['checkin', 'checkout']:
                # Intentar realizar la acción en una transacción atómica
                return process_employee_action(employee, checkpoint_id, action, pending_record)
            else:
                flash('Acción no reconocida. Por favor, inténtelo de nuevo.', 'danger')
        else:
            flash('PIN incorrecto. Inténtelo de nuevo.', 'danger')
    
    # Renderizar la plantilla con los datos necesarios
    return render_template(
        'checkpoints/employee_pin.html', 
        form=form,
        employee=employee,
        pending_record=pending_record,
        checkpoint=checkpoint
    )


```

#### `create_schedule_incident` (línea 1284)

**Docstring:**
```
Crea una incidencia relacionada con horarios
```

**Código:**
```python
def create_schedule_incident(record, incident_type, description):
    """
    Crea una incidencia relacionada con horarios
    """
    incident = CheckPointIncident(
        record_id=record.id,
        incident_type=incident_type,
        description=description
    )
    db.session.add(incident)
    return incident

```

#### `process_employee_action` (línea 1296)

**Docstring:**
```
Función auxiliar para procesar las acciones de entrada/salida
Encapsula la lógica de negocio y el manejo de transacciones
```

**Código:**
```python
def process_employee_action(employee, checkpoint_id, action, pending_record):
    """
    Función auxiliar para procesar las acciones de entrada/salida
    Encapsula la lógica de negocio y el manejo de transacciones
    """
    try:
        # CASO 1: Check-out (finalizar jornada)
        if action == 'checkout' and pending_record:
            # Obtener datos actuales para logging
            old_status = employee.is_on_shift
            record_id = pending_record.id
            
            # Actualizar en una transacción
            db.session.begin_nested()
            
            # 1. Registrar hora de salida
            checkout_time = get_current_time()
            pending_record.check_out_time = checkout_time
            db.session.add(pending_record)
            
            # Guardar siempre el registro original primero al hacer checkout
            from models_checkpoints import CheckPointOriginalRecord
            
            # Capturar los valores reales antes de cualquier ajuste
            # Importante: Guardamos la hora exacta que se introdujo al inicio de la jornada
            original_checkin = pending_record.check_in_time  # Esta es la hora original de entrada
            original_checkout = get_current_time()  # Esta es la hora real de salida antes de ajustes
            
            # Actualizar primero la hora de salida con la hora real
            pending_record.check_out_time = original_checkout
            db.session.add(pending_record)
            db.session.flush()  # Aseguramos que pending_record tenga todos sus campos actualizados
            
            # Crear un registro del estado original con las horas reales
            original_record = CheckPointOriginalRecord(
                record_id=pending_record.id,
                original_check_in_time=original_checkin,
                original_check_out_time=original_checkout,
                original_signature_data=pending_record.signature_data,
                original_has_signature=pending_record.has_signature,
                original_notes=pending_record.notes,
                adjustment_reason="Registro original al finalizar fichaje"
            )
            db.session.add(original_record)
            
            # 2. Verificar configuración de horas de contrato
            contract_hours = EmployeeContractHours.query.filter_by(employee_id=employee.id).first()
            if contract_hours:
                # Verificar si se debe ajustar el horario según configuración
                adjusted_checkin, adjusted_checkout = contract_hours.calculate_adjusted_hours(
                    original_checkin, original_checkout
                )
                
                # Verificar si hay ajustes a realizar
                needs_adjustment = (adjusted_checkin and adjusted_checkin != original_checkin) or \
                                  (adjusted_checkout and adjusted_checkout != original_checkout)
                
                # Si hay ajustes, actualizar el registro
                if needs_adjustment:
                    # Actualizar el registro con los valores ajustados
                    if adjusted_checkin and adjusted_checkin != original_checkin:
                        pending_record.check_in_time = adjusted_checkin
                        # Marcar con R en lugar de crear incidencia
                        pending_record.notes = (pending_record.notes or "") + f" [R] Hora entrada ajustada de {original_checkin.strftime('%H:%M')} a {adjusted_checkin.strftime('%H:%M')}"
                    
                    if adjusted_checkout and adjusted_checkout != original_checkout:
                        pending_record.check_out_time = adjusted_checkout
                        # Marcar con R en lugar de crear incidencia
                        pending_record.notes = (pending_record.notes or "") + f" [R] Hora salida ajustada de {original_checkout.strftime('%H:%M')} a {adjusted_checkout.strftime('%H:%M')}"
                    
                    # Marcar como ajustado
                    pending_record.adjusted = True
                    
                    # Actualizar la razón del ajuste en el registro original
                    original_record.adjustment_reason = "Ajuste automático por límite de horas de contrato"
                    db.session.add(original_record)
                
                # Verificar si hay horas extra
                duration = (pending_record.check_out_time - pending_record.check_in_time).total_seconds() / 3600
                if contract_hours.is_overtime(duration):
                    overtime_hours = duration - contract_hours.daily_hours
                    create_schedule_incident(
                        pending_record, 
                        CheckPointIncidentType.OVERTIME,
                        f"Jornada con {overtime_hours:.2f} horas extra sobre el límite diario de {contract_hours.daily_hours} horas"
                    )
            
            # 3. Actualizar estado del empleado
            employee.is_on_shift = False
            db.session.add(employee)
            
            # Confirmar transacción
            db.session.commit()
            
            # Log detallado post-transacción
            print(f"✅ CHECKOUT: Empleado ID {employee.id} - Estado: {old_status} → {employee.is_on_shift}")
            print(f"   Registro ID: {record_id}, Salida: {pending_record.check_out_time}")
            
            # Notificar al usuario
            flash(f'Jornada finalizada correctamente para {employee.first_name} {employee.last_name}. Por favor, firma tu registro.', 'success')
            
            # Redirigir directamente a la página de firma
            return redirect(url_for('checkpoints.checkpoint_record_signature', id=pending_record.id))
        
        # CASO 2: Check-in (iniciar jornada)
        elif action == 'checkin' and not pending_record:
            # Obtener datos actuales para logging
            old_status = employee.is_on_shift
            
            # Actualizar en una transacción
            db.session.begin_nested()
            
            # 1. Crear nuevo registro de fichaje
            checkin_time = get_current_time()
            new_record = CheckPointRecord(
                employee_id=employee.id,
                checkpoint_id=checkpoint_id,
                check_in_time=checkin_time
            )
            db.session.add(new_record)
            # Hacemos flush para que new_record obtenga un ID
            db.session.flush()
            
            # Guardar siempre el registro original al iniciar jornada
            from models_checkpoints import CheckPointOriginalRecord
            
            # Datos originales en el momento de iniciar la jornada
            original_record = CheckPointOriginalRecord(
                record_id=new_record.id,  # Ahora new_record.id ya tiene un valor
                original_check_in_time=checkin_time,
                original_check_out_time=None,
                original_signature_data=None,
                original_has_signature=False,
                original_notes=None,
                adjustment_reason="Registro original al iniciar fichaje"
            )
            db.session.add(original_record)
            
            # 2. Actualizar estado del empleado
            employee.is_on_shift = True
            db.session.add(employee)
            
            # Confirmar transacción
            db.session.commit()
            db.session.refresh(new_record)
            
            # Log detallado post-transacción
            print(f"✅ CHECKIN: Empleado ID {employee.id} - Estado: {old_status} → {employee.is_on_shift}")
            print(f"   Nuevo registro ID: {new_record.id}, Entrada: {new_record.check_in_time}")
            
            # Notificar al usuario
            flash(f'Jornada iniciada correctamente para {employee.first_name} {employee.last_name}', 'success')
            
            # Redirigir a detalles del registro, mostrando directamente la pantalla de finalizar jornada
            return redirect(url_for('checkpoints.record_details', id=new_record.id))
        
        # CASO 3: Estado inconsistente
        else:
            # Detectar y reportar inconsistencias específicamente
            if pending_record and action == 'checkin':
                flash('El empleado ya tiene una entrada activa sin salida registrada.', 'warning')
            elif not pending_record and action == 'checkout':
                flash('El empleado no tiene ninguna entrada activa para registrar salida.', 'warning')
            else:
                flash('Estado inconsistente. Contacte al administrador.', 'danger')
                
            # Intentar corregir inconsistencias entre is_on_shift y pending_record
            fix_employee_state_inconsistency(employee, pending_record)
            
            # Redirigir a la página principal del punto de fichaje
            return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    except Exception as e:
        # Deshacer cambios en caso de error
        db.session.rollback()
        
        # Log detallado del error
        error_details = f"ERROR en fichaje: {str(e)}"
        print(f"❌ {error_details}")
        print(f"   Acción: {action}, Empleado: {employee.id}, Checkpoint: {checkpoint_id}")
        
        # Notificar al usuario
        flash(f'Error al procesar el fichaje: {str(e)}', 'danger')
        
        # Redirigir a la página de PIN
        return redirect(url_for('checkpoints.employee_pin', id=employee.id))


```

#### `fix_employee_state_inconsistency` (línea 1484)

**Docstring:**
```
Función auxiliar para detectar y corregir inconsistencias entre 
el estado is_on_shift y los registros de fichaje
```

**Código:**
```python
def fix_employee_state_inconsistency(employee, pending_record):
    """
    Función auxiliar para detectar y corregir inconsistencias entre 
    el estado is_on_shift y los registros de fichaje
    """
    try:
        # Caso 1: Tiene registro pendiente pero is_on_shift=False
        if pending_record and not employee.is_on_shift:
            # Corregir: Poner is_on_shift=True
            employee.is_on_shift = True
            db.session.add(employee)
            db.session.commit()
            print(f"⚠️ CORREGIDO: Empleado {employee.id} tiene registro pendiente pero is_on_shift=False")
            
        # Caso 2: No tiene registro pendiente pero is_on_shift=True
        elif not pending_record and employee.is_on_shift:
            # Corregir: Poner is_on_shift=False
            employee.is_on_shift = False
            db.session.add(employee)
            db.session.commit()
            print(f"⚠️ CORREGIDO: Empleado {employee.id} no tiene registro pendiente pero is_on_shift=True")
            
    except Exception as e:
        db.session.rollback()
        print(f"❌ ERROR corrigiendo inconsistencia: {str(e)}")


@checkpoints_bp.route('/record/<int:id>')
@checkpoint_required
```

#### `record_details` (línea 1513)

**Docstring:**
```
Muestra los detalles de un registro recién creado
```

**Código:**
```python
def record_details(id):
    """Muestra los detalles de un registro recién creado"""
    record = CheckPointRecord.query.get_or_404(id)
    
    # Verificar que el registro pertenece al punto de fichaje actual
    checkpoint_id = session.get('checkpoint_id')
    if record.checkpoint_id != checkpoint_id:
        flash('Registro no válido para este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    # Obtener el checkpoint para la plantilla
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    
    # Log para depuración
    print(f"Mostrando registro: ID {record.id}, Empleado {record.employee.first_name} {record.employee.last_name}, " +
          f"Entrada: {record.check_in_time}, Salida: {record.check_out_time}, Estado: {record.employee.is_on_shift}")
    
    return render_template('checkpoints/record_details.html', record=record, checkpoint=checkpoint)


@checkpoints_bp.route('/record/<int:id>/checkout', methods=['POST'])
@checkpoint_required
```

#### `record_checkout` (línea 1535)

**Docstring:**
```
Registra la salida para un fichaje pendiente desde la pantalla de detalles
```

**Código:**
```python
def record_checkout(id):
    """Registra la salida para un fichaje pendiente desde la pantalla de detalles"""
    record = CheckPointRecord.query.get_or_404(id)
    
    # Verificaciones de seguridad
    checkpoint_id = session.get('checkpoint_id')
    if record.checkpoint_id != checkpoint_id:
        flash('Registro no válido para este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    # Verificar que el registro no tenga ya una salida
    if record.check_out_time:
        flash('Este fichaje ya tiene registrada una salida.', 'warning')
        return redirect(url_for('checkpoints.record_details', id=record.id))
    
    # Obtener el empleado asociado al registro
    employee = record.employee
    
    try:
        # Obtener estado actual para logging
        old_status = employee.is_on_shift
        
        # Usar una transacción anidada para mayor seguridad
        db.session.begin_nested()
        
        # 1. Actualizar el registro con la hora de salida
        current_time = get_current_time()
        record.check_out_time = current_time
        db.session.add(record)
        db.session.flush()  # Aseguramos que record tenga todos sus campos actualizados
        
        # Guardar siempre el registro original primero al hacer checkout
        from models_checkpoints import CheckPointOriginalRecord
        
        # Capturar los valores reales antes de cualquier ajuste
        # Importante: Guardamos la hora exacta que se introdujo al inicio de la jornada
        original_checkin = record.check_in_time  # Esta es la hora original de entrada
        original_checkout = current_time  # Esta es la hora real de salida antes de ajustes
        
        # Crear un registro del estado original con las horas reales
        original_record = CheckPointOriginalRecord(
            record_id=record.id,
            original_check_in_time=original_checkin,
            original_check_out_time=original_checkout,
            original_signature_data=record.signature_data,
            original_has_signature=record.has_signature,
            original_notes=record.notes,
            adjustment_reason="Registro original al finalizar fichaje desde pantalla de detalles"
        )
        db.session.add(original_record)
        
        # 2. Verificar configuración de horas de contrato
        contract_hours = EmployeeContractHours.query.filter_by(employee_id=employee.id).first()
        if contract_hours:
            # Verificar si se debe ajustar el horario según configuración
            adjusted_checkin, adjusted_checkout = contract_hours.calculate_adjusted_hours(
                original_checkin, original_checkout
            )
            
            # Verificar si hay ajustes a realizar
            needs_adjustment = (adjusted_checkin and adjusted_checkin != original_checkin) or \
                              (adjusted_checkout and adjusted_checkout != original_checkout)
            
            # Si hay ajustes, actualizar el registro
            if needs_adjustment:
                # Actualizar el registro con los valores ajustados
                if adjusted_checkin and adjusted_checkin != original_checkin:
                    record.check_in_time = adjusted_checkin
                    # Marcar con R en lugar de crear incidencia
                    record.notes = (record.notes or "") + f" [R] Hora entrada ajustada de {original_checkin.strftime('%H:%M')} a {adjusted_checkin.strftime('%H:%M')}"
                
                if adjusted_checkout and adjusted_checkout != original_checkout:
                    record.check_out_time = adjusted_checkout
                    # Marcar con R en lugar de crear incidencia
                    record.notes = (record.notes or "") + f" [R] Hora salida ajustada de {original_checkout.strftime('%H:%M')} a {adjusted_checkout.strftime('%H:%M')}"
                
                # Marcar como ajustado
                record.adjusted = True
                
                # Actualizar la razón del ajuste en el registro original
                original_record.adjustment_reason = "Ajuste automático por límite de horas de contrato"
                db.session.add(original_record)
            
            # Verificar si hay horas extra
            duration = (record.check_out_time - record.check_in_time).total_seconds() / 3600
            if contract_hours.is_overtime(duration):
                overtime_hours = duration - contract_hours.daily_hours
                create_schedule_incident(
                    record, 
                    CheckPointIncidentType.OVERTIME,
                    f"Jornada con {overtime_hours:.2f} horas extra sobre el límite diario de {contract_hours.daily_hours} horas"
                )
        
        # 3. Actualizar el estado del empleado
        employee.is_on_shift = False
        db.session.add(employee)
        
        # Confirmar la transacción
        db.session.commit()
        
        # Log detallado post-transacción
        print(f"✅ CHECKOUT (pantalla detalles): Empleado ID {employee.id} - Estado: {old_status} → {employee.is_on_shift}")
        print(f"   Registro ID: {record.id}, Entrada: {record.check_in_time}, Salida: {record.check_out_time}")
        
        # Notificar al usuario
        flash(f'Jornada finalizada correctamente para {employee.first_name} {employee.last_name}. Por favor, firma tu registro.', 'success')
        
        # Redirigir directamente a la página de firma
        return redirect(url_for('checkpoints.checkpoint_record_signature', id=record.id))
    except Exception as e:
        # Rollback en caso de error
        db.session.rollback()
        
        # Log detallado del error
        error_details = f"ERROR en checkout desde detalles: {str(e)}"
        print(f"❌ {error_details}")
        print(f"   Registro ID: {record.id}, Empleado ID: {employee.id}")
        
        # Notificar al usuario
        flash(f'Error al registrar la salida: {str(e)}', 'danger')
        
        # En caso de error, redirigir a la página de detalles
        return redirect(url_for('checkpoints.record_details', id=record.id))


@checkpoints_bp.route('/record/<int:id>/signature_pad', methods=['GET', 'POST'])
@checkpoint_required
```

#### `checkpoint_record_signature` (línea 1662)

**Docstring:**
```
Permite al empleado firmar un registro de fichaje desde el punto de fichaje
```

**Código:**
```python
def checkpoint_record_signature(id):
    """Permite al empleado firmar un registro de fichaje desde el punto de fichaje"""
    record = CheckPointRecord.query.get_or_404(id)
    
    # Verificaciones de seguridad
    checkpoint_id = session.get('checkpoint_id')
    if record.checkpoint_id != checkpoint_id:
        flash('Registro no válido para este punto de fichaje.', 'danger')
        return redirect(url_for('checkpoints.checkpoint_dashboard'))
    
    # Verificar que el registro tenga salida (no se puede firmar sin checkout)
    if not record.check_out_time:
        flash('No se puede firmar un registro sin hora de salida.', 'warning')
        return redirect(url_for('checkpoints.record_details', id=record.id))
    
    # Verificar que no esté ya firmado
    if record.has_signature:
        flash('Este registro ya ha sido firmado anteriormente.', 'info')
        return redirect(url_for('checkpoints.record_details', id=record.id))
    
    # Inicializar formulario
    form = SignaturePadForm()
    form.record_id.data = record.id
    
    # Procesar el formulario si es submit
    if form.validate_on_submit():
        try:
            # Usar una transacción anidada para mayor seguridad
            db.session.begin_nested()
            
            # Guardar los datos de la firma
            signature_data = form.signature_data.data
            
            # Actualizar el registro
            record.signature_data = signature_data
            record.has_signature = True
            
            # Guardar cambios
            db.session.add(record)
            db.session.commit()
            
            # Log detallado post-transacción
            print(f"✅ FIRMA REGISTRADA: Registro ID {record.id}, Empleado ID {record.employee_id}")
            
            # Notificar al usuario
            flash('Firma registrada correctamente.', 'success')
            
            # Redirigir a detalles del registro
            return redirect(url_for('checkpoints.record_details', id=record.id))
        except Exception as e:
            # Rollback en caso de error
            db.session.rollback()
            
            # Log detallado del error
            error_details = f"ERROR al guardar la firma: {str(e)}"
            print(f"❌ {error_details}")
            print(f"   Registro ID: {record.id}, Empleado ID: {record.employee_id}")
            
            # Notificar al usuario
            flash(f'Error al guardar la firma: {str(e)}', 'danger')
    
    # Renderizar la plantilla con los datos necesarios
    return render_template(
        'checkpoints/signature_pad.html', 
        form=form,
        record=record
    )


@checkpoints_bp.route('/daily-report')
@checkpoint_required
```

#### `daily_report` (línea 1733)

**Docstring:**
```
Muestra un informe de fichajes del día actual
```

**Código:**
```python
def daily_report():
    """Muestra un informe de fichajes del día actual"""
    checkpoint_id = session.get('checkpoint_id')
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    
    # Obtener fecha actual
    today = date.today()
    
    # Obtener todos los registros del día para este punto de fichaje
    records = CheckPointRecord.query.filter(
        CheckPointRecord.checkpoint_id == checkpoint_id,
        func.date(CheckPointRecord.check_in_time) == today
    ).order_by(CheckPointRecord.check_in_time).all()
    
    # Obtener empleados de la empresa
    company_id = checkpoint.company_id
    all_employees = Employee.query.filter_by(company_id=company_id, is_active=True).all()
    
    # Obtener IDs de empleados que han fichado hoy
    checked_in_ids = [record.employee_id for record in records]
    
    # Obtener empleados que no han fichado hoy
    missing_employees = [emp for emp in all_employees if emp.id not in checked_in_ids]
    
    # Contar registros pendientes de checkout
    pending_checkout = sum(1 for record in records if record.check_out_time is None)
    
    # Preparar estadísticas
    stats = {
        'total_employees': len(all_employees),
        'checked_in': len(checked_in_ids),
        'pending_checkout': pending_checkout
    }
    
    return render_template('checkpoints/daily_report.html', 
                         checkpoint=checkpoint,
                         records=records,
                         today=today,
                         stats=stats,
                         missing_employees=missing_employees)


@checkpoints_bp.route('/api/company-employees', methods=['GET'])
@checkpoint_required
```

#### `get_company_employees` (línea 1777)

**Docstring:**
```
Devuelve la lista de empleados de la empresa en formato JSON
```

**Código:**
```python
def get_company_employees():
    """Devuelve la lista de empleados de la empresa en formato JSON"""
    # Obtenemos la compañía del checkpoint actual en lugar de usar session['company_id']
    checkpoint_id = session.get('checkpoint_id')
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    company_id = checkpoint.company_id
    
    if not company_id:
        return jsonify([])
    
    # Obtenemos todos los empleados de la empresa, sin filtrar por is_active
    # para asegurar que coincida con la vista de empleados
    employees = Employee.query.filter_by(
        company_id=company_id
    ).order_by(Employee.first_name, Employee.last_name).all()
    
    employees_data = []
    for employee in employees:
        # Verificar si el empleado tiene un fichaje pendiente (sin check_out)
        pending_record = CheckPointRecord.query.filter_by(
            employee_id=employee.id,
            checkpoint_id=session.get('checkpoint_id'),
            check_out_time=None
        ).first()
        
        employees_data.append({
            'id': employee.id,
            'name': f"{employee.first_name} {employee.last_name}",
            'position': employee.position,
            'has_pending_record': pending_record is not None,
            'is_on_shift': employee.is_on_shift,
            'dni_last_digits': ''.join(c for c in employee.dni if c.isdigit())[-4:] if len(''.join(c for c in employee.dni if c.isdigit())) >= 4 else ''.join(c for c in employee.dni if c.isdigit())
        })
    
    return jsonify(employees_data)


@checkpoints_bp.route('/api/validate-pin', methods=['POST'])
@checkpoint_required
```

#### `validate_pin` (línea 1816)

**Docstring:**
```
Validar el PIN del empleado mediante AJAX
```

**Código:**
```python
def validate_pin():
    """Validar el PIN del empleado mediante AJAX"""
    if not request.is_json:
        return jsonify({"success": False, "message": "Se requiere un JSON"}), 400
        
    # Obtener datos de la solicitud
    data = request.get_json()
    employee_id = data.get('employee_id')
    pin = data.get('pin')
    
    if not employee_id or not pin:
        return jsonify({"success": False, "message": "Falta ID de empleado o PIN"}), 400
        
    # Obtener el checkpoint activo desde la sesión
    checkpoint_id = session.get('checkpoint_id')
    if not checkpoint_id:
        return jsonify({"success": False, "message": "Sesión de punto de fichaje no iniciada."}), 401
    
    # Verificar que el empleado existe y pertenece a la empresa del checkpoint
    employee = Employee.query.get(employee_id)
    if not employee:
        return jsonify({"success": False, "message": "Empleado no encontrado."}), 404
        
    checkpoint = CheckPoint.query.get(checkpoint_id)
    if not checkpoint:
        return jsonify({"success": False, "message": "Punto de fichaje no encontrado."}), 404
    
    if employee.company_id != checkpoint.company_id:
        return jsonify({"success": False, "message": "Empleado no pertenece a la empresa."}), 403
    
    # Verificar el PIN (últimos 4 dígitos del DNI, ignorando la letra final en DNI español)
    # Filtrar solo los dígitos del DNI
    dni_digits = ''.join(c for c in employee.dni if c.isdigit())
    pin_from_dni = dni_digits[-4:] if len(dni_digits) >= 4 else dni_digits
    
    if pin == pin_from_dni:
        # Verificar si hay un registro pendiente
        pending_record = CheckPointRecord.query.filter_by(
            employee_id=employee.id,
            checkpoint_id=checkpoint_id,
            check_out_time=None
        ).order_by(CheckPointRecord.check_in_time.desc()).first()
        
        action = "checkout" if pending_record else "checkin"
        
        return jsonify({
            "success": True, 
            "action": action,
            "is_on_shift": employee.is_on_shift
        })
    else:
        return jsonify({"success": False, "message": "PIN incorrecto"}), 401


# Endpoint para procesar auto-checkouts
@checkpoints_bp.route('/auto_checkout', methods=['GET', 'POST'])
@login_required
@checkpoint_required
```

#### `trigger_auto_checkout` (línea 1874)

**Docstring:**
```
Endpoint para informar que el sistema de auto-checkout ha sido eliminado
```

**Código:**
```python
def trigger_auto_checkout():
    """Endpoint para informar que el sistema de auto-checkout ha sido eliminado"""
    # Informar que la funcionalidad ha sido eliminada
    return jsonify({
        'success': False,
        'processed': 0,
        'message': 'El sistema de auto-checkout ha sido eliminado completamente. Ya no se realizarán cierres automáticos de fichajes.'
    }), 404

# Integrar el blueprint en la aplicación principal
@checkpoints_bp.route('/check_credentials', methods=['GET'])
```

#### `check_credentials` (línea 1885)

**Docstring:**
```
Endpoint temporal para comprobar credenciales
```

**Código:**
```python
def check_credentials():
    """Endpoint temporal para comprobar credenciales"""
    username = request.args.get('username', '')
    password = request.args.get('password', '')
    
    checkpoint = CheckPoint.query.filter_by(username=username).first()
    
    if checkpoint:
        is_valid = checkpoint.verify_password(password)
        return jsonify({
            'username': username,
            'checkpoint_id': checkpoint.id,
            'checkpoint_name': checkpoint.name,
            'password_valid': is_valid,
            'status': checkpoint.status.value
        })
    else:
        return jsonify({
            'username': username,
            'error': 'Usuario no encontrado'
        })

```

#### `init_app` (línea 1907)

**Código:**
```python
def init_app(app):
    # Registrar el blueprint
    app.register_blueprint(checkpoints_bp)
```


### routes_checkpoints_new.py

#### `admin_required` (línea 29)

**Código:**
```python
def admin_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or current_user.role != UserRole.ADMIN:
            flash('Acceso denegado. Se requieren permisos de administrador.', 'danger')
            return redirect(url_for('index'))
        return f(*args, **kwargs)
    return decorated_function


```

#### `decorated_function` (línea 31)

**Código:**
```python
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or current_user.role != UserRole.ADMIN:
            flash('Acceso denegado. Se requieren permisos de administrador.', 'danger')
            return redirect(url_for('index'))
        return f(*args, **kwargs)
    return decorated_function


```

#### `manager_required` (línea 39)

**Código:**
```python
def manager_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or (
            current_user.role != UserRole.ADMIN and 
            current_user.role != UserRole.GERENTE
        ):
            flash('Acceso denegado. Se requieren permisos de gerente o administrador.', 'danger')
            return redirect(url_for('index'))
        return f(*args, **kwargs)
    return decorated_function


```

#### `decorated_function` (línea 41)

**Código:**
```python
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or (
            current_user.role != UserRole.ADMIN and 
            current_user.role != UserRole.GERENTE
        ):
            flash('Acceso denegado. Se requieren permisos de gerente o administrador.', 'danger')
            return redirect(url_for('index'))
        return f(*args, **kwargs)
    return decorated_function


```

#### `checkpoint_required` (línea 52)

**Código:**
```python
def checkpoint_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'checkpoint_id' not in session:
            flash('Debe iniciar sesión como punto de fichaje.', 'warning')
            return redirect(url_for('checkpoints_slug.login'))
        return f(*args, **kwargs)
    return decorated_function

# Resto del código...

@checkpoints_bp.route('/company/<slug>/rrrrrr', methods=['GET'])
@login_required
@admin_required
```

#### `decorated_function` (línea 54)

**Código:**
```python
    def decorated_function(*args, **kwargs):
        if 'checkpoint_id' not in session:
            flash('Debe iniciar sesión como punto de fichaje.', 'warning')
            return redirect(url_for('checkpoints_slug.login'))
        return f(*args, **kwargs)
    return decorated_function

# Resto del código...

```

#### `view_original_records` (línea 66)

**Docstring:**
```
Página secreta para ver los registros originales antes de ajustes de una empresa específica
```

**Código:**
```python
def view_original_records(slug):
    """Página secreta para ver los registros originales antes de ajustes de una empresa específica"""
    from models_checkpoints import CheckPointOriginalRecord
    
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Esta página es solo para administradores
    page = request.args.get('page', 1, type=int)
    start_date = request.args.get('start_date')
    end_date = request.args.get('end_date')
    employee_id = request.args.get('employee_id', type=int)
    show_all = request.args.get('show_all', 'false')
    
    # Obtener los IDs de los empleados de esta empresa
    employee_ids = db.session.query(Employee.id).filter_by(company_id=company_id).all()
    employee_ids = [e[0] for e in employee_ids]
    
    # Construir la consulta base con filtro de empresa
    query = db.session.query(
        CheckPointOriginalRecord, 
        CheckPointRecord, 
        Employee
    ).join(
        CheckPointRecord, 
        CheckPointOriginalRecord.record_id == CheckPointRecord.id
    ).join(
        Employee,
        CheckPointRecord.employee_id == Employee.id
    ).filter(
        Employee.company_id == company_id
    )
    
    # Filtrar solo registros completos (con hora de salida)
    query = query.filter(CheckPointOriginalRecord.original_check_out_time.isnot(None))
    
    # Aplicar filtros si los hay
    if start_date:
        try:
            start_date = datetime.strptime(start_date, '%Y-%m-%d').date()
            query = query.filter(func.date(CheckPointOriginalRecord.original_check_in_time) >= start_date)
        except ValueError:
            pass
    
    if end_date:
        try:
            end_date = datetime.strptime(end_date, '%Y-%m-%d').date()
            query = query.filter(func.date(CheckPointOriginalRecord.original_check_in_time) <= end_date)
        except ValueError:
            pass
    
    if employee_id:
        query = query.filter(Employee.id == employee_id)
    
    # Ejecutar la consulta y obtener todos los resultados
    all_records = query.order_by(
        CheckPointOriginalRecord.adjusted_at.desc()
    ).all()
    
    # Filtrar registros duplicados (mismo empleado, misma fecha) manteniendo solo los completos
    filtered_records = []
    seen_records = set()  # Para rastrear combinaciones empleado-fecha ya procesadas
    
    for original, record, employee in all_records:
        # Crear una clave única para identificar registros del mismo empleado en el mismo día
        record_date = original.original_check_in_time.date()
        record_key = (employee.id, record_date)
        
        # Solo incluir cada combinación empleado-fecha una vez
        # Y asegurar que tenga hora de salida completa
        if record_key not in seen_records and original.original_check_out_time is not None:
            filtered_records.append((original, record, employee))
            seen_records.add(record_key)
    
    # Paginar los resultados filtrados manualmente
    total_records = len(filtered_records)
    per_page = 20
    total_pages = (total_records + per_page - 1) // per_page  # Redondear hacia arriba
    
    # Validar número de página
    if page < 1:
        page = 1
    if page > total_pages and total_pages > 0:
        page = total_pages
    
    # Calcular índices de inicio y fin para la página actual
    start_idx = (page - 1) * per_page
    end_idx = min(start_idx + per_page, total_records)
    
    # Obtener los registros para la página actual
    page_records = filtered_records[start_idx:end_idx] if filtered_records else []
    
    # Crear un objeto similar a la paginación de SQLAlchemy para usar en la plantilla
    class Pagination:
        def __init__(self, items, page, per_page, total):
            self.items = items
            self.page = page
            self.per_page = per_page
            self.total = total
            
        @property
        def pages(self):
            return (self.total + self.per_page - 1) // self.per_page
            
        @property
        def has_prev(self):
            return self.page > 1
            
        @property
        def has_next(self):
            return self.page < self.pages
            
        @property
        def prev_num(self):
            return self.page - 1 if self.has_prev else None
            
        @property
        def next_num(self):
            return self.page + 1 if self.has_next else None
    
    # Crear objeto de paginación
    paginated_records = Pagination(page_records, page, per_page, total_records)
    
    # Obtener la lista de empleados para el filtro (solo de esta empresa)
    employees = Employee.query.filter_by(company_id=company_id, is_active=True).order_by(Employee.first_name).all()
    
    # Si se solicita exportación
    export_format = request.args.get('export')
    if export_format == 'pdf':
        return export_original_records_pdf(filtered_records, start_date, end_date, company)
    
    return render_template(
        'checkpoints/original_records.html',
        original_records=paginated_records,
        employees=employees,
        company=company,
        company_id=company_id,
        filters={
            'start_date': start_date.strftime('%Y-%m-%d') if isinstance(start_date, date) else None,
            'end_date': end_date.strftime('%Y-%m-%d') if isinstance(end_date, date) else None,
            'employee_id': employee_id
        },
        show_all=show_all,
        title=f"Registros Originales de {company.name if company else ''} (Antes de Ajustes)"
    )

@checkpoints_bp.route('/company/<slug>/rrrrrr/edit/<int:id>', methods=['GET', 'POST'])
@login_required
@admin_required
```

#### `__init__` (línea 171)

**Código:**
```python
        def __init__(self, items, page, per_page, total):
            self.items = items
            self.page = page
            self.per_page = per_page
            self.total = total
            
        @property
```

#### `pages` (línea 178)

**Código:**
```python
        def pages(self):
            return (self.total + self.per_page - 1) // self.per_page
            
        @property
```

#### `has_prev` (línea 182)

**Código:**
```python
        def has_prev(self):
            return self.page > 1
            
        @property
```

#### `has_next` (línea 186)

**Código:**
```python
        def has_next(self):
            return self.page < self.pages
            
        @property
```

#### `prev_num` (línea 190)

**Código:**
```python
        def prev_num(self):
            return self.page - 1 if self.has_prev else None
            
        @property
```

#### `next_num` (línea 194)

**Código:**
```python
        def next_num(self):
            return self.page + 1 if self.has_next else None
    
    # Crear objeto de paginación
```

#### `edit_original_record` (línea 226)

**Docstring:**
```
Edita un registro original
```

**Código:**
```python
def edit_original_record(slug, id):
    """Edita un registro original"""
    from models_checkpoints import CheckPointOriginalRecord
    from flask_wtf import FlaskForm
    from wtforms import StringField, TimeField, TextAreaField, SubmitField
    from wtforms.validators import DataRequired, Optional, Length
    
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Obtener el registro original
    original_record = CheckPointOriginalRecord.query.get_or_404(id)
    record = CheckPointRecord.query.get_or_404(original_record.record_id)
    
    # Verificar que el registro pertenece a esta empresa
    if record.employee.company_id != company_id:
        flash('Registro no encontrado para esta empresa.', 'warning')
        return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
    
    # Crear un formulario para editar el registro
    class EditOriginalRecordForm(FlaskForm):
        original_check_in_date = StringField('Fecha de entrada original', validators=[DataRequired()])
        original_check_in_time = TimeField('Hora de entrada original', validators=[DataRequired()])
        original_check_out_time = TimeField('Hora de salida original', validators=[Optional()])
        notes = TextAreaField('Notas', validators=[Optional(), Length(max=500)])
        submit = SubmitField('Guardar cambios')
    
    form = EditOriginalRecordForm()
    
    # Pre-llenar el formulario con los datos actuales
    if request.method == 'GET':
        form.original_check_in_date.data = original_record.original_check_in_time.strftime('%Y-%m-%d')
        form.original_check_in_time.data = original_record.original_check_in_time.time()
        if original_record.original_check_out_time:
            form.original_check_out_time.data = original_record.original_check_out_time.time()
        form.notes.data = original_record.original_notes
    
    # Procesar el formulario
    if form.validate_on_submit():
        try:
            # Obtener fecha y hora de entrada
            check_in_date = datetime.strptime(form.original_check_in_date.data, '%Y-%m-%d').date()
            original_record.original_check_in_time = datetime.combine(check_in_date, form.original_check_in_time.data)
            
            # Obtener hora de salida si está presente
            if form.original_check_out_time.data:
                # Si la hora de salida es menor que la de entrada, asumimos que es del día siguiente
                if form.original_check_out_time.data < form.original_check_in_time.data:
                    check_out_date = check_in_date + timedelta(days=1)
                else:
                    check_out_date = check_in_date
                    
                original_record.original_check_out_time = datetime.combine(check_out_date, form.original_check_out_time.data)
            else:
                original_record.original_check_out_time = None
            
            # Actualizar notas
            original_record.original_notes = form.notes.data
            
            # Guardar cambios
            db.session.commit()
            flash('Registro original actualizado con éxito.', 'success')
            return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
            
        except Exception as e:
            db.session.rollback()
            flash(f'Error al actualizar el registro: {str(e)}', 'danger')
    
    return render_template('checkpoints/edit_original_record.html', 
                          form=form, 
                          original_record=original_record,
                          record=record,
                          company=company,
                          company_id=company_id)

@checkpoints_bp.route('/company/<slug>/rrrrrr/restore/<int:id>', methods=['GET'])
@login_required
@admin_required
```

#### `restore_original_record` (línea 315)

**Docstring:**
```
Restaura los valores originales en el registro actual
```

**Código:**
```python
def restore_original_record(slug, id):
    """Restaura los valores originales en el registro actual"""
    from models_checkpoints import CheckPointOriginalRecord
    
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Obtener el registro original y el registro actual
    original_record = CheckPointOriginalRecord.query.get_or_404(id)
    record = CheckPointRecord.query.get_or_404(original_record.record_id)
    
    # Verificar que el registro pertenece a esta empresa
    if record.employee.company_id != company_id:
        flash('Registro no encontrado para esta empresa.', 'warning')
        return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
    
    try:
        # Restaurar valores originales
        record.check_in_time = original_record.original_check_in_time
        record.check_out_time = original_record.original_check_out_time
        record.signature_data = original_record.original_signature_data
        record.has_signature = original_record.original_has_signature
        record.notes = original_record.original_notes
        
        # Actualizar razón de ajuste
        record.adjustment_reason = "Restaurado a valores originales"
        
        # Guardar cambios
        db.session.commit()
        flash('Registro restaurado a valores originales con éxito.', 'success')
    except Exception as e:
        db.session.rollback()
        flash(f'Error al restaurar el registro: {str(e)}', 'danger')
    
    return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))

@checkpoints_bp.route('/company/<slug>/rrrrrr/delete/<int:id>', methods=['GET'])
@login_required
@admin_required
```

#### `delete_original_record` (línea 365)

**Docstring:**
```
Elimina un registro original
```

**Código:**
```python
def delete_original_record(slug, id):
    """Elimina un registro original"""
    from models_checkpoints import CheckPointOriginalRecord
    
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Obtener el registro original
    original_record = CheckPointOriginalRecord.query.get_or_404(id)
    record = CheckPointRecord.query.get_or_404(original_record.record_id)
    
    # Verificar que el registro pertenece a esta empresa
    if record.employee.company_id != company_id:
        flash('Registro no encontrado para esta empresa.', 'warning')
        return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
    
    try:
        # Eliminar el registro
        db.session.delete(original_record)
        db.session.commit()
        flash('Registro original eliminado con éxito.', 'success')
    except Exception as e:
        db.session.rollback()
        flash(f'Error al eliminar el registro: {str(e)}', 'danger')
    
    return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))

@checkpoints_bp.route('/company/<slug>/rrrrrr/export', methods=['GET'])
@login_required
@admin_required
```

#### `export_original_records` (línea 406)

**Docstring:**
```
Exporta los registros originales a PDF
```

**Código:**
```python
def export_original_records(slug):
    """Exporta los registros originales a PDF"""
    from models_checkpoints import CheckPointOriginalRecord
    
    # Buscar la empresa por slug
    companies = Company.query.all()
    company = None
    company_id = None
    
    for comp in companies:
        if slugify(comp.name) == slug:
            company = comp
            company_id = comp.id
            break
    
    if not company:
        abort(404)
    
    # Obtener parámetros de filtro
    start_date = request.args.get('start_date')
    end_date = request.args.get('end_date')
    employee_id = request.args.get('employee_id', type=int)
    
    # Construir la consulta con filtro de empresa
    query = db.session.query(
        CheckPointOriginalRecord, 
        CheckPointRecord, 
        Employee
    ).join(
        CheckPointRecord, 
        CheckPointOriginalRecord.record_id == CheckPointRecord.id
    ).join(
        Employee,
        CheckPointRecord.employee_id == Employee.id
    ).filter(
        Employee.company_id == company_id,
        # Filtrar solo registros completos (con hora de entrada y salida)
        CheckPointRecord.check_out_time.isnot(None),
        CheckPointOriginalRecord.original_check_out_time.isnot(None)
    )
    
    # Aplicar filtros
    if start_date:
        try:
            start_date = datetime.strptime(start_date, '%Y-%m-%d').date()
            query = query.filter(func.date(CheckPointOriginalRecord.original_check_in_time) >= start_date)
        except ValueError:
            start_date = None
    
    if end_date:
        try:
            end_date = datetime.strptime(end_date, '%Y-%m-%d').date()
            query = query.filter(func.date(CheckPointOriginalRecord.original_check_in_time) <= end_date)
        except ValueError:
            end_date = None
    
    if employee_id:
        query = query.filter(Employee.id == employee_id)
    
    # Ordenar por empleado y fecha
    records = query.order_by(
        Employee.last_name,
        Employee.first_name,
        CheckPointOriginalRecord.original_check_in_time
    ).all()
    
    if not records:
        flash('No se encontraron registros para los filtros seleccionados', 'warning')
        return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
    
    # Filtrar registros duplicados (mismo empleado, misma fecha, mismo periodo)
    # Esto elimina casos en los que un registro puede aparecer como parcial y completo
    filtered_records = []
    seen_records = set()  # Para rastrear registros ya procesados
    
    for original, record, employee in records:
        # Crear una clave única para identificar registros duplicados
        record_key = (
            employee.id,
            original.original_check_in_time.strftime('%Y-%m-%d'),
            original.original_check_in_time.strftime('%H:%M'),
            original.original_check_out_time.strftime('%H:%M') if original.original_check_out_time else None
        )
        
        # Solo incluir registros que no hemos visto antes
        if record_key not in seen_records:
            filtered_records.append((original, record, employee))
            seen_records.add(record_key)
    
    if not filtered_records:
        flash('No se encontraron registros completos para los filtros seleccionados', 'warning')
        return redirect(url_for('checkpoints_slug.view_original_records', slug=slug))
    
    # Generar PDF con los registros filtrados
    return export_original_records_pdf(filtered_records, start_date, end_date, company)

```

#### `export_original_records_pdf` (línea 502)

**Docstring:**
```
Genera un PDF con los registros originales agrupados por semanas (lunes a domingo)
```

**Código:**
```python
def export_original_records_pdf(records, start_date=None, end_date=None, company=None):
    """Genera un PDF con los registros originales agrupados por semanas (lunes a domingo)"""
    from fpdf import FPDF
    from tempfile import NamedTemporaryFile
    import os
    from datetime import datetime, timedelta
    
    # Crear un archivo temporal para guardar el PDF
    pdf_file = NamedTemporaryFile(delete=False, suffix='.pdf')
    pdf_file.close()
    
    # Función auxiliar para obtener el lunes de la semana de una fecha
    def get_week_start(date_obj):
        """Retorna la fecha del lunes de la semana a la que pertenece date_obj"""
        # weekday() retorna 0 para lunes, 6 para domingo
        days_to_subtract = date_obj.weekday()
        return date_obj - timedelta(days=days_to_subtract)
    
    # Función auxiliar para obtener el domingo de la semana de una fecha
    def get_week_end(date_obj):
        """Retorna la fecha del domingo de la semana a la que pertenece date_obj"""
        # weekday() retorna 0 para lunes, 6 para domingo
        days_to_add = 6 - date_obj.weekday()
        return date_obj + timedelta(days=days_to_add)
    
    # Crear un PDF personalizado
    class OriginalRecordsPDF(FPDF):
        def header(self):
            # Logo y título
            self.set_font('Arial', 'B', 15)
            title = 'Registro de Ajustes de Fichajes'
            if company:
                title = f'Registro de Ajustes de Fichajes - {company.name}'
            self.cell(0, 10, title, 0, 1, 'C')
            
            # Período
            if start_date and end_date:
                period = f"{start_date.strftime('%d/%m/%Y')} - {end_date.strftime('%d/%m/%Y')}"
            elif start_date:
                period = f"Desde {start_date.strftime('%d/%m/%Y')}"
            elif end_date:
                period = f"Hasta {end_date.strftime('%d/%m/%Y')}"
            else:
                period = "Todos los registros"
                
            self.set_font('Arial', '', 10)
            self.cell(0, 10, f'Período: {period}', 0, 1, 'C')
            
            # Encabezados de la tabla
            self.set_fill_color(200, 220, 255)
            self.set_font('Arial', 'B', 8)
            self.cell(40, 7, 'Empleado', 1, 0, 'C', True)
            self.cell(20, 7, 'Fecha', 1, 0, 'C', True)
            self.cell(15, 7, 'Ent. Orig.', 1, 0, 'C', True)
            self.cell(15, 7, 'Sal. Orig.', 1, 0, 'C', True)
            self.cell(15, 7, 'Ent. Mod.', 1, 0, 'C', True)
            self.cell(15, 7, 'Sal. Mod.', 1, 0, 'C', True)
            self.cell(20, 7, 'Ajustado Por', 1, 0, 'C', True)
            self.cell(50, 7, 'Motivo', 1, 1, 'C', True)
            
        def footer(self):
            # Posición a 1.5 cm del final
            self.set_y(-15)
            # Número de página
            self.set_font('Arial', 'I', 8)
            self.cell(0, 10, f'Página {self.page_no()}/{{nb}}', 0, 0, 'C')
    
    # Crear PDF
    pdf = OriginalRecordsPDF()
    pdf.alias_nb_pages()
    pdf.add_page()
    pdf.set_auto_page_break(auto=True, margin=15)
    
    # Llenar el PDF con los datos
    pdf.set_font('Arial', '', 8)
    
    # Ordenar registros por empleado, fecha y hora
    sorted_records = sorted(records, key=lambda x: (
        x[2].id,  # employee.id
        x[0].original_check_in_time.date(),  # date
        x[0].original_check_in_time.time()  # time
    ))
    
    # Preparar diccionarios para acumular totales por empleado y semana
    current_employee = None
    current_week_start = None
    week_hours_original = 0
    week_hours_adjusted = 0
    total_hours_original = 0
    total_hours_adjusted = 0
    
    # Estructurar registros por empleado
    employee_records = {}
    for original, record, employee in sorted_records:
        if employee.id not in employee_records:
            employee_records[employee.id] = {
                'employee': employee,
                'weeks': {}
            }
        
        # Obtener fecha de inicio de la semana (lunes)
        week_start = get_week_start(original.original_check_in_time.date())
        week_end = get_week_end(original.original_check_in_time.date())
        week_key = week_start.strftime('%Y-%m-%d')
        
        if week_key not in employee_records[employee.id]['weeks']:
            employee_records[employee.id]['weeks'][week_key] = {
                'start_date': week_start,
                'end_date': week_end,
                'records': [],
                'original_hours': 0,
                'adjusted_hours': 0
            }
        
        # Calcular horas trabajadas
        hours_original = 0
        if original.original_check_out_time and original.original_check_in_time:
            # Usando la función duration ya modificada para manejar correctamente los turnos nocturnos
            hours_original = original.duration()
            employee_records[employee.id]['weeks'][week_key]['original_hours'] += hours_original
        
        hours_adjusted = 0
        if record.check_out_time and record.check_in_time:
            # Usando la función duration ya modificada para manejar correctamente los turnos nocturnos
            hours_adjusted = record.duration()
            employee_records[employee.id]['weeks'][week_key]['adjusted_hours'] += hours_adjusted
            
        # Guardar registro
        employee_records[employee.id]['weeks'][week_key]['records'].append((original, record, hours_original, hours_adjusted))
    
    # Generar PDF con los datos estructurados
    for emp_id, emp_data in employee_records.items():
        employee = emp_data['employee']
        employee_name = f"{employee.first_name} {employee.last_name}"
        
        # Imprimir nombre del empleado como encabezado
        pdf.set_font('Arial', 'B', 12)
        pdf.ln(5)
        pdf.cell(0, 10, f"Empleado: {employee_name}", 0, 1, 'L')
        pdf.set_font('Arial', '', 8)
        
        employee_total_original = 0
        employee_total_adjusted = 0
        
        # Procesar cada semana del empleado
        for week_key in sorted(emp_data['weeks'].keys()):
            week_data = emp_data['weeks'][week_key]
            
            # Imprimir encabezado de la semana
            week_header = f"Semana: {week_data['start_date'].strftime('%d/%m/%Y')} - {week_data['end_date'].strftime('%d/%m/%Y')}"
            pdf.set_font('Arial', 'B', 10)
            pdf.ln(3)
            pdf.cell(0, 7, week_header, 0, 1, 'L')
            pdf.set_font('Arial', '', 8)
            
            # Imprimir registros de la semana
            for original, record, hours_original, hours_adjusted in week_data['records']:
                date_str = original.original_check_in_time.strftime('%d/%m/%Y')
                
                in_time_orig = original.original_check_in_time.strftime('%H:%M')
                out_time_orig = original.original_check_out_time.strftime('%H:%M') if original.original_check_out_time else '-'
                
                in_time_mod = record.check_in_time.strftime('%H:%M')
                out_time_mod = record.check_out_time.strftime('%H:%M') if record.check_out_time else '-'
                
                adjusted_by = original.adjusted_by.username if original.adjusted_by else 'Sistema'
                
                # Imprimir fila
                pdf.cell(40, 7, "", 1, 0, 'L')  # Columna vacía para el empleado (ya está en el encabezado)
                pdf.cell(20, 7, date_str, 1, 0, 'C')
                pdf.cell(15, 7, in_time_orig, 1, 0, 'C')
                pdf.cell(15, 7, out_time_orig, 1, 0, 'C')
                pdf.cell(15, 7, in_time_mod, 1, 0, 'C')
                pdf.cell(15, 7, out_time_mod, 1, 0, 'C')
                pdf.cell(20, 7, adjusted_by, 1, 0, 'C')
                
                # Ajustar motivo para que quepa en una fila
                motivo = original.adjustment_reason
                if motivo and len(motivo) > 30:
                    motivo = motivo[:27] + '...'
                pdf.cell(50, 7, motivo or '', 1, 1, 'L')
            
            # Imprimir totales de la semana
            pdf.set_font('Arial', 'B', 8)
            pdf.set_fill_color(230, 230, 230)
            pdf.cell(90, 7, f'Total semana (horas originales): {week_data["original_hours"]:.2f}', 1, 0, 'R', True)
            pdf.cell(90, 7, f'Total semana (horas ajustadas): {week_data["adjusted_hours"]:.2f}', 1, 1, 'R', True)
            pdf.ln(5)
            
            # Acumular totales del empleado
            employee_total_original += week_data['original_hours']
            employee_total_adjusted += week_data['adjusted_hours']
        
        # Imprimir totales del empleado
        pdf.set_font('Arial', 'B', 10)
        pdf.set_fill_color(200, 220, 255)
        pdf.cell(90, 8, f'TOTAL EMPLEADO (horas originales): {employee_total_original:.2f}', 1, 0, 'R', True)
        pdf.cell(90, 8, f'TOTAL EMPLEADO (horas ajustadas): {employee_total_adjusted:.2f}', 1, 1, 'R', True)
        
        # Nueva página para cada empleado, excepto el último
        if list(employee_records.keys()).index(emp_id) < len(employee_records) - 1:
            pdf.add_page()
    
    # Guardar PDF
    pdf.output(pdf_file.name)
    
    # Enviar el archivo
    filename = f"registros_originales.pdf"
    return send_file(
        pdf_file.name,
        as_attachment=True,
        download_name=filename,
        mimetype='application/pdf'
    )

# Ruta para acceder directamente a un checkpoint específico por ID
@checkpoints_bp.route('/login/<int:checkpoint_id>', methods=['GET', 'POST'])
```

#### `get_week_start` (línea 514)

**Docstring:**
```
Retorna la fecha del lunes de la semana a la que pertenece date_obj
```

**Código:**
```python
    def get_week_start(date_obj):
        """Retorna la fecha del lunes de la semana a la que pertenece date_obj"""
        # weekday() retorna 0 para lunes, 6 para domingo
        days_to_subtract = date_obj.weekday()
        return date_obj - timedelta(days=days_to_subtract)
    
    # Función auxiliar para obtener el domingo de la semana de una fecha
```

#### `get_week_end` (línea 521)

**Docstring:**
```
Retorna la fecha del domingo de la semana a la que pertenece date_obj
```

**Código:**
```python
    def get_week_end(date_obj):
        """Retorna la fecha del domingo de la semana a la que pertenece date_obj"""
        # weekday() retorna 0 para lunes, 6 para domingo
        days_to_add = 6 - date_obj.weekday()
        return date_obj + timedelta(days=days_to_add)
    
    # Crear un PDF personalizado
```

#### `header` (línea 529)

**Código:**
```python
        def header(self):
            # Logo y título
            self.set_font('Arial', 'B', 15)
            title = 'Registro de Ajustes de Fichajes'
            if company:
                title = f'Registro de Ajustes de Fichajes - {company.name}'
            self.cell(0, 10, title, 0, 1, 'C')
            
            # Período
            if start_date and end_date:
                period = f"{start_date.strftime('%d/%m/%Y')} - {end_date.strftime('%d/%m/%Y')}"
            elif start_date:
                period = f"Desde {start_date.strftime('%d/%m/%Y')}"
            elif end_date:
                period = f"Hasta {end_date.strftime('%d/%m/%Y')}"
            else:
                period = "Todos los registros"
                
            self.set_font('Arial', '', 10)
            self.cell(0, 10, f'Período: {period}', 0, 1, 'C')
            
            # Encabezados de la tabla
            self.set_fill_color(200, 220, 255)
            self.set_font('Arial', 'B', 8)
            self.cell(40, 7, 'Empleado', 1, 0, 'C', True)
            self.cell(20, 7, 'Fecha', 1, 0, 'C', True)
            self.cell(15, 7, 'Ent. Orig.', 1, 0, 'C', True)
            self.cell(15, 7, 'Sal. Orig.', 1, 0, 'C', True)
            self.cell(15, 7, 'Ent. Mod.', 1, 0, 'C', True)
            self.cell(15, 7, 'Sal. Mod.', 1, 0, 'C', True)
            self.cell(20, 7, 'Ajustado Por', 1, 0, 'C', True)
            self.cell(50, 7, 'Motivo', 1, 1, 'C', True)
            
```

#### `footer` (línea 562)

**Código:**
```python
        def footer(self):
            # Posición a 1.5 cm del final
            self.set_y(-15)
            # Número de página
            self.set_font('Arial', 'I', 8)
            self.cell(0, 10, f'Página {self.page_no()}/{{nb}}', 0, 0, 'C')
    
    # Crear PDF
```

#### `login_to_checkpoint` (línea 719)

**Docstring:**
```
Acceso directo a un punto de fichaje específico por ID
```

**Código:**
```python
def login_to_checkpoint(checkpoint_id):
    """Acceso directo a un punto de fichaje específico por ID"""
    # Si ya hay una sesión activa, redirigir al dashboard
    if 'checkpoint_id' in session:
        return redirect(url_for('checkpoints_slug.checkpoint_dashboard'))
    
    # Buscar el checkpoint por ID
    checkpoint = CheckPoint.query.get_or_404(checkpoint_id)
    
    # Si el checkpoint no está activo, mostrar error
    if not checkpoint.is_active:
        flash('El punto de fichaje no está activo.', 'danger')
        return redirect(url_for('checkpoints_slug.select_company'))
    
    # Crear el formulario
    form = CheckPointLoginForm()
    
    # Procesar el formulario si es una solicitud POST y es válido
    if form.validate_on_submit():
        # Guardar la información del checkpoint en la sesión
        session['checkpoint_id'] = checkpoint.id
        session['company_id'] = checkpoint.company_id
        
        # Redirigir al formulario de ingreso de PIN
        return redirect(url_for('checkpoints_slug.employee_pin'))
    
    return render_template('checkpoints/login.html', form=form, checkpoint=checkpoint)

# Resto del código ...
```


### routes_tasks.py

#### `manager_required` (línea 27)

**Código:**
```python
def manager_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated:
            return redirect(url_for('login'))
        
        if not (current_user.is_admin() or current_user.is_gerente()):
            flash('No tienes permiso para acceder a esta página.', 'danger')
            return redirect(url_for('index'))
            
        return f(*args, **kwargs)
    return decorated_function

# Decorador para proteger rutas de usuario local
```

#### `decorated_function` (línea 29)

**Código:**
```python
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated:
            return redirect(url_for('login'))
        
        if not (current_user.is_admin() or current_user.is_gerente()):
            flash('No tienes permiso para acceder a esta página.', 'danger')
            return redirect(url_for('index'))
            
        return f(*args, **kwargs)
    return decorated_function

# Decorador para proteger rutas de usuario local
```

#### `local_user_required` (línea 41)

**Código:**
```python
def local_user_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'local_user_id' not in session:
            return redirect(url_for('tasks.local_login'))
            
        return f(*args, **kwargs)
    return decorated_function

# Rutas para el dashboard principal de tareas
@tasks_bp.route('/')
@login_required
```

#### `decorated_function` (línea 43)

**Código:**
```python
    def decorated_function(*args, **kwargs):
        if 'local_user_id' not in session:
            return redirect(url_for('tasks.local_login'))
            
        return f(*args, **kwargs)
    return decorated_function

# Rutas para el dashboard principal de tareas
```

#### `index` (línea 53)

**Docstring:**
```
Dashboard principal del módulo de tareas
```

**Código:**
```python
def index():
    """Dashboard principal del módulo de tareas"""
    # Determinar qué información mostrar según el rol del usuario
    if current_user.is_admin():
        # Los administradores ven todos los locales
        locations = Location.query.all()
    elif current_user.is_gerente():
        # Los gerentes ven solo los locales de sus empresas
        company_ids = [c.id for c in current_user.companies]
        locations = Location.query.filter(Location.company_id.in_(company_ids)).all()
    else:
        # Otros usuarios no tendrían acceso, pero por si acaso
        locations = []
    
    # Si hay locales, redirigir al primero
    if locations:
        first_location = locations[0]
        return redirect(url_for('tasks.view_location', id=first_location.id))
        
    # Si no hay locales, mostrar la página de dashboard con el botón para crear el primer local
    return render_template('tasks/dashboard.html', locations=locations)

# Rutas para gestión de locales
@tasks_bp.route('/locations')
@login_required
@manager_required
```

#### `list_locations` (línea 79)

**Docstring:**
```
Lista de locales disponibles
```

**Código:**
```python
def list_locations():
    """Lista de locales disponibles"""
    if current_user.is_admin():
        locations = Location.query.all()
    elif current_user.is_gerente() and current_user.company_id:
        locations = Location.query.filter_by(company_id=current_user.company_id).all()
    else:
        locations = []
    
    return render_template('tasks/location_list.html',
                          title='Gestión de Locales',
                          locations=locations)

@tasks_bp.route('/locations/create', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `create_location` (línea 95)

**Docstring:**
```
Crear un nuevo local
```

**Código:**
```python
def create_location():
    """Crear un nuevo local"""
    form = LocationForm()
    
    # Cargar empresas disponibles según el rol
    if current_user.is_admin():
        form.company_id.choices = [(c.id, c.name) for c in Company.query.all()]
    elif current_user.is_gerente() and current_user.company_id:
        company = Company.query.get(current_user.company_id)
        if company:
            form.company_id.choices = [(company.id, company.name)]
        else:
            form.company_id.choices = []
    else:
        form.company_id.choices = []
    
    if form.validate_on_submit():
        location = Location(
            name=form.name.data,
            address=form.address.data,
            city=form.city.data,
            postal_code=form.postal_code.data,
            description=form.description.data,
            company_id=form.company_id.data,
            is_active=form.is_active.data
        )
        
        # Si se proporcionó contraseña, establecerla de forma segura
        if form.portal_password.data:
            location.set_portal_password(form.portal_password.data)
        
        db.session.add(location)
        db.session.commit()
        
        log_activity(f'Local creado: {location.name}')
        flash(f'Local "{location.name}" creado correctamente.', 'success')
        return redirect(url_for('tasks.list_locations'))
    
    return render_template('tasks/location_form.html',
                          title='Crear Nuevo Local',
                          form=form)

@tasks_bp.route('/locations/edit/<int:id>', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `edit_location` (línea 140)

**Docstring:**
```
Editar un local existente
```

**Código:**
```python
def edit_location(id):
    """Editar un local existente"""
    location = Location.query.get_or_404(id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        flash('No tienes permiso para editar este local.', 'danger')
        return redirect(url_for('tasks.list_locations'))
    
    form = LocationForm(obj=location)
    
    # No cargar contraseña del portal, ya que la tenemos como hash
    form.portal_password.data = None
    form.confirm_portal_password.data = None
    
    # Cargar empresas disponibles según el rol
    if current_user.is_admin():
        form.company_id.choices = [(c.id, c.name) for c in Company.query.all()]
    elif current_user.is_gerente() and current_user.company_id:
        company = Company.query.get(current_user.company_id)
        if company:
            form.company_id.choices = [(company.id, company.name)]
        else:
            form.company_id.choices = []
    else:
        form.company_id.choices = []
    
    if form.validate_on_submit():
        location.name = form.name.data
        location.address = form.address.data
        location.city = form.city.data
        location.postal_code = form.postal_code.data
        location.description = form.description.data
        location.company_id = form.company_id.data
        location.is_active = form.is_active.data
        # Ya no se permite personalizar el nombre de usuario
        
        # Si se proporcionó una nueva contraseña, actualizarla
        if form.portal_password.data:
            location.set_portal_password(form.portal_password.data)
        
        db.session.commit()
        
        log_activity(f'Local actualizado: {location.name}')
        flash(f'Local "{location.name}" actualizado correctamente.', 'success')
        return redirect(url_for('tasks.list_locations'))
    
    return render_template('tasks/location_form.html',
                          title=f'Editar Local: {location.name}',
                          form=form,
                          location=location)

@tasks_bp.route('/locations/delete/<int:id>', methods=['POST'])
@login_required
@manager_required
```

#### `delete_location` (línea 195)

**Docstring:**
```
Eliminar un local
```

**Código:**
```python
def delete_location(id):
    """Eliminar un local"""
    location = Location.query.get_or_404(id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        flash('No tienes permiso para eliminar este local.', 'danger')
        return redirect(url_for('tasks.list_locations'))
    
    # Verificar si tiene tareas o usuarios asociados
    if location.tasks or location.local_users:
        flash('No se puede eliminar este local porque tiene tareas o usuarios asociados.', 'warning')
        return redirect(url_for('tasks.list_locations'))
    
    name = location.name
    db.session.delete(location)
    db.session.commit()
    
    log_activity(f'Local eliminado: {name}')
    flash(f'Local "{name}" eliminado correctamente.', 'success')
    return redirect(url_for('tasks.list_locations'))

@tasks_bp.route('/locations/<int:id>/groups')
@login_required
@manager_required
```

#### `list_task_groups` (línea 220)

**Docstring:**
```
Lista de grupos de tareas para un local
```

**Código:**
```python
def list_task_groups(id):
    """Lista de grupos de tareas para un local"""
    location = Location.query.get_or_404(id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        flash('No tienes permiso para ver los grupos de este local.', 'danger')
        return redirect(url_for('tasks.list_locations'))
    
    # Obtener todos los grupos para este local
    groups = TaskGroup.query.filter_by(location_id=id).all()
    
    return render_template('tasks/task_group_list.html',
                          title=f'Grupos de Tareas - {location.name}',
                          location=location,
                          groups=groups)

@tasks_bp.route('/locations/<int:location_id>/groups/create', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `create_task_group` (línea 240)

**Docstring:**
```
Crear un nuevo grupo de tareas
```

**Código:**
```python
def create_task_group(location_id):
    """Crear un nuevo grupo de tareas"""
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        flash('No tienes permiso para crear grupos en este local.', 'danger')
        return redirect(url_for('tasks.list_locations'))
    
    form = TaskGroupForm()
    form.location_id.choices = [(location.id, location.name)]
    form.location_id.data = location.id
    
    if form.validate_on_submit():
        group = TaskGroup(
            name=form.name.data,
            description=form.description.data,
            color=form.color.data,
            location_id=location.id
        )
        
        db.session.add(group)
        db.session.commit()
        
        log_activity(f'Grupo de tareas creado: {group.name} para local {location.name}')
        flash(f'Grupo "{group.name}" creado correctamente.', 'success')
        return redirect(url_for('tasks.list_task_groups', id=location.id))
    
    return render_template('tasks/task_group_form.html',
                          title='Crear Nuevo Grupo de Tareas',
                          form=form,
                          location=location)

@tasks_bp.route('/task-groups/<int:id>/edit', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `edit_task_group` (línea 276)

**Docstring:**
```
Editar un grupo de tareas existente
```

**Código:**
```python
def edit_task_group(id):
    """Editar un grupo de tareas existente"""
    group = TaskGroup.query.get_or_404(id)
    location = group.location
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        flash('No tienes permiso para editar este grupo.', 'danger')
        return redirect(url_for('tasks.list_locations'))
    
    form = TaskGroupForm(obj=group)
    form.location_id.choices = [(location.id, location.name)]
    
    if form.validate_on_submit():
        group.name = form.name.data
        group.description = form.description.data
        group.color = form.color.data
        
        db.session.commit()
        
        log_activity(f'Grupo de tareas actualizado: {group.name}')
        flash(f'Grupo "{group.name}" actualizado correctamente.', 'success')
        return redirect(url_for('tasks.list_task_groups', id=location.id))
    
    return render_template('tasks/task_group_form.html',
                          title=f'Editar Grupo: {group.name}',
                          form=form,
                          group=group,
                          location=location)

@tasks_bp.route('/task-groups/<int:id>/delete', methods=['POST'])
@login_required
@manager_required
```

#### `delete_task_group` (línea 309)

**Docstring:**
```
Eliminar un grupo de tareas
```

**Código:**
```python
def delete_task_group(id):
    """Eliminar un grupo de tareas"""
    group = TaskGroup.query.get_or_404(id)
    location = group.location
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        flash('No tienes permiso para eliminar este grupo.', 'danger')
        return redirect(url_for('tasks.list_locations'))
    
    # Verificar si hay tareas asignadas al grupo
    tasks = Task.query.filter_by(group_id=id).all()
    if tasks:
        # Actualizar las tareas para desasociarlas del grupo
        for task in tasks:
            task.group_id = None
        db.session.commit()
    
    name = group.name
    location_id = location.id
    db.session.delete(group)
    db.session.commit()
    
    log_activity(f'Grupo de tareas eliminado: {name}')
    flash(f'Grupo "{name}" eliminado correctamente.', 'success')
    return redirect(url_for('tasks.list_task_groups', id=location_id))

@tasks_bp.route('/locations/<int:id>')
@login_required
```

#### `view_location` (línea 338)

**Docstring:**
```
Ver detalles de un local
```

**Código:**
```python
def view_location(id):
    """Ver detalles de un local"""
    location = Location.query.get_or_404(id)
    
    # Verificar permisos (admin, gerente de la empresa, o empleado de la empresa)
    if not current_user.is_admin() and not (current_user.company_id == location.company_id):
        flash('No tienes permiso para ver este local.', 'danger')
        return redirect(url_for('tasks.index'))
    
    # Obtener tareas para hoy
    today = date.today()
    active_tasks = []
    pending_tasks = Task.query.filter_by(location_id=location.id, status=TaskStatus.PENDIENTE).all()
    completed_tasks = Task.query.filter_by(location_id=location.id, status=TaskStatus.COMPLETADA).all()
    
    for task in pending_tasks:
        if task.is_due_today():
            active_tasks.append(task)
    
    # Obtener usuarios locales
    local_users = LocalUser.query.filter_by(location_id=location.id).all()
    
    # Obtener tareas recientes completadas
    recent_completions = (db.session.query(TaskCompletion, Task, LocalUser)
                         .join(Task, TaskCompletion.task_id == Task.id)
                         .join(LocalUser, TaskCompletion.local_user_id == LocalUser.id)
                         .filter(Task.location_id == location.id)
                         .order_by(TaskCompletion.completion_date.desc())
                         .limit(10)
                         .all())
    
    # Generar datos para el gráfico de completados mensuales
    last_6_months = []
    monthly_counts = []
    current_month = datetime.now().month
    current_year = datetime.now().year
    
    for i in range(5, -1, -1):
        month = (current_month - i) % 12
        if month == 0:
            month = 12
        year = current_year if month <= current_month else current_year - 1
        
        month_start = datetime(year, month, 1)
        if month == 12:
            next_month_start = datetime(year + 1, 1, 1)
        else:
            next_month_start = datetime(year, month + 1, 1)
            
        # Obtener completados de este mes
        month_count = TaskCompletion.query\
            .join(Task, TaskCompletion.task_id == Task.id)\
            .filter(Task.location_id == location.id)\
            .filter(TaskCompletion.completion_date >= month_start)\
            .filter(TaskCompletion.completion_date < next_month_start)\
            .count()
            
        # Formato del mes en español
        month_names = ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"]
        month_label = f"{month_names[month-1]} {year}"
        
        last_6_months.append(month_label)
        monthly_counts.append(month_count)
    
    # Obtener el enlace directo al portal
    portal_url = url_for('tasks.local_portal', location_id=location.id, _external=True)
    
    return render_template('tasks/location_detail.html',
                          title=f'Local: {location.name}',
                          location=location,
                          active_tasks=active_tasks,
                          pending_tasks=pending_tasks,
                          completed_tasks=completed_tasks,
                          local_users=local_users,
                          recent_completions=recent_completions,
                          monthly_labels=last_6_months,
                          monthly_counts=monthly_counts,
                          portal_url=portal_url)

# Rutas para gestión de usuarios locales
@tasks_bp.route('/locations/<int:location_id>/users')
@login_required
@manager_required
```

#### `list_local_users` (línea 421)

**Docstring:**
```
Lista de usuarios de un local
```

**Código:**
```python
def list_local_users(location_id):
    """Lista de usuarios de un local"""
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        flash('No tienes permiso para ver los usuarios de este local.', 'danger')
        return redirect(url_for('tasks.list_locations'))
    
    users = LocalUser.query.filter_by(location_id=location_id).all()
    
    return render_template('tasks/local_user_list.html',
                          title=f'Usuarios del Local: {location.name}',
                          location=location,
                          users=users)

@tasks_bp.route('/locations/<int:location_id>/users/create', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `create_local_user` (línea 440)

**Docstring:**
```
Crear un nuevo usuario local
```

**Código:**
```python
def create_local_user(location_id):
    """Crear un nuevo usuario local"""
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        flash('No tienes permiso para crear usuarios en este local.', 'danger')
        return redirect(url_for('tasks.list_locations'))
    
    form = LocalUserForm()
    
    if form.validate_on_submit():
        # Generar nombre de usuario automáticamente (nombre + apellido + timestamp)
        timestamp = datetime.now().strftime('%y%m%d%H%M%S')
        username = f"{form.name.data.lower()}_{form.last_name.data.lower()}_{timestamp}"
        
        user = LocalUser(
            name=form.name.data,
            last_name=form.last_name.data,
            username=username,
            location_id=location_id,
            is_active=form.is_active.data
        )
        
        # Establecer PIN de acceso
        user.set_pin(form.pin.data)
        
        # Guardar foto si se proporciona
        if form.photo.data:
            filename = secure_filename(form.photo.data.filename)
            # Generar nombre único para evitar colisiones
            unique_filename = f"user_{datetime.now().strftime('%Y%m%d%H%M%S')}_{filename}"
            photo_path = os.path.join(current_app.config['UPLOAD_FOLDER'], 'local_users', unique_filename)
            
            # Asegurar que existe el directorio
            os.makedirs(os.path.dirname(photo_path), exist_ok=True)
            
            # Guardar archivo
            form.photo.data.save(photo_path)
            user.photo_path = os.path.join('local_users', unique_filename)
        
        db.session.add(user)
        db.session.commit()
        
        log_activity(f'Usuario local creado: {user.name} en {location.name}')
        flash(f'Usuario "{user.name}" creado correctamente.', 'success')
        return redirect(url_for('tasks.list_local_users', location_id=location_id))
    
    return render_template('tasks/local_user_form.html',
                          title=f'Crear Usuario en {location.name}',
                          form=form,
                          location=location)

@tasks_bp.route('/locations/<int:location_id>/users/edit/<int:id>', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `edit_local_user` (línea 496)

**Docstring:**
```
Editar un usuario local existente
```

**Código:**
```python
def edit_local_user(location_id, id):
    """Editar un usuario local existente"""
    location = Location.query.get_or_404(location_id)
    user = LocalUser.query.get_or_404(id)
    
    # Verificar que el usuario pertenece al local
    if user.location_id != location_id:
        flash('El usuario no pertenece a este local.', 'danger')
        return redirect(url_for('tasks.list_local_users', location_id=location_id))
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        flash('No tienes permiso para editar usuarios en este local.', 'danger')
        return redirect(url_for('tasks.list_locations'))
    
    # Crear formulario, PIN opcional para editarlo
    form = LocalUserForm(obj=user)
    form.pin.validators = [Optional()]  # Hacer el PIN opcional
    
    if form.validate_on_submit():
        user.name = form.name.data
        user.last_name = form.last_name.data
        user.is_active = form.is_active.data
        
        # Actualizar PIN solo si se proporciona
        if form.pin.data:
            user.set_pin(form.pin.data)
        
        # Actualizar foto si se proporciona
        if form.photo.data:
            # Eliminar foto anterior si existe
            if user.photo_path:
                old_photo_path = os.path.join(current_app.config['UPLOAD_FOLDER'], user.photo_path)
                if os.path.exists(old_photo_path):
                    os.remove(old_photo_path)
            
            filename = secure_filename(form.photo.data.filename)
            # Generar nombre único para evitar colisiones
            unique_filename = f"user_{datetime.now().strftime('%Y%m%d%H%M%S')}_{filename}"
            photo_path = os.path.join(current_app.config['UPLOAD_FOLDER'], 'local_users', unique_filename)
            
            # Asegurar que existe el directorio
            os.makedirs(os.path.dirname(photo_path), exist_ok=True)
            
            # Guardar archivo
            form.photo.data.save(photo_path)
            user.photo_path = os.path.join('local_users', unique_filename)
        
        db.session.commit()
        
        log_activity(f'Usuario local actualizado: {user.name} en {location.name}')
        flash(f'Usuario "{user.name}" actualizado correctamente.', 'success')
        return redirect(url_for('tasks.list_local_users', location_id=location_id))
    
    # Eliminar valores del campo PIN para seguridad
    form.pin.data = ''
    
    return render_template('tasks/local_user_form.html',
                          title=f'Editar Usuario: {user.name}',
                          form=form,
                          location=location,
                          user=user)

@tasks_bp.route('/locations/<int:location_id>/users/delete/<int:id>', methods=['POST'])
@login_required
@manager_required
```

#### `delete_local_user` (línea 562)

**Docstring:**
```
Eliminar un usuario local
```

**Código:**
```python
def delete_local_user(location_id, id):
    """Eliminar un usuario local"""
    location = Location.query.get_or_404(location_id)
    user = LocalUser.query.get_or_404(id)
    
    # Verificar que el usuario pertenece al local
    if user.location_id != location_id:
        flash('El usuario no pertenece a este local.', 'danger')
        return redirect(url_for('tasks.list_local_users', location_id=location_id))
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        flash('No tienes permiso para eliminar usuarios en este local.', 'danger')
        return redirect(url_for('tasks.list_locations'))
    
    # Verificar si tiene tareas completadas asociadas
    if user.completed_tasks:
        flash('No se puede eliminar este usuario porque tiene tareas completadas asociadas.', 'warning')
        return redirect(url_for('tasks.list_local_users', location_id=location_id))
    
    # Eliminar foto si existe
    if user.photo_path:
        photo_path = os.path.join(current_app.config['UPLOAD_FOLDER'], user.photo_path)
        if os.path.exists(photo_path):
            os.remove(photo_path)
    
    name = user.name
    db.session.delete(user)
    db.session.commit()
    
    log_activity(f'Usuario local eliminado: {name} de {location.name}')
    flash(f'Usuario "{name}" eliminado correctamente.', 'success')
    return redirect(url_for('tasks.list_local_users', location_id=location_id))

# Rutas para gestión de tareas
@tasks_bp.route('/locations/<int:location_id>/tasks')
@login_required
```

#### `list_tasks` (línea 599)

**Docstring:**
```
Lista de tareas de un local
```

**Código:**
```python
def list_tasks(location_id):
    """Lista de tareas de un local"""
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos (admin, gerente o empleado de la empresa)
    if not current_user.is_admin() and not (current_user.company_id == location.company_id):
        flash('No tienes permiso para ver las tareas de este local.', 'danger')
        return redirect(url_for('tasks.index'))
    
    # Filtrar por estado si se especifica
    status_filter = request.args.get('status')
    if status_filter and status_filter in [s.value for s in TaskStatus]:
        tasks = Task.query.filter_by(location_id=location_id, status=TaskStatus(status_filter)).all()
    else:
        tasks = Task.query.filter_by(location_id=location_id).all()
    
    # Agrupar tareas por estado
    tasks_by_status = {
        'pendiente': [],
        'completada': [],
        'vencida': [],
        'cancelada': []
    }
    
    for task in tasks:
        if task.status:
            tasks_by_status[task.status.value].append(task)
    
    return render_template('tasks/task_list.html',
                          title=f'Tareas del Local: {location.name}',
                          location=location,
                          tasks=tasks,
                          tasks_by_status=tasks_by_status)

@tasks_bp.route('/locations/<int:location_id>/tasks/create', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `create_task` (línea 636)

**Docstring:**
```
Crear una nueva tarea
```

**Código:**
```python
def create_task(location_id):
    """Crear una nueva tarea"""
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        flash('No tienes permiso para crear tareas en este local.', 'danger')
        return redirect(url_for('tasks.list_locations'))
    
    form = TaskForm()
    form.location_id.choices = [(location.id, location.name)]
    
    # Cargar grupos de tareas disponibles para este local
    groups = TaskGroup.query.filter_by(location_id=location_id).all()
    group_choices = [(0, 'Ningún grupo')] + [(g.id, g.name) for g in groups]
    form.group_id.choices = group_choices
    
    if form.validate_on_submit():
        task = Task(
            title=form.title.data,
            description=form.description.data,
            priority=TaskPriority(form.priority.data),
            frequency=TaskFrequency(form.frequency.data),
            start_date=form.start_date.data,
            end_date=form.end_date.data,
            location_id=form.location_id.data,
            created_by_id=current_user.id,
            status=TaskStatus.PENDIENTE
        )
        
        # Asignar grupo de tareas si se seleccionó uno
        if form.group_id.data and form.group_id.data != 0:
            task.group_id = form.group_id.data
        
        db.session.add(task)
        db.session.commit()
        
        # Para tareas personalizadas, guardar los días seleccionados
        if task.frequency == TaskFrequency.PERSONALIZADA:
            # Crear registros de días seleccionados
            if form.monday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.LUNES))
            if form.tuesday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.MARTES))
            if form.wednesday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.MIERCOLES))
            if form.thursday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.JUEVES))
            if form.friday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.VIERNES))
            if form.saturday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.SABADO))
            if form.sunday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.DOMINGO))
            
            db.session.commit()
        
        # Redirigir a la página de programación según la frecuencia
        log_activity(f'Tarea creada: {task.title} en {location.name}')
        flash(f'Tarea "{task.title}" creada correctamente. Ahora debes configurar su programación.', 'success')
        
        if task.frequency == TaskFrequency.DIARIA:
            return redirect(url_for('tasks.configure_daily_schedule', task_id=task.id))
        elif task.frequency == TaskFrequency.SEMANAL:
            return redirect(url_for('tasks.configure_weekly_schedule', task_id=task.id))
        elif task.frequency == TaskFrequency.MENSUAL:
            return redirect(url_for('tasks.configure_monthly_schedule', task_id=task.id))
        elif task.frequency == TaskFrequency.QUINCENAL:
            return redirect(url_for('tasks.configure_biweekly_schedule', task_id=task.id))
        else:
            return redirect(url_for('tasks.list_tasks', location_id=location_id))
    
    return render_template('tasks/task_form.html',
                          title=f'Crear Tarea en {location.name}',
                          form=form,
                          location=location,
                          location_id=location_id)

@tasks_bp.route('/tasks/<int:task_id>/daily-schedule', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `configure_daily_schedule` (línea 717)

**Docstring:**
```
Configurar horario diario para una tarea
```

**Código:**
```python
def configure_daily_schedule(task_id):
    """Configurar horario diario para una tarea"""
    task = Task.query.get_or_404(task_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != task.location.company_id):
        flash('No tienes permiso para configurar esta tarea.', 'danger')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    # Verificar que sea una tarea diaria
    if task.frequency != TaskFrequency.DIARIA:
        flash('Esta tarea no es de frecuencia diaria.', 'warning')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    # Buscar si ya existe un horario
    schedule = TaskSchedule.query.filter_by(task_id=task.id).first()
    
    form = DailyScheduleForm(obj=schedule)
    
    if form.validate_on_submit():
        if schedule:
            # Actualizar horario existente
            schedule.start_time = form.start_time.data
            schedule.end_time = form.end_time.data
        else:
            # Crear nuevo horario
            schedule = TaskSchedule(
                task_id=task.id,
                start_time=form.start_time.data,
                end_time=form.end_time.data
            )
            db.session.add(schedule)
        
        db.session.commit()
        
        log_activity(f'Horario diario configurado para tarea: {task.title}')
        flash('Horario configurado correctamente.', 'success')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    return render_template('tasks/schedule_form.html',
                          title=f'Configurar Horario Diario: {task.title}',
                          form=form,
                          task=task,
                          schedule_type='diario')

@tasks_bp.route('/tasks/<int:task_id>/weekly-schedule', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `configure_weekly_schedule` (línea 765)

**Docstring:**
```
Configurar horario semanal para una tarea
```

**Código:**
```python
def configure_weekly_schedule(task_id):
    """Configurar horario semanal para una tarea"""
    task = Task.query.get_or_404(task_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != task.location.company_id):
        flash('No tienes permiso para configurar esta tarea.', 'danger')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    # Verificar que sea una tarea semanal
    if task.frequency != TaskFrequency.SEMANAL:
        flash('Esta tarea no es de frecuencia semanal.', 'warning')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    # Buscar si ya existe un horario
    schedule = TaskSchedule.query.filter_by(task_id=task.id).first()
    
    form = WeeklyScheduleForm(obj=schedule)
    
    if form.validate_on_submit():
        if schedule:
            # Actualizar horario existente
            schedule.day_of_week = WeekDay(form.day_of_week.data)
            schedule.start_time = form.start_time.data
            schedule.end_time = form.end_time.data
        else:
            # Crear nuevo horario
            schedule = TaskSchedule(
                task_id=task.id,
                day_of_week=WeekDay(form.day_of_week.data),
                start_time=form.start_time.data,
                end_time=form.end_time.data
            )
            db.session.add(schedule)
        
        db.session.commit()
        
        log_activity(f'Horario semanal configurado para tarea: {task.title}')
        flash('Horario configurado correctamente.', 'success')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    return render_template('tasks/schedule_form.html',
                          title=f'Configurar Horario Semanal: {task.title}',
                          form=form,
                          task=task,
                          schedule_type='semanal')

@tasks_bp.route('/tasks/<int:task_id>/monthly-schedule', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `configure_monthly_schedule` (línea 815)

**Docstring:**
```
Configurar horario mensual para una tarea
```

**Código:**
```python
def configure_monthly_schedule(task_id):
    """Configurar horario mensual para una tarea"""
    task = Task.query.get_or_404(task_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != task.location.company_id):
        flash('No tienes permiso para configurar esta tarea.', 'danger')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    # Verificar que sea una tarea mensual
    if task.frequency != TaskFrequency.MENSUAL:
        flash('Esta tarea no es de frecuencia mensual.', 'warning')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    # Buscar si ya existe un horario
    schedule = TaskSchedule.query.filter_by(task_id=task.id).first()
    
    form = MonthlyScheduleForm(obj=schedule)
    
    if form.validate_on_submit():
        if schedule:
            # Actualizar horario existente
            schedule.day_of_month = form.day_of_month.data
            schedule.start_time = form.start_time.data
            schedule.end_time = form.end_time.data
        else:
            # Crear nuevo horario
            schedule = TaskSchedule(
                task_id=task.id,
                day_of_month=form.day_of_month.data,
                start_time=form.start_time.data,
                end_time=form.end_time.data
            )
            db.session.add(schedule)
        
        db.session.commit()
        
        log_activity(f'Horario mensual configurado para tarea: {task.title}')
        flash('Horario configurado correctamente.', 'success')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    return render_template('tasks/schedule_form.html',
                          title=f'Configurar Horario Mensual: {task.title}',
                          form=form,
                          task=task,
                          schedule_type='mensual')

@tasks_bp.route('/tasks/<int:task_id>/biweekly-schedule', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `configure_biweekly_schedule` (línea 865)

**Docstring:**
```
Configurar horario quincenal para una tarea
```

**Código:**
```python
def configure_biweekly_schedule(task_id):
    """Configurar horario quincenal para una tarea"""
    task = Task.query.get_or_404(task_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != task.location.company_id):
        flash('No tienes permiso para configurar esta tarea.', 'danger')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    # Verificar que sea una tarea quincenal
    if task.frequency != TaskFrequency.QUINCENAL:
        flash('Esta tarea no es de frecuencia quincenal.', 'warning')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    # Buscar si ya existe un horario
    schedule = TaskSchedule.query.filter_by(task_id=task.id).first()
    
    form = BiweeklyScheduleForm(obj=schedule)
    
    if form.validate_on_submit():
        if schedule:
            # Actualizar horario existente
            schedule.start_time = form.start_time.data
            schedule.end_time = form.end_time.data
        else:
            # Crear nuevo horario
            schedule = TaskSchedule(
                task_id=task.id,
                start_time=form.start_time.data,
                end_time=form.end_time.data
            )
            db.session.add(schedule)
        
        db.session.commit()
        
        log_activity(f'Horario quincenal configurado para tarea: {task.title}')
        flash('Horario configurado correctamente.', 'success')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    return render_template('tasks/schedule_form.html',
                          title=f'Configurar Horario Quincenal: {task.title}',
                          form=form,
                          task=task,
                          schedule_type='quincenal')

@tasks_bp.route('/tasks/<int:task_id>/edit', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `edit_task` (línea 913)

**Docstring:**
```
Editar una tarea existente
```

**Código:**
```python
def edit_task(task_id):
    """Editar una tarea existente"""
    task = Task.query.get_or_404(task_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != task.location.company_id):
        flash('No tienes permiso para editar esta tarea.', 'danger')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    form = TaskForm(obj=task)
    form.location_id.choices = [(task.location.id, task.location.name)]
    
    # Cargar grupos de tareas disponibles para este local
    groups = TaskGroup.query.filter_by(location_id=task.location_id).all()
    group_choices = [(0, 'Ningún grupo')] + [(g.id, g.name) for g in groups]
    form.group_id.choices = group_choices
    
    # Establecer el grupo seleccionado actualmente
    if task.group_id:
        form.group_id.data = task.group_id
    else:
        form.group_id.data = 0
    
    # Si es una tarea personalizada, marcar los días seleccionados
    if task.frequency == TaskFrequency.PERSONALIZADA:
        # Obtener los días de la semana configurados
        weekdays = TaskWeekday.query.filter_by(task_id=task.id).all()
        days_of_week = [day.day_of_week for day in weekdays]
        
        # Marcar los checkboxes correspondientes
        form.monday.data = WeekDay.LUNES in days_of_week
        form.tuesday.data = WeekDay.MARTES in days_of_week
        form.wednesday.data = WeekDay.MIERCOLES in days_of_week
        form.thursday.data = WeekDay.JUEVES in days_of_week
        form.friday.data = WeekDay.VIERNES in days_of_week
        form.saturday.data = WeekDay.SABADO in days_of_week
        form.sunday.data = WeekDay.DOMINGO in days_of_week
    
    # Guardar la frecuencia original
    original_frequency = task.frequency
    
    if form.validate_on_submit():
        task.title = form.title.data
        task.description = form.description.data
        task.priority = TaskPriority(form.priority.data)
        task.frequency = TaskFrequency(form.frequency.data)
        task.start_date = form.start_date.data
        task.end_date = form.end_date.data
        
        # Actualizar grupo de tareas
        if form.group_id.data and form.group_id.data != 0:
            task.group_id = form.group_id.data
        else:
            task.group_id = None
            
        db.session.commit()
        
        # Si la frecuencia es personalizada, actualizar los días de la semana
        if task.frequency == TaskFrequency.PERSONALIZADA:
            # Eliminar días existentes
            TaskWeekday.query.filter_by(task_id=task.id).delete()
            
            # Crear nuevos registros de días seleccionados
            if form.monday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.LUNES))
            if form.tuesday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.MARTES))
            if form.wednesday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.MIERCOLES))
            if form.thursday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.JUEVES))
            if form.friday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.VIERNES))
            if form.saturday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.SABADO))
            if form.sunday.data:
                db.session.add(TaskWeekday(task_id=task.id, day_of_week=WeekDay.DOMINGO))
            
            db.session.commit()
        
        log_activity(f'Tarea actualizada: {task.title}')
        
        # Si cambió la frecuencia, redirigir a configurar el horario
        if original_frequency != task.frequency:
            # Eliminar horarios anteriores
            TaskSchedule.query.filter_by(task_id=task.id).delete()
            db.session.commit()
            
            flash(f'Tarea "{task.title}" actualizada. La frecuencia ha cambiado, configura el nuevo horario.', 'success')
            
            if task.frequency == TaskFrequency.DIARIA:
                return redirect(url_for('tasks.configure_daily_schedule', task_id=task.id))
            elif task.frequency == TaskFrequency.SEMANAL:
                return redirect(url_for('tasks.configure_weekly_schedule', task_id=task.id))
            elif task.frequency == TaskFrequency.MENSUAL:
                return redirect(url_for('tasks.configure_monthly_schedule', task_id=task.id))
            elif task.frequency == TaskFrequency.QUINCENAL:
                return redirect(url_for('tasks.configure_biweekly_schedule', task_id=task.id))
        else:
            flash(f'Tarea "{task.title}" actualizada correctamente.', 'success')
            return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    return render_template('tasks/task_form.html',
                          title=f'Editar Tarea: {task.title}',
                          form=form,
                          task=task,
                          location=task.location,
                          location_id=task.location_id)

@tasks_bp.route('/tasks/<int:task_id>/delete', methods=['POST'])
@login_required
@manager_required
```

#### `delete_task` (línea 1025)

**Docstring:**
```
Eliminar una tarea
```

**Código:**
```python
def delete_task(task_id):
    """Eliminar una tarea"""
    task = Task.query.get_or_404(task_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != task.location.company_id):
        flash('No tienes permiso para eliminar esta tarea.', 'danger')
        return redirect(url_for('tasks.list_tasks', location_id=task.location_id))
    
    location_id = task.location_id
    title = task.title
    
    # Eliminar primero las programaciones y completados
    TaskSchedule.query.filter_by(task_id=task.id).delete()
    TaskCompletion.query.filter_by(task_id=task.id).delete()
    
    db.session.delete(task)
    db.session.commit()
    
    log_activity(f'Tarea eliminada: {title}')
    flash(f'Tarea "{title}" eliminada correctamente.', 'success')
    return redirect(url_for('tasks.list_tasks', location_id=location_id))

@tasks_bp.route('/tasks/<int:task_id>')
@login_required
```

#### `view_task` (línea 1050)

**Docstring:**
```
Ver detalles de una tarea
```

**Código:**
```python
def view_task(task_id):
    """Ver detalles de una tarea"""
    task = Task.query.get_or_404(task_id)
    
    # Verificar permisos (admin, gerente o empleado de la empresa)
    if not current_user.is_admin() and not (current_user.company_id == task.location.company_id):
        flash('No tienes permiso para ver esta tarea.', 'danger')
        return redirect(url_for('tasks.index'))
    
    # Obtener programación
    schedule = TaskSchedule.query.filter_by(task_id=task.id).first()
    
    # Obtener historial de completados
    completions = (db.session.query(TaskCompletion, LocalUser)
                  .join(LocalUser, TaskCompletion.local_user_id == LocalUser.id)
                  .filter(TaskCompletion.task_id == task.id)
                  .order_by(TaskCompletion.completion_date.desc())
                  .all())
    
    return render_template('tasks/task_detail.html',
                          title=f'Tarea: {task.title}',
                          task=task,
                          schedule=schedule,
                          completions=completions)

# Portal de acceso para usuarios locales
@tasks_bp.route('/portal')
```

#### `portal_selection` (línea 1077)

**Docstring:**
```
Página de selección de portal
```

**Código:**
```python
def portal_selection():
    """Página de selección de portal"""
    # Añadir logging para diagnóstico
    print("[DEBUG] Accediendo a la selección de portal")
    try:
        # Obtener todos los locations disponibles
        locations = Location.query.filter_by(is_active=True).all()
        print(f"[DEBUG] Locales encontrados: {len(locations)}")
        return render_template('tasks/portal_selection.html',
                            title='Selección de Portal',
                            locations=locations)
    except Exception as e:
        print(f"[ERROR] Error en portal_selection: {str(e)}")
        # Devolver una respuesta mínima para evitar 500
        return f"Error: {str(e)}", 500

@tasks_bp.route('/portal-test')
```

#### `portal_test` (línea 1094)

**Docstring:**
```
Ruta de prueba para diagnóstico
```

**Código:**
```python
def portal_test():
    """Ruta de prueba para diagnóstico"""
    try:
        # Intentamos primero una consulta simple sin usar los campos nuevos
        locations = db.session.query(Location.id, Location.name).filter_by(is_active=True).all()
        location_count = len(locations)
        location_names = [loc[1] for loc in locations]
        
        # Ahora verificamos los campos nuevos
        has_new_columns = True
        try:
            test_query = db.session.query(Location.id, Location.portal_username).first()
        except Exception as e:
            has_new_columns = False
            column_error = str(e)
        
        # Generar respuesta informativa
        result = {
            "status": "ok",
            "locations_count": location_count,
            "locations": location_names,
            "has_new_columns": has_new_columns
        }
        
        if not has_new_columns:
            result["column_error"] = column_error
            
        return jsonify(result)
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)})

@tasks_bp.route('/portal/<int:location_id>', methods=['GET', 'POST'])
```

#### `portal_login` (línea 1126)

**Docstring:**
```
Página de login para acceder al portal de un local
```

**Código:**
```python
def portal_login(location_id):
    """Página de login para acceder al portal de un local"""
    location = Location.query.get_or_404(location_id)
    
    form = PortalLoginForm()
    
    # Mostrar información de las credenciales fijas al cargar la página
    if not request.method == 'POST':
        flash(f"Credenciales fijas para este portal: Usuario: {location.portal_fixed_username}, Contraseña: {location.portal_fixed_password}", 
              "info")
    
    if form.validate_on_submit():
        # Verificar el nombre de usuario
        if form.username.data == location.portal_fixed_username:
            # Verificar la contraseña usando el método de verificación específico
            if location.check_portal_password(form.password.data):
                # Guardar en sesión que estamos autenticados en este portal
                session['portal_authenticated'] = True
                session['portal_location_id'] = location.id
                
                # Redireccionar al portal real
                return redirect(url_for('tasks.local_portal', location_id=location.id))
            else:
                flash('Contraseña incorrecta.', 'danger')
        else:
            flash('Nombre de usuario incorrecto.', 'danger')
    
    return render_template('tasks/portal_login.html',
                          title=f'Acceso al Portal {location.name}',
                          location=location,
                          form=form)

@tasks_bp.route('/local-login', methods=['GET', 'POST'])
```

#### `local_login` (línea 1159)

**Docstring:**
```
Página de login para usuarios locales
```

**Código:**
```python
def local_login():
    """Página de login para usuarios locales"""
    # Verificar si ya hay un usuario local en la sesión
    if 'local_user_id' in session:
        return redirect(url_for('tasks.local_user_labels'))
    
    # Obtener todos los locales activos
    locations = Location.query.filter_by(is_active=True).all()
    
    # Si hay un local guardado en la sesión, usarlo
    saved_location_id = session.get('saved_location_id')
    
    # Si solo hay un local, redireccionar al portal
    if len(locations) == 1:
        location = locations[0]
        return redirect(url_for('tasks.local_portal', location_id=location.id))
    
    return render_template('tasks/local_login.html',
                          title='Acceso a Portal de Tareas',
                          locations=locations,
                          saved_location_id=saved_location_id)

@tasks_bp.route('/local-portal/<int:location_id>')
```

#### `local_portal` (línea 1182)

**Docstring:**
```
Portal de acceso para un local
```

**Código:**
```python
def local_portal(location_id):
    """Portal de acceso para un local"""
    location = Location.query.get_or_404(location_id)
    
    # Verificar si el usuario está autenticado en el portal
    if not session.get('portal_authenticated') or session.get('portal_location_id') != location.id:
        # Si no está autenticado, redireccionar al login del portal
        return redirect(url_for('tasks.portal_login', location_id=location.id))
    
    # Guardar ID del local en la sesión
    session['location_id'] = location_id
    
    # Obtener usuarios activos del local
    local_users = LocalUser.query.filter_by(location_id=location_id, is_active=True).all()
    
    # Si no hay usuarios, crear un admin por defecto
    if not local_users:
        try:
            # Crear usuario admin por defecto
            admin_user = LocalUser(
                name="Admin",
                last_name="Local",
                username=f"admin_{location_id}",
                is_active=True,
                location_id=location_id
            )
            
            # Establecer PIN 1234
            admin_user.set_pin("1234")
            
            db.session.add(admin_user)
            db.session.commit()
            
            # Refrescar la lista de usuarios
            local_users = [admin_user]
            
            # Mostrar mensaje informativo 
            flash("Se ha creado un usuario administrador por defecto con PIN: 1234", "info")
            
        except Exception as e:
            flash(f"No se pudo crear el usuario por defecto: {str(e)}", "warning")
    
    return render_template('tasks/local_portal.html',
                          title=f'Portal de {location.name}',
                          location=location,
                          local_users=local_users)

@tasks_bp.route('/local-user-login/<int:user_id>', methods=['GET', 'POST'])
```

#### `local_user_login` (línea 1230)

**Docstring:**
```
Login con PIN para empleado local
```

**Código:**
```python
def local_user_login(user_id):
    """Login con PIN para empleado local"""
    if 'location_id' not in session:
        return redirect(url_for('tasks.index'))
    
    user = LocalUser.query.get_or_404(user_id)
    
    # Verificar que pertenece al local correcto
    if user.location_id != session['location_id']:
        flash('Usuario no válido para este local.', 'danger')
        return redirect(url_for('tasks.local_portal', location_id=session['location_id']))
    
    form = LocalUserPinForm()
    
    if form.validate_on_submit():
        if user.check_pin(form.pin.data):
            # Guardar usuario en sesión
            session['local_user_id'] = user.id
            
            log_activity(f'Acceso de usuario local: {user.name} en {user.location.name}')
            flash(f'Bienvenido, {user.name}!', 'success')
            return redirect(url_for('tasks.local_user_tasks'))
        else:
            flash('PIN incorrecto.', 'danger')
    
    return render_template('tasks/local_user_pin.html',
                          title=f'Acceso de {user.name}',
                          form=form,
                          user=user)

@tasks_bp.route('/local-logout')
```

#### `local_logout` (línea 1261)

**Docstring:**
```
Cerrar sesión de usuario local
```

**Código:**
```python
def local_logout():
    """Cerrar sesión de usuario local"""
    location_id = session.get('location_id')
    session.pop('local_user_id', None)
    flash('Has cerrado sesión correctamente.', 'success')
    
    if location_id:
        return redirect(url_for('tasks.local_portal', location_id=location_id))
    else:
        return redirect(url_for('tasks.index'))

@tasks_bp.route('/portal-logout')
```

#### `portal_logout` (línea 1273)

**Docstring:**
```
Cerrar sesión de portal local
```

**Código:**
```python
def portal_logout():
    """Cerrar sesión de portal local"""
    # Limpiar todas las variables de sesión relacionadas con el portal
    session.pop('location_id', None)
    session.pop('local_user_username', None)
    session.pop('local_user_id', None)
    session.pop('portal_authenticated', None)
    session.pop('portal_location_id', None)
    
    flash('Has cerrado sesión del portal correctamente.', 'success')
    return redirect(url_for('tasks.index'))

@tasks_bp.route('/local-user/tasks')
@tasks_bp.route('/local-user/tasks/<string:date_str>')
@tasks_bp.route('/local-user/tasks/group/<int:group_id>')
@tasks_bp.route('/local-user/tasks/<string:date_str>/group/<int:group_id>')
@local_user_required
```

#### `local_user_tasks` (línea 1290)

**Docstring:**
```
Panel de tareas para usuario local
```

**Código:**
```python
def local_user_tasks(date_str=None, group_id=None):
    """Panel de tareas para usuario local"""
    user_id = session['local_user_id']
    user = LocalUser.query.get_or_404(user_id)
    location = user.location
    
    # Determinar la fecha de las tareas (hoy por defecto, o la especificada)
    today = date.today()
    if date_str:
        try:
            selected_date = datetime.strptime(date_str, '%Y-%m-%d').date()
        except ValueError:
            selected_date = today
    else:
        selected_date = today
    
    # Calcular fechas para el carrusel (día anterior, actual, siguiente)
    prev_date = selected_date - timedelta(days=1)
    next_date = selected_date + timedelta(days=1)
    
    # Determinar los nombres de los días en español
    days_map = {
        0: 'LUN',
        1: 'MAR',
        2: 'MIÉ',
        3: 'JUE',
        4: 'VIE', 
        5: 'SÁB',
        6: 'DOM'
    }
    
    # Configurar el carrusel de fechas, preservando el filtro de grupo si existe
    base_url_params = {}
    if group_id is not None:
        base_url_params['group_id'] = group_id
        
    date_carousel = [
        {
            'date': prev_date,
            'day_name': days_map[prev_date.weekday()],
            'day': prev_date.day,
            'url': url_for('tasks.local_user_tasks', date_str=prev_date.strftime('%Y-%m-%d'), **base_url_params),
            'active': False
        },
        {
            'date': selected_date,
            'day_name': days_map[selected_date.weekday()],
            'day': selected_date.day,
            'url': url_for('tasks.local_user_tasks', date_str=selected_date.strftime('%Y-%m-%d'), **base_url_params),
            'active': True
        },
        {
            'date': next_date, 
            'day_name': days_map[next_date.weekday()],
            'day': next_date.day,
            'url': url_for('tasks.local_user_tasks', date_str=next_date.strftime('%Y-%m-%d'), **base_url_params),
            'active': False
        }
    ]
    
    # Inicializar colecciones
    active_tasks = []
    grouped_tasks = {}  # Diccionario para agrupar tareas por grupo
    ungrouped_tasks = [] # Lista para tareas sin grupo
    
    # Aplicar filtro de grupo si es necesario
    tasks_query = Task.query.filter_by(location_id=location.id, status=TaskStatus.PENDIENTE)
    
    # Si hay un grupo_id especificado, filtrar por ese grupo
    if group_id is not None:
        if group_id == 0:  # Tareas sin grupo
            tasks_query = tasks_query.filter(Task.group_id == None)
        else:  # Tareas del grupo específico
            tasks_query = tasks_query.filter(Task.group_id == group_id)
    
    # Obtener todas las tareas pendientes (aplicando los filtros)
    pending_tasks = tasks_query.all()
    
    # Obtener grupos de tareas para esta ubicación
    task_groups = TaskGroup.query.filter_by(location_id=location.id).all()
    group_dict = {group.id: group for group in task_groups}
    
    # Obtener el grupo actual si se especificó
    current_group = None
    if group_id and group_id > 0:
        current_group = TaskGroup.query.get(group_id)
    
    # Obtener todas las completadas de la fecha seleccionada
    all_completions = db.session.query(
        TaskCompletion, LocalUser
    ).join(
        LocalUser, TaskCompletion.local_user_id == LocalUser.id
    ).filter(
        TaskCompletion.task_id.in_([t.id for t in pending_tasks]),
        db.func.date(TaskCompletion.completion_date) == selected_date
    ).order_by(
        TaskCompletion.completion_date.desc()
    ).all()
    
    # Crear un diccionario de información de completado por tarea
    completion_info = {}
    for completion, local_user in all_completions:
        completion_info[completion.task_id] = {
            'user': f"{local_user.name} {local_user.last_name}",
            'time': completion.completion_date.strftime('%H:%M'),
            'completed_by_me': local_user.id == user_id
        }
    
    # Completiones específicas para este usuario en la fecha seleccionada
    user_completions = TaskCompletion.query.filter_by(
        local_user_id=user_id
    ).filter(
        db.func.date(TaskCompletion.completion_date) == selected_date
    ).order_by(
        TaskCompletion.completion_date.desc()
    ).all()
    
    # Lista de IDs de tareas completadas en la fecha seleccionada
    completed_task_ids = [c.task_id for c, _ in all_completions]
    
    # Función auxiliar para comprobar si una tarea debe aparecer en la fecha seleccionada
    def task_is_due_on_date(task, check_date):
        # Si la fecha está fuera del rango de fechas de la tarea, no es debido
        if task.start_date and check_date < task.start_date:
            return False
        if task.end_date and check_date > task.end_date:
            return False
        
        # Verificar frecuencia
        if task.frequency == TaskFrequency.DIARIA:
            return True
        
        # Para tareas semanales, verificar día de la semana
        if task.frequency == TaskFrequency.SEMANAL:
            weekday_name = WeekDay(days_map[check_date.weekday()].lower())
            for schedule in task.schedule_details:
                if schedule.day_of_week and schedule.day_of_week.value == weekday_name.value:
                    return True
            return False
        
        # Para tareas quincenales, verificar quincena
        if task.frequency == TaskFrequency.QUINCENAL:
            start_date = task.start_date or task.created_at.date()
            days_diff = (check_date - start_date).days
            return days_diff % 14 == 0
        
        # Para tareas mensuales, verificar día del mes
        if task.frequency == TaskFrequency.MENSUAL:
            # Si hay horario mensual definido, verificar día del mes
            schedules = [s for s in task.schedule_details if s.day_of_month]
            if schedules:
                return any(s.day_of_month == check_date.day for s in schedules)
            # Si no hay horario específico, usar el día de inicio como referencia
            start_date = task.start_date or task.created_at.date()
            return start_date.day == check_date.day
        
        # Para tareas personalizadas, verificar días específicos
        if task.frequency == TaskFrequency.PERSONALIZADA:
            # Verificar si alguno de los días de la semana coincide
            weekday_value = days_map[check_date.weekday()].lower()
            for weekday in task.weekdays:
                if weekday.day_of_week.value == weekday_value:
                    return True
            return False
        
        return False
    
    for task in pending_tasks:
        # Usar la nueva función para verificar tareas en fechas específicas
        task_due = False
        if selected_date == today:
            # Para hoy, usar el método existente por compatibilidad
            task_due = task.is_due_today()
        else:
            # Para otras fechas, usar nuestra nueva lógica
            task_due = task_is_due_on_date(task, selected_date)
            
        if task_due:
            # Verificar si ya ha sido completada
            task.completed_today = task.id in completed_task_ids
            
            # Agregar información de quién completó la tarea
            if task.id in completion_info:
                task.completion_info = completion_info[task.id]
            
            active_tasks.append(task)
            
            # Agrupar por grupo si tiene uno
            if task.group_id and task.group_id in group_dict:
                task_group_id = task.group_id
                if task_group_id not in grouped_tasks:
                    grouped_tasks[task_group_id] = {
                        'group': group_dict[task_group_id],
                        'tasks': []
                    }
                grouped_tasks[task_group_id]['tasks'].append(task)
            else:
                ungrouped_tasks.append(task)
    
    # Personalizar título en base al filtro
    if current_group:
        title = f'Tareas de {current_group.name} - {user.name}'
    elif group_id == 0:
        title = f'Tareas sin clasificar - {user.name}'
    else:
        title = f'Tareas de {user.name}'
        
    return render_template('tasks/local_user_tasks.html',
                          title=title,
                          user=user,
                          local_user=user,
                          location=location,
                          active_tasks=active_tasks,
                          ungrouped_tasks=ungrouped_tasks,
                          grouped_tasks=grouped_tasks,
                          task_groups=task_groups,
                          completed_tasks=user_completions,
                          selected_date=selected_date,
                          today=today, 
                          date_carousel=date_carousel,
                          current_group=current_group,
                          group_id=group_id)

@tasks_bp.route('/local-user/tasks/<int:task_id>/complete', methods=['GET', 'POST'])
@local_user_required
```

#### `task_is_due_on_date` (línea 1411)

**Código:**
```python
    def task_is_due_on_date(task, check_date):
        # Si la fecha está fuera del rango de fechas de la tarea, no es debido
        if task.start_date and check_date < task.start_date:
            return False
        if task.end_date and check_date > task.end_date:
            return False
        
        # Verificar frecuencia
        if task.frequency == TaskFrequency.DIARIA:
            return True
        
        # Para tareas semanales, verificar día de la semana
        if task.frequency == TaskFrequency.SEMANAL:
            weekday_name = WeekDay(days_map[check_date.weekday()].lower())
            for schedule in task.schedule_details:
                if schedule.day_of_week and schedule.day_of_week.value == weekday_name.value:
                    return True
            return False
        
        # Para tareas quincenales, verificar quincena
        if task.frequency == TaskFrequency.QUINCENAL:
            start_date = task.start_date or task.created_at.date()
            days_diff = (check_date - start_date).days
            return days_diff % 14 == 0
        
        # Para tareas mensuales, verificar día del mes
        if task.frequency == TaskFrequency.MENSUAL:
            # Si hay horario mensual definido, verificar día del mes
            schedules = [s for s in task.schedule_details if s.day_of_month]
            if schedules:
                return any(s.day_of_month == check_date.day for s in schedules)
            # Si no hay horario específico, usar el día de inicio como referencia
            start_date = task.start_date or task.created_at.date()
            return start_date.day == check_date.day
        
        # Para tareas personalizadas, verificar días específicos
        if task.frequency == TaskFrequency.PERSONALIZADA:
            # Verificar si alguno de los días de la semana coincide
            weekday_value = days_map[check_date.weekday()].lower()
            for weekday in task.weekdays:
                if weekday.day_of_week.value == weekday_value:
                    return True
            return False
        
        return False
    
    for task in pending_tasks:
        # Usar la nueva función para verificar tareas en fechas específicas
        task_due = False
        if selected_date == today:
            # Para hoy, usar el método existente por compatibilidad
            task_due = task.is_due_today()
        else:
            # Para otras fechas, usar nuestra nueva lógica
            task_due = task_is_due_on_date(task, selected_date)
            
        if task_due:
            # Verificar si ya ha sido completada
            task.completed_today = task.id in completed_task_ids
            
            # Agregar información de quién completó la tarea
            if task.id in completion_info:
                task.completion_info = completion_info[task.id]
            
            active_tasks.append(task)
            
            # Agrupar por grupo si tiene uno
            if task.group_id and task.group_id in group_dict:
                task_group_id = task.group_id
                if task_group_id not in grouped_tasks:
                    grouped_tasks[task_group_id] = {
                        'group': group_dict[task_group_id],
                        'tasks': []
                    }
                grouped_tasks[task_group_id]['tasks'].append(task)
            else:
                ungrouped_tasks.append(task)
    
    # Personalizar título en base al filtro
    if current_group:
        title = f'Tareas de {current_group.name} - {user.name}'
    elif group_id == 0:
        title = f'Tareas sin clasificar - {user.name}'
    else:
        title = f'Tareas de {user.name}'
        
    return render_template('tasks/local_user_tasks.html',
                          title=title,
                          user=user,
                          local_user=user,
                          location=location,
                          active_tasks=active_tasks,
                          ungrouped_tasks=ungrouped_tasks,
                          grouped_tasks=grouped_tasks,
                          task_groups=task_groups,
                          completed_tasks=user_completions,
                          selected_date=selected_date,
                          today=today, 
                          date_carousel=date_carousel,
                          current_group=current_group,
                          group_id=group_id)

```

#### `complete_task` (línea 1515)

**Docstring:**
```
Marcar una tarea como completada (versión con formulario)
```

**Código:**
```python
def complete_task(task_id):
    """Marcar una tarea como completada (versión con formulario)"""
    user_id = session['local_user_id']
    user = LocalUser.query.get_or_404(user_id)
    task = Task.query.get_or_404(task_id)
    
    # Verificar que la tarea pertenece al local del usuario
    if task.location_id != user.location_id:
        flash('Tarea no válida para este local.', 'danger')
        return redirect(url_for('tasks.local_user_tasks'))
    
    # Verificar si ya ha sido completada hoy por este usuario
    today = date.today()
    completion = TaskCompletion.query.filter_by(
        task_id=task.id,
        local_user_id=user_id
    ).filter(
        db.func.date(TaskCompletion.completion_date) == today
    ).first()
    
    if completion:
        flash('Ya has completado esta tarea hoy.', 'warning')
        return redirect(url_for('tasks.local_user_tasks'))
    
    form = TaskCompletionForm()
    
    if form.validate_on_submit():
        completion = TaskCompletion(
            task_id=task.id,
            local_user_id=user_id,
            notes=form.notes.data
        )
        
        db.session.add(completion)
        db.session.commit()
        
        log_activity(f'Tarea completada: {task.title} por {user.name}')
        flash('¡Tarea completada correctamente!', 'success')
        return redirect(url_for('tasks.local_user_tasks'))
    
    return render_template('tasks/complete_task.html',
                          title=f'Completar Tarea: {task.title}',
                          form=form,
                          task=task,
                          user=user)

@tasks_bp.route('/local-user/tasks/<int:task_id>/ajax-complete', methods=['POST'])
@local_user_required
```

#### `ajax_complete_task` (línea 1563)

**Docstring:**
```
Marcar una tarea como completada (versión AJAX)
```

**Código:**
```python
def ajax_complete_task(task_id):
    """Marcar una tarea como completada (versión AJAX)"""
    user_id = session['local_user_id']
    user = LocalUser.query.get_or_404(user_id)
    task = Task.query.get_or_404(task_id)
    
    # Verificar que la solicitud es AJAX
    if not request.is_json:
        return jsonify({'error': 'Se requiere petición JSON'}), 400
    
    # Verificar que la tarea pertenece al local del usuario
    if task.location_id != user.location_id:
        return jsonify({'error': 'Tarea no válida para este local'}), 403
    
    # Verificar si ya ha sido completada hoy por este usuario
    today = date.today()
    existing_completion = TaskCompletion.query.filter_by(
        task_id=task.id,
        local_user_id=user_id
    ).filter(
        db.func.date(TaskCompletion.completion_date) == today
    ).first()
    
    if existing_completion:
        return jsonify({'error': 'Ya has completado esta tarea hoy'}), 400
    
    # Obtener notas (opcional)
    data = request.json
    notes = data.get('notes', '')
    
    # Crear registro de tarea completada
    completion = TaskCompletion(
        task_id=task.id,
        local_user_id=user_id,
        notes=notes
    )
    
    db.session.add(completion)
    db.session.commit()
    
    log_activity(f'Tarea completada (AJAX): {task.title} por {user.name}')
    
    # Devolver información para actualizar la UI
    return jsonify({
        'success': True,
        'taskId': task.id,
        'taskTitle': task.title,
        'completedBy': f"{user.name} {user.last_name}",
        'completedAt': datetime.now().strftime('%H:%M')
    })

# API para regenerar contraseña del portal
@tasks_bp.route('/api/regenerate-password/<int:location_id>', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `regenerate_password` (línea 1618)

**Docstring:**
```
Devuelve la contraseña fija del portal de un local
```

**Código:**
```python
def regenerate_password(location_id):
    """Devuelve la contraseña fija del portal de un local"""
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        return jsonify({'error': 'No tienes permiso para obtener la contraseña de este local'}), 403
    
    # Devolver la contraseña fija
    fixed_password = location.portal_fixed_password
    
    return jsonify({'success': True, 'password': fixed_password})

# Actualizar credenciales personalizadas del portal
@tasks_bp.route('/locations/<int:location_id>/update-credentials', methods=['POST'])
@login_required
@manager_required
```

#### `update_portal_credentials` (línea 1635)

**Docstring:**
```
Actualiza la contraseña personalizada del portal para un local
```

**Código:**
```python
def update_portal_credentials(location_id):
    """Actualiza la contraseña personalizada del portal para un local"""
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        flash('No tienes permiso para modificar las credenciales de este local', 'danger')
        return redirect(url_for('tasks.list_locations'))
    
    # Obtener datos del formulario
    custom_password = request.form.get('custom_password', '').strip()
    
    try:
        # Actualizar contraseña personalizada solo si se ha proporcionado una nueva
        if custom_password:
            location.set_portal_password(custom_password)
            db.session.commit()
            
            # Registrar cambio en los logs
            log_activity(f'Actualización de contraseña del portal para local: {location.name}', user_id=current_user.id)
            
            flash('Contraseña del portal actualizada correctamente', 'success')
        else:
            flash('No se ha proporcionado una nueva contraseña', 'warning')
    except Exception as e:
        db.session.rollback()
        flash(f'Error al actualizar las credenciales: {str(e)}', 'danger')
    
    return redirect(url_for('tasks.view_location', id=location_id))

# API para obtener credenciales del portal
@tasks_bp.route('/locations/<int:location_id>/get-credentials', methods=['POST'])
@login_required
@manager_required
```

#### `get_portal_credentials` (línea 1669)

**Docstring:**
```
Obtiene las credenciales fijas del portal mediante AJAX de forma segura
```

**Código:**
```python
def get_portal_credentials(location_id):
    """Obtiene las credenciales fijas del portal mediante AJAX de forma segura"""
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or current_user.company_id != location.company_id):
        return jsonify({'success': False, 'error': 'No tienes permiso para obtener las credenciales de este local'}), 403
    
    # Registrar acceso a las credenciales en los logs
    log_activity(f'Acceso a credenciales de portal para local: {location.name}', user_id=current_user.id)
    
    # Obtener el nombre de usuario
    username = location.portal_fixed_username
    
    # Para la contraseña, tenemos un caso especial
    password = location.portal_fixed_password
    
    # Si la contraseña es None, significa que hay una contraseña personalizada (hash)
    # y no podemos mostrarla directamente - mostramos un marcador
    has_custom_password = location.portal_password_hash is not None
    password_placeholder = "********" if has_custom_password else password
    
    return jsonify({
        'success': True,
        'username': username,
        'password': password_placeholder,
        'has_custom_password': has_custom_password
    })

# API para estadísticas en tiempo real
@tasks_bp.route('/api/task-stats')
@login_required
```

#### `task_stats` (línea 1701)

**Docstring:**
```
API para obtener estadísticas de tareas
```

**Código:**
```python
def task_stats():
    """API para obtener estadísticas de tareas"""
    if current_user.is_admin():
        # Administradores ven estadísticas globales
        locations = Location.query.all()
        location_ids = [loc.id for loc in locations]
    elif current_user.is_gerente() and current_user.company_id:
        # Gerentes ven solo su empresa
        locations = Location.query.filter_by(company_id=current_user.company_id).all()
        location_ids = [loc.id for loc in locations]
    else:
        # Otros usuarios no ven nada
        return jsonify({'error': 'No autorizado'}), 403
    
    # Si no hay locales, devolver valores vacíos
    if not location_ids:
        return jsonify({
            'total_tasks': 0,
            'pending_tasks': 0,
            'completed_tasks': 0,
            'completion_rate': 0,
            'tasks_by_priority': {},
            'tasks_by_location': {}
        })
    
    # Contar tareas
    total_tasks = Task.query.filter(Task.location_id.in_(location_ids)).count()
    pending_tasks = Task.query.filter(Task.location_id.in_(location_ids), 
                                     Task.status==TaskStatus.PENDIENTE).count()
    completed_tasks = Task.query.filter(Task.location_id.in_(location_ids), 
                                       Task.status==TaskStatus.COMPLETADA).count()
    
    # Calcular tasa de completado
    completion_rate = (completed_tasks / total_tasks * 100) if total_tasks > 0 else 0
    
    # Tareas por prioridad
    tasks_by_priority_query = db.session.query(
        Task.priority,
        db.func.count(Task.id)
    ).filter(
        Task.location_id.in_(location_ids)
    ).group_by(Task.priority).all()
    
    tasks_by_priority = {}
    for priority, count in tasks_by_priority_query:
        if priority:
            tasks_by_priority[priority.value] = count
    
    # Tareas por local
    tasks_by_location_query = db.session.query(
        Location.name,
        db.func.count(Task.id)
    ).join(
        Task, Location.id == Task.location_id
    ).filter(
        Location.id.in_(location_ids)
    ).group_by(Location.name).all()
    
    tasks_by_location = {}
    for location_name, count in tasks_by_location_query:
        tasks_by_location[location_name] = count
    
    return jsonify({
        'total_tasks': total_tasks,
        'pending_tasks': pending_tasks,
        'completed_tasks': completed_tasks,
        'completion_rate': completion_rate,
        'tasks_by_priority': tasks_by_priority,
        'tasks_by_location': tasks_by_location
    })

# Página de etiquetas para usuario local
@tasks_bp.route('/local-user/labels')
@local_user_required
```

#### `local_user_labels` (línea 1775)

**Docstring:**
```
Generador de etiquetas para productos - Lista de productos disponibles
```

**Código:**
```python
def local_user_labels():
    """Generador de etiquetas para productos - Lista de productos disponibles"""
    user_id = session['local_user_id']
    user = LocalUser.query.get_or_404(user_id)
    location = user.location
    
    # Obtener los productos disponibles para este local
    products = Product.query.filter_by(location_id=location.id, is_active=True).order_by(Product.name).all()
    
    # Filtro de búsqueda (si existe)
    search_query = request.args.get('q', '')
    if search_query:
        products = [p for p in products if search_query.lower() in p.name.lower()]
    
    return render_template('tasks/local_user_labels.html',
                          title='Selección de Producto',
                          user=user,
                          location=location,
                          products=products,
                          search_query=search_query)

# Página de selección de conservación para un producto específico
@tasks_bp.route('/local-user/labels/<int:product_id>')
@local_user_required
```

#### `product_conservation_selection` (línea 1799)

**Docstring:**
```
Selección de tipo de conservación para un producto específico
```

**Código:**
```python
def product_conservation_selection(product_id):
    """Selección de tipo de conservación para un producto específico"""
    user_id = session['local_user_id']
    user = LocalUser.query.get_or_404(user_id)
    location = user.location
    
    # Obtener el producto
    product = Product.query.get_or_404(product_id)
    
    # Verificar que el producto pertenece al local del usuario
    if product.location_id != user.location_id:
        flash("El producto seleccionado no pertenece a este local", "danger")
        return redirect(url_for('tasks.local_user_labels'))
    
    # Obtener la fecha y hora actual
    now = datetime.now()
    
    return render_template('tasks/product_conservation_selection.html',
                          title=f'Etiqueta para: {product.name}',
                          user=user,
                          location=location,
                          product=product,
                          now=now)

# Gestor de etiquetas en la página de tareas
@tasks_bp.route('/dashboard/labels')
@tasks_bp.route('/dashboard/labels/<int:location_id>')
@login_required
@manager_required
```

#### `manage_labels` (línea 1828)

**Docstring:**
```
Gestor de etiquetas para la página de tareas, filtrado por ubicación si se especifica
```

**Código:**
```python
def manage_labels(location_id=None):
    """Gestor de etiquetas para la página de tareas, filtrado por ubicación si se especifica"""
    companies = []
    
    # Filtrar empresas según el rol del usuario
    if current_user.is_admin():
        companies = Company.query.all()
    else:
        companies = current_user.companies
    
    # Obtener ubicaciones asociadas a las empresas que puede ver
    company_ids = [c.id for c in companies]
    
    # Si se especifica una ubicación, verificar permisos
    location = None
    if location_id:
        location = Location.query.get_or_404(location_id)
        if location.company_id not in company_ids and not current_user.is_admin():
            flash('No tiene permisos para acceder a esta ubicación', 'danger')
            return redirect(url_for('tasks.index'))
        
        # Filtrar sólo por la ubicación especificada
        locations = [location]
        location_ids = [location_id]
    else:
        # Sin filtro de ubicación, mostrar todas las ubicaciones permitidas
        locations = Location.query.filter(Location.company_id.in_(company_ids)).all()
        location_ids = [loc.id for loc in locations]
    
    # Obtener todos los productos de las ubicaciones permitidas
    products = []
    if locations:
        products = Product.query.filter(Product.location_id.in_(location_ids)).order_by(Product.name).all()
    
    # Obtener etiquetas generadas recientemente (últimos 30 días)
    recent_labels = []
    if products:  # Si hay productos
        thirty_days_ago = datetime.now() - timedelta(days=30)
        
        recent_labels = db.session.query(
            ProductLabel, Product, LocalUser
        ).join(
            Product, ProductLabel.product_id == Product.id
        ).join(
            LocalUser, ProductLabel.local_user_id == LocalUser.id
        ).filter(
            Product.location_id.in_(location_ids),
            ProductLabel.created_at > thirty_days_ago
        ).order_by(
            ProductLabel.created_at.desc()
        ).limit(50).all()
    
    título = f'Etiquetas de {location.name}' if location else 'Gestor de Etiquetas'
    
    return render_template('tasks/manage_labels.html',
                          title=título,
                          companies=companies,
                          locations=locations,
                          selected_location=location,
                          products=products,
                          recent_labels=recent_labels)

@tasks_bp.route('/locations/<int:location_id>/label-editor', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `label_editor` (línea 1893)

**Docstring:**
```
Editor de diseño de etiquetas para un local
```

**Código:**
```python
def label_editor(location_id):
    """Editor de diseño de etiquetas para un local"""
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos
    if not current_user.is_admin() and (not current_user.is_gerente() or location.company_id not in [c.id for c in current_user.companies]):
        flash('No tienes permiso para editar etiquetas en este local.', 'danger')
        return redirect(url_for('tasks.manage_labels'))
    
    # Buscar plantilla existente o crear una nueva
    template = LabelTemplate.query.filter_by(location_id=location_id, is_default=True).first()
    
    if not template:
        template = LabelTemplate(
            name="Diseño Predeterminado",
            location_id=location_id,
            is_default=True
        )
        db.session.add(template)
        db.session.commit()
    
    form = LabelEditorForm(obj=template)
    
    if form.validate_on_submit():
        # Actualizar la plantilla con los datos del formulario
        form.populate_obj(template)
        template.name = form.layout_name.data
        template.updated_at = datetime.utcnow()
        
        db.session.commit()
        
        log_activity(f'Diseño de etiquetas actualizado para {location.name}')
        flash('Diseño de etiquetas actualizado correctamente.', 'success')
        return redirect(url_for('tasks.manage_labels', location_id=location_id))
    
    return render_template('tasks/label_editor.html',
                          title=f'Editor de Etiquetas - {location.name}',
                          form=form,
                          location=location,
                          template=template)

@tasks_bp.route('/locations/<int:location_id>/label-templates', methods=['GET'])
@login_required
@manager_required
```

#### `list_label_templates` (línea 1937)

**Docstring:**
```
Lista de plantillas de etiquetas para un local
```

**Código:**
```python
def list_label_templates(location_id):
    """Lista de plantillas de etiquetas para un local"""
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos
    if not current_user.is_admin() and (not current_user.is_gerente() or location.company_id not in [c.id for c in current_user.companies]):
        flash('No tienes permiso para ver plantillas de etiquetas en este local.', 'danger')
        return redirect(url_for('tasks.manage_labels'))
    
    templates = LabelTemplate.query.filter_by(location_id=location_id).all()
    
    return render_template('tasks/label_template_list.html',
                          title=f'Plantillas de Etiquetas - {location.name}',
                          location=location,
                          templates=templates)

@tasks_bp.route('/locations/<int:location_id>/label-templates/create', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `create_label_template` (línea 1956)

**Docstring:**
```
Crear una nueva plantilla de etiquetas
```

**Código:**
```python
def create_label_template(location_id):
    """Crear una nueva plantilla de etiquetas"""
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos
    if not current_user.is_admin() and (not current_user.is_gerente() or location.company_id not in [c.id for c in current_user.companies]):
        flash('No tienes permiso para crear plantillas de etiquetas en este local.', 'danger')
        return redirect(url_for('tasks.manage_labels'))
    
    form = LabelEditorForm()
    
    if form.validate_on_submit():
        template = LabelTemplate(
            name=form.layout_name.data,
            location_id=location_id,
            is_default=False,
            titulo_x=form.titulo_x.data,
            titulo_y=form.titulo_y.data,
            titulo_size=form.titulo_size.data,
            titulo_bold=form.titulo_bold.data,
            conservacion_x=form.conservacion_x.data,
            conservacion_y=form.conservacion_y.data,
            conservacion_size=form.conservacion_size.data,
            conservacion_bold=form.conservacion_bold.data,
            preparador_x=form.preparador_x.data,
            preparador_y=form.preparador_y.data,
            preparador_size=form.preparador_size.data,
            preparador_bold=form.preparador_bold.data,
            fecha_x=form.fecha_x.data,
            fecha_y=form.fecha_y.data,
            fecha_size=form.fecha_size.data,
            fecha_bold=form.fecha_bold.data,
            caducidad_x=form.caducidad_x.data,
            caducidad_y=form.caducidad_y.data,
            caducidad_size=form.caducidad_size.data,
            caducidad_bold=form.caducidad_bold.data,
            caducidad2_x=form.caducidad2_x.data,
            caducidad2_y=form.caducidad2_y.data,
            caducidad2_size=form.caducidad2_size.data,
            caducidad2_bold=form.caducidad2_bold.data
        )
        
        db.session.add(template)
        db.session.commit()
        
        log_activity(f'Plantilla de etiquetas creada: {template.name} para {location.name}')
        flash(f'Plantilla "{template.name}" creada correctamente.', 'success')
        return redirect(url_for('tasks.list_label_templates', location_id=location_id))
    
    return render_template('tasks/label_editor.html',
                          title=f'Nueva Plantilla de Etiquetas - {location.name}',
                          form=form,
                          location=location,
                          is_new=True)

@tasks_bp.route('/label-templates/<int:template_id>/edit', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `edit_label_template` (línea 2014)

**Docstring:**
```
Editar una plantilla de etiquetas existente
```

**Código:**
```python
def edit_label_template(template_id):
    """Editar una plantilla de etiquetas existente"""
    template = LabelTemplate.query.get_or_404(template_id)
    location = Location.query.get_or_404(template.location_id)
    
    # Verificar permisos
    if not current_user.is_admin() and (not current_user.is_gerente() or location.company_id not in [c.id for c in current_user.companies]):
        flash('No tienes permiso para editar plantillas de etiquetas en este local.', 'danger')
        return redirect(url_for('tasks.manage_labels'))
    
    form = LabelEditorForm(obj=template)
    
    if form.validate_on_submit():
        # Actualizar la plantilla con los datos del formulario
        form.populate_obj(template)
        template.updated_at = datetime.utcnow()
        
        db.session.commit()
        
        log_activity(f'Plantilla de etiquetas actualizada: {template.name}')
        flash(f'Plantilla "{template.name}" actualizada correctamente.', 'success')
        return redirect(url_for('tasks.list_label_templates', location_id=location.id))
    
    return render_template('tasks/label_editor.html',
                          title=f'Editar Plantilla: {template.name}',
                          form=form,
                          location=location,
                          template=template)

@tasks_bp.route('/label-templates/<int:template_id>/delete', methods=['POST'])
@login_required
@manager_required
```

#### `delete_label_template` (línea 2046)

**Docstring:**
```
Eliminar una plantilla de etiquetas
```

**Código:**
```python
def delete_label_template(template_id):
    """Eliminar una plantilla de etiquetas"""
    template = LabelTemplate.query.get_or_404(template_id)
    location = Location.query.get_or_404(template.location_id)
    
    # Verificar permisos
    if not current_user.is_admin() and (not current_user.is_gerente() or location.company_id not in [c.id for c in current_user.companies]):
        flash('No tienes permiso para eliminar plantillas de etiquetas en este local.', 'danger')
        return redirect(url_for('tasks.manage_labels'))
    
    # No permitir eliminar la plantilla predeterminada
    if template.is_default:
        flash('No puedes eliminar la plantilla predeterminada.', 'warning')
        return redirect(url_for('tasks.list_label_templates', location_id=location.id))
    
    name = template.name
    location_id = location.id
    
    db.session.delete(template)
    db.session.commit()
    
    log_activity(f'Plantilla de etiquetas eliminada: {name}')
    flash(f'Plantilla "{name}" eliminada correctamente.', 'success')
    return redirect(url_for('tasks.list_label_templates', location_id=location_id))

@tasks_bp.route('/label-templates/<int:template_id>/set-default', methods=['POST'])
@login_required
@manager_required
```

#### `set_default_label_template` (línea 2074)

**Docstring:**
```
Establecer una plantilla como predeterminada
```

**Código:**
```python
def set_default_label_template(template_id):
    """Establecer una plantilla como predeterminada"""
    template = LabelTemplate.query.get_or_404(template_id)
    location = Location.query.get_or_404(template.location_id)
    
    # Verificar permisos
    if not current_user.is_admin() and (not current_user.is_gerente() or location.company_id not in [c.id for c in current_user.companies]):
        flash('No tienes permiso para modificar plantillas de etiquetas en este local.', 'danger')
        return redirect(url_for('tasks.manage_labels'))
    
    # Quitar el estado predeterminado de todas las plantillas de esta ubicación
    default_templates = LabelTemplate.query.filter_by(location_id=location.id, is_default=True).all()
    for default_template in default_templates:
        default_template.is_default = False
    
    # Establecer esta plantilla como predeterminada
    template.is_default = True
    db.session.commit()
    
    log_activity(f'Plantilla de etiquetas establecida como predeterminada: {template.name}')
    flash(f'Plantilla "{template.name}" establecida como predeterminada.', 'success')
    return redirect(url_for('tasks.list_label_templates', location_id=location.id))

@tasks_bp.route('/dashboard/labels/download-template')
@login_required
@manager_required
```

#### `download_excel_template` (línea 2100)

**Docstring:**
```
Descargar plantilla vacía en Excel para importación de productos
```

**Código:**
```python
def download_excel_template():
    """Descargar plantilla vacía en Excel para importación de productos"""
    # Crear un nuevo libro de Excel
    wb = openpyxl.Workbook()
    ws = wb.active
    ws.title = "Productos"
    
    # Añadir encabezados
    ws['A1'] = "Nombre"
    ws['B1'] = "Descripción"
    ws['C1'] = "Vida útil (días)"
    ws['D1'] = "Descongelación (horas)"
    ws['E1'] = "Refrigeración (horas)"
    ws['F1'] = "Gastro (horas)"
    ws['G1'] = "Caliente (horas)"
    ws['H1'] = "Seco (horas)"
    
    # Añadir una fila de ejemplo
    ws['A2'] = "Ejemplo Producto"
    ws['B2'] = "Descripción de ejemplo"
    ws['C2'] = 12  # Vida útil secundaria en días
    ws['D2'] = 48  # Horas para descongelación (2 días)
    ws['E2'] = 72  # Horas para refrigeración (3 días) 
    ws['F2'] = 96  # Horas para gastro (4 días)
    ws['G2'] = 2   # Horas para caliente
    ws['H2'] = 168 # Horas para seco (7 días)
    
    # Guardar a un objeto BytesIO
    output = io.BytesIO()
    wb.save(output)
    output.seek(0)
    
    # Crear nombre de archivo para la plantilla
    filename = f"plantilla_productos_{datetime.now().strftime('%Y%m%d')}.xlsx"
    
    return send_file(
        output,
        download_name=filename,
        as_attachment=True,
        mimetype='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    )

@tasks_bp.route('/dashboard/labels/export/<int:location_id>')
@login_required
@manager_required
```

#### `export_labels_excel` (línea 2145)

**Docstring:**
```
Exportar lista de productos y tipos de conservación a Excel
```

**Código:**
```python
def export_labels_excel(location_id):
    """Exportar lista de productos y tipos de conservación a Excel"""
    # Verificar que el local existe y el usuario tiene permisos
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or 
                                       location.company_id not in [c.id for c in current_user.companies]):
        flash('No tienes permiso para exportar datos de este local.', 'danger')
        return redirect(url_for('tasks.manage_labels'))
    
    # Crear un nuevo libro de Excel
    wb = openpyxl.Workbook()
    ws = wb.active
    ws.title = "Productos"
    
    # Añadir encabezados
    ws['A1'] = "Nombre"
    ws['B1'] = "Descripción"
    ws['C1'] = "Vida útil (días)"
    ws['D1'] = "Descongelación (horas)"
    ws['E1'] = "Refrigeración (horas)"
    ws['F1'] = "Gastro (horas)"
    ws['G1'] = "Caliente (horas)"
    ws['H1'] = "Seco (horas)"
    
    # Obtener productos de este local
    products = Product.query.filter_by(location_id=location_id).order_by(Product.name).all()
    
    # Rellenar datos
    row = 2
    for product in products:
        ws[f'A{row}'] = product.name
        ws[f'B{row}'] = product.description or ""
        ws[f'C{row}'] = product.shelf_life_days
        
        # Buscar horas de conservación para cada tipo
        for conservation in product.conservation_types:
            # Usar horas directamente
            hours_valid = conservation.hours_valid
            
            if conservation.conservation_type == ConservationType.DESCONGELACION:
                ws[f'D{row}'] = hours_valid
            elif conservation.conservation_type == ConservationType.REFRIGERACION:
                ws[f'E{row}'] = hours_valid
            elif conservation.conservation_type == ConservationType.GASTRO:
                ws[f'F{row}'] = hours_valid
            elif conservation.conservation_type == ConservationType.CALIENTE:
                ws[f'G{row}'] = hours_valid
            elif conservation.conservation_type == ConservationType.SECO:
                ws[f'H{row}'] = hours_valid
        
        row += 1
    
    # Guardar a un objeto BytesIO
    output = io.BytesIO()
    wb.save(output)
    output.seek(0)
    
    # Crear nombre de archivo basado en la ubicación
    filename = f"productos_{location.name.replace(' ', '_').lower()}_{datetime.now().strftime('%Y%m%d')}.xlsx"
    
    return send_file(
        output,
        download_name=filename,
        as_attachment=True,
        mimetype='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    )

@tasks_bp.route('/dashboard/labels/import/<int:location_id>', methods=['POST'])
@login_required
@manager_required
```

#### `import_labels_excel` (línea 2217)

**Docstring:**
```
Importar lista de productos y tipos de conservación desde Excel
```

**Código:**
```python
def import_labels_excel(location_id):
    """Importar lista de productos y tipos de conservación desde Excel"""
    # Verificar que el local existe y el usuario tiene permisos
    location = Location.query.get_or_404(location_id)
    
    # Verificar permisos (admin o gerente de la empresa)
    if not current_user.is_admin() and (not current_user.is_gerente() or 
                                       location.company_id not in [c.id for c in current_user.companies]):
        flash('No tienes permiso para importar datos a este local.', 'danger')
        return redirect(url_for('tasks.manage_labels'))
    
    # Comprobar si se ha subido un archivo
    if 'excel_file' not in request.files:
        flash('No se ha seleccionado ningún archivo.', 'danger')
        return redirect(url_for('tasks.manage_labels', location_id=location_id))
        
    file = request.files['excel_file']
    
    if file.filename == '':
        flash('No se ha seleccionado ningún archivo.', 'danger')
        return redirect(url_for('tasks.manage_labels', location_id=location_id))
    
    if not file.filename.endswith('.xlsx'):
        flash('El archivo debe ser un archivo Excel (.xlsx).', 'danger')
        return redirect(url_for('tasks.manage_labels', location_id=location_id))
    
    try:
        # Cargar el archivo Excel
        wb = openpyxl.load_workbook(file)
        ws = wb.active
        
        # Contar productos actualizados y creados
        updated = 0
        created = 0
        errors = 0
        
        # Empezar desde la fila 2 (después de encabezados)
        for row in range(2, ws.max_row + 1):
            try:
                # Obtener datos del producto
                product_name = ws[f'A{row}'].value
                product_description = ws[f'B{row}'].value or ""
                
                # Si no hay nombre, ignorar esta fila
                if not product_name:
                    continue
                
                # Vida útil en días (caducidad secundaria)
                shelf_life_days = ws[f'C{row}'].value
                shelf_life_days = int(shelf_life_days) if shelf_life_days is not None else 0
                
                # Horas para cada tipo de conservación
                hours_descongelacion = ws[f'D{row}'].value
                hours_refrigeracion = ws[f'E{row}'].value
                hours_gastro = ws[f'F{row}'].value
                hours_caliente = ws[f'G{row}'].value
                hours_seco = ws[f'H{row}'].value
                
                # Usar horas directamente para almacenar en la base de datos
                hours_descongelacion = int(hours_descongelacion) if hours_descongelacion is not None else None
                hours_refrigeracion = int(hours_refrigeracion) if hours_refrigeracion is not None else None
                hours_gastro = int(hours_gastro) if hours_gastro is not None else None
                hours_caliente = int(hours_caliente) if hours_caliente is not None else None
                hours_seco = int(hours_seco) if hours_seco is not None else None
                
                # Buscar producto existente por nombre en esta ubicación
                product = Product.query.filter_by(name=product_name, location_id=location_id).first()
                
                if product:
                    # Actualizar producto existente
                    product.name = product_name
                    product.description = product_description
                    product.shelf_life_days = shelf_life_days
                    updated += 1
                else:
                    # Crear nuevo producto
                    product = Product(
                        name=product_name,
                        description=product_description,
                        shelf_life_days=shelf_life_days,
                        location_id=location_id
                    )
                    db.session.add(product)
                    db.session.flush()  # Para obtener el ID generado
                    created += 1
                
                # Actualizar tipos de conservación
                conservation_types = [
                    (ConservationType.DESCONGELACION, hours_descongelacion),
                    (ConservationType.REFRIGERACION, hours_refrigeracion),
                    (ConservationType.GASTRO, hours_gastro),
                    (ConservationType.CALIENTE, hours_caliente),
                    (ConservationType.SECO, hours_seco)
                ]
                
                for cons_type, hours in conservation_types:
                    if hours is not None and hours > 0:
                        # Buscar conservación existente o crear una nueva
                        conservation = ProductConservation.query.filter_by(
                            product_id=product.id, 
                            conservation_type=cons_type
                        ).first()
                        
                        if conservation:
                            conservation.hours_valid = hours
                        else:
                            conservation = ProductConservation(
                                product_id=product.id,
                                conservation_type=cons_type,
                                hours_valid=hours
                            )
                            db.session.add(conservation)
            
            except Exception as e:
                # Registrar error y continuar con la siguiente fila
                errors += 1
                current_app.logger.error(f"Error al importar fila {row}: {str(e)}")
        
        # Guardar todos los cambios
        db.session.commit()
        
        # Mostrar mensaje de éxito
        if errors > 0:
            flash(f'Importación completada con {created} productos creados, {updated} actualizados y {errors} errores.', 'warning')
        else:
            flash(f'Importación completada con éxito: {created} productos creados y {updated} actualizados.', 'success')
        
        # Registrar actividad
        log_activity(f'Importación de productos desde Excel para {location.name}: {created} creados, {updated} actualizados')
        
    except Exception as e:
        db.session.rollback()
        flash(f'Error al procesar el archivo: {str(e)}', 'danger')
        current_app.logger.error(f"Error al importar Excel: {str(e)}")
    
    return redirect(url_for('tasks.manage_labels', location_id=location_id))

@tasks_bp.route('/local-user/generate-labels', methods=['POST'])
@local_user_required
```

#### `generate_labels` (línea 2356)

**Docstring:**
```
Endpoint simplificado para generar e imprimir etiquetas directamente
```

**Código:**
```python
def generate_labels():
    """Endpoint simplificado para generar e imprimir etiquetas directamente"""
    try:
        # Obtener el usuario local
        user_id = session['local_user_id']
        user = LocalUser.query.get_or_404(user_id)
        
        # Obtener datos del formulario
        product_id = request.form.get('product_id', type=int)
        conservation_type_str = request.form.get('conservation_type')
        quantity = request.form.get('quantity', type=int, default=1)
        
        # Validaciones básicas
        if not product_id or not conservation_type_str:
            return "Error: Faltan datos obligatorios", 400
            
        # Validar cantidad (entre 1 y 100)
        quantity = max(1, min(100, quantity))
        
        # Obtener el producto
        product = Product.query.get_or_404(product_id)
        
        # Verificar que el producto pertenece al local del usuario
        if product.location_id != user.location_id:
            return "Error: El producto no pertenece a este local", 403
        
        # Convertir string a enum
        conservation_type = None
        for ct in ConservationType:
            if ct.value == conservation_type_str:
                conservation_type = ct
                break
                
        if not conservation_type:
            return "Error: Tipo de conservación no válido", 400
        
        # Obtener configuración de conservación específica
        conservation = ProductConservation.query.filter_by(
            product_id=product.id, 
            conservation_type=conservation_type
        ).first()
        
        # Fecha y hora actual
        now = datetime.now()
        
        # Calcular fecha de caducidad primaria (por conservación)
        if conservation:
            # Usar la configuración específica del producto
            expiry_datetime = now + timedelta(hours=conservation.hours_valid)
        else:
            # Valores predeterminados por tipo
            hours_map = {
                ConservationType.DESCONGELACION: 24,  # 1 día
                ConservationType.REFRIGERACION: 72,   # 3 días
                ConservationType.GASTRO: 48,          # 2 días
                ConservationType.CALIENTE: 2,         # 2 horas
                ConservationType.SECO: 720            # 30 días
            }
            hours = hours_map.get(conservation_type, 24)  # 24h por defecto
            expiry_datetime = now + timedelta(hours=hours)
            
        # Calcular fecha de caducidad secundaria (por vida útil en días)
        secondary_expiry_date = None
        if product.shelf_life_days > 0:
            secondary_expiry_date = product.get_shelf_life_expiry(now)
        
        # Obtener la plantilla de etiquetas predeterminada para este local
        template = LabelTemplate.query.filter_by(location_id=user.location_id, is_default=True).first()
        
        # Si no existe una plantilla predeterminada, crear una
        if not template:
            template = LabelTemplate(
                name="Diseño Predeterminado",
                location_id=user.location_id,
                is_default=True
            )
            db.session.add(template)
            db.session.commit()
        
        # Registrar las etiquetas en la base de datos
        try:
            for _ in range(quantity):
                label = ProductLabel(
                    product_id=product.id,
                    local_user_id=user.id,
                    conservation_type=conservation_type,
                    expiry_date=expiry_datetime.date()
                )
                db.session.add(label)
            db.session.commit()
        except Exception as e:
            db.session.rollback()
            current_app.logger.error(f"Error al registrar etiquetas: {str(e)}")
            # Continuar generando etiquetas aunque falle el registro
        
        # Verificar si necesitamos generar automáticamente una etiqueta de refrigeración después de descongelación
        auto_generate_refrigeration = False
        refrigeration_conservation_type = None
        refrigeration_expiry_datetime = None
        
        if conservation_type == ConservationType.DESCONGELACION:
            auto_generate_refrigeration = True
            
            # Obtener el tipo de conservación de refrigeración
            for ct in ConservationType:
                if ct.value == "refrigeracion":
                    refrigeration_conservation_type = ct
                    break
            
            # Obtener la configuración de refrigeración (si existe)
            ref_conservation = None
            if refrigeration_conservation_type:
                ref_conservation = ProductConservation.query.filter_by(
                    product_id=product.id, 
                    conservation_type=refrigeration_conservation_type
                ).first()
            
            # Calcular fecha de caducidad para refrigeración a partir de la fecha de caducidad de descongelación
            if ref_conservation:
                # La hora de inicio de refrigeración es la hora de finalización de descongelación
                refrigeration_expiry_datetime = expiry_datetime + timedelta(hours=ref_conservation.hours_valid)
            else:
                # Usar valor predeterminado si no hay configuración específica (3 días)
                refrigeration_expiry_datetime = expiry_datetime + timedelta(hours=72)
                
            # Registrar etiquetas de refrigeración
            try:
                for _ in range(quantity):
                    label = ProductLabel(
                        product_id=product.id,
                        local_user_id=user.id,
                        conservation_type=refrigeration_conservation_type,
                        expiry_date=refrigeration_expiry_datetime.date()
                    )
                    db.session.add(label)
                db.session.commit()
            except Exception as e:
                db.session.rollback()
                current_app.logger.error(f"Error al registrar etiquetas de refrigeración: {str(e)}")
                # Continuar de todos modos
        
        # Generar HTML para impresión directa, incluyendo las etiquetas de refrigeración si corresponde
        return render_template(
            'tasks/print_labels.html',
            product=product,
            user=user,
            location=user.location,
            conservation_type=conservation_type,
            now=now,
            expiry_datetime=expiry_datetime,
            secondary_expiry_date=secondary_expiry_date,
            quantity=quantity,
            template=template,
            auto_generate_refrigeration=auto_generate_refrigeration,
            refrigeration_conservation_type=refrigeration_conservation_type,
            refrigeration_expiry_datetime=refrigeration_expiry_datetime,
            refrigeration_start_time=expiry_datetime  # La hora de inicio de refrigeración es la hora de fin de descongelación
        )
        
    except Exception as e:
        current_app.logger.error(f"Error en generate_labels: {str(e)}")
        return "Error al generar etiquetas. Inténtelo nuevamente.", 500

@tasks_bp.route('/admin/products')
@tasks_bp.route('/admin/products/<int:location_id>')
@login_required
@manager_required
```

#### `list_products` (línea 2523)

**Docstring:**
```
Lista de productos, filtrada por ubicación si se especifica
```

**Código:**
```python
def list_products(location_id=None):
    """Lista de productos, filtrada por ubicación si se especifica"""
    companies = []
    
    # Filtrar empresas según el rol del usuario
    if current_user.is_admin():
        companies = Company.query.all()
    else:
        companies = current_user.companies
    
    # Obtener ubicaciones asociadas a las empresas que puede ver
    company_ids = [c.id for c in companies]
    
    # Si se especifica una ubicación, verificar permisos
    location = None
    if location_id:
        location = Location.query.get_or_404(location_id)
        if location.company_id not in company_ids and not current_user.is_admin():
            flash('No tiene permisos para acceder a esta ubicación', 'danger')
            return redirect(url_for('tasks.list_products'))
        
        # Filtrar sólo por la ubicación especificada
        locations = [location]
        location_ids = [location_id]
    else:
        # Sin filtro de ubicación, mostrar todas las ubicaciones permitidas
        locations = Location.query.filter(Location.company_id.in_(company_ids)).all()
        location_ids = [loc.id for loc in locations]
    
    # Si no hay ubicaciones, redireccionar a crear ubicación
    if not locations:
        flash('Primero debe crear una ubicación', 'warning')
        return redirect(url_for('tasks.list_locations'))
    
    # Obtener productos de esas ubicaciones
    products = Product.query.filter(Product.location_id.in_(location_ids)).order_by(Product.name).all()
    
    título = f'Productos de {location.name}' if location else 'Productos'
    
    return render_template(
        'tasks/product_list.html',
        title=título,
        products=products,
        locations=locations,
        selected_location=location
    )

@tasks_bp.route('/admin/products/create', methods=['GET', 'POST'])
@tasks_bp.route('/admin/products/create/<int:location_id>', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `create_product` (línea 2574)

**Docstring:**
```
Crear nuevo producto, opcionalmente preseleccionando una ubicación
```

**Código:**
```python
def create_product(location_id=None):
    """Crear nuevo producto, opcionalmente preseleccionando una ubicación"""
    form = ProductForm()
    
    # Si se proporciona un ID de ubicación, verificar permisos
    preselected_location = None
    if location_id:
        preselected_location = Location.query.get_or_404(location_id)
        if not current_user.is_admin():
            company_ids = [c.id for c in current_user.companies]
            if preselected_location.company_id not in company_ids:
                flash('No tiene permisos para crear productos en esta ubicación', 'danger')
                return redirect(url_for('tasks.list_products'))
    
    # Opciones de ubicaciones
    locations = []
    if current_user.is_admin():
        locations = Location.query.order_by(Location.name).all()
    else:
        company_ids = [c.id for c in current_user.companies]
        locations = Location.query.filter(Location.company_id.in_(company_ids)).order_by(Location.name).all()
    
    form.location_id.choices = [(l.id, f"{l.name} ({l.company.name})") for l in locations]
    
    # Si hay una ubicación preseleccionada, establecerla como valor predeterminado
    if preselected_location and request.method == 'GET':
        form.location_id.data = preselected_location.id
    
    if form.validate_on_submit():
        product = Product(
            name=form.name.data,
            description=form.description.data,
            shelf_life_days=form.shelf_life_days.data or 0,
            is_active=form.is_active.data,
            location_id=form.location_id.data
        )
        
        db.session.add(product)
        
        try:
            db.session.commit()
            flash(f'Producto "{product.name}" creado correctamente', 'success')
            return redirect(url_for('tasks.list_products'))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al crear producto: {str(e)}', 'danger')
    
    return render_template(
        'tasks/product_form.html',
        title='Nuevo Producto',
        form=form,
        is_edit=False
    )

@tasks_bp.route('/admin/products/<int:id>/edit', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `edit_product` (línea 2631)

**Docstring:**
```
Editar producto existente
```

**Código:**
```python
def edit_product(id):
    """Editar producto existente"""
    product = Product.query.get_or_404(id)
    
    # Verificar permisos
    if not current_user.is_admin():
        company_ids = [c.id for c in current_user.companies]
        location = Location.query.get_or_404(product.location_id)
        if location.company_id not in company_ids:
            flash('No tiene permisos para editar este producto', 'danger')
            return redirect(url_for('tasks.list_products'))
    
    form = ProductForm(obj=product)
    
    # Opciones de ubicaciones
    locations = []
    if current_user.is_admin():
        locations = Location.query.order_by(Location.name).all()
    else:
        company_ids = [c.id for c in current_user.companies]
        locations = Location.query.filter(Location.company_id.in_(company_ids)).order_by(Location.name).all()
    
    form.location_id.choices = [(l.id, f"{l.name} ({l.company.name})") for l in locations]
    
    if form.validate_on_submit():
        product.name = form.name.data
        product.description = form.description.data
        product.shelf_life_days = form.shelf_life_days.data or 0
        product.is_active = form.is_active.data
        product.location_id = form.location_id.data
        
        try:
            db.session.commit()
            flash(f'Producto "{product.name}" actualizado correctamente', 'success')
            return redirect(url_for('tasks.list_products'))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al actualizar producto: {str(e)}', 'danger')
    
    return render_template(
        'tasks/product_form.html',
        title=f'Editar Producto: {product.name}',
        form=form,
        product=product,
        is_edit=True
    )

@tasks_bp.route('/admin/products/<int:id>/conservations', methods=['GET', 'POST'])
@login_required
@manager_required
```

#### `manage_product_conservations` (línea 2681)

**Docstring:**
```
Gestionar tipos de conservación para un producto
```

**Código:**
```python
def manage_product_conservations(id):
    """Gestionar tipos de conservación para un producto"""
    product = Product.query.get_or_404(id)
    
    # Verificar permisos
    if not current_user.is_admin():
        company_ids = [c.id for c in current_user.companies]
        location = Location.query.get_or_404(product.location_id)
        if location.company_id not in company_ids:
            flash('No tiene permisos para gestionar este producto', 'danger')
            return redirect(url_for('tasks.list_products'))
    
    # Obtener configuraciones de conservación existentes
    conservations = ProductConservation.query.filter_by(product_id=product.id).all()
    
    # Diccionario para facilitar acceso
    conservation_dict = {}
    for conservation in conservations:
        conservation_dict[conservation.conservation_type.value] = conservation
    
    # Si es GET y hay un tipo de conservación seleccionado, prellenar el formulario
    selected_conservation = None
    selected_type = request.args.get('type')
    if request.method == 'GET' and selected_type:
        for cons in conservations:
            if cons.conservation_type.value == selected_type:
                selected_conservation = cons
                break
    
    # Crear formulario con o sin objeto preexistente
    form = ProductConservationForm(obj=selected_conservation)
    
    # Si se proporciona un tipo en la URL, preseleccionarlo
    if request.method == 'GET' and selected_type:
        form.conservation_type.data = selected_type
    
    if form.validate_on_submit():
        conservation_type_str = form.conservation_type.data
        conservation_type = None
        
        # Convertir string a enum
        for ct in ConservationType:
            if ct.value == conservation_type_str:
                conservation_type = ct
                break
        
        if not conservation_type:
            flash('Tipo de conservación no válido', 'danger')
            return redirect(url_for('tasks.manage_product_conservations', id=product.id))
        
        # Buscar si ya existe esta configuración
        conservation = ProductConservation.query.filter_by(
            product_id=product.id,
            conservation_type=conservation_type
        ).first()
        
        # Obtener horas directamente del formulario
        hours_valid = form.hours_valid.data
        
        # Debug logging
        current_app.logger.debug(f"Horas recibidas: {hours_valid}")
        
        if conservation:
            # Actualizar existente
            conservation.hours_valid = hours_valid
        else:
            # Crear nueva
            conservation = ProductConservation(
                product_id=product.id,
                conservation_type=conservation_type,
                hours_valid=hours_valid
            )
            db.session.add(conservation)
        
        try:
            db.session.commit()
            # Verificar que se guardó correctamente
            db.session.refresh(conservation)
            current_app.logger.debug(f"Guardado en BD: {conservation.hours_valid} horas")
            
            flash('Configuración de conservación guardada correctamente', 'success')
            return redirect(url_for('tasks.manage_product_conservations', id=product.id))
        except Exception as e:
            db.session.rollback()
            flash(f'Error al guardar configuración: {str(e)}', 'danger')
    
    return render_template(
        'tasks/product_conservations.html',
        title=f'Conservación: {product.name}',
        product=product,
        form=form,
        conservations=conservations,
        conservation_dict=conservation_dict
    )
```


### run_checkpoint_closer.py

#### `verificar_acceso_bd` (línea 27)

**Docstring:**
```
Verifica que podemos acceder a la base de datos y que las tablas necesarias están configuradas.
```

**Código:**
```python
def verificar_acceso_bd():
    """
    Verifica que podemos acceder a la base de datos y que las tablas necesarias están configuradas.
    """
    try:
        test_app = create_app()
        with test_app.app_context():
            from models_checkpoints import CheckPoint, CheckPointRecord, CheckPointStatus
            
            checkpoints_count = CheckPoint.query.count()
            
            # Comprobar si hay checkpoints activos
            active_checkpoints = CheckPoint.query.filter_by(status=CheckPointStatus.ACTIVE).count()
            
            # Comprobar si hay registros pendientes
            from sqlalchemy import func
            pending_records = CheckPointRecord.query.filter(
                CheckPointRecord.check_out_time.is_(None)
            ).count()
            
            # Comprobar la configuración de zona horaria
            from timezone_config import get_current_time, TIMEZONE
            current_time = get_current_time()
            
            # Loggear los resultados de la verificación
            logger.info(f"✓ Verificación de Base de Datos completada:")
            logger.info(f"  - Total de puntos de fichaje: {checkpoints_count}")
            logger.info(f"  - Puntos de fichaje activos: {active_checkpoints}")
            logger.info(f"  - Registros pendientes: {pending_records}")
            logger.info(f"  - Zona horaria configurada: {TIMEZONE}")
            logger.info(f"  - Hora actual (Madrid): {current_time}")
            
            return True
    except Exception as e:
        logger.error(f"✗ Error en verificación de acceso a base de datos: {e}", exc_info=True)
        return False

```

#### `run_once` (línea 64)

**Docstring:**
```
Ejecuta una verificación única de puntos de fichaje fuera de horario
y cierra los registros pendientes.
```

**Código:**
```python
def run_once():
    """
    Ejecuta una verificación única de puntos de fichaje fuera de horario
    y cierra los registros pendientes.
    """
    start_time = datetime.now()
    logger.info("="*80)
    logger.info(f"INICIANDO VERIFICACIÓN MANUAL DE CIERRE AUTOMÁTICO - {start_time}")
    logger.info(f"Versión: 1.1.0 - Verificación con logs detallados")
    logger.info("-"*80)
    
    # Comprobar que podemos acceder a la BD
    if not verificar_acceso_bd():
        logger.error("No se pudo realizar la verificación debido a errores de acceso a la BD")
        return False
        
    logger.info(f"Ejecutando verificación manual de puntos de fichaje: {datetime.now()}")
    
    app = create_app()
    
    with app.app_context():
        try:
            # Ejecutar la verificación de puntos de fichaje
            success = auto_close_pending_records()
            if success:
                logger.info("Verificación completada correctamente")
            else:
                logger.error("La verificación finalizó con errores")
                return False
        except Exception as e:
            logger.error(f"Error durante la verificación: {e}", exc_info=True)
            return False
    
    return True

if __name__ == "__main__":
    success = run_once()
    sys.exit(0 if success else 1)
```


### scheduled_checkpoints_closer.py

#### `verificar_sistema` (línea 38)

**Docstring:**
```
Verifica que el sistema está correctamente configurado y puede acceder a la base de datos.
Retorna True si todo está correcto, False si hay algún problema.
```

**Código:**
```python
def verificar_sistema():
    """
    Verifica que el sistema está correctamente configurado y puede acceder a la base de datos.
    Retorna True si todo está correcto, False si hay algún problema.
    """
    try:
        # Creamos una app temporal para verificar la conexión a la BD
        test_app = create_app()
        with test_app.app_context():
            # Comprobar que podemos acceder a los checkpoints (prueba de acceso a BD)
            from models_checkpoints import CheckPoint
            count = CheckPoint.query.count()
            logger.info(f"✓ Verificación de sistema: Conexión a BD correcta - {count} checkpoints encontrados")
            
            # Comprobar que podemos acceder a la zona horaria
            from timezone_config import get_current_time, TIMEZONE
            current_time = get_current_time()
            logger.info(f"✓ Verificación de sistema: Zona horaria configurada correctamente - {TIMEZONE} - Hora actual: {current_time}")
            
            return True
    except Exception as e:
        logger.error(f"✗ Error en verificación del sistema: {e}", exc_info=True)
        return False

```

#### `run_service` (línea 62)

**Docstring:**
```
Ejecuta el servicio de verificación periódica para el cierre de fichajes
pendientes en puntos de fichaje fuera de horario.
```

**Código:**
```python
def run_service():
    """
    Ejecuta el servicio de verificación periódica para el cierre de fichajes
    pendientes en puntos de fichaje fuera de horario.
    """
    start_time = datetime.now()
    logger.info("="*80)
    logger.info(f"INICIO SERVICIO DE CIERRE AUTOMÁTICO - {start_time.strftime('%Y-%m-%d %H:%M:%S')}")
    logger.info(f"Versión: 1.1.0 - Barrido con logs detallados")
    logger.info(f"Intervalo configurado: {CHECK_INTERVAL} segundos ({CHECK_INTERVAL/60:.1f} minutos)")
    logger.info("-"*80)
    
    # Verificar que el sistema funciona correctamente
    if not verificar_sistema():
        logger.critical("No se pudo iniciar el servicio debido a errores en la verificación del sistema")
        return False
        
    logger.info("✓ Sistema verificado correctamente - Iniciando servicio de barrido")
    
    app = create_app()
    
    try:
        # Bucle infinito para ejecución continua
        barrido_count = 0
        while True:
            start_time = time.time()
            barrido_count += 1
            logger.info(f"======= INICIO BARRIDO #{barrido_count} - {datetime.now().strftime('%Y-%m-%d %H:%M:%S')} =======")
            
            with app.app_context():
                try:
                    # Ejecutar la verificación de puntos de fichaje
                    success = auto_close_pending_records()
                    if success:
                        logger.info("Barrido completado correctamente")
                    else:
                        logger.error("El barrido finalizó con errores")
                except Exception as e:
                    logger.error(f"Error durante el barrido: {e}", exc_info=True)
            
            # Calcular tiempo de espera hasta la próxima ejecución
            execution_time = time.time() - start_time
            logger.info(f"Tiempo de ejecución del barrido: {execution_time:.2f} segundos")
            
            # Si la ejecución tomó menos que el intervalo, esperar el tiempo restante
            wait_time = max(1, CHECK_INTERVAL - execution_time)
            next_time = datetime.now() + timedelta(seconds=wait_time)
            logger.info(f"Esperando {wait_time:.2f} segundos hasta el próximo barrido...")
            logger.info(f"Próximo barrido programado para: {next_time.strftime('%Y-%m-%d %H:%M:%S')}")
            logger.info(f"======= FIN BARRIDO #{barrido_count} =======\n")
            
            time.sleep(wait_time)
    
    except KeyboardInterrupt:
        logger.info("Servicio detenido manualmente")
    except Exception as e:
        logger.critical(f"Error fatal en el servicio: {e}", exc_info=True)
        return False
    
    return True

if __name__ == "__main__":
    success = run_service()
    sys.exit(0 if success else 1)
```


### test_close_operation_hours.py

#### `list_checkpoints_with_hours` (línea 16)

**Docstring:**
```
Muestra todos los puntos de fichaje con horarios configurados
```

**Código:**
```python
def list_checkpoints_with_hours():
    """Muestra todos los puntos de fichaje con horarios configurados"""
    checkpoints = CheckPoint.query.filter(
        CheckPoint.operation_end_time.isnot(None)
    ).all()
    
    print(f"\n{'=' * 100}")
    print(f"PUNTOS DE FICHAJE CON HORARIOS CONFIGURADOS: {len(checkpoints)}")
    print(f"{'=' * 100}")
    
    for i, cp in enumerate(checkpoints, 1):
        status_text = "ACTIVO" if cp.status.name == "ACTIVE" else "INACTIVO"
        enforce_text = "✓ ACTIVADO" if cp.enforce_operation_hours else "✗ DESACTIVADO"
        
        # Contar registros pendientes
        pending_count = CheckPointRecord.query.filter(
            CheckPointRecord.checkpoint_id == cp.id,
            CheckPointRecord.check_out_time.is_(None)
        ).count()
        
        print(f"{i}. ID: {cp.id} - {cp.name} ({cp.company.name})")
        print(f"   Estado: {status_text} - Cierre automático: {enforce_text}")
        print(f"   Horario: {cp.operation_start_time or '??:??'} a {cp.operation_end_time or '??:??'}")
        print(f"   Registros pendientes: {pending_count}")
        print(f"   {'-' * 80}")
    
    return checkpoints

```

#### `force_close_checkpoint_records` (línea 44)

**Docstring:**
```
Fuerza el cierre de todos los registros pendientes para un punto de fichaje específico
sin importar la hora actual.
```

**Código:**
```python
def force_close_checkpoint_records(checkpoint_id):
    """
    Fuerza el cierre de todos los registros pendientes para un punto de fichaje específico
    sin importar la hora actual.
    """
    checkpoint = CheckPoint.query.get(checkpoint_id)
    if not checkpoint:
        print(f"Error: No se encontró un punto de fichaje con ID {checkpoint_id}")
        return False
        
    if not checkpoint.operation_end_time:
        print(f"Error: El punto de fichaje {checkpoint.name} no tiene configurada una hora de fin")
        return False
    
    print(f"\n{'=' * 100}")
    print(f"FORZANDO CIERRE DE FICHAJES PARA: {checkpoint.name}")
    print(f"{'=' * 100}")
    
    # Buscar registros pendientes
    pending_records = CheckPointRecord.query.filter(
        CheckPointRecord.checkpoint_id == checkpoint.id,
        CheckPointRecord.check_out_time.is_(None)
    ).all()
    
    if not pending_records:
        print(f"No hay registros pendientes para cerrar en el punto {checkpoint.name}")
        return True
    
    print(f"Encontrados {len(pending_records)} registros pendientes para cerrar.")
    
    # Cerrar cada registro pendiente
    for record in pending_records:
        # Crear una copia del registro original si es la primera modificación
        if not record.original_check_in_time:
            record.original_check_in_time = record.check_in_time
        
        # Asegurarse de que la fecha de entrada tenga información de zona horaria
        check_in_time = record.check_in_time
        if check_in_time and check_in_time.tzinfo is None:
            check_in_time = datetime_to_madrid(check_in_time)
        
        if not check_in_time:
            print(f"  ⚠️  Advertencia: El registro {record.id} no tiene hora de entrada válida")
            continue
            
        # Establecer la hora de salida como la hora de fin de funcionamiento
        check_in_date = check_in_time.date()
        check_out_time = datetime.combine(check_in_date, checkpoint.operation_end_time)
        check_out_time = TIMEZONE.localize(check_out_time)
        
        # Si la salida queda antes que la entrada, establecer la salida para el día siguiente
        if check_out_time and check_in_time and check_out_time < check_in_time:
            print(f"  ⚠️  Advertencia: Hora de entrada {check_in_time} posterior a hora de cierre {check_out_time}")
            print(f"  ⚠️  Ajustando la salida para el día siguiente")
            check_out_date = check_in_date + timedelta(days=1)
            check_out_time = datetime.combine(check_out_date, checkpoint.operation_end_time)
            check_out_time = TIMEZONE.localize(check_out_time)
        
        print(f"  • Empleado: {record.employee.first_name} {record.employee.last_name}")
        print(f"    Entrada: {record.check_in_time}")
        print(f"    Nueva Salida: {check_out_time}")
        
        # Asignar la hora de salida calculada
        record.check_out_time = check_out_time
        
        # Marcar que fue cerrado automáticamente
        record.notes = (record.notes or "") + " [Forzado manualmente - Cierre por fin de horario]"
        record.adjusted = True
        
        # Crear una incidencia
        incident = CheckPointIncident(
            record_id=record.id,
            incident_type=CheckPointIncidentType.MISSED_CHECKOUT,
            description=f"Salida forzada manualmente por fin de horario de funcionamiento ({checkpoint.operation_end_time})"
        )
        db.session.add(incident)
    
    # Guardar todos los cambios
    try:
        db.session.commit()
        print(f"\n✅ Se cerraron correctamente {len(pending_records)} registros pendientes")
        return True
    except Exception as e:
        db.session.rollback()
        print(f"\n❌ Error al cerrar los registros: {e}")
        return False

```

#### `main` (línea 131)

**Código:**
```python
def main():
    app = create_app()
    
    with app.app_context():
        # Ejecutar automáticamente la opción de listar puntos de fichaje
        print("\n=== VERIFICACIÓN DE PUNTOS DE FICHAJE CON HORARIOS ===")
        checkpoints = list_checkpoints_with_hours()
        
        if not checkpoints:
            print("\nNo hay puntos de fichaje con horarios configurados")
            return
        
        # Si solo hay un punto de fichaje, preguntar si quiere cerrarlo
        if len(checkpoints) == 1:
            checkpoint = checkpoints[0]
            print(f"\n¿Desea forzar el cierre de los fichajes para {checkpoint.name} (ID: {checkpoint.id})? (s/n)")
            print("Presione Ctrl+C para cancelar")
            
            try:
                # Forzar cierre automáticamente para el punto encontrado
                print(f"\nForzando cierre para el punto de fichaje: {checkpoint.name} (ID: {checkpoint.id})")
                force_close_checkpoint_records(checkpoint.id)
            except KeyboardInterrupt:
                print("\nOperación cancelada por el usuario.")
            except Exception as e:
                print(f"\nError: {e}")
        else:
            # Si hay varios puntos, mostrar el primero
            if checkpoints:
                checkpoint = checkpoints[0]
                print(f"\nForzando cierre para el primer punto de fichaje: {checkpoint.name} (ID: {checkpoint.id})")
                force_close_checkpoint_records(checkpoint.id)

if __name__ == "__main__":
    main()
```


### timezone_config.py

#### `get_current_time` (línea 12)

**Docstring:**
```
Obtiene la hora actual en la zona horaria configurada (Madrid)
```

**Código:**
```python
def get_current_time():
    """
    Obtiene la hora actual en la zona horaria configurada (Madrid)
    """
    # Obtener la hora UTC
    utc_now = datetime.now(timezone.utc)
    # Convertir a la hora de Madrid
    return utc_now.astimezone(TIMEZONE)

```

#### `datetime_to_madrid` (línea 21)

**Docstring:**
```
Convierte un objeto datetime a la zona horaria de Madrid
Si el datetime no tiene zona horaria, asume que es UTC
```

**Código:**
```python
def datetime_to_madrid(dt):
    """
    Convierte un objeto datetime a la zona horaria de Madrid
    Si el datetime no tiene zona horaria, asume que es UTC
    """
    if dt is None:
        return None
        
    # Si el datetime no tiene zona horaria (naive), asumimos que es UTC
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
        
    # Convertir a la zona horaria de Madrid
    return dt.astimezone(TIMEZONE)
    
```

#### `format_datetime` (línea 36)

**Docstring:**
```
Formatea un datetime en la zona horaria de Madrid
```

**Código:**
```python
def format_datetime(dt, format_str='%Y-%m-%d %H:%M:%S'):
    """
    Formatea un datetime en la zona horaria de Madrid
    """
    if dt is None:
        return ""
    
    try:
        madrid_dt = datetime_to_madrid(dt)
        return madrid_dt.strftime(format_str)
    except Exception as e:
        print(f"Error al formatear fecha: {e}")
        return ""
```


### update_db.py

#### `add_shelf_life_days_column` (línea 5)

**Docstring:**
```
Añade la columna shelf_life_days a la tabla products si no existe.
```

**Código:**
```python
def add_shelf_life_days_column():
    """Añade la columna shelf_life_days a la tabla products si no existe."""
    try:
        with app.app_context():
            # Verificar si la columna ya existe
            connection = db.engine.connect()
            result = connection.execute(text("""
                SELECT column_name 
                FROM information_schema.columns 
                WHERE table_name = 'products' AND column_name = 'shelf_life_days';
            """))
            
            # Si la columna no existe, agregarla
            if result.fetchone() is None:
                print("Añadiendo columna shelf_life_days a la tabla products...")
                connection.execute(text("""
                    ALTER TABLE products
                    ADD COLUMN shelf_life_days INTEGER NOT NULL DEFAULT 0;
                """))
                print("Columna añadida correctamente.")
            else:
                print("La columna shelf_life_days ya existe en la tabla products.")
            
            connection.commit()
    except Exception as e:
        print(f"Error al añadir la columna: {str(e)}")
        raise

if __name__ == "__main__":
    add_shelf_life_days_column()
    print("Actualización completada.")
```


### utils.py

#### `create_admin_user` (línea 19)

**Docstring:**
```
Create admin user if not exists.
```

**Código:**
```python
def create_admin_user():
    """Create admin user if not exists."""
    admin = User.query.filter_by(username='admin').first()
    if not admin:
        admin = User(
            username='admin',
            email='admin@example.com',
            role=UserRole.ADMIN,
            first_name='Admin',
            last_name='User',
            is_active=True
        )
        admin.set_password('admin12345')
        db.session.add(admin)
        db.session.commit()
        current_app.logger.info('Created admin user')

```

#### `allowed_file` (línea 36)

**Docstring:**
```
Check if file extension is allowed.
```

**Código:**
```python
def allowed_file(filename):
    """Check if file extension is allowed."""
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in current_app.config['ALLOWED_EXTENSIONS']

```

#### `save_file` (línea 41)

**Docstring:**
```
Save uploaded file to filesystem and return path.
```

**Código:**
```python
def save_file(file, description=None):
    """Save uploaded file to filesystem and return path."""
    if file and allowed_file(file.filename):
        # Get secure filename and add unique identifier
        original_filename = secure_filename(file.filename)
        file_ext = original_filename.rsplit('.', 1)[1].lower() if '.' in original_filename else ''
        unique_filename = f"{uuid.uuid4().hex}.{file_ext}" if file_ext else f"{uuid.uuid4().hex}"
        
        # Create path for file
        file_path = os.path.join(current_app.config['UPLOAD_FOLDER'], unique_filename)
        
        # Save file to disk
        file.save(file_path)
        
        return {
            'filename': unique_filename,
            'original_filename': original_filename,
            'file_path': file_path,
            'file_type': file.content_type if hasattr(file, 'content_type') else None,
            'file_size': os.path.getsize(file_path),
            'description': description
        }
    return None

```

#### `log_employee_change` (línea 65)

**Docstring:**
```
Log changes to employee data.
```

**Código:**
```python
def log_employee_change(employee, field_name, old_value, new_value):
    """Log changes to employee data."""
    history = EmployeeHistory(
        employee_id=employee.id,
        field_name=field_name,
        old_value=str(old_value) if old_value is not None else None,
        new_value=str(new_value) if new_value is not None else None,
        changed_by_id=current_user.id if current_user.is_authenticated else None,
        changed_at=datetime.utcnow()
    )
    db.session.add(history)

```

#### `log_activity` (línea 77)

**Docstring:**
```
Log user activity.
```

**Código:**
```python
def log_activity(action, user_id=None):
    """Log user activity."""
    try:
        # Crear una nueva sesión para evitar problemas con transacciones abiertas
        log = ActivityLog(
            user_id=user_id or (current_user.id if current_user.is_authenticated else None),
            action=action,
            ip_address=request.remote_addr if request else None,
            timestamp=datetime.utcnow()
        )
        db.session.add(log)
        
        try:
            db.session.commit()
        except Exception as e:
            db.session.rollback()
            current_app.logger.error(f"Error al registrar actividad: {e}")
    except Exception as e:
        # Si hay cualquier error, que no impacte el flujo principal de la aplicación
        current_app.logger.error(f"Error general en log_activity: {e}")

```

#### `can_manage_company` (línea 98)

**Docstring:**
```
Check if current user can manage the company.
```

**Código:**
```python
def can_manage_company(company_id):
    """Check if current user can manage the company."""
    if not current_user.is_authenticated:
        return False
    
    if current_user.is_admin():
        return True
        
    if current_user.is_gerente():
        # Verificar si el usuario tiene esta empresa asignada
        for company in current_user.companies:
            if company.id == company_id:
                return True
        
    return False

```

#### `can_manage_employee` (línea 114)

**Docstring:**
```
Check if current user can manage the employee.
```

**Código:**
```python
def can_manage_employee(employee):
    """Check if current user can manage the employee."""
    if not current_user.is_authenticated:
        return False
    
    if current_user.is_admin():
        return True
        
    if current_user.is_gerente():
        # Verificar si el usuario tiene la empresa del empleado asignada
        for company in current_user.companies:
            if company.id == employee.company_id:
                return True
        
    if current_user.is_empleado() and current_user.id == employee.user_id:
        return True
        
    return False

```

#### `can_view_employee` (línea 133)

**Docstring:**
```
Check if current user can view the employee.
```

**Código:**
```python
def can_view_employee(employee):
    """Check if current user can view the employee."""
    if not current_user.is_authenticated:
        return False
    
    if current_user.is_admin():
        return True
        
    if current_user.is_gerente():
        # Verificar si el usuario tiene la empresa del empleado asignada
        for company in current_user.companies:
            if company.id == employee.company_id:
                return True
        
    if current_user.is_empleado() and current_user.id == employee.user_id:
        return True
        
    return False

```

#### `generate_checkins_pdf` (línea 152)

**Docstring:**
```
Generate a PDF with employee check-ins between dates.
```

**Código:**
```python
def generate_checkins_pdf(employee, start_date=None, end_date=None):
    """Generate a PDF with employee check-ins between dates."""
    from models import EmployeeCheckIn
    import io
    from fpdf import FPDF
    from datetime import datetime
    
    # Get employee check-ins filtered by date if provided
    query = EmployeeCheckIn.query.filter_by(employee_id=employee.id)
    
    if start_date:
        query = query.filter(EmployeeCheckIn.check_in_time >= start_date)
    if end_date:
        query = query.filter(EmployeeCheckIn.check_in_time <= end_date)
    
    check_ins = query.order_by(EmployeeCheckIn.check_in_time).all()
    
    # Create PDF with direct BytesIO output
    pdf_buffer = io.BytesIO()
    
    try:
        # Create PDF
        pdf = FPDF()
        pdf.add_page()
        
        # Set fonts
        pdf.set_font('Arial', 'B', 14)
        
        # Title
        pdf.cell(0, 8, 'Registro de fichajes', 0, 1, 'C')
        
        # Date range information
        pdf.set_font('Arial', '', 9)
        date_range = ""
        if start_date and end_date:
            date_range = f"Periodo: {start_date.strftime('%d/%m/%Y')} - {end_date.strftime('%d/%m/%Y')}"
        elif start_date:
            date_range = f"Desde: {start_date.strftime('%d/%m/%Y')}"
        elif end_date:
            date_range = f"Hasta: {end_date.strftime('%d/%m/%Y')}"
        
        if date_range:
            pdf.cell(0, 5, date_range, 0, 1, 'C')
        
        # Compact header with company and employee info in a single row
        pdf.set_font('Arial', 'B', 10)
        pdf.cell(0, 7, f'Empresa: {employee.company.name} - CIF: {employee.company.tax_id}', 0, 1)
        pdf.set_font('Arial', '', 9)
        pdf.cell(0, 5, f'Empleado: {employee.first_name} {employee.last_name} - DNI: {employee.dni} - Puesto: {employee.position or ""}', 0, 1)
        pdf.ln(3)
        
        # Check-ins table header - Removed Notes column
        pdf.set_font('Arial', 'B', 9)
        pdf.cell(50, 7, 'Fecha', 1, 0, 'C')
        pdf.cell(35, 7, 'Entrada', 1, 0, 'C')
        pdf.cell(35, 7, 'Salida', 1, 0, 'C')
        pdf.cell(35, 7, 'Horas', 1, 1, 'C')
        
        # Check-ins data
        pdf.set_font('Arial', '', 9)
        total_hours = 0
        
        # Adjust rows per page
        max_rows_per_page = 35
        row_count = 0
        
        for checkin in check_ins:
            # Add a new page if necessary
            if row_count >= max_rows_per_page:
                pdf.add_page()
                
                # Add header again on new page
                pdf.set_font('Arial', 'B', 9)
                pdf.cell(50, 7, 'Fecha', 1, 0, 'C')
                pdf.cell(35, 7, 'Entrada', 1, 0, 'C')
                pdf.cell(35, 7, 'Salida', 1, 0, 'C')
                pdf.cell(35, 7, 'Horas', 1, 1, 'C')
                
                pdf.set_font('Arial', '', 9)
                row_count = 0
            
            # Format date and times
            date_str = checkin.check_in_time.strftime('%d/%m/%Y')
            check_in_str = checkin.check_in_time.strftime('%H:%M')
            check_out_str = checkin.check_out_time.strftime('%H:%M') if checkin.check_out_time else '-'
            
            # Calculate hours
            hours = 0
            hours_str = '-'
            if checkin.check_out_time:
                hours = (checkin.check_out_time - checkin.check_in_time).total_seconds() / 3600
                hours_str = f"{hours:.2f}"
                total_hours += hours
            
            # Draw row without Notes column
            pdf.cell(50, 6, date_str, 1, 0, 'C')
            pdf.cell(35, 6, check_in_str, 1, 0, 'C')
            pdf.cell(35, 6, check_out_str, 1, 0, 'C')
            pdf.cell(35, 6, hours_str, 1, 1, 'C')
            
            row_count += 1
        
        # Total hours
        pdf.set_font('Arial', 'B', 9)
        pdf.cell(120, 6, 'Total Horas:', 1, 0, 'R')
        pdf.cell(35, 6, f"{total_hours:.2f}", 1, 1, 'C')
        
        # Compact footer with signatures
        pdf.ln(5)
        pdf.set_font('Arial', '', 9)
        current_date = datetime.now().strftime('%d/%m/%Y')
        pdf.cell(0, 5, f'Fecha: {current_date}', 0, 1)
        
        pdf.cell(90, 5, 'Firma del empleado:', 0, 0)
        pdf.cell(90, 5, 'Firma del responsable:', 0, 1)
        pdf.ln(10)
        
        pdf.cell(90, 5, '_______________________', 0, 0)
        pdf.cell(90, 5, '_______________________', 0, 1)
        
        # Output PDF to BytesIO buffer
        pdf_output = pdf.output(dest='S').encode('latin1')
        pdf_buffer.write(pdf_output)
        pdf_buffer.seek(0)
        
        return pdf_buffer
    
    except Exception as e:
        print(f"Error generating PDF: {e}")
        return None


```

#### `export_company_employees_zip` (línea 284)

**Docstring:**
```
Export all employees and their documents in a ZIP file.
```

**Código:**
```python
def export_company_employees_zip(company_id):
    """Export all employees and their documents in a ZIP file."""
    try:
        from models import Company, Employee, EmployeeDocument
        import json
        
        company = Company.query.get(company_id)
        if not company:
            return None
        
        # Create a temporary directory
        temp_dir = tempfile.mkdtemp()
        zip_filename = f"empleados_{company.name.replace(' ', '_')}_{datetime.now().strftime('%Y%m%d%H%M%S')}.zip"
        zip_path = os.path.join(temp_dir, zip_filename)
        
        # Create ZIP file
        with zipfile.ZipFile(zip_path, 'w') as zipf:
            # Create company info JSON
            company_info = {
                'id': company.id,
                'name': company.name,
                'tax_id': company.tax_id,
                'address': company.address,
                'city': company.city,
                'postal_code': company.postal_code,
                'country': company.country,
                'sector': company.sector,
                'email': company.email,
                'phone': company.phone,
                'website': company.website,
                'created_at': company.created_at.strftime('%Y-%m-%d %H:%M:%S') if company.created_at else None,
                'export_date': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
            }
            
            # Add company info to ZIP
            zipf.writestr('empresa.json', json.dumps(company_info, indent=2, ensure_ascii=False))
            
            # Create employees directory in ZIP
            employees_data = []
            
            # Process each employee
            for employee in company.employees:
                # Create employee data dictionary
                employee_data = {
                    'id': employee.id,
                    'first_name': employee.first_name,
                    'last_name': employee.last_name,
                    'dni': employee.dni,
                    'position': employee.position,
                    'contract_type': employee.contract_type.value if employee.contract_type else None,
                    'bank_account': employee.bank_account,
                    'start_date': employee.start_date.strftime('%Y-%m-%d') if employee.start_date else None,
                    'end_date': employee.end_date.strftime('%Y-%m-%d') if employee.end_date else None,
                    'is_active': employee.is_active,
                    'status': employee.status.value if employee.status else None,
                    'status_start_date': employee.status_start_date.strftime('%Y-%m-%d') if employee.status_start_date else None,
                    'status_end_date': employee.status_end_date.strftime('%Y-%m-%d') if employee.status_end_date else None,
                    'status_notes': employee.status_notes,
                    'documents': []
                }
                
                # Create employee directory in ZIP
                employee_dir = f"empleados/{employee.id}_{employee.last_name}_{employee.first_name}"
                
                # Get employee documents
                documents = EmployeeDocument.query.filter_by(employee_id=employee.id).all()
                
                # Add documents to employee data
                for doc in documents:
                    document_info = {
                        'id': doc.id,
                        'filename': doc.filename,
                        'original_filename': doc.original_filename,
                        'file_type': doc.file_type,
                        'file_size': doc.file_size,
                        'description': doc.description,
                        'uploaded_at': doc.uploaded_at.strftime('%Y-%m-%d %H:%M:%S') if doc.uploaded_at else None
                    }
                    
                    employee_data['documents'].append(document_info)
                    
                    # Copy file to ZIP if exists
                    if os.path.exists(doc.file_path):
                        doc_zip_path = f"{employee_dir}/documentos/{doc.original_filename}"
                        zipf.write(doc.file_path, doc_zip_path)
                
                # Add employee JSON data to ZIP
                employee_json_path = f"{employee_dir}/datos.json"
                zipf.writestr(employee_json_path, json.dumps(employee_data, indent=2, ensure_ascii=False))
                
                # Create employee PDF summary
                pdf_file = create_employee_summary_pdf(employee)
                if pdf_file:
                    pdf_path = f"{employee_dir}/resumen.pdf"
                    zipf.writestr(pdf_path, pdf_file.getvalue())
                
                # Add employee to the list
                employees_data.append({
                    'id': employee.id,
                    'name': f"{employee.first_name} {employee.last_name}",
                    'dni': employee.dni,
                    'document_count': len(documents)
                })
            
            # Add employees index
            zipf.writestr('empleados_indice.json', json.dumps(employees_data, indent=2, ensure_ascii=False))
            
        # Create a BytesIO object for the ZIP file
        zip_buffer = io.BytesIO()
        with open(zip_path, 'rb') as f:
            zip_buffer.write(f.read())
        
        # Clean up temporary files
        shutil.rmtree(temp_dir)
        
        # Reset buffer position
        zip_buffer.seek(0)
        
        return {
            'buffer': zip_buffer,
            'filename': zip_filename
        }
        
    except Exception as e:
        print(f"Error exporting company employees: {str(e)}")
        # Clean up if needed
        if 'temp_dir' in locals():
            shutil.rmtree(temp_dir)
        return None


```

#### `create_employee_summary_pdf` (línea 415)

**Docstring:**
```
Create a PDF summary of an employee.
```

**Código:**
```python
def create_employee_summary_pdf(employee):
    """Create a PDF summary of an employee."""
    try:
        pdf_buffer = io.BytesIO()
        pdf = FPDF()
        pdf.add_page()
        
        # Header
        pdf.set_font('Arial', 'B', 16)
        pdf.cell(0, 10, 'Ficha de Empleado', 0, 1, 'C')
        
        # Company info
        pdf.set_font('Arial', 'B', 12)
        pdf.cell(0, 10, f'Empresa: {employee.company.name}', 0, 1)
        
        # Employee info
        pdf.set_font('Arial', 'B', 12)
        pdf.cell(0, 8, 'Datos Personales', 0, 1)
        
        pdf.set_font('Arial', '', 10)
        pdf.cell(40, 6, 'Nombre:', 0, 0)
        pdf.cell(0, 6, f'{employee.first_name} {employee.last_name}', 0, 1)
        
        pdf.cell(40, 6, 'DNI/NIE:', 0, 0)
        pdf.cell(0, 6, f'{employee.dni}', 0, 1)
        
        pdf.cell(40, 6, 'Puesto:', 0, 0)
        pdf.cell(0, 6, f'{employee.position or ""}', 0, 1)
        
        pdf.cell(40, 6, 'Tipo de Contrato:', 0, 0)
        pdf.cell(0, 6, f'{employee.contract_type.name if employee.contract_type else ""}', 0, 1)
        
        pdf.cell(40, 6, 'Cuenta Bancaria:', 0, 0)
        pdf.cell(0, 6, f'{employee.bank_account or ""}', 0, 1)
        
        pdf.cell(40, 6, 'Fecha de Inicio:', 0, 0)
        pdf.cell(0, 6, f'{employee.start_date.strftime("%d/%m/%Y") if employee.start_date else ""}', 0, 1)
        
        pdf.cell(40, 6, 'Fecha de Fin:', 0, 0)
        pdf.cell(0, 6, f'{employee.end_date.strftime("%d/%m/%Y") if employee.end_date else ""}', 0, 1)
        
        pdf.cell(40, 6, 'Estado:', 0, 0)
        pdf.cell(0, 6, f'{employee.status.name if employee.status else ""}', 0, 1)
        
        # Documents section
        pdf.ln(5)
        pdf.set_font('Arial', 'B', 12)
        pdf.cell(0, 8, 'Documentos Adjuntos', 0, 1)
        
        # Table header for documents
        pdf.set_font('Arial', 'B', 10)
        pdf.cell(80, 6, 'Nombre de Archivo', 1, 0)
        pdf.cell(50, 6, 'Tipo', 1, 0)
        pdf.cell(30, 6, 'Tamaño (KB)', 1, 0)
        pdf.cell(30, 6, 'Fecha', 1, 1)
        
        # Document list
        pdf.set_font('Arial', '', 8)
        for doc in employee.documents:
            # Truncate filename if too long
            filename = doc.original_filename[:50] + '...' if len(doc.original_filename) > 50 else doc.original_filename
            
            pdf.cell(80, 6, filename, 1, 0)
            pdf.cell(50, 6, doc.file_type or '', 1, 0)
            pdf.cell(30, 6, f'{doc.file_size / 1024:.1f} KB' if doc.file_size else '', 1, 0)
            pdf.cell(30, 6, doc.uploaded_at.strftime('%d/%m/%Y') if doc.uploaded_at else '', 1, 1)
        
        # Footer
        pdf.ln(10)
        pdf.set_font('Arial', 'I', 8)
        pdf.cell(0, 5, f'Generado el {datetime.now().strftime("%d/%m/%Y %H:%M")}', 0, 0, 'R')
        
        # Output to buffer
        pdf_output = pdf.output(dest='S').encode('latin1')
        pdf_buffer.write(pdf_output)
        pdf_buffer.seek(0)
        
        return pdf_buffer
    
    except Exception as e:
        print(f"Error creating employee PDF: {str(e)}")
        return None


```

#### `slugify` (línea 499)

**Docstring:**
```
Convierte un texto a formato slug (URL amigable)
Elimina caracteres especiales, espacios y acentos
```

**Código:**
```python
def slugify(text):
    """
    Convierte un texto a formato slug (URL amigable)
    Elimina caracteres especiales, espacios y acentos
    """
    if not text:
        return "empresa"
        
    # Normalizar texto (eliminar acentos)
    text = unicodedata.normalize('NFKD', str(text)).encode('ASCII', 'ignore').decode('utf-8')
    
    # Reemplazar puntos y otros caracteres especiales primero con guiones
    text = re.sub(r'[^\w\s-]', '-', text.lower())
    
    # Reemplazar espacios y múltiples guiones con un solo guión
    text = re.sub(r'[-\s]+', '-', text).strip('-_')
    
    # Si después de todo el procesamiento el slug está vacío, usar un valor por defecto
    if not text:
        return "empresa"
    
    # Añadir un identificador único al slug para evitar colisiones con nombres similares
    if '.' in str(text):
        import hashlib
        hash_suffix = hashlib.md5(str(text).encode()).hexdigest()[:6]
        text = f"{text}-{hash_suffix}"
        
    return text

```

#### `get_dashboard_stats` (línea 528)

**Docstring:**
```
Get statistics for dashboard (optimizado).
```

**Código:**
```python
def get_dashboard_stats():
    """Get statistics for dashboard (optimizado)."""
    import time
    import logging
    from flask_login import current_user
    from app import db
    from models import Employee, ActivityLog, User, Company
    from models_tasks import TaskInstance, Task, TaskStatus
    from sqlalchemy import func, case, text
    from datetime import datetime, timedelta
    
    # Iniciar temporizador para medir rendimiento
    start_time = time.time()
    logger = logging.getLogger(__name__)
    
    # Initialize stats
    stats = {
        'total_companies': 0,
        'total_employees': 0,
        'active_employees': 0,
        'employees_by_contract': {},
        'employees_by_status': {},
        'recent_activities': [],
        'employees_on_shift': 0,
        'today_tasks_total': 0,
        'today_tasks_completed': 0,
        'today_tasks_percentage': 0,
        'yesterday_tasks_percentage': 0,
        'week_tasks_percentage': 0
    }
    
    try:
        if current_user.is_admin():
            # Optimización: obtener conteos en una sola consulta en lugar de múltiples
            # esto evita varios viajes a la base de datos
            stats['total_companies'] = db.session.query(func.count(Company.id)).scalar() or 0
            
            # Optimizar la consulta de empleados
            employee_counts = db.session.query(
                func.count(Employee.id).label('total'),
                func.sum(case((Employee.is_active == True, 1), else_=0)).label('active')
            ).first()
            
            stats['total_employees'] = employee_counts.total or 0 
            stats['active_employees'] = employee_counts.active or 0
            
            # Optimización: reducir el número de consultas para los tipos de contrato y estados
            contract_counts = db.session.query(
                Employee.contract_type, func.count(Employee.id)
            ).group_by(Employee.contract_type).all()
            
            status_counts = db.session.query(
                Employee.status, func.count(Employee.id)
            ).group_by(Employee.status).all()
            
            # Procesar resultados
            for contract_type, count in contract_counts:
                if contract_type:
                    stats['employees_by_contract'][contract_type.value] = count
            
            for status, count in status_counts:
                if status:
                    stats['employees_by_status'][status.value] = count
            
            # Optimización: reducir el límite para mejorar rendimiento
            stats['recent_activities'] = ActivityLog.query.order_by(
                ActivityLog.timestamp.desc()
            ).limit(5).all()
            
        elif current_user.is_gerente() and current_user.company_id:
            company_id = current_user.company_id
            
            # Optimización: realizar una sola consulta para contar empleados
            employee_counts = db.session.query(
                func.count(Employee.id).label('total'),
                func.sum(case((Employee.is_active == True, 1), else_=0)).label('active')
            ).filter(Employee.company_id == company_id).first()
            
            stats['total_employees'] = employee_counts.total or 0
            stats['active_employees'] = employee_counts.active or 0
            
            # Optimización: consultamos tipo contrato y estado en una única operación
            contract_counts = db.session.query(
                Employee.contract_type, func.count(Employee.id)
            ).filter(Employee.company_id == company_id).group_by(Employee.contract_type).all()
            
            status_counts = db.session.query(
                Employee.status, func.count(Employee.id)
            ).filter(Employee.company_id == company_id).group_by(Employee.status).all()
            
            for contract_type, count in contract_counts:
                if contract_type:
                    stats['employees_by_contract'][contract_type.value] = count
            
            for status, count in status_counts:
                if status:
                    stats['employees_by_status'][status.value] = count
            
            # Optimización: evitar subconsulta ineficiente usando JOIN
            # en lugar de obtener todos los user_ids y luego filtrar
            stats['recent_activities'] = db.session.query(ActivityLog).join(
                User, ActivityLog.user_id == User.id
            ).filter(
                User.company_id == company_id
            ).order_by(ActivityLog.timestamp.desc()).limit(5).all()
        
        elif current_user.is_empleado() and current_user.employee:
            # Optimización: para empleados, solo obtener sus propias actividades
            # y limitar a 5 en lugar de 10 para mayor velocidad
            stats['recent_activities'] = ActivityLog.query.filter_by(
                user_id=current_user.id
            ).order_by(ActivityLog.timestamp.desc()).limit(5).all()
    except Exception as e:
        logger.error(f"Error al obtener estadísticas del dashboard: {str(e)}")
        
    # Obtener estadísticas de empleados en jornada (on shift)
    try:
        # Contar empleados que están actualmente en jornada
        employees_on_shift_query = db.session.query(func.count(Employee.id))
        
        if current_user.is_admin():
            # Para admin, contar todos los empleados en jornada
            stats['employees_on_shift'] = employees_on_shift_query.filter(
                Employee.is_on_shift == True
            ).scalar() or 0
        elif current_user.is_gerente() and current_user.company_id:
            # Para gerente, solo contar empleados de su empresa
            stats['employees_on_shift'] = employees_on_shift_query.filter(
                Employee.is_on_shift == True,
                Employee.company_id == current_user.company_id
            ).scalar() or 0
        
        # Obtener estadísticas de tareas
        today = datetime.now().date()
        yesterday = today - timedelta(days=1)
        week_start = today - timedelta(days=7)
        
        # Definir funciones de filtrado para reutilizar código
        def get_task_stats(start_date, end_date=None):
            # Si no se especifica fecha fin, usar solo el día de inicio
            if end_date is None:
                end_date = start_date
            
            # Obtener total de tareas para el período
            task_query = db.session.query(TaskInstance)
            
            # Filtrar por compañía si es gerente
            if not current_user.is_admin() and current_user.company_id:
                task_query = task_query.join(Task).join(
                    Task.location
                ).filter(
                    Task.location.has(company_id=current_user.company_id)
                )
            
            # Filtrar por fecha
            total_tasks = task_query.filter(
                TaskInstance.scheduled_date >= start_date,
                TaskInstance.scheduled_date <= end_date
            ).count()
            
            # Obtener tareas completadas
            completed_tasks = task_query.filter(
                TaskInstance.scheduled_date >= start_date,
                TaskInstance.scheduled_date <= end_date,
                TaskInstance.status == TaskStatus.COMPLETADA
            ).count()
            
            # Calcular porcentaje
            percentage = 0
            if total_tasks > 0:
                percentage = round((completed_tasks / total_tasks) * 100, 1)
                
            return total_tasks, completed_tasks, percentage
        
        # Obtener estadísticas para hoy
        today_total, today_completed, today_percentage = get_task_stats(today)
        stats['today_tasks_total'] = today_total
        stats['today_tasks_completed'] = today_completed
        stats['today_tasks_percentage'] = today_percentage
        
        # Obtener estadísticas para ayer
        _, _, yesterday_percentage = get_task_stats(yesterday)
        stats['yesterday_tasks_percentage'] = yesterday_percentage
        
        # Obtener estadísticas para la semana
        _, _, week_percentage = get_task_stats(week_start, today)
        stats['week_tasks_percentage'] = week_percentage
        
    except Exception as e:
        logger.error(f"Error al obtener estadísticas adicionales del dashboard: {str(e)}")
    
    # Registrar tiempo de ejecución para diagnóstico
    elapsed = time.time() - start_time
    logger.info(f"Dashboard stats completado en {elapsed:.3f} segundos")
    
    return stats
```

#### `get_task_stats` (línea 666)

**Código:**
```python
        def get_task_stats(start_date, end_date=None):
            # Si no se especifica fecha fin, usar solo el día de inicio
            if end_date is None:
                end_date = start_date
            
            # Obtener total de tareas para el período
            task_query = db.session.query(TaskInstance)
            
            # Filtrar por compañía si es gerente
            if not current_user.is_admin() and current_user.company_id:
                task_query = task_query.join(Task).join(
                    Task.location
                ).filter(
                    Task.location.has(company_id=current_user.company_id)
                )
            
            # Filtrar por fecha
            total_tasks = task_query.filter(
                TaskInstance.scheduled_date >= start_date,
                TaskInstance.scheduled_date <= end_date
            ).count()
            
            # Obtener tareas completadas
            completed_tasks = task_query.filter(
                TaskInstance.scheduled_date >= start_date,
                TaskInstance.scheduled_date <= end_date,
                TaskInstance.status == TaskStatus.COMPLETADA
            ).count()
            
            # Calcular porcentaje
            percentage = 0
            if total_tasks > 0:
                percentage = round((completed_tasks / total_tasks) * 100, 1)
                
            return total_tasks, completed_tasks, percentage
        
        # Obtener estadísticas para hoy
        today_total, today_completed, today_percentage = get_task_stats(today)
        stats['today_tasks_total'] = today_total
        stats['today_tasks_completed'] = today_completed
        stats['today_tasks_percentage'] = today_percentage
        
        # Obtener estadísticas para ayer
        _, _, yesterday_percentage = get_task_stats(yesterday)
        stats['yesterday_tasks_percentage'] = yesterday_percentage
        
        # Obtener estadísticas para la semana
        _, _, week_percentage = get_task_stats(week_start, today)
        stats['week_tasks_percentage'] = week_percentage
        
```

#### `create_database_backup` (línea 724)

**Docstring:**
```
Create a backup of the database
```

**Código:**
```python
def create_database_backup():
    """Create a backup of the database"""
    import os
    from datetime import datetime
    import subprocess
    
    # Create backups directory if it doesn't exist
    backup_dir = os.path.join(os.getcwd(), 'backups')
    os.makedirs(backup_dir, exist_ok=True)
    
    # Generate backup filename with timestamp
    timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
    backup_file = os.path.join(backup_dir, f'backup_{timestamp}.sql')
    
    try:
        # Get database URL from environment
        database_url = os.environ.get('DATABASE_URL')
        if not database_url:
            raise Exception("DATABASE_URL not found in environment variables")
            
        # Parse connection details from URL
        from urllib.parse import urlparse
        url = urlparse(database_url)
        
        # Create pg_dump command
        cmd = [
            'pg_dump',
            '-h', url.hostname,
            '-p', str(url.port),
            '-U', url.username,
            '-f', backup_file,
            url.path[1:]  # Database name
        ]
        
        # Execute backup
        process = subprocess.Popen(
            cmd,
            env={'PGPASSWORD': url.password},
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )
        
        output, error = process.communicate()
        
        if process.returncode != 0:
            raise Exception(f"Backup failed: {error.decode()}")
            
        return {
            'success': True,
            'file': backup_file,
            'timestamp': timestamp
        }
        
    except Exception as e:
        return {
            'success': False,
            'error': str(e)
        }

```


### utils_checkpoints.py

#### `__init__` (línea 13)

**Código:**
```python
    def __init__(self, title='Registro de Fichajes', *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.title = title
        self.set_title(title)
        self.set_author('Sistema de Fichajes')
        
        # Colores corporativos
        self.primary_color = (31, 79, 121)  # Azul corporativo oscuro
        self.secondary_color = (82, 125, 162)  # Azul corporativo medio
        self.accent_color = (236, 240, 245)  # Azul muy claro para fondos
        
```

#### `header` (línea 24)

**Código:**
```python
    def header(self):
        # Rectángulo superior de color
        self.set_fill_color(*self.primary_color)
        self.rect(0, 0, 210, 20, 'F')
        
        # Logo (si existe)
        logo_path = os.path.join(os.getcwd(), 'static', 'img', 'logo.png')
        if os.path.exists(logo_path):
            self.image(logo_path, 10, 3, 15)
        
        # Título con texto blanco (centrado verticalmente)
        self.set_font('Arial', 'B', 16)
        self.set_text_color(255, 255, 255)
        self.set_y(7)  # Centrar verticalmente el texto en la barra de 20mm
        self.cell(0, 6, self.title, 0, 1, 'C')
        
        # Restablecer color de texto
        self.set_text_color(0, 0, 0)
        
        # Línea de separación con color corporativo
        self.set_draw_color(*self.secondary_color)
        self.set_line_width(0.5)
        self.line(10, 25, 200, 25)
        
        # Restablecer color de texto
        self.set_text_color(0, 0, 0)
        self.ln(5)
        
```

#### `footer` (línea 52)

**Código:**
```python
    def footer(self):
        # Posición a 2 cm desde el final
        self.set_y(-20)
        
        # Línea de separación con color corporativo
        self.set_draw_color(*self.secondary_color)
        self.line(10, self.get_y(), 200, self.get_y())
        
        # Rectángulo inferior de color
        self.set_fill_color(*self.primary_color)
        self.rect(0, self.h - 12, 210, 12, 'F')
        
        # Número de página con texto blanco (centrado verticalmente)
        self.set_font('Arial', 'B', 9)
        self.set_text_color(255, 255, 255)
        self.set_y(self.h - 8)  # Centrar verticalmente en la barra de 12mm
        self.cell(0, 4, f'Página {self.page_no()}', 0, 0, 'C')


```

#### `draw_signature` (línea 71)

**Docstring:**
```
Dibuja la firma en el PDF desde datos base64
```

**Código:**
```python
def draw_signature(pdf, signature_data, x, y, width=50, height=20):
    """Dibuja la firma en el PDF desde datos base64"""
    if not signature_data:
        return
    
    try:
        # Procesar los datos base64
        if 'data:image/png;base64,' in signature_data:
            signature_data = signature_data.split('data:image/png;base64,')[1]
        
        # Decodificar los datos
        decoded_data = base64.b64decode(signature_data)
        
        # Crear una imagen temporal
        with tempfile.NamedTemporaryFile(delete=False, suffix='.png') as temp_file:
            temp_file.write(decoded_data)
            temp_file_path = temp_file.name
        
        # Dibujar la imagen en el PDF
        pdf.image(temp_file_path, x, y, width, height)
        
        # Eliminar el archivo temporal
        os.unlink(temp_file_path)
        
    except Exception as e:
        print(f"Error al dibujar la firma: {str(e)}")


```

#### `generate_pdf_report` (línea 99)

**Docstring:**
```
Genera un informe PDF de los registros de fichaje
```

**Código:**
```python
def generate_pdf_report(records, start_date, end_date, include_signature=True):
    """Genera un informe PDF de los registros de fichaje"""
    # Agrupar registros por empleado
    employees_records = {}
    
    for record in records:
        employee_id = record.employee_id
        
        if employee_id not in employees_records:
            employees_records[employee_id] = {
                'employee': record.employee,
                'records': []
            }
            
        employees_records[employee_id]['records'].append(record)
    
    # Crear PDF
    pdf = CheckPointPDF(title=f'Informe de Fichajes: {start_date.strftime("%d/%m/%Y")} - {end_date.strftime("%d/%m/%Y")}')
    pdf.set_auto_page_break(auto=True, margin=15)
    
    # Para cada empleado
    for employee_id, data in employees_records.items():
        employee = data['employee']
        records = data['records']
        company = employee.company
        
        # Añadir una nueva página para cada empleado
        pdf.add_page()
        
        # Añadir espacio adicional después del encabezado para evitar solapamiento con la barra
        pdf.ln(8)  # 8mm de espacio adicional
        
        # Calcular posiciones centradas (el ancho de la página es 210mm)
        # Usaremos un margen de 20mm a cada lado
        left_margin = 20
        right_margin = 20
        usable_width = 210 - left_margin - right_margin
        block_width = 85  # Cada bloque tendrá 85mm de ancho
        gap = 10          # 10mm de separación entre bloques
        
        # Posición X del primer bloque
        block1_x = left_margin
        # Posición X del segundo bloque
        block2_x = block1_x + block_width + gap
        
        # Crear fondos de color para los bloques de información
        # Bloque empleado (izquierda)
        pdf.set_fill_color(*pdf.accent_color)
        pdf.rect(block1_x, pdf.get_y(), block_width, 30, 'F')
        
        # Bloque empresa (derecha)
        pdf.rect(block2_x, pdf.get_y(), block_width, 30, 'F')
        
        # Líneas de contorno con color corporativo
        pdf.set_draw_color(*pdf.secondary_color)
        pdf.rect(block1_x, pdf.get_y(), block_width, 30, 'D')
        pdf.rect(block2_x, pdf.get_y(), block_width, 30, 'D')
        
        initial_y = pdf.get_y()
        
        # Títulos con fondo de color primario
        pdf.set_fill_color(*pdf.secondary_color)
        pdf.set_text_color(255, 255, 255)
        pdf.set_font('Arial', 'B', 10)
        
        # Título empleado
        pdf.rect(block1_x, pdf.get_y(), block_width, 6, 'F')
        pdf.set_xy(block1_x, pdf.get_y())
        pdf.cell(block_width, 6, 'DATOS DEL EMPLEADO', 0, 0, 'C')
        
        # Título empresa
        pdf.rect(block2_x, pdf.get_y(), block_width, 6, 'F')
        pdf.set_xy(block2_x, pdf.get_y())
        pdf.cell(block_width, 6, 'DATOS DE LA EMPRESA', 0, 1, 'C')
        
        # Restaurar color de texto
        pdf.set_text_color(0, 0, 0)
        
        # Espacio para etiquetas y valores
        label_width = 25
        value_width = block_width - label_width - 5  # 5mm de margen interno
        label_x1 = block1_x + 2  # 2mm de margen interno
        value_x1 = label_x1 + label_width
        label_x2 = block2_x + 2
        value_x2 = label_x2 + label_width
        
        # Etiquetas en negrita
        pdf.set_font('Arial', 'B', 9)
        pdf.set_xy(label_x1, pdf.get_y() + 2)
        pdf.cell(label_width, 5, 'Nombre:', 0, 0)
        
        pdf.set_xy(label_x2, pdf.get_y())
        pdf.cell(label_width, 5, 'Nombre:', 0, 1)
        
        # Datos en texto normal
        pdf.set_font('Arial', '', 9)
        pdf.set_xy(value_x1, pdf.get_y() - 5)
        pdf.cell(value_width, 5, f"{employee.first_name} {employee.last_name}", 0, 0)
        
        pdf.set_xy(value_x2, pdf.get_y())
        pdf.cell(value_width, 5, f"{company.name}", 0, 1)
        
        # DNI
        pdf.set_font('Arial', 'B', 9)
        pdf.set_xy(label_x1, pdf.get_y() + 2)
        pdf.cell(label_width, 5, 'DNI/NIE:', 0, 0)
        
        # CIF
        pdf.set_xy(label_x2, pdf.get_y())
        pdf.cell(label_width, 5, 'CIF:', 0, 1)
        
        pdf.set_font('Arial', '', 9)
        pdf.set_xy(value_x1, pdf.get_y() - 5)
        pdf.cell(value_width, 5, f"{employee.dni}", 0, 0)
        
        pdf.set_xy(value_x2, pdf.get_y())
        pdf.cell(value_width, 5, f"{company.tax_id}", 0, 1)
        
        # Puesto
        pdf.set_font('Arial', 'B', 9)
        pdf.set_xy(label_x1, pdf.get_y() + 2)
        pdf.cell(label_width, 5, 'Puesto:', 0, 0)
        
        # Dirección
        pdf.set_xy(label_x2, pdf.get_y())
        pdf.cell(label_width, 5, 'Dirección:', 0, 1)
        
        pdf.set_font('Arial', '', 9)
        pdf.set_xy(value_x1, pdf.get_y() - 5)
        pdf.cell(value_width, 5, f"{employee.position or '-'}", 0, 0)
        
        empresa_direccion = f"{company.address or ''}, {company.city or ''}"
        if empresa_direccion.strip() == ',':
            empresa_direccion = '-'
        pdf.set_xy(value_x2, pdf.get_y())
        pdf.cell(value_width, 5, empresa_direccion, 0, 1)
        
        # Asegurar que avanzamos correctamente después de los bloques
        pdf.set_y(initial_y + 35)  # Espacio antes de la tabla
        
        # Título de la tabla con fondo corporativo
        pdf.set_fill_color(*pdf.primary_color)
        pdf.set_text_color(255, 255, 255)
        pdf.set_font('Arial', 'B', 12)
        
        # Centrar el título de la tabla usando los mismos márgenes
        title_width = 170  # Ancho del título
        title_x = (210 - title_width) / 2  # Centrado en la página
        
        # Dibujar el rectángulo para el título
        pdf.rect(title_x, pdf.get_y(), title_width, 10, 'F')
        
        # Guardar la posición Y actual
        current_y = pdf.get_y()
        
        # Posicionar el texto verticalmente centrado dentro de la barra (subir un poco)
        pdf.set_y(current_y + 2.5)  # 2.5mm para centrar el texto en la barra de 10mm de alto
        pdf.cell(0, 5, 'REGISTROS DE FICHAJE', 0, 1, 'C')
        
        # Volver a la posición después del rectángulo y añadir espacio
        pdf.set_y(current_y + 10 + 5)  # 5mm de espacio adicional entre la barra y la tabla
        
        # Restaurar color de texto
        pdf.set_text_color(0, 0, 0)
        
        # Tabla de registros
        pdf.set_font('Arial', 'B', 10)
        
        # Cabecera de tabla
        col_widths = [35, 30, 30, 30, 40]
        header = ['Fecha', 'Entrada', 'Salida', 'Horas', 'Firma']
        table_width = sum(col_widths)
        
        # Calcular posición X para centrar la tabla
        table_x = (210 - table_width) / 2
        pdf.set_x(table_x)
        
        # Dibujar cabecera con color corporativo
        pdf.set_fill_color(*pdf.secondary_color)
        pdf.set_text_color(255, 255, 255)
        pdf.set_draw_color(*pdf.primary_color)
        pdf.set_line_width(0.3)
        
        for i, col in enumerate(header):
            pdf.cell(col_widths[i], 10, col, 1, 0, 'C', True)
        pdf.ln()
        
        # Restaurar color de texto para las filas
        pdf.set_text_color(0, 0, 0)
        pdf.set_font('Arial', '', 10)
        
        # Color alternado para las filas (efecto cebra)
        row_count = 0
        
        for record in records:
            # Posicionar al inicio de la fila, centrado
            pdf.set_x(table_x)
            
            # Cambiar color de fondo según fila par/impar
            if row_count % 2 == 0:
                pdf.set_fill_color(255, 255, 255)  # Blanco
            else:
                pdf.set_fill_color(*pdf.accent_color)  # Color claro
            
            # Fecha
            pdf.cell(col_widths[0], 10, record.check_in_time.strftime('%d/%m/%Y'), 1, 0, 'C', True)
            
            # Hora entrada
            pdf.cell(col_widths[1], 10, record.check_in_time.strftime('%H:%M'), 1, 0, 'C', True)
            
            # Hora salida
            if record.check_out_time:
                pdf.cell(col_widths[2], 10, record.check_out_time.strftime('%H:%M'), 1, 0, 'C', True)
            else:
                pdf.cell(col_widths[2], 10, '-', 1, 0, 'C', True)
            
            # Horas trabajadas
            duration = record.duration()
            if duration is not None:
                hours_str = f"{duration:.2f}h"
            else:
                hours_str = '-'
            pdf.cell(col_widths[3], 10, hours_str, 1, 0, 'C', True)
            
            # Celda para firma
            y_pos_before = pdf.get_y()
            pdf.cell(col_widths[4], 10, '', 1, 0, 'C', True)
            
            # Dibujar firma en la celda si existe
            if include_signature and record.has_signature and record.signature_data:
                # Guardar posición actual
                x_pos = pdf.get_x() - col_widths[4]
                # Dibujar la firma dentro de la celda
                draw_signature(pdf, record.signature_data, x_pos + 2, y_pos_before + 1, col_widths[4] - 4, 8)
            
            pdf.ln()
            row_count += 1
    
    # Crear un archivo temporal en disco
    temp_file = tempfile.NamedTemporaryFile(delete=False, suffix='.pdf')
    temp_filename = temp_file.name
    temp_file.close()
    
    # Guardar el PDF en el archivo temporal
    pdf.output(temp_filename)
    
    return temp_filename


# Variable global para control de ejecuciones simultáneas
# Cada vez que process_auto_checkouts() se está ejecutando, este valor es True
# SISTEMA DE AUTO-CHECKOUT ELIMINADO
# El sistema de auto-checkout ha sido eliminado por completo para evitar procesamiento
# automático de los registros sin fichaje de salida.
    
    return total_processed
```


### utils_tasks.py

#### `create_default_local_user` (línea 8)

**Docstring:**
```
Crea un usuario local por defecto si no existe ninguno para la ubicación.
```

**Código:**
```python
def create_default_local_user():
    """Crea un usuario local por defecto si no existe ninguno para la ubicación."""
    # Comprobamos si existen ubicaciones
    locations = Location.query.filter_by(is_active=True).all()
    if not locations:
        return None
    
    # Para cada ubicación, comprobamos si tiene usuarios locales
    for location in locations:
        local_users = LocalUser.query.filter_by(location_id=location.id).count()
        
        if local_users == 0:
            # Crear usuario admin por defecto para esta ubicación
            default_user = LocalUser(
                name="Admin",
                last_name="Local",
                username=f"admin_{location.id}",
                pin="1234",  # Se establecerá el hash más adelante
                is_active=True,
                location_id=location.id,
                created_at=datetime.utcnow(),
                updated_at=datetime.utcnow()
            )
            
            # Establecer el PIN real
            default_user.set_pin("1234")
            
            db.session.add(default_user)
            db.session.commit()
            
            return default_user
    
    return None

```

#### `get_portal_session` (línea 42)

**Docstring:**
```
Obtiene información de la sesión del portal.
```

**Código:**
```python
def get_portal_session():
    """Obtiene información de la sesión del portal."""
    return {
        'location_id': session.get('portal_location_id'),
        'location_name': session.get('portal_location_name'),
        'user_id': session.get('local_user_id'),
        'user_name': session.get('local_user_name')
    }
    
```

#### `clear_portal_session` (línea 51)

**Docstring:**
```
Limpia la sesión del portal.
```

**Código:**
```python
def clear_portal_session():
    """Limpia la sesión del portal."""
    if 'portal_location_id' in session:
        session.pop('portal_location_id')
    if 'portal_location_name' in session:
        session.pop('portal_location_name')
    if 'local_user_id' in session:
        session.pop('local_user_id')
    if 'local_user_name' in session:
        session.pop('local_user_name')
        
```

#### `generate_secure_password` (línea 62)

**Docstring:**
```
Genera una contraseña segura con el formato estandarizado 'Portal[ID]2025!'.
```

**Código:**
```python
def generate_secure_password(location_id=None):
    """Genera una contraseña segura con el formato estandarizado 'Portal[ID]2025!'."""
    if location_id:
        # Utilizamos un formato estándar para facilitar recordar la contraseña
        # Formato: Portal[ID]2025!
        return f"Portal{location_id}2025!"
    else:
        # Si no se proporciona ID, generamos una contraseña aleatoria más compleja
        # Asegurarnos de incluir al menos: una mayúscula, una minúscula, un número y un carácter especial
        uppercase = random.choice(string.ascii_uppercase)
        lowercase = random.choice(string.ascii_lowercase)
        digit = random.choice(string.digits)
        special = random.choice("!@#$%&*")
        
        # El resto de caracteres aleatorios
        remaining_length = 8  # Longitud total 12
        all_chars = string.ascii_letters + string.digits + "!@#$%&*"
        rest = ''.join(random.choice(all_chars) for _ in range(remaining_length))
        
        # Combinar todos los caracteres y mezclar
        password = uppercase + lowercase + digit + special + rest
        password_list = list(password)
        random.shuffle(password_list)
        
        return ''.join(password_list)

```

#### `regenerate_portal_password` (línea 88)

**Docstring:**
```
Regenera y actualiza la contraseña del portal de una ubicación.

Args:
    location_id: ID de la ubicación
    only_return: Si es True, solo devuelve la contraseña actual sin regenerarla

Returns:
    La contraseña actual o la nueva contraseña regenerada
```

**Código:**
```python
def regenerate_portal_password(location_id, only_return=False):
    """Regenera y actualiza la contraseña del portal de una ubicación.
    
    Args:
        location_id: ID de la ubicación
        only_return: Si es True, solo devuelve la contraseña actual sin regenerarla
    
    Returns:
        La contraseña actual o la nueva contraseña regenerada
    """
    try:
        location = Location.query.get(location_id)
        if not location:
            return None
            
        # Utilizamos un formato fijo para las contraseñas del portal
        # Formato estandarizado: Portal[ID]2025!
        # Esto permite que siempre podamos recuperar la contraseña sin almacenarla en texto plano
        fixed_password = generate_secure_password(location_id)
            
        if only_return:
            # Solo devolvemos la contraseña actual sin cambiar nada
            # Como usamos un formato fijo, siempre podemos reconstruirla
            return fixed_password
            
        # Actualizamos la contraseña en la base de datos
        location.set_portal_password(fixed_password)
        
        db.session.commit()
        return fixed_password
    except Exception as e:
        if not only_return:  # Solo hacemos rollback si estábamos modificando la BD
            db.session.rollback()
        print(f"Error al manipular contraseña: {str(e)}")
        return None
```


### verify_checkpoint_closures.py

#### `check_pending_records_after_hours` (línea 18)

**Docstring:**
```
Busca fichajes pendientes en puntos de fichaje que deberían haberse cerrado
automáticamente por estar fuera del horario de funcionamiento.
```

**Código:**
```python
def check_pending_records_after_hours():
    """
    Busca fichajes pendientes en puntos de fichaje que deberían haberse cerrado
    automáticamente por estar fuera del horario de funcionamiento.
    """
    current_time = get_current_time()
    current_time_utc = datetime.utcnow()
    
    print(f"\n{'=' * 100}")
    print(f"VERIFICACIÓN DE FICHAJES PENDIENTES FUERA DE HORARIO")
    print(f"{'=' * 100}")
    print(f"Fecha/hora actual: {current_time} ({TIMEZONE})")
    print(f"Fecha/hora UTC: {current_time_utc}")
    
    # Buscar puntos de fichaje con horario de funcionamiento configurado
    checkpoints = CheckPoint.query.filter(
        CheckPoint.enforce_operation_hours == True,
        CheckPoint.operation_end_time.isnot(None),
        CheckPoint.status == CheckPointStatus.ACTIVE
    ).all()
    
    if not checkpoints:
        print("No hay puntos de fichaje con horario de funcionamiento configurado.")
        return True
    
    print(f"\nPuntos de fichaje activos con horario: {len(checkpoints)}")
    
    # Estadísticas globales
    total_pending_records = 0
    total_incidents = 0
    
    for checkpoint in checkpoints:
        print(f"\n{'-' * 90}")
        print(f"Punto de fichaje: {checkpoint.name} (ID: {checkpoint.id})")
        print(f"Empresa: {checkpoint.company.name}")
        print(f"Horario: {checkpoint.operation_start_time} - {checkpoint.operation_end_time}")
        
        # Obtener hora de fin del punto de fichaje
        end_time = checkpoint.operation_end_time
        
        # Obtener fecha actual en Madrid
        current_date = get_current_time().date()
        
        # Crear datetime con la fecha actual y la hora de fin
        end_datetime = datetime.combine(current_date, end_time)
        end_datetime = TIMEZONE.localize(end_datetime)
        
        # Calcular si estamos después de la hora de fin
        is_past_end_time = current_time.time() > end_time
        
        print(f"¿Pasada hora de cierre? {'Sí' if is_past_end_time else 'No'}")
        
        # Si estamos pasada la hora de fin, buscar registros pendientes
        if is_past_end_time:
            # Buscar registros pendientes para este punto de fichaje
            pending_records = CheckPointRecord.query.filter(
                CheckPointRecord.checkpoint_id == checkpoint.id,
                CheckPointRecord.check_out_time.is_(None)
            ).all()
            
            if pending_records:
                print(f"⚠️ ALERTA: Se encontraron {len(pending_records)} registros pendientes fuera de horario.")
                print(f"Estos registros deberían haberse cerrado automáticamente.")
                
                for record in pending_records:
                    print(f"  • Empleado: {record.employee.first_name} {record.employee.last_name}")
                    print(f"    Entrada: {record.check_in_time}")
                    print(f"    ID registro: {record.id}")
                
                total_pending_records += len(pending_records)
            else:
                print(f"✅ Correcto: No hay registros pendientes fuera de horario.")
            
            # Verificar incidencias generadas por cierres automáticos en las últimas 24 horas
            yesterday = current_time_utc - timedelta(days=1)
            incidents = CheckPointIncident.query.join(CheckPointRecord).filter(
                CheckPointRecord.checkpoint_id == checkpoint.id,
                CheckPointIncident.incident_type == CheckPointIncidentType.MISSED_CHECKOUT,
                CheckPointIncident.created_at >= yesterday
            ).all()
            
            if incidents:
                print(f"📊 Incidencias de cierre automático en últimas 24h: {len(incidents)}")
                total_incidents += len(incidents)
    
    print(f"\n{'=' * 100}")
    print(f"RESUMEN DE LA VERIFICACIÓN")
    print(f"{'=' * 100}")
    print(f"Puntos de fichaje verificados: {len(checkpoints)}")
    print(f"Registros pendientes fuera de horario: {total_pending_records}")
    print(f"Incidencias de cierre automático en últimas 24h: {total_incidents}")
    
    if total_pending_records > 0:
        print(f"\n⚠️ ATENCIÓN: Se han encontrado {total_pending_records} registros pendientes que deberían haberse cerrado.")
        print(f"Esto puede indicar un problema con el sistema de cierre automático.")
        print(f"Recomendación: Verificar que el servicio 'scheduled_checkpoints_closer.py' esté en ejecución.")
        return False
    else:
        print(f"\n✅ ÉXITO: No se han encontrado registros pendientes fuera de horario.")
        print(f"El sistema de cierre automático está funcionando correctamente.")
        return True

if __name__ == "__main__":
    app = create_app()
    with app.app_context():
        success = check_pending_records_after_hours()
    
    sys.exit(0 if success else 1)
```


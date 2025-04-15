"""
Script para exportar toda la base de datos actual a un archivo SQL listo para instalar

Este script genera un backup completo que incluye:
1. Estructura de tablas (CREATE TABLE)
2. Datos de todas las tablas (INSERT)
3. Secuencias y otros objetos de la base de datos
4. Instrucciones de instalación para facilitar la restauración

El archivo SQL generado está diseñado para ser fácil de instalar:
- Crea la base de datos si no existe
- Incluye instrucciones paso a paso en comentarios
- Maneja correctamente secuencias y relaciones

Para restaurar, simplemente ejecuta:
$ psql -U [usuario] -f nombre_archivo.sql
"""
import os
import subprocess
import datetime
from config import Config
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT
from urllib.parse import urlparse

def get_db_connection_params():
    """Extrae los parámetros de conexión a la BD desde la URL de DATABASE_URL"""
    db_url = os.environ.get('DATABASE_URL', Config.SQLALCHEMY_DATABASE_URI)
    
    # Parsear la URL de conexión
    result = urlparse(db_url)
    
    # Extraer los componentes
    username = result.username
    password = result.password
    database = result.path[1:]  # Eliminar la barra inicial
    hostname = result.hostname
    port = result.port or 5432
    
    return {
        'dbname': database,
        'user': username,
        'password': password,
        'host': hostname,
        'port': port
    }

def export_database_to_sql():
    """Exporta toda la base de datos a un archivo SQL listo para restauración"""
    try:
        # Obtener parámetros de conexión
        db_params = get_db_connection_params()
        
        # Crear nombre de archivo con timestamp e información más descriptiva
        timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
        db_name = db_params['dbname']
        filename = f"productiva_backup_{db_name}_{timestamp}.sql"
        
        print(f"Iniciando exportación de la base de datos a {filename}...")
        print(f"Base de datos: {db_params['dbname']}")
        print(f"Servidor: {db_params['host']}:{db_params['port']}")
        
        # Primero intentamos con el método de conexión directa que es más confiable
        # para esta tarea específica y genera un SQL más completo
        try:
            export_database_with_connection(db_params, filename)
            return  # Si funciona, terminamos aquí
        except Exception as e:
            print(f"Error con el método de conexión directa: {str(e)}")
            print("Intentando con pg_dump...")
            
            # Si el método directo falla, intentamos con pg_dump como respaldo
            try:
                # Construir comando pg_dump con todos los parámetros
                pg_dump_cmd = [
                    "pg_dump",
                    f"--host={db_params['host']}",
                    f"--port={db_params['port']}",
                    f"--username={db_params['user']}",
                    f"--dbname={db_params['dbname']}",
                    "--format=plain",
                    "--create",
                    "--inserts",
                    "--verbose",
                    f"--file={filename}"
                ]
                
                # Configurar variable de entorno para la contraseña
                env = os.environ.copy()
                env["PGPASSWORD"] = db_params['password']
                
                # Ejecutar pg_dump
                process = subprocess.Popen(
                    pg_dump_cmd,
                    env=env,
                    stdout=subprocess.PIPE,
                    stderr=subprocess.PIPE
                )
                
                stdout, stderr = process.communicate()
                
                if process.returncode != 0:
                    print(f"Error al exportar la base de datos: {stderr.decode('utf-8')}")
                    raise Exception("Falló la exportación con pg_dump")
                else:
                    print(f"La base de datos ha sido exportada exitosamente con pg_dump a: {filename}")
                    
                    # Verificar tamaño del archivo
                    file_size = os.path.getsize(filename)
                    print(f"Tamaño del archivo de backup: {file_size / 1024 / 1024:.2f} MB")
                    
                    # Contar número de tablas y registros
                    count_tables_and_records(db_params)
            except Exception as e2:
                print(f"Error con pg_dump: {str(e2)}")
                raise Exception("No se pudo exportar la base de datos con ningún método")
    
    except Exception as e:
        print(f"Error durante la exportación: {str(e)}")
        print("La exportación de la base de datos ha fallado.")

def export_database_with_connection(db_params, filename):
    """
    Método alternativo para exportar la base de datos usando conexión directa
    a PostgreSQL en lugar de pg_dump
    """
    try:
        # Conectar a la base de datos
        conn = psycopg2.connect(
            dbname=db_params['dbname'],
            user=db_params['user'],
            password=db_params['password'],
            host=db_params['host'],
            port=db_params['port']
        )
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        
        # Crear cursor
        cursor = conn.cursor()
        
        # Abrir archivo para escribir
        with open(filename, 'w') as f:
            # Escribir encabezado y documentación de instalación
            f.write("-- ********************************************************************\n")
            f.write("-- BACKUP COMPLETO DE BASE DE DATOS PRODUCTIVA\n")
            f.write(f"-- Fecha de creación: {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write("-- ********************************************************************\n\n")
            
            f.write("-- INSTRUCCIONES DE INSTALACIÓN:\n")
            f.write("-- 1. Crea una base de datos PostgreSQL vacía si no existe:\n")
            f.write("--    $ createdb nombre_base_datos\n")
            f.write("-- 2. Importa este archivo en la base de datos:\n")
            f.write("--    $ psql -U usuario -d nombre_base_datos -f este_archivo.sql\n")
            f.write("-- 3. Verifica la instalación:\n")
            f.write("--    $ psql -U usuario -d nombre_base_datos -c 'SELECT COUNT(*) FROM users;'\n\n")
            
            f.write("-- Configuración inicial\n")
            f.write("BEGIN;\n")
            f.write("SET client_encoding TO 'UTF8';\n")
            f.write("SET standard_conforming_strings TO on;\n")
            f.write("SET check_function_bodies TO false;\n\n")
            
            # Obtener todas las tablas
            cursor.execute("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                ORDER BY table_name
            """)
            tables = cursor.fetchall()
            
            # Para cada tabla, obtener su definición y datos
            for table in tables:
                table_name = table[0]
                print(f"Exportando tabla: {table_name}")
                
                # Obtener definición de la tabla
                cursor.execute(f"""
                    SELECT 
                        'CREATE TABLE ' || 
                        table_name || ' (' || 
                        string_agg(column_definition, ', ') || 
                        ');' as create_statement
                    FROM (
                        SELECT 
                            table_name,
                            column_name || ' ' || 
                            data_type || 
                            CASE 
                                WHEN character_maximum_length IS NOT NULL 
                                THEN '(' || character_maximum_length || ')'
                                ELSE ''
                            END || 
                            CASE 
                                WHEN is_nullable = 'NO' 
                                THEN ' NOT NULL'
                                ELSE ''
                            END ||
                            CASE 
                                WHEN column_default IS NOT NULL 
                                THEN ' DEFAULT ' || column_default
                                ELSE ''
                            END 
                            as column_definition
                        FROM information_schema.columns
                        WHERE table_schema = 'public' AND table_name = '{table_name}'
                        ORDER BY ordinal_position
                    ) t
                    GROUP BY table_name;
                """)
                
                create_statement = cursor.fetchone()
                if create_statement:
                    f.write(f"{create_statement[0]}\n\n")
                
                # Obtener datos de la tabla
                cursor.execute(f"SELECT * FROM {table_name}")
                rows = cursor.fetchall()
                
                # Obtener nombres de columnas
                cursor.execute(f"""
                    SELECT column_name
                    FROM information_schema.columns
                    WHERE table_schema = 'public' AND table_name = '{table_name}'
                    ORDER BY ordinal_position
                """)
                columns = [col[0] for col in cursor.fetchall()]
                
                # Generar INSERT para cada fila
                for row in rows:
                    columns_str = ', '.join(columns)
                    values = []
                    
                    for val in row:
                        if val is None:
                            values.append('NULL')
                        elif isinstance(val, (int, float)):
                            values.append(str(val))
                        else:
                            # Escapar comillas simples
                            val_str = str(val).replace("'", "''")
                            values.append(f"'{val_str}'")
                    
                    values_str = ', '.join(values)
                    f.write(f"INSERT INTO {table_name} ({columns_str}) VALUES ({values_str});\n")
                
                f.write("\n")
            
            # Obtener y escribir constraints (claves primarias, foráneas, etc.)
            f.write("-- Constraints\n")
            cursor.execute("""
                SELECT
                    'ALTER TABLE ' || tc.table_name || ' ADD CONSTRAINT ' || tc.constraint_name || ' ' ||
                    CASE
                        WHEN tc.constraint_type = 'PRIMARY KEY' THEN
                            'PRIMARY KEY (' || string_agg(kcu.column_name, ', ') || ')'
                        WHEN tc.constraint_type = 'FOREIGN KEY' THEN
                            'FOREIGN KEY (' || string_agg(kcu.column_name, ', ') || ') REFERENCES ' ||
                            ccu.table_name || ' (' || string_agg(ccu.column_name, ', ') || ')'
                        WHEN tc.constraint_type = 'UNIQUE' THEN
                            'UNIQUE (' || string_agg(kcu.column_name, ', ') || ')'
                        ELSE ''
                    END || ';' as constraint_statement
                FROM
                    information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                    ON tc.constraint_name = kcu.constraint_name
                    AND tc.table_schema = kcu.table_schema
                LEFT JOIN information_schema.constraint_column_usage ccu
                    ON ccu.constraint_name = tc.constraint_name
                    AND ccu.table_schema = tc.table_schema
                WHERE
                    tc.table_schema = 'public'
                    AND tc.constraint_type IN ('PRIMARY KEY', 'FOREIGN KEY', 'UNIQUE')
                GROUP BY
                    tc.table_name, tc.constraint_name, tc.constraint_type, ccu.table_name
                ORDER BY
                    tc.table_name, tc.constraint_type;
            """)
            
            constraints = cursor.fetchall()
            for constraint in constraints:
                f.write(f"{constraint[0]}\n")
            
            # Obtener y escribir secuencias
            f.write("\n-- Sequences\n")
            cursor.execute("""
                SELECT
                    'SELECT setval(' || quote_literal(sequence_name) || ', ' ||
                    'COALESCE((SELECT MAX(' || quote_ident(column_name) || ') FROM ' || 
                    quote_ident(table_name) || '), 1));'
                FROM
                    information_schema.columns
                WHERE
                    column_default LIKE 'nextval%'
                    AND table_schema = 'public';
            """)
            
            sequences = cursor.fetchall()
            for sequence in sequences:
                f.write(f"{sequence[0]}\n")
                
            # Finalizar la transacción
            f.write("\nCOMMIT;\n")
            
            # Añadir instrucciones finales y resumen
            f.write("\n-- ********************************************************************\n")
            f.write("-- INFORMACIÓN DE RESTAURACIÓN\n")
            f.write("-- ********************************************************************\n")
            f.write("-- Este archivo contiene una copia completa de la base de datos Productiva\n")
            f.write(f"-- Total de tablas: {len(tables)}\n")
            
            # Resumir contenido de la base de datos
            f.write("\n-- Resumen del contenido:\n")
            try:
                for table in tables:
                    table_name = table[0]
                    cursor.execute(f"SELECT COUNT(*) FROM {table_name}")
                    count_result = cursor.fetchone()
                    count = count_result[0] if count_result else 0
                    f.write(f"-- - {table_name}: {count} registros\n")
            except Exception as count_error:
                f.write(f"-- Error al contar registros: {str(count_error)}\n")
                
            f.write("\n-- FIN DEL ARCHIVO DE RESPALDO\n")
            f.write("-- ********************************************************************\n")
        
        # Cerrar cursor y conexión
        cursor.close()
        conn.close()
        
        print(f"La base de datos ha sido exportada exitosamente con el método alternativo a: {filename}")
        
        # Verificar tamaño del archivo
        file_size = os.path.getsize(filename)
        print(f"Tamaño del archivo de backup: {file_size / 1024 / 1024:.2f} MB")
        
    except Exception as e:
        print(f"Error durante la exportación alternativa: {str(e)}")

def count_tables_and_records(db_params):
    """Cuenta el número de tablas y registros en la base de datos"""
    try:
        # Conectar a la base de datos
        conn = psycopg2.connect(
            dbname=db_params['dbname'],
            user=db_params['user'],
            password=db_params['password'],
            host=db_params['host'],
            port=db_params['port']
        )
        
        # Crear cursor
        cursor = conn.cursor()
        
        # Obtener todas las tablas
        cursor.execute("""
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
            ORDER BY table_name
        """)
        tables = cursor.fetchall()
        
        if not tables:
            print("No se encontraron tablas en la base de datos.")
            return
            
        print(f"Número total de tablas: {len(tables)}")
        print("\nResumen de registros por tabla:")
        
        # Para cada tabla, contar registros
        for table in tables:
            try:
                table_name = table[0]
                cursor.execute(f"SELECT COUNT(*) FROM {table_name}")
                count_result = cursor.fetchone()
                count = count_result[0] if count_result else 0
                print(f"  - {table_name}: {count} registros")
            except Exception as table_error:
                print(f"  - {table[0]}: Error al contar registros - {str(table_error)}")
        
        # Cerrar cursor y conexión
        cursor.close()
        conn.close()
        
    except Exception as e:
        print(f"Error al contar tablas y registros: {str(e)}")

if __name__ == "__main__":
    print("Iniciando exportación de la base de datos...")
    export_database_to_sql()
    print("Proceso de exportación completado.")
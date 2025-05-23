"""
Script para ayudar a depurar problemas de persistencia en el formulario de empleados.
Este script intentará leer y escribir directamente en la base de datos para ver dónde está el problema.
"""
from datetime import datetime
import sqlalchemy
from app import app, db
from models import Employee, EmployeeStatus, ContractType, Company
import sys

def print_separator():
    print("-" * 80)

def test_date_field_update(employee_id=1, test_date="2025-12-25"):
    """
    Prueba específicamente la actualización del campo end_date de un empleado.
    
    Args:
        employee_id: ID del empleado a actualizar
        test_date: Fecha de prueba a establecer en formato YYYY-MM-DD
    """
    with app.app_context():
        print_separator()
        print(f"PRUEBA DE ACTUALIZACIÓN DE end_date (Empleado ID: {employee_id})")
        print_separator()
        
        # 1. Recuperar el empleado
        employee = Employee.query.get(employee_id)
        if not employee:
            print(f"ERROR: No se encontró empleado con ID {employee_id}")
            return
            
        # 2. Mostrar información actual
        print(f"Empleado: {employee.first_name} {employee.last_name}")
        print(f"end_date actual: '{employee.end_date}', tipo: {type(employee.end_date)}")
        
        # 3. Actualizar el campo end_date directamente
        print(f"\nActualizando end_date a: '{test_date}'")
        old_value = employee.end_date
        
        try:
            # Actualizar con SQLAlchemy
            employee.end_date = test_date
            print(f"Valor después de asignación: '{employee.end_date}', tipo: {type(employee.end_date)}")
            
            # Commit de los cambios
            db.session.commit()
            print("Commit realizado correctamente")
            
            # Verificar que los cambios se han guardado
            db.session.refresh(employee)
            print(f"Valor después de refresh: '{employee.end_date}', tipo: {type(employee.end_date)}")
            
            # Consultar directamente la base de datos para estar seguros
            result = db.session.execute(sqlalchemy.text(
                f"SELECT end_date FROM employees WHERE id = {employee_id}"
            )).fetchone()
            
            db_value = result[0] if result else None
            print(f"Valor en base de datos (SQL): '{db_value}', tipo: {type(db_value)}")
            
            if employee.end_date == test_date:
                print("\n✅ ÉXITO: El valor se actualizó correctamente")
            else:
                print(f"\n❌ ERROR: El valor no se actualizó correctamente. Se esperaba '{test_date}' pero se obtuvo '{employee.end_date}'")
                
        except Exception as e:
            print(f"\n❌ ERROR: Excepción al actualizar: {str(e)}")
            db.session.rollback()
            print("Rollback realizado")
        
        # 4. Restaurar el valor original si es necesario
        if old_value != employee.end_date and '--keep' not in sys.argv:
            try:
                print(f"\nRestaurando valor original: '{old_value}'")
                employee.end_date = old_value
                db.session.commit()
                print("Valor restaurado correctamente")
            except Exception as e:
                print(f"ERROR al restaurar: {str(e)}")
                db.session.rollback()
        
        print_separator()

def test_sql_direct_update(employee_id=1, test_date="2025-12-31"):
    """
    Prueba la actualización directa con SQL en lugar de usando el ORM.
    
    Args:
        employee_id: ID del empleado a actualizar
        test_date: Fecha de prueba a establecer en formato YYYY-MM-DD
    """
    with app.app_context():
        print_separator()
        print(f"PRUEBA DE ACTUALIZACIÓN DIRECTA SQL (Empleado ID: {employee_id})")
        print_separator()
        
        # 1. Recuperar el valor actual
        employee = Employee.query.get(employee_id)
        old_value = employee.end_date
        print(f"Empleado: {employee.first_name} {employee.last_name}")
        print(f"end_date actual: '{old_value}', tipo: {type(old_value)}")
        
        try:
            # 2. Actualizar directamente con SQL
            print(f"\nActualizando mediante SQL a: '{test_date}'")
            
            # La sentencia SQL directa
            sql = f"UPDATE employees SET end_date = :date WHERE id = :id"
            db.session.execute(sqlalchemy.text(sql), 
                               {"date": test_date, "id": employee_id})
            db.session.commit()
            print("Ejecución SQL y commit realizados correctamente")
            
            # 3. Verificar que los cambios se han guardado
            employee = Employee.query.get(employee_id)  # Recargar
            print(f"Valor en objeto después de actualización: '{employee.end_date}', tipo: {type(employee.end_date)}")
            
            # Comprobar mediante consulta directa
            result = db.session.execute(sqlalchemy.text(
                f"SELECT end_date FROM employees WHERE id = {employee_id}"
            )).fetchone()
            
            db_value = result[0] if result else None
            print(f"Valor en base de datos (SQL): '{db_value}', tipo: {type(db_value)}")
            
            if employee.end_date == test_date:
                print("\n✅ ÉXITO: El valor se actualizó correctamente mediante SQL")
            else:
                print(f"\n❌ ERROR: El valor no se actualizó correctamente mediante SQL. Se esperaba '{test_date}' pero se obtuvo '{employee.end_date}'")
                
        except Exception as e:
            print(f"\n❌ ERROR: Excepción al actualizar con SQL: {str(e)}")
            db.session.rollback()
            print("Rollback realizado")
        
        # 4. Restaurar el valor original si es necesario
        if old_value != employee.end_date and '--keep' not in sys.argv:
            try:
                print(f"\nRestaurando valor original: '{old_value}'")
                db.session.execute(sqlalchemy.text(
                    f"UPDATE employees SET end_date = :date WHERE id = :id"),
                    {"date": old_value, "id": employee_id}
                )
                db.session.commit()
                print("Valor restaurado correctamente")
            except Exception as e:
                print(f"ERROR al restaurar: {str(e)}")
                db.session.rollback()
        
        print_separator()

def inspect_employee_form_data():
    """
    Inspecciona la estructura de la tabla de empleados y los campos relacionados con fechas.
    """
    with app.app_context():
        print_separator()
        print("INSPECCIÓN DE LA ESTRUCTURA DE TABLA DE EMPLEADOS")
        print_separator()
        
        # 1. Revisar la metadata de la tabla
        print("Estructura de la tabla Employee:")
        for column in Employee.__table__.columns:
            if column.name in ['start_date', 'end_date', 'status_start_date', 'status_end_date']:
                print(f"  - {column.name}: {column.type} (Nullable: {column.nullable})")
        
        # 2. Consultar algunos empleados y sus datos de fechas
        employees = Employee.query.limit(5).all()
        print("\nDatos de fechas de algunos empleados:")
        for emp in employees:
            print(f"ID: {emp.id}, Nombre: {emp.first_name} {emp.last_name}")
            print(f"  - start_date: '{emp.start_date}', tipo: {type(emp.start_date)}")
            print(f"  - end_date: '{emp.end_date}', tipo: {type(emp.end_date)}")
            print(f"  - status_start_date: '{emp.status_start_date}', tipo: {type(emp.status_start_date)}")
            print(f"  - status_end_date: '{emp.status_end_date}', tipo: {type(emp.status_end_date)}")
        
        # 3. Verificar cómo se almacenan en la base de datos
        print("\nConsulta directa a la base de datos:")
        query = """
        SELECT id, first_name, last_name, start_date, end_date, status_start_date, status_end_date 
        FROM employees LIMIT 5
        """
        results = db.session.execute(sqlalchemy.text(query)).fetchall()
        for row in results:
            print(f"ID: {row[0]}, Nombre: {row[1]} {row[2]}")
            print(f"  - start_date (DB): '{row[3]}', tipo: {type(row[3])}")
            print(f"  - end_date (DB): '{row[4]}', tipo: {type(row[4])}")
            print(f"  - status_start_date (DB): '{row[5]}', tipo: {type(row[5])}")
            print(f"  - status_end_date (DB): '{row[6]}', tipo: {type(row[6])}")
        
        print_separator()

if __name__ == "__main__":
    print("SCRIPT DE DEPURACIÓN DE PERSISTENCIA DE FECHAS")
    print("=============================================")
    print(f"Fecha y hora de ejecución: {datetime.now()}")
    
    # Ejecutar las pruebas
    inspect_employee_form_data()
    test_date_field_update()
    test_sql_direct_update()
    
    print("\nFIN DEL SCRIPT DE DEPURACIÓN")
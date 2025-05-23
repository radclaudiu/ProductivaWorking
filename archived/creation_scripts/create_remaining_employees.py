"""
Script para crear los empleados ficticios restantes para la empresa 100RCS
"""
from datetime import datetime, date, timedelta
import random
from app import db, create_app
from models import Employee, ContractType, EmployeeStatus

# Listas de nombres y apellidos ficticios para generar empleados aleatorios
first_names = [
    "Roberto", "Nuria", "Sergio", "Beatriz", "Guillermo", "Rosa", "Salvador"
]

last_names = [
    "Caballero", "Castillo", "Flores", "Fuentes", "Cabrera", "Rico", "Campos",
    "Nieto", "Lozano", "Crespo", "Santiago", "Reyes", "Medina", "Aguilar" 
]

positions = [
    "Administrativo", "Técnico", "Comercial", "Operario", "Encargado", "Supervisor", 
    "Jefe de Equipo", "Auxiliar", "Conductor", "Mecánico", "Analista", "Consultor"
]

def generate_dni():
    """Genera un DNI ficticio con formato español válido (8 números + letra)"""
    number = random.randint(10000000, 99999999)
    # Letras válidas para DNI español
    letters = "TRWAGMYFPDXBNJZSQVHLCKE"
    letter = letters[number % 23]
    return f"{number}{letter}"

def generate_bank_account():
    """Genera un número de cuenta bancaria ficticio"""
    country = "ES"
    control = f"{random.randint(10, 99)}"
    entity = f"{random.randint(1000, 9999)}"
    office = f"{random.randint(1000, 9999)}"
    dc = f"{random.randint(10, 99)}"
    account = f"{random.randint(1000000000, 9999999999)}"
    return f"{country}{control} {entity} {office} {dc} {account}"

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
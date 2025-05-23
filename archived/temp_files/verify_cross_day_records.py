"""
Script para verificar los registros de fichaje que cruzan de un día a otro.
"""
from app import create_app, db
from models import Employee, Company
from models_checkpoints import CheckPointRecord
from datetime import datetime, timedelta

app = create_app()

with app.app_context():
    # Buscar la empresa DNLN
    company = Company.query.filter(Company.name.like('%DNLN%')).first()
    print(f'Empresa encontrada: {company.name if company else "No encontrada"}')
    
    if company:
        # Buscar empleados de esa empresa
        employees = Employee.query.filter_by(company_id=company.id).all()
        employee_ids = [e.id for e in employees]
        
        # Obtener todos los registros para estos empleados
        records = CheckPointRecord.query.filter(
            CheckPointRecord.employee_id.in_(employee_ids)
        ).order_by(CheckPointRecord.check_in_time.desc()).all()
        
        print(f'Total de registros encontrados: {len(records)}')
        
        # Contar cuántos registros cruzan de un día a otro
        cross_day_count = 0
        normal_count = 0
        pending_count = 0  # Registros sin hora de salida
        
        for record in records:
            # Verificar si el registro tiene hora de salida
            if record.check_out_time is None:
                pending_count += 1
                if pending_count <= 5:
                    print(f"\nRegistro {record.id} (pendiente) - Empleado: {record.employee.first_name} {record.employee.last_name}")
                    print(f"  Entrada: {record.check_in_time.strftime('%Y-%m-%d %H:%M')}")
                    print(f"  Salida: Pendiente")
                continue
            
            # Para registros con entrada y salida
            if record.check_in_time.date() != record.check_out_time.date():
                cross_day_count += 1
                # Mostrar detalles de los 5 primeros registros que cruzan días
                if cross_day_count <= 5:
                    print(f"\nRegistro {record.id} - Empleado: {record.employee.first_name} {record.employee.last_name}")
                    print(f"  Entrada: {record.check_in_time.strftime('%Y-%m-%d %H:%M')}")
                    print(f"  Salida: {record.check_out_time.strftime('%Y-%m-%d %H:%M')}")
                    
                    # Calcular duración
                    duration = record.duration()
                    print(f"  Duración: {duration:.2f} horas")
            else:
                normal_count += 1
                # Mostrar detalles de los 5 primeros registros normales
                if normal_count <= 5:
                    print(f"\nRegistro {record.id} (mismo día) - Empleado: {record.employee.first_name} {record.employee.last_name}")
                    print(f"  Entrada: {record.check_in_time.strftime('%Y-%m-%d %H:%M')}")
                    print(f"  Salida: {record.check_out_time.strftime('%Y-%m-%d %H:%M')}")
                    
                    # Calcular duración
                    duration = record.duration()
                    print(f"  Duración: {duration:.2f} horas")
        
        total_with_checkout = cross_day_count + normal_count
        print(f"\nEstadísticas:")
        print(f"  Registros pendientes (sin salida): {pending_count}")
        print(f"  Registros completados: {total_with_checkout}")
        if total_with_checkout > 0:
            print(f"    - Registros que cruzan días: {cross_day_count} ({(cross_day_count/total_with_checkout)*100:.1f}%)")
            print(f"    - Registros en el mismo día: {normal_count} ({(normal_count/total_with_checkout)*100:.1f}%)")
        print(f"  Total registros: {len(records)}")
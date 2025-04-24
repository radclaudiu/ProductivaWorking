"""
Script para migrar la base de datos creando las tablas necesarias para el seguimiento de horas
trabajadas y calcular/actualizar los acumulados para los datos existentes.
"""

from app import db, create_app
from models_checkpoints import CheckPointOriginalRecord
from models_work_hours import EmployeeWorkHours, CompanyWorkHours
from utils_work_hours import update_employee_work_hours
import sys

def create_work_hours_tables():
    """
    Crea las tablas necesarias para el seguimiento de horas trabajadas:
    - employee_work_hours: Acumulados por empleado (diario, semanal, mensual)
    - company_work_hours: Acumulados por empresa (semanal, mensual)
    """
    app = create_app()
    with app.app_context():
        try:
            # Verificar si las tablas ya existen
            table_exists_employee = db.engine.dialect.has_table(db.engine.connect(), 'employee_work_hours')
            table_exists_company = db.engine.dialect.has_table(db.engine.connect(), 'company_work_hours')
            
            if table_exists_employee and table_exists_company:
                print("✅ Las tablas para seguimiento de horas ya existen")
                return True
                
            # Importamos aquí los modelos para asegurarnos de que la app está inicializada
            from models_work_hours import EmployeeWorkHours, CompanyWorkHours
            
            # Crear las tablas definidas en el modelo
            db.create_all()
            
            print("✅ Tablas para seguimiento de horas creadas correctamente")
            return True
            
        except Exception as e:
            print(f"❌ Error al crear las tablas: {str(e)}")
            return False

def calculate_and_update_hours():
    """
    Calcula y actualiza las horas trabajadas para todos los registros existentes,
    utilizando los datos de los registros originales.
    """
    app = create_app()
    with app.app_context():
        try:
            # Obtener todos los registros originales con check-out (para poder calcular horas)
            records = CheckPointOriginalRecord.query.filter(
                CheckPointOriginalRecord.original_check_out_time.isnot(None)
            ).all()
            
            processed_count = 0
            error_count = 0
            
            print(f"ℹ️ Procesando {len(records)} registros existentes...")
            
            # Primero, limpiamos las tablas para evitar duplicados
            db.session.execute("TRUNCATE TABLE employee_work_hours CASCADE")
            db.session.execute("TRUNCATE TABLE company_work_hours CASCADE")
            db.session.commit()
            print("ℹ️ Tablas limpiadas para evitar duplicados")
            
            # Procesamos cada registro
            for record in records:
                try:
                    # Obtenemos el registro principal para obtener el employee_id
                    if not hasattr(record, 'record') or not record.record:
                        print(f"⚠️ Registro {record.id} sin referencia a record")
                        continue
                        
                    employee_id = record.record.employee_id
                    
                    # Si ya tiene hours_worked calculado, usamos ese valor
                    if hasattr(record, 'hours_worked') and record.hours_worked > 0:
                        hours = record.hours_worked
                    else:
                        # Si no, usamos el método duration() que ya existe
                        hours = record.duration()
                        
                        # Actualizamos el campo hours_worked
                        if hours is not None:
                            record.hours_worked = round(hours, 2)
                    
                    # Si hay horas válidas, actualizamos los acumulados
                    if hours and hours > 0:
                        # Usamos la hora de check-in para determinar la semana/mes
                        result = update_employee_work_hours(
                            employee_id,
                            record.original_check_in_time,
                            hours
                        )
                        
                        if result:
                            processed_count += 1
                        else:
                            error_count += 1
                            
                except Exception as e:
                    error_count += 1
                    print(f"⚠️ Error al procesar registro {record.id}: {str(e)}")
                    
                # Guardar cambios cada 100 registros para evitar consumir mucha memoria
                if processed_count % 100 == 0 and processed_count > 0:
                    db.session.commit()
                    print(f"ℹ️ Procesados {processed_count} registros...")
            
            # Guardar todos los cambios
            db.session.commit()
            
            print(f"✅ Completado: {processed_count} registros procesados, {error_count} errores")
            return True
            
        except Exception as e:
            db.session.rollback()
            print(f"❌ Error al calcular y actualizar horas: {str(e)}")
            return False

def run_migration():
    """
    Ejecuta la migración completa para habilitar el seguimiento de horas trabajadas.
    """
    print("=" * 80)
    print("INICIANDO MIGRACIÓN PARA SEGUIMIENTO DE HORAS TRABAJADAS")
    print("=" * 80)
    
    # 1. Crear las tablas necesarias
    print("\n1. Creando tablas para seguimiento de horas...")
    if not create_work_hours_tables():
        print("❌ No se pudieron crear las tablas. Abortando migración.")
        return False
    
    # 2. Calcular y actualizar las horas para registros existentes
    print("\n2. Calculando y actualizando horas para registros existentes...")
    if not calculate_and_update_hours():
        print("❌ No se pudieron calcular las horas. La migración está incompleta.")
        return False
    
    print("\n✅ MIGRACIÓN COMPLETADA CORRECTAMENTE")
    return True

if __name__ == "__main__":
    success = run_migration()
    sys.exit(0 if success else 1)
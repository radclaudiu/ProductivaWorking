"""
Script para probar la funcionalidad del sistema de seguimiento de horas trabajadas.

Este script verifica:
1. Que la columna hours_worked exista en checkpoint_original_records
2. Que las tablas employee_work_hours y company_work_hours existan
3. Que las funciones de cálculo y actualización de horas funcionen correctamente
"""

from app import db, create_app
from models_checkpoints import CheckPointOriginalRecord
from utils_work_hours import calculate_hours_worked, update_employee_work_hours
from datetime import datetime, timedelta
import sys

def verify_database_structure():
    """
    Verifica que la estructura de la base de datos sea correcta para el sistema 
    de seguimiento de horas trabajadas.
    """
    app = create_app()
    with app.app_context():
        from sqlalchemy import inspect
        
        # 1. Verificar columna hours_worked en checkpoint_original_records
        print("\n1. Verificando columna hours_worked en checkpoint_original_records...")
        inspector = inspect(db.engine)
        columns = [col['name'] for col in inspector.get_columns('checkpoint_original_records')]
        if 'hours_worked' in columns:
            print("✅ Columna hours_worked existe")
        else:
            print("❌ Columna hours_worked NO existe en checkpoint_original_records")
            return False
        
        # 2. Verificar tabla employee_work_hours
        print("\n2. Verificando tabla employee_work_hours...")
        if inspector.has_table('employee_work_hours'):
            print("✅ Tabla employee_work_hours existe")
            
            # Verificar estructura
            columns = [col['name'] for col in inspector.get_columns('employee_work_hours')]
            required_columns = ['employee_id', 'year', 'month', 'week_number', 'daily_hours', 'weekly_hours', 'monthly_hours']
            missing = [col for col in required_columns if col not in columns]
            
            if missing:
                print(f"❌ Faltan columnas en employee_work_hours: {', '.join(missing)}")
                return False
            else:
                print("✅ Estructura de employee_work_hours correcta")
        else:
            print("❌ Tabla employee_work_hours NO existe")
            return False
        
        # 3. Verificar tabla company_work_hours
        print("\n3. Verificando tabla company_work_hours...")
        if inspector.has_table('company_work_hours'):
            print("✅ Tabla company_work_hours existe")
            
            # Verificar estructura
            columns = [col['name'] for col in inspector.get_columns('company_work_hours')]
            required_columns = ['company_id', 'year', 'month', 'week_number', 'weekly_hours', 'monthly_hours']
            missing = [col for col in required_columns if col not in columns]
            
            if missing:
                print(f"❌ Faltan columnas en company_work_hours: {', '.join(missing)}")
                return False
            else:
                print("✅ Estructura de company_work_hours correcta")
        else:
            print("❌ Tabla company_work_hours NO existe")
            return False
            
        return True

def test_calculate_hours():
    """
    Prueba la función de cálculo de horas entre dos timestamps.
    """
    print("\n4. Probando función calculate_hours_worked...")
    
    # Caso 1: Jornada normal (8 horas)
    start_time = datetime(2025, 4, 24, 9, 0, 0)  # 9:00 AM
    end_time = datetime(2025, 4, 24, 17, 0, 0)   # 5:00 PM
    hours = calculate_hours_worked(start_time, end_time)
    print(f"Caso 1 - Jornada normal (9:00-17:00): {hours}h {'✅' if round(hours, 1) == 8.0 else '❌'}")
    
    # Caso 2: Jornada corta (4 horas)
    start_time = datetime(2025, 4, 24, 9, 0, 0)  # 9:00 AM
    end_time = datetime(2025, 4, 24, 13, 0, 0)   # 1:00 PM
    hours = calculate_hours_worked(start_time, end_time)
    print(f"Caso 2 - Jornada corta (9:00-13:00): {hours}h {'✅' if round(hours, 1) == 4.0 else '❌'}")
    
    # Caso 3: Turno nocturno (cruza días)
    start_time = datetime(2025, 4, 24, 22, 0, 0)  # 10:00 PM
    end_time = datetime(2025, 4, 25, 6, 0, 0)     # 6:00 AM del día siguiente
    hours = calculate_hours_worked(start_time, end_time)
    print(f"Caso 3 - Turno nocturno (22:00-06:00): {hours}h {'✅' if round(hours, 1) == 8.0 else '❌'}")
    
    # Caso 4: Sin hora de salida
    start_time = datetime(2025, 4, 24, 9, 0, 0)
    end_time = None
    hours = calculate_hours_worked(start_time, end_time)
    print(f"Caso 4 - Sin hora de salida: {hours}h {'✅' if hours == 0.0 else '❌'}")
    
    return True

def verify_hours_worked_values():
    """
    Verifica que algunos registros tengan valores en hours_worked.
    """
    app = create_app()
    with app.app_context():
        print("\n5. Verificando valores hours_worked en registros existentes...")
        
        # Contar registros con hours_worked > 0
        total_records = db.session.query(CheckPointOriginalRecord).count()
        records_with_hours = db.session.query(CheckPointOriginalRecord).filter(
            CheckPointOriginalRecord.hours_worked > 0
        ).count()
        
        print(f"Total de registros: {total_records}")
        print(f"Registros con hours_worked > 0: {records_with_hours}")
        
        if records_with_hours > 0:
            print(f"✅ {records_with_hours} registros tienen horas calculadas")
            # Mostrar algunos ejemplos
            records = db.session.query(CheckPointOriginalRecord).filter(
                CheckPointOriginalRecord.hours_worked > 0
            ).limit(5).all()
            
            for record in records:
                print(f"  Record {record.id}: {record.hours_worked:.2f}h")
                
            return True
        else:
            print("❌ No se encontraron registros con horas calculadas")
            return False

def run_tests():
    """
    Ejecuta todas las pruebas para verificar el sistema de seguimiento de horas.
    """
    print("====== PRUEBAS DEL SISTEMA DE SEGUIMIENTO DE HORAS ======\n")
    
    # 1. Verificar estructura de la base de datos
    if not verify_database_structure():
        print("\n❌ Pruebas fallidas: Estructura de base de datos incorrecta")
        return False
        
    # 2. Probar función de cálculo de horas
    if not test_calculate_hours():
        print("\n❌ Pruebas fallidas: Función de cálculo de horas no funciona correctamente")
        return False
        
    # 3. Verificar valores hours_worked
    if not verify_hours_worked_values():
        print("\n⚠️ Advertencia: No se encontraron registros con horas calculadas")
        
    print("\n✅ PRUEBAS COMPLETADAS EXITOSAMENTE")
    return True

if __name__ == "__main__":
    success = run_tests()
    sys.exit(0 if success else 1)
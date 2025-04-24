import os
import uuid
import io
import tempfile
import zipfile
import json
import re
import unicodedata
from datetime import datetime
from werkzeug.utils import secure_filename
from flask import current_app, flash, request, send_file
from flask_login import current_user
from fpdf import FPDF
import shutil

from app import db
from models import User, Employee, EmployeeHistory, UserRole, ActivityLog, EmployeeDocument, Company
from flask import abort
from flask_login import current_user

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

def allowed_file(filename):
    """Check if file extension is allowed."""
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in current_app.config['ALLOWED_EXTENSIONS']

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

def check_company_access(company_id):
    """
    Verifica si el usuario actual puede acceder a la empresa especificada.
    
    Si no tiene acceso, aborta la solicitud con un error 403.
    
    Args:
        company_id: ID de la empresa a verificar
        
    Returns:
        Company: Objeto de la empresa si tiene acceso
    """
    if not current_user.is_authenticated:
        abort(403, description="Debe iniciar sesión para acceder a esta página")
    
    # Convertir company_id a entero para asegurar comparación correcta
    try:
        company_id_int = int(company_id)
    except (ValueError, TypeError):
        abort(404, description="Empresa no encontrada")
    
    # Buscar la empresa
    company = Company.query.get(company_id_int)
    if not company:
        abort(404, description="Empresa no encontrada")
    
    # Verificar permisos
    if not can_manage_company(company_id_int):
        abort(403, description="No tiene permiso para acceder a esta empresa")
        
    return company

def can_manage_company(company_id):
    """Check if current user can manage the company."""
    if not current_user.is_authenticated:
        return False
    
    if current_user.is_admin():
        return True
        
    if current_user.is_gerente():
        # Verificar si el usuario tiene esta empresa asignada
        from flask import current_app
        
        # Convertir company_id a entero para asegurar comparación correcta
        try:
            company_id_int = int(company_id)
        except (ValueError, TypeError):
            current_app.logger.error(f"ERROR: company_id no es un entero válido: {company_id}")
            return False
            
        # Verificación de permisos para gerentes
        
        # Verificar en las empresas asignadas
        for company in current_user.companies:
            try:
                current_company_id = int(company.id)
                
                if current_company_id == company_id_int:
                    return True
            except (ValueError, TypeError):
                current_app.logger.error(f"ERROR: ID de empresa asignada no válido: {company.id}")
                continue
        
        # Permiso denegado para esta empresa
    
    return False

def can_manage_employee(employee):
    """Check if current user can manage the employee."""
    if not current_user.is_authenticated:
        return False
    
    if current_user.is_admin():
        return True
        
    if current_user.is_gerente():
        # Verificar si el usuario tiene la empresa del empleado asignada
        from flask import current_app
        
        # Convertir company_id del empleado a entero para asegurar comparación correcta
        try:
            employee_company_id = int(employee.company_id)
        except (ValueError, TypeError):
            current_app.logger.error(f"ERROR: employee.company_id no es un entero válido: {employee.company_id}")
            return False
            
        # Verificación de permisos para gerentes en la gestión de empleados
        
        # Verificar en las empresas asignadas
        for company in current_user.companies:
            try:
                current_company_id = int(company.id)
                if current_company_id == employee_company_id:
                    return True
            except (ValueError, TypeError):
                current_app.logger.error(f"ERROR: ID de empresa asignada no válido: {company.id}")
                continue
        
        # Permiso denegado para el empleado de esta empresa
    
    if current_user.is_empleado() and current_user.id == employee.user_id:
        return True
        
    return False

def can_view_employee(employee):
    """Check if current user can view the employee."""
    if not current_user.is_authenticated:
        return False
    
    if current_user.is_admin():
        return True
        
    if current_user.is_gerente():
        # Verificar si el usuario tiene la empresa del empleado asignada
        from flask import current_app
        
        # Convertir company_id del empleado a entero para asegurar comparación correcta
        try:
            employee_company_id = int(employee.company_id)
        except (ValueError, TypeError):
            current_app.logger.error(f"ERROR: employee.company_id no es un entero válido: {employee.company_id}")
            return False
            
        # Verificar en las empresas asignadas
        for company in current_user.companies:
            try:
                current_company_id = int(company.id)
                if current_company_id == employee_company_id:
                    return True
            except (ValueError, TypeError):
                current_app.logger.error(f"ERROR: ID de empresa asignada no válido: {company.id}")
                continue
        
    if current_user.is_empleado() and current_user.id == employee.user_id:
        return True
        
    return False

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
def create_database_backup():
    """
    Create a simplified backup of the database by directly querying tables
    without using SQLAlchemy introspection
    """
    import os
    from datetime import datetime
    import tempfile
    import csv
    import zipfile
    from io import BytesIO
    import traceback
    from sqlalchemy import text
    
    # Generate timestamp for filenames
    timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
    
    try:
        # Create a temporary directory for our files
        with tempfile.TemporaryDirectory() as temp_dir:
            # Create a ZIP file in memory
            zip_buffer = BytesIO()
            with zipfile.ZipFile(zip_buffer, 'a', zipfile.ZIP_DEFLATED, False) as zip_file:
                # Get table information from our models
                main_tables = [
                    ('users', 'Tabla de usuarios'),
                    ('companies', 'Tabla de empresas'),
                    ('employees', 'Tabla de empleados'),
                    ('employee_documents', 'Documentos de empleados'),
                    ('employee_notes', 'Notas de empleados'),
                    ('employee_schedules', 'Horarios de empleados'),
                    ('employee_check_ins', 'Fichajes de empleados'),
                    ('employee_vacations', 'Vacaciones de empleados'),
                    ('activity_logs', 'Registros de actividad'),
                    ('checkpoints', 'Puntos de fichaje')
                ]
                
                # Create INFO file with backup details
                info_content = f"""# Database Backup
Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

## Tables Included
"""
                # Add table details to info
                for table_name, description in main_tables:
                    info_content += f"- {table_name}: {description}\n"
                
                info_file = os.path.join(temp_dir, 'INFO.md')
                with open(info_file, 'w') as f:
                    f.write(info_content)
                
                zip_file.write(info_file, arcname='INFO.md')
                
                # Export data for each table
                for table_name, _ in main_tables:
                    try:
                        # Execute direct SQL query to get column names
                        result = db.session.execute(text(f"SELECT column_name FROM information_schema.columns WHERE table_name = '{table_name}' ORDER BY ordinal_position")).fetchall()
                        
                        if not result:
                            continue  # Skip table if it doesn't exist
                            
                        columns = [row[0] for row in result]
                        
                        # Create CSV file for this table
                        csv_file_path = os.path.join(temp_dir, f"{table_name}.csv")
                        
                        with open(csv_file_path, 'w', newline='') as csv_file:
                            # Create CSV writer and write header
                            csv_writer = csv.writer(csv_file)
                            csv_writer.writerow(columns)
                            
                            # Execute direct SQL query to get all data from table
                            data_result = db.session.execute(text(f"SELECT * FROM {table_name}")).fetchall()
                            
                            # Write data rows
                            for row in data_result:
                                csv_writer.writerow([str(cell) if cell is not None else '' for cell in row])
                        
                        # Add CSV file to ZIP
                        zip_file.write(csv_file_path, arcname=f"{table_name}.csv")
                    except Exception as table_error:
                        # Create error log for this table
                        error_file = os.path.join(temp_dir, f"{table_name}_error.log")
                        with open(error_file, 'w') as f:
                            f.write(f"Error exporting table {table_name}: {str(table_error)}\n\n")
                            f.write(traceback.format_exc())
                        
                        # Add error log to ZIP
                        zip_file.write(error_file, arcname=f"errors/{table_name}_error.log")
            
            # Reset buffer position to beginning
            zip_buffer.seek(0)
            
            return {
                'success': True,
                'file': zip_buffer,
                'timestamp': timestamp
            }
        
    except Exception as e:
        import traceback
        return {
            'success': False,
            'error': str(e),
            'traceback': traceback.format_exc()
        }

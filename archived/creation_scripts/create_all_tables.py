from app import app, db

def create_all_tables():
    """
    Creates all tables needed by the application.
    """
    with app.app_context():
        # Import all models to ensure they're registered with SQLAlchemy
        from models import User, Company, Employee, ActivityLog, EmployeeDocument, EmployeeNote, EmployeeHistory, EmployeeSchedule, EmployeeCheckIn, EmployeeVacation
        from models_tasks import Location, LocalUser, Task, TaskSchedule, TaskCompletion, TaskPriority, TaskFrequency, TaskStatus, WeekDay, TaskGroup, Product, ProductConservation, ProductLabel, ConservationType, TaskInstance, TaskWeekday
        from models_checkpoints import CheckPoint, CheckPointRecord, CheckPointIncident, EmployeeContractHours, CheckPointStatus, CheckPointIncidentType
        
        # Create all tables
        db.create_all()
        
        print("✅ All tables created successfully!")

if __name__ == "__main__":
    create_all_tables()
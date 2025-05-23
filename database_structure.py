from app import app, db

def create_all_tables():
    """
    Creates all tables needed by the application.
    """
    with app.app_context():
        # Import all models to ensure they're registered with SQLAlchemy
        
        # Core models (User, Company, Employee, etc.)
        from models import (
            User, Company, Employee, ActivityLog, EmployeeDocument, 
            EmployeeNote, EmployeeHistory, EmployeeSchedule, 
            EmployeeCheckIn, EmployeeVacation
        )
        
        # Task-related models
        from models_tasks import (
            Location, LocalUser, Task, TaskSchedule, TaskCompletion, 
            TaskPriority, TaskFrequency, TaskStatus, WeekDay, 
            TaskGroup, Product, ProductConservation, ProductLabel, 
            ConservationType, TaskInstance, TaskWeekday, TaskMonthDay,
            NetworkPrinter
        )
        
        # Checkpoint-related models
        from models_checkpoints import (
            CheckPoint, CheckPointRecord, CheckPointIncident, 
            EmployeeContractHours, CheckPointStatus, 
            CheckPointIncidentType, CheckPointOriginalRecord
        )

        # Create all tables
        db.create_all()
        
        print("✅ All tables created successfully!")

if __name__ == "__main__":
    create_all_tables()

# Database Structure Overview
"""
This file documents the structure of the database used in the application.

# Core Models
- User: Application users with authentication and roles
- Company: Companies/organizations in the system
- Employee: Employee information and status
- EmployeeDocument: Documents associated with employees
- EmployeeNote: Notes about employees
- EmployeeHistory: History of changes to employee records
- EmployeeSchedule: Employee work schedules
- EmployeeCheckIn: Employee attendance records
- EmployeeVacation: Employee vacation records
- ActivityLog: System activity logs

# Task-related Models
- Location: Physical locations for tasks
- LocalUser: User specific to a location
- Task: Task definitions
- TaskSchedule: When tasks should be performed
- TaskCompletion: Records of completed tasks
- TaskPriority: Priority levels for tasks
- TaskFrequency: How often tasks should be performed
- TaskStatus: Current status of tasks
- WeekDay: Days of the week for task scheduling
- TaskGroup: Group of related tasks
- Product: Products in inventory
- ProductConservation: Conservation records for products
- ProductLabel: Labels for products
- ConservationType: Types of product conservation
- TaskInstance: Instances of tasks to be performed
- TaskWeekday: Days when tasks should be performed
- TaskMonthDay: Monthly schedule for tasks
- NetworkPrinter: Printers available on the network

# Checkpoint-related Models
- CheckPoint: Physical locations for employee check-in/out
- CheckPointRecord: Records of employee check-ins/outs
- CheckPointIncident: Incidents at checkpoints
- EmployeeContractHours: Contract hours for employees
- CheckPointStatus: Status options for checkpoints
- CheckPointIncidentType: Types of checkpoint incidents
- CheckPointOriginalRecord: Original unmodified check-in/out records
"""

# Database Relationship Map
"""
Relationships:

# User relationships:
- User has one Employee (1:1)
- User belongs to many Companies (N:M)
- User has many ActivityLogs (1:N)

# Company relationships:
- Company has many Employees (1:N)
- Company has many Users (N:M)

# Employee relationships:
- Employee belongs to one Company (N:1)
- Employee belongs to one User (N:1)
- Employee has many Documents (1:N)
- Employee has many Notes (1:N)
- Employee has many History entries (1:N)
- Employee has many Schedules (1:N)
- Employee has many CheckIns (1:N)
- Employee has many Vacations (1:N)

# Location relationships:
- Location has many LocalUsers (1:N)
- Location has many Tasks (1:N)
- Location has many CheckPoints (1:N)

# Task relationships:
- Task belongs to one Location (N:1)
- Task belongs to one TaskGroup (N:1)
- Task has many TaskSchedules (1:N)
- Task has many TaskWeekdays (1:N)
- Task has many TaskCompletions (1:N)
- Task has many TaskInstances (1:N)

# CheckPoint relationships:
- CheckPoint belongs to one Location (N:1)
- CheckPoint has many CheckPointRecords (1:N)
- CheckPoint has many CheckPointIncidents (1:N)

# Product relationships:
- Product has many ProductConservations (1:N)
"""

from app import create_app, db
from models import User, UserRole

def create_admin_user():
    app = create_app()
    with app.app_context():
        # Check if admin already exists
        admin = User.query.filter_by(username='admin').first()
        if admin:
            print('Admin user already exists')
            return
            
        # Create new admin user
        admin = User(
            username='admin',
            email='admin@example.com',
            role=UserRole.ADMIN,
            first_name='Admin',
            last_name='User',
            is_active=True
        )
        admin.set_password('admin')
        
        db.session.add(admin)
        db.session.commit()
        print('Admin user created successfully')

if __name__ == '__main__':
    create_admin_user()

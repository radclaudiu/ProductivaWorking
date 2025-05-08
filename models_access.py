"""
Modelos para la funcionalidad de acceso directo mediante tokens.
"""
from sqlalchemy import Column, Integer, String, Boolean, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
import secrets
import string

from app import db
from models_checkpoints import CheckPoint  # Importamos la clase de ubicaciones

class LocationAccessToken(db.Model):
    """Modelo para almacenar tokens de acceso directo para ubicaciones."""
    __tablename__ = 'location_access_tokens'
    
    id = Column(Integer, primary_key=True)
    location_id = Column(Integer, ForeignKey('checkpoints.id', ondelete='CASCADE'), nullable=False, unique=True)
    token = Column(String(128), nullable=False, unique=True)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=func.now())
    last_used_at = Column(DateTime, nullable=True)
    
    # Relación con la ubicación
    location = relationship("CheckPoint", backref="access_token", uselist=False)
    
    @staticmethod
    def generate_token(length=32):
        """
        Genera un token aleatorio seguro.
        
        Args:
            length (int): Longitud del token a generar.
            
        Returns:
            str: Token generado.
        """
        alphabet = string.ascii_letters + string.digits
        return ''.join(secrets.choice(alphabet) for _ in range(length))
    
    @staticmethod
    def create_for_location(location_id):
        """
        Crea o actualiza un token para una ubicación.
        
        Args:
            location_id (int): ID de la ubicación.
            
        Returns:
            LocationAccessToken: Objeto del token creado/actualizado.
        """
        # Verificar si ya existe un token para esa ubicación
        existing_token = LocationAccessToken.query.filter_by(location_id=location_id).first()
        
        if existing_token:
            # Actualizar el token existente
            existing_token.token = LocationAccessToken.generate_token()
            existing_token.is_active = True
            db.session.commit()
            return existing_token
        else:
            # Crear un nuevo token
            new_token = LocationAccessToken(
                location_id=location_id,
                token=LocationAccessToken.generate_token(),
                is_active=True
            )
            db.session.add(new_token)
            db.session.commit()
            return new_token
    
    def deactivate(self):
        """
        Desactiva este token de acceso.
        
        Returns:
            bool: True si se desactivó correctamente.
        """
        self.is_active = False
        db.session.commit()
        return True
    
    def reactivate(self):
        """
        Reactiva este token de acceso.
        
        Returns:
            bool: True si se reactivó correctamente.
        """
        self.is_active = True
        db.session.commit()
        return True
    
    def regenerate(self):
        """
        Regenera el token manteniendo el mismo registro.
        
        Returns:
            str: Nuevo token generado.
        """
        self.token = self.generate_token()
        db.session.commit()
        return self.token
    
    def update_last_used(self):
        """
        Actualiza la fecha de último uso.
        
        Returns:
            bool: True si se actualizó correctamente.
        """
        self.last_used_at = func.now()
        db.session.commit()
        return True
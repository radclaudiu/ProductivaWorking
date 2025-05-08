"""
Modelos para la funcionalidad de acceso directo mediante tokens.
"""
import os
import secrets
from datetime import datetime
from enum import Enum

from sqlalchemy import Column, Integer, String, Boolean, DateTime, ForeignKey, Enum as SQLEnum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func

from app import db

class PortalType(Enum):
    """Tipos de portales para los que se pueden generar tokens"""
    TASKS = "tasks"           # Portal de tareas
    CHECKPOINTS = "fichajes"  # Portal de fichajes/checkpoints

class LocationAccessToken(db.Model):
    """Modelo para almacenar tokens de acceso directo para ubicaciones."""
    __tablename__ = 'location_access_tokens'

    id = Column(Integer, primary_key=True)
    location_id = Column(Integer, ForeignKey('checkpoints.id', ondelete='CASCADE'), nullable=False)
    token = Column(String(128), nullable=False, unique=True)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=func.now())
    last_used_at = Column(DateTime, nullable=True)
    portal_type = Column(SQLEnum(PortalType), nullable=False, default=PortalType.TASKS)

    # Relación con el modelo Location (CheckPoint)
    location = relationship("CheckPoint", backref="access_tokens")

    @staticmethod
    def generate_token(length=32):
        """
        Genera un token aleatorio seguro.
        
        Args:
            length (int): Longitud del token a generar.
            
        Returns:
            str: Token generado.
        """
        return secrets.token_urlsafe(length)

    @staticmethod
    def create_for_location(location_id, portal_type=PortalType.TASKS):
        """
        Crea o actualiza un token para una ubicación y tipo de portal específico.
        
        Args:
            location_id (int): ID de la ubicación.
            portal_type (PortalType): Tipo de portal (TASKS o CHECKPOINTS).
            
        Returns:
            LocationAccessToken: Objeto del token creado/actualizado.
        """
        # Buscar si ya existe un token para esta ubicación y tipo de portal
        existing_token = LocationAccessToken.query.filter_by(
            location_id=location_id, 
            portal_type=portal_type
        ).first()
        
        if existing_token:
            # Si existe pero está desactivado, reactivarlo y generar nuevo token
            if not existing_token.is_active:
                existing_token.is_active = True
                existing_token.token = LocationAccessToken.generate_token()
                existing_token.created_at = func.now()
                existing_token.last_used_at = None
                db.session.commit()
            return existing_token
        
        # Crear nuevo token
        new_token = LocationAccessToken(
            location_id=location_id,
            token=LocationAccessToken.generate_token(),
            is_active=True,
            portal_type=portal_type
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
        self.created_at = func.now()
        self.last_used_at = None
        db.session.commit()
        return self.token

    def update_last_used(self):
        """
        Actualiza la fecha de último uso.
        
        Returns:
            bool: True si se actualizó correctamente.
        """
        self.last_used_at = datetime.now()
        db.session.commit()
        return True
        
    @staticmethod
    def get_token_by_value(token_value):
        """
        Obtiene un token de acceso por su valor.
        
        Args:
            token_value (str): Valor del token a buscar.
            
        Returns:
            LocationAccessToken: Objeto del token encontrado o None si no existe.
        """
        return LocationAccessToken.query.filter_by(token=token_value, is_active=True).first()
        
    @staticmethod
    def get_active_token_for_location(location_id, portal_type=PortalType.TASKS):
        """
        Obtiene el token activo para una ubicación y tipo de portal específico.
        
        Args:
            location_id (int): ID de la ubicación.
            portal_type (PortalType): Tipo de portal (TASKS o CHECKPOINTS).
            
        Returns:
            LocationAccessToken: Objeto del token activo o None si no existe.
        """
        return LocationAccessToken.query.filter_by(
            location_id=location_id,
            portal_type=portal_type,
            is_active=True
        ).first()
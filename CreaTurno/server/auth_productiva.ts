/**
 * Adaptador de autenticación para integrar CreaTurno con Productiva
 * 
 * Este archivo adapta el sistema de autenticación de CreaTurno para usar
 * la autenticación de Productiva en lugar de su propio sistema.
 */

import { Request, Response, NextFunction } from "express";
import jwt from "jsonwebtoken";
import { db } from "./storage_productiva";

// Interfaces
interface User {
  id: number;
  username: string;
  fullName: string;
  email: string;
  role: string;
  companies: number[];
}

interface AuthRequest extends Request {
  user?: User;
}

// Función para verificar si un usuario está autenticado
export async function isAuthenticated(req: AuthRequest, res: Response, next: NextFunction) {
  // En este punto, el usuario ya debe estar autenticado en Productiva
  // Lo que hacemos es verificar si los datos de usuario existen en el req.session
  // que habría sido configurado por la aplicación principal de Flask
  
  try {
    // El frontend envía los datos de usuario en un header X-User-Info
    const userInfoHeader = req.headers["x-user-info"];
    
    if (!userInfoHeader) {
      // Si no hay header, intentamos obtener la información de una cookie
      if (!req.cookies || !req.cookies.session) {
        return res.status(401).json({ error: "No autorizado" });
      }
      
      // Intentar decodificar la cookie de sesión de Flask
      // Esto dependerá de cómo esté configurada la cookie en Flask
      try {
        // Esta implementación es un placeholder y deberá adaptarse 
        // según cómo funciona realmente la sesión en Productiva
        const sessionData = JSON.parse(Buffer.from(req.cookies.session, "base64").toString("utf-8"));
        if (!sessionData.user_id) {
          return res.status(401).json({ error: "No autorizado" });
        }
        
        // Buscar usuario en la base de datos
        const user = await db.query.users.findFirst({
          where: (users, { eq }) => eq(users.id, sessionData.user_id),
        });
        
        if (!user) {
          return res.status(401).json({ error: "Usuario no encontrado" });
        }
        
        // Asignar usuario a la request
        req.user = {
          id: user.id,
          username: user.username,
          fullName: `${user.first_name} ${user.last_name}`,
          email: user.email,
          role: user.role || "user",
          companies: [], // Esto se debería llenar con las compañías del usuario
        };
        
        next();
      } catch (error) {
        console.error("Error al decodificar la cookie de sesión:", error);
        return res.status(401).json({ error: "Sesión inválida" });
      }
    } else {
      // Si hay header, parseamos la información de usuario
      try {
        const userInfo = JSON.parse(Buffer.from(userInfoHeader as string, "base64").toString("utf-8"));
        
        req.user = {
          id: userInfo.id,
          username: userInfo.username,
          fullName: userInfo.full_name,
          email: userInfo.email,
          role: userInfo.is_admin ? "admin" : "user",
          companies: userInfo.companies || [],
        };
        
        next();
      } catch (error) {
        console.error("Error al parsear la información de usuario:", error);
        return res.status(401).json({ error: "Información de usuario inválida" });
      }
    }
  } catch (error) {
    console.error("Error en middleware de autenticación:", error);
    return res.status(500).json({ error: "Error interno del servidor" });
  }
}

// Middleware para verificar si un usuario es administrador
export function isAdmin(req: AuthRequest, res: Response, next: NextFunction) {
  if (!req.user) {
    return res.status(401).json({ error: "No autorizado" });
  }
  
  if (req.user.role !== "admin") {
    return res.status(403).json({ error: "Acceso denegado - Se requiere rol de administrador" });
  }
  
  next();
}

// Middleware para verificar si un usuario tiene acceso a una empresa
export function hasCompanyAccess(req: AuthRequest, res: Response, next: NextFunction) {
  if (!req.user) {
    return res.status(401).json({ error: "No autorizado" });
  }
  
  const companyId = parseInt(req.params.companyId || req.body.companyId);
  
  if (!companyId) {
    return res.status(400).json({ error: "ID de empresa no proporcionado" });
  }
  
  // Los administradores tienen acceso a todas las empresas
  if (req.user.role === "admin") {
    return next();
  }
  
  // Verificar si el usuario tiene acceso a la empresa
  if (!req.user.companies.includes(companyId)) {
    return res.status(403).json({ error: "Acceso denegado - No tiene acceso a esta empresa" });
  }
  
  next();
}

// Función para obtener el usuario actual
export async function getCurrentUser(req: AuthRequest) {
  if (!req.user) {
    return null;
  }
  
  // Enriquecer la información del usuario con datos adicionales si es necesario
  const user = req.user;
  
  // Obtener lista de empresas del usuario si no se han cargado
  if (user.companies.length === 0) {
    try {
      const userCompanies = await db.query.userCompanies.findMany({
        where: (userCompanies, { eq }) => eq(userCompanies.user_id, user.id),
      });
      
      user.companies = userCompanies.map(uc => uc.company_id);
    } catch (error) {
      console.error("Error al obtener las empresas del usuario:", error);
    }
  }
  
  return user;
}
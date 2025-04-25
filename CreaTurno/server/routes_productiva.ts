import type { Express, Request, Response, NextFunction } from "express";
import { createServer, type Server } from "http";
import { ProductivaAdapter } from "./storage_productiva.js";
import { z } from "zod";
import { insertEmployeeSchema, insertShiftSchema, insertScheduleSchema, insertCompanySchema, insertScheduleTemplateSchema } from "@shared/schema.js";
import { setupAuth, isAuthenticated, isAdmin } from "./auth_productiva.js";

// Crear el adaptador para la base de datos de Productiva
const storage = new ProductivaAdapter();

export async function registerRoutes(app: Express): Promise<Server> {
  // Configurar autenticación simplificada para trabajar con el sistema de Flask
  setupAuth(app);
  
  // Rutas para empresas - Obtener la lista de empresas disponibles
  app.get("/api/companies", isAuthenticated, async (req, res) => {
    try {
      // Si tiene la cookie de admin, devolver todas las empresas
      // Si no, devolver solo las empresas a las que pertenece el usuario
      const isAdmin = req.headers.cookie && req.headers.cookie.includes('admin_session=');
      
      // Este userId debería extraerse de la sesión de Flask en una implementación real
      const userId = 1; // Usuario mock para pruebas
      
      const companies = await storage.getCompanies(isAdmin ? undefined : userId);
      res.json(companies);
    } catch (error) {
      console.error("Error al obtener empresas:", error);
      res.status(500).json({ 
        message: "Error al obtener empresas", 
        error: error instanceof Error ? error.message : String(error) 
      });
    }
  });
  
  // Rutas para empleados
  app.get("/api/employees", isAuthenticated, async (req, res) => {
    try {
      let companyId: number | undefined = undefined;
      
      if (req.query.companyId) {
        companyId = parseInt(req.query.companyId as string);
      }
      
      const employees = await storage.getEmployees(companyId);
      res.json(employees);
    } catch (error) {
      console.error("Error al obtener empleados:", error);
      res.status(500).json({ message: "Error al obtener empleados" });
    }
  });

  // Rutas para turnos
  app.get("/api/shifts", isAuthenticated, async (req, res) => {
    try {
      const { date, employeeId, companyId } = req.query;
      
      const shifts = await storage.getShifts(
        date as string | undefined,
        employeeId ? parseInt(employeeId as string) : undefined,
        companyId ? parseInt(companyId as string) : undefined
      );
      
      res.json(shifts);
    } catch (error) {
      console.error("Error al obtener turnos:", error);
      res.status(500).json({ message: "Error al obtener turnos" });
    }
  });
  
  app.post("/api/shifts", isAuthenticated, async (req, res) => {
    try {
      const validatedData = insertShiftSchema.parse(req.body);
      const shift = await storage.createShift(validatedData);
      res.status(201).json(shift);
    } catch (error) {
      if (error instanceof z.ZodError) {
        res.status(400).json({ message: "Datos de turno inválidos", errors: error.errors });
      } else {
        console.error("Error al crear turno:", error);
        res.status(500).json({ message: "Error al crear turno" });
      }
    }
  });
  
  app.put("/api/shifts/:id", isAuthenticated, async (req, res) => {
    try {
      const id = parseInt(req.params.id);
      const validatedData = insertShiftSchema.partial().parse(req.body);
      const shift = await storage.updateShift(id, validatedData);
      
      if (!shift) {
        return res.status(404).json({ message: "Turno no encontrado" });
      }
      
      res.json(shift);
    } catch (error) {
      if (error instanceof z.ZodError) {
        res.status(400).json({ message: "Datos de turno inválidos", errors: error.errors });
      } else {
        console.error("Error al actualizar turno:", error);
        res.status(500).json({ message: "Error al actualizar turno" });
      }
    }
  });
  
  app.delete("/api/shifts/:id", isAuthenticated, async (req, res) => {
    try {
      const id = parseInt(req.params.id);
      const success = await storage.deleteShift(id);
      
      if (!success) {
        return res.status(404).json({ message: "Turno no encontrado" });
      }
      
      res.status(204).end();
    } catch (error) {
      console.error("Error al eliminar turno:", error);
      res.status(500).json({ message: "Error al eliminar turno" });
    }
  });
  
  // Rutas para horarios
  app.get("/api/schedules", isAuthenticated, async (req, res) => {
    try {
      const companyId = req.query.companyId 
        ? parseInt(req.query.companyId as string) 
        : undefined;
        
      const schedules = await storage.getSchedules(companyId);
      res.json(schedules);
    } catch (error) {
      console.error("Error al obtener horarios:", error);
      res.status(500).json({ message: "Error al obtener horarios" });
    }
  });
  
  app.get("/api/schedules/:id", isAuthenticated, async (req, res) => {
    try {
      const id = parseInt(req.params.id);
      const schedule = await storage.getSchedule(id);
      
      if (!schedule) {
        return res.status(404).json({ message: "Horario no encontrado" });
      }
      
      res.json(schedule);
    } catch (error) {
      console.error("Error al obtener horario:", error);
      res.status(500).json({ message: "Error al obtener horario" });
    }
  });
  
  app.post("/api/schedules", isAuthenticated, async (req, res) => {
    try {
      // Asignar una ID de usuario ficticia para pruebas
      // En una implementación real, obtendríamos esto de la sesión de Flask
      const userId = 1;
      
      const validatedData = insertScheduleSchema.parse({
        ...req.body,
        createdBy: userId
      });
      
      const schedule = await storage.createSchedule(validatedData);
      res.status(201).json(schedule);
    } catch (error) {
      if (error instanceof z.ZodError) {
        res.status(400).json({ message: "Datos de horario inválidos", errors: error.errors });
      } else {
        console.error("Error al crear horario:", error);
        res.status(500).json({ message: "Error al crear horario" });
      }
    }
  });
  
  app.delete("/api/schedules/:id", isAuthenticated, async (req, res) => {
    try {
      const id = parseInt(req.params.id);
      const success = await storage.deleteSchedule(id);
      
      if (!success) {
        return res.status(404).json({ message: "Horario no encontrado" });
      }
      
      res.status(204).end();
    } catch (error) {
      console.error("Error al eliminar horario:", error);
      res.status(500).json({ message: "Error al eliminar horario" });
    }
  });
  
  // Gestión de horarios - guardar/cargar datos completos
  app.post("/api/schedules/:id/save", isAuthenticated, async (req, res) => {
    try {
      const id = parseInt(req.params.id);
      const { employees, shifts } = req.body;
      
      const schedule = await storage.getSchedule(id);
      if (!schedule) {
        return res.status(404).json({ message: "Horario no encontrado" });
      }
      
      const success = await storage.saveScheduleData(id, employees, shifts);
      
      if (!success) {
        return res.status(500).json({ message: "Error al guardar los datos del horario" });
      }
      
      res.status(200).json({ message: "Datos del horario guardados correctamente" });
    } catch (error) {
      console.error("Error al guardar datos del horario:", error);
      res.status(500).json({ message: "Error al guardar datos del horario" });
    }
  });
  
  app.get("/api/schedules/:id/load", isAuthenticated, async (req, res) => {
    try {
      const id = parseInt(req.params.id);
      const scheduleData = await storage.loadScheduleData(id);
      
      if (!scheduleData) {
        return res.status(404).json({ message: "Horario no encontrado" });
      }
      
      res.json(scheduleData);
    } catch (error) {
      console.error("Error al cargar datos del horario:", error);
      res.status(500).json({ message: "Error al cargar datos del horario" });
    }
  });
  
  // Plantillas de horario
  app.get("/api/schedule-templates", isAuthenticated, async (req, res) => {
    try {
      // ID de usuario ficticia para pruebas
      const userId = 1;
      
      const templates = await storage.getScheduleTemplates(userId);
      res.json(templates);
    } catch (error) {
      console.error("Error al obtener plantillas:", error);
      res.status(500).json({ message: "Error al obtener plantillas" });
    }
  });
  
  app.post("/api/schedule-templates", isAuthenticated, async (req, res) => {
    try {
      // ID de usuario ficticia para pruebas
      const userId = 1;
      
      const validatedData = insertScheduleTemplateSchema.parse({
        ...req.body,
        createdBy: userId
      });
      
      const template = await storage.createScheduleTemplate(validatedData);
      res.status(201).json(template);
    } catch (error) {
      if (error instanceof z.ZodError) {
        res.status(400).json({ message: "Datos de plantilla inválidos", errors: error.errors });
      } else {
        console.error("Error al crear plantilla:", error);
        res.status(500).json({ message: "Error al crear plantilla" });
      }
    }
  });
  
  // Crear servidor HTTP
  const server = createServer(app);
  return server;
}
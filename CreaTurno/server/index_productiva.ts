/**
 * Punto de entrada para la integración de CreaTurno con Productiva
 * 
 * Este archivo configura un servidor Express que utiliza los adaptadores
 * para la autenticación y el almacenamiento de datos en Productiva.
 */

import express from "express";
import cors from "cors";
import cookieParser from "cookie-parser";
import path from "path";
import { fileURLToPath } from "url";
import { isAuthenticated, isAdmin, hasCompanyAccess, getCurrentUser } from "./auth_productiva.js";
import { db, employees, locations, companies, creaturnoShifts, creaturnoShiftTemplates, creaturnoShiftRoles, eq, and, gte, lte } from "./storage_productiva.js";

// Obtener la ruta del directorio actual en ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Crear aplicación Express
const app = express();
const PORT = process.env.PORT || 5001;

// Middleware
app.use(express.json());
app.use(cookieParser());
app.use(cors({
  origin: true,
  credentials: true,
}));

// Servir archivos estáticos desde la carpeta client
const clientPath = path.join(__dirname, '..', 'client');
app.use('/creaturno-client', express.static(clientPath));

// Ruta para verificar el estado del servidor
app.get("/api/health", (req, res) => {
  res.json({ status: "ok", timestamp: new Date().toISOString() });
});

// Ruta para obtener usuario actual
app.get("/api/auth/me", isAuthenticated, async (req: any, res) => {
  try {
    const user = await getCurrentUser(req);
    
    if (!user) {
      return res.status(401).json({ error: "No autenticado" });
    }
    
    res.json(user);
  } catch (error) {
    console.error("Error al obtener usuario actual:", error);
    res.status(500).json({ error: "Error interno del servidor" });
  }
});

// Rutas para empresas
app.get("/api/companies", isAuthenticated, async (req: any, res) => {
  try {
    const user = await getCurrentUser(req);
    
    if (!user) {
      return res.status(401).json({ error: "No autenticado" });
    }
    
    let userCompanies;
    
    if (user.role === "admin") {
      // Los administradores ven todas las empresas
      userCompanies = await db.query.companies.findMany({
        where: (companies, { eq }) => eq(companies.is_active, true),
      });
    } else {
      // Los usuarios normales solo ven sus empresas asignadas
      userCompanies = await db.query.companies.findMany({
        where: (companies, { eq, and, inArray }) => 
          and(
            eq(companies.is_active, true),
            inArray(companies.id, user.companies)
          ),
      });
    }
    
    res.json(userCompanies);
  } catch (error) {
    console.error("Error al obtener empresas:", error);
    res.status(500).json({ error: "Error interno del servidor" });
  }
});

// Rutas para ubicaciones
app.get("/api/companies/:companyId/locations", isAuthenticated, hasCompanyAccess, async (req, res) => {
  try {
    const { companyId } = req.params;
    
    const companyLocations = await db.query.locations.findMany({
      where: (locations, { eq, and }) => 
        and(
          eq(locations.company_id, parseInt(companyId)),
          eq(locations.is_active, true)
        ),
    });
    
    res.json(companyLocations);
  } catch (error) {
    console.error("Error al obtener ubicaciones:", error);
    res.status(500).json({ error: "Error interno del servidor" });
  }
});

// Rutas para empleados
app.get("/api/companies/:companyId/employees", isAuthenticated, hasCompanyAccess, async (req, res) => {
  try {
    const { companyId } = req.params;
    
    const companyEmployees = await db.query.employees.findMany({
      where: (employees, { eq, and }) => 
        and(
          eq(employees.company_id, parseInt(companyId)),
          eq(employees.is_active, true)
        ),
    });
    
    res.json(companyEmployees);
  } catch (error) {
    console.error("Error al obtener empleados:", error);
    res.status(500).json({ error: "Error interno del servidor" });
  }
});

// Rutas para turnos
app.get("/api/shifts", isAuthenticated, async (req: any, res) => {
  try {
    const { startDate, endDate, companyId, locationId } = req.query;
    
    if (!startDate || !endDate) {
      return res.status(400).json({ error: "Se requieren fechas de inicio y fin" });
    }
    
    // Verificar si el usuario tiene acceso a la empresa
    if (companyId) {
      const user = await getCurrentUser(req);
      if (user && user.role !== "admin" && !user.companies.includes(parseInt(companyId as string))) {
        return res.status(403).json({ error: "Acceso denegado a esta empresa" });
      }
    }
    
    // Construir la condición de búsqueda
    let whereConditions = and(
      gte(creaturnoShifts.start_time, new Date(startDate as string)),
      lte(creaturnoShifts.start_time, new Date(endDate as string))
    );
    
    // Si se especifica una ubicación, filtrar por ella
    if (locationId) {
      whereConditions = and(
        whereConditions,
        eq(creaturnoShifts.location_id, parseInt(locationId as string))
      );
    } else if (companyId) {
      // Si se especifica una empresa pero no una ubicación,
      // obtener todas las ubicaciones de la empresa y filtrar por ellas
      const locations = await db.query.locations.findMany({
        where: (locations, { eq }) => eq(locations.company_id, parseInt(companyId as string)),
        columns: { id: true },
      });
      
      const locationIds = locations.map(loc => loc.id);
      
      if (locationIds.length > 0) {
        whereConditions = and(
          whereConditions,
          inArray(creaturnoShifts.location_id, locationIds)
        );
      }
    }
    
    // Obtener los turnos
    const shifts = await db.query.creaturnoShifts.findMany({
      where: whereConditions,
      with: {
        employee: true,
        location: true,
      },
    });
    
    res.json(shifts);
  } catch (error) {
    console.error("Error al obtener turnos:", error);
    res.status(500).json({ error: "Error interno del servidor" });
  }
});

app.post("/api/shifts", isAuthenticated, async (req: any, res) => {
  try {
    const { employee_id, location_id, start_time, end_time, role, color, notes } = req.body;
    
    if (!employee_id || !location_id || !start_time || !end_time) {
      return res.status(400).json({ error: "Faltan campos requeridos" });
    }
    
    // Verificar acceso a la empresa
    const employee = await db.query.employees.findFirst({
      where: (employees, { eq }) => eq(employees.id, employee_id),
      columns: { company_id: true },
    });
    
    if (!employee) {
      return res.status(404).json({ error: "Empleado no encontrado" });
    }
    
    const user = await getCurrentUser(req);
    if (user && user.role !== "admin" && !user.companies.includes(employee.company_id)) {
      return res.status(403).json({ error: "Acceso denegado a esta empresa" });
    }
    
    // Crear el turno
    const shift = await db.insert(creaturnoShifts).values({
      employee_id,
      location_id,
      start_time: new Date(start_time),
      end_time: new Date(end_time),
      role,
      color,
      notes,
      created_at: new Date(),
      updated_at: new Date(),
    }).returning();
    
    res.status(201).json(shift[0]);
  } catch (error) {
    console.error("Error al crear turno:", error);
    res.status(500).json({ error: "Error interno del servidor" });
  }
});

app.put("/api/shifts/:shiftId", isAuthenticated, async (req: any, res) => {
  try {
    const { shiftId } = req.params;
    const { employee_id, location_id, start_time, end_time, role, color, notes } = req.body;
    
    if (!shiftId) {
      return res.status(400).json({ error: "ID de turno requerido" });
    }
    
    // Verificar que el turno existe
    const existingShift = await db.query.creaturnoShifts.findFirst({
      where: (shifts, { eq }) => eq(shifts.id, parseInt(shiftId)),
      with: {
        employee: {
          columns: {
            company_id: true,
          },
        },
      },
    });
    
    if (!existingShift) {
      return res.status(404).json({ error: "Turno no encontrado" });
    }
    
    // Verificar acceso a la empresa
    const user = await getCurrentUser(req);
    if (user && user.role !== "admin" && !user.companies.includes(existingShift.employee.company_id)) {
      return res.status(403).json({ error: "Acceso denegado a esta empresa" });
    }
    
    // Actualizar el turno
    const shift = await db.update(creaturnoShifts)
      .set({
        employee_id: employee_id || existingShift.employee_id,
        location_id: location_id || existingShift.location_id,
        start_time: start_time ? new Date(start_time) : existingShift.start_time,
        end_time: end_time ? new Date(end_time) : existingShift.end_time,
        role: role !== undefined ? role : existingShift.role,
        color: color !== undefined ? color : existingShift.color,
        notes: notes !== undefined ? notes : existingShift.notes,
        updated_at: new Date(),
      })
      .where(eq(creaturnoShifts.id, parseInt(shiftId)))
      .returning();
    
    res.json(shift[0]);
  } catch (error) {
    console.error("Error al actualizar turno:", error);
    res.status(500).json({ error: "Error interno del servidor" });
  }
});

app.delete("/api/shifts/:shiftId", isAuthenticated, async (req: any, res) => {
  try {
    const { shiftId } = req.params;
    
    if (!shiftId) {
      return res.status(400).json({ error: "ID de turno requerido" });
    }
    
    // Verificar que el turno existe
    const existingShift = await db.query.creaturnoShifts.findFirst({
      where: (shifts, { eq }) => eq(shifts.id, parseInt(shiftId)),
      with: {
        employee: {
          columns: {
            company_id: true,
          },
        },
      },
    });
    
    if (!existingShift) {
      return res.status(404).json({ error: "Turno no encontrado" });
    }
    
    // Verificar acceso a la empresa
    const user = await getCurrentUser(req);
    if (user && user.role !== "admin" && !user.companies.includes(existingShift.employee.company_id)) {
      return res.status(403).json({ error: "Acceso denegado a esta empresa" });
    }
    
    // Eliminar el turno
    await db.delete(creaturnoShifts)
      .where(eq(creaturnoShifts.id, parseInt(shiftId)));
    
    res.json({ success: true });
  } catch (error) {
    console.error("Error al eliminar turno:", error);
    res.status(500).json({ error: "Error interno del servidor" });
  }
});

// Iniciar el servidor
app.listen(PORT, () => {
  console.log(`Servidor CreaTurno ejecutándose en el puerto ${PORT}`);
});
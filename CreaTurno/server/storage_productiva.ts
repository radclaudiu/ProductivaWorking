import { eq, and } from "drizzle-orm";
import { 
  Employee, InsertEmployee,
  Shift, InsertShift,
  Schedule, InsertSchedule,
  User, InsertUser,
  Company, InsertCompany,
  UserCompany, InsertUserCompany,
  ScheduleTemplate, InsertScheduleTemplate
} from "@shared/schema";
import { IStorage } from "./storage";
import { pool } from "./db";

// Adaptador para la base de datos PostgreSQL de Productiva
export class ProductivaAdapter implements IStorage {
  // Realiza consultas directas a la base de datos de Productiva
  private async query(text: string, params: any[] = []): Promise<any[]> {
    const client = await pool.connect();
    try {
      const result = await client.query(text, params);
      return result.rows;
    } finally {
      client.release();
    }
  }

  // User operations - Convertir usuarios de Productiva al formato de CreaTurno
  async getUser(id: number): Promise<User | undefined> {
    const rows = await this.query(`
      SELECT id, username, email, role, first_name || ' ' || last_name as full_name
      FROM users
      WHERE id = $1
    `, [id]);
    
    if (rows.length === 0) return undefined;
    
    return {
      id: rows[0].id,
      username: rows[0].username,
      email: rows[0].email,
      password: "", // No exponemos la contraseña
      fullName: rows[0].full_name,
      role: this.mapUserRole(rows[0].role),
      createdAt: new Date()
    };
  }
  
  // Mapea los roles de usuario de Productiva a CreaTurno
  private mapUserRole(role: string): string {
    switch (role) {
      case "admin":
        return "admin";
      case "gerente":
        return "manager";
      default:
        return "member";
    }
  }
  
  async getUserByEmail(email: string): Promise<User | undefined> {
    const rows = await this.query(`
      SELECT id, username, email, role, first_name || ' ' || last_name as full_name
      FROM users
      WHERE email = $1
    `, [email.toLowerCase()]);
    
    if (rows.length === 0) return undefined;
    
    return {
      id: rows[0].id,
      username: rows[0].username,
      email: rows[0].email,
      password: "", // No exponemos la contraseña
      fullName: rows[0].full_name,
      role: this.mapUserRole(rows[0].role),
      createdAt: new Date()
    };
  }
  
  async getUserByUsername(username: string): Promise<User | undefined> {
    const rows = await this.query(`
      SELECT id, username, email, role, first_name || ' ' || last_name as full_name
      FROM users
      WHERE username = $1
    `, [username]);
    
    if (rows.length === 0) return undefined;
    
    return {
      id: rows[0].id,
      username: rows[0].username,
      email: rows[0].email,
      password: "", // No exponemos la contraseña
      fullName: rows[0].full_name,
      role: this.mapUserRole(rows[0].role),
      createdAt: new Date()
    };
  }
  
  // En el adaptador no creamos usuarios directamente, lo hace el sistema principal
  async createUser(user: InsertUser): Promise<User> {
    throw new Error("No se pueden crear usuarios directamente desde CreaTurno");
  }
  
  async updateUser(id: number, user: Partial<InsertUser>): Promise<User | undefined> {
    throw new Error("No se pueden actualizar usuarios directamente desde CreaTurno");
  }
  
  async deleteUser(id: number): Promise<boolean> {
    throw new Error("No se pueden eliminar usuarios directamente desde CreaTurno");
  }
  
  async getAllUsers(): Promise<User[]> {
    const rows = await this.query(`
      SELECT id, username, email, role, first_name || ' ' || last_name as full_name
      FROM users
      WHERE is_active = true
      ORDER BY username
    `);
    
    return rows.map(row => ({
      id: row.id,
      username: row.username,
      email: row.email,
      password: "", // No exponemos la contraseña
      fullName: row.full_name,
      role: this.mapUserRole(row.role),
      createdAt: new Date()
    }));
  }
  
  // Company operations - Convertir empresas de Productiva al formato de CreaTurno
  async getCompanies(userId?: number): Promise<Company[]> {
    let query = `
      SELECT c.id, c.name, c.address, c.city, c.postal_code, c.country, 
             c.phone, c.email, c.website, c.tax_id, c.created_at
      FROM companies c
      WHERE c.is_active = true
    `;
    
    const params = [];
    
    if (userId) {
      // Filtrar por empresas a las que pertenece el usuario
      query = `
        SELECT c.id, c.name, c.address, c.city, c.postal_code, c.country, 
               c.phone, c.email, c.website, c.tax_id, c.created_at
        FROM companies c
        JOIN user_companies uc ON c.id = uc.company_id
        WHERE c.is_active = true AND uc.user_id = $1
      `;
      params.push(userId);
    }
    
    query += " ORDER BY c.name";
    
    const rows = await this.query(query, params);
    
    return rows.map(row => ({
      id: row.id,
      name: row.name,
      description: "",
      address: row.address || "",
      phone: row.phone || "",
      email: row.email || "",
      website: row.website || "",
      taxId: row.tax_id || "",
      startHour: 9,
      endHour: 22,
      logoUrl: "",
      isActive: true,
      createdAt: row.created_at,
      updatedAt: row.created_at,
      createdBy: null
    }));
  }
  
  async getCompany(id: number): Promise<Company | undefined> {
    const rows = await this.query(`
      SELECT c.id, c.name, c.address, c.city, c.postal_code, c.country, 
             c.phone, c.email, c.website, c.tax_id, c.created_at
      FROM companies c
      WHERE c.id = $1 AND c.is_active = true
    `, [id]);
    
    if (rows.length === 0) return undefined;
    
    return {
      id: rows[0].id,
      name: rows[0].name,
      description: "",
      address: rows[0].address || "",
      phone: rows[0].phone || "",
      email: rows[0].email || "",
      website: rows[0].website || "",
      taxId: rows[0].tax_id || "",
      startHour: 9,
      endHour: 22,
      logoUrl: "",
      isActive: true,
      createdAt: rows[0].created_at,
      updatedAt: rows[0].created_at,
      createdBy: null
    };
  }
  
  // En el adaptador no creamos empresas directamente, lo hace el sistema principal
  async createCompany(company: InsertCompany): Promise<Company> {
    throw new Error("No se pueden crear empresas directamente desde CreaTurno");
  }
  
  async updateCompany(id: number, company: Partial<InsertCompany>): Promise<Company | undefined> {
    throw new Error("No se pueden actualizar empresas directamente desde CreaTurno");
  }
  
  async deleteCompany(id: number): Promise<boolean> {
    throw new Error("No se pueden eliminar empresas directamente desde CreaTurno");
  }
  
  // User-Company relations - Obtener relaciones usuario-empresa de Productiva
  async getUserCompanies(userId: number): Promise<UserCompany[]> {
    const rows = await this.query(`
      SELECT uc.user_id, uc.company_id, 'member' as role, NOW() as created_at
      FROM user_companies uc
      WHERE uc.user_id = $1
    `, [userId]);
    
    return rows.map(row => ({
      id: 0, // ID sintético
      userId: row.user_id,
      companyId: row.company_id,
      role: row.role,
      createdAt: row.created_at
    }));
  }
  
  async getCompanyUsers(companyId: number): Promise<UserCompany[]> {
    const rows = await this.query(`
      SELECT uc.user_id, uc.company_id, 'member' as role, NOW() as created_at
      FROM user_companies uc
      WHERE uc.company_id = $1
    `, [companyId]);
    
    return rows.map(row => ({
      id: 0, // ID sintético
      userId: row.user_id,
      companyId: row.company_id,
      role: row.role,
      createdAt: row.created_at
    }));
  }
  
  // En el adaptador no modificamos relaciones usuario-empresa, lo hace el sistema principal
  async assignUserToCompany(userId: number, companyId: number, role: string): Promise<UserCompany> {
    throw new Error("No se pueden modificar relaciones usuario-empresa directamente desde CreaTurno");
  }
  
  async removeUserFromCompany(userId: number, companyId: number): Promise<boolean> {
    throw new Error("No se pueden eliminar relaciones usuario-empresa directamente desde CreaTurno");
  }
  
  // Employee operations - Convertir empleados de Productiva al formato de CreaTurno
  async getEmployees(companyId?: number): Promise<Employee[]> {
    let query = `
      SELECT e.id, e.first_name || ' ' || e.last_name as name, e.position as role,
             e.company_id, e.email, e.phone, e.address, e.start_date as hire_date,
             e.contract_type, e.bank_account, e.is_active
      FROM employees e
      WHERE e.is_active = true
    `;
    
    const params = [];
    
    if (companyId) {
      query += " AND e.company_id = $1";
      params.push(companyId);
    }
    
    query += " ORDER BY e.first_name, e.last_name";
    
    const rows = await this.query(query, params);
    
    return rows.map(row => ({
      id: row.id,
      name: row.name,
      role: row.role || "",
      companyId: row.company_id,
      email: row.email || "",
      phone: row.phone || "",
      address: row.address || "",
      hireDate: row.hire_date,
      contractType: row.contract_type || "",
      hourlyRate: 0,
      maxHoursPerWeek: 40,
      preferredDays: "",
      unavailableDays: "",
      isActive: row.is_active,
      notes: "",
      createdAt: new Date(),
      updatedAt: new Date()
    }));
  }
  
  async getEmployee(id: number): Promise<Employee | undefined> {
    const rows = await this.query(`
      SELECT e.id, e.first_name || ' ' || e.last_name as name, e.position as role,
             e.company_id, e.email, e.phone, e.address, e.start_date as hire_date,
             e.contract_type, e.bank_account, e.is_active
      FROM employees e
      WHERE e.id = $1
    `, [id]);
    
    if (rows.length === 0) return undefined;
    
    return {
      id: rows[0].id,
      name: rows[0].name,
      role: rows[0].role || "",
      companyId: rows[0].company_id,
      email: rows[0].email || "",
      phone: rows[0].phone || "",
      address: rows[0].address || "",
      hireDate: rows[0].hire_date,
      contractType: rows[0].contract_type || "",
      hourlyRate: 0,
      maxHoursPerWeek: 40,
      preferredDays: "",
      unavailableDays: "",
      isActive: rows[0].is_active,
      notes: "",
      createdAt: new Date(),
      updatedAt: new Date()
    };
  }
  
  // En CreaTurno sí podemos crear empleados, pero lo sincronizamos con la base de datos principal
  async createEmployee(employee: InsertEmployee): Promise<Employee> {
    throw new Error("No se pueden crear empleados directamente desde CreaTurno");
  }
  
  async updateEmployee(id: number, employee: Partial<InsertEmployee>): Promise<Employee | undefined> {
    throw new Error("No se pueden actualizar empleados directamente desde CreaTurno");
  }
  
  async deleteEmployee(id: number): Promise<boolean> {
    throw new Error("No se pueden eliminar empleados directamente desde CreaTurno");
  }
  
  // Shift operations - Estas operaciones sí las gestionamos en CreaTurno
  async getShifts(date?: string, employeeId?: number, companyId?: number): Promise<Shift[]> {
    let query = `
      SELECT s.id, s.employee_id, s.date, s.start_time, s.end_time, 
             s.notes, s.status, s.break_time, s.schedule_id
      FROM shifts s
    `;
    
    const whereConditions = [];
    const params = [];
    let paramIndex = 1;
    
    if (date) {
      whereConditions.push(`s.date = $${paramIndex++}`);
      params.push(date);
    }
    
    if (employeeId) {
      whereConditions.push(`s.employee_id = $${paramIndex++}`);
      params.push(employeeId);
    }
    
    if (companyId) {
      // Para filtrar por compañía, necesitamos unir con la tabla employees
      query += " JOIN employees e ON s.employee_id = e.id";
      whereConditions.push(`e.company_id = $${paramIndex++}`);
      params.push(companyId);
    }
    
    if (whereConditions.length > 0) {
      query += " WHERE " + whereConditions.join(" AND ");
    }
    
    query += " ORDER BY s.date, s.start_time";
    
    const rows = await this.query(query, params);
    
    return rows.map(row => ({
      id: row.id,
      employeeId: row.employee_id,
      date: row.date,
      startTime: row.start_time,
      endTime: row.end_time,
      notes: row.notes || "",
      status: row.status || "scheduled",
      breakTime: row.break_time || 0,
      actualStartTime: null,
      actualEndTime: null,
      totalHours: null,
      scheduleId: row.schedule_id,
      createdAt: new Date(),
      updatedAt: new Date()
    }));
  }
  
  async getShift(id: number): Promise<Shift | undefined> {
    const rows = await this.query(`
      SELECT s.id, s.employee_id, s.date, s.start_time, s.end_time, 
             s.notes, s.status, s.break_time, s.schedule_id
      FROM shifts s
      WHERE s.id = $1
    `, [id]);
    
    if (rows.length === 0) return undefined;
    
    return {
      id: rows[0].id,
      employeeId: rows[0].employee_id,
      date: rows[0].date,
      startTime: rows[0].start_time,
      endTime: rows[0].end_time,
      notes: rows[0].notes || "",
      status: rows[0].status || "scheduled",
      breakTime: rows[0].break_time || 0,
      actualStartTime: null,
      actualEndTime: null,
      totalHours: null,
      scheduleId: rows[0].schedule_id,
      createdAt: new Date(),
      updatedAt: new Date()
    };
  }
  
  async createShift(shift: InsertShift): Promise<Shift> {
    const result = await this.query(`
      INSERT INTO shifts (employee_id, date, start_time, end_time, notes, status, break_time, schedule_id)
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
      RETURNING id
    `, [
      shift.employeeId, 
      shift.date, 
      shift.startTime, 
      shift.endTime, 
      shift.notes || "", 
      shift.status || "scheduled", 
      shift.breakTime || 0,
      shift.scheduleId
    ]);
    
    const id = result[0].id;
    
    return {
      id,
      ...shift,
      actualStartTime: null,
      actualEndTime: null,
      totalHours: null,
      createdAt: new Date(),
      updatedAt: new Date()
    };
  }
  
  async updateShift(id: number, shift: Partial<InsertShift>): Promise<Shift | undefined> {
    // Construir la consulta de actualización dinámicamente
    const updates = [];
    const values = [];
    let paramIndex = 1;
    
    if (shift.employeeId !== undefined) {
      updates.push(`employee_id = $${paramIndex++}`);
      values.push(shift.employeeId);
    }
    
    if (shift.date !== undefined) {
      updates.push(`date = $${paramIndex++}`);
      values.push(shift.date);
    }
    
    if (shift.startTime !== undefined) {
      updates.push(`start_time = $${paramIndex++}`);
      values.push(shift.startTime);
    }
    
    if (shift.endTime !== undefined) {
      updates.push(`end_time = $${paramIndex++}`);
      values.push(shift.endTime);
    }
    
    if (shift.notes !== undefined) {
      updates.push(`notes = $${paramIndex++}`);
      values.push(shift.notes);
    }
    
    if (shift.status !== undefined) {
      updates.push(`status = $${paramIndex++}`);
      values.push(shift.status);
    }
    
    if (shift.breakTime !== undefined) {
      updates.push(`break_time = $${paramIndex++}`);
      values.push(shift.breakTime);
    }
    
    if (shift.scheduleId !== undefined) {
      updates.push(`schedule_id = $${paramIndex++}`);
      values.push(shift.scheduleId);
    }
    
    if (updates.length === 0) {
      return await this.getShift(id);
    }
    
    values.push(id);
    
    const result = await this.query(`
      UPDATE shifts
      SET ${updates.join(", ")}
      WHERE id = $${paramIndex}
      RETURNING id
    `, values);
    
    if (result.length === 0) return undefined;
    
    return await this.getShift(id);
  }
  
  async deleteShift(id: number): Promise<boolean> {
    const result = await this.query(`
      DELETE FROM shifts
      WHERE id = $1
      RETURNING id
    `, [id]);
    
    return result.length > 0;
  }
  
  // Schedule operations - Estas operaciones sí las gestionamos en CreaTurno
  async getSchedules(companyId?: number): Promise<Schedule[]> {
    let query = `
      SELECT s.id, s.name, s.description, s.company_id, s.template_id,
             s.start_date, s.end_date, s.status, s.department, s.created_by,
             s.created_at
      FROM schedules s
    `;
    
    const params = [];
    
    if (companyId) {
      query += " WHERE s.company_id = $1";
      params.push(companyId);
    }
    
    query += " ORDER BY s.created_at DESC";
    
    const rows = await this.query(query, params);
    
    return rows.map(row => ({
      id: row.id,
      name: row.name,
      description: row.description || "",
      companyId: row.company_id,
      templateId: row.template_id,
      startDate: row.start_date,
      endDate: row.end_date,
      status: row.status || "draft",
      department: row.department,
      createdBy: row.created_by,
      createdAt: row.created_at,
      updatedAt: row.created_at
    }));
  }
  
  async getSchedule(id: number): Promise<Schedule | undefined> {
    const rows = await this.query(`
      SELECT s.id, s.name, s.description, s.company_id, s.template_id,
             s.start_date, s.end_date, s.status, s.department, s.created_by,
             s.created_at
      FROM schedules s
      WHERE s.id = $1
    `, [id]);
    
    if (rows.length === 0) return undefined;
    
    return {
      id: rows[0].id,
      name: rows[0].name,
      description: rows[0].description || "",
      companyId: rows[0].company_id,
      templateId: rows[0].template_id,
      startDate: rows[0].start_date,
      endDate: rows[0].end_date,
      status: rows[0].status || "draft",
      department: rows[0].department,
      createdBy: rows[0].created_by,
      createdAt: rows[0].created_at,
      updatedAt: rows[0].created_at
    };
  }
  
  async createSchedule(schedule: InsertSchedule): Promise<Schedule> {
    const result = await this.query(`
      INSERT INTO schedules (name, description, company_id, template_id, start_date, end_date, status, department, created_by)
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
      RETURNING id, created_at
    `, [
      schedule.name,
      schedule.description || "",
      schedule.companyId,
      schedule.templateId || null,
      schedule.startDate || null,
      schedule.endDate || null,
      schedule.status || "draft",
      schedule.department || null,
      schedule.createdBy || null
    ]);
    
    const id = result[0].id;
    const createdAt = result[0].created_at;
    
    return {
      id,
      ...schedule,
      createdAt,
      updatedAt: createdAt
    };
  }
  
  async deleteSchedule(id: number): Promise<boolean> {
    // Primero eliminar los turnos asociados a este horario
    await this.query(`
      DELETE FROM shifts
      WHERE schedule_id = $1
    `, [id]);
    
    // Luego eliminar el horario
    const result = await this.query(`
      DELETE FROM schedules
      WHERE id = $1
      RETURNING id
    `, [id]);
    
    return result.length > 0;
  }
  
  // Estas operaciones específicas de CreaTurno
  async getScheduleTemplates(userId?: number): Promise<ScheduleTemplate[]> {
    let query = `
      SELECT t.id, t.name, t.description, t.is_default, t.start_hour, t.end_hour,
             t.time_increment, t.is_global, t.company_id, t.created_by, t.created_at
      FROM schedule_templates t
    `;
    
    const whereConditions = [];
    const params = [];
    let paramIndex = 1;
    
    if (userId) {
      // Para un usuario regular, obtener sus plantillas personales y las globales
      whereConditions.push(`(t.created_by = $${paramIndex++} OR t.is_global = true)`);
      params.push(userId);
    }
    
    if (whereConditions.length > 0) {
      query += " WHERE " + whereConditions.join(" AND ");
    }
    
    query += " ORDER BY t.created_at DESC";
    
    const rows = await this.query(query, params);
    
    return rows.map(row => ({
      id: row.id,
      name: row.name,
      description: row.description || "",
      isDefault: row.is_default,
      startHour: row.start_hour,
      endHour: row.end_hour,
      timeIncrement: row.time_increment,
      isGlobal: row.is_global,
      companyId: row.company_id,
      createdBy: row.created_by,
      createdAt: row.created_at,
      updatedAt: row.created_at
    }));
  }
  
  async getScheduleTemplate(id: number): Promise<ScheduleTemplate | undefined> {
    const rows = await this.query(`
      SELECT t.id, t.name, t.description, t.is_default, t.start_hour, t.end_hour,
             t.time_increment, t.is_global, t.company_id, t.created_by, t.created_at
      FROM schedule_templates t
      WHERE t.id = $1
    `, [id]);
    
    if (rows.length === 0) return undefined;
    
    return {
      id: rows[0].id,
      name: rows[0].name,
      description: rows[0].description || "",
      isDefault: rows[0].is_default,
      startHour: rows[0].start_hour,
      endHour: rows[0].end_hour,
      timeIncrement: rows[0].time_increment,
      isGlobal: rows[0].is_global,
      companyId: rows[0].company_id,
      createdBy: rows[0].created_by,
      createdAt: rows[0].created_at,
      updatedAt: rows[0].created_at
    };
  }
  
  async createScheduleTemplate(template: InsertScheduleTemplate): Promise<ScheduleTemplate> {
    const result = await this.query(`
      INSERT INTO schedule_templates (name, description, is_default, start_hour, end_hour, time_increment, is_global, company_id, created_by)
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
      RETURNING id, created_at
    `, [
      template.name,
      template.description || "",
      template.isDefault || false,
      template.startHour || 8,
      template.endHour || 20,
      template.timeIncrement || 15,
      template.isGlobal || false,
      template.companyId || null,
      template.createdBy || null
    ]);
    
    const id = result[0].id;
    const createdAt = result[0].created_at;
    
    return {
      id,
      ...template,
      createdAt,
      updatedAt: createdAt
    };
  }
  
  async updateScheduleTemplate(id: number, template: Partial<InsertScheduleTemplate>): Promise<ScheduleTemplate | undefined> {
    // Construir la consulta de actualización dinámicamente
    const updates = [];
    const values = [];
    let paramIndex = 1;
    
    if (template.name !== undefined) {
      updates.push(`name = $${paramIndex++}`);
      values.push(template.name);
    }
    
    if (template.description !== undefined) {
      updates.push(`description = $${paramIndex++}`);
      values.push(template.description);
    }
    
    if (template.isDefault !== undefined) {
      updates.push(`is_default = $${paramIndex++}`);
      values.push(template.isDefault);
    }
    
    if (template.startHour !== undefined) {
      updates.push(`start_hour = $${paramIndex++}`);
      values.push(template.startHour);
    }
    
    if (template.endHour !== undefined) {
      updates.push(`end_hour = $${paramIndex++}`);
      values.push(template.endHour);
    }
    
    if (template.timeIncrement !== undefined) {
      updates.push(`time_increment = $${paramIndex++}`);
      values.push(template.timeIncrement);
    }
    
    if (template.isGlobal !== undefined) {
      updates.push(`is_global = $${paramIndex++}`);
      values.push(template.isGlobal);
    }
    
    if (template.companyId !== undefined) {
      updates.push(`company_id = $${paramIndex++}`);
      values.push(template.companyId);
    }
    
    if (updates.length === 0) {
      return await this.getScheduleTemplate(id);
    }
    
    values.push(id);
    
    const result = await this.query(`
      UPDATE schedule_templates
      SET ${updates.join(", ")}
      WHERE id = $${paramIndex}
      RETURNING id
    `, values);
    
    if (result.length === 0) return undefined;
    
    return await this.getScheduleTemplate(id);
  }
  
  async deleteScheduleTemplate(id: number): Promise<boolean> {
    const result = await this.query(`
      DELETE FROM schedule_templates
      WHERE id = $1
      RETURNING id
    `, [id]);
    
    return result.length > 0;
  }
  
  // Save and load entire schedule data
  async saveScheduleData(scheduleId: number, employees: Employee[], shifts: Shift[]): Promise<boolean> {
    // Este método sería específico para el manejo de datos en CreaTurno
    // Podríamos no implementarlo o adaptar la lógica según sea necesario
    
    // Por ahora, implementación simple: guardamos los turnos uno por uno
    for (const shift of shifts) {
      if (!shift.id) {
        // Es un nuevo turno, crearlo
        await this.createShift({
          ...shift,
          scheduleId
        });
      } else {
        // Es un turno existente, actualizarlo
        await this.updateShift(shift.id, {
          ...shift,
          scheduleId
        });
      }
    }
    
    return true;
  }
  
  async loadScheduleData(scheduleId: number): Promise<{ employees: Employee[], shifts: Shift[] } | undefined> {
    const schedule = await this.getSchedule(scheduleId);
    if (!schedule) return undefined;
    
    // Obtener todos los turnos de este horario
    const shifts = await this.getShifts(undefined, undefined, schedule.companyId);
    const filteredShifts = shifts.filter(shift => shift.scheduleId === scheduleId);
    
    // Obtener todos los empleados de la empresa
    const employees = await this.getEmployees(schedule.companyId);
    
    return {
      employees,
      shifts: filteredShifts
    };
  }
}
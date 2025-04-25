/**
 * Adaptador de almacenamiento para integrar CreaTurno con Productiva
 * 
 * Este archivo adapta el sistema de almacenamiento de CreaTurno para usar
 * las tablas y modelos de Productiva en lugar de sus propias estructuras.
 */

import { drizzle } from "drizzle-orm/node-postgres";
import { pgTable, serial, varchar, timestamp, integer, text, boolean, time } from "drizzle-orm/pg-core";
import { relations } from "drizzle-orm";
import { eq, and, or, inArray, gt, lt, gte, lte } from "drizzle-orm";
import { Pool } from "pg";

// Configuración de la conexión a la base de datos
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
});

// Definición de esquemas para tablas existentes en Productiva
export const users = pgTable("users", {
  id: serial("id").primaryKey(),
  username: varchar("username", { length: 64 }).notNull(),
  email: varchar("email", { length: 120 }).notNull(),
  first_name: varchar("first_name", { length: 64 }),
  last_name: varchar("last_name", { length: 64 }),
  password_hash: varchar("password_hash", { length: 256 }),
  role: varchar("role", { length: 20 }),
  created_at: timestamp("created_at").defaultNow(),
  updated_at: timestamp("updated_at").defaultNow(),
});

export const companies = pgTable("companies", {
  id: serial("id").primaryKey(),
  name: varchar("name", { length: 100 }).notNull(),
  address: varchar("address", { length: 255 }),
  phone: varchar("phone", { length: 20 }),
  email: varchar("email", { length: 120 }),
  tax_id: varchar("tax_id", { length: 20 }),
  is_active: boolean("is_active").default(true),
  logo_url: varchar("logo_url", { length: 255 }),
  created_at: timestamp("created_at").defaultNow(),
  updated_at: timestamp("updated_at").defaultNow(),
});

export const userCompanies = pgTable("user_companies", {
  id: serial("id").primaryKey(),
  user_id: integer("user_id").notNull().references(() => users.id, { onDelete: "cascade" }),
  company_id: integer("company_id").notNull().references(() => companies.id, { onDelete: "cascade" }),
});

export const employees = pgTable("employees", {
  id: serial("id").primaryKey(),
  company_id: integer("company_id").notNull().references(() => companies.id, { onDelete: "cascade" }),
  first_name: varchar("first_name", { length: 64 }).notNull(),
  last_name: varchar("last_name", { length: 64 }).notNull(),
  email: varchar("email", { length: 120 }),
  phone: varchar("phone", { length: 20 }),
  position: varchar("position", { length: 64 }),
  dni: varchar("dni", { length: 20 }),
  social_security_number: varchar("social_security_number", { length: 20 }),
  bank_account: varchar("bank_account", { length: 50 }),
  admission_date: varchar("admission_date", { length: 20 }),
  contract_type: varchar("contract_type", { length: 50 }),
  is_active: boolean("is_active").default(true),
  created_at: timestamp("created_at").defaultNow(),
  updated_at: timestamp("updated_at").defaultNow(),
});

export const locations = pgTable("locations", {
  id: serial("id").primaryKey(),
  name: varchar("name", { length: 100 }).notNull(),
  address: varchar("address", { length: 255 }),
  company_id: integer("company_id").notNull().references(() => companies.id, { onDelete: "cascade" }),
  is_active: boolean("is_active").default(true),
  created_at: timestamp("created_at").defaultNow(),
  updated_at: timestamp("updated_at").defaultNow(),
});

// Definición de esquemas para las tablas de CreaTurno
export const creaturnoShifts = pgTable("creaturno_shifts", {
  id: serial("id").primaryKey(),
  employee_id: integer("employee_id").notNull().references(() => employees.id, { onDelete: "cascade" }),
  location_id: integer("location_id").notNull().references(() => locations.id, { onDelete: "cascade" }),
  start_time: timestamp("start_time", { withTimezone: true }).notNull(),
  end_time: timestamp("end_time", { withTimezone: true }).notNull(),
  role: varchar("role", { length: 100 }),
  color: varchar("color", { length: 50 }),
  notes: text("notes"),
  created_at: timestamp("created_at", { withTimezone: true }).defaultNow(),
  updated_at: timestamp("updated_at", { withTimezone: true }).defaultNow(),
});

export const creaturnoShiftTemplates = pgTable("creaturno_shift_templates", {
  id: serial("id").primaryKey(),
  name: varchar("name", { length: 100 }).notNull(),
  company_id: integer("company_id").notNull().references(() => companies.id, { onDelete: "cascade" }),
  start_time: time("start_time").notNull(),
  end_time: time("end_time").notNull(),
  days_of_week: varchar("days_of_week", { length: 255 }),
  role: varchar("role", { length: 100 }),
  color: varchar("color", { length: 50 }),
  notes: text("notes"),
  created_at: timestamp("created_at", { withTimezone: true }).defaultNow(),
  updated_at: timestamp("updated_at", { withTimezone: true }).defaultNow(),
});

export const creaturnoShiftRoles = pgTable("creaturno_shift_roles", {
  id: serial("id").primaryKey(),
  name: varchar("name", { length: 100 }).notNull(),
  company_id: integer("company_id").notNull().references(() => companies.id, { onDelete: "cascade" }),
  color: varchar("color", { length: 50 }),
  description: text("description"),
  created_at: timestamp("created_at", { withTimezone: true }).defaultNow(),
  updated_at: timestamp("updated_at", { withTimezone: true }).defaultNow(),
});

// Relaciones entre tablas
export const usersRelations = relations(users, ({ many }) => ({
  userCompanies: many(userCompanies),
}));

export const companiesRelations = relations(companies, ({ many }) => ({
  userCompanies: many(userCompanies),
  employees: many(employees),
  locations: many(locations),
  shiftTemplates: many(creaturnoShiftTemplates),
  shiftRoles: many(creaturnoShiftRoles),
}));

export const userCompaniesRelations = relations(userCompanies, ({ one }) => ({
  user: one(users, {
    fields: [userCompanies.user_id],
    references: [users.id],
  }),
  company: one(companies, {
    fields: [userCompanies.company_id],
    references: [companies.id],
  }),
}));

export const employeesRelations = relations(employees, ({ one, many }) => ({
  company: one(companies, {
    fields: [employees.company_id],
    references: [companies.id],
  }),
  shifts: many(creaturnoShifts),
}));

export const locationsRelations = relations(locations, ({ one, many }) => ({
  company: one(companies, {
    fields: [locations.company_id],
    references: [companies.id],
  }),
  shifts: many(creaturnoShifts),
}));

export const creaturnoShiftsRelations = relations(creaturnoShifts, ({ one }) => ({
  employee: one(employees, {
    fields: [creaturnoShifts.employee_id],
    references: [employees.id],
  }),
  location: one(locations, {
    fields: [creaturnoShifts.location_id],
    references: [locations.id],
  }),
}));

export const creaturnoShiftTemplatesRelations = relations(creaturnoShiftTemplates, ({ one }) => ({
  company: one(companies, {
    fields: [creaturnoShiftTemplates.company_id],
    references: [companies.id],
  }),
}));

export const creaturnoShiftRolesRelations = relations(creaturnoShiftRoles, ({ one }) => ({
  company: one(companies, {
    fields: [creaturnoShiftRoles.company_id],
    references: [companies.id],
  }),
}));

// Crear instancia de drizzle
export const db = drizzle(pool, {
  schema: {
    users,
    companies,
    userCompanies,
    employees,
    locations,
    creaturnoShifts,
    creaturnoShiftTemplates,
    creaturnoShiftRoles,
  },
});

// Exportar también helpers de drizzle para usarlos en queries
export { eq, and, or, inArray, gt, lt, gte, lte };
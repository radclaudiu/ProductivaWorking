package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para empleados.
 * Representa la estructura de datos de un empleado en el sistema.
 * 
 * @property id Identificador único del empleado
 * @property companyId ID de la empresa a la que pertenece
 * @property firstName Nombre del empleado
 * @property lastName Apellidos del empleado
 * @property email Correo electrónico del empleado
 * @property phone Número de teléfono
 * @property dni Documento Nacional de Identidad
 * @property position Cargo o puesto en la empresa
 * @property department Departamento al que pertenece
 * @property bankAccount Cuenta bancaria para nóminas
 * @property active Indica si el empleado está activo
 * @property hireDate Fecha de contratación (formato DD-MM-YYYY)
 * @property terminationDate Fecha de baja (formato DD-MM-YYYY, null si sigue activo)
 * @property contractHours Horas semanales según contrato
 * @property photoUrl URL o ruta a la foto del empleado
 * @property status Estado actual del empleado (ACTIVE, INACTIVE, VACATION, etc.)
 * @property statusStartDate Fecha de inicio del estado actual
 * @property statusEndDate Fecha de fin del estado actual (para vacaciones, bajas, etc.)
 * @property statusNotes Notas sobre el estado actual
 * @property lastCheckIn Fecha y hora del último fichaje de entrada
 * @property onShift Indica si el empleado está actualmente en turno
 */
@Entity(
    tableName = "employees",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("companyId")]
)
data class Employee(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("company_id")
    val companyId: Int,
    
    @SerializedName("first_name")
    val firstName: String,
    
    @SerializedName("last_name")
    val lastName: String,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("dni")
    val dni: String? = null,
    
    @SerializedName("position")
    val position: String? = null,
    
    @SerializedName("department")
    val department: String? = null,
    
    @SerializedName("bank_account")
    val bankAccount: String? = null,
    
    @SerializedName("active")
    val active: Boolean = true,
    
    @SerializedName("hire_date")
    val hireDate: String? = null,
    
    @SerializedName("termination_date")
    val terminationDate: String? = null,
    
    @SerializedName("contract_hours")
    val contractHours: Float? = null,
    
    @SerializedName("photo_url")
    val photoUrl: String? = null,
    
    @SerializedName("status")
    val status: String = "ACTIVE",
    
    @SerializedName("status_start_date")
    val statusStartDate: String? = null,
    
    @SerializedName("status_end_date")
    val statusEndDate: String? = null,
    
    @SerializedName("status_notes")
    val statusNotes: String? = null,
    
    @SerializedName("last_check_in")
    val lastCheckIn: String? = null,
    
    @SerializedName("on_shift")
    val onShift: Boolean = false
) {
    // Campo calculado para obtener el nombre completo
    val fullName: String
        get() = "$firstName $lastName"
}
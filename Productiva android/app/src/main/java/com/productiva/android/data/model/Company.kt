package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para empresas.
 * Representa la estructura de datos de una empresa en el sistema.
 * 
 * @property id Identificador único de la empresa
 * @property name Nombre de la empresa
 * @property taxId Identificación fiscal (CIF/NIF)
 * @property address Dirección de la empresa
 * @property city Ciudad donde se encuentra la empresa
 * @property postalCode Código postal
 * @property country País
 * @property phoneNumber Número de teléfono de contacto
 * @property email Correo electrónico de contacto
 * @property logo URL o ruta al logo de la empresa
 * @property active Indica si la empresa está activa
 * @property hourlyEmployeeCost Costo por hora promedio de los empleados
 */
@Entity(tableName = "companies")
data class Company(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("tax_id")
    val taxId: String? = null,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("city")
    val city: String? = null,
    
    @SerializedName("postal_code")
    val postalCode: String? = null,
    
    @SerializedName("country")
    val country: String? = null,
    
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("logo")
    val logo: String? = null,
    
    @SerializedName("active")
    val active: Boolean = true,
    
    @SerializedName("hourly_employee_cost")
    val hourlyEmployeeCost: Double? = null
)
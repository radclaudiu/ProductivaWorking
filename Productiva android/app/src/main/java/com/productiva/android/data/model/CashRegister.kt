package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para arqueos de caja.
 * Representa un arqueo de caja diario en el sistema.
 * 
 * @property id Identificador único del arqueo
 * @property companyId ID de la empresa a la que pertenece
 * @property date Fecha del arqueo (formato "YYYY-MM-DD")
 * @property openingBalance Saldo de apertura
 * @property closingBalance Saldo de cierre
 * @property cashSales Ventas en efectivo
 * @property cardSales Ventas con tarjeta
 * @property transferSales Ventas por transferencia bancaria
 * @property otherSales Ventas por otros medios
 * @property totalSales Total de ventas (suma de todos los métodos)
 * @property expectedClosingBalance Saldo de cierre esperado
 * @property discrepancy Discrepancia entre saldo esperado y real
 * @property notes Notas sobre el arqueo
 * @property createdBy ID del usuario que creó el arqueo
 * @property createdAt Fecha y hora de creación
 * @property status Estado del arqueo (OPEN, CLOSED)
 * @property syncStatus Estado de sincronización con el servidor
 */
@Entity(
    tableName = "cash_registers",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("companyId"),
        Index("date")
    ]
)
data class CashRegister(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,  // 0 para arqueos locales aún no sincronizados
    
    @SerializedName("company_id")
    val companyId: Int,
    
    @SerializedName("date")
    val date: String,  // YYYY-MM-DD
    
    @SerializedName("opening_balance")
    val openingBalance: Double,
    
    @SerializedName("closing_balance")
    val closingBalance: Double? = null,
    
    @SerializedName("cash_sales")
    val cashSales: Double = 0.0,
    
    @SerializedName("card_sales")
    val cardSales: Double = 0.0,
    
    @SerializedName("transfer_sales")
    val transferSales: Double = 0.0,
    
    @SerializedName("other_sales")
    val otherSales: Double = 0.0,
    
    @SerializedName("total_sales")
    val totalSales: Double = 0.0,
    
    @SerializedName("expected_closing_balance")
    val expectedClosingBalance: Double = 0.0,
    
    @SerializedName("discrepancy")
    val discrepancy: Double = 0.0,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("created_by")
    val createdBy: Int? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("status")
    val status: String = "OPEN",  // OPEN, CLOSED
    
    // Campos locales (no se envían al servidor)
    val syncStatus: String = "SYNCED"  // SYNCED, PENDING_SYNC, SYNC_ERROR
)
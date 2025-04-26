package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo para plantillas de etiquetas para impresoras Brother.
 * Contiene la configuración de formato y campos para generar etiquetas.
 */
@Entity(tableName = "label_templates")
data class LabelTemplate(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("type")
    val type: String, // "product", "location", "asset", "custom"
    
    @SerializedName("paper_size")
    val paperSize: String, // "62mm", "29mm", etc.
    
    @SerializedName("orientation")
    val orientation: String = "landscape", // "portrait" o "landscape"
    
    @SerializedName("dpi")
    val dpi: Int = 300,
    
    @SerializedName("font_size")
    val fontSize: Float = 10f,
    
    @SerializedName("font_family")
    val fontFamily: String = "Arial",
    
    @SerializedName("bold_title")
    val boldTitle: Boolean = true,
    
    @SerializedName("include_barcode")
    val includeBarcode: Boolean = true,
    
    @SerializedName("barcode_type")
    val barcodeType: String = "CODE128",
    
    @SerializedName("include_logo")
    val includeLogo: Boolean = false,
    
    @SerializedName("logo_url")
    val logoUrl: String? = null,
    
    @SerializedName("include_price")
    val includePrice: Boolean = true,
    
    @SerializedName("include_date")
    val includeDate: Boolean = false,
    
    @SerializedName("date_format")
    val dateFormat: String = "dd/MM/yyyy",
    
    @SerializedName("custom_fields")
    val customFields: List<String> = emptyList(),
    
    @SerializedName("is_default")
    val isDefault: Boolean = false,
    
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    @SerializedName("use_count")
    val useCount: Int = 0,
    
    // Campos locales
    var localUseCount: Int = 0,
    var needsSync: Boolean = false,
    var lastSyncTime: Long = 0
) {
    /**
     * Genera los datos necesarios para imprimir una etiqueta para un producto.
     */
    fun generateProductLabelData(product: Product): Map<String, Any> {
        val labelData = mutableMapOf<String, Any>()
        
        // Datos básicos del producto
        labelData["title"] = product.name
        labelData["code"] = product.code ?: ""
        
        // Código de barras
        if (includeBarcode && !product.barcode.isNullOrEmpty()) {
            labelData["barcode"] = product.barcode
            labelData["barcodeType"] = barcodeType
        } else if (includeBarcode) {
            // Si no hay código de barras pero se solicita incluirlo, usamos el código del producto
            labelData["barcode"] = product.code ?: product.id.toString()
            labelData["barcodeType"] = barcodeType
        }
        
        // Precio
        if (includePrice) {
            labelData["price"] = product.formattedPrice()
        }
        
        // Fecha
        if (includeDate) {
            val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
            labelData["date"] = formatter.format(Date())
        }
        
        // Logo
        if (includeLogo && !logoUrl.isNullOrEmpty()) {
            labelData["logoUrl"] = logoUrl
        }
        
        // Campos personalizados
        for (field in customFields) {
            when (field) {
                "category" -> labelData["category"] = product.category ?: ""
                "supplier" -> labelData["supplier"] = product.supplier ?: ""
                "stock" -> product.stock?.let { labelData["stock"] = it.toString() }
                "description" -> product.description?.let { 
                    // Truncar descripción para que quepa en la etiqueta
                    labelData["description"] = if (it.length > 50) it.substring(0, 47) + "..." else it
                }
            }
        }
        
        return labelData
    }
    
    /**
     * Incrementa el contador de uso local y marca para sincronización.
     */
    fun incrementUseCount(): LabelTemplate {
        return this.copy(
            localUseCount = localUseCount + 1,
            needsSync = true
        )
    }
    
    /**
     * Actualiza la plantilla con datos del servidor.
     */
    fun updateFromServer(serverTemplate: LabelTemplate): LabelTemplate {
        return if (needsSync) {
            // Si hay cambios locales pendientes, solo actualizamos campos no críticos
            this.copy(
                name = serverTemplate.name,
                description = serverTemplate.description,
                isDefault = serverTemplate.isDefault,
                updatedAt = serverTemplate.updatedAt,
                lastSyncTime = System.currentTimeMillis()
            )
        } else {
            // Actualización completa preservando el contador de uso local
            serverTemplate.copy(
                localUseCount = this.localUseCount,
                needsSync = false,
                lastSyncTime = System.currentTimeMillis()
            )
        }
    }
}
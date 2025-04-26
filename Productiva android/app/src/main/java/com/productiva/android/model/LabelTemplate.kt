package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo de datos que representa una plantilla de etiqueta para impresión.
 * Se almacena en la base de datos local y se sincroniza con el servidor.
 */
@Entity(tableName = "label_templates")
data class LabelTemplate(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("template_data")
    val templateData: String,
    
    @SerializedName("template_type")
    val templateType: String, // BROTHER, ZEBRA, GENERIC
    
    @SerializedName("label_width")
    val labelWidth: Int, // in mm
    
    @SerializedName("label_height")
    val labelHeight: Int, // in mm
    
    @SerializedName("dpi")
    val dpi: Int, // 203, 300, 600
    
    @SerializedName("orientation")
    val orientation: String, // PORTRAIT, LANDSCAPE
    
    @SerializedName("created_at")
    val createdAt: String?,
    
    @SerializedName("updated_at")
    val updatedAt: String?,
    
    @SerializedName("created_by")
    val createdBy: Int?,
    
    @SerializedName("company_id")
    val companyId: Int?,
    
    @SerializedName("is_default")
    val isDefault: Boolean,
    
    @SerializedName("usage_count")
    val usageCount: Int,
    
    @SerializedName("last_used_at")
    val lastUsedAt: String?,
    
    @SerializedName("fields")
    val fields: List<String>?, // Lista de campos disponibles en la plantilla
    
    // Campos locales
    val localUsageCount: Int = 0,
    val needsSync: Boolean = false
) {
    /**
     * Incrementa el contador de uso de la plantilla.
     */
    fun incrementUsage(): LabelTemplate {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        return this.copy(
            localUsageCount = localUsageCount + 1,
            lastUsedAt = now,
            needsSync = true
        )
    }
    
    /**
     * Actualiza esta plantilla con información del servidor.
     */
    fun updateFromServer(serverTemplate: LabelTemplate): LabelTemplate {
        return this.copy(
            name = serverTemplate.name,
            description = serverTemplate.description,
            templateData = serverTemplate.templateData,
            templateType = serverTemplate.templateType,
            labelWidth = serverTemplate.labelWidth,
            labelHeight = serverTemplate.labelHeight,
            dpi = serverTemplate.dpi,
            orientation = serverTemplate.orientation,
            updatedAt = serverTemplate.updatedAt,
            isDefault = serverTemplate.isDefault,
            usageCount = serverTemplate.usageCount,
            lastUsedAt = serverTemplate.lastUsedAt,
            fields = serverTemplate.fields,
            needsSync = false
        )
    }
    
    /**
     * Genera los datos de impresión para un producto específico.
     */
    fun generatePrintData(product: Product): String {
        // Reemplazar los marcadores de posición en la plantilla con los datos del producto
        var printData = templateData
        
        // Datos básicos del producto
        printData = printData.replace("[PRODUCT_ID]", product.id.toString())
        printData = printData.replace("[PRODUCT_NAME]", product.name)
        printData = printData.replace("[PRODUCT_CODE]", product.code ?: "")
        printData = printData.replace("[PRODUCT_BARCODE]", product.barcode ?: "")
        printData = printData.replace("[PRODUCT_SKU]", product.sku ?: "")
        printData = printData.replace("[PRODUCT_PRICE]", product.formattedPrice())
        
        // Datos de categoría y marca
        printData = printData.replace("[PRODUCT_CATEGORY]", product.category ?: "")
        printData = printData.replace("[PRODUCT_BRAND]", product.brand ?: "")
        
        // Fecha y hora actual
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        printData = printData.replace("[CURRENT_DATE]", currentDate)
        printData = printData.replace("[CURRENT_TIME]", currentTime)
        
        // Si el producto tiene fecha de creación o actualización
        product.createdAt?.let { date ->
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val parsedDate = inputFormat.parse(date)
                parsedDate?.let {
                    printData = printData.replace("[PRODUCT_CREATED_DATE]", outputFormat.format(it))
                }
            } catch (e: Exception) {
                printData = printData.replace("[PRODUCT_CREATED_DATE]", "")
            }
        } ?: run {
            printData = printData.replace("[PRODUCT_CREATED_DATE]", "")
        }
        
        // Otros datos opcionales
        product.description?.let { printData = printData.replace("[PRODUCT_DESCRIPTION]", it) }
            ?: run { printData = printData.replace("[PRODUCT_DESCRIPTION]", "") }
            
        product.weight?.let { printData = printData.replace("[PRODUCT_WEIGHT]", "$it kg") }
            ?: run { printData = printData.replace("[PRODUCT_WEIGHT]", "") }
            
        product.dimensions?.let { printData = printData.replace("[PRODUCT_DIMENSIONS]", it) }
            ?: run { printData = printData.replace("[PRODUCT_DIMENSIONS]", "") }
        
        return printData
    }
    
    companion object {
        /**
         * Crea una plantilla vacía para usar como comodín.
         */
        fun createEmpty(): LabelTemplate {
            return LabelTemplate(
                id = 0,
                name = "",
                description = null,
                templateData = "",
                templateType = "BROTHER",
                labelWidth = 62,
                labelHeight = 29,
                dpi = 300,
                orientation = "LANDSCAPE",
                createdAt = null,
                updatedAt = null,
                createdBy = null,
                companyId = null,
                isDefault = false,
                usageCount = 0,
                lastUsedAt = null,
                fields = null,
                localUsageCount = 0,
                needsSync = false
            )
        }
    }
}
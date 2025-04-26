package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.productiva.android.database.Converters

/**
 * Modelo para plantillas de etiquetas.
 * Representa una plantilla para imprimir etiquetas de productos.
 */
@Entity(tableName = "label_templates")
@TypeConverters(Converters::class)
data class LabelTemplate(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @SerializedName("creator_id")
    val creatorId: Int? = null,
    
    @SerializedName("width")
    val width: Int, // Ancho en mm
    
    @SerializedName("height")
    val height: Int, // Alto en mm
    
    @SerializedName("dpi")
    val dpi: Int = 203, // Resolución en DPI
    
    @SerializedName("orientation")
    val orientation: String = "landscape", // landscape o portrait
    
    @SerializedName("elements")
    val elements: List<LabelElement> = emptyList(),
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("is_default")
    val isDefault: Boolean = false,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    // Campos locales
    var lastSyncTime: Long = 0
) {
    /**
     * Verifica si la plantilla es compatible con una impresora específica.
     */
    fun isCompatibleWithPrinter(printerModel: String, printerDpi: Int): Boolean {
        // Lógica de compatibilidad
        return true
    }
    
    /**
     * Obtiene el tamaño de etiqueta como texto.
     */
    fun getSizeText(): String {
        return "${width}mm x ${height}mm"
    }
}

/**
 * Modelo para elementos de una etiqueta.
 * Representa un elemento visual dentro de una plantilla de etiqueta.
 */
data class LabelElement(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("type")
    val type: String, // text, barcode, qrcode, image, line, rectangle
    
    @SerializedName("x")
    val x: Int, // Posición X en píxeles
    
    @SerializedName("y")
    val y: Int, // Posición Y en píxeles
    
    @SerializedName("width")
    val width: Int? = null, // Ancho en píxeles
    
    @SerializedName("height")
    val height: Int? = null, // Alto en píxeles
    
    @SerializedName("content")
    val content: String? = null, // Contenido estático o variable (ej: {product.name})
    
    @SerializedName("font_family")
    val fontFamily: String? = null, // Solo para texto
    
    @SerializedName("font_size")
    val fontSize: Int? = null, // Solo para texto
    
    @SerializedName("font_style")
    val fontStyle: String? = null, // normal, bold, italic - Solo para texto
    
    @SerializedName("alignment")
    val alignment: String? = null, // left, center, right - Solo para texto
    
    @SerializedName("rotation")
    val rotation: Int = 0, // Rotación en grados
    
    @SerializedName("barcode_type")
    val barcodeType: String? = null, // code128, ean13, etc. - Solo para códigos de barras
    
    @SerializedName("qrcode_version")
    val qrcodeVersion: Int? = null, // Solo para QR
    
    @SerializedName("border_width")
    val borderWidth: Int? = null, // Solo para líneas y rectángulos
    
    @SerializedName("is_variable")
    val isVariable: Boolean = false, // Si el contenido es una variable o texto estático
    
    @SerializedName("variable_binding")
    val variableBinding: String? = null // Nombre de la variable (product.name, product.price, etc.)
) {
    /**
     * Evalúa el contenido del elemento con un producto específico.
     */
    fun evaluateContent(product: Product): String {
        if (!isVariable || variableBinding.isNullOrEmpty()) {
            return content ?: ""
        }
        
        // Procesar la variable según el binding
        return when (variableBinding) {
            "product.name" -> product.name
            "product.sku" -> product.sku
            "product.barcode" -> product.barcode ?: ""
            "product.price" -> product.price?.toString() ?: ""
            "product.category" -> product.category ?: ""
            else -> ""
        }
    }
}
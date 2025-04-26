package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.database.Converters

/**
 * Modelo de datos para una plantilla de etiqueta.
 * Define la estructura y el diseño de las etiquetas para impresión.
 */
@Entity(tableName = "label_templates")
@TypeConverters(Converters::class)
data class LabelTemplate(
    @PrimaryKey
    val id: Int,
    
    // Información básica de la plantilla
    val name: String,
    val description: String?,
    
    // Configuración física de la etiqueta
    val width: Int, // Ancho en mm
    val height: Int, // Alto en mm
    val orientation: String, // "portrait" o "landscape"
    val dpi: Int, // Resolución en puntos por pulgada
    
    // Configuración de impresora
    val printerModel: String?, // Modelo de impresora compatible
    val paperType: String?, // Tipo de papel/etiqueta
    
    // Elementos de la plantilla
    val elements: List<LabelElement>,
    
    // Datos adicionales
    val isDefault: Boolean, // Indica si es la plantilla predeterminada
    val createdBy: Int, // ID del usuario que creó la plantilla
    val companyId: Int, // ID de la empresa a la que pertenece
    
    // Datos para sincronización
    val lastSyncTime: Long
)

/**
 * Modelo de datos para un elemento dentro de una plantilla de etiqueta.
 */
data class LabelElement(
    val id: Int,
    
    // Tipo y posicionamiento
    val type: String, // "text", "barcode", "qrcode", "image", "line", "rectangle"
    val x: Int, // Posición X en mm desde la esquina superior izquierda
    val y: Int, // Posición Y en mm desde la esquina superior izquierda
    val width: Int?, // Ancho en mm (opcional según el tipo)
    val height: Int?, // Alto en mm (opcional según el tipo)
    val rotation: Int?, // Rotación en grados (0, 90, 180, 270)
    
    // Configuración específica para elementos de texto
    val text: String?, // Texto estático o plantilla con marcadores ({product_name}, {product_price}, etc.)
    val fontSize: Float?, // Tamaño de fuente en puntos
    val fontName: String?, // Nombre de la fuente
    val fontStyle: String?, // "normal", "bold", "italic", "bold_italic"
    val alignment: String?, // "left", "center", "right"
    
    // Configuración específica para códigos de barras y QR
    val barcodeType: String?, // "CODE_128", "EAN_13", "EAN_8", "UPC_A", "QR_CODE", etc.
    val barcodeContent: String?, // Contenido estático o marcador ({product_barcode}, {product_sku}, etc.)
    val showText: Boolean?, // Mostrar texto debajo del código de barras
    
    // Configuración específica para líneas y rectángulos
    val borderWidth: Float?, // Ancho del borde en puntos
    val fillColor: String?, // Color de relleno en formato hexadecimal (#RRGGBB)
    val borderColor: String?, // Color del borde en formato hexadecimal (#RRGGBB)
    
    // Configuración específica para imágenes
    val imageUrl: String?, // URL o ruta a la imagen
    val preserveAspectRatio: Boolean? // Mantener proporción original de la imagen
) {
    /**
     * Evalúa el contenido del elemento reemplazando marcadores con datos del producto.
     *
     * @param product Producto cuyos datos se usarán para reemplazar marcadores.
     * @return Contenido evaluado con los datos del producto.
     */
    fun evaluateContent(product: Product): String {
        // Determinar qué contenido evaluar según el tipo de elemento
        val templateContent = when (type.lowercase()) {
            "text" -> text ?: ""
            "barcode", "qrcode" -> barcodeContent ?: ""
            else -> ""
        }
        
        // Si no hay contenido o no contiene marcadores, devolverlo tal cual
        if (templateContent.isBlank() || !templateContent.contains("{")) {
            return templateContent
        }
        
        // Reemplazar marcadores con datos del producto
        return templateContent
            .replace("{product_name}", product.name)
            .replace("{product_id}", product.id.toString())
            .replace("{product_sku}", product.sku ?: "")
            .replace("{product_barcode}", product.barcode ?: "")
            .replace("{product_price}", product.price?.toString() ?: "")
            .replace("{product_category}", product.category ?: "")
            .replace("{product_brand}", product.brand ?: "")
            .replace("{product_stock}", product.stock.toString())
            .replace("{current_date}", java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date()))
            .replace("{expiry_date}", product.expiryDate?.let { 
                java.text.SimpleDateFormat("dd/MM/yyyy").format(it) 
            } ?: "")
    }
}
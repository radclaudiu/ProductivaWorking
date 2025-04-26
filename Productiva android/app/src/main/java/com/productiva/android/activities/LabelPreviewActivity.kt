package com.productiva.android.activities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.model.LabelTemplate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.EnumMap
import java.util.Locale

/**
 * Actividad para mostrar la vista previa de una plantilla de etiqueta
 */
class LabelPreviewActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var imageViewPreview: ImageView
    private lateinit var buttonPrint: Button
    
    private lateinit var app: ProductivaApplication
    private var template: LabelTemplate? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_preview)
        
        // Obtener la aplicación
        app = application as ProductivaApplication
        
        // Obtener la plantilla pasada
        template = intent.getParcelableExtra("template_preview")
        
        if (template == null) {
            Toast.makeText(this, "Error: No se pudo cargar la plantilla", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar)
        imageViewPreview = findViewById(R.id.imageViewPreview)
        buttonPrint = findViewById(R.id.buttonPrint)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Vista previa de etiqueta"
        
        // Configurar botones
        buttonPrint.setOnClickListener {
            printTestLabel()
        }
        
        // Generar y mostrar vista previa
        generatePreview()
    }
    
    /**
     * Genera la vista previa de la etiqueta
     */
    private fun generatePreview() {
        val template = template ?: return
        
        // Crear un bitmap con el tamaño de la etiqueta (escalado para la pantalla)
        val scale = 4 // Factor de escala para mejor visualización
        val width = template.widthMm * scale
        val height = template.heightMm * scale
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fondo blanco
        canvas.drawColor(Color.WHITE)
        
        // Configuración del borde
        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        
        // Dibujar borde
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), borderPaint)
        
        // Configuración de texto
        val textPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        // Calcular márgenes
        val marginLeft = template.marginLeft * scale
        val marginTop = template.marginTop * scale
        val marginRight = template.marginRight * scale
        val marginBottom = template.marginBottom * scale
        
        var currentY = marginTop.toFloat()
        
        // Título
        if (template.showTitle) {
            textPaint.textSize = getFontSizeForScreenByLevel(template.titleFontSize)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            
            val title = "TITULO DE MUESTRA"
            val titleHeight = drawTextWithWrap(canvas, title, marginLeft.toFloat(), currentY, width - marginLeft - marginRight, textPaint)
            currentY += titleHeight + 10f
        }
        
        // Texto adicional
        if (template.showExtraText) {
            textPaint.textSize = getFontSizeForScreenByLevel(template.extraTextFontSize)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            
            val extraText = "Texto adicional de ejemplo para la etiqueta"
            val extraTextHeight = drawTextWithWrap(canvas, extraText, marginLeft.toFloat(), currentY, width - marginLeft - marginRight, textPaint)
            currentY += extraTextHeight + 10f
        }
        
        // Fecha
        if (template.showDate) {
            textPaint.textSize = getFontSizeForScreenByLevel(template.dateFontSize)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            
            val date = SimpleDateFormat(template.dateFormat, Locale.getDefault()).format(Date())
            val dateText = "Fecha: $date"
            val dateHeight = drawTextWithWrap(canvas, dateText, marginLeft.toFloat(), currentY, width - marginLeft - marginRight, textPaint)
            currentY += dateHeight + 15f
        }
        
        // Código QR
        if (template.showQrCode) {
            val qrSize = (width - marginLeft - marginRight).coerceAtMost(height / 3)
            val qrCode = generateQRCodeBitmap("EJEMPLO QR", qrSize)
            
            if (qrCode != null) {
                // Centrar horizontalmente
                val qrLeft = marginLeft + (width - marginLeft - marginRight - qrSize) / 2
                
                canvas.drawBitmap(qrCode, qrLeft.toFloat(), currentY, null)
                currentY += qrSize + 10f
            }
        }
        
        // Código de barras
        if (template.showBarcode) {
            // Simular código de barras (simplificado para vista previa)
            val barcodeHeight = 40f
            val barcodeWidth = width - marginLeft - marginRight
            val barcodePaint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }
            
            // Dibujar serie de barras verticales
            var x = marginLeft.toFloat()
            val endX = x + barcodeWidth
            val step = 5f
            
            while (x < endX) {
                val barWidth = (Math.random() * 4 + 1).toFloat()
                if (Math.random() > 0.4) { // 60% de probabilidad de dibujar una barra
                    canvas.drawRect(x, currentY, x + barWidth, currentY + barcodeHeight, barcodePaint)
                }
                x += barWidth + step
            }
            
            // Añadir texto de ejemplo bajo el código
            textPaint.textSize = 14f
            textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText("0123456789", marginLeft.toFloat(), currentY + barcodeHeight + 15, textPaint)
            
            currentY += barcodeHeight + 30f
        }
        
        // Mostrar vista previa
        imageViewPreview.setImageBitmap(bitmap)
    }
    
    /**
     * Dibuja texto con ajuste de línea
     * 
     * @return Altura total del texto dibujado
     */
    private fun drawTextWithWrap(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Int, paint: Paint): Float {
        val textWidth = paint.measureText(text)
        
        // Si el texto cabe en una línea, dibujarlo directamente
        if (textWidth <= maxWidth) {
            canvas.drawText(text, x, y + paint.textSize, paint)
            return paint.textSize
        }
        
        // Si no, dividir en múltiples líneas
        val words = text.split(" ")
        var currentLine = words[0]
        var currentY = y + paint.textSize
        var maxHeight = paint.textSize
        
        for (i in 1 until words.size) {
            val word = words[i]
            val testLine = "$currentLine $word"
            val testWidth = paint.measureText(testLine)
            
            if (testWidth <= maxWidth) {
                currentLine = testLine
            } else {
                canvas.drawText(currentLine, x, currentY, paint)
                currentY += paint.textSize * 1.2f // Espacio entre líneas
                maxHeight += paint.textSize * 1.2f
                currentLine = word
            }
        }
        
        // Dibujar última línea
        canvas.drawText(currentLine, x, currentY, paint)
        
        return maxHeight
    }
    
    /**
     * Obtiene el tamaño de fuente basado en el nivel (1-5)
     */
    private fun getFontSizeForScreenByLevel(level: Int): Float {
        return when (level) {
            1 -> 18f
            2 -> 22f
            3 -> 28f
            4 -> 36f
            5 -> 48f
            else -> 28f
        }
    }
    
    /**
     * Genera un bitmap de código QR
     */
    private fun generateQRCodeBitmap(content: String, size: Int): Bitmap? {
        try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Imprime una etiqueta de prueba usando la plantilla actual
     */
    private fun printTestLabel() {
        Toast.makeText(this, "Función disponible en impresoras reales", Toast.LENGTH_SHORT).show()
        
        // En una implementación real, aquí se enviarían los datos a la impresora
        // utilizando BluetoothPrinterManager y la impresora previamente configurada
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
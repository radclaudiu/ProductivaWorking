package com.productiva.android.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.ByteArrayOutputStream

/**
 * Vista personalizada para capturar firmas dibujadas con el dedo.
 * Permite al usuario dibujar una firma en la pantalla y exportarla como una imagen.
 */
class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val path = Path()
    private var lastX = 0f
    private var lastY = 0f
    private var hasSignature = false

    // Bitmap donde se dibujará la firma
    private var signatureBitmap: Bitmap? = null
    private var signatureCanvas: Canvas? = null

    // Este método se llama cuando cambia el tamaño de la vista
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Crear un nuevo bitmap con el tamaño actual de la vista
        signatureBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        signatureCanvas = Canvas(signatureBitmap!!)
    }

    // Este método dibuja el contenido de la vista
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Dibujar el bitmap de la firma
        signatureBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        
        // Dibujar el trazo actual
        canvas.drawPath(path, paint)
    }

    // Este método maneja los eventos táctiles
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Comenzar un nuevo trazo
                path.moveTo(x, y)
                lastX = x
                lastY = y
                hasSignature = true
                return true
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                // Dibujar una línea desde el último punto al punto actual
                path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                lastX = x
                lastY = y
                
                // Dibujar el trazo en el canvas del bitmap
                signatureCanvas?.drawPath(path, paint)
                
                // Si es ACTION_UP, reiniciar el path
                if (event.action == MotionEvent.ACTION_UP) {
                    path.reset()
                }
                
                // Volver a dibujar la vista
                invalidate()
                return true
            }
            else -> return false
        }
    }

    /**
     * Limpia la firma actual.
     */
    fun clear() {
        hasSignature = false
        path.reset()
        signatureBitmap?.eraseColor(Color.TRANSPARENT)
        invalidate()
    }

    /**
     * Verifica si hay una firma dibujada.
     */
    fun hasSignature(): Boolean {
        return hasSignature
    }

    /**
     * Obtiene la firma como un bitmap.
     */
    fun getSignatureBitmap(): Bitmap? {
        return signatureBitmap
    }

    /**
     * Obtiene la firma como un array de bytes en formato PNG.
     */
    fun getSignatureBytes(): ByteArray? {
        val bitmap = signatureBitmap ?: return null
        
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        
        return outputStream.toByteArray()
    }

    /**
     * Establece el color del trazo.
     */
    fun setStrokeColor(color: Int) {
        paint.color = color
    }

    /**
     * Establece el grosor del trazo.
     */
    fun setStrokeWidth(width: Float) {
        paint.strokeWidth = width
    }
}
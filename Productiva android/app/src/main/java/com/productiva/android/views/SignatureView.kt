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

/**
 * Vista personalizada para captura de firmas.
 */
class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    // Path para dibujar la firma
    private val path = Path()
    
    // Paint para la firma
    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 5f
        isAntiAlias = true
    }
    
    // Coordenadas del último punto tocado
    private var lastX = 0f
    private var lastY = 0f
    
    // Bitmap para guardar la firma
    private var signatureBitmap: Bitmap? = null
    private var canvas: Canvas? = null
    
    // Flag para saber si hay firma
    private var hasSignature = false
    
    /**
     * Cambia el color de la firma.
     */
    fun setSignatureColor(color: Int) {
        paint.color = color
    }
    
    /**
     * Cambia el grosor de la línea de la firma.
     */
    fun setStrokeWidth(width: Float) {
        paint.strokeWidth = width
    }
    
    /**
     * Limpia la firma.
     */
    fun clear() {
        path.reset()
        hasSignature = false
        
        // Recrear el bitmap si es necesario
        if (width > 0 && height > 0) {
            createBitmap()
        }
        
        invalidate()
    }
    
    /**
     * Verifica si hay una firma.
     */
    fun hasSignature(): Boolean {
        return hasSignature
    }
    
    /**
     * Obtiene la firma como bitmap.
     */
    fun getSignatureBitmap(): Bitmap? {
        if (!hasSignature) {
            return null
        }
        return signatureBitmap
    }
    
    /**
     * Crea el bitmap para dibujar.
     */
    private fun createBitmap() {
        signatureBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(signatureBitmap!!)
        canvas?.drawColor(Color.WHITE)
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Crear bitmap cuando la vista cambia de tamaño
        createBitmap()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Dibujar el fondo
        canvas.drawColor(Color.WHITE)
        
        // Dibujar la firma
        canvas.drawPath(path, paint)
        
        // Guardar en el bitmap
        this.canvas?.drawPath(path, paint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                lastX = x
                lastY = y
                hasSignature = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // Calcular la distancia entre puntos
                val dx = Math.abs(x - lastX)
                val dy = Math.abs(y - lastY)
                
                // Si la distancia es significativa, dibujar una línea
                if (dx >= 3 || dy >= 3) {
                    path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                    lastX = x
                    lastY = y
                }
                
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Finalizar la línea actual
                path.lineTo(x, y)
                invalidate()
                return true
            }
            else -> return false
        }
    }
}
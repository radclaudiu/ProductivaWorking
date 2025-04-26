package com.productiva.android.view

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
 * Vista personalizada para capturar firmas
 */
class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 12f
    }
    
    private val path = Path()
    private var latestX = 0f
    private var latestY = 0f
    
    // Para determinar si la vista está vacía o no
    private var hasSignature = false
    
    /**
     * Propiedad para verificar si la firma está vacía
     */
    val isEmpty: Boolean
        get() = !hasSignature
    
    init {
        // Establecer un fondo blanco para la vista
        setBackgroundColor(Color.WHITE)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                latestX = x
                latestY = y
                hasSignature = true
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(x - latestX)
                val dy = Math.abs(y - latestY)
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    path.quadTo(latestX, latestY, (x + latestX) / 2, (y + latestY) / 2)
                    latestX = x
                    latestY = y
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                path.lineTo(latestX, latestY)
                invalidate()
            }
        }
        return true
    }
    
    /**
     * Limpia la firma
     */
    fun clear() {
        path.reset()
        hasSignature = false
        invalidate()
    }
    
    /**
     * Obtiene un bitmap de la firma
     */
    fun getSignatureBitmap(): Bitmap? {
        if (isEmpty) {
            return null
        }
        
        val signatureBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(signatureBitmap)
        canvas.drawColor(Color.WHITE)
        draw(canvas)
        return signatureBitmap
    }
    
    companion object {
        private const val TOUCH_TOLERANCE = 4f
    }
}
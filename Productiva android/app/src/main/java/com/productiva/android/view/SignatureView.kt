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
 * Vista personalizada para capturar la firma del usuario
 */
class SignatureView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    
    // Configuración del pincel para dibujar
    private val paint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 8f
    }
    
    // Path para almacenar los trazos de la firma
    private val path = Path()
    
    // Coordenadas del último punto tocado
    private var lastX = 0f
    private var lastY = 0f
    
    // Tolerancia para el movimiento (evita dibujar puntos muy cercanos)
    private val touchTolerance = 4f
    
    /**
     * Limpia la firma (borra todos los trazos)
     */
    fun clear() {
        path.reset()
        invalidate()
    }
    
    /**
     * Verifica si la firma está vacía (sin trazos)
     */
    fun isEmpty(): Boolean {
        return path.isEmpty
    }
    
    /**
     * Obtiene un bitmap con la firma actual
     */
    fun getSignatureBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawPath(path, paint)
        return bitmap
    }
    
    /**
     * Dibuja el path de la firma en el canvas
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }
    
    /**
     * Maneja los eventos de toque para dibujar la firma
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touch_start(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touch_move(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touch_up()
                invalidate()
            }
            else -> return false
        }
        return true
    }
    
    /**
     * Inicia un nuevo trazo en las coordenadas especificadas
     */
    private fun touch_start(x: Float, y: Float) {
        path.moveTo(x, y)
        lastX = x
        lastY = y
    }
    
    /**
     * Continúa el trazo actual hasta las nuevas coordenadas
     * Usa quadTo para crear curvas suaves entre puntos
     */
    private fun touch_move(x: Float, y: Float) {
        val dx = Math.abs(x - lastX)
        val dy = Math.abs(y - lastY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // Curva cuadrática para suavizar la línea
            path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
            lastX = x
            lastY = y
        }
    }
    
    /**
     * Finaliza el trazo actual
     */
    private fun touch_up() {
        // Completa el trazo conectando al último punto
        path.lineTo(lastX, lastY)
    }
}
package com.productiva.android.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Vista personalizada para capturar una firma
 */
class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    // Constantes para la configuración del pincel
    private val STROKE_WIDTH = 6f
    private val STROKE_COLOR = Color.BLACK
    private val BACKGROUND_COLOR = Color.WHITE
    
    // Propiedades de dibujo
    private val paint = Paint().apply {
        color = STROKE_COLOR
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    
    private val backgroundPaint = Paint().apply {
        color = BACKGROUND_COLOR
        style = Paint.Style.FILL
    }
    
    private val path = Path()
    private var lastX = 0f
    private var lastY = 0f
    
    // Lista de trazos para almacenar el historial de dibujo
    private val paths = mutableListOf<PathData>()
    
    // Indica si la vista está vacía
    var isEmpty = true
        private set
    
    // Interfaz para notificar cambios en la firma
    private var onSignatureChangeListener: (() -> Unit)? = null
    
    init {
        // Asegurar que la vista es focusable y puede recibir eventos táctiles
        isFocusable = true
        isFocusableInTouchMode = true
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Dibujar fondo
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Dibujar trazos guardados
        for (pathData in paths) {
            canvas.drawPath(pathData.path, pathData.paint)
        }
        
        // Dibujar trazo actual
        canvas.drawPath(path, paint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Iniciar nuevo trazo
                path.moveTo(x, y)
                lastX = x
                lastY = y
                isEmpty = false
                onSignatureChangeListener?.invoke()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // Continuar trazo actual
                val dx = Math.abs(x - lastX)
                val dy = Math.abs(y - lastY)
                
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    // Usar curva de Bezier para suavizar el trazo
                    path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                    lastX = x
                    lastY = y
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Finalizar trazo actual
                path.lineTo(lastX, lastY)
                
                // Guardar el trazo actual en la lista de trazos
                val pathCopy = Path()
                pathCopy.set(path)
                val paintCopy = Paint(paint)
                paths.add(PathData(pathCopy, paintCopy))
                
                // Reiniciar el trazo actual
                path.reset()
                
                invalidate()
                return true
            }
            else -> return false
        }
    }
    
    /**
     * Limpia la firma
     */
    fun clear() {
        path.reset()
        paths.clear()
        isEmpty = true
        invalidate()
        onSignatureChangeListener?.invoke()
    }
    
    /**
     * Configura un listener para cambios en la firma
     */
    fun setOnSignatureChangeListener(listener: () -> Unit) {
        onSignatureChangeListener = listener
    }
    
    /**
     * Clase interna para almacenar un trazo y su pincel
     */
    private data class PathData(
        val path: Path,
        val paint: Paint
    )
    
    companion object {
        private const val TOUCH_TOLERANCE = 4f
    }
}
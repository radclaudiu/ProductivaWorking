package com.productiva.android.views

import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import com.productiva.android.R
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

/**
 * Vista personalizada para capturar firmas mediante gestos táctiles
 */
class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Ruta del archivo de firma guardado
    private var filePath: String? = null

    // Configuración del camino de la firma
    private val signaturePaint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }

    // Camino para almacenar los trazos
    private val path = Path()

    // Coordenadas para dibujar
    private var currentX = 0f
    private var currentY = 0f

    // Umbral de movimiento para considerar un toque válido
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    // Bitmap donde se dibuja
    private var signalBitmap = Bitmap.createBitmap(
        1, 1, Bitmap.Config.ARGB_8888
    )

    // Canvas sobre el que se dibuja
    private val canvas = Canvas()

    // Rectángulo para dibujar
    private val frame = RectF()

    init {
        // Inicialización de atributos personalizados si se necesitan
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.SignatureView, defStyleAttr, 0
        )

        // Aquí se pueden obtener atributos personalizados
        // Por ejemplo: lineColor = a.getColor(R.styleable.SignatureView_lineColor, Color.BLACK)

        a.recycle()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val newCanvas = Canvas(newBitmap)
        newCanvas.drawBitmap(signalBitmap, 0f, 0f, null)

        // Reciclar el bitmap anterior si existe
        if (signalBitmap != newBitmap) {
            signalBitmap.recycle()
        }

        canvas.setBitmap(newBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(ResourcesCompat.getColor(resources, R.color.colorSignatureBackground, null))
        canvas.drawPath(path, signaturePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    private fun touchStart(x: Float, y: Float) {
        path.reset()
        path.moveTo(x, y)
        currentX = x
        currentY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = abs(x - currentX)
        val dy = abs(y - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // Creamos una curva Bezier desde el último punto hasta el actual
            path.quadTo(
                currentX, currentY,
                (x + currentX) / 2, (y + currentY) / 2
            )
            // Actualizamos las coordenadas actuales
            currentX = x
            currentY = y
        }
    }

    private fun touchUp() {
        // Completamos el trazo
        path.lineTo(currentX, currentY)
        // Dibujamos el trazo en el canvas
        canvas.drawPath(path, signaturePaint)
        // Resetamos el path para el próximo trazo
        path.reset()
    }

    /**
     * Limpia la vista de firma
     */
    fun clear() {
        path.reset()
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    /**
     * Guarda la firma actual como una imagen PNG
     * @return Ruta del archivo guardado o null si ocurrió un error
     */
    fun save(): String? {
        val fileName = "firma_${System.currentTimeMillis()}.png"
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(directory, fileName)

        try {
            FileOutputStream(file).use { out ->
                // Crear un bitmap con fondo blanco para la firma
                val signatureBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val signatureCanvas = Canvas(signatureBitmap)
                signatureCanvas.drawColor(Color.WHITE)
                draw(signatureCanvas)

                signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                signatureBitmap.recycle()
            }
            filePath = file.absolutePath
            return filePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Verifica si la vista contiene una firma
     * @return true si la vista contiene una firma
     */
    fun hasSignature(): Boolean {
        // Si el bitmap es completamente transparente, no hay firma
        val emptyBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val emptyCanvas = Canvas(emptyBitmap)
        emptyCanvas.drawColor(Color.TRANSPARENT)

        val signalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val signalCanvas = Canvas(signalBitmap)
        draw(signalCanvas)

        val result = !signalBitmap.sameAs(emptyBitmap)

        emptyBitmap.recycle()
        signalBitmap.recycle()

        return result
    }

    /**
     * Obtiene la ruta del archivo de la firma guardada
     */
    fun getSignaturePath(): String? {
        return filePath
    }

    companion object {
        private const val STROKE_WIDTH = 12f
    }
}
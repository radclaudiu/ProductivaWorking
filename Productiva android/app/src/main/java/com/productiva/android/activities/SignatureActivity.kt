package com.productiva.android.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.productiva.android.R
import com.productiva.android.views.SignatureView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Actividad para capturar una firma
 */
class SignatureActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var signatureView: SignatureView
    private lateinit var buttonClear: Button
    private lateinit var buttonSave: Button
    private lateinit var textViewInstructions: TextView
    
    private var filePath: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature)
        
        // Obtener ruta del archivo para guardar la firma
        filePath = intent.getStringExtra("file_path")
        
        if (filePath == null) {
            Toast.makeText(this, "Error: No se pudo inicializar la captura de firma", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar)
        signatureView = findViewById(R.id.signatureView)
        buttonClear = findViewById(R.id.buttonClear)
        buttonSave = findViewById(R.id.buttonSave)
        textViewInstructions = findViewById(R.id.textViewInstructions)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Capturar firma"
        
        // Configurar listeners
        buttonClear.setOnClickListener {
            signatureView.clear()
            updateSaveButton()
        }
        
        buttonSave.setOnClickListener {
            if (signatureView.isEmpty) {
                Toast.makeText(this, "Por favor, firme antes de guardar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            saveSignature()
        }
        
        // Configurar listener para la vista de firma
        signatureView.setOnSignatureChangeListener {
            updateSaveButton()
        }
        
        // Estado inicial
        updateSaveButton()
    }
    
    /**
     * Actualiza el estado del botón de guardar
     */
    private fun updateSaveButton() {
        buttonSave.isEnabled = !signatureView.isEmpty
    }
    
    /**
     * Guarda la firma en el archivo especificado
     */
    private fun saveSignature() {
        // Convertir la vista a bitmap
        val bitmap = getBitmapFromView(signatureView)
        
        try {
            val file = File(filePath!!)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
            }
            
            // Mostrar mensaje de éxito
            Toast.makeText(this, "Firma guardada correctamente", Toast.LENGTH_SHORT).show()
            
            // Devolver resultado y cerrar actividad
            setResult(RESULT_OK)
            finish()
        } catch (e: IOException) {
            Toast.makeText(this, "Error al guardar la firma: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Convierte una vista a bitmap
     */
    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Confirmar si queremos descartar la firma
                if (!signatureView.isEmpty) {
                    // Mostrar diálogo de confirmación
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Descartar firma")
                        .setMessage("¿Estás seguro que deseas descartar la firma?")
                        .setPositiveButton("Descartar") { _, _ ->
                            setResult(RESULT_CANCELED)
                            finish()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                    true
                } else {
                    // Si no hay firma, simplemente cerrar
                    setResult(RESULT_CANCELED)
                    finish()
                    true
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onBackPressed() {
        // Usar el mismo comportamiento que el botón de navegación hacia atrás
        onOptionsItemSelected(
            MenuItem.BUILDER.invoke { setItemId(android.R.id.home) }.build()
        )
    }
}
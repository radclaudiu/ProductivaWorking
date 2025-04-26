package com.productiva.android.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.gcacace.signaturepad.views.SignaturePad
import com.productiva.android.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Actividad para capturar firma
 */
class SignatureCaptureActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var signaturePad: SignaturePad
    private lateinit var clearButton: Button
    private lateinit var saveButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature_capture)
        
        // Inicializar views
        toolbar = findViewById(R.id.toolbar)
        signaturePad = findViewById(R.id.signaturePad)
        clearButton = findViewById(R.id.buttonClear)
        saveButton = findViewById(R.id.buttonSave)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Capturar Firma"
        
        // Configurar SignaturePad
        signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                // No hacemos nada aquí
            }
            
            override fun onSigned() {
                saveButton.isEnabled = true
                clearButton.isEnabled = true
            }
            
            override fun onClear() {
                saveButton.isEnabled = false
                clearButton.isEnabled = false
            }
        })
        
        // Configurar botones
        clearButton.setOnClickListener {
            signaturePad.clear()
        }
        
        saveButton.setOnClickListener {
            if (signaturePad.isEmpty) {
                Toast.makeText(this, "Por favor, firme antes de guardar", Toast.LENGTH_SHORT).show()
            } else {
                val signatureBitmap = signaturePad.signatureBitmap
                val path = saveSignature(signatureBitmap)
                
                if (path != null) {
                    val resultIntent = Intent()
                    resultIntent.putExtra("signature_path", path)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar la firma", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Inicialmente deshabilitar botones
        saveButton.isEnabled = false
        clearButton.isEnabled = false
    }
    
    /**
     * Guarda la firma como imagen PNG
     */
    private fun saveSignature(signature: Bitmap): String? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(
            "SIGNATURE_${timeStamp}_",
            ".png",
            storageDir
        )
        
        try {
            // Crear un canvas para dibujar la firma con fondo blanco
            val signatureWithBackground = Bitmap.createBitmap(
                signature.width,
                signature.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(signatureWithBackground)
            canvas.drawColor(android.graphics.Color.WHITE)
            canvas.drawBitmap(signature, 0f, 0f, null)
            
            // Guardar la imagen
            FileOutputStream(imageFile).use { out ->
                signatureWithBackground.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            return imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
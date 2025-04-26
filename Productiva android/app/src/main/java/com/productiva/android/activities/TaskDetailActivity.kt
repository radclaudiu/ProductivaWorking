package com.productiva.android.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Actividad para ver y completar una tarea
 */
class TaskDetailActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var frequencyTextView: TextView
    private lateinit var notesEditText: EditText
    private lateinit var photoImageView: ImageView
    private lateinit var signatureImageView: ImageView
    private lateinit var addPhotoButton: Button
    private lateinit var captureSignatureButton: Button
    private lateinit var completeTaskButton: Button
    private lateinit var printLabelButton: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    
    private val taskRepository by lazy {
        (application as ProductivaApplication).taskRepository
    }
    
    private val sessionManager by lazy {
        (application as ProductivaApplication).sessionManager
    }
    
    private var taskId: Int = -1
    private var currentPhotoPath: String? = null
    private var currentSignaturePath: String? = null
    
    // Lanzadores para resultados de actividades
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoImageView.visibility = View.VISIBLE
            Glide.with(this)
                .load(currentPhotoPath)
                .centerCrop()
                .into(photoImageView)
        }
    }
    
    private val captureSignatureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val signaturePath = result.data?.getStringExtra("signature_path")
            if (!signaturePath.isNullOrEmpty()) {
                currentSignaturePath = signaturePath
                signatureImageView.visibility = View.VISIBLE
                Glide.with(this)
                    .load(signaturePath)
                    .centerCrop()
                    .into(signatureImageView)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        
        // Obtener ID de tarea
        taskId = intent.getIntExtra("task_id", -1)
        if (taskId == -1) {
            Toast.makeText(this, "Error: Tarea no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Inicializar views
        toolbar = findViewById(R.id.toolbar)
        titleTextView = findViewById(R.id.textViewTaskTitle)
        descriptionTextView = findViewById(R.id.textViewTaskDescription)
        frequencyTextView = findViewById(R.id.textViewTaskFrequency)
        notesEditText = findViewById(R.id.editTextNotes)
        photoImageView = findViewById(R.id.imageViewPhoto)
        signatureImageView = findViewById(R.id.imageViewSignature)
        addPhotoButton = findViewById(R.id.buttonAddPhoto)
        captureSignatureButton = findViewById(R.id.buttonCaptureSignature)
        completeTaskButton = findViewById(R.id.buttonCompleteTask)
        printLabelButton = findViewById(R.id.fabPrintLabel)
        progressBar = findViewById(R.id.progressBar)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalle de Tarea"
        
        // Cargar detalles de la tarea
        val taskDao = (application as ProductivaApplication).database.taskDao()
        taskDao.getTaskById(taskId).observe(this) { task ->
            if (task != null) {
                titleTextView.text = task.title
                descriptionTextView.text = task.description ?: "Sin descripción"
                frequencyTextView.text = formatFrequency(task.frequency)
                
                // Mostrar/ocultar botones según la tarea
                addPhotoButton.visibility = if (task.needsPhoto) View.VISIBLE else View.GONE
                captureSignatureButton.visibility = if (task.needsSignature) View.VISIBLE else View.GONE
                printLabelButton.visibility = if (task.printLabel) View.VISIBLE else View.GONE
            } else {
                Toast.makeText(this, "Error: Tarea no encontrada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        
        // Configurar botones
        addPhotoButton.setOnClickListener {
            checkCameraPermission()
        }
        
        captureSignatureButton.setOnClickListener {
            captureSignature()
        }
        
        printLabelButton.setOnClickListener {
            printLabel()
        }
        
        completeTaskButton.setOnClickListener {
            completeTask()
        }
    }
    
    /**
     * Formatea la frecuencia de la tarea para mostrarla
     */
    private fun formatFrequency(frequency: String): String {
        return when (frequency.lowercase()) {
            "daily" -> "Diaria"
            "weekly" -> "Semanal"
            "monthly" -> "Mensual"
            "work_days" -> "Días laborables"
            "weekends" -> "Fines de semana"
            else -> frequency.replaceFirstChar { it.uppercase() }
        }
    }
    
    /**
     * Verifica el permiso de cámara antes de tomar una foto
     */
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            takePhoto()
        }
    }
    
    /**
     * Lanza la cámara para tomar una foto
     */
    private fun takePhoto() {
        val photoFile = createImageFile()
        currentPhotoPath = photoFile.absolutePath
        
        val photoURI = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }
        
        takePictureLauncher.launch(takePictureIntent)
    }
    
    /**
     * Crea un archivo temporal para la foto
     */
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
    
    /**
     * Lanza la actividad para capturar firma
     */
    private fun captureSignature() {
        val intent = Intent(this, SignatureCaptureActivity::class.java)
        captureSignatureLauncher.launch(intent)
    }
    
    /**
     * Imprime una etiqueta para la tarea
     */
    private fun printLabel() {
        val intent = Intent(this, PrintLabelActivity::class.java)
        intent.putExtra("task_id", taskId)
        startActivity(intent)
    }
    
    /**
     * Completa la tarea actual
     */
    private fun completeTask() {
        // Obtener datos necesarios
        val userId = sessionManager.getSelectedUserId()
        val locationId = sessionManager.getLocationId()
        val token = sessionManager.getAuthToken()
        val notes = notesEditText.text.toString()
        
        if (userId == -1 || locationId == -1 || token == null) {
            Toast.makeText(this, "Error: Faltan datos de sesión", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Mostrar progreso
        setLoading(true)
        
        // Completar tarea
        lifecycleScope.launch {
            val result = taskRepository.completeTask(
                token = token,
                taskId = taskId,
                userId = userId,
                locationId = locationId,
                notes = if (notes.isNotEmpty()) notes else null,
                signatureFilePath = currentSignaturePath,
                photoFilePath = currentPhotoPath
            )
            
            runOnUiThread {
                setLoading(false)
                
                if (result.isSuccess) {
                    Toast.makeText(this@TaskDetailActivity, "Tarea completada correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error al completar la tarea"
                    Toast.makeText(this@TaskDetailActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * Actualiza la UI para mostrar/ocultar indicadores de carga
     */
    private fun setLoading(loading: Boolean) {
        if (loading) {
            progressBar.visibility = View.VISIBLE
            completeTaskButton.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            completeTaskButton.isEnabled = true
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
    }
}
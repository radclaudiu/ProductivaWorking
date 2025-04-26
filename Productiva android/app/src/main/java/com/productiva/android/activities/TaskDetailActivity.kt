package com.productiva.android.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
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
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.model.Task
import com.productiva.android.repository.TaskRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Actividad para ver detalles de una tarea y completarla
 */
class TaskDetailActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var textViewTaskTitle: TextView
    private lateinit var textViewTaskDescription: TextView
    private lateinit var textViewTaskFrequency: TextView
    private lateinit var imageViewPhoto: ImageView
    private lateinit var imageViewSignature: ImageView
    private lateinit var editTextNotes: EditText
    private lateinit var buttonTakePhoto: Button
    private lateinit var buttonCaptureFirma: Button
    private lateinit var buttonPrintLabel: Button
    private lateinit var buttonCompleteTask: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var taskRepository: TaskRepository
    private lateinit var app: ProductivaApplication
    
    private var currentTask: Task? = null
    private var photoFile: File? = null
    private var signatureFile: File? = null
    private var currentPhotoPath: String? = null
    private var currentSignaturePath: String? = null
    
    // Lanzadores de actividades para resultados
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // La foto se guardó en el archivo photoFile
            currentPhotoPath = photoFile?.absolutePath
            
            // Mostrar la imagen capturada
            photoFile?.let {
                if (it.exists()) {
                    val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                    imageViewPhoto.setImageBitmap(bitmap)
                    imageViewPhoto.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private val captureSignatureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // La firma se guardó en el archivo pasado a la actividad
            signatureFile?.let {
                if (it.exists()) {
                    currentSignaturePath = it.absolutePath
                    val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                    imageViewSignature.setImageBitmap(bitmap)
                    imageViewSignature.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private val printLabelLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { 
        // No necesitamos hacer nada con el resultado
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        
        // Obtener instancia de la aplicación
        app = application as ProductivaApplication
        
        // Inicializar el repositorio de tareas
        taskRepository = app.taskRepository
        
        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar)
        textViewTaskTitle = findViewById(R.id.textViewTaskTitle)
        textViewTaskDescription = findViewById(R.id.textViewTaskDescription)
        textViewTaskFrequency = findViewById(R.id.textViewTaskFrequency)
        imageViewPhoto = findViewById(R.id.imageViewPhoto)
        imageViewSignature = findViewById(R.id.imageViewSignature)
        editTextNotes = findViewById(R.id.editTextNotes)
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto)
        buttonCaptureFirma = findViewById(R.id.buttonCaptureFirma)
        buttonPrintLabel = findViewById(R.id.buttonPrintLabel)
        buttonCompleteTask = findViewById(R.id.buttonCompleteTask)
        progressBar = findViewById(R.id.progressBar)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalle de tarea"
        
        // Configurar listeners
        buttonTakePhoto.setOnClickListener {
            if (checkCameraPermission()) {
                dispatchTakePictureIntent()
            } else {
                requestCameraPermission()
            }
        }
        
        buttonCaptureFirma.setOnClickListener {
            captureSignature()
        }
        
        buttonPrintLabel.setOnClickListener {
            printLabel()
        }
        
        buttonCompleteTask.setOnClickListener {
            completeTask()
        }
        
        // Obtener tarea de la intent
        val taskId = intent.getIntExtra("task_id", -1)
        if (taskId != -1) {
            loadTask(taskId)
        } else {
            finish()
        }
    }
    
    /**
     * Carga la tarea de la base de datos local
     */
    private fun loadTask(taskId: Int) {
        val taskDao = app.database.taskDao()
        taskDao.getTaskById(taskId).observe(this) { task ->
            if (task != null) {
                currentTask = task
                updateUI(task)
            } else {
                Toast.makeText(this, "No se pudo cargar la tarea", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    /**
     * Actualiza la UI con los datos de la tarea
     */
    private fun updateUI(task: Task) {
        textViewTaskTitle.text = task.title
        textViewTaskDescription.text = task.description ?: "Sin descripción"
        textViewTaskFrequency.text = formatFrequency(task.frequency)
        
        // Configurar visibilidad de botones según los requisitos de la tarea
        buttonTakePhoto.visibility = if (task.needsPhoto) View.VISIBLE else View.GONE
        buttonCaptureFirma.visibility = if (task.needsSignature) View.VISIBLE else View.GONE
        buttonPrintLabel.visibility = if (task.printLabel) View.VISIBLE else View.GONE
    }
    
    /**
     * Formatea la frecuencia para mostrarla
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
     * Verifica el permiso de cámara
     */
    private fun checkCameraPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED)
    }
    
    /**
     * Solicita el permiso de cámara
     */
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }
    
    /**
     * Inicia la captura de foto
     */
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Asegurarse de que hay una actividad de cámara para manejar el intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Crear el archivo donde se guardará la foto
                val photoFile: File? = try {
                    createImageFile("PHOTO")
                } catch (ex: IOException) {
                    Log.e("TaskDetailActivity", "Error al crear archivo de imagen", ex)
                    null
                }
                
                // Continuar solo si el archivo fue creado exitosamente
                photoFile?.also {
                    this.photoFile = it
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.productiva.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureLauncher.launch(takePictureIntent)
                }
            }
        }
    }
    
    /**
     * Crea un archivo para guardar una imagen
     */
    @Throws(IOException::class)
    private fun createImageFile(prefix: String): File {
        // Crear un nombre de archivo único
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        
        return File.createTempFile(
            "${prefix}_${timeStamp}_",  /* prefijo */
            ".jpg",                      /* sufijo */
            storageDir                   /* directorio */
        )
    }
    
    /**
     * Inicia la captura de firma
     */
    private fun captureSignature() {
        try {
            // Crear archivo para la firma
            signatureFile = createImageFile("SIGNATURE")
            
            // Lanzar actividad para capturar firma
            val intent = Intent(this, SignatureActivity::class.java)
            intent.putExtra("file_path", signatureFile?.absolutePath)
            captureSignatureLauncher.launch(intent)
        } catch (e: IOException) {
            Log.e("TaskDetailActivity", "Error al crear archivo para firma", e)
            Toast.makeText(this, "Error al iniciar captura de firma", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Inicia la impresión de etiqueta
     */
    private fun printLabel() {
        val intent = Intent(this, PrintLabelActivity::class.java)
        intent.putExtra("task_id", currentTask?.id ?: -1)
        intent.putExtra("task_title", currentTask?.title)
        printLabelLauncher.launch(intent)
    }
    
    /**
     * Completa la tarea actual
     */
    private fun completeTask() {
        val task = currentTask ?: return
        
        // Validar requisitos
        if (task.needsPhoto && currentPhotoPath == null) {
            Toast.makeText(this, "Se requiere una foto para completar la tarea", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (task.needsSignature && currentSignaturePath == null) {
            Toast.makeText(this, "Se requiere una firma para completar la tarea", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Mostrar progreso
        setLoading(true)
        
        // Notas
        val notes = editTextNotes.text.toString().trim()
        
        lifecycleScope.launch {
            try {
                val result = taskRepository.completeTask(
                    taskId = task.id,
                    notes = notes,
                    photoPath = currentPhotoPath,
                    signaturePath = currentSignaturePath
                )
                
                if (result.isSuccess) {
                    // Intentar sincronizar inmediatamente
                    syncTaskCompletion()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Toast.makeText(applicationContext, error, Toast.LENGTH_LONG).show()
                    setLoading(false)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    applicationContext,
                    "Error al completar la tarea: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                setLoading(false)
            }
        }
    }
    
    /**
     * Sincroniza el completado de tarea con el servidor
     */
    private fun syncTaskCompletion() {
        val token = app.sessionManager.getAuthToken()
        
        if (token == null) {
            Toast.makeText(this, "Sesión expirada", Toast.LENGTH_SHORT).show()
            setLoading(false)
            return
        }
        
        lifecycleScope.launch {
            try {
                val result = taskRepository.syncPendingTaskCompletions(token)
                
                setLoading(false)
                
                if (result.isSuccess) {
                    // Mostrar diálogo de éxito
                    showSuccessDialog()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Toast.makeText(
                        applicationContext,
                        "Tarea completada sin conexión. Se sincronizará más tarde: $error",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Volver a la actividad anterior
                    finish()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(
                    applicationContext,
                    "Tarea completada sin conexión. Se sincronizará más tarde: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                
                // Volver a la actividad anterior
                finish()
            }
        }
    }
    
    /**
     * Muestra un diálogo de tarea completada con éxito
     */
    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("¡Tarea completada!")
            .setMessage("La tarea se ha completado correctamente.")
            .setPositiveButton("Aceptar") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Actualiza la UI para mostrar/ocultar indicadores de carga
     */
    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        buttonCompleteTask.isEnabled = !loading
        buttonTakePhoto.isEnabled = !loading
        buttonCaptureFirma.isEnabled = !loading
        buttonPrintLabel.isEnabled = !loading
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(
                    this,
                    "Se necesita permiso de cámara para capturar fotos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
    }
}
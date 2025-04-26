package com.productiva.android.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.model.LabelTemplate
import kotlinx.coroutines.launch

/**
 * Actividad para configurar plantillas de etiquetas
 */
class LabelTemplateActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var editTextTemplateName: EditText
    private lateinit var editTextWidth: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var editTextDateFormat: EditText
    private lateinit var switchShowTitle: Switch
    private lateinit var switchShowDate: Switch
    private lateinit var switchShowExtraText: Switch
    private lateinit var switchShowQrCode: Switch
    private lateinit var switchShowBarcode: Switch
    private lateinit var seekBarTitleSize: SeekBar
    private lateinit var textViewTitleSize: TextView
    private lateinit var seekBarDateSize: SeekBar
    private lateinit var textViewDateSize: TextView
    private lateinit var seekBarExtraTextSize: SeekBar
    private lateinit var textViewExtraTextSize: TextView
    private lateinit var editTextMarginTop: EditText
    private lateinit var editTextMarginLeft: EditText
    private lateinit var editTextMarginRight: EditText
    private lateinit var editTextMarginBottom: EditText
    private lateinit var switchDefaultTemplate: Switch
    private lateinit var buttonSave: Button
    private lateinit var buttonPreview: Button
    
    private lateinit var app: ProductivaApplication
    private var templateId: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_template)
        
        // Obtener ID de plantilla si existe
        templateId = intent.getIntExtra("template_id", 0)
        
        // Obtener la aplicación
        app = application as ProductivaApplication
        
        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar)
        editTextTemplateName = findViewById(R.id.editTextTemplateName)
        editTextWidth = findViewById(R.id.editTextWidth)
        editTextHeight = findViewById(R.id.editTextHeight)
        editTextDateFormat = findViewById(R.id.editTextDateFormat)
        switchShowTitle = findViewById(R.id.switchShowTitle)
        switchShowDate = findViewById(R.id.switchShowDate)
        switchShowExtraText = findViewById(R.id.switchShowExtraText)
        switchShowQrCode = findViewById(R.id.switchShowQrCode)
        switchShowBarcode = findViewById(R.id.switchShowBarcode)
        seekBarTitleSize = findViewById(R.id.seekBarTitleSize)
        textViewTitleSize = findViewById(R.id.textViewTitleSize)
        seekBarDateSize = findViewById(R.id.seekBarDateSize)
        textViewDateSize = findViewById(R.id.textViewDateSize)
        seekBarExtraTextSize = findViewById(R.id.seekBarExtraTextSize)
        textViewExtraTextSize = findViewById(R.id.textViewExtraTextSize)
        editTextMarginTop = findViewById(R.id.editTextMarginTop)
        editTextMarginLeft = findViewById(R.id.editTextMarginLeft)
        editTextMarginRight = findViewById(R.id.editTextMarginRight)
        editTextMarginBottom = findViewById(R.id.editTextMarginBottom)
        switchDefaultTemplate = findViewById(R.id.switchDefaultTemplate)
        buttonSave = findViewById(R.id.buttonSave)
        buttonPreview = findViewById(R.id.buttonPreview)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (templateId == 0) "Nueva plantilla" else "Editar plantilla"
        
        // Configurar seekbars (1-5)
        setupSeekBar(seekBarTitleSize, textViewTitleSize, "Tamaño del título")
        setupSeekBar(seekBarDateSize, textViewDateSize, "Tamaño de la fecha")
        setupSeekBar(seekBarExtraTextSize, textViewExtraTextSize, "Tamaño del texto adicional")
        
        // Configurar botones
        buttonSave.setOnClickListener {
            saveTemplate()
        }
        
        buttonPreview.setOnClickListener {
            previewTemplate()
        }
        
        // Cargar plantilla si estamos editando
        if (templateId > 0) {
            loadTemplate()
        } else {
            // Valores predeterminados para nueva plantilla
            editTextWidth.setText("62")
            editTextHeight.setText("100")
            editTextDateFormat.setText("dd/MM/yyyy HH:mm")
            editTextMarginTop.setText("3")
            editTextMarginLeft.setText("3")
            editTextMarginRight.setText("3")
            editTextMarginBottom.setText("3")
            
            seekBarTitleSize.progress = 4    // 4 de 5
            seekBarDateSize.progress = 2     // 2 de 5
            seekBarExtraTextSize.progress = 3 // 3 de 5
            
            switchShowTitle.isChecked = true
            switchShowDate.isChecked = true
            switchShowExtraText.isChecked = true
            switchShowQrCode.isChecked = false
            switchShowBarcode.isChecked = false
        }
    }
    
    /**
     * Configura un SeekBar para tamaños de fuente (1-5)
     */
    private fun setupSeekBar(seekBar: SeekBar, textView: TextView, labelText: String) {
        seekBar.max = 4  // 0-4 -> 1-5
        seekBar.progress = 2  // 3 por defecto
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = progress + 1  // Convertir a 1-5
                textView.text = "$labelText: $size"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // No se necesita implementación
            }
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // No se necesita implementación
            }
        })
        
        // Establecer texto inicial
        val initialSize = seekBar.progress + 1
        textView.text = "$labelText: $initialSize"
    }
    
    /**
     * Carga la plantilla para editarla
     */
    private fun loadTemplate() {
        lifecycleScope.launch {
            val template = app.database.labelTemplateDao().getLabelTemplateByIdSync(templateId)
            
            runOnUiThread {
                if (template != null) {
                    // Llenar datos
                    editTextTemplateName.setText(template.name)
                    editTextWidth.setText(template.widthMm.toString())
                    editTextHeight.setText(template.heightMm.toString())
                    editTextDateFormat.setText(template.dateFormat)
                    
                    switchShowTitle.isChecked = template.showTitle
                    switchShowDate.isChecked = template.showDate
                    switchShowExtraText.isChecked = template.showExtraText
                    switchShowQrCode.isChecked = template.showQrCode
                    switchShowBarcode.isChecked = template.showBarcode
                    
                    // Tamaños de fuente (1-5)
                    seekBarTitleSize.progress = template.titleFontSize - 1
                    seekBarDateSize.progress = template.dateFontSize - 1
                    seekBarExtraTextSize.progress = template.extraTextFontSize - 1
                    
                    // Márgenes
                    editTextMarginTop.setText(template.marginTop.toString())
                    editTextMarginLeft.setText(template.marginLeft.toString())
                    editTextMarginRight.setText(template.marginRight.toString())
                    editTextMarginBottom.setText(template.marginBottom.toString())
                    
                    // Plantilla predeterminada
                    switchDefaultTemplate.isChecked = template.isDefault
                } else {
                    Toast.makeText(
                        this@LabelTemplateActivity,
                        "Error: No se encontró la plantilla",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }
    
    /**
     * Guarda la plantilla de etiqueta
     */
    private fun saveTemplate() {
        try {
            // Validar datos
            val name = editTextTemplateName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "El nombre de la plantilla no puede estar vacío", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Leer valores de dimensiones
            val width = editTextWidth.text.toString().toInt()
            val height = editTextHeight.text.toString().toInt()
            val dateFormat = editTextDateFormat.text.toString().trim()
            
            if (width <= 0 || height <= 0) {
                Toast.makeText(this, "Las dimensiones deben ser mayores que cero", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (dateFormat.isEmpty()) {
                Toast.makeText(this, "El formato de fecha no puede estar vacío", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Leer valores de márgenes
            val marginTop = editTextMarginTop.text.toString().toInt()
            val marginLeft = editTextMarginLeft.text.toString().toInt()
            val marginRight = editTextMarginRight.text.toString().toInt()
            val marginBottom = editTextMarginBottom.text.toString().toInt()
            
            // Obtener valores de los switches
            val showTitle = switchShowTitle.isChecked
            val showDate = switchShowDate.isChecked
            val showExtraText = switchShowExtraText.isChecked
            val showQrCode = switchShowQrCode.isChecked
            val showBarcode = switchShowBarcode.isChecked
            val isDefault = switchDefaultTemplate.isChecked
            
            // Obtener tamaños de fuente (1-5)
            val titleFontSize = seekBarTitleSize.progress + 1
            val dateFontSize = seekBarDateSize.progress + 1
            val extraTextFontSize = seekBarExtraTextSize.progress + 1
            
            // Crear objeto de plantilla
            val template = LabelTemplate(
                id = templateId,
                name = name,
                isDefault = isDefault,
                widthMm = width,
                heightMm = height,
                showTitle = showTitle,
                showDate = showDate,
                showExtraText = showExtraText,
                showQrCode = showQrCode,
                showBarcode = showBarcode,
                titleFontSize = titleFontSize,
                dateFontSize = dateFontSize,
                extraTextFontSize = extraTextFontSize,
                marginTop = marginTop,
                marginLeft = marginLeft,
                marginRight = marginRight,
                marginBottom = marginBottom,
                dateFormat = dateFormat
            )
            
            // Guardar en la base de datos
            lifecycleScope.launch {
                val dao = app.database.labelTemplateDao()
                
                // Si esta plantilla se configura como predeterminada, actualizar todas las demás
                if (isDefault) {
                    dao.clearDefaultTemplates()
                }
                
                val id = dao.insertLabelTemplate(template)
                
                runOnUiThread {
                    Toast.makeText(
                        this@LabelTemplateActivity,
                        "Plantilla guardada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(
                this,
                "Por favor, ingrese valores numéricos válidos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Muestra una vista previa de la plantilla
     */
    private fun previewTemplate() {
        try {
            // Construir plantilla temporal con valores actuales
            val name = editTextTemplateName.text.toString().trim().ifEmpty { "Vista previa" }
            val width = editTextWidth.text.toString().toInt()
            val height = editTextHeight.text.toString().toInt()
            val dateFormat = editTextDateFormat.text.toString().trim().ifEmpty { "dd/MM/yyyy HH:mm" }
            
            val marginTop = editTextMarginTop.text.toString().toInt()
            val marginLeft = editTextMarginLeft.text.toString().toInt()
            val marginRight = editTextMarginRight.text.toString().toInt()
            val marginBottom = editTextMarginBottom.text.toString().toInt()
            
            val showTitle = switchShowTitle.isChecked
            val showDate = switchShowDate.isChecked
            val showExtraText = switchShowExtraText.isChecked
            val showQrCode = switchShowQrCode.isChecked
            val showBarcode = switchShowBarcode.isChecked
            
            val titleFontSize = seekBarTitleSize.progress + 1
            val dateFontSize = seekBarDateSize.progress + 1
            val extraTextFontSize = seekBarExtraTextSize.progress + 1
            
            val template = LabelTemplate(
                id = 0,
                name = name,
                isDefault = false,
                widthMm = width,
                heightMm = height,
                showTitle = showTitle,
                showDate = showDate,
                showExtraText = showExtraText,
                showQrCode = showQrCode,
                showBarcode = showBarcode,
                titleFontSize = titleFontSize,
                dateFontSize = dateFontSize,
                extraTextFontSize = extraTextFontSize,
                marginTop = marginTop,
                marginLeft = marginLeft,
                marginRight = marginRight,
                marginBottom = marginBottom,
                dateFormat = dateFormat
            )
            
            // Mostrar vista previa
            val intent = Intent(this, LabelPreviewActivity::class.java)
            intent.putExtra("template_preview", template)
            startActivity(intent)
        } catch (e: NumberFormatException) {
            Toast.makeText(
                this,
                "Por favor, ingrese valores numéricos válidos para ver la vista previa",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
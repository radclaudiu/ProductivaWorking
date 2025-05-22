package com.productiva.android

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.productiva.android.printer.BrotherPrinterManager

/**
 * Actividad principal que contiene una WebView con capacidades de impresión Brother
 * Esta actividad carga el portal web de Productiva y añade la interfaz JavaScript
 * para permitir la comunicación entre la web y la impresora Brother.
 */
class BrotherPrinterWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var printerManager: BrotherPrinterManager
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brother_printer_webview)
        
        // Inicializar el gestor de impresión
        printerManager = BrotherPrinterManager(this)
        
        // Configurar la WebView
        webView = findViewById(R.id.webView)
        
        // Habilitar JavaScript
        webView.settings.javaScriptEnabled = true
        
        // Permitir zoom y pantalla completa
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        
        // Optimizaciones para mejor rendimiento
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.allowFileAccess = true
        
        // Configurar WebViewClient para gestionar navegación
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // Permitir que la WebView maneje todas las URLs del dominio de Productiva
                val url = request?.url.toString()
                return if (url.contains("productiva") || url.startsWith("http://localhost") || url.startsWith("https://localhost")) {
                    false // La WebView maneja estas URLs
                } else {
                    true // El sistema maneja otras URLs
                }
            }
        }
        
        // Configurar WebChromeClient para características avanzadas (alertas, etc.)
        webView.webChromeClient = WebChromeClient()
        
        // Añadir la interfaz JavaScript para comunicación con la impresora Brother
        webView.addJavascriptInterface(printerManager.getJavaScriptInterface(), "AndroidBrotherPrinter")
        
        // Cargar la URL inicial del portal de tareas de Productiva
        loadInitialUrl()
    }
    
    /**
     * Carga la URL inicial del portal de tareas
     */
    private fun loadInitialUrl() {
        // Utiliza la URL de tu servidor o una URL local para desarrollo
        val productivaPrinterUrl = "https://tuservidor.com/local-user/labels"
        
        // También puedes pasar un token de acceso como parámetro si es necesario
        // Ejemplo: "https://tuservidor.com/local-user/labels?token=ABCDE12345"
        
        webView.loadUrl(productivaPrinterUrl)
    }
    
    /**
     * Gestiona el botón de retroceso para navegar en la WebView
     */
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
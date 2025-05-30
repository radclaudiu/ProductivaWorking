# Configuración Android para Impresión Brother TD-4550DNWB

## Configuración Completa para Recibir Etiquetas Web y Enviar a Impresora Bluetooth

Esta guía detalla la configuración específica de la aplicación Android para recibir datos de etiquetas desde la aplicación web y enviarlas a la impresora Brother TD-4550DNWB vía Bluetooth.

## 1. Configuración de Dependencias

### build.gradle (app level)
```gradle
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.productiva.labelprinter"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }
    
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.10.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.webkit:webkit:1.8.0'
    
    // Brother SDK para impresión
    implementation 'com.brother.ptouch.sdk:printerlibrary:4.4.0'
    
    // Manejo de permisos
    implementation 'com.karumi:dexter:6.2.3'
    
    // JSON para comunicación con WebView
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Para logs y debugging
    implementation 'com.jakewharton.timber:timber:5.0.1'
}
```

### settings.gradle (project level)
```gradle
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Repositorio oficial de Brother
        maven { 
            url 'https://nexus.brother.co.jp/repository/maven-public/' 
        }
    }
}

rootProject.name = "ProductivaLabelPrinter"
include ':app'
```

## 2. Configuración de Permisos AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Permisos básicos -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />

    <!-- Permisos Bluetooth (Android 11 y anteriores) -->
    <uses-permission android:name="android.permission.BLUETOOTH" 
        android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" 
        android:maxSdkVersion="30" />

    <!-- Permisos Bluetooth (Android 12+) -->
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />

    <!-- Permisos de ubicación (requeridos para Bluetooth en Android 6+) -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    <!-- Características del dispositivo -->
    <uses-feature android:name="android.hardware.bluetooth" android:required="true" />
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="false" />

    <application
        android:name=".ProductivaApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:requestLegacyExternalStorage="true"
        android:theme="@style/Theme.ProductivaLabelPrinter"
        android:usesCleartextTraffic="true"
        tools:targetApi="31">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="portrait"
            android:theme="@style/Theme.ProductivaLabelPrinter.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

## 3. Configuración de la Clase de Aplicación

### ProductivaApplication.java
```java
package com.productiva.labelprinter;

import android.app.Application;
import timber.log.Timber;

public class ProductivaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Configurar logging para debug
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        
        Timber.d("Productiva Label Printer Application initialized");
    }
}
```

## 4. Controlador Principal de Impresión Brother

### BrotherLabelController.java
```java
package com.productiva.labelprinter.printing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.ptouch.sdk.PrinterStatus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class BrotherLabelController {
    private static final String TAG = "BrotherLabelController";
    
    private Context context;
    private Printer printer;
    private PrinterInfo printerInfo;
    private ExecutorService executorService;
    private String connectedPrinterMac;
    
    public BrotherLabelController(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        initializePrinter();
    }
    
    private void initializePrinter() {
        printer = new Printer();
        printerInfo = printer.getPrinterInfo();
        
        // Configuración específica para Brother TD-4550DNWB
        printerInfo.printerModel = PrinterInfo.Model.TD_4550DNWB;
        printerInfo.port = PrinterInfo.Port.BLUETOOTH;
        printerInfo.orientation = PrinterInfo.Orientation.LANDSCAPE;
        printerInfo.paperSize = PrinterInfo.PaperSize.CUSTOM;
        printerInfo.resolution = PrinterInfo.Resolution.RESOLUTION_300;
        
        // Configuración de papel personalizado para etiquetas 35x40mm
        printerInfo.customPaperWidth = 35.0f; // mm
        printerInfo.customPaperLength = 40.0f; // mm
        printerInfo.customFeed = 3.0f; // mm de margen adicional
        
        // Configuraciones adicionales para mejor calidad
        printerInfo.numberOfCopies = 1;
        printerInfo.printQuality = PrinterInfo.PrintQuality.HIGH_RESOLUTION;
        printerInfo.align = PrinterInfo.Align.CENTER;
        printerInfo.valign = PrinterInfo.VAlign.MIDDLE;
        
        printer.setPrinterInfo(printerInfo);
        
        Timber.d("Brother TD-4550DNWB printer initialized for 35x40mm labels");
    }
    
    // Buscar impresoras Brother disponibles
    public void searchForPrinters(PrinterSearchListener listener) {
        executorService.execute(() -> {
            try {
                Timber.d("Iniciando búsqueda de impresoras Brother...");
                NetPrinter[] foundPrinters = printer.getNetPrinters(PrinterInfo.Port.BLUETOOTH);
                
                if (foundPrinters != null && foundPrinters.length > 0) {
                    Timber.d("Encontradas %d impresoras", foundPrinters.length);
                    listener.onPrintersFound(foundPrinters);
                } else {
                    Timber.w("No se encontraron impresoras Brother");
                    listener.onNoPrintersFound();
                }
            } catch (Exception e) {
                Timber.e(e, "Error al buscar impresoras");
                listener.onSearchError(e.getMessage());
            }
        });
    }
    
    // Conectar a impresora específica
    public void connectToPrinter(String macAddress, PrinterConnectionListener listener) {
        executorService.execute(() -> {
            try {
                Timber.d("Conectando a impresora: %s", macAddress);
                
                printerInfo.macAddress = macAddress;
                printer.setPrinterInfo(printerInfo);
                
                // Verificar estado de la impresora
                PrinterStatus status = printer.getPrinterStatus();
                
                if (status.errorCode == PrinterInfo.ErrorCode.ERROR_NONE) {
                    connectedPrinterMac = macAddress;
                    Timber.d("Conexión exitosa a: %s", macAddress);
                    listener.onConnected(macAddress);
                } else {
                    Timber.e("Fallo de conexión: %s", status.errorCode);
                    listener.onConnectionError(getErrorDescription(status.errorCode));
                }
                
            } catch (Exception e) {
                Timber.e(e, "Error de conexión");
                listener.onConnectionError("Error de conexión: " + e.getMessage());
            }
        });
    }
    
    // Imprimir etiqueta desde datos base64
    public void printLabelFromBase64(String base64ImageData, PrintJobListener listener) {
        executorService.execute(() -> {
            try {
                if (connectedPrinterMac == null) {
                    listener.onPrintError("No hay impresora conectada");
                    return;
                }
                
                Timber.d("Iniciando impresión de etiqueta...");
                
                // Decodificar imagen base64
                byte[] imageBytes = Base64.decode(base64ImageData, Base64.DEFAULT);
                Bitmap originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                
                if (originalBitmap == null) {
                    listener.onPrintError("No se pudo decodificar la imagen de la etiqueta");
                    return;
                }
                
                Timber.d("Imagen decodificada: %dx%d pixels", 
                    originalBitmap.getWidth(), originalBitmap.getHeight());
                
                // Ajustar imagen para impresión óptima en TD-4550DNWB
                Bitmap printBitmap = prepareImageForPrinting(originalBitmap);
                
                // Verificar estado antes de imprimir
                PrinterStatus preStatus = printer.getPrinterStatus();
                if (preStatus.errorCode != PrinterInfo.ErrorCode.ERROR_NONE) {
                    listener.onPrintError("Impresora no lista: " + getErrorDescription(preStatus.errorCode));
                    return;
                }
                
                // Ejecutar impresión
                PrinterStatus printStatus = printer.printImage(printBitmap);
                
                if (printStatus.errorCode == PrinterInfo.ErrorCode.ERROR_NONE) {
                    Timber.d("Etiqueta impresa correctamente");
                    listener.onPrintSuccess();
                } else {
                    Timber.e("Error de impresión: %s", printStatus.errorCode);
                    listener.onPrintError("Error de impresión: " + getErrorDescription(printStatus.errorCode));
                }
                
                // Limpiar bitmap de memoria
                if (printBitmap != originalBitmap) {
                    printBitmap.recycle();
                }
                originalBitmap.recycle();
                
            } catch (Exception e) {
                Timber.e(e, "Error durante impresión");
                listener.onPrintError("Error inesperado: " + e.getMessage());
            }
        });
    }
    
    // Preparar imagen para impresión óptima
    private Bitmap prepareImageForPrinting(Bitmap originalBitmap) {
        // Dimensiones objetivo para 35x40mm a 300 DPI
        int targetWidth = Math.round(35f * 300f / 25.4f);  // ~413 pixels
        int targetHeight = Math.round(40f * 300f / 25.4f); // ~472 pixels
        
        Bitmap resizedBitmap;
        
        if (originalBitmap.getWidth() != targetWidth || originalBitmap.getHeight() != targetHeight) {
            resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true);
            Timber.d("Imagen redimensionada a: %dx%d", targetWidth, targetHeight);
        } else {
            resizedBitmap = originalBitmap;
        }
        
        return resizedBitmap;
    }
    
    // Convertir códigos de error a mensajes descriptivos
    private String getErrorDescription(PrinterInfo.ErrorCode errorCode) {
        switch (errorCode) {
            case ERROR_PAPER_EMPTY:
                return "Sin papel en la impresora";
            case ERROR_BATTERY_EMPTY:
                return "Batería baja en la impresora";
            case ERROR_COMMUNICATION_ERROR:
                return "Error de comunicación Bluetooth";
            case ERROR_PAPER_JAM:
                return "Papel atascado en la impresora";
            case ERROR_BUSY:
                return "Impresora ocupada, intente de nuevo";
            case ERROR_NOT_SAME_MODEL:
                return "Modelo de impresora incompatible";
            case ERROR_BROTHER_PRINTER_NOT_FOUND:
                return "Impresora Brother no encontrada";
            case ERROR_PAPER_CUT_ERROR:
                return "Error en el cortador de papel";
            case ERROR_OVERHEATING:
                return "Impresora sobrecalentada, espere";
            default:
                return "Error desconocido: " + errorCode.toString();
        }
    }
    
    // Obtener estado de conexión
    public boolean isConnected() {
        return connectedPrinterMac != null;
    }
    
    public String getConnectedPrinterMac() {
        return connectedPrinterMac;
    }
    
    // Desconectar impresora
    public void disconnect() {
        connectedPrinterMac = null;
        Timber.d("Impresora desconectada");
    }
    
    // Limpiar recursos
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    // Interfaces de callback
    public interface PrinterSearchListener {
        void onPrintersFound(NetPrinter[] printers);
        void onNoPrintersFound();
        void onSearchError(String error);
    }
    
    public interface PrinterConnectionListener {
        void onConnected(String macAddress);
        void onConnectionError(String error);
    }
    
    public interface PrintJobListener {
        void onPrintSuccess();
        void onPrintError(String error);
    }
}
```

## 5. Bridge JavaScript para WebView

### WebViewBridge.java
```java
package com.productiva.labelprinter.webview;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.brother.ptouch.sdk.NetPrinter;
import com.google.gson.Gson;
import com.productiva.labelprinter.printing.BrotherLabelController;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class WebViewBridge {
    private static final String TAG = "WebViewBridge";
    
    private Context context;
    private WebView webView;
    private BrotherLabelController printerController;
    private Gson gson;
    
    public WebViewBridge(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
        this.printerController = new BrotherLabelController(context);
        this.gson = new Gson();
    }
    
    @JavascriptInterface
    public void searchPrinters() {
        Timber.d("JavaScript solicitó búsqueda de impresoras");
        
        printerController.searchForPrinters(new BrotherLabelController.PrinterSearchListener() {
            @Override
            public void onPrintersFound(NetPrinter[] printers) {
                // Convertir array de impresoras a JSON
                String printersJson = gson.toJson(printers);
                
                runOnUiThread(() -> {
                    callJavaScript("window.BrotherPrint.onPrintersFound('" + 
                        escapeJson(printersJson) + "')");
                });
            }
            
            @Override
            public void onNoPrintersFound() {
                runOnUiThread(() -> {
                    callJavaScript("window.BrotherPrint.onPrintersFound('[]')");
                });
            }
            
            @Override
            public void onSearchError(String error) {
                runOnUiThread(() -> {
                    callJavaScript("window.BrotherPrint.onPrintError('" + 
                        escapeJson(error) + "')");
                });
            }
        });
    }
    
    @JavascriptInterface
    public void connectPrinter(String macAddress) {
        Timber.d("JavaScript solicitó conexión a: %s", macAddress);
        
        printerController.connectToPrinter(macAddress, 
            new BrotherLabelController.PrinterConnectionListener() {
                @Override
                public void onConnected(String macAddress) {
                    runOnUiThread(() -> {
                        callJavaScript("window.BrotherPrint.onPrinterConnected()");
                    });
                }
                
                @Override
                public void onConnectionError(String error) {
                    runOnUiThread(() -> {
                        callJavaScript("window.BrotherPrint.onPrintError('" + 
                            escapeJson(error) + "')");
                    });
                }
            });
    }
    
    @JavascriptInterface
    public void printImage(String base64ImageData) {
        Timber.d("JavaScript solicitó impresión de etiqueta");
        
        printerController.printLabelFromBase64(base64ImageData, 
            new BrotherLabelController.PrintJobListener() {
                @Override
                public void onPrintSuccess() {
                    runOnUiThread(() -> {
                        callJavaScript("window.BrotherPrint.onPrintSuccess()");
                    });
                }
                
                @Override
                public void onPrintError(String error) {
                    runOnUiThread(() -> {
                        callJavaScript("window.BrotherPrint.onPrintError('" + 
                            escapeJson(error) + "')");
                    });
                }
            });
    }
    
    @JavascriptInterface
    public String getPrinterStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("connected", printerController.isConnected());
        status.put("macAddress", printerController.getConnectedPrinterMac());
        
        return gson.toJson(status);
    }
    
    // Métodos auxiliares
    private void runOnUiThread(Runnable action) {
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).runOnUiThread(action);
        }
    }
    
    private void callJavaScript(String script) {
        webView.evaluateJavascript(script, null);
    }
    
    private String escapeJson(String input) {
        return input.replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    public void cleanup() {
        if (printerController != null) {
            printerController.cleanup();
        }
    }
}
```

## 6. Configuración de MainActivity

### MainActivity.java
```java
package com.productiva.labelprinter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.productiva.labelprinter.webview.WebViewBridge;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String WEB_APP_URL = "https://tu-servidor.com/tasks/local-user/labels";
    
    private WebView webView;
    private WebViewBridge webViewBridge;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        webView = findViewById(R.id.webview);
        
        requestRequiredPermissions();
    }
    
    private void requestRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        
        // Permisos base
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        
        // Permisos Bluetooth según versión de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            permissions.add(Manifest.permission.BLUETOOTH);
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        
        Dexter.withContext(this)
            .withPermissions(permissions)
            .withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {
                    if (report.areAllPermissionsGranted()) {
                        Timber.d("Todos los permisos concedidos");
                        initializeWebView();
                    } else {
                        Timber.e("Permisos denegados, cerrando aplicación");
                        finish();
                    }
                }
                
                @Override
                public void onPermissionRationaleShouldBeShown(
                    List<PermissionRequest> permissions, 
                    PermissionToken token) {
                    token.continuePermissionRequest();
                }
            }).check();
    }
    
    private void initializeWebView() {
        WebSettings webSettings = webView.getSettings();
        
        // Configuraciones básicas
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        // Configuraciones para mejor rendimiento
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(false);
        
        // Configurar JavaScript bridge
        webViewBridge = new WebViewBridge(this, webView);
        webView.addJavascriptInterface(webViewBridge, "AndroidBridge");
        
        // Configurar WebViewClient
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Timber.d("Página cargada: %s", url);
                
                // Notificar a JavaScript que la app está lista
                view.evaluateJavascript(
                    "console.log('Android WebView iniciado para impresión Brother');", 
                    null
                );
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Mantener navegación dentro del WebView
                view.loadUrl(url);
                return true;
            }
        });
        
        // Cargar aplicación web
        Timber.d("Cargando aplicación web: %s", WEB_APP_URL);
        webView.loadUrl(WEB_APP_URL);
    }
    
    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webViewBridge != null) {
            webViewBridge.cleanup();
        }
    }
}
```

## 7. Layout de la Actividad

### res/layout/activity_main.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#FFFFFF">

    <WebView
        android:id="@+id/webview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_centerInParent="true" />

</RelativeLayout>
```

## 8. Recursos de Strings

### res/values/strings.xml
```xml
<resources>
    <string name="app_name">Productiva Etiquetas</string>
    <string name="permission_bluetooth_title">Permisos Bluetooth</string>
    <string name="permission_bluetooth_message">La aplicación necesita acceso a Bluetooth para conectar con la impresora Brother TD-4550DNWB</string>
    <string name="permission_location_title">Permisos de Ubicación</string>
    <string name="permission_location_message">Android requiere permisos de ubicación para buscar dispositivos Bluetooth</string>
</resources>
```

## 9. Proguard Rules (si usas ofuscación)

### proguard-rules.pro
```proguard
# Mantener clases del SDK Brother
-keep class com.brother.ptouch.sdk.** { *; }

# Mantener JavaScript bridge
-keep class com.productiva.labelprinter.webview.WebViewBridge { *; }
-keepclassmembers class com.productiva.labelprinter.webview.WebViewBridge {
    @android.webkit.JavascriptInterface <methods>;
}

# Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
```

## 10. Configuración de Red (opcional)

### res/xml/network_security_config.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">tu-servidor.com</domain>
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
</network-security-config>
```

Esta configuración completa permite que tu aplicación Android:
1. Reciba datos de etiquetas desde la aplicación web
2. Se conecte vía Bluetooth a impresoras Brother TD-4550DNWB
3. Imprima etiquetas de 35x40mm con alta calidad
4. Maneje errores y estados de conexión apropiadamente

La integración funcionará automáticamente cuando el JavaScript detecte el `AndroidBridge` disponible en el WebView.
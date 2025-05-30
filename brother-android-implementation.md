# Implementación Android para Impresora Brother TD-4550DNWB

## Guía Completa de Integración

Esta guía detalla cómo implementar la funcionalidad de impresión Brother TD-4550DNWB en su aplicación Android WebView para etiquetas de 35x40mm.

## 1. Dependencias del Proyecto

### build.gradle (Module: app)
```gradle
dependencies {
    implementation 'com.brother.ptouch.sdk:printerlibrary:4.4.0'
    implementation 'androidx.webkit:webkit:1.7.0'
    implementation 'com.karumi:dexter:6.2.3'
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

### AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<!-- Para Android 12+ -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<application
    android:requestLegacyExternalStorage="true"
    ... >
```

## 2. Clase Principal de Impresión

### BrotherPrintManager.java
```java
package com.productiva.app.printing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.ptouch.sdk.PrinterStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BrotherPrintManager {
    private static final String TAG = "BrotherPrintManager";
    private Context context;
    private Printer printer;
    private PrinterInfo printerInfo;
    
    public BrotherPrintManager(Context context) {
        this.context = context;
        setupPrinter();
    }
    
    private void setupPrinter() {
        printer = new Printer();
        printerInfo = printer.getPrinterInfo();
        
        // Configuración específica para TD-4550DNWB con etiquetas 35x40mm
        printerInfo.printerModel = PrinterInfo.Model.TD_4550DNWB;
        printerInfo.port = PrinterInfo.Port.BLUETOOTH;
        printerInfo.orientation = PrinterInfo.Orientation.LANDSCAPE;
        printerInfo.paperSize = PrinterInfo.PaperSize.CUSTOM;
        printerInfo.resolution = PrinterInfo.Resolution.RESOLUTION_300;
        
        // Configurar tamaño personalizado para 35x40mm
        printerInfo.customPaperWidth = 35; // mm
        printerInfo.customPaperLength = 40; // mm
        printerInfo.customFeed = 3; // mm de alimentación adicional
        
        printer.setPrinterInfo(printerInfo);
        
        Log.d(TAG, "Printer configured for TD-4550DNWB, 35x40mm labels");
    }
    
    public void searchBluetoothPrinters(PrinterSearchCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Searching for Brother printers...");
                NetPrinter[] printers = printer.getNetPrinters(PrinterInfo.Port.BLUETOOTH);
                
                if (printers != null && printers.length > 0) {
                    Log.d(TAG, "Found " + printers.length + " printers");
                    callback.onPrintersFound(printers);
                } else {
                    Log.d(TAG, "No printers found");
                    callback.onPrintersFound(new NetPrinter[0]);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching printers", e);
                callback.onError("Error al buscar impresoras: " + e.getMessage());
            }
        }).start();
    }
    
    public void connectToPrinter(String macAddress, PrinterConnectionCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Connecting to printer: " + macAddress);
                printerInfo.macAddress = macAddress;
                printer.setPrinterInfo(printerInfo);
                
                // Verificar conexión
                PrinterStatus status = printer.getPrinterStatus();
                
                if (status.errorCode == PrinterInfo.ErrorCode.ERROR_NONE) {
                    Log.d(TAG, "Successfully connected to printer");
                    callback.onConnected();
                } else {
                    Log.e(TAG, "Failed to connect: " + status.errorCode);
                    callback.onError("Error de conexión: " + status.errorCode.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, "Connection error", e);
                callback.onError("Error al conectar: " + e.getMessage());
            }
        }).start();
    }
    
    public void printImage(String base64Image, PrintCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Starting image print...");
                
                // Decodificar imagen base64
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                
                if (bitmap == null) {
                    callback.onError("Error al decodificar imagen");
                    return;
                }
                
                Log.d(TAG, "Image decoded, size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                
                // Redimensionar si es necesario para 35x40mm a 300 DPI
                int targetWidth = 413; // 35mm * 300DPI / 25.4
                int targetHeight = 472; // 40mm * 300DPI / 25.4
                
                if (bitmap.getWidth() != targetWidth || bitmap.getHeight() != targetHeight) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
                    Log.d(TAG, "Image resized to: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                }
                
                // Imprimir
                PrinterStatus status = printer.printImage(bitmap);
                
                if (status.errorCode == PrinterInfo.ErrorCode.ERROR_NONE) {
                    Log.d(TAG, "Print completed successfully");
                    callback.onSuccess();
                } else {
                    Log.e(TAG, "Print failed: " + status.errorCode);
                    callback.onError("Error de impresión: " + getErrorMessage(status.errorCode));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Print error", e);
                callback.onError("Error al imprimir: " + e.getMessage());
            }
        }).start();
    }
    
    private String getErrorMessage(PrinterInfo.ErrorCode errorCode) {
        switch (errorCode) {
            case ERROR_PAPER_EMPTY:
                return "Sin papel en la impresora";
            case ERROR_BATTERY_EMPTY:
                return "Batería baja";
            case ERROR_COMMUNICATION_ERROR:
                return "Error de comunicación";
            case ERROR_PAPER_JAM:
                return "Papel atascado";
            case ERROR_BUSY:
                return "Impresora ocupada";
            case ERROR_NOT_SAME_MODEL:
                return "Modelo de impresora incorrecto";
            default:
                return errorCode.toString();
        }
    }
    
    // Interfaces de callback
    public interface PrinterSearchCallback {
        void onPrintersFound(NetPrinter[] printers);
        void onError(String error);
    }
    
    public interface PrinterConnectionCallback {
        void onConnected();
        void onError(String error);
    }
    
    public interface PrintCallback {
        void onSuccess();
        void onError(String error);
    }
}
```

## 3. Interface JavaScript para WebView

### WebViewInterface.java
```java
package com.productiva.app.webview;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.brother.ptouch.sdk.NetPrinter;
import com.google.gson.Gson;
import com.productiva.app.printing.BrotherPrintManager;

public class WebViewInterface {
    private static final String TAG = "WebViewInterface";
    private Context context;
    private BrotherPrintManager printManager;
    private WebView webView;
    
    public WebViewInterface(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
        this.printManager = new BrotherPrintManager(context);
    }
    
    @JavascriptInterface
    public void searchPrinters() {
        Log.d(TAG, "JavaScript called searchPrinters");
        
        printManager.searchBluetoothPrinters(new BrotherPrintManager.PrinterSearchCallback() {
            @Override
            public void onPrintersFound(NetPrinter[] printers) {
                Gson gson = new Gson();
                String jsonPrinters = gson.toJson(printers);
                
                runOnUiThread(() -> {
                    webView.evaluateJavascript(
                        "window.BrotherPrint.onPrintersFound('" + jsonPrinters + "')", 
                        null
                    );
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    webView.evaluateJavascript(
                        "window.BrotherPrint.onPrintError('" + error + "')", 
                        null
                    );
                });
            }
        });
    }
    
    @JavascriptInterface
    public void connectPrinter(String macAddress) {
        Log.d(TAG, "JavaScript called connectPrinter: " + macAddress);
        
        printManager.connectToPrinter(macAddress, new BrotherPrintManager.PrinterConnectionCallback() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> {
                    webView.evaluateJavascript(
                        "window.BrotherPrint.onPrinterConnected()", 
                        null
                    );
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    webView.evaluateJavascript(
                        "window.BrotherPrint.onPrintError('" + error + "')", 
                        null
                    );
                });
            }
        });
    }
    
    @JavascriptInterface
    public void printImage(String base64Image) {
        Log.d(TAG, "JavaScript called printImage");
        
        printManager.printImage(base64Image, new BrotherPrintManager.PrintCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    webView.evaluateJavascript(
                        "window.BrotherPrint.onPrintSuccess()", 
                        null
                    );
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    webView.evaluateJavascript(
                        "window.BrotherPrint.onPrintError('" + error + "')", 
                        null
                    );
                });
            }
        });
    }
    
    private void runOnUiThread(Runnable action) {
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).runOnUiThread(action);
        }
    }
}
```

## 4. MainActivity con WebView

### MainActivity.java
```java
package com.productiva.app;

import android.Manifest;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.productiva.app.webview.WebViewInterface;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private WebViewInterface webInterface;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        webView = findViewById(R.id.webview);
        setupWebView();
        requestPermissions();
    }
    
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        // Configurar interface JavaScript
        webInterface = new WebViewInterface(this, webView);
        webView.addJavascriptInterface(webInterface, "AndroidBridge");
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Notificar a JavaScript que la app Android está lista
                view.evaluateJavascript("console.log('Android WebView ready')", null);
            }
        });
        
        // Cargar la aplicación web
        webView.loadUrl("https://tu-dominio.com/tasks/local-user/labels");
    }
    
    private void requestPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
            .withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {
                    if (report.areAllPermissionsGranted()) {
                        // Todos los permisos concedidos
                        setupBrotherPrinting();
                    } else {
                        // Algunos permisos denegados
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
    
    private void setupBrotherPrinting() {
        // Inicialización adicional si es necesaria
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
```

## 5. Layout XML

### activity_main.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <WebView
        android:id="@+id/webview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</LinearLayout>
```

## 6. Configuración de Gradle

### settings.gradle
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Repositorio Brother SDK
        maven { url 'https://nexus.brother.co.jp/repository/maven-public/' }
    }
}
```

## 7. Flujo de Funcionamiento

### Paso 1: Inicialización
1. El usuario abre la aplicación Android
2. Se cargan los permisos Bluetooth
3. Se inicializa el WebView con la interface JavaScript
4. Se carga la página web de etiquetas

### Paso 2: Búsqueda de Impresoras
1. Usuario toca "Imprimir en TD-4550DNWB"
2. JavaScript llama `AndroidBridge.searchPrinters()`
3. Android busca impresoras Brother vía Bluetooth
4. Retorna lista de impresoras encontradas a JavaScript

### Paso 3: Conexión
1. Usuario selecciona impresora de la lista
2. JavaScript llama `AndroidBridge.connectPrinter(macAddress)`
3. Android establece conexión Bluetooth con la impresora
4. Confirma conexión exitosa a JavaScript

### Paso 4: Impresión
1. JavaScript genera imagen de etiqueta 35x40mm
2. Convierte imagen a base64
3. Llama `AndroidBridge.printImage(base64Image)`
4. Android decodifica imagen y envía a impresora Brother
5. Retorna resultado de impresión a JavaScript

## 8. Resolución de Problemas

### Error: No se encuentran impresoras
- Verificar que la impresora esté encendida
- Verificar que Bluetooth esté activado
- Verificar permisos de ubicación (necesarios para Bluetooth en Android 6+)

### Error: Fallo de conexión
- Verificar que la impresora esté en modo emparejamiento
- Verificar distancia entre dispositivos
- Reiniciar Bluetooth si es necesario

### Error: Fallo de impresión
- Verificar papel en la impresora
- Verificar que las etiquetas sean de 35x40mm
- Verificar batería de la impresora

## 9. Integración Completa

Una vez implementado todo el código anterior, la aplicación Android funcionará como un navegador especializado que puede imprimir directamente en la impresora Brother TD-4550DNWB usando etiquetas de 35x40mm.

La integración con el JavaScript existente ya está preparada y funcionará automáticamente al detectar la presencia del `AndroidBridge` en el WebView.
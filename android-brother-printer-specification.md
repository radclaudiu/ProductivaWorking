# Especificación Técnica: Android App para Impresión Brother TD-4550DNWB

## Resumen del Sistema

Crear una aplicación Android que funcione como WebView especializada para recibir datos de etiquetas desde una aplicación web y enviarlas a una impresora Brother TD-4550DNWB vía Bluetooth. La aplicación debe manejar etiquetas de 35x40mm con manejo completo de errores y notificaciones.

## 1. Datos que Recibe la Aplicación Android

### 1.1 Formato de Entrada desde la Web
La página web enviará los siguientes datos a través del JavaScript Bridge:

```javascript
// Estructura de datos de etiqueta
{
    "productName": "POLLO EMPANADO",           // Nombre del producto (string)
    "conservationType": "REFRIGERACIÓN",       // Tipo de conservación (string)
    "preparedBy": "EMP: Juan Pérez García",    // Empleado que preparó (string)
    "startDate": "INICIO: 30/05/2025 14:30",  // Fecha de preparación (string)
    "expiryDate": "Caducidad: 02/06/2025 14:30", // Fecha de caducidad (string)
    "secondaryExpiryDate": "Caducidad primaria: 01/06/2025", // Fecha secundaria opcional (string)
    "quantity": 3                              // Cantidad de etiquetas a imprimir (integer)
}
```

### 1.2 Imagen Base64 Generada
La aplicación web genera automáticamente una imagen en formato base64 con las siguientes características:
- **Dimensiones**: 413x472 pixels (equivalente a 35x40mm a 300 DPI)
- **Formato**: PNG en base64 sin prefijo data:image
- **Contenido**: Etiqueta completa con todos los textos posicionados
- **Calidad**: Optimizada para impresión térmica Brother

```javascript
// Ejemplo de llamada desde JavaScript web a Android
AndroidBridge.printImage("iVBORw0KGgoAAAANSUhEUgAAA..."); // Imagen base64
```

## 2. Datos que Debe Enviar a la Impresora Brother

### 2.1 Configuración de Impresión
```java
// Configuración específica para Brother TD-4550DNWB
printerInfo.printerModel = PrinterInfo.Model.TD_4550DNWB;
printerInfo.port = PrinterInfo.Port.BLUETOOTH;
printerInfo.orientation = PrinterInfo.Orientation.LANDSCAPE;
printerInfo.paperSize = PrinterInfo.PaperSize.CUSTOM;
printerInfo.resolution = PrinterInfo.Resolution.RESOLUTION_300;

// Tamaño de papel personalizado
printerInfo.customPaperWidth = 35.0f; // mm
printerInfo.customPaperLength = 40.0f; // mm
printerInfo.customFeed = 3.0f; // mm
```

### 2.2 Proceso de Envío
1. **Decodificar imagen base64** → Bitmap Android
2. **Verificar dimensiones** → 413x472 pixels
3. **Redimensionar si necesario** → Mantener proporción
4. **Enviar a Brother SDK** → `printer.printImage(bitmap)`

## 3. Flujo de Comunicación Completo

### 3.1 Métodos JavaScript Disponibles en Web
```javascript
// Buscar impresoras disponibles
AndroidBridge.searchPrinters();

// Conectar a impresora específica  
AndroidBridge.connectPrinter("00:80:77:31:01:07"); // MAC address

// Imprimir imagen
AndroidBridge.printImage(base64ImageData);

// Obtener estado de conexión
AndroidBridge.getPrinterStatus();
```

### 3.2 Callbacks de Android a JavaScript
```javascript
// Callbacks globales que Android llamará
window.BrotherPrint = {
    onPrintersFound: function(printersJsonString) {
        // Lista de impresoras encontradas
    },
    onPrinterConnected: function() {
        // Impresora conectada exitosamente
    },
    onPrintSuccess: function() {
        // Impresión completada correctamente
    },
    onPrintError: function(errorMessage) {
        // Error durante cualquier operación
    }
};
```

## 4. Manejo de Errores y Notificaciones

### 4.1 Tipos de Errores a Manejar

#### Errores de Búsqueda de Impresoras
```java
// Casos de error en búsqueda
- "No se encontraron impresoras Brother"
- "Bluetooth desactivado"
- "Permisos de ubicación requeridos"
- "Error de comunicación Bluetooth"
```

#### Errores de Conexión
```java
// Casos de error en conexión
- "Impresora fuera de alcance"
- "Error de emparejamiento Bluetooth"
- "Impresora ya conectada a otro dispositivo"
- "Modelo de impresora incompatible"
```

#### Errores de Impresión
```java
// Códigos específicos Brother TD-4550DNWB
ERROR_PAPER_EMPTY → "Sin papel en la impresora"
ERROR_BATTERY_EMPTY → "Batería baja en la impresora"
ERROR_COMMUNICATION_ERROR → "Error de comunicación"
ERROR_PAPER_JAM → "Papel atascado"
ERROR_BUSY → "Impresora ocupada, intente de nuevo"
ERROR_OVERHEATING → "Impresora sobrecalentada, espere"
```

### 4.2 Sistema de Notificaciones Android

#### Notificaciones de Estado
```java
// Notificación durante búsqueda
NotificationCompat.Builder()
    .setContentTitle("Buscando Impresoras")
    .setContentText("Escaneando dispositivos Brother...")
    .setSmallIcon(R.drawable.ic_printer_search)
    .setOngoing(true);

// Notificación de conexión exitosa
NotificationCompat.Builder()
    .setContentTitle("Impresora Conectada")
    .setContentText("Brother TD-4550DNWB lista para imprimir")
    .setSmallIcon(R.drawable.ic_printer_connected);

// Notificación de impresión exitosa
NotificationCompat.Builder()
    .setContentTitle("Etiqueta Impresa")
    .setContentText("Impresión completada correctamente")
    .setSmallIcon(R.drawable.ic_print_success);
```

#### Notificaciones de Error
```java
// Notificación de error crítico
NotificationCompat.Builder()
    .setContentTitle("Error de Impresión")
    .setContentText(errorMessage)
    .setSmallIcon(R.drawable.ic_error)
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setAutoCancel(true);
```

## 5. Estructura de Archivos Requerida

### 5.1 Archivos Java Principales

#### `/app/src/main/java/com/productiva/labelprinter/MainActivity.java`
```java
// Actividad principal con WebView
// - Configuración de permisos Bluetooth
// - Inicialización del WebView
// - Configuración del JavaScript Bridge
// - Manejo del ciclo de vida
```

#### `/app/src/main/java/com/productiva/labelprinter/printing/BrotherPrintManager.java`
```java
// Controlador principal de impresión Brother
// - Configuración del SDK Brother TD-4550DNWB
// - Búsqueda de impresoras Bluetooth
// - Conexión y desconexión de impresoras
// - Ejecución de trabajos de impresión
// - Manejo de errores específicos Brother
```

#### `/app/src/main/java/com/productiva/labelprinter/webview/WebViewBridge.java`
```java
// Interface JavaScript para comunicación Web-Android
// - Métodos @JavascriptInterface para llamadas desde web
// - Conversión de datos JSON a objetos Java
// - Envío de callbacks a JavaScript
// - Manejo de hilos UI/Background
```

#### `/app/src/main/java/com/productiva/labelprinter/notifications/NotificationManager.java`
```java
// Sistema de notificaciones
// - Creación de canales de notificación
// - Notificaciones de estado de impresión
// - Notificaciones de error
// - Manejo de sonidos y vibración
```

#### `/app/src/main/java/com/productiva/labelprinter/utils/ImageProcessor.java`
```java
// Procesamiento de imágenes para impresión
// - Decodificación de base64 a Bitmap
// - Redimensionamiento a 413x472 pixels
// - Optimización para impresión térmica
// - Manejo de memoria y liberación de recursos
```

### 5.2 Archivos de Configuración

#### `/app/build.gradle`
```gradle
// Dependencias del proyecto
dependencies {
    implementation 'com.brother.ptouch.sdk:printerlibrary:4.4.0'
    implementation 'androidx.webkit:webkit:1.8.0'
    implementation 'com.karumi:dexter:6.2.3'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'com.jakewharton.timber:timber:5.0.1'
}
```

#### `/app/src/main/AndroidManifest.xml`
```xml
<!-- Permisos requeridos -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

### 5.3 Archivos de Recursos

#### `/app/src/main/res/layout/activity_main.xml`
```xml
<!-- Layout con WebView pantalla completa -->
<WebView
    android:id="@+id/webview"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

#### `/app/src/main/res/values/strings.xml`
```xml
<!-- Strings para la aplicación -->
<string name="app_name">Productiva Etiquetas</string>
<string name="notification_channel_printing">Impresión</string>
<string name="notification_channel_errors">Errores</string>
```

#### `/app/src/main/res/drawable/`
```
ic_printer_search.xml    // Icono búsqueda impresoras
ic_printer_connected.xml // Icono impresora conectada  
ic_print_success.xml     // Icono impresión exitosa
ic_error.xml            // Icono de error
```

## 6. Casos de Uso Específicos

### 6.1 Flujo Normal de Impresión
1. Usuario abre app Android → carga página web de etiquetas
2. Selecciona producto → genera etiqueta 35x40mm
3. Toca "Imprimir en TD-4550DNWB" → Android busca impresoras
4. Selecciona impresora de lista → Android conecta vía Bluetooth
5. Web envía imagen base64 → Android imprime en Brother
6. Notificación de éxito → retorno automático a lista de productos

### 6.2 Flujo de Manejo de Errores
1. Error de búsqueda → notificación + opción de reintentar
2. Error de conexión → notificación + lista de impresoras actualizada
3. Error de impresión → notificación específica + diagnóstico
4. Sin papel → notificación "Agregue papel y reintente"
5. Batería baja → notificación "Cargue la impresora"

## 7. Configuración de Desarrollo

### 7.1 SDK Mínimo
```gradle
android {
    compileSdk 34
    defaultConfig {
        minSdk 21  // Android 5.0 para soporte Bluetooth LE
        targetSdk 34
    }
}
```

### 7.2 Configuración de Red
```xml
<!-- network_security_config.xml para HTTPS/HTTP -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">tu-servidor.com</domain>
    </domain-config>
</network-security-config>
```

Esta especificación completa permite crear una aplicación Android que recibe datos de etiquetas desde la web, se conecta a impresoras Brother TD-4550DNWB vía Bluetooth, y maneja todos los errores con notificaciones apropiadas para el usuario.
# Guía de Integración de Impresión Brother con Android

## Descripción General

Esta documentación explica cómo funciona la integración entre la aplicación web Productiva y la aplicación Android personalizada para imprimir etiquetas directamente en impresoras Brother.

## Componentes del Sistema

### 1. Aplicación Web (Productiva)

- **brother-android-native.js**: Script JavaScript que detecta si la página está siendo cargada dentro de la app Android y se comunica con ella para imprimir etiquetas.
- **print_labels.html**: Plantilla HTML que muestra la etiqueta a imprimir y ofrece un botón específico para la app Android.

### 2. Aplicación Android

- **BrotherPrinterManager.kt**: Clase que gestiona la comunicación con las impresoras Brother usando la biblioteca SDK oficial.
- **BrotherPrinterWebViewActivity.kt**: Actividad principal que contiene la WebView con capacidades de impresión.

## Flujo de Funcionamiento

1. El usuario abre la app Android personalizada
2. La app carga el portal de tareas de Productiva en su WebView integrada
3. El usuario navega hasta la sección de etiquetas y selecciona un producto
4. Al generar una etiqueta, el script `brother-android-native.js` detecta que está dentro de la app Android
5. Cuando el usuario presiona "Imprimir Etiqueta":
   - Los datos de la etiqueta (producto, fechas, etc.) se convierten a formato JSON
   - Se envían a través de la interfaz JavaScript a la app Android
   - La app Android utiliza la biblioteca SDK de Brother para generar los comandos ESC/P
   - La impresora Brother recibe los comandos y genera la etiqueta física

## Requisitos Técnicos

### Para la Aplicación Web

- Incluir el script `brother-android-native.js` en la página de impresión de etiquetas
- Añadir el botón específico para Android con ID `android-print-btn`
- Asegurar que todos los elementos de la etiqueta tienen IDs consistentes (product-name, conservation-type, etc.)

### Para la Aplicación Android

- Biblioteca SDK oficial de Brother para Android (com.brother.ptouch.sdk)
- Permisos en el AndroidManifest.xml:
  - `android.permission.BLUETOOTH`
  - `android.permission.BLUETOOTH_ADMIN`
  - `android.permission.BLUETOOTH_CONNECT` (para Android 12+)
  - `android.permission.INTERNET`

## Modelos de Impresoras Compatibles

- **Modelo Principal**: Brother TD-4550DNWB
- Otros modelos compatibles con la biblioteca SDK de Brother

## Personalización de Etiquetas

La generación de comandos ESC/P para la impresora Brother se encuentra en:
- JavaScript: Función `generateBrotherCommand()` en `brother-android-native.js`
- Android: Método `generateESCPCommand()` en `BrotherPrinterManager.kt`

Estas funciones pueden modificarse para ajustar el formato, tamaño de fuente, negrita, etc.

## Configuración de la Aplicación Android

### Añadir la Biblioteca Brother al build.gradle

```gradle
dependencies {
    // SDK de Brother para impresoras
    implementation 'com.brother.sdk:brother-sdk:4.5.0'
    // Otras dependencias...
}
```

### Inicializar la WebView con la Interfaz JavaScript

```kotlin
// En BrotherPrinterWebViewActivity.kt
webView.addJavascriptInterface(printerManager.getJavaScriptInterface(), "AndroidBrotherPrinter")
```

## Resolución de Problemas

### La App Android No Detecta la Impresora

1. Verificar que la impresora está encendida y con Bluetooth activado
2. Comprobar que la impresora ha sido previamente emparejada con el dispositivo Android
3. Verificar que la app tiene los permisos necesarios

### La Impresión Falla

1. Revisar los logs de Android para ver el error específico
2. Verificar que la impresora tiene papel suficiente
3. Comprobar que los comandos ESC/P son compatibles con el modelo específico de impresora

## Desarrollo Futuro

- Añadir soporte para múltiples impresoras y selección en la interfaz
- Implementar memorización de la última impresora utilizada
- Añadir soporte para impresión por USB además de Bluetooth
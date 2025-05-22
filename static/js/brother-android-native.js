/**
 * Módulo para impresión directa en impresoras Brother desde Android
 * Versión 1.0 (22/05/2025)
 * 
 * Este script está diseñado para funcionar con la app Android personalizada
 * que contiene un navegador web interno con capacidades nativas
 * para imprimir directamente en impresoras Brother.
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log("Inicializando sistema de impresión Brother para Android");
    
    // Verificar si el botón está presente
    const printButton = document.getElementById('android-print-btn');
    const messageArea = document.getElementById('print-message');
    
    if (!printButton) {
        // Si no existe el botón específico, intentar crear uno
        createAndroidPrintButton();
        return;
    }
    
    // Función para mostrar mensajes
    const showMessage = (message, isError = false) => {
        if (!messageArea) return;
        
        messageArea.style.display = "block";
        messageArea.textContent = message;
        messageArea.className = isError ? 
            "alert alert-danger mt-3" : 
            "alert alert-info mt-3";
        
        // Ocultar después de 5 segundos solo si no es error
        if (!isError) {
            setTimeout(() => {
                messageArea.style.display = "none";
            }, 5000);
        }
    };
    
    // Función para obtener los datos de impresión
    const getPrintData = () => {
        const productName = document.getElementById("product-name")?.textContent || "";
        const conservationType = document.getElementById("conservation-type")?.textContent || "";
        const preparedBy = document.getElementById("prepared-by")?.textContent || "";
        const startDate = document.getElementById("start-date")?.textContent || "";
        const expiryDate = document.getElementById("expiry-date")?.textContent || "";
        const secondaryExpiryDate = document.getElementById("secondary-expiry-date")?.textContent || "";
        
        // Cantidad de etiquetas
        const quantity = parseInt(document.getElementById("quantity")?.value || "1");
        
        return {
            productName,
            conservationType,
            preparedBy,
            startDate,
            expiryDate,
            secondaryExpiryDate,
            quantity
        };
    };
    
    // Función principal para iniciar la impresión
    const startNativePrinting = () => {
        try {
            // Verificar si estamos en la app Android nativa
            if (typeof AndroidBrotherPrinter === 'undefined') {
                // Si no estamos en la app nativa, mostrar mensaje y caer en el método alternativo
                showMessage("No se detectó la app nativa de Android. Usando método alternativo de impresión.", true);
                
                // Intentar usar el método Bluetooth estándar si está disponible
                if (typeof startBrotherPrinting === 'function') {
                    startBrotherPrinting();
                } else {
                    showMessage("Por favor, use la aplicación Android 'Productiva' para imprimir etiquetas.", true);
                }
                
                return;
            }
            
            // Obtener datos para la impresión
            const printData = getPrintData();
            
            // Preparar datos en formato JSON para enviar a Android
            const printDataJson = JSON.stringify(printData);
            
            showMessage("Enviando datos a la impresora Brother...");
            
            // Llamar a la función de Android para imprimir
            // Esta función será proporcionada por la WebView de Android
            const result = AndroidBrotherPrinter.printLabel(printDataJson);
            
            if (result === "SUCCESS") {
                showMessage("¡Impresión completada con éxito!");
            } else {
                showMessage("Error al imprimir: " + result, true);
            }
            
        } catch (error) {
            console.error("Error de impresión:", error);
            showMessage("Error al imprimir: " + error.message, true);
            
            // Intentar método alternativo
            if (typeof startBrotherPrinting === 'function') {
                showMessage("Intentando método alternativo de impresión...");
                startBrotherPrinting();
            }
        }
    };
    
    // Crear un botón de impresión Android si no existe
    function createAndroidPrintButton() {
        // Buscar el botón de Bluetooth
        const bluetoothButton = document.getElementById('bluetooth-print-btn');
        
        if (bluetoothButton) {
            // Crear un nuevo botón junto al de Bluetooth
            const androidButton = document.createElement('button');
            androidButton.id = 'android-print-btn';
            androidButton.className = bluetoothButton.className;
            androidButton.innerHTML = '<i class="bi bi-android me-2"></i>Imprimir con App Android';
            
            // Insertar después del botón de Bluetooth
            bluetoothButton.parentNode.insertBefore(androidButton, bluetoothButton.nextSibling);
            
            // Añadir espacio entre botones
            const spacer = document.createElement('span');
            spacer.className = 'mx-2';
            bluetoothButton.parentNode.insertBefore(spacer, androidButton);
            
            // Asignar evento
            androidButton.addEventListener('click', function(e) {
                e.preventDefault();
                startNativePrinting();
            });
            
            console.log("Botón de impresión Android creado");
        } else {
            console.log("No se encontró el botón de Bluetooth para referencia");
        }
    }
    
    // Asignar el evento al botón de impresión
    printButton.addEventListener('click', function(e) {
        e.preventDefault();
        startNativePrinting();
    });
    
    // Detector de API nativa de Android
    window.checkAndroidPrinterAPI = function() {
        if (typeof AndroidBrotherPrinter !== 'undefined') {
            console.log("API de impresora Brother para Android detectada");
            
            // Notificar a la app Android que estamos listos
            try {
                AndroidBrotherPrinter.onWebViewReady();
            } catch (e) {
                console.log("Error al notificar disponibilidad: ", e);
            }
            
            return true;
        }
        return false;
    };
    
    // Verificar periódicamente si la API de Android está disponible
    let apiCheckInterval = setInterval(function() {
        if (window.checkAndroidPrinterAPI()) {
            clearInterval(apiCheckInterval);
        }
    }, 1000);
    
    // Verificar inmediatamente
    window.checkAndroidPrinterAPI();
});
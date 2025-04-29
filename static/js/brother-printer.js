/**
 * Módulo de comunicación con impresoras Brother
 * Versión: 2.0 (2025-04-29)
 * 
 * Este archivo proporciona funciones básicas para la comunicación
 * con impresoras térmicas Brother vía diferentes interfaces.
 */

// Namespace para evitar colisiones con otras librerías
const BrotherPrinter = {
    // Constantes para comandos de impresión
    COMMANDS: {
        ESC: 0x1B,
        GS: 0x1D,
        INIT: function() { return [this.ESC, 0x40]; },
        CENTER: function() { return [this.ESC, 0x61, 0x01]; },
        LEFT: function() { return [this.ESC, 0x61, 0x00]; },
        BOLD_ON: function() { return [this.ESC, 0x45, 0x01]; },
        BOLD_OFF: function() { return [this.ESC, 0x45, 0x00]; },
        FONT_NORMAL: function() { return [this.GS, 0x21, 0x00]; },
        FONT_DOUBLE: function() { return [this.GS, 0x21, 0x11]; },
        LINE_FEED: function() { return [0x0A]; },
        CUT_PAPER: function() { return [this.GS, 0x56, 0x41, 0x10]; }
    },
    
    // Información sobre navegadores compatibles
    BROWSER_SUPPORT: {
        bluetooth: function() {
            return navigator.bluetooth !== undefined;
        },
        
        // Comprobar si estamos en un dispositivo móvil
        isMobile: function() {
            return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        },
        
        // Verificar compatibilidad general con impresoras
        isPrinterCompatible: function() {
            return this.bluetooth() && this.isMobile();
        }
    },
    
    // Función para generar comandos para una etiqueta
    generateLabelCommand: function(labelData) {
        // Inicializar el array de comandos
        let cmd = [];
        
        // Inicializar impresora
        cmd = cmd.concat(this.COMMANDS.INIT());
        
        // Título (nombre del producto) - centrado, grande y en negrita
        cmd = cmd.concat(this.COMMANDS.CENTER(), this.COMMANDS.FONT_DOUBLE(), this.COMMANDS.BOLD_ON());
        cmd = cmd.concat(Array.from(new TextEncoder().encode(labelData.productName || "")));
        cmd = cmd.concat(this.COMMANDS.LINE_FEED(), this.COMMANDS.LINE_FEED());
        
        // Tipo de conservación - centrado y negrita
        cmd = cmd.concat(this.COMMANDS.FONT_NORMAL(), this.COMMANDS.BOLD_ON());
        cmd = cmd.concat(Array.from(new TextEncoder().encode(labelData.conservationType || "")));
        cmd = cmd.concat(this.COMMANDS.LINE_FEED(), this.COMMANDS.LINE_FEED());
        
        // Información del preparador - alineado a la izquierda y normal
        cmd = cmd.concat(this.COMMANDS.LEFT(), this.COMMANDS.BOLD_OFF());
        cmd = cmd.concat(Array.from(new TextEncoder().encode(labelData.preparedBy || "")));
        cmd = cmd.concat(this.COMMANDS.LINE_FEED());
        
        // Fecha de inicio
        cmd = cmd.concat(Array.from(new TextEncoder().encode(labelData.startDate || "")));
        cmd = cmd.concat(this.COMMANDS.LINE_FEED());
        
        // Fecha de caducidad (en negrita)
        cmd = cmd.concat(this.COMMANDS.BOLD_ON());
        cmd = cmd.concat(Array.from(new TextEncoder().encode(labelData.expiryDate || "")));
        cmd = cmd.concat(this.COMMANDS.LINE_FEED());
        
        // Fecha de caducidad secundaria si existe
        if (labelData.secondaryExpiryDate) {
            cmd = cmd.concat(this.COMMANDS.BOLD_OFF());
            cmd = cmd.concat(Array.from(new TextEncoder().encode(labelData.secondaryExpiryDate)));
            cmd = cmd.concat(this.COMMANDS.LINE_FEED());
        }
        
        // Espacio final y corte de papel
        cmd = cmd.concat(
            this.COMMANDS.LINE_FEED(), 
            this.COMMANDS.LINE_FEED(),
            this.COMMANDS.LINE_FEED(),
            this.COMMANDS.CUT_PAPER()
        );
        
        return new Uint8Array(cmd);
    },
    
    // Información sobre parámetros actuales
    printerStatus: {
        lastConnectedDevice: null,
        isConnected: false,
        lastPrinterName: localStorage.getItem('lastBrotherPrinterName') || "",
        pendingJobs: 0
    },
    
    // Métodos para UI
    UI: {
        // Mostrar mensaje de compatibilidad con Web Bluetooth
        checkCompatibility: function(messageElement) {
            if (!messageElement) return false;
            
            if (!BrotherPrinter.BROWSER_SUPPORT.bluetooth()) {
                messageElement.textContent = "Tu navegador no soporta Bluetooth para impresoras. Intenta usar Chrome en tu tablet.";
                messageElement.style.display = "block";
                messageElement.className = "alert alert-warning";
                return false;
            }
            
            if (!BrotherPrinter.BROWSER_SUPPORT.isMobile()) {
                messageElement.textContent = "La impresión Bluetooth funciona mejor en tablets y dispositivos móviles.";
                messageElement.style.display = "block";
                messageElement.className = "alert alert-info";
                return true; // Sigue siendo compatible, solo es una advertencia
            }
            
            return true;
        },
        
        // Mostrar información de la última impresora usada
        showLastPrinterInfo: function(container) {
            if (!container || !BrotherPrinter.printerStatus.lastPrinterName) return;
            
            let infoElement = document.createElement('p');
            infoElement.className = "text-muted small";
            infoElement.innerHTML = `<i class="bi bi-printer"></i> Última impresora: ${BrotherPrinter.printerStatus.lastPrinterName}`;
            container.appendChild(infoElement);
        }
    }
};

// Auto-inicialización
document.addEventListener('DOMContentLoaded', function() {
    console.log("Módulo Brother Printer inicializado");
    
    // Verificar compatibilidad con Web Bluetooth API en consola
    console.log("¿Es compatible con Web Bluetooth?", BrotherPrinter.BROWSER_SUPPORT.bluetooth());
    console.log("¿Es dispositivo móvil?", BrotherPrinter.BROWSER_SUPPORT.isMobile());
    
    // Esta librería base no hace nada automáticamente, solo proporciona 
    // funcionalidades para ser usadas por brother-bluetooth.js y brother-label-pwa.js
});
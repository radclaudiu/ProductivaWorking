/**
 * Módulo para impresión directa en impresoras Brother TD-4550DNWB desde Android
 * Versión 2.0 (30/05/2025) - Protocolo Brother SDK integrado
 * 
 * Este script implementa el protocolo Brother completo para WebView Android
 * con capacidades de búsqueda Bluetooth, conexión y impresión de etiquetas 44x38mm
 */

// Callbacks globales para recibir respuestas del sistema Android
window.BrotherPrint = {
    // Se llama cuando se encuentran impresoras
    onPrintersFound: function(printersJsonString) {
        const printers = JSON.parse(printersJsonString);
        if (printers.length > 0) {
            // Mostrar lista al usuario o conectar automáticamente
            printers.forEach(printer => {
                console.log(`Encontrada: ${printer.name} - ${printer.address}`);
            });
            
            // Conectar a la primera
            AndroidBridge.connectPrinter(printers[0].address);
        }
        
        if (brotherBridge) {
            brotherBridge.handlePrintersFound(printersJsonString);
        }
    },
    
    // Se llama cuando la impresora se conecta
    onPrinterConnected: function() {
        console.log('Impresora conectada');
        if (brotherBridge) {
            brotherBridge.handlePrinterConnected();
        }
    },
    
    // Se llama cuando la impresión es exitosa
    onPrintSuccess: function() {
        console.log('Impresión completada');
        alert('Etiqueta impresa correctamente');
        if (brotherBridge) {
            brotherBridge.handlePrintSuccess();
        }
    },
    
    // Se llama cuando hay un error
    onPrintError: function(errorMessage) {
        console.error('Error:', errorMessage);
        alert('Error al imprimir: ' + errorMessage);
        if (brotherBridge) {
            brotherBridge.handlePrintError(errorMessage);
        }
    }
};

// Variable global para el bridge
let brotherBridge;

// Clase principal para el bridge de Brother Print
class BrotherPrintBridge {
    constructor() {
        this.isAndroidApp = typeof AndroidBridge !== 'undefined';
        this.connectedPrinter = null;
        this.availablePrinters = [];
        this.isSearching = false;
        
        // Los callbacks globales ya están definidos arriba
        
        console.log("Brother Print Bridge inicializado", {
            isAndroidApp: this.isAndroidApp,
            androidBridge: typeof AndroidBridge
        });
    }
    
    // Buscar impresoras Brother TD-4550DNWB disponibles via Bluetooth
    searchPrinters() {
        if (!this.isAndroidApp) {
            this.showMessage('Esta función solo está disponible en la app Android', true);
            return;
        }
        
        if (this.isSearching) {
            this.showMessage('Búsqueda ya en progreso...', false);
            return;
        }
        
        this.isSearching = true;
        this.showMessage('Buscando impresoras Brother TD-4550DNWB...');
        
        try {
            AndroidBridge.searchPrinters();
        } catch (error) {
            console.error('Error al buscar impresoras:', error);
            this.showMessage('Error al buscar impresoras: ' + error.message, true);
            this.isSearching = false;
        }
    }
    
    // Conectar a impresora específica por MAC address
    connectToPrinter(macAddress) {
        if (!this.isAndroidApp) {
            this.showMessage('Esta función solo está disponible en la app Android', true);
            return;
        }
        
        if (!macAddress) {
            this.showMessage('Seleccione una impresora para conectar', true);
            return;
        }
        
        this.showMessage('Conectando a impresora Brother...');
        
        try {
            AndroidBridge.connectPrinter(macAddress);
        } catch (error) {
            console.error('Error al conectar impresora:', error);
            this.showMessage('Error al conectar: ' + error.message, true);
        }
    }
    
    // Imprimir etiqueta usando datos del DOM
    printLabel() {
        // 1. Verificar si AndroidBridge está disponible
        if (typeof AndroidBridge === 'undefined') {
            alert('Esta función solo está disponible en la app Android');
            return;
        }
        
        // 2. Verificar estado de la impresora
        try {
            const status = JSON.parse(AndroidBridge.getPrinterStatus());
            if (!status.connected && !status.configured) {
                // No hay impresora, buscar una
                AndroidBridge.searchPrinters();
                // Esperar callback onPrintersFound
                return;
            }
        } catch (error) {
            // Si no existe getPrinterStatus, buscar impresoras directamente
            AndroidBridge.searchPrinters();
            return;
        }
        
        // 3. Obtener datos y generar etiqueta
        const labelData = this.getLabelData();
        if (!labelData) {
            this.showMessage('Error al obtener datos de la etiqueta', true);
            return;
        }
        
        // 4. Imprimir
        this.imprimirEtiqueta(labelData);
    }
    
    // Función para imprimir etiqueta con el flujo completo
    imprimirEtiqueta(datos) {
        const canvas = this.generarEtiqueta(datos);
        const base64 = canvas.toDataURL('image/png').replace('data:image/png;base64,', '');
        
        // Enviar a imprimir
        try {
            this.showMessage('Enviando etiqueta a impresora...');
            AndroidBridge.printImage(base64);
        } catch (error) {
            console.error('Error al enviar a impresora:', error);
            this.showMessage('Error al imprimir: ' + error.message, true);
        }
    }
    
    // Función para generar etiqueta según especificaciones
    generarEtiqueta(datos) {
        const canvas = document.createElement('canvas');
        canvas.width = 520;
        canvas.height = 449;
        const ctx = canvas.getContext('2d');
        
        // Fondo blanco
        ctx.fillStyle = 'white';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        
        // Dibujar etiqueta
        ctx.fillStyle = 'black';
        ctx.font = 'bold 32px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(datos.productName, canvas.width / 2, 80);
        
        ctx.font = 'bold 24px Arial';
        ctx.fillText(datos.conservationType, canvas.width / 2, 130);
        
        ctx.font = '18px Arial';
        ctx.textAlign = 'left';
        ctx.fillText(datos.preparedBy, 40, 200);
        ctx.fillText(datos.startDate, 40, 240);
        
        ctx.font = 'bold 20px Arial';
        ctx.fillText(datos.expiryDate, 40, 290);
        
        // Borde
        ctx.strokeStyle = 'black';
        ctx.lineWidth = 3;
        ctx.strokeRect(10, 10, 500, 429);
        
        return canvas;
    }
    
    // Obtener datos de la etiqueta desde el DOM
    getLabelData() {
        try {
            const productName = document.getElementById("product-name")?.textContent?.trim() || "";
            const conservationType = document.getElementById("conservation-type")?.textContent?.trim() || "";
            const preparedBy = document.getElementById("prepared-by")?.textContent?.trim() || "";
            const startDate = document.getElementById("start-date")?.textContent?.trim() || "";
            const expiryDate = document.getElementById("expiry-date")?.textContent?.trim() || "";
            const secondaryExpiryDate = document.getElementById("secondary-expiry-date")?.textContent?.trim() || "";
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
        } catch (error) {
            console.error('Error al obtener datos de etiqueta:', error);
            return null;
        }
    }
    
    // Generar imagen de etiqueta optimizada para Brother TD-4550DNWB (44x38mm)
    generateLabelImage(labelData) {
        return new Promise((resolve, reject) => {
            try {
                // PASO 1: Generar la etiqueta en un canvas
                const canvas = document.createElement('canvas');
                canvas.width = 520;   // 44mm a 300 DPI
                canvas.height = 449;  // 38mm a 300 DPI
                const ctx = canvas.getContext('2d');
                
                // PASO 2: Dibujar la etiqueta
                ctx.fillStyle = 'white';
                ctx.fillRect(0, 0, canvas.width, canvas.height);
                
                // Configuración de texto
                ctx.fillStyle = 'black';
                ctx.textAlign = 'center';
                
                // Título del producto (tamaño más grande)
                ctx.font = 'bold 32px Arial';
                const productLines = this.wrapText(ctx, labelData.productName.toUpperCase(), canvas.width - 20);
                let yPos = 60;
                productLines.forEach(line => {
                    ctx.fillText(line, canvas.width / 2, yPos);
                    yPos += 36;
                });
                
                // Tipo de conservación
                yPos += 15;
                ctx.font = 'bold 24px Arial';
                ctx.fillStyle = 'black';
                ctx.fillText(labelData.conservationType, canvas.width / 2, yPos);
                
                // Empleado que preparó
                yPos += 40;
                ctx.font = '18px Arial';
                ctx.fillStyle = 'black';
                ctx.fillText(labelData.preparedBy, canvas.width / 2, yPos);
                
                // Fecha de inicio
                yPos += 35;
                ctx.font = '16px Arial';
                ctx.fillText(labelData.startDate, canvas.width / 2, yPos);
                
                // Fecha de caducidad (destacada)
                yPos += 45;
                ctx.font = 'bold 20px Arial';
                ctx.fillStyle = 'black';
                // Crear rectángulo para la fecha de caducidad
                const caducidadText = labelData.expiryDate;
                const textWidth = ctx.measureText(caducidadText).width;
                const rectWidth = textWidth + 24;
                const rectHeight = 32;
                const rectX = (canvas.width - rectWidth) / 2;
                const rectY = yPos - 24;
                
                ctx.strokeStyle = 'black';
                ctx.lineWidth = 2;
                ctx.strokeRect(rectX, rectY, rectWidth, rectHeight);
                ctx.fillText(caducidadText, canvas.width / 2, yPos);
                
                // Fecha de caducidad secundaria si existe
                if (labelData.secondaryExpiryDate) {
                    yPos += 40;
                    ctx.font = '16px Arial';
                    ctx.fillStyle = '#666666';
                    ctx.fillText(labelData.secondaryExpiryDate, canvas.width / 2, yPos);
                }
                
                // PASO 3: Convertir a base64 (IMPORTANTE: quitar el prefijo)
                const base64 = canvas.toDataURL('image/png').replace('data:image/png;base64,', '');
                resolve(base64);
                
            } catch (error) {
                reject(error);
            }
        });
    }
    
    // Función auxiliar para dividir texto en líneas
    wrapText(context, text, maxWidth) {
        const words = text.split(' ');
        const lines = [];
        let currentLine = words[0];
        
        for (let i = 1; i < words.length; i++) {
            const word = words[i];
            const width = context.measureText(currentLine + " " + word).width;
            if (width < maxWidth) {
                currentLine += " " + word;
            } else {
                lines.push(currentLine);
                currentLine = word;
            }
        }
        lines.push(currentLine);
        return lines;
    }
    
    // Callbacks para comunicación con Android
    handlePrintersFound(printersJson) {
        try {
            this.availablePrinters = JSON.parse(printersJson);
            this.isSearching = false;
            console.log("Impresoras encontradas:", this.availablePrinters);
            
            // Flujo de primera conexión automática - ya implementado en window.BrotherPrint.onPrintersFound
            // Este método es para compatibilidad con el bridge interno
            if (this.availablePrinters.length === 0) {
                this.showMessage('No se encontraron impresoras Brother TD-4550DNWB disponibles', true);
            }
        } catch (e) {
            console.error('Error parsing printers:', e);
            this.showMessage('Error al procesar lista de impresoras', true);
            this.isSearching = false;
        }
    }
    
    handlePrinterConnected() {
        this.connectedPrinter = true;
        this.showMessage('Impresora Brother TD-4550DNWB conectada correctamente');
        this.hidePrinterSelection();
    }
    
    handlePrintSuccess() {
        this.showMessage('Etiqueta impresa correctamente');
        
        // Auto retorno después de impresión exitosa (2 segundos)
        setTimeout(() => {
            if (window.history.length > 1) {
                window.history.back();
            } else {
                window.location.href = '/tasks/local-user/labels';
            }
        }, 2000);
    }
    
    handlePrintError(error) {
        this.showMessage('Error al imprimir: ' + error, true);
        console.error('Print error:', error);
    }
    
    // Mostrar interfaz de selección de impresoras
    showPrinterSelection() {
        const messageArea = document.getElementById('print-message');
        if (!messageArea) return;
        
        if (this.availablePrinters.length === 0) {
            messageArea.innerHTML = `
                <div class="alert alert-warning">
                    <strong>No se encontraron impresoras Brother TD-4550DNWB</strong><br>
                    Asegúrese de que la impresora esté encendida y en modo de emparejamiento Bluetooth.
                    <button onclick="brotherBridge.searchPrinters()" class="btn btn-primary btn-sm mt-2">
                        <i class="bi bi-search"></i> Buscar de nuevo
                    </button>
                </div>
            `;
            return;
        }
        
        let html = '<div class="alert alert-info"><strong>Seleccione su impresora Brother TD-4550DNWB:</strong></div>';
        html += '<div class="mb-3">';
        
        this.availablePrinters.forEach(printer => {
            html += `
                <button onclick="brotherBridge.connectToPrinter('${printer.macAddress}')" 
                        class="btn btn-outline-primary me-2 mb-2">
                    <i class="bi bi-printer"></i> ${printer.modelName || 'Brother TD-4550DNWB'}<br>
                    <small>${printer.macAddress}</small>
                </button>
            `;
        });
        
        html += `
            <button onclick="brotherBridge.searchPrinters()" class="btn btn-secondary mb-2">
                <i class="bi bi-arrow-clockwise"></i> Buscar de nuevo
            </button>
        `;
        html += '</div>';
        
        messageArea.innerHTML = html;
    }
    
    hidePrinterSelection() {
        const messageArea = document.getElementById('print-message');
        if (messageArea) {
            messageArea.innerHTML = `
                <div class="alert alert-success">
                    <i class="bi bi-check-circle"></i> Impresora conectada. 
                    <button onclick="brotherBridge.printLabel()" class="btn btn-success btn-sm ms-2">
                        <i class="bi bi-printer"></i> Imprimir Etiqueta
                    </button>
                </div>
            `;
        }
    }
    
    // Función para mostrar mensajes
    showMessage(message, isError = false) {
        const messageArea = document.getElementById('print-message');
        if (!messageArea) return;
        
        messageArea.style.display = "block";
        messageArea.innerHTML = `
            <div class="alert ${isError ? 'alert-danger' : 'alert-info'}">
                ${isError ? '<i class="bi bi-exclamation-triangle"></i>' : '<i class="bi bi-info-circle"></i>'} 
                ${message}
            </div>
        `;
        
        // Auto-hide non-error messages
        if (!isError) {
            setTimeout(() => {
                if (messageArea.textContent.includes(message)) {
                    messageArea.style.display = "none";
                }
            }, 5000);
        }
    }
}

// Instancia global del bridge
let brotherBridge;

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    console.log("Inicializando sistema de impresión Brother TD-4550DNWB para Android");
    
    // Crear instancia del bridge
    brotherBridge = new BrotherPrintBridge();
    
    // Verificar botón API Brother TD-4550DNWB
    const brotherApiPrintBtn = document.getElementById('brother-api-print-btn');
    if (brotherApiPrintBtn) {
        brotherApiPrintBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            if (!brotherBridge.isAndroidApp) {
                brotherBridge.showMessage('Para usar la API Brother TD-4550DNWB, abra esta página en la aplicación Android', true);
                return;
            }
            
            // Usar directamente la API Brother sin verificar conexión previa
            brotherBridge.showMessage('Conectando con impresora Brother TD-4550DNWB...');
            
            // Buscar y conectar automáticamente
            brotherBridge.searchPrinters();
        });
    }
    
    // Verificar botón Android Brother TD-4550DNWB
    const brotherAndroidPrintBtn = document.getElementById('brother-android-print-btn');
    if (brotherAndroidPrintBtn) {
        brotherAndroidPrintBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            if (!brotherBridge.isAndroidApp) {
                brotherBridge.showMessage('Esta función solo está disponible en la aplicación Android', true);
                return;
            }
            
            // Si no hay impresora conectada, buscar primero
            if (!brotherBridge.connectedPrinter) {
                brotherBridge.searchPrinters();
            } else {
                // Si ya hay impresora conectada, imprimir directamente
                const quantity = parseInt(document.getElementById('quantity-selector')?.value || "1");
                
                // Imprimir múltiples etiquetas si es necesario
                for (let i = 0; i < quantity; i++) {
                    setTimeout(() => {
                        brotherBridge.printLabel();
                    }, i * 1000); // Delay de 1 segundo entre etiquetas
                }
            }
        });
    }
    
    // Soporte para el botón de Android legacy si existe
    const androidPrintBtn = document.getElementById('android-print-btn');
    if (androidPrintBtn) {
        androidPrintBtn.addEventListener('click', function(e) {
            e.preventDefault();
            brotherBridge.searchPrinters();
        });
    }
    
    // Auto-búsqueda si estamos en Android app
    if (brotherBridge.isAndroidApp) {
        console.log("Aplicación Android detectada, interfaz Brother lista");
        brotherBridge.showMessage('Aplicación Android detectada. Toque "Imprimir en TD-4550DNWB" para comenzar.');
    } else {
        console.log("No se detectó aplicación Android");
        brotherBridge.showMessage('Para usar la impresora Brother TD-4550DNWB, abra esta página en la aplicación Android Productiva.', true);
    }
});
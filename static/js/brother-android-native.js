/**
 * Módulo para impresión directa en impresoras Brother TD-4550DNWB desde Android
 * Versión 2.0 (30/05/2025) - Protocolo Brother SDK integrado
 * 
 * Este script implementa el protocolo Brother completo para WebView Android
 * con capacidades de búsqueda Bluetooth, conexión y impresión de etiquetas 35x40mm
 */

// Clase principal para el bridge de Brother Print
class BrotherPrintBridge {
    constructor() {
        this.isAndroidApp = typeof AndroidBridge !== 'undefined';
        this.connectedPrinter = null;
        this.availablePrinters = [];
        this.isSearching = false;
        
        // Configurar callbacks globales para comunicación con Android
        window.BrotherPrint = {
            onPrintersFound: (printersJson) => this.handlePrintersFound(printersJson),
            onPrinterConnected: () => this.handlePrinterConnected(),
            onPrintSuccess: () => this.handlePrintSuccess(),
            onPrintError: (error) => this.handlePrintError(error)
        };
        
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
        if (!this.isAndroidApp) {
            this.showMessage('Esta función solo está disponible en la app Android', true);
            return;
        }
        
        if (!this.connectedPrinter) {
            this.showMessage('Conecte una impresora Brother TD-4550DNWB primero', true);
            this.showPrinterSelection();
            return;
        }
        
        // Obtener datos de la etiqueta
        const labelData = this.getLabelData();
        if (!labelData) {
            this.showMessage('Error al obtener datos de la etiqueta', true);
            return;
        }
        
        // Generar imagen de la etiqueta (35x40mm)
        this.generateLabelImage(labelData)
            .then(imageBase64 => {
                this.showMessage('Enviando etiqueta a impresora...');
                try {
                    AndroidBridge.printImage(imageBase64);
                } catch (error) {
                    console.error('Error al enviar a impresora:', error);
                    this.showMessage('Error al imprimir: ' + error.message, true);
                }
            })
            .catch(error => {
                console.error('Error al generar imagen:', error);
                this.showMessage('Error al generar imagen de etiqueta: ' + error.message, true);
            });
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
    
    // Generar imagen de etiqueta optimizada para Brother TD-4550DNWB (35x40mm)
    generateLabelImage(labelData) {
        return new Promise((resolve, reject) => {
            try {
                // Crear canvas con las dimensiones exactas para 35x40mm a 300 DPI
                const canvas = document.createElement('canvas');
                const ctx = canvas.getContext('2d');
                
                // 35mm x 40mm a 300 DPI = 413 x 472 pixels aproximadamente
                const width = 413;
                const height = 472;
                
                canvas.width = width;
                canvas.height = height;
                
                // Fondo blanco
                ctx.fillStyle = '#FFFFFF';
                ctx.fillRect(0, 0, width, height);
                
                // Configuración de texto
                ctx.fillStyle = '#000000';
                ctx.textAlign = 'center';
                
                // Título del producto (tamaño más grande)
                ctx.font = 'bold 28px Arial';
                const productLines = this.wrapText(ctx, labelData.productName.toUpperCase(), width - 20);
                let yPos = 40;
                productLines.forEach(line => {
                    ctx.fillText(line, width / 2, yPos);
                    yPos += 32;
                });
                
                // Tipo de conservación
                yPos += 10;
                ctx.font = 'bold 20px Arial';
                ctx.fillStyle = '#333333';
                ctx.fillText(labelData.conservationType, width / 2, yPos);
                
                // Empleado que preparó
                yPos += 35;
                ctx.font = '16px Arial';
                ctx.fillStyle = '#000000';
                ctx.fillText(labelData.preparedBy, width / 2, yPos);
                
                // Fecha de inicio
                yPos += 30;
                ctx.font = '14px Arial';
                ctx.fillText(labelData.startDate, width / 2, yPos);
                
                // Fecha de caducidad (destacada)
                yPos += 40;
                ctx.font = 'bold 18px Arial';
                ctx.fillStyle = '#000000';
                // Crear rectángulo para la fecha de caducidad
                const caducidadText = labelData.expiryDate;
                const textWidth = ctx.measureText(caducidadText).width;
                const rectWidth = textWidth + 20;
                const rectHeight = 30;
                const rectX = (width - rectWidth) / 2;
                const rectY = yPos - 20;
                
                ctx.strokeStyle = '#000000';
                ctx.lineWidth = 2;
                ctx.strokeRect(rectX, rectY, rectWidth, rectHeight);
                ctx.fillText(caducidadText, width / 2, yPos);
                
                // Fecha de caducidad secundaria si existe
                if (labelData.secondaryExpiryDate) {
                    yPos += 35;
                    ctx.font = '14px Arial';
                    ctx.fillStyle = '#666666';
                    ctx.fillText(labelData.secondaryExpiryDate, width / 2, yPos);
                }
                
                // Convertir canvas a base64
                const imageBase64 = canvas.toDataURL('image/png').split(',')[1];
                resolve(imageBase64);
                
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
            this.showPrinterSelection();
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
    
    // Verificar botón principal de impresión Brother Android
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
/**
 * Módulo mejorado para impresión de etiquetas Brother en tablets
 * Versión 2.0 (29/04/2025)
 * 
 * Este script está diseñado específicamente para trabajar con impresoras
 * Brother QL/PT usando Web Bluetooth API en dispositivos tablet modernos,
 * priorizando la experiencia de usuario en entornos táctiles.
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log("Inicializando sistema de impresora Brother para tablets");
    
    // Variables para recordar la última impresora usada
    let lastConnectedPrinter = localStorage.getItem('lastBrotherPrinter');
    let lastConnectedPrinterName = localStorage.getItem('lastBrotherPrinterName');
    
    // Verificar si el botón está presente
    const printButton = document.getElementById('bluetooth-print-btn');
    const messageArea = document.getElementById('print-message');
    
    if (!printButton) {
        console.log("No se encontró el botón de impresión en la página");
        return;
    }
    
    // Mostrar información de la última impresora conectada si existe
    if (lastConnectedPrinterName) {
        let useLastText = "Última impresora: " + lastConnectedPrinterName;
        
        // Agregar indicador de última impresora si no existe
        let lastPrinterInfo = document.getElementById('last-printer-info');
        if (!lastPrinterInfo) {
            lastPrinterInfo = document.createElement('p');
            lastPrinterInfo.id = 'last-printer-info';
            lastPrinterInfo.className = 'text-white-50 text-center small mt-2';
            lastPrinterInfo.innerHTML = '<i class="bi bi-printer-fill me-1"></i> ' + useLastText;
            
            // Insertar después del área de mensajes
            if (messageArea) {
                messageArea.parentNode.insertBefore(lastPrinterInfo, messageArea.nextSibling);
            } else if (printButton) {
                printButton.parentNode.parentNode.appendChild(lastPrinterInfo);
            }
        } else {
            lastPrinterInfo.innerHTML = '<i class="bi bi-printer-fill me-1"></i> ' + useLastText;
        }
    }
    
    // Función para mostrar mensajes
    const showMessage = (message, isError = false) => {
        if (!messageArea) return;
        
        messageArea.style.display = "block";
        messageArea.textContent = message;
        messageArea.className = isError ? 
            "alert alert-danger mt-3" : 
            "alert alert-info mt-3";
        
        // Añadir un tamaño de letra más grande para tablets
        messageArea.style.fontSize = "1.1rem";
        
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
    
    // Generar los comandos específicos para las impresoras Brother
    const generateBrotherCommand = (data) => {
        // Comandos ESC/POS para impresoras Brother
        const ESC = 0x1B;
        const GS = 0x1D;
        const INIT = [ESC, 0x40]; // Inicializar impresora
        const ALIGN_CENTER = [ESC, 0x61, 0x01]; // Centrar texto
        const ALIGN_LEFT = [ESC, 0x61, 0x00]; // Alinear a la izquierda
        const BOLD_ON = [ESC, 0x45, 0x01]; // Activar negrita
        const BOLD_OFF = [ESC, 0x45, 0x00]; // Desactivar negrita
        const FONT_SIZE_NORMAL = [GS, 0x21, 0x00]; // Tamaño normal
        const FONT_SIZE_DOUBLE = [GS, 0x21, 0x11]; // Doble alto y ancho
        const LINE_FEED = [0x0A]; // Salto de línea
        const CUT_PAPER = [GS, 0x56, 0x41, 0x10]; // Cortar papel
        
        // Construir los comandos completos
        let command = [];
        
        // Inicializar la impresora
        command = command.concat(INIT);
        
        // Título del producto (en grande y negrita)
        command = command.concat(ALIGN_CENTER, FONT_SIZE_DOUBLE, BOLD_ON);
        command = command.concat(Array.from(new TextEncoder().encode(data.productName)));
        command = command.concat(LINE_FEED, LINE_FEED);
        
        // Tipo de conservación (centrado y negrita)
        command = command.concat(FONT_SIZE_NORMAL, BOLD_ON);
        command = command.concat(Array.from(new TextEncoder().encode(data.conservationType)));
        command = command.concat(LINE_FEED, LINE_FEED);
        
        // Información del empleado
        command = command.concat(ALIGN_LEFT, BOLD_OFF);
        command = command.concat(Array.from(new TextEncoder().encode(data.preparedBy)));
        command = command.concat(LINE_FEED);
        
        // Fecha de inicio
        command = command.concat(Array.from(new TextEncoder().encode(data.startDate)));
        command = command.concat(LINE_FEED);
        
        // Fecha de caducidad (en negrita)
        command = command.concat(BOLD_ON);
        command = command.concat(Array.from(new TextEncoder().encode(data.expiryDate)));
        command = command.concat(LINE_FEED);
        
        // Fecha de caducidad secundaria si existe
        if (data.secondaryExpiryDate) {
            command = command.concat(BOLD_OFF);
            command = command.concat(Array.from(new TextEncoder().encode(data.secondaryExpiryDate)));
            command = command.concat(LINE_FEED);
        }
        
        // Espacio final y cortar papel
        command = command.concat(LINE_FEED, LINE_FEED, LINE_FEED, CUT_PAPER);
        
        return new Uint8Array(command);
    };
    
    // Función principal para iniciar la impresión
    const startBrotherPrinting = async () => {
        try {
            // Verificar si el navegador soporta Web Bluetooth API
            if (!navigator.bluetooth) {
                showMessage("Tu navegador no soporta Bluetooth. Por favor, utiliza Chrome para Android o iOS reciente", true);
                return;
            }
            
            // Verificar si hay una última impresora conocida
            let device;
            
            // Solicitar dispositivo Bluetooth
            showMessage("Buscando impresoras Brother... Selecciona tu impresora de la lista");
            
            // Solicitar dispositivo Bluetooth - usando filtros más permisivos para mejor compatibilidad
            device = await navigator.bluetooth.requestDevice({
                // Aceptar cualquier dispositivo Bluetooth para máxima compatibilidad
                acceptAllDevices: true,
                // Incluir servicios opcionales para ampliar compatibilidad con diferentes modelos
                optionalServices: [
                    // Servicios genéricos
                    'generic_access',
                    'battery_service',
                    
                    // Servicios de impresora Brother conocidos
                    '000018f0-0000-1000-8000-00805f9b34fb',  // Servicio principal Brother
                    '1222e5db-36e1-4a53-bfef-bf7da833c5a5',  // Servicio alternativo
                    '00001101-0000-1000-8000-00805f9b34fb',  // Serial Port Profile
                    
                    // Servicios genéricos Bluetooth
                    '00001800-0000-1000-8000-00805f9b34fb',  // Generic Access
                    '00001801-0000-1000-8000-00805f9b34fb',  // Generic Attribute
                    '0000180a-0000-1000-8000-00805f9b34fb',  // Device Information
                    '0000180f-0000-1000-8000-00805f9b34fb',  // Battery Service
                    
                    // Servicios adicionales para impresoras térmicas
                    'e7810a71-73ae-499d-8c15-faa9aef0c3f2',  // Thermal printer
                    'af20fbac-2518-4998-9af7-af42540731b3',  // Alternate printer
                    'bef8d6c9-9c21-4c9e-b632-bd58c1009f9f',  // Data transfer
                    
                    // Rangos SPP adicionales
                    '0000110a-0000-1000-8000-00805f9b34fb',
                    '0000110b-0000-1000-8000-00805f9b34fb',
                    '0000110c-0000-1000-8000-00805f9b34fb',
                    '0000110d-0000-1000-8000-00805f9b34fb',
                    '0000110e-0000-1000-8000-00805f9b34fb',
                    '0000110f-0000-1000-8000-00805f9b34fb'
                ]
            });
            
            // Guardar referencia a la última impresora
            localStorage.setItem('lastBrotherPrinter', device.id || '');
            localStorage.setItem('lastBrotherPrinterName', device.name || 'Impresora Brother');
            
            // Actualizar información de la última impresora
            let lastPrinterInfo = document.getElementById('last-printer-info');
            if (lastPrinterInfo) {
                lastPrinterInfo.innerHTML = '<i class="bi bi-printer-fill me-1"></i> Última impresora: ' + device.name;
            }
            
            showMessage(`Conectando a ${device.name}...`);
            
            // Conectar al dispositivo GATT
            const server = await device.gatt.connect();
            
            // Lista de servicios a probar para encontrar el correcto
            const serviceIds = [
                '000018f0-0000-1000-8000-00805f9b34fb',  // Brother principal
                '1222e5db-36e1-4a53-bfef-bf7da833c5a5',  // Alternativo
                '00001101-0000-1000-8000-00805f9b34fb',  // SPP (Serial Port Profile)
                'e7810a71-73ae-499d-8c15-faa9aef0c3f2',  // Thermal printer
                'af20fbac-2518-4998-9af7-af42540731b3'   // Alternate printer
            ];
            
            // Obtener el servicio correcto
            let service = null;
            
            // Probar obtener todos los servicios primero
            try {
                const services = await server.getPrimaryServices();
                console.log("Servicios disponibles:", services.map(s => s.uuid));
                
                // Buscar uno con características de escritura
                for (const svc of services) {
                    try {
                        const chars = await svc.getCharacteristics();
                        for (const char of chars) {
                            if (char.properties.write || char.properties.writeWithoutResponse) {
                                service = svc;
                                console.log(`Encontrado servicio con características de escritura: ${svc.uuid}`);
                                break;
                            }
                        }
                        if (service) break;
                    } catch (e) {
                        console.log(`Error al obtener características para ${svc.uuid}:`, e);
                    }
                }
            } catch (e) {
                console.log("No se pudo obtener lista completa de servicios:", e);
            }
            
            // Si no encontramos un servicio adecuado, probar los específicos
            if (!service) {
                for (const serviceId of serviceIds) {
                    try {
                        service = await server.getPrimaryService(serviceId);
                        console.log(`Servicio encontrado: ${serviceId}`);
                        break;
                    } catch (e) {
                        console.log(`Servicio ${serviceId} no disponible:`, e);
                    }
                }
            }
            
            if (!service) {
                throw new Error("No se encontró un servicio compatible en la impresora");
            }
            
            // Obtener características de escritura
            const characteristics = await service.getCharacteristics();
            console.log("Características disponibles:", characteristics.length);
            
            // Encontrar una característica que permita escritura
            let characteristic = null;
            for (const char of characteristics) {
                if (char.properties.write || char.properties.writeWithoutResponse) {
                    characteristic = char;
                    console.log(`Característica de escritura encontrada: ${char.uuid}`);
                    break;
                }
            }
            
            if (!characteristic) {
                throw new Error("No se encontró una característica de escritura");
            }
            
            // Obtener datos para la impresión
            const printData = getPrintData();
            const command = generateBrotherCommand(printData);
            
            showMessage(`Enviando datos a la impresora ${device.name}...`);
            
            // Imprimir la cantidad de etiquetas solicitada
            for (let i = 0; i < printData.quantity; i++) {
                // Esperar entre impresiones
                if (i > 0) {
                    await new Promise(resolve => setTimeout(resolve, 1000));
                }
                
                // Imprimir etiqueta
                if (characteristic.properties.writeWithoutResponse) {
                    await characteristic.writeValueWithoutResponse(command);
                } else {
                    await characteristic.writeValue(command);
                }
                
                showMessage(`Imprimiendo etiqueta ${i+1} de ${printData.quantity}...`);
            }
            
            showMessage("¡Impresión completada con éxito!");
            
            // Desconectar del dispositivo después de un breve retraso
            setTimeout(() => {
                try {
                    if (device.gatt.connected) {
                        device.gatt.disconnect();
                        console.log("Desconectado de la impresora");
                    }
                } catch (e) {
                    console.log("Error al desconectar:", e);
                }
            }, 2000);
            
        } catch (error) {
            // Manejar errores específicos
            let errorMessage = "Error de conexión Bluetooth: ";
            
            if (error.name === 'NotFoundError') {
                errorMessage += "No se encontraron dispositivos compatibles";
            } else if (error.name === 'SecurityError') {
                errorMessage += "No se concedió permiso para acceder a Bluetooth";
            } else if (error.name === 'NetworkError') {
                errorMessage += "Error de comunicación con la impresora";
            } else if (error.message) {
                errorMessage += error.message;
            } else {
                errorMessage += "Error desconocido";
            }
            
            console.error("Error de impresión:", error);
            showMessage(errorMessage, true);
        }
    };
    
    // Asignar el evento al botón de impresión
    printButton.addEventListener('click', function(e) {
        e.preventDefault();
        startBrotherPrinting();
    });
    
    // Feedback visual al pulsar botones en tablets
    const addTouchFeedback = (element) => {
        if (!element) return;
        
        element.addEventListener('touchstart', function() {
            this.style.transform = 'scale(0.95)';
            this.style.opacity = '0.9';
        });
        
        ['touchend', 'touchcancel'].forEach(evt => {
            element.addEventListener(evt, function() {
                this.style.transform = 'scale(1)';
                this.style.opacity = '1';
            });
        });
    };
    
    // Aplicar feedback táctil al botón principal
    addTouchFeedback(printButton);
    
    console.log("Sistema de impresión Brother para tablets inicializado correctamente");
});
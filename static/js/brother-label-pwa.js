// Brother Label PWA - Versión mejorada para acceso Bluetooth en tablets
document.addEventListener('DOMContentLoaded', function() {
    // Verificar si estamos en un contexto PWA o standalone
    const isPwa = window.matchMedia('(display-mode: standalone)').matches || 
                  window.navigator.standalone || 
                  document.referrer.includes('android-app://');
    
    // Verificar si estamos en una tablet
    const isTablet = /iPad|Android(?!.*Mobile)|Tablet/i.test(navigator.userAgent);
    
    console.log("PWA Status:", isPwa, "Tablet Status:", isTablet);
    
    // Elemento de mensajes y estado
    const createStatusElement = () => {
        const existingStatus = document.getElementById('printer-status');
        if (existingStatus) return existingStatus;
        
        const statusElement = document.createElement('div');
        statusElement.id = 'printer-status';
        statusElement.className = 'alert alert-info mt-3';
        statusElement.style.display = 'none';
        
        // Añadir después de los controles de impresión
        const printControls = document.querySelector('.print-controls');
        if (printControls) {
            printControls.appendChild(statusElement);
        } else {
            document.body.appendChild(statusElement);
        }
        
        return statusElement;
    };
    
    // Mostrar estado
    const showStatus = (message, type = 'info') => {
        const statusElement = createStatusElement();
        statusElement.textContent = message;
        statusElement.className = `alert alert-${type} mt-3`;
        statusElement.style.display = 'block';
        
        // Registrar mensaje en la consola también
        console.log(`[Printer Status ${type}]:`, message);
    };
    
    // Generar comandos para la impresora Brother
    const generateBrotherCommand = (data) => {
        // Inicio de comandos ESC/POS
        const ESC = 0x1B;
        const GS = 0x1D;
        const INIT = [ESC, 0x40]; // Inicializar impresora
        const FONT_B = [ESC, 0x4D, 0x01]; // Fuente B (más pequeña)
        const FONT_A = [ESC, 0x4D, 0x00]; // Fuente A (normal)
        const ALIGN_CENTER = [ESC, 0x61, 0x01]; // Centrar texto
        const ALIGN_LEFT = [ESC, 0x61, 0x00]; // Alinear a la izquierda
        const BOLD_ON = [ESC, 0x45, 0x01]; // Activar negrita
        const BOLD_OFF = [ESC, 0x45, 0x00]; // Desactivar negrita
        const FONT_SIZE_NORMAL = [GS, 0x21, 0x00]; // Tamaño normal
        const FONT_SIZE_DOUBLE = [GS, 0x21, 0x11]; // Doble alto y ancho
        const LINE_FEED = [0x0A]; // Salto de línea
        const CUT_PAPER = [GS, 0x56, 0x41, 0x10]; // Cortar papel
        
        // Construir el comando completo
        let command = [];
        
        // Inicializar impresora
        command = command.concat(INIT);
        
        // Configurar título (Nombre del producto)
        command = command.concat(ALIGN_CENTER, FONT_SIZE_DOUBLE, BOLD_ON);
        command = command.concat(Array.from(new TextEncoder().encode(data.productName)));
        command = command.concat(LINE_FEED, LINE_FEED);
        
        // Configurar tipo de conservación
        command = command.concat(FONT_SIZE_NORMAL, BOLD_ON);
        command = command.concat(Array.from(new TextEncoder().encode(data.conservationType)));
        command = command.concat(LINE_FEED, LINE_FEED);
        
        // Configurar información de preparación
        command = command.concat(ALIGN_LEFT, BOLD_OFF);
        command = command.concat(Array.from(new TextEncoder().encode(data.preparedBy)));
        command = command.concat(LINE_FEED);
        
        // Configurar fecha de inicio
        command = command.concat(Array.from(new TextEncoder().encode(data.startDate)));
        command = command.concat(LINE_FEED);
        
        // Configurar fecha de caducidad (en negrita)
        command = command.concat(BOLD_ON);
        command = command.concat(Array.from(new TextEncoder().encode(data.expiryDate)));
        command = command.concat(LINE_FEED);
        
        // Agregar fecha de caducidad secundaria si existe
        if (data.secondaryExpiryDate) {
            command = command.concat(BOLD_OFF);
            command = command.concat(Array.from(new TextEncoder().encode(data.secondaryExpiryDate)));
            command = command.concat(LINE_FEED);
        }
        
        // Agregar espacio al final y cortar papel
        command = command.concat(LINE_FEED, LINE_FEED, LINE_FEED, CUT_PAPER);
        
        return new Uint8Array(command);
    };
    
    // Extraer los datos de la etiqueta del DOM
    const getEtiquetaData = () => {
        return {
            productName: document.getElementById("product-name").textContent || "",
            conservationType: document.getElementById("conservation-type").textContent || "",
            preparedBy: document.getElementById("prepared-by").textContent || "",
            startDate: document.getElementById("start-date").textContent || "",
            expiryDate: document.getElementById("expiry-date").textContent || "",
            secondaryExpiryDate: document.getElementById("secondary-expiry-date") ? 
                document.getElementById("secondary-expiry-date").textContent : "",
            quantity: parseInt(document.getElementById("quantity").value || "1")
        };
    };
    
    // Conectar a la impresora Brother vía Web Bluetooth
    const connectBrother = async () => {
        try {
            if (!navigator.bluetooth) {
                throw new Error("Tu navegador no soporta Bluetooth. Intenta con Chrome o Edge en Android");
            }
            
            showStatus("Buscando impresoras Brother cercanas...");
            
            // Servicios conocidos para impresoras Brother
            const brotherServices = [
                '000018f0-0000-1000-8000-00805f9b34fb',  // Brother/Generic printer service
                '1222e5db-36e1-4a53-bfef-bf7da833c5a5',  // Printer service variant
                '00001101-0000-1000-8000-00805f9b34fb',  // Serial Port Profile
                'e7810a71-73ae-499d-8c15-faa9aef0c3f2',  // Thermal printer service
                'af20fbac-2518-4998-9af7-af42540731b3',  // Printer data service
                'bef8d6c9-9c21-4c9e-b632-bd58c1009f9f',  // Data transfer service
            ];
            
            // Intentar con nombres conocidos de dispositivos Brother
            // Pero también aceptar cualquier dispositivo si el usuario así lo desea
            const device = await navigator.bluetooth.requestDevice({
                filters: [
                    { namePrefix: "Brother" },
                    { namePrefix: "PT-" },     // Brother P-touch
                    { namePrefix: "QL" },      // Brother QL series
                    { namePrefix: "TD" },      // Brother TD series
                    { namePrefix: "RJ" },      // Brother RJ series
                ],
                acceptAllDevices: true,
                optionalServices: brotherServices
            });
            
            showStatus(`Dispositivo Bluetooth seleccionado: ${device.name}. Conectando...`);
            
            // Guardar en localStorage para futuros usos
            localStorage.setItem('lastBrotherDevice', device.name);
            
            // Conectar al dispositivo GATT
            const server = await device.gatt.connect();
            
            // Buscar un servicio de impresión disponible
            let service = null;
            for (const serviceUuid of brotherServices) {
                try {
                    service = await server.getPrimaryService(serviceUuid);
                    console.log(`Servicio encontrado: ${serviceUuid}`);
                    break;
                } catch (e) {
                    console.log(`Servicio ${serviceUuid} no disponible`);
                }
            }
            
            if (!service) {
                throw new Error(`No se encontró un servicio de impresión compatible en ${device.name}`);
            }
            
            // Buscar una característica con capacidad de escritura
            const characteristics = await service.getCharacteristics();
            let writeCharacteristic = null;
            
            for (const characteristic of characteristics) {
                if (characteristic.properties.write || characteristic.properties.writeWithoutResponse) {
                    writeCharacteristic = characteristic;
                    break;
                }
            }
            
            if (!writeCharacteristic) {
                throw new Error("No se encontró una característica con permisos de escritura");
            }
            
            return {
                device,
                server,
                writeCharacteristic,
                // Determinar si requiere respuesta o no
                writeWithResponse: writeCharacteristic.properties.write,
                writeWithoutResponse: writeCharacteristic.properties.writeWithoutResponse
            };
            
        } catch (error) {
            showStatus(`Error al conectar: ${error.message}`, 'danger');
            throw error;
        }
    };
    
    // Imprimir etiqueta a través de conexión Bluetooth
    const printBrotherLabel = async () => {
        try {
            // Obtener datos de la etiqueta
            const data = getEtiquetaData();
            
            // Conectar a la impresora
            showStatus("Conectando a la impresora...");
            const connection = await connectBrother();
            
            // Generar comando de impresión
            const command = generateBrotherCommand(data);
            
            // Imprimir el número especificado de etiquetas
            for (let i = 0; i < data.quantity; i++) {
                showStatus(`Imprimiendo etiqueta ${i+1} de ${data.quantity}...`);
                
                // Enviar el comando en chunks para evitar desbordamientos de buffer
                const CHUNK_SIZE = 512;
                
                for (let j = 0; j < command.length; j += CHUNK_SIZE) {
                    const chunk = command.slice(j, j + CHUNK_SIZE);
                    
                    if (connection.writeWithoutResponse) {
                        await connection.writeCharacteristic.writeValueWithoutResponse(chunk);
                    } else {
                        await connection.writeCharacteristic.writeValue(chunk);
                    }
                    
                    // Pausa breve entre chunks
                    await new Promise(resolve => setTimeout(resolve, 50));
                }
                
                // Pausa entre etiquetas múltiples
                if (i < data.quantity - 1) {
                    await new Promise(resolve => setTimeout(resolve, 500));
                }
            }
            
            showStatus(`¡Impresión exitosa! Se imprimieron ${data.quantity} etiqueta(s)`, 'success');
            
            // Desconectar de la impresora
            if (connection.device.gatt.connected) {
                connection.device.gatt.disconnect();
            }
            
            return true;
            
        } catch (error) {
            console.error("Error de impresión:", error);
            
            // Determinar mensaje de error apropiado según el tipo de error
            let errorMessage = "Error desconocido al imprimir";
            
            if (error.name === 'NotFoundError') {
                errorMessage = "No se encontró ninguna impresora Brother compatible";
            } else if (error.name === 'SecurityError') {
                errorMessage = "No se tienen permisos suficientes para acceder a Bluetooth";
            } else if (error.name === 'NetworkError') {
                errorMessage = "La impresora se desconectó durante la operación";
            } else if (error.message) {
                errorMessage = error.message;
            }
            
            showStatus(errorMessage, 'danger');
            return false;
        }
    };
    
    // Alternativa: Generar PDF para descarga (para navegadores sin soporte Bluetooth)
    const generateLabelPDF = () => {
        showStatus("Generando PDF para imprimir...");
        
        // Crear elemento para descargar
        const downloadLink = document.createElement('a');
        downloadLink.setAttribute('href', `/tasks/download_label_pdf?product_id=${getProductId()}&quantity=${getQuantity()}`);
        downloadLink.setAttribute('download', 'etiqueta.pdf');
        downloadLink.style.display = 'none';
        document.body.appendChild(downloadLink);
        
        // Simular clic en el enlace
        downloadLink.click();
        
        // Limpiar
        document.body.removeChild(downloadLink);
        
        showStatus("PDF generado. Guárdalo e imprímelo en tu impresora de etiquetas.", 'success');
    };
    
    // Funciones auxiliares para obtener datos
    const getProductId = () => {
        // Intentar extraer del URL actual
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('product_id') || '';
    };
    
    const getQuantity = () => {
        return document.getElementById("quantity")?.value || 1;
    };
    
    // Configurar evento del botón de impresión
    const setupPrintButton = () => {
        const printButton = document.getElementById('bluetooth-print-btn');
        if (!printButton) return;
        
        // Añadir clase para PWA si estamos en modo PWA
        if (isPwa) {
            printButton.classList.add('pwa-mode');
            printButton.textContent = '🖨️ Imprimir Etiqueta (Modo App)';
        }
        
        // Añadir alternativa PDF si no hay soporte Bluetooth
        if (!navigator.bluetooth) {
            // Crear botón PDF como alternativa
            const pdfButton = document.createElement('button');
            pdfButton.id = 'pdf-download-btn';
            pdfButton.className = 'btn btn-lg btn-outline-secondary ms-2';
            pdfButton.textContent = '📄 Descargar PDF';
            pdfButton.addEventListener('click', generateLabelPDF);
            
            // Añadir junto al botón original
            printButton.parentNode.appendChild(pdfButton);
            
            // Actualizar etiqueta del botón principal
            printButton.textContent = '🖶 Imprimir (Web)';
        }
        
        // Configurar evento principal
        printButton.addEventListener('click', async (e) => {
            e.preventDefault();
            
            if (navigator.bluetooth) {
                // Intentar impresión Bluetooth directa
                await printBrotherLabel();
            } else {
                // Mostrar opciones alternativas
                showStatus("Tu navegador no soporta conexión Bluetooth directa a impresoras. " +
                           "Usa la opción de descarga PDF o instala esta web como aplicación " +
                           "para acceder a más funciones.", 'warning');
            }
        });
    };
    
    // Inicializar
    setupPrintButton();
    
    // Mostrar mensaje inicial según contexto
    if (isPwa && navigator.bluetooth) {
        showStatus("Modo aplicación activo. Podrás conectar directamente a tu impresora Brother.", 'info');
    } else if (navigator.bluetooth) {
        showStatus("Pulsa el botón para conectar con la impresora Brother Bluetooth", 'info');
    } else {
        showStatus("Tu navegador no soporta conexión directa a impresoras. Recomendamos instalar esta web como aplicación o usar Chrome en Android para todas las funciones.", 'warning');
    }
});
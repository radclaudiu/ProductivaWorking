// Script mejorado para impresión Bluetooth con impresoras Brother desde dispositivos móviles
document.addEventListener('DOMContentLoaded', function() {
    // Verificar si hay un botón de impresión Bluetooth en la página
    const bluetoothPrintBtn = document.getElementById("bluetooth-print-btn");
    if (!bluetoothPrintBtn) return;
    
    console.log("Inicializando sistema de impresión Bluetooth");
    
    // Función para detectar si estamos en un dispositivo móvil
    const isMobileDevice = () => {
        const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        console.log("¿Es dispositivo móvil?", isMobile, "User Agent:", navigator.userAgent);
        return isMobile;
    };
    
    // Llamar inmediatamente para verificar
    console.log("Estado de detección móvil:", isMobileDevice());
    
    // Función para mostrar mensajes al usuario
    const showBluetoothMessage = (message, isError = false) => {
        // Usar el área de mensajes existente si está en la página
        const messageArea = document.getElementById("print-message") || createBluetoothMessageArea();
        messageArea.textContent = message;
        messageArea.className = isError ? "alert alert-danger mt-3" : "alert alert-success mt-3";
        messageArea.style.display = "block";
        
        // Ocultar el mensaje después de 5 segundos
        setTimeout(() => {
            messageArea.style.display = "none";
        }, 5000);
    };
    
    // Crear área de mensajes si no existe
    const createBluetoothMessageArea = () => {
        const messageArea = document.createElement("div");
        messageArea.id = "bluetooth-message";
        messageArea.className = "alert alert-info mt-3";
        messageArea.style.display = "none";
        
        // Insertar después del botón de impresión
        const parentElement = bluetoothPrintBtn.parentNode.parentNode;
        parentElement.appendChild(messageArea);
        
        return messageArea;
    };
    
    // Generar comandos específicos para impresoras térmicas Brother
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
    
    // Iniciar la impresión Bluetooth solo en dispositivos móviles
    if (isMobileDevice()) {
        // Asegurarnos que no hay otros listeners
        bluetoothPrintBtn.replaceWith(bluetoothPrintBtn.cloneNode(true));
        
        // Obtener la referencia al botón nuevamente
        const newBluetoothBtn = document.getElementById("bluetooth-print-btn");
        
        // Aplicar la lógica de impresión Bluetooth
        newBluetoothBtn.addEventListener("click", async function(e) {
            e.preventDefault();
        
        try {
            // Verificar si el navegador soporta Web Bluetooth API
            if (!navigator.bluetooth) {
                showBluetoothMessage("Tu navegador no soporta Bluetooth. Intenta con Chrome en Android", true);
                return;
            }
            
            showBluetoothMessage("Buscando dispositivos Bluetooth... Selecciona tu impresora de la lista.");
            
            // Solicitar cualquier dispositivo Bluetooth (sin filtros para máxima compatibilidad)
            const device = await navigator.bluetooth.requestDevice({
                // Aceptar cualquier dispositivo Bluetooth
                acceptAllDevices: true,
                // Incluir servicios opcionales para ampliar compatibilidad
                optionalServices: [
                    'generic_access',
                    'battery_service', 
                    '000018f0-0000-1000-8000-00805f9b34fb',  // Servicio de impresión estándar
                    '1222e5db-36e1-4a53-bfef-bf7da833c5a5',  // Servicio de impresión alternativo
                    '00001101-0000-1000-8000-00805f9b34fb',   // Serial Port Profile
                    // Servicios genéricos de impresión que podrían usar otras impresoras
                    '00001800-0000-1000-8000-00805f9b34fb',  // Servicio genérico
                    '00001801-0000-1000-8000-00805f9b34fb',  // Servicio de atributos genéricos
                    '0000180a-0000-1000-8000-00805f9b34fb',  // Información del dispositivo
                    '0000180f-0000-1000-8000-00805f9b34fb'   // Servicio de batería
                ]
            });
            
            showBluetoothMessage(`Impresora encontrada: ${device.name}. Conectando...`);
            
            // Conectar al dispositivo
            const server = await device.gatt.connect();
            
            // Obtener los datos de la etiqueta
            const productName = document.getElementById("product-name").textContent || "";
            const conservationType = document.getElementById("conservation-type").textContent || "";
            const preparedBy = document.getElementById("prepared-by").textContent || "";
            const startDate = document.getElementById("start-date").textContent || "";
            const expiryDate = document.getElementById("expiry-date").textContent || "";
            const secondaryExpiryDate = document.getElementById("secondary-expiry-date") ? 
                document.getElementById("secondary-expiry-date").textContent : "";
            
            // Cantidad de etiquetas a imprimir
            const quantity = parseInt(document.getElementById("quantity").value || "1");
            
            // Intentar obtener servicio de impresión (diferentes UUIDs para diferentes modelos)
            let service;
            try {
                // Primero intentar con servicio estándar de impresión
                service = await server.getPrimaryService('000018f0-0000-1000-8000-00805f9b34fb');
            } catch (e) {
                try {
                    // Luego intentar con servicio de impresión alternativo
                    service = await server.getPrimaryService('1222e5db-36e1-4a53-bfef-bf7da833c5a5');
                } catch (e2) {
                    try {
                        // Finalmente intentar con Serial Port Profile
                        service = await server.getPrimaryService('00001101-0000-1000-8000-00805f9b34fb');
                    } catch (e3) {
                        throw new Error("No se pudo encontrar un servicio de impresión compatible. Asegúrate de que la impresora esté en modo de emparejamiento.");
                    }
                }
            }
            
            // Buscar características de escritura
            let characteristic;
            const characteristics = await service.getCharacteristics();
            
            // Buscar una característica con propiedades de escritura
            for (let char of characteristics) {
                if (char.properties.write || char.properties.writeWithoutResponse) {
                    characteristic = char;
                    break;
                }
            }
            
            if (!characteristic) {
                throw new Error("No se encontró una característica de escritura en la impresora");
            }
            
            // Preparar los datos para la impresión
            const printData = {
                productName,
                conservationType,
                preparedBy,
                startDate,
                expiryDate,
                secondaryExpiryDate
            };
            
            // Generar comando de impresión para Brother
            const command = generateBrotherCommand(printData);
            
            // Enviar datos a la impresora (repetir según cantidad)
            for (let i = 0; i < quantity; i++) {
                showBluetoothMessage(`Imprimiendo etiqueta ${i+1} de ${quantity}...`);
                
                // Enviar comando en chunks si es muy grande
                const CHUNK_SIZE = 512; // Tamaño máximo de chunk
                
                for (let j = 0; j < command.length; j += CHUNK_SIZE) {
                    const chunk = command.slice(j, j + CHUNK_SIZE);
                    
                    if (characteristic.properties.writeWithoutResponse) {
                        await characteristic.writeValueWithoutResponse(chunk);
                    } else {
                        await characteristic.writeValue(chunk);
                    }
                    
                    // Pequeña pausa entre chunks
                    await new Promise(resolve => setTimeout(resolve, 50));
                }
                
                // Pausa entre etiquetas
                if (i < quantity - 1) {
                    await new Promise(resolve => setTimeout(resolve, 500));
                }
            }
            
            showBluetoothMessage(`¡Impresión completada! ${quantity} etiqueta(s) enviada(s) a la impresora Bluetooth`);
            
            // Desconectar
            device.gatt.disconnect();
        
        } catch (error) {
            console.error("Error de impresión Bluetooth:", error);
            
            // Mensajes de error específicos para diferentes situaciones
            if (error.name === 'NotFoundError') {
                showBluetoothMessage("No se encontró ningún dispositivo compatible. Asegúrate de que el dispositivo esté encendido y en modo de emparejamiento.", true);
            } else if (error.name === 'SecurityError') {
                showBluetoothMessage("No se concedieron permisos para acceder al Bluetooth. Debes permitir el acceso a Bluetooth en tu navegador.", true);
            } else if (error.name === 'NetworkError') {
                showBluetoothMessage("Error de conexión con el dispositivo Bluetooth. Verifica que esté encendido y dentro del alcance.", true);
            } else if (error.message.includes("User cancelled")) {
                showBluetoothMessage("Selección de dispositivo cancelada por el usuario.", true);
            } else if (error.message.includes("No se pudo encontrar un servicio")) {
                showBluetoothMessage("El dispositivo seleccionado no parece ser una impresora compatible. Intenta con otro dispositivo o verifica que esté en modo de emparejamiento correcto.", true);
            } else {
                showBluetoothMessage("Error al conectar: " + (error.message || "Error desconocido") + ". Intenta reiniciar el dispositivo y la aplicación, luego vuelve a intentarlo.", true);
                console.error("Detalles completos del error:", error);
            }
        }
        });
    }
});
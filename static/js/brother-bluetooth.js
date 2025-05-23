// Script mejorado para impresión Bluetooth con impresoras Brother desde dispositivos móviles
// Versión optimizada para impresora TD-4550DNWB con retorno automático (2025-05-23)
document.addEventListener('DOMContentLoaded', function() {
    console.log("Inicializando sistema de impresión directa para Brother TD-4550DNWB");

    // Verificar si estamos en la página de impresión de etiquetas
    const isLabelPage = document.querySelector('.etiqueta') !== null;
    if (!isLabelPage) return;
    
    // Buscar botones de impresión (normal y directo)
    const bluetoothPrintBtn = document.getElementById("bluetooth-print-btn");
    const directPrintBtn = document.getElementById("direct-print-btn");
    
    // Si no hay ningún botón, salir
    if (!bluetoothPrintBtn && !directPrintBtn) return;
    
    // Mostrar el botón Bluetooth si está oculto
    if (bluetoothPrintBtn) {
        bluetoothPrintBtn.style.display = "block";
        bluetoothPrintBtn.classList.add("btn-lg");
        bluetoothPrintBtn.innerHTML = '<i class="bi bi-printer"></i> Imprimir en TD-4550DNWB';
    }
    
    console.log("Sistema de impresión directa con autoretorno inicializado");
    
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
                // Incluir servicios opcionales para ampliar compatibilidad - Lista ampliada para mayor compatibilidad
                optionalServices: [
                    // Servicios genéricos
                    'generic_access',
                    'battery_service',
                    
                    // Servicios de impresora Brother conocidos
                    '000018f0-0000-1000-8000-00805f9b34fb',  // Servicio de impresión estándar
                    '1222e5db-36e1-4a53-bfef-bf7da833c5a5',  // Servicio de impresión alternativo
                    '00001101-0000-1000-8000-00805f9b34fb',  // Serial Port Profile
                    
                    // Servicios genéricos Bluetooth
                    '00001800-0000-1000-8000-00805f9b34fb',  // Servicio genérico
                    '00001801-0000-1000-8000-00805f9b34fb',  // Servicio de atributos genéricos
                    '0000180a-0000-1000-8000-00805f9b34fb',  // Información del dispositivo
                    '0000180f-0000-1000-8000-00805f9b34fb',  // Servicio de batería
                    
                    // Servicios adicionales para impresoras térmicas
                    'e7810a71-73ae-499d-8c15-faa9aef0c3f2',  // Servicio de impresión alternativo
                    'af20fbac-2518-4998-9af7-af42540731b3',  // Servicio de impresión alternativo
                    'bef8d6c9-9c21-4c9e-b632-bd58c1009f9f',  // Servicio de transferencia de datos
                    
                    // Rangos comunes para servicios Serial (SPP) en diferentes modelos
                    '0000110a-0000-1000-8000-00805f9b34fb',
                    '0000110b-0000-1000-8000-00805f9b34fb',
                    '0000110c-0000-1000-8000-00805f9b34fb',
                    '0000110d-0000-1000-8000-00805f9b34fb',
                    '0000110e-0000-1000-8000-00805f9b34fb',
                    '0000110f-0000-1000-8000-00805f9b34fb'
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
            
            // Intentar obtener servicio de impresión probando con todos los UUIDs disponibles
            showBluetoothMessage(`Buscando servicios en la impresora ${device.name}...`);
            console.log("Obteniendo todos los servicios disponibles en el dispositivo...");
            
            // Lista de servicios comunes de impresoras Bluetooth
            const printerServiceIds = [
                '000018f0-0000-1000-8000-00805f9b34fb',  // Brother/Generic printer service
                '1222e5db-36e1-4a53-bfef-bf7da833c5a5',  // Printer service variant
                '00001101-0000-1000-8000-00805f9b34fb',  // Serial Port Profile
                'e7810a71-73ae-499d-8c15-faa9aef0c3f2',  // Thermal printer service
                'af20fbac-2518-4998-9af7-af42540731b3',  // Printer data service
                'bef8d6c9-9c21-4c9e-b632-bd58c1009f9f',  // Data transfer service
                '0000110a-0000-1000-8000-00805f9b34fb',  // SPP variant
                '0000110b-0000-1000-8000-00805f9b34fb',  // SPP variant
                '0000110c-0000-1000-8000-00805f9b34fb',  // SPP variant
                '0000110d-0000-1000-8000-00805f9b34fb',  // SPP variant
                '0000110e-0000-1000-8000-00805f9b34fb',  // SPP variant
                '0000110f-0000-1000-8000-00805f9b34fb'   // SPP variant
            ];
            
            // Obtener todos los servicios disponibles primero
            let allServices;
            try {
                // Intentar obtener la lista de todos los servicios
                allServices = await server.getPrimaryServices();
                console.log("Servicios disponibles en el dispositivo:", allServices.map(s => s.uuid));
            } catch (e) {
                console.log("No se pudo obtener lista de servicios:", e);
                // Continuar con el enfoque manual
            }
            
            // Si pudimos obtener todos los servicios, intentar encontrar uno con características de escritura
            let service = null;
            
            if (allServices && allServices.length > 0) {
                // Probar servicios disponibles primero
                for (const svc of allServices) {
                    try {
                        const chars = await svc.getCharacteristics();
                        // Buscar una característica con propiedades de escritura
                        for (const char of chars) {
                            if (char.properties.write || char.properties.writeWithoutResponse) {
                                service = svc;
                                console.log(`Servicio con características de escritura encontrado: ${svc.uuid}`);
                                break;
                            }
                        }
                        if (service) break;
                    } catch (e) {
                        console.log(`Error al inspeccionar servicio ${svc.uuid}:`, e);
                    }
                }
            }
            
            // Si no encontramos un servicio válido, intentar explícitamente con los IDs conocidos
            if (!service) {
                console.log("Intentando servicios conocidos de impresoras uno por uno");
                for (const serviceId of printerServiceIds) {
                    try {
                        console.log(`Intentando servicio: ${serviceId}`);
                        service = await server.getPrimaryService(serviceId);
                        console.log(`Servicio encontrado: ${serviceId}`);
                        break;
                    } catch (e) {
                        console.log(`Servicio ${serviceId} no encontrado`);
                    }
                }
            }
            
            // Si aún no encontramos un servicio válido
            if (!service) {
                throw new Error("No se pudo encontrar un servicio de impresión compatible. Intenta con otra impresora Bluetooth o verifica que esté en modo descubrible.");
            }
            
            showBluetoothMessage(`Servicio de impresión encontrado en ${device.name}. Preparando datos...`);
            
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
            
            showBluetoothMessage(`¡Impresión completada! Regresando a productos...`);
            
            // Desconectar
            device.gatt.disconnect();
            
            // Redirigir a la página de productos después de 1.5 segundos
            setTimeout(() => {
                window.location.href = '/tasks/local-user/labels';
                // Se agregará un parámetro para mostrar mensaje de éxito en la página de etiquetas
                localStorage.setItem('print_success', 'true');
            }, 1500);
        
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
                // Mostrar información de diagnóstico más detallada
                const errorMsg = error.message || "Error desconocido";
                showBluetoothMessage("Error al conectar: " + errorMsg + ". Puedes intentar de nuevo o volver a la lista de productos.", true);
                
                // Crear información de diagnóstico
                const diagnosticInfo = {
                    userAgent: navigator.userAgent,
                    platform: navigator.platform,
                    errorName: error.name,
                    errorMessage: error.message,
                    errorStack: error.stack,
                    date: new Date().toISOString()
                };
                
                // Añadir botones para intentar de nuevo o volver a productos
                const messageArea = document.getElementById("print-message");
                if (messageArea && messageArea.parentNode) {
                    // Eliminar botones anteriores si existen
                    const existingButtons = document.getElementById("error-action-buttons");
                    if (existingButtons) {
                        existingButtons.remove();
                    }
                    
                    // Crear contenedor de botones
                    const buttonsContainer = document.createElement('div');
                    buttonsContainer.id = "error-action-buttons";
                    buttonsContainer.className = 'mt-3 d-flex justify-content-center gap-2';
                    buttonsContainer.innerHTML = `
                        <button class="btn btn-primary" onclick="window.location.reload()">
                            <i class="bi bi-arrow-clockwise me-1"></i>Intentar de nuevo
                        </button>
                        <a href="/tasks/local-user/labels" class="btn btn-secondary">
                            <i class="bi bi-arrow-left me-1"></i>Volver a productos
                        </a>
                    `;
                    
                    // Insertar después del mensaje de error
                    messageArea.parentNode.insertBefore(buttonsContainer, messageArea.nextSibling);
                }
                
                console.error("Detalles completos del error:", error);
                console.error("Información de diagnóstico:", diagnosticInfo);
                
                // Mostrar diagnóstico más detallado en la página
                const diagArea = document.getElementById('bluetooth-diagnostics') || 
                    (() => {
                        const div = document.createElement('div');
                        div.id = 'bluetooth-diagnostics';
                        div.className = 'alert alert-info mt-3 small';
                        div.style.whiteSpace = 'pre-wrap';
                        div.style.display = 'none';
                        
                        // Agregar botón de mostrar/ocultar
                        const toggleBtn = document.createElement('button');
                        toggleBtn.innerText = 'Mostrar información de diagnóstico';
                        toggleBtn.className = 'btn btn-sm btn-secondary mt-2';
                        toggleBtn.onclick = () => {
                            if (div.style.display === 'none') {
                                div.style.display = 'block';
                                toggleBtn.innerText = 'Ocultar información de diagnóstico';
                            } else {
                                div.style.display = 'none';
                                toggleBtn.innerText = 'Mostrar información de diagnóstico';
                            }
                        };
                        
                        // Insertar elementos en la página
                        const parentElement = document.getElementById("bluetooth-print-btn").parentNode.parentNode;
                        parentElement.appendChild(toggleBtn);
                        parentElement.appendChild(div);
                        return div;
                    })();
                
                // Actualizar información de diagnóstico
                diagArea.innerHTML = `
                <h6>Información de diagnóstico Bluetooth:</h6>
                <p><strong>Error:</strong> ${errorMsg}</p>
                <p><strong>Tipo de error:</strong> ${error.name || 'No disponible'}</p>
                <p><strong>Navegador:</strong> ${navigator.userAgent}</p>
                <p><strong>Plataforma:</strong> ${navigator.platform}</p>
                <p><strong>Fecha/hora:</strong> ${new Date().toLocaleString()}</p>
                <p><strong>Bluetooth disponible:</strong> ${navigator.bluetooth ? 'Sí' : 'No'}</p>
                <p><strong>Stack de error:</strong></p>
                <pre>${error.stack || 'No disponible'}</pre>
                
                <p>Por favor, comparte esta información al reportar el problema.</p>`;
            }
        }
        });
    }
});
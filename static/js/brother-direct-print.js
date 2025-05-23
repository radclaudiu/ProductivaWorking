/**
 * Módulo para impresión directa en impresora Brother TD-4550DNWB
 * Versión 1.0 (23/05/2025)
 * 
 * Este script está diseñado para enviar directamente a la impresora Brother TD-4550DNWB
 * y regresar automáticamente a la página de productos una vez impreso.
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log("Inicializando sistema de impresión directa Brother TD-4550DNWB");
    
    // Verificar si estamos en la página de impresión de etiquetas
    const isLabelPage = document.querySelector('.etiqueta') !== null;
    if (!isLabelPage) return;
    
    // Redirigir automáticamente a la página de productos después de imprimir
    const redirectToProducts = () => {
        window.location.href = '/tasks/local-user/labels';
    };
    
    // Función para mostrar mensajes
    const showDirectMessage = (message, isError = false) => {
        const messageArea = document.getElementById('print-message');
        if (!messageArea) return;
        
        messageArea.style.display = "block";
        messageArea.textContent = message;
        messageArea.className = isError ? 
            "alert alert-danger mt-3" : 
            "alert alert-info mt-3";
    };
    
    // Encontrar el botón de impresión o crearlo si no existe
    const modifyPrintButton = () => {
        const printButton = document.getElementById('bluetooth-print-btn');
        if (!printButton) return false;
        
        // Modificar el botón existente
        printButton.innerHTML = '<i class="bi bi-printer me-2"></i>Imprimir en TD-4550DNWB';
        printButton.style.display = 'block';
        printButton.classList.add('btn-primary');
        
        // Eliminar cualquier evento anterior y añadir el nuevo
        const newPrintButton = printButton.cloneNode(true);
        printButton.parentNode.replaceChild(newPrintButton, printButton);
        
        // Asignar el nuevo evento para impresión directa
        newPrintButton.addEventListener('click', directBrotherPrint);
        
        return true;
    };
    
    // Función principal para impresión directa
    const directBrotherPrint = async (e) => {
        if (e) e.preventDefault();
        
        try {
            showDirectMessage("Conectando con impresora Brother TD-4550DNWB...");
            
            // Verificar si el navegador soporta Web Bluetooth API
            if (!navigator.bluetooth) {
                showDirectMessage("Tu navegador no soporta Bluetooth. Usa Chrome o Edge en Android", true);
                return;
            }
            
            // Especificar el nombre exacto de la impresora Brother TD-4550DNWB
            const BROTHER_PRINTER_NAME = "TD-4550DNWB";
            
            // Buscar el dispositivo específico por nombre
            const device = await navigator.bluetooth.requestDevice({
                filters: [
                    { name: BROTHER_PRINTER_NAME },
                    { namePrefix: "TD-4550" }  // Por si el nombre no es exacto
                ],
                // Incluir servicios opcionales específicos para Brother
                optionalServices: [
                    '00001101-0000-1000-8000-00805f9b34fb',  // Serial Port Profile
                    '000018f0-0000-1000-8000-00805f9b34fb',  // Brother service
                    '1222e5db-36e1-4a53-bfef-bf7da833c5a5',  // Printer service
                    // Servicios genéricos
                    '00001800-0000-1000-8000-00805f9b34fb',
                    '00001801-0000-1000-8000-00805f9b34fb'
                ]
            });
            
            showDirectMessage(`Impresora encontrada: ${device.name}. Conectando...`);
            
            // Conectar al dispositivo
            const server = await device.gatt.connect();
            
            // Buscar el servicio de impresión (Serial Port Profile para Brother)
            let service;
            try {
                service = await server.getPrimaryService('00001101-0000-1000-8000-00805f9b34fb');
            } catch (e) {
                console.log("Error al buscar servicio SPP, intentando servicio alternativo", e);
                service = await server.getPrimaryService('000018f0-0000-1000-8000-00805f9b34fb');
            }
            
            if (!service) {
                throw new Error("No se pudo encontrar el servicio de impresión en la TD-4550DNWB");
            }
            
            // Buscar características de escritura
            const characteristics = await service.getCharacteristics();
            let characteristic;
            
            for (let char of characteristics) {
                if (char.properties.write || char.properties.writeWithoutResponse) {
                    characteristic = char;
                    break;
                }
            }
            
            if (!characteristic) {
                throw new Error("No se encontró una característica de escritura en la TD-4550DNWB");
            }
            
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
            
            showDirectMessage(`Preparando ${quantity} etiqueta(s) para impresión...`);
            
            // Generar comando específico para la TD-4550DNWB
            const command = generateBrotherTD4550Command({
                productName,
                conservationType,
                preparedBy,
                startDate,
                expiryDate,
                secondaryExpiryDate
            });
            
            // Enviar datos a la impresora
            for (let i = 0; i < quantity; i++) {
                showDirectMessage(`Imprimiendo etiqueta ${i+1} de ${quantity}...`);
                
                // Enviar comando en chunks de 512 bytes (máximo recomendado)
                const CHUNK_SIZE = 512;
                for (let j = 0; j < command.length; j += CHUNK_SIZE) {
                    const chunk = command.slice(j, j + CHUNK_SIZE);
                    
                    // Usar el método apropiado según las propiedades disponibles
                    if (characteristic.properties.writeWithoutResponse) {
                        await characteristic.writeValueWithoutResponse(chunk);
                    } else {
                        await characteristic.writeValue(chunk);
                    }
                    
                    // Pequeña pausa entre chunks
                    await new Promise(resolve => setTimeout(resolve, 50));
                }
                
                // Pausa entre etiquetas múltiples
                if (i < quantity - 1) {
                    await new Promise(resolve => setTimeout(resolve, 500));
                }
            }
            
            showDirectMessage("¡Impresión exitosa! Regresando a productos...");
            
            // Desconectar dispositivo
            device.gatt.disconnect();
            
            // Esperar 1.5 segundos antes de redirigir para que el usuario vea el mensaje
            setTimeout(redirectToProducts, 1500);
            
        } catch (error) {
            console.error("Error de impresión:", error);
            showDirectMessage("Error: " + error.message, true);
            
            // Permitir que el usuario intente de nuevo o regrese manualmente
            const buttonsContainer = document.createElement('div');
            buttonsContainer.className = 'mt-3';
            buttonsContainer.innerHTML = `
                <button class="btn btn-primary me-2" onclick="window.location.reload()">
                    <i class="bi bi-arrow-clockwise me-1"></i>Intentar de nuevo
                </button>
                <button class="btn btn-secondary" onclick="window.location.href='/tasks/local-user/labels'">
                    <i class="bi bi-arrow-left me-1"></i>Volver a productos
                </button>
            `;
            
            // Añadir botones después del mensaje de error
            const messageArea = document.getElementById('print-message');
            if (messageArea && messageArea.parentNode) {
                messageArea.parentNode.appendChild(buttonsContainer);
            }
        }
    };
    
    // Generar comandos específicos para la impresora Brother TD-4550DNWB
    const generateBrotherTD4550Command = (data) => {
        // Comandos especiales para la TD-4550DNWB
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
    
    // Iniciar la modificación del botón de impresión
    if (!modifyPrintButton()) {
        console.log("No se pudo modificar el botón de impresión");
    }
    
    // Al cargar la página, modificar la interfaz para impresión directa
    // e iniciar la impresión automática si estamos en modo automático
    const autoprint = new URLSearchParams(window.location.search).get('autoprint');
    if (autoprint === 'true') {
        // Pequeño retraso para asegurar que la página esté completamente cargada
        setTimeout(function() {
            directBrotherPrint();
        }, 500);
    }
});
/**
 * Raspberry Pi Print Server API
 * 
 * Este módulo proporciona funciones para enviar comandos de impresión
 * a un servidor Raspberry Pi que actúa como intermediario entre
 * la aplicación web y la impresora de etiquetas conectada por USB.
 */
const RaspberryPrintServer = (function() {
    'use strict';
    
    // Estado
    let defaultPrinter = null;
    let printerList = [];
    
    /**
     * Configura la impresora predeterminada
     * @param {Object} printer - Objeto con la configuración de la impresora
     */
    function setDefaultPrinter(printer) {
        defaultPrinter = printer;
        console.log('Impresora Raspberry Pi predeterminada configurada:', printer.name);
    }
    
    /**
     * Carga la lista de impresoras Raspberry Pi desde la API
     * @param {string} locationId - ID de la ubicación
     * @returns {Promise} - Promesa con la lista de impresoras
     */
    function loadRaspberryPrinters(locationId) {
        return fetch(`/api/printers/${locationId}?type=raspberry_pi`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar las impresoras Raspberry Pi');
                }
                return response.json();
            })
            .then(data => {
                printerList = data.printers.filter(p => p.printer_type === 'raspberry_pi');
                
                // Configurar la impresora predeterminada
                const defaultFound = printerList.find(p => p.is_default);
                if (defaultFound) {
                    setDefaultPrinter(defaultFound);
                } else if (printerList.length > 0) {
                    setDefaultPrinter(printerList[0]);
                }
                
                return printerList;
            })
            .catch(error => {
                console.error('Error cargando impresoras Raspberry Pi:', error);
                throw error;
            });
    }
    
    /**
     * Verifica el estado de la impresora Raspberry Pi
     * @param {Object} printer - Objeto con la configuración de la impresora
     * @returns {Promise} - Promesa con el estado de la impresora
     */
    function checkPrinterStatus(printer) {
        const url = `http://${printer.ip_address}:${printer.port || 5000}/status`;
        
        return fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error de conexión: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            return {
                success: data.success,
                message: data.message || 'Conexión establecida',
                printerStatus: data.printer_status || 'ready'
            };
        })
        .catch(error => {
            console.error('Error verificando estado de impresora:', error);
            return {
                success: false,
                message: `Error: ${error.message || 'No se pudo conectar con el servidor'}`,
                printerStatus: 'offline'
            };
        });
    }
    
    /**
     * Envía un comando de impresión a la Raspberry Pi
     * @param {Object} printer - Objeto con la configuración de la impresora
     * @param {Object} data - Datos de la etiqueta
     * @returns {Promise} - Promesa con el resultado de la impresión
     */
    function printLabel(printer, data) {
        const url = `http://${printer.ip_address}:${printer.port || 5000}${printer.api_path || '/print'}`;
        
        // Preparar los datos de la etiqueta
        const printData = {
            label_data: data,
            printer_settings: {
                usb_port: printer.usb_port || '/dev/usb/lp0',
                model: printer.model || 'generic',
                width: 35,  // 35mm de ancho
                height: 40,  // 40mm de alto
                quantity: data.quantity || 1
            }
        };
        
        // Configurar las opciones de la solicitud
        const requestOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(printData)
        };
        
        // Si la impresora requiere autenticación, agregar credenciales
        if (printer.requires_auth && printer.username) {
            requestOptions.headers['Authorization'] = 'Basic ' + btoa(`${printer.username}:${printer.password || ''}`);
        }
        
        // Enviar la solicitud
        return fetch(url, requestOptions)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Error en la impresión: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    return {
                        success: true,
                        message: data.message || 'Etiqueta enviada correctamente'
                    };
                } else {
                    throw new Error(data.message || 'Error desconocido en el servidor');
                }
            })
            .catch(error => {
                console.error('Error enviando etiqueta:', error);
                return {
                    success: false,
                    message: `Error: ${error.message || 'No se pudo enviar la etiqueta'}`
                };
            });
    }
    
    // API pública
    return {
        setDefaultPrinter,
        loadRaspberryPrinters,
        checkPrinterStatus,
        printLabel
    };
})();

// Inicialización cuando se carga la página
document.addEventListener('DOMContentLoaded', function() {
    console.log('Raspberry Pi Print Server API inicializada');
});

/**
 * Raspberry Pi Print Server API
 * 
 * Este módulo proporciona funciones para enviar comandos de impresión
 * a un servidor Raspberry Pi que actúa como intermediario entre
 * la aplicación web y la impresora de etiquetas conectada por USB.
 */

const RaspberryPrintServer = (function() {
    // Variable para almacenar la impresora predeterminada
    let defaultPrinter = null;
    
    /**
     * Configura la impresora predeterminada
     * @param {Object} printer - Objeto con la configuración de la impresora
     */
    function setDefaultPrinter(printer) {
        defaultPrinter = printer;
        console.log(`Impresora Raspberry Pi predeterminada configurada: ${printer.name}`);
        return defaultPrinter;
    }
    
    /**
     * Carga la lista de impresoras Raspberry Pi desde la API
     * @param {string} locationId - ID de la ubicación
     * @returns {Promise} - Promesa con la lista de impresoras
     */
    function loadRaspberryPrinters(locationId) {
        return fetch(`/tasks/api/printers/${locationId}?type=raspberry_pi`)
            .then(response => response.json())
            .then(data => {
                if (data.success && data.printers.length > 0) {
                    // Establecer la primera impresora por defecto si existe
                    const defaultPrinters = data.printers.filter(p => p.is_default);
                    if (defaultPrinters.length > 0) {
                        setDefaultPrinter(defaultPrinters[0]);
                    } else {
                        setDefaultPrinter(data.printers[0]);
                    }
                    return data.printers;
                } else {
                    return [];
                }
            })
            .catch(error => {
                console.error('Error al cargar las impresoras Raspberry Pi:', error);
                return [];
            });
    }
    
    /**
     * Verifica el estado de la impresora Raspberry Pi
     * @param {Object} printer - Objeto con la configuración de la impresora
     * @returns {Promise} - Promesa con el estado de la impresora
     */
    function checkPrinterStatus(printer) {
        const printerToCheck = printer || defaultPrinter;
        
        if (!printerToCheck) {
            return Promise.reject('No hay impresora configurada');
        }
        
        const url = `http://${printerToCheck.ip_address}:${printerToCheck.port || 5000}/status`;
        
        return fetch(url)
            .then(response => response.json())
            .then(data => {
                console.log('Estado de la impresora:', data);
                return data;
            })
            .catch(error => {
                console.error('Error al verificar el estado de la impresora:', error);
                return { status: 'offline', error: error.message };
            });
    }
    
    /**
     * Envía un comando de impresión a la Raspberry Pi
     * @param {Object} printer - Objeto con la configuración de la impresora
     * @param {Object} data - Datos de la etiqueta
     * @returns {Promise} - Promesa con el resultado de la impresión
     */
    function printLabel(printer, data) {
        const printerToUse = printer || defaultPrinter;
        
        if (!printerToUse) {
            return Promise.reject('No hay impresora configurada');
        }
        
        const url = `http://${printerToUse.ip_address}:${printerToUse.port || 5000}${printerToUse.api_path || '/print'}`;
        
        // Agregar información del puerto USB a los datos
        const printData = {
            ...data,
            usb_port: printerToUse.usb_port || '/dev/usb/lp0',
            printer_model: printerToUse.model || 'QL-800'
        };
        
        return fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(printData)
        })
        .then(response => response.json())
        .then(result => {
            console.log('Resultado de la impresión:', result);
            return result;
        })
        .catch(error => {
            console.error('Error al imprimir la etiqueta:', error);
            return { success: false, error: error.message };
        });
    }
    
    // Método para obtener una impresora específica por ID
    function getPrinterById(printerId) {
        return fetch(`/tasks/api/printer/${printerId}`)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    return data.printer;
                } else {
                    throw new Error('No se pudo obtener la impresora');
                }
            });
    }
    
    return {
        setDefaultPrinter,
        loadRaspberryPrinters,
        checkPrinterStatus,
        printLabel,
        getPrinterById
    };
})();

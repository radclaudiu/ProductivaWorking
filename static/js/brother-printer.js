/**
 * Brother Network Printer API
 * Este módulo proporciona funciones para imprimir etiquetas en impresoras Brother
 * a través de su API de red utilizando fetch/XHR.
 */
const BrotherPrinter = (function() {
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
        console.log('Impresora predeterminada configurada:', printer.name);
    }
    
    /**
     * Carga la lista de impresoras desde la API
     * @param {string} locationId - ID de la ubicación
     * @returns {Promise} - Promesa con la lista de impresoras
     */
    function loadPrinters(locationId) {
        return fetch(`/api/printers/${locationId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar las impresoras');
                }
                return response.json();
            })
            .then(data => {
                printerList = data.printers;
                
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
                console.error('Error cargando impresoras:', error);
                throw error;
            });
    }
    
    /**
     * Genera los datos de la etiqueta para imprimir
     * @param {Object} labelData - Datos para la etiqueta
     * @returns {Object} - Datos formateados para la API de Brother
     */
    function generateLabelData(labelData) {
        return {
            labelFormat: {
                width: 62,  // Ancho en mm (típico para QL-820NWB)
                height: 100, // Alto en mm (ajustar según el tipo de etiqueta)
                margin: 3,   // Margen en mm
                rotate: false // No rotar
            },
            labelContent: {
                title: {
                    text: labelData.title || '',
                    font: 'Arial',
                    size: labelData.titleSize || 16,
                    bold: labelData.titleBold !== undefined ? labelData.titleBold : true,
                    x: labelData.titleX || 10,
                    y: labelData.titleY || 10
                },
                lines: [
                    {
                        text: `Tipo: ${labelData.conservationType || ''}`,
                        font: 'Arial',
                        size: labelData.conservationSize || 12,
                        bold: labelData.conservationBold !== undefined ? labelData.conservationBold : true,
                        x: labelData.conservationX || 10,
                        y: labelData.conservationY || 30
                    },
                    {
                        text: `Preparado por: ${labelData.preparedBy || ''}`,
                        font: 'Arial',
                        size: labelData.preparedBySize || 10,
                        bold: labelData.preparedByBold !== undefined ? labelData.preparedByBold : false,
                        x: labelData.preparedByX || 10,
                        y: labelData.preparedByY || 45
                    },
                    {
                        text: `Fecha: ${labelData.date || ''}`,
                        font: 'Arial',
                        size: labelData.dateSize || 10,
                        bold: labelData.dateBold !== undefined ? labelData.dateBold : false,
                        x: labelData.dateX || 10,
                        y: labelData.dateY || 60
                    },
                    {
                        text: `Cad: ${labelData.expiryDate || ''}`,
                        font: 'Arial',
                        size: labelData.expirySize || 14,
                        bold: labelData.expiryBold !== undefined ? labelData.expiryBold : true,
                        x: labelData.expiryX || 10,
                        y: labelData.expiryY || 75
                    }
                ]
            },
            printSettings: {
                copies: labelData.copies || 1,
                dpi: "300dpi",
                printQuality: "high",
                printType: "cut"
            }
        };
    }
    
    /**
     * Envía un trabajo de impresión a la impresora
     * @param {Object} printer - Objeto con la configuración de la impresora
     * @param {Object} labelData - Datos para la etiqueta
     * @returns {Promise} - Promesa con el resultado de la impresión
     */
    function printLabel(printer, labelData) {
        const data = generateLabelData(labelData);
        const printerEndpoint = printer.api_endpoint || `http://${printer.ip_address}:${printer.port}${printer.api_path}`;
        
        // Mostrar información del trabajo
        console.log(`Enviando trabajo a ${printer.name} (${printerEndpoint})`);
        
        // Configurar opciones de la petición
        const options = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        };
        
        // Añadir autenticación si es necesaria
        if (printer.requires_auth && printer.username) {
            const credentials = `${printer.username}:${printer.password}`;
            options.headers['Authorization'] = `Basic ${btoa(credentials)}`;
        }
        
        // Enviar la petición
        return fetch(printerEndpoint, options)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Error ${response.status}: ${response.statusText}`);
                }
                return response.json();
            })
            .then(result => {
                console.log('Impresión exitosa:', result);
                return { success: true, message: 'Etiqueta enviada a la impresora correctamente' };
            })
            .catch(error => {
                console.error('Error al imprimir:', error);
                return { 
                    success: false, 
                    message: `Error al imprimir: ${error.message}`,
                    error: error
                };
            });
    }
    
    /**
     * Imprime una etiqueta usando la impresora predeterminada
     * @param {Object} labelData - Datos para la etiqueta
     * @returns {Promise} - Promesa con el resultado de la impresión
     */
    function printWithDefaultPrinter(labelData) {
        if (!defaultPrinter) {
            return Promise.reject(new Error('No hay impresora predeterminada configurada'));
        }
        
        return printLabel(defaultPrinter, labelData);
    }
    
    /**
     * Verifica la conexión con una impresora
     * @param {Object} printer - Objeto con la configuración de la impresora
     * @returns {Promise} - Promesa con el resultado de la verificación
     */
    function checkPrinterConnection(printer) {
        const printerEndpoint = `http://${printer.ip_address}:${printer.port}/info`;
        
        return fetch(printerEndpoint, {
            method: 'GET',
            headers: printer.requires_auth ? {
                'Authorization': `Basic ${btoa(`${printer.username}:${printer.password}`)}`
            } : {}
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error ${response.status}: ${response.statusText}`);
            }
            return { success: true, message: 'Conexión exitosa con la impresora' };
        })
        .catch(error => {
            console.error('Error al verificar la conexión:', error);
            return { 
                success: false, 
                message: `Error al conectar con la impresora: ${error.message}`,
                error: error
            };
        });
    }
    
    // API pública
    return {
        setDefaultPrinter,
        loadPrinters,
        printLabel,
        printWithDefaultPrinter,
        checkPrinterConnection,
        
        // Getters
        getPrinterList: () => printerList,
        getDefaultPrinter: () => defaultPrinter
    };
})();

// Inicialización al cargar la página
document.addEventListener('DOMContentLoaded', function() {
    console.log('Brother Network Printer API inicializada');
    
    // Opciones de inicialización
    const labelContainer = document.getElementById('label-preview');
    if (labelContainer) {
        console.log('Contenedor de vista previa de etiqueta detectado');
        
        // Aquí podría implementarse código para mostrar una vista previa de la etiqueta
    }
});
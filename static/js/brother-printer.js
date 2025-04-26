// Script para impresión con impresora Brother TD-40 usando bpac-js
document.addEventListener('DOMContentLoaded', function() {
    // Verificar si hay un botón de impresión Brother en la página
    const brotherPrintBtn = document.getElementById("brother-print-btn");
    const configPrinterBtn = document.getElementById("config-printer-btn");
    if (!brotherPrintBtn && !configPrinterBtn) return;

    // Cargar la biblioteca bpac-js (versión UMD en lugar de ES modules)
    const scriptTag = document.createElement('script');
    scriptTag.src = "https://cdn.jsdelivr.net/npm/bpac-js@1.1.1/dist/bpac.umd.js"; // Usar versión UMD específica
    scriptTag.onload = initBrotherPrinting;
    console.log("Cargando biblioteca de Brother desde CDN (versión UMD)");
    document.head.appendChild(scriptTag);

    // Almacenar configuración de la impresora en localStorage
    const getPrinterConfig = () => {
        const defaultConfig = {
            templatePath: "C:/Templates/etiqueta-producto.lbx",
            exportPath: "C:/Temp/",
            lastUpdated: null
        };
        
        try {
            const savedConfig = localStorage.getItem('brotherPrinterConfig');
            return savedConfig ? JSON.parse(savedConfig) : defaultConfig;
        } catch (e) {
            console.error("Error al cargar configuración de impresora:", e);
            return defaultConfig;
        }
    };
    
    const savePrinterConfig = (config) => {
        config.lastUpdated = new Date().toISOString();
        localStorage.setItem('brotherPrinterConfig', JSON.stringify(config));
    };

    function initBrotherPrinting() {
        console.log("Biblioteca de Brother cargada correctamente");
        
        // Inicializar botón de configuración de impresora
        if (configPrinterBtn) {
            configPrinterBtn.addEventListener("click", function(e) {
                e.preventDefault();
                showPrinterConfigModal();
            });
        }
        
        // Inicializar botón de impresión
        if (brotherPrintBtn) {
            brotherPrintBtn.addEventListener("click", async function(e) {
                e.preventDefault();
                
                try {
                    // Obtener datos del formulario o elementos de la página
                    const productName = document.getElementById("product-name").textContent || "";
                    const conservationType = document.getElementById("conservation-type").textContent || "";
                    const preparedBy = document.getElementById("prepared-by").textContent || "";
                    const startDate = document.getElementById("start-date").textContent || "";
                    const expiryDate = document.getElementById("expiry-date").textContent || "";
                    const secondaryExpiryDate = document.getElementById("secondary-expiry-date") ? 
                        document.getElementById("secondary-expiry-date").textContent : "";

                    // Crear instancia de BrotherSdk (biblioteca UMD)
                    // La biblioteca UMD crea un objeto global bpac
                    if (!window.bpac) {
                        console.error("No se pudo cargar la biblioteca de Brother (bpac)");
                        showPrintMessage("Error: No se pudo cargar la biblioteca de impresión. Intenta recargar la página.", true);
                        return;
                    }
                    
                    console.log("Biblioteca Brother detectada correctamente:", window.bpac);

                    const config = getPrinterConfig();
                    // Usar la API de bpac en lugar de BrotherSdk
                    // Crear objeto de documento b-PAC
                    const bDoc = new window.bpac.Document();
                    
                    // Abrir plantilla
                    const openResult = bDoc.open(config.templatePath);
                    if (!openResult) {
                        throw new Error(`No se pudo abrir la plantilla: ${config.templatePath}`);
                    }

                    // Los datos deben coincidir con los objetos/variables en la plantilla
                    const data = {
                        producto: productName,
                        conservacion: conservationType,
                        preparado: preparedBy,
                        fecha: startDate,
                        caducidad: expiryDate,
                        caducidadPrimaria: secondaryExpiryDate
                    };

                    const options = {
                        copies: parseInt(document.getElementById("quantity").value || "1"), 
                        printName: "Etiqueta-Producto", 
                        highResolution: true
                    };

                    showPrintMessage("Enviando a la impresora Brother...");
                    
                    // Asignar los datos a los campos de la plantilla
                    Object.keys(data).forEach(key => {
                        if (bDoc.getObject(key)) {
                            bDoc.getObject(key).text = data[key] || "";
                        }
                    });
                    
                    // Configurar opciones de impresión
                    const copies = options.copies || 1;
                    
                    // Imprimir
                    const printResult = bDoc.print(copies);
                    
                    if (printResult) {
                        showPrintMessage(`Etiqueta enviada correctamente a la impresora Brother (${copies} copias)`);
                    } else {
                        showPrintMessage("No se pudo imprimir en la impresora Brother", true);
                    }
                    
                    // Cerrar el documento
                    bDoc.close();
                } catch (error) {
                    console.error("Error al imprimir con Brother:", error);
                    showPrintMessage("Error al imprimir: " + (error.message || "Error desconocido"), true);
                }
            });
        }
    }

    function showPrinterConfigModal() {
        // Crear modal si no existe
        let modal = document.getElementById('printerConfigModal');
        if (!modal) {
            const config = getPrinterConfig();
            
            // Crear el modal
            modal = document.createElement('div');
            modal.id = 'printerConfigModal';
            modal.className = 'modal fade';
            modal.tabIndex = -1;
            modal.setAttribute('aria-labelledby', 'printerConfigModalLabel');
            modal.setAttribute('aria-hidden', 'true');
            
            modal.innerHTML = `
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="printerConfigModalLabel">Configuración de Impresora Brother</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <form id="printerConfigForm">
                                <div class="mb-3">
                                    <label for="templatePath" class="form-label">Ruta de la Plantilla (.lbx)</label>
                                    <input type="text" class="form-control" id="templatePath" value="${config.templatePath}" 
                                        placeholder="C:/Templates/etiqueta-producto.lbx">
                                    <div class="form-text">Ruta completa al archivo .lbx creado con P-touch Editor</div>
                                </div>
                                <div class="mb-3">
                                    <label for="exportPath" class="form-label">Carpeta Temporal</label>
                                    <input type="text" class="form-control" id="exportPath" value="${config.exportPath}" 
                                        placeholder="C:/Temp/">
                                    <div class="form-text">Carpeta con permisos de escritura para archivos temporales</div>
                                </div>
                                <div class="alert alert-info">
                                    <h6>Información Importante:</h6>
                                    <p>La plantilla (.lbx) debe contener campos con estos nombres exactos:</p>
                                    <ul>
                                        <li><code>producto</code> - Nombre del producto</li>
                                        <li><code>conservacion</code> - Tipo de conservación</li>
                                        <li><code>preparado</code> - Empleado que preparó</li>
                                        <li><code>fecha</code> - Fecha de inicio</li>
                                        <li><code>caducidad</code> - Fecha de caducidad</li>
                                        <li><code>caducidadPrimaria</code> - Caducidad primaria (opcional)</li>
                                    </ul>
                                </div>
                            </form>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-primary" id="savePrinterConfig">Guardar Configuración</button>
                        </div>
                    </div>
                </div>
            `;
            
            document.body.appendChild(modal);
            
            // Inicializar el modal usando Bootstrap
            const bootstrapModal = new bootstrap.Modal(modal);
            
            // Manejar guardado de configuración
            document.getElementById('savePrinterConfig').addEventListener('click', function() {
                const newConfig = {
                    templatePath: document.getElementById('templatePath').value.trim(),
                    exportPath: document.getElementById('exportPath').value.trim()
                };
                
                if (!newConfig.templatePath) {
                    showPrintMessage("La ruta de la plantilla no puede estar vacía", true);
                    return;
                }
                
                if (!newConfig.exportPath) {
                    showPrintMessage("La carpeta temporal no puede estar vacía", true);
                    return;
                }
                
                savePrinterConfig(newConfig);
                bootstrapModal.hide();
                showPrintMessage("Configuración de impresora guardada correctamente");
            });
            
            // Mostrar el modal
            bootstrapModal.show();
        } else {
            // Si el modal ya existe, solo mostrarlo
            const bootstrapModal = new bootstrap.Modal(modal);
            bootstrapModal.show();
        }
    }

    function showPrintMessage(message, isError = false) {
        const messageArea = document.getElementById("print-message") || createMessageArea();
        messageArea.textContent = message;
        messageArea.className = isError ? "alert alert-danger mt-3" : "alert alert-success mt-3";
        messageArea.style.display = "block";
        
        // Ocultar el mensaje después de 5 segundos
        setTimeout(() => {
            messageArea.style.display = "none";
        }, 5000);
    }

    function createMessageArea() {
        const messageArea = document.createElement("div");
        messageArea.id = "print-message";
        messageArea.className = "alert alert-info mt-3";
        messageArea.style.display = "none";
        
        // Insertar después del botón de impresión o configuración
        const parentElement = (brotherPrintBtn || configPrinterBtn).parentNode;
        parentElement.appendChild(messageArea);
        
        return messageArea;
    }
});
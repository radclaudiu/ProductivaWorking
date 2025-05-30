/**
 * Implementación simplificada para impresión Brother TD-4550DNWB
 * Compatible con AndroidBridge básico
 */

// Función principal de impresión
function printBrotherLabel(labelData, quantity = 1) {
    try {
        // Verificar disponibilidad de AndroidBridge
        if (typeof AndroidBridge === 'undefined') {
            console.error('AndroidBridge no está disponible');
            return false;
        }

        // Verificar método printImage
        if (typeof AndroidBridge.printImage !== 'function') {
            console.error('AndroidBridge.printImage no está disponible');
            return false;
        }

        // Generar canvas con dimensiones Brother TD-4550DNWB
        const canvas = createLabelCanvas(labelData);
        
        // Convertir a base64
        const base64Image = canvas.toDataURL('image/png').replace('data:image/png;base64,', '');
        
        console.log('Generando etiqueta:', labelData.productName);
        console.log('Cantidad:', quantity);
        console.log('Tamaño canvas:', canvas.width + 'x' + canvas.height);
        
        // Enviar a impresora (una por una)
        for (let i = 0; i < quantity; i++) {
            AndroidBridge.printImage(base64Image);
        }
        
        return true;
        
    } catch (error) {
        console.error('Error en printBrotherLabel:', error);
        return false;
    }
}

// Crear canvas de etiqueta con dimensiones específicas Brother TD-4550DNWB
function createLabelCanvas(data) {
    const canvas = document.createElement('canvas');
    
    // Dimensiones para 44mm x 38mm a 300 DPI
    canvas.width = 520;   // 44mm
    canvas.height = 449;  // 38mm
    
    const ctx = canvas.getContext('2d');
    
    // Fondo blanco
    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // Configuración de texto
    ctx.fillStyle = '#000000';
    ctx.textAlign = 'center';
    
    // Título del producto (centrado, grande)
    ctx.font = 'bold 32px Arial';
    ctx.fillText(data.productName || 'PRODUCTO', canvas.width / 2, 80);
    
    // Tipo de conservación (centrado, mediano)
    ctx.font = 'bold 24px Arial';
    ctx.fillText(data.conservationType || 'CONSERVACIÓN', canvas.width / 2, 130);
    
    // Información adicional (izquierda)
    ctx.textAlign = 'left';
    const margin = 35;
    
    ctx.font = '18px Arial';
    ctx.fillText(data.preparedBy || 'Preparado por: Usuario', margin, 200);
    ctx.fillText(data.startDate || 'Fecha inicio', margin, 240);
    
    // Fecha de caducidad (destacada)
    ctx.font = 'bold 20px Arial';
    ctx.fillText(data.expiryDate || 'Fecha caducidad', margin, 290);
    
    // Borde
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 3;
    ctx.strokeRect(10, 10, 500, 429);
    
    return canvas;
}

// Exportar funciones globalmente
window.printBrotherLabel = printBrotherLabel;
window.createLabelCanvas = createLabelCanvas;
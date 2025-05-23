/**
 * Script para mostrar mensaje de confirmación después de imprimir una etiqueta
 * Se activa cuando se regresa a la página de etiquetas desde la pantalla de impresión
 */
document.addEventListener('DOMContentLoaded', function() {
    // Verificar si hay un mensaje de impresión exitosa en localStorage
    if (localStorage.getItem('print_success') === 'true') {
        // Mostrar mensaje de éxito
        const successMessage = document.getElementById('print-success-message');
        if (successMessage) {
            successMessage.style.display = 'block';
            
            // Limpiar después de 5 segundos
            setTimeout(function() {
                successMessage.style.display = 'none';
            }, 5000);
            
            // Limpiar la bandera del localStorage
            localStorage.removeItem('print_success');
        }
    }
});
/**
 * Script para mejorar la interacción con el formulario de horarios semanales
 */
document.addEventListener('DOMContentLoaded', function() {
    // Lista de todos los días de la semana
    const days = ["lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"];
    
    // Función para actualizar los campos de hora según si el día es laborable o no
    function updateTimeFields(dayName) {
        const checkbox = document.getElementById(`${dayName}_is_working_day`);
        const startTimeField = document.getElementById(`${dayName}_start_time`);
        const endTimeField = document.getElementById(`${dayName}_end_time`);
        
        if (checkbox && startTimeField && endTimeField) {
            // Si el día no es laborable, los campos aparecen deshabilitados visualmente
            // pero NO usamos .disabled para que los valores se sigan enviando
            // startTimeField.disabled = !checkbox.checked;
            // endTimeField.disabled = !checkbox.checked;
            
            // Aplicar estilos visuales para mejorar la experiencia de usuario
            const timeFields = document.querySelectorAll(`.${dayName}-time-field`);
            timeFields.forEach(field => {
                if (checkbox.checked) {
                    // Día laborable: normal
                    field.classList.remove('opacity-50');
                    field.classList.remove('bg-light');
                } else {
                    // Día no laborable: deshabilitado visualmente
                    field.classList.add('opacity-50');
                    field.classList.add('bg-light');
                }
            });
            
            // Resaltar visualmente la fila del día
            const dayRow = checkbox.closest('.day-row');
            if (dayRow) {
                if (checkbox.checked) {
                    dayRow.classList.add('border-start', 'border-3', 'border-primary');
                    dayRow.classList.remove('bg-light');
                } else {
                    dayRow.classList.remove('border-start', 'border-3', 'border-primary');
                    dayRow.classList.add('bg-light');
                }
            }
        }
    }
    
    // Inicializar y configurar eventos para todos los días
    days.forEach(day => {
        const checkbox = document.getElementById(`${day}_is_working_day`);
        if (checkbox) {
            // Aplicar estado inicial
            updateTimeFields(day);
            
            // Añadir listener para cambios en tiempo real
            checkbox.addEventListener('change', function() {
                updateTimeFields(day);
            });
        }
    });
});
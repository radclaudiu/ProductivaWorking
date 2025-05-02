// Sistema de ayuda interactivo para Productiva

document.addEventListener('DOMContentLoaded', function() {
    // Instalar el botón flotante si no estamos en la página de ayuda
    if (!window.location.pathname.includes('/help')) {
        installHelpButton();
    } else {
        initializeHelpPage();
    }
});

// Función para instalar el botón flotante de ayuda
function installHelpButton() {
    const button = document.createElement('div');
    button.className = 'help-float-button';
    button.innerHTML = '?';
    button.title = 'Ayuda';
    button.setAttribute('aria-label', 'Abrir ayuda');
    
    button.addEventListener('click', function() {
        // Guardar la página actual para poder volver
        localStorage.setItem('helpReturnUrl', window.location.href);
        
        // Obtener el contexto de la página actual para mostrar ayuda relevante
        const currentPath = window.location.pathname;
        let contextParam = '';
        
        if (currentPath.includes('/companies')) {
            contextParam = '?section=companies';
        } else if (currentPath.includes('/employees') || currentPath.includes('/checkpoints')) {
            contextParam = '?section=checkpoints';
        } else if (currentPath.includes('/tasks')) {
            contextParam = '?section=tasks';
        } else if (currentPath.includes('/cash-register')) {
            contextParam = '?section=cash-register';
        } else if (currentPath.includes('/monthly-expenses')) {
            contextParam = '?section=expenses';
        }
        
        // Redirigir a la página de ayuda con el contexto
        window.location.href = '/help' + contextParam;
    });
    
    document.body.appendChild(button);
}

// Función para inicializar la página de ayuda
function initializeHelpPage() {
    // Inicializar buscador
    const searchInput = document.getElementById('helpSearch');
    if (searchInput) {
        searchInput.addEventListener('input', filterHelpContent);
    }
    
    // Inicializar pestañas
    const tabs = document.querySelectorAll('.help-tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');
            activateTab(tabId);
        });
    });
    
    // Abrir la sección relevante según el parámetro de URL
    const urlParams = new URLSearchParams(window.location.search);
    const section = urlParams.get('section');
    if (section) {
        activateTab(section);
    } else {
        // Por defecto, abrir la primera pestaña
        const firstTab = document.querySelector('.help-tab');
        if (firstTab) {
            const firstTabId = firstTab.getAttribute('data-tab');
            activateTab(firstTabId);
        }
    }
    
    // Inicializar expandibles de preguntas
    const questions = document.querySelectorAll('.help-question');
    questions.forEach(question => {
        question.addEventListener('click', function() {
            const answer = this.nextElementSibling;
            if (answer.classList.contains('help-hidden')) {
                answer.classList.remove('help-hidden');
            } else {
                answer.classList.add('help-hidden');
            }
        });
    });
    
    // Inicializar botón de volver
    const backButton = document.getElementById('helpBackButton');
    if (backButton) {
        backButton.addEventListener('click', function() {
            const returnUrl = localStorage.getItem('helpReturnUrl') || '/';
            window.location.href = returnUrl;
        });
    }
    
    // Inicializar edición (solo para administradores)
    initializeEditButtons();
}

// Función para filtrar el contenido de ayuda
function filterHelpContent() {
    const searchText = document.getElementById('helpSearch').value.toLowerCase();
    const helpItems = document.querySelectorAll('.help-item');
    
    helpItems.forEach(item => {
        const question = item.querySelector('.help-question').textContent.toLowerCase();
        const answer = item.querySelector('.help-answer').textContent.toLowerCase();
        
        if (question.includes(searchText) || answer.includes(searchText)) {
            item.style.display = 'block';
        } else {
            item.style.display = 'none';
        }
    });
    
    // Mostrar mensajes si no hay resultados en las secciones visibles
    const activeSections = document.querySelectorAll('.help-tab-content.active');
    activeSections.forEach(section => {
        const visibleItems = Array.from(section.querySelectorAll('.help-item')).filter(item => {
            return item.style.display !== 'none';
        });
        
        const noResultsMsg = section.querySelector('.help-no-results');
        if (visibleItems.length === 0 && searchText !== '') {
            if (!noResultsMsg) {
                const msg = document.createElement('p');
                msg.className = 'help-no-results';
                msg.textContent = 'No se encontraron resultados para "' + searchText + '"';
                section.appendChild(msg);
            }
        } else if (noResultsMsg) {
            noResultsMsg.remove();
        }
    });
}

// Función para activar una pestaña
function activateTab(tabId) {
    // Desactivar todas las pestañas
    const allTabs = document.querySelectorAll('.help-tab');
    allTabs.forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Desactivar todos los contenidos
    const allContents = document.querySelectorAll('.help-tab-content');
    allContents.forEach(content => {
        content.classList.remove('active');
    });
    
    // Activar la pestaña seleccionada
    const selectedTab = document.querySelector(`.help-tab[data-tab="${tabId}"]`);
    if (selectedTab) {
        selectedTab.classList.add('active');
    }
    
    // Activar el contenido seleccionado
    const selectedContent = document.getElementById(`helpContent-${tabId}`);
    if (selectedContent) {
        selectedContent.classList.add('active');
    }
    
    // Actualizar la URL para reflejar la pestaña activa sin recargar la página
    history.replaceState(null, null, `?section=${tabId}`);
}

// Función para inicializar los botones de edición
function initializeEditButtons() {
    const editButtons = document.querySelectorAll('.help-edit-button');
    
    editButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            
            const itemId = this.getAttribute('data-item');
            const answerElement = document.getElementById(`answer-${itemId}`);
            const answerText = answerElement.innerHTML;
            
            // Crear y mostrar el formulario de edición
            const editForm = document.createElement('div');
            editForm.className = 'help-edit-form';
            editForm.innerHTML = `
                <textarea id="edit-${itemId}">${answerText}</textarea>
                <button class="help-save-button" data-item="${itemId}">Guardar</button>
                <button class="help-cancel-button" data-item="${itemId}">Cancelar</button>
            `;
            
            answerElement.style.display = 'none';
            answerElement.parentNode.insertBefore(editForm, answerElement.nextSibling);
            
            // Manejar el guardado
            const saveButton = editForm.querySelector('.help-save-button');
            saveButton.addEventListener('click', function() {
                const newText = document.getElementById(`edit-${itemId}`).value;
                answerElement.innerHTML = newText;
                
                // Aquí enviaríamos el texto al servidor con AJAX
                saveHelpContent(itemId, newText);
                
                // Limpiar el formulario de edición
                editForm.remove();
                answerElement.style.display = 'block';
            });
            
            // Manejar la cancelación
            const cancelButton = editForm.querySelector('.help-cancel-button');
            cancelButton.addEventListener('click', function() {
                editForm.remove();
                answerElement.style.display = 'block';
            });
        });
    });
}

// Función para guardar el contenido editado (req. implementación del servidor)
function saveHelpContent(itemId, content) {
    // Esta función enviaría los datos al servidor mediante una petición AJAX
    console.log(`Guardando contenido para el ítem ${itemId}...`);
    
    fetch('/api/help/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            itemId: itemId,
            content: content
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            console.log('Contenido guardado correctamente');
            // Agregar alguna notificación visual de éxito
            const notification = document.createElement('div');
            notification.className = 'help-notification';
            notification.textContent = 'Contenido actualizado correctamente';
            document.body.appendChild(notification);
            
            setTimeout(() => {
                notification.remove();
            }, 3000);
        } else {
            console.error('Error al guardar contenido:', data.error);
        }
    })
    .catch(error => {
        console.error('Error al comunicarse con el servidor:', error);
    });
}

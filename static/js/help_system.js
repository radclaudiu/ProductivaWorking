// Sistema de ayuda interactivo para Productiva

document.addEventListener('DOMContentLoaded', function() {
    // Ya no instalamos el botón flotante, está en el footer
    if (window.location.pathname.includes('/help')) {
        initializeHelpPage();
    } else {
        // Configurar el botón de ayuda en el footer
        setupHelpFooterButton();
    }
});

// Agregar una alternativa en caso de que DOMContentLoaded ya haya ocurrido
if (document.readyState === 'complete' || document.readyState === 'interactive') {
    if (window.location.pathname.includes('/help')) {
        setTimeout(initializeHelpPage, 100);
    } else {
        setTimeout(setupHelpFooterButton, 100);
    }
}

// Función para configurar el botón de ayuda en el footer
function setupHelpFooterButton() {
    const helpButton = document.querySelector('.help-footer-button');
    
    if (helpButton) {
        helpButton.addEventListener('click', function(e) {
            e.preventDefault(); // Prevenir el comportamiento de enlace por defecto
            
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
    }
}

// Función para inicializar la página de ayuda
function initializeHelpPage() {
    // Inicializar buscador
    const searchInput = document.getElementById('helpSearch');
    if (searchInput) {
        // Limpiar el campo de búsqueda al cargar la página
        searchInput.value = '';
        
        // Enfocar el campo de búsqueda automáticamente para mejor experiencia de usuario
        setTimeout(() => {
            searchInput.focus();
        }, 500);
        
        // Activar la búsqueda en tiempo real mientras se escribe
        searchInput.addEventListener('input', filterHelpContent);
        
        // También manejar el envío del formulario para evitar recargas
        searchInput.form?.addEventListener('submit', function(e) {
            e.preventDefault();
            filterHelpContent();
        });
    }
    
    // Inicializar pestañas
    const tabs = document.querySelectorAll('.help-tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');
            activateTab(tabId);
            
            // Al cambiar de pestaña, reiniciar la búsqueda
            if (searchInput) {
                searchInput.value = '';
                filterHelpContent();
            }
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
        // Asegurar que todas las respuestas estén inicialmente ocultas
        const answer = question.nextElementSibling;
        if (answer && answer.classList.contains('help-answer')) {
            answer.classList.add('help-hidden');
        }
        
        // Definir un manejador global para los clics de preguntas si no existe
        if (!window.questionClickHandler) {
            window.questionClickHandler = function(e) {
                console.log('Clic en pregunta (manejador global)');
                const question = this;
                const answer = question.nextElementSibling;
                if (answer && answer.classList.contains('help-answer')) {
                    if (answer.classList.contains('help-hidden')) {
                        // Expandir respuesta
                        answer.classList.remove('help-hidden');
                        question.classList.add('active');
                    } else {
                        // Contraer respuesta
                        answer.classList.add('help-hidden');
                        question.classList.remove('active');
                    }
                }
            };
        }
        
        // Eliminar eventos de clic existentes para evitar duplicados
        question.removeEventListener('click', window.questionClickHandler);
        
        // Función para manejar el clic en preguntas
        function handleQuestionClick() {
            console.log('Clic en pregunta');
            const answer = this.nextElementSibling;
            if (answer && answer.classList.contains('help-answer')) {
                if (answer.classList.contains('help-hidden')) {
                    // Expandir respuesta
                    answer.classList.remove('help-hidden');
                    this.classList.add('active');
                } else {
                    // Contraer respuesta
                    answer.classList.add('help-hidden');
                    this.classList.remove('active');
                }
            }
        }
        
        // Quitar el manejador local y usar el manejador global
        question.removeEventListener('click', handleQuestionClick);
        
        // Añadir el evento de clic para expandir/colapsar usando el manejador global
        question.addEventListener('click', window.questionClickHandler);
    });
    
    // Hacer clic una vez en cada pregunta para asegurar que el evento esté registrado
    setTimeout(() => {
        console.log('Inicializando eventos de clic para preguntas');
        questions.forEach(question => {
            question.click();
            question.click(); // Hacemos doble clic para volver al estado inicial
        });
    }, 500);
    
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
    
    // Forzar una primera ejecución del filtro para asegurar que todo es visible al inicio
    filterHelpContent();
}

// Función para filtrar el contenido de ayuda
function filterHelpContent() {
    const searchInput = document.getElementById('helpSearch');
    if (!searchInput) return;
    
    const searchText = searchInput.value.toLowerCase().trim();
    const helpItems = document.querySelectorAll('.help-item');
    let totalVisibleItems = 0;
    let totalMatchedItems = 0;
    
    // Expandir/colapsar respuestas según la búsqueda
    helpItems.forEach(item => {
        const question = item.querySelector('.help-question').textContent.toLowerCase();
        const answerElement = item.querySelector('.help-answer');
        const answer = answerElement.textContent.toLowerCase();
        
        // Determina si este ítem coincide con la búsqueda
        const questionMatches = question.includes(searchText);
        const answerMatches = answer.includes(searchText);
        const matches = questionMatches || answerMatches;
        
        // Determina cuál mostrar
        if (searchText === '') {
            // Sin búsqueda - mostrar todo
            item.style.display = 'block';
            totalVisibleItems++;
            // Mantener respuestas colapsadas por defecto
            answerElement.classList.add('help-hidden');
            item.querySelector('.help-question').classList.remove('active');
        } else if (matches) {
            // Coincidencia encontrada - mostrar y expandir
            item.style.display = 'block';
            // Expandir respuestas automáticamente cuando hay coincidencia
            answerElement.classList.remove('help-hidden');
            // Actualizar estado visual de la pregunta para mostrar que está expandida
            item.querySelector('.help-question').classList.add('active');
            totalVisibleItems++;
            totalMatchedItems++;
            
            // Resaltar el texto coincidente si está implementada la función (implementación futura)
            // highlightMatches(item, searchText);
        } else {
            // Sin coincidencia - ocultar
            item.style.display = 'none';
        }
    });
    
    // Actualizar contador de resultados si existe
    const resultsCounter = document.getElementById('helpResultsCounter');
    if (resultsCounter && searchText !== '') {
        resultsCounter.textContent = `${totalMatchedItems} resultados encontrados`;
        resultsCounter.style.display = 'block';
    } else if (resultsCounter) {
        resultsCounter.style.display = 'none';
    }
    
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
            } else {
                // Actualizar mensaje existente
                noResultsMsg.textContent = 'No se encontraron resultados para "' + searchText + '"';
                noResultsMsg.style.display = 'block';
            }
        } else if (noResultsMsg) {
            // Ocultar mensaje si hay resultados o no hay búsqueda
            if (searchText === '') {
                noResultsMsg.style.display = 'none';
            } else {
                noResultsMsg.remove();
            }
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

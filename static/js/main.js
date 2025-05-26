// Registro del Service Worker para PWA
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/static/js/service-worker.js')
      .then((registration) => {
        console.log('Service Worker registrado correctamente:', registration.scope);
      })
      .catch((error) => {
        console.log('Error al registrar el Service Worker:', error);
      });
  });
}

// Función para determinar la sección activa basada en la URL actual
function setActiveNavItem() {
  const path = window.location.pathname;
  
  // Limpiar cualquier elemento activo
  document.querySelectorAll('.navbar-nav .btn').forEach(btn => {
    btn.classList.remove('active');
  });
  
  // Establecer el elemento activo según la URL
  if (path.includes('/checkpoints')) {
    document.querySelector('a[href*="/checkpoints"]')?.classList.add('active');
  } else if (path.includes('/companies') || path.includes('/company')) {
    document.querySelector('a[href*="/companies"]')?.classList.add('active');
  } else if (path.includes('/tasks')) {
    document.querySelector('a[href*="/tasks"]')?.classList.add('active');
  } else if (path.includes('/cash_register')) {
    document.querySelector('a[href*="/cash_register"]')?.classList.add('active');
  } else if (path.includes('/monthly_expenses')) {
    document.querySelector('a[href*="/monthly_expenses"]')?.classList.add('active');
  }
}

// Ejecutar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
  // Establecer el elemento de navegación activo
  setActiveNavItem();
  
  // Manejar confirmaciones de eliminación
  setupConfirmationHandlers();
  
  // Limpiar cualquier modal-backdrop que pueda estar activo
  cleanupModalBackdrops();
});

// Función para limpiar overlays problemáticos
function cleanupModalBackdrops() {
  // Eliminar todos los modal-backdrop con cualquier combinación de clases
  const backdrops = document.querySelectorAll('.modal-backdrop, .modal-backdrop.fade, .modal-backdrop.show, .modal-backdrop.fade.show');
  backdrops.forEach(backdrop => {
    backdrop.remove();
  });
  
  // Asegurar que el body no tenga clases de modal
  document.body.classList.remove('modal-open');
  document.body.style.overflow = '';
  document.body.style.paddingRight = '';
  
  // Ejecutar también después de un breve delay para capturar overlays tardíos
  setTimeout(() => {
    const lateBackdrops = document.querySelectorAll('.modal-backdrop');
    lateBackdrops.forEach(backdrop => {
      backdrop.remove();
    });
  }, 100);
}

// Ejecutar limpieza cada vez que se hace clic en cualquier parte
document.addEventListener('click', function() {
  setTimeout(cleanupModalBackdrops, 50);
});

// Función para manejar las confirmaciones de eliminación
function setupConfirmationHandlers() {
  const confirmButtons = document.querySelectorAll('.confirm-action');
  
  confirmButtons.forEach(button => {
    button.addEventListener('click', function(e) {
      e.preventDefault();
      
      const confirmMessage = this.getAttribute('data-confirm-message') || '¿Estás seguro de que deseas eliminar este elemento?';
      const form = this.closest('form');
      
      // Usar alert simple y directo
      if (window.confirm(confirmMessage + '\n\nEsta acción no se puede deshacer.')) {
        if (form) {
          form.submit();
        }
      }
      
      return false;
    });
  });
}

// Función para mostrar notificación toast de confirmación
function showToastConfirmation(message, onConfirm) {
  // Subir al inicio de la página suavemente
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  });
  
  // Crear contenedor de toast si no existe
  let toastContainer = document.getElementById('toast-container');
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.id = 'toast-container';
    
    toastContainer.style.cssText = `
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      max-width: 350px;
    `;
    document.body.appendChild(toastContainer);
  }
  
  // Crear toast
  const toastId = 'toast-' + Date.now();
  const toastHTML = `
    <div id="${toastId}" class="toast show" role="alert" style="margin-bottom: 10px;">
      <div class="toast-header bg-danger text-white">
        <i class="bi bi-exclamation-triangle me-2"></i>
        <strong class="me-auto">Confirmar Eliminación</strong>
        <button type="button" class="btn-close btn-close-white" data-dismiss="toast"></button>
      </div>
      <div class="toast-body">
        <p class="mb-3">${message}</p>
        <div class="d-flex gap-2">
          <button type="button" class="btn btn-danger btn-sm flex-fill" data-action="confirm">
            <i class="bi bi-trash"></i> Eliminar
          </button>
          <button type="button" class="btn btn-secondary btn-sm flex-fill" data-action="cancel">
            Cancelar
          </button>
        </div>
      </div>
    </div>
  `;
  
  toastContainer.insertAdjacentHTML('beforeend', toastHTML);
  
  const toast = document.getElementById(toastId);
  const confirmBtn = toast.querySelector('[data-action="confirm"]');
  const cancelBtn = toast.querySelector('[data-action="cancel"]');
  const closeBtn = toast.querySelector('.btn-close');
  
  // Manejar confirmación
  confirmBtn.addEventListener('click', function() {
    onConfirm();
    toast.remove();
  });
  
  // Manejar cancelación
  const cancelHandler = () => toast.remove();
  cancelBtn.addEventListener('click', cancelHandler);
  closeBtn.addEventListener('click', cancelHandler);
  
  // Auto-cerrar después de 8 segundos
  setTimeout(() => {
    if (toast.parentNode) {
      toast.remove();
    }
  }, 8000);
}

// Función para mostrar modal de confirmación personalizado
function showConfirmationModal(message, onConfirm) {
  // Remover modal existente si lo hay
  const existingModal = document.getElementById('dynamicConfirmModal');
  if (existingModal) {
    existingModal.remove();
  }
  
  // Crear nuevo modal
  const modalHTML = `
    <div class="modal fade" id="dynamicConfirmModal" tabindex="-1" aria-labelledby="dynamicConfirmModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title" id="dynamicConfirmModalLabel">Confirmar Eliminación</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="d-flex align-items-center">
              <i class="bi bi-exclamation-triangle text-warning me-3" style="font-size: 2rem;"></i>
              <div>
                <p class="mb-0">${message}</p>
                <small class="text-muted">Esta acción no se puede deshacer.</small>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
            <button type="button" class="btn btn-danger" id="confirmDeleteBtn">
              <i class="bi bi-trash"></i> Eliminar
            </button>
          </div>
        </div>
      </div>
    </div>
  `;
  
  // Agregar al DOM
  document.body.insertAdjacentHTML('beforeend', modalHTML);
  
  // Configurar eventos
  const modal = document.getElementById('dynamicConfirmModal');
  const confirmBtn = document.getElementById('confirmDeleteBtn');
  
  confirmBtn.addEventListener('click', function() {
    // Cerrar modal
    const bsModal = bootstrap.Modal.getInstance(modal);
    if (bsModal) {
      bsModal.hide();
    }
    
    // Ejecutar acción de confirmación
    onConfirm();
  });
  
  // Limpiar modal cuando se cierre
  modal.addEventListener('hidden.bs.modal', function() {
    modal.remove();
  });
  
  // Mostrar modal
  const bsModal = new bootstrap.Modal(modal);
  bsModal.show();
}


// Deshabilitar pantallas de carga adicionales durante la navegación
window.addEventListener('DOMContentLoaded', () => {
  // Asegurarse de que no se muestre ninguna pantalla de carga durante la navegación normal
  const disableSplashOnNavigation = () => {
    const splashScreen = document.getElementById('splash-screen');
    if (splashScreen) {
      splashScreen.style.display = 'none';
    }
    
    // Almacenar en localStorage que el usuario ya ha visto la splash screen
    localStorage.setItem('splash_shown', 'true');
  };
  
  // Esperar 100ms para asegurarnos de que cualquier otra lógica de splash se ha ejecutado
  setTimeout(disableSplashOnNavigation, 100);
  
  // Prevenir que aparezca durante la navegación
  const links = document.querySelectorAll('a');
  links.forEach(link => {
    if (link.hostname === window.location.hostname) {
      link.addEventListener('click', () => {
        // Ocultar inmediatamente cualquier splash screen al hacer clic en un enlace interno
        disableSplashOnNavigation();
      });
    }
  });
});

// Función para mostrar el banner de instalación de PWA en dispositivos iOS
let deferredPrompt;

window.addEventListener('beforeinstallprompt', (e) => {
  // Prevenir que Chrome muestre automáticamente la ventana de instalación
  e.preventDefault();
  // Guardar el evento para usarlo más tarde
  deferredPrompt = e;
  // Mostrar nuestro botón personalizado de instalación
  showInstallPromotion();
});

function showInstallPromotion() {
  const installButton = document.getElementById('install-button');
  const installButtonContainer = document.getElementById('install-button-container');
  
  // Comprobar si estamos en la página de login
  const currentPath = window.location.pathname;
  const isLoginPage = currentPath === '/login' || currentPath === '/';
  
  // Solo mostrar en login
  if (isLoginPage && installButton && installButtonContainer) {
    // Mostrar el contenedor y el botón
    installButtonContainer.classList.remove('d-none');
    installButton.style.display = 'block';
    
    installButton.addEventListener('click', async () => {
      // Ocultar el botón de instalación
      installButton.style.display = 'none';
      installButtonContainer.classList.add('d-none');
      
      // Mostrar el diálogo de instalación
      deferredPrompt.prompt();
      
      // Esperar a que el usuario responda al diálogo
      const { outcome } = await deferredPrompt.userChoice;
      console.log(`User response to the install prompt: ${outcome}`);
      
      // Limpiar el evento guardado
      deferredPrompt = null;
    });
  } else if (installButton && installButtonContainer) {
    // Ocultar el botón en las demás páginas
    installButtonContainer.classList.add('d-none');
    installButton.style.display = 'none';
  }
}

// Detectar si la aplicación ya está instalada
window.addEventListener('appinstalled', (evt) => {
  console.log('Aplicación instalada');
  // Ocultar el botón de instalación si está visible
  const installButton = document.getElementById('install-button');
  if (installButton) {
    installButton.style.display = 'none';
  }
});

// Detección de estado online/offline
function updateOnlineStatus() {
  const onlineStatus = document.querySelector('.online-status');
  const offlineStatus = document.querySelector('.offline-status');
  const offlineNotification = document.getElementById('offline-notification');
  
  if (navigator.onLine) {
    // Estamos online
    if (onlineStatus) onlineStatus.style.display = 'inline-block';
    if (offlineStatus) offlineStatus.style.display = 'none';
    
    // Ocultar notificación offline
    if (offlineNotification) {
      offlineNotification.classList.remove('visible');
    }
    
    // Intentar sincronizar datos pendientes
    if ('serviceWorker' in navigator && 'SyncManager' in window) {
      navigator.serviceWorker.ready
        .then(registration => {
          return registration.sync.register('sync-checkpoints');
        })
        .catch(err => console.log('Error al registrar la sincronización: ', err));
    }
  } else {
    // Estamos offline
    if (onlineStatus) onlineStatus.style.display = 'none';
    if (offlineStatus) offlineStatus.style.display = 'inline-block';
    
    // Mostrar notificación offline
    if (offlineNotification) {
      offlineNotification.classList.add('visible');
      
      // Ocultar después de 5 segundos
      setTimeout(() => {
        offlineNotification.classList.remove('visible');
      }, 5000);
    }
  }
}

// Registrar los eventos de cambio de estado de conexión
window.addEventListener('online', updateOnlineStatus);
window.addEventListener('offline', updateOnlineStatus);

// Comprobar el estado inicial cuando la página se carga
document.addEventListener('DOMContentLoaded', () => {
  updateOnlineStatus();
});

// Función para alternar la visibilidad de la contraseña
function togglePasswordVisibility(passwordId, buttonId) {
    const passwordInput = document.getElementById(passwordId);
    const toggleButton = document.getElementById(buttonId);
    
    if (passwordInput && toggleButton) {
        toggleButton.addEventListener('click', function() {
            // Cambiar el tipo de input entre password y text
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            
            // Cambiar el ícono
            const icon = toggleButton.querySelector('i');
            if (icon) {
                if (type === 'password') {
                    icon.classList.remove('bi-eye-slash');
                    icon.classList.add('bi-eye');
                } else {
                    icon.classList.remove('bi-eye');
                    icon.classList.add('bi-eye-slash');
                }
            }
        });
    }
}

// Función para comprobar si la aplicación está en modo standalone (instalada)
function isRunningStandalone() {
  return (window.matchMedia('(display-mode: standalone)').matches) ||
         (window.navigator.standalone) || // Para iOS
         document.referrer.includes('android-app://');
}

// Ajustar la interfaz si la aplicación está instalada
if (isRunningStandalone()) {
  console.log('La aplicación está ejecutándose en modo instalado');
  // Añadir clase para estilos específicos de la PWA instalada
  document.body.classList.add('pwa-installed');
  
  // Mostrar la pantalla de splash SOLO en la primera carga después de instalar
  document.addEventListener('DOMContentLoaded', () => {
    const splashScreen = document.getElementById('splash-screen');
    // Comprobar si es la primera vez que se abre la aplicación
    const isFirstVisit = sessionStorage.getItem('app_initialized') !== 'true';
    
    if (splashScreen && isFirstVisit) {
      // Establecer que la aplicación ya ha sido inicializada
      sessionStorage.setItem('app_initialized', 'true');
      
      // Mostrar splash screen
      splashScreen.style.display = 'flex';
      
      // Ocultar después de 1 segundo (reducido de 2 segundos)
      setTimeout(() => {
        splashScreen.classList.add('hidden');
        // Eliminar completamente después de que termine la animación
        setTimeout(() => {
          splashScreen.style.display = 'none';
        }, 300); // Reducido de 500ms
      }, 1000); // Reducido de 2000ms
    } else if (splashScreen) {
      // Si no es la primera visita, ocultar la pantalla de splash inmediatamente
      splashScreen.style.display = 'none';
    }
  });
} else {
  // Mostrar instrucciones de instalación para iOS
  // Esperar a que el DOM esté completamente cargado
  document.addEventListener('DOMContentLoaded', () => {
    // Reducir el tiempo de espera para mostrar las instrucciones
    setTimeout(showIOSInstallInstructions, 1000); // Reducido de 2000ms
  });
}

// Función para mostrar un banner para usuarios de iOS (que no tienen soporte nativo para PWA)
function showIOSInstallInstructions() {
  // Detectar si es un dispositivo iOS
  const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream;
  const isInStandaloneMode = window.navigator.standalone;
  
  // Comprobar si estamos en la página de login
  const currentPath = window.location.pathname;
  const isLoginPage = currentPath === '/login' || currentPath === '/';
  
  // Solo mostrar en login
  if (isLoginPage && isIOS && !isInStandaloneMode) {
    const iosBanner = document.createElement('div');
    iosBanner.innerHTML = `
      <div id="ios-install-banner">
        <span class="close-banner" onclick="this.parentElement.style.display='none'">&times;</span>
        <p>Instala esta aplicación en tu iPhone: pulsa <strong>Compartir</strong> <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/><polyline points="16 6 12 2 8 6"/><line x1="12" y1="2" x2="12" y2="15"/></svg> y luego <strong>Añadir a la pantalla de inicio</strong></p>
      </div>
    `;
    document.body.appendChild(iosBanner);
    
    // Estilos para el banner con colores tierra
    const style = document.createElement('style');
    style.textContent = `
      #ios-install-banner {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        background-color: #5c4033;
        color: white;
        padding: 12px;
        text-align: center;
        z-index: 9999;
        font-family: 'Roboto', Arial, sans-serif;
        box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.2);
      }
      .close-banner {
        position: absolute;
        right: 10px;
        top: 5px;
        cursor: pointer;
        font-size: 20px;
      }
    `;
    document.head.appendChild(style);
  }
}
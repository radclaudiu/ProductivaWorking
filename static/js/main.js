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
  
  if (installButton && installButtonContainer) {
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

// Función para comprobar si la aplicación está en modo standalone (instalada)
function isRunningStandalone() {
  return (window.matchMedia('(display-mode: standalone)').matches) ||
         (window.navigator.standalone) || // Para iOS
         document.referrer.includes('android-app://');
}

// Ajustar la interfaz si la aplicación está instalada
if (isRunningStandalone()) {
  console.log('La aplicación está ejecutándose en modo instalado');
  // Aquí puedes añadir código para ajustar la interfaz si es necesario
  document.body.classList.add('pwa-installed');
}

// Función para mostrar un banner para usuarios de iOS (que no tienen soporte nativo para PWA)
function showIOSInstallInstructions() {
  // Detectar si es un dispositivo iOS
  const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream;
  const isInStandaloneMode = window.navigator.standalone;
  
  if (isIOS && !isInStandaloneMode) {
    const iosBanner = document.createElement('div');
    iosBanner.innerHTML = `
      <div id="ios-install-banner">
        <span class="close-banner" onclick="this.parentElement.style.display='none'">&times;</span>
        <p>Instala esta aplicación en tu iPhone: pulsa <strong>Compartir</strong> <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/><polyline points="16 6 12 2 8 6"/><line x1="12" y1="2" x2="12" y2="15"/></svg> y luego <strong>Añadir a la pantalla de inicio</strong></p>
      </div>
    `;
    document.body.appendChild(iosBanner);
    
    // Estilos para el banner
    const style = document.createElement('style');
    style.textContent = `
      #ios-install-banner {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        background-color: #3498db;
        color: white;
        padding: 12px;
        text-align: center;
        z-index: 9999;
        font-family: Arial, sans-serif;
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
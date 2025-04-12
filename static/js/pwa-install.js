// Variables para el banner de instalación
let deferredPrompt;
const installBanner = document.getElementById('pwa-install-banner');
const installButton = document.getElementById('pwa-install-button');
const closeBanner = document.getElementById('pwa-close-banner');

// Escuchar el evento beforeinstallprompt
window.addEventListener('beforeinstallprompt', (e) => {
  // Prevenir que Chrome muestre automáticamente el diálogo de instalación
  e.preventDefault();
  
  // Guardar el evento para usarlo después
  deferredPrompt = e;
  
  // Mostrar el banner solo si no está guardado en localStorage
  if (!localStorage.getItem('pwaInstallDismissed')) {
    if (installBanner) {
      installBanner.classList.remove('d-none');
    }
  }
});

// Configurar el botón de instalación
if (installButton) {
  installButton.addEventListener('click', async () => {
    if (!deferredPrompt) {
      return;
    }
    
    // Mostrar el diálogo de instalación
    deferredPrompt.prompt();
    
    // Esperar a que el usuario responda
    const { outcome } = await deferredPrompt.userChoice;
    console.log(`User response to the install prompt: ${outcome}`);
    
    // Limpiar la variable deferredPrompt ya que ya no se puede usar
    deferredPrompt = null;
    
    // Ocultar el banner
    if (installBanner) {
      installBanner.classList.add('d-none');
    }
  });
}

// Configurar el botón para cerrar el banner
if (closeBanner) {
  closeBanner.addEventListener('click', () => {
    if (installBanner) {
      installBanner.classList.add('d-none');
      // Guardar en localStorage para no mostrar el banner de nuevo
      localStorage.setItem('pwaInstallDismissed', 'true');
    }
  });
}

// Detectar si la aplicación ya está instalada
window.addEventListener('appinstalled', () => {
  // Ocultar el banner una vez que la aplicación está instalada
  if (installBanner) {
    installBanner.classList.add('d-none');
  }
  console.log('PWA fue instalada correctamente');
});
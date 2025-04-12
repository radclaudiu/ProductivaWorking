// Variables para la instalación
let deferredPrompt;
let installBanner;
let installButton;
let closeBanner;
let navbarInstallButton;

// Función para instalar la PWA
const installPWA = async () => {
  if (!deferredPrompt) {
    console.log('No hay prompt de instalación disponible');
    alert('La instalación no está disponible en este momento. Asegúrate de usar Chrome o Edge en Android, o intenta agregar la aplicación manualmente a la pantalla de inicio desde el menú del navegador.');
    return;
  }
  
  // Mostrar el diálogo de instalación
  deferredPrompt.prompt();
  
  // Esperar a que el usuario responda
  const { outcome } = await deferredPrompt.userChoice;
  console.log(`Respuesta del usuario al prompt de instalación: ${outcome}`);
  
  // Limpiar la variable deferredPrompt ya que ya no se puede usar
  deferredPrompt = null;
  
  // Ocultar el banner
  if (installBanner) {
    installBanner.classList.add('d-none');
  }
};

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
  installBanner = document.getElementById('pwa-install-banner');
  installButton = document.getElementById('pwa-install-button');
  closeBanner = document.getElementById('pwa-close-banner');
  navbarInstallButton = document.getElementById('navbar-install-pwa');
  
  // Inicialmente ocultar el botón de instalar en la barra de navegación
  // Lo mostraremos solo cuando la app sea instalable
  if (navbarInstallButton) {
    navbarInstallButton.style.display = 'none';
  }
  
  // Configurar el botón de instalación en el banner
  if (installButton) {
    installButton.addEventListener('click', installPWA);
  }
  
  // Configurar el botón de instalación en la barra de navegación
  if (navbarInstallButton) {
    navbarInstallButton.addEventListener('click', (e) => {
      e.preventDefault();
      installPWA();
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
  
  // Mostrar mensaje para debugging en consola
  if ('serviceWorker' in navigator) {
    console.log('Service Worker es soportado');
  } else {
    console.log('Service Worker NO es soportado');
  }
  
  const isStandalone = window.matchMedia('(display-mode: standalone)').matches;
  if (isStandalone) {
    console.log('Aplicación ejecutándose en modo standalone');
  }
});

// Escuchar el evento beforeinstallprompt
window.addEventListener('beforeinstallprompt', (e) => {
  console.log('Evento beforeinstallprompt capturado!');
  
  // Prevenir que Chrome muestre automáticamente el diálogo de instalación
  e.preventDefault();
  
  // Guardar el evento para usarlo después
  deferredPrompt = e;
  
  // Mostrar el botón de instalación en la barra de navegación
  if (navbarInstallButton) {
    navbarInstallButton.style.display = 'block';
    console.log('Botón de instalación en barra de navegación activado');
  }
  
  // Mostrar el banner solo si no está guardado en localStorage
  if (!localStorage.getItem('pwaInstallDismissed')) {
    if (installBanner) {
      installBanner.classList.remove('d-none');
      console.log('Banner de instalación mostrado');
    } else {
      console.log('Banner de instalación no encontrado en el DOM');
    }
  } else {
    console.log('Banner de instalación fue descartado anteriormente');
  }
});

// Detectar si la aplicación ya está instalada
window.addEventListener('appinstalled', () => {
  // Ocultar el banner una vez que la aplicación está instalada
  if (installBanner) {
    installBanner.classList.add('d-none');
  }
  console.log('PWA fue instalada correctamente');
  
  // Opcional: Mostrar un mensaje de agradecimiento
  alert('¡Gracias por instalar Productiva! Ahora puedes acceder desde el icono en tu pantalla de inicio.');
});
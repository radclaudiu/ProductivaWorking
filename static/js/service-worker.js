// Nombre de la caché
const CACHE_NAME = 'productiva-pwa-cache-v1';

// Archivos a cachear para funcionamiento offline
const urlsToCache = [
  '/',
  '/login',
  '/dashboard',
  '/checkpoints',
  '/static/css/styles.css',
  '/static/js/main.js',
  '/static/manifest.json',
  '/static/icons/icon-72x72.png',
  '/static/icons/icon-96x96.png',
  '/static/icons/icon-128x128.png',
  '/static/icons/icon-144x144.png',
  '/static/icons/icon-152x152.png',
  '/static/icons/icon-192x192.png',
  '/static/icons/icon-384x384.png',
  '/static/icons/icon-512x512.png',
  '/static/favicon.ico'
];

// Instalar el Service Worker
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => {
        console.log('Cache abierta');
        return cache.addAll(urlsToCache);
      })
  );
});

// Interceptar peticiones y servir desde cache si están disponibles
self.addEventListener('fetch', (event) => {
  // No cachear peticiones POST (API) o peticiones a URLs externas
  if (event.request.method !== 'GET' || !event.request.url.startsWith(self.location.origin)) {
    return;
  }

  event.respondWith(
    caches.match(event.request)
      .then((response) => {
        // Servir desde cache si existe
        if (response) {
          return response;
        }

        // Hacer la petición a la red
        return fetch(event.request).then(
          (response) => {
            // Verificar respuesta válida
            if(!response || response.status !== 200 || response.type !== 'basic') {
              return response;
            }

            // Clonar la respuesta para poder almacenarla en caché
            var responseToCache = response.clone();

            caches.open(CACHE_NAME)
              .then((cache) => {
                cache.put(event.request, responseToCache);
              });

            return response;
          }
        );
      })
      .catch(() => {
        // Si falla la red y no hay caché, mostrar página offline (si existe)
        if (event.request.mode === 'navigate') {
          return caches.match('/offline.html');
        }
      })
  );
});

// Activar el Service Worker y eliminar cachés antiguas
self.addEventListener('activate', (event) => {
  const cacheWhitelist = [CACHE_NAME];

  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheWhitelist.indexOf(cacheName) === -1) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});

// Sincronización en segundo plano para los datos de la aplicación
self.addEventListener('sync', (event) => {
  if (event.tag === 'sync-checkpoints') {
    event.waitUntil(syncCheckpoints());
  }
});

// Función para sincronizar datos de checkpoints cuando vuelve la conexión
async function syncCheckpoints() {
  // Simulación de sincronización de datos (en una implementación real 
  // se recuperarían datos de indexedDB y se enviarían al servidor)
  console.log('Sincronizando datos de checkpoints...');
  
  // Implementación real que sería necesaria:
  // 1. Recuperar datos pendientes de IndexedDB
  // 2. Enviar datos al servidor
  // 3. Actualizar el estado en IndexedDB
}

// Manejo de notificaciones push
self.addEventListener('push', (event) => {
  const data = event.data.json();
  
  const options = {
    body: data.body,
    icon: '/static/icons/icon-192x192.png',
    badge: '/static/icons/icon-72x72.png',
    vibrate: [100, 50, 100],
    data: {
      dateOfArrival: Date.now(),
      primaryKey: '1'
    },
    actions: [
      {
        action: 'explore',
        title: 'Ver detalles',
        icon: '/static/icons/icon-72x72.png'
      },
      {
        action: 'close',
        title: 'Cerrar',
        icon: '/static/icons/icon-72x72.png'
      }
    ]
  };

  event.waitUntil(
    self.registration.showNotification(data.title, options)
  );
});
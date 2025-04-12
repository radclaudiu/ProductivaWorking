// Nombre de la caché
const CACHE_NAME = 'productiva-static-v1';

// Solo recursos estáticos
const STATIC_ASSETS = [
  '/static/css/bootstrap.min.css',
  '/static/css/style.css',
  '/static/js/bootstrap.bundle.min.js',
  '/static/js/jquery.min.js',
  '/static/js/pwa-install.js',
  '/static/pwa/icon-192.png',
  '/static/pwa/icon-512.png',
  '/static/offline.html',
  '/manifest.json'
];

// Instalación del Service Worker
self.addEventListener('install', event => {
  console.log('Service Worker: Instalando...');
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        console.log('Service Worker: Cacheando archivos estáticos');
        return cache.addAll(STATIC_ASSETS);
      })
      .then(() => {
        console.log('Service Worker: Todos los recursos han sido cacheados');
        return self.skipWaiting();
      })
  );
});

// Activación
self.addEventListener('activate', event => {
  console.log('Service Worker: Activando...');
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cache => {
          if (cache !== CACHE_NAME) {
            console.log('Service Worker: Limpiando caché antigua', cache);
            return caches.delete(cache);
          }
        })
      );
    })
    .then(() => {
      console.log('Service Worker: Ahora está activo y controlando la página');
      return self.clients.claim();
    })
  );
});

// Estrategia "Stale While Revalidate" solo para recursos estáticos
self.addEventListener('fetch', event => {
  // Ignorar solicitudes que no sean GET o que sean a rutas específicas
  if (event.request.method !== 'GET' || 
      event.request.url.includes('/api/') ||
      event.request.url.includes('/login') ||
      event.request.url.includes('/dashboard')) {
    return;
  }
  
  // Verificar si la solicitud es para un recurso estático que deberíamos cachear
  const isStaticAsset = STATIC_ASSETS.some(asset => 
    event.request.url.includes(asset));
  
  if (isStaticAsset) {
    console.log('Service Worker: Recuperando desde caché', event.request.url);
    event.respondWith(
      caches.match(event.request)
        .then(cachedResponse => {
          // Devolver cache mientras revalidamos en segundo plano
          const fetchPromise = fetch(event.request)
            .then(networkResponse => {
              // Actualizar la caché con la nueva respuesta
              if (networkResponse.ok) {
                caches.open(CACHE_NAME)
                  .then(cache => {
                    cache.put(event.request, networkResponse.clone());
                  });
              }
              return networkResponse;
            })
            .catch(error => {
              console.error('Service Worker: Error al recuperar recurso', error);
            });
          
          return cachedResponse || fetchPromise;
        })
    );
  } else {
    // Para otras solicitudes, intentamos obtenerlas de la red y mostramos
    // la página offline si hay un error
    event.respondWith(
      fetch(event.request)
        .catch(() => {
          return caches.match('/static/offline.html');
        })
    );
  }
});
# Configuración de Conexión para Productiva Android

## Correcciones Realizadas

Se han unificado las URLs de conexión que estaban inconsistentes en diferentes partes del código para que la aplicación se conecte a la web de Productiva:

1. **ApiClient.kt**:
   - Anterior: `DEFAULT_SERVER_URL = "https://productiva.example.com/api/"`
   - Nuevo: `DEFAULT_SERVER_URL = "https://workspace.replit.app/api/"`

2. **ApiConfig.kt**:
   - Anterior: `BASE_URL = "https://productiva.replit.app/api/"`
   - Nuevo: `BASE_URL = "https://workspace.replit.app/api/"`

3. **PreferenceManager.kt**:
   - Anterior: `DEFAULT_SERVER_URL = "http://192.168.1.1:5000"`
   - Nuevo: `DEFAULT_SERVER_URL = "https://workspace.replit.app"`

## Conexión al Servidor

Para desarrolladores que están trabajando con el simulador o emulador en Android Studio, `localhost` o `10.0.2.2` se refiere a la máquina de desarrollo (host) y no al dispositivo Android.

### Opciones de Configuración

Dependiendo de tu entorno de desarrollo, puede que necesites usar una de estas configuraciones:

#### Para emulador Android (AVD):
```kotlin
// Usa 10.0.2.2 para conectar con el localhost de la máquina host desde el emulador
private const val DEFAULT_SERVER_URL = "http://10.0.2.2:5000/api/"
```

#### Para dispositivo físico en la misma red WiFi:
```kotlin
// Usa la IP de tu computadora en la red local
private const val DEFAULT_SERVER_URL = "http://192.168.1.xxx:5000/api/"
```

#### Para entorno de producción:
```kotlin
// Usa la URL del servidor de producción
private const val DEFAULT_SERVER_URL = "https://api.productiva.com/api/"
```

## Cómo Modificar la URL en Tiempo de Ejecución

La aplicación permite cambiar la URL del servidor en tiempo de ejecución:

1. Usando la pantalla de configuración de la aplicación.
2. Llamando al método `updateServerUrl` del `ApiClient`:

```kotlin
val apiClient = ApiClient.getInstance(context)
apiClient.updateServerUrl("http://nueva-url-servidor.com/api/")
```

## Troubleshooting

Si encuentras problemas de conexión:

1. **Verifica que la URL sea correcta**: Asegúrate de incluir el protocolo (http/https) y el puerto si es necesario.
2. **Verifica que el servidor esté activo**: Intenta acceder a la URL desde un navegador.
3. **Configuración de red**: En dispositivos físicos, asegúrate de que el dispositivo y el servidor estén en la misma red.
4. **Permisos de Internet**: Verifica que la aplicación tiene permisos de Internet en el `AndroidManifest.xml`.
5. **Borrar datos de caché**: A veces, las configuraciones antiguas pueden persistir. Intenta borrar los datos de la aplicación.

```xml
<!-- En AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

## Credenciales de Prueba

Para realizar pruebas con la aplicación, usa las siguientes credenciales:

Usuario: `admin@example.com`
Contraseña: `admin`

Si estas credenciales no funcionan, es posible que tengas que usar las que están configuradas en tu servidor local.
# Guía para Configurar JDK 8 en Android Studio

## ¿Por qué es necesario JDK 8?

El error que estamos experimentando está relacionado con incompatibilidades entre KAPT (el procesador de anotaciones de Kotlin) y versiones más recientes de Java (Java 11, 17, etc.). Aunque se han intentado muchas soluciones, la más fiable y directa es configurar explícitamente JDK 8 para este proyecto.

## Paso 1: Descargar e Instalar JDK 8

### Para Windows:
1. Visita [Eclipse Temurin (anteriormente AdoptOpenJDK)](https://adoptium.net/temurin/releases/?version=8)
2. Descarga la versión JDK 8 para Windows (x64)
3. Ejecuta el instalador y sigue las instrucciones

### Para macOS:
1. Visita [Eclipse Temurin](https://adoptium.net/temurin/releases/?version=8)
2. Descarga la versión JDK 8 para macOS
3. Abre el paquete .pkg y sigue las instrucciones de instalación

### Para Linux:
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-8-jdk

# Fedora/Red Hat
sudo dnf install java-1.8.0-openjdk-devel
```

## Paso 2: Configurar Android Studio para usar JDK 8

1. Cierra completamente Android Studio si está abierto
2. Abre Android Studio y carga el proyecto Productiva
3. Ve al menú **File → Settings** (en macOS: **Android Studio → Preferences**)
4. Navega a **Build, Execution, Deployment → Build Tools → Gradle**
5. En el campo **Gradle JDK**, haz clic en el desplegable
6. Selecciona **Download JDK...**
7. En el diálogo, selecciona:
   - **Vendor**: Eclipse Temurin (anteriormente AdoptOpenJDK)
   - **Version**: 8
   - **Location**: Deja la ubicación predeterminada
8. Haz clic en **Download**
9. Espera a que se complete la descarga y haz clic en **OK**
10. Sincroniza el proyecto con archivos Gradle (**File → Sync Project with Gradle Files**)

## Paso 3: Verificar la Configuración

1. Abre la terminal dentro de Android Studio (**View → Tool Windows → Terminal**)
2. Ejecuta `./gradlew --version`
3. Verifica que la salida muestre que está usando JDK 8:
   ```
   JVM:          1.8.0_xxx (Eclipse Adoptium)
   ```

## Paso 4: Modificar archivo local.properties (si es necesario)

Si Android Studio aún tiene problemas para encontrar o usar JDK 8, puedes forzar el uso editando el archivo `local.properties`:

### Windows:
```properties
# Añade esta línea al final de local.properties
org.gradle.java.home=C:\\Program Files\\Eclipse Adoptium\\jdk-8.0.xx
# Ajusta la ruta según tu instalación
```

### macOS:
```properties
# Añade esta línea al final de local.properties
org.gradle.java.home=/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home
# Ajusta la ruta según tu instalación
```

### Linux:
```properties
# Añade esta línea al final de local.properties
org.gradle.java.home=/usr/lib/jvm/java-8-openjdk-amd64
# Ajusta la ruta según tu instalación
```

## Paso 5: Limpiar y Reconstruir el Proyecto

1. En Android Studio, selecciona **Build → Clean Project**
2. Espera a que se complete
3. Luego selecciona **Build → Rebuild Project**

## Solución de Problemas

Si continúas teniendo problemas:

1. Ejecuta nuestro script de solución definitiva:
   - Windows: `SOLUCION_DEFINITIVA_KAPT.bat`
   - Linux/macOS: `./solucion_definitiva_kapt.sh`

2. Asegúrate de que JDK 8 esté en tu PATH:
   ```bash
   # Verifica la versión de Java en uso
   java -version
   ```

3. Si tienes múltiples versiones de Java instaladas, configura JAVA_HOME para apuntar a JDK 8 durante la compilación:
   ```bash
   # Windows (PowerShell)
   $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-8.0.xx"
   
   # Linux/macOS
   export JAVA_HOME=/path/to/jdk8
   ```

4. Intenta ejecutar Gradle con JDK 8 explícitamente:
   ```bash
   ./gradlew build -Dorg.gradle.java.home=/path/to/jdk8
   ```

## Nota Importante

Este ajuste es específico para este proyecto debido a las dependencias que requieren KAPT. Para proyectos futuros, considera:

1. Usar KSP en lugar de KAPT (cuando sea posible)
2. Actualizar a versiones más recientes de Kotlin y AGP que tengan mejor compatibilidad con Java 11+
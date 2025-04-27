# Migración a JDK 11

Este documento describe la migración de la aplicación Productiva Android desde JDK 8 a JDK 11.

## Cambios realizados

1. **En app/build.gradle**:
   - Actualizada la configuración de `compileOptions` para usar Java 11:
     ```groovy
     compileOptions {
         coreLibraryDesugaringEnabled true
         sourceCompatibility JavaVersion.VERSION_11
         targetCompatibility JavaVersion.VERSION_11
     }
     ```
   - Actualizada la configuración de `kotlinOptions` para usar Java 11:
     ```groovy
     kotlinOptions {
         jvmTarget = '11'
         // Se mantienen los argumentos de compatibilidad
         freeCompilerArgs += [
             "-Xjvm-default=all",
             "-Xsam-conversions=class"
         ]
     }
     ```
   - Actualizado JVM Toolchain para Kotlin:
     ```groovy
     kotlin {
         jvmToolchain(11)
     }
     ```

2. **En build.gradle (raíz)**:
   - Agregada configuración global de Java 11:
     ```groovy
     buildscript {
         // ...
         ext {
             javaVersion = JavaVersion.VERSION_11
         }
     }
     ```
   - Configuración automática para subproyectos:
     ```groovy
     subprojects {
         afterEvaluate {
             if (project.hasProperty('android')) {
                 android {
                     compileOptions {
                         sourceCompatibility = JavaVersion.VERSION_11
                         targetCompatibility = JavaVersion.VERSION_11
                     }
                 }
                 
                 // Para proyectos Kotlin
                 if (project.hasProperty('kotlin')) {
                     kotlin {
                         jvmToolchain(11)
                     }
                 }
             }
         }
     }
     ```

3. **En settings.gradle**:
   - Agregada configuración específica para plugins:
     ```groovy
     pluginManagement {
         // ...
         plugins {
             id 'com.android.application' version '8.2.0'
             id 'com.android.library' version '8.2.0'
             id 'org.jetbrains.kotlin.android' version '1.9.0'
             id 'com.google.devtools.ksp' version '1.9.0-1.0.13'
         }
     }
     ```

4. **En local.properties**:
   - Agregada ruta al JDK 11 para el entorno local:
     ```properties
     # Configuración JDK 11 para este equipo
     org.gradle.java.home=C:\\Program Files\\Java\\jdk-11
     ```

5. **En gradle.properties**:
   - Actualizada la configuración de Java Toolchain para usar JDK 11:
     ```properties
     # Configuración de Java Toolchain - permite usar JDK 11
     org.gradle.java.installations.auto-download=true
     org.gradle.java.installations.auto-detect=true
     org.gradle.java.home.version=11
     ```
   - Se mantienen los argumentos JVM para compatibilidad con módulos Java:
     ```properties
     org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8 \
       --add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
       --add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
       ...
     ```

## Beneficios de JDK 11

La migración a JDK 11 proporciona las siguientes ventajas:

1. **Mejoras de rendimiento**: JDK 11 incluye optimizaciones significativas en comparación con JDK 8.
2. **Características modernas del lenguaje**:
   - Inferencia de tipos para variables locales (`var`)
   - Mejoras en la API de String (isBlank, lines, repeat, etc.)
   - API HTTP Client moderna
   - APIs de colecciones mejoradas
3. **Mayor compatibilidad** con las últimas bibliotecas y herramientas.
4. **Soporte a largo plazo**: Java 11 es una versión LTS (Long Term Support).
5. **Mejor manejo de memoria** con recolector de basura mejorado.

## Requisitos para desarrollo

Para trabajar con la aplicación, asegúrate de tener instalado:

1. **JDK 11** (recomendado: AdoptOpenJDK, Amazon Corretto, o Oracle JDK)
2. **Android Studio Flamingo (2022.2.1)** o superior
3. **Gradle 8.2** o superior (ya configurado en el proyecto con la versión 8.2.0)

## Verificación de la configuración

Para verificar que estás usando Java 11 en Android Studio:

1. Ve a **File > Project Structure > SDK Location**
2. En "JDK Location" debe aparecer una ruta a JDK 11
3. Si no es así, instala JDK 11 y selecciónalo en esta ventana

Alternativamente, puedes verificar desde línea de comandos:

```bash
# En la carpeta raíz del proyecto
./gradlew --version
```

Debería mostrar "JVM: 11.x.x" en la salida.

## Posibles problemas y soluciones

### Error: "Unsupported class file major version XX"

**Solución**: Verifica que estás usando JDK 11 tanto para la compilación como para ejecutar Android Studio.

### Error con KAPT

**Solución**: Asegúrate de que tienes las siguientes configuraciones en `gradle.properties`:
```properties
kapt.incremental.apt=true
kapt.use.worker.api=true
kapt.include.compile.classpath=false
```

### Error de compatibilidad con librería externa

**Solución**: Si alguna librería no es compatible con Java 11, puedes:
1. Buscar una versión más reciente de la librería
2. Contactar a los mantenedores de la librería
3. Usar desugaring para mantener compatibilidad retroactiva

## Notas adicionales

- Esta migración no afecta la compatibilidad con dispositivos Android antiguos, ya que el bytecode final sigue siendo compatible.
- El nivel de API mínimo sigue siendo 26 (Android 8.0).

## Script de verificación

El siguiente script puede utilizarse para verificar que la configuración de JDK 11 está correctamente aplicada:

```bash
#!/bin/bash
# verify_jdk11.sh - Script para verificar configuración de JDK 11

echo "=== Verificación de configuración JDK 11 ==="
echo

# Comprobar versión de Gradle y JDK
echo "Comprobando versión de Gradle y JDK..."
./gradlew --version
echo

# Verificar archivos de configuración
echo "Verificando archivos de configuración..."

# app/build.gradle
if grep -q "JavaVersion.VERSION_11" app/build.gradle && grep -q "jvmTarget = '11'" app/build.gradle; then
    echo "✅ app/build.gradle: Correctamente configurado para JDK 11"
else
    echo "❌ app/build.gradle: Falta configuración para JDK 11"
fi

# build.gradle (raíz)
if grep -q "javaVersion = JavaVersion.VERSION_11" build.gradle; then
    echo "✅ build.gradle (raíz): Correctamente configurado para JDK 11"
else
    echo "❌ build.gradle (raíz): Falta configuración para JDK 11"
fi

# gradle.properties
if grep -q "org.gradle.java.home.version=11" gradle.properties; then
    echo "✅ gradle.properties: Correctamente configurado para JDK 11"
else
    echo "❌ gradle.properties: Falta configuración para JDK 11"
fi

# settings.gradle
if grep -q "pluginManagement" settings.gradle && grep -q "plugins" settings.gradle; then
    echo "✅ settings.gradle: Tiene configuración de plugins"
else
    echo "❌ settings.gradle: Falta configuración de plugins"
fi

echo
echo "Verificación completada."
```

Guarda este script como `verify_jdk11.sh` en la raíz del proyecto, dale permisos de ejecución con `chmod +x verify_jdk11.sh` y ejecútalo para verificar la configuración.
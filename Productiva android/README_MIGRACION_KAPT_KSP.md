# Migración de KAPT a KSP

## Cambios Realizados

Para solucionar el error `java.lang.IllegalAccessError: superclass access check failed: class org.jetbrains.kotlin.kapt3.base.javac.KaptJavaCompiler`, se han realizado las siguientes modificaciones:

1. Se reemplazó el plugin KAPT por KSP en el archivo `app/build.gradle`:
   ```gradle
   // Antes
   id 'kotlin-kapt'
   
   // Después
   id 'com.google.devtools.ksp'
   ```

2. Se agregó el plugin KSP al archivo `build.gradle` principal:
   ```gradle
   plugins {
       // ...
       id 'com.google.devtools.ksp' version '1.9.0-1.0.11' apply false
   }
   ```

3. Se cambiaron las dependencias de procesadores de anotaciones de KAPT a KSP:
   ```gradle
   // Antes
   kapt 'androidx.room:room-compiler:2.6.1'
   
   // Después
   ksp 'androidx.room:room-compiler:2.6.1'
   ```

4. Se añadieron configuraciones para KSP en `gradle.properties`:
   ```
   ksp.incremental=true
   ```

5. Se añadieron flags de compatibilidad de JVM en `gradle.properties` para lidiar con módulos Java:
   ```
   org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8 \
     --add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED \
     --add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED \
     --add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
     --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
   ```

## Beneficios de KSP sobre KAPT

- **Mayor velocidad**: KSP es hasta un 40% más rápido que KAPT.
- **Mejor compatibilidad**: KSP funciona perfectamente con Java 11+.
- **API más segura**: KSP proporciona una API mejor documentada y más estable.
- **Incremental por defecto**: KSP tiene mejor soporte para compilación incremental.

## Limpieza para reconstruir el proyecto

Después de estos cambios, es recomendable limpiar y reconstruir el proyecto desde cero:

1. Ejecuta el script de limpieza: `COMANDOS_PARA_REPARAR_ERROR_KAPT.bat` (Windows) o `./reparar_error_kapt.sh` (Linux/Mac)
2. Cierra y vuelve a abrir Android Studio
3. Sincroniza el proyecto con los archivos Gradle
4. Reconstruye el proyecto

## Nota adicional

Si sigues experimentando problemas, una solución alternativa es configurar Android Studio para usar Java 8:

1. Ve a File → Settings → Build, Execution, Deployment → Build Tools → Gradle
2. Cambia "Gradle JDK" a la versión 8 instalada
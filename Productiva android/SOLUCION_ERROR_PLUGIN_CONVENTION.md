# Solución para el Error "org.gradle.api.plugins.convention type has been deprecated"

## Descripción del Problema

El error "org.gradle.api.plugins.convention type has been deprecated" ocurre porque las versiones recientes de Gradle (7.0+) han dejado obsoleta la API de convención de plugins que se usaba en versiones anteriores. Esta API permitía personalizar las configuraciones de proyecto mediante convenciones, pero ahora se recomienda usar el nuevo modelo de plugins.

## Archivos Afectados

Este error puede aparecer en diferentes archivos de configuración Gradle:

1. `build.gradle` (raíz del proyecto)
2. `app/build.gradle`
3. Posibles plugins personalizados que usen la API de convención

## Solución

Hemos creado scripts para automatizar la corrección de este problema:

* **Para Windows**: Ejecuta el archivo `CORREGIR_ERROR_PLUGIN_CONVENTION.bat`
* **Para Linux/Mac**: Ejecuta el archivo `corregir_error_plugin_convention.sh` (necesitarás hacerlo ejecutable con `chmod +x corregir_error_plugin_convention.sh`)

Estos scripts realizarán los siguientes cambios automáticamente:

### 1. Actualizar la sintaxis en build.gradle (raíz)

Cambiar cualquier uso de convenciones antiguas al nuevo estilo de plugins:

```groovy
// ANTES (convención antigua)
apply plugin: 'java-library'
convention.plugins.java.sourceCompatibility = JavaVersion.VERSION_1_8

// DESPUÉS (estilo nuevo)
plugins {
    id 'java-library'
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
```

### 2. Actualizar el uso en app/build.gradle

```groovy
// ANTES 
android.convention.plugins.somePlugin = value

// DESPUÉS
android.somePlugin = value
```

### 3. Migrar plugins personalizados

Si tienes plugins personalizados que usan la API de convención, deberás migrarlos para usar el nuevo enfoque de extensiones de Gradle:

```groovy
// ANTES
class MyPluginConvention {
    def someProperty = 'defaultValue'
}

project.convention.plugins.myPlugin = new MyPluginConvention()

// DESPUÉS
class MyPluginExtension {
    String someProperty = 'defaultValue'
}

project.extensions.create('myPlugin', MyPluginExtension)
```

## Verificación

Para verificar que la solución funciona correctamente:

1. Ejecuta una sincronización de Gradle desde Android Studio
2. Verifica que no aparezcan advertencias o errores relacionados con "convention"
3. Realiza una compilación completa con `./gradlew build` o desde Android Studio

## Recursos Adicionales

- [Documentación de Gradle sobre migración desde convención a extensiones](https://docs.gradle.org/current/userguide/custom_plugins.html)
- [Buenas prácticas para desarrollar plugins de Gradle](https://docs.gradle.org/current/userguide/implementing_gradle_plugins.html)

## Notas de Compatibilidad

Esta solución es compatible con:

- Gradle 7.0+
- Android Gradle Plugin 7.0+

Al migrar a las nuevas APIs, asegúrate de mantener la compatibilidad con las versiones mínimas de Gradle y Android Gradle Plugin que estés utilizando en tu proyecto.
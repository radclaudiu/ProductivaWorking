@echo off
setlocal enabledelayedexpansion

echo Iniciando correccion de problemas comunes de JDK 11...

REM Archivos a verificar
set BUILD_GRADLE=build.gradle
set APP_BUILD_GRADLE=app\build.gradle
set GRADLE_PROPERTIES=gradle.properties
set SETTINGS_GRADLE=settings.gradle
set LOCAL_PROPERTIES=local.properties

echo Realizando copias de seguridad de archivos...

REM Hacer copias de seguridad de archivos importantes
if exist "%BUILD_GRADLE%" (
    copy "%BUILD_GRADLE%" "%BUILD_GRADLE%.bak" >nul
    echo - Copia de seguridad creada: %BUILD_GRADLE%.bak
)

if exist "%APP_BUILD_GRADLE%" (
    copy "%APP_BUILD_GRADLE%" "%APP_BUILD_GRADLE%.bak" >nul
    echo - Copia de seguridad creada: %APP_BUILD_GRADLE%.bak
)

if exist "%GRADLE_PROPERTIES%" (
    copy "%GRADLE_PROPERTIES%" "%GRADLE_PROPERTIES%.bak" >nul
    echo - Copia de seguridad creada: %GRADLE_PROPERTIES%.bak
)

if exist "%SETTINGS_GRADLE%" (
    copy "%SETTINGS_GRADLE%" "%SETTINGS_GRADLE%.bak" >nul
    echo - Copia de seguridad creada: %SETTINGS_GRADLE%.bak
)

echo.
echo Aplicando correcciones para JDK 11...

REM 1. Verificar y corregir build.gradle (raíz)
if exist "%BUILD_GRADLE%" (
    echo Verificando %BUILD_GRADLE%...
    
    REM Verificar si ya existe configuración de Java 11
    findstr /C:"javaVersion = JavaVersion.VERSION_11" "%BUILD_GRADLE%" >nul
    if !errorlevel! neq 0 (
        echo - Agregando configuración de JDK 11 en %BUILD_GRADLE%
        
        REM Buscar la sección de buildscript para añadir configuración de Java 11
        findstr /C:"buildscript {" "%BUILD_GRADLE%" >nul
        if !errorlevel! equ 0 (
            powershell -Command "(Get-Content '%BUILD_GRADLE%') -replace 'buildscript \{', 'buildscript {\n    // Configuración de JDK 11 para todo el proyecto\n    ext {\n        javaVersion = JavaVersion.VERSION_11\n    }' | Set-Content '%BUILD_GRADLE%'"
        )
        
        REM Agregar configuración para subproyectos si no existe
        findstr /C:"subprojects {" "%BUILD_GRADLE%" >nul
        if !errorlevel! neq 0 (
            echo.>> "%BUILD_GRADLE%"
            echo // Aplicar configuración de Java 11 a todos los subproyectos>> "%BUILD_GRADLE%"
            echo subprojects {>> "%BUILD_GRADLE%"
            echo     afterEvaluate {>> "%BUILD_GRADLE%"
            echo         if (project.hasProperty('android')) {>> "%BUILD_GRADLE%"
            echo             android {>> "%BUILD_GRADLE%"
            echo                 compileOptions {>> "%BUILD_GRADLE%"
            echo                     sourceCompatibility = JavaVersion.VERSION_11>> "%BUILD_GRADLE%"
            echo                     targetCompatibility = JavaVersion.VERSION_11>> "%BUILD_GRADLE%"
            echo                 }>> "%BUILD_GRADLE%"
            echo             }>> "%BUILD_GRADLE%"
            echo             >> "%BUILD_GRADLE%"
            echo             // Para proyectos Kotlin>> "%BUILD_GRADLE%"
            echo             if (project.hasProperty('kotlin')) {>> "%BUILD_GRADLE%"
            echo                 kotlin {>> "%BUILD_GRADLE%"
            echo                     jvmToolchain(11)>> "%BUILD_GRADLE%"
            echo                 }>> "%BUILD_GRADLE%"
            echo             }>> "%BUILD_GRADLE%"
            echo         }>> "%BUILD_GRADLE%"
            echo     }>> "%BUILD_GRADLE%"
            echo }>> "%BUILD_GRADLE%"
        )
    ) else (
        echo - Configuración de JDK 11 ya existe en %BUILD_GRADLE%
    )
) else (
    echo No se encontró el archivo %BUILD_GRADLE%
)

REM 2. Verificar y corregir app/build.gradle
if exist "%APP_BUILD_GRADLE%" (
    echo Verificando %APP_BUILD_GRADLE%...
    
    REM Verificar configuración Java 11 en compileOptions
    findstr /C:"sourceCompatibility JavaVersion.VERSION_11" "%APP_BUILD_GRADLE%" >nul
    if !errorlevel! neq 0 (
        echo - Actualizando compileOptions en %APP_BUILD_GRADLE%
        
        REM Buscar bloque compileOptions
        findstr /C:"compileOptions {" "%APP_BUILD_GRADLE%" >nul
        if !errorlevel! equ 0 (
            powershell -Command "(Get-Content '%APP_BUILD_GRADLE%') -replace 'sourceCompatibility JavaVersion\.VERSION_\d+', 'sourceCompatibility JavaVersion.VERSION_11' | Set-Content '%APP_BUILD_GRADLE%'"
            powershell -Command "(Get-Content '%APP_BUILD_GRADLE%') -replace 'targetCompatibility JavaVersion\.VERSION_\d+', 'targetCompatibility JavaVersion.VERSION_11' | Set-Content '%APP_BUILD_GRADLE%'"
        )
    ) else (
        echo - Configuración de compileOptions ya actualizada a JDK 11
    )
    
    REM Verificar configuración Kotlin JVM target 11
    findstr /C:"jvmTarget = '11'" "%APP_BUILD_GRADLE%" >nul
    if !errorlevel! neq 0 (
        echo - Actualizando kotlinOptions en %APP_BUILD_GRADLE%
        
        REM Buscar bloque kotlinOptions
        findstr /C:"kotlinOptions {" "%APP_BUILD_GRADLE%" >nul
        if !errorlevel! equ 0 (
            powershell -Command "(Get-Content '%APP_BUILD_GRADLE%') -replace 'jvmTarget = '\d+''', 'jvmTarget = ''11''' | Set-Content '%APP_BUILD_GRADLE%'"
        )
    ) else (
        echo - Configuración de kotlinOptions ya actualizada a JDK 11
    )
    
    REM Verificar configuración Kotlin toolchain
    findstr /C:"jvmToolchain(11)" "%APP_BUILD_GRADLE%" >nul
    if !errorlevel! neq 0 (
        echo - Actualizando Kotlin toolchain en %APP_BUILD_GRADLE%
        
        REM Buscar bloque kotlin 
        findstr /C:"kotlin {" "%APP_BUILD_GRADLE%" >nul
        if !errorlevel! equ 0 (
            powershell -Command "(Get-Content '%APP_BUILD_GRADLE%') -replace 'jvmToolchain\(\d+\)', 'jvmToolchain(11)' | Set-Content '%APP_BUILD_GRADLE%'"
        ) else (
            echo.>> "%APP_BUILD_GRADLE%"
            echo // Configuración para Kotlin Serialization>> "%APP_BUILD_GRADLE%"
            echo kotlin {>> "%APP_BUILD_GRADLE%"
            echo     // Actualizado a JDK 11>> "%APP_BUILD_GRADLE%"
            echo     jvmToolchain(11)>> "%APP_BUILD_GRADLE%"
            echo }>> "%APP_BUILD_GRADLE%"
        )
    ) else (
        echo - Configuración de Kotlin toolchain ya actualizada a JDK 11
    )
) else (
    echo No se encontró el archivo %APP_BUILD_GRADLE%
)

REM 3. Verificar y actualizar gradle.properties
if exist "%GRADLE_PROPERTIES%" (
    echo Verificando %GRADLE_PROPERTIES%...
    
    REM Verificar configuración de Java Toolchain
    findstr /C:"org.gradle.java.home.version=11" "%GRADLE_PROPERTIES%" >nul
    if !errorlevel! neq 0 (
        echo - Agregando configuración de Java Toolchain en %GRADLE_PROPERTIES%
        
        REM Verificar si ya existe alguna otra versión configurada
        findstr /C:"org.gradle.java.home.version=" "%GRADLE_PROPERTIES%" >nul
        if !errorlevel! equ 0 (
            powershell -Command "(Get-Content '%GRADLE_PROPERTIES%') -replace 'org\.gradle\.java\.home\.version=\d+', 'org.gradle.java.home.version=11' | Set-Content '%GRADLE_PROPERTIES%'"
        ) else (
            echo.>> "%GRADLE_PROPERTIES%"
            echo # Configuración de Java Toolchain - permite usar JDK 11>> "%GRADLE_PROPERTIES%"
            echo org.gradle.java.installations.auto-download=true>> "%GRADLE_PROPERTIES%"
            echo org.gradle.java.installations.auto-detect=true>> "%GRADLE_PROPERTIES%"
            echo # Versión JDK para proyecto>> "%GRADLE_PROPERTIES%"
            echo org.gradle.java.home.version=11>> "%GRADLE_PROPERTIES%"
        )
    ) else (
        echo - Configuración de Java Toolchain ya existe en %GRADLE_PROPERTIES%
    )
) else (
    echo No se encontró el archivo %GRADLE_PROPERTIES%
)

REM 4. Verificar y actualizar local.properties
if exist "%LOCAL_PROPERTIES%" (
    echo Verificando %LOCAL_PROPERTIES%...
    
    REM Verificar si ya existe configuración de JDK específico
    findstr /C:"org.gradle.java.home=" "%LOCAL_PROPERTIES%" >nul
    if !errorlevel! neq 0 (
        echo - Agregando configuración de JDK 11 en %LOCAL_PROPERTIES%
        echo.>> "%LOCAL_PROPERTIES%"
        echo # Configuración JDK 11 para este equipo>> "%LOCAL_PROPERTIES%"
        echo org.gradle.java.home=C:\\Program Files\\Java\\jdk-11>> "%LOCAL_PROPERTIES%"
    ) else (
        echo - Configuración de JDK específico ya existe en %LOCAL_PROPERTIES%
    )
) else (
    echo No se encontró el archivo %LOCAL_PROPERTIES%
)

REM 5. Verificar y actualizar settings.gradle
if exist "%SETTINGS_GRADLE%" (
    echo Verificando %SETTINGS_GRADLE%...
    
    REM Verificar si ya existe configuración de plugins
    findstr /C:"plugins {" "%SETTINGS_GRADLE%" >nul
    if !errorlevel! neq 0 (
        echo - Agregando configuración de plugins en %SETTINGS_GRADLE%
        
        findstr /C:"pluginManagement {" "%SETTINGS_GRADLE%" >nul
        if !errorlevel! equ 0 (
            REM Crear archivo temporal para insertar la configuración
            type "%SETTINGS_GRADLE%" > temp_settings.gradle
            powershell -Command "(Get-Content 'temp_settings.gradle') -replace 'pluginManagement \{', 'pluginManagement {\n    // Configuración específica para plugins - asegura que se use JDK 11\n    plugins {\n        id ''com.android.application'' version ''8.2.0''\n        id ''com.android.library'' version ''8.2.0''\n        id ''org.jetbrains.kotlin.android'' version ''1.9.0''\n        id ''com.google.devtools.ksp'' version ''1.9.0-1.0.13''\n    }' | Set-Content '%SETTINGS_GRADLE%'"
            del temp_settings.gradle
        )
    ) else (
        echo - Configuración de plugins ya existe en %SETTINGS_GRADLE%
    )
) else (
    echo No se encontró el archivo %SETTINGS_GRADLE%
)

echo.
echo ¡Proceso de corrección completado!
echo Para verificar la configuración, ejecute el script verify_jdk11.bat
echo.
echo Por favor, sincronice su proyecto en Android Studio para aplicar los cambios.

pause
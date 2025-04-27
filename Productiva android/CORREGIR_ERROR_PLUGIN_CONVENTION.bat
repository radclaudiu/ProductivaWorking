@echo off
setlocal enabledelayedexpansion

echo Iniciando correccion de errores de convencion de plugins...

REM Archivos a verificar
set BUILD_GRADLE=build.gradle
set APP_BUILD_GRADLE=app\build.gradle
set SETTINGS_GRADLE=settings.gradle

REM 1. Verificar si hay problemas en build.gradle del proyecto
if exist "%BUILD_GRADLE%" (
    echo Verificando %BUILD_GRADLE%...
    
    REM Buscar usos de convention.plugins
    findstr /C:"convention.plugins" "%BUILD_GRADLE%" >nul
    if !errorlevel! equ 0 (
        echo Encontrado uso obsoleto de convention.plugins en %BUILD_GRADLE%
        REM Realizar copia de seguridad
        copy "%BUILD_GRADLE%" "%BUILD_GRADLE%.bak" >nul
        echo Copia de seguridad creada: %BUILD_GRADLE%.bak
        
        REM Crear archivo temporal con los cambios
        type "%BUILD_GRADLE%" | powershell -Command "$input | ForEach-Object { $_ -replace 'convention.plugins.java.sourceCompatibility = JavaVersion', 'java { sourceCompatibility = JavaVersion' }" > temp.gradle
        type temp.gradle | powershell -Command "$input | ForEach-Object { $_ -replace 'convention.plugins.java.targetCompatibility = JavaVersion', 'java { targetCompatibility = JavaVersion' }" > "%BUILD_GRADLE%"
        del temp.gradle
        
        echo Se ha actualizado %BUILD_GRADLE% para usar el nuevo estilo de plugins
    ) else (
        echo No se encontraron problemas en %BUILD_GRADLE%
    )
) else (
    echo No se encontró el archivo %BUILD_GRADLE%
)

REM 2. Verificar app/build.gradle
if exist "%APP_BUILD_GRADLE%" (
    echo Verificando %APP_BUILD_GRADLE%...
    
    REM Buscar usos de convention.plugins en el bloque android
    findstr /C:"android.convention.plugins" "%APP_BUILD_GRADLE%" >nul
    if !errorlevel! equ 0 (
        echo Encontrado uso obsoleto de android.convention.plugins en %APP_BUILD_GRADLE%
        REM Realizar copia de seguridad
        copy "%APP_BUILD_GRADLE%" "%APP_BUILD_GRADLE%.bak" >nul
        echo Copia de seguridad creada: %APP_BUILD_GRADLE%.bak
        
        REM Reemplazar patrones de convención antigua con el nuevo estilo
        type "%APP_BUILD_GRADLE%" | powershell -Command "$input | ForEach-Object { $_ -replace 'android.convention.plugins.somePlugin', 'android.somePlugin' }" > "%APP_BUILD_GRADLE%.tmp"
        move /y "%APP_BUILD_GRADLE%.tmp" "%APP_BUILD_GRADLE%" >nul
        
        echo Se ha actualizado %APP_BUILD_GRADLE% para usar el nuevo estilo de plugins
    ) else (
        echo No se encontraron problemas en %APP_BUILD_GRADLE%
    )
) else (
    echo No se encontró el archivo %APP_BUILD_GRADLE%
)

REM 3. Buscar plugins personalizados que puedan usar la API de convención
echo Buscando plugins personalizados que usen la API de convención...

set PLUGINS_ENCONTRADOS=0
for /r %%f in (*.gradle) do (
    findstr /C:"convention.plugins" "%%f" >nul
    if !errorlevel! equ 0 (
        if !PLUGINS_ENCONTRADOS! equ 0 (
            echo Se encontraron archivos que usan la API de convención obsoleta:
            set PLUGINS_ENCONTRADOS=1
        )
        echo %%f
    )
)

if !PLUGINS_ENCONTRADOS! equ 0 (
    echo No se encontraron plugins personalizados que usen la API de convención obsoleta
) else (
    echo Estos archivos requieren revisión manual. Consulta el documento SOLUCION_ERROR_PLUGIN_CONVENTION.md
)

echo ¡Proceso de corrección completado!
echo Por favor, sincroniza tu proyecto en Android Studio para verificar los cambios.

pause
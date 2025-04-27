@echo off
setlocal enabledelayedexpansion

echo Verificando configuracion de JDK 11 para Productiva Android...

REM Verificar Java instalado
echo Verificando version de Java instalada:
where java >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_VERSION=%%g
    )
    
    set JAVA_VERSION=!JAVA_VERSION:"=!
    echo Java version: !JAVA_VERSION!
    
    REM Verificar si es Java 11
    echo !JAVA_VERSION! | findstr /b /c:"11" >nul
    if !errorlevel! equ 0 (
        echo [32m✓ JDK 11 detectado correctamente[0m
    ) else (
        echo [31m✗ No se detecto JDK 11. Por favor instala JDK 11 para continuar.[0m
    )
) else (
    echo [31m✗ Java no esta instalado o no esta en el PATH[0m
)

REM Verificar configuración en build.gradle
echo.
echo Verificando configuracion en app\build.gradle:
set APP_BUILD_GRADLE=app\build.gradle

if exist "%APP_BUILD_GRADLE%" (
    REM Verificar compileOptions
    findstr /c:"sourceCompatibility JavaVersion.VERSION_11" "%APP_BUILD_GRADLE%" >nul
    if !errorlevel! equ 0 (
        echo [32m✓ sourceCompatibility configurado para Java 11[0m
    ) else (
        echo [31m✗ sourceCompatibility no esta configurado para Java 11[0m
    )
    
    findstr /c:"targetCompatibility JavaVersion.VERSION_11" "%APP_BUILD_GRADLE%" >nul
    if !errorlevel! equ 0 (
        echo [32m✓ targetCompatibility configurado para Java 11[0m
    ) else (
        echo [31m✗ targetCompatibility no esta configurado para Java 11[0m
    )
    
    REM Verificar kotlinOptions
    findstr /c:"jvmTarget = '11'" "%APP_BUILD_GRADLE%" >nul
    if !errorlevel! equ 0 (
        echo [32m✓ kotlinOptions.jvmTarget configurado para Java 11[0m
    ) else (
        echo [31m✗ kotlinOptions.jvmTarget no esta configurado para Java 11[0m
    )
    
    REM Verificar jvmToolchain
    findstr /c:"jvmToolchain(11)" "%APP_BUILD_GRADLE%" >nul
    if !errorlevel! equ 0 (
        echo [32m✓ kotlin.jvmToolchain configurado para Java 11[0m
    ) else (
        echo [31m✗ kotlin.jvmToolchain no esta configurado para Java 11[0m
    )
) else (
    echo [31m✗ No se encontro el archivo %APP_BUILD_GRADLE%[0m
)

REM Verificar configuración en gradle.properties
echo.
echo Verificando configuracion en gradle.properties:
set GRADLE_PROPERTIES=gradle.properties

if exist "%GRADLE_PROPERTIES%" (
    findstr /c:"org.gradle.java.home.version=11" "%GRADLE_PROPERTIES%" >nul
    if !errorlevel! equ 0 (
        echo [32m✓ JDK version configurada a 11 en gradle.properties[0m
    ) else (
        echo [31m✗ JDK version no esta configurada a 11 en gradle.properties[0m
    )
    
    findstr /c:"org.gradle.java.installations.auto-download=true" "%GRADLE_PROPERTIES%" >nul
    if !errorlevel! equ 0 (
        echo [32m✓ Auto-download de JDK habilitado[0m
    ) else (
        echo [33m⚠ Auto-download de JDK no esta habilitado[0m
    )
) else (
    echo [31m✗ No se encontro el archivo %GRADLE_PROPERTIES%[0m
)

echo.
echo Resumen de verificacion:
echo Si todos los items tienen ✓, el proyecto esta correctamente configurado para JDK 11.
echo Si hay items con ✗, revisa el documento MIGRACION_JDK11.md para instrucciones detalladas.
echo.
echo [32mPara mas informacion, consulta el archivo MIGRACION_JDK11.md[0m

pause
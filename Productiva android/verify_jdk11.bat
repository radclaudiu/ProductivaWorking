@echo off
REM verify_jdk11.bat - Script para verificar configuración de JDK 11 en Windows

echo === Verificación de configuración JDK 11 ===
echo.

REM Comprobar versión de Gradle y JDK
echo Comprobando versión de Gradle y JDK...
call gradlew.bat --version
echo.

REM Verificar archivos de configuración
echo Verificando archivos de configuración...
echo.

REM app/build.gradle
findstr /C:"JavaVersion.VERSION_11" app\build.gradle >nul 2>&1
set status_app_java=%errorlevel%
findstr /C:"jvmTarget = '11'" app\build.gradle >nul 2>&1
set status_app_kotlin=%errorlevel%

if %status_app_java% EQU 0 if %status_app_kotlin% EQU 0 (
    echo [✓] app/build.gradle: Correctamente configurado para JDK 11
) else (
    echo [✗] app/build.gradle: Falta configuración para JDK 11
)

REM build.gradle (raíz)
findstr /C:"javaVersion = JavaVersion.VERSION_11" build.gradle >nul 2>&1
if %errorlevel% EQU 0 (
    echo [✓] build.gradle (raíz): Correctamente configurado para JDK 11
) else (
    echo [✗] build.gradle (raíz): Falta configuración para JDK 11
)

REM gradle.properties
findstr /C:"org.gradle.java.home.version=11" gradle.properties >nul 2>&1
if %errorlevel% EQU 0 (
    echo [✓] gradle.properties: Correctamente configurado para JDK 11
) else (
    echo [✗] gradle.properties: Falta configuración para JDK 11
)

REM settings.gradle
findstr /C:"pluginManagement" settings.gradle >nul 2>&1
set status_plugin_mgmt=%errorlevel%
findstr /C:"plugins" settings.gradle >nul 2>&1
set status_plugins=%errorlevel%

if %status_plugin_mgmt% EQU 0 if %status_plugins% EQU 0 (
    echo [✓] settings.gradle: Tiene configuración de plugins
) else (
    echo [✗] settings.gradle: Falta configuración de plugins
)

REM Verificar toolchain de Kotlin
findstr /C:"jvmToolchain(11)" app\build.gradle >nul 2>&1
if %errorlevel% EQU 0 (
    echo [✓] Kotlin toolchain: Correctamente configurado para JDK 11
) else (
    echo [✗] Kotlin toolchain: Falta configuración para JDK 11
)

REM Verificar JDK local configurado
findstr /C:"org.gradle.java.home=" local.properties >nul 2>&1
if %errorlevel% EQU 0 (
    echo [✓] local.properties: Tiene configurado JDK específico
) else (
    echo [✗] local.properties: No tiene configurado JDK específico
)

REM Verificar presencia de JDK en el sistema
where javac >nul 2>&1
if %errorlevel% EQU 0 (
    for /f "tokens=2" %%i in ('javac -version 2^>^&1') do set JAVA_VERSION=%%i
    echo [✓] JDK instalado: Versión %JAVA_VERSION% detectada
) else (
    echo [✗] JDK no encontrado: Asegúrate de que javac está en tu PATH
)

echo.
echo Verificación completada.
pause
@echo off
echo LIMPIAR PROYECTO COMPLETO
echo =======================
echo Este script realizará una limpieza profunda del proyecto y eliminará
echo todos los archivos temporales y de caché que pueden causar problemas.
echo.
echo ADVERTENCIA: Asegúrate de tener una copia de seguridad de cualquier cambio no guardado.
echo.
set /p CONFIRMAR="¿Estás seguro de que quieres continuar? (S/N): "

if /i "%CONFIRMAR%" neq "S" (
    echo Operación cancelada.
    goto :EOF
)

echo.
echo Limpiando archivos temporales y caché...

:: Limpiar caché de Gradle
echo - Eliminando caché de Gradle...
rd /s /q .gradle
rd /s /q %USERPROFILE%\.gradle\caches\build-cache-*

:: Limpiar directorios de compilación
echo - Eliminando directorios de compilación...
rd /s /q build
rd /s /q app\build
rd /s /q app\.cxx
rd /s /q buildSrc\build

:: Limpiar archivos JNI 
echo - Eliminando archivos JNI...
rd /s /q app\src\main\jniLibs

:: Limpiar archivos de Android Studio
echo - Eliminando archivos de Android Studio...
rd /s /q .idea\libraries
rd /s /q .idea\modules
del /q .idea\workspace.xml
del /q .idea\modules.xml
del /q .idea\*.iml
del /q *.iml
del /q app\*.iml

:: Eliminar archivos de Kotlin
echo - Eliminando archivos de Kotlin...
rd /s /q app\src\main\kotlin\META-INF
rd /s /q .kotlin-classes

:: Eliminar archivos generados
echo - Eliminando archivos generados...
rd /s /q app\src\main\java\com\productiva\android\generated
rd /s /q app\src\main\java\com\productiva\android\di\hilt
rd /s /q app\build\generated

:: Limpiar y reconstruir con Gradle (si existe gradlew)
if exist gradlew (
    echo - Ejecutando limpieza de Gradle...
    call gradlew clean --refresh-dependencies --no-daemon
)

echo.
echo =========================
echo LIMPIEZA COMPLETA EXITOSA
echo =========================
echo.
echo Para completar el proceso:
echo 1. Cierra Android Studio completamente
echo 2. Vuelve a abrir el proyecto
echo 3. Sincroniza con archivos Gradle (File -^> Sync Project with Gradle Files)
echo 4. Reconstruye el proyecto (Build -^> Rebuild Project)
echo.
pause
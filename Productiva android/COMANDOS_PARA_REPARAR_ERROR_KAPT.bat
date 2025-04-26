@echo off
echo Reparando el problema de KAPT y Java...

:: Limpieza del proyecto
echo Limpiando el proyecto...
call gradlew clean

:: Invalidar y reiniciar caches
echo Reiniciando caches...
rd /s /q .gradle
rd /s /q build
rd /s /q app\build

:: Actualizar las configuraciones de Gradle
echo Sincronizando con Gradle...
call gradlew --refresh-dependencies

echo Proceso completado. Intenta compilar de nuevo el proyecto.
pause
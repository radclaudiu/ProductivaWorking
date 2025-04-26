@echo off
echo Reparando el problema de KSP...

:: Limpiar caché y archivos generados
echo Limpiando caché y archivos generados...
rd /s /q .gradle
rd /s /q build
rd /s /q app\build
rd /s /q app\.cxx
rd /s /q app\src\main\jniLibs
rd /s /q .idea\libraries
rd /s /q .idea\modules
rd /s /q .idea\workspace.xml

:: Revertir a KAPT por ahora
echo Revirtiendo temporalmente a KAPT...
copy /y app\build.gradle.kapt app\build.gradle

:: Limpiar y reconstruir
echo Sincronizando con Gradle y reconstruyendo...
call gradlew clean --refresh-dependencies --no-daemon

echo Reparación completada. Por favor, sincroniza el proyecto con Gradle y recompila.
echo.
echo Si continúas experimentando problemas, considera estas opciones:
echo 1. Asegúrate de tener JDK 8 instalado y seleccionado en Android Studio
echo 2. Ajusta gradle.properties para usar JDK 8 específicamente
echo 3. Configura Android Studio para usar JDK 8:
echo    File -^> Settings -^> Build, Execution, Deployment -^> Build Tools -^> Gradle -^> Gradle JDK
echo.
pause
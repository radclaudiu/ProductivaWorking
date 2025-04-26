#!/bin/bash
echo "Reparando el problema de KSP..."

# Limpiar caché y archivos generados
echo "Limpiando caché y archivos generados..."
rm -rf .gradle
rm -rf build
rm -rf app/build
rm -rf app/.cxx
rm -rf app/src/main/jniLibs
rm -rf .idea/libraries
rm -rf .idea/modules
rm -rf .idea/workspace.xml

# Revertir a KAPT por ahora
echo "Revirtiendo temporalmente a KAPT..."
cp app/build.gradle.kapt app/build.gradle

# Limpiar y reconstruir
echo "Sincronizando con Gradle y reconstruyendo..."
./gradlew clean --refresh-dependencies --no-daemon

echo "Reparación completada. Por favor, sincroniza el proyecto con Gradle y recompila."
echo ""
echo "Si continúas experimentando problemas, considera estas opciones:"
echo "1. Asegúrate de tener JDK 8 instalado y seleccionado en Android Studio"
echo "2. Ajusta gradle.properties para usar JDK 8 específicamente"
echo "3. Configura Android Studio para usar JDK 8:"
echo "   File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JDK"
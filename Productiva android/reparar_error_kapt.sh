#!/bin/bash
echo "Reparando el problema de KAPT y Java..."

# Limpieza del proyecto
echo "Limpiando el proyecto..."
./gradlew clean

# Invalidar y reiniciar caches
echo "Reiniciando caches..."
rm -rf .gradle
rm -rf build
rm -rf app/build

# Actualizar las configuraciones de Gradle
echo "Sincronizando con Gradle..."
./gradlew --refresh-dependencies

echo "Proceso completado. Intenta compilar de nuevo el proyecto."
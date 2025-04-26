#!/bin/bash

echo "LIMPIAR PROYECTO COMPLETO"
echo "======================="
echo "Este script realizará una limpieza profunda del proyecto y eliminará"
echo "todos los archivos temporales y de caché que pueden causar problemas."
echo ""
echo "ADVERTENCIA: Asegúrate de tener una copia de seguridad de cualquier cambio no guardado."
echo ""
read -p "¿Estás seguro de que quieres continuar? (s/N): " CONFIRMAR

if [[ ! "$CONFIRMAR" =~ ^[Ss]$ ]]; then
    echo "Operación cancelada."
    exit 0
fi

echo ""
echo "Limpiando archivos temporales y caché..."

# Limpiar caché de Gradle
echo "- Eliminando caché de Gradle..."
rm -rf .gradle
rm -rf ~/.gradle/caches/build-cache-*

# Limpiar directorios de compilación
echo "- Eliminando directorios de compilación..."
rm -rf build
rm -rf app/build
rm -rf app/.cxx
rm -rf buildSrc/build

# Limpiar archivos JNI 
echo "- Eliminando archivos JNI..."
rm -rf app/src/main/jniLibs

# Limpiar archivos de Android Studio
echo "- Eliminando archivos de Android Studio..."
rm -rf .idea/libraries
rm -rf .idea/modules
rm -f .idea/workspace.xml
rm -f .idea/modules.xml
rm -f .idea/*.iml
rm -f *.iml
rm -f app/*.iml

# Eliminar archivos de Kotlin
echo "- Eliminando archivos de Kotlin..."
rm -rf app/src/main/kotlin/META-INF
rm -rf .kotlin-classes

# Eliminar archivos generados
echo "- Eliminando archivos generados..."
rm -rf app/src/main/java/com/productiva/android/generated
rm -rf app/src/main/java/com/productiva/android/di/hilt
rm -rf app/build/generated

# Limpiar y reconstruir con Gradle (si existe gradlew)
if [ -f "gradlew" ]; then
    echo "- Ejecutando limpieza de Gradle..."
    chmod +x gradlew
    ./gradlew clean --refresh-dependencies --no-daemon
fi

echo ""
echo "========================="
echo "LIMPIEZA COMPLETA EXITOSA"
echo "========================="
echo ""
echo "Para completar el proceso:"
echo "1. Cierra Android Studio completamente"
echo "2. Vuelve a abrir el proyecto"
echo "3. Sincroniza con archivos Gradle (File → Sync Project with Gradle Files)"
echo "4. Reconstruye el proyecto (Build → Rebuild Project)"
echo ""
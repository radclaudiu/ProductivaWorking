#!/bin/bash

echo "SOLUCION DEFINITIVA PARA ERROR DE KAPT"
echo "===================================="
echo ""
echo "Este script realizara una solucion completa para el error de KAPT:"
echo "\"superclass access check failed: class org.jetbrains.kotlin.kapt3.base...\""
echo ""
echo "IMPORTANTE: Esta solucion requiere JDK 8 instalado en tu sistema."
echo "Si no tienes JDK 8, cancelar y descargar de: https://adoptium.net/temurin/releases/?version=8"
echo ""
read -p "Continuar con la solucion? (s/N): " CONFIRMAR

if [[ ! "$CONFIRMAR" =~ ^[Ss]$ ]]; then
    echo "Operacion cancelada."
    exit 0
fi

echo ""
echo "1. Realizando copia de seguridad de archivos importantes..."
mkdir -p backups
cp gradle.properties backups/gradle.properties.bak
cp app/build.gradle backups/build.gradle.bak
cp settings.gradle backups/settings.gradle.bak
if [ -f build.gradle ]; then
    cp build.gradle backups/root-build.gradle.bak
fi

echo ""
echo "2. Configurando JDK 8 para el proyecto..."

cat > local.properties.jdk << EOF
# Configuracion forzada para usar JDK 8
org.gradle.java.home=/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home
# Ajusta la ruta anterior a la ubicacion de tu JDK 8
# Por ejemplo, podria ser:
# /usr/lib/jvm/java-8-openjdk-amd64
# o
# ~/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home
EOF

echo ""
echo "3. Limpiando caches y archivos temporales..."
rm -rf .gradle
rm -rf build
rm -rf app/build
rm -rf .idea/libraries
rm -rf .idea/modules
rm -rf app/.cxx
echo "La limpieza mejora considerablemente las posibilidades de exito."

echo ""
echo "4. Ajustando configuracion gradle.properties..."
cat > gradle.properties.new << EOF
# Project-wide Gradle settings
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
# Enable AndroidX
android.useAndroidX=true
# Automatically convert third-party libraries to use AndroidX
android.enableJetifier=true
# Kotlin code style
kotlin.code.style=official
# Parallel execution
org.gradle.parallel=true
# Incremental compilation
org.gradle.caching=true
# Configuration on demand
org.gradle.configureondemand=true
# Enable modern R8 desugaring
android.enableR8.fullMode=true
# KAPT configurations
kapt.incremental.apt=true
kapt.use.worker.api=true
kapt.include.compile.classpath=false
# Non-transitive R classes
android.nonTransitiveRClass=true
# Forzar autodeteccion de JDK instalados
org.gradle.java.installations.auto-detect=true
# Importante: No descargar JDKs automaticamente
org.gradle.java.installations.auto-download=false
EOF

mv gradle.properties.new gradle.properties

echo ""
echo "5. Ajustando configuracion app/build.gradle para KAPT..."
cp app/build.gradle app/build.gradle.ksp
grep -v "id 'com.google.devtools.ksp'" app/build.gradle | grep -v "ksp " > app/build.gradle.tmp
mv app/build.gradle.tmp app/build.gradle

echo ""
echo "6. Insertando configuracion KAPT en app/build.gradle..."
sed -i -e 's/id '\''org.jetbrains.kotlin.android'\''/id '\''org.jetbrains.kotlin.android'\''\nid '\''kotlin-kapt'\''/g' app/build.gradle

echo ""
echo "7. Asegurando dependencias KAPT en lugar de KSP..."
sed -i -e 's/ksp '\''androidx.room:room-compiler:2.6.1'\''/kapt '\''androidx.room:room-compiler:2.6.1'\''/g' app/build.gradle

echo ""
echo "8. Aplicando configuraciones adicionales a kotlinOptions..."
sed -i -e 's/jvmTarget = '\''1.8'\''/jvmTarget = '\''1.8'\''\n        \/\/ Opciones para mejorar compatibilidad\n        freeCompilerArgs += [\n            "-Xjvm-default=all",\n            "-Xsam-conversions=class"\n        ]/g' app/build.gradle

echo ""
echo "9. Agregando JDK 8 como toolchain explicita..."
sed -i -e 's/\/\/ Usando compatibilidad con Java 8/\/\/ Configurando JDK 8 explicitamente\n    jvmToolchain(8)/g' app/build.gradle

echo ""
echo "======================================"
echo "INSTRUCCIONES FINALES MUY IMPORTANTES"
echo "======================================"
echo ""
echo "1. CERRAR completamente Android Studio"
echo "2. VOLVER A ABRIR el proyecto"
echo "3. En Android Studio, ir a:"
echo "   File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle"
echo "4. En \"Gradle JDK\" seleccionar explicitamente JDK 8"
echo "5. Aceptar cambios y sincronizar el proyecto"
echo ""
echo "IMPORTANTE: Esto deberia resolver definitivamente el problema."
echo ""
echo "Si el problema persiste, editar el archivo local.properties.jdk"
echo "y agregar la ruta correcta a tu JDK 8, luego renombrarlo a local.properties"
echo ""
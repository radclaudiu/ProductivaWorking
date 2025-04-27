#!/bin/bash
# verify_jdk11.sh - Script para verificar configuración de JDK 11

echo "=== Verificación de configuración JDK 11 ==="
echo

# Comprobar versión de Gradle y JDK
echo "Comprobando versión de Gradle y JDK..."
./gradlew --version
echo

# Verificar archivos de configuración
echo "Verificando archivos de configuración..."

# app/build.gradle
if grep -q "JavaVersion.VERSION_11" app/build.gradle && grep -q "jvmTarget = '11'" app/build.gradle; then
    echo "✅ app/build.gradle: Correctamente configurado para JDK 11"
else
    echo "❌ app/build.gradle: Falta configuración para JDK 11"
fi

# build.gradle (raíz)
if grep -q "javaVersion = JavaVersion.VERSION_11" build.gradle; then
    echo "✅ build.gradle (raíz): Correctamente configurado para JDK 11"
else
    echo "❌ build.gradle (raíz): Falta configuración para JDK 11"
fi

# gradle.properties
if grep -q "org.gradle.java.home.version=11" gradle.properties; then
    echo "✅ gradle.properties: Correctamente configurado para JDK 11"
else
    echo "❌ gradle.properties: Falta configuración para JDK 11"
fi

# settings.gradle
if grep -q "pluginManagement" settings.gradle && grep -q "plugins" settings.gradle; then
    echo "✅ settings.gradle: Tiene configuración de plugins"
else
    echo "❌ settings.gradle: Falta configuración de plugins"
fi

# Verificar toolchain de Kotlin
if grep -q "jvmToolchain(11)" app/build.gradle; then
    echo "✅ Kotlin toolchain: Correctamente configurado para JDK 11"
else
    echo "❌ Kotlin toolchain: Falta configuración para JDK 11"
fi

# Verificar presencia de JDK 11 en sistema
if command -v javac >/dev/null 2>&1; then
    java_version=$(javac -version 2>&1 | awk '{print $2}' | cut -d'.' -f1)
    if [ "$java_version" -ge 11 ]; then
        echo "✅ JDK instalado: Versión $java_version detectada (compatible con JDK 11)"
    else
        echo "❌ JDK instalado: Versión $java_version detectada (se requiere 11 o superior)"
    fi
else
    echo "❌ JDK no encontrado: Asegúrate de que javac está en tu PATH"
fi

echo
echo "Verificación completada."
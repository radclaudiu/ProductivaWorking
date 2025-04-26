#!/bin/bash
echo "Reparando el problema de KAPT acceso denegado a superclass..."

# Crear copia de seguridad
echo "Creando copia de seguridad de archivos importantes..."
cp gradle.properties gradle.properties.bak
cp app/build.gradle app/build.gradle.bak

# Limpiar caché y archivos generados
echo "Limpiando cache y archivos generados..."
rm -rf .gradle
rm -rf build
rm -rf app/build
rm -rf app/.cxx
rm -rf app/src/main/jniLibs
rm -rf .idea/libraries
rm -rf .idea/modules
rm -rf .idea/caches
rm -rf .idea/workspace.xml
rm -rf "$HOME/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-compiler-embeddable"

# Verificar JDK y configurar
echo "Verificando versión de Java..."
java -version

# Mensaje de ayuda
echo ""
echo "SOLUCIÓN PARA ERROR DE KAPT:"
echo "========================="
echo ""
echo "La razón de este error es que KAPT necesita acceso a clases de Sun internas" 
echo "cuando se ejecuta en Java 11+. Hemos actualizado la configuración para"
echo "permitir este acceso específicamente."
echo ""
echo "RECOMENDACIONES ADICIONALES:"
echo ""
echo "1. Si continúas teniendo problemas, utiliza JDK 8 en lugar de JDK 11 o superior"
echo "   - Descarga JDK 8 desde: https://adoptium.net/es/temurin/releases/?version=8"
echo "   - Configura Android Studio para usar esta versión de Java:"
echo "     File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JDK"
echo ""
echo "2. También puedes configurar la variable JAVA_HOME para apuntar a JDK 8"
echo ""
echo "3. Si sigues teniendo problemas, ejecuta los siguientes comandos:"
echo "   ./gradlew clean --refresh-dependencies"
echo "   ./gradlew --stop"
echo "   ./gradlew build -Dorg.gradle.java.home=/path/to/jdk8"
echo ""
echo "Reparación completada. Por favor, sincroniza el proyecto con Gradle y recompila."
echo ""
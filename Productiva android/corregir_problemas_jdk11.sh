#!/bin/bash

# Script para corregir problemas comunes al migrar a JDK 11
# Este script aplica configuraciones necesarias para usar JDK 11 en el proyecto

# Colores para mensajes
ROJO='\033[0;31m'
VERDE='\033[0;32m'
AMARILLO='\033[0;33m'
NC='\033[0m' # Sin Color

echo -e "${AMARILLO}Iniciando corrección de problemas comunes de JDK 11...${NC}"

# Archivos a verificar
BUILD_GRADLE="build.gradle"
APP_BUILD_GRADLE="app/build.gradle"
GRADLE_PROPERTIES="gradle.properties"
SETTINGS_GRADLE="settings.gradle"
LOCAL_PROPERTIES="local.properties"

echo -e "${AMARILLO}Realizando copias de seguridad de archivos...${NC}"

# Hacer copias de seguridad de archivos importantes
if [ -f "$BUILD_GRADLE" ]; then
    cp "$BUILD_GRADLE" "${BUILD_GRADLE}.bak"
    echo -e "${VERDE}- Copia de seguridad creada: ${BUILD_GRADLE}.bak${NC}"
fi

if [ -f "$APP_BUILD_GRADLE" ]; then
    cp "$APP_BUILD_GRADLE" "${APP_BUILD_GRADLE}.bak"
    echo -e "${VERDE}- Copia de seguridad creada: ${APP_BUILD_GRADLE}.bak${NC}"
fi

if [ -f "$GRADLE_PROPERTIES" ]; then
    cp "$GRADLE_PROPERTIES" "${GRADLE_PROPERTIES}.bak"
    echo -e "${VERDE}- Copia de seguridad creada: ${GRADLE_PROPERTIES}.bak${NC}"
fi

if [ -f "$SETTINGS_GRADLE" ]; then
    cp "$SETTINGS_GRADLE" "${SETTINGS_GRADLE}.bak"
    echo -e "${VERDE}- Copia de seguridad creada: ${SETTINGS_GRADLE}.bak${NC}"
fi

echo ""
echo -e "${AMARILLO}Aplicando correcciones para JDK 11...${NC}"

# 1. Verificar y corregir build.gradle (raíz)
if [ -f "$BUILD_GRADLE" ]; then
    echo -e "${AMARILLO}Verificando $BUILD_GRADLE...${NC}"
    
    # Verificar si ya existe configuración de Java 11
    if ! grep -q "javaVersion = JavaVersion.VERSION_11" "$BUILD_GRADLE"; then
        echo -e "${VERDE}- Agregando configuración de JDK 11 en $BUILD_GRADLE${NC}"
        
        # Buscar la sección de buildscript para añadir configuración de Java 11
        if grep -q "buildscript {" "$BUILD_GRADLE"; then
            sed -i 's/buildscript {/buildscript {\n    \/\/ Configuración de JDK 11 para todo el proyecto\n    ext {\n        javaVersion = JavaVersion.VERSION_11\n    }/g' "$BUILD_GRADLE"
        fi
        
        # Agregar configuración para subproyectos si no existe
        if ! grep -q "subprojects {" "$BUILD_GRADLE"; then
            echo "" >> "$BUILD_GRADLE"
            echo "// Aplicar configuración de Java 11 a todos los subproyectos" >> "$BUILD_GRADLE"
            echo "subprojects {" >> "$BUILD_GRADLE"
            echo "    afterEvaluate {" >> "$BUILD_GRADLE"
            echo "        if (project.hasProperty('android')) {" >> "$BUILD_GRADLE"
            echo "            android {" >> "$BUILD_GRADLE"
            echo "                compileOptions {" >> "$BUILD_GRADLE"
            echo "                    sourceCompatibility = JavaVersion.VERSION_11" >> "$BUILD_GRADLE"
            echo "                    targetCompatibility = JavaVersion.VERSION_11" >> "$BUILD_GRADLE"
            echo "                }" >> "$BUILD_GRADLE"
            echo "            }" >> "$BUILD_GRADLE"
            echo "            " >> "$BUILD_GRADLE"
            echo "            // Para proyectos Kotlin" >> "$BUILD_GRADLE"
            echo "            if (project.hasProperty('kotlin')) {" >> "$BUILD_GRADLE"
            echo "                kotlin {" >> "$BUILD_GRADLE"
            echo "                    jvmToolchain(11)" >> "$BUILD_GRADLE"
            echo "                }" >> "$BUILD_GRADLE"
            echo "            }" >> "$BUILD_GRADLE"
            echo "        }" >> "$BUILD_GRADLE"
            echo "    }" >> "$BUILD_GRADLE"
            echo "}" >> "$BUILD_GRADLE"
        fi
    else
        echo -e "${VERDE}- Configuración de JDK 11 ya existe en $BUILD_GRADLE${NC}"
    fi
else
    echo -e "${ROJO}No se encontró el archivo $BUILD_GRADLE${NC}"
fi

# 2. Verificar y corregir app/build.gradle
if [ -f "$APP_BUILD_GRADLE" ]; then
    echo -e "${AMARILLO}Verificando $APP_BUILD_GRADLE...${NC}"
    
    # Verificar configuración Java 11 en compileOptions
    if ! grep -q "sourceCompatibility JavaVersion.VERSION_11" "$APP_BUILD_GRADLE"; then
        echo -e "${VERDE}- Actualizando compileOptions en $APP_BUILD_GRADLE${NC}"
        
        # Buscar bloque compileOptions y actualizar versión de Java
        if grep -q "compileOptions {" "$APP_BUILD_GRADLE"; then
            sed -i 's/sourceCompatibility JavaVersion\.VERSION_[0-9]\+/sourceCompatibility JavaVersion.VERSION_11/g' "$APP_BUILD_GRADLE"
            sed -i 's/targetCompatibility JavaVersion\.VERSION_[0-9]\+/targetCompatibility JavaVersion.VERSION_11/g' "$APP_BUILD_GRADLE"
        fi
    else
        echo -e "${VERDE}- Configuración de compileOptions ya actualizada a JDK 11${NC}"
    fi
    
    # Verificar configuración Kotlin JVM target 11
    if ! grep -q "jvmTarget = '11'" "$APP_BUILD_GRADLE"; then
        echo -e "${VERDE}- Actualizando kotlinOptions en $APP_BUILD_GRADLE${NC}"
        
        # Buscar bloque kotlinOptions y actualizar JVM target
        if grep -q "kotlinOptions {" "$APP_BUILD_GRADLE"; then
            sed -i "s/jvmTarget = '[0-9]\+'/jvmTarget = '11'/g" "$APP_BUILD_GRADLE"
        fi
    else
        echo -e "${VERDE}- Configuración de kotlinOptions ya actualizada a JDK 11${NC}"
    fi
    
    # Verificar configuración Kotlin toolchain
    if ! grep -q "jvmToolchain(11)" "$APP_BUILD_GRADLE"; then
        echo -e "${VERDE}- Actualizando Kotlin toolchain en $APP_BUILD_GRADLE${NC}"
        
        # Buscar bloque kotlin y actualizar JVM toolchain
        if grep -q "kotlin {" "$APP_BUILD_GRADLE"; then
            sed -i 's/jvmToolchain([0-9]\+)/jvmToolchain(11)/g' "$APP_BUILD_GRADLE"
        else
            echo "" >> "$APP_BUILD_GRADLE"
            echo "// Configuración para Kotlin Serialization" >> "$APP_BUILD_GRADLE"
            echo "kotlin {" >> "$APP_BUILD_GRADLE"
            echo "    // Actualizado a JDK 11" >> "$APP_BUILD_GRADLE"
            echo "    jvmToolchain(11)" >> "$APP_BUILD_GRADLE"
            echo "}" >> "$APP_BUILD_GRADLE"
        fi
    else
        echo -e "${VERDE}- Configuración de Kotlin toolchain ya actualizada a JDK 11${NC}"
    fi
else
    echo -e "${ROJO}No se encontró el archivo $APP_BUILD_GRADLE${NC}"
fi

# 3. Verificar y actualizar gradle.properties
if [ -f "$GRADLE_PROPERTIES" ]; then
    echo -e "${AMARILLO}Verificando $GRADLE_PROPERTIES...${NC}"
    
    # Verificar configuración de Java Toolchain
    if ! grep -q "org.gradle.java.home.version=11" "$GRADLE_PROPERTIES"; then
        echo -e "${VERDE}- Agregando configuración de Java Toolchain en $GRADLE_PROPERTIES${NC}"
        
        # Verificar si ya existe alguna otra versión configurada
        if grep -q "org.gradle.java.home.version=" "$GRADLE_PROPERTIES"; then
            sed -i 's/org\.gradle\.java\.home\.version=[0-9]\+/org.gradle.java.home.version=11/g' "$GRADLE_PROPERTIES"
        else
            echo "" >> "$GRADLE_PROPERTIES"
            echo "# Configuración de Java Toolchain - permite usar JDK 11" >> "$GRADLE_PROPERTIES"
            echo "org.gradle.java.installations.auto-download=true" >> "$GRADLE_PROPERTIES"
            echo "org.gradle.java.installations.auto-detect=true" >> "$GRADLE_PROPERTIES"
            echo "# Versión JDK para proyecto" >> "$GRADLE_PROPERTIES"
            echo "org.gradle.java.home.version=11" >> "$GRADLE_PROPERTIES"
        fi
    else
        echo -e "${VERDE}- Configuración de Java Toolchain ya existe en $GRADLE_PROPERTIES${NC}"
    fi
else
    echo -e "${ROJO}No se encontró el archivo $GRADLE_PROPERTIES${NC}"
fi

# 4. Verificar y actualizar local.properties
if [ -f "$LOCAL_PROPERTIES" ]; then
    echo -e "${AMARILLO}Verificando $LOCAL_PROPERTIES...${NC}"
    
    # Verificar si ya existe configuración de JDK específico
    if ! grep -q "org.gradle.java.home=" "$LOCAL_PROPERTIES"; then
        echo -e "${VERDE}- Agregando configuración de JDK 11 en $LOCAL_PROPERTIES${NC}"
        echo "" >> "$LOCAL_PROPERTIES"
        echo "# Configuración JDK 11 para este equipo" >> "$LOCAL_PROPERTIES"
        # Usar ruta estándar para JDK en Linux/Mac
        echo "org.gradle.java.home=/usr/lib/jvm/java-11-openjdk" >> "$LOCAL_PROPERTIES"
    else
        echo -e "${VERDE}- Configuración de JDK específico ya existe en $LOCAL_PROPERTIES${NC}"
    fi
else
    echo -e "${ROJO}No se encontró el archivo $LOCAL_PROPERTIES${NC}"
fi

# 5. Verificar y actualizar settings.gradle
if [ -f "$SETTINGS_GRADLE" ]; then
    echo -e "${AMARILLO}Verificando $SETTINGS_GRADLE...${NC}"
    
    # Verificar si ya existe configuración de plugins
    if ! grep -q "plugins {" "$SETTINGS_GRADLE"; then
        echo -e "${VERDE}- Agregando configuración de plugins en $SETTINGS_GRADLE${NC}"
        
        if grep -q "pluginManagement {" "$SETTINGS_GRADLE"; then
            # Crear un archivo temporal con la configuración modificada
            sed 's/pluginManagement {/pluginManagement {\n    \/\/ Configuración específica para plugins - asegura que se use JDK 11\n    plugins {\n        id \"com.android.application\" version \"8.2.0\"\n        id \"com.android.library\" version \"8.2.0\"\n        id \"org.jetbrains.kotlin.android\" version \"1.9.0\"\n        id \"com.google.devtools.ksp\" version \"1.9.0-1.0.13\"\n    }/g' "$SETTINGS_GRADLE" > temp_settings.gradle
            mv temp_settings.gradle "$SETTINGS_GRADLE"
        fi
    else
        echo -e "${VERDE}- Configuración de plugins ya existe en $SETTINGS_GRADLE${NC}"
    fi
else
    echo -e "${ROJO}No se encontró el archivo $SETTINGS_GRADLE${NC}"
fi

echo ""
echo -e "${VERDE}¡Proceso de corrección completado!${NC}"
echo -e "${AMARILLO}Para verificar la configuración, ejecute el script ./verify_jdk11.sh${NC}"
echo ""
echo -e "${AMARILLO}Por favor, sincronice su proyecto en Android Studio para aplicar los cambios.${NC}"

# Dar permisos de ejecución al script de verificación si existe
if [ -f "verify_jdk11.sh" ]; then
    chmod +x verify_jdk11.sh
    echo -e "${VERDE}Se han otorgado permisos de ejecución al script verify_jdk11.sh${NC}"
fi
#!/bin/bash

# Script para verificar la configuración de JDK 11 en el proyecto Android

# Colores para mensajes
ROJO='\033[0;31m'
VERDE='\033[0;32m'
AMARILLO='\033[0;33m'
NC='\033[0m' # Sin Color

echo -e "${AMARILLO}Verificando configuración de JDK 11 para Productiva Android...${NC}"

# Verificar Java instalado
echo -e "${AMARILLO}Verificando versión de Java instalada:${NC}"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo -e "Java versión: ${VERDE}$JAVA_VERSION${NC}"
    
    # Verificar si es Java 11
    if [[ $JAVA_VERSION == 11* ]]; then
        echo -e "${VERDE}✓ JDK 11 detectado correctamente${NC}"
    else
        echo -e "${ROJO}✗ No se detectó JDK 11. Por favor instala JDK 11 para continuar.${NC}"
    fi
else
    echo -e "${ROJO}✗ Java no está instalado o no está en el PATH${NC}"
fi

# Verificar configuración en build.gradle
echo -e "\n${AMARILLO}Verificando configuración en app/build.gradle:${NC}"
APP_BUILD_GRADLE="app/build.gradle"

if [ -f "$APP_BUILD_GRADLE" ]; then
    # Verificar compileOptions
    if grep -q "sourceCompatibility JavaVersion.VERSION_11" "$APP_BUILD_GRADLE"; then
        echo -e "${VERDE}✓ sourceCompatibility configurado para Java 11${NC}"
    else
        echo -e "${ROJO}✗ sourceCompatibility no está configurado para Java 11${NC}"
    fi
    
    if grep -q "targetCompatibility JavaVersion.VERSION_11" "$APP_BUILD_GRADLE"; then
        echo -e "${VERDE}✓ targetCompatibility configurado para Java 11${NC}"
    else
        echo -e "${ROJO}✗ targetCompatibility no está configurado para Java 11${NC}"
    fi
    
    # Verificar kotlinOptions
    if grep -q "jvmTarget = '11'" "$APP_BUILD_GRADLE"; then
        echo -e "${VERDE}✓ kotlinOptions.jvmTarget configurado para Java 11${NC}"
    else
        echo -e "${ROJO}✗ kotlinOptions.jvmTarget no está configurado para Java 11${NC}"
    fi
    
    # Verificar jvmToolchain
    if grep -q "jvmToolchain(11)" "$APP_BUILD_GRADLE"; then
        echo -e "${VERDE}✓ kotlin.jvmToolchain configurado para Java 11${NC}"
    else
        echo -e "${ROJO}✗ kotlin.jvmToolchain no está configurado para Java 11${NC}"
    fi
else
    echo -e "${ROJO}✗ No se encontró el archivo $APP_BUILD_GRADLE${NC}"
fi

# Verificar configuración en gradle.properties
echo -e "\n${AMARILLO}Verificando configuración en gradle.properties:${NC}"
GRADLE_PROPERTIES="gradle.properties"

if [ -f "$GRADLE_PROPERTIES" ]; then
    if grep -q "org.gradle.java.home.version=11" "$GRADLE_PROPERTIES"; then
        echo -e "${VERDE}✓ JDK versión configurada a 11 en gradle.properties${NC}"
    else
        echo -e "${ROJO}✗ JDK versión no está configurada a 11 en gradle.properties${NC}"
    fi
    
    if grep -q "org.gradle.java.installations.auto-download=true" "$GRADLE_PROPERTIES"; then
        echo -e "${VERDE}✓ Auto-download de JDK habilitado${NC}"
    else
        echo -e "${AMARILLO}⚠ Auto-download de JDK no está habilitado${NC}"
    fi
else
    echo -e "${ROJO}✗ No se encontró el archivo $GRADLE_PROPERTIES${NC}"
fi

echo -e "\n${AMARILLO}Resumen de verificación:${NC}"
echo -e "Si todos los items tienen ✓, el proyecto está correctamente configurado para JDK 11."
echo -e "Si hay items con ✗, revisa el documento MIGRACION_JDK11.md para instrucciones detalladas."
echo -e "\n${VERDE}Para más información, consulta el archivo MIGRACION_JDK11.md${NC}"
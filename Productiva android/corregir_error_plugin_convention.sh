#!/bin/bash

# Script para corregir los errores de convencion de plugins en Gradle
# Este script busca y reemplaza usos obsoletos de la API de convención de plugins
# en archivos de configuración de Gradle.

# Colores para mensajes
ROJO='\033[0;31m'
VERDE='\033[0;32m'
AMARILLO='\033[0;33m'
NC='\033[0m' # Sin Color

echo -e "${AMARILLO}Iniciando corrección de errores de convencion de plugins...${NC}"

# Archivos a verificar
BUILD_GRADLE="build.gradle"
APP_BUILD_GRADLE="app/build.gradle"
SETTINGS_GRADLE="settings.gradle"

# 1. Verificar si hay problemas en build.gradle del proyecto
if [ -f "$BUILD_GRADLE" ]; then
    echo -e "${AMARILLO}Verificando $BUILD_GRADLE...${NC}"
    
    # Buscar usos de convention.plugins
    if grep -q "convention.plugins" "$BUILD_GRADLE"; then
        echo -e "${ROJO}Encontrado uso obsoleto de convention.plugins en $BUILD_GRADLE${NC}"
        # Realizar copia de seguridad
        cp "$BUILD_GRADLE" "${BUILD_GRADLE}.bak"
        echo -e "${VERDE}Copia de seguridad creada: ${BUILD_GRADLE}.bak${NC}"
        
        # Reemplazar patrón de convención antigua con el nuevo estilo
        sed -i 's/convention.plugins.java.sourceCompatibility = JavaVersion/java { sourceCompatibility = JavaVersion/g' "$BUILD_GRADLE"
        sed -i 's/convention.plugins.java.targetCompatibility = JavaVersion/java { targetCompatibility = JavaVersion/g' "$BUILD_GRADLE"
        
        echo -e "${VERDE}Se ha actualizado $BUILD_GRADLE para usar el nuevo estilo de plugins${NC}"
    else
        echo -e "${VERDE}No se encontraron problemas en $BUILD_GRADLE${NC}"
    fi
else
    echo -e "${AMARILLO}No se encontró el archivo $BUILD_GRADLE${NC}"
fi

# 2. Verificar app/build.gradle
if [ -f "$APP_BUILD_GRADLE" ]; then
    echo -e "${AMARILLO}Verificando $APP_BUILD_GRADLE...${NC}"
    
    # Buscar usos de convention.plugins en el bloque android
    if grep -q "android.convention.plugins" "$APP_BUILD_GRADLE"; then
        echo -e "${ROJO}Encontrado uso obsoleto de android.convention.plugins en $APP_BUILD_GRADLE${NC}"
        # Realizar copia de seguridad
        cp "$APP_BUILD_GRADLE" "${APP_BUILD_GRADLE}.bak"
        echo -e "${VERDE}Copia de seguridad creada: ${APP_BUILD_GRADLE}.bak${NC}"
        
        # Reemplazar patrones de convención antigua con el nuevo estilo
        sed -i 's/android.convention.plugins.somePlugin/android.somePlugin/g' "$APP_BUILD_GRADLE"
        
        echo -e "${VERDE}Se ha actualizado $APP_BUILD_GRADLE para usar el nuevo estilo de plugins${NC}"
    else
        echo -e "${VERDE}No se encontraron problemas en $APP_BUILD_GRADLE${NC}"
    fi
else
    echo -e "${AMARILLO}No se encontró el archivo $APP_BUILD_GRADLE${NC}"
fi

# 3. Buscar plugins personalizados que puedan usar la API de convención
echo -e "${AMARILLO}Buscando plugins personalizados que usen la API de convención...${NC}"

PLUGINS_ENCONTRADOS=$(find . -name "*.gradle" -type f -exec grep -l "convention.plugins" {} \;)

if [ -n "$PLUGINS_ENCONTRADOS" ]; then
    echo -e "${ROJO}Se encontraron archivos que usan la API de convención obsoleta:${NC}"
    echo "$PLUGINS_ENCONTRADOS"
    echo -e "${AMARILLO}Estos archivos requieren revisión manual. Consulta el documento SOLUCION_ERROR_PLUGIN_CONVENTION.md${NC}"
else
    echo -e "${VERDE}No se encontraron plugins personalizados que usen la API de convención obsoleta${NC}"
fi

echo -e "${VERDE}¡Proceso de corrección completado!${NC}"
echo -e "${AMARILLO}Por favor, sincroniza tu proyecto en Android Studio para verificar los cambios.${NC}"
@echo off
echo CORRECCION DE ERROR DE SINTAXIS EN BUILD.GRADLE
echo ============================================
echo.
echo Este script corregira el error:
echo "Could not find method compileSdk() for arguments [34] on project ':app'"
echo.
echo Este error ocurre cuando la sintaxis del build.gradle no es correcta.
echo.
set /p CONFIRMAR="Continuar con la reparacion? (S/N): "

if /i "%CONFIRMAR%" neq "S" (
    echo Operacion cancelada.
    goto :EOF
)

echo.
echo 1. Creando copia de seguridad del build.gradle actual...
if not exist backups mkdir backups
copy app\build.gradle backups\build.gradle.error.bak

echo.
echo 2. Reemplazando con la version corregida...
copy app\build.gradle.corrected app\build.gradle
echo Archivo reemplazado correctamente.

echo.
echo 3. Verificando configuracion del android block...
powershell -Command "(Get-Content app\build.gradle) | Select-String -Pattern 'android \{' -Context 0,5" 

echo.
echo =============================================
echo INSTRUCCIONES PARA COMPLETAR LA REPARACION
echo =============================================
echo.
echo 1. Cierra completamente Android Studio si esta abierto
echo 2. Borra las siguientes carpetas (si existen):
echo    - .gradle
echo    - app\build
echo    - .idea\caches
echo 3. Vuelve a abrir el proyecto en Android Studio
echo 4. Si se te pide sincronizar, acepta
echo 5. Si continua el error, intenta:
echo    A) Invalidar caches/restart en Android Studio:
echo       File -^> Invalidate Caches/Restart...
echo    B) Ejecutar el script SOLUCION_DEFINITIVA_KAPT.bat
echo.
echo IMPORTANTE: La sintaxis correcta para compileSdk es:
echo    compileSdk 34    (sin parentesis)
echo NO compileSdk(34)   (esto causa el error)
echo.
pause
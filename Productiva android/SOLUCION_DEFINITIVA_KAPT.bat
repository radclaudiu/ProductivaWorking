@echo off
echo SOLUCION DEFINITIVA PARA ERROR DE KAPT
echo ====================================
echo.
echo Este script realizara una solucion completa para el error de KAPT:
echo "superclass access check failed: class org.jetbrains.kotlin.kapt3.base..."
echo.
echo IMPORTANTE: Esta solucion requiere JDK 8 instalado en tu sistema.
echo Si no tienes JDK 8, cancelar y descargar de: https://adoptium.net/temurin/releases/?version=8
echo.
set /p CONFIRMAR="Continuar con la solucion? (S/N): "

if /i "%CONFIRMAR%" neq "S" (
    echo Operacion cancelada.
    goto :EOF
)

echo.
echo 1. Realizando copia de seguridad de archivos importantes...
if not exist backups mkdir backups
copy gradle.properties backups\gradle.properties.bak
copy app\build.gradle backups\build.gradle.bak
copy settings.gradle backups\settings.gradle.bak
if exist build.gradle copy build.gradle backups\root-build.gradle.bak

echo.
echo 2. Configurando JDK 8 para el proyecto...

echo # Configuracion forzada para usar JDK 8 > local.properties.jdk
echo org.gradle.java.home=C:\\Program Files\\Eclipse Adoptium\\jdk-8.0 >> local.properties.jdk
echo # Ajusta la ruta anterior a la ubicacion de tu JDK 8 >> local.properties.jdk
echo # Por ejemplo, podria ser: >> local.properties.jdk
echo # C:\\Program Files\\Java\\jdk1.8.0_301 >> local.properties.jdk
echo # o >> local.properties.jdk
echo # C:\\Program Files\\Eclipse Adoptium\\jdk-8.0 >> local.properties.jdk

echo.
echo 3. Limpiando caches y archivos temporales...
rd /s /q .gradle
rd /s /q build
rd /s /q app\build
rd /s /q .idea\libraries
rd /s /q .idea\modules
rd /s /q app\.cxx
echo La limpieza mejora considerablemente las posibilidades de exito.

echo.
echo 4. Ajustando configuracion gradle.properties...
(
echo # Project-wide Gradle settings
echo org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
echo # Enable AndroidX
echo android.useAndroidX=true
echo # Automatically convert third-party libraries to use AndroidX
echo android.enableJetifier=true
echo # Kotlin code style
echo kotlin.code.style=official
echo # Parallel execution
echo org.gradle.parallel=true
echo # Incremental compilation
echo org.gradle.caching=true
echo # Configuration on demand
echo org.gradle.configureondemand=true
echo # Enable modern R8 desugaring
echo android.enableR8.fullMode=true
echo # KAPT configurations
echo kapt.incremental.apt=true
echo kapt.use.worker.api=true
echo kapt.include.compile.classpath=false
echo # Non-transitive R classes
echo android.nonTransitiveRClass=true
echo # Forzar autodeteccion de JDK instalados
echo org.gradle.java.installations.auto-detect=true
echo # Importante: No descargar JDKs automaticamente
echo org.gradle.java.installations.auto-download=false
) > gradle.properties.new

echo.
echo 5. Ajustando configuracion app/build.gradle para KAPT...
copy app\build.gradle app\build.gradle.ksp
type app\build.gradle | findstr /v "id 'com.google.devtools.ksp'" | findstr /v "ksp " > app\build.gradle.tmp
move /y app\build.gradle.tmp app\build.gradle

echo.
echo 6. Insertando configuracion KAPT en app/build.gradle...
powershell -Command "(Get-Content app\build.gradle) -replace \"id 'org.jetbrains.kotlin.android'\", \"id 'org.jetbrains.kotlin.android'\nid 'kotlin-kapt'\" | Set-Content app\build.gradle.tmp"
move /y app\build.gradle.tmp app\build.gradle

echo.
echo 7. Asegurando dependencias KAPT en lugar de KSP...
powershell -Command "(Get-Content app\build.gradle) -replace \"ksp 'androidx.room:room-compiler:2.6.1'\", \"kapt 'androidx.room:room-compiler:2.6.1'\" | Set-Content app\build.gradle.tmp"
move /y app\build.gradle.tmp app\build.gradle

echo.
echo 8. Aplicando configuraciones adicionales a kotlinOptions...
powershell -Command "(Get-Content app\build.gradle) -replace \"jvmTarget = '1.8'\", \"jvmTarget = '1.8'\n        // Opciones para mejorar compatibilidad\n        freeCompilerArgs += [\n            \`\"-Xjvm-default=all\`\",\n            \`\"-Xsam-conversions=class\`\"\n        ]\" | Set-Content app\build.gradle.tmp"
move /y app\build.gradle.tmp app\build.gradle

echo.
echo 9. Agregando JDK 8 como toolchain explicita...
powershell -Command "(Get-Content app\build.gradle) -replace \"// Usando compatibilidad con Java 8\", \"// Configurando JDK 8 explicitamente\n    jvmToolchain(8)\" | Set-Content app\build.gradle.tmp"
move /y app\build.gradle.tmp app\build.gradle

echo.
echo ======================================
echo INSTRUCCIONES FINALES MUY IMPORTANTES
echo ======================================
echo.
echo 1. CERRAR completamente Android Studio
echo 2. VOLVER A ABRIR el proyecto
echo 3. En Android Studio, ir a:
echo    File -^> Settings -^> Build, Execution, Deployment -^> Build Tools -^> Gradle
echo 4. En "Gradle JDK" seleccionar explicitamente JDK 8
echo 5. Aceptar cambios y sincronizar el proyecto
echo.
echo IMPORTANTE: Esto deberia resolver definitivamente el problema.
echo.
echo Si el problema persiste, editar el archivo local.properties.jdk
echo y agregar la ruta correcta a tu JDK 8, luego renombrarlo a local.properties
echo.
pause
package com.productiva.android.utils

import android.content.Context
import android.util.Log
import com.productiva.android.BuildConfig
import java.io.File
import java.io.FileOutputStream

/**
 * Clase utilitaria para depurar y diagnosticar problemas con recursos.
 * 
 * Esta clase provee herramientas para analizar y resolver problemas 
 * relacionados con recursos en tiempo de ejecución y compilación.
 */
class ResourceDebugHelper {
    companion object {
        private const val TAG = "ResourceDebugHelper"
        
        /**
         * Analiza los recursos de la aplicación y registra información diagnóstica.
         * Este método debe ser llamado durante la inicialización de la aplicación.
         * 
         * @param context El contexto de la aplicación
         */
        fun analyzeResources(context: Context) {
            Log.d(TAG, "Iniciando análisis de recursos de la aplicación")
            
            try {
                // Verificar estructura de íconos adaptativos
                validateAdaptiveIconStructure(context)
                
                // Verificar posibles recursos duplicados
                val duplicates = checkForDuplicateResources(context)
                if (duplicates.isNotEmpty()) {
                    Log.w(TAG, "Se encontraron ${duplicates.size} posibles recursos duplicados")
                    for (id in duplicates) {
                        try {
                            Log.w(TAG, "  - ${context.resources.getResourceName(id)}")
                        } catch (e: Exception) {
                            Log.w(TAG, "  - Recurso con ID: $id")
                        }
                    }
                }
                
                // Guardar informe para depuración
                if (BuildConfig.DEBUG) {
                    val reportFile = saveResourceReport(context)
                    Log.d(TAG, "Informe de recursos guardado en: ${reportFile?.absolutePath ?: "Error al guardar"}")
                }
                
                Log.d(TAG, "Análisis de recursos completado")
            } catch (e: Exception) {
                Log.e(TAG, "Error durante el análisis de recursos", e)
            }
        }
        
        /**
         * Reporta soluciones para problemas comunes de compilación relacionados con recursos.
         * Este método muestra información útil para resolver problemas en tiempo de compilación.
         */
        fun reportSolutionForBuildIssues() {
            Log.i(TAG, "====================================================")
            Log.i(TAG, "SOLUCIONES PARA PROBLEMAS COMUNES DE COMPILACIÓN:")
            Log.i(TAG, "1. Para errores de recursos duplicados:")
            Log.i(TAG, "   - Agregar 'android.nonTransitiveRClass=true' en gradle.properties")
            Log.i(TAG, "   - Usar recursos con nombres únicos en todo el proyecto")
            Log.i(TAG, "   - Configurar 'android.enableR8.fullMode=true' en gradle.properties")
            Log.i(TAG, "")
            Log.i(TAG, "2. Para conflictos de íconos adaptativos:")
            Log.i(TAG, "   - Asegurarse de que minSdk >= 26 para íconos adaptativos")
            Log.i(TAG, "   - Proporcionar recursos foreground y background")
            Log.i(TAG, "   - Usar resource_validation.xml para definir prioridades")
            Log.i(TAG, "====================================================")
        }
        
        /**
         * Genera un informe de los recursos de la aplicación.
         * 
         * @param context El contexto de la aplicación
         * @return Un string con información sobre los recursos cargados
         */
        fun generateResourceReport(context: Context): String {
            val sb = StringBuilder()
            sb.appendLine("======= INFORME DE RECURSOS =======")
            sb.appendLine("Fecha: ${java.util.Date()}")
            sb.appendLine("Nombre del paquete: ${context.packageName}")
            
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                sb.appendLine("Versión de la aplicación: ${packageInfo.versionName}")
            } catch (e: Exception) {
                sb.appendLine("Error al obtener versión")
            }
            
            sb.appendLine("-----------------------------------")
            
            // Información sobre recursos conflictivos
            val conflictingResources = ResourceConflictResolver.getConflictingResources()
            sb.appendLine("Recursos conflictivos encontrados: ${conflictingResources.size}")
            
            conflictingResources.forEach { resId ->
                try {
                    val resourceName = context.resources.getResourceName(resId)
                    sb.appendLine("- $resourceName (ID: $resId)")
                } catch (e: Exception) {
                    sb.appendLine("- Recurso desconocido (ID: $resId)")
                }
            }
            
            sb.appendLine("-----------------------------------")
            sb.appendLine("Información de compatibilidad de íconos:")
            sb.appendLine("Versión de Android: ${android.os.Build.VERSION.SDK_INT} (${android.os.Build.VERSION.RELEASE})")
            sb.appendLine("¿Soporta íconos adaptativos? ${IconVersionProvider.supportsAdaptiveIcons()}")
            
            return sb.toString()
        }
        
        /**
         * Guarda un informe de recursos en el almacenamiento interno.
         * 
         * @param context El contexto de la aplicación
         * @return El archivo generado o null si hubo un error
         */
        fun saveResourceReport(context: Context): File? {
            return try {
                val report = generateResourceReport(context)
                val filename = "resource_report_${System.currentTimeMillis()}.txt"
                val file = File(context.filesDir, filename)
                
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(report.toByteArray())
                }
                
                Log.d(TAG, "Informe de recursos guardado en: ${file.absolutePath}")
                file
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar el informe de recursos", e)
                null
            }
        }
        
        /**
         * Verifica la estructura de recursos adaptativos.
         * 
         * @param context El contexto de la aplicación
         * @return true si la estructura es válida, false en caso contrario
         */
        fun validateAdaptiveIconStructure(context: Context): Boolean {
            if (!IconVersionProvider.supportsAdaptiveIcons()) {
                Log.d(TAG, "El dispositivo no soporta íconos adaptativos, no es necesaria la validación")
                return true
            }
            
            val foregroundId = context.resources.getIdentifier(
                "ic_launcher_foreground", "drawable", context.packageName)
            val backgroundId = context.resources.getIdentifier(
                "ic_launcher_background", "drawable", context.packageName)
            
            val valid = foregroundId != 0 && backgroundId != 0
            
            if (!valid) {
                Log.w(TAG, "Estructura de íconos adaptativos incompleta: " +
                      "foreground=${foregroundId != 0}, background=${backgroundId != 0}")
            } else {
                Log.d(TAG, "Estructura de íconos adaptativos válida")
            }
            
            return valid
        }
        
        /**
         * Verifica recursos potencialmente duplicados.
         * 
         * @param context El contexto de la aplicación
         * @return Lista de identificadores de recursos que podrían estar duplicados
         */
        fun checkForDuplicateResources(context: Context): List<Int> {
            val duplicateCandidates = mutableListOf<Int>()
            
            // Lista de recursos que comúnmente pueden estar duplicados
            val resourcesToCheck = listOf(
                "ic_launcher" to "mipmap",
                "ic_launcher_round" to "mipmap",
                "activity_main" to "layout",
                "fragment_home" to "layout",
                "app_name" to "string"
            )
            
            for ((name, type) in resourcesToCheck) {
                try {
                    val id = context.resources.getIdentifier(name, type, context.packageName)
                    if (id != 0) {
                        try {
                            // Intentar acceder al recurso para verificar si hay problemas
                            when (type) {
                                "mipmap", "drawable" -> context.resources.getDrawable(id, null)
                                "string" -> context.resources.getString(id)
                                "layout" -> context.resources.getLayout(id)
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Posible recurso duplicado: $name ($type)", e)
                            duplicateCandidates.add(id)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al verificar el recurso: $name ($type)", e)
                }
            }
            
            return duplicateCandidates
        }
        
        /**
         * Diagnóstica problemas comunes de recursos y propone soluciones.
         * 
         * @param context El contexto de la aplicación
         * @return String con el diagnóstico y posibles soluciones
         */
        fun diagnoseResourceIssues(context: Context): String {
            val sb = StringBuilder()
            sb.appendLine("======= DIAGNÓSTICO DE RECURSOS =======")
            
            // Verificar estructura de íconos adaptativos
            val adaptiveValid = validateAdaptiveIconStructure(context)
            if (!adaptiveValid) {
                sb.appendLine("PROBLEMA: Estructura de íconos adaptativos incompleta")
                sb.appendLine("SOLUCIÓN: Asegúrese de proporcionar ic_launcher_foreground.xml" +
                              " y valores para ic_launcher_background")
                sb.appendLine()
            }
            
            // Verificar posibles duplicados
            val duplicates = checkForDuplicateResources(context)
            if (duplicates.isNotEmpty()) {
                sb.appendLine("PROBLEMA: Se encontraron ${duplicates.size} posibles recursos duplicados")
                sb.appendLine("SOLUCIÓN: Verifique los siguientes recursos:")
                duplicates.forEach { resId ->
                    try {
                        sb.appendLine("- ${context.resources.getResourceName(resId)}")
                    } catch (e: Exception) {
                        sb.appendLine("- Recurso desconocido (ID: $resId)")
                    }
                }
                sb.appendLine("Considere usar resource_validation.xml para definir reglas")
                sb.appendLine()
            }
            
            // Verificar recursos en conflicto
            val conflicts = ResourceConflictResolver.getConflictingResources()
            if (conflicts.isNotEmpty()) {
                sb.appendLine("PROBLEMA: Se encontraron ${conflicts.size} recursos en conflicto")
                sb.appendLine("SOLUCIÓN: Utilice ResourceConflictResolver para acceder " +
                              "a estos recursos de forma segura")
                sb.appendLine()
            }
            
            if (!adaptiveValid && duplicates.isEmpty() && conflicts.isEmpty()) {
                sb.appendLine("No se encontraron problemas específicos con los recursos.")
                sb.appendLine("La aplicación parece estar utilizando los recursos correctamente.")
            }
            
            return sb.toString()
        }
    }
}
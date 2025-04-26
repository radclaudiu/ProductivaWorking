package com.productiva.android.network

/**
 * Clase genérica para representar respuestas de la API.
 * Contiene información sobre el éxito o fracaso de la petición,
 * así como los datos devueltos o mensajes de error.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errors: Map<String, List<String>>? = null
) {
    /**
     * Comprueba si hay errores de validación para un campo específico.
     */
    fun hasValidationError(field: String): Boolean {
        return errors?.containsKey(field) == true
    }
    
    /**
     * Obtiene el primer mensaje de error para un campo específico.
     */
    fun getFirstValidationError(field: String): String? {
        return errors?.get(field)?.firstOrNull()
    }
    
    /**
     * Obtiene todos los mensajes de error para un campo específico.
     */
    fun getValidationErrors(field: String): List<String> {
        return errors?.get(field) ?: emptyList()
    }
    
    /**
     * Comprueba si hay algún error de validación.
     */
    fun hasAnyValidationError(): Boolean {
        return errors?.isNotEmpty() == true
    }
    
    /**
     * Obtiene el mensaje de error general o el primer error de validación.
     */
    fun getErrorMessage(): String {
        return when {
            message != null -> message
            hasAnyValidationError() -> {
                val firstField = errors?.keys?.firstOrNull()
                val firstError = firstField?.let { getFirstValidationError(it) }
                firstError ?: "Error de validación"
            }
            else -> "Error desconocido"
        }
    }
    
    companion object {
        /**
         * Crea una respuesta de éxito.
         */
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(true, data, message)
        }
        
        /**
         * Crea una respuesta de error.
         */
        fun <T> error(message: String, errors: Map<String, List<String>>? = null): ApiResponse<T> {
            return ApiResponse(false, null, message, errors)
        }
    }
}
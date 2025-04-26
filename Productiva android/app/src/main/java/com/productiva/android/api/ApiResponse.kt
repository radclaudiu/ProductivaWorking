package com.productiva.android.api

/**
 * Clase para envolver las respuestas de la API
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errors: Map<String, List<String>>? = null,
    val token: String? = null
) {
    companion object {
        /**
         * Crea una instancia de ApiResponse que indica éxito
         */
        fun <T> success(data: T?, message: String? = null, token: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message,
                token = token
            )
        }
        
        /**
         * Crea una instancia de ApiResponse que indica error
         */
        fun <T> error(message: String, errors: Map<String, List<String>>? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                errors = errors
            )
        }
    }
    
    /**
     * Determina si la respuesta es exitosa y contiene datos
     */
    fun isSuccessfulWithData(): Boolean {
        return success && data != null
    }
}
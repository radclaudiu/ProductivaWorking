package com.productiva.android.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Interceptor para registrar peticiones y respuestas HTTP.
 * Útil para depuración.
 */
class LoggingInterceptor : Interceptor {
    
    companion object {
        private const val TAG = "LoggingInterceptor"
        private val UTF8 = StandardCharsets.UTF_8
        private const val MAX_BODY_LOG_SIZE = 2048 // Limitar tamaño del cuerpo para evitar logs muy grandes
    }
    
    /**
     * Intercepta la petición y registra información de ella y de la respuesta.
     *
     * @param chain Cadena de interceptores.
     * @return Respuesta de la petición.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Registrar la petición
        val requestTime = System.currentTimeMillis()
        Log.d(TAG, "Enviando petición a ${request.url}")
        Log.d(TAG, "Método: ${request.method}")
        Log.d(TAG, "Headers: ${request.headers}")
        
        // Registrar cuerpo de la petición si existe
        if (request.body != null) {
            val requestBuffer = Buffer()
            try {
                request.body?.writeTo(requestBuffer)
                val requestBody = requestBuffer.readString(UTF8)
                
                // Limitar el tamaño del cuerpo para no saturar los logs
                val truncatedBody = if (requestBody.length > MAX_BODY_LOG_SIZE) {
                    requestBody.substring(0, MAX_BODY_LOG_SIZE) + "... [truncado]"
                } else {
                    requestBody
                }
                
                Log.d(TAG, "Cuerpo: $truncatedBody")
            } catch (e: IOException) {
                Log.e(TAG, "Error al leer el cuerpo de la petición", e)
            }
        }
        
        // Ejecutar la petición
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.e(TAG, "Error en la petición a ${request.url}", e)
            throw e
        }
        
        // Registrar la respuesta
        val responseTime = System.currentTimeMillis()
        val duration = responseTime - requestTime
        Log.d(TAG, "Recibida respuesta de ${response.request.url} en ${duration}ms")
        Log.d(TAG, "Código de estado: ${response.code}")
        Log.d(TAG, "Headers: ${response.headers}")
        
        // Registrar cuerpo de la respuesta (esto consume el cuerpo, por lo que hay que clonarlo)
        val responseBody = response.body
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer completo
            val buffer = source.buffer.clone()
            
            val contentType = responseBody.contentType()
            val charset = contentType?.charset(UTF8) ?: UTF8
            
            val bodyString = buffer.readString(charset)
            val truncatedBody = if (bodyString.length > MAX_BODY_LOG_SIZE) {
                bodyString.substring(0, MAX_BODY_LOG_SIZE) + "... [truncado]"
            } else {
                bodyString
            }
            
            Log.d(TAG, "Cuerpo: $truncatedBody")
            
            // Recrear el cuerpo para no interferir con el resto de la cadena
            val contentLength = responseBody.contentLength()
            response.newBuilder()
                .body(ResponseBody.create(contentType, bodyString))
                .build()
        }
        
        return response
    }
}
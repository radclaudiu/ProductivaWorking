package com.productiva.android.network

import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor para verificar el estado de la conexión a Internet antes de
 * realizar solicitudes HTTP.
 *
 * Esta clase comprueba si hay una conexión a Internet disponible antes de
 * proceder con una solicitud. Si no hay conexión, lanza una NoConnectivityException.
 *
 * @property context Contexto de la aplicación
 * @property networkStatusManager Administrador del estado de la red
 */
class NetworkConnectionInterceptor(
    private val context: Context,
    private val networkStatusManager: NetworkStatusManager
) : Interceptor {

    /**
     * Intercepta la solicitud HTTP y verifica la conectividad.
     * Si no hay conexión, lanza una excepción. Si hay conexión, procede con la solicitud.
     *
     * @param chain Cadena de interceptores
     * @return Respuesta HTTP si hay conexión
     * @throws NoConnectivityException si no hay conexión a Internet
     */
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isNetworkAvailable()) {
            throw NoConnectivityException("No hay conexión a Internet disponible")
        }
        
        // Si hay conexión, continuamos con la solicitud
        val builder: Request.Builder = chain.request().newBuilder()
        return chain.proceed(builder.build())
    }
    
    /**
     * Verifica si hay una conexión a Internet disponible usando el NetworkStatusManager.
     */
    private fun isNetworkAvailable(): Boolean = runBlocking {
        networkStatusManager.connectionStatus.first().isConnected
    }
    
    /**
     * Excepción lanzada cuando no hay conectividad a Internet.
     */
    class NoConnectivityException(message: String) : IOException(message)
}
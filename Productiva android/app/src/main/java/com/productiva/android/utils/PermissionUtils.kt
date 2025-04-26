package com.productiva.android.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Utilidades para gestionar permisos en diferentes versiones de Android.
 */
object PermissionUtils {

    /**
     * Verifica si se concedieron todos los permisos de Bluetooth necesarios según la versión de Android.
     *
     * @param context Contexto de la aplicación.
     * @return true si se concedieron todos los permisos necesarios, false en caso contrario.
     */
    fun hasBluetoothPermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+)
            hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT) &&
                    hasPermission(context, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            // Android 11 y anteriores
            hasPermission(context, Manifest.permission.BLUETOOTH) &&
                    hasPermission(context, Manifest.permission.BLUETOOTH_ADMIN)
        }
    }

    /**
     * Solicita permisos de Bluetooth según la versión de Android.
     *
     * @param activity Actividad desde la que se solicitan los permisos.
     * @param requestCode Código de solicitud para identificar el resultado.
     */
    fun requestBluetoothPermissions(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+)
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                requestCode
            )
        } else {
            // Android 11 y anteriores
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                ),
                requestCode
            )
        }
    }

    /**
     * Solicita permisos de Bluetooth usando ActivityResultLauncher (API moderna).
     *
     * @param permissionLauncher Launcher para solicitar permisos.
     */
    fun requestBluetoothPermissionsWithLauncher(permissionLauncher: ActivityResultLauncher<Array<String>>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+)
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            // Android 11 y anteriores
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            )
        }
    }

    /**
     * Verifica si se concedió un permiso específico.
     *
     * @param context Contexto de la aplicación.
     * @param permission Permiso a verificar.
     * @return true si se concedió el permiso, false en caso contrario.
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verifica si se concedió el permiso de notificaciones (importante para Android 13+).
     *
     * @param context Contexto de la aplicación.
     * @return true si se concedió el permiso, false en caso contrario.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // En versiones anteriores no se requiere permiso explícito
            true
        }
    }

    /**
     * Solicita el permiso de notificaciones (Android 13+).
     *
     * @param permissionLauncher Launcher para solicitar permisos.
     */
    fun requestNotificationPermission(permissionLauncher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Abre la configuración de la aplicación para permitir al usuario habilitar manualmente
     * permisos que ha denegado permanentemente.
     *
     * @param context Contexto de la aplicación.
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        context.startActivity(intent)
    }
}
package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.data.converters.MapConverter
import java.util.*

/**
 * Entidad que representa una operación pendiente de sincronización con el servidor.
 * Almacena los detalles de las operaciones que se realizaron offline y
 * necesitan ser enviadas al servidor cuando se recupere la conexión.
 *
 * @property id Identificador único para la operación
 * @property entityType Tipo de entidad afectada (checkpoint, tarea, etc.)
 * @property entityId Identificador de la entidad afectada
 * @property operationType Tipo de operación (CREATE, UPDATE, DELETE)
 * @property data Datos de la operación en formato JSON (como Map)
 * @property createdAt Timestamp de creación de la operación pendiente
 * @property priority Prioridad de la operación (mayor = más prioritario)
 * @property attemptCount Número de intentos de sincronización realizados
 * @property lastAttemptAt Timestamp del último intento de sincronización
 * @property errorMessage Mensaje de error del último intento fallido
 */
@Entity(tableName = "sync_pending_operations")
@TypeConverters(MapConverter::class)
data class SyncPendingOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: String,
    val entityId: Long,
    val operationType: String,
    val data: Map<String, Any>,
    val createdAt: Long = System.currentTimeMillis(),
    val priority: Int = PRIORITY_NORMAL,
    val attemptCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val errorMessage: String? = null
) {
    companion object {
        // Tipos de entidades
        const val ENTITY_CHECKPOINT_RECORD = "checkpoint_record"
        const val ENTITY_TASK_ASSIGNMENT = "task_assignment"
        const val ENTITY_CASH_REGISTER = "cash_register"
        
        // Tipos de operaciones
        const val OPERATION_CREATE = "CREATE"
        const val OPERATION_UPDATE = "UPDATE"
        const val OPERATION_DELETE = "DELETE"
        
        // Niveles de prioridad
        const val PRIORITY_HIGH = 100
        const val PRIORITY_NORMAL = 50
        const val PRIORITY_LOW = 10
        
        /**
         * Crea una nueva operación pendiente para un registro de fichaje
         */
        fun createForCheckpointRecord(
            recordId: Long,
            operationType: String,
            data: Map<String, Any>
        ): SyncPendingOperation {
            return SyncPendingOperation(
                entityType = ENTITY_CHECKPOINT_RECORD,
                entityId = recordId,
                operationType = operationType,
                data = data,
                priority = PRIORITY_HIGH // Los fichajes son de alta prioridad
            )
        }
        
        /**
         * Crea una nueva operación pendiente para una asignación de tarea
         */
        fun createForTaskAssignment(
            assignmentId: Long,
            operationType: String,
            data: Map<String, Any>
        ): SyncPendingOperation {
            return SyncPendingOperation(
                entityType = ENTITY_TASK_ASSIGNMENT,
                entityId = assignmentId,
                operationType = operationType,
                data = data,
                priority = PRIORITY_NORMAL
            )
        }
        
        /**
         * Crea una nueva operación pendiente para un arqueo de caja
         */
        fun createForCashRegister(
            registerId: Long,
            operationType: String,
            data: Map<String, Any>
        ): SyncPendingOperation {
            return SyncPendingOperation(
                entityType = ENTITY_CASH_REGISTER,
                entityId = registerId,
                operationType = operationType,
                data = data,
                priority = PRIORITY_HIGH // Los arqueos son de alta prioridad
            )
        }
    }
    
    /**
     * Incrementa el contador de intentos y actualiza la fecha del último intento
     */
    fun incrementAttempt(errorMsg: String? = null): SyncPendingOperation {
        return this.copy(
            attemptCount = this.attemptCount + 1,
            lastAttemptAt = System.currentTimeMillis(),
            errorMessage = errorMsg
        )
    }
    
    /**
     * Verifica si la operación está bloqueada por demasiados intentos fallidos
     */
    fun isBlocked(): Boolean {
        return attemptCount >= 5 // Bloquear después de 5 intentos fallidos
    }
}
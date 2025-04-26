package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

/**
 * Modelo de datos para plantillas de etiquetas
 */
@Entity(tableName = "label_templates")
data class LabelTemplate(
    @PrimaryKey
    val id: Int,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String?,
    
    @ColumnInfo(name = "userId")
    val userId: Int?,
    
    @ColumnInfo(name = "companyId")
    val companyId: Int?,
    
    @ColumnInfo(name = "templateData")
    val templateData: String,
    
    @ColumnInfo(name = "paperWidth")
    val paperWidth: Int = 62,
    
    @ColumnInfo(name = "paperHeight")
    val paperHeight: Int = 29,
    
    @ColumnInfo(name = "orientation")
    val orientation: String = "landscape",
    
    @ColumnInfo(name = "dpi")
    val dpi: Int = 300,
    
    @ColumnInfo(name = "isPublic")
    val isPublic: Boolean = false,
    
    @ColumnInfo(name = "createdAt")
    val createdAt: Date?,
    
    @ColumnInfo(name = "updatedAt")
    val updatedAt: Date?,
    
    @ColumnInfo(name = "synced")
    val synced: Boolean = true,
    
    @ColumnInfo(name = "pendingUpload")
    val pendingUpload: Boolean = false,
    
    @ColumnInfo(name = "localId")
    val localId: Long? = null
) {
    companion object {
        const val ORIENTATION_PORTRAIT = "portrait"
        const val ORIENTATION_LANDSCAPE = "landscape"
        
        val ORIENTATIONS = listOf(
            ORIENTATION_PORTRAIT,
            ORIENTATION_LANDSCAPE
        )
    }
}
package com.productiva.android.utils

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.models.Location

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations")
    fun getAllLocations(): LiveData<List<Location>>
    
    @Query("SELECT * FROM locations WHERE companyId = :companyId")
    fun getLocationsByCompanyId(companyId: Int): LiveData<List<Location>>
    
    @Query("SELECT * FROM locations WHERE id = :locationId")
    fun getLocationById(locationId: Int): LiveData<Location>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLocations(locations: List<Location>)
    
    @Update
    suspend fun updateLocation(location: Location)
    
    @Query("DELETE FROM locations")
    suspend fun deleteAllLocations()
}
package com.leaseflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.leaseflow.app.data.local.entities.PropertyEntity

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties")
    suspend fun getAll(): List<PropertyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PropertyEntity>)
}

package com.leaseflow.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.leaseflow.app.data.local.dao.PropertyDao
import com.leaseflow.app.data.local.entities.PropertyEntity

@Database(
    entities = [PropertyEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
}

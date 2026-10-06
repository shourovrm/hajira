package com.rms.hazira.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PersonEntity::class, DayRecordEntity::class, PaymentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class HaziraDatabase : RoomDatabase() {

    abstract fun personDao(): PersonDao

    abstract fun dayRecordDao(): DayRecordDao

    abstract fun paymentDao(): PaymentDao

    companion object {
        fun create(context: Context): HaziraDatabase {
            return Room.databaseBuilder(context.applicationContext, HaziraDatabase::class.java, "hazira.db").build()
        }
    }
}

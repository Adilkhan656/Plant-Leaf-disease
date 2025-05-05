package com.example.cropchecker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cropchecker.data.PredictionDao
import com.example.cropchecker.models.Prediction

@Database(entities = [Prediction::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun predictionDao(): PredictionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prediction_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

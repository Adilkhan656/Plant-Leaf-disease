package com.example.cropchecker.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.cropchecker.models.Prediction

@Dao
interface PredictionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: Prediction)

    @Query("SELECT * FROM predictions ORDER BY timestamp DESC")
    fun getAllPredictions(): LiveData<List<Prediction>>

    @Query("DELETE FROM predictions")
    suspend fun deleteAllPredictions()

    @Delete
    suspend fun deletePrediction(prediction: Prediction)
}

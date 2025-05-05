package com.example.cropchecker.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import com.example.cropchecker.data.PredictionDao
import com.example.cropchecker.models.Prediction
import kotlinx.coroutines.launch

class MainViewModel(private val predictionDao: PredictionDao) : ViewModel() {

    val allPredictions: LiveData<List<Prediction>> = predictionDao.getAllPredictions()

    fun insertPrediction(prediction: Prediction) {
        viewModelScope.launch {
            predictionDao.insertPrediction(prediction)
        }
    }

    fun deletePrediction(prediction: Prediction) {
        viewModelScope.launch {
            predictionDao.deletePrediction(prediction)
        }
    }

    companion object {
        fun create(context: Context): MainViewModel {
            val database = DatabaseProvider.getDatabase(context)
            return MainViewModel(database.predictionDao())
        }
    }
}

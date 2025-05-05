package com.example.cropchecker.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions")
data class Prediction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val diseaseName: String,
    val confidence: Float,
    val timestamp: String,
    val imageUrl: String
)

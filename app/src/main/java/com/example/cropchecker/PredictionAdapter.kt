package com.example.cropchecker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cropchecker.R
import com.example.cropchecker.models.Prediction

class PredictionAdapter(
    private var predictions: MutableList<Prediction>,
    private val onDeleteClickListener: (Prediction) -> Unit,
    private val onItemClickListener: (Prediction) -> Unit
) : RecyclerView.Adapter<PredictionAdapter.PredictionViewHolder>() {

    inner class PredictionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val diseaseName: TextView = itemView.findViewById(R.id.diseaseName)
        private val confidence: TextView = itemView.findViewById(R.id.confidence)
        private val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewP)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(prediction: Prediction) {
            diseaseName.text = "Disease: ${prediction.diseaseName}"
            confidence.text = "Confidence: ${String.format("%.2f", prediction.confidence * 100)}%"
            timestamp.text = "Time: ${prediction.timestamp}"

            Glide.with(itemView.context)
                .load(prediction.imageUrl)
                .into(imageView)

            deleteButton.setOnClickListener {
                onDeleteClickListener(prediction)
            }

            itemView.setOnClickListener {
                onItemClickListener(prediction)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prediction, parent, false)
        return PredictionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        holder.bind(predictions[position])
    }

    override fun getItemCount(): Int = predictions.size

    fun submitList(newPredictions: List<Prediction>) {
        predictions.clear()
        predictions.addAll(newPredictions)
        notifyDataSetChanged()
    }

    fun deletePrediction(prediction: Prediction) {
        val index = predictions.indexOfFirst { it.id == prediction.id }
        if (index != -1) {
            predictions.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}

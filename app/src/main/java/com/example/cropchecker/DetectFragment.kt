package com.example.cropchecker

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cropchecker.database.AppDatabase
import com.example.cropchecker.models.Prediction
import isomora.com.greendoctor.TensorFlowClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DetectFragment : Fragment() {

    private lateinit var cameraButton: LinearLayout
    private lateinit var galleryButton: LinearLayout
    private lateinit var predictButton: Button
    private lateinit var textView: TextView
    private lateinit var imageView: ImageView
    private lateinit var uploadTextView: TextView // Add this line
    private lateinit var bitmap: Bitmap
    private lateinit var classifier: TensorFlowClassifier

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_GALLERY = 100
    private val MODEL_INPUT_SIZE = 224
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_detect, container, false)

        cameraButton = view.findViewById(R.id.cameraButton)
        galleryButton = view.findViewById(R.id.galleryButton)
        textView = view.findViewById(R.id.predictionResult)
        imageView = view.findViewById(R.id.imageView)
        predictButton = view.findViewById(R.id.Predict)
        uploadTextView = view.findViewById(R.id.placeholderText)

        classifier = TensorFlowClassifier(
            requireContext().assets,
            "plant_disease_model.tflite",
            "labels.txt",
            MODEL_INPUT_SIZE
        )

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }

        galleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GALLERY)
        }

        predictButton.setOnClickListener {
            if (::bitmap.isInitialized) {
                val results = classifier.recognizeImage(bitmap)
                if (results.isNotEmpty()) {
                    val bestResult = results[0]
                    val remedy = getRemedy(bestResult.title)
                    val confidenceText = (bestResult.confidence * 100).format(2)
                    val formattedRemedy = remedy.split("\n").joinToString("<br>• ", prefix = "• ")

                    val resultText = """
    <div style="text-align:center;">
        <b>Crop:</b> ${bestResult.title}<br>
        <b>Confidence:</b> $confidenceText%<br><br>
        <b>Remedy:</b><br>
        $formattedRemedy
    </div>
""".trimIndent()

                    textView.text = HtmlCompat.fromHtml(resultText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    textView.gravity = Gravity.CENTER


                    val timestamp = getCurrentTimestamp()
                    val imagePath = imageUri?.toString() ?: ""

                    val prediction = Prediction(
                        diseaseName = bestResult.title,
                        confidence = bestResult.confidence,
                        timestamp = timestamp,
                        imageUrl = imagePath
                    )

                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            AppDatabase.getDatabase(requireContext()).predictionDao().insertPrediction(prediction)
                        }
                    }
                } else {
                    textView.text = "Could not identify any crop disease."
                }
            } else {
                Toast.makeText(requireContext(), "Please capture or select an image first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val photo = data.extras?.get("data") as? Bitmap
                    if (photo != null) {
                        bitmap = Bitmap.createScaledBitmap(photo, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true)
                        imageView.setImageBitmap(bitmap)
                        imageUri = null
                        uploadTextView.visibility = View.GONE // Hide the upload text
                    }
                }
                REQUEST_GALLERY -> {
                    val uri: Uri? = data.data
                    uri?.let {
                        try {
                            val selectedImage = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                            bitmap = Bitmap.createScaledBitmap(selectedImage, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true)
                            imageView.setImageBitmap(bitmap)
                            imageUri = uri
                            uploadTextView.visibility = View.GONE // Hide the upload text
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getRemedy(label: String): String {
        return TensorFlowClassifier.remedies[label]?.joinToString("\n")
            ?: "No specific remedy found. Consider consulting an expert."
    }

}

package isomora.com.greendoctor

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.PriorityQueue

class TensorFlowClassifier(
    assetManager: AssetManager,
    modelPath: String,
    labelPath: String,
    inputSize: Int
) {

    private var interpreter: Interpreter
    private var labels: List<String>
    private val inputSize: Int = inputSize
    private val pixelSize = 3
    private val imageMean = 0
    private val imageStd = 255.0f
    private val maxResults = 3
    private val threshold = 0.4f
    companion object {
         val remedies = mapOf(
             "apple apple scab" to listOf(
                 "Apply fungicides regularly during early season.",
                 "Use scab-resistant apple varieties.",
                 "Prune trees to increase air circulation.",
                 "Clean fallen leaves and debris."
             ),
             "apple black rot" to listOf(
                 "Remove and destroy mummified fruit.",
                 "Apply fungicides at petal fall and fruit set.",
                 "Prune cankers and disinfect tools.",
                 "Avoid tree wounds and maintain nutrition."
             ),
             "apple cedar apple rust" to listOf(
                 "Remove nearby cedar or juniper trees.",
                 "Apply fungicide during early leaf development.",
                 "Prune infected leaves early.",
                 "Use rust-resistant apple cultivars."
             ),
             "apple healthy" to listOf(
                 "Tree is healthy — continue monitoring.",
                 "Water deeply once a week.",
                 "Fertilize in early spring.",
                 "Prune during dormancy for shape and health."
             ),
             "blueberry healthy" to listOf(
                 "Ensure acidic soil (pH 4.5–5.5).",
                 "Mulch with pine needles or bark.",
                 "Prune dead branches post-harvest.",
                 "Water at the base to prevent disease."
             ),
             "cherry including sour powdery mildew" to listOf(
                 "Apply sulfur-based fungicides weekly.",
                 "Prune to improve airflow in canopy.",
                 "Avoid overhead irrigation.",
                 "Remove fallen leaves and plant debris."
             ),
             "cherry including sour healthy" to listOf(
                 "Tree is healthy — maintain spacing.",
                 "Water evenly during dry periods.",
                 "Use mulch to retain moisture.",
                 "Monitor regularly for aphids and leaf spots."
             ),
             "corn maize cercospora leaf spot gray leaf spot" to listOf(
                 "Plant resistant corn hybrids.",
                 "Use crop rotation to reduce spores.",
                 "Apply fungicides at VT-R1 stages.",
                 "Avoid dense planting to increase airflow."
             ),
             "corn maize common rust" to listOf(
                 "Use rust-resistant corn hybrids.",
                 "Apply foliar fungicides at first sign.",
                 "Remove crop residues post-harvest.",
                 "Avoid excess nitrogen application."
             ),
             "corn maize northern leaf blight" to listOf(
                 "Apply fungicides early (at VT stage).",
                 "Use hybrid seeds with disease resistance.",
                 "Rotate crops with soybeans or legumes.",
                 "Destroy infected crop residue."
             ),
             "corn maize healthy" to listOf(
                 "Maintain proper irrigation schedules.",
                 "Apply balanced fertilizers regularly.",
                 "Keep fields free from weeds.",
                 "Monitor for early disease symptoms."
             ),
             "grape black rot" to listOf(
                 "Apply systemic fungicides early in season.",
                 "Remove all infected berries and leaves.",
                 "Prune for good air circulation.",
                 "Use resistant grape varieties."
             ),
             "grape esca black measles" to listOf(
                 "Avoid vine stress during drought.",
                 "Prune infected canes completely.",
                 "Control trunk disease insects.",
                 "Maintain proper nutrition and irrigation."
             ),
             "grape leaf blight isariopsis leaf spot" to listOf(
                 "Apply copper-based fungicides early.",
                 "Ensure good air movement between vines.",
                 "Remove fallen leaves regularly.",
                 "Prune heavily infected vines."
             ),
             "grape healthy" to listOf(
                 "Water deeply and infrequently.",
                 "Provide proper trellis support.",
                 "Apply compost for nutrients.",
                 "Inspect leaves weekly for pests/disease."
             ),
             "orange haunglongbing citrus greening" to listOf(
                 "Remove and destroy infected trees immediately.",
                 "Control psyllid insect vector aggressively.",
                 "Use certified disease-free nursery trees.",
                 "Avoid tree wounds and prune properly."
             ),
             "peach bacterial spot" to listOf(
                 "Use resistant peach cultivars.",
                 "Spray copper fungicide before bloom.",
                 "Avoid excessive nitrogen fertilizer.",
                 "Prune out cankers and infected shoots."
             ),
             "peach healthy" to listOf(
                 "Prune during dormancy for airflow.",
                 "Water deeply but infrequently.",
                 "Check for leaf curl and pests.",
                 "Apply compost around root zone."
             ),
             "pepper bell bacterial spot" to listOf(
                 "Use certified disease-free seeds.",
                 "Spray with copper-based fungicides weekly.",
                 "Avoid overhead watering.",
                 "Rotate crops with non-solanaceous plants."
             ),
             "pepper bell healthy" to listOf(
                 "Fertilize with phosphorus-rich mix.",
                 "Stake or cage for support.",
                 "Water evenly to prevent blossom end rot.",
                 "Inspect leaves weekly for spots or pests."
             ),
             "potato early blight" to listOf(
                 "Apply chlorothalonil or mancozeb fungicides.",
                 "Remove lower infected leaves quickly.",
                 "Practice 3-year crop rotation.",
                 "Avoid overhead irrigation."
             ),
             "potato late blight" to listOf(
                 "Use certified disease-free tubers.",
                 "Apply systemic fungicides regularly.",
                 "Destroy infected plants immediately.",
                 "Do not compost infected foliage."
             ),
             "potato healthy" to listOf(
                 "Hill soil around base for better tubers.",
                 "Apply mulch for moisture retention.",
                 "Water consistently and deeply.",
                 "Monitor for blight signs weekly."
             ),
             "raspberry healthy" to listOf(
                 "Provide full sun and support trellising.",
                 "Mulch for moisture and weed control.",
                 "Prune after fruiting.",
                 "Check regularly for cane borers."
             ),
             "soybean healthy" to listOf(
                 "Rotate crops annually to prevent disease.",
                 "Check for soybean rust signs.",
                 "Apply foliar fertilizer at V4-V6 stage.",
                 "Scout weekly for aphids and mites."
             ),
             "squash powdery mildew" to listOf(
                 "Use potassium bicarbonate sprays.",
                 "Apply neem oil or sulfur fungicides.",
                 "Ensure plants are well-spaced.",
                 "Water at base, not from above."
             ),
             "strawberry leaf scorch" to listOf(
                 "Remove and destroy infected leaves.",
                 "Avoid overhead watering.",
                 "Use mulch to prevent soil splash.",
                 "Apply organic fungicides if needed."
             ),
             "strawberry healthy" to listOf(
                 "Fertilize after harvest season.",
                 "Use straw mulch to retain moisture.",
                 "Check flowers for pests.",
                 "Allow sunlight and airflow to center."
             ),
             "tomato bacterial spot" to listOf(
                 "Use copper-based fungicides weekly.",
                 "Avoid working with wet plants.",
                 "Use resistant tomato varieties.",
                 "Disinfect tools between uses."
             ),
             "tomato early blight" to listOf(
                 "Remove lower yellowing leaves.",
                 "Apply fungicide every 7–10 days.",
                 "Rotate crops yearly.",
                 "Stake plants to avoid soil contact."
             ),
             "tomato late blight" to listOf(
                 "Spray chlorothalonil or metalaxyl.",
                 "Use certified disease-free seedlings.",
                 "Remove infected plants quickly.",
                 "Improve garden drainage."
             ),
             "tomato leaf mold" to listOf(
                 "Increase air circulation with pruning.",
                 "Apply sulfur or copper fungicide.",
                 "Avoid overhead watering.",
                 "Plant in sunny location."
             ),
             "tomato septoria leaf spot" to listOf(
                 "Remove infected leaves weekly.",
                 "Use mulch to avoid soil splash.",
                 "Apply fungicide early in season.",
                 "Water at the base only."
             ),
             "tomato spider mites two spotted spider mite" to listOf(
                 "Spray with miticide or neem oil.",
                 "Use insecticidal soap weekly.",
                 "Keep humidity high to deter mites.",
                 "Remove heavily infested leaves."
             ),
             "tomato target spot" to listOf(
                 "Remove and destroy infected leaves.",
                 "Use preventive fungicides regularly.",
                 "Avoid dense planting for airflow.",
                 "Rotate with non-host crops."
             ),
             "tomato tomato yellow leaf curl virus" to listOf(
                 "Control whitefly populations immediately.",
                 "Use reflective mulch.",
                 "Remove infected plants early.",
                 "Choose virus-resistant varieties."
             ),
             "tomato tomato mosaic virus" to listOf(
                 "Disinfect tools between plants.",
                 "Remove and destroy infected plants.",
                 "Avoid smoking near plants.",
                 "Use resistant tomato seeds."
             ),
             "tomato healthy" to listOf(
                 "Support plant with cage or stake.",
                 "Water at base early in day.",
                 "Apply compost monthly.",
                 "Watch for hornworms or pests."
             )
        )

    }
    data class Recognition(
        var id: String = "",
        var title: String = "",
        var confidence: Float = 0F,
        var remedy: String = ""
    ) {
        override fun toString(): String {
            return "Title = $title, Confidence = $confidence, Remedy = $remedy"
        }
    }

    init {
        interpreter = Interpreter(loadModelFile(assetManager, modelPath))
        labels = loadLabelList(assetManager, labelPath)
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }
    }

    fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)
        val byteBuffer = convertBitmapToByteBuffer(resized)
        val result = Array(1) { FloatArray(labels.size) }
        interpreter.run(byteBuffer, result)
        return getSortedResult(result)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * pixelSize)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)

        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val value = intValues[pixel++]
                byteBuffer.putFloat(((value shr 16 and 0xFF) - imageMean) / imageStd)
                byteBuffer.putFloat(((value shr 8 and 0xFF) - imageMean) / imageStd)
                byteBuffer.putFloat(((value and 0xFF) - imageMean) / imageStd)
            }
        }
        return byteBuffer
    }

    private fun getSortedResult(labelProbArray: Array<FloatArray>): List<Recognition> {
        val pq = PriorityQueue(
            maxResults,
            Comparator<Recognition> { o1, o2 ->
                java.lang.Float.compare(o2.confidence, o1.confidence)
            })

        for (i in labels.indices) {
            val confidence = labelProbArray[0][i]
            if (confidence >= threshold) {
                val title = if (labels.size > i) labels[i] else "Unknown"
                val remedy = remedies[title] ?: "No remedy available."
                pq.add(
                    Recognition(
                        id = "$i",
                        title = title,
                        confidence = confidence,
                        remedy = remedy.toString()
                    )
                )
            }
        }

        val recognitions = mutableListOf<Recognition>()
        val size = pq.size.coerceAtMost(maxResults)
        repeat(size) {
            recognitions.add(pq.poll())
        }

        return recognitions
    }
}
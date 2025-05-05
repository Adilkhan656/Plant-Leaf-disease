import android.content.Context
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.lang.reflect.Field
import java.lang.reflect.Modifier

fun ViewPager2.setSlowScroll(context: Context, duration: Int = 1000) {
    try {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView
        val layoutManager = recyclerView.layoutManager!!

        val scrollerField = layoutManager.javaClass.superclass!!.getDeclaredField("mSmoothScroller")
        scrollerField.isAccessible = true

        val interpolator = DecelerateInterpolator()

        val newScroller = object : androidx.recyclerview.widget.LinearSmoothScroller(context) {
            override fun onTargetFound(
                targetView: android.view.View,
                state: RecyclerView.State,
                action: Action
            ) {
                val dx = calculateDxToMakeVisible(targetView, horizontalSnapPreference)
                val dy = calculateDyToMakeVisible(targetView, verticalSnapPreference)
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toInt()
                val time = calculateTimeForDeceleration(distance)
                if (time > 0) {
                    action.update(dx, dy, time, DecelerateInterpolator())
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: android.util.DisplayMetrics): Float {
                return 1000f / displayMetrics.densityDpi // lower = slower
            }
        }


        scrollerField.set(layoutManager, newScroller)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

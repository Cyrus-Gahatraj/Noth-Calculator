import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import com.example.nothcalculator.CalculatorGestureListener
import kotlin.math.abs

class CalculatorGestureDetector(context: Context, private val listener: CalculatorGestureListener) :
    GestureDetector.OnGestureListener {

    private val gestureDetector: GestureDetector = GestureDetector(context, this)
    private var x1: Float = 0f
    private var y1: Float = 0f
    private companion object {
        const val MIN_DISTANT = 150
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        x1 = e.x
        y1 = e.y
        return true
    }


    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    override fun onLongPress(e: MotionEvent) {}


    override fun onShowPress(e: MotionEvent) {}
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean = false

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val x2 = e2.x
        val y2 = e2.y

        val valueX: Float = x2 - x1
        val valueY: Float = y2 - y1

        if (abs(valueY) > abs(valueX)) {
            if (valueY > MIN_DISTANT) {
                listener.onSwipeDown()
            }
        } else {
            if (valueX < -MIN_DISTANT) {
                listener.onSwipeLeft()
            }
        }
        return true
    }
}
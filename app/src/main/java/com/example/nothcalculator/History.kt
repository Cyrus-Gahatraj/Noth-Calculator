package com.example.nothcalculator


import android.content.Intent
import android.gesture.Gesture
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nothcalculator.MainActivity.Companion.MIN_DISTANT
import kotlin.math.abs
import kotlin.reflect.KProperty

class History : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var gestureDetector: GestureDetector
    private var x2:Float = 0.0f
    private var x1:Float = 0.0f
    private var y2:Float = 0.0f
    private var y1:Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        gestureDetector = GestureDetector(this,this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean{

        gestureDetector.onTouchEvent(event)

        when (event.action){

            //start swipe
            0->{
                x1 = event.x
                y1 = event.y
            }

            //stop swipe
            1->{
                x2 = event.x
                y2 = event.y

                val valueY:Float = y2-y1

                if (abs(valueY) > MIN_DISTANT){
                    if (y2 < y1){
                        Intent(this@History,MainActivity::class.java).also{
                            startActivity(it)
                        }
                    }
                }
            }
        }

        return super.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        //TODO("Not yet implemented")
        return false
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        // TODO("Not yet implemented")
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        // TODO("Not yet implemented")
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        //TODO("Not yet implemented")
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        //TODO("Not yet implemented")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        //TODO("Not yet implemented")
        return false
    }

}
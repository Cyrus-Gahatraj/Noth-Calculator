package com.example.nothcalculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import org.mariuszgromada.math.mxparser.Expression
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.abs
import android.content.Intent

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private var expression = ""
    private var openBracket = false
    private var isResultShown = false
    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView

    companion object{
        const val MIN_DISTANT = 150

    }

    // Gesture Detection
    private lateinit var gestureDetector: GestureDetector
    private var x2:Float = 0.0f
    private var x1:Float = 0.0f
    private var y2:Float = 0.0f
    private var y1:Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expressionView = findViewById(R.id.expressionView)
        resultView = findViewById(R.id.resultView)

        gestureDetector = GestureDetector(this, this)

    }



    fun onButtonClick(view: View){
        if (view is MaterialButton) {
            val buttonText = view.text.toString()

            when (buttonText) {

                getString(R.string.all_clear) -> {
                    expression = ""
                    expressionView.text = ""
                    resultView.text = ""
                    resultView.visibility = View.GONE
                    openBracket = false
                    isResultShown = false
                }

                getString(R.string.delete) -> {
                    if(!expression.isEmpty()){
                        expression = expression.dropLast(1)
                        expressionView.text = expression
                    }
                    resultView.visibility = View.GONE
                    isResultShown = false
                }

                getString(R.string.equals) -> {
                    calculateResult()

                }

                getString(R.string.brackets) -> {
                    expression += if (openBracket) ")" else { "(" }
                    openBracket = !openBracket
                    expressionView.text = expression
                    resultView.visibility = View.GONE
                    isResultShown = false
                }

                else ->{
                    if (isResultShown){
                        if (buttonText.toIntOrNull() != null){
                            expression = ""
                        }
                    }
                    expression += buttonText
                    expressionView.text = expression
                    resultView.visibility = View.GONE
                    isResultShown = false
                }
            }

            if (!isResultShown){
                expressionView.textSize = getString(R.string.textSize).toFloat()
            }
        }
    }


    @SuppressLint("SetTextI18n")
    fun calculateResult() {
        val exp = Expression(expression)
        val result = exp.calculate()

        if (result.isNaN()) {
            resultView.text = "Error"
        } else {
            var resultString = result.toString()
            if (ceil(result) == floor(result)) {
                resultString = result.toLong().toString()
            } else{

            }
            resultView.text = resultString
            expression = resultString
        }

        resultView.visibility = View.VISIBLE
        expressionView.textSize = 32.0f
        isResultShown = true

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

                val valueX:Float = x2-x1
                val valueY:Float = y2-y1

                if (abs(valueX) > MIN_DISTANT){
                    if(x2 < x1){
                        Toast.makeText(this, "Right", Toast.LENGTH_SHORT).show()
                        //TODO("Make the other calculator")
                    }
                } else if(abs(valueY) > MIN_DISTANT){
                    if (y2 > y1){
                        Intent(this@MainActivity,History::class.java).also{
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


package com.example.nothcalculator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import org.mariuszgromada.math.mxparser.Expression
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private var expression = ""
    private var openBracket = false
    private var isResultShown = false
    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView

    companion object {
        const val MIN_DISTANT = 150
    }

    // Gesture Detection
    private lateinit var gestureDetector: GestureDetector
    private var x1: Float = 0.0f
    private var y1: Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expressionView = findViewById(R.id.expressionView)
        resultView = findViewById(R.id.resultView)

        gestureDetector = GestureDetector(this, this)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun onButtonClick(view: View) {
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
                    if (expression.isNotEmpty()) {
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
                    expression += if (openBracket) ")" else "("
                    openBracket = !openBracket
                    expressionView.text = expression
                    resultView.visibility = View.GONE
                    isResultShown = false
                }

                else -> {
                    if (isResultShown && buttonText.toIntOrNull() != null) {
                        expression = ""
                    }
                    expression += buttonText
                    expressionView.text = expression
                    resultView.visibility = View.GONE
                    isResultShown = false
                }
            }

            if (!isResultShown) {
                expressionView.textSize = getString(R.string.textSize).toFloat()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun saveToDB(exp: String, result: String) {
        val db = HistoryDataBaseHelper(this)
        val date = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
        db.addCalculation(exp, result, date)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("SetTextI18n")
    private fun calculateResult() {
        val expObj = Expression(expression)
        val result = expObj.calculate()

        if (result.isNaN()) {
            resultView.text = "Error"
        } else {
            var resultString = result.toString()
            if (ceil(result) == floor(result)) {
                resultString = result.toLong().toString()
            }
            resultView.text = resultString
            expression = resultString

            saveToDB(expressionView.text.toString(), resultString)
        }

        resultView.visibility = View.VISIBLE
        expressionView.textSize = 32.0f
        isResultShown = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
                y1 = event.y
            }

            MotionEvent.ACTION_UP -> {
                val x2 = event.x
                val y2 = event.y

                val valueX: Float = x2 - x1
                val valueY: Float = y2 - y1

                if (abs(valueX) > MIN_DISTANT) {
                    if (x2 < x1) {
                        Toast.makeText(this, "Right", Toast.LENGTH_SHORT).show()
                        // TODO: Make the other calculator
                    }
                } else if (abs(valueY) > MIN_DISTANT) {
                    if (y2 > y1) {
                        Intent(this@MainActivity, History::class.java).also {
                            startActivity(it)
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean = false
    override fun onFling(
        e1: MotionEvent?, e2: MotionEvent,
        velocityX: Float, velocityY: Float
    ): Boolean = false

    override fun onLongPress(e: MotionEvent) {}
    override fun onScroll(
        e1: MotionEvent?, e2: MotionEvent,
        distanceX: Float, distanceY: Float
    ): Boolean = false

    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
}

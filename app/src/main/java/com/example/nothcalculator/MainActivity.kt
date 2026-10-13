package com.example.nothcalculator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import org.mariuszgromada.math.mxparser.Expression
import kotlin.math.ceil
import kotlin.math.floor

class MainActivity : AppCompatActivity(), CalculatorGestureListener {

    private var expression = ""
    private var isResultShown = false
    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView
    private lateinit var settingBtn: ImageButton

    private lateinit var gestureDetector: CalculatorGestureDetector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val calcDisplay = findViewById<ConstraintLayout>(R.id.calcDisplay)
        expressionView = calcDisplay.findViewById(R.id.expressionView)
        resultView = calcDisplay.findViewById(R.id.resultView)

        gestureDetector = CalculatorGestureDetector(this, this)

        settingBtn = calcDisplay.findViewById(R.id.btnSettings)
        settingBtn.setOnClickListener {
            Intent(this@MainActivity, Settings::class.java).also{
                startActivity(it)
            }
        }
    }

    fun onButtonClick(view: View) {
        if (view is MaterialButton) {
            val buttonText = view.text.toString()

            when (buttonText) {
                getString(R.string.all_clear) -> {
                    expression = ""
                    expressionView.text = ""
                    resultView.text = ""
                    resultView.visibility = View.GONE
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
                    val openCount = expression.count { it == '(' }
                    val closeCount = expression.count { it == ')' }
                    val bracket = if (openCount > closeCount && expression.isNotEmpty()) {
                        val last = expression.last()
                        if (last.isDigit() || last == ')' || last == '%') ")" else "("
                    } else {
                        "("
                    }
                    expression += bracket
                    expressionView.text = expression
                    resultView.visibility = View.GONE
                    isResultShown = false
                }

                else -> {
                    if (isResultShown && (buttonText.toIntOrNull() != null || resultView.text == "Error")) {
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

    private fun saveToDB(exp: String, result: String) {
        val db = HistoryDataBaseHelper(this)
        val date = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
        db.addCalculation(exp, result, date)
    }

    private fun normalizeExpression(exp: String): String {
        var s = exp
        val opens = s.count { it == '(' }
        val closes = s.count { it == ')' }
        if (opens > closes) {
            s += ")".repeat(opens - closes)
        }
        return s
    }

    @SuppressLint("SetTextI18n")
    private fun calculateResult() {
        if (expression == "") {
            return
        }
        val normalized = normalizeExpression(expression)
        val expObj = Expression(normalized)
        val result = expObj.calculate()

        if (result.isNaN() || result.isInfinite()) {
            resultView.text = "Error"
        } else {
            var resultString = result.toString()
            if (ceil(result) == floor(result) && kotlin.math.abs(result) < 1e15) {
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
        return super.onTouchEvent(event)
    }

    override fun onSwipeDown() {
        Intent(this@MainActivity, History::class.java).also{
            startActivity(it)
        }
    }

    override fun onSwipeLeft() {
        Intent(this@MainActivity, Modes::class.java).also{
            startActivity(it)
        }
    }


}
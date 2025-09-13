package com.example.nothcalculator

import CalculatorGestureDetector
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import org.mariuszgromada.math.mxparser.Expression
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.floor

class Scientific : AppCompatActivity(), CalculatorGestureListener {

    private lateinit var gestureDetector: CalculatorGestureDetector

    private var expression = ""

    private var isResultShown = false
    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView
    private lateinit var settingBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scientific)

        val calcDisplay = findViewById<ConstraintLayout>(R.id.calcDisplay)
        expressionView = calcDisplay.findViewById(R.id.expressionView)
        resultView = calcDisplay.findViewById(R.id.resultView)

        gestureDetector = CalculatorGestureDetector(this, this)

        settingBtn = calcDisplay.findViewById(R.id.btnSettings)
        settingBtn.setOnClickListener {
            startActivity(Intent(this@Scientific, Settings::class.java))
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("SetTextI18n") // Add this annotation
    fun onButtonClick(view: View) {
        if (view !is MaterialButton) return

        val rawText = view.text.toString()
        val buttonText = mapButtonToExpression(rawText)

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
            getString(R.string.equals) -> calculateResult()

            getString(R.string.answer) -> {
                if (resultView.text != "Error") {
                    if (isResultShown) {
                        expression = getString(R.string.answer)
                    } else {
                        expression += getString(R.string.answer)
                    }
                    expressionView.text = expression
                    resultView.visibility = View.GONE
                    isResultShown = false
                }
            }

            else -> {
                if (rawText == "±") {
                    toggleSign()
                } else if (isResultShown && rawText.toIntOrNull() != null) {
                    expression = buttonText
                } else {
                    expression += buttonText
                }
                expressionView.text = expression
                resultView.visibility = View.GONE
                isResultShown = false
            }
        }

        if (!isResultShown) {
            expressionView.textSize = getString(R.string.textSize).toFloat()
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
        expression = replace(expression)
        if (expression == ""){
            return
        }
        val normalized = normalizeExpression(expression)
        val expObj = Expression(normalized)
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


    private fun mapButtonToExpression(text: String): String {
        return when (text) {
            "sin" -> "sin("
            "cos" -> "cos("
            "tan" -> "tan("
            "log" -> "log10("
            "ln" -> "ln("
            "x^y" -> "^"
            "x²" -> "^2"
            else -> text
        }
    }

    fun replace(expression: String): String{
        var newExpression = expression.replace("mod", "#")
        newExpression = newExpression.replace(getString(R.string.answer), resultView.text.toString() )
        return newExpression
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

    private fun toggleSign() {
        if (expression.isEmpty()) {
            expression = "-"
            expressionView.text = expression
            return
        }

        val i = expression.lastIndex
        var start = i
        while (start >= 0 && (expression[start].isDigit() || expression[start] == '.')) {
            start--
        }
        val segmentStart = start + 1

        if (segmentStart <= i) {
            val seg = expression.substring(segmentStart..i)
            if (segmentStart >= 1 && expression[segmentStart - 1] == '-') {
                expression = expression.removeRange(segmentStart - 1, segmentStart) + seg
            } else {
                expression = expression.substring(0, segmentStart) + "-" + expression.substring(segmentStart)
            }
        } else {
            expression = "-" + expression
        }

        expressionView.text = expression
        resultView.visibility = View.GONE
        isResultShown = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onSwipeDown() {
        startActivity(Intent(this@Scientific, History::class.java))
    }

    override fun onSwipeLeft() {
        startActivity(Intent(this@Scientific, Modes::class.java))
    }
}

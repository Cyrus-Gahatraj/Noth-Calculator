package com.example.nothcalculator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.mXparser
import kotlin.math.ceil
import kotlin.math.floor

class Scientific : AppCompatActivity(), CalculatorGestureListener {

    private lateinit var gestureDetector: CalculatorGestureDetector

    private var expression = ""
    private var lastAnswer = "0"

    private var isResultShown = false
    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView
    private lateinit var expressionScrollView: HorizontalScrollView
    private lateinit var resultScrollView: HorizontalScrollView
    private lateinit var settingBtn: ImageButton
    private lateinit var modeBadge: TextView

    private val historyLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selected = result.data?.getStringExtra("selected_calculation")
            if (!selected.isNullOrEmpty()) {
                expression = selected
                updateExpressionDisplay()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scientific)

        val calcDisplay = findViewById<ConstraintLayout>(R.id.calcDisplay)
        expressionView = calcDisplay.findViewById(R.id.expressionView)
        resultView = calcDisplay.findViewById(R.id.resultView)
        expressionScrollView = calcDisplay.findViewById(R.id.expressionScrollView)
        resultScrollView = calcDisplay.findViewById(R.id.resultScrollView)

        expressionView.setOnClickListener {
            if (isResultShown) {
                vibrateClick(it)
                isResultShown = false
                resultScrollView.visibility = View.GONE
                expressionView.alpha = 1.0f
                expressionScrollView.post {
                    expressionScrollView.fullScroll(View.FOCUS_RIGHT)
                }
            }
        }

        resultView.setOnClickListener {
            if (isResultShown) {
                vibrateClick(it)
                val clean = resultView.text.toString().removePrefix("=").trim()
                if (clean != "Error") {
                    expression = clean
                    updateExpressionDisplay()
                }
            }
        }

        modeBadge = calcDisplay.findViewById(R.id.tvModeBadge)
        modeBadge.visibility = View.VISIBLE
        val settingsPrefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val initialAngle = settingsPrefs.getString("angle_unit", "DEG") ?: "DEG"
        modeBadge.text = initialAngle
        modeBadge.setOnClickListener {
            val newAngle = if (modeBadge.text == "DEG") "RAD" else "DEG"
            getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
                .putString("angle_unit", newAngle).apply()
            modeBadge.text = newAngle
            vibrateClick(it)
        }

        gestureDetector = CalculatorGestureDetector(this, this)

        settingBtn = calcDisplay.findViewById(R.id.btnSettings)
        settingBtn.setOnClickListener {
            startActivity(Intent(this@Scientific, Settings::class.java))
        }

        val initialCalc = intent?.getStringExtra("selected_calculation")
        if (!initialCalc.isNullOrEmpty()) {
            expression = initialCalc
            updateExpressionDisplay()
        }
    }

    override fun onResume() {
        super.onResume()
        val settingsPrefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        if (settingsPrefs.getBoolean("screen_on", false)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        val angleUnit = settingsPrefs.getString("angle_unit", "DEG") ?: "DEG"
        if (::modeBadge.isInitialized) {
            modeBadge.text = angleUnit
        }
    }

    @SuppressLint("SetTextI18n")
    fun onButtonClick(view: View) {
        if (view !is MaterialButton) return

        val settingsPrefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        if (settingsPrefs.getBoolean("vibration", true)) {
            vibrateClick(view)
        }

        val rawText = view.text.toString()
        val buttonText = mapButtonToExpression(rawText)

        when (buttonText) {
            getString(R.string.all_clear) -> {
                expression = ""
                resultView.text = ""
                updateExpressionDisplay()
            }
            getString(R.string.delete) -> {
                if (expression.isNotEmpty()) {
                    val tokens = listOf("sin(", "cos(", "tan(", "log10(", "ln(", "sqrt(", getString(R.string.answer))
                    val matchingToken = tokens.firstOrNull { expression.endsWith(it) }
                    expression = if (matchingToken != null) {
                        expression.dropLast(matchingToken.length)
                    } else {
                        expression.dropLast(1)
                    }
                }
                updateExpressionDisplay()
            }
            getString(R.string.equals) -> calculateResult()

            getString(R.string.answer) -> {
                if (isResultShown) {
                    expression = getString(R.string.answer)
                } else {
                    expression += getString(R.string.answer)
                }
                updateExpressionDisplay()
            }

            else -> {
                if (rawText == "±") {
                    toggleSign()
                } else {
                    if (isResultShown && (rawText.toIntOrNull() != null || resultView.text == "Error")) {
                        expression = buttonText
                    } else {
                        expression += buttonText
                    }
                    updateExpressionDisplay()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrateClick(view: View) {
        try {
            view.isHapticFeedbackEnabled = true
            view.performHapticFeedback(
                android.view.HapticFeedbackConstants.KEYBOARD_TAP,
                android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or android.view.HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(30L)
                }
            }
        } catch (_: Exception) {}
    }

    private fun updateExpressionDisplay() {
        expressionView.alpha = 1.0f
        expressionView.text = expression
        val len = expression.length
        val size = when {
            len <= 6 -> 54f
            len <= 9 -> 46f
            len <= 13 -> 38f
            len <= 17 -> 32f
            len <= 22 -> 26f
            else -> 22f
        }
        expressionView.textSize = size
        resultScrollView.visibility = View.GONE
        isResultShown = false
        expressionScrollView.post {
            expressionScrollView.fullScroll(View.FOCUS_RIGHT)
        }
    }

    private fun updateResultDisplay(resultString: String) {
        resultView.text = resultString
        val len = resultString.length
        val size = when {
            len <= 7 -> 44f
            len <= 11 -> 36f
            len <= 15 -> 30f
            len <= 20 -> 24f
            else -> 20f
        }
        resultView.textSize = size
        expressionView.textSize = 24f
        expressionView.alpha = 0.55f
        resultView.alpha = 1.0f
        resultScrollView.visibility = View.VISIBLE
        isResultShown = true
        resultScrollView.post {
            resultScrollView.fullScroll(View.FOCUS_RIGHT)
        }
        expressionScrollView.post {
            expressionScrollView.fullScroll(View.FOCUS_RIGHT)
        }
    }

    private fun saveToDB(exp: String, result: String) {
        val db = HistoryDataBaseHelper(this)
        val date = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
        db.addCalculation(exp, result, date)
    }

    @SuppressLint("SetTextI18n")
    private fun calculateResult() {
        expression = replace(expression)
        if (expression == ""){
            return
        }

        val settingsPrefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val angleUnit = settingsPrefs.getString("angle_unit", "DEG") ?: "DEG"
        if (angleUnit == "DEG") {
            mXparser.setDegreesMode()
        } else {
            mXparser.setRadiansMode()
        }

        val normalized = normalizeExpression(expression)
        val expObj = Expression(normalized)
        val result = expObj.calculate()

        if (result.isNaN() || result.isInfinite()) {
            updateResultDisplay("Error")
        } else {
            var resultString = result.toString()
            if (ceil(result) == floor(result) && kotlin.math.abs(result) < 1e15) {
                resultString = result.toLong().toString()
            }
            val currentExp = expressionView.text.toString()
            updateResultDisplay(resultString)
            expression = resultString
            lastAnswer = resultString

            saveToDB(currentExp, resultString)
        }
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
            "√" -> "sqrt("
            "π" -> "pi"
            else -> text
        }
    }

    fun replace(expression: String): String {
        var newExpression = expression.replace("mod", "#")
        newExpression = newExpression.replace(getString(R.string.answer), lastAnswer)
        newExpression = newExpression.replace("√", "sqrt(")
        newExpression = newExpression.replace("π", "pi")
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
        } else if (expression == "-") {
            expression = ""
        } else {
            val lastChar = expression.last()
            val operators = listOf('+', '-', '*', '/', '(', '^', '#', '×')

            if (lastChar == '-') {
                expression = if (expression.length >= 2 && expression[expression.length - 2] in operators) {
                    expression.dropLast(1)
                } else if (expression.length >= 2) {
                    expression.dropLast(1) + "+"
                } else {
                    ""
                }
            } else if (lastChar == '+') {
                expression = expression.dropLast(1) + "-"
            } else if (lastChar in operators) {
                expression = "$expression-"
            } else {
                var start = expression.lastIndex
                while (start >= 0 && (expression[start].isDigit() || expression[start] == '.')) {
                    start--
                }
                val numStart = start + 1
                expression = if (numStart > expression.lastIndex) {
                    "$expression-"
                } else if (numStart == 0) {
                    "-$expression"
                } else if (numStart == 1 && expression[0] == '-') {
                    expression.substring(1)
                } else if (numStart >= 2 && expression[numStart - 1] == '-' && expression[numStart - 2] in operators) {
                    expression.removeRange(numStart - 1, numStart)
                } else if (expression[numStart - 1] == '-') {
                    expression.substring(0, numStart - 1) + "+" + expression.substring(numStart)
                } else if (expression[numStart - 1] == '+') {
                    expression.substring(0, numStart - 1) + "-" + expression.substring(numStart)
                } else {
                    expression.substring(0, numStart) + "-" + expression.substring(numStart)
                }
            }
        }

        updateExpressionDisplay()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val selected = intent.getStringExtra("selected_calculation")
        if (!selected.isNullOrEmpty()) {
            expression = selected
            updateExpressionDisplay()
        }
    }

    override fun onSwipeDown() {
        val intent = Intent(this@Scientific, History::class.java).apply {
            putExtra("caller_activity", "Scientific")
        }
        historyLauncher.launch(intent)
    }

    override fun onSwipeLeft() {
        Intent(this@Scientific, Modes::class.java).apply {
            putExtra("current_mode", "Scientific")
        }.also {
            startActivity(it)
        }
    }
}

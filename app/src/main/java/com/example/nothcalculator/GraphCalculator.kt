package com.example.nothcalculator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class GraphCalculator : AppCompatActivity(), CalculatorGestureListener {

    private lateinit var graphView: GraphView
    private lateinit var etFunction: EditText
    private lateinit var gestureDetector: CalculatorGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        gestureDetector = CalculatorGestureDetector(this, this)

        val header = findViewById<HeadingView>(R.id.graphHeader)
        header.setHeading("GRAPH")
        header.setBackButton { finish() }

        graphView = findViewById(R.id.graphView)
        etFunction = findViewById(R.id.etFunction)

        etFunction.setText("sin(x)")
        graphView.setFunction("sin(x)")

        etFunction.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                plotCurrentFunction()
                true
            } else {
                false
            }
        }

        findViewById<View>(R.id.btnClearFunction).setOnClickListener {
            vibrateTap(it)
            etFunction.setText("")
        }

        findViewById<MaterialButton>(R.id.btnPlot).setOnClickListener {
            vibrateTap(it)
            plotCurrentFunction()
        }

        // Quick Function Preset Chips
        setupPresetChip(R.id.chipSin, "sin(x)")
        setupPresetChip(R.id.chipCos, "cos(x)")
        setupPresetChip(R.id.chipTan, "tan(x)")
        setupPresetChip(R.id.chipSquare, "x^2")
        setupPresetChip(R.id.chipCubic, "x^3 - 3*x")
        setupPresetChip(R.id.chipSqrt, "sqrt(x)")
        setupPresetChip(R.id.chipInverse, "1/x")
        setupPresetChip(R.id.chipExp, "exp(x)")
        setupPresetChip(R.id.chipAbs, "abs(x)")

        // Zoom Controls
        findViewById<MaterialButton>(R.id.btnZoomIn).setOnClickListener {
            vibrateTap(it)
            graphView.zoomIn()
        }

        findViewById<MaterialButton>(R.id.btnZoomOut).setOnClickListener {
            vibrateTap(it)
            graphView.zoomOut()
        }

        findViewById<MaterialButton>(R.id.btnResetZoom).setOnClickListener {
            vibrateTap(it)
            graphView.resetView()
        }
    }

    private fun setupPresetChip(chipId: Int, formula: String) {
        findViewById<MaterialButton>(chipId).setOnClickListener {
            vibrateTap(it)
            etFunction.setText(formula)
            graphView.setFunction(formula)
            hideKeyboard()
        }
    }

    private fun plotCurrentFunction() {
        hideKeyboard()
        val text = etFunction.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter a function of x", Toast.LENGTH_SHORT).show()
            return
        }
        graphView.setFunction(text)
        if (!graphView.isFunctionValid) {
            Toast.makeText(this, "Invalid function syntax", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        currentFocus?.let {
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun vibrateTap(view: View) {
        val settingsPrefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        if (settingsPrefs.getBoolean("vibration", true)) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // If event is outside graphView, allow gestureDetector to catch swipes
        val graphCoords = IntArray(2)
        graphView.getLocationOnScreen(graphCoords)
        val x = event.rawX
        val y = event.rawY
        val isInsideGraph = x >= graphCoords[0] && x <= graphCoords[0] + graphView.width &&
                y >= graphCoords[1] && y <= graphCoords[1] + graphView.height

        if (!isInsideGraph) {
            gestureDetector.onTouchEvent(event)
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onSwipeLeft() {
        Intent(this@GraphCalculator, Modes::class.java).apply {
            putExtra("current_mode", "Graph")
        }.also {
            startActivity(it)
        }
    }

    override fun onSwipeRight() {
        finish()
    }

    override fun onSwipeDown() {}
}

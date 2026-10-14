package com.example.nothcalculator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.UUID

class GraphCalculator : AppCompatActivity(), CalculatorGestureListener {

    private lateinit var graphView: GraphView
    private lateinit var functionsContainer: LinearLayout
    private lateinit var btnAddFunction: MaterialButton
    private lateinit var gestureDetector: CalculatorGestureDetector

    private val graphsList = mutableListOf<GraphItem>()
    private val colorPalette = intArrayOf(
        0xFFD71921.toInt(), // Nothing Red
        0xFFF8D332.toInt(), // Nothing Yellow
        0xFF38BDF8.toInt(), // Sky Blue
        0xFFA78BFA.toInt(), // Purple
        0xFFE2E4E8.toInt()  // Steel White
    )

    private val subscripts = arrayOf("₁", "₂", "₃", "₄", "₅")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        gestureDetector = CalculatorGestureDetector(this, this)

        val header = findViewById<HeadingView>(R.id.graphHeader)
        header.setHeading("GRAPH")
        header.setBackButton { finish() }

        graphView = findViewById(R.id.graphView)
        functionsContainer = findViewById(R.id.functionsContainer)
        btnAddFunction = findViewById(R.id.btnAddFunction)

        // Initial default function: y1 = sin(x)
        val initialItem = GraphItem(
            id = UUID.randomUUID().toString(),
            formula = "sin(x)",
            color = colorPalette[0]
        )
        graphsList.add(initialItem)
        graphView.setGraphs(graphsList)

        renderFunctionViews()

        btnAddFunction.setOnClickListener {
            vibrateTap(it)
            if (graphsList.size >= 5) {
                Toast.makeText(this, "Maximum 5 graphs supported", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nextColor = colorPalette[graphsList.size % colorPalette.size]
            val defaultFormulas = arrayOf("cos(x)", "x^2", "x^3 - 3*x", "1/x")
            val nextFormula = defaultFormulas.getOrElse(graphsList.size - 1) { "x" }
            val newItem = GraphItem(
                id = UUID.randomUUID().toString(),
                formula = nextFormula,
                color = nextColor
            )
            graphsList.add(newItem)
            graphView.setGraphs(graphsList)
            renderFunctionViews()
        }

        // Floating Zoom Controls on Top Canvas
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

    @SuppressLint("SetTextI18n")
    private fun renderFunctionViews() {
        functionsContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        graphsList.forEachIndexed { index, item ->
            val rowView = inflater.inflate(R.layout.item_graph_function, functionsContainer, false)

            val tvTag = rowView.findViewById<TextView>(R.id.tvFunctionTag)
            val etFormula = rowView.findViewById<EditText>(R.id.etFunctionFormula)
            val btnClear = rowView.findViewById<TextView>(R.id.btnClearText)
            val btnDelete = rowView.findViewById<TextView>(R.id.btnDeleteFunction)

            val sub = subscripts.getOrElse(index) { "${index + 1}" }
            tvTag.text = "y$sub"

            // Tag background with curve color
            val tagBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 14f * resources.displayMetrics.density
                setColor(item.color)
            }
            tvTag.background = tagBg
            tvTag.alpha = if (item.isVisible) 1.0f else 0.35f

            // Toggle visibility on tag tap
            tvTag.setOnClickListener {
                vibrateTap(it)
                val isVisible = graphView.toggleGraphVisibility(item.id)
                item.isVisible = isVisible
                tvTag.alpha = if (isVisible) 1.0f else 0.35f
            }

            etFormula.setText(item.formula)

            etFormula.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    val formula = s?.toString() ?: ""
                    item.formula = formula
                    graphView.updateGraph(item.id, formula)
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            etFormula.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard()
                    true
                } else {
                    false
                }
            }

            btnClear.setOnClickListener {
                vibrateTap(it)
                etFormula.setText("")
            }

            // Only allow deleting if more than 1 function exists
            if (graphsList.size > 1) {
                btnDelete.visibility = View.VISIBLE
                btnDelete.setOnClickListener {
                    vibrateTap(it)
                    graphsList.removeAt(index)
                    graphView.setGraphs(graphsList)
                    renderFunctionViews()
                }
            } else {
                btnDelete.visibility = View.GONE
            }

            functionsContainer.addView(rowView)
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
        // Allow gesture navigation when touch is outside graph canvas
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

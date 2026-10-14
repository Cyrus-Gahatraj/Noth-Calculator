package com.example.nothcalculator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class Modes : AppCompatActivity(), CalculatorGestureListener {

    private lateinit var defaultCard: MaterialCardView
    private lateinit var scientificCard: MaterialCardView
    private lateinit var emiCard: MaterialCardView
    private lateinit var graphCard: MaterialCardView
    private lateinit var gestureDetector: CalculatorGestureDetector

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modes)

        gestureDetector = CalculatorGestureDetector(this, this)

        val header = findViewById<HeadingView>(R.id.modeHeader)
        header.setHeading("Modes")
        header.setBackButton { finish() }

        val currentMode = intent.getStringExtra("current_mode") ?: "Default"
        val dotDefault = findViewById<View>(R.id.dotActiveDefault)
        val dotScientific = findViewById<View>(R.id.dotActiveScientific)
        val dotEmi = findViewById<View>(R.id.dotActiveEmi)
        val dotGraph = findViewById<View>(R.id.dotActiveGraph)

        dotDefault.visibility = if (currentMode.equals("Default", ignoreCase = true)) View.VISIBLE else View.GONE
        dotScientific.visibility = if (currentMode.equals("Scientific", ignoreCase = true)) View.VISIBLE else View.GONE
        dotEmi.visibility = if (currentMode.equals("EMI", ignoreCase = true)) View.VISIBLE else View.GONE
        dotGraph.visibility = if (currentMode.equals("Graph", ignoreCase = true)) View.VISIBLE else View.GONE

        defaultCard = findViewById(R.id.defaultCalc)
        defaultCard.setOnClickListener {
            vibrateTap(it)
            val intent = Intent(this@Modes, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("switched_mode", true)
            }
            startActivity(intent)
            finish()
        }

        scientificCard = findViewById(R.id.scientificCalc)
        scientificCard.setOnClickListener {
            vibrateTap(it)
            val intent = Intent(this@Modes, Scientific::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        emiCard = findViewById(R.id.emiCalc)
        emiCard.setOnClickListener {
            vibrateTap(it)
            val intent = Intent(this@Modes, EmiCalculator::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        graphCard = findViewById(R.id.graphCalc)
        graphCard.setOnClickListener {
            vibrateTap(it)
            val intent = Intent(this@Modes, GraphCalculator::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun vibrateTap(view: View) {
        val settingsPrefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        if (settingsPrefs.getBoolean("vibration", true)) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onSwipeLeft() {
        finish()
    }

    override fun onSwipeRight() {
        finish()
    }

    override fun onSwipeDown() {}
}
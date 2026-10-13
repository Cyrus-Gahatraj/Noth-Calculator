package com.example.nothcalculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class History : AppCompatActivity(), CalculatorGestureListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyHistoryView: View
    private lateinit var clearButton: Button
    private lateinit var db: HistoryDataBaseHelper
    private lateinit var gestureDetector: CalculatorGestureDetector

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        gestureDetector = CalculatorGestureDetector(this, this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        emptyHistoryView = findViewById(R.id.emptyHistoryView)
        clearButton = findViewById(R.id.clearAllButton)

        db = HistoryDataBaseHelper(this@History)
        val listOfCalculationHistory = db.getHistory()

        if (listOfCalculationHistory.isEmpty()) {
            emptyHistoryView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            clearButton.visibility = View.GONE
        } else {
            emptyHistoryView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            clearButton.visibility = View.VISIBLE

            val groupedHistory = listOfCalculationHistory
                .groupBy { it.date }
                .map { (date, calculations) -> HistoryGroup(date, calculations) }

            recyclerView.adapter = CalculationGroupAdapter(groupedHistory)
        }

        // Header setup
        val header = findViewById<HeadingView>(R.id.historyHeader)
        header.setHeading("History")
        header.setBackButton { finish() }

        clearButton.setOnClickListener {
            val settingsPrefs = getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE)
            if (settingsPrefs.getBoolean("vibration", true)) {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            }
            db.clearHistory()
            recyclerView.adapter = CalculationGroupAdapter(emptyList())
            emptyHistoryView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            clearButton.visibility = View.GONE
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

    override fun onSwipeDown() {
    }
}

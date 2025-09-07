package com.example.nothcalculator


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility

import com.example.nothcalculator.MainActivity.Companion.MIN_DISTANT
import kotlin.math.abs

class History : AppCompatActivity(), GestureDetector.OnGestureListener {

    // Recycle view
    private val listOfCalculations = mutableListOf<CalculationHistory>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: CalculationAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    // Database
    private lateinit var db: HistoryDataBaseHelper

    private lateinit var gestureDetector: GestureDetector
    private var x2:Float = 0.0f
    private var x1:Float = 0.0f
    private var y2:Float = 0.0f
    private var y1:Float = 0.0f

    // Buttons
    private lateinit var backButton: TextView
    private lateinit var clearButton: Button

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        gestureDetector = GestureDetector(this, this)

        viewManager = LinearLayoutManager(this)
        viewAdapter = CalculationAdapter(listOfCalculations)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        db = HistoryDataBaseHelper(this@History)
        val listOfCalculationHistory = db.getHistory()


        listOfCalculations.clear()
        listOfCalculations.addAll(listOfCalculationHistory)
        viewAdapter.notifyDataSetChanged()

        backButton = findViewById(R.id.backButton)
        clearButton = findViewById(R.id.clearAllButton)

        if (listOfCalculations.size == 0){
            clearButton.visibility = View.GONE
        }

        backButton.setOnClickListener {
            finish()
        }

        clearButton.setOnClickListener {
            db.clearHistory()
            listOfCalculations.clear()
            viewAdapter.notifyDataSetChanged()
        }
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
                        finish()
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
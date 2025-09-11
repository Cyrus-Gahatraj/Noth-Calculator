package com.example.nothcalculator


import android.annotation.SuppressLint
import android.content.Context
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

import com.example.nothcalculator.MainActivity.Companion.MIN_DISTANT
import kotlin.math.abs



class History : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var recyclerView: RecyclerView

    // Database
    private lateinit var db: HistoryDataBaseHelper

    private lateinit var gestureDetector: GestureDetector
    private var x2:Float = 0.0f
    private var x1:Float = 0.0f
    private var y2:Float = 0.0f
    private var y1:Float = 0.0f


    private lateinit var clearButton: Button


    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        gestureDetector = GestureDetector(this, this)

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Database setup
        db = HistoryDataBaseHelper(this@History)
        val listOfCalculationHistory = db.getHistory()

        val groupedHistory = listOfCalculationHistory
            .groupBy { it.date }
            .map { (date, calculations) -> HistoryGroup(date, calculations) }

        val groupAdapter = CalculationGroupAdapter(groupedHistory)
        recyclerView.adapter = groupAdapter

        // Header setup
        val header = findViewById<HeadingView>(R.id.historyHeader)
        header.setHeading("History")
        header.setBackButton { finish() }

        clearButton = findViewById(R.id.clearAllButton)

        if (listOfCalculationHistory.isEmpty()) {
            clearButton.visibility = View.GONE
        }

        clearButton.setOnClickListener {
            db.clearHistory()
            recyclerView.adapter = CalculationGroupAdapter(emptyList())
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




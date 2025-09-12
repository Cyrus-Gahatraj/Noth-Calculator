package com.example.nothcalculator


import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class History : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    // Database
    private lateinit var db: HistoryDataBaseHelper


    private lateinit var clearButton: Button


    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

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

}




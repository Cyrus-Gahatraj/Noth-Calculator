package com.example.nothcalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalculationAdapter(private val listOfCalculation: MutableList<CalculationHistory>) :
    RecyclerView.Adapter<CalculationAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val expression: TextView
        val result: TextView

        init {
            expression = view.findViewById(R.id.expressionText)
            result = view.findViewById(R.id.resultText)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.history_layout, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val calculation = listOfCalculation[position]

        viewHolder.expression.text = calculation.expression
        viewHolder.result.text = calculation.result
    }

    override fun getItemCount() = listOfCalculation.size

}
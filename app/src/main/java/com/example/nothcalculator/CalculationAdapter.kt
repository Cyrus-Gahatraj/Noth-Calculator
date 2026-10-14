package com.example.nothcalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalculationAdapter(
    private val listOfCalculation: MutableList<CalculationHistory>,
    private val onItemClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<CalculationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val expression: TextView = view.findViewById(R.id.expressionText)
        val result: TextView = view.findViewById(R.id.resultText)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.history_layout, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val calculation = listOfCalculation[position]

        viewHolder.expression.text = calculation.expression
        viewHolder.result.text = if (calculation.result.startsWith("=")) calculation.result else "= ${calculation.result}"

        val cleanResult = calculation.result.removePrefix("=").trim()

        viewHolder.result.setOnClickListener {
            onItemClick?.invoke(cleanResult)
        }

        viewHolder.expression.setOnClickListener {
            onItemClick?.invoke(calculation.expression)
        }

        viewHolder.itemView.setOnClickListener {
            onItemClick?.invoke(cleanResult)
        }
    }

    override fun getItemCount() = listOfCalculation.size
}
package com.example.nothcalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class CalculationGroupAdapter(
    private val groups: List<HistoryGroup>
) : RecyclerView.Adapter<CalculationGroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateHeader: TextView = view.findViewById(R.id.dateHeader)
        val calculationList: RecyclerView = view.findViewById(R.id.calculationList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]

        holder.dateHeader.text = group.date

        holder.calculationList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CalculationAdapter(group.calculations.toMutableList())
            isNestedScrollingEnabled = false
        }
    }

    override fun getItemCount(): Int = groups.size
}

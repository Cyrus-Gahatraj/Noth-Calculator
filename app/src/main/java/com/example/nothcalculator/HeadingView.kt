package com.example.nothcalculator

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class HeadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr) {

    private val headingText: TextView
    private val backButton: TextView

    init{
        inflate(context, R.layout.heading_view, this)
        headingText = findViewById<TextView>(R.id.headingText)
        backButton = findViewById<TextView>(R.id.backButton)
    }

    fun setHeading(text: String){
        headingText.text = text
    }

    fun setBackButton(action: ()->Unit){
        backButton.setOnClickListener { action() }
    }
}


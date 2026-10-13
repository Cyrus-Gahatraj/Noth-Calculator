package com.example.nothcalculator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class Modes : AppCompatActivity() {

    private lateinit var default: CardView
    private lateinit var scientific: CardView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modes)

        val header = findViewById<HeadingView>(R.id.modeHeader)
        header.setHeading("Mode")
        header.setBackButton { finish() }

        default = findViewById(R.id.defaultCalc)
        default.setOnClickListener {
            val intent = Intent(this@Modes, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        scientific = findViewById(R.id.scientificCalc)
        scientific.setOnClickListener {
            val intent = Intent(this@Modes, Scientific::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }
}
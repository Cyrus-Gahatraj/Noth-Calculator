package com.example.nothcalculator

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class NothCalculatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this) // Initialize ThreeTenBP
    }
}
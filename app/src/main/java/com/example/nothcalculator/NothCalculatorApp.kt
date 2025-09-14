package com.example.nothcalculator

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.jakewharton.threetenabp.AndroidThreeTen

class NothCalculatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this) 
        applySavedTheme()
    }
    
    private fun applySavedTheme() {
        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean("night", false)
        
        val nightMode = if (isNightMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
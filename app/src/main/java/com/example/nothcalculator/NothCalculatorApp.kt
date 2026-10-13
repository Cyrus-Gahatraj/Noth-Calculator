package com.example.nothcalculator

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.jakewharton.threetenabp.AndroidThreeTen
import org.mariuszgromada.math.mxparser.License

class NothCalculatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        License.iConfirmNonCommercialUse("Noth Calculator")
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
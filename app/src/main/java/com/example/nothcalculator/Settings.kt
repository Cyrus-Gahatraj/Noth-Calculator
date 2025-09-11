package com.example.nothcalculator

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class Settings : AppCompatActivity() {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var modeSwitch: Switch

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val heading = findViewById<HeadingView>(R.id.settingsHeader)
        heading.setHeading("Settings")
        heading.setBackButton{ finish() }

        modeSwitch = findViewById(R.id.modeSwitch)

        val sharePreference = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val editor = sharePreference.edit()
        val nightMode = sharePreference.getBoolean("night", false)

        if(nightMode) {
            modeSwitch.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        modeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

            if (!isChecked){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor.putBoolean("night", false)
                editor.apply()

            } else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean("night", true)
                editor.apply()
            }
        }

    }
}
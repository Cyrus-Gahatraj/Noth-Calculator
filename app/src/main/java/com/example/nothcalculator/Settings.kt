package com.example.nothcalculator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Settings : AppCompatActivity() {

    private lateinit var modeSwitch: SwitchCompat
    private lateinit var screenOnSwitch: SwitchCompat
    private lateinit var vibrationSwitch: SwitchCompat
    private lateinit var angleUnitText: TextView
    private lateinit var startupModeText: TextView
    private lateinit var historyCountSubtitle: TextView

    private lateinit var db: HistoryDataBaseHelper

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        db = HistoryDataBaseHelper(this)

        val heading = findViewById<HeadingView>(R.id.settingsHeader)
        heading.setHeading("Settings")
        heading.setBackButton { finish() }

        // Mode SharedPreferences (Dark mode)
        val modePrefs = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val modeEditor = modePrefs.edit()

        // General Settings SharedPreferences
        val settingsPrefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val settingsEditor = settingsPrefs.edit()

        // Appearance
        modeSwitch = findViewById(R.id.modeSwitch)
        val nightMode = modePrefs.getBoolean("night", false)
        modeSwitch.isChecked = nightMode

        findViewById<View>(R.id.rowDarkMode).setOnClickListener {
            modeSwitch.isChecked = !modeSwitch.isChecked
        }

        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                modeEditor.putBoolean("night", false).apply()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                modeEditor.putBoolean("night", true).apply()
            }
        }

        // Screen On
        screenOnSwitch = findViewById(R.id.screenOnSwitch)
        val screenOn = settingsPrefs.getBoolean("screen_on", false)
        screenOnSwitch.isChecked = screenOn

        findViewById<View>(R.id.rowScreenOn).setOnClickListener {
            screenOnSwitch.isChecked = !screenOnSwitch.isChecked
        }

        screenOnSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsEditor.putBoolean("screen_on", isChecked).apply()
        }

        // Vibration
        vibrationSwitch = findViewById(R.id.vibrationSwitch)
        val vibrationEnabled = settingsPrefs.getBoolean("vibration", true)
        vibrationSwitch.isChecked = vibrationEnabled

        findViewById<View>(R.id.rowVibration).setOnClickListener {
            vibrationSwitch.isChecked = !vibrationSwitch.isChecked
        }

        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsEditor.putBoolean("vibration", isChecked).apply()
        }

        // Angle Unit (DEG / RAD)
        angleUnitText = findViewById(R.id.angleUnitText)
        var currentAngleUnit = settingsPrefs.getString("angle_unit", "DEG") ?: "DEG"
        angleUnitText.text = currentAngleUnit

        findViewById<View>(R.id.rowAngleUnit).setOnClickListener {
            currentAngleUnit = if (currentAngleUnit == "DEG") "RAD" else "DEG"
            settingsEditor.putString("angle_unit", currentAngleUnit).apply()
            angleUnitText.text = currentAngleUnit
            Toast.makeText(this, "Angle unit set to $currentAngleUnit", Toast.LENGTH_SHORT).show()
        }

        // Startup Mode
        startupModeText = findViewById(R.id.startupModeText)
        var currentStartupMode = settingsPrefs.getString("startup_mode", "Default") ?: "Default"
        startupModeText.text = currentStartupMode

        findViewById<View>(R.id.rowStartupMode).setOnClickListener {
            currentStartupMode = if (currentStartupMode == "Default") "Scientific" else "Default"
            settingsEditor.putString("startup_mode", currentStartupMode).apply()
            startupModeText.text = currentStartupMode
            Toast.makeText(this, "Startup mode set to $currentStartupMode", Toast.LENGTH_SHORT).show()
        }

        // History count and clear
        historyCountSubtitle = findViewById(R.id.historyCountSubtitle)
        updateHistoryCount()

        findViewById<View>(R.id.rowClearHistory).setOnClickListener {
            val count = db.getHistoryCount()
            if (count == 0) {
                Toast.makeText(this, "History is already empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            MaterialAlertDialogBuilder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to delete all $count saved calculations?")
                .setPositiveButton("Clear") { _, _ ->
                    db.clearHistory()
                    updateHistoryCount()
                    Toast.makeText(this, "Calculation history cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Version subtitle
        val versionSubtitle = findViewById<TextView>(R.id.versionSubtitle)
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (_: Exception) {
            "0.4.4"
        }
        versionSubtitle.text = "Version $versionName • Nothing OS aesthetic"

        // GitHub Row
        findViewById<View>(R.id.rowGithub).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Cyrus-Gahatraj/Noth-Calculator"))
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateHistoryCount() {
        val count = db.getHistoryCount()
        historyCountSubtitle.text = "$count calculation${if (count == 1) "" else "s"} saved"
    }

    override fun onResume() {
        super.onResume()
        updateHistoryCount()
    }
}
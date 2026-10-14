package com.example.nothcalculator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Settings : AppCompatActivity() {

    private lateinit var themeModeText: TextView
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

        // Mode SharedPreferences (Theme)
        val modePrefs = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val modeEditor = modePrefs.edit()

        // General Settings SharedPreferences
        val settingsPrefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val settingsEditor = settingsPrefs.edit()

        // --- Theme (System, Dark, Light) ---
        themeModeText = findViewById(R.id.themeModeText)
        var currentThemeMode = modePrefs.getString("theme_mode", "system") ?: "system"
        updateThemeText(currentThemeMode)

        val themeNames = arrayOf("System Default", "Dark", "Light")
        val themeValues = arrayOf("system", "dark", "light")

        findViewById<View>(R.id.rowTheme).setOnClickListener {
            val selectedIndex = when (currentThemeMode) {
                "dark" -> 1
                "light" -> 2
                else -> 0
            }

            MaterialAlertDialogBuilder(this)
                .setTitle("Choose Theme")
                .setSingleChoiceItems(themeNames, selectedIndex) { dialog, which ->
                    val chosenMode = themeValues[which]
                    currentThemeMode = chosenMode
                    modeEditor.putString("theme_mode", chosenMode)
                    // Keep backwards compatibility with boolean "night"
                    modeEditor.putBoolean("night", chosenMode == "dark")
                    modeEditor.apply()

                    updateThemeText(chosenMode)

                    val nightMode = when (chosenMode) {
                        "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                        "light" -> AppCompatDelegate.MODE_NIGHT_NO
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                    AppCompatDelegate.setDefaultNightMode(nightMode)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // --- Keep Screen On ---
        screenOnSwitch = findViewById(R.id.screenOnSwitch)
        val screenOn = settingsPrefs.getBoolean("screen_on", false)
        screenOnSwitch.isChecked = screenOn

        findViewById<View>(R.id.rowScreenOn).setOnClickListener {
            screenOnSwitch.isChecked = !screenOnSwitch.isChecked
        }

        screenOnSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsEditor.putBoolean("screen_on", isChecked).apply()
        }

        // --- Vibration on Keypress ---
        vibrationSwitch = findViewById(R.id.vibrationSwitch)
        val vibrationEnabled = settingsPrefs.getBoolean("vibration", true)
        vibrationSwitch.isChecked = vibrationEnabled

        findViewById<View>(R.id.rowVibration).setOnClickListener {
            vibrationSwitch.isChecked = !vibrationSwitch.isChecked
        }

        vibrationSwitch.setOnCheckedChangeListener { view, isChecked ->
            settingsEditor.putBoolean("vibration", isChecked).apply()
            if (isChecked) {
                vibrateClick(view)
            }
        }

        // --- Angle Unit (DEG / RAD) ---
        angleUnitText = findViewById(R.id.angleUnitText)
        var currentAngleUnit = settingsPrefs.getString("angle_unit", "DEG") ?: "DEG"
        angleUnitText.text = currentAngleUnit

        findViewById<View>(R.id.rowAngleUnit).setOnClickListener {
            currentAngleUnit = if (currentAngleUnit == "DEG") "RAD" else "DEG"
            settingsEditor.putString("angle_unit", currentAngleUnit).apply()
            angleUnitText.text = currentAngleUnit
            Toast.makeText(this, "Angle unit set to $currentAngleUnit", Toast.LENGTH_SHORT).show()
        }

        // --- Startup Mode (Default / Scientific / EMI / Graph) ---
        startupModeText = findViewById(R.id.startupModeText)
        var currentStartupMode = settingsPrefs.getString("startup_mode", "Default") ?: "Default"
        startupModeText.text = currentStartupMode

        val modeDisplayNames = arrayOf("Default (Standard)", "Scientific", "EMI Calculator", "Graphing Calculator")
        val modeKeyValues = arrayOf("Default", "Scientific", "EMI", "Graph")

        findViewById<View>(R.id.rowStartupMode).setOnClickListener {
            val currentIndex = modeKeyValues.indexOf(currentStartupMode).let { if (it >= 0) it else 0 }
            MaterialAlertDialogBuilder(this)
                .setTitle("Startup Mode")
                .setSingleChoiceItems(modeDisplayNames, currentIndex) { dialog, which ->
                    val chosen = modeKeyValues[which]
                    currentStartupMode = chosen
                    settingsEditor.putString("startup_mode", chosen).apply()
                    startupModeText.text = chosen
                    Toast.makeText(this, "Startup mode set to $chosen", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // --- Clear History ---
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

        // --- Version subtitle ---
        val versionSubtitle = findViewById<TextView>(R.id.versionSubtitle)
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (_: Exception) {
            "0.4.10"
        }
        versionSubtitle.text = "Version $versionName • Nothing OS aesthetic"

        // --- Welcome Guide Row ---
        findViewById<View>(R.id.rowOnboarding).setOnClickListener {
            val intent = Intent(this, OnboardingActivity::class.java).apply {
                putExtra("from_settings", true)
            }
            startActivity(intent)
        }

        // --- GitHub Row ---
        findViewById<View>(R.id.rowGithub).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Cyrus-Gahatraj/Noth-Calculator"))
            startActivity(intent)
        }
    }

    private fun updateThemeText(mode: String) {
        themeModeText.text = when (mode) {
            "dark" -> "Dark"
            "light" -> "Light"
            else -> "System Default"
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrateClick(view: View) {
        try {
            view.isHapticFeedbackEnabled = true
            view.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(30L)
                }
            }
        } catch (_: Exception) {}
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
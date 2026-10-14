package com.example.nothcalculator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: MaterialButton
    private lateinit var btnSkip: TextView
    private lateinit var dot0: ImageView
    private lateinit var dot1: ImageView
    private lateinit var dot2: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.onboardingViewPager)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
        dot0 = findViewById(R.id.dot0)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)

        viewPager.adapter = OnboardingAdapter()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)
            }
        })

        btnNext.setOnClickListener {
            vibrateTap(it)
            val current = viewPager.currentItem
            if (current < 2) {
                viewPager.setCurrentItem(current + 1, true)
            } else {
                finishOnboarding()
            }
        }

        btnSkip.setOnClickListener {
            vibrateTap(it)
            finishOnboarding()
        }
    }

    private fun updateIndicators(position: Int) {
        when (position) {
            0 -> {
                dot0.setImageResource(R.drawable.indicator_dot_active_red)
                dot1.setImageResource(R.drawable.indicator_dot_inactive)
                dot2.setImageResource(R.drawable.indicator_dot_inactive)
                btnNext.text = "NEXT"
            }
            1 -> {
                dot0.setImageResource(R.drawable.indicator_dot_inactive)
                dot1.setImageResource(R.drawable.indicator_dot_active_yellow)
                dot2.setImageResource(R.drawable.indicator_dot_inactive)
                btnNext.text = "NEXT"
            }
            2 -> {
                dot0.setImageResource(R.drawable.indicator_dot_inactive)
                dot1.setImageResource(R.drawable.indicator_dot_inactive)
                dot2.setImageResource(R.drawable.indicator_dot_active_red)
                btnNext.text = "GET STARTED"
            }
        }
    }

    private fun finishOnboarding() {
        val settingsPrefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        settingsPrefs.edit().putBoolean("onboarding_completed", true).apply()

        val isFromSettings = intent.getBooleanExtra("from_settings", false)
        if (!isFromSettings) {
            val intent = Intent(this@OnboardingActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }
        finish()
    }

    private fun vibrateTap(view: View) {
        val settingsPrefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        if (settingsPrefs.getBoolean("vibration", true)) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
}

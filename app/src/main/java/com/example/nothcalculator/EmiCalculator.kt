package com.example.nothcalculator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.text.DecimalFormat
import kotlin.math.pow

class EmiCalculator : AppCompatActivity(), CalculatorGestureListener {

    private lateinit var etPrincipal: EditText
    private lateinit var etInterestRate: EditText
    private lateinit var etTenure: EditText

    private lateinit var btnTenureYears: TextView
    private lateinit var btnTenureMonths: TextView
    private lateinit var tvTenureUnitIcon: TextView

    private lateinit var tvEmiAmount: TextView
    private lateinit var tvTotalPrincipal: TextView
    private lateinit var tvTotalInterest: TextView
    private lateinit var tvTotalPayable: TextView

    private lateinit var viewPrincipalBar: View
    private lateinit var viewInterestBar: View

    private lateinit var gestureDetector: CalculatorGestureDetector

    private var isTenureInYears = true
    private val currencyFormat = DecimalFormat("#,##0.00")
    private val wholeFormat = DecimalFormat("#,##0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emi)

        gestureDetector = CalculatorGestureDetector(this, this)

        val header = findViewById<HeadingView>(R.id.emiHeader)
        header.setHeading("EMI")
        header.setBackButton { finish() }

        initViews()
        setupListeners()
        calculateEmi()
    }

    private fun initViews() {
        etPrincipal = findViewById(R.id.etPrincipal)
        etInterestRate = findViewById(R.id.etInterestRate)
        etTenure = findViewById(R.id.etTenure)

        btnTenureYears = findViewById(R.id.btnTenureYears)
        btnTenureMonths = findViewById(R.id.btnTenureMonths)
        tvTenureUnitIcon = findViewById(R.id.tvTenureUnitIcon)

        tvEmiAmount = findViewById(R.id.tvEmiAmount)
        tvTotalPrincipal = findViewById(R.id.tvTotalPrincipal)
        tvTotalInterest = findViewById(R.id.tvTotalInterest)
        tvTotalPayable = findViewById(R.id.tvTotalPayable)

        viewPrincipalBar = findViewById(R.id.viewPrincipalBar)
        viewInterestBar = findViewById(R.id.viewInterestBar)

        // Set default values
        etPrincipal.setText("100000")
        etInterestRate.setText("8.5")
        etTenure.setText("5")
    }

    private fun setupListeners() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                calculateEmi()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etPrincipal.addTextChangedListener(watcher)
        etInterestRate.addTextChangedListener(watcher)
        etTenure.addTextChangedListener(watcher)

        // Principal quick chips
        findViewById<MaterialButton>(R.id.chipAdd10k).setOnClickListener {
            vibrateTap(it)
            addPrincipal(10000.0)
        }
        findViewById<MaterialButton>(R.id.chipAdd50k).setOnClickListener {
            vibrateTap(it)
            addPrincipal(50000.0)
        }
        findViewById<MaterialButton>(R.id.chipAdd100k).setOnClickListener {
            vibrateTap(it)
            addPrincipal(100000.0)
        }
        findViewById<MaterialButton>(R.id.chipClearPrincipal).setOnClickListener {
            vibrateTap(it)
            etPrincipal.setText("")
        }

        // Rate quick chips
        findViewById<MaterialButton>(R.id.chipRate75).setOnClickListener {
            vibrateTap(it)
            etInterestRate.setText("7.5")
        }
        findViewById<MaterialButton>(R.id.chipRate85).setOnClickListener {
            vibrateTap(it)
            etInterestRate.setText("8.5")
        }
        findViewById<MaterialButton>(R.id.chipRate10).setOnClickListener {
            vibrateTap(it)
            etInterestRate.setText("10.0")
        }
        findViewById<MaterialButton>(R.id.chipRate12).setOnClickListener {
            vibrateTap(it)
            etInterestRate.setText("12.0")
        }

        // Tenure unit toggle
        btnTenureYears.setOnClickListener {
            if (!isTenureInYears) {
                vibrateTap(it)
                setTenureUnit(true)
            }
        }
        btnTenureMonths.setOnClickListener {
            if (isTenureInYears) {
                vibrateTap(it)
                setTenureUnit(false)
            }
        }

        // Tenure quick chips
        findViewById<MaterialButton>(R.id.chipTenure1).setOnClickListener {
            vibrateTap(it)
            etTenure.setText(if (isTenureInYears) "1" else "12")
        }
        findViewById<MaterialButton>(R.id.chipTenure3).setOnClickListener {
            vibrateTap(it)
            etTenure.setText(if (isTenureInYears) "3" else "36")
        }
        findViewById<MaterialButton>(R.id.chipTenure5).setOnClickListener {
            vibrateTap(it)
            etTenure.setText(if (isTenureInYears) "5" else "60")
        }
        findViewById<MaterialButton>(R.id.chipTenure10).setOnClickListener {
            vibrateTap(it)
            etTenure.setText(if (isTenureInYears) "10" else "120")
        }
        findViewById<MaterialButton>(R.id.chipTenure20).setOnClickListener {
            vibrateTap(it)
            etTenure.setText(if (isTenureInYears) "20" else "240")
        }

        // Action buttons
        findViewById<MaterialButton>(R.id.btnCalculate).setOnClickListener {
            vibrateTap(it)
            hideKeyboard()
            calculateEmi()
        }

        findViewById<MaterialButton>(R.id.btnReset).setOnClickListener {
            vibrateTap(it)
            etPrincipal.setText("100000")
            etInterestRate.setText("8.5")
            etTenure.setText("5")
            setTenureUnit(true)
            calculateEmi()
        }
    }

    private fun addPrincipal(amount: Double) {
        val current = etPrincipal.text.toString().toDoubleOrNull() ?: 0.0
        val updated = current + amount
        etPrincipal.setText(wholeFormat.format(updated).replace(",", ""))
    }

    private fun setTenureUnit(years: Boolean) {
        isTenureInYears = years
        val currentTenure = etTenure.text.toString().toDoubleOrNull()
        if (years) {
            btnTenureYears.setBackgroundColor(getColor(R.color.red))
            btnTenureYears.setTextColor(getColor(R.color.full_white))
            btnTenureMonths.setBackgroundResource(0)
            btnTenureMonths.setTextColor(getColor(R.color.settings_subtitle_dark))
            if (currentTenure != null && currentTenure > 0) {
                val convertedYears = (currentTenure / 12.0).toInt().coerceAtLeast(1)
                etTenure.setText(convertedYears.toString())
            }
            findViewById<MaterialButton>(R.id.chipTenure1).text = "1Y"
            findViewById<MaterialButton>(R.id.chipTenure3).text = "3Y"
            findViewById<MaterialButton>(R.id.chipTenure5).text = "5Y"
            findViewById<MaterialButton>(R.id.chipTenure10).text = "10Y"
            findViewById<MaterialButton>(R.id.chipTenure20).text = "20Y"
        } else {
            btnTenureMonths.setBackgroundColor(getColor(R.color.red))
            btnTenureMonths.setTextColor(getColor(R.color.full_white))
            btnTenureYears.setBackgroundResource(0)
            btnTenureYears.setTextColor(getColor(R.color.settings_subtitle_dark))
            if (currentTenure != null && currentTenure > 0) {
                val convertedMonths = (currentTenure * 12.0).toInt()
                etTenure.setText(convertedMonths.toString())
            }
            findViewById<MaterialButton>(R.id.chipTenure1).text = "12M"
            findViewById<MaterialButton>(R.id.chipTenure3).text = "36M"
            findViewById<MaterialButton>(R.id.chipTenure5).text = "60M"
            findViewById<MaterialButton>(R.id.chipTenure10).text = "120M"
            findViewById<MaterialButton>(R.id.chipTenure20).text = "240M"
        }
        calculateEmi()
    }

    @SuppressLint("SetTextI18n")
    private fun calculateEmi() {
        val principal = etPrincipal.text.toString().toDoubleOrNull() ?: 0.0
        val annualRate = etInterestRate.text.toString().toDoubleOrNull() ?: 0.0
        val rawTenure = etTenure.text.toString().toDoubleOrNull() ?: 0.0

        val totalMonths = if (isTenureInYears) rawTenure * 12.0 else rawTenure

        if (principal <= 0 || totalMonths <= 0) {
            tvEmiAmount.text = "--"
            tvTotalPrincipal.text = "$ 0"
            tvTotalInterest.text = "$ 0"
            tvTotalPayable.text = "$ 0"
            updateBarRatio(50f, 50f)
            return
        }

        val monthlyRate = (annualRate / 12.0) / 100.0

        val monthlyEmi: Double
        val totalPayment: Double
        val totalInterest: Double

        if (monthlyRate == 0.0) {
            monthlyEmi = principal / totalMonths
            totalPayment = principal
            totalInterest = 0.0
        } else {
            val factor = (1.0 + monthlyRate).pow(totalMonths)
            monthlyEmi = (principal * monthlyRate * factor) / (factor - 1.0)
            totalPayment = monthlyEmi * totalMonths
            totalInterest = (totalPayment - principal).coerceAtLeast(0.0)
        }

        tvEmiAmount.text = currencyFormat.format(monthlyEmi)
        tvTotalPrincipal.text = "$ ${wholeFormat.format(principal)}"
        tvTotalInterest.text = "$ ${wholeFormat.format(totalInterest)}"
        tvTotalPayable.text = "$ ${wholeFormat.format(totalPayment)}"

        val principalRatio = if (totalPayment > 0) ((principal / totalPayment) * 100).toFloat() else 100f
        val interestRatio = (100f - principalRatio).coerceAtLeast(1f)

        updateBarRatio(principalRatio, interestRatio)
    }

    private fun updateBarRatio(principalWeight: Float, interestWeight: Float) {
        val pParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, principalWeight)
        val iParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, interestWeight)
        viewPrincipalBar.layoutParams = pParams
        viewInterestBar.layoutParams = iParams
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        currentFocus?.let {
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun vibrateTap(view: View) {
        val settingsPrefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        if (settingsPrefs.getBoolean("vibration", true)) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onSwipeLeft() {
        Intent(this@EmiCalculator, Modes::class.java).apply {
            putExtra("current_mode", "EMI")
        }.also {
            startActivity(it)
        }
    }

    override fun onSwipeRight() {
        finish()
    }

    override fun onSwipeDown() {}
}

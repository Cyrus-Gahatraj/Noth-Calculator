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
import android.widget.ScrollView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.text.DecimalFormat
import kotlin.math.pow

class EmiCalculator : AppCompatActivity(), CalculatorGestureListener {

    private lateinit var emiScrollView: ScrollView
    private lateinit var etPrincipal: EditText
    private lateinit var etInterestRate: EditText
    private lateinit var etTenure: EditText

    private lateinit var btnTenureYears: TextView
    private lateinit var btnTenureMonths: TextView

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
        setupWindowInsets()
        // Starts completely blank
        calculateEmi()
    }

    private fun initViews() {
        emiScrollView = findViewById(R.id.emiScrollView)
        etPrincipal = findViewById(R.id.etPrincipal)
        etInterestRate = findViewById(R.id.etInterestRate)
        etTenure = findViewById(R.id.etTenure)

        btnTenureYears = findViewById(R.id.btnTenureYears)
        btnTenureMonths = findViewById(R.id.btnTenureMonths)

        tvEmiAmount = findViewById(R.id.tvEmiAmount)
        tvTotalPrincipal = findViewById(R.id.tvTotalPrincipal)
        tvTotalInterest = findViewById(R.id.tvTotalInterest)
        tvTotalPayable = findViewById(R.id.tvTotalPayable)

        viewPrincipalBar = findViewById(R.id.viewPrincipalBar)
        viewInterestBar = findViewById(R.id.viewInterestBar)

        // Opens completely blank
        etPrincipal.setText("")
        etInterestRate.setText("")
        etTenure.setText("")
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.emiRoot)) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val isKeyboardVisible = imeInsets.bottom > 0
            val bottomPadding = if (isKeyboardVisible) imeInsets.bottom else sysInsets.bottom
            emiScrollView.setPadding(0, 0, 0, bottomPadding)

            if (isKeyboardVisible) {
                emiScrollView.postDelayed({
                    currentFocus?.let { focused ->
                        val rect = android.graphics.Rect()
                        focused.getDrawingRect(rect)
                        emiScrollView.offsetDescendantRectToMyCoords(focused, rect)
                        emiScrollView.smoothScrollTo(0, rect.top - (24 * resources.displayMetrics.density).toInt())
                    }
                }, 150)
            }
            insets
        }
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

        val focusListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                emiScrollView.postDelayed({
                    val rect = android.graphics.Rect()
                    v.getDrawingRect(rect)
                    emiScrollView.offsetDescendantRectToMyCoords(v, rect)
                    emiScrollView.smoothScrollTo(0, rect.top - (24 * resources.displayMetrics.density).toInt())
                }, 150)
            }
        }
        etPrincipal.onFocusChangeListener = focusListener
        etInterestRate.onFocusChangeListener = focusListener
        etTenure.onFocusChangeListener = focusListener

        // Segmented Tenure unit switch
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

        // Action buttons
        findViewById<MaterialButton>(R.id.btnCalculate).setOnClickListener {
            vibrateTap(it)
            hideKeyboard()
            calculateEmi()
        }

        findViewById<MaterialButton>(R.id.btnReset).setOnClickListener {
            vibrateTap(it)
            etPrincipal.setText("")
            etInterestRate.setText("")
            etTenure.setText("")
            setTenureUnit(true)
            calculateEmi()
        }
    }

    private fun setTenureUnit(years: Boolean) {
        isTenureInYears = years
        val currentTenure = etTenure.text.toString().toDoubleOrNull()
        if (years) {
            btnTenureYears.setBackgroundResource(R.drawable.bg_segmented_active)
            btnTenureYears.setTextColor(getColor(R.color.full_white))
            btnTenureMonths.setBackgroundResource(0)
            btnTenureMonths.setTextColor(getColor(R.color.settings_subtitle_dark))
            if (currentTenure != null && currentTenure > 0) {
                val convertedYears = (currentTenure / 12.0).toInt().coerceAtLeast(1)
                etTenure.setText(convertedYears.toString())
            }
        } else {
            btnTenureMonths.setBackgroundResource(R.drawable.bg_segmented_active)
            btnTenureMonths.setTextColor(getColor(R.color.full_white))
            btnTenureYears.setBackgroundResource(0)
            btnTenureYears.setTextColor(getColor(R.color.settings_subtitle_dark))
            if (currentTenure != null && currentTenure > 0) {
                val convertedMonths = (currentTenure * 12.0).toInt()
                etTenure.setText(convertedMonths.toString())
            }
        }
        calculateEmi()
    }

    @SuppressLint("SetTextI18n")
    private fun calculateEmi() {
        val principal = etPrincipal.text.toString().toDoubleOrNull() ?: 0.0
        val annualRate = etInterestRate.text.toString().toDoubleOrNull() ?: 0.0
        val rawTenure = etTenure.text.toString().toDoubleOrNull() ?: 0.0

        val totalMonths = if (isTenureInYears) rawTenure * 12.0 else rawTenure

        if (principal <= 0.0 || totalMonths <= 0.0) {
            tvEmiAmount.text = "--"
            tvTotalPrincipal.text = if (principal > 0) "$ ${wholeFormat.format(principal)}" else "$ 0"
            tvTotalInterest.text = "$ 0"
            tvTotalPayable.text = if (principal > 0) "$ ${wholeFormat.format(principal)}" else "$ 0"
            updateBarRatio(50f, 50f)
            return
        }

        val monthlyRate = (annualRate / 12.0) / 100.0

        val monthlyEmi: Double
        val totalPayment: Double
        val totalInterest: Double

        if (monthlyRate <= 0.0) {
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

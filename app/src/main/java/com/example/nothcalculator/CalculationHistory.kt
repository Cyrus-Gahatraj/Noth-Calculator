package com.example.nothcalculator

import java.text.SimpleDateFormat
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
data class CalculationHistory(
    var expression: String,
    var result: String,
    val date: String
)

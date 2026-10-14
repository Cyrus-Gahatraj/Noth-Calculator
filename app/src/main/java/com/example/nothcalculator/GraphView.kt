package com.example.nothcalculator

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import org.mariuszgromada.math.mxparser.Argument
import org.mariuszgromada.math.mxparser.Expression
import java.text.DecimalFormat
import kotlin.math.abs

data class GraphItem(
    val id: String,
    var formula: String,
    var color: Int,
    var isVisible: Boolean = true,
    var parsedExpression: Expression? = null,
    var isValid: Boolean = true,
    var xArg: Argument = Argument("x = 0")
)

class GraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // View boundaries in mathematical units
    var xMin = -10.0f
        private set
    var xMax = 10.0f
        private set
    var yMin = -10.0f
        private set
    var yMax = 10.0f
        private set

    // Multi-graph collection
    private val graphItems = mutableListOf<GraphItem>()

    // Touch inspection point (mathematical coordinates)
    private var inspectX: Float? = null
    private var isInspecting: Boolean = false

    // Pan tracking
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false

    // Scale gesture detector
    private val scaleDetector: ScaleGestureDetector

    // Paints
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val curvePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val crosshairPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cursorOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cursorInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hudBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hudStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hudTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val curvePath = Path()
    private val decimalFormat = DecimalFormat("0.##")
    private val hudFormat = DecimalFormat("0.00")

    init {
        val ndotTypeface = ResourcesCompat.getFont(context, R.font.ndot)

        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val axisColor = if (isDark) 0xFF6B7280.toInt() else 0xFF9CA3AF.toInt()
        val gridColor = if (isDark) 0xFF2A2A2E.toInt() else 0xFFE5E7EB.toInt()
        val textColor = if (isDark) 0xFF9CA3AF.toInt() else 0xFF6B7280.toInt()

        axisPaint.apply {
            color = axisColor
            strokeWidth = dpToPx(1.5f)
            style = Paint.Style.STROKE
        }

        gridPaint.apply {
            color = gridColor
            strokeWidth = dpToPx(1f)
            style = Paint.Style.STROKE
        }

        textPaint.apply {
            color = textColor
            textSize = dpToPx(10f)
            typeface = ndotTypeface
            textAlign = Paint.Align.CENTER
        }

        curvePaint.apply {
            strokeWidth = dpToPx(3.2f)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        crosshairPaint.apply {
            color = if (isDark) 0x66FFFFFF.toInt() else 0x66000000.toInt()
            strokeWidth = dpToPx(1f)
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(dpToPx(4f), dpToPx(4f)), 0f)
        }

        cursorOuterPaint.apply {
            style = Paint.Style.FILL
        }

        cursorInnerPaint.apply {
            style = Paint.Style.FILL
        }

        hudBgPaint.apply {
            color = if (isDark) 0xEE232326.toInt() else 0xF5FFFFFF.toInt()
            style = Paint.Style.FILL
        }

        hudStrokePaint.apply {
            color = if (isDark) 0xFF34343A.toInt() else 0xFFE5E7EB.toInt()
            strokeWidth = dpToPx(1f)
            style = Paint.Style.STROKE
        }

        hudTextPaint.apply {
            color = if (isDark) 0xFFFFFFFF.toInt() else 0xFF18181B.toInt()
            textSize = dpToPx(11f)
            typeface = ndotTypeface
        }

        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val factor = detector.scaleFactor
                if (factor != 1.0f && factor > 0f) {
                    zoom(1.0f / factor, detector.focusX, detector.focusY)
                    return true
                }
                return false
            }
        })
    }

    fun setGraphs(items: List<GraphItem>) {
        graphItems.clear()
        for (item in items) {
            compileGraph(item)
            graphItems.add(item)
        }
        inspectX = null
        invalidate()
    }

    fun updateGraph(id: String, formula: String) {
        val item = graphItems.find { it.id == id } ?: return
        item.formula = formula
        compileGraph(item)
        invalidate()
    }

    fun toggleGraphVisibility(id: String): Boolean {
        val item = graphItems.find { it.id == id } ?: return false
        item.isVisible = !item.isVisible
        invalidate()
        return item.isVisible
    }

    private fun compileGraph(item: GraphItem) {
        val clean = item.formula.trim()
        if (clean.isEmpty()) {
            item.isValid = false
            item.parsedExpression = null
            return
        }
        val normalized = normalizeFormula(clean)
        try {
            item.xArg = Argument("x = 0")
            val expr = Expression(normalized, item.xArg)
            item.isValid = expr.checkSyntax()
            item.parsedExpression = if (item.isValid) expr else null
        } catch (_: Exception) {
            item.isValid = false
            item.parsedExpression = null
        }
    }

    private fun normalizeFormula(fn: String): String {
        var s = fn.replace("×", "*")
            .replace("÷", "/")
            .replace("π", "pi")
            .replace("e^x", "exp(x)")
            .replace("√", "sqrt")

        val opens = s.count { it == '(' }
        val closes = s.count { it == ')' }
        if (opens > closes) {
            s += ")".repeat(opens - closes)
        }
        return s
    }

    fun zoomIn() {
        val centerX = (xMin + xMax) / 2f
        val centerY = (yMin + yMax) / 2f
        val halfX = (xMax - xMin) * 0.37f
        val halfY = (yMax - yMin) * 0.37f
        setWindow(centerX - halfX, centerX + halfX, centerY - halfY, centerY + halfY)
    }

    fun zoomOut() {
        val centerX = (xMin + xMax) / 2f
        val centerY = (yMin + yMax) / 2f
        val halfX = (xMax - xMin) * 0.67f
        val halfY = (yMax - yMin) * 0.67f
        setWindow(centerX - halfX, centerX + halfX, centerY - halfY, centerY + halfY)
    }

    fun resetView() {
        setWindow(-10f, 10f, -10f, 10f)
    }

    private fun zoom(scale: Float, focusScreenX: Float, focusScreenY: Float) {
        val focusMathX = screenToMathX(focusScreenX)
        val focusMathY = screenToMathY(focusScreenY)

        val newRangeX = (xMax - xMin) * scale
        val newRangeY = (yMax - yMin) * scale

        if (newRangeX in 0.5f..1000f && newRangeY in 0.5f..1000f) {
            val ratioX = focusScreenX / width.toFloat()
            val ratioY = 1.0f - (focusScreenY / height.toFloat())

            val newXMin = focusMathX - ratioX * newRangeX
            val newXMax = newXMin + newRangeX
            val newYMin = focusMathY - ratioY * newRangeY
            val newYMax = newYMin + newRangeY

            setWindow(newXMin, newXMax, newYMin, newYMax)
        }
    }

    private fun setWindow(newXMin: Float, newXMax: Float, newYMin: Float, newYMax: Float) {
        xMin = newXMin
        xMax = newXMax
        yMin = newYMin
        yMax = newYMax
        invalidate()
    }

    private fun screenToMathX(sx: Float): Float = xMin + (sx / width.toFloat()) * (xMax - xMin)
    private fun screenToMathY(sy: Float): Float = yMin + ((height - sy) / height.toFloat()) * (yMax - yMin)

    private fun mathToScreenX(mx: Float): Float = ((mx - xMin) / (xMax - xMin)) * width.toFloat()
    private fun mathToScreenY(my: Float): Float = height.toFloat() - ((my - yMin) / (yMax - yMin)) * height.toFloat()

    private fun calculateGridStep(range: Float): Float {
        return when {
            range <= 4f -> 0.5f
            range <= 10f -> 1.0f
            range <= 25f -> 2.0f
            range <= 60f -> 5.0f
            range <= 150f -> 10.0f
            range <= 300f -> 25.0f
            else -> 50.0f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        // 1. Draw Grid Lines & Numbers
        drawGridAndAxes(canvas)

        // 2. Draw All Active Function Curves
        for (item in graphItems) {
            if (item.isVisible && item.isValid && item.parsedExpression != null) {
                drawSingleCurve(canvas, item)
            }
        }

        // 3. Draw Inspection Indicator & HUD
        drawInspection(canvas)
    }

    private fun drawGridAndAxes(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        val stepX = calculateGridStep(xMax - xMin)
        val stepY = calculateGridStep(yMax - yMin)

        // Vertical grid lines (X ticks)
        val firstX = (kotlin.math.floor(xMin / stepX) * stepX).toFloat()
        var curX = firstX
        while (curX <= xMax + stepX) {
            val sx = mathToScreenX(curX)
            if (sx in 0f..w) {
                canvas.drawLine(sx, 0f, sx, h, gridPaint)
                if (abs(curX) > 0.001f) {
                    val labelY = mathToScreenY(0f).coerceIn(dpToPx(14f), h - dpToPx(6f))
                    canvas.drawText(decimalFormat.format(curX), sx, labelY + dpToPx(12f), textPaint)
                }
            }
            curX += stepX
        }

        // Horizontal grid lines (Y ticks)
        val firstY = (kotlin.math.floor(yMin / stepY) * stepY).toFloat()
        var curY = firstY
        while (curY <= yMax + stepY) {
            val sy = mathToScreenY(curY)
            if (sy in 0f..h) {
                canvas.drawLine(0f, sy, w, sy, gridPaint)
                if (abs(curY) > 0.001f) {
                    val labelX = mathToScreenX(0f).coerceIn(dpToPx(18f), w - dpToPx(18f))
                    canvas.drawText(decimalFormat.format(curY), labelX - dpToPx(12f), sy + dpToPx(4f), textPaint)
                }
            }
            curY += stepY
        }

        // Main Axes
        val axisY = mathToScreenY(0f)
        if (axisY in 0f..h) {
            canvas.drawLine(0f, axisY, w, axisY, axisPaint)
        }

        val axisX = mathToScreenX(0f)
        if (axisX in 0f..w) {
            canvas.drawLine(axisX, 0f, axisX, h, axisPaint)
        }

        // Origin (0,0) marker
        if (axisX in 0f..w && axisY in 0f..h) {
            canvas.drawText("0", axisX - dpToPx(8f), axisY + dpToPx(12f), textPaint)
        }
    }

    private fun drawSingleCurve(canvas: Canvas, item: GraphItem) {
        val expr = item.parsedExpression ?: return
        curvePaint.color = item.color
        curvePath.reset()

        val w = width
        val stepPixels = 3
        var isPathStarted = false
        var prevMathY = 0.0
        val yRange = (yMax - yMin).toDouble()

        for (px in 0..w step stepPixels) {
            val mx = screenToMathX(px.toFloat()).toDouble()
            item.xArg.setArgumentValue(mx)
            val my = expr.calculate()

            if (my.isNaN() || my.isInfinite()) {
                isPathStarted = false
                continue
            }

            val sy = mathToScreenY(my.toFloat())

            // Detect asymptotic discontinuities (e.g., tan(x), 1/x)
            if (isPathStarted) {
                val diff = abs(my - prevMathY)
                if (diff > yRange * 1.5 && (my * prevMathY < 0)) {
                    curvePath.moveTo(px.toFloat(), sy)
                    prevMathY = my
                    continue
                }
            }

            if (!isPathStarted) {
                curvePath.moveTo(px.toFloat(), sy)
                isPathStarted = true
            } else {
                curvePath.lineTo(px.toFloat(), sy)
            }
            prevMathY = my
        }

        canvas.drawPath(curvePath, curvePaint)
    }

    private fun drawInspection(canvas: Canvas) {
        val ix = inspectX ?: return

        val sx = mathToScreenX(ix)
        val hudItems = mutableListOf<String>()

        // Draw vertical crosshair
        canvas.drawLine(sx, 0f, sx, height.toFloat(), crosshairPaint)

        // Draw indicator dots on each visible curve
        var index = 1
        for (item in graphItems) {
            if (item.isVisible && item.isValid && item.parsedExpression != null) {
                item.xArg.setArgumentValue(ix.toDouble())
                val my = item.parsedExpression!!.calculate()

                if (!my.isNaN() && !my.isInfinite()) {
                    val sy = mathToScreenY(my.toFloat())

                    // Horizontal crosshair to Y axis
                    canvas.drawLine(0f, sy, width.toFloat(), sy, crosshairPaint)

                    // Dot with curve's specific color
                    cursorOuterPaint.color = (item.color and 0x00FFFFFF) or 0x33000000
                    canvas.drawCircle(sx, sy, dpToPx(11f), cursorOuterPaint)

                    cursorInnerPaint.color = item.color
                    canvas.drawCircle(sx, sy, dpToPx(5f), cursorInnerPaint)

                    hudItems.add("y$index: ${hudFormat.format(my)}")
                }
            }
            index++
        }

        if (hudItems.isEmpty()) return

        // Build HUD string: "x: 2.00 | y1: 0.91 | y2: 4.00"
        val hudText = "x: ${hudFormat.format(ix)}  " + hudItems.joinToString("  ")
        val textWidth = hudTextPaint.measureText(hudText)
        val paddingH = dpToPx(12f)
        val hudW = textWidth + paddingH * 2
        val hudH = dpToPx(30f)

        val hudLeft = (width - hudW) / 2f
        val hudTop = dpToPx(10f)
        val hudRect = RectF(hudLeft, hudTop, hudLeft + hudW, hudTop + hudH)

        canvas.drawRoundRect(hudRect, dpToPx(8f), dpToPx(8f), hudBgPaint)
        canvas.drawRoundRect(hudRect, dpToPx(8f), dpToPx(8f), hudStrokePaint)
        canvas.drawText(hudText, hudLeft + paddingH, hudTop + dpToPx(19f), hudTextPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        if (scaleDetector.isInProgress) {
            isInspecting = false
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = false
                parent?.requestDisallowInterceptTouchEvent(true)
                updateInspection(event.x)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY

                if (abs(dx) > dpToPx(4f) || abs(dy) > dpToPx(4f) || isDragging) {
                    isDragging = true
                    val mathDx = (dx / width.toFloat()) * (xMax - xMin)
                    val mathDy = (dy / height.toFloat()) * (yMax - yMin)

                    setWindow(xMin - mathDx, xMax - mathDx, yMin + mathDy, yMax + mathDy)
                    lastTouchX = event.x
                    lastTouchY = event.y
                    updateInspection(event.x)
                } else {
                    updateInspection(event.x)
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun updateInspection(touchX: Float) {
        inspectX = screenToMathX(touchX)
        isInspecting = true
        invalidate()
    }

    private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density
}

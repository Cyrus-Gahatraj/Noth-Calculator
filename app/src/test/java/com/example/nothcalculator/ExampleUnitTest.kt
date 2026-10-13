package com.example.nothcalculator

import org.junit.Test
import org.junit.Assert.*
import org.mariuszgromada.math.mxparser.Expression
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class ExampleUnitTest {

    @Test
    fun testBasicArithmetic() {
        assertEquals(4, 2 + 2)
        assertEquals(30.0, Expression("5*6").calculate(), 0.0001)
        assertEquals(2.5, Expression("5/2").calculate(), 0.0001)
    }

    @Test
    fun testParenthesesAutoClose() {
        fun normalizeExpression(exp: String): String {
            var s = exp
            val opens = s.count { it == '(' }
            val closes = s.count { it == ')' }
            if (opens > closes) {
                s += ")".repeat(opens - closes)
            }
            return s
        }

        assertEquals("(5+3)", normalizeExpression("(5+3"))
        assertEquals("((5+3))", normalizeExpression("((5+3"))
        assertEquals(8.0, Expression(normalizeExpression("(5+3")).calculate(), 0.0001)
        assertEquals(8.0, Expression(normalizeExpression("((5+3)")).calculate(), 0.0001)
    }

    @Test
    fun testBracketSelection() {
        fun getNextBracket(exp: String): String {
            val openCount = exp.count { it == '(' }
            val closeCount = exp.count { it == ')' }
            return if (openCount > closeCount && exp.isNotEmpty()) {
                val last = exp.last()
                if (last.isDigit() || last == ')' || last == '%') ")" else "("
            } else {
                "("
            }
        }

        assertEquals("(", getNextBracket(""))
        assertEquals("(", getNextBracket("5+"))
        assertEquals("(", getNextBracket("("))
        assertEquals(")", getNextBracket("(5"))
        assertEquals(")", getNextBracket("(5+3"))
        assertEquals("(", getNextBracket("(5+"))
        assertEquals(")", getNextBracket("((5+3)*2"))
    }

    @Test
    fun testToggleSignLogic() {
        fun toggleSign(exp: String): String {
            var expression = exp
            if (expression.isEmpty()) {
                return "-"
            } else if (expression == "-") {
                return ""
            } else {
                val lastChar = expression.last()
                val operators = listOf('+', '-', '*', '/', '(', '^', '#', '×')

                if (lastChar == '-') {
                    return if (expression.length >= 2 && expression[expression.length - 2] in operators) {
                        expression.dropLast(1)
                    } else if (expression.length >= 2) {
                        expression.dropLast(1) + "+"
                    } else {
                        ""
                    }
                } else if (lastChar == '+') {
                    return expression.dropLast(1) + "-"
                } else if (lastChar in operators) {
                    return "$expression-"
                } else {
                    var start = expression.lastIndex
                    while (start >= 0 && (expression[start].isDigit() || expression[start] == '.')) {
                        start--
                    }
                    val numStart = start + 1
                    return if (numStart > expression.lastIndex) {
                        "$expression-"
                    } else if (numStart == 0) {
                        "-$expression"
                    } else if (numStart == 1 && expression[0] == '-') {
                        expression.substring(1)
                    } else if (numStart >= 2 && expression[numStart - 1] == '-' && expression[numStart - 2] in operators) {
                        expression.removeRange(numStart - 1, numStart)
                    } else if (expression[numStart - 1] == '-') {
                        expression.substring(0, numStart - 1) + "+" + expression.substring(numStart)
                    } else if (expression[numStart - 1] == '+') {
                        expression.substring(0, numStart - 1) + "-" + expression.substring(numStart)
                    } else {
                        expression.substring(0, numStart) + "-" + expression.substring(numStart)
                    }
                }
            }
        }

        assertEquals("-", toggleSign(""))
        assertEquals("", toggleSign("-"))
        assertEquals("-5", toggleSign("5"))
        assertEquals("5", toggleSign("-5"))
        assertEquals("10-5", toggleSign("10+5"))
        assertEquals("10+5", toggleSign("10-5"))
        assertEquals("10*-5", toggleSign("10*5"))
        assertEquals("10*5", toggleSign("10*-5"))
        assertEquals("(-5", toggleSign("(5"))
        assertEquals("(5", toggleSign("(-5"))
    }

    @Test
    fun testResultFormatting() {
        fun formatResult(result: Double): String {
            if (result.isNaN() || result.isInfinite()) {
                return "Error"
            }
            var resultString = result.toString()
            if (ceil(result) == floor(result) && abs(result) < 1e15) {
                resultString = result.toLong().toString()
            }
            return resultString
        }

        assertEquals("Error", formatResult(Double.NaN))
        assertEquals("Error", formatResult(Double.POSITIVE_INFINITY))
        assertEquals("Error", formatResult(Double.NEGATIVE_INFINITY))
        assertEquals("5", formatResult(5.0))
        assertEquals("2.5", formatResult(2.5))
        assertEquals("1.0E25", formatResult(1e25))
    }

    @Test
    fun testTokenDeletion() {
        fun deleteLastToken(exp: String): String {
            val tokens = listOf("sin(", "cos(", "tan(", "log10(", "ln(", "Ans")
            val matchingToken = tokens.firstOrNull { exp.endsWith(it) }
            return if (matchingToken != null) {
                exp.dropLast(matchingToken.length)
            } else {
                exp.dropLast(1)
            }
        }

        assertEquals("5+", deleteLastToken("5+sin("))
        assertEquals("5+", deleteLastToken("5+cos("))
        assertEquals("5+", deleteLastToken("5+log10("))
        assertEquals("5+", deleteLastToken("5+Ans"))
        assertEquals("5", deleteLastToken("53"))
    }
}
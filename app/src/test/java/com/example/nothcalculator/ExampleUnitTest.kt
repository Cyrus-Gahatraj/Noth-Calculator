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
    fun testScientificFunctions() {
        assertEquals(4.0, Expression("sqrt(16)").calculate(), 0.0001)
        assertEquals(Math.PI, Expression("pi").calculate(), 0.0001)
        assertEquals(Math.E, Expression("e").calculate(), 0.0001)
        assertEquals(120.0, Expression("5!").calculate(), 0.0001)
        assertEquals(1.0, Expression("10#3").calculate(), 0.0001)

        fun replaceExpr(expr: String, lastAnswer: String): String {
            var res = expr.replace("mod", "#")
            res = res.replace("Ans", lastAnswer)
            res = res.replace("√", "sqrt(")
            res = res.replace("π", "pi")
            res = res.replace("×", "*")
            return res
        }

        fun normalize(exp: String): String {
            var s = exp
            val opens = s.count { it == '(' }
            val closes = s.count { it == ')' }
            if (opens > closes) {
                s += ")".repeat(opens - closes)
            }
            return s
        }

        assertEquals(4.0, Expression(normalize(replaceExpr("√16", "0"))).calculate(), 0.0001)
        assertEquals(Math.PI, Expression(normalize(replaceExpr("π", "0"))).calculate(), 0.0001)
        org.mariuszgromada.math.mxparser.mXparser.setDegreesMode()
        assertEquals(1.0, Expression("sin(90)").calculate(), 0.0001)
        org.mariuszgromada.math.mxparser.mXparser.setRadiansMode()
        assertEquals(1.0, Expression("sin(pi/2)").calculate(), 0.0001)
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
            val tokens = listOf("sin(", "cos(", "tan(", "log10(", "ln(", "sqrt(", "Ans")
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
        assertEquals("5+", deleteLastToken("5+sqrt("))
        assertEquals("5+", deleteLastToken("5+Ans"))
        assertEquals("5", deleteLastToken("53"))
    }

    @Test
    fun testEmiCalculation() {
        fun computeEmi(principal: Double, annualRate: Double, totalMonths: Double): Triple<Double, Double, Double> {
            if (principal <= 0 || totalMonths <= 0) {
                return Triple(0.0, 0.0, 0.0)
            }
            val monthlyRate = (annualRate / 12.0) / 100.0
            val emi: Double
            val totalPayment: Double
            val totalInterest: Double

            if (monthlyRate == 0.0) {
                emi = principal / totalMonths
                totalPayment = principal
                totalInterest = 0.0
            } else {
                val factor = Math.pow(1.0 + monthlyRate, totalMonths)
                emi = (principal * monthlyRate * factor) / (factor - 1.0)
                totalPayment = emi * totalMonths
                totalInterest = (totalPayment - principal).coerceAtLeast(0.0)
            }
            return Triple(emi, totalInterest, totalPayment)
        }

        // Test Standard Loan: $100,000 at 8.5% for 5 years (60 months)
        val (emi1, interest1, total1) = computeEmi(100000.0, 8.5, 60.0)
        assertEquals(2051.65, emi1, 0.01)
        assertEquals(23099.18, interest1, 0.02)
        assertEquals(123099.18, total1, 0.02)

        // Test Zero Interest Loan: $12,000 at 0% for 12 months
        val (emi2, interest2, total2) = computeEmi(12000.0, 0.0, 12.0)
        assertEquals(1000.0, emi2, 0.001)
        assertEquals(0.0, interest2, 0.001)
        assertEquals(12000.0, total2, 0.001)

        // Test Edge Cases (0 principal or 0 tenure)
        val (emi3, interest3, total3) = computeEmi(0.0, 8.5, 60.0)
        assertEquals(0.0, emi3, 0.0)
        assertEquals(0.0, interest3, 0.0)
        assertEquals(0.0, total3, 0.0)
    }

    @Test
    fun testGraphFunctionEvaluation() {
        fun normalizeFormula(fn: String): String {
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

        fun evalAt(formula: String, xVal: Double): Double {
            val normalized = normalizeFormula(formula)
            val xArg = org.mariuszgromada.math.mxparser.Argument("x = $xVal")
            val expr = org.mariuszgromada.math.mxparser.Expression(normalized, xArg)
            assertTrue("Formula '$formula' should have valid syntax", expr.checkSyntax())
            return expr.calculate()
        }

        // Trigonometric in radians mode
        org.mariuszgromada.math.mxparser.mXparser.setRadiansMode()
        assertEquals(0.0, evalAt("sin(x)", 0.0), 0.0001)
        assertEquals(1.0, evalAt("sin(x)", Math.PI / 2.0), 0.0001)
        assertEquals(1.0, evalAt("cos(x)", 0.0), 0.0001)
        assertEquals(-1.0, evalAt("cos(x)", Math.PI), 0.0001)

        // Polynomials
        assertEquals(9.0, evalAt("x^2", 3.0), 0.0001)
        assertEquals(2.0, evalAt("x^3 - 3*x", 2.0), 0.0001)

        // Square root and inverse
        assertEquals(4.0, evalAt("sqrt(x)", 16.0), 0.0001)
        assertEquals(0.5, evalAt("1/x", 2.0), 0.0001)
        assertEquals(Math.E, evalAt("exp(x)", 1.0), 0.0001)
        assertEquals(5.0, evalAt("abs(x)", -5.0), 0.0001)

        // Auto-closing parenthesis
        assertEquals(16.0, evalAt("(x+2)^2", 2.0), 0.0001)
    }
}
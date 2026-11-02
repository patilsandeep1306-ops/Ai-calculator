package com.example.math

import com.example.model.MathStep
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

object ExpressionEvaluator {

    var isRadianMode: Boolean = true

    /**
     * Evaluates a mathematical expression and returns the formatted numeric result.
     * Throws an IllegalArgumentException on syntax or mathematical error.
     */
    fun evaluate(expression: String, isRadian: Boolean = isRadianMode): Double {
        val sanitized = sanitize(expression)
        if (sanitized.isBlank()) return 0.0
        val tokens = tokenize(sanitized)
        val rpn = shuntingYard(tokens)
        return evaluateRpn(rpn, isRadian)
    }

    /**
     * Attempts to evaluate and returns null if incomplete or invalid (useful for live preview).
     */
    fun tryEvaluate(expression: String, isRadian: Boolean = isRadianMode): Double? {
        return try {
            val result = evaluate(expression, isRadian)
            if (result.isNaN() || result.isInfinite()) null else result
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Generates a step-by-step PEMDAS / BODMAS explanation of how the expression is evaluated.
     */
    fun explainSteps(expression: String, isRadian: Boolean = isRadianMode): List<MathStep> {
        val steps = mutableListOf<MathStep>()
        var stepCount = 1

        val clean = sanitize(expression)
        steps.add(
            MathStep(
                stepNumber = stepCount++,
                title = "Original Expression",
                description = "Parse order of operations (Parentheses, Exponents, Multiply/Divide, Add/Subtract).",
                expression = expression
            )
        )

        // Find and evaluate any functions (sin, cos, sqrt, etc.)
        val funcRegex = Regex("""(sin|cos|tan|asin|acos|atan|sqrt|cbrt|ln|log|abs)\(([^()]+)\)""")
        var current = clean
        var match = funcRegex.find(current)
        while (match != null) {
            val fn = match.groupValues[1]
            val argStr = match.groupValues[2]
            val argVal = tryEvaluate(argStr, isRadian) ?: break
            val fnVal = evaluateSingleFunction(fn, argVal, isRadian)
            val formatted = formatNumber(fnVal)
            steps.add(
                MathStep(
                    stepNumber = stepCount++,
                    title = "Evaluate Function: $fn($argStr)",
                    description = "Compute $fn with input $argStr (${if (isRadian) "Radians" else "Degrees"}): $fn($argStr) = $formatted",
                    expression = current.replace(match.value, formatted)
                )
            )
            current = current.replaceFirst(match.value, formatted)
            match = funcRegex.find(current)
        }

        // Parentheses breakdown
        val parenRegex = Regex("""\(([^()]+)\)""")
        var pMatch = parenRegex.find(current)
        while (pMatch != null) {
            val inner = pMatch.groupValues[1]
            val innerVal = tryEvaluate(inner, isRadian) ?: break
            val formatted = formatNumber(innerVal)
            steps.add(
                MathStep(
                    stepNumber = stepCount++,
                    title = "Evaluate Inside Parentheses ($inner)",
                    description = "Inner expression simplifies to $formatted following standard operator precedence.",
                    expression = current.replace(pMatch.value, formatted)
                )
            )
            current = current.replaceFirst(pMatch.value, formatted)
            pMatch = parenRegex.find(current)
        }

        // Powers and roots
        val powRegex = Regex("""(-?\d+(?:\.\d+)?)\^(\d+(?:\.\d+)?)""")
        var powMatch = powRegex.find(current)
        while (powMatch != null) {
            val base = powMatch.groupValues[1].toDoubleOrNull() ?: break
            val exp = powMatch.groupValues[2].toDoubleOrNull() ?: break
            val res = base.pow(exp)
            val formatted = formatNumber(res)
            steps.add(
                MathStep(
                    stepNumber = stepCount++,
                    title = "Compute Exponent: ${powMatch.value}",
                    description = "Raise base $base to the power of $exp: $base^$exp = $formatted",
                    expression = current.replace(powMatch.value, formatted)
                )
            )
            current = current.replaceFirst(powMatch.value, formatted)
            powMatch = powRegex.find(current)
        }

        // Final result calculation
        val finalVal = tryEvaluate(clean, isRadian)
        if (finalVal != null) {
            steps.add(
                MathStep(
                    stepNumber = stepCount,
                    title = "Final Evaluated Result",
                    description = "All operations completed according to algebraic order of operations.",
                    expression = "= ${formatNumber(finalVal)}"
                )
            )
        }

        return steps
    }

    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
        if (value == 0.0 || value == -0.0) return "0"

        // If it's an integer value, show as integer
        if (abs(value - round(value)) < 1e-10 && abs(value) < 1e12) {
            return round(value).toLong().toString()
        }

        // Format to reasonable decimal places without trailing zeros
        return try {
            val bd = BigDecimal(value.toString())
                .setScale(8, RoundingMode.HALF_UP)
                .stripTrailingZeros()
            bd.toPlainString()
        } catch (_: Exception) {
            String.format(java.util.Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
        }
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "PI")
            .replace("pi", "PI", ignoreCase = true)
            .replace("√", "sqrt")
            .replace("−", "-")
            .replace(" ", "")
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c in "+-*/^%()!" -> {
                    // Check for unary minus:
                    if (c == '-') {
                        val prev = tokens.lastOrNull()
                        if (prev == null || prev in "+-*/^%(" || prev == "u-") {
                            tokens.add("u-")
                            i++
                            continue
                        }
                    }
                    tokens.add(c.toString())
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < len && (expr[i].isLetter() || expr[i].isDigit())) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                else -> i++
            }
        }
        return tokens
    }

    private fun precedence(op: String): Int = when (op) {
        "!" -> 5
        "u-" -> 4
        "^" -> 4
        "*", "/", "%" -> 3
        "+", "-" -> 2
        else -> 0
    }

    private fun isRightAssociative(op: String): Boolean = op == "^" || op == "u-"

    private val functions = setOf(
        "sin", "cos", "tan", "asin", "acos", "atan",
        "sqrt", "cbrt", "ln", "log", "abs", "exp"
    )

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token.equals("PI", ignoreCase = true) -> output.add(Math.PI.toString())
                token == "e" || token == "E" -> output.add(Math.E.toString())
                functions.contains(token.lowercase()) -> stack.addFirst(token.lowercase())
                token == "(" -> stack.addFirst(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.first() != "(") {
                        output.add(stack.removeFirst())
                    }
                    if (stack.isNotEmpty() && stack.first() == "(") {
                        stack.removeFirst() // Remove '('
                    }
                    if (stack.isNotEmpty() && functions.contains(stack.first())) {
                        output.add(stack.removeFirst())
                    }
                }
                token == "!" -> {
                    // Postfix factorial
                    output.add("!")
                }
                else -> {
                    // Operator
                    while (stack.isNotEmpty() && stack.first() != "(") {
                        val top = stack.first()
                        val topPrec = precedence(top)
                        val tokenPrec = precedence(token)
                        if ((isRightAssociative(token) && tokenPrec < topPrec) ||
                            (!isRightAssociative(token) && tokenPrec <= topPrec)
                        ) {
                            output.add(stack.removeFirst())
                        } else {
                            break
                        }
                    }
                    stack.addFirst(token)
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeFirst()
            if (top != "(" && top != ")") {
                output.add(top)
            }
        }

        return output
    }

    private fun evaluateRpn(rpn: List<String>, isRadian: Boolean): Double {
        val stack = ArrayDeque<Double>()

        for (token in rpn) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.addFirst(num)
                continue
            }

            when (token) {
                "u-" -> {
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(-a)
                }
                "!" -> {
                    val a = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand for !")
                    stack.addFirst(factorial(a.roundToInt()))
                }
                "+" -> {
                    val b = stack.removeFirstOrNull() ?: 0.0
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(a + b)
                }
                "-" -> {
                    val b = stack.removeFirstOrNull() ?: 0.0
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(a - b)
                }
                "*" -> {
                    val b = stack.removeFirstOrNull() ?: 1.0
                    val a = stack.removeFirstOrNull() ?: 1.0
                    stack.addFirst(a * b)
                }
                "/" -> {
                    val b = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing denominator")
                    if (b == 0.0) throw ArithmeticException("Division by zero")
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(a / b)
                }
                "%" -> {
                    val b = stack.removeFirstOrNull() ?: 1.0
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(a % b)
                }
                "^" -> {
                    val b = stack.removeFirstOrNull() ?: 1.0
                    val a = stack.removeFirstOrNull() ?: 0.0
                    stack.addFirst(a.pow(b))
                }
                else -> {
                    // Function
                    val arg = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing argument for $token")
                    stack.addFirst(evaluateSingleFunction(token, arg, isRadian))
                }
            }
        }

        return stack.firstOrNull() ?: 0.0
    }

    private fun evaluateSingleFunction(fn: String, arg: Double, isRadian: Boolean): Double {
        val radArg = if (isRadian) arg else Math.toRadians(arg)
        return when (fn) {
            "sin" -> sin(radArg)
            "cos" -> cos(radArg)
            "tan" -> {
                val res = tan(radArg)
                if (abs(res) > 1e12) throw ArithmeticException("Tangent undefined")
                res
            }
            "asin" -> {
                if (arg < -1.0 || arg > 1.0) throw IllegalArgumentException("Input out of range [-1, 1]")
                val res = asin(arg)
                if (isRadian) res else Math.toDegrees(res)
            }
            "acos" -> {
                if (arg < -1.0 || arg > 1.0) throw IllegalArgumentException("Input out of range [-1, 1]")
                val res = acos(arg)
                if (isRadian) res else Math.toDegrees(res)
            }
            "atan" -> {
                val res = atan(arg)
                if (isRadian) res else Math.toDegrees(res)
            }
            "sqrt" -> {
                if (arg < 0) throw IllegalArgumentException("Square root of negative number")
                sqrt(arg)
            }
            "cbrt" -> cbrt(arg)
            "ln" -> {
                if (arg <= 0) throw IllegalArgumentException("Log of non-positive number")
                ln(arg)
            }
            "log" -> {
                if (arg <= 0) throw IllegalArgumentException("Log of non-positive number")
                log10(arg)
            }
            "abs" -> abs(arg)
            "exp" -> exp(arg)
            else -> throw IllegalArgumentException("Unknown function $fn")
        }
    }

    private fun factorial(n: Int): Double {
        if (n < 0) throw IllegalArgumentException("Negative factorial")
        if (n > 170) return Double.POSITIVE_INFINITY
        var res = 1.0
        for (i in 2..n) {
            res *= i
        }
        return res
    }
}

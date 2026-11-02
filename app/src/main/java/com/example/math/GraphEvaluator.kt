package com.example.math

import androidx.compose.ui.geometry.Offset

object GraphEvaluator {

    data class Point(val x: Double, val y: Double)

    /**
     * Evaluates expression across range [minX, maxX] with specified step count.
     */
    fun evaluateRange(
        expression: String,
        minX: Double = -10.0,
        maxX: Double = 10.0,
        steps: Int = 200
    ): List<Point> {
        val points = mutableListOf<Point>()
        val stepSize = (maxX - minX) / steps

        var x = minX
        while (x <= maxX + 1e-9) {
            val y = evaluateAt(expression, x)
            if (y != null && !y.isNaN() && !y.isInfinite() && kotlin.math.abs(y) < 1000.0) {
                points.add(Point(x, y))
            }
            x += stepSize
        }
        return points
    }

    /**
     * Evaluates expression replacing variable 'x' with numeric value.
     */
    fun evaluateAt(expression: String, xValue: Double): Double? {
        val sanitized = expression
            .replace("x", "($xValue)", ignoreCase = false)
            .replace("X", "($xValue)")
        return ExpressionEvaluator.tryEvaluate(sanitized, isRadian = true)
    }
}

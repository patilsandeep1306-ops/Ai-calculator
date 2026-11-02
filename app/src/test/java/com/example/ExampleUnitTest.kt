package com.example

import com.example.math.AiMathSolver
import com.example.math.ExpressionEvaluator
import com.example.math.UnitConverterData
import com.example.model.UnitCategoryType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBasicArithmetic() {
        val res = ExpressionEvaluator.evaluate("15 + 27 - 6 * 4 / 2")
        // 15 + 27 - 12 = 30
        assertEquals(30.0, res, 0.0001)
    }

    @Test
    fun testOrderOfOperationsAndParentheses() {
        val res = ExpressionEvaluator.evaluate("2 + 3 * (4 + 1)^2")
        // 2 + 3 * 25 = 2 + 75 = 77
        assertEquals(77.0, res, 0.0001)
    }

    @Test
    fun testScientificFunctions() {
        val sqrtRes = ExpressionEvaluator.evaluate("sqrt(144) + 5!")
        // 12 + 120 = 132
        assertEquals(132.0, sqrtRes, 0.0001)
    }

    @Test
    fun testAiSolverTipSplit() {
        val solution = AiMathSolver.solve("Split $100 among 4 people with 20% tip")
        assertEquals("Finance", solution.category)
        assertTrue(solution.finalAnswer.contains("30"))
    }

    @Test
    fun testAiSolverQuadratic() {
        val solution = AiMathSolver.solve("x^2 - 5x + 6 = 0")
        assertEquals("Algebra", solution.category)
        assertTrue(solution.finalAnswer.contains("x₁ = 3") || solution.finalAnswer.contains("3"))
    }

    @Test
    fun testUnitConverterLength() {
        val meter = UnitConverterData.categories[UnitCategoryType.LENGTH]!!.first { it.id == "m" }
        val km = UnitConverterData.categories[UnitCategoryType.LENGTH]!!.first { it.id == "km" }
        val res = UnitConverterData.convert(2500.0, meter, km)
        assertEquals(2.5, res, 0.0001)
    }
}

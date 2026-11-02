package com.example.model

data class CalculationHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isScientific: Boolean = false
)

data class MathStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val expression: String? = null
)

data class AiProblemSolution(
    val problemTitle: String,
    val category: String,
    val summary: String,
    val formulaUsed: String? = null,
    val givenValues: Map<String, String> = emptyMap(),
    val steps: List<MathStep> = emptyList(),
    val finalAnswer: String,
    val unit: String? = null,
    val explanation: String? = null,
    val verification: String? = null
)

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val factorToBase: Double, // Value in base unit = input * factorToBase (for linear)
    val customToFactor: ((Double) -> Double)? = null, // for temperature
    val customFromFactor: ((Double) -> Double)? = null
)

enum class UnitCategoryType(val displayName: String) {
    LENGTH("Length"),
    WEIGHT("Weight / Mass"),
    TEMPERATURE("Temperature"),
    SPEED("Speed"),
    AREA("Area"),
    VOLUME("Volume"),
    DATA("Digital Storage"),
    TIME("Time"),
    ENERGY("Energy")
}

data class PresetFunction(
    val name: String,
    val expression: String,
    val description: String
)

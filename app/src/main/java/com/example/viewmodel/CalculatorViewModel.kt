package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.math.AiMathSolver
import com.example.math.ExpressionEvaluator
import com.example.math.UnitConverterData
import com.example.model.AiProblemSolution
import com.example.model.CalculationHistoryItem
import com.example.model.ConversionUnit
import com.example.model.MathStep
import com.example.model.PresetFunction
import com.example.model.UnitCategoryType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalculatorViewModel : ViewModel() {

    // --- Calculator State ---
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _liveResult = MutableStateFlow<String?>(null)
    val liveResult: StateFlow<String?> = _liveResult.asStateFlow()

    private val _isScientific = MutableStateFlow(false)
    val isScientific: StateFlow<Boolean> = _isScientific.asStateFlow()

    private val _isRadian = MutableStateFlow(true)
    val isRadian: StateFlow<Boolean> = _isRadian.asStateFlow()

    private val _history = MutableStateFlow<List<CalculationHistoryItem>>(emptyList())
    val history: StateFlow<List<CalculationHistoryItem>> = _history.asStateFlow()

    private val _activeExplanation = MutableStateFlow<List<MathStep>?>(null)
    val activeExplanation: StateFlow<List<MathStep>?> = _activeExplanation.asStateFlow()

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

    // --- AI Solver State ---
    private val _aiQuery = MutableStateFlow("")
    val aiQuery: StateFlow<String> = _aiQuery.asStateFlow()

    private val _aiSolution = MutableStateFlow<AiProblemSolution?>(null)
    val aiSolution: StateFlow<AiProblemSolution?> = _aiSolution.asStateFlow()

    private val _isSolving = MutableStateFlow(false)
    val isSolving: StateFlow<Boolean> = _isSolving.asStateFlow()

    // --- Grapher State ---
    private val _graphExpression = MutableStateFlow("sin(x)")
    val graphExpression: StateFlow<String> = _graphExpression.asStateFlow()

    val presetFunctions = listOf(
        PresetFunction("Sine Wave", "sin(x)", "Periodic harmonic oscillator"),
        PresetFunction("Cosine Wave", "cos(x)", "Phase-shifted harmonic wave"),
        PresetFunction("Parabola", "x^2 - 4", "Quadratic function with roots at x = ±2"),
        PresetFunction("Cubic Curve", "x^3 - 3*x", "Polynomial with local maxima and minima"),
        PresetFunction("Gaussian Bell", "exp(-x^2)", "Standard normal probability density shape"),
        PresetFunction("Damped Oscillation", "sin(3*x)/(1 + x^2)", "Decaying periodic oscillation"),
        PresetFunction("Absolute V", "abs(x)", "V-shaped piecewise linear function")
    )

    // --- Unit Converter State ---
    private val _converterCategory = MutableStateFlow(UnitCategoryType.LENGTH)
    val converterCategory: StateFlow<UnitCategoryType> = _converterCategory.asStateFlow()

    private val _fromUnit = MutableStateFlow(UnitConverterData.categories[UnitCategoryType.LENGTH]!![0]) // Meter
    val fromUnit: StateFlow<ConversionUnit> = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow(UnitConverterData.categories[UnitCategoryType.LENGTH]!![1]) // Kilometer
    val toUnit: StateFlow<ConversionUnit> = _toUnit.asStateFlow()

    private val _converterInput = MutableStateFlow("1")
    val converterInput: StateFlow<String> = _converterInput.asStateFlow()

    private val _converterOutput = MutableStateFlow("0.001")
    val converterOutput: StateFlow<String> = _converterOutput.asStateFlow()

    init {
        // Pre-populate with sample calculations for a lively first-launch experience
        _history.value = listOf(
            CalculationHistoryItem(
                expression = "250 * 1.18",
                result = "295",
                isScientific = false
            ),
            CalculationHistoryItem(
                expression = "sqrt(144) + 5^2",
                result = "37",
                isScientific = true
            )
        )
        // Solve an initial preset in AI Solver for first launch demonstration
        solveAiProblem("Split $120 dinner bill between 4 friends with 18% tip")
    }

    // --- Calculator Actions ---

    fun onAppend(text: String) {
        val current = _expression.value
        // Prevent duplicate decimals in current number segment
        if (text == ".") {
            val lastToken = current.takeLastWhile { it.isDigit() || it == '.' }
            if (lastToken.contains(".")) return
        }
        _expression.value = current + text
        updateLiveResult()
    }

    fun onBackspace() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = current.dropLast(1)
            updateLiveResult()
        }
    }

    fun onClear() {
        _expression.value = ""
        _liveResult.value = null
    }

    fun onToggleScientific() {
        _isScientific.value = !_isScientific.value
    }

    fun onToggleAngleMode() {
        val newMode = !_isRadian.value
        _isRadian.value = newMode
        ExpressionEvaluator.isRadianMode = newMode
        updateLiveResult()
    }

    fun onCalculate() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) return

        try {
            val resultNum = ExpressionEvaluator.evaluate(expr, isRadian = _isRadian.value)
            val formatted = ExpressionEvaluator.formatNumber(resultNum)
            val historyItem = CalculationHistoryItem(
                expression = expr,
                result = formatted,
                isScientific = _isScientific.value
            )
            _history.value = listOf(historyItem) + _history.value
            _expression.value = formatted
            _liveResult.value = null
        } catch (e: Exception) {
            _liveResult.value = e.localizedMessage ?: "Error"
        }
    }

    fun explainCurrentExpression() {
        val expr = _expression.value.ifBlank {
            _history.value.firstOrNull()?.expression ?: "12 + 5 * (8 - 2)^2"
        }
        _activeExplanation.value = ExpressionEvaluator.explainSteps(expr, isRadian = _isRadian.value)
    }

    fun explainHistoryItem(item: CalculationHistoryItem) {
        _activeExplanation.value = ExpressionEvaluator.explainSteps(item.expression, isRadian = _isRadian.value)
    }

    fun dismissExplanation() {
        _activeExplanation.value = null
    }

    fun loadFromHistory(item: CalculationHistoryItem) {
        _expression.value = item.result
        updateLiveResult()
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    // Memory operations
    fun memoryAdd() {
        val currentVal = _expression.value.toDoubleOrNull() ?: ExpressionEvaluator.tryEvaluate(_expression.value) ?: 0.0
        _memoryValue.value += currentVal
    }

    fun memorySubtract() {
        val currentVal = _expression.value.toDoubleOrNull() ?: ExpressionEvaluator.tryEvaluate(_expression.value) ?: 0.0
        _memoryValue.value -= currentVal
    }

    fun memoryRecall() {
        _expression.value = ExpressionEvaluator.formatNumber(_memoryValue.value)
        updateLiveResult()
    }

    fun memoryClear() {
        _memoryValue.value = 0.0
    }

    private fun updateLiveResult() {
        val expr = _expression.value.trim()
        if (expr.isBlank()) {
            _liveResult.value = null
            return
        }
        val preview = ExpressionEvaluator.tryEvaluate(expr, isRadian = _isRadian.value)
        _liveResult.value = preview?.let { ExpressionEvaluator.formatNumber(it) }
    }

    // --- AI Solver Actions ---

    fun onAiQueryChange(newQuery: String) {
        _aiQuery.value = newQuery
    }

    fun solveAiProblem(query: String = _aiQuery.value) {
        if (query.isBlank()) return
        _aiQuery.value = query
        viewModelScope.launch {
            _isSolving.value = true
            // Slight delay gives a smooth natural AI thinking effect
            delay(250)
            val solution = AiMathSolver.solve(query)
            _aiSolution.value = solution
            _isSolving.value = false
        }
    }

    // --- Grapher Actions ---

    fun onGraphExpressionChange(expr: String) {
        _graphExpression.value = expr
    }

    fun selectGraphPreset(preset: PresetFunction) {
        _graphExpression.value = preset.expression
    }

    // --- Unit Converter Actions ---

    fun setConverterCategory(category: UnitCategoryType) {
        _converterCategory.value = category
        val units = UnitConverterData.categories[category] ?: return
        _fromUnit.value = units[0]
        _toUnit.value = if (units.size > 1) units[1] else units[0]
        recalculateConversion()
    }

    fun setFromUnit(unit: ConversionUnit) {
        _fromUnit.value = unit
        recalculateConversion()
    }

    fun setToUnit(unit: ConversionUnit) {
        _toUnit.value = unit
        recalculateConversion()
    }

    fun swapUnits() {
        val temp = _fromUnit.value
        _fromUnit.value = _toUnit.value
        _toUnit.value = temp
        recalculateConversion()
    }

    fun onConverterInputChange(value: String) {
        _converterInput.value = value
        recalculateConversion()
    }

    private fun recalculateConversion() {
        val num = _converterInput.value.toDoubleOrNull() ?: 0.0
        val converted = UnitConverterData.convert(num, _fromUnit.value, _toUnit.value)
        _converterOutput.value = ExpressionEvaluator.formatNumber(converted)
    }
}

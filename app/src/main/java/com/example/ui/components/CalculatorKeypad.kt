package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CalculatorKeypad(
    isScientific: Boolean,
    isRadian: Boolean,
    onAppend: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCalculate: () -> Unit,
    onToggleAngleMode: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun performHaptic() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(18)
                }
            }
        } catch (_: Exception) {
            // Ignore if vibrator unavailable
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Memory bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MemoryButton(text = "MC", onClick = { performHaptic(); onMemoryClear() })
            MemoryButton(text = "MR", onClick = { performHaptic(); onMemoryRecall() })
            MemoryButton(text = "M+", onClick = { performHaptic(); onMemoryAdd() })
            MemoryButton(text = "M-", onClick = { performHaptic(); onMemorySubtract() })
            MemoryButton(
                text = if (isRadian) "RAD" else "DEG",
                onClick = { performHaptic(); onToggleAngleMode() },
                isHighlight = true
            )
        }

        // Scientific Extended Panel
        AnimatedVisibility(
            visible = isScientific,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScientificKey("sin", "sin(") { performHaptic(); onAppend("sin(") }
                    ScientificKey("cos", "cos(") { performHaptic(); onAppend("cos(") }
                    ScientificKey("tan", "tan(") { performHaptic(); onAppend("tan(") }
                    ScientificKey("ln", "ln(") { performHaptic(); onAppend("ln(") }
                    ScientificKey("log", "log(") { performHaptic(); onAppend("log(") }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScientificKey("√", "sqrt(") { performHaptic(); onAppend("sqrt(") }
                    ScientificKey("x²", "^2") { performHaptic(); onAppend("^2") }
                    ScientificKey("xʸ", "^") { performHaptic(); onAppend("^") }
                    ScientificKey("π", "π") { performHaptic(); onAppend("π") }
                    ScientificKey("e", "e") { performHaptic(); onAppend("e") }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScientificKey("asin", "asin(") { performHaptic(); onAppend("asin(") }
                    ScientificKey("acos", "acos(") { performHaptic(); onAppend("acos(") }
                    ScientificKey("atan", "atan(") { performHaptic(); onAppend("atan(") }
                    ScientificKey("n!", "!") { performHaptic(); onAppend("!") }
                    ScientificKey("|x|", "abs(") { performHaptic(); onAppend("abs(") }
                }
            }
        }

        // Standard 5-row Keypad
        // Row 1: AC, ( ), %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeyButton(
                text = "AC",
                type = KeyType.CLEAR,
                modifier = Modifier.weight(1f),
                onClick = { performHaptic(); onClear() }
            )
            KeyButton(
                text = "(",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f),
                onClick = { performHaptic(); onAppend("(") }
            )
            KeyButton(
                text = ")",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f),
                onClick = { performHaptic(); onAppend(")") }
            )
            KeyButton(
                text = "%",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f),
                onClick = { performHaptic(); onAppend("%") }
            )
            KeyButton(
                text = "÷",
                type = KeyType.OPERATOR,
                modifier = Modifier.weight(1f),
                onClick = { performHaptic(); onAppend("÷") }
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeyButton("7", KeyType.NUMBER, Modifier.weight(1f)) { performHaptic(); onAppend("7") }
            KeyButton("8", KeyType.NUMBER, Modifier.weight(1f)) { performHaptic(); onAppend("8") }
            KeyButton("9", KeyType.NUMBER, Modifier.weight(1f)) { performHaptic(); onAppend("9") }
            KeyButton("×", KeyType.OPERATOR, Modifier.weight(1f)) { performHaptic(); onAppend("×") }
        }

        // Row 3: 4, 5, 6, -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeyButton("4", KeyType.NUMBER, Modifier.weight(1f)) { performHaptic(); onAppend("4") }
            KeyButton("5", KeyType.NUMBER, Modifier.weight(1f)) { performHaptic(); onAppend("5") }
            KeyButton("6", KeyType.NUMBER, Modifier.weight(1f)) { performHaptic(); onAppend("6") }
            KeyButton("−", KeyType.OPERATOR, Modifier.weight(1f)) { performHaptic(); onAppend("-") }
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeyButton("1", KeyType.NUMBER, Modifier.weight(1f)) { performHaptic(); onAppend("1") }
            KeyButton("2", KeyType.NUMBER, Modifier.weight(1f)) { performHaptic(); onAppend("2") }
            KeyButton("3", KeyType.NUMBER, Modifier.weight(1f)) { performHaptic(); onAppend("3") }
            KeyButton("+", KeyType.OPERATOR, Modifier.weight(1f)) { performHaptic(); onAppend("+") }
        }

        // Row 5: 0, ., ⌫, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeyButton("0", KeyType.NUMBER, Modifier.weight(1f)) { performHaptic(); onAppend("0") }
            KeyButton(".", KeyType.NUMBER, Modifier.weight(1f)) { performHaptic(); onAppend(".") }
            IconButtonKey(
                icon = { Icon(Icons.Default.Backspace, contentDescription = "Backspace", tint = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier.weight(1f),
                onClick = { performHaptic(); onBackspace() }
            )
            KeyButton(
                text = "=",
                type = KeyType.EQUALS,
                modifier = Modifier.weight(1f),
                onClick = { performHaptic(); onCalculate() }
            )
        }
    }
}

enum class KeyType {
    NUMBER, OPERATOR, FUNCTION, CLEAR, EQUALS
}

@Composable
fun RowScope.KeyButton(
    text: String,
    type: KeyType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == SlateBackground

    val bgColor = when (type) {
        KeyType.NUMBER -> if (isDark) KeyNumberDark else LightKeyNumber
        KeyType.OPERATOR -> if (isDark) KeyOperatorDark else LightKeyOperator
        KeyType.FUNCTION -> if (isDark) KeyFunctionDark else LightSurfaceElevated
        KeyType.CLEAR -> CoralAccent.copy(alpha = 0.2f)
        KeyType.EQUALS -> KeyEquals
    }

    val contentColor = when (type) {
        KeyType.NUMBER -> MaterialTheme.colorScheme.onSurface
        KeyType.OPERATOR -> NeonCyan
        KeyType.FUNCTION -> ElectricViolet
        KeyType.CLEAR -> CoralAccent
        KeyType.EQUALS -> KeyEqualsContent
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = modifier
            .height(56.dp)
            .testTag("key_$text")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = contentColor,
                fontSize = if (type == KeyType.NUMBER) 22.sp else 20.sp,
                fontWeight = if (type == KeyType.EQUALS || type == KeyType.OPERATOR) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun RowScope.IconButtonKey(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == SlateBackground
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) KeyFunctionDark else LightSurfaceElevated,
        modifier = modifier
            .height(56.dp)
            .testTag("key_backspace")
    ) {
        Box(contentAlignment = Alignment.Center) {
            icon()
        }
    }
}

@Composable
fun RowScope.ScientificKey(
    label: String,
    insertValue: String,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == SlateBackground
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) MidnightCardElevated else LightSurfaceElevated,
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("key_sci_$label")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RowScope.MemoryButton(
    text: String,
    onClick: () -> Unit,
    isHighlight: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isHighlight) ElectricViolet.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .weight(1f)
            .height(34.dp)
            .testTag("key_mem_$text")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isHighlight) ElectricViolet else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

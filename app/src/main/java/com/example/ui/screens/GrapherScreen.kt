package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.GraphEvaluator
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.NeonCyan
import com.example.viewmodel.CalculatorViewModel

@Composable
fun GrapherScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expr by viewModel.graphExpression.collectAsState()
    var inspectX by remember { mutableStateOf<Double?>(null) }
    var inspectY by remember { mutableStateOf<Double?>(null) }

    val minX = -8.0
    val maxX = 8.0
    val minY = -5.0
    val maxY = 5.0

    // Evaluate points when expr changes
    val points = remember(expr) {
        GraphEvaluator.evaluateRange(expr, minX = minX, maxX = maxX, steps = 180)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Function Input Field
        OutlinedTextField(
            value = expr,
            onValueChange = {
                viewModel.onGraphExpressionChange(it)
                inspectX = null
                inspectY = null
            },
            label = { Text("Function f(x) =") },
            leadingIcon = {
                Icon(
                    Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = NeonCyan
                )
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("graph_function_input")
        )

        // Preset Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.presetFunctions.forEach { preset ->
                FilterChip(
                    selected = expr == preset.expression,
                    onClick = {
                        viewModel.selectGraphPreset(preset)
                        inspectX = null
                        inspectY = null
                    },
                    label = { Text(preset.name, fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Live Coordinate Inspector Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Touch/drag canvas to inspect",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (inspectX != null && inspectY != null) {
                    Text(
                        text = "x: ${String.format(java.util.Locale.US, "%.2f", inspectX)}, y: ${String.format(java.util.Locale.US, "%.2f", inspectY)}",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Text(
                        text = "Domain: [-8, 8]",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Interactive Graph Canvas
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("interactive_graph_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .pointerInput(expr) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val normX = offset.x / width
                            val mathX = minX + normX * (maxX - minX)
                            inspectX = mathX
                            inspectY = GraphEvaluator.evaluateAt(expr, mathX)
                        }
                    }
                    .pointerInput(expr) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val width = size.width
                            val normX = (change.position.x / width).coerceIn(0f, 1f)
                            val mathX = minX + normX * (maxX - minX)
                            inspectX = mathX
                            inspectY = GraphEvaluator.evaluateAt(expr, mathX)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                val toScreenX: (Double) -> Float = { x ->
                    ((x - minX) / (maxX - minX) * w).toFloat()
                }
                val toScreenY: (Double) -> Float = { y ->
                    (h - (y - minY) / (maxY - minY) * h).toFloat()
                }

                val gridColor = Color(0xFF334155).copy(alpha = 0.35f)
                val axisColor = Color(0xFF64748B)

                // Grid lines
                for (x in -8..8 step 2) {
                    val sx = toScreenX(x.toDouble())
                    drawLine(gridColor, Offset(sx, 0f), Offset(sx, h), strokeWidth = 1f)
                }
                for (y in -4..4 step 2) {
                    val sy = toScreenY(y.toDouble())
                    drawLine(gridColor, Offset(0f, sy), Offset(w, sy), strokeWidth = 1f)
                }

                // X & Y Principal Axes
                val originX = toScreenX(0.0)
                val originY = toScreenY(0.0)
                drawLine(axisColor, Offset(0f, originY), Offset(w, originY), strokeWidth = 2f)
                drawLine(axisColor, Offset(originX, 0f), Offset(originX, h), strokeWidth = 2f)

                // Plot Curve Path
                if (points.isNotEmpty()) {
                    val path = Path()
                    var isFirst = true

                    for (pt in points) {
                        val sx = toScreenX(pt.x)
                        val sy = toScreenY(pt.y).coerceIn(-100f, h + 100f)

                        if (isFirst) {
                            path.moveTo(sx, sy)
                            isFirst = false
                        } else {
                            path.lineTo(sx, sy)
                        }
                    }

                    drawPath(
                        path = path,
                        color = NeonCyan,
                        style = Stroke(width = 3.5f)
                    )
                }

                // Inspector point marker
                if (inspectX != null && inspectY != null) {
                    val ix = toScreenX(inspectX!!)
                    val iy = toScreenY(inspectY!!).coerceIn(0f, h)

                    // Vertical tracking guideline
                    drawLine(
                        color = ElectricViolet.copy(alpha = 0.6f),
                        start = Offset(ix, 0f),
                        end = Offset(ix, h),
                        strokeWidth = 1.5f
                    )
                    // Marker circle
                    drawCircle(
                        color = ElectricViolet,
                        radius = 8f,
                        center = Offset(ix, iy)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(ix, iy)
                    )
                }
            }
        }
    }
}

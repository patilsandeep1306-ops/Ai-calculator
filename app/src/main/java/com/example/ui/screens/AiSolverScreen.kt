package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AiSolutionCard
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.viewmodel.CalculatorViewModel

@Composable
fun AiSolverScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.aiQuery.collectAsState()
    val solution by viewModel.aiSolution.collectAsState()
    val isSolving by viewModel.isSolving.collectAsState()
    val focusManager = LocalFocusManager.current

    val quickPrompts = listOf(
        "Split $140 bill between 4 friends with 18% tip",
        "Loan $250,000 at 6.5% for 30 years",
        "Compound interest on $5,000 at 7% for 4 years",
        "30% off $160 with 8% sales tax",
        "x^2 - 5x + 6 = 0",
        "3x + 15 = 45",
        "Circle area with radius 7",
        "Hypotenuse for sides 6 and 8",
        "BMI for weight 70 kg and height 1.75 m",
        "Drive 240 miles in 3.5 hours average speed",
        "Stats 12, 18, 25, 30, 18, 42"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Hero Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeonCyan.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyan
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "AI Math Problem Solver",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ask word problems, loans, tips, algebra, geometry or physics in natural language.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Input Search Bar
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onAiQueryChange(it) },
                placeholder = {
                    Text(
                        "e.g. Split $150 among 4 with 20% tip",
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricViolet
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onAiQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.solveAiProblem(query)
                            },
                            modifier = Modifier.testTag("submit_ai_problem")
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Solve",
                                tint = NeonCyan
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    viewModel.solveAiProblem(query)
                }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_problem_input")
            )
        }

        // Quick Suggestions Horizontal Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "QUICK EXAMPLES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPrompts.forEach { prompt ->
                        SuggestionChip(
                            onClick = {
                                viewModel.onAiQueryChange(prompt)
                                viewModel.solveAiProblem(prompt)
                            },
                            label = { Text(prompt, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Loading Indicator or Solution Card
        item {
            if (isSolving) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = NeonCyan)
                        Text(
                            text = "AI is analyzing and solving...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (solution != null) {
                AiSolutionCard(solution = solution!!)
            }
        }
    }
}

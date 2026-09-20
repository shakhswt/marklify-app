package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Question
import com.example.data.entity.TestEntity
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.LiquidGlassBackdrop
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDetailScreen(
    testId: Long,
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTestEditor: (Long) -> Unit,
    onNavigateToSheetGenerator: (Long) -> Unit,
    onNavigateToScanner: (Long) -> Unit,
    onNavigateToResults: (Long) -> Unit
) {
    var test by remember { mutableStateOf<TestEntity?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    val questions by viewModel.getQuestionsForTest(testId).collectAsState(initial = emptyList())

    LaunchedEffect(testId) {
        test = viewModel.getTestById(testId)
    }

    LiquidGlassBackdrop(animated = false) {
        Scaffold(
            topBar = {
                LiquidGlassTopAppBar(
                    title = { Text(test?.name ?: "Test Details", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showRenameDialog = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename Test", tint = TextSecondary)
                        }
                        IconButton(onClick = { onNavigateToResults(testId) }) {
                            Icon(imageVector = Icons.Default.Analytics, contentDescription = "Past Results", tint = TextSecondary)
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    // Info Summary Card
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        fillColor = GlassFill,
                        showSpecular = true
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = test?.name ?: "Loading...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${test?.questionCount ?: 0} Questions  •  4 Options (A, B, C, D)",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LiquidGlassButton(
                                    onClick = { onNavigateToTestEditor(testId) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("edit_questions_button"),
                                    variant = GlassButtonVariant.Neutral,
                                    icon = Icons.Default.Edit,
                                    text = "Edit Keys"
                                )

                                LiquidGlassButton(
                                    onClick = { onNavigateToSheetGenerator(testId) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("generate_sheet_detail_button"),
                                    variant = GlassButtonVariant.Neutral,
                                    icon = Icons.Default.Print,
                                    text = "Blank Sheet"
                                )

                                LiquidGlassButton(
                                    onClick = {
                                        viewModel.loadTestForScan(testId) {
                                            onNavigateToScanner(testId)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("scan_sheet_detail_button"),
                                    variant = GlassButtonVariant.Success,
                                    icon = Icons.Default.CameraAlt,
                                    text = "Scan"
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Questions & Answer Keys (${questions.size})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        TextButton(onClick = { onNavigateToResults(testId) }) {
                            Text("View Results", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }

                items(questions, key = { it.id }) { q ->
                    QuestionPreviewCard(question = q)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showRenameDialog && test != null) {
        var renameText by remember(test) { mutableStateOf(test?.name ?: "") }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = FrostedBarBackground,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Rename Test", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("New Test Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                LiquidGlassButton(
                    text = "Rename",
                    enabled = renameText.isNotBlank(),
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameTest(testId, renameText)
                            test = test?.copy(name = renameText.trim())
                            showRenameDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun QuestionPreviewCard(question: Question) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        fillColor = GlassFill,
        showSpecular = false,
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(GlassFillElevated)
                        .border(1.dp, GlassBorderBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${question.questionNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = question.questionText.ifBlank { "Question #${question.questionNumber}" },
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "A: ${question.optionA}  •  B: ${question.optionB}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            // Key Bubble: Neutral frosted glass badge
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(GlassFillElevated)
                    .border(1.dp, GlassBorderBright, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = question.correctAnswer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            }
        }
    }
}

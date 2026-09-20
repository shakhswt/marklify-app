package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
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
fun TestEditorScreen(
    testId: Long,
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit
) {
    var test by remember { mutableStateOf<TestEntity?>(null) }
    val initialQuestions by viewModel.getQuestionsForTest(testId).collectAsState(initial = emptyList())
    var editableQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var hasInitialized by remember { mutableStateOf(false) }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(testId) {
        test = viewModel.getTestById(testId)
    }

    LaunchedEffect(initialQuestions) {
        if (!hasInitialized && initialQuestions.isNotEmpty()) {
            editableQuestions = initialQuestions
            hasInitialized = true
        }
    }

    LiquidGlassBackdrop(animated = false) {
        Scaffold(
            topBar = {
                LiquidGlassTopAppBar(
                    title = {
                        Column {
                            Text(test?.name ?: "Edit Questions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${editableQuestions.size} Questions (1–100)", fontSize = 11.sp, color = TextSecondary)
                        }
                    },
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
                        LiquidGlassButton(
                            onClick = {
                                viewModel.updateQuestionsBatch(editableQuestions)
                                showSavedSnackbar = true
                            },
                            modifier = Modifier.testTag("save_questions_button"),
                            variant = GlassButtonVariant.Neutral,
                            icon = Icons.Default.Save,
                            text = "Save"
                        )
                    }
                )
            },
            snackbarHost = {
                if (showSavedSnackbar) {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        containerColor = FrostedBarBackground,
                        contentColor = TextPrimary,
                        action = {
                            TextButton(onClick = { showSavedSnackbar = false }) {
                                Text("OK", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    ) {
                        Text("All questions & answer keys saved successfully!")
                    }
                }
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
                // Adjust question count row
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        fillColor = GlassFill,
                        showSpecular = true
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Question Count", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                Text("Quickly adjust number of items", fontSize = 11.sp, color = TextSecondary)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(10, 20, 50, 100).forEach { count ->
                                    val isSelected = editableQuestions.size == count
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) GlassFillElevated else GlassFillSubtle)
                                            .border(
                                                1.dp,
                                                if (isSelected) GlassBorderBright else GlassBorderSubtle,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                viewModel.setTestQuestionCount(testId, count)
                                                hasInitialized = false
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) TextPrimary else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                itemsIndexed(editableQuestions, key = { _, q -> q.id }) { index, q ->
                    QuestionEditorCard(
                        question = q,
                        onUpdate = { updated ->
                            val updatedList = editableQuestions.toMutableList()
                            updatedList[index] = updated
                            editableQuestions = updatedList
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun QuestionEditorCard(
    question: Question,
    onUpdate: (Question) -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_${question.questionNumber}"),
        shape = RoundedCornerShape(20.dp),
        fillColor = GlassFill,
        showSpecular = false,
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(GlassFillElevated)
                            .border(1.dp, GlassBorderBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${question.questionNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Question #${question.questionNumber}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }

                // Correct Answer key selector: Saturated color ONLY for semantic selected key
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Key:", fontSize = 12.sp, color = TextSecondary)
                    listOf("A", "B", "C", "D").forEach { letter ->
                        val isSelected = question.correctAnswer.equals(letter, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SuccessGreenBg else GlassFillSubtle)
                                .border(
                                    1.dp,
                                    if (isSelected) SuccessGreenBorder else GlassBorderSubtle,
                                    CircleShape
                                )
                                .clickable {
                                    onUpdate(question.copy(correctAnswer = letter))
                                }
                                .testTag("q_${question.questionNumber}_key_$letter"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) SuccessGreen else TextSecondary
                            )
                        }
                    }
                }
            }

            // Question Text Input
            OutlinedTextField(
                value = question.questionText,
                onValueChange = { onUpdate(question.copy(questionText = it)) },
                label = { Text("Question Text (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Options A, B
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = question.optionA,
                    onValueChange = { onUpdate(question.copy(optionA = it)) },
                    label = { Text("Option A") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = question.optionB,
                    onValueChange = { onUpdate(question.copy(optionB = it)) },
                    label = { Text("Option B") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Options C, D
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = question.optionC,
                    onValueChange = { onUpdate(question.copy(optionC = it)) },
                    label = { Text("Option C") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = question.optionD,
                    onValueChange = { onUpdate(question.copy(optionD = it)) },
                    label = { Text("Option D") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
    }
}

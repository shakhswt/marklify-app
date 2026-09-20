package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.Question
import com.example.data.entity.TestEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import com.example.util.HapticManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestEditorScreen(
    testId: Long,
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val appSettings by viewModel.appSettings.collectAsState()
    var test by remember { mutableStateOf<TestEntity?>(null) }
    val initialQuestions by viewModel.getQuestionsForTest(testId).collectAsState(initial = emptyList())
    var editableQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }

    var selectedTab by remember { mutableIntStateOf(1) } // 0: Sections, 1: Answer Key (Screenshot #7)
    var selectedExamSet by remember { mutableStateOf("A") }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(testId) {
        test = viewModel.getTestById(testId)
    }

    LaunchedEffect(initialQuestions) {
        if (initialQuestions.isNotEmpty()) {
            editableQuestions = initialQuestions
        }
    }

    Scaffold(
        topBar = {
            MarklifyTopAppBar(
                title = {
                    Column {
                        Text(if (selectedTab == 1) "Answer Key" else "Configure Sections", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(test?.name ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.width(180.dp),
                        containerColor = Color.Transparent
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Sections", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Key", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MarklifyButton(
                        text = "Clear",
                        onClick = {
                            editableQuestions = editableQuestions.map { it.copy(correctAnswer = "") }
                        },
                        modifier = Modifier.weight(1f),
                        variant = MarklifyButtonVariant.Outlined
                    )

                    MarklifyButton(
                        text = "Save",
                        onClick = {
                            viewModel.updateQuestionsBatch(editableQuestions)
                            HapticManager.performSubmissionSuccess(context, appSettings.hapticsEnabled)
                            showSavedSnackbar = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_answer_key_button"),
                        variant = MarklifyButtonVariant.Primary,
                        icon = Icons.Default.Save
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Select Set Header (EvalBee Screenshot #7 Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Set",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("A", "B", "C", "D").forEach { setLetter ->
                        MarklifyChip(
                            selected = selectedExamSet == setLetter,
                            onClick = { selectedExamSet = setLetter },
                            label = setLetter
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Answer Key Questions List (Screenshot #7 Style)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(editableQuestions, key = { _, q -> q.id }) { index, q ->
                    AnswerKeyItemRow(
                        question = q,
                        onKeySelected = { newKey ->
                            val list = editableQuestions.toMutableList()
                            list[index] = q.copy(correctAnswer = newKey)
                            editableQuestions = list
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerKeyItemRow(
    question: Question,
    onKeySelected: (String) -> Unit
) {
    MarklifyCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Question Number on Left
            Text(
                text = "${question.questionNumber}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(36.dp)
            )

            // Key Selection Bubbles (Screenshot #7 Style)
            when (question.questionType) {
                "MCQ5" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("A", "B", "C", "D", "E").forEach { letter ->
                            val isSelected = question.correctAnswer.equals(letter, ignoreCase = true)
                            KeyOptionCircle(
                                label = letter,
                                selected = isSelected,
                                onClick = { onKeySelected(letter) }
                            )
                        }
                    }
                }
                "TRUE_FALSE" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("True", "False").forEach { tf ->
                            val isSelected = question.correctAnswer.equals(tf, ignoreCase = true)
                            KeyOptionPill(
                                label = tf,
                                selected = isSelected,
                                onClick = { onKeySelected(tf) }
                            )
                        }
                    }
                }
                "NUMERICAL" -> {
                    OutlinedTextField(
                        value = question.correctAnswer,
                        onValueChange = onKeySelected,
                        modifier = Modifier.width(120.dp),
                        singleLine = true,
                        placeholder = { Text("42") }
                    )
                }
                else -> { // MCQ4
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("A", "B", "C", "D").forEach { letter ->
                            val isSelected = question.correctAnswer.equals(letter, ignoreCase = true)
                            KeyOptionCircle(
                                label = letter,
                                selected = isSelected,
                                onClick = { onKeySelected(letter) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyOptionCircle(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (selected) SuccessGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun KeyOptionPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(80.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (selected) SuccessGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

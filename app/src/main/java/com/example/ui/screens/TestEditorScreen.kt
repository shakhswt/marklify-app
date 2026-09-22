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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Setup, 1: Sections, 2: Answer Key
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
                        val titleText = when (selectedTab) {
                            0 -> stringResource(R.string.exam_setup)
                            1 -> stringResource(R.string.configure_sections)
                            else -> stringResource(R.string.answer_key)
                        }
                        Text(titleText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                        modifier = Modifier.width(220.dp),
                        containerColor = Color.Transparent
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text(stringResource(R.string.setup_tab), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(stringResource(R.string.sections_tab), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text(stringResource(R.string.key_tab), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
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
                        text = stringResource(R.string.none_answer),
                        onClick = {
                            editableQuestions = editableQuestions.map { it.copy(correctAnswer = "") }
                        },
                        modifier = Modifier.weight(1f),
                        variant = MarklifyButtonVariant.Outlined
                    )

                    MarklifyButton(
                        text = stringResource(R.string.save),
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
        ) {
            if (selectedTab == 0) {
                // EXAM SETUP WIZARD
                var rollDigits by remember { mutableIntStateOf(test?.numRollDigits ?: 5) }
                var examSets by remember { mutableIntStateOf(test?.numExamSets ?: 1) }
                var subjectsCount by remember { mutableIntStateOf(1) }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.exam_configuration), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    MarklifyCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.roll_no_digits), fontSize = 15.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { if (rollDigits > 1) rollDigits-- }) { Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                                    Text("$rollDigits", fontSize = 16.sp, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    IconButton(onClick = { if (rollDigits < 8) rollDigits++ }) { Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.exam_sets), fontSize = 15.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { if (examSets > 1) examSets-- }) { Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                                    Text("$examSets", fontSize = 16.sp, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    IconButton(onClick = { if (examSets < 6) examSets++ }) { Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.subjects_label), fontSize = 15.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { if (subjectsCount > 1) subjectsCount-- }) { Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                                    Text("$subjectsCount", fontSize = 16.sp, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    IconButton(onClick = { if (subjectsCount < 10) subjectsCount++ }) { Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.subject_summary), fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    MarklifyCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Text(stringResource(R.string.sr_no), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(stringResource(R.string.subject_col), modifier = Modifier.weight(3f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(stringResource(R.string.sections_col), modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            HorizontalDivider()
                            for (i in 1..subjectsCount) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("$i", modifier = Modifier.weight(1f), fontSize = 14.sp)
                                    OutlinedTextField(
                                        value = stringResource(R.string.subject_name_format, i),
                                        onValueChange = {},
                                        modifier = Modifier.weight(3f).height(48.dp).padding(end = 8.dp),
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 14.sp)
                                    )
                                    OutlinedTextField(
                                        value = "1",
                                        onValueChange = {},
                                        modifier = Modifier.weight(2f).height(48.dp),
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 14.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    // Select Set Header (EvalBee Screenshot #7 Style)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.select_set),
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

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

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
    }
}

@Composable
fun AnswerKeyItemRow(
    question: Question,
    onKeySelected: (String) -> Unit
) {
    MarklifyCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${question.questionNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Option selection (A/B/C/D or True/False depending on type)
            when (question.questionType) {
                "TRUE_FALSE" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("True", "False").forEach { option ->
                            KeyOptionPill(
                                label = option,
                                selected = question.correctAnswer.equals(option, ignoreCase = true),
                                onClick = { onKeySelected(option) }
                            )
                        }
                    }
                }
                else -> {
                    val options = if (question.questionType == "MCQ5") listOf("A", "B", "C", "D", "E") else listOf("A", "B", "C", "D")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        options.forEach { option ->
                            KeyOptionCircle(
                                label = option,
                                selected = question.correctAnswer.equals(option, ignoreCase = true),
                                onClick = { onKeySelected(option) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyOptionCircle(
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
fun KeyOptionPill(
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

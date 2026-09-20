package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.Question
import com.example.omr.processing.DetectedQuestionAnswer
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import com.example.util.HapticManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit,
    onConfirmAndScore: (Long) -> Unit
) {
    val context = LocalContext.current
    val appSettings by viewModel.appSettings.collectAsState()
    val test by viewModel.selectedTest.collectAsState()
    val questions by viewModel.selectedQuestions.collectAsState()
    val warpedBitmap by viewModel.scannedBitmap.collectAsState()
    val detectedItems by viewModel.detectedAnswersList.collectAsState()
    val reviewAnswers by viewModel.reviewAnswers.collectAsState()

    val studentName by viewModel.studentName.collectAsState()
    val studentId by viewModel.studentId.collectAsState()
    var showFullImageDialog by remember { mutableStateOf(false) }

    val flaggedCount = remember(detectedItems) {
        detectedItems.count { it.isAmbiguous || it.isMultiple }
    }

    Scaffold(
        topBar = {
            MarklifyTopAppBar(
                title = {
                    Column {
                        Text("Review Scan & Score", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = if (flaggedCount > 0) "$flaggedCount items need review" else "All answers clear",
                            fontSize = 12.sp,
                            color = if (flaggedCount > 0) WarningAmber else SuccessGreen
                        )
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Answered: ${reviewAnswers.size} / ${questions.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Student: ${studentName.ifBlank { "Anonymous" }} (Roll: ${studentId.ifBlank { "N/A" }})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    MarklifyButton(
                        text = "Confirm & Score",
                        onClick = {
                            viewModel.confirmAndScore { resultId ->
                                HapticManager.performSubmissionSuccess(context, appSettings.hapticsEnabled)
                                onConfirmAndScore(resultId)
                            }
                        },
                        modifier = Modifier.testTag("confirm_and_score_button"),
                        variant = MarklifyButtonVariant.Primary,
                        icon = Icons.Default.CheckCircle
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Student Details Card
            item {
                Spacer(modifier = Modifier.height(4.dp))
                MarklifyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Student Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = studentName,
                                onValueChange = { viewModel.studentName.value = it },
                                label = { Text("Student Name") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("student_name_input")
                            )
                            OutlinedTextField(
                                value = studentId,
                                onValueChange = { viewModel.studentId.value = it },
                                label = { Text("Roll Number") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("student_id_input")
                            )
                        }
                    }
                }
            }

            // Scanned Sheet Thumbnail
            if (warpedBitmap != null) {
                item {
                    MarklifyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFullImageDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp, 80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    bitmap = warpedBitmap!!.asImageBitmap(),
                                    contentDescription = "Scanned OMR Sheet",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Aligned OMR Sheet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Perspective corrected & verified",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap to enlarge preview",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Verify & Edit Answers",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(questions, key = { it.questionNumber }) { q ->
                val detectedItem = detectedItems.firstOrNull { it.questionNumber == q.questionNumber }
                val currentChosenAnswer = reviewAnswers[q.questionNumber] ?: detectedItem?.detectedAnswer ?: "unanswered"

                ReviewQuestionCard(
                    question = q,
                    detected = detectedItem,
                    chosenAnswer = currentChosenAnswer,
                    onSelectAnswer = { letter ->
                        viewModel.updateReviewAnswer(q.questionNumber, letter)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showFullImageDialog && warpedBitmap != null) {
        MarklifyDialog(
            onDismissRequest = { showFullImageDialog = false }
        ) {
            Text(
                text = "Scanned OMR Sheet Preview",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1200f / 1600f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = warpedBitmap!!.asImageBitmap(),
                    contentDescription = "Full Scanned Sheet",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            MarklifyButton(
                text = stringResource(R.string.close),
                onClick = { showFullImageDialog = false },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReviewQuestionCard(
    question: Question,
    detected: DetectedQuestionAnswer?,
    chosenAnswer: String,
    onSelectAnswer: (String) -> Unit
) {
    MarklifyCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Q#${question.questionNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Key: ${question.correctAnswer}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (detected != null) {
                    when {
                        detected.isMultiple -> MarklifyBadge(text = "Multiple", containerColor = WarningAmberBg, contentColor = WarningAmber)
                        detected.isAmbiguous -> MarklifyBadge(text = "Ambiguous", containerColor = WarningAmberBg, contentColor = WarningAmber)
                        detected.isUnanswered -> MarklifyBadge(text = "Blank", containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        else -> MarklifyBadge(text = "Detected: ${detected.detectedAnswer}", containerColor = SuccessGreenBg, contentColor = SuccessGreen)
                    }
                }
            }

            // Option selection pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("A", "B", "C", "D").forEach { letter ->
                    val isSelected = chosenAnswer.equals(letter, ignoreCase = true)
                    MarklifyChip(
                        selected = isSelected,
                        onClick = { onSelectAnswer(letter) },
                        label = letter,
                        modifier = Modifier.weight(1f)
                    )
                }

                val isUnanswered = chosenAnswer.equals("unanswered", ignoreCase = true)
                MarklifyChip(
                    selected = isUnanswered,
                    onClick = { onSelectAnswer("unanswered") },
                    label = "Blank",
                    modifier = Modifier.weight(1.2f)
                )
            }
        }
    }
}

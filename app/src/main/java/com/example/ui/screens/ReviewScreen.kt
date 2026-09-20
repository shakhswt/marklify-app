package com.example.ui.screens

import android.graphics.Bitmap
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
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.LiquidGlassBackdrop
import com.example.ui.components.LiquidGlassBadge
import com.example.ui.components.LiquidGlassBottomBar
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassDialog
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.components.LiquidGlassTopAppBar
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

    LiquidGlassBackdrop(animated = false) {
        Scaffold(
            topBar = {
                LiquidGlassTopAppBar(
                    title = {
                        Column {
                            Text(stringResource(R.string.review_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                text = if (flaggedCount > 0) stringResource(R.string.flagged_count, flaggedCount) else stringResource(R.string.all_clean),
                                fontSize = 11.sp,
                                color = if (flaggedCount > 0) WarningAmber else SuccessGreen
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = TextPrimary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                LiquidGlassBottomBar {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.answered_ratio, reviewAnswers.size, questions.size),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.student_prefix, studentName.ifBlank { stringResource(R.string.student_anonymous) }),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    LiquidGlassButton(
                        onClick = {
                            viewModel.confirmAndScore { resultId ->
                                HapticManager.performSubmissionSuccess(context, appSettings.hapticsEnabled)
                                onConfirmAndScore(resultId)
                            }
                        },
                        modifier = Modifier.testTag("confirm_and_score_button"),
                        variant = GlassButtonVariant.Success,
                        icon = Icons.Default.CheckCircle,
                        text = stringResource(R.string.confirm_and_score)
                    )
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
                // Student Information Form
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        fillColor = GlassFill,
                        showSpecular = true
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.student_details),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LiquidGlassTextField(
                                    value = studentName,
                                    onValueChange = { viewModel.studentName.value = it },
                                    label = stringResource(R.string.student_name_label),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("student_name_input")
                                )
                                LiquidGlassTextField(
                                    value = studentId,
                                    onValueChange = { viewModel.studentId.value = it },
                                    label = stringResource(R.string.student_id_label),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("student_id_input")
                                )
                            }
                        }
                    }
                }

                // Warped Sheet Image Preview Thumbnail
                if (warpedBitmap != null) {
                    item {
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            fillColor = GlassFill,
                            showSpecular = true,
                            onClick = { showFullImageDialog = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp, 80.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White)
                                            .border(1.dp, GlassBorderBright, RoundedCornerShape(10.dp))
                                    ) {
                                        Image(
                                            bitmap = warpedBitmap!!.asImageBitmap(),
                                            contentDescription = stringResource(R.string.scanned_sheet_capture),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.scanned_sheet_capture),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = stringResource(R.string.top_down_perspective),
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = stringResource(R.string.tap_to_enlarge),
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Question Items with manual overrides
                item {
                    Text(
                        text = stringResource(R.string.verify_answers_hint),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
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
    }

    if (showFullImageDialog && warpedBitmap != null) {
        LiquidGlassDialog(
            onDismissRequest = { showFullImageDialog = false }
        ) {
            Text(
                text = stringResource(R.string.scanned_sheet_capture),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1200f / 1600f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, GlassBorderBright, RoundedCornerShape(14.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = warpedBitmap!!.asImageBitmap(),
                    contentDescription = stringResource(R.string.scanned_sheet_capture),
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                LiquidGlassButton(
                    text = stringResource(R.string.close),
                    onClick = { showFullImageDialog = false }
                )
            }
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
    val isFlagged = detected?.isAmbiguous == true || detected?.isMultiple == true

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        fillColor = GlassFill,
        borderColor = if (isFlagged) WarningAmberBorder else GlassBorder,
        showSpecular = false,
        elevation = 3.dp
    ) {
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
                        text = "Q#${question.questionNumber}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.key_prefix, question.correctAnswer),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Confidence / Flag Tag
                if (detected != null) {
                    when {
                        detected.isMultiple -> {
                            LiquidGlassBadge(
                                text = stringResource(R.string.multiple_marked_flag),
                                textColor = WarningAmber,
                                backgroundColor = WarningAmberBg,
                                borderColor = WarningAmberBorder
                            )
                        }
                        detected.isAmbiguous -> {
                            LiquidGlassBadge(
                                text = stringResource(R.string.ambiguous_flag),
                                textColor = WarningAmber,
                                backgroundColor = WarningAmberBg,
                                borderColor = WarningAmberBorder
                            )
                        }
                        detected.isUnanswered -> {
                            LiquidGlassBadge(
                                text = stringResource(R.string.unanswered_flag),
                                textColor = TextMuted,
                                backgroundColor = GlassFillSubtle,
                                borderColor = GlassBorderSubtle
                            )
                        }
                        else -> {
                            LiquidGlassBadge(
                                text = stringResource(R.string.detected_answer_badge, detected.detectedAnswer),
                                textColor = SuccessGreen,
                                backgroundColor = SuccessGreenBg,
                                borderColor = SuccessGreenBorder
                            )
                        }
                    }
                }
            }

            // Radio/Button Pills: A, B, C, D, None
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("A", "B", "C", "D").forEach { letter ->
                    val isSelected = chosenAnswer.equals(letter, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) GlassFillElevated else GlassFillSubtle)
                            .border(
                                1.dp,
                                if (isSelected) GlassBorderBright else GlassBorderSubtle,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectAnswer(letter) }
                            .testTag("review_q_${question.questionNumber}_$letter"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }

                // Unanswered pill
                val isUnanswered = chosenAnswer.equals("unanswered", ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isUnanswered) WarningAmberBg else GlassFillSubtle)
                        .border(
                            1.dp,
                            if (isUnanswered) WarningAmberBorder else GlassBorderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectAnswer("unanswered") }
                        .testTag("review_q_${question.questionNumber}_none"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.none_answer),
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (isUnanswered) WarningAmber else TextSecondary
                    )
                }
            }
        }
    }
}

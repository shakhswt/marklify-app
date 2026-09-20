package com.example.ui.screens

import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DetectedAnswer
import com.example.data.entity.Question
import com.example.data.entity.ScanResult
import com.example.data.entity.TestEntity
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.LiquidGlassBackdrop
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassTopAppBar
import com.example.ui.components.liquidGlassSurface
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultDetailScreen(
    resultId: Long,
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit
) {
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }
    var test by remember { mutableStateOf<TestEntity?>(null) }
    val detectedAnswers by viewModel.getDetectedAnswersForScan(resultId).collectAsState(initial = emptyList())
    var testQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var showFullImageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(resultId) {
        val result = viewModel.getScanResultById(resultId)
        scanResult = result
        if (result != null) {
            val t = viewModel.getTestById(result.testId)
            test = t
            testQuestions = viewModel.getQuestionsListForTest(result.testId)
        }
    }

    val dateStr = remember(scanResult?.scanTime) {
        val time = scanResult?.scanTime ?: 0L
        if (time > 0) {
            SimpleDateFormat("MMMM d, yyyy  •  HH:mm", Locale.getDefault()).format(Date(time))
        } else ""
    }

    val sheetBitmap = remember(scanResult?.imagePath) {
        scanResult?.imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        }
    }

    val scoreColor = when {
        (scanResult?.percentage ?: 0f) >= 80f -> SuccessGreen
        (scanResult?.percentage ?: 0f) >= 50f -> WarningAmber
        else -> ErrorRed
    }

    val scoreBg = when {
        (scanResult?.percentage ?: 0f) >= 80f -> SuccessGreenBg
        (scanResult?.percentage ?: 0f) >= 50f -> WarningAmberBg
        else -> ErrorRedBg
    }

    val scoreBorder = when {
        (scanResult?.percentage ?: 0f) >= 80f -> SuccessGreenBorder
        (scanResult?.percentage ?: 0f) >= 50f -> WarningAmberBorder
        else -> ErrorRedBorder
    }

    LiquidGlassBackdrop(animated = false) {
        Scaffold(
            topBar = {
                LiquidGlassTopAppBar(
                    title = { Text("Grading Report", fontWeight = FontWeight.Bold) },
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
                        scanResult?.let { res ->
                            IconButton(onClick = {
                                viewModel.deleteScanResult(res)
                                onNavigateBack()
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            val result = scanResult
            if (result == null) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TextPrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        // Score Overview Card
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            fillColor = GlassFill,
                            showSpecular = true
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = result.studentName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Student ID: ${result.studentId}  •  ${test?.name ?: "Test"}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Text(text = dateStr, fontSize = 11.sp, color = TextSecondary)

                                Spacer(modifier = Modifier.height(18.dp))

                                // Large Score Pill
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(scoreBg)
                                        .border(2.dp, scoreBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${result.percentage.toInt()}%",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 26.sp,
                                            color = scoreColor
                                        )
                                        Text(
                                            text = "${result.finalScore.toInt()}/${result.totalQuestions}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // 5 Stat Badges: Correct / Wrong / Multiple / Blank / Review
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatBadge(label = "Correct", count = result.correct, color = SuccessGreen, icon = "✓")
                                    StatBadge(label = "Wrong", count = result.wrong, color = ErrorRed, icon = "✗")
                                    StatBadge(label = "Multiple", count = result.multipleMarked, color = WarningAmber, icon = "⚏")
                                    StatBadge(label = "Blank", count = result.unanswered, color = TextSecondary, icon = "—")
                                    StatBadge(label = "Review", count = result.ambiguous, color = PurpleAccent, icon = "?")
                                }
                            }
                        }
                    }

                    // Warped Sheet Image Preview Thumbnail (if saved)
                    if (sheetBitmap != null) {
                        item {
                            LiquidGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                fillColor = GlassFill,
                                showSpecular = true,
                                onClick = { showFullImageDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp, 72.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .border(1.dp, GlassBorderBright, RoundedCornerShape(8.dp))
                                    ) {
                                        Image(
                                            bitmap = sheetBitmap.asImageBitmap(),
                                            contentDescription = "Scanned Sheet",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text("Captured Sheet Image", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                        Text("Tap to view full scanned paper", fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Question-by-Question Breakdown",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    }

                    // Per-question correctness list
                    items(detectedAnswers, key = { it.id }) { item ->
                        val questionKey = testQuestions.firstOrNull { it.questionNumber == item.questionNumber }?.correctAnswer ?: ""
                        ResultQuestionRow(
                            detected = item,
                            correctKey = questionKey
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (showFullImageDialog && sheetBitmap != null) {
        AlertDialog(
            onDismissRequest = { showFullImageDialog = false },
            containerColor = FrostedBarBackground,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Captured Sheet Photo", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1200f / 1600f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, GlassBorderBright, RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = sheetBitmap.asImageBitmap(),
                        contentDescription = "Full Sheet",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            confirmButton = {
                LiquidGlassButton(
                    text = "Close",
                    onClick = { showFullImageDialog = false }
                )
            }
        )
    }
}

@Composable
private fun StatBadge(
    label: String,
    count: Int,
    color: Color,
    icon: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(GlassFillElevated)
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "$count", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun ResultQuestionRow(
    detected: DetectedAnswer,
    correctKey: String
) {
    val statusColor = when {
        detected.isCorrect -> SuccessGreen
        detected.detectedAnswer.equals("unanswered", ignoreCase = true) -> TextSecondary
        detected.detectedAnswer.equals("ambiguous", ignoreCase = true) -> WarningAmber
        else -> ErrorRed
    }

    val statusIcon = when {
        detected.isCorrect -> "✓"
        detected.detectedAnswer.equals("unanswered", ignoreCase = true) -> "—"
        detected.detectedAnswer.equals("ambiguous", ignoreCase = true) -> "?"
        else -> "✗"
    }

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        fillColor = GlassFill,
        showSpecular = false,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(GlassFillElevated)
                        .border(1.dp, statusColor.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusIcon,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Question #${detected.questionNumber}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = if (correctKey.isNotBlank()) "Correct Key: $correctKey" else "",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Student Answer Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (detected.isCorrect) SuccessGreenBg else GlassFillElevated)
                    .border(1.dp, if (detected.isCorrect) SuccessGreenBorder else GlassBorderSubtle, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Answer: ${detected.detectedAnswer.uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (detected.isCorrect) SuccessGreen else TextPrimary
                )
            }
        }
    }
}

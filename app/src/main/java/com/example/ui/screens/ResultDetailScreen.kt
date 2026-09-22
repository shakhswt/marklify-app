package com.example.ui.screens

import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.DetectedAnswer
import com.example.data.entity.Question
import com.example.data.entity.ScanResult
import com.example.data.entity.TestEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultDetailScreen(
    resultId: Long,
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit
) {
    var currentResultId by remember { mutableLongStateOf(resultId) }
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }
    var test by remember { mutableStateOf<TestEntity?>(null) }
    val detectedAnswers by viewModel.getDetectedAnswersForScan(currentResultId).collectAsState(initial = emptyList())
    var testQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var showFullImageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentResultId) {
        val result = viewModel.getScanResultById(currentResultId)
        scanResult = result
        if (result != null) {
            val t = viewModel.getTestById(result.testId)
            test = t
            testQuestions = viewModel.getQuestionsListForTest(result.testId)
        }
    }

    val allResultsForTest by viewModel.getScanResultsForTest(scanResult?.testId ?: 0L).collectAsState(initial = emptyList())
    val currentIndex = remember(allResultsForTest, currentResultId) {
        allResultsForTest.indexOfFirst { it.id == currentResultId }.coerceAtLeast(0)
    }

    val sheetBitmap = remember(scanResult?.imagePath) {
        scanResult?.imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        }
    }

    Scaffold(
        topBar = {
            MarklifyTopAppBar(
                title = { Text(stringResource(R.string.roll_no_format, scanResult?.studentId ?: "N/A"), fontWeight = FontWeight.Bold) },
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
                    scanResult?.let { res ->
                        IconButton(onClick = {
                            viewModel.deleteScanResult(res)
                            onNavigateBack()
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = ErrorRed)
                        }
                    }
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(R.string.share), tint = MaterialTheme.colorScheme.onSurface)
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
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val prevResult = allResultsForTest.getOrNull(currentIndex - 1)
                    val nextResult = allResultsForTest.getOrNull(currentIndex + 1)

                    Text(
                        text = stringResource(R.string.previous_report),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (prevResult != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.clickable(enabled = prevResult != null) {
                            if (prevResult != null) currentResultId = prevResult.id
                        }
                    )

                    Text(
                        text = stringResource(R.string.report_paging_format, if (allResultsForTest.isNotEmpty()) currentIndex + 1 else 0, allResultsForTest.size),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = stringResource(R.string.next_report),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (nextResult != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.clickable(enabled = nextResult != null) {
                            if (nextResult != null) currentResultId = nextResult.id
                        }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val result = scanResult
        if (result == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Summary Table Card (EvalBee Screenshot #4 Style)
                item {
                    MarklifyCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.section_col), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.5f))
                                Text(stringResource(R.string.score_col), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text(stringResource(R.string.percentage_col), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.2f))
                                Text(stringResource(R.string.correct_col), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.section_name_format, 1), fontSize = 13.sp, modifier = Modifier.weight(1.5f))
                                Text("${result.finalScore}", fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text(String.format(Locale.US, "%.1f%%", result.percentage), fontSize = 13.sp, modifier = Modifier.weight(1.2f))
                                Text("${result.correct}", fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.total_marks), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.5f))
                                Text("${result.finalScore}", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text(String.format(Locale.US, "%.1f%%", result.percentage), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.2f))
                                Text("${result.correct}", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // OMR Visual Overlay Sheet View (Screenshot #4 Style)
                item {
                    Text(stringResource(R.string.visual_omr_overlay), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                item {
                    MarklifyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFullImageDialog = true },
                        containerColor = Color.White
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1200f / 1600f)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (sheetBitmap != null) {
                                Image(
                                    bitmap = sheetBitmap.asImageBitmap(),
                                    contentDescription = stringResource(R.string.omr_sheet_scan_overlay),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(stringResource(R.string.omr_sheet_scan_overlay), color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    }
                }

                item {
                    Text(stringResource(R.string.question_details), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                items(detectedAnswers, key = { it.id }) { item ->
                    val questionKey = testQuestions.firstOrNull { it.questionNumber == item.questionNumber }?.correctAnswer ?: ""
                    ResultQuestionRow(
                        detected = item,
                        correctKey = questionKey
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    if (showFullImageDialog && sheetBitmap != null) {
        MarklifyDialog(
            onDismissRequest = { showFullImageDialog = false }
        ) {
            Text(stringResource(R.string.full_omr_overlay_view), fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                    bitmap = sheetBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.captured_sheet_image),
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
private fun ResultQuestionRow(detected: DetectedAnswer, correctKey: String) {
    val isMultiple = detected.detectedAnswer.equals("multiple", ignoreCase = true)
    val isAmbiguous = detected.detectedAnswer.equals("ambiguous", ignoreCase = true)
    val isUnanswered = detected.detectedAnswer.equals("unanswered", ignoreCase = true)
    val isCorrect = detected.isCorrect

    val statusColor = when {
        isCorrect -> SuccessGreen
        isMultiple -> WarningAmber
        isAmbiguous -> MarklifyBlue
        isUnanswered -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> ErrorRed
    }

    MarklifyCard(modifier = Modifier.fillMaxWidth()) {
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
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${detected.questionNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.question_number_short, detected.questionNumber),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.student_key_format, detected.detectedAnswer.ifBlank { "—" }, correctKey),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            MarklifyBadge(
                text = when {
                    isCorrect -> stringResource(R.string.correct_badge)
                    isMultiple -> stringResource(R.string.multiple_badge)
                    isAmbiguous -> stringResource(R.string.ambiguous_badge)
                    isUnanswered -> stringResource(R.string.blank_badge)
                    else -> stringResource(R.string.wrong_badge)
                },
                containerColor = statusColor.copy(alpha = 0.15f),
                contentColor = statusColor
            )
        }
    }
}

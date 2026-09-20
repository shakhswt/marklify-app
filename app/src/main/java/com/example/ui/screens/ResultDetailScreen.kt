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
                title = { Text("Roll No : ${scanResult?.studentId ?: "90"}", fontWeight = FontWeight.Bold) },
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
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                        }
                    }
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
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
                    Text("<", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.clickable { })
                    Text("Report 1/15", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(">", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.clickable { })
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
                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Section", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.5f))
                                Text("Score", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("Percentage", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.2f))
                                Text("Correct", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("Rank", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Section 1 Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Section 1", fontSize = 13.sp, modifier = Modifier.weight(1.5f))
                                Text("4.0", fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("80.0%", fontSize = 13.sp, modifier = Modifier.weight(1.2f))
                                Text("4", fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("1", fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }

                            // Section 2 Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Section 2", fontSize = 13.sp, modifier = Modifier.weight(1.5f))
                                Text("1.0", fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("20.0%", fontSize = 13.sp, modifier = Modifier.weight(1.2f))
                                Text("1", fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("4", fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Total Marks Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Marks", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.5f))
                                Text("${result.finalScore}", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text(String.format(Locale.US, "%.2f%%", result.percentage), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.2f))
                                Text("${result.correct}", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("1", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // OMR Visual Overlay Sheet View (Screenshot #4 Style)
                item {
                    Text("Visual OMR Overlay", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                                    contentDescription = "OMR Visual Overlay Sheet",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text("OMR Sheet Scan Overlay", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Per Question breakdown
                item {
                    Text("Question Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
            Text("Full OMR Overlay View", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                        text = "Q#${detected.questionNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Student: ${detected.detectedAnswer.ifBlank { "—" }}  •  Key: $correctKey",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            MarklifyBadge(
                text = when {
                    isCorrect -> "Correct"
                    isMultiple -> "Multiple"
                    isAmbiguous -> "Ambiguous"
                    isUnanswered -> "Blank"
                    else -> "Wrong"
                },
                containerColor = statusColor.copy(alpha = 0.15f),
                contentColor = statusColor
            )
        }
    }
}

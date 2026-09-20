package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Grading
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
import com.example.data.backup.DataBackupManager
import com.example.data.entity.ScanResult
import com.example.data.entity.TestEntity
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.LiquidGlassBackdrop
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassDialog
import com.example.ui.components.LiquidGlassEmptyState
import com.example.ui.components.LiquidGlassFAB
import com.example.ui.components.LiquidGlassTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    testId: Long,
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResultDetail: (Long) -> Unit,
    onNavigateToScanner: (Long) -> Unit
) {
    val context = LocalContext.current
    var test by remember { mutableStateOf<TestEntity?>(null) }
    val results by viewModel.getScanResultsForTest(testId).collectAsState(initial = emptyList())
    var resultToDelete by remember { mutableStateOf<ScanResult?>(null) }
    var isExportingCsv by remember { mutableStateOf(false) }

    LaunchedEffect(testId) {
        test = viewModel.getTestById(testId)
    }

    LiquidGlassBackdrop(animated = false) {
        Scaffold(
            topBar = {
                LiquidGlassTopAppBar(
                    title = {
                        Column {
                            Text(test?.name ?: stringResource(R.string.results_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(stringResource(R.string.graded_sheets_count, results.size), fontSize = 11.sp, color = TextSecondary)
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
                    },
                    actions = {
                        // Export CSV Button
                        if (results.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    isExportingCsv = true
                                    viewModel.exportTestResultsCsv(context, testId) { csvFile ->
                                        isExportingCsv = false
                                        DataBackupManager.shareFile(
                                            context = context,
                                            file = csvFile,
                                            mimeType = "text/csv",
                                            chooserTitle = "Export Test Results CSV"
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("export_csv_button")
                            ) {
                                if (isExportingCsv) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = TextPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = stringResource(R.string.export_csv),
                                        tint = TextPrimary
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.loadTestForScan(testId) {
                                    onNavigateToScanner(testId)
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = stringResource(R.string.scan_sheet), tint = SuccessGreen)
                        }
                    }
                )
            },
            floatingActionButton = {
                LiquidGlassFAB(
                    icon = Icons.Default.CameraAlt,
                    onClick = {
                        viewModel.loadTestForScan(testId) {
                            onNavigateToScanner(testId)
                        }
                    },
                    modifier = Modifier.testTag("scan_another_fab"),
                    contentDescription = stringResource(R.string.scan_sheet)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidGlassEmptyState(
                        icon = Icons.Default.Grading,
                        title = stringResource(R.string.no_scans_yet),
                        description = stringResource(R.string.no_scans_hint)
                    )
                }
            } else {
                val avgPercentage = remember(results) {
                    if (results.isNotEmpty()) results.map { it.percentage }.average().toFloat() else 0f
                }
                val totalMultipleMarks = remember(results) {
                    results.sumOf { it.multipleMarked }
                }
                val totalBlanks = remember(results) {
                    results.sumOf { it.unanswered }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    // Aggregate Stats Summary Card
                    item {
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            fillColor = GlassFill,
                            showSpecular = true
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.class_performance_overview),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(GlassFillElevated)
                                            .border(1.dp, GlassBorderBright, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.students_count, results.size),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${avgPercentage.toInt()}%",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp,
                                            color = if (avgPercentage >= 70f) SuccessGreen else WarningAmber
                                        )
                                        Text(stringResource(R.string.average_label), fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$totalMultipleMarks",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp,
                                            color = WarningAmber
                                        )
                                        Text(stringResource(R.string.multiple_marked_flag), fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$totalBlanks",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp,
                                            color = TextSecondary
                                        )
                                        Text(stringResource(R.string.unanswered_flag), fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    items(results, key = { it.id }) { scan ->
                        val dateStr = remember(scan.scanTime) {
                            SimpleDateFormat("MMM d, yyyy  HH:mm", Locale.getDefault()).format(Date(scan.scanTime))
                        }

                        val scoreColor = when {
                            scan.percentage >= 80f -> SuccessGreen
                            scan.percentage >= 50f -> WarningAmber
                            else -> ErrorRed
                        }

                        val scoreBg = when {
                            scan.percentage >= 80f -> SuccessGreenBg
                            scan.percentage >= 50f -> WarningAmberBg
                            else -> ErrorRedBg
                        }

                        val scoreBorder = when {
                            scan.percentage >= 80f -> SuccessGreenBorder
                            scan.percentage >= 50f -> WarningAmberBorder
                            else -> ErrorRedBorder
                        }

                        LiquidGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("result_card_${scan.id}"),
                            shape = RoundedCornerShape(20.dp),
                            fillColor = GlassFill,
                            showSpecular = true,
                            onClick = { onNavigateToResultDetail(scan.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(scoreBg)
                                            .border(1.dp, scoreBorder, RoundedCornerShape(14.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${scan.percentage.toInt()}%",
                                            fontWeight = FontWeight.Bold,
                                            color = scoreColor,
                                            fontSize = 15.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = scan.studentName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "ID: ${scan.studentId}  •  $dateStr",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "Score: ${scan.finalScore.toInt()}/${scan.totalQuestions}  •  ✓ ${scan.correct}  ✗ ${scan.wrong}  — ${scan.unanswered}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { resultToDelete = scan }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete),
                                            tint = ErrorRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    resultToDelete?.let { scan ->
        LiquidGlassDialog(
            onDismissRequest = { resultToDelete = null }
        ) {
            Text(
                text = stringResource(R.string.delete_result_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.delete_result_message, scan.studentName),
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassButton(
                    text = stringResource(R.string.cancel),
                    variant = GlassButtonVariant.Neutral,
                    modifier = Modifier.weight(1f),
                    onClick = { resultToDelete = null }
                )
                LiquidGlassButton(
                    text = stringResource(R.string.delete),
                    variant = GlassButtonVariant.Danger,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.deleteScanResult(scan)
                        resultToDelete = null
                    }
                )
            }
        }
    }
}

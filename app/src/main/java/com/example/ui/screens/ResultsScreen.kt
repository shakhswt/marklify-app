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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.DataBackupManager
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
                            Text(test?.name ?: "Grading Results", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${results.size} Graded Sheets", fontSize = 11.sp, color = TextSecondary)
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
                                        contentDescription = "Export CSV",
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
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Scan Another", tint = SuccessGreen)
                        }
                    }
                )
            },
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(22.dp),
                            fillColor = GlassFillElevated,
                            showSpecular = true,
                            elevation = 8.dp
                        )
                        .clickable {
                            viewModel.loadTestForScan(testId) {
                                onNavigateToScanner(testId)
                            }
                        }
                        .testTag("scan_another_fab")
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Sheet", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        shape = RoundedCornerShape(24.dp),
                        fillColor = GlassFill
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(GlassFillElevated)
                                    .border(1.dp, GlassBorderBright, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Grading,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("No scans for this test yet", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Tap the scan button to scan and auto-grade student sheets.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
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
                                        text = "Class Performance Overview",
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
                                            text = "${results.size} Students",
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
                                        Text("Average", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$totalMultipleMarks",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp,
                                            color = WarningAmber
                                        )
                                        Text("Multiple Marked", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$totalBlanks",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp,
                                            color = TextSecondary
                                        )
                                        Text("Unanswered", fontSize = 11.sp, color = TextSecondary)
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
                                            text = "Final Score: ${scan.finalScore.toInt()}/${scan.totalQuestions}  •  Wrong: ${scan.wrong}  •  Multiple: ${scan.multipleMarked}  •  Blank: ${scan.unanswered}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { resultToDelete = scan }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = ErrorRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Open",
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
        AlertDialog(
            onDismissRequest = { resultToDelete = null },
            containerColor = FrostedBarBackground,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Delete Result?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete the grading result for '${scan.studentName}'?", color = TextSecondary) },
            confirmButton = {
                LiquidGlassButton(
                    text = "Delete",
                    variant = GlassButtonVariant.Danger,
                    onClick = {
                        viewModel.deleteScanResult(scan)
                        resultToDelete = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { resultToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

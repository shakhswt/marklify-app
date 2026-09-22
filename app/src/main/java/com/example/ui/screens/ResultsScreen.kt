package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
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
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel

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

    Scaffold(
        topBar = {
            MarklifyTopAppBar(
                title = { Text(test?.name ?: stringResource(R.string.results_title), fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.loadTestForScan(testId) {
                                onNavigateToScanner(testId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.scan_sheet), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
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
            // Stats Row Cards (EvalBee Screenshot #6 Style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val totalPossibleMarks = (test?.questionCount ?: 30).toFloat()
                MarklifyCard(modifier = Modifier.weight(1.2f)) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MarklifyBlueContainerLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Σ", fontWeight = FontWeight.Bold, color = MarklifyBlue, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(stringResource(R.string.marks_max_format, totalPossibleMarks), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$totalPossibleMarks", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                MarklifyCard(modifier = Modifier.weight(1.2f)) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MarklifyBlueContainerLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MarklifyBlue, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(stringResource(R.string.reports_count_format, results.size), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${results.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (results.isNotEmpty()) {
                                isExportingCsv = true
                                viewModel.exportTestResultsCsv(context, testId) { csvFile ->
                                    isExportingCsv = false
                                    DataBackupManager.shareFile(
                                        context = context,
                                        file = csvFile,
                                        mimeType = "text/csv",
                                        chooserTitle = context.getString(R.string.export_csv)
                                    )
                                }
                            }
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.FileDownload, contentDescription = stringResource(R.string.export), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Student Report Items (EvalBee Screenshot #6 Style)
            if (results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_scans_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(results, key = { _, item -> item.id }) { index, scan ->
                        val rank = index + 1
                        StudentReportCard(
                            scan = scan,
                            rank = rank,
                            onClick = { onNavigateToResultDetail(scan.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (resultToDelete != null) {
        MarklifyDialog(
            onDismissRequest = { resultToDelete = null }
        ) {
            Text(stringResource(R.string.delete_result_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(stringResource(R.string.delete_result_message, resultToDelete?.studentName ?: ""))
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MarklifyButton(
                    text = stringResource(R.string.cancel),
                    variant = MarklifyButtonVariant.Neutral,
                    modifier = Modifier.weight(1f),
                    onClick = { resultToDelete = null }
                )
                MarklifyButton(
                    text = stringResource(R.string.delete),
                    variant = MarklifyButtonVariant.Danger,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        resultToDelete?.let { viewModel.deleteScanResult(it) }
                        resultToDelete = null
                    }
                )
            }
        }
    }
}

@Composable
private fun StudentReportCard(
    scan: ScanResult,
    rank: Int,
    onClick: () -> Unit
) {
    val initial = scan.studentName.take(1).uppercase().ifBlank { "S" }

    MarklifyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar Circle
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MarklifyBlueContainerLight
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(initial, fontWeight = FontWeight.Bold, color = MarklifyBlue, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = scan.studentName.ifBlank { stringResource(R.string.student_anonymous) },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = scan.studentId.ifBlank { "—" },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Rank Badge on Right (EvalBee Screenshot #6 Style)
                MarklifyBadge(
                    text = "🏅 $rank",
                    containerColor = MarklifyBlueContainerLight,
                    contentColor = MarklifyBlue
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score Summary Row: Σ 17.0  |  ✓ 17  |  ✗ 9  |  ○ 4
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Σ ${scan.finalScore}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✓", fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${scan.correct}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✗", fontWeight = FontWeight.Bold, color = ErrorRed, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${scan.wrong}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("○", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${scan.unanswered}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

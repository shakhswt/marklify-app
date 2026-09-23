package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.backup.DataBackupManager
import com.example.data.entity.GradedTestRecord
import com.example.data.entity.ScanResult
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.LiquidGlassBackdrop
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassChip
import com.example.ui.components.LiquidGlassDialog
import com.example.ui.components.LiquidGlassEmptyState
import com.example.ui.components.LiquidGlassSearchField
import com.example.ui.components.LiquidGlassTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import java.text.SimpleDateFormat
import java.util.*

// Extracted to avoid expensive allocations during LazyColumn scrolling
private val historyDateFormatter = SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault())

enum class HistorySortOrder(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    HIGHEST_SCORE("Highest Score"),
    LOWEST_SCORE("Lowest Score"),
    STUDENT_NAME("Student (A-Z)")
}

enum class HistoryScoreFilter(val label: String) {
    ALL("All"),
    HIGH_SCORE("≥ 80%"),
    AVERAGE("50% - 79%"),
    NEEDS_REVIEW("< 50%"),
    MULTIPLE_MARKED("Multiple Marked")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResultDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val allRecords by viewModel.allGradedTestRecords.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(HistoryScoreFilter.ALL) }
    var selectedSort by remember { mutableStateOf(HistorySortOrder.NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<GradedTestRecord?>(null) }
    var isExportingCsv by remember { mutableStateOf(false) }

    // Filter and Sort records
    val filteredRecords = remember(allRecords, searchQuery, selectedFilter, selectedSort) {
        var list = allRecords

        // Search query
        // Using contains(ignoreCase=true) instead of lowercase() to avoid string allocations during search
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim()
            list = list.filter {
                it.studentName.contains(q, ignoreCase = true) ||
                it.studentId.contains(q, ignoreCase = true) ||
                it.testName.contains(q, ignoreCase = true)
            }
        }

        // Category filter
        list = when (selectedFilter) {
            HistoryScoreFilter.ALL -> list
            HistoryScoreFilter.HIGH_SCORE -> list.filter { it.percentage >= 80f }
            HistoryScoreFilter.AVERAGE -> list.filter { it.percentage in 50f..79.99f }
            HistoryScoreFilter.NEEDS_REVIEW -> list.filter { it.percentage < 50f }
            HistoryScoreFilter.MULTIPLE_MARKED -> list.filter { it.multipleMarked > 0 }
        }

        // Sorting
        when (selectedSort) {
            HistorySortOrder.NEWEST -> list.sortedByDescending { it.scanTime }
            HistorySortOrder.OLDEST -> list.sortedBy { it.scanTime }
            HistorySortOrder.HIGHEST_SCORE -> list.sortedByDescending { it.finalScore }
            HistorySortOrder.LOWEST_SCORE -> list.sortedBy { it.finalScore }
            HistorySortOrder.STUDENT_NAME -> list.sortedBy { it.studentName.lowercase() }
        }
    }

    // Analytics calculations
    val totalGraded = allRecords.size
    val averageScore = if (allRecords.isNotEmpty()) allRecords.map { it.percentage }.average().toFloat() else 0f
    val topScore = if (allRecords.isNotEmpty()) allRecords.maxOf { it.percentage } else 0f
    val passCount = allRecords.count { it.percentage >= 50f }
    val passRate = if (totalGraded > 0) (passCount * 100f / totalGraded) else 0f

    LiquidGlassBackdrop(animated = false) {
        Scaffold(
            topBar = {
                LiquidGlassTopAppBar(
                    title = {
                        Column {
                            Text(stringResource(R.string.history_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(stringResource(R.string.graded_sheets_count, totalGraded), fontSize = 11.sp, color = TextSecondary)
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
                        // Export Complete History CSV
                        if (allRecords.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    isExportingCsv = true
                                    viewModel.exportAllGradedTestsCsv(context) { csvFile ->
                                        isExportingCsv = false
                                        DataBackupManager.shareFile(
                                            context = context,
                                            file = csvFile,
                                            mimeType = "text/csv",
                                            chooserTitle = "Export Graded Tests History CSV"
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("export_all_history_csv")
                            ) {
                                if (isExportingCsv) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = TextPrimary)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = stringResource(R.string.export_all_csv),
                                        tint = TextPrimary
                                    )
                                }
                            }
                        }

                        // Sort menu button
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(imageVector = Icons.Default.Sort, contentDescription = stringResource(R.string.sort), tint = TextPrimary)
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                HistorySortOrder.values().forEach { sort ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = sort.label,
                                                fontWeight = if (selectedSort == sort) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedSort == sort) TextPrimary else TextSecondary
                                            )
                                        },
                                        onClick = {
                                            selectedSort = sort
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            if (selectedSort == sort) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = TextPrimary)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
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
                // Summary Analytics Card
                if (allRecords.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            fillColor = GlassFill,
                            showSpecular = true
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.overall_performance),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (passRate >= 60f) SuccessGreenBg else WarningAmberBg)
                                            .border(1.dp, if (passRate >= 60f) SuccessGreenBorder else WarningAmberBorder, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.pass_rate_badge, passRate.toInt()),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = if (passRate >= 60f) SuccessGreen else WarningAmber
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    HistoryStatBox(
                                        title = "Graded",
                                        value = "$totalGraded",
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    HistoryStatBox(
                                        title = stringResource(R.string.average_label),
                                        value = "${averageScore.toInt()}%",
                                        color = if (averageScore >= 70f) SuccessGreen else WarningAmber,
                                        modifier = Modifier.weight(1f)
                                    )
                                    HistoryStatBox(
                                        title = stringResource(R.string.highest_label),
                                        value = "${topScore.toInt()}%",
                                        color = SuccessGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Search Bar
                item {
                    LiquidGlassSearchField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = stringResource(R.string.search_history_placeholder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_search_input")
                    )
                }

                // Filter Chips Row
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(HistoryScoreFilter.values()) { filter ->
                            LiquidGlassChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = filter.label
                            )
                        }
                    }
                }

                // Graded Test History List
                if (filteredRecords.isEmpty()) {
                    item {
                        LiquidGlassEmptyState(
                            icon = Icons.Default.AssignmentLate,
                            title = if (searchQuery.isNotBlank() || selectedFilter != HistoryScoreFilter.ALL) {
                                stringResource(R.string.no_matching_graded_tests)
                            } else {
                                stringResource(R.string.no_history_title)
                            },
                            description = if (searchQuery.isNotBlank()) {
                                stringResource(R.string.no_matching_hint)
                            } else {
                                stringResource(R.string.no_history_desc)
                            }
                        )
                    }
                } else {
                    items(filteredRecords, key = { it.id }) { record ->
                        GradedTestHistoryCard(
                            record = record,
                            onClick = { onNavigateToResultDetail(record.id) },
                            onDelete = { recordToDelete = record }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    recordToDelete?.let { record ->
        LiquidGlassDialog(
            onDismissRequest = { recordToDelete = null }
        ) {
            Text(
                text = stringResource(R.string.delete_result_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.delete_result_message, record.studentName),
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
                    onClick = { recordToDelete = null }
                )
                LiquidGlassButton(
                    text = stringResource(R.string.delete),
                    variant = GlassButtonVariant.Danger,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val scanResult = ScanResult(
                            id = record.id,
                            testId = record.testId,
                            studentName = record.studentName,
                            studentId = record.studentId,
                            scanTime = record.scanTime,
                            totalQuestions = record.totalQuestions,
                            correct = record.correct,
                            wrong = record.wrong,
                            unanswered = record.unanswered,
                            multipleMarked = record.multipleMarked,
                            ambiguous = record.ambiguous,
                            percentage = record.percentage,
                            finalScore = record.finalScore,
                            imagePath = record.imagePath
                        )
                        val deletedToastMsg = context.getString(R.string.record_deleted_toast)
                        viewModel.deleteScanResult(scanResult)
                        recordToDelete = null
                        Toast.makeText(context, deletedToastMsg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun GradedTestHistoryCard(
    record: GradedTestRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(record.scanTime) {
        historyDateFormatter.format(Date(record.scanTime))
    }

    val scoreColor = when {
        record.percentage >= 80f -> SuccessGreen
        record.percentage >= 50f -> WarningAmber
        else -> ErrorRed
    }

    val scoreBg = when {
        record.percentage >= 80f -> SuccessGreenBg
        record.percentage >= 50f -> WarningAmberBg
        else -> ErrorRedBg
    }

    val scoreBorder = when {
        record.percentage >= 80f -> SuccessGreenBorder
        record.percentage >= 50f -> WarningAmberBorder
        else -> ErrorRedBorder
    }

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("graded_history_card_${record.id}"),
        shape = RoundedCornerShape(20.dp),
        fillColor = GlassFill,
        showSpecular = true,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Student Name, Score Badge, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score Badge + Student Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(scoreBg)
                            .border(1.dp, scoreBorder, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${record.percentage.toInt()}%",
                                fontWeight = FontWeight.Bold,
                                color = scoreColor,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${record.correct}/${record.totalQuestions}",
                                fontSize = 10.sp,
                                color = scoreColor.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = record.studentName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ID: ${record.studentId}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text("•", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = record.testName,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Delete & Open actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete) {
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

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GlassBorderSubtle)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Footer Row: Breakdown Pills and Scan Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score breakdown
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScoreStatPill(label = "✓ ${record.correct}", color = SuccessGreen, bg = SuccessGreenBg, border = SuccessGreenBorder)
                    ScoreStatPill(label = "✗ ${record.wrong}", color = ErrorRed, bg = ErrorRedBg, border = ErrorRedBorder)
                    if (record.multipleMarked > 0) {
                        ScoreStatPill(label = "⚠ ${record.multipleMarked}", color = WarningAmber, bg = WarningAmberBg, border = WarningAmberBorder)
                    }
                    if (record.unanswered > 0) {
                        ScoreStatPill(label = "— ${record.unanswered}", color = TextSecondary, bg = GlassFillSubtle, border = GlassBorderSubtle)
                    }
                }

                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ScoreStatPill(label: String, color: Color, bg: Color, border: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun HistoryStatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassFillElevated)
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

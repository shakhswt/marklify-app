package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ScanResult
import com.example.data.entity.TestEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MarklifyViewModel,
    onNavigateToTopics: () -> Unit,
    onNavigateToTopicDetail: (Long) -> Unit,
    onNavigateToTestDetail: (Long) -> Unit,
    onNavigateToSheetGenerator: (Long) -> Unit,
    onNavigateToScanner: (Long) -> Unit,
    onNavigateToResultDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val topicCount by viewModel.topicCount.collectAsState()
    val testCount by viewModel.testCount.collectAsState()
    val scanCount by viewModel.scanCount.collectAsState()
    val allTests by viewModel.allTests.collectAsState()
    val recentScans by viewModel.recentScanResults.collectAsState()
    val topics by viewModel.topics.collectAsState()

    var showCreateTopicDialog by remember { mutableStateOf(false) }
    var showCreateTestDialog by remember { mutableStateOf(false) }
    var showSelectTestForSheetDialog by remember { mutableStateOf(false) }
    var showSelectTestForScanDialog by remember { mutableStateOf(false) }

    com.example.ui.components.LiquidGlassBackdrop(animated = true) {
        Scaffold(
            topBar = {
                com.example.ui.components.LiquidGlassTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassFillElevated)
                                    .border(1.dp, GlassBorderBright, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Marklify",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "OMR Sheet Creator & Auto-Grader",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onNavigateToTopics,
                            modifier = Modifier.testTag("topics_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "View Topics",
                                tint = TextSecondary
                            )
                        }
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Scanner Settings & Calibration",
                                tint = TextSecondary
                            )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Row
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Topics",
                        count = topicCount.toString(),
                        icon = Icons.Default.Folder,
                        accentColor = ElectricBlue,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToTopics() }
                    )
                    StatCard(
                        title = "Tests",
                        count = testCount.toString(),
                        icon = Icons.Default.Assignment,
                        accentColor = IndigoAccent,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Scans",
                        count = scanCount.toString(),
                        icon = Icons.Default.QrCodeScanner,
                        accentColor = PurpleAccent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToHistory() }
                    )
                }
            }

            // Quick Actions Header
            item {
                Text(
                    text = "Quick Actions",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            }

            // 4 Grid Action Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = "Create Topic",
                            subtitle = "Organize tests by subject",
                            icon = Icons.Default.CreateNewFolder,
                            color = ElectricBlue,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("create_topic_button"),
                            onClick = { showCreateTopicDialog = true }
                        )
                        ActionCard(
                            title = "Create Test",
                            subtitle = "Questions & answer keys",
                            icon = Icons.Default.AddBox,
                            color = IndigoAccent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("create_test_button"),
                            onClick = {
                                if (topics.isEmpty()) {
                                    showCreateTopicDialog = true
                                } else {
                                    showCreateTestDialog = true
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = "Generate Sheet",
                            subtitle = "Printable OMR bubble form",
                            icon = Icons.Default.Print,
                            color = PurpleAccent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("generate_sheet_button"),
                            onClick = { showSelectTestForSheetDialog = true }
                        )
                        ActionCard(
                            title = "Scan Sheet",
                            subtitle = "CameraX & OpenCV auto-grade",
                            icon = Icons.Default.CameraAlt,
                            color = SuccessGreen,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("scan_sheet_button"),
                            onClick = { showSelectTestForScanDialog = true }
                        )
                    }
                }
            }

            // Recent Scans Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Scan Results",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    TextButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("view_full_history_button")
                    ) {
                        Text(
                            text = "View Full History →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricBlue
                        )
                    }
                }
            }

            if (recentScans.isEmpty()) {
                item {
                    com.example.ui.components.LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        fillColor = GlassFill,
                        showSpecular = true
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(GlassFillElevated)
                                    .border(1.dp, GlassBorderBright, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Scanner,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No scans completed yet",
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Generate a blank sheet, print or display it, and tap 'Scan Sheet' to auto-grade.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(recentScans, key = { it.id }) { scan ->
                    RecentScanCard(
                        scan = scan,
                        onClick = { onNavigateToResultDetail(scan.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialog: Create Topic
    if (showCreateTopicDialog) {
        var topicName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateTopicDialog = false },
            title = { Text("Create New Topic") },
            text = {
                OutlinedTextField(
                    value = topicName,
                    onValueChange = { topicName = it },
                    label = { Text("Topic Name (e.g. Mathematics)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("topic_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (topicName.isNotBlank()) {
                            viewModel.createTopic(topicName) {
                                showCreateTopicDialog = false
                            }
                        }
                    },
                    enabled = topicName.isNotBlank(),
                    modifier = Modifier.testTag("save_topic_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTopicDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Create Test
    if (showCreateTestDialog) {
        var testName by remember { mutableStateOf("") }
        var questionCountText by remember { mutableStateOf("20") }
        var selectedTopicId by remember { mutableStateOf(topics.firstOrNull()?.id ?: 0L) }

        AlertDialog(
            onDismissRequest = { showCreateTestDialog = false },
            title = { Text("Create New Test") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Topic:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        topics.take(3).forEach { topic ->
                            FilterChip(
                                selected = selectedTopicId == topic.id,
                                onClick = { selectedTopicId = topic.id },
                                label = { Text(topic.name, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = testName,
                        onValueChange = { testName = it },
                        label = { Text("Test Name (e.g. Midterm 2026)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_name_input")
                    )

                    OutlinedTextField(
                        value = questionCountText,
                        onValueChange = { questionCountText = it.filter { char -> char.isDigit() }.take(3) },
                        label = { Text("Question Count (1–100)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val count = questionCountText.toIntOrNull()?.coerceIn(1, 100) ?: 20
                        if (testName.isNotBlank() && selectedTopicId > 0) {
                            viewModel.createTest(selectedTopicId, testName, count) { newTestId ->
                                showCreateTestDialog = false
                                onNavigateToTestDetail(newTestId)
                            }
                        }
                    },
                    enabled = testName.isNotBlank() && selectedTopicId > 0,
                    modifier = Modifier.testTag("save_test_button")
                ) {
                    Text("Create Test")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTestDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Select Test for Blank Sheet
    if (showSelectTestForSheetDialog) {
        AlertDialog(
            onDismissRequest = { showSelectTestForSheetDialog = false },
            title = { Text("Select Test for OMR Sheet") },
            text = {
                if (allTests.isEmpty()) {
                    Text("No tests found. Please create a test first.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                        items(allTests) { test ->
                            ListItem(
                                headlineContent = { Text(test.name, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text("${test.questionCount} Questions") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showSelectTestForSheetDialog = false
                                        onNavigateToSheetGenerator(test.id)
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSelectTestForSheetDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Dialog: Select Test for Scan Sheet
    if (showSelectTestForScanDialog) {
        AlertDialog(
            onDismissRequest = { showSelectTestForScanDialog = false },
            title = { Text("Select Test to Scan") },
            text = {
                if (allTests.isEmpty()) {
                    Text("No tests found. Please create a test first.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                        items(allTests) { test ->
                            ListItem(
                                headlineContent = { Text(test.name, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text("${test.questionCount} Questions") },
                                trailingContent = {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = SuccessGreen
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showSelectTestForScanDialog = false
                                        viewModel.loadTestForScan(test.id) {
                                            onNavigateToScanner(test.id)
                                        }
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSelectTestForScanDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    com.example.ui.components.LiquidGlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        fillColor = GlassFill,
        showSpecular = true
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(GlassFillSubtle)
                        .border(1.dp, GlassBorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    com.example.ui.components.LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        fillColor = GlassFill,
        showSpecular = true,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(GlassFillElevated)
                    .border(1.dp, GlassBorderBright, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecentScanCard(
    scan: ScanResult,
    onClick: () -> Unit
) {
    val dateStr = remember(scan.scanTime) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(scan.scanTime))
    }

    // High-contrast semantic glass pills: ONLY functional states get color tint
    val (scoreColor, scoreBg, scoreBorder) = when {
        scan.percentage >= 80f -> Triple(SuccessGreen, SuccessGreenBg, SuccessGreenBorder)
        scan.percentage >= 50f -> Triple(WarningAmber, WarningAmberBg, WarningAmberBorder)
        else -> Triple(ErrorRed, ErrorRedBg, ErrorRedBorder)
    }

    com.example.ui.components.LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scan_result_item_${scan.id}"),
        shape = RoundedCornerShape(22.dp),
        fillColor = GlassFill,
        showSpecular = true,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        text = "Final Score: ${scan.finalScore.toInt()}/${scan.totalQuestions}  •  ✓ ${scan.correct}  ✗ ${scan.wrong}  — ${scan.unanswered}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View Details",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

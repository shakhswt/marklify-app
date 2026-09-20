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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.ScanResult
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import com.example.util.HapticManager
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
    val context = LocalContext.current
    val topicCount by viewModel.topicCount.collectAsState()
    val testCount by viewModel.testCount.collectAsState()
    val scanCount by viewModel.scanCount.collectAsState()
    val allTests by viewModel.allTests.collectAsState()
    val recentScans by viewModel.recentScanResults.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    var showCreateTopicDialog by remember { mutableStateOf(false) }
    var showCreateTestDialog by remember { mutableStateOf(false) }
    var showSelectTestForSheetDialog by remember { mutableStateOf(false) }
    var showSelectTestForScanDialog by remember { mutableStateOf(false) }

    LiquidGlassBackdrop(animated = true) {
        Scaffold(
            topBar = {
                LiquidGlassTopAppBar(
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
                                    text = stringResource(R.string.app_name),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.app_tagline),
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
                                contentDescription = stringResource(R.string.dashboard_topics),
                                tint = TextSecondary
                            )
                        }
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings_title),
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
                            title = stringResource(R.string.dashboard_topics),
                            count = topicCount.toString(),
                            icon = Icons.Default.Folder,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToTopics() }
                        )
                        StatCard(
                            title = stringResource(R.string.dashboard_tests),
                            count = testCount.toString(),
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = stringResource(R.string.dashboard_graded_sheets),
                            count = scanCount.toString(),
                            icon = Icons.Default.QrCodeScanner,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToHistory() }
                        )
                    }
                }

                // Quick Actions Header
                item {
                    Text(
                        text = stringResource(R.string.quick_actions),
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
                                title = stringResource(R.string.create_topic),
                                subtitle = stringResource(R.string.create_topic_desc),
                                icon = Icons.Default.CreateNewFolder,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("create_topic_button"),
                                onClick = { showCreateTopicDialog = true }
                            )
                            ActionCard(
                                title = stringResource(R.string.create_test),
                                subtitle = stringResource(R.string.create_test_desc),
                                icon = Icons.Default.AddBox,
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
                                title = stringResource(R.string.generate_sheet),
                                subtitle = stringResource(R.string.generate_sheet_desc),
                                icon = Icons.Default.Print,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("generate_sheet_button"),
                                onClick = { showSelectTestForSheetDialog = true }
                            )
                            ActionCard(
                                title = stringResource(R.string.scan_sheet),
                                subtitle = stringResource(R.string.scan_sheet_desc),
                                icon = Icons.Default.CameraAlt,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scan_sheet_button"),
                                onClick = { showSelectTestForScanDialog = true }
                            )
                        }
                    }
                }

                // Recent Graded Sheets Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recent_scans),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )

                        if (recentScans.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.view_all_history),
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { onNavigateToHistory() }
                            )
                        }
                    }
                }

                if (recentScans.isEmpty()) {
                    item {
                        LiquidGlassEmptyState(
                            icon = Icons.Default.QrCodeScanner,
                            title = stringResource(R.string.no_recent_scans),
                            description = stringResource(R.string.no_recent_scans_desc)
                        )
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
    }

    // Dialog: Create Topic in Liquid Glass
    if (showCreateTopicDialog) {
        var topicName by remember { mutableStateOf("") }
        LiquidGlassDialog(
            onDismissRequest = { showCreateTopicDialog = false }
        ) {
            Text(
                text = stringResource(R.string.create_topic_dialog_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            LiquidGlassTextField(
                value = topicName,
                onValueChange = { topicName = it },
                label = stringResource(R.string.topic_name_label),
                placeholder = "e.g. Mathematics",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("topic_name_input")
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassButton(
                    text = stringResource(R.string.cancel),
                    onClick = { showCreateTopicDialog = false },
                    modifier = Modifier.weight(1f),
                    variant = GlassButtonVariant.Neutral
                )
                LiquidGlassButton(
                    text = stringResource(R.string.confirm),
                    onClick = {
                        if (topicName.isNotBlank()) {
                            viewModel.createTopic(topicName) {
                                HapticManager.performSubmissionSuccess(context, appSettings.hapticsEnabled)
                                showCreateTopicDialog = false
                            }
                        }
                    },
                    enabled = topicName.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_topic_button"),
                    variant = GlassButtonVariant.Primary
                )
            }
        }
    }

    // Dialog: Create Test in Liquid Glass
    if (showCreateTestDialog) {
        var testName by remember { mutableStateOf("") }
        var questionCountText by remember { mutableStateOf("20") }
        var selectedTopicId by remember { mutableStateOf(topics.firstOrNull()?.id ?: 0L) }

        LiquidGlassDialog(
            onDismissRequest = { showCreateTestDialog = false }
        ) {
            Text(
                text = stringResource(R.string.create_test_dialog_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.select_topic_label),
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                topics.take(3).forEach { topic ->
                    LiquidGlassChip(
                        selected = selectedTopicId == topic.id,
                        onClick = { selectedTopicId = topic.id },
                        label = topic.name,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LiquidGlassTextField(
                value = testName,
                onValueChange = { testName = it },
                label = stringResource(R.string.test_name_label),
                placeholder = "e.g. Midterm 2026",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))
            LiquidGlassTextField(
                value = questionCountText,
                onValueChange = { questionCountText = it.filter { char -> char.isDigit() }.take(3) },
                label = stringResource(R.string.question_count_label),
                placeholder = "20",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassButton(
                    text = stringResource(R.string.cancel),
                    onClick = { showCreateTestDialog = false },
                    modifier = Modifier.weight(1f),
                    variant = GlassButtonVariant.Neutral
                )
                LiquidGlassButton(
                    text = stringResource(R.string.confirm),
                    onClick = {
                        val count = questionCountText.toIntOrNull()?.coerceIn(1, 100) ?: 20
                        if (testName.isNotBlank() && selectedTopicId > 0) {
                            viewModel.createTest(selectedTopicId, testName, count) { newTestId ->
                                HapticManager.performSubmissionSuccess(context, appSettings.hapticsEnabled)
                                showCreateTestDialog = false
                                onNavigateToTestDetail(newTestId)
                            }
                        }
                    },
                    enabled = testName.isNotBlank() && selectedTopicId > 0,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_test_button"),
                    variant = GlassButtonVariant.Primary
                )
            }
        }
    }

    // Dialog: Select Test for Blank Sheet in Liquid Glass
    if (showSelectTestForSheetDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showSelectTestForSheetDialog = false }
        ) {
            Text(
                text = stringResource(R.string.select_test_for_sheet),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (allTests.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_tests_found),
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allTests) { test ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(GlassFillElevated)
                                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable {
                                    showSelectTestForSheetDialog = false
                                    onNavigateToSheetGenerator(test.id)
                                }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(test.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                Text(stringResource(R.string.questions_count, test.questionCount), fontSize = 12.sp, color = TextSecondary)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            LiquidGlassButton(
                text = stringResource(R.string.close),
                onClick = { showSelectTestForSheetDialog = false },
                modifier = Modifier.fillMaxWidth(),
                variant = GlassButtonVariant.Neutral
            )
        }
    }

    // Dialog: Select Test for Scan Sheet in Liquid Glass
    if (showSelectTestForScanDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showSelectTestForScanDialog = false }
        ) {
            Text(
                text = stringResource(R.string.select_test_for_scan),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (allTests.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_tests_found),
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allTests) { test ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(GlassFillElevated)
                                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable {
                                    showSelectTestForScanDialog = false
                                    viewModel.loadTestForScan(test.id) {
                                        onNavigateToScanner(test.id)
                                    }
                                }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(test.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                Text(stringResource(R.string.questions_count, test.questionCount), fontSize = 12.sp, color = TextSecondary)
                            }
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            LiquidGlassButton(
                text = stringResource(R.string.close),
                onClick = { showSelectTestForScanDialog = false },
                modifier = Modifier.fillMaxWidth(),
                variant = GlassButtonVariant.Neutral
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LiquidGlassCard(
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

    val (scoreColor, scoreBg, scoreBorder) = when {
        scan.percentage >= 80f -> Triple(SuccessGreen, SuccessGreenBg, SuccessGreenBorder)
        scan.percentage >= 50f -> Triple(WarningAmber, WarningAmberBg, WarningAmberBorder)
        else -> Triple(ErrorRed, ErrorRedBg, ErrorRedBorder)
    }

    LiquidGlassCard(
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
                        text = stringResource(
                            R.string.final_score_summary,
                            scan.finalScore.toInt(),
                            scan.totalQuestions,
                            scan.correct,
                            scan.wrong,
                            scan.unanswered
                        ),
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.details),
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

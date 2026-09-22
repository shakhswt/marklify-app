package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
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
import com.example.R
import com.example.data.entity.TestEntity
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
    onNavigateToHistory: () -> Unit,
    onNavigateToSaveExam: () -> Unit
) {
    val context = LocalContext.current
    val allTests by viewModel.allTests.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showArchivedOnly by remember { mutableStateOf(false) }

    var showCreateTopicDialog by remember { mutableStateOf(false) }
    var showCreateTestDialog by remember { mutableStateOf(false) }

    val filteredTests = remember(allTests, searchQuery, showArchivedOnly) {
        allTests.filter { test ->
            val matchesQuery = test.name.contains(searchQuery, ignoreCase = true)
            val matchesArchive = if (showArchivedOnly) test.isArchived else !test.isArchived
            matchesQuery && matchesArchive
        }
    }

    val archivedCount = remember(allTests) { allTests.count { it.isArchived } }

    Scaffold(
        topBar = {
            MarklifyTopAppBar(
                title = {
                    Text(
                        text = "Exams",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = "Backup/Restore",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = {
                            if (topics.isEmpty()) {
                                showCreateTopicDialog = true
                            } else {
                                showCreateTestDialog = true
                            }
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("add_new_exam_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add New", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
            // Search & Filter Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search exams...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Archived Banner Item (EvalBee Style)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showArchivedOnly = !showArchivedOnly },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (showArchivedOnly) "Showing Archived Exams" else "Archived",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    MarklifyBadge(
                        text = archivedCount.toString(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Exam Cards List
            if (filteredTests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (showArchivedOnly) "No archived exams" else "No exams created yet",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap '+ Add New' to create your first exam",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTests, key = { it.id }) { exam ->
                        ExamCardItem(
                            exam = exam,
                            onClick = { onNavigateToTestDetail(exam.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Dialog: Create Topic
    if (showCreateTopicDialog) {
        var topicName by remember { mutableStateOf("") }
        MarklifyDialog(
            onDismissRequest = { showCreateTopicDialog = false }
        ) {
            Text(
                text = "Create Category / Subject",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = topicName,
                onValueChange = { topicName = it },
                label = { Text("Category / Class Name") },
                placeholder = { Text("e.g. NEET / I PUC") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("topic_name_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MarklifyButton(
                    text = stringResource(R.string.cancel),
                    onClick = { showCreateTopicDialog = false },
                    modifier = Modifier.weight(1f),
                    variant = MarklifyButtonVariant.Neutral
                )
                MarklifyButton(
                    text = stringResource(R.string.confirm),
                    onClick = {
                        if (topicName.isNotBlank()) {
                            viewModel.createTopic(topicName) {
                                HapticManager.performSubmissionSuccess(context, appSettings.hapticsEnabled)
                                showCreateTopicDialog = false
                                showCreateTestDialog = true
                            }
                        }
                    },
                    enabled = topicName.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_topic_button"),
                    variant = MarklifyButtonVariant.Primary
                )
            }
        }
    }

    // Dialog: Create Exam
    if (showCreateTestDialog) {
        var testName by remember { mutableStateOf("") }
        var questionCountText by remember { mutableStateOf("50") }
        var selectedTopicId by remember { mutableStateOf(topics.firstOrNull()?.id ?: 0L) }

        MarklifyDialog(
            onDismissRequest = { showCreateTestDialog = false }
        ) {
            Text(
                text = "Create New Exam",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Category / Class",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                topics.take(3).forEach { topic ->
                    MarklifyChip(
                        selected = selectedTopicId == topic.id,
                        onClick = { selectedTopicId = topic.id },
                        label = topic.name,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = testName,
                onValueChange = { testName = it },
                label = { Text("Exam Name") },
                placeholder = { Text("e.g. Neet weekly 43") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_name_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = questionCountText,
                onValueChange = { questionCountText = it.filter { char -> char.isDigit() }.take(3) },
                label = { Text("Total Questions") },
                placeholder = { Text("50") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MarklifyButton(
                    text = stringResource(R.string.cancel),
                    onClick = { showCreateTestDialog = false },
                    modifier = Modifier.weight(1f),
                    variant = MarklifyButtonVariant.Neutral
                )
                MarklifyButton(
                    text = stringResource(R.string.confirm),
                    onClick = {
                        val count = questionCountText.toIntOrNull()?.coerceIn(1, 300) ?: 50
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
                    variant = MarklifyButtonVariant.Primary
                )
            }
        }
    }
}

@Composable
private fun ExamCardItem(
    exam: TestEntity,
    onClick: () -> Unit
) {
    val monthStr = remember(exam.createdAt) {
        SimpleDateFormat("MMM", Locale.US).format(Date(exam.createdAt))
    }
    val dayStr = remember(exam.createdAt) {
        SimpleDateFormat("dd", Locale.US).format(Date(exam.createdAt))
    }

    MarklifyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("exam_card_${exam.id}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date Badge on Left (EvalBee Style)
            Surface(
                modifier = Modifier.size(width = 50.dp, height = 54.dp),
                shape = RoundedCornerShape(10.dp),
                color = MarklifyBlueContainerLight
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = monthStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MarklifyBlue
                    )
                    Text(
                        text = dayStr,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MarklifyBlue
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exam.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (exam.isPublic) {
                        MarklifyBadge(
                            text = "🌐 Public",
                            containerColor = SuccessGreenBg,
                            contentColor = SuccessGreen
                        )
                    } else {
                        MarklifyBadge(
                            text = "🔒 Private",
                            containerColor = WarningAmberBg,
                            contentColor = WarningAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stats Row: ? 50  |  [=] 0  |  Category
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${exam.questionCount}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CropFree,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "0",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = exam.examType,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

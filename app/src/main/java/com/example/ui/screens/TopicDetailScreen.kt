package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.TestEntity
import com.example.data.entity.Topic
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.LiquidGlassBackdrop
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassDialog
import com.example.ui.components.LiquidGlassEmptyState
import com.example.ui.components.LiquidGlassFAB
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.components.LiquidGlassTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicId: Long,
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTestDetail: (Long) -> Unit,
    onNavigateToSheetGenerator: (Long) -> Unit,
    onNavigateToScanner: (Long) -> Unit
) {
    var topic by remember { mutableStateOf<Topic?>(null) }
    val tests by viewModel.getTestsForTopic(topicId).collectAsState(initial = emptyList())
    var showCreateTestDialog by remember { mutableStateOf(false) }
    var testToDelete by remember { mutableStateOf<TestEntity?>(null) }
    var testToRename by remember { mutableStateOf<TestEntity?>(null) }

    LaunchedEffect(topicId) {
        topic = viewModel.getTopicById(topicId)
    }

    LiquidGlassBackdrop(animated = false) {
        Scaffold(
            topBar = {
                LiquidGlassTopAppBar(
                    title = { Text(topic?.name ?: stringResource(R.string.topic_tests_title), fontWeight = FontWeight.Bold) },
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
                        IconButton(onClick = { showCreateTestDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.create_test),
                                tint = TextPrimary
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                LiquidGlassFAB(
                    icon = Icons.Default.Add,
                    onClick = { showCreateTestDialog = true },
                    modifier = Modifier.testTag("create_test_fab"),
                    contentDescription = stringResource(R.string.create_test_dialog)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                if (tests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        LiquidGlassEmptyState(
                            icon = Icons.Default.Assignment,
                            title = stringResource(R.string.no_tests_in_topic),
                            description = stringResource(R.string.no_tests_in_topic_hint)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tests, key = { it.id }) { test ->
                            val dateStr = remember(test.createdAt) {
                                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(test.createdAt))
                            }

                            LiquidGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("test_card_${test.id}"),
                                shape = RoundedCornerShape(22.dp),
                                fillColor = GlassFill,
                                showSpecular = true,
                                onClick = { onNavigateToTestDetail(test.id) }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(GlassFillElevated)
                                                    .border(1.dp, GlassBorderBright, RoundedCornerShape(14.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Assignment,
                                                    contentDescription = null,
                                                    tint = TextPrimary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(
                                                    text = test.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 16.sp,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = stringResource(R.string.questions_and_date, test.questionCount, dateStr),
                                                    fontSize = 12.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { testToRename = test }) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = stringResource(R.string.rename_test_title),
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(onClick = { testToDelete = test }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.delete),
                                                    tint = ErrorRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        LiquidGlassButton(
                                            onClick = { onNavigateToSheetGenerator(test.id) },
                                            modifier = Modifier.weight(1f),
                                            variant = GlassButtonVariant.Neutral,
                                            icon = Icons.Default.Print,
                                            text = stringResource(R.string.sheet_pdf)
                                        )

                                        LiquidGlassButton(
                                            onClick = {
                                                viewModel.loadTestForScan(test.id) {
                                                    onNavigateToScanner(test.id)
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            variant = GlassButtonVariant.Success,
                                            icon = Icons.Default.CameraAlt,
                                            text = stringResource(R.string.scan_sheet)
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
    }

    if (showCreateTestDialog) {
        var testName by remember { mutableStateOf("") }
        var questionCountText by remember { mutableStateOf("20") }

        LiquidGlassDialog(
            onDismissRequest = { showCreateTestDialog = false }
        ) {
            Text(
                text = stringResource(R.string.create_test_dialog),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LiquidGlassTextField(
                    value = testName,
                    onValueChange = { testName = it },
                    label = stringResource(R.string.test_name_hint),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LiquidGlassTextField(
                    value = questionCountText,
                    onValueChange = { questionCountText = it.filter { c -> c.isDigit() }.take(3) },
                    label = stringResource(R.string.question_count_label),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassButton(
                    text = stringResource(R.string.cancel),
                    variant = GlassButtonVariant.Neutral,
                    modifier = Modifier.weight(1f),
                    onClick = { showCreateTestDialog = false }
                )
                LiquidGlassButton(
                    text = stringResource(R.string.create_test),
                    variant = GlassButtonVariant.Primary,
                    enabled = testName.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val count = questionCountText.toIntOrNull()?.coerceIn(1, 100) ?: 20
                        if (testName.isNotBlank()) {
                            viewModel.createTest(topicId, testName, count) {
                                showCreateTestDialog = false
                            }
                        }
                    }
                )
            }
        }
    }

    testToDelete?.let { test ->
        LiquidGlassDialog(
            onDismissRequest = { testToDelete = null }
        ) {
            Text(
                text = stringResource(R.string.delete_test_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.delete_test_message, test.name),
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
                    onClick = { testToDelete = null }
                )
                LiquidGlassButton(
                    text = stringResource(R.string.delete),
                    variant = GlassButtonVariant.Danger,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.deleteTest(test)
                        testToDelete = null
                    }
                )
            }
        }
    }

    testToRename?.let { testItem ->
        var renameTestText by remember(testItem) { mutableStateOf(testItem.name) }
        LiquidGlassDialog(
            onDismissRequest = { testToRename = null }
        ) {
            Text(
                text = stringResource(R.string.rename_test_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
            LiquidGlassTextField(
                value = renameTestText,
                onValueChange = { renameTestText = it },
                label = stringResource(R.string.new_test_name_label),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
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
                    onClick = { testToRename = null }
                )
                LiquidGlassButton(
                    text = stringResource(R.string.rename),
                    variant = GlassButtonVariant.Primary,
                    enabled = renameTestText.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (renameTestText.isNotBlank()) {
                            viewModel.renameTest(testItem.id, renameTestText)
                            testToRename = null
                        }
                    }
                )
            }
        }
    }
}

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TestEntity
import com.example.data.entity.Topic
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
                    title = { Text(topic?.name ?: "Topic Tests", fontWeight = FontWeight.Bold) },
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
                        IconButton(onClick = { showCreateTestDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Test", tint = TextPrimary)
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
                        .clickable { showCreateTestDialog = true }
                        .testTag("create_test_fab")
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create Test",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
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
                                        imageVector = Icons.Default.Assignment,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("No tests in this topic yet", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Tap + to add a test and configure its answer key.", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
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
                                                    text = "${test.questionCount} Questions  •  $dateStr",
                                                    fontSize = 12.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { testToRename = test }) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Rename Test",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(onClick = { testToDelete = test }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
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
                                            text = "Sheet PDF"
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
                                            text = "Scan"
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

        AlertDialog(
            onDismissRequest = { showCreateTestDialog = false },
            containerColor = FrostedBarBackground,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Create Test", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = testName,
                        onValueChange = { testName = it },
                        label = { Text("Test Name (e.g. Unit 3 Test)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = questionCountText,
                        onValueChange = { questionCountText = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Number of Questions (1–100)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                LiquidGlassButton(
                    text = "Create",
                    enabled = testName.isNotBlank(),
                    onClick = {
                        val count = questionCountText.toIntOrNull()?.coerceIn(1, 100) ?: 20
                        if (testName.isNotBlank()) {
                            viewModel.createTest(topicId, testName, count) {
                                showCreateTestDialog = false
                            }
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showCreateTestDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    testToDelete?.let { test ->
        AlertDialog(
            onDismissRequest = { testToDelete = null },
            containerColor = FrostedBarBackground,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Delete Test?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${test.name}' and all associated questions & scan history?", color = TextSecondary) },
            confirmButton = {
                LiquidGlassButton(
                    text = "Delete",
                    variant = GlassButtonVariant.Danger,
                    onClick = {
                        viewModel.deleteTest(test)
                        testToDelete = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { testToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    testToRename?.let { testItem ->
        var renameTestText by remember(testItem) { mutableStateOf(testItem.name) }
        AlertDialog(
            onDismissRequest = { testToRename = null },
            containerColor = FrostedBarBackground,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Rename Test", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameTestText,
                    onValueChange = { renameTestText = it },
                    label = { Text("New Test Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                LiquidGlassButton(
                    text = "Rename",
                    enabled = renameTestText.isNotBlank(),
                    onClick = {
                        if (renameTestText.isNotBlank()) {
                            viewModel.renameTest(testItem.id, renameTestText)
                            testToRename = null
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { testToRename = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

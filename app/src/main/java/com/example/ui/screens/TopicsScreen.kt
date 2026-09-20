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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
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
fun TopicsScreen(
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTopicDetail: (Long) -> Unit
) {
    val topics by viewModel.topics.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var topicToDelete by remember { mutableStateOf<Topic?>(null) }
    var topicToRename by remember { mutableStateOf<Topic?>(null) }

    LiquidGlassBackdrop(animated = false) {
        Scaffold(
            topBar = {
                LiquidGlassTopAppBar(
                    title = { Text("Topics & Subjects", fontWeight = FontWeight.Bold) },
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
                        IconButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.testTag("add_topic_fab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Topic",
                                tint = TextPrimary
                            )
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
                        .clickable { showCreateDialog = true }
                        .testTag("add_topic_button")
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
                            text = "New Topic",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (topics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidGlassCard(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
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
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("No topics yet", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Tap + to add your first subject or topic.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(topics, key = { it.id }) { topic ->
                        val dateStr = remember(topic.createdAt) {
                            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(topic.createdAt))
                        }

                        LiquidGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("topic_card_${topic.id}"),
                            shape = RoundedCornerShape(22.dp),
                            fillColor = GlassFill,
                            showSpecular = true,
                            onClick = { onNavigateToTopicDetail(topic.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
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
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = TextPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = topic.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Created $dateStr",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { topicToRename = topic }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Rename Topic",
                                            tint = TextSecondary
                                        )
                                    }
                                    IconButton(onClick = { topicToDelete = topic }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Topic",
                                            tint = ErrorRed
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

    if (showCreateDialog) {
        var newName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = FrostedBarBackground,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Create New Topic", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Topic / Subject Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                LiquidGlassButton(
                    text = "Create",
                    enabled = newName.isNotBlank(),
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.createTopic(newName) {
                                showCreateDialog = false
                            }
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    topicToDelete?.let { topic ->
        AlertDialog(
            onDismissRequest = { topicToDelete = null },
            containerColor = FrostedBarBackground,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Delete Topic?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Deleting '${topic.name}' will also delete all tests and questions inside it.", color = TextSecondary) },
            confirmButton = {
                LiquidGlassButton(
                    text = "Delete",
                    variant = GlassButtonVariant.Danger,
                    onClick = {
                        viewModel.deleteTopic(topic)
                        topicToDelete = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { topicToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    topicToRename?.let { topic ->
        var renameText by remember(topic) { mutableStateOf(topic.name) }
        AlertDialog(
            onDismissRequest = { topicToRename = null },
            containerColor = FrostedBarBackground,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Rename Topic", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("New Topic Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                LiquidGlassButton(
                    text = "Rename",
                    enabled = renameText.isNotBlank(),
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameTopic(topic.id, renameText)
                            topicToRename = null
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { topicToRename = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

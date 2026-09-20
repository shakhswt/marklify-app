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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
                    title = { Text(stringResource(R.string.topics_and_subjects), fontWeight = FontWeight.Bold) },
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
                        IconButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.testTag("add_topic_fab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.create_topic),
                                tint = TextPrimary
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                LiquidGlassFAB(
                    icon = Icons.Default.Add,
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("add_topic_button"),
                    contentDescription = stringResource(R.string.new_topic)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (topics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidGlassEmptyState(
                        icon = Icons.Default.Folder,
                        title = stringResource(R.string.no_topics_title),
                        description = stringResource(R.string.no_topics_hint)
                    )
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
                                            text = stringResource(R.string.created_date, dateStr),
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { topicToRename = topic }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = stringResource(R.string.rename_topic),
                                            tint = TextSecondary
                                        )
                                    }
                                    IconButton(onClick = { topicToDelete = topic }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete_topic),
                                            tint = ErrorRed
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

    if (showCreateDialog) {
        var newName by remember { mutableStateOf("") }
        LiquidGlassDialog(
            onDismissRequest = { showCreateDialog = false }
        ) {
            Text(
                text = stringResource(R.string.create_topic_dialog_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
            LiquidGlassTextField(
                value = newName,
                onValueChange = { newName = it },
                label = stringResource(R.string.topic_name_label),
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
                    onClick = { showCreateDialog = false }
                )
                LiquidGlassButton(
                    text = stringResource(R.string.create_topic),
                    variant = GlassButtonVariant.Primary,
                    enabled = newName.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.createTopic(newName) {
                                showCreateDialog = false
                            }
                        }
                    }
                )
            }
        }
    }

    topicToDelete?.let { topic ->
        LiquidGlassDialog(
            onDismissRequest = { topicToDelete = null }
        ) {
            Text(
                text = stringResource(R.string.delete_topic_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.delete_topic_message, topic.name),
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
                    onClick = { topicToDelete = null }
                )
                LiquidGlassButton(
                    text = stringResource(R.string.delete),
                    variant = GlassButtonVariant.Danger,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.deleteTopic(topic)
                        topicToDelete = null
                    }
                )
            }
        }
    }

    topicToRename?.let { topic ->
        var renameText by remember(topic) { mutableStateOf(topic.name) }
        LiquidGlassDialog(
            onDismissRequest = { topicToRename = null }
        ) {
            Text(
                text = stringResource(R.string.rename_topic),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
            LiquidGlassTextField(
                value = renameText,
                onValueChange = { renameText = it },
                label = stringResource(R.string.new_topic_name_label),
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
                    onClick = { topicToRename = null }
                )
                LiquidGlassButton(
                    text = stringResource(R.string.rename),
                    variant = GlassButtonVariant.Primary,
                    enabled = renameText.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameTopic(topic.id, renameText)
                            topicToRename = null
                        }
                    }
                )
            }
        }
    }
}

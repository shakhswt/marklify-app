package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.backup.DataBackupManager
import com.example.omr.processing.AnswerDetector
import com.example.omr.processing.BubbleReading
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import com.example.util.HapticManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentSettings by viewModel.appSettings.collectAsState()

    var fillThreshold by remember(currentSettings) { mutableFloatStateOf(currentSettings.fillThreshold) }
    var unansweredThreshold by remember(currentSettings) { mutableFloatStateOf(currentSettings.unansweredThreshold) }
    var multipleDiffMargin by remember(currentSettings) { mutableFloatStateOf(currentSettings.multipleDiffMargin) }
    var ambiguousDiffMargin by remember(currentSettings) { mutableFloatStateOf(currentSettings.ambiguousDiffMargin) }
    var defaultPageSize by remember(currentSettings) { mutableStateOf(currentSettings.defaultPageSize) }

    var importMessage by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    // Backup restore file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream)).readText()
                }
                if (!jsonString.isNullOrBlank()) {
                    viewModel.importDatabaseBackup(
                        jsonString = jsonString,
                        onError = { errorMsg ->
                            Toast.makeText(context, context.getString(R.string.backup_read_failed, errorMsg), Toast.LENGTH_LONG).show()
                        },
                        onComplete = { stats ->
                            HapticManager.performSubmissionSuccess(context, currentSettings.hapticsEnabled)
                            importMessage = context.getString(
                                R.string.backup_restored_summary,
                                stats.topicCount,
                                stats.testCount,
                                stats.questionCount,
                                stats.scanCount
                            )
                        }
                    )
                } else {
                    Toast.makeText(context, context.getString(R.string.backup_empty), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.backup_read_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            MarklifyTopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.resetThresholdsToDefault()
                            fillThreshold = AnswerDetector.DEFAULT_FILL_THRESHOLD
                            unansweredThreshold = AnswerDetector.DEFAULT_UNANSWERED_THRESHOLD
                            multipleDiffMargin = AnswerDetector.DEFAULT_MULTIPLE_DIFF_MARGIN
                            ambiguousDiffMargin = AnswerDetector.DEFAULT_AMBIGUOUS_DIFF_MARGIN
                            Toast.makeText(context, context.getString(R.string.reset_thresholds_toast), Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = stringResource(R.string.reset_thresholds),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Theme Selector Card (Classic Light / Dark / System)
            MarklifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Appearance Theme",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themes = listOf(
                            Triple("SYSTEM", "System", "⚙️"),
                            Triple("LIGHT", "Light", "☀️"),
                            Triple("DARK", "Dark", "🌙")
                        )
                        themes.forEach { (code, label, icon) ->
                            val isSelected = currentSettings.themeMode == code
                            MarklifyChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setThemeMode(code)
                                    HapticManager.performBubbleDetectedTick(context, currentSettings.hapticsEnabled)
                                },
                                label = "$icon $label",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Language Picker Card
            MarklifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.language_section),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val languages = listOf(
                            Triple("en", stringResource(R.string.language_en), "🇺🇸"),
                            Triple("uz", stringResource(R.string.language_uz), "🇺🇿"),
                            Triple("ru", stringResource(R.string.language_ru), "🇷🇺")
                        )
                        languages.forEach { (code, label, flag) ->
                            val isSelected = currentSettings.appLanguage == code
                            MarklifyChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setAppLanguage(code)
                                    HapticManager.performBubbleDetectedTick(context, currentSettings.hapticsEnabled)
                                },
                                label = "$flag $label",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. Tactile Haptic Feedback Switch Card
            MarklifyCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.haptics_section),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.haptics_desc),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    MarklifySwitch(
                        checked = currentSettings.hapticsEnabled,
                        onCheckedChange = {
                            viewModel.setHapticsEnabled(it)
                            if (it) HapticManager.performSubmissionSuccess(context, true)
                        }
                    )
                }
            }

            // 4. Default Page Size Card
            MarklifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.default_page_size),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val a4Selected = defaultPageSize == "A4"
                        MarklifyChip(
                            selected = a4Selected,
                            onClick = { defaultPageSize = "A4" },
                            label = stringResource(R.string.format_a4),
                            modifier = Modifier.weight(1f)
                        )

                        val letterSelected = defaultPageSize == "LETTER"
                        MarklifyChip(
                            selected = letterSelected,
                            onClick = { defaultPageSize = "LETTER" },
                            label = stringResource(R.string.format_letter),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Database Backup & Restore Card
            MarklifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.backup_section),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = stringResource(R.string.backup_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MarklifyButton(
                            onClick = {
                                isExporting = true
                                viewModel.exportDatabaseBackup(context) { file ->
                                    isExporting = false
                                    DataBackupManager.shareFile(
                                        context = context,
                                        file = file,
                                        mimeType = "application/json",
                                        chooserTitle = context.getString(R.string.export_backup_chooser)
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            variant = MarklifyButtonVariant.Neutral,
                            icon = Icons.Default.Download,
                            text = stringResource(R.string.export_json)
                        )

                        MarklifyButton(
                            onClick = { filePickerLauncher.launch("application/json") },
                            modifier = Modifier.weight(1f),
                            variant = MarklifyButtonVariant.Neutral,
                            icon = Icons.Default.Upload,
                            text = stringResource(R.string.restore_json)
                        )
                    }
                }
            }

            // Save Settings Button
            MarklifyButton(
                onClick = {
                    viewModel.saveThresholdSettings(
                        fillThreshold = fillThreshold,
                        unansweredThreshold = unansweredThreshold,
                        multipleDiffMargin = multipleDiffMargin,
                        ambiguousDiffMargin = ambiguousDiffMargin,
                        defaultPageSize = defaultPageSize
                    )
                    HapticManager.performSubmissionSuccess(context, currentSettings.hapticsEnabled)
                    Toast.makeText(context, context.getString(R.string.settings_saved_toast), Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_settings_button"),
                variant = MarklifyButtonVariant.Primary,
                icon = Icons.Default.Save,
                text = stringResource(R.string.save_settings)
            )

            // Restore Dialog
            importMessage?.let { msg ->
                MarklifyDialog(
                    onDismissRequest = { importMessage = null }
                ) {
                    Text(
                        text = stringResource(R.string.backup_restored_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    MarklifyButton(
                        text = stringResource(R.string.done),
                        onClick = { importMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        variant = MarklifyButtonVariant.Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

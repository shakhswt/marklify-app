package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.entity.AppSettings
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import com.example.util.HapticManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentSettings by viewModel.appSettings.collectAsState()

    var editableSettings by remember(currentSettings) { mutableStateOf(currentSettings) }
    var hasChanges by remember(currentSettings, editableSettings) { mutableStateOf(currentSettings != editableSettings) }

    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var isImportSuccess by remember { mutableStateOf(true) }

    // File launcher for Json Restore
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: ""
                viewModel.importDatabaseBackup(
                    jsonString = jsonString,
                    onError = { err ->
                        importStatusMessage = err
                        isImportSuccess = false
                    },
                    onComplete = { stats ->
                        importStatusMessage = context.getString(R.string.restored_summary_format, stats.topicCount, stats.testCount, stats.scanCount)
                        isImportSuccess = true
                    }
                )
            } catch (e: Exception) {
                importStatusMessage = context.getString(R.string.failed_open_backup, e.message ?: "")
                isImportSuccess = false
            }
        }
    }

    Scaffold(
        topBar = {
            MarklifyTopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.resetThresholdsToDefault()
                        Toast.makeText(context, context.getString(R.string.reset_thresholds_toast), Toast.LENGTH_SHORT).show()
                    }) {
                        Text(stringResource(R.string.reset_thresholds), fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MarklifyButton(
                        text = stringResource(R.string.cancel),
                        onClick = { editableSettings = currentSettings },
                        modifier = Modifier.weight(1f),
                        variant = MarklifyButtonVariant.Neutral,
                        enabled = hasChanges
                    )
                    MarklifyButton(
                        text = stringResource(R.string.save_settings),
                        onClick = {
                            viewModel.saveSettings(editableSettings)
                            HapticManager.performSubmissionSuccess(context, editableSettings.hapticsEnabled)
                            Toast.makeText(context, context.getString(R.string.settings_saved_toast), Toast.LENGTH_SHORT).show()
                            hasChanges = false
                        },
                        modifier = Modifier.weight(1f),
                        variant = MarklifyButtonVariant.Primary,
                        enabled = hasChanges
                    )
                }
            }
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
            // 1. Appearance Theme (Existing)
            Text(stringResource(R.string.appearance_theme), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            MarklifyCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.theme_mode), fontSize = 15.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "SYSTEM" to stringResource(R.string.theme_system),
                                "LIGHT" to stringResource(R.string.theme_light),
                                "DARK" to stringResource(R.string.theme_dark)
                            ).forEach { (mode, label) ->
                                MarklifyChip(
                                    selected = editableSettings.themeMode == mode,
                                    onClick = {
                                        editableSettings = editableSettings.copy(themeMode = mode)
                                        viewModel.setThemeMode(mode)
                                    },
                                    label = label
                                )
                            }
                        }
                    }
                }
            }

            // 2. Language (Existing)
            Text(stringResource(R.string.language_section), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            MarklifyCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.app_language), fontSize = 15.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "en" to "🇬🇧 English",
                                "uz" to "🇺🇿 O'zbek",
                                "ru" to "🇷🇺 Русский"
                            ).forEach { (code, label) ->
                                MarklifyChip(
                                    selected = editableSettings.appLanguage == code,
                                    onClick = {
                                        editableSettings = editableSettings.copy(appLanguage = code)
                                        viewModel.setAppLanguage(code)
                                    },
                                    label = label
                                )
                            }
                        }
                    }
                }
            }

            // 3. NEW: Scan Settings
            Text(stringResource(R.string.scan_settings), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            MarklifyCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(stringResource(R.string.sound), fontSize = 15.sp)
                            Text(stringResource(R.string.sound_desc), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        MarklifySwitch(
                            checked = editableSettings.soundEnabled,
                            onCheckedChange = { editableSettings = editableSettings.copy(soundEnabled = it) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(stringResource(R.string.save_images), fontSize = 15.sp)
                            Text(stringResource(R.string.save_images_desc), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        MarklifySwitch(
                            checked = editableSettings.saveImagesEnabled,
                            onCheckedChange = { editableSettings = editableSettings.copy(saveImagesEnabled = it) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(stringResource(R.string.auto_save), fontSize = 15.sp)
                            Text(stringResource(R.string.auto_save_desc), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        MarklifySwitch(
                            checked = editableSettings.autoSaveEnabled,
                            onCheckedChange = { editableSettings = editableSettings.copy(autoSaveEnabled = it) }
                        )
                    }
                    if (editableSettings.autoSaveEnabled) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.delay_seconds), fontSize = 15.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1, 3, 5).forEach { sec ->
                                    MarklifyChip(
                                        selected = editableSettings.autoSaveDelaySeconds == sec,
                                        onClick = { editableSettings = editableSettings.copy(autoSaveDelaySeconds = sec) },
                                        label = "${sec}s"
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.scan_resolution), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = editableSettings.scanResolution == "Default",
                                onClick = { editableSettings = editableSettings.copy(scanResolution = "Default") }
                            )
                            Column {
                                Text(stringResource(R.string.default_option), fontSize = 15.sp)
                                Text(stringResource(R.string.scan_resolution_default_desc), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = editableSettings.scanResolution == "High",
                                onClick = { editableSettings = editableSettings.copy(scanResolution = "High") }
                            )
                            Text(stringResource(R.string.highest_label), fontSize = 15.sp)
                        }
                    }
                }
            }

            // 4. Haptic Feedback Switch (Existing)
            Text(stringResource(R.string.haptics_section), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            MarklifyCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(stringResource(R.string.haptics_toggle), fontSize = 15.sp)
                            Text(stringResource(R.string.haptics_subtitle), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        MarklifySwitch(
                            checked = editableSettings.hapticsEnabled,
                            onCheckedChange = {
                                editableSettings = editableSettings.copy(hapticsEnabled = it)
                                viewModel.setHapticsEnabled(it)
                            }
                        )
                    }
                }
            }

            // 5. Default Page Size (Existing)
            Text(stringResource(R.string.default_page_size), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            MarklifyCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.paper_size), fontSize = 15.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("A4", "Letter").forEach { size ->
                                MarklifyChip(
                                    selected = editableSettings.defaultPageSize == size,
                                    onClick = { editableSettings = editableSettings.copy(defaultPageSize = size) },
                                    label = size
                                )
                            }
                        }
                    }
                }
            }

            // 6. NEW: Template Design
            Text(stringResource(R.string.template_design), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            MarklifyCard {
                Column {
                    val headerStub = stringResource(R.string.header_template_stub)
                    val labelsStub = stringResource(R.string.labels_template_stub)
                    SettingsRowArrow(title = stringResource(R.string.header_template)) { Toast.makeText(context, headerStub, Toast.LENGTH_SHORT).show() }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    SettingsRowArrow(title = stringResource(R.string.labels_template)) { Toast.makeText(context, labelsStub, Toast.LENGTH_SHORT).show() }
                }
            }

            // 7. Database Backup & Restore (Existing)
            Text(stringResource(R.string.backup_section), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            MarklifyCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(stringResource(R.string.backup_desc), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MarklifyButton(
                            text = stringResource(R.string.export_json),
                            onClick = {
                                viewModel.exportDatabaseBackup(context) { file ->
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.export_backup_chooser)))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            variant = MarklifyButtonVariant.Outlined,
                            icon = Icons.Default.FileDownload
                        )

                        MarklifyButton(
                            text = stringResource(R.string.restore_json),
                            onClick = { importLauncher.launch("application/json") },
                            modifier = Modifier.weight(1f),
                            variant = MarklifyButtonVariant.Outlined,
                            icon = Icons.Default.FileUpload
                        )
                    }

                    importStatusMessage?.let { msg ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isImportSuccess) SuccessGreenBg else ErrorRedBg
                        ) {
                            Text(
                                text = msg,
                                modifier = Modifier.padding(12.dp),
                                color = if (isImportSuccess) SuccessGreen else ErrorRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 8. NEW: Account Setting
            Text(stringResource(R.string.account_setting), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            MarklifyCard {
                Column {
                    val editStub = stringResource(R.string.edit_profile_stub)
                    val cancelNote = stringResource(R.string.cancel_account_deletion_note)
                    val termsStub = stringResource(R.string.terms_privacy_policy_stub)
                    SettingsRowArrow(title = stringResource(R.string.edit_profile)) { Toast.makeText(context, editStub, Toast.LENGTH_SHORT).show() }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    SettingsRowArrow(title = stringResource(R.string.cancel_account_deletion)) { Toast.makeText(context, cancelNote, Toast.LENGTH_SHORT).show() }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    SettingsRowArrow(title = stringResource(R.string.terms_privacy_policy)) { Toast.makeText(context, termsStub, Toast.LENGTH_SHORT).show() }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsRowArrow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 15.sp)
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

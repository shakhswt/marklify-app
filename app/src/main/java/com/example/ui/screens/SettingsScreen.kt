package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

    // Live test calibration computation
    val testResults = remember(fillThreshold, unansweredThreshold, multipleDiffMargin, ambiguousDiffMargin) {
        val detector = AnswerDetector(
            fillThreshold = fillThreshold,
            unansweredThreshold = unansweredThreshold,
            multipleDiffMargin = multipleDiffMargin,
            ambiguousDiffMargin = ambiguousDiffMargin
        )
        val testMap = mapOf(
            1 to listOf(
                BubbleReading(1, 0, "A", 0.68f, 68, 100),
                BubbleReading(1, 1, "B", 0.08f, 8, 100),
                BubbleReading(1, 2, "C", 0.04f, 4, 100),
                BubbleReading(1, 3, "D", 0.06f, 6, 100)
            ),
            2 to listOf(
                BubbleReading(2, 0, "A", 0.52f, 52, 100),
                BubbleReading(2, 1, "B", 0.49f, 49, 100),
                BubbleReading(2, 2, "C", 0.05f, 5, 100),
                BubbleReading(2, 3, "D", 0.03f, 3, 100)
            ),
            3 to listOf(
                BubbleReading(3, 0, "A", 0.12f, 12, 100),
                BubbleReading(3, 1, "B", 0.14f, 14, 100),
                BubbleReading(3, 2, "C", 0.09f, 9, 100),
                BubbleReading(3, 3, "D", 0.11f, 11, 100)
            ),
            4 to listOf(
                BubbleReading(4, 0, "A", 0.32f, 32, 100),
                BubbleReading(4, 1, "B", 0.24f, 24, 100),
                BubbleReading(4, 2, "C", 0.05f, 5, 100),
                BubbleReading(4, 3, "D", 0.03f, 3, 100)
            )
        )
        detector.detectAnswers(testMap)
    }

    LiquidGlassBackdrop(animated = false) {
        Scaffold(
            topBar = {
                LiquidGlassTopAppBar(
                    title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
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
                                tint = TextSecondary
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Language Picker Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    fillColor = GlassFill,
                    showSpecular = true
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.language_section),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = TextPrimary
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
                                LiquidGlassChip(
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

                // 2. Tactile Haptic Feedback Switch Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    fillColor = GlassFill,
                    showSpecular = true
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(GlassFillElevated)
                                    .border(1.dp, GlassBorderBright, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.haptics_section),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.haptics_desc),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        LiquidGlassSwitch(
                            checked = currentSettings.hapticsEnabled,
                            onCheckedChange = {
                                viewModel.setHapticsEnabled(it)
                                if (it) HapticManager.performSubmissionSuccess(context, true)
                            }
                        )
                    }
                }

                // 3. Calibration Header Info Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    fillColor = GlassFill,
                    showSpecular = true
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GlassFillElevated)
                                .border(1.dp, GlassBorderBright, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.calibration_section),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.calibration_desc),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // 4. Calibration Sliders Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    fillColor = GlassFill,
                    showSpecular = true
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = stringResource(R.string.calibration_section),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )

                        // 1. Fill Threshold
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.fill_threshold), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(String.format(Locale.US, "%.0f%%", fillThreshold * 100), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text(stringResource(R.string.fill_threshold_desc), fontSize = 11.sp, color = TextSecondary)
                            Slider(
                                value = fillThreshold,
                                onValueChange = { fillThreshold = it },
                                valueRange = 0.15f..0.70f,
                                steps = 10,
                                colors = SliderDefaults.colors(
                                    thumbColor = TextPrimary,
                                    activeTrackColor = TextPrimary.copy(alpha = 0.7f),
                                    inactiveTrackColor = GlassFillSubtle
                                )
                            )
                        }

                        // 2. Unanswered Threshold
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.unanswered_threshold), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(String.format(Locale.US, "%.0f%%", unansweredThreshold * 100), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text(stringResource(R.string.unanswered_threshold_desc), fontSize = 11.sp, color = TextSecondary)
                            Slider(
                                value = unansweredThreshold,
                                onValueChange = { unansweredThreshold = it },
                                valueRange = 0.05f..0.40f,
                                steps = 7,
                                colors = SliderDefaults.colors(
                                    thumbColor = TextPrimary,
                                    activeTrackColor = TextPrimary.copy(alpha = 0.7f),
                                    inactiveTrackColor = GlassFillSubtle
                                )
                            )
                        }

                        // 3. Multiple Mark Difference Margin
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.multiple_diff), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(String.format(Locale.US, "%.0f%%", multipleDiffMargin * 100), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                            }
                            Text(stringResource(R.string.multiple_diff_desc), fontSize = 11.sp, color = TextSecondary)
                            Slider(
                                value = multipleDiffMargin,
                                onValueChange = { multipleDiffMargin = it },
                                valueRange = 0.05f..0.35f,
                                steps = 5,
                                colors = SliderDefaults.colors(
                                    thumbColor = WarningAmber,
                                    activeTrackColor = WarningAmber,
                                    inactiveTrackColor = GlassFillSubtle
                                )
                            )
                        }

                        // 4. Ambiguous Difference Margin
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.ambiguous_diff), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(String.format(Locale.US, "%.0f%%", ambiguousDiffMargin * 100), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PurpleAccent)
                            }
                            Text(stringResource(R.string.ambiguous_diff_desc), fontSize = 11.sp, color = TextSecondary)
                            Slider(
                                value = ambiguousDiffMargin,
                                onValueChange = { ambiguousDiffMargin = it },
                                valueRange = 0.05f..0.40f,
                                steps = 6,
                                colors = SliderDefaults.colors(
                                    thumbColor = PurpleAccent,
                                    activeTrackColor = PurpleAccent,
                                    inactiveTrackColor = GlassFillSubtle
                                )
                            )
                        }
                    }
                }

                // 5. Live Test Pattern Preview Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    fillColor = GlassFill,
                    showSpecular = true
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.live_calibration),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            LiquidGlassBadge(text = stringResource(R.string.simulated_sample))
                        }

                        Text(
                            text = stringResource(R.string.live_calibration_desc),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        testResults.forEach { item ->
                            val (label, badgeColor, badgeBg, badgeBorder) = when {
                                item.isUnanswered -> Quad(stringResource(R.string.unanswered_flag), TextSecondary, GlassFillSubtle, GlassBorderSubtle)
                                item.isMultiple -> Quad(stringResource(R.string.multiple_marked_flag), WarningAmber, WarningAmberBg, WarningAmberBorder)
                                item.isAmbiguous -> Quad(stringResource(R.string.ambiguous_flag), PurpleAccent, Color(0x33A78BFA), Color(0x66A78BFA))
                                else -> Quad(stringResource(R.string.single_answer, item.detectedAnswer), SuccessGreen, SuccessGreenBg, SuccessGreenBorder)
                            }

                            val topRatio = item.bubbleReadings.maxOfOrNull { it.fillRatio } ?: 0f

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassFillElevated)
                                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.question_number, item.questionNumber),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Peak: ${(topRatio * 100).toInt()}% • Conf: ${(item.confidence * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeBg)
                                        .border(1.dp, badgeBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = badgeColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Default Page Size Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    fillColor = GlassFill,
                    showSpecular = true
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.default_page_size),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val a4Selected = defaultPageSize == "A4"
                            LiquidGlassChip(
                                selected = a4Selected,
                                onClick = { defaultPageSize = "A4" },
                                label = stringResource(R.string.format_a4),
                                modifier = Modifier.weight(1f)
                            )

                            val letterSelected = defaultPageSize == "LETTER"
                            LiquidGlassChip(
                                selected = letterSelected,
                                onClick = { defaultPageSize = "LETTER" },
                                label = stringResource(R.string.format_letter),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 7. Save Settings Button
                LiquidGlassButton(
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
                    variant = GlassButtonVariant.Primary,
                    icon = Icons.Default.Save,
                    text = stringResource(R.string.save_settings)
                )

                // 8. Database Backup & Restore Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    fillColor = GlassFill,
                    showSpecular = true
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.backup_section),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = stringResource(R.string.backup_desc),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LiquidGlassButton(
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
                                variant = GlassButtonVariant.Neutral,
                                icon = Icons.Default.Download,
                                text = stringResource(R.string.export_json)
                            )

                            LiquidGlassButton(
                                onClick = { filePickerLauncher.launch("application/json") },
                                modifier = Modifier.weight(1f),
                                variant = GlassButtonVariant.Neutral,
                                icon = Icons.Default.Upload,
                                text = stringResource(R.string.restore_json)
                            )
                        }
                    }
                }

                // Restore Dialog in Liquid Glass
                importMessage?.let { msg ->
                    LiquidGlassDialog(
                        onDismissRequest = { importMessage = null }
                    ) {
                        Text(
                            text = stringResource(R.string.backup_restored_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = msg,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        LiquidGlassButton(
                            text = stringResource(R.string.done),
                            onClick = { importMessage = null },
                            modifier = Modifier.fillMaxWidth(),
                            variant = GlassButtonVariant.Primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

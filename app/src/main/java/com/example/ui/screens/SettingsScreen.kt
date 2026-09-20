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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.DataBackupManager
import com.example.omr.processing.AnswerDetector
import com.example.omr.processing.BubbleReading
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.LiquidGlassBackdrop
import com.example.ui.components.LiquidGlassBadge
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
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

    var isSavedNotification by remember { mutableStateOf(false) }
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
                    viewModel.importDatabaseBackup(jsonString) { stats ->
                        importMessage = "Successfully imported:\n• ${stats.topicCount} topics\n• ${stats.testCount} tests\n• ${stats.questionCount} questions\n• ${stats.scanCount} scan records"
                    }
                } else {
                    Toast.makeText(context, "Selected backup file was empty", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read backup: ${e.message}", Toast.LENGTH_LONG).show()
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
        // Simulated test pattern: 4 questions with varied bubble fills
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
                    title = { Text("Settings & Calibration", fontWeight = FontWeight.Bold) },
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
                            onClick = {
                                viewModel.resetThresholdsToDefault()
                                fillThreshold = AnswerDetector.DEFAULT_FILL_THRESHOLD
                                unansweredThreshold = AnswerDetector.DEFAULT_UNANSWERED_THRESHOLD
                                multipleDiffMargin = AnswerDetector.DEFAULT_MULTIPLE_DIFF_MARGIN
                                ambiguousDiffMargin = AnswerDetector.DEFAULT_AMBIGUOUS_DIFF_MARGIN
                                Toast.makeText(context, "Thresholds reset to default", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset to Defaults",
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
                // Header Info Card
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
                                text = "OMR Sensitivity Calibration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tune optical mark detection margins to adapt to varying pencil darkness, pen strokes, or ambient lighting.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Calibration Sliders Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    fillColor = GlassFill,
                    showSpecular = true
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Detection Thresholds",
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
                                Text("Marked Fill Threshold", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(String.format(Locale.US, "%.0f%%", fillThreshold * 100), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text("Minimum bubble pixel coverage to consider a mark valid.", fontSize = 11.sp, color = TextSecondary)
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
                                Text("Blank Bubble Floor", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(String.format(Locale.US, "%.0f%%", unansweredThreshold * 100), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text("Any bubble filled below this percentage is treated as completely blank.", fontSize = 11.sp, color = TextSecondary)
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
                                Text("Multiple Mark Margin", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(String.format(Locale.US, "%.0f%%", multipleDiffMargin * 100), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                            }
                            Text("If two bubbles differ by less than this margin and exceed fill threshold, marked as multiple.", fontSize = 11.sp, color = TextSecondary)
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
                                Text("Ambiguity / Review Margin", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(String.format(Locale.US, "%.0f%%", ambiguousDiffMargin * 100), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PurpleAccent)
                            }
                            Text("If 1st and 2nd bubble values are within this margin, flags item for teacher verification.", fontSize = 11.sp, color = TextSecondary)
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

                // Live Test Pattern Preview Card
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
                                text = "Live Calibration Test Pattern",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            LiquidGlassBadge(text = "Simulated Sample")
                        }

                        Text(
                            text = "See how your current slider parameters classify 4 representative answer scenarios in real-time:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        testResults.forEach { item ->
                            val (label, badgeColor, badgeBg, badgeBorder) = when {
                                item.isUnanswered -> Quad("Unanswered", TextSecondary, GlassFillSubtle, GlassBorderSubtle)
                                item.isMultiple -> Quad("Multiple Marked", WarningAmber, WarningAmberBg, WarningAmberBorder)
                                item.isAmbiguous -> Quad("Ambiguous / Review", PurpleAccent, Color(0x33A78BFA), Color(0x66A78BFA))
                                else -> Quad("Single Answer: ${item.detectedAnswer}", SuccessGreen, SuccessGreenBg, SuccessGreenBorder)
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
                                        text = "Question #${item.questionNumber}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Peak Fill: ${(topRatio * 100).toInt()}% • Confidence: ${(item.confidence * 100).toInt()}%",
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

                // Default Page Size
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    fillColor = GlassFill,
                    showSpecular = true
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Default Printable Sheet Paper Size",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val a4Selected = defaultPageSize == "A4"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (a4Selected) GlassFillElevated else GlassFillSubtle)
                                    .border(1.dp, if (a4Selected) GlassBorderBright else GlassBorderSubtle, RoundedCornerShape(14.dp))
                                    .clickable { defaultPageSize = "A4" }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "A4 (210 × 297 mm)",
                                    fontSize = 12.sp,
                                    fontWeight = if (a4Selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (a4Selected) TextPrimary else TextSecondary
                                )
                            }

                            val letterSelected = defaultPageSize == "LETTER"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (letterSelected) GlassFillElevated else GlassFillSubtle)
                                    .border(1.dp, if (letterSelected) GlassBorderBright else GlassBorderSubtle, RoundedCornerShape(14.dp))
                                    .clickable { defaultPageSize = "LETTER" }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "US Letter (8.5 × 11\")",
                                    fontSize = 12.sp,
                                    fontWeight = if (letterSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (letterSelected) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Save Settings Button
                LiquidGlassButton(
                    onClick = {
                        viewModel.saveThresholdSettings(
                            fillThreshold = fillThreshold,
                            unansweredThreshold = unansweredThreshold,
                            multipleDiffMargin = multipleDiffMargin,
                            ambiguousDiffMargin = ambiguousDiffMargin,
                            defaultPageSize = defaultPageSize
                        )
                        Toast.makeText(context, "Settings & Thresholds Saved Successfully", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_settings_button"),
                    variant = GlassButtonVariant.Primary,
                    icon = Icons.Default.Save,
                    text = "Save Thresholds & Settings"
                )

                // Database Backup & Restore Card
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
                                text = "Database Backup & Restore",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Export your entire topics, tests, question keys, and graded scan records to an offline JSON file, or restore data on a new device.",
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
                                            chooserTitle = "Export Marklify Database Backup"
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                variant = GlassButtonVariant.Neutral,
                                icon = Icons.Default.Download,
                                text = "Export JSON"
                            )

                            LiquidGlassButton(
                                onClick = { filePickerLauncher.launch("application/json") },
                                modifier = Modifier.weight(1f),
                                variant = GlassButtonVariant.Neutral,
                                icon = Icons.Default.Upload,
                                text = "Restore JSON"
                            )
                        }
                    }
                }

                // Restore Dialog / Message
                importMessage?.let { msg ->
                    AlertDialog(
                        onDismissRequest = { importMessage = null },
                        containerColor = FrostedBarBackground,
                        shape = RoundedCornerShape(24.dp),
                        title = { Text("Backup Restored", fontWeight = FontWeight.Bold, color = TextPrimary) },
                        text = { Text(msg, color = TextSecondary) },
                        confirmButton = {
                            LiquidGlassButton(
                                text = "Done",
                                onClick = { importMessage = null }
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

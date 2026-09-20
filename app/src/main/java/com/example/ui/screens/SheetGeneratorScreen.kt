package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.entity.TestEntity
import com.example.data.entity.Topic
import com.example.omr.spec.SheetSpec
import com.example.pdf.OmrSheetGenerator
import com.example.pdf.PageFormat
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.LiquidGlassBackdrop
import com.example.ui.components.LiquidGlassBottomBar
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassChip
import com.example.ui.components.LiquidGlassTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetGeneratorScreen(
    testId: Long,
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToScanner: (Long) -> Unit
) {
    val context = LocalContext.current
    val appSettings by viewModel.appSettings.collectAsState()

    var test by remember { mutableStateOf<TestEntity?>(null) }
    var topic by remember { mutableStateOf<Topic?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var selectedPageFormat by remember {
        mutableStateOf(if (appSettings.defaultPageSize == "LETTER") PageFormat.LETTER else PageFormat.A4)
    }

    LaunchedEffect(testId) {
        val t = viewModel.getTestById(testId)
        test = t
        if (t != null) {
            val top = viewModel.getTopicById(t.topicId)
            topic = top
            withContext(Dispatchers.Default) {
                val spec = SheetSpec(questionCount = t.questionCount)
                val bmp = OmrSheetGenerator.generateBitmap(
                    spec = spec,
                    testName = t.name,
                    topicName = top?.name ?: "General"
                )
                previewBitmap = bmp
            }
        }
    }

    LiquidGlassBackdrop(animated = false) {
        Scaffold(
            topBar = {
                LiquidGlassTopAppBar(
                    title = { Text(stringResource(R.string.sheet_generator_title), fontWeight = FontWeight.Bold) },
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
                                val currentTest = test ?: return@IconButton
                                viewModel.loadTestForScan(currentTest.id) {
                                    onNavigateToScanner(currentTest.id)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = stringResource(R.string.scan_sheet),
                                tint = SuccessGreen
                            )
                        }
                    }
                )
            },
            bottomBar = {
                LiquidGlassBottomBar {
                    LiquidGlassButton(
                        onClick = {
                            val currentTest = test ?: return@LiquidGlassButton
                            val currentTopic = topic
                            try {
                                val spec = SheetSpec(questionCount = currentTest.questionCount)
                                val pdfFile = OmrSheetGenerator.generatePdf(
                                    context = context,
                                    spec = spec,
                                    testName = currentTest.name,
                                    topicName = currentTopic?.name ?: "General",
                                    testId = currentTest.id,
                                    pageFormat = selectedPageFormat
                                )
                                OmrSheetGenerator.printPdf(
                                    context = context,
                                    pdfFile = pdfFile,
                                    jobName = "OMR Sheet - ${currentTest.name}"
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, "Printing failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("print_sheet_button"),
                        variant = GlassButtonVariant.Neutral,
                        icon = Icons.Default.Print,
                        text = stringResource(R.string.print_sheet)
                    )

                    LiquidGlassButton(
                        onClick = {
                            val currentTest = test ?: return@LiquidGlassButton
                            val currentTopic = topic
                            isGeneratingPdf = true
                            try {
                                val spec = SheetSpec(questionCount = currentTest.questionCount)
                                val pdfFile = OmrSheetGenerator.generatePdf(
                                    context = context,
                                    spec = spec,
                                    testName = currentTest.name,
                                    topicName = currentTopic?.name ?: "General",
                                    testId = currentTest.id,
                                    pageFormat = selectedPageFormat
                                )
                                sharePdf(context, pdfFile)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isGeneratingPdf = false
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("generate_pdf_button"),
                        variant = GlassButtonVariant.Neutral,
                        icon = Icons.Default.Share,
                        text = stringResource(R.string.share)
                    )

                    LiquidGlassButton(
                        onClick = {
                            val currentTest = test ?: return@LiquidGlassButton
                            viewModel.loadTestForScan(currentTest.id) {
                                onNavigateToScanner(currentTest.id)
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("scan_now_button"),
                        variant = GlassButtonVariant.Success,
                        icon = Icons.Default.CameraAlt,
                        text = stringResource(R.string.scan_sheet)
                    )
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Specs Banner
                test?.let { currentTest ->
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        fillColor = GlassFill,
                        showSpecular = true
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentTest.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GlassFillElevated)
                                        .border(1.dp, GlassBorderBright, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.questions_count, currentTest.questionCount),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (currentTest.questionCount <= 25) {
                                    "Layout: 1 Column • ${selectedPageFormat.displayName} • 4 Corner Markers"
                                } else {
                                    "Layout: 2 Columns • ${selectedPageFormat.displayName} • 4 Corner Markers"
                                },
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Paper Format Selection
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    fillColor = GlassFill
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.sheet_format),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LiquidGlassChip(
                                selected = selectedPageFormat == PageFormat.A4,
                                onClick = { selectedPageFormat = PageFormat.A4 },
                                label = stringResource(R.string.format_a4),
                                modifier = Modifier.weight(1f)
                            )
                            LiquidGlassChip(
                                selected = selectedPageFormat == PageFormat.LETTER,
                                onClick = { selectedPageFormat = PageFormat.LETTER },
                                label = stringResource(R.string.format_letter),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Sheet Canvas/Bitmap Preview
                Text(
                    text = "${stringResource(R.string.preview_sheet)} (${selectedPageFormat.displayName})",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1200f / 1600f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, GlassBorderBright, RoundedCornerShape(16.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = stringResource(R.string.preview_sheet),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(color = TextSecondary)
                    }
                }

                // Quick instructions card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    fillColor = GlassFill
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.tips_scanning_title),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(stringResource(R.string.tip_scale, selectedPageFormat.shortName), fontSize = 12.sp, color = TextSecondary)
                        Text(stringResource(R.string.tip_markers), fontSize = 12.sp, color = TextSecondary)
                        Text(stringResource(R.string.tip_shading), fontSize = 12.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun sharePdf(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Marklify OMR Sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share or Print OMR Sheet"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open share dialog: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

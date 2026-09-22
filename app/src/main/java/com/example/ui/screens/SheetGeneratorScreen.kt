package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
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
import com.example.ui.components.*
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

    var layoutType by remember { mutableStateOf("Continuous") }
    var labelType by remember { mutableStateOf("Default") }
    var headerType by remember { mutableStateOf("Default") }

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

    Scaffold(
        topBar = {
            MarklifyTopAppBar(
                title = { Text(stringResource(R.string.sheet_generator_title), fontWeight = FontWeight.Bold) },
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MarklifyButton(
                        text = stringResource(R.string.back),
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f),
                        variant = MarklifyButtonVariant.Outlined
                    )

                    MarklifyButton(
                        text = stringResource(R.string.save_pdf),
                        onClick = {
                            val currentTest = test ?: return@MarklifyButton
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
                                sharePdf(context, pdfFile)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("generate_pdf_button"),
                        variant = MarklifyButtonVariant.Primary,
                        icon = Icons.Default.Share
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Dropdown Controls (EvalBee Screenshot #3 Style)
            MarklifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.layout_label), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MarklifyChip(selected = true, onClick = {}, label = stringResource(R.string.continuous_layout))
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.label_label), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MarklifyChip(selected = true, onClick = {}, label = stringResource(R.string.default_option))
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.header_label), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MarklifyChip(selected = true, onClick = {}, label = stringResource(R.string.default_option))
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val customToastMsg = stringResource(R.string.custom_answer_sheet)
                    MarklifyButton(
                        text = stringResource(R.string.custom_answer_sheet),
                        onClick = {
                            Toast.makeText(context, customToastMsg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        variant = MarklifyButtonVariant.Primary
                    )
                }
            }

            // Printable OMR Preview Container
            MarklifyCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.preview_sheet),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1200f / 1600f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (previewBitmap != null) {
                            Image(
                                bitmap = previewBitmap!!.asImageBitmap(),
                                contentDescription = stringResource(R.string.preview_sheet),
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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

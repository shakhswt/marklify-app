package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.entity.TestEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Extracted to avoid expensive allocations during recompisitions
private val testDetailMonthFormatter = SimpleDateFormat("MMM", Locale.US)
private val testDetailDayFormatter = SimpleDateFormat("dd", Locale.US)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDetailScreen(
    testId: Long,
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTestEditor: (Long) -> Unit,
    onNavigateToSheetGenerator: (Long) -> Unit,
    onNavigateToScanner: (Long) -> Unit,
    onNavigateToResults: (Long) -> Unit
) {
    val context = LocalContext.current
    var test by remember { mutableStateOf<TestEntity?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    val scanCount by viewModel.scanCount.collectAsState()

    LaunchedEffect(testId) {
        test = viewModel.getTestById(testId)
    }

    val monthStr = remember(test?.createdAt) {
        test?.createdAt?.let { testDetailMonthFormatter.format(Date(it)) } ?: "Jan"
    }
    val dayStr = remember(test?.createdAt) {
        test?.createdAt?.let { testDetailDayFormatter.format(Date(it)) } ?: "20"
    }

    Scaffold(
        topBar = {
            MarklifyTopAppBar(
                title = { Text(stringResource(R.string.test_detail_title), fontWeight = FontWeight.Bold) },
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
                    val linkSharedToast = stringResource(R.string.exam_link_shared)
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(imageVector = Icons.Default.Archive, contentDescription = stringResource(R.string.archive), tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, linkSharedToast, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(R.string.share), tint = MaterialTheme.colorScheme.onSurface)
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
            // Cloud Sync Row
            var isSyncEnabled by remember { mutableStateOf(false) }
            MarklifyCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.cloud_sync), fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    val cloudUnavailableMsg = stringResource(R.string.cloud_sync_unavailable)
                    MarklifySwitch(
                        checked = isSyncEnabled,
                        onCheckedChange = { 
                            isSyncEnabled = it
                            if (it) {
                                Toast.makeText(context, cloudUnavailableMsg, Toast.LENGTH_LONG).show()
                                isSyncEnabled = false
                            }
                        }
                    )
                }
            }

            // Top Summary Card (EvalBee Screenshot #5 Style)
            MarklifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date Badge
                        Surface(
                            modifier = Modifier.size(width = 48.dp, height = 52.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MarklifyBlueContainerLight
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(monthStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MarklifyBlue)
                                Text(dayStr, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MarklifyBlue)
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val defaultExamLabel = stringResource(R.string.exam_type_default)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = test?.name ?: defaultExamLabel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                MarklifyBadge(
                                    text = if (test?.isPublic == true) stringResource(R.string.public_chip) else stringResource(R.string.private_chip),
                                    containerColor = if (test?.isPublic == true) SuccessGreenBg else WarningAmberBg,
                                    contentColor = if (test?.isPublic == true) SuccessGreen else WarningAmber
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                MarklifyBadge(text = test?.examType ?: defaultExamLabel, containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                MarklifyBadge(text = stringResource(R.string.not_synced), containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("? ${test?.questionCount ?: 50}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("🔑 Key", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("👤 ${test?.examType ?: "NEET"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress & Start Scanning
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sheet Scanned $scanCount / 1",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MarklifyButton(
                            text = stringResource(R.string.scan_sheet),
                            onClick = {
                                viewModel.loadTestForScan(testId) {
                                    onNavigateToScanner(testId)
                                }
                            },
                            modifier = Modifier.testTag("start_scanning_button"),
                            variant = MarklifyButtonVariant.Primary
                        )
                    }
                }
            }

            // Exam Management Section
            Text(
                text = stringResource(R.string.exam_management),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionIconButton(
                    title = stringResource(R.string.answer_key),
                    icon = Icons.Default.VpnKey,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTestEditor(testId) }
                )
                ActionIconButton(
                    title = stringResource(R.string.scan_sheet),
                    icon = Icons.Default.CropFree,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.loadTestForScan(testId) {
                            onNavigateToScanner(testId)
                        }
                    }
                )
                ActionIconButton(
                    title = stringResource(R.string.settings_title),
                    icon = Icons.Default.Settings,
                    modifier = Modifier.weight(1f),
                    onClick = { showRenameDialog = true }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionIconButton(
                    title = stringResource(R.string.generate_sheet),
                    icon = Icons.Default.GridOn,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSheetGenerator(testId) }
                )
                val webUnavailMsg = stringResource(R.string.web_features_unavailable)
                ActionIconButton(
                    title = stringResource(R.string.web_features),
                    icon = Icons.Default.Language,
                    modifier = Modifier.weight(1f),
                    onClick = { Toast.makeText(context, webUnavailMsg, Toast.LENGTH_LONG).show() }
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            // Reporting Section
            Text(
                text = stringResource(R.string.reporting_section),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionIconButton(
                    title = stringResource(R.string.view_results),
                    icon = Icons.Default.InsertDriveFile,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToResults(testId) }
                )
                val exportingExcelMsg = stringResource(R.string.exporting_excel)
                ActionIconButton(
                    title = stringResource(R.string.export_csv),
                    icon = Icons.Default.TableChart,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.exportTestResultsCsv(context, testId) { file ->
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, exportingExcelMsg))
                        }
                    }
                )
                ActionIconButton(
                    title = stringResource(R.string.question_analysis),
                    icon = Icons.Default.PieChart,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToResults(testId) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val resultsPublishedMsg = stringResource(R.string.results_published)
                val absenteesListMsg = stringResource(R.string.absentees_list)
                ActionIconButton(
                    title = stringResource(R.string.results_published),
                    icon = Icons.Default.Publish,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        Toast.makeText(context, resultsPublishedMsg, Toast.LENGTH_SHORT).show()
                    }
                )
                ActionIconButton(
                    title = stringResource(R.string.absentees_list),
                    icon = Icons.Default.PersonOff,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        Toast.makeText(context, absenteesListMsg, Toast.LENGTH_SHORT).show()
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            // Other Section
            Text(
                text = stringResource(R.string.other_section),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val exportExmStub = stringResource(R.string.exporting_exm_stub)
                val importExmStub = stringResource(R.string.importing_exm_stub)
                ActionIconButton(
                    title = stringResource(R.string.export_exm),
                    icon = Icons.Default.FileDownload,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        Toast.makeText(context, exportExmStub, Toast.LENGTH_SHORT).show()
                    }
                )
                ActionIconButton(
                    title = stringResource(R.string.import_exm),
                    icon = Icons.Default.FileUpload,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        Toast.makeText(context, importExmStub, Toast.LENGTH_SHORT).show()
                    }
                )
                ActionIconButton(
                    title = stringResource(R.string.import_sheet_image),
                    icon = Icons.Default.Image,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onNavigateToScanner(testId)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showRenameDialog && test != null) {
        var renameText by remember(test) { mutableStateOf(test?.name ?: "") }
        MarklifyDialog(
            onDismissRequest = { showRenameDialog = false }
        ) {
            Text(
                text = stringResource(R.string.rename_test_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = renameText,
                onValueChange = { renameText = it },
                label = { Text(stringResource(R.string.new_test_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MarklifyButton(
                    text = stringResource(R.string.cancel),
                    variant = MarklifyButtonVariant.Neutral,
                    modifier = Modifier.weight(1f),
                    onClick = { showRenameDialog = false }
                )
                MarklifyButton(
                    text = stringResource(R.string.rename),
                    variant = MarklifyButtonVariant.Primary,
                    enabled = renameText.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameTest(testId, renameText)
                            test = test?.copy(name = renameText.trim())
                            showRenameDialog = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ActionIconButton(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.OpenCVState
import com.example.data.entity.Question
import com.example.data.entity.TestEntity
import com.example.omr.detector.MarkerResult
import com.example.omr.spec.PointF
import com.example.omr.spec.SheetSpec
import com.example.pdf.OmrSheetGenerator
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarklifyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.res.stringResource
import com.example.BuildConfig
import com.example.R
import com.example.util.HapticManager
import java.io.InputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    testId: Long,
    viewModel: MarklifyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val isOpenCvInitialized by OpenCVState.isInitialized.collectAsStateWithLifecycle()

    val test by viewModel.selectedTest.collectAsState()
    val questions by viewModel.selectedQuestions.collectAsState()

    val appSettings by viewModel.appSettings.collectAsState()
    val wasStable = remember { AtomicBoolean(false) }
    val isAnalyzing = remember { AtomicBoolean(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var statusText by remember { mutableStateOf("Looking for sheet...") }
    var detectedCorners by remember { mutableStateOf<List<PointF>?>(null) }
    var isProcessingAutoCapture by remember { mutableStateOf(false) }
    var lastErrorMessage by remember { mutableStateOf<String?>(null) }
    var isStableLocked by remember { mutableStateOf(false) }

    val isCapturing = remember { AtomicBoolean(false) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Fallback Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    statusText = "Scanning from photo..."
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (bitmap != null) {
                        val currentTest = test
                        val currentQuestions = questions
                        if (currentTest != null && currentQuestions.isNotEmpty()) {
                            val result = viewModel.omrEngine.processFullSheet(
                                sourceBitmap = bitmap,
                                questionCount = currentTest.questionCount,
                                questions = currentQuestions
                            )

                            withContext(Dispatchers.Main) {
                                if (result.success && result.warpedBitmap != null) {
                                    if (!result.detectedRollNumber.isNullOrBlank()) {
                                        viewModel.studentId.value = result.detectedRollNumber
                                    }
                                    viewModel.setScanSuccess(result.warpedBitmap, result.detectedAnswers)
                                    onNavigateToReview()
                                } else {
                                    lastErrorMessage = result.errorMessage ?: "Could not detect sheet."
                                    statusText = "Looking for sheet..."
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        lastErrorMessage = "Failed to load image: ${e.message}"
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Check OpenCV Initialization State
    if (!isOpenCvInitialized) {
        LiquidGlassBackdrop {
            Scaffold(
                topBar = {
                    LiquidGlassTopAppBar(
                        title = { Text(stringResource(R.string.opencv_unavailable_title), fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back),
                                    tint = TextPrimary
                                )
                            }
                        }
                    )
                },
                containerColor = Color.Transparent
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidGlassErrorCard(
                        title = stringResource(R.string.opencv_unavailable_title),
                        message = stringResource(R.string.opencv_unavailable_desc),
                        retryText = stringResource(R.string.retry_opencv),
                        onRetry = {
                            val success = OpenCVState.initialize()
                            if (success) {
                                Toast.makeText(context, context.getString(R.string.opencv_retry_success), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, context.getString(R.string.opencv_retry_failed), Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            LiquidGlassTopAppBar(
                title = {
                    Column {
                        Text(test?.name ?: stringResource(R.string.scanner_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(stringResource(R.string.scanner_subtitle, questions.size), fontSize = 11.sp, color = TextSecondary)
                    }
                },
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
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.testTag("gallery_picker_button")
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = stringResource(R.string.pick_photo), tint = TextPrimary)
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasCameraPermission) {
                // CameraX Preview & Analyzer
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                if (isCapturing.get() || isProcessingAutoCapture) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }
                                if (!isAnalyzing.compareAndSet(false, true)) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                try {
                                    val bitmap = imageProxyToBitmap(imageProxy)
                                    if (bitmap != null) {
                                        // Downscale analyzer frame for fast live marker detection
                                        val downscaled = scaleBitmapDown(bitmap, 640)
                                        val scaleFactor = bitmap.width.toFloat() / downscaled.width.toFloat()

                                        val markerResult = viewModel.omrEngine.detectFrame(downscaled)

                                        if (markerResult.isStable && !wasStable.getAndSet(true)) {
                                            HapticManager.performBubbleDetectedTick(context, appSettings.hapticsEnabled)
                                        } else if (!markerResult.isStable) {
                                            wasStable.set(false)
                                        }

                                        // Update UI status
                                        coroutineScope.launch(Dispatchers.Main) {
                                            statusText = markerResult.status
                                            isStableLocked = markerResult.isStable
                                            if (markerResult.found && markerResult.corners != null) {
                                                detectedCorners = markerResult.corners.map { pt ->
                                                    PointF(pt.x * scaleFactor, pt.y * scaleFactor)
                                                }
                                            } else {
                                                detectedCorners = null
                                            }
                                        }

                                        // Auto-capture when markers are stable (Requirement 1: USE REAL HIGH-RESOLUTION CAPTURE)
                                        if (markerResult.isStable && markerResult.corners != null && isCapturing.compareAndSet(false, true)) {
                                            coroutineScope.launch(Dispatchers.Main) {
                                                statusText = "Capturing high-resolution photo..."
                                                isProcessingAutoCapture = true
                                            }

                                            imageCapture.takePicture(
                                                cameraExecutor,
                                                object : ImageCapture.OnImageCapturedCallback() {
                                                    override fun onCaptureSuccess(capturedProxy: ImageProxy) {
                                                        val highResBitmap = imageProxyToBitmap(capturedProxy)
                                                        capturedProxy.close()

                                                        if (highResBitmap == null) {
                                                            coroutineScope.launch(Dispatchers.Main) {
                                                                lastErrorMessage = "Failed to capture high-resolution photo."
                                                                isCapturing.set(false)
                                                                isProcessingAutoCapture = false
                                                                viewModel.omrEngine.resetStability()
                                                            }
                                                            return
                                                        }

                                                        coroutineScope.launch(Dispatchers.Default) {
                                                            withContext(Dispatchers.Main) {
                                                                statusText = "Reading bubbles on high-res photo..."
                                                            }

                                                            val currentTest = test
                                                            val currentQuestions = questions
                                                            if (currentTest != null && currentQuestions.isNotEmpty()) {
                                                                // Detect markers on high-res photo for sub-millimeter precision
                                                                val hrDownscaled = scaleBitmapDown(highResBitmap, 640)
                                                                val hrScale = highResBitmap.width.toFloat() / hrDownscaled.width.toFloat()
                                                                val hrMarkerResult = viewModel.omrEngine.detectFrame(hrDownscaled)

                                                                val targetCorners: List<PointF>? = if (hrMarkerResult.found && hrMarkerResult.corners != null) {
                                                                    hrMarkerResult.corners.map { pt -> PointF(pt.x * hrScale, pt.y * hrScale) }
                                                                } else {
                                                                    // Fallback: translate stable analyzer corners to high-res photo dimensions
                                                                    val scaleX = highResBitmap.width.toFloat() / bitmap.width.toFloat()
                                                                    val scaleY = highResBitmap.height.toFloat() / bitmap.height.toFloat()
                                                                    markerResult.corners.map { pt ->
                                                                        val origX = pt.x * scaleFactor
                                                                        val origY = pt.y * scaleFactor
                                                                        PointF(origX * scaleX, origY * scaleY)
                                                                    }
                                                                }

                                                                val spec = SheetSpec(questionCount = currentTest.questionCount)
                                                                val warped = if (targetCorners != null) {
                                                                    viewModel.omrEngine.perspectiveCorrector.correctPerspective(
                                                                        sourceBitmap = highResBitmap,
                                                                        detectedCorners = targetCorners,
                                                                        spec = spec
                                                                    )
                                                                } else null

                                                                if (warped != null) {
                                                                    val bubbleMap = viewModel.omrEngine.bubbleReader.readBubbles(warped, spec)
                                                                    val detectedAnswers = viewModel.omrEngine.answerDetector.detectAnswers(bubbleMap)
                                                                    val rollNumber = viewModel.omrEngine.extractRollNumber(detectedAnswers)
                                                                    val questionAnswers = detectedAnswers.filter { it.questionNumber > 0 }

                                                                    withContext(Dispatchers.Main) {
                                                                        if (!rollNumber.isNullOrBlank()) {
                                                                            viewModel.studentId.value = rollNumber
                                                                        }
                                                                        viewModel.setScanSuccess(warped, questionAnswers)
                                                                        onNavigateToReview()
                                                                    }
                                                                } else {
                                                                    withContext(Dispatchers.Main) {
                                                                        lastErrorMessage = "Failed to align sheet perspective. Hold camera steady."
                                                                        isCapturing.set(false)
                                                                        isProcessingAutoCapture = false
                                                                        viewModel.omrEngine.resetStability()
                                                                    }
                                                                }
                                                            } else {
                                                                withContext(Dispatchers.Main) {
                                                                    isCapturing.set(false)
                                                                    isProcessingAutoCapture = false
                                                                }
                                                            }
                                                        }
                                                    }

                                                    override fun onError(exception: ImageCaptureException) {
                                                        coroutineScope.launch(Dispatchers.Main) {
                                                            lastErrorMessage = "Photo capture error: ${exception.message}"
                                                            isCapturing.set(false)
                                                            isProcessingAutoCapture = false
                                                            viewModel.omrEngine.resetStability()
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                } catch (e: Throwable) {
                                    Log.e("ScannerScreen", "Analyzer error: ${e.message}")
                                } finally {
                                    isAnalyzing.set(false)
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis,
                                    imageCapture
                                )
                            } catch (exc: Exception) {
                                Log.e("ScannerScreen", "Camera bind failed", exc)
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    }
                )

                // Overlay Guides
                ScannerOverlay(
                    statusText = statusText,
                    isStable = isStableLocked,
                    hasCorners = detectedCorners != null
                )

            } else {
                // Permission Denied View in Liquid Glass
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        fillColor = GlassFill,
                        showSpecular = true
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(GlassFillElevated)
                                    .border(1.dp, GlassBorderBright, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Text(
                                text = stringResource(R.string.camera_permission_required),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.camera_permission_desc),
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LiquidGlassButton(
                                text = stringResource(R.string.grant_camera_permission),
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                variant = GlassButtonVariant.Primary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            LiquidGlassButton(
                                text = stringResource(R.string.pick_photo),
                                onClick = { galleryLauncher.launch("image/*") },
                                variant = GlassButtonVariant.Neutral,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Top Status Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                StatusPill(
                    status = statusText,
                    isStable = isStableLocked,
                    isProcessing = isProcessingAutoCapture
                )
            }

            // Error banner if any
            lastErrorMessage?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 90.dp, start = 16.dp, end = 16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GlassFillElevated),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.border(1.dp, ErrorRedBorder, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WarningAmber)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = err, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { lastErrorMessage = null }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry", tint = ElectricBlue)
                            }
                        }
                    }
                }
            }

            // Bottom Action Bar: Liquid glass controls over live camera feed
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(FrostedBarBackground)
                    .drawBehind {
                        // 1dp specular top border
                        drawLine(
                            color = GlassBorderBright,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fallback to gallery
                    LiquidGlassButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.testTag("gallery_button"),
                        variant = GlassButtonVariant.Neutral,
                        icon = Icons.Default.PhotoLibrary,
                        text = "Gallery"
                    )

                    // Manual High-Res Capture Shutter Button
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(GlassFillElevated)
                            .border(2.dp, if (isStableLocked) SuccessGreenBorder else GlassBorderBright, CircleShape)
                            .clickable {
                                if (!isCapturing.get() && !isProcessingAutoCapture) {
                                    isCapturing.set(true)
                                    isProcessingAutoCapture = true
                                    statusText = "Capturing high-resolution photo..."
                                    imageCapture.takePicture(
                                        cameraExecutor,
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                                val hrBitmap = imageProxyToBitmap(imageProxy)
                                                imageProxy.close()
                                                if (hrBitmap != null) {
                                                    coroutineScope.launch(Dispatchers.Default) {
                                                        val currentTest = test
                                                        val currentQuestions = questions
                                                        if (currentTest != null && currentQuestions.isNotEmpty()) {
                                                            val result = viewModel.omrEngine.processFullSheet(
                                                                sourceBitmap = hrBitmap,
                                                                questionCount = currentTest.questionCount,
                                                                questions = currentQuestions
                                                            )
                                                            withContext(Dispatchers.Main) {
                                                                isProcessingAutoCapture = false
                                                                isCapturing.set(false)
                                                                if (result.success && result.warpedBitmap != null) {
                                                                    if (!result.detectedRollNumber.isNullOrBlank()) {
                                                                        viewModel.studentId.value = result.detectedRollNumber
                                                                    }
                                                                    viewModel.setScanSuccess(result.warpedBitmap, result.detectedAnswers)
                                                                    onNavigateToReview()
                                                                } else {
                                                                    lastErrorMessage = result.errorMessage ?: "Could not detect sheet. Please realign."
                                                                    viewModel.omrEngine.resetStability()
                                                                }
                                                            }
                                                        } else {
                                                            withContext(Dispatchers.Main) {
                                                                isProcessingAutoCapture = false
                                                                isCapturing.set(false)
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    coroutineScope.launch(Dispatchers.Main) {
                                                        isProcessingAutoCapture = false
                                                        isCapturing.set(false)
                                                        lastErrorMessage = "Failed to capture photo"
                                                    }
                                                }
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    isProcessingAutoCapture = false
                                                    isCapturing.set(false)
                                                    lastErrorMessage = "Capture failed: ${exception.message}"
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                            .testTag("manual_capture_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isStableLocked) SuccessGreen else GlassFillElevated)
                                .border(1.dp, GlassBorderBright, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Capture High-Res Photo",
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (BuildConfig.DEBUG) {
                        // Test Sheet (allows instant simulated grading of an on-screen generated sheet for testing!)
                        LiquidGlassButton(
                            onClick = {
                                val currentTest = test ?: return@LiquidGlassButton
                                val currentQuestions = questions
                                coroutineScope.launch(Dispatchers.Default) {
                                    isProcessingAutoCapture = true
                                    val spec = SheetSpec(questionCount = currentTest.questionCount, questions = currentQuestions)
                                    val bmp = OmrSheetGenerator.generateBitmap(
                                        spec = spec,
                                        testName = currentTest.name,
                                        topicName = "General"
                                    )
                                    val result = viewModel.omrEngine.processFullSheet(
                                        sourceBitmap = bmp,
                                        questionCount = currentTest.questionCount,
                                        questions = currentQuestions
                                    )
                                    withContext(Dispatchers.Main) {
                                        isProcessingAutoCapture = false
                                        if (result.success && result.warpedBitmap != null) {
                                            if (!result.detectedRollNumber.isNullOrBlank()) {
                                                viewModel.studentId.value = result.detectedRollNumber
                                            }
                                            viewModel.setScanSuccess(result.warpedBitmap, result.detectedAnswers)
                                            onNavigateToReview()
                                        } else {
                                            lastErrorMessage = result.errorMessage ?: "Test scan failed"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.testTag("test_simulate_button"),
                            variant = GlassButtonVariant.Neutral,
                            text = "Test Sample"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    status: String,
    isStable: Boolean,
    isProcessing: Boolean
) {
    val indicatorColor = when {
        isProcessing -> PurpleAccent
        isStable -> SuccessGreen
        status.contains("blurry", ignoreCase = true) || status.contains("error", ignoreCase = true) -> ErrorRed
        status.contains("steady", ignoreCase = true) -> WarningAmber
        else -> TextSecondary
    }

    val borderColor = when {
        isStable -> SuccessGreenBorder
        status.contains("blurry", ignoreCase = true) || status.contains("error", ignoreCase = true) -> ErrorRedBorder
        status.contains("steady", ignoreCase = true) -> WarningAmberBorder
        else -> GlassBorderBright
    }

    Box(
        modifier = Modifier
            .liquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                fillColor = GlassFillElevated,
                borderColor = borderColor,
                elevation = 6.dp,
                showSpecular = true
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = indicatorColor, strokeWidth = 2.dp)
            } else if (isStable) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = indicatorColor, modifier = Modifier.size(18.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(indicatorColor, CircleShape)
                )
            }

            Text(
                text = status,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun ScannerOverlay(
    statusText: String,
    isStable: Boolean,
    hasCorners: Boolean
) {
    val reticleColor = if (isStable) SuccessGreen else if (hasCorners) WarningAmber else ElectricBlue

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Calculate guide rect (A4 aspect ratio ~ 1:1.33)
        val guideWidth = w * 0.84f
        val guideHeight = guideWidth * 1.35f
        val left = (w - guideWidth) / 2f
        val top = (h - guideHeight) / 2f - 20f
        val right = left + guideWidth
        val bottom = top + guideHeight

        // Draw 4 corner L-bracket alignment markers
        val cornerLen = 35.dp.toPx()
        val strokeW = 4.dp.toPx()

        // TL
        drawLine(reticleColor, Offset(left, top), Offset(left + cornerLen, top), strokeW)
        drawLine(reticleColor, Offset(left, top), Offset(left, top + cornerLen), strokeW)

        // TR
        drawLine(reticleColor, Offset(right, top), Offset(right - cornerLen, top), strokeW)
        drawLine(reticleColor, Offset(right, top), Offset(right, top + cornerLen), strokeW)

        // BL
        drawLine(reticleColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeW)
        drawLine(reticleColor, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeW)

        // BR
        drawLine(reticleColor, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeW)
        drawLine(reticleColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeW)

        // Guide border
        drawRect(
            color = reticleColor.copy(alpha = 0.25f),
            topLeft = Offset(left, top),
            size = Size(guideWidth, guideHeight),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )
        )
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    val bitmap = image.toBitmap()
    val rotation = image.imageInfo.rotationDegrees
    return if (rotation != 0) {
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

private fun scaleBitmapDown(bitmap: Bitmap, maxDim: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= maxDim && height <= maxDim) return bitmap

    val scale = if (width > height) {
        maxDim.toFloat() / width.toFloat()
    } else {
        maxDim.toFloat() / height.toFloat()
    }

    return Bitmap.createScaledBitmap(
        bitmap,
        (width * scale).toInt(),
        (height * scale).toInt(),
        true
    )
}

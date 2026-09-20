package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.backup.BackupImportStats
import com.example.data.backup.DataBackupManager
import com.example.data.entity.AppSettings
import com.example.data.entity.DetectedAnswer
import com.example.data.entity.GradedTestRecord
import com.example.data.entity.Question
import com.example.data.entity.ScanResult
import com.example.data.entity.TestEntity
import com.example.data.entity.Topic
import com.example.data.repository.GradedTestRepository
import com.example.omr.OmrEngine
import com.example.omr.processing.AnswerDetector
import com.example.omr.processing.DetectedQuestionAnswer
import com.example.omr.processing.ScoringEngine
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MarklifyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val topicDao = db.topicDao()
    private val testDao = db.testDao()
    private val questionDao = db.questionDao()
    private val scanResultDao = db.scanResultDao()
    private val detectedAnswerDao = db.detectedAnswerDao()
    private val settingsDao = db.settingsDao()

    val gradedTestRepository = GradedTestRepository(scanResultDao, detectedAnswerDao)

    val omrEngine = OmrEngine()
    private val scoringEngine = ScoringEngine()

    // Settings
    val appSettings: StateFlow<AppSettings> = settingsDao.getSettingsFlow()
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    // Counts for Dashboard
    val topicCount: StateFlow<Int> = topicDao.getTopicCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val testCount: StateFlow<Int> = testDao.getTestCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val scanCount: StateFlow<Int> = gradedTestRepository.totalGradedTestCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Lists
    val topics: StateFlow<List<Topic>> = topicDao.getAllTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTests: StateFlow<List<TestEntity>> = testDao.getAllTests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGradedTests: StateFlow<List<ScanResult>> = gradedTestRepository.allGradedTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGradedTestRecords: StateFlow<List<GradedTestRecord>> = gradedTestRepository.allGradedTestRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentScanResults: StateFlow<List<ScanResult>> = gradedTestRepository.getRecentGradedTests(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active session for Scanning & Review
    private val _selectedTest = MutableStateFlow<TestEntity?>(null)
    val selectedTest: StateFlow<TestEntity?> = _selectedTest.asStateFlow()

    private val _selectedQuestions = MutableStateFlow<List<Question>>(emptyList())
    val selectedQuestions: StateFlow<List<Question>> = _selectedQuestions.asStateFlow()

    private val _scannedBitmap = MutableStateFlow<Bitmap?>(null)
    val scannedBitmap: StateFlow<Bitmap?> = _scannedBitmap.asStateFlow()

    private val _detectedAnswersList = MutableStateFlow<List<DetectedQuestionAnswer>>(emptyList())
    val detectedAnswersList: StateFlow<List<DetectedQuestionAnswer>> = _detectedAnswersList.asStateFlow()

    // Map of overridden/confirmed answers in Review Screen (questionNumber -> "A", "B", "C", "D", "unanswered")
    private val _reviewAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val reviewAnswers: StateFlow<Map<Int, String>> = _reviewAnswers.asStateFlow()

    val studentName = MutableStateFlow("")
    val studentId = MutableStateFlow("")

    init {
        // Observe settings changes and update AnswerDetector thresholds
        viewModelScope.launch {
            appSettings.collect { settings ->
                omrEngine.answerDetector.updateThresholds(
                    fill = settings.fillThreshold,
                    unanswered = settings.unansweredThreshold,
                    multipleDiff = settings.multipleDiffMargin,
                    ambiguousDiff = settings.ambiguousDiffMargin
                )
            }
        }

        // Seed default demo topic and test if database is empty
        viewModelScope.launch(Dispatchers.IO) {
            seedDemoDataIfEmpty()
        }
    }

    private suspend fun seedDemoDataIfEmpty() {
        val count = testDao.getTestById(1)
        if (count == null) {
            val topicId = topicDao.insertTopic(
                Topic(name = "General Science", createdAt = System.currentTimeMillis())
            )
            val testId = testDao.insertTest(
                TestEntity(
                    topicId = topicId,
                    name = "Physics & Chemistry Quiz",
                    questionCount = 10,
                    createdAt = System.currentTimeMillis()
                )
            )
            val defaultKey = listOf("A", "B", "C", "D", "A", "B", "C", "D", "B", "C")
            val questions = (1..10).map { qNum ->
                Question(
                    testId = testId,
                    questionNumber = qNum,
                    questionText = "Question #$qNum: Sample concept explanation question.",
                    optionA = "Concept Alpha",
                    optionB = "Concept Beta",
                    optionC = "Concept Gamma",
                    optionD = "Concept Delta",
                    correctAnswer = defaultKey[qNum - 1]
                )
            }
            questionDao.insertQuestions(questions)
        }
    }

    // Settings actions
    fun saveThresholdSettings(
        fillThreshold: Float,
        unansweredThreshold: Float,
        multipleDiffMargin: Float,
        ambiguousDiffMargin: Float,
        defaultPageSize: String = appSettings.value.defaultPageSize
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = settingsDao.getSettings() ?: AppSettings()
            val updated = current.copy(
                fillThreshold = fillThreshold,
                unansweredThreshold = unansweredThreshold,
                multipleDiffMargin = multipleDiffMargin,
                ambiguousDiffMargin = ambiguousDiffMargin,
                defaultPageSize = defaultPageSize
            )
            settingsDao.saveSettings(updated)
            omrEngine.answerDetector.updateThresholds(
                fill = fillThreshold,
                unanswered = unansweredThreshold,
                multipleDiff = multipleDiffMargin,
                ambiguousDiff = ambiguousDiffMargin
            )
        }
    }

    fun setAppLanguage(language: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = settingsDao.getSettings() ?: AppSettings()
            settingsDao.saveSettings(current.copy(appLanguage = language))
        }
    }

    fun setThemeMode(themeMode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = settingsDao.getSettings() ?: AppSettings()
            settingsDao.saveSettings(current.copy(themeMode = themeMode))
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = settingsDao.getSettings() ?: AppSettings()
            settingsDao.saveSettings(current.copy(hapticsEnabled = enabled))
        }
    }

    fun resetThresholdsToDefault() {
        val current = appSettings.value
        saveThresholdSettings(
            fillThreshold = AnswerDetector.DEFAULT_FILL_THRESHOLD,
            unansweredThreshold = AnswerDetector.DEFAULT_UNANSWERED_THRESHOLD,
            multipleDiffMargin = AnswerDetector.DEFAULT_MULTIPLE_DIFF_MARGIN,
            ambiguousDiffMargin = AnswerDetector.DEFAULT_AMBIGUOUS_DIFF_MARGIN,
            defaultPageSize = current.defaultPageSize
        )
    }

    // Topic actions
    fun createTopic(name: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = topicDao.insertTopic(Topic(name = name.trim()))
            withContext(Dispatchers.Main) { onCreated(id) }
        }
    }

    fun renameTopic(topicId: Long, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = topicDao.getTopicById(topicId) ?: return@launch
            topicDao.updateTopic(existing.copy(name = newName.trim()))
        }
    }

    fun deleteTopic(topic: Topic) {
        viewModelScope.launch(Dispatchers.IO) {
            topicDao.deleteTopic(topic)
        }
    }

    // Test actions
    fun getTestsForTopic(topicId: Long): Flow<List<TestEntity>> = testDao.getTestsByTopic(topicId)

    suspend fun getTestById(testId: Long): TestEntity? = withContext(Dispatchers.IO) {
        testDao.getTestById(testId)
    }

    suspend fun getTopicById(topicId: Long): Topic? = withContext(Dispatchers.IO) {
        topicDao.getTopicById(topicId)
    }

    fun createTest(
        topicId: Long,
        name: String,
        questionCount: Int,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = questionCount.coerceIn(1, 300)
            val testId = testDao.insertTest(
                TestEntity(
                    topicId = topicId,
                    name = name.trim(),
                    questionCount = count,
                    createdAt = System.currentTimeMillis()
                )
            )
            val defaultLetters = listOf("A", "B", "C", "D")
            val questions = (1..count).map { q ->
                Question(
                    testId = testId,
                    questionNumber = q,
                    questionText = "Question $q",
                    optionA = "Option A",
                    optionB = "Option B",
                    optionC = "Option C",
                    optionD = "Option D",
                    correctAnswer = defaultLetters[(q - 1) % 4]
                )
            }
            questionDao.insertQuestions(questions)
            withContext(Dispatchers.Main) {
                onCreated(testId)
            }
        }
    }

    fun renameTest(testId: Long, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = testDao.getTestById(testId) ?: return@launch
            val updated = existing.copy(name = newName.trim())
            testDao.updateTest(updated)
            if (_selectedTest.value?.id == testId) {
                _selectedTest.value = updated
            }
        }
    }

    fun deleteTest(test: TestEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            testDao.deleteTest(test)
        }
    }

    // Questions actions
    fun getQuestionsForTest(testId: Long): Flow<List<Question>> = questionDao.getQuestionsByTest(testId)

    suspend fun getQuestionsListForTest(testId: Long): List<Question> = withContext(Dispatchers.IO) {
        questionDao.getQuestionsListByTest(testId)
    }

    fun updateQuestion(question: Question) {
        viewModelScope.launch(Dispatchers.IO) {
            questionDao.updateQuestion(question)
        }
    }

    fun updateQuestionsBatch(questions: List<Question>) {
        viewModelScope.launch(Dispatchers.IO) {
            for (q in questions) {
                questionDao.updateQuestion(q)
            }
        }
    }

    fun setTestQuestionCount(testId: Long, newCount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentTest = testDao.getTestById(testId) ?: return@launch
            val validCount = newCount.coerceIn(1, 300)
            testDao.updateTest(currentTest.copy(questionCount = validCount))

            val existing = questionDao.getQuestionsListByTest(testId)
            if (existing.size < validCount) {
                val toAdd = ((existing.size + 1)..validCount).map { q ->
                    val defaultLetters = listOf("A", "B", "C", "D")
                    Question(
                        testId = testId,
                        questionNumber = q,
                        questionText = "Question $q",
                        optionA = "Option A",
                        optionB = "Option B",
                        optionC = "Option C",
                        optionD = "Option D",
                        correctAnswer = defaultLetters[(q - 1) % 4]
                    )
                }
                questionDao.insertQuestions(toAdd)
            } else if (existing.size > validCount) {
                for (q in existing) {
                    if (q.questionNumber > validCount) {
                        questionDao.deleteQuestion(q)
                    }
                }
            }
        }
    }

    // Scanning & Review workflow
    fun loadTestForScan(testId: Long, onLoaded: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val test = testDao.getTestById(testId)
            val questions = questionDao.getQuestionsListByTest(testId)
            _selectedTest.value = test
            _selectedQuestions.value = questions
            _scannedBitmap.value = null
            _detectedAnswersList.value = emptyList()
            _reviewAnswers.value = emptyMap()
            studentName.value = "Student ${System.currentTimeMillis() % 1000}"
            studentId.value = "ID-${(1000..9999).random()}"
            withContext(Dispatchers.Main) {
                onLoaded()
            }
        }
    }

    fun setScanSuccess(warpedBitmap: Bitmap, detectedAnswers: List<DetectedQuestionAnswer>) {
        _scannedBitmap.value = warpedBitmap
        _detectedAnswersList.value = detectedAnswers
        val initialReview = detectedAnswers.associate {
            it.questionNumber to it.detectedAnswer
        }
        _reviewAnswers.value = initialReview
    }

    fun updateReviewAnswer(questionNumber: Int, overrideAnswer: String) {
        val current = _reviewAnswers.value.toMutableMap()
        current[questionNumber] = overrideAnswer
        _reviewAnswers.value = current
    }

    fun confirmAndScore(onScoreCompleted: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val test = _selectedTest.value ?: return@launch
            val questions = _selectedQuestions.value
            val answers = _reviewAnswers.value
            val bitmap = _scannedBitmap.value

            val evaluation = scoringEngine.score(answers, questions)

            var savedImagePath: String? = null
            if (bitmap != null) {
                try {
                    val imagesDir = File(getApplication<Application>().filesDir, "scan_images").apply { mkdirs() }
                    val imageFile = File(imagesDir, "scan_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(imageFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    savedImagePath = imageFile.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val scanResult = ScanResult(
                testId = test.id,
                studentName = studentName.value.ifBlank { "Anonymous Student" },
                studentId = studentId.value.ifBlank { "N/A" },
                scanTime = System.currentTimeMillis(),
                totalQuestions = evaluation.totalQuestions,
                correct = evaluation.correct,
                wrong = evaluation.wrong,
                unanswered = evaluation.unanswered,
                multipleMarked = evaluation.multipleMarked,
                ambiguous = evaluation.ambiguous,
                percentage = evaluation.percentage,
                finalScore = evaluation.correct.toFloat(),
                imagePath = savedImagePath
            )

            val detectedEntities = evaluation.evaluatedAnswers.map { item ->
                DetectedAnswer(
                    scanResultId = 0,
                    questionNumber = item.questionNumber,
                    detectedAnswer = item.detectedAnswer,
                    isCorrect = item.isCorrect
                )
            }

            val scanResultId = gradedTestRepository.saveGradedTestWithAnswers(scanResult, detectedEntities)

            withContext(Dispatchers.Main) {
                onScoreCompleted(scanResultId)
            }
        }
    }

    // Results & Graded Test History actions
    fun getScanResultsForTest(testId: Long): Flow<List<ScanResult>> = gradedTestRepository.getGradedTestsForTest(testId)

    fun getGradedTestRecordsForTest(testId: Long): Flow<List<GradedTestRecord>> =
        gradedTestRepository.getGradedTestRecordsForTest(testId)

    fun searchGradedTestRecords(query: String): Flow<List<GradedTestRecord>> =
        gradedTestRepository.searchGradedTestRecords(query)

    suspend fun getScanResultById(id: Long): ScanResult? = gradedTestRepository.getGradedTestById(id)

    fun getDetectedAnswersForScan(scanResultId: Long): Flow<List<DetectedAnswer>> =
        gradedTestRepository.getAnswersFlowByScanResult(scanResultId)

    fun deleteScanResult(scanResult: ScanResult) {
        viewModelScope.launch(Dispatchers.IO) {
            gradedTestRepository.deleteGradedTest(scanResult)
            scanResult.imagePath?.let { path ->
                try {
                    File(path).delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // CSV Exports
    fun exportTestResultsCsv(context: Context, testId: Long, onReady: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val test = testDao.getTestById(testId)
            val testName = test?.name ?: "Test_$testId"
            val results = gradedTestRepository.getGradedTestsListByTest(testId)
            val csvFile = DataBackupManager.createCsvResultsFile(context, testName, results)
            withContext(Dispatchers.Main) {
                onReady(csvFile)
            }
        }
    }

    fun exportAllGradedTestsCsv(context: Context, onReady: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val records = allGradedTestRecords.value
            val csvFile = DataBackupManager.createAllGradedTestsCsvFile(context, records)
            withContext(Dispatchers.Main) {
                onReady(csvFile)
            }
        }
    }

    // Database Backup & Restore
    fun exportDatabaseBackup(context: Context, onReady: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val allTopics = topicDao.getAllTopicsList()
            val tests = testDao.getAllTestsList()
            val questions = questionDao.getAllQuestionsList()
            val scans = scanResultDao.getAllScanResultsList()
            val detected = detectedAnswerDao.getAllAnswersList()

            val backupFile = DataBackupManager.createBackupJsonFile(
                context = context,
                topics = allTopics,
                tests = tests,
                questions = questions,
                scanResults = scans,
                detectedAnswers = detected
            )

            withContext(Dispatchers.Main) {
                onReady(backupFile)
            }
        }
    }

    fun importDatabaseBackup(
        jsonString: String,
        onError: (String) -> Unit = {},
        onComplete: (BackupImportStats) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = DataBackupManager.parseBackupJson(jsonString)
                db.withTransaction {
                    for (topic in data.topics) {
                        topicDao.insertTopic(topic)
                    }
                    for (test in data.tests) {
                        testDao.insertTest(test)
                    }
                    if (data.questions.isNotEmpty()) {
                        questionDao.insertQuestions(data.questions)
                    }
                    for (scan in data.scanResults) {
                        scanResultDao.insertScanResult(scan)
                    }
                    if (data.detectedAnswers.isNotEmpty()) {
                        detectedAnswerDao.insertAnswers(data.detectedAnswers)
                    }
                }

                val stats = BackupImportStats(
                    topicCount = data.topics.size,
                    testCount = data.tests.size,
                    questionCount = data.questions.size,
                    scanCount = data.scanResults.size
                )

                withContext(Dispatchers.Main) {
                    onComplete(stats)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Failed to restore backup")
                }
            }
        }
    }
}

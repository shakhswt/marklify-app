package com.example.data.repository

import com.example.data.dao.DetectedAnswerDao
import com.example.data.dao.ScanResultDao
import com.example.data.entity.DetectedAnswer
import com.example.data.entity.GradedTestRecord
import com.example.data.entity.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository adhering to modern Android architecture principles,
 * encapsulating local Room database access for graded test history,
 * student names, final scores, and detailed item evaluations.
 */
class GradedTestRepository(
    private val scanResultDao: ScanResultDao,
    private val detectedAnswerDao: DetectedAnswerDao
) {
    /**
     * Observes the complete history of all graded tests.
     */
    val allGradedTests: Flow<List<ScanResult>> = scanResultDao.getAllScanResults()

    /**
     * Observes the complete history of graded tests joined with test names for display.
     */
    val allGradedTestRecords: Flow<List<GradedTestRecord>> = scanResultDao.getAllGradedTestRecords()

    /**
     * Observes the total number of graded tests in history.
     */
    val totalGradedTestCount: Flow<Int> = scanResultDao.getScanCount()

    /**
     * Observes the most recently graded tests up to the given limit.
     */
    fun getRecentGradedTests(limit: Int = 10): Flow<List<ScanResult>> =
        scanResultDao.getRecentScanResults(limit)

    /**
     * Observes graded test history for a specific test.
     */
    fun getGradedTestsForTest(testId: Long): Flow<List<ScanResult>> =
        scanResultDao.getScanResultsByTest(testId)

    /**
     * Observes graded test history records with test metadata for a specific test.
     */
    fun getGradedTestRecordsForTest(testId: Long): Flow<List<GradedTestRecord>> =
        scanResultDao.getGradedTestRecordsByTest(testId)

    /**
     * Searches graded test history by student name or student ID.
     */
    fun searchGradedTestRecords(query: String): Flow<List<GradedTestRecord>> =
        scanResultDao.searchGradedTestRecords(query)

    /**
     * Observes a single graded test result by ID.
     */
    fun getGradedTestFlowById(id: Long): Flow<ScanResult?> =
        scanResultDao.getScanResultFlowById(id)

    /**
     * Observes the average final score for a specific test.
     */
    fun getAverageScoreForTest(testId: Long): Flow<Float?> =
        scanResultDao.getAverageScoreForTest(testId)

    /**
     * Observes the highest final score achieved for a specific test.
     */
    fun getHighestScoreForTest(testId: Long): Flow<Float?> =
        scanResultDao.getHighestScoreForTest(testId)

    /**
     * Observes detailed detected answers for a specific graded sheet.
     */
    fun getAnswersFlowByScanResult(scanResultId: Long): Flow<List<DetectedAnswer>> =
        detectedAnswerDao.getAnswersByScanResult(scanResultId)

    // Suspend Database Operations

    suspend fun getGradedTestById(id: Long): ScanResult? = withContext(Dispatchers.IO) {
        scanResultDao.getScanResultById(id)
    }

    suspend fun getGradedTestsListByTest(testId: Long): List<ScanResult> = withContext(Dispatchers.IO) {
        scanResultDao.getScanResultsListByTest(testId)
    }

    suspend fun getAllGradedTestsList(): List<ScanResult> = withContext(Dispatchers.IO) {
        scanResultDao.getAllScanResultsList()
    }

    suspend fun getAnswersListByScanResult(scanResultId: Long): List<DetectedAnswer> = withContext(Dispatchers.IO) {
        detectedAnswerDao.getAnswersListByScanResult(scanResultId)
    }

    /**
     * Stores a graded test result in the local Room database and returns its assigned ID.
     */
    suspend fun saveGradedTest(scanResult: ScanResult): Long = withContext(Dispatchers.IO) {
        scanResultDao.insertScanResult(scanResult)
    }

    /**
     * Atomically stores a graded test along with its detailed detected answers.
     */
    suspend fun saveGradedTestWithAnswers(
        scanResult: ScanResult,
        answers: List<DetectedAnswer>
    ): Long = withContext(Dispatchers.IO) {
        val resultId = scanResultDao.insertScanResult(scanResult)
        if (answers.isNotEmpty()) {
            val assignedAnswers = answers.map { it.copy(scanResultId = resultId) }
            detectedAnswerDao.insertAnswers(assignedAnswers)
        }
        resultId
    }

    /**
     * Updates an existing graded test result.
     */
    suspend fun updateGradedTest(scanResult: ScanResult) = withContext(Dispatchers.IO) {
        scanResultDao.updateScanResult(scanResult)
    }

    /**
     * Deletes a graded test result from history.
     */
    suspend fun deleteGradedTest(scanResult: ScanResult) = withContext(Dispatchers.IO) {
        scanResultDao.deleteScanResult(scanResult)
    }

    /**
     * Deletes a graded test result by ID.
     */
    suspend fun deleteGradedTestById(id: Long) = withContext(Dispatchers.IO) {
        scanResultDao.deleteScanResultById(id)
    }

    /**
     * Clears all graded test history for a specific test.
     */
    suspend fun deleteGradedTestsForTest(testId: Long) = withContext(Dispatchers.IO) {
        scanResultDao.deleteScanResultsForTest(testId)
    }
}

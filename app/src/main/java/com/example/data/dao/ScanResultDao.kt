package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.GradedTestRecord
import com.example.data.entity.ScanResult
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {
    @Query("SELECT * FROM scan_results ORDER BY scanTime DESC")
    fun getAllScanResults(): Flow<List<ScanResult>>

    @Query("""
        SELECT 
            s.id,
            s.testId,
            COALESCE(t.name, 'Test #' || s.testId) AS testName,
            s.studentName,
            s.studentId,
            s.scanTime,
            s.totalQuestions,
            s.correct,
            s.wrong,
            s.unanswered,
            s.multipleMarked,
            s.ambiguous,
            s.percentage,
            s.finalScore,
            s.imagePath
        FROM scan_results s
        LEFT JOIN tests t ON s.testId = t.id
        ORDER BY s.scanTime DESC
    """)
    fun getAllGradedTestRecords(): Flow<List<GradedTestRecord>>

    @Query("""
        SELECT 
            s.id,
            s.testId,
            COALESCE(t.name, 'Test #' || s.testId) AS testName,
            s.studentName,
            s.studentId,
            s.scanTime,
            s.totalQuestions,
            s.correct,
            s.wrong,
            s.unanswered,
            s.multipleMarked,
            s.ambiguous,
            s.percentage,
            s.finalScore,
            s.imagePath
        FROM scan_results s
        LEFT JOIN tests t ON s.testId = t.id
        WHERE s.studentName LIKE '%' || :query || '%' OR s.studentId LIKE '%' || :query || '%'
        ORDER BY s.scanTime DESC
    """)
    fun searchGradedTestRecords(query: String): Flow<List<GradedTestRecord>>

    @Query("""
        SELECT 
            s.id,
            s.testId,
            COALESCE(t.name, 'Test #' || s.testId) AS testName,
            s.studentName,
            s.studentId,
            s.scanTime,
            s.totalQuestions,
            s.correct,
            s.wrong,
            s.unanswered,
            s.multipleMarked,
            s.ambiguous,
            s.percentage,
            s.finalScore,
            s.imagePath
        FROM scan_results s
        LEFT JOIN tests t ON s.testId = t.id
        WHERE s.testId = :testId
        ORDER BY s.scanTime DESC
    """)
    fun getGradedTestRecordsByTest(testId: Long): Flow<List<GradedTestRecord>>

    @Query("SELECT * FROM scan_results WHERE testId = :testId ORDER BY scanTime DESC")
    fun getScanResultsByTest(testId: Long): Flow<List<ScanResult>>

    @Query("SELECT * FROM scan_results WHERE testId = :testId ORDER BY scanTime DESC")
    suspend fun getScanResultsListByTest(testId: Long): List<ScanResult>

    @Query("SELECT * FROM scan_results ORDER BY scanTime DESC")
    suspend fun getAllScanResultsList(): List<ScanResult>

    @Query("SELECT * FROM scan_results WHERE id = :id")
    suspend fun getScanResultById(id: Long): ScanResult?

    @Query("SELECT * FROM scan_results WHERE id = :id")
    fun getScanResultFlowById(id: Long): Flow<ScanResult?>

    @Query("SELECT COUNT(*) FROM scan_results")
    fun getScanCount(): Flow<Int>

    @Query("SELECT * FROM scan_results ORDER BY scanTime DESC LIMIT :limit")
    fun getRecentScanResults(limit: Int = 5): Flow<List<ScanResult>>

    @Query("SELECT AVG(finalScore) FROM scan_results WHERE testId = :testId")
    fun getAverageScoreForTest(testId: Long): Flow<Float?>

    @Query("SELECT MAX(finalScore) FROM scan_results WHERE testId = :testId")
    fun getHighestScoreForTest(testId: Long): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanResult(scanResult: ScanResult): Long

    @Update
    suspend fun updateScanResult(scanResult: ScanResult)

    @Delete
    suspend fun deleteScanResult(scanResult: ScanResult)

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun deleteScanResultById(id: Long)

    @Query("DELETE FROM scan_results WHERE testId = :testId")
    suspend fun deleteScanResultsForTest(testId: Long)
}

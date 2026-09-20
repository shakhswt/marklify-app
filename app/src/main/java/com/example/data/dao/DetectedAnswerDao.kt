package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.DetectedAnswer
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectedAnswerDao {
    @Query("SELECT * FROM detected_answers WHERE scanResultId = :scanResultId ORDER BY questionNumber ASC")
    fun getAnswersByScanResult(scanResultId: Long): Flow<List<DetectedAnswer>>

    @Query("SELECT * FROM detected_answers WHERE scanResultId = :scanResultId ORDER BY questionNumber ASC")
    suspend fun getAnswersListByScanResult(scanResultId: Long): List<DetectedAnswer>

    @Query("SELECT * FROM detected_answers ORDER BY scanResultId ASC, questionNumber ASC")
    suspend fun getAllAnswersList(): List<DetectedAnswer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<DetectedAnswer>): List<Long>

    @Query("DELETE FROM detected_answers WHERE scanResultId = :scanResultId")
    suspend fun deleteAnswersByScanResult(scanResultId: Long)
}

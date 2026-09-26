package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE testId = :testId ORDER BY questionNumber ASC")
    fun getQuestionsByTest(testId: Long): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE testId = :testId ORDER BY questionNumber ASC")
    suspend fun getQuestionsListByTest(testId: Long): List<Question>

    @Query("SELECT * FROM questions ORDER BY testId ASC, questionNumber ASC")
    suspend fun getAllQuestionsList(): List<Question>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>): List<Long>

    @Update
    suspend fun updateQuestion(question: Question)

    @Update
    suspend fun updateQuestions(questions: List<Question>)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Delete
    suspend fun deleteQuestions(questions: List<Question>)

    @Query("DELETE FROM questions WHERE testId = :testId")
    suspend fun deleteQuestionsByTest(testId: Long)
}

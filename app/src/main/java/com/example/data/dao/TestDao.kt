package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.TestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {
    @Query("SELECT * FROM tests WHERE topicId = :topicId ORDER BY createdAt DESC")
    fun getTestsByTopic(topicId: Long): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests ORDER BY createdAt DESC")
    fun getAllTests(): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests ORDER BY createdAt DESC")
    suspend fun getAllTestsList(): List<TestEntity>

    @Query("SELECT * FROM tests WHERE id = :id")
    suspend fun getTestById(id: Long): TestEntity?

    @Query("SELECT COUNT(*) FROM tests")
    fun getTestCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity): Long

    @Update
    suspend fun updateTest(test: TestEntity)

    @Delete
    suspend fun deleteTest(test: TestEntity)

    @Query("DELETE FROM tests WHERE id = :id")
    suspend fun deleteTestById(id: Long)
}

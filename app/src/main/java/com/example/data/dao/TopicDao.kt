package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.Topic
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY createdAt DESC")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics ORDER BY createdAt DESC")
    suspend fun getAllTopicsList(): List<Topic>

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun getTopicById(id: Long): Topic?

    @Query("SELECT COUNT(*) FROM topics")
    fun getTopicCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic): Long

    @Update
    suspend fun updateTopic(topic: Topic)

    @Delete
    suspend fun deleteTopic(topic: Topic)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteTopicById(id: Long)
}

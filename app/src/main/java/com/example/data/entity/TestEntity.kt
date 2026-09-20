package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tests",
    foreignKeys = [
        ForeignKey(
            entity = Topic::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId")]
)
data class TestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topicId: Long,
    val name: String,
    val questionCount: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val examType: String = "NEET",
    val isPublic: Boolean = false,
    val isArchived: Boolean = false,
    val examDate: Long = System.currentTimeMillis(),
    val description: String = ""
)

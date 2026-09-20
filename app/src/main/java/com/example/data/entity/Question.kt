package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = TestEntity::class,
            parentColumns = ["id"],
            childColumns = ["testId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("testId")]
)
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testId: Long,
    val questionNumber: Int,
    val questionText: String = "",
    val optionA: String = "Option A",
    val optionB: String = "Option B",
    val optionC: String = "Option C",
    val optionD: String = "Option D",
    val correctAnswer: String = "A" // "A", "B", "C", "D"
)

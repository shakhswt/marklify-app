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
    val correctAnswer: String = "A", // "A", "B", "C", "D", "E", "True", "False", "42", etc.
    val sectionName: String = "Section1",
    val questionType: String = "MCQ4", // "MCQ4", "MCQ5", "TRUE_FALSE", "NUMERICAL", "MATRIX"
    val correctMarks: Float = 1.0f,
    val negativeMarks: Float = 0.0f,
    val allowPartial: Boolean = false,
    val allowOptional: Boolean = false,
    val numDigits: Int = 1,
    val hasNegativeSign: Boolean = false,
    val hasDecimal: Boolean = false
)

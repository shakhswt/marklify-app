package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "detected_answers",
    foreignKeys = [
        ForeignKey(
            entity = ScanResult::class,
            parentColumns = ["id"],
            childColumns = ["scanResultId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scanResultId")]
)
data class DetectedAnswer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scanResultId: Long,
    val questionNumber: Int,
    val detectedAnswer: String, // "A", "B", "C", "D", "unanswered", "multiple", "ambiguous"
    val isCorrect: Boolean
)

package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scan_results",
    foreignKeys = [
        ForeignKey(
            entity = TestEntity::class,
            parentColumns = ["id"],
            childColumns = ["testId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("testId"),
        Index("studentName"),
        Index("scanTime")
    ]
)
data class ScanResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testId: Long,
    val studentName: String,
    val studentId: String,
    val scanTime: Long = System.currentTimeMillis(),
    val totalQuestions: Int,
    val correct: Int,
    val wrong: Int,
    val unanswered: Int,
    val multipleMarked: Int = 0,
    val ambiguous: Int = 0,
    val percentage: Float,
    val finalScore: Float = correct.toFloat(),
    val imagePath: String? = null
)

package com.example.data.entity

/**
 * Data projection combining a graded test scan with its associated test name,
 * providing a complete record for history display including student name,
 * student ID, final score, and detailed score breakdown.
 */
data class GradedTestRecord(
    val id: Long,
    val testId: Long,
    val testName: String,
    val studentName: String,
    val studentId: String,
    val scanTime: Long,
    val totalQuestions: Int,
    val correct: Int,
    val wrong: Int,
    val unanswered: Int,
    val multipleMarked: Int = 0,
    val ambiguous: Int = 0,
    val percentage: Float,
    val finalScore: Float,
    val imagePath: String? = null
)

package com.example.omr.processing

import com.example.data.entity.Question

data class ScoringEvaluation(
    val totalQuestions: Int,
    val correct: Int,
    val wrong: Int,
    val unanswered: Int,
    val multipleMarked: Int,
    val ambiguous: Int,
    val percentage: Float,
    val evaluatedAnswers: List<EvaluatedAnswerItem>
)

data class EvaluatedAnswerItem(
    val questionNumber: Int,
    val detectedAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean
)

class ScoringEngine {
    /**
     * Scores the detected answers against the test questions.
     * Scoring Rules:
     * Correct = 1 point
     * Wrong = 0
     * Unanswered = 0
     * Multiple marked = 0 (tracked separately)
     * Ambiguous = 0
     * No negative marking.
     */
    fun score(
        detectedAnswers: Map<Int, String>, // questionNumber -> answer ("A", "B", "C", "D", "unanswered", "multiple", "ambiguous")
        questions: List<Question>
    ): ScoringEvaluation {
        val totalQuestions = questions.size
        var correct = 0
        var wrong = 0
        var unanswered = 0
        var multipleMarked = 0
        var ambiguous = 0

        val evaluatedAnswers = mutableListOf<EvaluatedAnswerItem>()

        for (q in questions.sortedBy { it.questionNumber }) {
            val rawAnswer = detectedAnswers[q.questionNumber]?.trim() ?: "unanswered"
            val correctAnswer = q.correctAnswer.trim().uppercase()

            val isCorrect: Boolean
            when (rawAnswer.lowercase()) {
                "unanswered" -> {
                    unanswered++
                    isCorrect = false
                }
                "multiple" -> {
                    multipleMarked++
                    isCorrect = false
                }
                "ambiguous" -> {
                    ambiguous++
                    isCorrect = false
                }
                else -> {
                    val normalizedAnswer = rawAnswer.uppercase()
                    if (normalizedAnswer == correctAnswer) {
                        correct++
                        isCorrect = true
                    } else {
                        wrong++
                        isCorrect = false
                    }
                }
            }

            evaluatedAnswers.add(
                EvaluatedAnswerItem(
                    questionNumber = q.questionNumber,
                    detectedAnswer = rawAnswer,
                    correctAnswer = correctAnswer,
                    isCorrect = isCorrect
                )
            )
        }

        val percentage = if (totalQuestions > 0) {
            (correct.toFloat() / totalQuestions.toFloat()) * 100f
        } else 0f

        return ScoringEvaluation(
            totalQuestions = totalQuestions,
            correct = correct,
            wrong = wrong,
            unanswered = unanswered,
            multipleMarked = multipleMarked,
            ambiguous = ambiguous,
            percentage = percentage,
            evaluatedAnswers = evaluatedAnswers
        )
    }
}

package com.example.omr.processing

import com.example.data.entity.Question

data class SectionResult(
    val sectionName: String,
    val marks: Float,
    val maxMarks: Float,
    val percentage: Float,
    val correct: Int,
    val wrong: Int,
    val unanswered: Int
)

data class ScoringEvaluation(
    val totalQuestions: Int,
    val correct: Int,
    val wrong: Int,
    val unanswered: Int,
    val multipleMarked: Int,
    val ambiguous: Int,
    val percentage: Float,
    val totalScore: Float,
    val maxPossibleScore: Float,
    val evaluatedAnswers: List<EvaluatedAnswerItem>,
    val sectionBreakdown: List<SectionResult> = emptyList()
)

data class EvaluatedAnswerItem(
    val questionNumber: Int,
    val detectedAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val marksAwarded: Float = 0f
)

class ScoringEngine {
    /**
     * Scores detected answers against questions and an optional custom answer key map.
     * Respects per-section/per-question correctMarks, negativeMarks, and allowOptionalAttempts.
     */
    fun score(
        detectedAnswers: Map<Int, String>, // questionNumber -> answer
        questions: List<Question>,
        answerKeyOverride: Map<Int, String> = emptyMap()
    ): ScoringEvaluation {
        val totalQuestions = questions.size
        var correctCount = 0
        var wrongCount = 0
        var unansweredCount = 0
        var multipleMarkedCount = 0
        var ambiguousCount = 0

        var totalScore = 0f
        var maxPossibleScore = 0f

        val evaluatedAnswers = mutableListOf<EvaluatedAnswerItem>()
        val sectionMap = mutableMapOf<String, MutableList<EvaluatedAnswerItem>>()
        val sectionQuestionsMap = questions.groupBy { it.sectionName.ifBlank { "Section1" } }

        for (q in questions.sortedBy { it.questionNumber }) {
            val rawAnswer = detectedAnswers[q.questionNumber]?.trim() ?: "unanswered"
            val correctAnswer = (answerKeyOverride[q.questionNumber] ?: q.correctAnswer).trim().uppercase()

            var marksAwarded = 0f
            val isCorrect: Boolean

            when (rawAnswer.lowercase()) {
                "unanswered" -> {
                    unansweredCount++
                    isCorrect = false
                    marksAwarded = 0f
                    if (!q.allowOptional) {
                        maxPossibleScore += q.correctMarks
                    }
                }
                "multiple" -> {
                    multipleMarkedCount++
                    isCorrect = false
                    marksAwarded = -q.negativeMarks
                    maxPossibleScore += q.correctMarks
                }
                "ambiguous" -> {
                    ambiguousCount++
                    isCorrect = false
                    marksAwarded = 0f
                    maxPossibleScore += q.correctMarks
                }
                else -> {
                    val normalizedAnswer = rawAnswer.uppercase()
                    maxPossibleScore += q.correctMarks
                    if (normalizedAnswer == correctAnswer) {
                        correctCount++
                        isCorrect = true
                        marksAwarded = q.correctMarks
                    } else {
                        wrongCount++
                        isCorrect = false
                        marksAwarded = -q.negativeMarks
                    }
                }
            }

            totalScore += marksAwarded

            val item = EvaluatedAnswerItem(
                questionNumber = q.questionNumber,
                detectedAnswer = rawAnswer,
                correctAnswer = correctAnswer,
                isCorrect = isCorrect,
                marksAwarded = marksAwarded
            )
            evaluatedAnswers.add(item)
            val secName = q.sectionName.ifBlank { "Section1" }
            sectionMap.getOrPut(secName) { mutableListOf() }.add(item)
        }

        // Calculate per-section breakdowns
        val sectionBreakdown = sectionQuestionsMap.map { (secName, qList) ->
            val items = sectionMap[secName] ?: emptyList()
            val secCorrect = items.count { it.isCorrect }
            val secWrong = items.count { !it.isCorrect && it.detectedAnswer != "unanswered" && it.detectedAnswer != "ambiguous" }
            val secUnanswered = items.count { it.detectedAnswer == "unanswered" }
            val secMarks = items.sumOf { it.marksAwarded.toDouble() }.toFloat()
            val secMaxMarks = qList.filter { q ->
                val ans = detectedAnswers[q.questionNumber]?.lowercase() ?: "unanswered"
                !q.allowOptional || ans != "unanswered"
            }.sumOf { it.correctMarks.toDouble() }.toFloat()

            val secPct = if (secMaxMarks > 0f) (secMarks / secMaxMarks) * 100f else 0f
            SectionResult(
                sectionName = secName,
                marks = secMarks,
                maxMarks = secMaxMarks,
                percentage = secPct,
                correct = secCorrect,
                wrong = secWrong,
                unanswered = secUnanswered
            )
        }

        val percentage = if (maxPossibleScore > 0f) {
            (totalScore / maxPossibleScore) * 100f
        } else if (totalQuestions > 0) {
            (correctCount.toFloat() / totalQuestions.toFloat()) * 100f
        } else 0f

        return ScoringEvaluation(
            totalQuestions = totalQuestions,
            correct = correctCount,
            wrong = wrongCount,
            unanswered = unansweredCount,
            multipleMarked = multipleMarkedCount,
            ambiguous = ambiguousCount,
            percentage = percentage.coerceAtLeast(0f),
            totalScore = totalScore,
            maxPossibleScore = maxPossibleScore,
            evaluatedAnswers = evaluatedAnswers,
            sectionBreakdown = sectionBreakdown
        )
    }
}

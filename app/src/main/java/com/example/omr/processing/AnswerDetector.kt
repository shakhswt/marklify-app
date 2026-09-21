package com.example.omr.processing

data class DetectedQuestionAnswer(
    val questionNumber: Int,
    val detectedAnswer: String, // "A", "B", "C", "D", "unanswered", "multiple", "ambiguous"
    val confidence: Float,
    val isAmbiguous: Boolean,
    val isMultiple: Boolean,
    val isUnanswered: Boolean,
    val bubbleReadings: List<BubbleReading>
)

class AnswerDetector(
    var fillThreshold: Float = DEFAULT_FILL_THRESHOLD,
    var unansweredThreshold: Float = DEFAULT_UNANSWERED_THRESHOLD,
    var multipleDiffMargin: Float = DEFAULT_MULTIPLE_DIFF_MARGIN,
    var ambiguousDiffMargin: Float = DEFAULT_AMBIGUOUS_DIFF_MARGIN
) {
    companion object {
        const val DEFAULT_FILL_THRESHOLD = 0.35f
        const val DEFAULT_UNANSWERED_THRESHOLD = 0.22f
        const val DEFAULT_MULTIPLE_DIFF_MARGIN = 0.15f
        const val DEFAULT_AMBIGUOUS_DIFF_MARGIN = 0.20f
    }

    fun updateThresholds(
        fill: Float = fillThreshold,
        unanswered: Float = unansweredThreshold,
        multipleDiff: Float = multipleDiffMargin,
        ambiguousDiff: Float = ambiguousDiffMargin
    ) {
        fillThreshold = fill
        unansweredThreshold = unanswered
        multipleDiffMargin = multipleDiff
        ambiguousDiffMargin = ambiguousDiff
    }

    /**
     * Classifies OMR bubble readings into detected answers ("A", "B", "C", "D", "unanswered", "multiple", "ambiguous").
     *
     * Threshold Relationship & Margin Interaction:
     * - [multipleDiffMargin] (default 0.15): Evaluated when both top1 and top2 exceed [fillThreshold]. If (top1 - top2) < 0.15,
     *   the student explicitly shaded two or more options with strong fill, so the question is marked as "multiple".
     * - [ambiguousDiffMargin] (default 0.20): Evaluated when top1 exceeds [unansweredThreshold]. If (top1 - top2) falls
     *   between 0.15 and 0.20, or if top1 is between [unansweredThreshold] and [fillThreshold], the mark distinction is weak
     *   (e.g., incomplete erasure, faint mark, or smudge). The question is classified as "ambiguous" for teacher review.
     * - Clear single answer: Requires top1 >= [fillThreshold] AND (top1 - top2) >= [ambiguousDiffMargin] (0.20).
     */
    fun detectAnswers(bubbleMap: Map<Int, List<BubbleReading>>): List<DetectedQuestionAnswer> {
        val resultList = mutableListOf<DetectedQuestionAnswer>()

        for ((questionNumber, readings) in bubbleMap.toSortedMap()) {
            if (readings.isEmpty()) {
                resultList.add(
                    DetectedQuestionAnswer(
                        questionNumber = questionNumber,
                        detectedAnswer = "unanswered",
                        confidence = 0f,
                        isAmbiguous = false,
                        isMultiple = false,
                        isUnanswered = true,
                        bubbleReadings = emptyList()
                    )
                )
                continue
            }

            val sorted = readings.sortedByDescending { it.fillRatio }
            val top1 = sorted[0]
            val top2 = if (sorted.size > 1) sorted[1] else null

            val detectedAnswer: String
            var isAmbiguous = false
            var isMultiple = false
            var isUnanswered = false
            val confidence: Float

            if (top1.fillRatio < unansweredThreshold) {
                // No bubble has significant mark
                detectedAnswer = "unanswered"
                isUnanswered = true
                confidence = 1f - top1.fillRatio
            } else if (top2 != null && top2.fillRatio >= fillThreshold && (top1.fillRatio - top2.fillRatio) < multipleDiffMargin) {
                // Two or more bubbles are filled with close values
                detectedAnswer = "multiple"
                isMultiple = true
                confidence = (top1.fillRatio + top2.fillRatio) / 2f
            } else if (top1.fillRatio < fillThreshold || (top2 != null && (top1.fillRatio - top2.fillRatio) < ambiguousDiffMargin)) {
                // Marginal fill or poor distinction between 1st and 2nd bubble
                detectedAnswer = "ambiguous"
                isAmbiguous = true
                confidence = top1.fillRatio
            } else {
                // Clear single answer
                detectedAnswer = top1.optionLetter
                confidence = (top1.fillRatio - (top2?.fillRatio ?: 0f)).coerceIn(0f, 1f)
            }

            resultList.add(
                DetectedQuestionAnswer(
                    questionNumber = questionNumber,
                    detectedAnswer = detectedAnswer,
                    confidence = confidence,
                    isAmbiguous = isAmbiguous,
                    isMultiple = isMultiple,
                    isUnanswered = isUnanswered,
                    bubbleReadings = readings
                )
            )
        }

        return resultList
    }
}

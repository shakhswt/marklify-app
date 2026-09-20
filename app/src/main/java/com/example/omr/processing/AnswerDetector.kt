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

package com.example.omr

import android.graphics.Bitmap
import com.example.data.entity.Question
import com.example.omr.detector.MarkerDetector
import com.example.omr.detector.MarkerResult
import com.example.omr.processing.AnswerDetector
import com.example.omr.processing.BubbleReader
import com.example.omr.processing.DetectedQuestionAnswer
import com.example.omr.processing.PerspectiveCorrector
import com.example.omr.processing.ScoringEngine
import com.example.omr.processing.ScoringEvaluation
import com.example.omr.spec.SheetSpec

data class OmrProcessResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val warpedBitmap: Bitmap? = null,
    val detectedAnswers: List<DetectedQuestionAnswer> = emptyList(),
    val scoring: ScoringEvaluation? = null,
    val detectedRollNumber: String? = null,
    val detectedExamSet: String? = null
)

class OmrEngine(
    val markerDetector: MarkerDetector = MarkerDetector(),
    val perspectiveCorrector: PerspectiveCorrector = PerspectiveCorrector(),
    val bubbleReader: BubbleReader = BubbleReader(),
    val answerDetector: AnswerDetector = AnswerDetector(),
    val scoringEngine: ScoringEngine = ScoringEngine()
) {
    fun detectFrame(bitmap: Bitmap): MarkerResult {
        return markerDetector.detectMarkers(bitmap)
    }

    fun resetStability() {
        markerDetector.resetStability()
    }

    fun extractRollNumber(detectedAnswers: List<DetectedQuestionAnswer>): String? {
        val digits = mutableListOf<String>()
        var anyFound = false
        for (col in 0 until 5) {
            val qNum = -100 - col
            val ans = detectedAnswers.firstOrNull { it.questionNumber == qNum }?.detectedAnswer
            if (ans != null && ans.length == 1 && ans[0].isDigit()) {
                digits.add(ans)
                anyFound = true
            } else {
                digits.add("0")
            }
        }
        return if (anyFound) digits.joinToString("") else null
    }

    fun extractExamSet(detectedAnswers: List<DetectedQuestionAnswer>): String? {
        val ans = detectedAnswers.firstOrNull { it.questionNumber == -2 }?.detectedAnswer
        return if (ans != null && ans in listOf("A", "B", "C", "D")) ans else null
    }

    /**
     * Complete OMR processing pipeline:
     * 1. Detect 4 markers
     * 2. Perspective warp to normalized SheetSpec
     * 3. Read bubble pixels
     * 4. Classify answers
     * 5. Extract Roll Number & Exam Set
     * 6. Score answers against question key
     */
    fun processFullSheet(
        sourceBitmap: Bitmap,
        questionCount: Int,
        questions: List<Question>
    ): OmrProcessResult {
        val spec = SheetSpec(questionCount = questionCount, questions = questions)

        // 1. Detect markers
        val markerResult = markerDetector.detectMarkers(sourceBitmap)
        if (!markerResult.found || markerResult.corners == null || markerResult.corners.size != 4) {
            val failureReason = markerResult.failureReason ?: "Could not find all four markers."
            return OmrProcessResult(
                success = false,
                errorMessage = failureReason
            )
        }

        // 2. Perspective warp
        val warped = perspectiveCorrector.correctPerspective(sourceBitmap, markerResult.corners, spec)
        if (warped == null) {
            return OmrProcessResult(
                success = false,
                errorMessage = "Failed to align sheet perspective. Adjust the sheet."
            )
        }

        // 3. Read bubbles
        val bubbleMap = bubbleReader.readBubbles(warped, spec)

        // 4. Classify answers
        val detectedAnswers = answerDetector.detectAnswers(bubbleMap)

        // 5. Extract metadata
        val rollNumber = extractRollNumber(detectedAnswers)
        val examSet = extractExamSet(detectedAnswers)

        // 6. Score question answers (questionNumber > 0)
        val questionAnswers = detectedAnswers.filter { it.questionNumber > 0 }
        val answerMap = questionAnswers.associate { it.questionNumber to it.detectedAnswer }
        val scoring = scoringEngine.score(answerMap, questions)

        return OmrProcessResult(
            success = true,
            warpedBitmap = warped,
            detectedAnswers = questionAnswers,
            scoring = scoring,
            detectedRollNumber = rollNumber,
            detectedExamSet = examSet
        )
    }
}

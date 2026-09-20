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
    val scoring: ScoringEvaluation? = null
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

    /**
     * Complete OMR processing pipeline:
     * 1. Detect 4 markers
     * 2. Perspective warp to normalized SheetSpec
     * 3. Read bubble pixels
     * 4. Classify answers
     * 5. Score answers against question key
     */
    fun processFullSheet(
        sourceBitmap: Bitmap,
        questionCount: Int,
        questions: List<Question>
    ): OmrProcessResult {
        val spec = SheetSpec(questionCount = questionCount)

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

        // 5. Score
        val answerMap = detectedAnswers.associate { it.questionNumber to it.detectedAnswer }
        val scoring = scoringEngine.score(answerMap, questions)

        return OmrProcessResult(
            success = true,
            warpedBitmap = warped,
            detectedAnswers = detectedAnswers,
            scoring = scoring
        )
    }
}

package com.example.omr.spec

import kotlin.math.max
import kotlin.math.min

data class PointF(val x: Float, val y: Float)
data class RectF(val left: Float, val top: Float, val right: Float, val bottom: Float)

data class BubbleLocation(
    val questionNumber: Int,
    val optionIndex: Int, // 0 for A, 1 for B, 2 for C, 3 for D
    val optionLetter: String, // "A", "B", "C", "D"
    val centerX: Float,
    val centerY: Float,
    val radius: Float
)

/**
 * Shared layout specification for OMR sheets.
 * BOTH the Blank Sheet Generator (PDF / Canvas) and Scanner / BubbleReader
 * MUST use this exact coordinate specification.
 */
class SheetSpec(
    val questionCount: Int,
    val targetWidth: Float = TARGET_WIDTH,
    val targetHeight: Float = TARGET_HEIGHT
) {
    companion object {
        const val TARGET_WIDTH = 1200f
        const val TARGET_HEIGHT = 1600f

        // Corner marker dimensions
        const val MARKER_SIZE = 70f
        const val MARKER_MARGIN = 45f

        val OPTION_LETTERS = listOf("A", "B", "C", "D")
    }

    val isTwoColumns: Boolean = questionCount > 25
    val columnsCount: Int = if (isTwoColumns) 2 else 1

    val rowsPerColumn: Int = if (isTwoColumns) {
        (questionCount + 1) / 2
    } else {
        questionCount
    }

    // Corner Marker centers in normalized coordinates
    val tlMarker = PointF(MARKER_MARGIN + MARKER_SIZE / 2f, MARKER_MARGIN + MARKER_SIZE / 2f)
    val trMarker = PointF(targetWidth - MARKER_MARGIN - MARKER_SIZE / 2f, MARKER_MARGIN + MARKER_SIZE / 2f)
    val brMarker = PointF(targetWidth - MARKER_MARGIN - MARKER_SIZE / 2f, targetHeight - MARKER_MARGIN - MARKER_SIZE / 2f)
    val blMarker = PointF(MARKER_MARGIN + MARKER_SIZE / 2f, targetHeight - MARKER_MARGIN - MARKER_SIZE / 2f)

    /**
     * Marker rects for drawing/verification
     */
    val tlRect = RectF(MARKER_MARGIN, MARKER_MARGIN, MARKER_MARGIN + MARKER_SIZE, MARKER_MARGIN + MARKER_SIZE)
    val trRect = RectF(targetWidth - MARKER_MARGIN - MARKER_SIZE, MARKER_MARGIN, targetWidth - MARKER_MARGIN, MARKER_MARGIN + MARKER_SIZE)
    val brRect = RectF(targetWidth - MARKER_MARGIN - MARKER_SIZE, targetHeight - MARKER_MARGIN - MARKER_SIZE, targetWidth - MARKER_MARGIN, targetHeight - MARKER_MARGIN)
    val blRect = RectF(MARKER_MARGIN, targetHeight - MARKER_MARGIN - MARKER_SIZE, MARKER_MARGIN + MARKER_SIZE, targetHeight - MARKER_MARGIN)

    fun getMarkerCorners(): List<PointF> = listOf(tlMarker, trMarker, brMarker, blMarker)

    // Layout margins for questions
    private val questionAreaTop = 270f
    private val questionAreaBottom = 1530f
    private val availableHeight = questionAreaBottom - questionAreaTop

    val rowHeight: Float = if (rowsPerColumn > 0) {
        min(48f, availableHeight / max(rowsPerColumn, 1))
    } else 48f

    val bubbleRadius: Float = max(7f, min(14f, rowHeight * 0.32f))
    val bubbleSpacing: Float = max(32f, bubbleRadius * 3.2f)

    // Column horizontal placement
    private val col1Left: Float = if (isTwoColumns) 100f else (targetWidth - 450f) / 2f
    private val col2Left: Float = if (isTwoColumns) 640f else 0f

    fun getColumnLeft(columnIndex: Int): Float = if (columnIndex == 0) col1Left else col2Left

    /**
     * Returns the bubble location for a given question and option
     */
    fun getBubbleLocation(questionNumber: Int, optionIndex: Int): BubbleLocation {
        val colIndex = if (isTwoColumns) {
            if (questionNumber <= rowsPerColumn) 0 else 1
        } else {
            0
        }

        val rowIndex = if (colIndex == 0) {
            questionNumber - 1
        } else {
            questionNumber - 1 - rowsPerColumn
        }

        val colLeft = getColumnLeft(colIndex)
        val centerY = questionAreaTop + (rowIndex + 0.5f) * rowHeight
        // Left part has question number (width ~60), then bubbles A, B, C, D
        val bubblesStartX = colLeft + 85f
        val centerX = bubblesStartX + optionIndex * bubbleSpacing
        val letter = OPTION_LETTERS.getOrElse(optionIndex) { "A" }

        return BubbleLocation(
            questionNumber = questionNumber,
            optionIndex = optionIndex,
            optionLetter = letter,
            centerX = centerX,
            centerY = centerY,
            radius = bubbleRadius
        )
    }

    /**
     * Get all 4 bubbles for a question
     */
    fun getBubblesForQuestion(questionNumber: Int): List<BubbleLocation> {
        return (0..3).map { optionIndex ->
            getBubbleLocation(questionNumber, optionIndex)
        }
    }

    /**
     * Get all bubbles across all questions in the test
     */
    fun getAllBubbles(): List<BubbleLocation> {
        val list = mutableListOf<BubbleLocation>()
        for (q in 1..questionCount) {
            list.addAll(getBubblesForQuestion(q))
        }
        return list
    }
}

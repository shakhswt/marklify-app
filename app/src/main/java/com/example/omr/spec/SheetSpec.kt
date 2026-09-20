package com.example.omr.spec

import com.example.data.entity.Question
import kotlin.math.max
import kotlin.math.min

data class PointF(val x: Float, val y: Float)
data class RectF(val left: Float, val top: Float, val right: Float, val bottom: Float)

enum class QuestionTypeSpec(val typeKey: String) {
    MCQ4("MCQ4"),
    MCQ5("MCQ5"),
    TRUE_FALSE("TRUE_FALSE"),
    NUMERICAL("NUMERICAL"),
    MATRIX("MATRIX")
}

data class BubbleLocation(
    val questionNumber: Int,
    val optionIndex: Int, // 0-based
    val optionLetter: String, // "A", "B", "C", "D", "E", "True", "False", "0"-"9", "+", "-", "A1", "B2", etc.
    val centerX: Float,
    val centerY: Float,
    val radius: Float,
    val sectionName: String = "Section1",
    val questionType: String = "MCQ4",
    val matrixRow: String = "",
    val matrixCol: String = "",
    val digitColumn: Int = 0
)

/**
 * Shared layout specification for OMR sheets.
 * BOTH the Blank Sheet Generator (PDF / Canvas) and Scanner / BubbleReader
 * MUST use this exact coordinate specification.
 */
class SheetSpec(
    val questionCount: Int,
    val questions: List<Question> = emptyList(),
    val targetWidth: Float = TARGET_WIDTH,
    val targetHeight: Float = TARGET_HEIGHT,
    val hasRollNoGrid: Boolean = true,
    val hasExamSet: Boolean = true
) {
    companion object {
        const val TARGET_WIDTH = 1200f
        const val TARGET_HEIGHT = 1600f

        // Corner marker dimensions
        const val MARKER_SIZE = 70f
        const val MARKER_MARGIN = 45f

        val EXAM_SETS = listOf("A", "B", "C", "D")
        val ROLL_NO_COLS = 5
        val DIGITS = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    }

    // Corner Marker centers in normalized coordinates
    val tlMarker = PointF(MARKER_MARGIN + MARKER_SIZE / 2f, MARKER_MARGIN + MARKER_SIZE / 2f)
    val trMarker = PointF(targetWidth - MARKER_MARGIN - MARKER_SIZE / 2f, MARKER_MARGIN + MARKER_SIZE / 2f)
    val brMarker = PointF(targetWidth - MARKER_MARGIN - MARKER_SIZE / 2f, targetHeight - MARKER_MARGIN - MARKER_SIZE / 2f)
    val blMarker = PointF(MARKER_MARGIN + MARKER_SIZE / 2f, targetHeight - MARKER_MARGIN - MARKER_SIZE / 2f)

    val tlRect = RectF(MARKER_MARGIN, MARKER_MARGIN, MARKER_MARGIN + MARKER_SIZE, MARKER_MARGIN + MARKER_SIZE)
    val trRect = RectF(targetWidth - MARKER_MARGIN - MARKER_SIZE, MARKER_MARGIN, targetWidth - MARKER_MARGIN, MARKER_MARGIN + MARKER_SIZE)
    val brRect = RectF(targetWidth - MARKER_MARGIN - MARKER_SIZE, targetHeight - MARKER_MARGIN - MARKER_SIZE, targetWidth - MARKER_MARGIN, targetHeight - MARKER_MARGIN)
    val blRect = RectF(MARKER_MARGIN, targetHeight - MARKER_MARGIN - MARKER_SIZE, MARKER_MARGIN + MARKER_SIZE, targetHeight - MARKER_MARGIN)

    fun getMarkerCorners(): List<PointF> = listOf(tlMarker, trMarker, brMarker, blMarker)

    // Header & Roll No Placement
    val rollNoStartX = 280f
    val rollNoStartY = 160f
    val rollNoColWidth = 36f
    val rollNoRowHeight = 32f
    val rollNoBubbleRadius = 12f

    val examSetStartX = 620f
    val examSetStartY = 160f
    val examSetSpacing = 42f

    // Roll No Bubbles
    fun getRollNoBubbles(): List<BubbleLocation> {
        val list = mutableListOf<BubbleLocation>()
        if (!hasRollNoGrid) return list
        for (col in 0 until ROLL_NO_COLS) {
            for (row in 0 until 10) {
                val cx = rollNoStartX + col * rollNoColWidth + rollNoColWidth / 2f
                val cy = rollNoStartY + 35f + row * rollNoRowHeight
                list.add(
                    BubbleLocation(
                        questionNumber = -1, // -1 denotes Roll No
                        optionIndex = row,
                        optionLetter = DIGITS[row],
                        centerX = cx,
                        centerY = cy,
                        radius = rollNoBubbleRadius,
                        digitColumn = col
                    )
                )
            }
        }
        return list
    }

    // Exam Set Bubbles
    fun getExamSetBubbles(): List<BubbleLocation> {
        val list = mutableListOf<BubbleLocation>()
        if (!hasExamSet) return list
        for (idx in EXAM_SETS.indices) {
            val cx = examSetStartX + idx * examSetSpacing + examSetSpacing / 2f
            val cy = examSetStartY + 35f
            list.add(
                BubbleLocation(
                    questionNumber = -2, // -2 denotes Exam Set
                    optionIndex = idx,
                    optionLetter = EXAM_SETS[idx],
                    centerX = cx,
                    centerY = cy,
                    radius = rollNoBubbleRadius
                )
            )
        }
        return list
    }

    // Layout dimensions for Questions
    val isTwoColumns: Boolean = questionCount > 25
    val columnsCount: Int = if (isTwoColumns) 2 else 1
    val rowsPerColumn: Int = if (isTwoColumns) (questionCount + 1) / 2 else max(questionCount, 1)

    private val questionAreaTop = 500f
    private val questionAreaBottom = 1520f
    private val availableHeight = questionAreaBottom - questionAreaTop

    val rowHeight: Float = if (rowsPerColumn > 0) {
        min(44f, availableHeight / max(rowsPerColumn, 1))
    } else 44f

    val bubbleRadius: Float = max(8f, min(13f, rowHeight * 0.30f))
    val bubbleSpacing: Float = max(30f, bubbleRadius * 3.0f)

    private val col1Left: Float = if (isTwoColumns) 100f else (targetWidth - 450f) / 2f
    private val col2Left: Float = if (isTwoColumns) 640f else 0f

    fun getColumnLeft(columnIndex: Int): Float = if (columnIndex == 0) col1Left else col2Left

    /**
     * Get bubbles for a specific question based on its question type (MCQ4, MCQ5, True/False, Numerical, Matrix)
     */
    fun getBubblesForQuestion(questionNumber: Int): List<BubbleLocation> {
        val question = questions.find { it.questionNumber == questionNumber }
        val qType = question?.questionType ?: "MCQ4"
        val secName = question?.sectionName ?: "Section1"

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
        val bubblesStartX = colLeft + 85f

        val list = mutableListOf<BubbleLocation>()

        when (qType) {
            "MCQ5" -> {
                val letters = listOf("A", "B", "C", "D", "E")
                for (i in 0..4) {
                    list.add(
                        BubbleLocation(
                            questionNumber = questionNumber,
                            optionIndex = i,
                            optionLetter = letters[i],
                            centerX = bubblesStartX + i * bubbleSpacing,
                            centerY = centerY,
                            radius = bubbleRadius,
                            sectionName = secName,
                            questionType = qType
                        )
                    )
                }
            }
            "TRUE_FALSE" -> {
                val labels = listOf("True", "False")
                val spacing = bubbleSpacing * 1.5f
                for (i in 0..1) {
                    list.add(
                        BubbleLocation(
                            questionNumber = questionNumber,
                            optionIndex = i,
                            optionLetter = labels[i],
                            centerX = bubblesStartX + i * spacing,
                            centerY = centerY,
                            radius = bubbleRadius,
                            sectionName = secName,
                            questionType = qType
                        )
                    )
                }
            }
            "NUMERICAL" -> {
                val digitsCount = question?.numDigits ?: 2
                val hasNeg = question?.hasNegativeSign ?: false
                var curX = bubblesStartX
                if (hasNeg) {
                    list.add(
                        BubbleLocation(
                            questionNumber = questionNumber,
                            optionIndex = 0,
                            optionLetter = "-",
                            centerX = curX,
                            centerY = centerY,
                            radius = bubbleRadius,
                            sectionName = secName,
                            questionType = qType,
                            digitColumn = -1
                        )
                    )
                    curX += bubbleSpacing
                }
                for (dCol in 0 until digitsCount) {
                    for (dVal in 0..9) {
                        list.add(
                            BubbleLocation(
                                questionNumber = questionNumber,
                                optionIndex = dVal,
                                optionLetter = dVal.toString(),
                                centerX = curX + dVal * (bubbleSpacing * 0.7f),
                                centerY = centerY,
                                radius = bubbleRadius * 0.85f,
                                sectionName = secName,
                                questionType = qType,
                                digitColumn = dCol
                            )
                        )
                    }
                    curX += 10 * (bubbleSpacing * 0.7f) + 12f
                }
            }
            "MATRIX" -> {
                val rows = listOf("A", "B", "C", "D")
                val cols = listOf("1", "2", "3", "4")
                var idx = 0
                for (rIdx in rows.indices) {
                    for (cIdx in cols.indices) {
                        list.add(
                            BubbleLocation(
                                questionNumber = questionNumber,
                                optionIndex = idx++,
                                optionLetter = "${rows[rIdx]}${cols[cIdx]}",
                                centerX = bubblesStartX + cIdx * bubbleSpacing,
                                centerY = centerY + (rIdx - 1.5f) * (rowHeight * 0.5f),
                                radius = bubbleRadius * 0.8f,
                                sectionName = secName,
                                questionType = qType,
                                matrixRow = rows[rIdx],
                                matrixCol = cols[cIdx]
                            )
                        )
                    }
                }
            }
            else -> { // Default MCQ4
                val letters = listOf("A", "B", "C", "D")
                for (i in 0..3) {
                    list.add(
                        BubbleLocation(
                            questionNumber = questionNumber,
                            optionIndex = i,
                            optionLetter = letters[i],
                            centerX = bubblesStartX + i * bubbleSpacing,
                            centerY = centerY,
                            radius = bubbleRadius,
                            sectionName = secName,
                            questionType = qType
                        )
                    )
                }
            }
        }

        return list
    }

    /**
     * Get all question bubbles across the test
     */
    fun getAllQuestionBubbles(): List<BubbleLocation> {
        val list = mutableListOf<BubbleLocation>()
        val count = if (questions.isNotEmpty()) questions.size else questionCount
        for (q in 1..count) {
            list.addAll(getBubblesForQuestion(q))
        }
        return list
    }

    /**
     * Get all bubbles on sheet (Roll No + Exam Set + Questions)
     */
    fun getAllBubbles(): List<BubbleLocation> {
        val list = mutableListOf<BubbleLocation>()
        list.addAll(getRollNoBubbles())
        list.addAll(getExamSetBubbles())
        list.addAll(getAllQuestionBubbles())
        return list
    }
}

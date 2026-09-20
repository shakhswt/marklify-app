package com.example.omr.processing

import android.graphics.Bitmap
import android.util.Log
import com.example.omr.spec.BubbleLocation
import com.example.omr.spec.SheetSpec
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.roundToInt

data class BubbleReading(
    val questionNumber: Int,
    val optionIndex: Int,
    val optionLetter: String,
    val fillRatio: Float,
    val darkPixels: Int,
    val totalPixels: Int
)

class BubbleReader {
    companion object {
        private const val TAG = "BubbleReader"
    }

    /**
     * Reads all bubbles in the warped sheet image according to SheetSpec coordinates
     */
    fun readBubbles(warpedBitmap: Bitmap, spec: SheetSpec): Map<Int, List<BubbleReading>> {
        var mat: Mat? = null
        var gray: Mat? = null
        var binary: Mat? = null

        val resultMap = mutableMapOf<Int, MutableList<BubbleReading>>()

        try {
            mat = Mat()
            Utils.bitmapToMat(warpedBitmap, mat)

            gray = Mat()
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY)

            // Blur slightly to reduce paper grain
            val blurred = Mat()
            Imgproc.GaussianBlur(gray, blurred, Size(3.0, 3.0), 0.0)

            // Otsu's thresholding to separate paper white from dark ink
            binary = Mat()
            Imgproc.threshold(
                blurred,
                binary,
                0.0,
                255.0,
                Imgproc.THRESH_BINARY_INV or Imgproc.THRESH_OTSU
            )
            blurred.release()

            val allBubbles = spec.getAllBubbles()

            for (bubble in allBubbles) {
                val reading = evaluateBubble(binary, bubble)
                resultMap.getOrPut(bubble.questionNumber) { mutableListOf() }.add(reading)
            }

        } catch (e: Throwable) {
            Log.e(TAG, "Error in readBubbles: ${e.message}", e)
        } finally {
            mat?.release()
            gray?.release()
            binary?.release()
        }

        return resultMap
    }

    private fun evaluateBubble(binaryMat: Mat, bubble: BubbleLocation): BubbleReading {
        val cx = bubble.centerX.roundToInt()
        val cy = bubble.centerY.roundToInt()
        // Evaluate inner 80% to avoid counting the printed circular border
        val r = (bubble.radius * 0.80f).roundToInt().coerceAtLeast(3)

        var darkPixelCount = 0
        var totalSampledPixels = 0

        val minX = (cx - r).coerceAtLeast(0)
        val maxX = (cx + r).coerceAtMost(binaryMat.cols() - 1)
        val minY = (cy - r).coerceAtLeast(0)
        val maxY = (cy + r).coerceAtMost(binaryMat.rows() - 1)

        val rSquared = r * r

        // Sample pixels inside the circle
        for (y in minY..maxY) {
            val dy = y - cy
            for (x in minX..maxX) {
                val dx = x - cx
                if (dx * dx + dy * dy <= rSquared) {
                    totalSampledPixels++
                    val pixelValue = binaryMat.get(y, x)?.get(0) ?: 0.0
                    if (pixelValue > 127.0) { // Inverted binary: >127 means dark mark
                        darkPixelCount++
                    }
                }
            }
        }

        val fillRatio = if (totalSampledPixels > 0) {
            darkPixelCount.toFloat() / totalSampledPixels.toFloat()
        } else 0f

        return BubbleReading(
            questionNumber = bubble.questionNumber,
            optionIndex = bubble.optionIndex,
            optionLetter = bubble.optionLetter,
            fillRatio = fillRatio,
            darkPixels = darkPixelCount,
            totalPixels = totalSampledPixels
        )
    }
}

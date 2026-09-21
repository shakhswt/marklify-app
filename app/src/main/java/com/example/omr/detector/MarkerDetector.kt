package com.example.omr.detector

import android.graphics.Bitmap
import android.util.Log
import com.example.omr.spec.PointF
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

data class MarkerResult(
    val found: Boolean,
    val corners: List<PointF>?, // Order: TL, TR, BR, BL
    val status: String,
    val isStable: Boolean,
    val failureReason: String? = null
)

class MarkerDetector {
    companion object {
        private const val TAG = "MarkerDetector"
        private const val MIN_LAPLACIAN_VARIANCE = 30.0
        private const val STABILITY_DISTANCE_THRESHOLD = 25.0f
        private const val REQUIRED_STABLE_FRAMES = 3
    }

    private var previousCorners: List<PointF>? = null
    private var stableFrameCount = 0

    fun resetStability() {
        previousCorners = null
        stableFrameCount = 0
    }

    fun detectMarkers(bitmap: Bitmap): MarkerResult {
        var mat: Mat? = null
        var gray: Mat? = null
        var blurred: Mat? = null
        var thresh: Mat? = null

        try {
            mat = Mat()
            Utils.bitmapToMat(bitmap, mat)

            val width = mat.cols()
            val height = mat.rows()

            // 1. Check blurriness
            gray = Mat()
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY)

            val laplacian = Mat()
            Imgproc.Laplacian(gray, laplacian, CvType.CV_64F)
            val mean = MatOfDouble()
            val stddev = MatOfDouble()
            Core.meanStdDev(laplacian, mean, stddev)
            val variance = stddev.toArray()[0] * stddev.toArray()[0]
            laplacian.release()
            mean.release()
            stddev.release()

            if (variance < MIN_LAPLACIAN_VARIANCE) {
                resetStability()
                return MarkerResult(
                    found = false,
                    corners = null,
                    status = "Image is too blurry. Please scan again.",
                    isStable = false,
                    failureReason = "Image is too blurry. Please scan again."
                )
            }

            // 2. Blur & Adaptive Threshold
            blurred = Mat()
            Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)

            thresh = Mat()
            Imgproc.adaptiveThreshold(
                blurred,
                thresh,
                255.0,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY_INV,
                19,
                5.0
            )

            // 3. Find contours
            val contours = ArrayList<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
            hierarchy.release()

            val imageArea = width.toDouble() * height.toDouble()
            val minMarkerArea = imageArea * 0.0003 // at least 0.03% of sheet
            val maxMarkerArea = imageArea * 0.08  // at most 8%

            val candidateCenters = ArrayList<Point>()
            val candidateRects = ArrayList<Rect>()

            for (contour in contours) {
                val contour2f = MatOfPoint2f(*contour.toArray())
                val arcLen = Imgproc.arcLength(contour2f, true)
                val approx = MatOfPoint2f()
                Imgproc.approxPolyDP(contour2f, approx, 0.04 * arcLen, true)

                val area = Imgproc.contourArea(approx)
                if (area in minMarkerArea..maxMarkerArea) {
                    val rect = Imgproc.boundingRect(contour)
                    val aspectRatio = rect.width.toDouble() / rect.height.toDouble()

                    if (aspectRatio in 0.65..1.55) {
                        // Check if center has high black pixel fill ratio
                        val roi = thresh.submat(rect)
                        val nonZero = Core.countNonZero(roi)
                        val totalRectPixels = rect.width * rect.height
                        val fillRatio = nonZero.toDouble() / totalRectPixels.toDouble()
                        roi.release()

                        if (fillRatio > 0.50) { // Solid black marker in THRESH_BINARY_INV
                            val cx = rect.x + rect.width / 2.0
                            val cy = rect.y + rect.height / 2.0
                            candidateCenters.add(Point(cx, cy))
                            candidateRects.add(rect)
                        }
                    }
                }
                contour.release()
                contour2f.release()
                approx.release()
            }

            if (candidateCenters.size < 4) {
                resetStability()
                return MarkerResult(
                    found = false,
                    corners = null,
                    status = "Looking for sheet...",
                    isStable = false,
                    failureReason = "Could not find all four markers."
                )
            }

            // Find 4 best corner markers across the 4 quadrants
            val orderedCorners = selectBestFourCorners(candidateCenters, width, height)
            if (orderedCorners == null) {
                resetStability()
                return MarkerResult(
                    found = false,
                    corners = null,
                    status = "Adjust the sheet.",
                    isStable = false,
                    failureReason = "Adjust the sheet."
                )
            }

            // Check distance / scale: Move camera closer if sheet takes less than 25% of image width/height
            val (tl, tr, br, bl) = orderedCorners
            val topWidth = hypot(tr.x - tl.x, tr.y - tl.y)
            val bottomWidth = hypot(br.x - bl.x, br.y - bl.y)
            val avgWidth = (topWidth + bottomWidth) / 2.0
            val leftHeight = hypot(bl.x - tl.x, bl.y - tl.y)
            val rightHeight = hypot(br.x - tr.x, br.y - tr.y)
            val avgHeight = (leftHeight + rightHeight) / 2.0

            if (avgWidth < width * 0.35 || avgHeight < height * 0.35) {
                resetStability()
                return MarkerResult(
                    found = true,
                    corners = orderedCorners,
                    status = "Move the camera closer.",
                    isStable = false,
                    failureReason = "Move the camera closer."
                )
            }

            // 4. Stability Check across consecutive frames
            val isStableNow = checkStability(orderedCorners)
            val currentStatus = when {
                stableFrameCount >= REQUIRED_STABLE_FRAMES -> "Scanning..."
                stableFrameCount > 0 -> "Hold steady..."
                else -> "Sheet detected. Hold steady..."
            }

            return MarkerResult(
                found = true,
                corners = orderedCorners,
                status = currentStatus,
                isStable = isStableNow
            )

        } catch (e: Throwable) {
            Log.e(TAG, "Error detecting markers: ${e.message}", e)
            resetStability()
            return MarkerResult(
                found = false,
                corners = null,
                status = "Looking for sheet...",
                isStable = false,
                failureReason = e.message
            )
        } finally {
            mat?.release()
            gray?.release()
            blurred?.release()
            thresh?.release()
        }
    }

    private fun checkStability(current: List<PointF>): Boolean {
        val prev = previousCorners
        if (prev == null || prev.size != 4) {
            previousCorners = current
            stableFrameCount = 1
            return false
        }

        var maxDelta = 0.0f
        for (i in 0..3) {
            val d = hypot(current[i].x - prev[i].x, current[i].y - prev[i].y)
            maxDelta = max(maxDelta, d)
        }

        previousCorners = current
        if (maxDelta <= STABILITY_DISTANCE_THRESHOLD) {
            stableFrameCount++
        } else {
            stableFrameCount = 1
        }

        return stableFrameCount >= REQUIRED_STABLE_FRAMES
    }

    /**
     * Finds the 4 candidate points that best match Top-Left, Top-Right, Bottom-Right, Bottom-Left
     */
    private fun selectBestFourCorners(
        candidates: List<Point>,
        imageWidth: Int,
        imageHeight: Int
    ): List<PointF>? {
        if (candidates.size < 4) return null

        // Find the outer bounds / centroid of candidates
        var sumX = 0.0
        var sumY = 0.0
        for (c in candidates) {
            sumX += c.x
            sumY += c.y
        }
        val cx = sumX / candidates.size
        val cy = sumY / candidates.size

        // Quadrants:
        // TL: x < cx, y < cy
        // TR: x > cx, y < cy
        // BR: x > cx, y > cy
        // BL: x < cx, y > cy
        var bestTL: Point? = null
        var bestTR: Point? = null
        var bestBR: Point? = null
        var bestBL: Point? = null

        var minTLDist = Double.MAX_VALUE
        var minTRDist = Double.MAX_VALUE
        var minBRDist = Double.MAX_VALUE
        var minBLDist = Double.MAX_VALUE

        for (c in candidates) {
            // Distance to image corners
            val dTL = hypot(c.x - 0.0, c.y - 0.0)
            val dTR = hypot(c.x - imageWidth.toDouble(), c.y - 0.0)
            val dBR = hypot(c.x - imageWidth.toDouble(), c.y - imageHeight.toDouble())
            val dBL = hypot(c.x - 0.0, c.y - imageHeight.toDouble())

            if (c.x <= cx && c.y <= cy && dTL < minTLDist) {
                minTLDist = dTL
                bestTL = c
            }
            if (c.x >= cx && c.y <= cy && dTR < minTRDist) {
                minTRDist = dTR
                bestTR = c
            }
            if (c.x >= cx && c.y >= cy && dBR < minBRDist) {
                minBRDist = dBR
                bestBR = c
            }
            if (c.x <= cx && c.y >= cy && dBL < minBLDist) {
                minBLDist = dBL
                bestBL = c
            }
        }

        if (bestTL != null && bestTR != null && bestBR != null && bestBL != null) {
            // Verify that all 4 are distinct
            val set = setOf(bestTL, bestTR, bestBR, bestBL)
            if (set.size == 4) {
                return listOf(
                    PointF(bestTL.x.toFloat(), bestTL.y.toFloat()),
                    PointF(bestTR.x.toFloat(), bestTR.y.toFloat()),
                    PointF(bestBR.x.toFloat(), bestBR.y.toFloat()),
                    PointF(bestBL.x.toFloat(), bestBL.y.toFloat())
                )
            }
        }

        return null
    }
}

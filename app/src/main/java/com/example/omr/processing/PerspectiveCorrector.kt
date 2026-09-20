package com.example.omr.processing

import android.graphics.Bitmap
import android.util.Log
import com.example.omr.spec.PointF
import com.example.omr.spec.SheetSpec
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class PerspectiveCorrector {
    companion object {
        private const val TAG = "PerspectiveCorrector"
    }

    /**
     * Warps the input bitmap using 4 detected corners to produce a normalized top-down sheet bitmap
     * of dimensions spec.targetWidth x spec.targetHeight.
     */
    fun correctPerspective(
        sourceBitmap: Bitmap,
        detectedCorners: List<PointF>, // Order: TL, TR, BR, BL
        spec: SheetSpec
    ): Bitmap? {
        if (detectedCorners.size != 4) return null

        var srcMat: Mat? = null
        var dstMat: Mat? = null
        var transformMatrix: Mat? = null

        return try {
            srcMat = Mat()
            Utils.bitmapToMat(sourceBitmap, srcMat)

            // Source points from detected corners
            val srcPoints = MatOfPoint2f(
                Point(detectedCorners[0].x.toDouble(), detectedCorners[0].y.toDouble()),
                Point(detectedCorners[1].x.toDouble(), detectedCorners[1].y.toDouble()),
                Point(detectedCorners[2].x.toDouble(), detectedCorners[2].y.toDouble()),
                Point(detectedCorners[3].x.toDouble(), detectedCorners[3].y.toDouble())
            )

            // Destination points from SheetSpec marker centers
            val dstPoints = MatOfPoint2f(
                Point(spec.tlMarker.x.toDouble(), spec.tlMarker.y.toDouble()),
                Point(spec.trMarker.x.toDouble(), spec.trMarker.y.toDouble()),
                Point(spec.brMarker.x.toDouble(), spec.brMarker.y.toDouble()),
                Point(spec.blMarker.x.toDouble(), spec.blMarker.y.toDouble())
            )

            transformMatrix = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)
            dstMat = Mat()

            val targetSize = Size(spec.targetWidth.toDouble(), spec.targetHeight.toDouble())
            Imgproc.warpPerspective(
                srcMat,
                dstMat,
                transformMatrix,
                targetSize,
                Imgproc.INTER_LINEAR
            )

            val outputBitmap = Bitmap.createBitmap(
                spec.targetWidth.toInt(),
                spec.targetHeight.toInt(),
                Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(dstMat, outputBitmap)
            outputBitmap
        } catch (e: Throwable) {
            Log.e(TAG, "Error in correctPerspective: ${e.message}", e)
            null
        } finally {
            srcMat?.release()
            dstMat?.release()
            transformMatrix?.release()
        }
    }
}

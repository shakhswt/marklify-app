package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import com.example.R
import com.example.omr.spec.SheetSpec
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

enum class PageFormat(val displayName: String, val shortName: String, val widthPt: Int, val heightPt: Int) {
    A4("A4 (210 × 297 mm)", "A4", 595, 842),
    LETTER("US Letter (8.5 × 11 in)", "US Letter", 612, 792)
}

object OmrSheetGenerator {

    /**
     * Renders the printable OMR sheet onto an Android Canvas using the exact SheetSpec coordinates.
     */
    fun renderToCanvas(
        canvas: Canvas,
        spec: SheetSpec,
        testName: String,
        topicName: String
    ) {
        // 1. Clear background to pure white
        canvas.drawColor(Color.WHITE)

        val solidBlackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        val strokeBlackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2.2f
        }

        val thinStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 32f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val fieldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }

        val qNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 15f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // 2. Draw Registration Corner Markers
        canvas.drawRect(spec.tlRect.left, spec.tlRect.top, spec.tlRect.right, spec.tlRect.bottom, solidBlackPaint)
        canvas.drawRect(spec.trRect.left, spec.trRect.top, spec.trRect.right, spec.trRect.bottom, solidBlackPaint)
        canvas.drawRect(spec.brRect.left, spec.brRect.top, spec.brRect.right, spec.brRect.bottom, solidBlackPaint)
        canvas.drawRect(spec.blRect.left, spec.blRect.top, spec.blRect.right, spec.blRect.bottom, solidBlackPaint)

        // Frame
        canvas.drawRect(
            spec.tlRect.left - 10f,
            spec.tlRect.top - 10f,
            spec.trRect.right + 10f,
            spec.blRect.bottom + 10f,
            thinStrokePaint
        )

        // 3. Title & Header Information
        val centerX = spec.targetWidth / 2f
        canvas.drawText("MARKLIFY OMR ANSWER SHEET", centerX, 70f, titlePaint)
        val actualCount = if (spec.questions.isNotEmpty()) spec.questions.size else spec.questionCount
        canvas.drawText("$testName  •  $topicName  •  $actualCount Questions", centerX, 102f, subtitlePaint)

        // Student Info Header Box
        val boxLeft = 120f
        val boxRight = spec.targetWidth - 120f
        canvas.drawRect(boxLeft, 115f, boxRight, 150f, thinStrokePaint)

        canvas.drawText("NAME:", boxLeft + 15f, 138f, fieldPaint)
        canvas.drawText("EXAM:", centerX - 100f, 138f, fieldPaint)
        canvas.drawText("DATE:", boxRight - 220f, 138f, fieldPaint)

        // 4. Roll Number Grid
        if (spec.hasRollNoGrid) {
            canvas.drawText("Roll No", spec.rollNoStartX + 65f, spec.rollNoStartY - 10f, fieldPaint)
            val rollGridWidth = spec.numRollDigits * spec.rollNoColWidth
            canvas.drawRect(
                spec.rollNoStartX,
                spec.rollNoStartY,
                spec.rollNoStartX + rollGridWidth,
                spec.rollNoStartY + 30f,
                thinStrokePaint
            )
            for (col in 1 until spec.numRollDigits) {
                val gx = spec.rollNoStartX + col * spec.rollNoColWidth
                canvas.drawLine(gx, spec.rollNoStartY, gx, spec.rollNoStartY + 30f, thinStrokePaint)
            }
            // Roll No Bubbles
            val rollBubbles = spec.getRollNoBubbles()
            for (b in rollBubbles) {
                // Digit row label on left
                if (b.digitColumn == 0) {
                    canvas.drawText(b.optionLetter, b.centerX - 24f, b.centerY + 4f, letterPaint)
                }
                canvas.drawCircle(b.centerX, b.centerY, b.radius, strokeBlackPaint)
            }
        }

        // 5. Exam Set Grid
        if (spec.hasExamSet) {
            canvas.drawText("Exam Set", spec.examSetStartX + 50f, spec.examSetStartY - 10f, fieldPaint)
            val setBubbles = spec.getExamSetBubbles()
            for (b in setBubbles) {
                canvas.drawText(b.optionLetter, b.centerX, b.centerY - b.radius - 4f, letterPaint)
                canvas.drawCircle(b.centerX, b.centerY, b.radius, strokeBlackPaint)
            }
        }

        // 6. Dividers for Multi-column layout
        if (spec.isTwoColumns) {
            canvas.drawLine(centerX, 480f, centerX, 1530f, thinStrokePaint)
        }

        // 7. Draw Questions & Bubbles
        val count = if (spec.questions.isNotEmpty()) spec.questions.size else spec.questionCount
        for (q in 1..count) {
            val bubbles = spec.getBubblesForQuestion(q)
            if (bubbles.isEmpty()) continue

            val firstBubble = bubbles[0]
            val centerY = firstBubble.centerY

            // Draw Question Number
            val qText = String.format("%02d.", q)
            val textBounds = Rect()
            qNumPaint.getTextBounds(qText, 0, qText.length, textBounds)
            canvas.drawText(qText, firstBubble.centerX - 35f, centerY + textBounds.height() / 2f, qNumPaint)

            // Draw Bubbles
            for (bubble in bubbles) {
                canvas.drawCircle(bubble.centerX, bubble.centerY, bubble.radius, strokeBlackPaint)

                if (bubble.optionLetter.length <= 5) {
                    val lBounds = Rect()
                    letterPaint.getTextBounds(bubble.optionLetter, 0, bubble.optionLetter.length, lBounds)
                    canvas.drawText(
                        bubble.optionLetter,
                        bubble.centerX,
                        bubble.centerY + lBounds.height() / 2f - 1f,
                        letterPaint
                    )
                }
            }
        }
    }

    /**
     * Generates a high-resolution Bitmap of the OMR sheet
     */
    fun generateBitmap(
        spec: SheetSpec,
        testName: String,
        topicName: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            spec.targetWidth.toInt(),
            spec.targetHeight.toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        renderToCanvas(canvas, spec, testName, topicName)
        return bitmap
    }

    /**
     * Generates a printable PDF file formatted as A4 or US Letter.
     */
    fun generatePdf(
        context: Context,
        spec: SheetSpec,
        testName: String,
        topicName: String,
        testId: Long,
        pageFormat: PageFormat = PageFormat.A4
    ): File {
        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(pageFormat.widthPt, pageFormat.heightPt, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        val scaleX = pageFormat.widthPt.toFloat() / spec.targetWidth
        val scaleY = pageFormat.heightPt.toFloat() / spec.targetHeight
        val scale = Math.min(scaleX, scaleY)

        val offsetX = (pageFormat.widthPt.toFloat() - spec.targetWidth * scale) / 2f
        val offsetY = (pageFormat.heightPt.toFloat() - spec.targetHeight * scale) / 2f

        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)

        renderToCanvas(canvas, spec, testName, topicName)

        canvas.restore()
        pdfDocument.finishPage(page)

        val outputDir = File(context.cacheDir, "omr_sheets").apply { mkdirs() }
        val outputFile = File(outputDir, "OMR_Sheet_Test_${testId}_${pageFormat.shortName}_${System.currentTimeMillis()}.pdf")

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    /**
     * Prints a generated PDF using Android's built-in PrintManager.
     */
    fun printPdf(context: Context, pdfFile: File, jobName: String = "Marklify OMR Sheet") {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            val printUnavailMsg = context.getString(R.string.printing_unavailable)
            Toast.makeText(context, printUnavailMsg, Toast.LENGTH_SHORT).show()
            return
        }

        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (destination == null) {
                    callback?.onWriteFailed("No output destination file")
                    return
                }
                try {
                    FileInputStream(pdfFile).use { input ->
                        FileOutputStream(destination.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }

        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    }
}

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
     * Renders the OMR sheet onto an Android Canvas using the exact SheetSpec coordinates.
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
            strokeWidth = 2.5f
        }

        val thinStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 34f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val fieldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }

        val instructionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }

        val qNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 16f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // 2. Draw the 4 solid black corner markers
        canvas.drawRect(spec.tlRect.left, spec.tlRect.top, spec.tlRect.right, spec.tlRect.bottom, solidBlackPaint)
        canvas.drawRect(spec.trRect.left, spec.trRect.top, spec.trRect.right, spec.trRect.bottom, solidBlackPaint)
        canvas.drawRect(spec.brRect.left, spec.brRect.top, spec.brRect.right, spec.brRect.bottom, solidBlackPaint)
        canvas.drawRect(spec.blRect.left, spec.blRect.top, spec.blRect.right, spec.blRect.bottom, solidBlackPaint)

        // 3. Subtle outer alignment frame
        canvas.drawRect(
            spec.tlRect.left - 10f,
            spec.tlRect.top - 10f,
            spec.trRect.right + 10f,
            spec.blRect.bottom + 10f,
            thinStrokePaint
        )

        // 4. Header content
        val centerX = spec.targetWidth / 2f
        canvas.drawText("MARKLIFY OMR ANSWER SHEET", centerX, 70f, titlePaint)
        canvas.drawText("$testName  •  $topicName  •  ${spec.questionCount} Questions", centerX, 105f, subtitlePaint)

        // Student Info Box
        val boxLeft = 140f
        val boxRight = spec.targetWidth - 140f
        val boxTop = 125f
        val boxBottom = 220f
        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, thinStrokePaint)

        canvas.drawText("STUDENT NAME:", boxLeft + 20f, 160f, fieldPaint)
        canvas.drawLine(boxLeft + 165f, 160f, centerX - 20f, 160f, thinStrokePaint)

        canvas.drawText("STUDENT ID:", centerX + 20f, 160f, fieldPaint)
        canvas.drawLine(centerX + 140f, 160f, boxRight - 20f, 160f, thinStrokePaint)

        canvas.drawText("DATE: __________________", boxLeft + 20f, 200f, fieldPaint)
        canvas.drawText("SCORE: ________ / ${spec.questionCount}", centerX + 20f, 200f, fieldPaint)

        // Instruction line
        canvas.drawText(
            "INSTRUCTIONS: Completely fill the bubble with dark pen/pencil. Correct: [●]  Incorrect: [✘] [✔] [—]",
            centerX,
            250f,
            instructionPaint
        )

        // 5. Draw Column headers & Bubble items
        if (spec.isTwoColumns) {
            // Divider between columns
            canvas.drawLine(centerX, 270f, centerX, 1530f, thinStrokePaint)
        }

        // Draw questions and bubbles
        for (q in 1..spec.questionCount) {
            val bubbles = spec.getBubblesForQuestion(q)
            val firstBubble = bubbles[0]
            val centerY = firstBubble.centerY

            // Draw Question Number
            val qText = String.format("%02d.", q)
            val textBounds = Rect()
            qNumPaint.getTextBounds(qText, 0, qText.length, textBounds)
            canvas.drawText(qText, firstBubble.centerX - 35f, centerY + textBounds.height() / 2f, qNumPaint)

            // Draw Bubbles A, B, C, D
            for (bubble in bubbles) {
                // Outer circle stroke
                canvas.drawCircle(bubble.centerX, bubble.centerY, bubble.radius, strokeBlackPaint)

                // Option letter centered inside circle
                val lBounds = Rect()
                letterPaint.getTextBounds(bubble.optionLetter, 0, 1, lBounds)
                canvas.drawText(
                    bubble.optionLetter,
                    bubble.centerX,
                    bubble.centerY + lBounds.height() / 2f - 1f,
                    letterPaint
                )
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

         // Page dimensions in PostScript points (72 points/inch)
         val pageInfo = PdfDocument.PageInfo.Builder(pageFormat.widthPt, pageFormat.heightPt, 1).create()
         val page = pdfDocument.startPage(pageInfo)

         val canvas = page.canvas
         // Scale canvas to fit 1200x1600 spec into target page format
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
            Toast.makeText(context, "Printing is not available on this device", Toast.LENGTH_SHORT).show()
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

package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.entity.DetectedAnswer
import com.example.data.entity.Question
import com.example.data.entity.ScanResult
import com.example.data.entity.TestEntity
import com.example.data.entity.Topic
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupImportData(
    val topics: List<Topic>,
    val tests: List<TestEntity>,
    val questions: List<Question>,
    val scanResults: List<ScanResult>,
    val detectedAnswers: List<DetectedAnswer>
)

data class BackupImportStats(
    val topicCount: Int,
    val testCount: Int,
    val questionCount: Int,
    val scanCount: Int
)

object DataBackupManager {

    /**
     * Exports all database entities into a JSON file and returns the generated File.
     */
    fun createBackupJsonFile(
        context: Context,
        topics: List<Topic>,
        tests: List<TestEntity>,
        questions: List<Question>,
        scanResults: List<ScanResult>,
        detectedAnswers: List<DetectedAnswer>
    ): File {
        val root = JSONObject()
        root.put("app", "Marklify")
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())

        // Topics
        val topicsArray = JSONArray()
        for (topic in topics) {
            val obj = JSONObject()
            obj.put("id", topic.id)
            obj.put("name", topic.name)
            obj.put("createdAt", topic.createdAt)
            topicsArray.put(obj)
        }
        root.put("topics", topicsArray)

        // Tests
        val testsArray = JSONArray()
        for (test in tests) {
            val obj = JSONObject()
            obj.put("id", test.id)
            obj.put("topicId", test.topicId)
            obj.put("name", test.name)
            obj.put("questionCount", test.questionCount)
            obj.put("createdAt", test.createdAt)
            testsArray.put(obj)
        }
        root.put("tests", testsArray)

        // Questions
        val questionsArray = JSONArray()
        for (q in questions) {
            val obj = JSONObject()
            obj.put("id", q.id)
            obj.put("testId", q.testId)
            obj.put("questionNumber", q.questionNumber)
            obj.put("questionText", q.questionText)
            obj.put("optionA", q.optionA)
            obj.put("optionB", q.optionB)
            obj.put("optionC", q.optionC)
            obj.put("optionD", q.optionD)
            obj.put("correctAnswer", q.correctAnswer)
            questionsArray.put(obj)
        }
        root.put("questions", questionsArray)

        // Scan Results
        val scansArray = JSONArray()
        for (scan in scanResults) {
            val obj = JSONObject()
            obj.put("id", scan.id)
            obj.put("testId", scan.testId)
            obj.put("studentName", scan.studentName)
            obj.put("studentId", scan.studentId)
            obj.put("scanTime", scan.scanTime)
            obj.put("totalQuestions", scan.totalQuestions)
            obj.put("correct", scan.correct)
            obj.put("wrong", scan.wrong)
            obj.put("unanswered", scan.unanswered)
            obj.put("multipleMarked", scan.multipleMarked)
            obj.put("ambiguous", scan.ambiguous)
            obj.put("percentage", scan.percentage.toDouble())
            scansArray.put(obj)
        }
        root.put("scanResults", scansArray)

        // Detected Answers
        val answersArray = JSONArray()
        for (ans in detectedAnswers) {
            val obj = JSONObject()
            obj.put("id", ans.id)
            obj.put("scanResultId", ans.scanResultId)
            obj.put("questionNumber", ans.questionNumber)
            obj.put("detectedAnswer", ans.detectedAnswer)
            obj.put("isCorrect", ans.isCorrect)
            answersArray.put(obj)
        }
        root.put("detectedAnswers", answersArray)

        val dir = File(context.cacheDir, "backups").apply { mkdirs() }
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "marklify_backup_$dateStr.json")
        FileOutputStream(file).use { out ->
            out.write(root.toString(2).toByteArray(Charsets.UTF_8))
        }
        return file
    }

    /**
     * Parses a backup JSON string into strongly-typed entity lists.
     */
    fun parseBackupJson(jsonString: String): BackupImportData {
        val root = JSONObject(jsonString)

        val topics = mutableListOf<Topic>()
        val topicsArray = root.optJSONArray("topics") ?: JSONArray()
        for (i in 0 until topicsArray.length()) {
            val obj = topicsArray.getJSONObject(i)
            topics.add(
                Topic(
                    id = obj.optLong("id", 0),
                    name = obj.getString("name"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        val tests = mutableListOf<TestEntity>()
        val testsArray = root.optJSONArray("tests") ?: JSONArray()
        for (i in 0 until testsArray.length()) {
            val obj = testsArray.getJSONObject(i)
            tests.add(
                TestEntity(
                    id = obj.optLong("id", 0),
                    topicId = obj.getLong("topicId"),
                    name = obj.getString("name"),
                    questionCount = obj.optInt("questionCount", 10),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        val questions = mutableListOf<Question>()
        val questionsArray = root.optJSONArray("questions") ?: JSONArray()
        for (i in 0 until questionsArray.length()) {
            val obj = questionsArray.getJSONObject(i)
            questions.add(
                Question(
                    id = obj.optLong("id", 0),
                    testId = obj.getLong("testId"),
                    questionNumber = obj.getInt("questionNumber"),
                    questionText = obj.optString("questionText", "Question ${obj.getInt("questionNumber")}"),
                    optionA = obj.optString("optionA", "Option A"),
                    optionB = obj.optString("optionB", "Option B"),
                    optionC = obj.optString("optionC", "Option C"),
                    optionD = obj.optString("optionD", "Option D"),
                    correctAnswer = obj.optString("correctAnswer", "A")
                )
            )
        }

        val scans = mutableListOf<ScanResult>()
        val scansArray = root.optJSONArray("scanResults") ?: JSONArray()
        for (i in 0 until scansArray.length()) {
            val obj = scansArray.getJSONObject(i)
            scans.add(
                ScanResult(
                    id = obj.optLong("id", 0),
                    testId = obj.getLong("testId"),
                    studentName = obj.optString("studentName", "Student"),
                    studentId = obj.optString("studentId", "N/A"),
                    scanTime = obj.optLong("scanTime", System.currentTimeMillis()),
                    totalQuestions = obj.optInt("totalQuestions", 0),
                    correct = obj.optInt("correct", 0),
                    wrong = obj.optInt("wrong", 0),
                    unanswered = obj.optInt("unanswered", 0),
                    multipleMarked = obj.optInt("multipleMarked", 0),
                    ambiguous = obj.optInt("ambiguous", 0),
                    percentage = obj.optDouble("percentage", 0.0).toFloat(),
                    imagePath = null
                )
            )
        }

        val answers = mutableListOf<DetectedAnswer>()
        val answersArray = root.optJSONArray("detectedAnswers") ?: JSONArray()
        for (i in 0 until answersArray.length()) {
            val obj = answersArray.getJSONObject(i)
            answers.add(
                DetectedAnswer(
                    id = obj.optLong("id", 0),
                    scanResultId = obj.getLong("scanResultId"),
                    questionNumber = obj.getInt("questionNumber"),
                    detectedAnswer = obj.optString("detectedAnswer", "unanswered"),
                    isCorrect = obj.optBoolean("isCorrect", false)
                )
            )
        }

        return BackupImportData(topics, tests, questions, scans, answers)
    }

    /**
     * Exports test scan results into CSV format for spreadsheets (Excel / Google Sheets).
     */
    fun createCsvResultsFile(
        context: Context,
        testName: String,
        results: List<ScanResult>
    ): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val sanitizedTestName = testName.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "marklify_${sanitizedTestName}_results_$dateStr.csv")

        val sb = StringBuilder()
        // Header
        sb.append("Student Name,Student ID,Total Questions,Correct,Wrong,Unanswered,Multiple Marked,Ambiguous,Score Percentage,Scan Date\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        for (r in results) {
            // Escape CSV strings
            val name = escapeCsv(r.studentName)
            val sid = escapeCsv(r.studentId)
            val scanDate = dateFormat.format(Date(r.scanTime))
            val percent = String.format(Locale.US, "%.1f%%", r.percentage)

            sb.append("$name,$sid,${r.totalQuestions},${r.correct},${r.wrong},${r.unanswered},${r.multipleMarked},${r.ambiguous},$percent,$scanDate\n")
        }

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun createAllGradedTestsCsvFile(
        context: Context,
        records: List<com.example.data.entity.GradedTestRecord>
    ): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "marklify_graded_tests_history_$dateStr.csv")

        val sb = StringBuilder()
        sb.append("Student Name,Student ID,Test Name,Final Score,Total Questions,Correct,Wrong,Unanswered,Multiple Marked,Score Percentage,Scan Date\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        for (r in records) {
            val name = escapeCsv(r.studentName)
            val sid = escapeCsv(r.studentId)
            val testName = escapeCsv(r.testName)
            val scanDate = dateFormat.format(Date(r.scanTime))
            val percent = String.format(Locale.US, "%.1f%%", r.percentage)

            sb.append("$name,$sid,$testName,${r.finalScore},${r.totalQuestions},${r.correct},${r.wrong},${r.unanswered},${r.multipleMarked},$percent,$scanDate\n")
        }

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }
        return file
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    /**
     * Shares any file using FileProvider.
     */
    fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

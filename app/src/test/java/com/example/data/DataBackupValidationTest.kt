package com.example.data

import com.example.data.backup.DataBackupManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DataBackupValidationTest {

    @Test
    fun testParseValidBackupJson() {
        val json = """
        {
          "app": "Marklify",
          "version": 2,
          "exportedAt": 1700000000000,
          "topics": [
            { "id": 1, "name": "Biology 101", "createdAt": 1700000000000 }
          ],
          "tests": [
            { "id": 10, "topicId": 1, "name": "Cellular Respiration", "questionCount": 20, "createdAt": 1700000000000 }
          ],
          "questions": [
            {
              "id": 100,
              "testId": 10,
              "questionNumber": 1,
              "questionText": "What is ATP?",
              "optionA": "Energy carrier",
              "optionB": "Protein",
              "optionC": "Lipid",
              "optionD": "Carbohydrate",
              "correctAnswer": "A"
            }
          ],
          "scanResults": [
            {
              "id": 500,
              "testId": 10,
              "studentName": "John Doe",
              "studentId": "STU001",
              "scanTime": 1700000005000,
              "totalQuestions": 20,
              "correct": 18,
              "wrong": 2,
              "unanswered": 0,
              "multipleMarked": 0,
              "ambiguous": 0,
              "percentage": 90.0,
              "finalScore": 18.0,
              "imagePath": null
            }
          ],
          "detectedAnswers": [
            {
              "id": 1000,
              "scanResultId": 500,
              "questionNumber": 1,
              "detectedAnswer": "A",
              "isCorrect": true
            }
          ]
        }
        """.trimIndent()

        val data = DataBackupManager.parseBackupJson(json)
        assertEquals(1, data.topics.size)
        assertEquals("Biology 101", data.topics[0].name)
        assertEquals(1, data.tests.size)
        assertEquals("Cellular Respiration", data.tests[0].name)
        assertEquals(20, data.tests[0].questionCount)
        assertEquals(1, data.questions.size)
        assertEquals("A", data.questions[0].correctAnswer)
        assertEquals(1, data.scanResults.size)
        assertEquals("John Doe", data.scanResults[0].studentName)
        assertEquals(90.0f, data.scanResults[0].percentage, 0.001f)
        assertEquals(1, data.detectedAnswers.size)
        assertTrue(data.detectedAnswers[0].isCorrect)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseEmptyJsonThrows() {
        DataBackupManager.parseBackupJson("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseInvalidAppIdentifierThrows() {
        val json = """{ "app": "OtherApp", "version": 1 }"""
        DataBackupManager.parseBackupJson(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseMalformedJsonThrows() {
        val json = """{ this is not valid json }"""
        DataBackupManager.parseBackupJson(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseEmptyTopicNameThrows() {
        val json = """
        {
          "app": "Marklify",
          "version": 2,
          "topics": [
            { "id": 1, "name": "   ", "createdAt": 1700000000000 }
          ]
        }
        """.trimIndent()
        DataBackupManager.parseBackupJson(json)
    }

    @Test
    fun testCsvFormulaInjectionEscaping() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val results = listOf(
            com.example.data.entity.ScanResult(
                id = 1,
                testId = 10,
                studentName = "=CMD('calc')",
                studentId = "+12345",
                scanTime = 1700000000000,
                totalQuestions = 10,
                correct = 10,
                wrong = 0,
                unanswered = 0,
                multipleMarked = 0,
                ambiguous = 0,
                percentage = 100.0f,
                imagePath = null
            )
        )
        val csvFile = DataBackupManager.createCsvResultsFile(context, "Test1", results)
        val content = csvFile.readText()
        assertTrue("Student name should be escaped", content.contains("'=CMD('calc')"))
        assertTrue("Student ID should be escaped", content.contains("'+12345"))
    }
}

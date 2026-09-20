package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.DetectedAnswer
import com.example.data.entity.ScanResult
import com.example.data.entity.TestEntity
import com.example.data.entity.Topic
import com.example.data.repository.GradedTestRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GradedTestRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: GradedTestRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = GradedTestRepository(db.scanResultDao(), db.detectedAnswerDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testStoreAndRetrieveGradedTestHistory() = runBlocking {
        // 1. Create Topic and Test
        val topicId = db.topicDao().insertTopic(Topic(name = "Science"))
        val testId = db.testDao().insertTest(
            TestEntity(topicId = topicId, name = "Physics Exam", questionCount = 20)
        )

        // 2. Store Graded Test Result for Alice with student name and final score
        val aliceResult = ScanResult(
            testId = testId,
            studentName = "Alice Johnson",
            studentId = "STU-001",
            totalQuestions = 20,
            correct = 18,
            wrong = 2,
            unanswered = 0,
            percentage = 90.0f,
            finalScore = 18.0f
        )
        val aliceId = repository.saveGradedTest(aliceResult)
        assertTrue(aliceId > 0)

        // 3. Store Graded Test Result for Bob with student name and final score
        val bobResult = ScanResult(
            testId = testId,
            studentName = "Bob Smith",
            studentId = "STU-002",
            totalQuestions = 20,
            correct = 15,
            wrong = 4,
            unanswered = 1,
            percentage = 75.0f,
            finalScore = 15.0f
        )
        val bobAnswers = listOf(
            DetectedAnswer(scanResultId = 0, questionNumber = 1, detectedAnswer = "A", isCorrect = true),
            DetectedAnswer(scanResultId = 0, questionNumber = 2, detectedAnswer = "B", isCorrect = false)
        )
        val bobId = repository.saveGradedTestWithAnswers(bobResult, bobAnswers)
        assertTrue(bobId > 0)

        // 4. Retrieve Graded Tests history
        val allGraded = repository.allGradedTests.first()
        assertEquals(2, allGraded.size)

        // Check Alice record
        val fetchedAlice = repository.getGradedTestById(aliceId)
        assertNotNull(fetchedAlice)
        assertEquals("Alice Johnson", fetchedAlice?.studentName)
        assertEquals("STU-001", fetchedAlice?.studentId)
        assertEquals(18.0f, fetchedAlice?.finalScore)
        assertEquals(90.0f, fetchedAlice?.percentage)

        // Check Bob record and joined test name projection
        val records = repository.allGradedTestRecords.first()
        assertEquals(2, records.size)
        val bobRecord = records.find { it.studentName == "Bob Smith" }
        assertNotNull(bobRecord)
        assertEquals("Physics Exam", bobRecord?.testName)
        assertEquals(15.0f, bobRecord?.finalScore)

        // 5. Test search by student name
        val searchAlice = repository.searchGradedTestRecords("alice").first()
        assertEquals(1, searchAlice.size)
        assertEquals("Alice Johnson", searchAlice[0].studentName)

        // 6. Test delete
        repository.deleteGradedTestById(aliceId)
        val remaining = repository.allGradedTests.first()
        assertEquals(1, remaining.size)
        assertEquals("Bob Smith", remaining[0].studentName)
    }
}

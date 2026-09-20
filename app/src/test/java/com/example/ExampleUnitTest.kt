package com.example

import com.example.data.entity.Question
import com.example.omr.processing.AnswerDetector
import com.example.omr.processing.BubbleReading
import com.example.omr.processing.ScoringEngine
import com.example.omr.spec.SheetSpec
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testSheetSpecCalculations() {
    val singleColSpec = SheetSpec(questionCount = 20)
    assertEquals(1, singleColSpec.columnsCount)
    // 20 questions * 4 options (A, B, C, D) = 80 total question bubble coordinates
    assertEquals(80, singleColSpec.getAllQuestionBubbles().size)

    val dualColSpec = SheetSpec(questionCount = 50)
    assertEquals(2, dualColSpec.columnsCount)
    // 50 questions * 4 options = 200 total question bubbles
    assertEquals(200, dualColSpec.getAllQuestionBubbles().size)
  }

  @Test
  fun testSheetSpecLargeQuestionCount300() {
    val largeSpec = SheetSpec(questionCount = 300)
    assertEquals(2, largeSpec.columnsCount)
    val questionBubbles = largeSpec.getAllQuestionBubbles()
    assertEquals(1200, questionBubbles.size) // 300 questions * 4 options = 1200
  }

  @Test
  fun testAnswerDetectorSingleAnswer() {
    val detector = AnswerDetector()
    val map: Map<Int, List<BubbleReading>> = mapOf(
      1 to listOf(
        BubbleReading(1, 0, "A", 0.05f, 5, 100),
        BubbleReading(1, 1, "B", 0.75f, 75, 100),
        BubbleReading(1, 2, "C", 0.04f, 4, 100),
        BubbleReading(1, 3, "D", 0.08f, 8, 100)
      )
    )

    val results = detector.detectAnswers(map)
    assertEquals(1, results.size)
    val q1 = results[0]
    assertEquals(1, q1.questionNumber)
    assertEquals("B", q1.detectedAnswer)
    assertFalse(q1.isUnanswered)
    assertFalse(q1.isMultiple)
    assertFalse(q1.isAmbiguous)
  }

  @Test
  fun testAnswerDetectorMultipleMarked() {
    val detector = AnswerDetector()
    val map: Map<Int, List<BubbleReading>> = mapOf(
      1 to listOf(
        BubbleReading(1, 0, "A", 0.80f, 80, 100),
        BubbleReading(1, 1, "B", 0.78f, 78, 100),
        BubbleReading(1, 2, "C", 0.05f, 5, 100),
        BubbleReading(1, 3, "D", 0.08f, 8, 100)
      )
    )

    val results = detector.detectAnswers(map)
    assertEquals("multiple", results[0].detectedAnswer)
    assertTrue(results[0].isMultiple)
  }

  @Test
  fun testAnswerDetectorUnanswered() {
    val detector = AnswerDetector()
    val map: Map<Int, List<BubbleReading>> = mapOf(
      1 to listOf(
        BubbleReading(1, 0, "A", 0.05f, 5, 100),
        BubbleReading(1, 1, "B", 0.08f, 8, 100),
        BubbleReading(1, 2, "C", 0.04f, 4, 100),
        BubbleReading(1, 3, "D", 0.06f, 6, 100)
      )
    )

    val results = detector.detectAnswers(map)
    assertEquals("unanswered", results[0].detectedAnswer)
    assertTrue(results[0].isUnanswered)
  }

  @Test
  fun testScoringEngine() {
    val engine = ScoringEngine()
    val questions = listOf(
      Question(testId = 1, questionNumber = 1, questionText = "Q1", optionA = "A", optionB = "B", optionC = "C", optionD = "D", correctAnswer = "A"),
      Question(testId = 1, questionNumber = 2, questionText = "Q2", optionA = "A", optionB = "B", optionC = "C", optionD = "D", correctAnswer = "B"),
      Question(testId = 1, questionNumber = 3, questionText = "Q3", optionA = "A", optionB = "B", optionC = "C", optionD = "D", correctAnswer = "C"),
      Question(testId = 1, questionNumber = 4, questionText = "Q4", optionA = "A", optionB = "B", optionC = "C", optionD = "D", correctAnswer = "D")
    )

    val answers = mapOf(
      1 to "A", // Correct
      2 to "C", // Wrong
      3 to "unanswered", // Unanswered
      4 to "D"  // Correct
    )

    val score = engine.score(answers, questions)
    assertEquals(4, score.totalQuestions)
    assertEquals(2, score.correct)
    assertEquals(1, score.wrong)
    assertEquals(1, score.unanswered)
    assertEquals(0, score.multipleMarked)
    assertEquals(50f, score.percentage, 0.01f)

    // Test with multiple answers marked
    val answersWithMultiple = mapOf(
      1 to "A",
      2 to "multiple",
      3 to "unanswered",
      4 to "D"
    )
    val score2 = engine.score(answersWithMultiple, questions)
    assertEquals(1, score2.multipleMarked)
    assertEquals(0, score2.wrong)
  }
}

package com.example.ui.navigation

object NavRoutes {
    const val DASHBOARD = "dashboard"
    const val TOPICS = "topics"
    const val TOPIC_DETAIL = "topic_detail/{topicId}"
    const val TEST_DETAIL = "test_detail/{testId}"
    const val TEST_EDITOR = "test_editor/{testId}"
    const val SHEET_GENERATOR = "sheet_generator/{testId}"
    const val SCANNER = "scanner/{testId}"
    const val REVIEW = "review"
    const val RESULTS = "results/{testId}"
    const val RESULT_DETAIL = "result_detail/{resultId}"
    const val SETTINGS = "settings"
    const val HISTORY = "history"

    fun topicDetail(topicId: Long) = "topic_detail/$topicId"
    fun testDetail(testId: Long) = "test_detail/$testId"
    fun testEditor(testId: Long) = "test_editor/$testId"
    fun sheetGenerator(testId: Long) = "sheet_generator/$testId"
    fun scanner(testId: Long) = "scanner/$testId"
    fun results(testId: Long) = "results/$testId"
    fun resultDetail(resultId: Long) = "result_detail/$resultId"
}

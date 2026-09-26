## 2024-09-26 - [Room Database Batch Optimization]
**Learning:** Room DB operations inside loops without explicit batching trigger individual SQLite transactions. This is particularly problematic in MarklifyViewModel during question list modifications.
**Action:** Always prefer collection-based DAO methods (`@Update suspend fun updateQuestions(questions: List<Question>)`) over individual operations to minimize SQLite lock overhead and I/O.

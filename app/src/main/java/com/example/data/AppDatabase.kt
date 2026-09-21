package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.entity.*

@Database(
    entities = [
        Topic::class,
        TestEntity::class,
        Question::class,
        ScanResult::class,
        DetectedAnswer::class,
        AppSettings::class,
        AnswerKeySet::class,
        Subject::class,
        SectionConfigEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun testDao(): TestDao
    abstract fun questionDao(): QuestionDao
    abstract fun scanResultDao(): ScanResultDao
    abstract fun detectedAnswerDao(): DetectedAnswerDao
    abstract fun settingsDao(): SettingsDao
    abstract fun answerKeySetDao(): AnswerKeySetDao
    abstract fun subjectDao(): SubjectDao
    abstract fun sectionConfigDao(): SectionConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_settings ADD COLUMN appLanguage TEXT NOT NULL DEFAULT 'en'")
                db.execSQL("ALTER TABLE app_settings ADD COLUMN hapticsEnabled INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // TestEntity additions
                db.execSQL("ALTER TABLE tests ADD COLUMN examType TEXT NOT NULL DEFAULT 'NEET'")
                db.execSQL("ALTER TABLE tests ADD COLUMN isPublic INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tests ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tests ADD COLUMN examDate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tests ADD COLUMN description TEXT NOT NULL DEFAULT ''")

                // Question additions
                db.execSQL("ALTER TABLE questions ADD COLUMN sectionName TEXT NOT NULL DEFAULT 'Section1'")
                db.execSQL("ALTER TABLE questions ADD COLUMN questionType TEXT NOT NULL DEFAULT 'MCQ4'")
                db.execSQL("ALTER TABLE questions ADD COLUMN correctMarks REAL NOT NULL DEFAULT 1.0")
                db.execSQL("ALTER TABLE questions ADD COLUMN negativeMarks REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE questions ADD COLUMN allowPartial INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN allowOptional INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN numDigits INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE questions ADD COLUMN hasNegativeSign INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN hasDecimal INTEGER NOT NULL DEFAULT 0")

                // AppSettings additions
                db.execSQL("ALTER TABLE app_settings ADD COLUMN themeMode TEXT NOT NULL DEFAULT 'SYSTEM'")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. AnswerKeySets table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS answer_key_sets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        testId INTEGER NOT NULL,
                        setLetter TEXT NOT NULL,
                        questionNumber INTEGER NOT NULL,
                        correctAnswer TEXT NOT NULL,
                        FOREIGN KEY(testId) REFERENCES tests(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_answer_key_sets_testId ON answer_key_sets(testId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_answer_key_sets_testId_setLetter_questionNumber ON answer_key_sets(testId, setLetter, questionNumber)")

                // 2. ScanResult examSet column
                db.execSQL("ALTER TABLE scan_results ADD COLUMN examSet TEXT NOT NULL DEFAULT 'A'")

                // 3. Subjects table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS subjects (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        testId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        orderIndex INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(testId) REFERENCES tests(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subjects_testId ON subjects(testId)")

                // 4. SectionConfigs table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS section_configs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        subjectId INTEGER NOT NULL,
                        sectionName TEXT NOT NULL DEFAULT 'Section1',
                        questionType TEXT NOT NULL DEFAULT 'MCQ4',
                        questionCount INTEGER NOT NULL DEFAULT 10,
                        correctMarks REAL NOT NULL DEFAULT 1.0,
                        negativeMarks REAL NOT NULL DEFAULT 0.0,
                        allowPartialMarks INTEGER NOT NULL DEFAULT 0,
                        allowOptionalAttempts INTEGER NOT NULL DEFAULT 0,
                        numDigits INTEGER NOT NULL DEFAULT 1,
                        hasNegativeSign INTEGER NOT NULL DEFAULT 0,
                        hasDecimal INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(subjectId) REFERENCES subjects(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_section_configs_subjectId ON section_configs(subjectId)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "marklify_database"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

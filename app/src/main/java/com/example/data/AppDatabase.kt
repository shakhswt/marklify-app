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
        SectionConfigEntity::class,
        ClassEntity::class,
        StudentEntity::class,
        AttendanceRecord::class
    ],
    version = 8,
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
    abstract fun classDao(): ClassDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private fun SupportSQLiteDatabase.addColumnIfMissing(table: String, columnDef: String, columnName: String) {
            var exists = false
            try {
                val cursor = query("PRAGMA table_info($table)")
                cursor?.use {
                    val nameIndex = it.getColumnIndex("name")
                    if (nameIndex >= 0) {
                        while (it.moveToNext()) {
                            if (it.getString(nameIndex) == columnName) {
                                exists = true
                                break
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                exists = false
            }
            if (!exists) {
                execSQL("ALTER TABLE $table ADD COLUMN $columnDef")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.addColumnIfMissing("app_settings", "appLanguage TEXT NOT NULL DEFAULT 'en'", "appLanguage")
                db.addColumnIfMissing("app_settings", "hapticsEnabled INTEGER NOT NULL DEFAULT 1", "hapticsEnabled")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // TestEntity additions
                db.addColumnIfMissing("tests", "examType TEXT NOT NULL DEFAULT 'NEET'", "examType")
                db.addColumnIfMissing("tests", "isPublic INTEGER NOT NULL DEFAULT 0", "isPublic")
                db.addColumnIfMissing("tests", "isArchived INTEGER NOT NULL DEFAULT 0", "isArchived")
                db.addColumnIfMissing("tests", "examDate INTEGER NOT NULL DEFAULT 0", "examDate")
                db.addColumnIfMissing("tests", "description TEXT NOT NULL DEFAULT ''", "description")

                // Question additions
                db.addColumnIfMissing("questions", "sectionName TEXT NOT NULL DEFAULT 'Section1'", "sectionName")
                db.addColumnIfMissing("questions", "questionType TEXT NOT NULL DEFAULT 'MCQ4'", "questionType")
                db.addColumnIfMissing("questions", "correctMarks REAL NOT NULL DEFAULT 1.0", "correctMarks")
                db.addColumnIfMissing("questions", "negativeMarks REAL NOT NULL DEFAULT 0.0", "negativeMarks")
                db.addColumnIfMissing("questions", "allowPartial INTEGER NOT NULL DEFAULT 0", "allowPartial")
                db.addColumnIfMissing("questions", "allowOptional INTEGER NOT NULL DEFAULT 0", "allowOptional")
                db.addColumnIfMissing("questions", "numDigits INTEGER NOT NULL DEFAULT 1", "numDigits")
                db.addColumnIfMissing("questions", "hasNegativeSign INTEGER NOT NULL DEFAULT 0", "hasNegativeSign")
                db.addColumnIfMissing("questions", "hasDecimal INTEGER NOT NULL DEFAULT 0", "hasDecimal")

                // AppSettings additions
                db.addColumnIfMissing("app_settings", "themeMode TEXT NOT NULL DEFAULT 'SYSTEM'", "themeMode")
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
                db.addColumnIfMissing("scan_results", "examSet TEXT NOT NULL DEFAULT 'A'", "examSet")

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

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Classes table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS classes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // 2. Students table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS students (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        classId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        rollId TEXT,
                        FOREIGN KEY(classId) REFERENCES classes(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_students_classId ON students(classId)")

                // 3. Attendance Records table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS attendance_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        classId INTEGER NOT NULL,
                        studentId INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        present INTEGER NOT NULL,
                        FOREIGN KEY(classId) REFERENCES classes(id) ON DELETE CASCADE,
                        FOREIGN KEY(studentId) REFERENCES students(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_records_classId ON attendance_records(classId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_records_studentId ON attendance_records(studentId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_attendance_records_classId_studentId_date ON attendance_records(classId, studentId, date)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. AppSettings additions
                db.addColumnIfMissing("app_settings", "soundEnabled INTEGER NOT NULL DEFAULT 1", "soundEnabled")
                db.addColumnIfMissing("app_settings", "saveImagesEnabled INTEGER NOT NULL DEFAULT 1", "saveImagesEnabled")
                db.addColumnIfMissing("app_settings", "autoSaveEnabled INTEGER NOT NULL DEFAULT 0", "autoSaveEnabled")
                db.addColumnIfMissing("app_settings", "autoSaveDelaySeconds INTEGER NOT NULL DEFAULT 3", "autoSaveDelaySeconds")
                db.addColumnIfMissing("app_settings", "scanResolution TEXT NOT NULL DEFAULT 'Default'", "scanResolution")

                // 2. TestEntity additions
                db.addColumnIfMissing("tests", "numRollDigits INTEGER NOT NULL DEFAULT 5", "numRollDigits")
                db.addColumnIfMissing("tests", "numExamSets INTEGER NOT NULL DEFAULT 1", "numExamSets")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "marklify_database"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

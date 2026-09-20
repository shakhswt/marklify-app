package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DetectedAnswerDao
import com.example.data.dao.QuestionDao
import com.example.data.dao.ScanResultDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.TestDao
import com.example.data.dao.TopicDao
import com.example.data.entity.AppSettings
import com.example.data.entity.DetectedAnswer
import com.example.data.entity.Question
import com.example.data.entity.ScanResult
import com.example.data.entity.TestEntity
import com.example.data.entity.Topic

@Database(
    entities = [
        Topic::class,
        TestEntity::class,
        Question::class,
        ScanResult::class,
        DetectedAnswer::class,
        AppSettings::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun testDao(): TestDao
    abstract fun questionDao(): QuestionDao
    abstract fun scanResultDao(): ScanResultDao
    abstract fun detectedAnswerDao(): DetectedAnswerDao
    abstract fun settingsDao(): SettingsDao

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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "marklify_database"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

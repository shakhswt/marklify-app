package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 3,
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "marklify_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

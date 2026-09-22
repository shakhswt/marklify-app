package com.example.data

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.entity.AppSettings
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    @Test
    fun testAppSettingsDefaultValues() {
        val defaultSettings = AppSettings()
        assertEquals("en", defaultSettings.appLanguage)
        assertTrue(defaultSettings.hapticsEnabled)
        assertEquals("SYSTEM", defaultSettings.themeMode)
        assertEquals(0.35f, defaultSettings.fillThreshold, 0.001f)
        assertEquals(0.22f, defaultSettings.unansweredThreshold, 0.001f)
        assertEquals("A4", defaultSettings.defaultPageSize)
        assertTrue(defaultSettings.soundEnabled)
        assertTrue(defaultSettings.saveImagesEnabled)
        assertFalse(defaultSettings.autoSaveEnabled)
        assertEquals(3, defaultSettings.autoSaveDelaySeconds)
        assertEquals("Default", defaultSettings.scanResolution)
    }

    @Test
    fun testMigration3To4ExecutesAlterStatements() {
        val executedQueries = mutableListOf<String>()

        val invocationHandler = InvocationHandler { _, method: Method, args: Array<out Any>? ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedQueries.add(args[0] as String)
            }
            null
        }

        val mockDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
            invocationHandler
        ) as SupportSQLiteDatabase

        AppDatabase.MIGRATION_3_4.migrate(mockDb)

        assertEquals(2, executedQueries.size)
        assertTrue(
            "Expected ALTER TABLE app_settings ADD COLUMN appLanguage",
            executedQueries[0].contains("ALTER TABLE app_settings ADD COLUMN appLanguage")
        )
        assertTrue(
            "Expected ALTER TABLE app_settings ADD COLUMN hapticsEnabled",
            executedQueries[1].contains("ALTER TABLE app_settings ADD COLUMN hapticsEnabled")
        )
        assertEquals(3, AppDatabase.MIGRATION_3_4.startVersion)
        assertEquals(4, AppDatabase.MIGRATION_3_4.endVersion)
    }

    @Test
    fun testMigration4To5ExecutesAlterStatements() {
        val executedQueries = mutableListOf<String>()

        val invocationHandler = InvocationHandler { _, method: Method, args: Array<out Any>? ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedQueries.add(args[0] as String)
            }
            null
        }

        val mockDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
            invocationHandler
        ) as SupportSQLiteDatabase

        AppDatabase.MIGRATION_4_5.migrate(mockDb)

        assertTrue(executedQueries.size >= 15)
        assertTrue(executedQueries.any { it.contains("ALTER TABLE tests ADD COLUMN examType") })
        assertTrue(executedQueries.any { it.contains("ALTER TABLE questions ADD COLUMN questionType") })
        assertTrue(executedQueries.any { it.contains("ALTER TABLE app_settings ADD COLUMN themeMode") })
        assertEquals(4, AppDatabase.MIGRATION_4_5.startVersion)
        assertEquals(5, AppDatabase.MIGRATION_4_5.endVersion)
    }

    @Test
    fun testMigration5To6ExecutesAlterStatements() {
        val executedQueries = mutableListOf<String>()

        val invocationHandler = InvocationHandler { _, method: Method, args: Array<out Any>? ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedQueries.add(args[0] as String)
            }
            null
        }

        val mockDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
            invocationHandler
        ) as SupportSQLiteDatabase

        AppDatabase.MIGRATION_5_6.migrate(mockDb)

        assertTrue(executedQueries.any { it.contains("CREATE TABLE IF NOT EXISTS answer_key_sets") })
        assertTrue(executedQueries.any { it.contains("ALTER TABLE scan_results ADD COLUMN examSet") })
        assertTrue(executedQueries.any { it.contains("CREATE TABLE IF NOT EXISTS subjects") })
        assertTrue(executedQueries.any { it.contains("CREATE TABLE IF NOT EXISTS section_configs") })
        assertEquals(5, AppDatabase.MIGRATION_5_6.startVersion)
        assertEquals(6, AppDatabase.MIGRATION_5_6.endVersion)
    }

    @Test
    fun testMigration6To7ExecutesAlterStatements() {
        val executedQueries = mutableListOf<String>()

        val invocationHandler = InvocationHandler { _, method: Method, args: Array<out Any>? ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedQueries.add(args[0] as String)
            }
            null
        }

        val mockDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
            invocationHandler
        ) as SupportSQLiteDatabase

        AppDatabase.MIGRATION_6_7.migrate(mockDb)

        assertTrue(executedQueries.any { it.contains("CREATE TABLE IF NOT EXISTS classes") })
        assertTrue(executedQueries.any { it.contains("CREATE TABLE IF NOT EXISTS students") })
        assertTrue(executedQueries.any { it.contains("CREATE TABLE IF NOT EXISTS attendance_records") })
        assertEquals(6, AppDatabase.MIGRATION_6_7.startVersion)
        assertEquals(7, AppDatabase.MIGRATION_6_7.endVersion)
    }

    @Test
    fun testMigration7To8ExecutesAlterStatements() {
        val executedQueries = mutableListOf<String>()

        val invocationHandler = InvocationHandler { _, method: Method, args: Array<out Any>? ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedQueries.add(args[0] as String)
            }
            null
        }

        val mockDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
            invocationHandler
        ) as SupportSQLiteDatabase

        AppDatabase.MIGRATION_7_8.migrate(mockDb)

        assertTrue(executedQueries.any { it.contains("ALTER TABLE app_settings ADD COLUMN soundEnabled") })
        assertTrue(executedQueries.any { it.contains("ALTER TABLE app_settings ADD COLUMN saveImagesEnabled") })
        assertTrue(executedQueries.any { it.contains("ALTER TABLE app_settings ADD COLUMN autoSaveEnabled") })
        assertTrue(executedQueries.any { it.contains("ALTER TABLE tests ADD COLUMN numRollDigits") })
        assertEquals(7, AppDatabase.MIGRATION_7_8.startVersion)
        assertEquals(8, AppDatabase.MIGRATION_7_8.endVersion)
    }

    @Test
    fun testFullMigrationChainIdempotent() {
        val helperFactory = FrameworkSQLiteOpenHelperFactory()
        val config = SupportSQLiteOpenHelper.Configuration.builder(
            ApplicationProvider.getApplicationContext()
        )
            .name(null) // in-memory database
            .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE app_settings (id INTEGER PRIMARY KEY NOT NULL, fillThreshold REAL NOT NULL DEFAULT 0.35, unansweredThreshold REAL NOT NULL DEFAULT 0.22, multipleDiffMargin REAL NOT NULL DEFAULT 0.15, ambiguousDiffMargin REAL NOT NULL DEFAULT 0.20, defaultPageSize TEXT NOT NULL DEFAULT 'A4')")
                    db.execSQL("CREATE TABLE tests (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, topicId INTEGER NOT NULL, name TEXT NOT NULL, questionCount INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE questions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, testId INTEGER NOT NULL, questionNumber INTEGER NOT NULL, questionText TEXT NOT NULL, optionA TEXT NOT NULL, optionB TEXT NOT NULL, optionC TEXT NOT NULL, optionD TEXT NOT NULL, correctAnswer TEXT NOT NULL)")
                    db.execSQL("CREATE TABLE scan_results (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, testId INTEGER NOT NULL, studentName TEXT NOT NULL, studentId TEXT NOT NULL, scanTime INTEGER NOT NULL, totalQuestions INTEGER NOT NULL, correct INTEGER NOT NULL, wrong INTEGER NOT NULL, unanswered INTEGER NOT NULL, multipleMarked INTEGER NOT NULL DEFAULT 0, ambiguous INTEGER NOT NULL DEFAULT 0, percentage REAL NOT NULL, finalScore REAL NOT NULL DEFAULT 0, imagePath TEXT)")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = helperFactory.create(config)
        val db = helper.writableDatabase

        // Execute migrations 3->4, 4->5, 5->6, 6->7, 7->8
        AppDatabase.MIGRATION_3_4.migrate(db)
        AppDatabase.MIGRATION_4_5.migrate(db)
        AppDatabase.MIGRATION_5_6.migrate(db)
        AppDatabase.MIGRATION_6_7.migrate(db)
        AppDatabase.MIGRATION_7_8.migrate(db)

        // Execute all migrations AGAIN (simulating an already-migrated or partially-migrated database)
        AppDatabase.MIGRATION_3_4.migrate(db)
        AppDatabase.MIGRATION_4_5.migrate(db)
        AppDatabase.MIGRATION_5_6.migrate(db)
        AppDatabase.MIGRATION_6_7.migrate(db)
        AppDatabase.MIGRATION_7_8.migrate(db)

        // Verify that no exceptions occurred and columns exist
        val cursor = db.query("PRAGMA table_info(app_settings)")
        val columns = mutableListOf<String>()
        cursor.use {
            val nameIdx = it.getColumnIndex("name")
            while (it.moveToNext()) {
                columns.add(it.getString(nameIdx))
            }
        }
        assertTrue(columns.contains("soundEnabled"))
        assertTrue(columns.contains("saveImagesEnabled"))
        assertTrue(columns.contains("autoSaveEnabled"))
        assertTrue(columns.contains("autoSaveDelaySeconds"))
        assertTrue(columns.contains("scanResolution"))

        db.close()
    }
}

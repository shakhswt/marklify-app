package com.example.data

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.entity.AppSettings
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

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
}

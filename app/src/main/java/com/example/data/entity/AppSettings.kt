package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val fillThreshold: Float = 0.35f,
    val unansweredThreshold: Float = 0.22f,
    val multipleDiffMargin: Float = 0.15f,
    val ambiguousDiffMargin: Float = 0.20f,
    val defaultPageSize: String = "A4",
    val appLanguage: String = "en",
    val hapticsEnabled: Boolean = true
)

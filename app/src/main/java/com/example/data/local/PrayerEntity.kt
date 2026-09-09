package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_cache")
data class PrayerEntity(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val fajrMillis: Long,
    val dhuhrMillis: Long,
    val asrMillis: Long, // Hanafi calculation
    val asrShafiiMillis: Long, // Shafi'i calculation
    val maghribMillis: Long,
    val ishaMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val calculationMethod: String,
    val cachedAt: Long = System.currentTimeMillis()
)

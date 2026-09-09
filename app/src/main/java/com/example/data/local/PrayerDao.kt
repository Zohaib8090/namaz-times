package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_cache WHERE date = :date LIMIT 1")
    fun getPrayerByDate(date: String): Flow<PrayerEntity?>

    @Query("SELECT * FROM prayer_cache WHERE date = :date LIMIT 1")
    suspend fun getPrayerByDateSync(date: String): PrayerEntity?

    @Query("SELECT * FROM prayer_cache WHERE date >= :startDate ORDER BY date ASC LIMIT :limit")
    fun getUpcomingPrayers(startDate: String, limit: Int = 7): Flow<List<PrayerEntity>>

    @Query("SELECT * FROM prayer_cache WHERE date >= :startDate ORDER BY date ASC LIMIT :limit")
    suspend fun getUpcomingPrayersSync(startDate: String, limit: Int = 7): List<PrayerEntity>

    @Query("SELECT * FROM prayer_cache ORDER BY date ASC")
    fun getAllCachedPrayers(): Flow<List<PrayerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayer(prayer: PrayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayers(prayers: List<PrayerEntity>)

    @Query("DELETE FROM prayer_cache WHERE date < :cutoffDate")
    suspend fun deleteOldPrayers(cutoffDate: String)
}

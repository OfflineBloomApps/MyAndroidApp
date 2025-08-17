package com.example.habitbloombasic.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for habits and checkmarks. In addition to the existing
 * queries for observing habits and performing CRUD operations, this DAO
 * exposes two new streams:
 *
 *  - [observeDistinctDays] returns the number of unique days on which at
 *    least one habit was marked. This value drives the XP/level calculation.
 *  - [observeLastMarkDate] returns the epoch day of the most recent
 *    checkmark across all habits. It allows the view model to detect
 *    skipped days and gently degrade the flower's progress if a user
 *    hasn't checked off any habit recently.
 */
@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun observeHabits(): Flow<List<HabitEntity>>

    /**
     * Count the number of distinct days on which at least one habit has been
     * checked. Each unique date contributes one XP point to the flower.
     */
    @Query("SELECT COUNT(DISTINCT dateEpoch) FROM checkmarks")
    fun observeDistinctDays(): Flow<Int>

    /**
     * Return the epoch day of the latest checkmark across all habits.
     * This value is nullable because there may be no checkmarks yet.
     */
    @Query("SELECT MAX(dateEpoch) FROM checkmarks")
    fun observeLastMarkDate(): Flow<Long?>

    @Insert
    suspend fun insertHabit(h: HabitEntity): Long

    @Delete
    suspend fun deleteHabit(h: HabitEntity)

    @Query("SELECT * FROM checkmarks WHERE habitId = :habitId ORDER BY dateEpoch DESC")
    suspend fun getCheckmarks(habitId: Int): List<CheckmarkEntity>

    @Query("SELECT * FROM checkmarks WHERE habitId = :habitId AND dateEpoch = :dateEpoch LIMIT 1")
    suspend fun getCheckmarkForDate(habitId: Int, dateEpoch: Long): CheckmarkEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheckmark(m: CheckmarkEntity)

    @Query("DELETE FROM checkmarks WHERE habitId = :habitId AND dateEpoch = :dateEpoch")
    suspend fun deleteCheckmark(habitId: Int, dateEpoch: Long)
}
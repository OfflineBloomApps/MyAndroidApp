package com.example.habitbloombasic.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с привычками и отметками выполнения.
 * Добавлены потоки:
 * - observeDistinctDays: число уникальных дней, когда отмечена хотя бы одна привычка (XP).
 * - observeLastMarkDate: дата последней отметки (для определения пропусков).
 */
@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT COUNT(DISTINCT dateEpoch) FROM checkmarks")
    fun observeDistinctDays(): Flow<Int>

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

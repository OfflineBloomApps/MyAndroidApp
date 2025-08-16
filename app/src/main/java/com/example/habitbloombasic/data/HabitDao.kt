package com.example.habitbloombasic.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun observeHabits(): Flow<List<HabitEntity>>
    @Insert suspend fun insertHabit(h: HabitEntity): Long
    @Delete suspend fun deleteHabit(h: HabitEntity)
    @Query("SELECT * FROM checkmarks WHERE habitId = :habitId ORDER BY dateEpoch DESC")
    suspend fun getCheckmarks(habitId: Int): List<CheckmarkEntity>
    @Query("SELECT * FROM checkmarks WHERE habitId = :habitId AND dateEpoch = :dateEpoch LIMIT 1")
    suspend fun getCheckmarkForDate(habitId: Int, dateEpoch: Long): CheckmarkEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertCheckmark(m: CheckmarkEntity)
    @Query("DELETE FROM checkmarks WHERE habitId = :habitId AND dateEpoch = :dateEpoch")
    suspend fun deleteCheckmark(habitId: Int, dateEpoch: Long)
}
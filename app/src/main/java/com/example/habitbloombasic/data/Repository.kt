package com.example.habitbloombasic.data

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/**
 * Repository acts as a thin abstraction over [HabitDao]. It exposes
 * streams of habits as well as distinct-day counts and the most
 * recently recorded checkmark. These flows allow the view model to
 * compute XP, levels and progress for the flower. All heavy
 * computations are deferred to the UI layer, keeping this class
 * focused on simple data access.
 */
data class HabitUi(val id: Int, val title: String, val checkedToday: Boolean, val streak: Int)

class Repository(private val dao: HabitDao) {
    /** Observe the list of habits. */
    fun flow(): Flow<List<HabitEntity>> = dao.observeHabits()

    /** Observe the count of unique days on which at least one habit was marked. */
    fun distinctDaysFlow(): Flow<Int> = dao.observeDistinctDays()

    /** Observe the epoch day of the most recent checkmark across all habits. */
    fun lastMarkDateFlow(): Flow<Long?> = dao.observeLastMarkDate()

    suspend fun add(title: String) {
        dao.insertHabit(HabitEntity(title = title.trim()))
    }

    suspend fun del(id: Int, title: String) {
        dao.deleteHabit(HabitEntity(id = id, title = title))
    }

    /**
     * Toggle today's mark for the given habit. If the user hasn't
     * checked the habit today, this call inserts a new checkmark; if
     * it is already checked, the mark is removed.
     */
    suspend fun toggleToday(id: Int) {
        val todayEpoch = LocalDate.now().toEpochDay()
        val existing = dao.getCheckmarkForDate(id, todayEpoch)
        if (existing == null) {
            dao.insertCheckmark(CheckmarkEntity(habitId = id, dateEpoch = todayEpoch))
        } else {
            dao.deleteCheckmark(id, todayEpoch)
        }
    }

    /**
     * Convert the list of [HabitEntity] into a list of [HabitUi]. Each
     * UI model holds whether the habit has been completed today and
     * its current streak. The streak is computed by counting
     * consecutive days starting from today and moving backwards. Only
     * days for which the habit was checked contribute to the streak.
     */
    suspend fun toUi(list: List<HabitEntity>): List<HabitUi> {
        val today = LocalDate.now().toEpochDay()
        return list.map { habit ->
            val marks = dao.getCheckmarks(habit.id)
            val checked = marks.any { it.dateEpoch == today }
            val streak = calcStreak(marks.map { it.dateEpoch })
            HabitUi(habit.id, habit.title, checked, streak)
        }
    }

    private fun calcStreak(days: List<Long>): Int {
        if (days.isEmpty()) return 0
        val daySet = days.toSet()
        var currentDay = LocalDate.now().toEpochDay()
        var count = 0
        while (daySet.contains(currentDay)) {
            count += 1
            currentDay -= 1
        }
        return count
    }
}
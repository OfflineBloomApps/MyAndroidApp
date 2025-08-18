package com.example.habitbloombasic.data

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий оборачивает DAO, предоставляет потоки уникальных дней и последней отметки.
 */
data class HabitUi(val id: Int, val title: String, val checkedToday: Boolean, val streak: Int)

class Repository(private val dao: HabitDao) {
    fun flow(): Flow<List<HabitEntity>> = dao.observeHabits()
    fun distinctDaysFlow(): Flow<Int> = dao.observeDistinctDays()
    fun lastMarkDateFlow(): Flow<Long?> = dao.observeLastMarkDate()

    suspend fun add(title: String) {
        dao.insertHabit(HabitEntity(title = title.trim()))
    }

    suspend fun del(id: Int, title: String) {
        dao.deleteHabit(HabitEntity(id = id, title = title))
    }

    suspend fun toggleToday(id: Int) {
        val today = LocalDate.now().toEpochDay()
        val mark = dao.getCheckmarkForDate(id, today)
        if (mark == null) dao.insertCheckmark(CheckmarkEntity(habitId = id, dateEpoch = today))
        else dao.deleteCheckmark(id, today)
    }

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
        val set = days.toSet()
        var day = LocalDate.now().toEpochDay()
        var count = 0
        while (set.contains(day)) { count++; day-- }
        return count
    }
}

package com.example.habitbloombasic.data
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

data class HabitUi(val id: Int, val title: String, val checkedToday: Boolean, val streak: Int)

class Repository(private val dao: HabitDao) {
    fun flow(): Flow<List<HabitEntity>> = dao.observeHabits()
    fun xpFlow(): Flow<Int> = dao.observeXp()

    suspend fun add(title: String) { dao.insertHabit(HabitEntity(title = title.trim())) }
    suspend fun del(id: Int, title: String) = dao.deleteHabit(HabitEntity(id = id, title = title))

    suspend fun toggleToday(id: Int) {
        val t = LocalDate.now().toEpochDay()
        val e = dao.getCheckmarkForDate(id, t)
        if (e == null) dao.insertCheckmark(CheckmarkEntity(habitId = id, dateEpoch = t))
        else dao.deleteCheckmark(id, t)
    }

    suspend fun toUi(list: List<HabitEntity>): List<HabitUi> {
        val today = LocalDate.now().toEpochDay()
        return list.map { h ->
            val marks = dao.getCheckmarks(h.id)
            val checked = marks.any { it.dateEpoch == today }
            val streak = calc(marks.map { it.dateEpoch })
            HabitUi(h.id, h.title, checked, streak)
        }
    }

    private fun calc(days: List<Long>): Int {
        if (days.isEmpty()) return 0
        val set = days.toSet(); var cur = LocalDate.now().toEpochDay(); var s = 0
        while (set.contains(cur)) { s += 1; cur -= 1 }
        return s
    }
}

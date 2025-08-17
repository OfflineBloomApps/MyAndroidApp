package com.example.habitbloombasic.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitbloombasic.data.AppDatabase
import com.example.habitbloombasic.data.HabitUi
import com.example.habitbloombasic.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Состояние экрана. Помимо списка привычек, хранит XP-дни, уровень, прогресс и сообщение.
 */
data class UiState(
    val items: List<HabitUi> = emptyList(),
    val showDialog: Boolean = false,
    val newTitle: String = "",
    val xpDays: Int = 0,
    val level: Int = 1,
    val progress: Float = 0f,
    val message: String? = null
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(AppDatabase.get(app).habitDao())
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    // Внутренние переменные для сравнения с прошлым уровнем/прогрессом
    private var lastLevel = 1
    private var lastProgress = 0f

    init {
        viewModelScope.launch {
            combine(
                repo.flow(),
                repo.distinctDaysFlow(),
                repo.lastMarkDateFlow()
            ) { habits, xpDays, lastDate ->
                val itemsUi = repo.toUi(habits)
                // Уровень = каждый 7 дней, прогресс = остаток / 7
                val level = xpDays / 7 + 1
                val baseProgress = (xpDays % 7) / 7f

                var adjustedProgress = baseProgress
                // Увядание: если пропущено >1 дня, прогресс уменьшаем
                if (lastDate != null) {
                    val today = LocalDate.now().toEpochDay()
                    val diff = (today - lastDate).toInt()
                    if (diff > 1) {
                        val degrade = (diff - 1) * 0.2f
                        adjustedProgress = (baseProgress - degrade).coerceAtLeast(0f)
                    }
                }
                Triple(itemsUi, level to adjustedProgress, xpDays)
            }.collectLatest { (itemsUi, levelProgress, xpDays) ->
                val (level, progress) = levelProgress
                var message: String? = null
                if (level > lastLevel) {
                    message = "Поздравляем! Ваш цветок вырос до $level уровня! Продолжайте следовать привычкам."
                    lastLevel = level
                } else if (progress < lastProgress) {
                    message = "Цветку нужен полив! Вы пропустили несколько дней. Вернитесь к привычкам."
                }
                lastProgress = progress
                _state.value = _state.value.copy(
                    items = itemsUi,
                    xpDays = xpDays,
                    level = level,
                    progress = progress,
                    message = message
                )
            }
        }
    }

    fun addClick() {
        _state.value = _state.value.copy(showDialog = true, newTitle = "")
    }

    fun dismiss() {
        _state.value = _state.value.copy(showDialog = false, newTitle = "")
    }

    fun title(t: String) {
        _state.value = _state.value.copy(newTitle = t)
    }

    fun add() {
        val t = _state.value.newTitle.trim()
        if (t.isEmpty()) return
        viewModelScope.launch {
            repo.add(t)
            _state.value = _state.value.copy(showDialog = false, newTitle = "")
        }
    }

    fun toggle(id: Int) {
        viewModelScope.launch { repo.toggleToday(id) }
    }

    fun delete(id: Int) {
        val title = _state.value.items.firstOrNull { it.id == id }?.title ?: ""
        viewModelScope.launch { repo.del(id, title) }
    }

    /** Сбрасывает текущее сообщение после закрытия диалога */
    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}

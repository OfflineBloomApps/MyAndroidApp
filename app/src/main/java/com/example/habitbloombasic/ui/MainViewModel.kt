package com.example.habitbloombasic.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.first
import androidx.lifecycle.viewModelScope
import com.example.habitbloombasic.data.AppDatabase
import com.example.habitbloombasic.data.HabitUi
import com.example.habitbloombasic.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch  // Добавьте этот import, если нет

data class UiState(
    val habits: List<HabitUi> = emptyList(),
    val showAddDialog: Boolean = false,
    val newTitle: String = "",
    val message: String? = null,
    val xp: Int = 0,
    val lastMarkDate: Long? = null
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(AppDatabase.get(app).habitDao())

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.flow(),
                repo.distinctDaysFlow(),
                repo.lastMarkDateFlow()
            ) { habits, xp, lastMark ->
                val uiHabits = repo.toUi(habits)  // Теперь это работает, потому что внутри launch
                UiState(habits = uiHabits, xp = xp, lastMarkDate = lastMark)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    init {
        viewModelScope.launch {
            combine(
                repo.flow(),
                repo.distinctDaysFlow(),
                repo.lastMarkDateFlow()
            ) { habits, xp, lastMark ->
                val uiHabits = repo.toUi(habits)  // Теперь это работает, потому что внутри launch
                UiState(habits = uiHabits, xp = xp, lastMarkDate = lastMark)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    // Вставьте здесь новый блок:
    init {
        viewModelScope.launch {
            if (repo.flow().first().isEmpty()) {  // Если нет привычек, добавляем примеры
                repo.add("Уборка комнаты")
                repo.add("Мытьё окон")
                repo.add("Вынос мусора")
                repo.add("Чистка зубов")
                repo.add("Помыть голову")
                repo.add("Прогулка на свежем воздухе")  // Дополнительный пример
                repo.add("Чтение книги")  // Ещё один
            }
        }
    }


    fun toggle(id: Int) {
        viewModelScope.launch {
            repo.toggleToday(id)
        }
    }

    fun addClick() {
        _state.value = _state.value.copy(showAddDialog = true, newTitle = "")
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            val habit = _state.value.habits.find { it.id == id }
            if (habit != null) {
                repo.del(id, habit.title)
            }
        }
    }

    fun title(newTitle: String) {
        _state.value = _state.value.copy(newTitle = newTitle)
    }

    fun add() {
        viewModelScope.launch {
            val title = _state.value.newTitle.trim()
            if (title.isNotEmpty()) {
                repo.add(title)
                _state.value = _state.value.copy(showAddDialog = false, message = "Привычка добавлена")
            } else {
                _state.value = _state.value.copy(message = "Название не может быть пустым")
            }
        }
    }

    fun dismiss() {
        _state.value = _state.value.copy(showAddDialog = false)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)


    }
}


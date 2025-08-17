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
import kotlinx.coroutines.launch

data class UiState(
    val items: List<HabitUi> = emptyList(),
    val showDialog: Boolean = false,
    val newTitle: String = ""
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(AppDatabase.get(app).habitDao())
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.flow().collectLatest { list ->
                _state.value = _state.value.copy(items = repo.toUi(list))
            }
        }
    }

    fun addClick() { _state.value = _state.value.copy(showDialog = true, newTitle = "") }
    fun dismiss() { _state.value = _state.value.copy(showDialog = false, newTitle = "") }
    fun title(t: String) { _state.value = _state.value.copy(newTitle = t) }

    fun add() {
        val t = _state.value.newTitle.trim()
        if (t.isEmpty()) return
        viewModelScope.launch {
            repo.add(t)
            _state.value = _state.value.copy(showDialog = false, newTitle = "")
        }
    }

    fun toggle(id: Int) = viewModelScope.launch { repo.toggleToday(id) }
    fun delete(id: Int) {
        val title = _state.value.items.firstOrNull { it.id == id }?.title ?: ""
        viewModelScope.launch { repo.del(id, title) }
    }
}

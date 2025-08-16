package com.example.habitbloombasic.ui
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitbloombasic.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class UiState(val items: List<HabitUi> = emptyList(), val showDialog: Boolean = false, val newTitle: String = "")

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(AppDatabase.get(app).habitDao())
    private val _s = MutableStateFlow(UiState())
    val state = _s.asStateFlow()

    init {
        viewModelScope.launch {
            repo.flow().collectLatest { list -> _s.value = _s.value.copy(items = repo.toUi(list)) }
        }
    }
    fun addClick() { _s.value = _s.value.copy(showDialog = true, newTitle = "") }
    fun dismiss() { _s.value = _s.value.copy(showDialog = false, newTitle = "") }
    fun title(t: String) { _s.value = _s.value.copy(newTitle = t) }
    fun add() {
        val t = _s.value.newTitle.trim(); if (t.isEmpty()) return
        viewModelScope.launch { repo.add(t); _s.value = _s.value.copy(showDialog = false, newTitle = "") }
    }
    fun toggle(id: Int) { viewModelScope.launch { repo.toggleToday(id) } }
    fun delete(id: Int) {
        val title = _s.value.items.firstOrNull { it.id == id }?.title ?: ""
        viewModelScope.launch { repo.del(id, title) }
    }
}
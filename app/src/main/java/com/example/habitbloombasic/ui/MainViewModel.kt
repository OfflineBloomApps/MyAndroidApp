package com.example.habitbloombasic.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitbloombasic.data.AppDatabase
import com.example.habitbloombasic.data.HabitUi
import com.example.habitbloombasic.data.Repository
fun clearMessage() {
    _state.value = _state.value.copy(message = null)
}

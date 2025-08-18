package com.example.habitbloombasic.ui

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HabitBloomApp() {

    val app = LocalContext.current.applicationContext as Application

    val vm: MainViewModel = viewModel(
        factory = viewModelFactory {
            initializer { MainViewModel(app) }
        }
    )

    val s by vm.state.collectAsState()

    MaterialTheme {
        MainScreen(
            state = s,
            onToggle = vm::toggle,
            onAddClick = vm::addClick,
            onDelete = vm::delete,
            onTitleChange = vm::title,
            onAddConfirm = vm::add,
            onDialogDismiss = vm::dismiss
        )
    }
}

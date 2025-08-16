package com.example.habitbloombasic.ui
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.MaterialTheme

@Composable
fun HabitBloomApp(vm: MainViewModel = viewModel(factory = androidx.lifecycle.viewmodel.initializer {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
    MainViewModel(app)
})) {
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
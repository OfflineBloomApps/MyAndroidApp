package com.example.habitbloombasic.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.habitbloombasic.R
import com.example.habitbloombasic.data.HabitUi

@Composable
fun MainScreen(
    state: UiState,
    onToggle: (Int) -> Unit,
    onAddClick: () -> Unit,
    onDelete: (Int) -> Unit,
    onTitleChange: (String) -> Unit,
    onAddConfirm: () -> Unit,
    onDialogDismiss: () -> Unit
) {
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Text("+") } }) { pad ->
        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.empty_hint))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(pad)) {
                items(state.items) { it ->
                    RowItem(it, onToggle = { onToggle(it.id) }, onDelete = { onDelete(it.id) })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
    if (state.showDialog) {
        AddDialog(title = state.newTitle, onTitleChange = onTitleChange, onDismiss = onDialogDismiss, onSave = onAddConfirm)
    }
}

@Composable
private fun RowItem(item: HabitUi, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text("Стрик: ${item.streak}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.checked_today))
                    Switch(checked = item.checkedToday, onCheckedChange = { onToggle() })
                }
                TextButton(onClick = onDelete) { Text("Удалить") }
            }
        }
    }
}
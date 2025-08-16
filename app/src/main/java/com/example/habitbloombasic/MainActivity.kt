package com.example.habitbloombasic
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.habitbloombasic.ui.HabitBloomApp   // <- такой импорт

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HabitBloomApp() }
    }
}

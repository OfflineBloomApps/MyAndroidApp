package com.example.habitbloombasic.data
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(tableName = "checkmarks", indices = [Index(value = ["habitId","dateEpoch"], unique = true)])
data class CheckmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val dateEpoch: Long
)
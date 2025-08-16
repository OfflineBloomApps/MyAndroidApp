package com.example.habitbloombasic.data
import androidx.room.TypeConverter
import java.time.LocalDate
class Converters {
    @TypeConverter fun fromEpochDay(v: Long?): LocalDate? = v?.let { LocalDate.ofEpochDay(it) }
    @TypeConverter fun toEpochDay(d: LocalDate?): Long? = d?.toEpochDay()
}
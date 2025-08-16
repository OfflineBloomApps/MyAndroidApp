package com.example.habitbloombasic.data
import android.content.Context
import androidx.room.*
@Database(entities = [HabitEntity::class, CheckmarkEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    companion object {
        @Volatile private var I: AppDatabase? = null
        fun get(ctx: Context): AppDatabase =
            I ?: synchronized(this) {
                I ?: Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "habitbloom.db").build().also { I = it }
            }
    }
}
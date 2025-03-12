package com.jinghang.android.TaskTracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.jinghang.android.TaskTracker.Task

@Database(entities = [Task::class], version = 3, exportSchema = false)
@TypeConverters(TaskTypeConverters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

val migration_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE task ADD COLUMN description TEXT")
    }
}

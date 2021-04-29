package com.haverzard.workitout.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.haverzard.workitout.dao.HistoryDao
import com.haverzard.workitout.dao.RoutineExerciseScheduleDao
import com.haverzard.workitout.dao.SingleExerciseScheduleDao
import com.haverzard.workitout.entities.*

@Database(
        entities = [RoutineExerciseSchedule::class, SingleExerciseSchedule::class, History::class],
        version = 1,
        exportSchema = false)
@TypeConverters(ScheduleEnumConverters::class, HistoryConverters::class)
abstract class WorkOutDatabase : RoomDatabase() {

    // DAOs
    abstract fun singleExerciseScheduleDao(): SingleExerciseScheduleDao
    abstract fun routineExerciseScheduleDao(): RoutineExerciseScheduleDao
    abstract fun historyDao(): HistoryDao

    companion object {
        // singleton
        @Volatile
        private var INSTANCE: WorkOutDatabase? = null

        fun getDatabase(
            context: Context
        ): WorkOutDatabase {
            // auto build database if not exist
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        WorkOutDatabase::class.java,
                        "workout_database"
                )
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
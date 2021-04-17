package com.haverzard.workitout.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.haverzard.workitout.dao.RoutineExerciseScheduleDao
import com.haverzard.workitout.dao.SingleExerciseScheduleDao
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.ScheduleEnumConverters
import com.haverzard.workitout.entities.SingleExerciseSchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
        entities = [RoutineExerciseSchedule::class, SingleExerciseSchedule::class],
        version = 1,
        exportSchema = false)
@TypeConverters(ScheduleEnumConverters::class)
public abstract class WorkOutDatabase : RoomDatabase() {

    // DAOs
    abstract fun singleExerciseScheduleDao(): SingleExerciseScheduleDao
    abstract fun routineExerciseScheduleDao(): RoutineExerciseScheduleDao

    private class WordDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                }
            }
        }
    }

    companion object {
        // singleton
        @Volatile
        private var INSTANCE: WorkOutDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): WorkOutDatabase {
            // auto build database if not exist
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        WorkOutDatabase::class.java,
                        "workout_database"
                )
                .addCallback(WordDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
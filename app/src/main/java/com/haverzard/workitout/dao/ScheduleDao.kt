package com.haverzard.workitout.dao

import androidx.room.*
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineExerciseScheduleDao {

    @Query("SELECT * FROM routine_schedule_table")
    fun getSchedules(): Flow<List<RoutineExerciseSchedule>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(schedule: RoutineExerciseSchedule)

    @Delete
    suspend fun delete(schedule: RoutineExerciseSchedule)
}

@Dao
interface SingleExerciseScheduleDao {

    @Query("SELECT * FROM single_exercise_schedule_table ORDER BY date, start_time ASC")
    fun getSchedules(): Flow<List<SingleExerciseSchedule>>

    @Query("SELECT * FROM single_exercise_schedule_table WHERE start_time >= TIME('now') AND date >= DATE('now') AND routine_id IS NULL ORDER BY date, start_time ASC")
    fun getNonRoutineSchedules(): Flow<List<SingleExerciseSchedule>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(schedule: SingleExerciseSchedule)

    @Delete
    suspend fun delete(schedule: SingleExerciseSchedule)
}
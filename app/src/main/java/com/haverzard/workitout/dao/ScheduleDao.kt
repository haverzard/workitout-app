package com.haverzard.workitout.dao

import androidx.room.*
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineExerciseScheduleDao {

    @Query("SELECT * FROM routine_schedule_table")
    fun getSchedules(): Flow<List<RoutineExerciseSchedule>>

    @Query("SELECT * FROM routine_schedule_table WHERE start_time >= TIME('now') AND end_time <= TIME('now') AND instr(days, :day) LIMIT 1")
    fun getCurrentSchedule(day: String): RoutineExerciseSchedule?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(schedule: RoutineExerciseSchedule)

    @Delete
    suspend fun delete(schedule: RoutineExerciseSchedule)
}

@Dao
interface SingleExerciseScheduleDao {

    @Query("SELECT * FROM single_exercise_schedule_table ORDER BY date, start_time ASC")
    fun getSchedules(): Flow<List<SingleExerciseSchedule>>

    @Query("SELECT * FROM single_exercise_schedule_table WHERE (date = DATE('now', 'localtime') AND start_time >= TIME('now', 'localtime')) OR date > DATE('now', 'localtime') ORDER BY date, start_time ASC")
    fun getCurrentSchedules(): Flow<List<SingleExerciseSchedule>>

    @Query("SELECT * FROM single_exercise_schedule_table WHERE start_time <= TIME('now') AND end_time >= TIME('now') AND date == DATE('now') LIMIT 1")
    fun getCurrentSchedule(): SingleExerciseSchedule?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(schedule: SingleExerciseSchedule)

    @Delete
    suspend fun delete(schedule: SingleExerciseSchedule)
}
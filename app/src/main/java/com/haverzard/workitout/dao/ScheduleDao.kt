package com.haverzard.workitout.dao

import androidx.room.*
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineExerciseScheduleDao {

    @Query("SELECT * FROM routine_schedule_table")
    fun getSchedules(): Flow<List<RoutineExerciseSchedule>>

    @Query("SELECT * FROM routine_schedule_table WHERE id = :id")
    fun getSchedule(id: Int): RoutineExerciseSchedule

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(schedule: RoutineExerciseSchedule): Long

    @Delete
    suspend fun delete(schedule: RoutineExerciseSchedule)
}

@Dao
interface SingleExerciseScheduleDao {

    @Query("SELECT * FROM single_exercise_schedule_table ORDER BY date, start_time ASC")
    fun getSchedules(): Flow<List<SingleExerciseSchedule>>

    @Query("SELECT * FROM single_exercise_schedule_table WHERE (date = DATE('now', 'localtime') AND start_time >= TIME('now', 'localtime')) OR date > DATE('now', 'localtime') ORDER BY date, start_time ASC")
    fun getCurrentSchedules(): Flow<List<SingleExerciseSchedule>>

    @Query("SELECT * FROM single_exercise_schedule_table WHERE id = :id")
    fun getSchedule(id: Int): SingleExerciseSchedule

    @Query("UPDATE single_exercise_schedule_table SET start_time = TIME('now', 'localtime', '-1 hours') WHERE id = :id")
    suspend fun update(id: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(schedule: SingleExerciseSchedule): Long

    @Delete
    suspend fun delete(schedule: SingleExerciseSchedule)
}
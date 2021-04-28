package com.haverzard.workitout.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.haverzard.workitout.util.CustomTime
import java.sql.Date

@Entity(tableName = "routine_schedule_table")
data class RoutineExerciseSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val exercise_type: ExerciseType,
    val target: Double,
    val days: List<Day>,
    val start_time: CustomTime,
    val end_time: CustomTime,
)

@Entity(tableName = "single_exercise_schedule_table")
data class SingleExerciseSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val exercise_type: ExerciseType,
    val target: Double,
    val date: Date,
    val start_time: CustomTime,
    val end_time: CustomTime,
)
package com.haverzard.workitout.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.sql.Date
import java.sql.Time

@Entity(tableName = "routine_schedule_table")
data class RoutineExerciseSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val exercise_type: ExerciseType,
    val target: Double,
    val days: List<Day>,
    val start_time: Time,
    val end_time: Time,
)

@Entity(tableName = "single_exercise_schedule_table",
    foreignKeys = [ForeignKey(entity = RoutineExerciseSchedule::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("routine_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class SingleExerciseSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val routine_id: Int?,
    val exercise_type: ExerciseType,
    val target: Double,
    val date: Date,
    val start_time: Time,
    val end_time: Time,
)
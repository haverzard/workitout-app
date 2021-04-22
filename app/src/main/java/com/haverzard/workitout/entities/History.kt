package com.haverzard.workitout.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.sql.Date
import java.sql.Time

@Entity(tableName = "histories",
    foreignKeys = [
        ForeignKey(entity = SingleExerciseSchedule::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("single_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(entity = RoutineExerciseSchedule::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("routine_id"),
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val single_id: Int?,
    val routine_id: Int?,
    val exercise_type: ExerciseType,
    val date: Date,
    val start_time: Time,
    val end_time: Time,
    val target_reached: Double,
    val points: List<LatLng>,
)
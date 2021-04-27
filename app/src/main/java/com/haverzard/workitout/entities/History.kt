package com.haverzard.workitout.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.sql.Date
import java.sql.Time

@Entity(tableName = "histories")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val exercise_type: ExerciseType,
    val date: Date,
    val start_time: Time,
    val end_time: Time,
    val target_reached: Double,
    val points: List<LatLng>,
)
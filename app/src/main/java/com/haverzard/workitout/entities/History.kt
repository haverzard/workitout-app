package com.haverzard.workitout.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.haverzard.workitout.util.CustomTime
import android.icu.util.Calendar

@Entity(tableName = "histories")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val exercise_type: ExerciseType,
    val date: Calendar,
    val start_time: CustomTime,
    val end_time: CustomTime,
    val target_reached: Double,
    val points: List<LatLng>,
)
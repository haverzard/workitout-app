package com.haverzard.workitout.entities

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng


class HistoryConverters {

    @TypeConverter
    fun fromLatLng(value: LatLng) = "${value.latitude}-${value.longitude}"

    @TypeConverter
    fun toLatLng(value: String): LatLng {
        val latlng = value.split("-").map { it.toDouble() }
        return LatLng(latlng[0], latlng[1])
    }

    @TypeConverter
    fun fromPoints(value: List<LatLng>): String {
        if (value.isEmpty()) return ""
        return value.joinToString(",") { fromLatLng(it) }
    }

    @TypeConverter
    fun toPoints(value: String): List<LatLng> {
        if (value.isEmpty()) return List(0) { LatLng(0.0, 0.0) }
        return value.split(",").map { toLatLng(it) }
    }
}


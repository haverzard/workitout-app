package com.haverzard.workitout.entities

import androidx.room.TypeConverter
import com.haverzard.workitout.util.CalendarPlus
import com.haverzard.workitout.util.CustomTime
import android.icu.util.Calendar

class ScheduleEnumConverters {

    @TypeConverter
    fun fromDate(value: Calendar): String {
        return "%04d-%02d-%02d".format(value.get(Calendar.YEAR), value.get(Calendar.MONTH)+1, value.get(Calendar.DATE))
    }

    @TypeConverter
    fun toDate(value: String): Calendar {
        val date = value.split("-").map { it.toInt() }
        return CalendarPlus.initCalendarDate(date[0], date[1]-1, date[2])
    }

    @TypeConverter
    fun fromTime(value: CustomTime): String = "%02d:%02d:%02d".format(value.hours, value.minutes, value.seconds)

    @TypeConverter
    fun toTime(value: String): CustomTime {
        val time = value.split(":").map { it.toInt() }
        return CustomTime(time[0], time[1], time[2])
    }

    @TypeConverter
    fun toDays(value: String): List<Day> {
        return value.split(",").map { toDay(it) }
    }

    @TypeConverter
    fun fromDays(value: List<Day>) = value.joinToString(separator = ",")

    @TypeConverter
    fun toDay(value: String) = enumValueOf<Day>(value)

    @TypeConverter
    fun toExerciseType(value: String) = enumValueOf<ExerciseType>(value)

    @TypeConverter
    fun fromExerciseType(value: ExerciseType) = value.name
}


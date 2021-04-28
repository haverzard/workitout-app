package com.haverzard.workitout.entities

import androidx.room.TypeConverter
import java.sql.Date
import java.sql.Time


class ScheduleEnumConverters {

    @TypeConverter
    fun fromDate(value: Date) = "%04d-%02d-%02d".format(value.year, value.month+1, value.date)

    @TypeConverter
    fun toDate(value: String): Date {
        val date = value.split("-").map { it.toInt() }
        return Date(date[0], date[1]-1, date[2])
    }

    @TypeConverter
    fun fromTime(value: Time) = value.toString()

    @TypeConverter
    fun toTime(value: String): Time = Time.valueOf(value)

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


package com.haverzard.workitout.entities

import android.icu.text.SimpleDateFormat
import androidx.room.TypeConverter
import java.sql.Date
import java.sql.Time


enum class Day {
    Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
}

enum class ExerciseType {
    Cycling, Walking
}

class ScheduleEnumConverters {
    private val sdf = SimpleDateFormat("yyyy-MM-dd")

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
    fun toTime(value: String) = Time.valueOf(value)

    @TypeConverter
    fun toDays(value: String): List<Day> {
        return value.split(",").map { toDay(it) }
    }

    @TypeConverter
    fun fromDays(value: List<Day>) = value.joinToString(separator = ",")

    @TypeConverter
    fun toDay(value: String) = enumValueOf<Day>(value)

    @TypeConverter
    fun fromDay(value: Day) = value.name

    @TypeConverter
    fun toExerciseType(value: String) = enumValueOf<ExerciseType>(value)

    @TypeConverter
    fun fromExerciseType(value: ExerciseType) = value.name
}


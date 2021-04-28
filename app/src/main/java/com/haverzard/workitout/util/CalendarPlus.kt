package com.haverzard.workitout.util

import android.icu.util.Calendar
import java.text.DateFormat

class CalendarPlus {
    companion object {
        fun initCalendarDate(year: Int, month: Int, day: Int): Calendar {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DATE, day)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            return cal
        }

        fun toLocaleString(cal: Calendar): String {
            val df: DateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
            return df.format(cal.time)
        }
    }
}

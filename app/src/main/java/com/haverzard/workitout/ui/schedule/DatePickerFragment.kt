package com.haverzard.workitout.ui.schedule

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var observer: DatePickerDialogFragmentEvents
    private lateinit var calendar: Calendar

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity as Context, this, year, month, day)
    }

    fun setObserver(o: DatePickerDialogFragmentEvents) {
        observer = o
    }

    fun setCalendar(c: Calendar) {
        calendar = c
    }

    fun setCalendar(year: Int, month: Int, day: Int) {
        calendar.set(year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        observer.onDateSet(year, month, day)
    }
}

interface DatePickerDialogFragmentEvents {
    fun onDateSet(year: Int, month: Int, day: Int)
}
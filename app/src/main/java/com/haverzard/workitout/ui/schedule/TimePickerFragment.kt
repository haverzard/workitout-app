package com.haverzard.workitout.ui.schedule

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var observer: TimePickerDialogFragmentEvents
    private lateinit var calendar: Calendar
    private lateinit var arg: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity as Context, this, hour, minute, false)
    }

    fun setObserver(o: TimePickerDialogFragmentEvents) {
        observer = o
    }

    fun setCalendar(c: Calendar) {
        calendar = c
    }

    fun setCalendar(hour: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
    }

    fun setArg(a: String) {
        arg = a
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        observer.onTimeSet(hour, minute, arg)
    }
}

interface TimePickerDialogFragmentEvents {
    fun onTimeSet(hour: Int, minute: Int, arg: String)
}
package com.haverzard.workitout.ui.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.entities.Day
import com.haverzard.workitout.entities.ExerciseType
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import com.haverzard.workitout.receivers.ScheduleReceiver
import kotlinx.coroutines.launch
import java.sql.Date
import java.sql.Time

class AddScheduleFragment : Fragment(), DatePickerDialogFragmentEvents, TimePickerDialogFragmentEvents {

    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var alarmManager: AlarmManager
    private val datePicker = DatePickerFragment()
    private val timePicker = TimePickerFragment()
    private val safeArgs: AddScheduleFragmentArgs by navArgs()

    private var date: Date? = null
    private var startTime: Time? = null
    private var endTime: Time? = null
    private var exerciseType: ExerciseType? = null
    private var days = HashSet<Day>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scheduleViewModel = ViewModelProviders.of(
            this, ScheduleViewModelFactory((activity?.application as WorkOutApplication).repository)
        ).get(ScheduleViewModel::class.java)
        alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val root = inflater.inflate(R.layout.fragment_add_schedule, container, false)

        when (safeArgs.type) {
            "routine" -> {
                root.findViewById<TextView>(R.id.pick_date).visibility = View.GONE
            }
            "single" -> {
                root.findViewById<LinearLayout>(R.id.days_holder).visibility = View.GONE
            }
        }
        setupOnClickListeners(root)

        return root
    }

    private fun setupOnClickListeners(root: View) {
        var dayIt = 0
        val posDays = Day.values()
        root.findViewById<LinearLayout>(R.id.days_holder).children.forEach { view ->
            val selected  = posDays[dayIt]
            view.setOnClickListener {
                if (days.contains(selected)) {
                    days.remove(selected)
                    it.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                } else {
                    days.add(selected)
                    it.setBackgroundColor(resources.getColor(R.color.selected))
                }
            }
            dayIt += 1
        }
        root.findViewById<TextView>(R.id.pick_date)?.setOnClickListener {
            datePicker.setObserver(this)
            datePicker.setCalendar(Calendar.getInstance())
            if (date != null) {
                datePicker.setCalendar(date!!.year, date!!.month, date!!.date)
            }
            datePicker.show(fragmentManager, "datePicker")
        }

        root.findViewById<TextView>(R.id.pick_start_time)?.setOnClickListener {
            timePicker.setObserver(this)
            timePicker.setCalendar(Calendar.getInstance())
            timePicker.setArg("start")
            if (startTime != null) {
                timePicker.setCalendar(startTime!!.hours, startTime!!.minutes)
            }
            timePicker.show(fragmentManager, "timePicker")
        }

        root.findViewById<TextView>(R.id.pick_end_time)?.setOnClickListener {
            timePicker.setObserver(this)
            timePicker.setCalendar(Calendar.getInstance())
            timePicker.setArg("end")
            if (endTime != null) {
                timePicker.setCalendar(endTime!!.hours, endTime!!.minutes)
            }
            timePicker.show(fragmentManager, "timePicker")
        }

        val cyclingButton = root.findViewById<ImageButton>(R.id.exercise_cycling)
        val walkingButton = root.findViewById<ImageButton>(R.id.exercise_walking)
        walkingButton?.setOnClickListener {
            walkingButton
                .setBackgroundColor(resources.getColor(R.color.selected))
            cyclingButton
                .setBackgroundColor(resources.getColor(R.color.colorPrimary))
            exerciseType = ExerciseType.Walking
            root.findViewById<EditText>(R.id.enter_target).setText("")
            root.findViewById<EditText>(R.id.enter_target).inputType = InputType.TYPE_CLASS_NUMBER
            root.findViewById<TextView>(R.id.target_unit).text = "steps"
        }

        cyclingButton?.setOnClickListener {
            cyclingButton
                .setBackgroundColor(resources.getColor(R.color.selected))
            walkingButton
                .setBackgroundColor(resources.getColor(R.color.colorPrimary))
            exerciseType = ExerciseType.Cycling

            root.findViewById<EditText>(R.id.enter_target).setText("")
            root.findViewById<EditText>(R.id.enter_target).inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            root.findViewById<TextView>(R.id.target_unit).text = "km"
        }

        root.findViewById<TextView>(R.id.button_save)?.setOnClickListener {
            val targetStr = root.findViewById<EditText>(R.id.enter_target).text.toString()
            if (
                exerciseType == null
                || (safeArgs.type == "single" && date == null)
                || (safeArgs.type == "routine" && days.isEmpty())
                || startTime == null
                || endTime == null
                || targetStr == ""
            ) {
                Toast.makeText(
                    activity,
                    R.string.fill_empty_field,
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            val target = targetStr.toDouble()
            if (target < 0) {
                Toast.makeText(
                    activity,
                    "Negative target not allowed",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // setup time
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            val currentDate = Date(year, month, day).time + Time(hour, minute, second).time
            val yearSub = Date(1970, 0, 0).time
            val timeSub = Time(hour, minute, second).time

            if (date != null && date!!.time + startTime!!.time <= currentDate) {
                Toast.makeText(
                    activity,
                    "Schedule only for future exercise",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (startTime!!.time >= endTime!!.time) {
                Toast.makeText(
                    activity,
                    R.string.enter_correct_time,
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            (context!!.applicationContext as WorkOutApplication).applicationScope.launch {
                if (safeArgs.type == "single") {
                    val schedule = SingleExerciseSchedule(
                        id = 0,
                        exercise_type = exerciseType!!,
                        target = target,
                        date = date!!,
                        start_time = startTime!!,
                        end_time = endTime!!,
                    )

                    val id = scheduleViewModel.insertSingleSchedule(schedule)
                    val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
                        intent.putExtra("requestCode", id*2)
                        intent.putExtra("start", true)
                        PendingIntent.getBroadcast(context, (id*8).toInt(), intent, 0)
                    }
                    alarmManager.setExact(
                        AlarmManager.RTC,
                        date!!.time - yearSub + startTime!!.time,
                        alarmIntent
                    )
                } else {
                    val schedule = RoutineExerciseSchedule(
                        id = 0,
                        exercise_type = exerciseType!!,
                        target = target,
                        days = days.toList().sorted(),
                        start_time = startTime!!,
                        end_time = endTime!!,
                    )

                    val id = scheduleViewModel.insertRoutineSchedule(schedule)
                    val currentDay = (calendar.get(Calendar.DAY_OF_WEEK) - 2) % 7
                    schedule.days.forEach {
                        val dayIdx = Day.values().indexOf(it)
                        val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
                            intent.putExtra("requestCode", (id + 1)*2 - 1)
                            intent.putExtra("start", true)
                            PendingIntent.getBroadcast(context, ((id+1)*8-dayIdx-1).toInt(), intent, 0)
                        }
                        var delta = (dayIdx - currentDay)
                        if (delta < 0) {
                            delta += 7
                        }
                        alarmManager.setInexactRepeating(
                            AlarmManager.RTC,
                            currentDate - timeSub - yearSub + startTime!!.time + AlarmManager.INTERVAL_DAY * delta,
                            AlarmManager.INTERVAL_DAY * 7,
                            alarmIntent
                        )
                    }
                }
            }
            Toast.makeText(
                activity,
                R.string.schedule_created,
                Toast.LENGTH_LONG
            ).show()
            fragmentManager?.popBackStackImmediate()
        }
    }

    override fun onDateSet(year: Int, month: Int, day: Int) {
        val dateStr = "%04d-%02d-%02d".format(year, month, day)
        date = Date(year, month, day)
        view?.findViewById<TextView>(R.id.pick_date)?.text = dateStr
    }

    override fun onTimeSet(hour: Int, minute: Int, arg: String) {
        val timeStr = "%02d:%02d".format(hour, minute)
        if (arg == "start") {
            startTime = Time(hour, minute, 0)
            view?.findViewById<TextView>(R.id.pick_start_time)?.text = timeStr
        } else {
            endTime = Time(hour, minute, 0)
            view?.findViewById<TextView>(R.id.pick_end_time)?.text = timeStr
        }
    }

}

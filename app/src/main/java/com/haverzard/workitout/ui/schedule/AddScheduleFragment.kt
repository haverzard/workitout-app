package com.haverzard.workitout.ui.schedule

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.adapter.ScheduleListAdapter
import com.haverzard.workitout.entities.Day
import com.haverzard.workitout.entities.ExerciseType
import com.haverzard.workitout.entities.SingleExerciseSchedule
import com.haverzard.workitout.viewmodel.ScheduleViewModel
import com.haverzard.workitout.viewmodel.ScheduleViewModelFactory
import java.sql.Date
import java.sql.Time

class AddScheduleFragment : Fragment(), DatePickerDialogFragmentEvents, TimePickerDialogFragmentEvents {

    private lateinit var scheduleViewModel: ScheduleViewModel
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

    fun setupOnClickListeners(root: View) {
        var dayIt = 0
        val posDays = Day.values()
        root.findViewById<LinearLayout>(R.id.days_holder).children.forEach {
            val selected  = posDays[dayIt]
            it.setOnClickListener {
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
            if (startTime!!.time >= endTime!!.time) {
                Toast.makeText(
                    activity,
                    R.string.enter_correct_time,
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            scheduleViewModel.insert(
                SingleExerciseSchedule(
                    id = 0,
                    routine_id = null,
                    exercise_type = exerciseType!!,
                    target = target,
                    date = date!!,
                    start_time = startTime!!,
                    end_time = endTime!!,
                )
            )
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}

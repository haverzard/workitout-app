package com.haverzard.workitout.ui.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.entities.Day
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import com.haverzard.workitout.receivers.ScheduleReceiver
import com.haverzard.workitout.services.SharedPreferenceUtil
import com.haverzard.workitout.viewmodel.ScheduleViewModel
import com.haverzard.workitout.viewmodel.ScheduleViewModelFactory
import java.sql.Date

class ScheduleFragment : Fragment() {

    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var alarmManager: AlarmManager

    private val scheduleTypeDialog = ScheduleTypeDialog()
    private var autoTrack = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        scheduleViewModel = ViewModelProviders.of(
        this, ScheduleViewModelFactory((activity?.application as WorkOutApplication).repository)
        ).get(ScheduleViewModel::class.java)
        alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val root = inflater.inflate(R.layout.fragment_schedule, container, false)
        root.findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener {
            scheduleTypeDialog.show(fragmentManager, "timePicker")
        }

        autoTrack = SharedPreferenceUtil.getAutoTrackPref(context!!)
        var autoTrackSwitch = root.findViewById<Switch>(R.id.auto_track_switch)
        autoTrackSwitch?.isChecked = autoTrack
        autoTrackSwitch?.setOnClickListener {
            autoTrack = !autoTrack
            SharedPreferenceUtil.saveAutoTrackPref(context!!, autoTrack)
        }

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = ScheduleListAdapter(object :
            ScheduleListAdapter.ScheduleSelectedListener {
            override fun onScheduleSelected(schedule: SingleExerciseSchedule) {
                var alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
                    PendingIntent.getBroadcast(context, schedule.id*8, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
                alarmManager.cancel(alarmIntent)
                scheduleViewModel.deleteSingleSchedule(schedule)
            }

            override fun onScheduleSelected(schedule: RoutineExerciseSchedule) {
                schedule.days.forEach {
                    val day = Day.values().indexOf(it)
                    var alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
                        PendingIntent.getBroadcast(context, (schedule.id+1)*8-day-1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    }
                    alarmManager.cancel(alarmIntent)
                }
                scheduleViewModel.deleteRoutineSchedule(schedule)
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        scheduleViewModel.schedules.observe(owner = this) { schedules ->
            schedules.let { adapter.submitList(it) }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}

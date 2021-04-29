package com.haverzard.workitout.ui.schedule

import android.Manifest
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.entities.Day
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import com.haverzard.workitout.receivers.ScheduleReceiver
import com.haverzard.workitout.util.SharedPreferenceUtil


private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
class ScheduleFragment : Fragment() {

    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var alarmManager: AlarmManager
    private lateinit var activityManager: ActivityManager

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
        activityManager = context?.getSystemService(ACTIVITY_SERVICE) as ActivityManager

        val root = inflater.inflate(R.layout.fragment_schedule, container, false)
        root.findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener {
            scheduleTypeDialog.show(fragmentManager, "timePicker")
        }

        autoTrack = SharedPreferenceUtil.getAutoTrackPref(context!!)
        val autoTrackSwitch = root.findViewById<Switch>(R.id.auto_track_switch)
        autoTrackSwitch?.isChecked = autoTrack
        autoTrackSwitch?.setOnClickListener {
            if (!permissionApproved()) {
                requestForegroundPermissions()
            } else {
                autoTrack = !autoTrack
                SharedPreferenceUtil.saveAutoTrackPref(context!!, autoTrack)
            }
            autoTrackSwitch.isChecked = autoTrack
        }

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = ScheduleListAdapter(object :
            ScheduleListAdapter.ScheduleSelectedListener {
            override fun onScheduleSelected(schedule: SingleExerciseSchedule) {
                val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
                    PendingIntent.getBroadcast(
                        context,
                        schedule.id * 8,
                        intent,
                        PendingIntent.FLAG_NO_CREATE
                    )
                }
                if (alarmIntent != null) {
                    alarmManager.cancel(alarmIntent)
                }
                scheduleViewModel.deleteSingleSchedule(schedule)
            }

            override fun onScheduleSelected(schedule: RoutineExerciseSchedule) {
                schedule.days.forEach {
                    val day = Day.values().indexOf(it)
                    val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
                        PendingIntent.getBroadcast(
                            context,
                            (schedule.id + 1) * 8 - day - 1,
                            intent,
                            PendingIntent.FLAG_NO_CREATE
                        )
                    }
                    if (alarmIntent != null) {
                        alarmManager.cancel(alarmIntent)
                    }
                }
                scheduleViewModel.deleteRoutineSchedule(schedule)
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        scheduleViewModel.schedules.observe(this) { schedules ->
            schedules.let { adapter.submitList(it) }
        }
        return root
    }

    private fun permissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
            && PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
            && (Settings.canDrawOverlays(activity) || !SharedPreferenceUtil.alertWindowEnabled())
    }

    private fun requestForegroundPermissions() {
        val provideRationale = permissionApproved()

        if (provideRationale) {
            Snackbar.make(
                view!!,
                getString(R.string.permission_rationale),
                Snackbar.LENGTH_LONG
            )
                .setAction("Okay!") {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACTIVITY_RECOGNITION,
                        ),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION,
                ),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
            if (SharedPreferenceUtil.alertWindowEnabled()) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package: " + activity?.packageName)
                )
                startActivityForResult(intent, 5469)
            }
        }
    }

}

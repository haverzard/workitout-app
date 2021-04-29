package com.haverzard.workitout.receivers

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import androidx.core.app.NotificationCompat
import com.haverzard.workitout.MainActivity
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.entities.ExerciseType
import com.haverzard.workitout.services.TrackingService
import com.haverzard.workitout.util.CustomTime
import com.haverzard.workitout.util.NotificationHelper
import com.haverzard.workitout.util.SharedPreferenceUtil
import kotlinx.coroutines.launch
import java.util.*


class ScheduleReceiver: BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras!!
        val start = extras.getBoolean("start", false)
        val id = extras.getLong("requestCode", 0)
        var title = "Let's work out!"
        var body: String

        // inits
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val repository = (context.applicationContext as WorkOutApplication).repository
        val scope = (context.applicationContext as WorkOutApplication).applicationScope

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val currentTime = calendar.timeInMillis - CustomTime(hour, minute, second).time
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val autoTrack = SharedPreferenceUtil.getAutoTrackPref(context)

        scope.launch {
            var end = false
            if (id % 2 == 0L) {
                val schedule = repository.getSingleSchedule((id / 2).toInt())
                if (start) {
                    body = "It's your time to do some %s. ".format(
                        schedule.exercise_type.name.toLowerCase(Locale.ROOT)
                    )
                    body += if (schedule.exercise_type == ExerciseType.Cycling) {
                        "Your target: %.2f km".format(schedule.target)
                    } else {
                        "Your target: %d steps".format(schedule.target.toInt())
                    }

                    val alarmIntent =
                        Intent(context, ScheduleReceiver::class.java).let { intent ->
                            intent.putExtra("requestCode", (schedule.id * 2).toLong())
                            intent.putExtra("start", false)
                            PendingIntent.getBroadcast(
                                context,
                                schedule.id * 8,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        }
                    alarmManager.setExact(
                        AlarmManager.RTC,
                        schedule.date.timeInMillis + schedule.end_time.time,
                        alarmIntent
                    )
                    if (autoTrack) {
                        val serviceIntent = Intent(context, TrackingService::class.java)
                        serviceIntent.putExtra("exercise_type", schedule.exercise_type.name)
                        serviceIntent.putExtra("target", schedule.target)
                        context.startForegroundService(serviceIntent)
                    }
                    repository.updateSingleSchedule(schedule.id)
                } else {
                    title = "Work out ends!"
                    body = "You have completed your work out"
                    if (autoTrack) {
                        val serviceIntent = Intent(context, TrackingService::class.java)
                        context.stopService(serviceIntent)
                    }
                    repository.deleteSingleSchedule(schedule)
                    end = true
                }
            } else {
                val schedule = repository.getRoutineSchedule(((id - 1) / 2).toInt())
                if (start) {
                    body = "It's your time to do some %s".format(
                        schedule.exercise_type.name.toLowerCase(Locale.ROOT)
                    )
                    body += if (schedule.exercise_type == ExerciseType.Cycling) {
                        "Your target: %.2f km".format(schedule.target)
                    } else {
                        "Your target: %d steps".format(schedule.target.toInt())
                    }
                    val alarmIntent =
                        Intent(context, ScheduleReceiver::class.java).let { intent ->
                            intent.putExtra("requestCode", ((schedule.id + 1) * 2 - 1).toLong())
                            intent.putExtra("start", false)
                            PendingIntent.getBroadcast(
                                context,
                                (schedule.id + 1) * 8 - day - 1,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        }
                    alarmManager.setExact(
                        AlarmManager.RTC,
                        currentTime + schedule.end_time.time,
                        alarmIntent
                    )
                    if (autoTrack) {
                        val serviceIntent = Intent(context, TrackingService::class.java)
                        serviceIntent.putExtra("exercise_type", schedule.exercise_type.name)
                        serviceIntent.putExtra("target", schedule.target)
                        context.startForegroundService(serviceIntent)
                    }
                } else {
                    title = "Work out ends!"
                    body = "You have completed your work out"
                    Intent(context, ScheduleReceiver::class.java).let { intent ->
                        intent.putExtra("requestCode", (schedule.id + 1) * 2 - 1)
                        intent.putExtra("start", true)
                        PendingIntent.getBroadcast(
                            context,
                            (schedule.id + 1) * 8 - day - 1,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    }
                    if (autoTrack) {
                        val serviceIntent = Intent(context, TrackingService::class.java)
                        context.stopService(serviceIntent)
                    }
                    end = true
                }
            }
            if (!end || !autoTrack) {
                notificationManager.notify(
                    NotificationHelper.NOTIFICATION_SCHEDULER_ID,
                    generateNotification(
                        context,
                        title,
                        body,
                    )
                )
            }
        }
    }

    private fun generateNotification(context: Context, titleText: String, mainNotificationText: String): Notification {
        val launchActivityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context, 0, launchActivityIntent, 0)

        return NotificationHelper.generateNotification(context, titleText, mainNotificationText)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .build()
    }
}
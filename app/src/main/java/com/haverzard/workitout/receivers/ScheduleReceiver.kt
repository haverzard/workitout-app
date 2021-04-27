package com.haverzard.workitout.receivers

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import androidx.core.app.NotificationCompat
import com.haverzard.workitout.MainActivity
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.services.SharedPreferenceUtil
import com.haverzard.workitout.services.TrackingService
import kotlinx.coroutines.launch
import java.sql.Date
import java.util.*


private const val NOTIFICATION_ID = 1351812002
private const val NOTIFICATION_CHANNEL_ID = "workitout_02"

class ScheduleReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED) {
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
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val currentDate = Date(year, month, day).time
            val yearSub = Date(1970, 0, 0).time
            val autoTrack = SharedPreferenceUtil.getAutoTrackPref(context)

            scope.launch {
                if (id % 2 == 0L) {
                    val schedule = repository.getSingleSchedule((id / 2).toInt())
                    if (start) {
                        body = "It's your time to do some %s".format(
                            schedule.exercise_type.name.toLowerCase(Locale.ROOT)
                        )

                        val alarmIntent =
                            Intent(context, ScheduleReceiver::class.java).let { intent ->
                                intent.putExtra("requestCode", (schedule.id * 2).toLong())
                                intent.putExtra("start", false)
                                intent.action = AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED
                                PendingIntent.getBroadcast(
                                    context,
                                    schedule.id * 8,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                )
                            }
                        alarmManager.setExact(
                            AlarmManager.RTC,
                            schedule.date.time - yearSub + schedule.end_time.time,
                            alarmIntent
                        )
                        if (autoTrack) {
                            val serviceIntent = Intent(context, TrackingService::class.java)
                            serviceIntent.putExtra("start", true)
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
                            serviceIntent.putExtra("start", false)
                            serviceIntent.putExtra("exercise_type", schedule.exercise_type.name)
                            context.startForegroundService(serviceIntent)
                        }
                        repository.deleteSingleSchedule(schedule)
                    }
                } else {
                    val schedule = repository.getRoutineSchedule(((id - 1) / 2).toInt())
                    if (start) {
                        body = "It's your time to do some %s".format(
                            schedule.exercise_type.name.toLowerCase(Locale.ROOT)
                        )
                        val alarmIntent =
                            Intent(context, ScheduleReceiver::class.java).let { intent ->
                                intent.putExtra("requestCode", ((schedule.id + 1) * 2 - 1).toLong())
                                intent.putExtra("start", false)
                                intent.action = AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED
                                PendingIntent.getBroadcast(
                                    context,
                                    (schedule.id + 1) * 8 - day - 1,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                )
                            }
                        alarmManager.setExact(
                            AlarmManager.RTC,
                            currentDate - yearSub + schedule.end_time.time,
                            alarmIntent
                        )
                        if (autoTrack) {
                            val serviceIntent = Intent(context, TrackingService::class.java)
                            serviceIntent.putExtra("start", true)
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
                            serviceIntent.putExtra("start", false)
                            serviceIntent.putExtra("exercise_type", schedule.exercise_type.name)
                            context.startForegroundService(serviceIntent)
                        }
                    }
                }
                notificationManager.notify(
                    NOTIFICATION_ID,
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
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(notificationChannel)

        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        // 3. Set up main Intent/Pending Intents for notification.
        val launchActivityIntent = Intent(context, MainActivity::class.java)

        val activityPendingIntent = PendingIntent.getActivity(
            context, 0, launchActivityIntent, 0)

        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(context,
                NOTIFICATION_CHANNEL_ID
            )

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .build()
    }
}
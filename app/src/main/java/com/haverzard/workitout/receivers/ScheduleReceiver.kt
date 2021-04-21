package com.haverzard.workitout.receivers

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.haverzard.workitout.MainActivity
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import kotlinx.coroutines.launch
import java.sql.Time


private const val NOTIFICATION_ID = 1351812002
private const val NOTIFICATION_CHANNEL_ID = "workitout_02"

class ScheduleReceiver: BroadcastReceiver() {
    private var lastTime: Time? = null

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        System.out.println(action)
        // inits
        var notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var repository = (context.applicationContext as WorkOutApplication).repository
        var scope = (context.applicationContext as WorkOutApplication).applicationScope

        notificationManager.notify(
            NOTIFICATION_ID,
            generateNotification(
                context,
                "Let's work out!",
                "Test"
            )
        )
        scope.launch {
            val schedule = repository.getCurrentSingleSchedule()
            if (schedule != null
                && (lastTime == null || lastTime!! < schedule.start_time)
            ) {
                lastTime = schedule.start_time
                notificationManager.notify(
                    NOTIFICATION_ID,
                    generateNotification(
                        context,
                        "Let's work out!",
                        "It's your time to do some %s".format(
                            schedule.exercise_type.name.toLowerCase()
                        )
                    )
                )
            } else {
                val routine = repository.getCurrentRoutineSchedule()
                if (routine != null
                    && (lastTime == null || lastTime!! < routine.start_time)
                ) {
                    lastTime = routine.start_time
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        generateNotification(
                            context,
                            "Let's work out!",
                            "It's your time to do some %s".format(
                                routine.exercise_type.name.toLowerCase()
                            )
                        )
                    )
                }
            }
        }
    }

    private fun generateNotification(context: Context, titleText: String, mainNotificationText: String): Notification {
        var notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT)

            notificationManager.createNotificationChannel(notificationChannel)
        }

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
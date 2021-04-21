package com.haverzard.workitout.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.haverzard.workitout.MainActivity
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.entities.ExerciseType
import com.haverzard.workitout.repository.WorkOutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.sql.Time


class ScheduleService: Service() {

    private lateinit var timeChangedReceiver: BroadcastReceiver
    private lateinit var notificationManager: NotificationManager
    private lateinit var repository: WorkOutRepository
    private lateinit var scope: CoroutineScope

    private var lastTime: Time? = null
    private val localBinder = LocalBinder()
    private val intentFilter = IntentFilter()

    override fun onCreate() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        repository = (application as WorkOutApplication).repository
        scope = (application as WorkOutApplication).applicationScope

        // init filter
        intentFilter.addAction(Intent.ACTION_TIME_TICK)
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED)

        // init receiver
        timeChangedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
//                System.out.println("hehe123")
                val action = intent.action
                System.out.println(action)

                scope.launch {
                    val schedule = repository.getCurrentSingleSchedule()
                    if (schedule != null
                        && (lastTime == null || lastTime!! < schedule.start_time)
                    ) {
                        lastTime = schedule.start_time
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            generateNotification(
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
        }

//        registerReceiver(timeChangedReceiver, intentFilter);
    }

    suspend fun notifySchedule() {

    }

    override fun onDestroy() {
        // on destroy
        unregisterReceiver(timeChangedReceiver)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        // application is on foreground
        stopForeground(true)
        registerReceiver(timeChangedReceiver, intentFilter)
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        // application is on foreground
        stopForeground(true)
        registerReceiver(timeChangedReceiver, intentFilter)
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        // application is not on foreground
        unregisterReceiver(timeChangedReceiver)
        return true
    }

    private fun generateNotification(titleText: String, mainNotificationText: String): Notification {
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
        val launchActivityIntent = Intent(this, MainActivity::class.java)

        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, 0)

        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

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

    inner class LocalBinder : Binder() {
        internal val service: ScheduleService
            get() = this@ScheduleService
    }

    companion object {
        private const val PACKAGE_NAME = "com.haverzard.com"

//        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
//            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

//        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"

//        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
//            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        private const val NOTIFICATION_ID = 1351812002

        private const val NOTIFICATION_CHANNEL_ID = "workitout_02"
    }
}
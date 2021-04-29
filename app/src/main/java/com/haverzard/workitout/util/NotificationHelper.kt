package com.haverzard.workitout.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.haverzard.workitout.MainActivity
import com.haverzard.workitout.R

object NotificationHelper {

    private const val NOTIFICATION_CHANNEL_ID = "workitout_01"
    const val NOTIFICATION_TRACKER_ID = 1351812001
    const val NOTIFICATION_SCHEDULER_ID = 1351812002

    fun generateNotification(
        context: Context,
        titleText: String,
        mainNotificationText: String,
    ): NotificationCompat.Builder {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(notificationChannel)

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

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
    }
}
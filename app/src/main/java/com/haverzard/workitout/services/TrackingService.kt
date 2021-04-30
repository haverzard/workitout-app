package com.haverzard.workitout.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.util.Calendar
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.haverzard.workitout.MainActivity
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.entities.ExerciseType
import com.haverzard.workitout.entities.History
import com.haverzard.workitout.util.CalendarPlus
import com.haverzard.workitout.util.CustomTime
import com.haverzard.workitout.util.NotificationHelper
import com.haverzard.workitout.util.SharedPreferenceUtil
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TrackingService: Service(), SensorEventListener {
    // managers & application
    private lateinit var notificationManager: NotificationManager
    private lateinit var sensorManager: SensorManager
    private lateinit var activityManager: ActivityManager
    private lateinit var application: WorkOutApplication

    private val localBinder = LocalBinder()

    // location data
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null

    // data
    private var enableTarget = false
    private var target = 0.0
    private var targetReached = 0.0
    private var points = MutableList(0) { LatLng(0.0, 0.0) }
    private var currentDate: Calendar? = null
    private var startTime: CustomTime? = null
    private var endTime: CustomTime? = null
    private var exerciseType: ExerciseType? = null

    override fun onCreate() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(2)
            fastestInterval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.MINUTES.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        application = (this.applicationContext as WorkOutApplication)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (currentLocation != null) {
                    targetReached += currentLocation!!.distanceTo(locationResult.lastLocation).toDouble() / 1000
                }
                currentLocation = locationResult.lastLocation

                val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                val latLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                if (points.isEmpty() || points[points.size-1] != latLng) {
                    points.add(latLng)
                }
                var notifText = "You have been cycling for $targetReached km"
                if (enableTarget) {
                    notifText += "\nYour target is $target km"
                }
                notificationManager.notify(
                    NotificationHelper.NOTIFICATION_TRACKER_ID,
                    generateNotification(notifText))
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val exerciseType = intent.getStringExtra("exercise_type")
        if (exerciseType != null) {
            val scheduledExerciseType = SharedPreferenceUtil.getExerciseType(this)
            if (scheduledExerciseType == null || scheduledExerciseType == "") {
                SharedPreferenceUtil.saveExerciseType(this, exerciseType)
                target = intent.getDoubleExtra("target", 0.0)
                if (exerciseType == "Cycling") {
                    enableTarget = target != 0.0
                    subscribeToLocationUpdates()
                } else {
                    enableTarget = target != 0.0
                    subscribeToStepCounter()
                }
            }
        } else {
            stopSelf()
        }
        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        val scheduledExerciseType = SharedPreferenceUtil.getExerciseType(this)
        if (scheduledExerciseType != null) {
            if (scheduledExerciseType == "Cycling") {
                unsubscribeToLocationUpdates()
            } else {
                unsubscribeToStepCounter()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        // application is on foreground
        stopForeground(true)
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        // application is on foreground
        stopForeground(true)
        super.onRebind(intent)
    }

    override fun onSensorChanged(event: SensorEvent) {
        targetReached += 1.0
        var notifText = "You have been walking for ${targetReached.toInt()} steps."
        if (enableTarget) {
            notifText += " \nYour target is ${target.toInt()} steps."
        }
        notificationManager.notify(
            NotificationHelper.NOTIFICATION_TRACKER_ID,
            generateNotification(notifText))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun initData() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        currentDate = CalendarPlus.initCalendarDate(year, month, day)
        targetReached = 0.0
        points.clear()
        startTime = CustomTime(hour, minute, second)
        endTime = null
        exerciseType = ExerciseType.valueOf(SharedPreferenceUtil.getExerciseType(this)!!)
    }

    private fun saveData() {
        enableTarget = false
        SharedPreferenceUtil.saveExerciseType(this, "")
        if (startTime != null) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            endTime = CustomTime(hour, minute, second)
            if (startTime!! > endTime!!) {
                endTime = CustomTime(23, 59, 59)
            }
            application.applicationScope.launch {
                val history = History(
                    0,
                    exerciseType!!,
                    currentDate!!,
                    startTime!!,
                    endTime!!,
                    targetReached,
                    points.toList(),
                )
                val id = application.repository.insertHistory(history)
                launchActivity(id)
            }
        }
    }

    private fun launchActivity(id: Long) {
        val launchActivityIntent = Intent(this, MainActivity::class.java)
        launchActivityIntent.putExtra("history_id", id)

        SharedPreferenceUtil.saveHistoryId(this, id)
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationHelper
            .generateNotification(
                this,
                "Work out ends!",
                "You have completed your work out"
            )
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            NotificationHelper.NOTIFICATION_SCHEDULER_ID,
            notification,
        )

        if (SharedPreferenceUtil.alertWindowEnabled() || SharedPreferenceUtil.getForegroundPref(this)) {
            launchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            this.startActivity(launchActivityIntent)
            stopSelf()
        }
    }

    private fun subscribeToStepCounter() {
        initData()
        var notifText = "You have been walking for ${targetReached.toInt()} steps."
        if (enableTarget) {
            notifText += " \nYour target is ${target.toInt()} steps."
        }
        startForeground(
            NotificationHelper.NOTIFICATION_TRACKER_ID,
            generateNotification(notifText))
        SharedPreferenceUtil.saveTrackingPref(this, true)
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    private fun unsubscribeToStepCounter() {
        SharedPreferenceUtil.saveTrackingPref(this, false)
        sensorManager.unregisterListener(this)
        notificationManager.cancel(NotificationHelper.NOTIFICATION_TRACKER_ID)
        saveData()
    }

    private fun subscribeToLocationUpdates() {
        initData()
        SharedPreferenceUtil.saveTrackingPref(this, true)

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper())
            var notifText = "You have been cycling for $targetReached km."
            if (enableTarget) {
                notifText += " \nYour target is $target km."
            }
            startForeground(
                NotificationHelper.NOTIFICATION_TRACKER_ID,
                generateNotification(notifText))
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveTrackingPref(this, false)
        }
    }

    private fun unsubscribeToLocationUpdates() {
        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    stopSelf()
                }
            }
            SharedPreferenceUtil.saveTrackingPref(this, false)
            notificationManager.cancel(NotificationHelper.NOTIFICATION_TRACKER_ID)
            saveData()
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveTrackingPref(this, true)
        }
    }

    private fun generateNotification(mainNotificationText: String): Notification {
        val launchActivityIntent = Intent(this, MainActivity::class.java)
        val cancelIntent = Intent(this, TrackingService::class.java)

        val servicePendingIntent = PendingIntent.getService(
            this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, 0)

        return NotificationHelper.generateNotification(this, getString(R.string.app_name), mainNotificationText)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_launcher_background,
                getString(R.string.launch_activity),
                activityPendingIntent
            )
            .addAction(
                R.drawable.ic_cancel_blue_24dp,
                getString(R.string.stop_tracking),
                servicePendingIntent
            )
            .build()
    }

    inner class LocalBinder : Binder()

    companion object {
        private const val PACKAGE_NAME = "com.haverzard.com"

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
    }
}


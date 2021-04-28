package com.haverzard.workitout.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import kotlinx.coroutines.launch
import java.sql.Date
import java.sql.Time
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashSet

class TrackingService: Service(), SensorEventListener {
    private lateinit var notificationManager: NotificationManager
    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var application: WorkOutApplication

    private var configurationChange: Boolean = false
    private var currentLocation: Location? = null
    private val localBinder = LocalBinder()

    // data
    private var enableTarget = false
    private var target = 0.0
    private var targetReached = 0.0
    private var points = HashSet<LatLng>(0)
    private var currentDate: Date? = null
    private var startTime: Time? = null
    private var endTime: Time? = null
    private var exerciseType: ExerciseType? = null

    override fun onCreate() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
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

                points.add(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
                var notifText = "You have been cycling for $targetReached km"
                if (enableTarget) {
                    notifText += "\nYour target is $target km"
                }
                notificationManager.notify(
                    NOTIFICATION_ID,
                    generateNotification(notifText))
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val exerciseType = intent.getStringExtra("exercise_type")
        if (exerciseType != null) {
            val scheduledExerciseType = SharedPreferenceUtil.getExerciseType(this)
            val sameAsScheduled = exerciseType == scheduledExerciseType
            val isNotScheduled = scheduledExerciseType == null || scheduledExerciseType == ""
            if (isNotScheduled)
                SharedPreferenceUtil.saveExerciseType(this, exerciseType)
            val start = intent.getBooleanExtra("start", false)
            target = intent.getDoubleExtra("target", 0.0)
            if (exerciseType == "Cycling") {
                if (start && isNotScheduled) {
                    enableTarget = true
                    subscribeToLocationUpdates()
                } else if (!start && sameAsScheduled) {
                    unsubscribeToLocationUpdates()
                }
            } else {
                if (start && isNotScheduled) {
                    enableTarget = true
                    subscribeToStepCounter()
                } else if (!start && sameAsScheduled) {
                    unsubscribeToStepCounter()
                }
            }
        }
        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        // application is on foreground
        stopForeground(true)
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        // application is on foreground
        stopForeground(true)
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onSensorChanged(event: SensorEvent) {
        targetReached += 1.0
        var notifText = "You have been walking for ${targetReached.toInt()} steps."
        if (enableTarget) {
            notifText += " \nYour target is ${target.toInt()} steps."
        }
        notificationManager.notify(
            NOTIFICATION_ID,
            generateNotification(notifText))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {}

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    fun initData() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        currentDate = Date(year, month, day)
        targetReached = 0.0
        points.clear()
        startTime = Time(hour, minute, second)
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
            endTime = Time(hour, minute, second)
            if (startTime!! >= endTime!!) {
                endTime = Time(23, 59, 59)
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
                 application.repository.insertHistory(history)
            }
        }
    }

    fun subscribeToStepCounter() {
        initData()
        SharedPreferenceUtil.saveLocationTrackingPref(this, true)
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
            SensorManager.SENSOR_DELAY_GAME
        )
        var notifText = "You have been walking for ${targetReached.toInt()} steps."
        if (enableTarget) {
            notifText += " \nYour target is ${target.toInt()} steps."
        }
        notificationManager.notify(
            NOTIFICATION_ID,
            generateNotification(notifText))
    }

    fun unsubscribeToStepCounter() {
        SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        SharedPreferenceUtil.saveExerciseType(this, "")
        sensorManager.unregisterListener(this)
        notificationManager.cancel(NOTIFICATION_ID)
        saveData()
    }

    fun subscribeToLocationUpdates() {
        initData()
        SharedPreferenceUtil.saveLocationTrackingPref(this, true)

        // start service
        startService(Intent(applicationContext, TrackingService::class.java))

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper())
            var notifText = "You have been cycling for $targetReached km."
            if (enableTarget) {
                notifText += " \nYour target is $target km."
            }
            notificationManager.notify(
                NOTIFICATION_ID,
                generateNotification(notifText))
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        }
    }

    fun unsubscribeToLocationUpdates() {
        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    stopSelf()
                }
            }
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
            SharedPreferenceUtil.saveExerciseType(this, "")
            notificationManager.cancel(NOTIFICATION_ID)
            saveData()
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, true)
        }
    }

    private fun generateNotification(mainNotificationText: String): Notification {
        println("Generating...")
        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get data
        //      1. Create Notification Channel for O+
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up Intent / Pending Intent for notification
        //      4. Build and issue the notification

        // 0. Get data
        val titleText = getString(R.string.app_name)

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(notificationChannel)

        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        // 3. Set up main Intent/Pending Intents for notification.
        val launchActivityIntent = Intent(this, MainActivity::class.java)

        val cancelIntent = Intent(this, TrackingService::class.java)
        cancelIntent.putExtra("start", false)
        cancelIntent.putExtra("exercise_type", SharedPreferenceUtil.getExerciseType(this))

        val servicePendingIntent = PendingIntent.getService(
            this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

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

    inner class LocalBinder : Binder() {
        internal val service: TrackingService
            get() = this@TrackingService
    }

    companion object {
        private const val PACKAGE_NAME = "com.haverzard.com"

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"

        private const val NOTIFICATION_ID = 1351812001

        private const val NOTIFICATION_CHANNEL_ID = "workitout_01"
    }
}

internal object SharedPreferenceUtil {

    private const val KEY_AUTO_TRACK = "auto_track"
    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"
    const val KEY_EXERCISE_TYPE = "exercise_type"

//    fun getLocationTrackingPref(context: Context): Boolean =
//        context.getSharedPreferences(
//            context.getString(R.string.preference_file_key),
//            Context.MODE_PRIVATE,
//        ).getBoolean(KEY_FOREGROUND_ENABLED, false)

    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) {
        val editor = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).edit()
        editor.putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
        editor.apply()
    }

    fun getExerciseType(context: Context): String? =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).getString(KEY_EXERCISE_TYPE, "")

    fun saveExerciseType(context: Context, exerciseType: String) {
        val editor = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).edit()
        editor.putString(KEY_EXERCISE_TYPE, exerciseType)
        editor.apply()
    }

    fun getAutoTrackPref(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).getBoolean(KEY_AUTO_TRACK, false)

    fun saveAutoTrackPref(context: Context, autoTrack: Boolean) {
        val editor = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).edit()
        editor.putBoolean(KEY_AUTO_TRACK, autoTrack)
        editor.apply()
    }
}
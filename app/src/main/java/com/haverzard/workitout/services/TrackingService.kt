package com.haverzard.workitout.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.util.Calendar
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.RotateAnimation
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
import kotlin.math.roundToInt

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
                System.out.println("update")
                super.onLocationResult(locationResult)
                if (currentLocation != null) {
                    targetReached += currentLocation!!.distanceTo(locationResult.lastLocation).toDouble() / 1000
                }
                currentLocation = locationResult.lastLocation

                val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                points.add(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
                notificationManager.notify(
                    NOTIFICATION_ID,
                    generateNotification("You have been cycling for ${targetReached} km"))
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val cancelLocationTrackingFromNotification =
            intent.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        if (cancelLocationTrackingFromNotification) {
            unsubscribeToLocationUpdates()
            stopSelf()
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
        notificationManager.notify(
            NOTIFICATION_ID,
            generateNotification("You have been walking for ${targetReached.toInt()} steps"))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {}

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        System.out.println("New config")
        configurationChange = true
    }

    fun initData() {
        var calendar = Calendar.getInstance()
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

    fun saveData() {
        System.out.println(startTime)
        if (startTime != null) {
            var calendar = Calendar.getInstance()
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
                    null,
                    null,
                    exerciseType!!,
                    currentDate!!,
                    startTime!!,
                    endTime!!,
                    targetReached,
                    points.toList(),
                )
                System.out.println(history)
                // TODO: uncomment and implement history page
                // application.repository.insertHistory(history)
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
        notificationManager.notify(
            NOTIFICATION_ID,
            generateNotification("You have been walking for ${targetReached.toInt()} steps"))
    }

    fun unsubscribeToStepCounter() {
        SharedPreferenceUtil.saveLocationTrackingPref(this, false)
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
            notificationManager.notify(
                NOTIFICATION_ID,
                generateNotification("You have been cycling for ${targetReached} km"))
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
            notificationManager.cancel(NOTIFICATION_ID)
            saveData()
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, true)
        }
    }

    private fun generateNotification(mainNotificationText: String): Notification {
        System.out.println("Generating...")
        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get data
        //      1. Create Notification Channel for O+
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up Intent / Pending Intent for notification
        //      4. Build and issue the notification

        // 0. Get data
        val titleText = getString(R.string.app_name)

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

        val cancelIntent = Intent(this, TrackingService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)

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
                R.drawable.ic_launcher_background, getString(R.string.launch_activity),
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

        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        private const val NOTIFICATION_ID = 1351812001

        private const val NOTIFICATION_CHANNEL_ID = "workitout_01"
    }
}

internal object SharedPreferenceUtil {

    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"
    const val KEY_EXERCISE_TYPE = "exercise_type"

    fun getLocationTrackingPref(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).getBoolean(KEY_FOREGROUND_ENABLED, false)

    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) {
        var editor = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).edit()
        editor.putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
        editor.commit()
    }

    fun getExerciseType(context: Context): String? =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).getString(KEY_EXERCISE_TYPE, "")

    fun saveExerciseType(context: Context, exerciseType: String) {
        var editor = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).edit()
        editor.putString(KEY_EXERCISE_TYPE, exerciseType)
        editor.commit()
    }
}
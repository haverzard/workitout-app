package com.haverzard.workitout.ui.tracker

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.haverzard.workitout.BuildConfig
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.entities.SingleExerciseSchedule
import com.haverzard.workitout.services.SharedPreferenceUtil
import com.haverzard.workitout.services.TrackingService
import com.haverzard.workitout.viewmodel.ScheduleViewModel
import com.haverzard.workitout.viewmodel.ScheduleViewModelFactory
import kotlin.math.roundToInt


private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
class TrackerFragment : Fragment(), SensorEventListener {

    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var sensorManager: SensorManager
    private lateinit var image: ImageView
    private lateinit var sharedPreferences: SharedPreferences

    private var trackingService: TrackingService? = null
    private var trackingServiceBound = false

    private var currentDegree = 0f
    private var exerciseType = ""

    private val trackingServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as TrackingService.LocalBinder
            trackingService = binder.service
            trackingServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            trackingService = null
            trackingServiceBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scheduleViewModel = ViewModelProviders.of(
            this, ScheduleViewModelFactory((activity?.application as WorkOutApplication).repository)
        ).get(ScheduleViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_tracker, container, false)
        image = root.findViewById(R.id.compass)
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sharedPreferences =
            activity!!.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        var cyclingButton = root.findViewById<ImageButton>(R.id.exercise_cycling)
        var walkingButton = root.findViewById<ImageButton>(R.id.exercise_walking)
        var trackButton = root.findViewById<MaterialButton>(R.id.btn_track)
        if (!sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)) {
            SharedPreferenceUtil.saveExerciseType(activity!!, "")
        } else {
            trackButton.text = "Stop Tracking"
            trackButton.backgroundTintList = ContextCompat.getColorStateList(context!!, android.R.color.holo_red_dark)
            root.findViewById<LinearLayout>(R.id.btn_container).visibility = View.INVISIBLE
        }
        walkingButton.setOnClickListener {
            SharedPreferenceUtil.saveExerciseType(activity!!, "Walking")
            walkingButton.setBackgroundColor(resources.getColor(R.color.selected))
            cyclingButton.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }
        cyclingButton.setOnClickListener {
            SharedPreferenceUtil.saveExerciseType(activity!!, "Cycling")
            cyclingButton.setBackgroundColor(resources.getColor(R.color.selected))
            walkingButton.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }

        trackButton.setOnClickListener {
            val enabled = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
            val exerciseType = sharedPreferences.getString(
                SharedPreferenceUtil.KEY_EXERCISE_TYPE, "")
            if (exerciseType == "") {
                Toast.makeText(
                    activity,
                    "Pick an exercise type!",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (enabled) {
                if (exerciseType == "Cycling") {
                    trackingService?.unsubscribeToLocationUpdates()
                } else {
                    trackingService?.unsubscribeToStepCounter()
                }
                cyclingButton.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                walkingButton.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                SharedPreferenceUtil.saveExerciseType(activity!!, "")
                root.findViewById<LinearLayout>(R.id.btn_container).visibility = View.VISIBLE
                (it as MaterialButton).text = "Start Tracking"
                it.backgroundTintList = ContextCompat.getColorStateList(context!!, android.R.color.holo_green_dark)
            } else {
                if (permissionApproved()) {
                    if (exerciseType == "Cycling") {
                        trackingService?.subscribeToLocationUpdates()
                    } else {
                        trackingService?.subscribeToStepCounter()
                    }
                    root.findViewById<LinearLayout>(R.id.btn_container).visibility = View.INVISIBLE
                    (it as MaterialButton).text = "Stop Tracking"
                    it.backgroundTintList = ContextCompat.getColorStateList(context!!, android.R.color.holo_red_dark)
                } else {
                    requestForegroundPermissions()
                }
            }
        }
        return root
    }

    override fun onStart() {
        super.onStart()

        val serviceIntent = Intent(activity!!, TrackingService::class.java)
        activity!!.bindService(serviceIntent, trackingServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onPause() {
        if (trackingServiceBound) {
            activity!!.unbindService(trackingServiceConnection)
            trackingServiceBound = false
        }
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() -> {}
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    trackingService?.subscribeToLocationUpdates()
                else -> {
                    // disable button
                    Snackbar.make(
                        view!!,
                        "Permission was denied, but app requires it for core functionality",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Settings") {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        // get angle
        val degree = event.values[0].roundToInt().toFloat()

        // create rotate animation
        val anim = RotateAnimation(
            currentDegree,
            -degree,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        anim.duration = 180
        anim.fillAfter = true
        image.startAnimation(anim)

        currentDegree = -degree
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
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestPermissions() {
        val provideRationale = permissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(
                view!!,
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction("Okay!") {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

//    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {
//
//        override fun onReceive(context: Context, intent: Intent) {
//            val location = intent.getParcelableExtra<Location>(
//                ForegroundOnlyLocationService.EXTRA_LOCATION
//            )
//
//            if (location != null) {
//                logResultsToScreen("Foreground location: ${location.toText()}")
//            }
//        }
//    }
}
package com.haverzard.workitout.ui.tracker

import android.Manifest
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.haverzard.workitout.BuildConfig
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.services.TrackingService
import com.haverzard.workitout.ui.schedule.ScheduleViewModel
import com.haverzard.workitout.ui.schedule.ScheduleViewModelFactory
import com.haverzard.workitout.util.SharedPreferenceUtil
import kotlin.math.abs


private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
class TrackerFragment : Fragment(), SensorEventListener {

    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private lateinit var image: ImageView
    private lateinit var sharedPreferences: SharedPreferences

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var currentDegree = 0f
    private var exerciseType = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scheduleViewModel = ViewModelProviders.of(
            this, ScheduleViewModelFactory((activity?.application as WorkOutApplication).repository)
        ).get(ScheduleViewModel::class.java)
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) as Sensor
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) as Sensor
        sharedPreferences =
            activity!!.getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )

        val root = inflater.inflate(R.layout.fragment_tracker, container, false)
        image = root.findViewById(R.id.compass)

        if (!permissionApproved()) {
            requestForegroundPermissions()
        }

        setupOnClickListener(root)
        return root
    }

    private fun setupOnClickListener(root: View) {
        val cyclingButton = root.findViewById<ImageButton>(R.id.exercise_cycling)
        val walkingButton = root.findViewById<ImageButton>(R.id.exercise_walking)
        val trackButton = root.findViewById<MaterialButton>(R.id.btn_track)

        if (!SharedPreferenceUtil.getTracking(activity!!)) {
            SharedPreferenceUtil.saveExerciseType(activity!!, "")
        } else {
            trackButton.text = getString(R.string.stop_tracking_btn)
            trackButton.backgroundTintList = ContextCompat.getColorStateList(
                context!!,
                android.R.color.holo_red_dark
            )
            root.findViewById<LinearLayout>(R.id.btn_container).visibility = View.INVISIBLE
            exerciseType = SharedPreferenceUtil.getExerciseType(activity!!)!!
        }
        walkingButton.setOnClickListener {
            walkingButton.setBackgroundColor(ContextCompat.getColor(activity!!, R.color.selected))
            cyclingButton.setBackgroundColor(
                ContextCompat.getColor(
                    activity!!,
                    R.color.colorPrimary
                )
            )
            exerciseType = "Walking"
        }
        cyclingButton.setOnClickListener {
            cyclingButton.setBackgroundColor(ContextCompat.getColor(activity!!, R.color.selected))
            walkingButton.setBackgroundColor(
                ContextCompat.getColor(
                    activity!!,
                    R.color.colorPrimary
                )
            )
            exerciseType = "Cycling"
        }

        trackButton.setOnClickListener {
            val enabled = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_TRACKING_ENABLED, false
            )
            if (exerciseType == "") {
                Toast.makeText(
                    activity,
                    "Pick an exercise type!",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val serviceIntent = Intent(activity!!, TrackingService::class.java)
            if (enabled) {
                activity!!.stopService(serviceIntent)
                cyclingButton.setBackgroundColor(
                    ContextCompat.getColor(
                        activity!!,
                        R.color.colorPrimary
                    )
                )
                walkingButton.setBackgroundColor(
                    ContextCompat.getColor(
                        activity!!,
                        R.color.colorPrimary
                    )
                )
                root.findViewById<LinearLayout>(R.id.btn_container).visibility = View.VISIBLE
                (it as MaterialButton).text = getString(R.string.start_tracking_btn)
                it.backgroundTintList = ContextCompat.getColorStateList(
                    context!!,
                    android.R.color.holo_green_dark
                )
            } else {
                if (!permissionApproved()) {
                    requestForegroundPermissions()
                } else {
                    serviceIntent.putExtra("exercise_type", exerciseType)
                    activity!!.startForegroundService(serviceIntent)
                    root.findViewById<LinearLayout>(R.id.btn_container).visibility = View.INVISIBLE
                    (it as MaterialButton).text = getString(R.string.stop_tracking_btn)
                    it.backgroundTintList =
                        ContextCompat.getColorStateList(context!!, android.R.color.holo_red_dark)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() -> {
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    println("Permission granted!")
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
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) gravity = event.values
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) geomagnetic = event.values
        if (gravity != null && geomagnetic != null) {
            val rMat = FloatArray(9)
            val iMat = FloatArray(9)
            val success = SensorManager.getRotationMatrix(rMat, iMat, gravity, geomagnetic)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rMat, orientation)
                val azimuth = orientation[0]

                // convert azimuth to degree
                val degree = (-azimuth*360/(2*Math.PI)).toFloat()
                // accept 5 degrees changes only
                if (abs(degree - currentDegree) >= 5) {
                    val anim = RotateAnimation(
                        currentDegree,
                        degree,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                    )
                    anim.duration = 180
                    anim.fillAfter = true
                    image.startAnimation(anim)

                    currentDegree = degree
                }
            }
        }
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
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACTIVITY_RECOGNITION
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
                    Manifest.permission.ACTIVITY_RECOGNITION
                ),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
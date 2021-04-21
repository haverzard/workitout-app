package com.haverzard.workitout

import android.content.*
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.IBinder
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.haverzard.workitout.services.ScheduleService
import com.haverzard.workitout.services.SharedPreferenceUtil
import com.haverzard.workitout.services.TrackingService

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
//    private lateinit var sharedPreferences: SharedPreferences

    private var scheduleService: ScheduleService? = null
    private var scheduleServiceBound = false

    private val scheduleServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ScheduleService.LocalBinder
            scheduleService = binder.service
            scheduleServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            scheduleService = null
            scheduleServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_news, R.id.navigation_tracker, R.id.navigation_history, R.id.navigation_schedule))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()

        val scheduleIntent = Intent(this, ScheduleService::class.java)
        bindService(scheduleIntent, scheduleServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        if (scheduleServiceBound) {
            unbindService(scheduleServiceConnection)
            scheduleServiceBound = false
        }

        super.onStop()
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }
}

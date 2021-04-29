package com.haverzard.workitout

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.haverzard.workitout.util.NotificationHelper
import com.haverzard.workitout.util.SharedPreferenceUtil


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SharedPreferenceUtil.saveForegroundPref(this, true)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_news,
                R.id.navigation_tracker,
                R.id.navigation_history,
                R.id.navigation_schedule
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        goToHistory()

        var reset = true
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.activeNotifications.forEach {
            if (it.id == NotificationHelper.NOTIFICATION_TRACKER_ID) {
                reset = false
            }
        }
        if (reset) {
            SharedPreferenceUtil.saveTrackingPref(this, false)
            SharedPreferenceUtil.saveExerciseType(this, "")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }

    private fun goToHistory() {
        var historyId = intent.extras?.getLong("history_id")?.toInt()
        if (historyId == null) historyId = SharedPreferenceUtil.getHistoryId(this).toInt()
        if (historyId != -1) {
            val bundle = Bundle()
            bundle.putInt("history_id", historyId)
            findNavController(R.id.nav_host_fragment).navigate(R.id.navigation_history_detail, bundle)
            intent.removeExtra("history_id")
            SharedPreferenceUtil.saveHistoryId(this, -1L)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        goToHistory()
    }

    override fun onDestroy() {
        super.onDestroy()
        SharedPreferenceUtil.saveForegroundPref(this, false)
    }
}

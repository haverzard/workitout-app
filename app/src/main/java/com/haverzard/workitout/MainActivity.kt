package com.haverzard.workitout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.haverzard.workitout.services.SharedPreferenceUtil

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SharedPreferenceUtil.saveForegroundPref(this, false)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_news, R.id.navigation_tracker, R.id.navigation_history, R.id.navigation_schedule))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val historyId = intent.extras?.getLong("history_id")?.toInt()
        if (historyId != null) {
            val bundle = Bundle()
            bundle.putInt("history_id", historyId)
            navController.navigate(R.id.navigation_history_detail, bundle)
        }
    }

    override fun onDestroy() {
        SharedPreferenceUtil.saveForegroundPref(this, true)
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }
}

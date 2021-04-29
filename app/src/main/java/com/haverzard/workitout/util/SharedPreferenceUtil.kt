package com.haverzard.workitout.util

import android.content.Context
import com.haverzard.workitout.BuildConfig
import com.haverzard.workitout.R

object SharedPreferenceUtil {

    private const val KEY_AUTO_TRACK = "auto_track"
    private const val KEY_EXERCISE_TYPE = "exercise_type"
    private const val KEY_FOREGROUND_ACTIVITY = "foreground_activity"
    private const val KEY_HISTORY_ID = "history_id"
    const val KEY_TRACKING_ENABLED = "tracking"

    fun alertWindowEnabled(): Boolean {
        return BuildConfig.ENABLE_ALERT_WINDOW == "true"
    }

    fun getHistoryId(context: Context): Long =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).getLong(KEY_HISTORY_ID, -1L)

    fun saveHistoryId(context: Context, historyId: Long) {
        val editor = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).edit()
        editor.putLong(KEY_HISTORY_ID, historyId)
        editor.apply()
    }

    fun getForegroundPref(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).getBoolean(KEY_FOREGROUND_ACTIVITY, false)

    fun saveForegroundPref(context: Context, foreground: Boolean) {
        val editor = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).edit()
        editor.putBoolean(KEY_FOREGROUND_ACTIVITY, foreground)
        editor.apply()
    }

    fun getTracking(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).getBoolean(KEY_TRACKING_ENABLED, false)

    fun saveTrackingPref(context: Context, tracking: Boolean) {
        val editor = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE,
        ).edit()
        editor.putBoolean(KEY_TRACKING_ENABLED, tracking)
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
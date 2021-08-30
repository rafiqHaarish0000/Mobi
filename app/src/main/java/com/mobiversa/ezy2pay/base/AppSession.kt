package com.mobiversa.ezy2pay.base

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.PreferenceHelper
import org.jetbrains.anko.defaultSharedPreferences

class AppSession(context: Context) {

    companion object {
        fun getInstance(context: Context) = AppSession(context)
    }

    fun saveSession(key: String, value: String, context: Context) {
        val preferences =
            context.getSharedPreferences(Constants.Preferences.APP_SESSIONS, Context.MODE_PRIVATE)
                .edit()
        preferences.putString(key, value)
        preferences.apply()
    }

    fun saveSession(key: String, value: Long, context: Context) {
        val preferences =
            context.getSharedPreferences(Constants.Preferences.APP_SESSIONS, Context.MODE_PRIVATE)
                .edit()
        preferences.putLong(key, value)
        preferences.apply()
    }

    fun getSession(key: String, default: String, context: Context): String {
        val preferences =
            context.getSharedPreferences(Constants.Preferences.APP_SESSIONS, Context.MODE_PRIVATE)
        return preferences.getString(key, default)!!
    }

    fun getSession(key: String, default: Long, context: Context): Long {
        val preferences =
            context.getSharedPreferences(Constants.Preferences.APP_SESSIONS, Context.MODE_PRIVATE)
        return preferences.getLong(key, default)
    }

    fun clearAppSession(context: Context) {
        context.getSharedPreferences(Constants.Preferences.APP_SESSIONS, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
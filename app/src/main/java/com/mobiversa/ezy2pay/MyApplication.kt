package com.mobiversa.ezy2pay

import android.app.Application
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics.getInstance(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        Log.e(TAG, "onCreate: Build Type -> ${BuildConfig.BUILD_TYPE}")
    }

}
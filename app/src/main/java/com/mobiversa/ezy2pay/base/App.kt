package com.mobiversa.ezy2pay.base

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        setupExeceptionHandler()
    }

    private fun setupExeceptionHandler() {

    }

    companion object {
        lateinit var instance: App
            private set
    }
}
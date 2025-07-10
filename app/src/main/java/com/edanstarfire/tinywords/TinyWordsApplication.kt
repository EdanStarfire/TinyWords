package com.edanstarfire.tinywords

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TinyWordsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // You can do other application-wide initializations here if needed
    }
}
package com.leovp.android.packageinformation

import android.app.Application

/**
 * Author: Michael Leo
 * Date: 19-5-31 下午5:46
 */
class CustomApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: CustomApplication
            private set
    }
}
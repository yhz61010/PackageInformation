package com.leovp.android.packageinformation.utils

import android.content.Context
import com.leovp.android.packageinformation.CustomApplication
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 * Author: Michael Leo
 * Date: 19-6-10 上午11:35
 */
class CustomPreference<T>(val key: String, val default: T) : ReadWriteProperty<Any?, T> {

    private val TAG = CustomPreference::class.java.simpleName

    private val prefs by lazy {
        CustomApplication.instance.getSharedPreferences(
            "package_information",
            Context.MODE_PRIVATE
        )
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreference(key, value)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getPreference(key, default)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getPreference(key: String, default: T) = with(prefs) {
        val result: Any = when (default) {
            is Long -> getLong(key, default)
            is String -> getString(key, default)
            is Int -> getInt(key, default)
            is Boolean -> getBoolean(key, default)
            is Float -> getFloat(key, default)
            else -> throw IllegalArgumentException("This type can not be read")
        }
        result as T
    }

    private fun putPreference(key: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(key, value)
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            else -> throw IllegalArgumentException("This type can not be saved")
        }
    }.apply()
}

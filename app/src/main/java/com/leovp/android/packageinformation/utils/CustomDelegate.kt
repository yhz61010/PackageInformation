package com.leovp.android.packageinformation.utils

/**
 * Author: Michael Leo
 * Date: 19-6-10 下午1:46
 */
object CustomDelegate {
    fun <T> preference(key: String, default: T) = CustomPreference(key, default)
}

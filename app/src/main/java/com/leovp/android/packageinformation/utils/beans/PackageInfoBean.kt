package com.leovp.android.packageinformation.utils.beans

import android.graphics.drawable.Drawable

/**
 * Author: Michael Leo
 * Date: 19-5-17 上午11:37
 */
data class PackageInfoBean(
    val appIcon: Drawable,
    val appName: String,
    val appPackage: String,
    val appVersion: String = "",
    val appVersionCode: Long,
    val appSize: Long,
    val launchActivity: String,
    val isSystemApp: Boolean = false,

    val sha1: String,
    val md5: String,
    val installedDate: Long,
    val lastUpdateDate: Long,
    val sourceDir: String
)
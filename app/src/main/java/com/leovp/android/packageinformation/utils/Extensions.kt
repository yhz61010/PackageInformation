package com.leovp.android.packageinformation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * Author: Michael Leo
 * Date: 19-5-20 下午3:30
 */
fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

/**
 * Add the following permission in your `AndroidManifest.xml`:
 * ```xml
 * <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
 * ```
 *
 * https://stackoverflow.com/a/64946118/1685062
 */
@SuppressLint("QueryPermissionsNeeded")
fun Context.queryCompactIntentActivities(intent: Intent, flags: Int): List<ResolveInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.packageManager.queryIntentActivities(
            intent,
            PackageManager.ResolveInfoFlags.of(flags.toLong())
        )
    } else {
        this.packageManager.queryIntentActivities(intent, flags)
    }
}

fun Context.getCompactPackageInfo(packageName: String, flags: Int): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.packageManager.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(flags.toLong())
        )
    } else {
        this.packageManager.getPackageInfo(packageName, flags)
    }
}
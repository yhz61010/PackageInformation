package com.leovp.android.packageinformation.utils

import android.content.Context
import android.content.pm.PackageManager
import java.io.File
import java.text.DecimalFormat


/**
 * Author: Michael Leo
 * Date: 19-5-20 下午4:00
 */
object APKUtil {
    private val TAG = APKUtil::class.java.simpleName

    @JvmStatic fun getApkSize(context: Context, packageName: String): Long {
        var size: Long = 0
        try {
            /*
                publicSourceDir
                    Full path to the publicly available parts of sourceDir,
                    including resources and manifest.
             */
            size = File(context.packageManager.getApplicationInfo(packageName, 0).publicSourceDir).length()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return size
    }

    // Custom method to get human readable file size from bytes
    @JvmStatic fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        ) + " " + units[digitGroups]
    }
}
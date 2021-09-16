package com.leovp.android.packageinformation.utils

import android.content.Intent
import com.leovp.android.packageinformation.CustomApplication


/**
 * Author: Michael Leo
 * Date: 19-6-4 下午3:16
 */
object DeviceUtil {
    private val TAG = DeviceUtil::class.java.simpleName

    @JvmStatic
    fun dip2px(dipValue: Float): Int {
        val scale = CustomApplication.instance.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }


    @JvmStatic
    fun px2dip(pxValue: Float): Int {
        val scale = CustomApplication.instance.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    @JvmStatic
    fun getPackageName(): String {
        try {
            val pm = CustomApplication.instance.packageManager
            val packageInfo = pm.getPackageInfo(CustomApplication.instance.packageName, 0)
            return packageInfo.packageName
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    @JvmStatic
    fun restartApp() {
        val intent = CustomApplication.instance.packageManager.getLaunchIntentForPackage(getPackageName())
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        CustomApplication.instance.startActivity(intent)
    }
}
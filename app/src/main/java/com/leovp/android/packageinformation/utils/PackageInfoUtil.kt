package com.leovp.android.packageinformation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import com.leovp.android.packageinformation.utils.StringUtil.bytesToHex
import com.leovp.android.packageinformation.utils.beans.PackageInfoBean
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Author: Michael Leo
 * Date: 19-5-17 上午11:14
 */
object PackageInfoUtil {

    private val TAG = PackageInfoUtil::class.java.simpleName

    @SuppressLint("PackageManagerGetSignatures")
    @JvmStatic
    suspend fun getAllInstalledApp(ctx: Context): List<PackageInfoBean> = withContext(Dispatchers.IO) {
        val st = System.currentTimeMillis()
        val pm = ctx.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val appList: List<ResolveInfo> = ctx.queryCompactIntentActivities(mainIntent, 0)
        val allTasks = mutableListOf<Deferred<PackageInfoBean>>()

        for (app in appList) {
            allTasks.add(async {
                var isSystemApp = false
                if (app.activityInfo != null
                    && ((app.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM > 0)
                            || app.activityInfo.processName == ctx.packageName)
                ) {
                    isSystemApp = true
                }

                val pkgInfo: PackageInfo = ctx.getCompactPackageInfo(app.activityInfo.packageName, 0)
                val signatureListSha1: List<String>
                val signatureListMd5: List<String>
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // New signature
                    val sig = pm.getPackageInfo(app.activityInfo.packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
                    signatureListSha1 = if (sig.hasMultipleSigners()) {
                        // Send all with apkContentsSigners
                        sig.apkContentsSigners.map {
                            val digest = MessageDigest.getInstance("SHA")
                            digest.update(it.toByteArray())
                            StringUtil.formatHash(bytesToHex(digest.digest()))
                        }
                    } else {
                        // Send one with signingCertificateHistory
                        sig.signingCertificateHistory.map {
                            val digest = MessageDigest.getInstance("SHA")
                            digest.update(it.toByteArray())
                            StringUtil.formatHash(bytesToHex(digest.digest()))
                        }
                    }
                    signatureListMd5 = if (sig.hasMultipleSigners()) {
                        // Send all with apkContentsSigners
                        sig.apkContentsSigners.map {
                            val digest = MessageDigest.getInstance("MD5")
                            digest.update(it.toByteArray())
                            StringUtil.formatHash(bytesToHex(digest.digest()))
                        }
                    } else {
                        // Send one with signingCertificateHistory
                        sig.signingCertificateHistory.map {
                            val digest = MessageDigest.getInstance("MD5")
                            digest.update(it.toByteArray())
                            StringUtil.formatHash(bytesToHex(digest.digest()))
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val sig = pm.getPackageInfo(app.activityInfo.packageName, PackageManager.GET_SIGNATURES).signatures
                    signatureListSha1 = sig.map {
                        val digest = MessageDigest.getInstance("SHA")
                        digest.update(it.toByteArray())
                        StringUtil.formatHash(bytesToHex(digest.digest()))
                    }

                    signatureListMd5 = sig.map {
                        val digest = MessageDigest.getInstance("MD5")
                        digest.update(it.toByteArray())
                        StringUtil.formatHash(bytesToHex(digest.digest()))
                    }
                }
                val currentAppLaunchActivity = pm.getLaunchIntentForPackage(app.activityInfo.packageName)?.resolveActivity(pm)?.className ?: ""
                val currentApp = PackageInfoBean(
                    app.loadIcon(pm),
                    app.loadLabel(pm).toString(),
                    app.activityInfo.packageName,
                    pkgInfo.versionName,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pkgInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        pkgInfo.versionCode.toLong()
                    },
                    APKUtil.getApkSize(ctx, app.activityInfo.packageName),
                    currentAppLaunchActivity,
                    isSystemApp,
                    signatureListSha1.joinToString(separator = "\n"),
                    signatureListMd5.joinToString(separator = "\n"),
                    pkgInfo.firstInstallTime,
                    pkgInfo.lastUpdateTime,
                    app.activityInfo.applicationInfo.publicSourceDir
                )
                currentApp
            })
        }
        Log.d(TAG, "getAllInstalledApp. Cost: " + (System.currentTimeMillis() - st))
        allTasks.awaitAll()
    }
}
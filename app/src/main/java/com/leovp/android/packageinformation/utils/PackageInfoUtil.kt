package com.leovp.android.packageinformation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
        val appList: List<PackageInfo> = pm.getInstalledPackages(PackageManager.MATCH_ALL)
        val allTasks = mutableListOf<Deferred<PackageInfoBean>>()

        for (app in appList) {
            val appInfo = app.applicationInfo
            requireNotNull(appInfo) { "Application information must not be null." }
            allTasks.add(async {
                var isSystemApp = false
                if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM > 0)
                    || appInfo.processName == ctx.packageName) {
                    isSystemApp = true
                }

                val pkgInfo: PackageInfo = ctx.getCompactPackageInfo(app.packageName, 0)
                var signatureListSha1: List<String> = emptyList<String>()
                var signatureListMd5: List<String> = emptyList<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // New signature
                    val sig = pm.getPackageInfo(app.packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
                    if (sig != null) {
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
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val sig = pm.getPackageInfo(app.packageName, PackageManager.GET_SIGNATURES).signatures
                    if (sig != null) {
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
                }
                val currentAppLaunchActivity = pm.getLaunchIntentForPackage(app.packageName)?.resolveActivity(pm)?.className ?: ""
                val currentApp = PackageInfoBean(
                    appInfo.loadIcon(pm),
                    appInfo.loadLabel(pm).toString(),
                    app.packageName,
                    pkgInfo.versionName ?: "",
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pkgInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        pkgInfo.versionCode.toLong()
                    },
                    APKUtil.getApkSize(ctx, app.packageName),
                    currentAppLaunchActivity,
                    isSystemApp,
                    signatureListSha1.joinToString(separator = "\n"),
                    signatureListMd5.joinToString(separator = "\n"),
                    pkgInfo.firstInstallTime,
                    pkgInfo.lastUpdateTime,
                    appInfo.publicSourceDir
                )
                currentApp
            })
        }
        Log.d(TAG, "getAllInstalledApp. Cost: " + (System.currentTimeMillis() - st))
        allTasks.awaitAll()
    }
}
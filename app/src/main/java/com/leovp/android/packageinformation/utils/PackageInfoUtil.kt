package com.leovp.android.packageinformation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.leovp.android.packageinformation.utils.StringUtil.bytesToHex
import com.leovp.android.packageinformation.utils.beans.PackageInfoBean
import java.security.MessageDigest

/**
 * Author: Michael Leo
 * Date: 19-5-17 上午11:14
 */
object PackageInfoUtil {

    private val TAG = PackageInfoUtil::class.java.simpleName

    @SuppressLint("PackageManagerGetSignatures")
    @JvmStatic
    fun getAllInstalledApp(ctx: Context): MutableList<PackageInfoBean> {
        val st = System.currentTimeMillis()
        val pm = ctx.packageManager
        val packages =
//            pm.getInstalledPackages(PackageManager.GET_ACTIVITIES or PackageManager.GET_META_DATA)
            pm.getInstalledPackages(0)

        var isSystemApp: Boolean
        var currentAppLaunchActivity: String
        var currentApp: PackageInfoBean
        val allPackages: MutableList<PackageInfoBean> = mutableListOf()

        for (packageInfo in packages) {
            isSystemApp = false
            if ((packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                isSystemApp = true
            }

            var signatureListSha1: List<String>
            var signatureListMd5: List<String>
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // New signature
                val sig =
                    pm.getPackageInfo(packageInfo.packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
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
                val sig = pm.getPackageInfo(packageInfo.packageName, PackageManager.GET_SIGNATURES).signatures
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

            currentAppLaunchActivity =
                pm.getLaunchIntentForPackage(packageInfo.packageName)?.resolveActivity(pm)?.className ?: ""
            currentApp = PackageInfoBean(
                packageInfo.applicationInfo.loadIcon(pm),
                packageInfo.applicationInfo.loadLabel(pm).toString(),
                packageInfo.packageName,
                packageInfo.versionName ?: "",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else (packageInfo.versionCode.toLong()),
                APKUtil.getApkSize(ctx, packageInfo.packageName),
                currentAppLaunchActivity,
                isSystemApp,
                signatureListSha1.joinToString(separator = "\n"),
                signatureListMd5.joinToString(separator = "\n"),
                packageInfo.firstInstallTime,
                packageInfo.lastUpdateTime,
                packageInfo.applicationInfo.sourceDir
            )

            allPackages.add(currentApp)
        }
        Log.d(TAG, "getAllInstalledApp. Cost: " + (System.currentTimeMillis() - st))

        return allPackages
    }
}
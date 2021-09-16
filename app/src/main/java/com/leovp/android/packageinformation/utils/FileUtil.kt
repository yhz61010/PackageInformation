package com.leovp.android.packageinformation.utils

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.leovp.android.packageinformation.CustomApplication
import java.io.File


/**
 * Author: Michael Leo
 * Date: 19-6-4 下午7:14
 */
object FileUtil {
    private val TAG = FileUtil::class.java.simpleName

    enum class FOLDER_TYPE(var type: String) {
        APP("app"), BAK("bak"), ICON("icon")
    }

    @JvmStatic
    fun getUriForFile(file: File): Uri {
        Log.i(TAG, "getUriForFile File: $file")
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                CustomApplication.instance,
                DeviceUtil.getPackageName() + ".fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
        Log.i(TAG, "getUriForFile result: $uri")
        return uri
    }

    @JvmStatic
    fun copyFileToSdCard(srcFile: File, folderType: FOLDER_TYPE, targetFileName: String): File {
        val sdCard = Environment.getExternalStorageDirectory()
        val dir = File(sdCard.absolutePath + "/leo-package-info/${folderType.type}/")
        dir.mkdirs()

        val inputStream = srcFile.inputStream()
        val targetFile = File(dir, targetFileName)
        targetFile.delete()

//        var srcFileLength = srcFile.length().let {
//            if (it > Int.MAX_VALUE) throw OutOfMemoryError("File $this is too big ($it bytes) to fit in memory.") else it
//        }.toInt()

        val buffer = ByteArray(8 * 1024)
        var offset = 0
        var len = 0
        while ({ offset = inputStream.read(buffer); len += offset;offset }() > 0) {
            Log.i(TAG, "File length written: $len")
            targetFile.appendBytes(buffer.copyOfRange(0, offset))
        }

//        return srcFile.copyTo(targetFile, true)
        return targetFile
    }

    @JvmStatic
    fun exportAppIconToSdCard(bmp: Bitmap, iconName: String): File {
        val sdCard = Environment.getExternalStorageDirectory()
        val dir = File(sdCard.absolutePath + "/leo-package-info/icon/")
        dir.mkdirs()

        val file = File(dir, iconName)
        val fos = file.outputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        return file
    }
}
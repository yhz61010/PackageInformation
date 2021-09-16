package com.leovp.android.packageinformation.utils

/**
 * Author: Michael Leo
 * Date: 19-6-3 上午10:29
 */
object StringUtil {
    @JvmStatic
    fun bytesToHex(bytes: ByteArray): String {
        val hexArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
        val hexChars = CharArray(bytes.size * 2)
        var v: Int
        for (j in bytes.indices) {
            v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v.ushr(4)]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    @JvmStatic
    fun formatHash(hash: String): String {
        var formattedHash = mutableListOf<String>()
        for (i in hash.indices step 2) {
            formattedHash.add(hash.substring(i, i + 2))
        }
        return formattedHash.joinToString(":")
    }
}
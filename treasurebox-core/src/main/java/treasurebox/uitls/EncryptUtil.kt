package treasurebox.uitls

import java.security.MessageDigest

/**
 * @author : chenjimou
 * @Description : 加密相关工具类
 * @Date : 2024/06/03
 */
object EncryptUtil {
    /**
     * 生成md5值（32位）
     */
    fun encrypt32ToMD5(str: String): String {
        // 加密后的16进制字符串
        var hexStr = ""
        try {
            // 此 MessageDigest 类为应用程序提供信息摘要算法的功能
            val md5 = MessageDigest.getInstance("MD5")
            // 转换为MD5码
            val digest = md5.digest(str.toByteArray(charset("utf-8")))
            hexStr = NumberSystemConvert.toHexString(digest)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return hexStr
    }

    /**
     * 生成md5值（16位）
     */
    fun encrypt16ToMD5(str: String): String {
        return encrypt32ToMD5(str).substring(8, 24)
    }


    /**
     * 加密解密算法 执行一次加密，两次解密
     */
    fun convertMD5(inStr: String): String {
        val a = inStr.toCharArray()
        for (i in a.indices) {
            a[i] = (a[i].code xor 't'.code).toChar()
        }
        return String(a)
    }
}

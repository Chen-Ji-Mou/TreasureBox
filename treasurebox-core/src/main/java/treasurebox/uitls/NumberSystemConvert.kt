package treasurebox.uitls

import java.util.Locale

/**
 * @author : chenjimou
 * @Description : 进制转换相关工具类
 * @Date : 2024/06/03
 */
object NumberSystemConvert {
    /**
     * byte转10进制
     */
    fun bytesToDecimal(src: ByteArray, end: Int): IntArray? {
        val values = IntArray(src.size)
        if (src.isEmpty() || end > src.size) {
            return null
        }
        for (i in 0 until end) {
            if (i in 1..64) {
                values[i] = src[i].toInt() + 128
            } else {
                val v = (src[i].toInt()) and 0xff
                values[i] = v
            }
        }
        return values
    }

    /**
     * 16进制转byte
     */
    fun HexToByte(hexString: String): ByteArray {
        val len = hexString.length
        val b = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (((hexString[i].digitToIntOrNull(16)
                ?: (-1 shl 4)) + (hexString[i + 1].digitToIntOrNull(16) ?: -1))).toByte()
            i += 2
        }
        return b
    }

    /**
     * byte转为16进制字符
     */
    fun toHexString(byteArray: ByteArray?): String {
        if (byteArray == null || byteArray.isEmpty()) {
            return "null"
        }

        val hexString = StringBuilder()
        for (i in byteArray.indices) {
            //0~F前面不零
            if ((byteArray[i].toInt() and 0xff) < 0x10) {
                hexString.append("0")
            }
            hexString.append(Integer.toHexString(0xFF and byteArray[i].toInt()))
        }
        return hexString.toString().lowercase(Locale.getDefault())
    }
}

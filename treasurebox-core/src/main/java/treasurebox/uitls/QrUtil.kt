package treasurebox.uitls

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

/**
 * @author heqinze@skyworth.com
 * @description
 * @date 2023/11/7
 */
object QrUtil {
    val HINTS: MutableMap<EncodeHintType, Any?> = EnumMap(
        EncodeHintType::class.java
    )

    init {
        HINTS[EncodeHintType.CHARACTER_SET] = "utf-8"
        HINTS[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        HINTS[EncodeHintType.MARGIN] = 0
    }

    /**
     * 根据content 内容生成二维码 异步调用
     *
     * @param content
     * @param size
     * @return
     */
    fun encodeQrCode(content: String?, size: Int, logo: Bitmap?): Bitmap? {
        try {
            val matrix =
                MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, HINTS)
            val pixels = IntArray(size * size)
            for (y in 0 until size) {
                for (x in 0 until size) {
                    if (matrix[x, y]) {
                        pixels[y * size + x] = Color.BLACK
                    } else {
                        pixels[y * size + x] = Color.WHITE
                    }
                }
            }
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
            return addLogoToQrcode(bitmap, logo)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun addLogoToQrcode(src: Bitmap, logo: Bitmap?): Bitmap? {
        if (logo == null) {
            return src
        }
        val srcWidth = src.width
        val srcHeight = src.height
        val logoWidth = logo.width
        val logoHeight = logo.height

        val scaleFactor = srcWidth * 1.0f / 5 / logoWidth
        var bitmap: Bitmap? = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap!!)
            canvas.drawBitmap(src, 0f, 0f, null)
            //            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(
                logo,
                ((srcWidth - logoWidth) / 2).toFloat(),
                ((srcHeight - logoHeight) / 2).toFloat(),
                null
            )
            canvas.save()
            canvas.restore()
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap = null
        }
        return bitmap
    }
}

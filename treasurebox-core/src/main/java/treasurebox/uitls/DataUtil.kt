package treasurebox.uitls

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID


/**
 * @author chenjimou
 * @description 数据相关工具类
 * @date 2024/06/03
 */
object DataUtil {
    /**
     * json文件转换成 json字符串
     */
    fun parseJsonToStr(resId: Int, context: Context): String {
        val isr: InputStreamReader?
        val message = StringBuilder()
        val bufReader: BufferedReader?
        try {
            val `is` = context.resources.openRawResource(resId)
            isr = InputStreamReader(`is`)
            bufReader = BufferedReader(isr)
            var line: String?
            while (bufReader.readLine().also { line = it } != null) {
                message.append(line!!.replace("\\n", "\n"))
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return message.toString()
    }

    /**
     * 根据id字符串获取 R.xxx.xxx（int） defType：mipmap、string ...
     */
    @SuppressLint("DiscouragedApi")
    fun getResIdByStr(context: Context, name: String, defType: String) =
        context.resources.getIdentifier(name, defType, context.packageName)

    /**
     * 将 R.xxx.xxx（int）转换成 R.xxx.xxx（string）
     */
    fun getStrByResId(context: Context, resId: Int) =
        "R.${context.resources.getResourceTypeName(resId)}.${
            context.resources.getResourceEntryName(resId)
        }"

    /**
     * 保存bitmap至sdcard
     *
     * @param path 路径为：//storage/emulated/0/Android/data/{package_name}/files/{path}
     * @param fileName 文件名称，默认通过UUID生成
     * @return 完整的文件路径，不为空则保存成功
     */
    fun saveBitmapToSdcard(
        context: Context, bitmap: Bitmap, path: String? = null, fileName: String? = null
    ): String? {
        val filePath = "${context.getExternalFilesDir(path)}/${fileName ?: UUID.randomUUID()}.jpg"
        val fos = FileOutputStream(File(filePath))
        return try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            filePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            fos.close()
        }
    }

    /**
     * 根据宽高计算图片的采样率
     */
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        // 如果Bitmap的宽高大于View的宽高
        if (height > reqHeight || width > reqWidth) {
            // 均除2
            val halfHeight = height / 2
            val halfWidth = width / 2
            // 进行遍历计算
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                // inSampleSize逐渐*2
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 按照imageview的centerCrop模式裁剪bitmap生成新bitmap
     *
     * @param srcBitmap 原bitmap
     * @param desWidth 目标bitmap宽度
     * @param desHeight 目标bitmap高度
     */
    fun centerCrop(
        srcBitmap: Bitmap, desWidth: Int, desHeight: Int
    ): Bitmap {
        val srcWidth = srcBitmap.width
        val srcHeight = srcBitmap.height
        var newWidth = srcWidth
        var newHeight = srcHeight
        val srcRate = srcWidth.toFloat() / srcHeight
        val desRate = desWidth.toFloat() / desHeight
        var dx = 0
        var dy = 0
        if (srcRate == desRate) {
            return srcBitmap
        } else if (srcRate > desRate) {
            newWidth = (srcHeight * desRate).toInt()
            dx = (srcWidth - newWidth) / 2
        } else {
            newHeight = (srcWidth / desRate).toInt()
            dy = (srcHeight - newHeight) / 2
        }
        //创建目标Bitmap，并用选取的区域来绘制
        return Bitmap.createBitmap(srcBitmap, dx, dy, newWidth, newHeight)
    }
}
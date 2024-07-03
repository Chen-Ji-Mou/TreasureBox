package treasurebox.blur.rs.impl

import android.content.Context
import android.graphics.Bitmap

/**
 * @author chenjimou
 * @description TODO
 * @date 2024/7/3
 */
internal interface BaseRSImpl {
    fun prepare(context: Context, buffer: Bitmap, radius: Float): Boolean
    fun blur(input: Bitmap, output: Bitmap)
    fun release()
}
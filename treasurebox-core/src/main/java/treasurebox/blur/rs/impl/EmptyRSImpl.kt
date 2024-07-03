package treasurebox.blur.rs.impl

import android.content.Context
import android.graphics.Bitmap
import treasurebox.blur.rs.impl.BaseRSImpl

/**
 * @author chenjimou
 * @description TODO
 * @date 2024/7/3
 */
internal class EmptyRSImpl : BaseRSImpl {
    override fun prepare(context: Context, buffer: Bitmap, radius: Float): Boolean {
        return false
    }

    override fun blur(input: Bitmap, output: Bitmap) {
    }

    override fun release() {
    }
}
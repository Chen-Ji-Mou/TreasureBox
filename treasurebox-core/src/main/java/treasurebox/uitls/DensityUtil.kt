package treasurebox.uitls

import android.content.res.Resources
import android.util.TypedValue

/**
 * @author chenjimou
 * @description 屏幕适配相关工具类
 * @date 2024/06/03
 */
object DensityUtil {
    @JvmStatic
    fun dp2px(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics
        )
    }
}

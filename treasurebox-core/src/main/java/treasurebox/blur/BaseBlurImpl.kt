package treasurebox.blur

import android.graphics.Bitmap
import android.graphics.Color
import android.view.ViewGroup
import androidx.annotation.ColorInt

/**
 * @author chenjimou
 * @description 高斯模糊实现基类
 * @date 2024/7/2
 */
internal interface BaseBlurImpl {
    fun init(parent: ViewGroup)
    fun setBlurBitmap(bitmap: Bitmap)
    fun setBlurRadius(radius: Float)
    fun setBlurSamplingRate(rate: Float)
    fun setBlurOverlayColor(@ColorInt color: Int)
    fun refreshBlurOverlay()
    fun setCornerRadius(radius: Float)
    fun setBorder(width: Float, @ColorInt color: Int = Color.TRANSPARENT)
    fun showBlurOrNot(isShow: Boolean)
    fun release()
}
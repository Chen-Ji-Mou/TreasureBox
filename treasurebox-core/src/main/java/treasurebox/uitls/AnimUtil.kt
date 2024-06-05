package treasurebox.uitls

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import treasurebox.uitls.DensityUtil.dp2px
import treasurebox.uitls.UiCompat.CompatAnimatorUpdateListener

/**
 * @author chenjimou
 * @description v1.0
 * @date
 */
object AnimUtil {
    private var shakeAnim: ObjectAnimator? = null
    private var scaleAnim: AnimatorSet? = null

    @JvmStatic
    fun showEdgeShakeAnim(view: View, keyCode: Int) {
        if (null == shakeAnim) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> shakeAnim = nopeX(view)
                KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_UP -> shakeAnim = nopeY(view)
            }
            if (null == shakeAnim) return
            shakeAnim?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    shakeAnim = null
                }
            })
        }
        if (shakeAnim?.isRunning == false) {
            shakeAnim?.start()
        }
    }

    @JvmStatic
    fun showClickAnim(view: View, runnable: (() -> Unit)? = null) {
        if (null != scaleAnim && scaleAnim?.isRunning == true) return
        val scale = getScaleRatio(view)
        scaleAnim = AnimatorSet()
        val a1 = ObjectAnimator.ofFloat(view, "scaleX", 1f, scale, 1f)
        val a2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, scale, 1f)
        scaleAnim?.setDuration(200)
        scaleAnim?.interpolator = DecelerateInterpolator()
        scaleAnim?.playTogether(a1, a2)
        scaleAnim?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                scaleAnim = null
                runnable?.let { it() }
            }

            override fun onAnimationCancel(animator: Animator) {
                scaleAnim = null
                runnable?.let { it() }
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        scaleAnim?.start()
    }

    @JvmOverloads
    @JvmStatic
    fun focusAnimate(view: View, hasFocus: Boolean, scale: Float = getScaleRatio(view)) {
        var scaleNew = scale
        if (hasFocus) {
            if (view.animate() != null) {
                view.animate().cancel()
            }
            val duration: Long = 140
            view.animate().scaleX(scaleNew).scaleY(scaleNew).setDuration(duration).setInterpolator(
                DecelerateInterpolator()
            ).start()
        } else {
            scaleNew = 1.0f
            if (view.animate() != null) view.animate().cancel()
            val duration: Long = 140
            view.animate().scaleX(scaleNew).scaleY(scaleNew).setDuration(duration).setInterpolator(
                DecelerateInterpolator()
            ).start()
        }
    }

    /**
     * 边缘横向抖动动画
     */
    private fun nopeX(view: View): ObjectAnimator {
        val delta = dp2px(6f).toInt()

        val kf0 = Keyframe.ofFloat(0f, 0f)
        val kf1 = Keyframe.ofFloat(.1f, -delta.toFloat())
        val kf2 = Keyframe.ofFloat(.2f, 0f)
        val kf3 = Keyframe.ofFloat(.3f, delta.toFloat())
        val kf4 = Keyframe.ofFloat(.4f, 0f)
        val kf5 = Keyframe.ofFloat(.5f, -delta.toFloat())
        val kf6 = Keyframe.ofFloat(.6f, 0f)
        val kf7 = Keyframe.ofFloat(.7f, delta.toFloat())
        val kf8 = Keyframe.ofFloat(.8f, 0f)
        val kf9 = Keyframe.ofFloat(.9f, -delta.toFloat())
        val kf10 = Keyframe.ofFloat(1f, 0f)

        val pvhTranslateX = PropertyValuesHolder.ofKeyframe(
            View.TRANSLATION_X, kf0, kf1, kf2, kf3, kf4, kf5, kf6, kf7, kf8, kf9, kf10
        )

        val animator = ObjectAnimator.ofPropertyValuesHolder(view, pvhTranslateX).setDuration(500)
        return animator
    }

    /**
     * 边缘竖向抖动动画
     */
    private fun nopeY(view: View): ObjectAnimator {
        val delta = dp2px(6f).toInt()

        val kf0 = Keyframe.ofFloat(0f, 0f)
        val kf1 = Keyframe.ofFloat(.1f, -delta.toFloat())
        val kf2 = Keyframe.ofFloat(.2f, 0f)
        val kf3 = Keyframe.ofFloat(.3f, delta.toFloat())
        val kf4 = Keyframe.ofFloat(.4f, 0f)
        val kf5 = Keyframe.ofFloat(.5f, -delta.toFloat())
        val kf6 = Keyframe.ofFloat(.6f, 0f)
        val kf7 = Keyframe.ofFloat(.7f, delta.toFloat())
        val kf8 = Keyframe.ofFloat(.8f, 0f)
        val kf9 = Keyframe.ofFloat(.9f, -delta.toFloat())
        val kf10 = Keyframe.ofFloat(1f, 0f)

        val pvhTranslateY = PropertyValuesHolder.ofKeyframe(
            View.TRANSLATION_Y, kf0, kf1, kf2, kf3, kf4, kf5, kf6, kf7, kf8, kf9, kf10
        )

        val animator = ObjectAnimator.ofPropertyValuesHolder(view, pvhTranslateY).setDuration(500)
        return animator
    }

    private fun getScaleRatio(view: View): Float {
        val w = view.width
        val h = view.height
        var scale = 1f
        if (w > h) { //宽大于高
            if (w > dp2px(1760f)) {
                scale = 1.01f
            } else if (dp2px(861f) <= w && w <= dp2px(1760f)) {
                scale = 1.02f
            } else if (dp2px(561f) <= w && w < dp2px(861f)) {
                scale = 1.04f
            } else if (dp2px(411f) <= w && w < dp2px(561f)) {
                scale = 1.06f
            } else if (dp2px(0f) < w && w < dp2px(411f)) {
                scale = 1.08f
            }
        } else if (w < h) { //高大于宽
            if (h > dp2px(568f)) {
                scale = 1.01f
            } else if (dp2px(445f) <= h && h <= dp2px(568f)) {
                scale = 1.05f
            } else if (dp2px(361f) <= h && h < dp2px(445f)) {
                scale = 1.06f
            } else if (dp2px(0f) < h && h < dp2px(361f)) {
                scale = 1.07f
            }
        } else { //高等于宽
            if (w > dp2px(568f)) {
                scale = 1.01f
            } else if (dp2px(411f) <= w && w <= dp2px(568f)) {
                scale = 1.04f
            } else if (dp2px(321f) <= w && w < dp2px(411f)) {
                scale = 1.05f
            } else if (dp2px(261f) <= w && w < dp2px(321f)) {
                scale = 1.07f
            } else if (dp2px(0f) < w && w < dp2px(261f)) {
                scale = 1.08f
            }
        }
        return scale
    }
}

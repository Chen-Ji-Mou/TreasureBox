package treasurebox.blur.rs

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.core.view.isVisible
import treasurebox.blur.BaseBlurImpl
import treasurebox.blur.rs.impl.AndroidStockRSImpl
import treasurebox.blur.rs.impl.AndroidXRSImpl
import treasurebox.blur.rs.impl.BaseRSImpl
import treasurebox.blur.rs.impl.EmptyRSImpl
import treasurebox.blur.rs.impl.SupportLibraryRSImpl
import treasurebox.uitls.DensityUtil

/**
 * @author chenjimou
 * @description TODO
 * @date 2024/7/2
 */
internal class RSBlurImpl @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs), BaseBlurImpl {
    private lateinit var mParent: ViewGroup
    private var mDecorView: View? = null
    private lateinit var mRSImpl: BaseRSImpl
    private var mBitmapToBlur: Bitmap? = null
    private var mBitmapToBlurCanvas: Canvas? = null
    private var mBlurredBitmap: Bitmap? = null
    private val mRectSrc = Rect()
    private val mRectFDst = RectF()
    private val mCornerPath = Path()
    private val mOverlayPaint: Paint = Paint().also {
        it.isAntiAlias = true
    }
    private val mBorderPaint: Paint = Paint().also {
        it.style = Paint.Style.STROKE
        it.isAntiAlias = true
    }
    private val mCornerRadii = floatArrayOf(DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS)
    private var mCornerRids: FloatArray? = null
    private var mOverlayColor: Int = Color.TRANSPARENT
    /**
     * default 10dp (0 < r <= 25)
     */
    private var mBlurRadius: Float = 10.0f
    private var mNeedDraw: Boolean = false
    private var mNeedPrepare: Boolean = false

    override fun init(parent: ViewGroup) {
        this.mParent = parent
        this.mRSImpl = getBlurImpl()
        parent.addView(this, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    override fun setBlurBitmap(bitmap: Bitmap) {
        // TODO 仅模糊传入的bitmap，不处理decorView
    }

    override fun setBlurRadius(radius: Float) {
        if (radius > 25.0f) {
            this.mBlurRadius = 25.0f
        } else {
            this.mBlurRadius = radius
        }
        refreshBlurOverlay()
    }

    override fun setBlurOverlayColor(color: Int) {
        this.mOverlayColor = color
        refreshBlurOverlay()
    }

    override fun refreshBlurOverlay() {
        this.mNeedDraw = true
        invalidate()
    }

    override fun setCornerRadius(radius: Float) {
        for ((index, _) in mCornerRadii.withIndex()) {
            mCornerRadii[index] = radius
        }
        initCornerRids()
        refreshBlurOverlay()
    }

    override fun setBorder(width: Float, color: Int) {
        mBorderPaint.strokeWidth = DensityUtil.dp2px(width)
        mBorderPaint.color = color
        refreshBlurOverlay()
    }

    override fun release() {
        if (mBitmapToBlur != null) {
            mBitmapToBlur?.recycle()
            mBitmapToBlur = null
            mBitmapToBlurCanvas = null
        }
        if (mBlurredBitmap != null) {
            mBlurredBitmap?.recycle()
            mBlurredBitmap = null
        }
        mRSImpl.release()
        mDecorView = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow")
        mDecorView = getActivityDecorView()
    }

    private fun getActivityDecorView(): View? {
        var ctx = context
        var i = 0
        while (i < 4 && ctx !is Activity && ctx is ContextWrapper) {
            ctx = ctx.baseContext
            i++
        }
        return if (ctx is Activity) {
            ctx.window.decorView
        } else {
            null
        }
    }

    private fun getBlurImpl(): BaseRSImpl {
        if (BLUR_IMPL == 0) {
            // try to use stock impl first
            try {
                val impl = AndroidStockRSImpl()
                val bmp = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
                impl.prepare(context, bmp, 4f)
                impl.release()
                bmp.recycle()
                BLUR_IMPL = 3
            } catch (e: Throwable) {
                // class not found or unsatisfied link
            }
        }
        if (BLUR_IMPL == 0) {
            try {
                javaClass.classLoader?.loadClass("androidx.renderscript.RenderScript")
                // initialize RenderScript to load jni impl
                // may throw unsatisfied link error
                val impl = AndroidXRSImpl()
                val bmp = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
                impl.prepare(context, bmp, 4f)
                impl.release()
                bmp.recycle()
                BLUR_IMPL = 1
            } catch (e: Throwable) {
                // class not found or unsatisfied link
            }
        }
        if (BLUR_IMPL == 0) {
            try {
                javaClass.classLoader?.loadClass("android.support.v8.renderscript.RenderScript")
                // initialize RenderScript to load jni impl
                // may throw unsatisfied link error
                val impl = SupportLibraryRSImpl()
                val bmp = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
                impl.prepare(context, bmp, 4f)
                impl.release()
                bmp.recycle()
                BLUR_IMPL = 2
            } catch (e: Throwable) {
                // class not found or unsatisfied link
            }
        }
        if (BLUR_IMPL == 0) {
            // fallback to empty impl, which doesn't have blur effect
            BLUR_IMPL = -1
        }
        Log.d(TAG, "BLUR_IMPL = $BLUR_IMPL")
        return when (BLUR_IMPL) {
            1 -> AndroidXRSImpl()
            2 -> SupportLibraryRSImpl()
            3 -> AndroidStockRSImpl()
            else -> EmptyRSImpl()
        }
    }

    private fun initCornerRids() {
        if (mCornerRids == null) {
            mCornerRids = floatArrayOf(
                mCornerRadii[0], mCornerRadii[0],
                mCornerRadii[1], mCornerRadii[1],
                mCornerRadii[2], mCornerRadii[2],
                mCornerRadii[3], mCornerRadii[3]
            )
        } else {
            mCornerRids?.set(0, mCornerRadii[0])
            mCornerRids?.set(1, mCornerRadii[0])
            mCornerRids?.set(2, mCornerRadii[1])
            mCornerRids?.set(3, mCornerRadii[1])
            mCornerRids?.set(4, mCornerRadii[2])
            mCornerRids?.set(5, mCornerRadii[2])
            mCornerRids?.set(6, mCornerRadii[3])
            mCornerRids?.set(7, mCornerRadii[3])
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (mNeedDraw) {
            if (mBitmapToBlur == null || mBitmapToBlurCanvas == null || mBlurredBitmap == null) {
                this.mNeedPrepare = true
            }

            if (mBitmapToBlur == null) {
                mBitmapToBlur = Bitmap.createBitmap(
                    width, height, Bitmap.Config.ARGB_8888
                )
            }

            if (mBitmapToBlurCanvas == null) {
                mBitmapToBlurCanvas = Canvas(mBitmapToBlur!!)
            }

            if (mBlurredBitmap == null) {
                mBlurredBitmap = Bitmap.createBitmap(
                    width, height, Bitmap.Config.ARGB_8888
                )
            }

            if (mNeedPrepare) {
                if (mRSImpl.prepare(context, mBitmapToBlur!!, mBlurRadius)) {
                    this.mNeedPrepare = false
                } else {
                    this.mNeedDraw = false
                    return
                }
            }

            val locations = IntArray(2)
            mDecorView?.getLocationOnScreen(locations)
            var x = -locations[0]
            var y = -locations[1]
            getLocationOnScreen(locations)
            x += locations[0]
            y += locations[1]

            Log.d(TAG, "onLayout [$x,$y]")

            this.visibility = INVISIBLE

            if (mParent.childCount > 1) {
                for (i in 1 until mParent.childCount) {
                    val childView = mParent.getChildAt(i)
                    if (childView.isVisible) {
                        childView.visibility = INVISIBLE
                    }
                }
            }

            val rc = mBitmapToBlurCanvas!!.save()
            try {
                mBitmapToBlurCanvas!!.translate((-x).toFloat(), (-y).toFloat())
                if (mDecorView?.background != null) {
                    mDecorView?.background?.draw(mBitmapToBlurCanvas!!)
                }
                mDecorView?.draw(mBitmapToBlurCanvas!!)
            } catch (e: Throwable) {
                Log.d(TAG, Log.getStackTraceString(e))
            } finally {
                mBitmapToBlurCanvas!!.restoreToCount(rc)
            }

            if (mParent.childCount > 1) {
                for (i in 1 until mParent.childCount) {
                    val childView = mParent.getChildAt(i)
                    if (!childView.isVisible) {
                        childView.visibility = VISIBLE
                    }
                }
            }

            this.visibility = VISIBLE

            mRSImpl.blur(mBitmapToBlur!!, mBlurredBitmap!!)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mNeedDraw) {
            //圆角的半径，依次为左上角xy半径，右上角，右下角，左下角
            mRectFDst.right = width.toFloat()
            mRectFDst.bottom = height.toFloat()

            /*向路径中添加圆角矩形。radii数组定义圆角矩形的四个圆角的x,y半径。radii长度必须为8*/
            //Path.Direction.CW：clockwise ，沿顺时针方向绘制,Path.Direction.CCW：counter-clockwise ，沿逆时针方向绘制
            mCornerRids?.let {
                mCornerPath.addRoundRect(mRectFDst, it, Path.Direction.CW)
                mCornerPath.close()
                canvas.clipPath(mCornerPath)
            }

            mRectSrc.right = mBlurredBitmap!!.width
            mRectSrc.bottom = mBlurredBitmap!!.height
            canvas.drawBitmap(mBlurredBitmap!!, mRectSrc, mRectFDst, null)

            mOverlayPaint.color = mOverlayColor
            canvas.drawRect(mRectFDst, mOverlayPaint)

            canvas.drawPath(mCornerPath, mBorderPaint)
        }
    }

    companion object {
        private val TAG = this::class.java.simpleName
        private const val DEFAULT_RADIUS = 0.0f
        private var BLUR_IMPL: Int = 0
    }
}
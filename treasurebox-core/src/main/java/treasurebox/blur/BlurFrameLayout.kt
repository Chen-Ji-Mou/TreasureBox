package treasurebox.blur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import treasurebox.blur.gl.GLBlurImpl
import treasurebox.blur.rs.RSBlurImpl
import treasurebox.core.R

/**
 * @author chenjimou
 * @description TODO
 * @date 2024/6/18
 */
class BlurFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var blurRenderType: BlurRenderType = BlurRenderType.RENDERSCRIPT
    private val blurImpl: BaseBlurImpl

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BlurFrameLayout)

        val type = typedArray.getInt(R.styleable.BlurFrameLayout_blur_render_type, 0)
        this.blurRenderType = when (type) {
            1 -> BlurRenderType.RENDERSCRIPT
            else -> BlurRenderType.OPENGL
        }

        blurImpl = when (blurRenderType) {
            BlurRenderType.OPENGL -> GLBlurImpl(context, attrs)
            BlurRenderType.RENDERSCRIPT -> RSBlurImpl(context, attrs)
        }
        blurImpl.init(this)

        val blurRadius = typedArray.getInteger(R.styleable.BlurFrameLayout_blur_radius, 10)
        if (blurRadius != 10) {
            setBlurRadius(blurRadius.toFloat())
        }

        val blurBitmap = typedArray.getResourceId(R.styleable.BlurFrameLayout_blur_bitmap, 0)
        if (blurBitmap != 0) {
            setBlurResource(blurBitmap)
        }

        val blurOverlayColor = typedArray.getColor(
            R.styleable.BlurFrameLayout_blur_overlay_color, Color.TRANSPARENT
        )
        if (blurOverlayColor != Color.TRANSPARENT) {
            setBlurOverlayColor(blurOverlayColor)
        }

        val cornerRadius =
            typedArray.getDimension(R.styleable.BlurFrameLayout_corner_radius, 0.0f)
        if (cornerRadius != 0.0f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setCornerRadius(cornerRadius)
        }

        val borderWidth =
            typedArray.getDimension(R.styleable.BlurFrameLayout_border_width, 0.0f)
        if (borderWidth != 0.0f) {
            setBorder(cornerRadius)
        }

        val borderColor = typedArray.getColor(
            R.styleable.BlurFrameLayout_border_color, Color.TRANSPARENT
        )
        if (borderWidth != 0.0f && borderColor != Color.TRANSPARENT) {
            setBorder(borderWidth, borderColor)
        }

        typedArray.recycle()
    }

    fun setBlurDrawable(drawable: Drawable) {
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        if (width == -1 || height == -1) {
            Log.e(TAG, "Drawable must have width and height.")
            return
        }
        val config =
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        val bitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        setBlurBitmap(bitmap)
    }

    fun setBlurResource(resId: Int) {
        val options = BitmapFactory.Options()
        options.inScaled = false
        // Read in the resource
        val bitmap = BitmapFactory.decodeResource(
            context.resources, resId, options
        )
        if (bitmap == null) {
            Log.e(TAG, "Resource could not be decoded.")
            return
        }
        setBlurBitmap(bitmap)
    }

    fun setBlurBitmap(bitmap: Bitmap) {
        blurImpl.setBlurBitmap(bitmap)
    }

    fun setBlurRadius(radius: Float) {
        blurImpl.setBlurRadius(radius)
    }

    fun setBlurOverlayColor(@ColorInt color: Int) {
        blurImpl.setBlurOverlayColor(color)
    }

    fun refreshBlurOverlay() {
        blurImpl.refreshBlurOverlay()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setCornerRadius(radius: Float) {
        blurImpl.setCornerRadius(radius)
    }

    fun setBorder(width: Float, color: Int = Color.TRANSPARENT) {
        blurImpl.setBorder(width, color)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "onDetachedFromWindow")
        blurImpl.release()
    }

    companion object {
        private val TAG = this::class.java.simpleName
    }
}
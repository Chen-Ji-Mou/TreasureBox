package treasurebox.blur.gl

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import treasurebox.blur.BaseBlurImpl
import treasurebox.core.R
import treasurebox.uitls.DensityUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.LinkedList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author chenjimou
 * @description TODO
 * @date 2024/7/2
 */
internal class GLBlurImpl @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), BaseBlurImpl {
    private lateinit var parent: ViewGroup
    private var decorView: View? = null
    private val renderView = GLSurfaceView(context)
    private val render = GLBlurRender(context)
    private val overlayView = View(context)
    private var overlayColor: Int = Color.TRANSPARENT
    private var overlayBitmap: Bitmap? = null
    private var overlayBitmapCanvas: Canvas? = null
    private var needOverlayDraw: Boolean = false
    private var cornerRadius: Float = 0.0f

    override fun init(parent: ViewGroup) {
        this.parent = parent
        this.addView(this, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        renderView.run {
            visibility = INVISIBLE

            setEGLContextClientVersion(3)
            setEGLConfigChooser(8, 8, 8, 8, 16, 0)
            debugFlags = GLSurfaceView.DEBUG_CHECK_GL_ERROR or GLSurfaceView.DEBUG_LOG_GL_CALLS

            setRenderer(render)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }
        this.addView(
            overlayView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        overlayView.visibility = INVISIBLE
        parent.addView(this, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    override fun setBlurBitmap(bitmap: Bitmap) {
        Log.d(TAG, "setBlurBitmap ${render.isInitialized}")
        render.setBlurBitmap(bitmap)
        if (render.isInitialized) {
            renderView.requestRender()
        }
    }

    override fun setBlurRadius(radius: Float) {
        Log.d(TAG, "setBlurRadius ${render.isInitialized}")
        render.setBlurRadius(radius)
        if (render.isInitialized) {
            renderView.requestRender()
        }
    }

    override fun setBlurOverlayColor(color: Int) {
        Log.d(TAG, "setBlurOverlayColor ${render.isInitialized}")
        this.overlayColor = color
        overlayView.setBackgroundColor(color)
        refreshBlurOverlay()
    }

    override fun refreshBlurOverlay() {
        this.needOverlayDraw = true
        requestLayout()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun setCornerRadius(radius: Float) {
        this.cornerRadius = radius
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    0, 0, view.width, view.height, DensityUtil.dp2px(radius)
                )
            }
        }
        clipToOutline = true
    }

    override fun setBorder(width: Float, color: Int) {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        if (cornerRadius != 0.0f) {
            drawable.cornerRadius = DensityUtil.dp2px(cornerRadius)
        }
        if (overlayColor != Color.TRANSPARENT) {
            drawable.setColor(overlayColor)
        }
        drawable.setStroke(DensityUtil.dp2px(width).toInt(), color)
        overlayView.background = drawable
        refreshBlurOverlay()
    }

    override fun release() {
        render.release()
        if (overlayBitmap != null) {
            overlayBitmap?.recycle()
            overlayBitmap = null
            overlayBitmapCanvas = null
        }
        decorView = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow")
        decorView = getActivityDecorView()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        Log.d(TAG, "isSelf = ${changedView == this}, visibility = $visibility")
        if (visibility == VISIBLE) {
            render.onResume()
            renderView.onResume()
        } else {
            renderView.onPause()
            render.onPause()
        }
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

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (needOverlayDraw) {
            if (overlayBitmap == null) {
                overlayBitmap = Bitmap.createBitmap(
                    width, height, Bitmap.Config.ARGB_8888
                )
            }

            if (overlayBitmapCanvas == null) {
                overlayBitmapCanvas = Canvas(overlayBitmap!!)
            }

            val locations = IntArray(2)
            decorView?.getLocationOnScreen(locations)
            var x = -locations[0]
            var y = -locations[1]
            getLocationOnScreen(locations)
            x += locations[0]
            y += locations[1]

            Log.d(TAG, "onLayout [$x,$y]")

            if (renderView.isVisible) {
                renderView.visibility = INVISIBLE
            }

            if (overlayView.isVisible) {
                overlayView.visibility = INVISIBLE
            }

            if (parent.childCount > 1) {
                for (i in 1 until parent.childCount) {
                    val childView = parent.getChildAt(i)
                    if (childView.isVisible) {
                        childView.visibility = INVISIBLE
                    }
                }
            }

            val rc = overlayBitmapCanvas!!.save()
            try {
                overlayBitmapCanvas!!.translate((-x).toFloat(), (-y).toFloat())
                if (decorView?.background != null) {
                    decorView?.background?.draw(overlayBitmapCanvas!!)
                }
                decorView?.draw(overlayBitmapCanvas!!)
            } catch (e: Throwable) {
                Log.d(TAG, Log.getStackTraceString(e))
            } finally {
                overlayBitmapCanvas!!.restoreToCount(rc)
            }

            if (parent.childCount > 1) {
                for (i in 1 until parent.childCount) {
                    val childView = parent.getChildAt(i)
                    if (!childView.isVisible) {
                        childView.visibility = VISIBLE
                    }
                }
            }

            overlayView.visibility = VISIBLE

            setBlurBitmap(overlayBitmap!!)

            this.needOverlayDraw = false
        }
    }

    companion object {
        private val TAG = this::class.java.simpleName
    }
}

private class GLBlurRender(private val context: Context) : GLSurfaceView.Renderer {
    private var imageTextureRef: Int = GLHelper.NO_TEXTURE
    private val position = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f,
    )
    private val texturePosition = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
    )
    private var programId: Int = 0
    private var positionId: Int = 0
    private val positionBuffer by lazy {
        ByteBuffer.allocateDirect(position.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            .also { it.put(position) }
    }
    private var texturePositionId: Int = 0
    private val texturePositionBuffer by lazy {
        ByteBuffer.allocateDirect(texturePosition.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer().also { it.put(texturePosition) }
    }
    private var imageTextureId: Int = 0
    private var blurRadiusId = 0

    private var bitmap: Bitmap? = null
    private var blurRadius: Float = 10.0f

    private val runOnInit = LinkedList<Runnable>()
    private val runOnDraw = LinkedList<Runnable>()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val vertexCodeStr = GLHelper.loadRaw(context.resources, R.raw.blur_vertex)
        val fragmentCodeStr = GLHelper.loadRaw(context.resources, R.raw.blur_fragment)

        programId = GLHelper.loadProgram(vertexCodeStr, fragmentCodeStr)

        Log.d(TAG, "programId = $programId")

        positionId = GLES30.glGetAttribLocation(programId, "inputPosition")
        texturePositionId = GLES30.glGetAttribLocation(programId, "inputTexturePosition")
        imageTextureId = GLES30.glGetUniformLocation(programId, "inputImageTexture")
        blurRadiusId = GLES30.glGetUniformLocation(programId, "blurRadius")

        synchronized(runOnInit) {
            while (!runOnInit.isEmpty()) {
                runOnInit.removeFirst().run()
            }
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        val displayMetrics = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(
            displayMetrics
        )

        Log.d(TAG, "onSurfaceChanged ${displayMetrics.widthPixels} ${displayMetrics.heightPixels}")

        val screenWidthId = GLES30.glGetUniformLocation(programId, "screenWidth")
        GLES30.glUniform1f(screenWidthId, displayMetrics.widthPixels.toFloat())

        val screenHeightId = GLES30.glGetUniformLocation(programId, "screenHeight")
        GLES30.glUniform1f(screenHeightId, displayMetrics.heightPixels.toFloat())
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        GLES30.glUseProgram(programId)

        synchronized(runOnDraw) {
            while (!runOnDraw.isEmpty()) {
                runOnDraw.removeFirst().run()
            }
        }

        Log.d(TAG, "imageTextureRef = $imageTextureRef")

        if (imageTextureRef != GLHelper.NO_TEXTURE) {
            positionBuffer.position(0)
            GLES30.glVertexAttribPointer(positionId, 2, GLES30.GL_FLOAT, false, 0, positionBuffer)
            GLES30.glEnableVertexAttribArray(positionId)

            texturePositionBuffer.position(0)
            GLES30.glVertexAttribPointer(
                texturePositionId, 2, GLES30.GL_FLOAT, false, 0, texturePositionBuffer
            )
            GLES30.glEnableVertexAttribArray(texturePositionId)

            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTextureRef)
            GLES30.glUniform1i(imageTextureId, 0)

            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

            GLES30.glDisableVertexAttribArray(positionId)
            GLES30.glDisableVertexAttribArray(texturePositionId)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        }
    }

    fun setBlurBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
        if (!isInitialized) {
            synchronized(runOnInit) {
                if (!runOnInit.contains(setBitmapTextureTask)) {
                    runOnInit.addLast(setBitmapTextureTask)
                }
            }
        } else {
            synchronized(runOnDraw) {
                if (!runOnDraw.contains(setBitmapTextureTask)) {
                    runOnDraw.addLast(setBitmapTextureTask)
                }
            }
        }
    }

    fun setBlurRadius(radius: Float) {
        this.blurRadius = radius
        if (!isInitialized) {
            synchronized(runOnInit) {
                if (!runOnInit.contains(setBlurRadiusTask)) {
                    runOnInit.addLast(setBlurRadiusTask)
                }
            }
        } else {
            synchronized(runOnDraw) {
                if (!runOnDraw.contains(setBlurRadiusTask)) {
                    runOnDraw.addLast(setBlurRadiusTask)
                }
            }
        }
    }

    fun onResume() {
        synchronized(runOnInit) {
            if (!runOnInit.contains(setBitmapTextureTask)) {
                runOnInit.addLast(setBitmapTextureTask)
            }
            if (!runOnInit.contains(setBlurRadiusTask)) {
                runOnInit.addLast(setBlurRadiusTask)
            }
        }
    }

    fun onPause() {
        imageTextureRef = GLHelper.NO_TEXTURE
        programId = 0
        positionId = 0
        texturePositionId = 0
        imageTextureId = 0
        blurRadiusId = 0
    }

    fun release() {
        bitmap?.recycle()
        bitmap = null
    }

    val isInitialized: Boolean get() = programId != 0

    private val setBitmapTextureTask = Runnable {
        if (bitmap != null) {
            val textureId = GLHelper.loadTexture(bitmap!!, imageTextureRef, false)
            if (textureId != 0) {
                this.imageTextureRef = textureId
            }
        } else {
            GLES30.glDeleteTextures(1, IntArray(imageTextureRef), 0)
            this.imageTextureRef = GLHelper.NO_TEXTURE
        }
    }

    private val setBlurRadiusTask = Runnable {
        GLES30.glUniform1f(blurRadiusId, blurRadius * 1.5f)
    }

    companion object {
        private val TAG = this::class.java.simpleName
    }
}
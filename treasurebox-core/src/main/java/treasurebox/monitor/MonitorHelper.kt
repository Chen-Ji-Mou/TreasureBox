package treasurebox.monitor

import android.os.Looper
import android.util.Log
import android.view.Choreographer

/**
 * @author chenjimou
 * @description TODO
 * @date 2024/3/21
 */
object MonitorHelper {
    const val TAG = "BlockWarning"

    fun monitoringUIThread() {
        Looper.getMainLooper().setMessageLogging(MonitorLooperPrinter.instance)
    }

    fun monitorFrameDraw() {
        Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
            var mLastFrameNanos: Long = 0L
            override fun doFrame(frameTimeNanos: Long) {
                if (mLastFrameNanos == 0L) {
                    mLastFrameNanos = frameTimeNanos
                }
                val cost = (frameTimeNanos - mLastFrameNanos) / 1_000_000.0f
                if (cost > 100f) {
                    Log.w(TAG, "frame draw block appear (spent ${cost}ms over 100ms) :")
                    for (stack in StackCache.getStackTrace(mLastFrameNanos, frameTimeNanos)) {
                        Log.w(TAG, stack)
                    }
                }
                mLastFrameNanos = frameTimeNanos
                Choreographer.getInstance().postFrameCallback(this)
            }
        })
    }
}
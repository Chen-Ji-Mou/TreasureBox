package treasurebox.monitor

import android.os.Looper
import android.util.Log
import android.view.Choreographer
import java.util.concurrent.Executors

/**
 * @author chenjimou
 * @date 2024/3/21
 */
object MonitorHelper {
    /**
     * 开启UI主线程卡顿监控
     * @param blockThreshold 判定为卡顿的最低耗时阈值
     * @param stackCostThreshold 判定堆栈为超时的最低耗时阈值
     */
    @JvmStatic
    fun startMonitorUIThread(blockThreshold: Long? = null, stackCostThreshold: Long? = null) {
        if (blockThreshold != null) {
            MonitorLooperPrinter.instance.setBlockThreshold(blockThreshold)
        }
        if (stackCostThreshold != null) {
            StackCache.setStackCostThreshold(stackCostThreshold)
        }
        Looper.getMainLooper().setMessageLogging(MonitorLooperPrinter.instance)
    }

    /**
     * 关闭UI主线程卡顿监控
     */
    @JvmStatic
    fun stopMonitorUIThread() {
        Looper.getMainLooper().setMessageLogging(null)
    }

    private const val DEFAULT_BLOCK_THRESHOLD: Long = 500L
    private var blockThreshold: Long = DEFAULT_BLOCK_THRESHOLD

    private val frameCallback = object : Choreographer.FrameCallback {
        var mLastFrameNanos: Long = 0L
        override fun doFrame(frameTimeNanos: Long) {
            if (mLastFrameNanos == 0L) {
                mLastFrameNanos = frameTimeNanos
            }
            val cost = (frameTimeNanos - mLastFrameNanos) / 1_000_000
            if (cost > blockThreshold) {
                for ((index, stack) in StackCache.getStackTrace(mLastFrameNanos, frameTimeNanos)
                    .withIndex()) {
                    if (index == 0) {
                        MonitorLog.w("frame draw block appear (spent ${cost}ms over ${blockThreshold}ms)")
                    }
                    MonitorLog.w(stack)
                }
            }
            mLastFrameNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    /**
     * 开启绘制帧卡顿监控
     * @param blockThreshold 判定为卡顿的最低耗时阈值
     * @param stackCostThreshold 判定堆栈为超时的最低耗时阈值
     */
    @JvmStatic
    fun startMonitorFrameDraw(blockThreshold: Long? = null, stackCostThreshold: Long? = null) {
        if (blockThreshold != null) {
            this.blockThreshold = blockThreshold
        }
        if (stackCostThreshold != null) {
            StackCache.setStackCostThreshold(stackCostThreshold)
        }
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    /**
     * 关闭绘制帧卡顿监控
     */
    @JvmStatic
    fun stopMonitorFrameDraw() {
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}
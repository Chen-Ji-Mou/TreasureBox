package treasurebox.monitor

import android.util.Log
import java.util.concurrent.Executors

/**
 * @author chenjimou
 * @description 由于发生卡顿时所采集到的堆栈可能会比较多，直接用logcat输出会导致主线程卡顿，致使堆栈耗时不准确，因此使用线程池并发输出日志。
 * @date 2024/12/11
 */
internal object MonitorLog {

    private const val TAG = "BlockWarning"

    /**
     * 高并发线程池，保证Log都能输出
     */
    private val ioThread = Executors.newCachedThreadPool()

    fun i(content: String) {
        ioThread.execute { Log.i(TAG, content) }
    }

    fun d(content: String) {
        ioThread.execute { Log.d(TAG, content) }
    }

    fun w(content: String) {
        ioThread.execute { Log.w(TAG, content) }
    }

    fun e(content: String) {
        ioThread.execute { Log.e(TAG, content) }
    }
}
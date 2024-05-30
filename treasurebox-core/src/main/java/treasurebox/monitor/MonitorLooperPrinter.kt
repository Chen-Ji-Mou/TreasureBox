package treasurebox.monitor

import android.util.Log
import android.util.Printer
import treasurebox.monitor.MonitorHelper.TAG

/**
 * @author chenjimou
 * @description TODO
 * @date 2024/5/30
 */
class MonitorLooperPrinter private constructor() : Printer {
    private var mEnterMillis: Long = 0L

    override fun println(log: String?) {
        val logNotNull = log ?: return
        if (logNotNull.startsWith(">>>>>")) {
            mEnterMillis = System.nanoTime()
        } else if (logNotNull.startsWith("<<<<<")) {
            val exitMillis = System.nanoTime()
            val cost = (exitMillis - mEnterMillis) / 1_000_000
            if (cost >= BLOCK_THRESHOLD) {
                Log.w(TAG, "ui thread block appear (spent ${cost}ms over ${BLOCK_THRESHOLD}ms) :")
                for (stack in StackCache.getStackTrace(mEnterMillis, exitMillis)) {
                    Log.w(TAG, stack)
                }
            }
            mEnterMillis = 0L
        }
    }

    companion object {
        private const val BLOCK_THRESHOLD: Long = 1000L
        val instance by lazy(lock = LazyThreadSafetyMode.SYNCHRONIZED) { MonitorLooperPrinter() }
    }
}
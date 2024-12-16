package treasurebox.monitor

import android.util.Printer

/**
 * @author chenjimou
 * @date 2024/5/30
 */
internal class MonitorLooperPrinter private constructor() : Printer {
    private var enterMillis: Long = 0L
    private var blockThreshold: Long = DEFAULT_BLOCK_THRESHOLD

    override fun println(log: String?) {
        val logNotNull = log ?: return
        if (logNotNull.startsWith(">>>>>")) {
            enterMillis = System.nanoTime()
        } else if (logNotNull.startsWith("<<<<<")) {
            val exitMillis = System.nanoTime()
            val cost = (exitMillis - enterMillis) / 1_000_000
            if (cost >= blockThreshold) {
                for ((index, stack) in StackCache.getStackTrace(enterMillis, exitMillis).withIndex()) {
                    if (index == 0) {
                        MonitorLog.w("ui thread block appear (spent ${cost}ms over ${blockThreshold}ms)")
                    }
                    MonitorLog.w(stack)
                }
            }
            enterMillis = 0L
        }
    }

    fun setBlockThreshold(millis: Long) {
        blockThreshold = millis
    }

    companion object {
        private const val DEFAULT_BLOCK_THRESHOLD: Long = 500L
        val instance by lazy(lock = LazyThreadSafetyMode.SYNCHRONIZED) { MonitorLooperPrinter() }
    }
}
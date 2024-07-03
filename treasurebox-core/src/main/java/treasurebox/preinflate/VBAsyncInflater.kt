package treasurebox.preinflate

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.core.util.Pools
import java.util.concurrent.ArrayBlockingQueue

/**
 * @author chenjimou
 * @description 异步加载Inflater，基于AsyncLayoutInflater修改
 * @date 2023/11/28
 */
internal class VBAsyncInflater private constructor() {
    private lateinit var mInflateThread: InflateThread
    private val mHandler: Handler

    @UiThread
    fun syncInflate(parent: ViewGroup, operate: VBInflateOperate<*>) {
        Log.d(TAG, "[syncInflate] operate: ${operate.javaClass.simpleName}")
        operate.inflate(parent)
    }

    @UiThread
    fun asyncInflate(
        parent: ViewGroup, operate: VBInflateOperate<*>, callback: () -> Unit
    ) {
        Log.d(TAG, "[asyncInflate] operate: ${operate.javaClass.simpleName}")
        val request = mInflateThread.obtainRequest()
        request.inflater = this
        request.parent = parent
        request.operate = operate
        request.callback = callback
        mInflateThread.enqueue(request)
    }

    private val mHandlerCallback = Handler.Callback { msg ->
        val request = msg.obj as InflateRequest
        if (request.operate?.isInflated == false) {
            request.operate?.inflate(request.parent)
        }
        request.callback?.let { it() }
        mInflateThread.releaseRequest(request)
        true
    }

    init {
        mHandler = Handler(Looper.getMainLooper(), mHandlerCallback)
        mInflateThread = InflateThread.instance
    }

    private data class InflateRequest(
        var inflater: VBAsyncInflater? = null,
        var parent: ViewGroup? = null,
        var operate: VBInflateOperate<*>? = null,
        var callback: (() -> Unit)? = null
    )

    private class InflateThread private constructor() : Thread() {
        private val mQueue = ArrayBlockingQueue<InflateRequest>(10)
        private val mRequestPool = Pools.SynchronizedPool<InflateRequest>(10)

        init {
            start()
        }

        // Extracted to its own method to ensure locals have a constrained liveness
        // scope by the GC. This is needed to avoid keeping previous request references
        // alive for an indeterminate amount of time, see b/33158143 for details
        private fun runInner() {
            val request: InflateRequest = try {
                mQueue.take()
            } catch (ex: InterruptedException) {
                // Odd, just continue
                Log.w(TAG, ex)
                return
            }
            Log.d(TAG, "[runInner] operate: ${request.operate?.javaClass?.simpleName}")
            try {
                request.operate?.inflate(request.parent)
            } catch (ex: RuntimeException) {
                // Probably a Looper failure, retry on the UI thread
                Log.w(
                    TAG,
                    "Failed to inflate resource in the background! Retrying on the UI thread",
                    ex
                )
            }
            Message.obtain(request.inflater?.mHandler, 0, request).sendToTarget()
        }

        override fun run() {
            while (true) {
                runInner()
            }
        }

        fun obtainRequest(): InflateRequest {
            var obj = mRequestPool.acquire()
            if (obj == null) {
                obj = InflateRequest()
            }
            return obj
        }

        fun releaseRequest(obj: InflateRequest) {
            obj.inflater = null
            obj.operate = null
            obj.parent = null
            obj.callback = null
            mRequestPool.release(obj)
        }

        fun enqueue(request: InflateRequest) {
            try {
                mQueue.put(request)
            } catch (e: InterruptedException) {
                throw RuntimeException(
                    "Failed to enqueue async inflate request", e
                )
            }
        }

        companion object {
            val instance: InflateThread by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
                InflateThread()
            }
        }
    }

    companion object {
        private val TAG = VBAsyncInflater::class.java.simpleName
        val instance: VBAsyncInflater by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            VBAsyncInflater()
        }
    }
}
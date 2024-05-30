package treasurebox.preinflate

import android.util.Log
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * @author chenjimou
 * @description 预加载ViewBinding帮助类，封装接口
 * @date 2023/11/28
 */
class PreInflateHelper<Binding : ViewBinding> {
    private val mViewCache = VBInflateCache<Binding>()
    private val mLayoutInflater = VBAsyncInflater.instance

    @JvmOverloads
    fun preloadOnce(
        parent: ViewGroup, operate: VBInflateOperate<Binding>, maxCount: Int = DEFAULT_PRELOAD_COUNT
    ) {
        preload(parent, arrayListOf(operate), maxCount, 1)
    }

    @JvmOverloads
    fun preload(
        parent: ViewGroup,
        operates: ArrayList<out VBInflateOperate<Binding>>,
        maxCount: Int = DEFAULT_PRELOAD_COUNT,
        forcePreCount: Int = 0
    ) {
        val viewsAvailableCount = mViewCache.getViewBindingPoolAvailableCount()
        if (viewsAvailableCount >= maxCount) {
            Log.d(
                TAG, "[preload] the cache has reached its maximum and cannot be preloaded anymore!"
            )
            return
        }
        var needPreloadCount = maxCount - viewsAvailableCount
        if (forcePreCount > 0) {
            needPreloadCount = forcePreCount.coerceAtMost(needPreloadCount)
        }
        Log.d(
            TAG,
            "[preload] operate: ${operates.first().javaClass.simpleName} needPreloadCount: $needPreloadCount, viewsAvailableCount: $viewsAvailableCount"
        )
        if (operates.size != needPreloadCount) {
            Log.d(
                TAG,
                "[preload] the number of operates does not match the target number of loads (needPreloadCount), so preloading cannot be performed!"
            )
            return
        }
        for (i in 0 until needPreloadCount) {
            preAsyncInflateView(parent, operates[i])
        }
    }

    private fun preAsyncInflateView(
        parent: ViewGroup, operate: VBInflateOperate<Binding>
    ) {
        mLayoutInflater.asyncInflate(parent, operate) {
            mViewCache.putViewBinding(operate.binding)
        }
    }

    fun getViewBinding(
        parent: ViewGroup, operate: VBInflateOperate<Binding>
    ): Binding {
        return getViewBinding(parent, operate, DEFAULT_PRELOAD_COUNT)
    }

    fun getViewBinding(
        parent: ViewGroup, operate: VBInflateOperate<Binding>, maxCount: Int
    ): Binding {
        val bindingCache = mViewCache.getViewBinding()
        if (bindingCache != null) {
            Log.d(TAG, "[getViewBinding] get viewBinding from cache!")
            preloadOnce(parent, operate, maxCount)
            return bindingCache
        }
        mLayoutInflater.syncInflate(parent, operate)
        return operate.binding
    }

    companion object {
        private val TAG = PreInflateHelper::class.java.simpleName

        /**
         * 默认的预加载缓存池大小，默认是3，可根据需求设置
         */
        const val DEFAULT_PRELOAD_COUNT = 3
    }
}
package treasurebox.preinflate

import androidx.viewbinding.ViewBinding
import java.lang.ref.SoftReference
import java.util.LinkedList

/**
 * @author chenjimou
 * @description 已加载ViewBinding的缓存
 * @date 2023/11/28
 */
internal class VBInflateCache<Binding : ViewBinding> {
    private val mViewBindingPools = LinkedList<SoftReference<Binding>>()
    fun getViewBindingPoolAvailableCount(): Int {
        val it = mViewBindingPools.iterator()
        var count = 0
        while (it.hasNext()) {
            if (it.next().get() != null) {
                count++
            } else {
                it.remove()
            }
        }
        return count
    }

    fun putViewBinding(binding: Binding) {
        mViewBindingPools.offer(SoftReference(binding))
    }

    fun getViewBinding(): Binding? {
        return getViewBindingFromPool(mViewBindingPools)
    }

    private fun getViewBindingFromPool(bindings: LinkedList<SoftReference<Binding>>): Binding? {
        return if (bindings.isEmpty()) {
            null
        } else {
            bindings.pop().get() ?: return getViewBindingFromPool(bindings)
        }
    }
}
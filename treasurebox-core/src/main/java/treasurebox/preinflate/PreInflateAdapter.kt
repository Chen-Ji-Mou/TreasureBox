package treasurebox.preinflate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * @author chenjimou
 * @description 预加载ViewBinding模板Adapter
 * @date 2023/11/29
 */
@Suppress("LeakingThis")
abstract class PreInflateAdapter<Binding : ViewBinding>(parent: ViewGroup) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflateHelper = PreInflateHelper<Binding>()

    init {
        val operates = arrayListOf<VBInflateOperate<Binding>>()
        for (i in 0 until PreInflateHelper.DEFAULT_PRELOAD_COUNT) {
            operates.add(inflateOperate)
        }
        inflateHelper.preload(parent, operates)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        val binding = inflateHelper.getViewBinding(parent, inflateOperate)
        return onCreateViewHolder(binding, viewType)
    }

    abstract fun onCreateViewHolder(binding: Binding, viewType: Int): RecyclerView.ViewHolder

    abstract val inflateOperate: VBInflateOperate<Binding>
}
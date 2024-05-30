package treasurebox.preinflate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

/**
 * @author chenjimou
 * @description 封装加载ViewBinding操作的抽象类
 * @date 2023/11/28
 */
@Suppress("UNCHECKED_CAST")
abstract class VBInflateOperate<Binding : ViewBinding> {
    lateinit var binding: Binding

    fun inflate(parent: ViewGroup?) {
        val parameterizedType = javaClass.genericSuperclass as ParameterizedType
        val genericType = parameterizedType.actualTypeArguments.first() as Class<*>
        val inflateMethod = genericType.getDeclaredMethod(
            "inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
        )
        val result = inflateMethod.invoke(
            null, LayoutInflater.from(parent?.context), parent, false
        )
        binding = result as Binding
    }

    val isInflated: Boolean get() = this::binding.isInitialized
}
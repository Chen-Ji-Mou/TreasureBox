package treasurebox.monitor

import android.os.Looper
import kotlin.math.ceil

/**
 * @author chenjimou
 * @description 用于缓存堆栈数据的单例，由于@see[methodEnter]和@see[methodExit]需要被插桩入外部代码，因此该类不能加internal。外部不能直接调用该类函数。
 * @date 2024/4/18
 */
object StackCache {
    private const val STACK_CACHE_SIZE: Int = 5000000
    private var STACK_COST_THRESHOLD: Long = 50L
    private val stackCacheMap = object : LinkedHashMap<Long, MethodRecord>(
        STACK_CACHE_SIZE, 0.75f, true
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, MethodRecord>?): Boolean {
            return size > STACK_CACHE_SIZE
        }
    }

    /**
     * 设置判定堆栈为超时的最低耗时阈值，用于过滤出耗时较大的堆栈
     */
    @JvmStatic
    internal fun setStackCostThreshold(threshold: Long) {
        STACK_COST_THRESHOLD = threshold
    }

    @JvmStatic
    internal fun getStackTrace(begin: Long, end: Long): MutableList<String> {
        val stackTraces = mutableListOf<String>()

        // 按时间戳排序并筛选出在时间段内的函数执行记录（MethodRecord）
        val matchedStack = stackCacheMap.entries.sortedBy { it.key }.filter { it.key in begin..end }

        // 整合MethodRecord转化为函数执行堆栈（Stack）
        val rootStacks = mutableListOf<Stack>()
        val parentStack = mutableListOf<Stack>()
        var currentStack: Stack? = null

        for ((timestamp, entity) in matchedStack) {
            when (entity.type) {
                1 -> {
                    // 开始阶段
                    val newStack =
                        Stack(entity.name, startTime = timestamp / 1_000_000, parent = currentStack)
                    if (currentStack != null) {
                        currentStack.children.add(newStack)
                        parentStack.add(currentStack)
                    }
                    currentStack = newStack
                    if (currentStack.parent == null) {
                        // 如果当前阶段没有父阶段，则将其添加到根阶段列表中
                        rootStacks.add(currentStack)
                    }
                }

                2 -> {
                    // 结束阶段
                    if (currentStack != null) {
                        currentStack.endTime = timestamp / 1_000_000
                        currentStack.duration = timestamp / 1_000_000 - currentStack.startTime
                        currentStack = if (parentStack.isNotEmpty()) {
                            parentStack.removeLast()
                        } else {
                            null
                        }
                    }
                }
            }
        }

        // 可能由于所截取时间段内有部分Stack缺失了结束的MethodRecord导致duration为0，因此根据Stack的children进行统计
        fun fillDuration(stacks: MutableList<Stack>): Long {
            var duration = 0L
            for (stack in stacks) {
                duration += if (stack.duration != 0L) {
                    stack.duration
                } else {
                    fillDuration(stack.children)
                }
            }
            return duration
        }

        for (rootStack in rootStacks) {
            if (rootStack.duration == 0L) {
                rootStack.duration = fillDuration(rootStack.children)
            }
        }

        // 根据设定的堆栈耗时阈值（STACK_COST_THRESHOLD）筛选耗时较大的堆栈信息，精简打印
        fun deleteUseless(stacks: MutableList<Stack>) {
            val iterator = stacks.iterator()
            while (iterator.hasNext()) {
                val stack = iterator.next()
                if (stack.duration < STACK_COST_THRESHOLD) {
                    iterator.remove()
                } else {
                    deleteUseless(stack.children)
                }
            }
        }

        val iterator = rootStacks.iterator()
        while (iterator.hasNext()) {
            val rootStack = iterator.next()
            if (rootStack.duration < STACK_COST_THRESHOLD) {
                iterator.remove()
            } else {
                deleteUseless(rootStack.children)
            }
        }

        // 深度优先遍历总堆栈树（rootStacks）整理日志打印message
        fun printChildren(sb: StringBuilder, stacks: MutableList<Stack>, count: Int) {
            for (stack in stacks) {
                val content = "> ${stack.name} cost: ${stack.duration}ms\n"
                if (sb.length + count + content.length > 4096) {
                    stackTraces.add(sb.toString())
                    sb.clear()
                }
                for (i in 0 until count) {
                    sb.append("-")
                }
                sb.append("> ${stack.name} cost: ${stack.duration}ms\n")
                printChildren(sb, stack.children, count + 1)
            }
        }

        val sb = StringBuilder()
        for (rootStack in rootStacks.sortedByDescending { it.duration }) {
            val content = "-> ${rootStack.name} cost: ${rootStack.duration}ms\n"
            if (sb.length + content.length > 4096) {
                stackTraces.add(sb.toString())
                sb.clear()
            }
            sb.append(content)
            printChildren(sb, rootStack.children, 2)
        }

        // 如果堆栈信息过少未超过logcat单条日志长度限制（4096）
        if (stackTraces.isEmpty() && sb.isNotEmpty()) {
            stackTraces.add(sb.toString())
        }

        return stackTraces
    }

    @JvmStatic
    fun methodEnter(methodName: String) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            stackCacheMap[System.nanoTime()] = MethodRecord(methodName, 1)
        }
    }

    @JvmStatic
    fun methodExit(methodName: String) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            stackCacheMap[System.nanoTime()] = MethodRecord(methodName, 2)
        }
    }

    private data class Stack(
        val name: String,
        val parent: Stack?,
        val children: MutableList<Stack> = mutableListOf(),
        var startTime: Long = 0L,
        var endTime: Long = 0L,
        var duration: Long = 0L
    )
}
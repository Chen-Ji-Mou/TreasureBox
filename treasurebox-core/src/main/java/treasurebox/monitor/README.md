# 仿腾讯Matrix TraceCanary——实现logcat直观实时卡顿监控

## 为什么

在平时开发过程中，会有一些排查性能的要求，尤其是运行时卡顿，需要不断尝试在可能的地方补上日志统计耗时，排查效率较低。
目前业内最好的运行时卡顿监控方案是腾讯的Matrix TraceCanary，但接入成本过大且本身不支持可视化需要自己实现，仅仅为了满足debug时方便看日志有些得不偿失。
故打算仿Matrix TraceCanary的方案开发一个轻量级的日志工具。

## 原理

Matrix TraceCanary的原理是通过监控Choreographer绘制帧间时间差判断是否出现卡顿，并且在编译期字节码插桩收集运行时堆栈，在卡顿出现时保存对应时间段内的堆栈。
经过实际初步尝试后发现通过监控Choreographer绘制帧间时间差判断是否出现卡顿有些过于灵敏，反而会导致信息冗余。
最终决定通过监控主线程Looper处理事件耗时判断是否出现卡顿，同时在编译期字节码插桩收集运行时堆栈。

# 待优化
由于会保存大量的运行时堆栈信息，会占用一定的运行时内存，虽然已使用LRU算法优化，但仍会占用运行时内存约8MB左右。
目前只能监控项目级的代码耗时，无法监控系统级的代码耗时。
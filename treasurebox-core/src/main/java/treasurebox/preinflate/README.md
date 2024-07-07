# RecyclerView滑动卡顿解决方案——异步inflate

## 原理

RecyclerView滑动卡顿的主要原因是由于滑动时预加载创建/复用ViewHolder时出现耗时，而ViewHolder的耗时主要是因为进行inflate操作创建对应itemView。

> 另一部分的原因是ViewHolder复用时数据绑定耗时，可以通过异步加载数据的方式解决，但由于具体业务逻辑的不同，不方便去定制一套解决方案，因此就不讨论这部分原因。

本方案通过异步预加载inflate的方式去解决耗时问题，在RecyclerView未初始化前通过异步加载一定数量的itemView，满足RecyclerView的ViewHolder复用机制。

本方案借鉴于[RecyclerView性能优化之异步预加载](https://juejin.cn/post/7248599585752793125)，基于文章的方案进行修改简化，适配了ViewBinding。

## 使用方法

可直接通过继承PreInflateAdapter类快速接入本方案。
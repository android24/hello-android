# 18.4 从 Dalvik 到 ART：解释执行、JIT、AOT 与 Profile

Dex 被 ClassLoader 找到之后，还需要被运行时执行。

Android 现在的主要运行时是 ART。在它之前，早期 Android 使用的是 Dalvik 虚拟机。

本节我们不钻到虚拟机实现细节深处，而是先建立工程视角：

```text
Dex 代码
  -> Dalvik / ART 加载与校验
      -> 解释执行 / JIT / AOT
          -> Profile 引导优化
              -> 影响启动、安装和运行性能
```

## 本节定位

本节负责回答：

- Dalvik 是什么，ART 又改变了什么？
- 解释执行、JIT、AOT 有什么区别？
- Profile Guided Optimization 为什么影响启动性能？
- Baseline Profile 为什么能改善冷启动？
- ART 相关问题在工程中通常如何出现？

## 学习目标

学完本节后，你应该能够：

- 用工程语言解释 ART 的作用。
- 能对比 Dalvik 和 ART 的核心差异。
- 区分解释执行、JIT 和 AOT。
- 理解 Profile 与启动性能的关系。
- 知道为什么大型 App 会关注 Baseline Profile。
- 能把启动慢和类加载、方法编译联系起来。

## 第一部分：Dalvik 是早期 Android 的虚拟机

Dalvik 是早期 Android 使用的运行时。它面向移动设备设计，执行 Dex 字节码。

你可以把它放在这条历史线里：

```text
早期 Android
  -> Dalvik
      -> 解释执行为主，后来加入 JIT
          -> 运行时逐渐识别热点代码

Android 4.4
  -> ART 预览

Android 5.0+
  -> ART 成为默认运行时
      -> AOT / JIT / Profile 逐步演进
```

Dalvik 时代，很多性能优化文章会强调：

- 方法数限制。
- MultiDex 兼容。
- JIT 热身。
- 类加载时机。
- dexopt。
- 启动阶段主 dex 边界。

这些历史知识不是过时废纸。它们能帮你理解为什么 Android 工程里会留下 MultiDex、热修复、dexElements、main dex list 这些关键词。

## 第二部分：ART 不只是“执行代码”

ART 负责很多事情：

- 加载类。
- 校验 Dex。
- 管理对象和内存。
- 执行方法。
- 垃圾回收。
- 运行时优化。
- 和 native 世界交互。

所以 ART 不是一个黑盒按钮，而是 App 运行性能和稳定性的核心环境。

## 第三部分：Dalvik 与 ART 的核心差异

先看一张工程视角的对比表：

| 对比项 | Dalvik | ART |
| --- | --- | --- |
| 主要时代 | Android 5.0 之前为主 | Android 5.0 之后默认 |
| 执行对象 | Dex | Dex |
| 执行策略 | 解释执行，后来加入 JIT | 解释执行、JIT、AOT、Profile 组合 |
| 编译时机 | 更偏运行时热点编译 | 可安装后、后台、运行时多阶段优化 |
| 产物关键词 | odex、dexopt | oat、vdex、art profile、baseline profile |
| 启动影响 | 热点代码需要运行后逐步变快 | 可提前优化启动和关键路径 |
| 工程关注 | MultiDex、主 dex、运行时性能 | Baseline Profile、启动路径、dexopt 状态 |

一句话概括：

```text
Dalvik 更像边跑边热身的早期虚拟机
ART 更像会结合安装、后台、运行时和 Profile 持续优化的现代运行时
```

当然，现代 ART 也不是单纯 AOT。它在不同 Android 版本中不断调整策略，逐步形成解释执行、JIT、AOT 和 Profile 的组合拳。

## 第四部分：解释执行

解释执行可以理解为：

```text
运行到某条 Dex 指令
  -> 运行时解释这条指令
      -> 执行
```

优点是启动门槛低，不需要提前把所有代码编译成本地机器码。

缺点是热点代码长期解释执行会慢。

## 第五部分：JIT

JIT 是 Just-In-Time Compilation。

大意是：

```text
代码先运行
  -> ART 观察哪些方法经常被执行
      -> 把热点方法编译成本地机器码
          -> 后续运行更快
```

JIT 更灵活，但它需要运行时收集信息和编译。

Dalvik 后期已经引入 JIT。ART 也曾在早期更强调 AOT，后来又把 JIT 和 Profile 结合回来。这个演进告诉我们：运行时不是在“JIT 和 AOT 之间二选一”，而是在性能、安装速度、存储空间和耗电之间做平衡。

## 第六部分：AOT

AOT 是 Ahead-Of-Time Compilation。

大意是：

```text
运行前或安装后
  -> 提前把部分代码编译成本地机器码
      -> 运行时更快进入执行
```

AOT 可以提升运行性能，但过度编译会增加安装、存储和更新成本。

ART 早期经常被理解成“安装时 AOT 编译”。这个说法有历史意义，但不够完整。

更准确地说，现代 Android 会在解释、JIT、AOT、Profile 之间做平衡。

## 第七部分：Profile 是运行时记忆

Profile 记录了 App 实际运行中哪些类、哪些方法更常用。

大致作用：

```text
用户真实运行 App
  -> 系统记录热点方法
      -> 后续编译优化更有方向
```

这就是 Profile Guided Optimization。

它比盲目编译所有代码更聪明。

## 第八部分：Baseline Profile

Baseline Profile 是开发者提前提供的一份“启动和关键路径热点提示”。

它告诉系统：

```text
这些类和方法很可能在启动或关键路径使用
请优先优化它们
```

这对冷启动、首帧和关键页面进入速度有帮助。

尤其是 Compose、复杂首页、大型 App，Baseline Profile 往往值得关注。

## 第九部分：ART 与启动性能

启动慢不一定只是布局复杂，也可能来自：

- 启动路径类太多。
- 反射扫描太重。
- 初始化过早。
- Dex 数量和加载路径复杂。
- JIT 尚未热起来。
- 缺少合适 Profile。

所以启动优化要同时看：

```text
Application 初始化
  -> ContentProvider 初始化
      -> 类加载数量
          -> 反射扫描
              -> 主线程 I/O
                  -> 首帧渲染
```

第 18 章关注的是其中“类加载和运行时优化”这一段。

## 第十部分：Dalvik 时代知识为什么还值得学

你可能会问：既然现代 Android 默认 ART，为什么还要讲 Dalvik？

原因有三个：

- 很多经典文章、热修复框架和 MultiDex 资料写于 Dalvik / ART 过渡时期。
- 很多术语仍然留在工程实践中，比如 dexopt、odex、dexElements、main dex。
- 对比 Dalvik 和 ART，能帮你理解 Android 为什么从“运行时热身”走向“Profile 引导优化”。

学 Dalvik 不是为了回到过去，而是为了看懂 Android 运行时为什么变成今天这样。

## 第十一部分：常见工程问题

| 现象 | 可能关联 | 排查方向 |
| --- | --- | --- |
| 冷启动慢 | 类加载、解释执行、Profile 不足 | Startup trace、Baseline Profile |
| 首次进入页面慢 | JIT 尚未优化、初始化过重 | Trace 热点方法 |
| 安装后首次启动慢 | dexopt / profile 状态 | 对比二次启动 |
| 反射扫描慢 | 类加载和字符串查找多 | 减少启动期扫描 |
| 更新后性能波动 | Profile 需要重新积累 | 观察安装后几次启动 |
| 老项目 MultiDex 异常 | Dalvik 时代主 dex 边界 | 查 main dex list、启动路径类 |
| 老热修复方案不稳定 | 类加载时机、运行时差异 | 对比系统版本和框架支持范围 |

## 本节小挑战

### 启动优化归因题

如果一个页面第二次打开明显比第一次快，可能有哪些原因？

你可以从这些角度回答：

- 类已经加载。
- 资源已经缓存。
- JIT 已经优化热点方法。
- 数据已经缓存。
- 页面初始化路径减少。

### Dalvik 对比题

请解释：

```text
为什么说 Dalvik 和 ART 都执行 Dex，但它们对启动、安装和运行时优化的取舍不同？
```

你需要回答：

- Dalvik 时代为什么更强调 JIT 热身和 MultiDex 兼容。
- ART 为什么会引入 AOT、Profile 和 Baseline Profile。
- 为什么现代 ART 不是单纯 AOT。

## 本节实践任务

### 基础任务

- 对比 App 第一次启动和第二次启动的日志。
- 搜索启动路径中的反射扫描。
- 记录 Application 中做了哪些初始化。
- 用自己的话写一段 Dalvik 与 ART 的差异说明。

### 进阶任务

- 了解 Baseline Profile 的生成和使用方式。
- 用性能工具观察启动阶段热点方法。
- 思考哪些类应该避免在启动路径加载。
- 查阅一篇老热修复或 MultiDex 文章，标注其中哪些描述依赖 Dalvik / 早期 ART 背景。

## 本节小结

Dalvik 和 ART 都围绕 Dex 执行代码，但它们代表了 Android 运行时从早期虚拟机到现代运行时的演进。解释执行降低启动门槛，JIT 优化热点代码，AOT 提前编译关键代码，Profile 让优化更有方向。理解 Dalvik 与 ART 的差异后，启动优化、MultiDex、热修复、dexopt 和 Baseline Profile 就能放在同一条历史线里理解。

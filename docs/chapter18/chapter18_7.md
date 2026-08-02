# 18.7 代码加载体验问题：ClassNotFound、NoSuchMethod、VerifyError 与 UnsatisfiedLinkError

本节把第 18 章的知识落到问题诊断上。

代码加载相关问题通常来得很突然：

```text
debug 正常，release 崩溃
新版本正常，老设备崩溃
某些 ABI 设备崩溃
插件灰度用户崩溃
热修复下发后不生效
```

如果只看崩溃名字，很容易头疼。把它们放回加载链路，就会清楚很多。

## 本节定位

本节负责回答：

- 常见代码加载崩溃分别意味着什么？
- 第一证据应该查哪里？
- debug 正常、release 崩溃时优先怀疑什么？
- 依赖版本冲突为什么会导致 `NoSuchMethodError`？
- so 加载失败如何快速归因？

## 学习目标

学完本节后，你应该能够：

- 区分 `ClassNotFoundException` 和 `NoClassDefFoundError`。
- 解释 `NoSuchMethodError` 和依赖版本漂移的关系。
- 初步判断 `VerifyError`。
- 排查 `UnsatisfiedLinkError`。
- 写一份代码加载问题诊断报告。

## 第一部分：ClassNotFoundException

含义：

```text
运行时按类名查找类，但 ClassLoader 找不到。
```

第一证据：

- 崩溃类名。
- 当前 ClassLoader。
- APK 中是否包含该类。
- mapping 文件。
- R8 keep 规则。
- 动态 dex 路径。

常见原因：

- 类没有打进 APK。
- release 被 R8 shrink。
- 类名被混淆，但反射仍用旧名字。
- 插件 dex 没加载。
- ClassLoader 用错。

## 第二部分：NoClassDefFoundError

它常见于：

```text
编译期能找到
运行时某个依赖类缺失
```

比如 A 类存在，但 A 引用的 B 类运行时不存在。

第一证据：

- 崩溃堆栈中真正缺失的类。
- 依赖树。
- 最终 APK。
- 运行设备版本。

## 第三部分：NoSuchMethodError

含义：

```text
运行时找到了类，但找不到调用的方法。
```

这常常和依赖版本不一致有关：

```text
编译期：common-lib v2 有 fun newApi()
运行时：被打包进 APK 的 common-lib v1 没有 newApi()
```

第一证据：

- 方法签名。
- 编译期依赖版本。
- 运行时最终依赖版本。
- Gradle dependencyInsight。

## 第四部分：VerifyError

`VerifyError` 表示运行时校验类或字节码时发现不合法。

可能原因：

- 字节码插桩错误。
- 混淆优化异常。
- 低版本设备不兼容。
- 动态生成或修改字节码出错。
- 依赖冲突导致类结构不一致。

第一证据：

- 崩溃设备系统版本。
- 是否只发生在 release。
- 是否用了字节码插桩、热修复、AOP、ASM。
- R8 / AGP 版本变化。

## 第五部分：UnsatisfiedLinkError

它属于 native 库加载问题。

常见形式：

```text
couldn't find "libxxx.so"
No implementation found for native method
```

第一证据：

- 缺失的 so 名称。
- 设备 ABI。
- APK / AAB 中的 lib 目录。
- `System.loadLibrary` 调用位置。
- JNI 方法签名。

## 第六部分：debug 正常 release 崩溃

优先怀疑：

- R8 shrink 删除了反射类。
- 类名 / 方法名被混淆。
- keep 规则不完整。
- release 依赖版本不同。
- native so 打包配置不同。
- 构建变体资源或代码入口不同。

不要第一时间怀疑系统坏了。先看构建产物。

## 第七部分：诊断路线

```text
确认崩溃类型
  -> 提取类名 / 方法名 / so 名
      -> 对比 debug 和 release
          -> 查看最终 APK 内容
              -> 查看 mapping 和 keep
                  -> 查看依赖树
                      -> 查看 ClassLoader / ABI / 设备版本
                          -> 写出第一证据和修复方向
```

## 第八部分：代码加载问题诊断表

| 问题 | 第一证据 | 常见原因 | 修复方向 |
| --- | --- | --- | --- |
| ClassNotFound | 类名、APK、ClassLoader | 类缺失、R8、动态 dex 未加载 | keep、检查 dex、修加载路径 |
| NoClassDefFound | 缺失依赖类、依赖树 | 间接依赖缺失 | 补依赖、统一版本 |
| NoSuchMethod | 方法签名、依赖版本 | 编译期和运行时版本不一致 | dependencyInsight、锁版本 |
| VerifyError | 设备版本、插桩链路 | 字节码不合法 | 检查 ASM / R8 / 热修复 |
| UnsatisfiedLink | so 名、ABI、APK lib | so 缺失、ABI 不匹配、JNI 签名 | 补 so、修 ABI、keep JNI |

## 本节小挑战

### release 崩溃分类题

release 出现：

```text
ClassNotFoundException: com.example.router.GeneratedRouteTable
```

debug 正常。

请写出排查顺序。

建议回答：

- 查是否只通过反射加载。
- 查 R8 mapping。
- 查 keep 规则。
- 查 release APK dex。
- 查路由代码生成是否在 release 执行。

## 本节实践任务

### 基础任务

- 收集一个代码加载相关崩溃案例。
- 写出崩溃类型、第一证据、可能原因。
- 查一次 APK 中是否存在目标类或 so。

### 进阶任务

- 用 `dependencyInsight` 分析一个依赖版本。
- 写一个反射入口的 keep 规则。
- 对比 debug / release 产物差异。

## 本节小结

代码加载问题看似分散，本质都可以回到 Dex、ClassLoader、R8、依赖版本、ART 校验和 so 加载。诊断时不要只看异常名，要提取“类名、方法名、so 名、ClassLoader、ABI、mapping、依赖树、最终 APK”这些证据。证据齐了，很多玄学崩溃都会变成工程问题。

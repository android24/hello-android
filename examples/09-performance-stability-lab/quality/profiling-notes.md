# 观察与分析建议

## Android Studio Profiler

可以用 Profiler 观察：

- CPU：后台计算时是否占用明显 CPU。
- Memory：点击 `保留 2MB` 后内存是否增长。
- Energy：后台任务是否持续运行。

## Logcat

建议为实验动作增加日志：

```text
Startup: process -> activity
Jank: main thread blocked
Memory: allocated chunk count
Crash: handled exception
```

日志要服务定位，不要只输出“走到这里了”。

## Frame / Jank

如果要继续扩展示例，可以加入：

- `JankStats`
- `Macrobenchmark`
- `Baseline Profile`
- Perfetto trace

这些工具会在更深入的性能章节里继续展开。

## 分析顺序

```text
先观察现象
  -> 再确认范围
      -> 再定位原因
          -> 再做改动
              -> 最后验证结果
```

性能优化不要靠猜。每一次优化都应该有优化前后的证据。

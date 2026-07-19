# View / Choreographer / SurfaceFlinger 源码阅读建议

第 15 章不要求一次读完整条渲染源码。建议先带着 App 侧现象找入口：

```text
UI 不刷新
  -> View.invalidate / requestLayout
      -> ViewRootImpl.scheduleTraversals
          -> Choreographer.doFrame
              -> ViewRootImpl.performTraversals
                  -> HardwareRenderer
                      -> BufferQueue
                          -> SurfaceFlinger
```

## 推荐入口

```text
frameworks/base/core/java/android/view/View.java
frameworks/base/core/java/android/view/ViewGroup.java
frameworks/base/core/java/android/view/ViewRootImpl.java
frameworks/base/core/java/android/view/Choreographer.java
frameworks/base/graphics/java/android/graphics/HardwareRenderer.java
frameworks/base/libs/hwui/
frameworks/native/libs/gui/BufferQueue.cpp
frameworks/native/services/surfaceflinger/
```

## 阅读问题

- `invalidate()` 如何向父级传播？
- `requestLayout()` 为什么会影响 measure / layout？
- ViewRootImpl 为什么要 `scheduleTraversals()`？
- Choreographer 如何把 traversal 安排到 VSYNC？
- HardwareRenderer 和 RenderThread 大致负责什么？
- BufferQueue 如何连接生产者和消费者？
- SurfaceFlinger 为什么能合成多个 Layer？

## 建议写下来的结论

- 一句话解释 invalidate。
- 一句话解释 requestLayout。
- 一句话解释 Choreographer。
- 一句话解释 RenderThread。
- 一句话解释 SurfaceFlinger。

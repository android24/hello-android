# 一帧渲染链路报告模板

## 基本信息

- 操作：
- 页面：
- 设备 / 模拟器：
- 观察时间：

## 我的预期

- 这次操作更像触发 invalidate 还是 requestLayout：
- 是否可能触发 measure / layout：
- 是否可能出现慢帧：

## 观察证据

- 刷新请求：
- View 探针日志：
- Choreographer 帧间隔：
- 最大帧间隔：
- 慢帧数量：
- 滚动 / 动画表现：

## 链路推断

```text
状态变化
  -> 刷新请求
      -> ViewRootImpl / Choreographer
          -> measure / layout / draw
              -> HardwareRenderer / RenderThread
                  -> Surface / BufferQueue / SurfaceFlinger
```

## 结论

- 本次变化影响的是：
- 最可能的瓶颈是：
- 可以尝试的优化方向：

## 仍不确定

- 

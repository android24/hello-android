# Hello Android Capstone 事故诊断报告模板

## 基本信息

```text
事故标题：
applicationId：
影响版本：
versionCode：
BUILD_PROFILE：
影响设备：
发生时间：
负责人：
traceId：
Logcat Tag：
Trace section：
```

## 用户现象

```text
用户做了什么：
页面表现是什么：
是否可复现：
影响范围有多大：
```

## 时间线

```text
[traceId] 点击事件：
[traceId] ViewModel intent：
[traceId] UseCase：
[traceId] Repository：
[traceId] 后台任务：
[traceId] UI 刷新：
```

## Demo 交互状态

```text
注入的事故剧本：
被自动推进的终章任务：
被自动推进的动手实验：
能力雷达变化：
发布门禁变化：
毕业结论变化：
```

## 证据列表

| 证据 | 结论 | 局限 |
| --- | --- | --- |
| logcat |  |  |
| Perfetto |  |  |
| dumpsys |  |  |
| gfxinfo |  |  |
| meminfo |  |  |
| 应用结构化日志 |  |  |

## 假设与排除

| 假设 | 支持证据 | 反证 | 结论 |
| --- | --- | --- | --- |
| 主线程阻塞 |  |  |  |
| Binder 等待 |  |  |  |
| 后台调度限制 |  |  |  |
| 存储或权限问题 |  |  |  |
| 资源或配置问题 |  |  |  |

## 根因

```text
直接原因：
触发条件：
工程治理缺口：
```

## 修复与回归

```text
修复方案：
回归路径：
修复前指标：
修复后指标：
是否需要补监控：
是否需要补发布门禁：
```

# 系统证据链报告模板

## 基本信息

```text
包名：
版本：
构建号：
设备：
Android 版本：
复现时间点：
traceId：
检查人：
```

## 问题现象

```text
用户看到什么：
是否稳定复现：
影响范围：
业务影响：
```

## 第一证据：logcat

```text
关键词：
开始时间：
结束时间：
关键日志：
traceId 对齐：
第一结论：
```

## 系统状态：dumpsys

```text
使用的 service：
关键字段：
系统状态：
和现象的关系：
```

## 时间线：Perfetto

```text
trace 文件：
时间窗口：
trace section：
主线程状态：
RenderThread 状态：
Binder / system_server 状态：
慢帧或等待片段：
```

## 性能数字

```text
gfxinfo：
meminfo：
procstats：
simpleperf：
指标是否超过基线：
```

## 证据链答题

```text
第一证据判断：
系统状态证据判断：
时间线证据判断：
脱敏判断：
答题得分：
错误项解释：
```

## 完整现场：bugreport / DropBox / traces / tombstone

```text
bugreport 文件：
ANR trace：
tombstone：
DropBox：
需要符号文件：
```

## 根因推论

```text
根因候选：
支持证据：
反证或不确定点：
下一步验证：
```

## 修复方案

```text
短期止血：
长期修复：
风险：
负责人：
```

## 回归验证

```text
验证设备：
验证版本：
复现脚本：
修复前证据：
修复后证据：
是否达到基线：
```

## 脱敏检查

```text
账号 / token：
设备标识：
文件路径：
定位 / 联系方式：
业务敏感数据：
分享范围：
```

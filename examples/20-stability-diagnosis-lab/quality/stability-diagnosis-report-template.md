# 第20章稳定性诊断报告

## 事故信息

- 问题标题：
- 问题类型：
- 发生时间：
- App 版本：
- 设备 / 系统：
- 前后台状态：
- processName：
- pid：
- threadName：

## 第一证据

- ANR reason：
- Java exception：
- Native signal：
- DropBox tag：
- bugreport / dumpsys 入口：
- 第一业务栈：
- 关键 trace：

## 等待链 / 崩溃链

- main 线程状态：
- 是否有锁等待：
- 持锁线程：
- 是否有 Binder 等待：
- remote 进程状态：
- native so：
- ABI：

## 判断

- 根因：
- 为什么不是其他类型：
- 还缺什么证据：

## 修复与回归

- 修复方案：
- 降级方案：
- 回归用例：
- 灰度策略：
- 监控指标：

# Hello Android Capstone 发版评审模板

## 版本信息

```text
applicationId：
版本号：
构建号：
Git commit：
构建类型：
BUILD_PROFILE：
检查时间：
```

## 发布结论

```text
可以发布 / 小流量灰度 / 暂缓发布 / 修复后再评审
```

## 发布门禁

| 检查项 | 风险等级 | 是否通过 | 证据 |
| --- | --- | --- | --- |
| release 构建可安装启动 | P3 |  |  |
| mapping / symbols 已归档 | P0 |  |  |
| 签名证书一致 | P0 |  |  |
| 新增权限有拒绝路径 | P1 |  |  |
| exported 组件有保护条件 | P1 |  |  |
| 核心性能基线无退化 | P1 |  |  |
| 事故报告模板完整 | P2 |  |  |
| 灰度和回滚策略明确 | P2 |  |  |

## 工程证据

```text
mapping 归档位置：
symbols 归档位置：
签名证书 SHA-256：
历史证书 SHA-256：
Logcat Tag：
Trace section：
Perfetto / bugreport / dumpsys 附件：
```

## 监控基线

```text
crash-free：
ANR rate：
冷启动 P95：
详情打开 P95：
慢帧率：
后台同步成功率：
权限拒绝率：
核心路径完成率：
```

## 风险与动作

| 风险 | 等级 | 负责人 | 截止时间 | 动作 |
| --- | --- | --- | --- | --- |
|  |  |  |  |  |

## 最终判断

```text
为什么敢发：
或者为什么不能发：
如果灰度异常如何回滚：
```

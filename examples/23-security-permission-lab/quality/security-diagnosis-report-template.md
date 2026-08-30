# 第23章安全诊断报告模板

## 基本信息

问题标题：
用户现象：
Android 版本：
设备 / 厂商：
packageName：
uid：
pid：
processName：
targetSdk：
buildProfile：
安装来源：
签名证书 SHA-256：
升级场景：
签名判断结论：
debug APK 证书 SHA-256：
release APK 证书 SHA-256：
debug / release 对比结论：

## 访问对象

能力或数据：
是否涉及用户敏感数据：
是否涉及跨应用通信：
是否涉及导出、分享或日志上传：

## 声明与授权

Manifest 声明：
runtime permission：
permission flags：
用户设置：
AppOps：
AppOps op：
AppOps mode：
前后台状态：
targetSdk 行为差异：

## 媒体访问

访问方式：
是否使用 Photo Picker：
picked Uri：
是否请求整库媒体权限：
是否涉及部分照片授权：
Uri grant 生命周期：
是否需要持久访问：

## 组件与入口

组件名：
组件类型：
exported：
intent-filter：
组件 permission：
调用方 packageName：
调用方 uid：
调用方签名：
输入参数是否校验：
是否需要用户确认：

## PendingIntent

场景：
目标组件：
是否显式 Intent：
flags：
是否 immutable：
是否 mutable：
requestCode：
extras 是否可被接收方改写：
风险判断：

## Provider / Uri / FileProvider

Provider authority：
Uri：
Uri grant：
读写方向：
授权生命周期：
FileProvider paths：
是否只暴露 share/ 或 export/：
是否分享脱敏副本：
是否通过 SAF 导出：
SAF 目标 Uri：

## Keystore 与数据保护

数据类型：
数据敏感等级：
保存位置：
是否加密：
key alias：
key 是否可用：
异常类型：
是否参与备份：
恢复后是否重新校验：
日志是否脱敏：
导出前是否用户确认：

## 系统证据

第一证据：

```text

```

推荐命令：

```bash
adb shell dumpsys package <package>
adb shell cmd appops get <package>
adb shell ps -A | grep <package>
adb shell run-as <package> ls files
apksigner verify --verbose --print-certs <apk>
```

关键日志：

```text

```

## 根因判断

属于哪一类：

```text
身份问题
权限问题
AppOps / 设置问题
组件边界问题
PendingIntent 授权令牌问题
签名 / 发布链路问题
媒体访问范围问题
Keystore / 数据保护问题
日志 / 导出泄露问题
```

根因说明：

```text

```

## 诊断答题

选择的问题类型：
选择的第一证据：
选择的修复动作：
得分：
反馈：
标准诊断：

## 自由文本报告

自由报告得分：
自由报告反馈：

```text
现象：
第一证据：
根因：
修复：
回归：
```

## 修复方案

代码修复：
配置修复：
产品引导：
降级路径：
数据清理或迁移：
服务端兜底：

## 回归验证

回归用例：
验证设备：
验证 Android 版本：
验证命令：
预期结果：
实际结果：
是否需要监控：

## 复盘

这次事故第一证据是什么？
哪个系统模块参与了判断？
我们之前漏看了哪一层？
如何避免同类问题再次出现？

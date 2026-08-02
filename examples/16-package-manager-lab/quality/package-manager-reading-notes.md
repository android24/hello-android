# 第16章源码阅读笔记：PMS 与包管理

## 建议阅读顺序

```text
PackageManager API
  -> PackageInfo / ActivityInfo / ResolveInfo
      -> Manifest 解析结果
          -> PackageManagerService 查询入口
              -> ComputerEngine 解析和可见性判断
                  -> 安装、签名、权限相关 helper
```

## 先带着这些问题看

- App 侧 `getPackageInfo` 能拿到哪些字段？
- 哪些字段来自 Manifest，哪些字段来自安装状态？
- `queryIntentActivities` 为什么必须同时看 action、category、data 和 MIME？
- `exported=false`、组件 permission、enabled=false 分别挡在哪一层？
- Android 11+ 包可见性为什么会让查询结果变少？
- 签名信息如何影响覆盖安装和 signature 权限？

## 推荐入口

```text
frameworks/base/core/java/android/content/pm/PackageManager.java
frameworks/base/core/java/android/content/pm/PackageInfo.java
frameworks/base/core/java/android/content/pm/ActivityInfo.java
frameworks/base/core/java/android/content/pm/ResolveInfo.java
frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java
frameworks/base/services/core/java/com/android/server/pm/ComputerEngine.java
frameworks/base/services/core/java/com/android/server/pm/InstallPackageHelper.java
frameworks/base/services/core/java/com/android/server/pm/ScanPackageUtils.java
```

## 观察记录

```text
实验：
预期：
实际：
PackageManager 证据：
PMS 入口推断：
结论：
```

# 第18章代码加载诊断报告

## 现象

- 操作：
- 崩溃 / 异常：
- debug 是否正常：
- release 是否正常：

## 第一证据

- 目标类 / 方法 / so：
- 当前 ClassLoader：
- parent ClassLoader：
- dex / apk 路径：
- dexElements 顺序：
- 是否通过反射：
- 是否开启 R8：
- mapping 证据：
- keep 规则：
- 依赖版本：
- 设备 ABI：
- nativeLibraryDir：
- 目标 so：
- JNI 注册方式：
- 插件 APK：
- 插件入口类：
- 插件 ClassLoader：
- 宿主 contract：
- 热修复目标类：
- 补丁类：
- 补丁前结果：
- 补丁后结果：

## 判断

- 更像代码缺失、ClassLoader 不可见、R8 shrink / 混淆、依赖版本漂移，还是 native 加载问题？
- 如果是动态加载问题，是 APK 不存在、ClassLoader 路径错误、入口类找不到，还是 contract 不可见？
- 如果是热修复问题，是补丁未加载、加载太晚、类已经被加载，还是补丁结构不兼容？
- 哪个证据最关键？
- 仍然缺少什么证据？

## 修复方案

- 短期修复：
- 长期治理：
- 回归验证：

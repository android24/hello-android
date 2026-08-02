# 第17章源码阅读笔记：资源系统

## 建议阅读顺序

```text
AAPT2 资源编译
  -> R 文件与 resources.arsc
      -> Resources / ResourcesImpl
          -> AssetManager
              -> Configuration
                  -> Theme / Attribute
                      -> 多模块资源合并
                          -> 动态换肤方案选择
```

## 先带着这些问题看

- `R.string.xxx` 为什么只是一个 int？
- `0xPPTTEEEE` 如何进入资源表查找？
- `resources.arsc` 如何保存多个 Configuration 版本？
- `AssetManager` 如何加载资源路径？
- `Resources` 和 `ResourcesImpl` 如何分工？
- Theme attr 为什么依赖 Context？
- 动态资源替换为什么优先使用 Theme / ResourceProvider / Configuration？
- Theme、ResourceProvider、Configuration、外部皮肤包和 RRO 分别替换了资源读取链路的哪一层？
- 资源 shrink 和 `getIdentifier` 为什么容易一起出问题？
- 多模块资源最终如何进入 merged resources？

## 推荐入口

```text
frameworks/base/core/java/android/content/res/Resources.java
frameworks/base/core/java/android/content/res/ResourcesImpl.java
frameworks/base/core/java/android/content/res/AssetManager.java
frameworks/base/core/java/android/content/res/Configuration.java
frameworks/base/libs/androidfw/ResourceTypes.cpp
frameworks/base/tools/aapt2/
```

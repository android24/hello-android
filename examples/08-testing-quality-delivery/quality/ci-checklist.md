# CI 检查清单

## 最小质量门禁

```text
:app:assembleDevDebug
:domain-course:testDebugUnitTest
:data-course:testDebugUnitTest
:feature-course:testDebugUnitTest
:app:lintDevDebug
```

这三项用于保护主分支的最低质量线：

- `:app:assembleDevDebug`：确认 App 能构建。
- `:domain-course:testDebugUnitTest`：确认 UseCase 测试通过。
- `:data-course:testDebugUnitTest`：确认 Repository 测试通过。
- `:feature-course:testDebugUnitTest`：确认 ViewModel 状态测试通过。
- `:app:lintDevDebug`：确认没有明显 Android 配置和代码风险。

## 推荐扩展

```text
:app:assembleProdRelease
:app:connectedDevDebugAndroidTest
:feature-course:connectedDebugAndroidTest
```

推荐在发版前或夜间任务中运行：

- `assembleProdRelease`：提前发现 release 变体问题。
- `connectedDevDebugAndroidTest`：在 app 环境验证 Hilt 测试替换。
- `connectedDebugAndroidTest`：在设备或模拟器上验证关键 UI 路径。

## 失败排查顺序

1. 先看是否是编译失败。
2. 再看是否是单元测试断言失败。
3. 如果是 lint，判断是新增问题还是历史问题。
4. 如果是 UI 测试，确认测试数据和设备状态是否稳定。
5. 修复后重新运行同一条命令。

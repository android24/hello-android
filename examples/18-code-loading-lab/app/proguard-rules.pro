# 第 18 章实验：这条 keep 规则用于保护字符串反射入口。
# 你可以临时注释它，再构建 release 包，观察反射风险卡和 mapping 的变化。
-keep class com.helloandroid.codeloading.GeneratedRouteTable { *; }
-keep class com.helloandroid.codeloading.ReflectionTarget { *; }

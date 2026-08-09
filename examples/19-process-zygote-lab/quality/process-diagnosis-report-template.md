# 第19章进程问题诊断报告

## 现象

- 操作：
- 页面 / 组件：
- 是否后台回来：
- 是否多进程：
- 是否出现 ANR：

## 进程证据

- packageName：
- processName：
- pid：
- ppid：
- uid：
- oom_score_adj：
- Application 创建时间：
- Activity 创建时间：
- Provider 是否提前初始化：
- 内存草稿：
- 持久化草稿：
- saved pid：
- current pid：
- remote processName：
- remote pid：
- remote binder death：
- isolated processName：
- isolated pid：
- isolated uid：
- isolated binder death：

## 线程证据

- main 线程状态：
- Binder 线程状态：
- RenderThread 状态：
- worker 线程状态：
- 是否有 held lock：
- 是否有 waiting lock：
- 是否有同步 Binder 调用：
- ANR 类型：
- trace 中 main 线程关键栈：
- trace 中 Binder 线程关键栈：
- 系统超时原因：

## OOM Adj 样本

- 前台样本：
- 后台返回样本：
- remote 后样本：
- isolated 样本：

## 判断

- 更像进程重启、Activity 重建、多进程状态错乱、Binder 阻塞、主线程阻塞，还是后台回收？
- 哪个证据最关键？
- 还缺什么证据？

## 修复

- 短期修复：
- 长期治理：
- 回归验证：

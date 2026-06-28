package com.helloandroid.performance

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerformanceLabViewModel : ViewModel() {
    private companion object {
        const val TAG = "PerformanceLab"
    }

    private val retainedMemory = mutableListOf<ByteArray>()

    private val _uiState = MutableStateFlow(PerformanceLabState())
    val uiState: StateFlow<PerformanceLabState> = _uiState

    fun recordFirstActivityCreate(
        elapsedFromProcessStart: Long,
        buildProfile: String,
        verboseLogEnabled: Boolean
    ) {
        Log.d(TAG, "Startup recorded: ${elapsedFromProcessStart}ms, build=$buildProfile, verbose=$verboseLogEnabled")
        _uiState.update {
            it.copy(
                startupReport = StartupReport(
                    elapsedFromProcessStartMs = elapsedFromProcessStart,
                    buildProfile = buildProfile,
                    verboseLogEnabled = verboseLogEnabled
                )
            )
        }
    }

    fun simulateMainThreadJank() {
        val start = SystemClock.elapsedRealtime()
        Thread.sleep(180)
        val cost = SystemClock.elapsedRealtime() - start
        Log.d(TAG, "Main thread jank simulated: ${cost}ms")
        _uiState.update {
            it.copy(
                mainThreadReport = "主线程被阻塞 ${cost}ms。真实项目里，这类代码会带来掉帧和输入延迟。",
                observationHint = "观察点：点击瞬间页面会短暂停顿；在 Logcat 搜索 $TAG 可以看到阻塞耗时。",
                healthScore = it.healthScore.copy(
                    responsiveness = (it.healthScore.responsiveness - 10).coerceAtLeast(0)
                )
            )
        }
    }

    fun runBackgroundCpuWork() {
        Log.d(TAG, "Background CPU work started")
        _uiState.update {
            it.copy(
                cpuReport = "后台计算中，UI 仍可继续响应",
                observationHint = "观察点：计算进行时页面仍能滚动；可以用 Profiler 查看 CPU 波动。"
            )
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                countPrimeNumbers(limit = 45_000)
            }

            Log.d(TAG, "Background CPU work finished, primeCount=$result")
            _uiState.update {
                it.copy(
                    cpuReport = "后台线程完成计算：45,000 以内共有 ${result} 个质数。",
                    healthScore = it.healthScore.copy(
                        responsiveness = (it.healthScore.responsiveness + 5).coerceAtMost(25)
                    )
                )
            }
        }
    }

    fun allocateMemoryChunk() {
        retainedMemory += ByteArray(2 * 1024 * 1024)
        Log.d(TAG, "Memory chunk retained, count=${retainedMemory.size}")
        updateMemoryReport()
    }

    fun releaseMemory() {
        retainedMemory.clear()
        System.gc()
        Log.d(TAG, "Retained memory released")
        updateMemoryReport()
    }

    fun captureHandledCrash() {
        val report = runCatching {
            error("模拟课程详情解析失败：missing lessonId")
        }.fold(
            onSuccess = { "没有异常" },
            onFailure = { error ->
                "已捕获异常：${error::class.simpleName} / ${error.message}"
            }
        )

        Log.d(TAG, "Handled crash report: $report")
        _uiState.update {
            it.copy(
                stabilityReport = report,
                observationHint = "观察点：异常被捕获后没有直接崩溃；稳定性分数只轻微下降，说明风险被控制住了。",
                healthScore = it.healthScore.copy(
                    stability = it.healthScore.stability.coerceAtMost(18)
                )
            )
        }
    }

    fun simulateAnrRisk() {
        val start = SystemClock.elapsedRealtime()
        Thread.sleep(650)
        val cost = SystemClock.elapsedRealtime() - start
        Log.d(TAG, "ANR risk simulated: ${cost}ms")
        _uiState.update {
            it.copy(
                mainThreadReport = "ANR 风险演示：主线程等待 ${cost}ms。真实 ANR 阈值更高，但治理原则相同。",
                observationHint = "观察点：这是受控短等待，不会故意制造真正 ANR；真实排查要看主线程堆栈和 trace。",
                healthScore = it.healthScore.copy(
                    responsiveness = (it.healthScore.responsiveness - 15).coerceAtLeast(0)
                )
            )
        }
    }

    fun resetLab() {
        retainedMemory.clear()
        Log.d(TAG, "Lab reset")
        _uiState.update {
            it.copy(
                mainThreadReport = "尚未执行主线程压力实验",
                cpuReport = "尚未执行后台 CPU 实验",
                memoryReport = MemoryReport(),
                stabilityReport = "尚未捕获异常",
                observationHint = "先选择一个实验，再观察页面反馈、Logcat 或 Profiler 变化。",
                healthScore = HealthScore()
            )
        }
    }

    private fun updateMemoryReport() {
        val runtime = Runtime.getRuntime()
        val usedMb = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        _uiState.update {
            val memoryScore = (20 - retainedMemory.size * 2).coerceIn(0, 20)
            it.copy(
                memoryReport = MemoryReport(
                    allocatedChunks = retainedMemory.size,
                    allocatedMb = retainedMemory.size * 2,
                    runtimeUsedMb = usedMb
                ),
                observationHint = "观察点：内存块数量变化后，可以在 Memory Profiler 中观察堆内存趋势。",
                healthScore = it.healthScore.copy(memory = memoryScore)
            )
        }
    }

    private fun countPrimeNumbers(limit: Int): Int {
        var count = 0
        for (number in 2..limit) {
            if (isPrime(number)) {
                count++
            }
        }
        return count
    }

    private fun isPrime(number: Int): Boolean {
        var divisor = 2
        while (divisor * divisor <= number) {
            if (number % divisor == 0) {
                return false
            }
            divisor++
        }
        return true
    }
}

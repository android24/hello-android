package com.helloandroid.performance

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerformanceLabViewModel : ViewModel() {
    private val retainedMemory = mutableListOf<ByteArray>()

    private val _uiState = MutableStateFlow(PerformanceLabState())
    val uiState: StateFlow<PerformanceLabState> = _uiState

    fun recordFirstActivityCreate(
        elapsedFromProcessStart: Long,
        buildProfile: String,
        verboseLogEnabled: Boolean
    ) {
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
        _uiState.update {
            it.copy(
                mainThreadReport = "主线程被阻塞 ${cost}ms。真实项目里，这类代码会带来掉帧和输入延迟。"
            )
        }
    }

    fun runBackgroundCpuWork() {
        _uiState.update { it.copy(cpuReport = "后台计算中，UI 仍可继续响应") }

        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                countPrimeNumbers(limit = 45_000)
            }

            _uiState.update {
                it.copy(
                    cpuReport = "后台线程完成计算：45,000 以内共有 ${result} 个质数。"
                )
            }
        }
    }

    fun allocateMemoryChunk() {
        retainedMemory += ByteArray(2 * 1024 * 1024)
        updateMemoryReport()
    }

    fun releaseMemory() {
        retainedMemory.clear()
        System.gc()
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

        _uiState.update { it.copy(stabilityReport = report) }
    }

    fun simulateAnrRisk() {
        val start = SystemClock.elapsedRealtime()
        Thread.sleep(650)
        val cost = SystemClock.elapsedRealtime() - start
        _uiState.update {
            it.copy(
                mainThreadReport = "ANR 风险演示：主线程等待 ${cost}ms。真实 ANR 阈值更高，但治理原则相同。"
            )
        }
    }

    private fun updateMemoryReport() {
        val runtime = Runtime.getRuntime()
        val usedMb = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        _uiState.update {
            it.copy(
                memoryReport = MemoryReport(
                    allocatedChunks = retainedMemory.size,
                    allocatedMb = retainedMemory.size * 2,
                    runtimeUsedMb = usedMb
                )
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

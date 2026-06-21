package com.helloandroid.async.data.legacy

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

class LegacyAsyncPlayground {
    private val mainHandler = Handler(Looper.getMainLooper())

    fun runThreadDemo(onResult: (String) -> Unit) {
        Thread {
            Thread.sleep(600)
            val workerName = Thread.currentThread().name

            mainHandler.post {
                onResult("Thread 完成任务，结果从 $workerName 切回主线程")
            }
        }.start()
    }

    fun runExecutorDemo(onResult: (String) -> Unit) {
        val executor = Executors.newFixedThreadPool(2)

        repeat(3) { index ->
            executor.execute {
                Thread.sleep(350L + index * 180L)
                val workerName = Thread.currentThread().name

                mainHandler.post {
                    onResult("线程池任务 ${index + 1} 完成，执行线程：$workerName")
                }
            }
        }

        executor.shutdown()
    }
}

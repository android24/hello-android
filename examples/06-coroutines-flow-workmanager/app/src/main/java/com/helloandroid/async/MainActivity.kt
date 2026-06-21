package com.helloandroid.async

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.work.WorkManager
import com.helloandroid.async.data.legacy.LegacyAsyncPlayground
import com.helloandroid.async.data.local.InMemoryLessonCache
import com.helloandroid.async.data.preferences.InMemorySyncPreferences
import com.helloandroid.async.data.remote.FakeCourseApi
import com.helloandroid.async.data.repository.SyncRepository
import com.helloandroid.async.ui.SyncCenterApp
import com.helloandroid.async.ui.SyncCenterViewModel
import com.helloandroid.async.ui.SyncCenterViewModelFactory
import com.helloandroid.async.worker.BackgroundSyncScheduler

class MainActivity : ComponentActivity() {
    private val repository: SyncRepository by lazy {
        SyncRepository(
            api = FakeCourseApi(),
            cache = InMemoryLessonCache(),
            preferences = InMemorySyncPreferences()
        )
    }

    private val backgroundSyncScheduler: BackgroundSyncScheduler by lazy {
        BackgroundSyncScheduler(WorkManager.getInstance(applicationContext))
    }

    private val viewModel: SyncCenterViewModel by viewModels {
        SyncCenterViewModelFactory(
            repository = repository,
            legacyAsyncPlayground = LegacyAsyncPlayground(),
            backgroundSyncScheduler = backgroundSyncScheduler
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SyncCenterApp(viewModel = viewModel)
        }
    }
}

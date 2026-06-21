package com.helloandroid.networkroomdatastore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.helloandroid.networkroomdatastore.data.local.AppDatabase
import com.helloandroid.networkroomdatastore.data.preferences.UserPreferencesDataSource
import com.helloandroid.networkroomdatastore.data.remote.FakeLessonApi
import com.helloandroid.networkroomdatastore.data.repository.LessonRepository
import com.helloandroid.networkroomdatastore.ui.HelloNetworkRoomDatastoreApp
import com.helloandroid.networkroomdatastore.ui.LessonListViewModel
import com.helloandroid.networkroomdatastore.ui.LessonListViewModelFactory

class MainActivity : ComponentActivity() {
    private val repository: LessonRepository by lazy {
        val database = AppDatabase.getInstance(applicationContext)
        LessonRepository(
            api = FakeLessonApi(),
            lessonDao = database.lessonDao(),
            preferencesDataSource = UserPreferencesDataSource(applicationContext)
        )
    }

    private val viewModel: LessonListViewModel by viewModels {
        LessonListViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HelloNetworkRoomDatastoreApp(viewModel = viewModel)
        }
    }
}

package com.helloandroid.architecture.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.helloandroid.architecture.R
import com.helloandroid.architecture.clean.presentation.CleanCourseRoute
import com.helloandroid.architecture.common.ArchitectureMode
import com.helloandroid.architecture.common.SampleLessonSource
import com.helloandroid.architecture.mvc.MvcCourseRoute
import com.helloandroid.architecture.mvi.MviCourseRoute
import com.helloandroid.architecture.mvp.MvpCourseRoute
import com.helloandroid.architecture.mvvm.MvvmCourseRoute

@Composable
fun ArchitectureEvolutionApp() {
    val source = remember { SampleLessonSource() }
    var selectedModeName by rememberSaveable { mutableStateOf(ArchitectureMode.MVC.name) }
    val selectedMode = ArchitectureMode.valueOf(selectedModeName)

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.page_background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ArchitectureHeader()

                ArchitectureModeSelector(
                    selectedMode = selectedMode,
                    onModeSelected = { mode -> selectedModeName = mode.name }
                )

                when (selectedMode) {
                    ArchitectureMode.MVC -> MvcCourseRoute(source = source)
                    ArchitectureMode.MVP -> MvpCourseRoute(source = source)
                    ArchitectureMode.MVVM -> MvvmCourseRoute(source = source)
                    ArchitectureMode.MVI -> MviCourseRoute(source = source)
                    ArchitectureMode.CLEAN -> CleanCourseRoute(source = source)
                }
            }
        }
    }
}

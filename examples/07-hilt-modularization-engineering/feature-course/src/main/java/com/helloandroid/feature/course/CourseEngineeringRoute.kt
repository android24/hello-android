package com.helloandroid.feature.course

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CourseEngineeringRoute(
    viewModel: CourseEngineeringViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CourseEngineeringScreen(
        uiState = uiState,
        onRefresh = viewModel::refresh
    )
}

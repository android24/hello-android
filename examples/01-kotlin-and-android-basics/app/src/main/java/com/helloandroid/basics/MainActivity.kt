package com.helloandroid.basics

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val course = CourseInfo(
            name = getString(R.string.course_name),
            stage = getString(R.string.course_stage),
            lessonIndex = 1
        )

        Log.d(TAG, buildWelcomeMessage(course))

        setContent {
            HelloAndroidBasicsApp(course = course)
        }
    }
}

@Composable
fun HelloAndroidBasicsApp(course: CourseInfo) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CourseWelcomeScreen(course = course)
        }
    }
}

@Composable
fun CourseWelcomeScreen(course: CourseInfo) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = buildWelcomeMessage(course),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(id = R.string.course_description),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

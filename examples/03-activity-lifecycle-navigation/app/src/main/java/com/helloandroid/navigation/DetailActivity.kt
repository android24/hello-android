package com.helloandroid.navigation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val DETAIL_LIFECYCLE_TAG = "DetailActivityLifecycle"

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logLifecycle("onCreate")

        val lesson = readLessonFromIntent()

        setContent {
            DetailScreenApp(
                lesson = lesson,
                onBack = { finish() }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        logLifecycle("onStart")
    }

    override fun onResume() {
        super.onResume()
        logLifecycle("onResume")
    }

    override fun onPause() {
        logLifecycle("onPause")
        super.onPause()
    }

    override fun onStop() {
        logLifecycle("onStop")
        super.onStop()
    }

    override fun onDestroy() {
        logLifecycle("onDestroy")
        super.onDestroy()
    }

    private fun readLessonFromIntent(): NavigationLesson {
        return NavigationLesson(
            index = intent.getIntExtra(EXTRA_LESSON_INDEX, 0),
            title = intent.getStringExtra(EXTRA_LESSON_TITLE) ?: getString(R.string.default_detail_title),
            description = intent.getStringExtra(EXTRA_LESSON_DESCRIPTION) ?: getString(R.string.default_detail_description)
        )
    }

    private fun logLifecycle(callback: String) {
        Log.d(DETAIL_LIFECYCLE_TAG, callback)
    }
}

@Composable
fun DetailScreenApp(
    lesson: NavigationLesson,
    onBack: () -> Unit
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.page_background)
        ) {
            DetailScreen(
                lesson = lesson,
                onBack = onBack
            )
        }
    }
}

@Composable
fun DetailScreen(
    lesson: NavigationLesson,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.detail_prefix, lesson.index),
            style = MaterialTheme.typography.labelLarge,
            color = colorResource(id = R.color.brand_blue),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = lesson.title,
            style = MaterialTheme.typography.headlineSmall,
            color = colorResource(id = R.color.text_primary),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = lesson.description,
            style = MaterialTheme.typography.bodyLarge,
            color = colorResource(id = R.color.text_secondary)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = stringResource(id = R.string.back_to_home))
        }
    }
}

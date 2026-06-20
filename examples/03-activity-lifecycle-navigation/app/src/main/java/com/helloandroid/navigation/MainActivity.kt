package com.helloandroid.navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val MAIN_LIFECYCLE_TAG = "MainActivityLifecycle"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logLifecycle("onCreate")

        setContent {
            HelloActivityNavigationApp(
                lessons = navigationLessons(),
                onOpenLesson = ::openLesson
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

    private fun openLesson(lesson: NavigationLesson) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra(EXTRA_LESSON_INDEX, lesson.index)
            putExtra(EXTRA_LESSON_TITLE, lesson.title)
            putExtra(EXTRA_LESSON_DESCRIPTION, lesson.description)
        }
        startActivity(intent)
    }

    private fun logLifecycle(callback: String) {
        Log.d(MAIN_LIFECYCLE_TAG, callback)
    }
}

@Composable
fun HelloActivityNavigationApp(
    lessons: List<NavigationLesson>,
    onOpenLesson: (NavigationLesson) -> Unit
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.page_background)
        ) {
            LessonHomeScreen(
                lessons = lessons,
                onOpenLesson = onOpenLesson
            )
        }
    }
}

@Composable
fun LessonHomeScreen(
    lessons: List<NavigationLesson>,
    onOpenLesson: (NavigationLesson) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.home_title),
            style = MaterialTheme.typography.headlineSmall,
            color = colorResource(id = R.color.text_primary),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = colorResource(id = R.color.text_secondary)
        )

        Spacer(modifier = Modifier.height(24.dp))

        lessons.forEach { lesson ->
            NavigationLessonCard(
                lesson = lesson,
                onOpenLesson = onOpenLesson
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun NavigationLessonCard(
    lesson: NavigationLesson,
    onOpenLesson: (NavigationLesson) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_background))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${lesson.index}. ${lesson.title}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(id = R.color.text_primary),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = lesson.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(id = R.color.text_secondary)
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenLesson(lesson) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = stringResource(id = R.string.open_detail))
            }
        }
    }
}

package com.helloandroid.composebasics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HelloComposeBasicsApp()
        }
    }
}

@Composable
fun HelloComposeBasicsApp() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.page_background)
        ) {
            CourseHomeScreen(lessons = composeLessons())
        }
    }
}

@Composable
fun CourseHomeScreen(lessons: List<LessonItem>) {
    var startClickCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        CourseHeader()

        Spacer(modifier = Modifier.height(20.dp))

        StageSummary(clickCount = startClickCount)

        Spacer(modifier = Modifier.height(20.dp))

        LessonList(lessons = lessons)

        Spacer(modifier = Modifier.height(24.dp))

        StartLearningButton(
            clickCount = startClickCount,
            onClick = { startClickCount++ }
        )
    }
}

@Composable
fun CourseHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(id = R.string.course_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.text_primary)
        )

        Text(
            text = stringResource(id = R.string.course_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@Composable
fun StageSummary(clickCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.stage_background),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(id = R.string.current_stage),
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(id = R.color.brand_blue),
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = if (clickCount == 0) {
                    stringResource(id = R.string.not_started)
                } else {
                    stringResource(id = R.string.started_count, clickCount)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(id = R.color.text_primary)
            )
        }
    }
}

@Composable
fun LessonList(lessons: List<LessonItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        lessons.forEach { lesson ->
            LessonCard(lesson = lesson)
        }
    }
}

@Composable
fun LessonCard(lesson: LessonItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LessonIndexBadge(index = lesson.index)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colorResource(id = R.color.text_primary),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
        }
    }
}

@Composable
fun LessonIndexBadge(index: Int) {
    Box(
        modifier = Modifier
            .background(
                color = colorResource(id = R.color.brand_blue),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = index.toString(),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StartLearningButton(
    clickCount: Int,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = if (clickCount == 0) {
                stringResource(id = R.string.start_learning)
            } else {
                stringResource(id = R.string.keep_learning)
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun CourseHomeScreenPreview() {
    HelloComposeBasicsApp()
}

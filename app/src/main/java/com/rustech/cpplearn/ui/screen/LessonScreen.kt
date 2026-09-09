package com.rustech.cpplearn.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rustech.cpplearn.data.model.Lesson
import com.rustech.cpplearn.data.repository.ContentRepository
import com.rustech.cpplearn.ui.components.VoiceOverButton

@Composable
fun LessonScreen(
    lessonId: String,
    onQuizClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { ContentRepository() }
    var lesson by remember { mutableStateOf<Lesson?>(null) }
    var slideIndex by remember { mutableStateOf(0) }

    LaunchedEffect(lessonId) {
        lesson = repository.getLesson(lessonId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(lesson?.title ?: "Lesson") }) }
    ) { padding ->
        val currentLesson = lesson

        if (currentLesson == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp)) {

            // Animated slide-to-slide tutorial content — this is the "animated tutorial".
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = slideIndex,
                    transitionSpec = {
                        (slideInHorizontally(tween(350)) { it } + fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally(tween(350)) { -it } + fadeOut(tween(350)))
                    },
                    label = "lessonSlide"
                ) { index ->
                    val slideText = currentLesson.contentSlides.getOrNull(index)
                        ?: "Wala nang laman ang slide na ito."
                    Text(slideText, style = MaterialTheme.typography.bodyLarge)
                }
            }

            // Click-to-play voice-over — never autoplays.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                VoiceOverButton(voiceoverUrl = currentLesson.voiceoverUrl)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    enabled = slideIndex > 0,
                    onClick = { slideIndex-- }
                ) { Text("Previous") }

                if (slideIndex < currentLesson.contentSlides.lastIndex) {
                    Button(onClick = { slideIndex++ }) { Text("Next") }
                } else {
                    Button(onClick = { onQuizClick(currentLesson.id) }) { Text("Take the Quiz") }
                }
            }
        }
    }
}

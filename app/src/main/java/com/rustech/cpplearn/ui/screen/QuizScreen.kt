package com.rustech.cpplearn.ui.screen

import android.media.SoundPool
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rustech.cpplearn.R
import com.rustech.cpplearn.data.model.QuizQuestion
import com.rustech.cpplearn.data.repository.AuthRepository
import com.rustech.cpplearn.data.repository.ContentRepository
import com.rustech.cpplearn.data.repository.ScoreRepository
import com.rustech.cpplearn.ui.theme.RustechGreen
import com.rustech.cpplearn.ui.theme.RustechRed
import kotlinx.coroutines.launch

@Composable
fun QuizScreen(
    lessonId: String,
    onFinished: (score: Int, total: Int) -> Unit
) {
    val contentRepository = remember { ContentRepository() }
    val scoreRepository = remember { ScoreRepository() }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val soundPool = remember {
        SoundPool.Builder().setMaxStreams(2).build()
    }
    val correctSoundId = remember { soundPool.load(context, R.raw.correct_sound, 1) }
    val wrongSoundId = remember { soundPool.load(context, R.raw.wrong_sound, 1) }

    DisposableEffect(Unit) {
        onDispose { soundPool.release() }
    }

    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var selectedChoice by remember { mutableStateOf(-1) }
    var isRevealed by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(lessonId) {
        questions = contentRepository.getQuiz(lessonId)
        isLoading = false
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Quiz") }) }) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val question = questions.getOrNull(currentIndex)
        if (question == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Walang quiz para sa lesson na ito.")
            }
            return@Scaffold
        }

        val isCorrectAnswer = selectedChoice == question.correctIndex

        // Shake animation for the wrong choice — plays once when the answer is revealed.
        val shakeOffset = remember { Animatable(0f) }
        LaunchedEffect(isRevealed, currentIndex) {
            if (isRevealed) {
                if (isCorrectAnswer) {
                    soundPool.play(correctSoundId, 1f, 1f, 1, 0, 1f)
                } else {
                    soundPool.play(wrongSoundId, 1f, 1f, 1, 0, 1f)
                    shakeOffset.snapTo(0f)
                    val keyframes = listOf(0f, -14f, 14f, -10f, 10f, -5f, 5f, 0f)
                    for (target in keyframes) {
                        shakeOffset.animateTo(target, animationSpec = tween(45))
                    }
                }
            } else {
                shakeOffset.snapTo(0f)
            }
        }

        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp)) {
            Text(
                "Tanong ${currentIndex + 1} / ${questions.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(8.dp))
            Text(question.question, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            // Feedback banner — animates in only after the answer is checked.
            AnimatedVisibility(
                visible = isRevealed,
                enter = fadeIn(tween(250)) + expandVertically(tween(250)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                val bannerColor = if (isCorrectAnswer) RustechGreen else RustechRed
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(bannerColor.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCorrectAnswer) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                            contentDescription = null,
                            tint = bannerColor
                        )
                        Text(
                            text = if (isCorrectAnswer) "Tama! Galing!"
                            else "Mali. Ang tamang sagot ay: ${question.choices.getOrNull(question.correctIndex) ?: ""}",
                            color = bannerColor,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            question.choices.forEachIndexed { index, choice ->
                val isThisSelected = selectedChoice == index
                val isThisCorrect = index == question.correctIndex

                // Color logic: before reveal, only the selected choice is tinted.
                // After reveal, the correct answer is always green; a wrong pick is red.
                val targetColor = when {
                    !isRevealed && isThisSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    isRevealed && isThisCorrect -> RustechGreen.copy(alpha = 0.22f)
                    isRevealed && isThisSelected && !isThisCorrect -> RustechRed.copy(alpha = 0.22f)
                    else -> MaterialTheme.colorScheme.surface
                }
                val animatedColor by animateColorAsState(targetValue = targetColor, animationSpec = tween(300), label = "choiceColor")

                val cardModifier = if (isRevealed && isThisSelected && !isThisCorrect) {
                    Modifier.offset(x = shakeOffset.value.dp)
                } else {
                    Modifier
                }

                Card(
                    onClick = { if (!isRevealed) selectedChoice = index },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .then(cardModifier),
                    colors = CardDefaults.cardColors(containerColor = animatedColor)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isThisSelected,
                            onClick = { if (!isRevealed) selectedChoice = index },
                            enabled = !isRevealed
                        )
                        Text(choice, modifier = Modifier.padding(start = 8.dp).weight(1f))

                        AnimatedVisibility(
                            visible = isRevealed && (isThisCorrect || (isThisSelected && !isThisCorrect)),
                            enter = scaleIn(tween(250)) + fadeIn(tween(250))
                        ) {
                            Icon(
                                imageVector = if (isThisCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                contentDescription = null,
                                tint = if (isThisCorrect) RustechGreen else RustechRed
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (!isRevealed) {
                Button(
                    enabled = selectedChoice != -1,
                    onClick = {
                        if (isCorrectAnswer) score++
                        isRevealed = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("I-check ang Sagot")
                }
            } else {
                Button(
                    enabled = !isSubmitting,
                    onClick = {
                        if (currentIndex < questions.lastIndex) {
                            currentIndex++
                            selectedChoice = -1
                            isRevealed = false
                        } else {
                            isSubmitting = true
                            val uid = authRepository.currentUid
                            scope.launch {
                                if (uid != null) {
                                    scoreRepository.submitScore(uid, lessonId, score, questions.size)
                                }
                                isSubmitting = false
                                onFinished(score, questions.size)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (currentIndex < questions.lastIndex) "Susunod" else "Tapusin ang Quiz")
                }
            }
        }
    }
}

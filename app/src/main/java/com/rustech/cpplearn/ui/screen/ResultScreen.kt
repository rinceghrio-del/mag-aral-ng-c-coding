package com.rustech.cpplearn.ui.screen

import android.media.SoundPool
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustech.cpplearn.R
import com.rustech.cpplearn.ui.theme.RustechAmber
import com.rustech.cpplearn.ui.theme.RustechCyan
import com.rustech.cpplearn.ui.theme.RustechGreen
import com.rustech.cpplearn.ui.theme.RustechTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Itinuturing na "pasado" ang isang lesson kapag nakuha ang 60% o higit pa
// ng mga tanong nang tama.
private const val PASSING_RATIO = 0.6f

@Composable
fun ResultScreen(
    score: Int,
    total: Int,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val passed = total > 0 && (score.toFloat() / total) >= PASSING_RATIO

    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val applauseSoundId = remember { soundPool.load(context, R.raw.applause, 1) }
    DisposableEffect(Unit) {
        onDispose { soundPool.release() }
    }

    LaunchedEffect(Unit) {
        if (passed) {
            soundPool.play(applauseSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    // Animated counting-up ng score, mula 0 papunta sa totoong score.
    var displayedScore by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 0..score) {
            displayedScore = i
            delay(110L)
        }
    }

    // Bounce-in animation ng trophy icon.
    val trophyScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(150L)
        trophyScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = if (passed) RustechAmber else RustechTextSecondary,
                modifier = Modifier
                    .size(96.dp)
                    .scale(trophyScale.value)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = if (passed) "Pasado ka! Magaling!" else "Ayos lang, ulitin mo na lang!",
                style = MaterialTheme.typography.headlineMedium,
                color = if (passed) RustechGreen else MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "$displayedScore / $total",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = if (passed) RustechCyan else RustechTextSecondary
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "tamang sagot",
                style = MaterialTheme.typography.bodyMedium,
                color = RustechTextSecondary
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Bumalik sa Lessons")
            }
        }

        if (passed) {
            ConfettiOverlay()
        }
    }
}

private data class ConfettiSpec(
    val xFraction: Float,
    val colorIndex: Int,
    val delayMs: Int,
    val durationMs: Int,
    val sizeDp: Int
)

@Composable
private fun ConfettiOverlay() {
    val colors = listOf(RustechCyan, RustechAmber, RustechGreen)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val heightPx = constraints.maxHeight.toFloat()
        val widthPx = constraints.maxWidth.toFloat()

        val particles = remember {
            List(26) {
                ConfettiSpec(
                    xFraction = Random.nextFloat(),
                    colorIndex = Random.nextInt(colors.size),
                    delayMs = Random.nextInt(0, 450),
                    durationMs = Random.nextInt(1100, 1900),
                    sizeDp = Random.nextInt(6, 14)
                )
            }
        }

        particles.forEachIndexed { index, spec ->
            key(index) {
                val yAnim = remember { Animatable(-60f) }
                val rotationAnim = remember { Animatable(0f) }

                LaunchedEffect(Unit) {
                    delay(spec.delayMs.toLong())
                    launch {
                        yAnim.animateTo(
                            targetValue = heightPx + 80f,
                            animationSpec = tween(spec.durationMs, easing = LinearEasing)
                        )
                    }
                    launch {
                        rotationAnim.animateTo(
                            targetValue = 360f * (1 + spec.colorIndex),
                            animationSpec = tween(spec.durationMs, easing = LinearEasing)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (spec.xFraction * widthPx).toInt(),
                                y = yAnim.value.toInt()
                            )
                        }
                        .size(spec.sizeDp.dp)
                        .rotate(rotationAnim.value)
                        .background(colors[spec.colorIndex], RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

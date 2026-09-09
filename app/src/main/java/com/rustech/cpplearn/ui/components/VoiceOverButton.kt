package com.rustech.cpplearn.ui.components

import android.media.MediaPlayer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Floating button that plays the lesson's voice-over ONLY when tapped.
 * Never autoplays. Shows a small spinner while buffering, then a stop-able
 * playing state.
 */
@Composable
fun VoiceOverButton(voiceoverUrl: String, modifier: Modifier = Modifier) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    if (voiceoverUrl.isBlank()) return

    FloatingActionButton(
        modifier = modifier.padding(8.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        onClick = {
            if (isPlaying) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                isPlaying = false
                return@FloatingActionButton
            }

            isLoading = true
            val player = MediaPlayer()
            player.setDataSource(voiceoverUrl)
            player.setOnPreparedListener {
                isLoading = false
                isPlaying = true
                it.start()
            }
            player.setOnCompletionListener {
                isPlaying = false
                it.release()
                mediaPlayer = null
            }
            player.prepareAsync()
            mediaPlayer = player
        }
    ) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            else -> Icon(
                imageVector = if (isPlaying) Icons.Filled.HourglassEmpty else Icons.Filled.VolumeUp,
                contentDescription = "Play voice-over"
            )
        }
    }
}

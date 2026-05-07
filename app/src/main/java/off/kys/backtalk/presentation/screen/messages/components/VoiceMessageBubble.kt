package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.util.AudioPlayer
import java.io.File
import java.util.concurrent.TimeUnit

@Composable
fun VoiceMessageBubble(
    voicePath: String,
    duration: Long,
    waveformData: List<Float>,
    contentColor: Color
) {
    val audioPlayer = remember { AudioPlayer() }
    val isPlaying by audioPlayer.isPlaying.collectAsState()
    
    Row(
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = {
                if (isPlaying) {
                    audioPlayer.pause()
                } else {
                    val file = File(voicePath)
                    if (file.exists()) {
                        audioPlayer.playFile(file)
                    }
                }
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(
                    if (isPlaying) R.drawable.round_pause_24
                    else R.drawable.round_keyboard_voice_24
                ),
                contentDescription = null,
                tint = contentColor
            )
        }

        WaveformVisualizer(
            waveformData = waveformData,
            modifier = Modifier
                .weight(1f)
                .height(32.dp),
            color = contentColor
        )

        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun WaveformVisualizer(
    waveformData: List<Float>,
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = 3.dp.toPx()
        val gapWidth = 2.dp.toPx()
        val totalBarWidth = barWidth + gapWidth
        
        val maxBars = (width / totalBarWidth).toInt()
        val barsToShow = if (waveformData.size > maxBars) {
            val step = waveformData.size / maxBars
            List(maxBars) { i -> waveformData[i * step] }
        } else {
            waveformData
        }

        barsToShow.forEachIndexed { index, amplitude ->
            val x = index * totalBarWidth
            val barHeight = (amplitude * height).coerceAtLeast(2.dp.toPx())
            val y = (height - barHeight) / 2
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    return "%02d:%02d".format(minutes, seconds)
}

package off.kys.backtalk.util

import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Utility class for playing audio files.
 */
class AudioPlayer {

    private var player: MediaPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    fun playFile(file: File, onCompletion: () -> Unit = {}) {
        stop()
        
        player = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
            _isPlaying.value = true
            
            setOnCompletionListener {
                _isPlaying.value = false
                _progress.value = 1f
                onCompletion()
            }
        }
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
        _isPlaying.value = false
        _progress.value = 0f
    }
    
    fun pause() {
        player?.pause()
        _isPlaying.value = false
    }
    
    fun resume() {
        player?.start()
        _isPlaying.value = true
    }
}

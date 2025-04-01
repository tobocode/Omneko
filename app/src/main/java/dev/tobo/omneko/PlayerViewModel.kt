package dev.tobo.omneko

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PlayerViewModel : ViewModel() {
    var dataDir: File? = null

    private val _progress = MutableStateFlow(0.0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    private val _completed = MutableStateFlow(false)
    val completed: StateFlow<Boolean> = _completed.asStateFlow()

    fun downloadAndPlayVideo(context: Context, videoUri: Uri?, player: Player) {
        dataDir = File(context.cacheDir, "video")
        dataDir?.deleteRecursively()

        if (videoUri == null || dataDir == null) {
            val toast = Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT)
            toast.show()
        } else {
            if (!_running.value && !_completed.value) {
                viewModelScope.launch(Dispatchers.IO) {
                    _running.value = true

                    YoutubeDL.getInstance().init(context)
                    FFmpeg.getInstance().init(context)

                    val request = YoutubeDLRequest(videoUri.toString())
                    request.addOption("-o", dataDir?.path + "/video.%(ext)s")
                    request.addOption("-S", "ext:mp4")
                    YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, text ->
                        println("$progress % (ETA $etaInSeconds) $text")

                        _progress.value = progress.coerceIn(0.0f, 100.0f) / 100.0f
                    }

                    val videoFile = File(dataDir, "video.mp4")

                    if (videoFile.exists()) {
                        withContext(Dispatchers.Main) {
                            player.setMediaItem(MediaItem.fromUri(videoFile.toUri()))
                            player.repeatMode = Player.REPEAT_MODE_ALL
                            player.prepare()
                        }
                    }

                    _running.value = false
                    _completed.value = true
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dataDir?.deleteRecursively()
    }
}
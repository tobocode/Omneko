package dev.tobo.omneko

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

class PlayerViewModel : ViewModel() {
    var dataDir: File? = null

    private val _player: MutableStateFlow<Player?> = MutableStateFlow(null)
    val player: StateFlow<Player?> = _player.asStateFlow()

    private val _progress = MutableStateFlow(0.0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    private val _completed = MutableStateFlow(false)
    val completed: StateFlow<Boolean> = _completed.asStateFlow()

    private val _channel = MutableStateFlow("Channel")
    val channel: StateFlow<String> = _channel.asStateFlow()

    private val _title = MutableStateFlow("Video Title")
    val title: StateFlow<String> = _title.asStateFlow()

    fun downloadAndPlayVideo(context: Context, videoUri: Uri?) {
        if (_player.value == null) {
            _player.value = ExoPlayer.Builder(context).build()
        }

        if (!_running.value && !_completed.value) {
            _channel.value = ""
            _title.value = ""

            dataDir = File(context.cacheDir, "video")
            dataDir?.deleteRecursively()

            if (videoUri == null || dataDir == null) {
                val toast = Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT)
                toast.show()
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    _running.value = true

                    try {
                        YoutubeDL.getInstance().init(context)
                        FFmpeg.getInstance().init(context)

                        val request = YoutubeDLRequest(videoUri.toString())
                        request.addOption("-o", dataDir?.path + "/video.%(ext)s")
                        request.addOption("-S", "ext:mp4")
                        request.addOption("--write-comments")
                        YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, text ->
                            println("$progress % (ETA $etaInSeconds) $text")

                            _progress.value = progress.coerceIn(0.0f, 100.0f) / 100.0f
                        }
                    } catch (e: YoutubeDLException) {
                        withContext(Dispatchers.Main) {
                            val toast = Toast.makeText(context, "Unsupported URL, the video doesn't exist or YoutubeDL is out of date", Toast.LENGTH_LONG)
                            toast.show()
                        }
                    }

                    val infoFile = File(dataDir, "video.info.json")
                    if (infoFile.exists()) {
                        val json = Json { ignoreUnknownKeys = true }
                        val jsonObject = json.parseToJsonElement(infoFile.readText()).jsonObject

                        _channel.value = jsonObject["channel"]?.jsonPrimitive?.content ?: ""
                        _title.value = jsonObject["title"]?.jsonPrimitive?.content ?: ""
                    }

                    val videoFile = File(dataDir, "video.mp4")
                    if (videoFile.exists()) {
                        withContext(Dispatchers.Main) {
                            _player.value?.setMediaItem(MediaItem.fromUri(videoFile.toUri()))
                            _player.value?.repeatMode = Player.REPEAT_MODE_ALL
                            _player.value?.prepare()
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
        _player.value?.release()
        dataDir?.deleteRecursively()
    }
}
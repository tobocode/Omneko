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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

data class PlayerState(
    val player: Player? = null,
    val progress: Float = 0.0f,
    val running: Boolean = false,
    val completed: Boolean = false,
    val channel: String = "Channel",
    val uploader: String = "Uploader",
    val uploaderUrl: String = "",
    val title: String = "Video Title",
    val description: String = "Video description",
    val viewCount: Int = 0,
    val likeCount: Int = 0
)

class PlayerViewModel : ViewModel() {
    var dataDir: File? = null

    private val _playerState =  MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    fun downloadAndPlayVideo(context: Context, videoUri: Uri?) {
        if (_playerState.value.player == null) {
            _playerState.update { currentState -> currentState.copy(player = ExoPlayer.Builder(context).build()) }
        }

        if (!_playerState.value.running && !_playerState.value.completed) {
            _playerState.update { currentState ->
                currentState.copy(
                    channel = "",
                    uploader = "",
                    uploaderUrl = "",
                    title = "",
                    description = ""
                )
            }

            dataDir = File(context.cacheDir, "video")
            dataDir?.deleteRecursively()

            if (videoUri == null || dataDir == null) {
                val toast = Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT)
                toast.show()
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    _playerState.update { currentState -> currentState.copy(running = true) }

                    try {
                        YoutubeDL.getInstance().init(context)
                        FFmpeg.getInstance().init(context)

                        val request = YoutubeDLRequest(videoUri.toString())
                        request.addOption("-o", dataDir?.path + "/video.%(ext)s")
                        request.addOption("-S", "ext:mp4")
                        request.addOption("--write-comments")
                        YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, text ->
                            println("$progress % (ETA $etaInSeconds) $text")

                            _playerState.update { currentState -> currentState.copy(progress = progress.coerceIn(0.0f, 100.0f) / 100.0f) }
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

                        _playerState.update { currentState ->
                            currentState.copy(
                                channel = jsonObject["channel"]?.jsonPrimitive?.content ?: "",
                                uploader = jsonObject["uploader"]?.jsonPrimitive?.content ?: "",
                                uploaderUrl = jsonObject["uploader_url"]?.jsonPrimitive?.content ?: "",
                                title = jsonObject["title"]?.jsonPrimitive?.content ?: "",
                                description = jsonObject["description"]?.jsonPrimitive?.content ?: "",
                                viewCount = jsonObject["view_count"]?.jsonPrimitive?.content?.toInt() ?: 0,
                                likeCount = jsonObject["like_count"]?.jsonPrimitive?.content?.toInt() ?: 0
                            )
                        }
                    }

                    val videoFile = File(dataDir, "video.mp4")
                    if (videoFile.exists()) {
                        withContext(Dispatchers.Main) {
                            _playerState.value.player?.setMediaItem(MediaItem.fromUri(videoFile.toUri()))
                            _playerState.value.player?.repeatMode = Player.REPEAT_MODE_ALL
                            _playerState.value.player?.prepare()
                        }
                    }

                    _playerState.update { currentState ->
                        currentState.copy(
                            running = true,
                            completed = true
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _playerState.value.player?.release()
        dataDir?.deleteRecursively()
    }
}
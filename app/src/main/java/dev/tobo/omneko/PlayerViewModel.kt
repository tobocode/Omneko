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

data class PlayerState(
    val downloadProgress: Float = 0.0f
)

class PlayerViewModel : ViewModel() {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    fun downloadAndPlayVideo(context: Context, videoUri: Uri?, videoFile: File?, player: Player?) {
        if (player == null) {
            val toast = Toast.makeText(context, "No player was specified", Toast.LENGTH_SHORT)
            toast.show()
            return
        }

        if (videoUri == null || videoFile == null) {
            val toast = Toast.makeText(context, "No link was opened", Toast.LENGTH_SHORT)
            toast.show()
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                YoutubeDL.getInstance().init(context)
                FFmpeg.getInstance().init(context)

                val request = YoutubeDLRequest(videoUri.toString())
                request.addOption("-o", videoFile.path)
                request.addOption("-S", "ext:mp4:m4a")
                YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, text ->
                    println("$progress % (ETA $etaInSeconds) $text")
                }

                if (videoFile.exists() == true) {
                    withContext(Dispatchers.Main) {
                        player.setMediaItem(MediaItem.fromUri(videoFile.toUri()))
                        player.repeatMode = Player.REPEAT_MODE_ALL
                        player.prepare()
                    }
                }
            }
        }
    }
}
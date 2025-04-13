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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.FileInputStream

data class PlayerState(
    val player: Player? = null,
    val progress: Float = 0.0f,
    val running: Boolean = false,
    val commentsRunning: Boolean = false,
    val completed: Boolean = false,
    val commentsCompleted: Boolean = false,
    val channel: String = "Channel",
    val uploader: String = "Uploader",
    val uploaderUrl: String = "",
    val title: String = "Video Title",
    val description: String = "Video description",
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val rootComment: Comment = Comment("root", "", "", listOf(
        Comment("id1", "Comment text", "CommenterName"),
        Comment("id2", "Some more content", "AnotherCommenter", listOf(
            Comment("id3", "Test comment reply", "CommenterName"),
            Comment("id4", "One more reply", "AnotherCommenter")
        )),
        Comment("id5", "Yet another comment", "YetAnotherCommenter", listOf(
            Comment("id6", "Another reply", "AnotherCommenter")
        ))
    ))
)

data class Comment(
    val id: String,
    val text: String,
    val author: String,
    val children: List<Comment> = emptyList()
) {
    fun addCommentNode(parentId: String, comment: Comment): Comment? {
        if (parentId == this.id) {
            return this.copy(
                children = this.children + comment
            )
        }

        for (child in children) {
            val updatedChild = child.addCommentNode(parentId, comment)

            if (updatedChild != null) {
                return this.copy(
                    children = children.map { if (it.id == parentId) updatedChild else it }
                )
            }
        }

        return null
    }
}

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
                    description = "",
                    rootComment = Comment("root", "", "")
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
                        request.addOption("--write-info-json")
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
                            running = false,
                            completed = true
                        )
                    }
                }

                downloadComments(context, videoUri)
            }
        }
    }

    fun downloadComments(context: Context, videoUri: Uri?) {
        if (videoUri != null && !_playerState.value.commentsRunning && !_playerState.value.commentsCompleted) {
            viewModelScope.launch(Dispatchers.IO) {
                _playerState.update { currentState -> currentState.copy(commentsRunning = true) }

                try {
                    YoutubeDL.getInstance().init(context)
                    FFmpeg.getInstance().init(context)

                    val request = YoutubeDLRequest(videoUri.toString())
                    request.addOption("-o", dataDir?.path + "/video_comments.%(ext)s")
                    request.addOption("--write-comments")
                    request.addOption("--skip-download")
                    YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, text ->
                        println("$progress % (ETA $etaInSeconds) $text")
                    }
                } catch (e: YoutubeDLException) {
                    withContext(Dispatchers.Main) {
                        val toast = Toast.makeText(context, "Unsupported URL, the video doesn't exist or YoutubeDL is out of date", Toast.LENGTH_LONG)
                        toast.show()
                    }
                }

                val infoFile = File(dataDir, "video_comments.info.json")
                if (infoFile.exists()) {
                    val json = Json { ignoreUnknownKeys = true }
                    val jsonObject = json.parseToJsonElement(infoFile.readText()).jsonObject

                    val jsonCommentList = jsonObject["comments"]?.jsonArray

                    var rootComment = Comment("root", "", "")

                    if (jsonCommentList != null) {
                        for (jsonComment in jsonCommentList) {
                            val parent = jsonComment.jsonObject["parent"]?.jsonPrimitive?.content

                            var comment = Comment(
                                jsonComment.jsonObject["id"]?.jsonPrimitive?.content ?: "",
                                jsonComment.jsonObject["text"]?.jsonPrimitive?.content ?: "",
                                jsonComment.jsonObject["author"]?.jsonPrimitive?.content ?: ""
                            )

                            rootComment = rootComment.addCommentNode(parent ?: "root", comment) ?: rootComment
                        }
                    }

                    _playerState.update { currentState -> currentState.copy(rootComment = rootComment) }
                }

                _playerState.update { currentState ->
                    currentState.copy(
                        commentsRunning = false,
                        commentsCompleted = true
                    )
                }
            }
        }
    }

    fun copyVideoTo(context: Context, uri: Uri?) {
        if (uri == null) {
            val toast = Toast.makeText(context, "Failed to download video", Toast.LENGTH_LONG)
            toast.show()
        } else {
            val videoFile = File(dataDir, "video.mp4")

            context.contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream != null) {
                    FileInputStream(videoFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            val toast = Toast.makeText(context, "Video file saved", Toast.LENGTH_LONG)
            toast.show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        _playerState.value.player?.release()
        dataDir?.deleteRecursively()
    }
}
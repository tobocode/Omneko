package dev.tobo.omneko

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import dev.tobo.omneko.ui.theme.OmnekoTheme
import java.io.File
import java.util.concurrent.TimeUnit
import android.content.Intent
import android.widget.Toast

class MainActivity : ComponentActivity() {
    var player: Player? = null
    var videoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        videoFile = File("$cacheDir/video")
        videoFile?.delete()

        val appLinkIntent: Intent = intent
        val appLinkAction: String? = appLinkIntent.action
        val appLinkData: Uri? = appLinkIntent.data

        if (appLinkAction == Intent.ACTION_VIEW) {
            YoutubeDL.getInstance().init(this)
            val request = YoutubeDLRequest(appLinkData.toString())
            request.addOption("-o", videoFile?.path ?: "")
            YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, _ ->
                println("$progress % (ETA $etaInSeconds)")
            }
        }

        enableEdgeToEdge()
        setContent {
            OmnekoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VideoPlayer(
                        player = player,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (videoFile?.exists() == true) {
            player = ExoPlayer.Builder(this).build()
            player?.setMediaItem(MediaItem.fromUri(videoFile?.toUri() ?: Uri.EMPTY))
            player?.repeatMode = Player.REPEAT_MODE_ALL
            player?.prepare()
        } else {
            val toast = Toast.makeText(this, "No link was openend", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    override fun onPause() {
        super.onPause()
        player?.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        videoFile?.delete()
    }
}

@Composable
fun VideoPlayer(player: Player? = null, modifier: Modifier = Modifier) {
    PlayerSurface(
        player ?: ExoPlayer.Builder(LocalContext.current).build(),
        modifier.clickable {
            player?.playWhenReady = !player.playWhenReady
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PlayerPreview() {
    OmnekoTheme {
        VideoPlayer()
    }
}
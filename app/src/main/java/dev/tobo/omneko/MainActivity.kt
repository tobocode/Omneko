package dev.tobo.omneko

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import dev.tobo.omneko.ui.theme.OmnekoTheme
import java.io.File

class MainActivity : ComponentActivity() {
    var player: Player? = null
    var videoUri: Uri? = null
    var videoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = ExoPlayer.Builder(this).build()

        videoFile = File("$cacheDir/video.mp4")
        videoFile?.delete()

        val appLinkIntent: Intent = intent
        val appLinkAction: String? = appLinkIntent.action
        val appLinkData: Uri? = appLinkIntent.data

        if (appLinkAction == Intent.ACTION_VIEW) {
            videoUri = appLinkData
        }

        enableEdgeToEdge()
        setContent {
            OmnekoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VideoPlayer(Modifier.padding(innerPadding), videoUri, videoFile, player)
                }
            }
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
fun VideoPlayer(modifier: Modifier = Modifier, videoUri: Uri?, videoFile: File?, player: Player? = null, viewModel: PlayerViewModel = viewModel()) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.downloadAndPlayVideo(context, videoUri, videoFile, player)
    }

    if (player != null) {
        PlayerSurface(
            player,
            modifier.clickable {
                player.playWhenReady = !player.playWhenReady
            }
        )
    } else {
        Surface(modifier.fillMaxSize()) {  }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerPreview() {
    OmnekoTheme {
        VideoPlayer(Modifier, null, null, null)
    }
}
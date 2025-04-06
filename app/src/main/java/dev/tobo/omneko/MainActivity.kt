package dev.tobo.omneko

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.ui.compose.PlayerSurface
import dev.tobo.omneko.ui.theme.OmnekoTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    val model: PlayerViewModel by viewModels()
    var videoUri: Uri? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            Intent.ACTION_VIEW -> videoUri = intent.data
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)

                    if (URLUtil.isValidUrl(sharedText)) {
                        videoUri = sharedText?.toUri()
                    }
                }
            }
        }

        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            OmnekoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VideoPlayer(Modifier.padding(innerPadding), videoUri)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        model.player.value?.playWhenReady = false
    }
}

@Composable
fun VideoPlayer(modifier: Modifier = Modifier, videoUri: Uri?, viewModel: PlayerViewModel = viewModel()) {
    val context = LocalContext.current

    val player by viewModel.player.collectAsState()

    val progress by viewModel.progress.collectAsState()
    val completed by viewModel.completed.collectAsState()

    val channel by viewModel.channel.collectAsState()
    val title by viewModel.title.collectAsState()

    var videoProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        viewModel.downloadAndPlayVideo(context, videoUri)

        while (true) {
            delay(100)

            if (player != null) {
                if (player!!.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM) == true && player!!.isPlaying == true) {
                    videoProgress = player!!.contentPosition.toFloat() / player!!.duration.toFloat()
                }
            }
        }
    }

    Box(modifier) {
        if (player == null) {
            Surface(Modifier.fillMaxSize()) {  }
        } else {
            player?.let {
                PlayerSurface(
                    it,
                    Modifier.clickable {
                        player!!.playWhenReady = !player!!.playWhenReady
                    }
                )
            }
        }

        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            InfoBox(channel, title)

            LinearProgressIndicator(
                progress = { videoProgress },
                modifier = Modifier.fillMaxWidth(),
                strokeCap = StrokeCap.Square,
                gapSize = 0.dp,
                drawStopIndicator = { }
            )
        }

        if (videoUri != null && !completed) {
            CircularProgressIndicator(
                progress = { progress },
                Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun InfoBox(channel: String, title: String) {
    Text(
        channel,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(start = 10.dp, bottom = 5.dp)
    )

    Text(
        title,
        modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PlayerPreview() {
    OmnekoTheme {
        VideoPlayer(Modifier, null)
    }
}
package dev.tobo.omneko

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
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

class PlayerActivity : ComponentActivity() {
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
            VideoPlayer(videoUri)
        }
    }

    override fun onPause() {
        super.onPause()
        model.playerState.value.player?.playWhenReady = false
    }
}

@Composable
fun VideoPlayer(videoUri: Uri?, viewModel: PlayerViewModel = viewModel()) {
    val context = LocalContext.current

    val playerState by viewModel.playerState.collectAsState()

    var videoProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        viewModel.downloadAndPlayVideo(context, videoUri)

        while (true) {
            delay(100)

            if (playerState.player != null) {
                if (playerState.player!!.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM) == true && playerState.player!!.isPlaying == true) {
                    videoProgress = playerState.player!!.contentPosition.toFloat() / playerState.player!!.duration.toFloat()
                }
            }
        }
    }

    OmnekoTheme {
        Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Color.Black) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                if (playerState.player == null) {
                    Surface(Modifier.fillMaxSize(), color = Color.Black) {  }
                } else {
                    playerState.player?.let {
                        PlayerSurface(
                            it,
                            Modifier.clickable {
                                playerState.player!!.playWhenReady = !playerState.player!!.playWhenReady
                            }
                        )
                    }
                }

                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Row() {
                        InfoBox(modifier = Modifier.align(Alignment.Bottom).weight(1.0f), playerState.channel, playerState.title)

                        Column(modifier = Modifier.padding(10.dp)) {
                            StackButton(modifier = Modifier, Icons.AutoMirrored.Filled.Comment, "Comments", enabled = false) { }
                            StackButton(modifier = Modifier, Icons.Filled.Info, "Info", enabled = false) { }
                            StackButton(modifier = Modifier, Icons.Filled.Settings, "Settings") {
                                val intent = Intent(context, MainActivity::class.java)
                                context.startActivity(intent)
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { videoProgress },
                        modifier = Modifier.fillMaxWidth(),
                        strokeCap = StrokeCap.Square,
                        gapSize = 0.dp,
                        drawStopIndicator = { }
                    )
                }

                if (videoUri != null && !playerState.completed) {
                    LabeledProgress(modifier = Modifier.align(Alignment.Center), playerState.progress)
                }
            }
        }
    }
}

@Composable
fun InfoBox(modifier: Modifier = Modifier, channel: String, title: String) {
    Column(modifier = modifier) {
        Text(
            channel,
            color = Color.White,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 10.dp, bottom = 5.dp)
        )

        Text(
            title,
            color = Color.White,
            modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
        )
    }
}

@Composable
fun StackButton(modifier: Modifier = Modifier, icon: ImageVector, iconDescription: String, enabled: Boolean = true, onClick: () -> Unit) {
    IconButton(
        enabled = enabled,
        modifier = modifier.padding(8.dp).size(55.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = Color(0x66222222),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledContentColor = Color(0xFF666666)
        ),
        onClick = onClick
    ) {
        Icon(
            icon,
            contentDescription = iconDescription,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
fun LabeledProgress(modifier: Modifier = Modifier, progress: Float) {
    Column(modifier = modifier) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 10.dp)
        )

        Text(
            "Downloading video",
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Preview(name = "Dark mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light mode", showBackground = true)
@Composable
fun PlayerPreview() {
    VideoPlayer(null)
}
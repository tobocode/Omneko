package dev.tobo.omneko

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.webkit.URLUtil
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.ui.compose.PlayerSurface
import dev.tobo.omneko.ui.theme.OmnekoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    val mutableShowInfoSheet = remember { mutableStateOf (false) }
    var showInfoSheet by mutableShowInfoSheet

    val mutableSelectedTabIndex = remember { mutableIntStateOf(0) }
    var selectedTabIndex by mutableSelectedTabIndex

    val downloadFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.copyVideoTo(context, result.data?.data)
        }
    }

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

    OmnekoTheme(forceLightStatusBar = true) {
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
                    Row {
                        InfoBox(modifier = Modifier.align(Alignment.Bottom).weight(1.0f), playerState.channel, playerState.title)

                        Column(modifier = Modifier.padding(10.dp)) {
                            StackButton(Icons.Filled.Download, "Download", enabled = playerState.completed) {
                                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "video/mp4"

                                    putExtra(Intent.EXTRA_TITLE, "video.mp4")
                                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                                }

                                downloadFileLauncher.launch(intent)
                            }

                            StackButton(Icons.AutoMirrored.Filled.Comment, "Comments", enabled = playerState.completed) {
                                selectedTabIndex = 1
                                showInfoSheet = true
                            }

                            StackButton(Icons.Filled.Info, "Info", enabled = playerState.completed) {
                                selectedTabIndex = 0
                                showInfoSheet = true
                            }

                            StackButton(Icons.Filled.Settings, "Settings") {
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

                if (showInfoSheet) {
                    InfoSheet(mutableShowInfoSheet, mutableSelectedTabIndex, viewModel)
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
fun StackButton(icon: ImageVector, iconDescription: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
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

@Preview(name = "Info Sheet")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoSheet(mutableShowInfoSheet: MutableState<Boolean> = mutableStateOf(true), mutableSelectedTabIndex: MutableIntState = mutableIntStateOf(1), viewModel: PlayerViewModel = viewModel()) {
    val infoSheetState = if (LocalInspectionMode.current) {
        rememberStandardBottomSheetState(initialValue = SheetValue.Expanded)
    } else {
        rememberModalBottomSheetState()
    }

    val scope = rememberCoroutineScope()
    var showInfoSheet by mutableShowInfoSheet

    var selectedTabIndex by mutableSelectedTabIndex
    val pagerState = rememberPagerState(pageCount = { 2 })

    LaunchedEffect(Unit) {
        pagerState.scrollToPage(selectedTabIndex)

        snapshotFlow { pagerState.currentPage }.collect { page ->
            selectedTabIndex = page
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            showInfoSheet = false
        },
        sheetState = infoSheetState
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            indicator = { tabPositions ->
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                )
            }
        ) {
            listOf("About", "Comments").forEachIndexed { index, tabTitle ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                ) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = tabTitle
                    )
                }
            }
        }

        HorizontalPager(state = pagerState) { page ->
            when(page) {
                0 -> VideoInfoPage(viewModel)
                1 -> CommentPage(viewModel)
            }
        }
    }
}

@Composable
fun VideoInfoPage(viewModel: PlayerViewModel = viewModel()) {
    val playerState by viewModel.playerState.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(playerState.title, fontWeight = FontWeight.Bold)
        Text("Uploader: @${playerState.uploader}")

        // This creates a link that should open the uploaders channel in the browser,
        // but if the app is configured to handle the sites links, it may start downloading all
        // videos from that channel.
        // TODO: Force the link to open in the browser or add support for viewing entire channels
//        Text(buildAnnotatedString {
//            append("Uploader: ")
//
//            pushLink(LinkAnnotation.Url(
//                playerState.uploaderUrl,
//                TextLinkStyles(
//                    style = SpanStyle(color = MaterialTheme.colorScheme.primary)
//                )
//            ))
//
//            append("@${playerState.uploader}")
//            pop()
//        })

        HorizontalDivider()

        Text("Description:")
        Text(playerState.description)

        HorizontalDivider()

        Text("View count: ${playerState.viewCount}")
        Text("Like count: ${playerState.likeCount}")
    }
}

@Composable
fun CommentPage(viewModel: PlayerViewModel = viewModel()) {
    val playerState by viewModel.playerState.collectAsState()

    if (!playerState.commentsCompleted && playerState.commentsRunning) {
        Box(modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).padding(100.dp))
        }
    } else {
        if (playerState.rootComment.children.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(playerState.rootComment.children) { comment ->
                    Comment(comment, viewModel)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text("No comments available", modifier = Modifier.padding(100.dp).align(Alignment.Center))
            }
        }
    }
}

@Composable
fun Comment(comment: Comment, viewModel: PlayerViewModel = viewModel()) {
    var showReplies by remember { mutableStateOf(false) }

    Column {
        Text(comment.author,
            modifier = Modifier.padding(bottom = 4.dp),
            fontWeight = FontWeight.Bold)
        Text(comment.text)

        if (comment.children.isNotEmpty()) {
            TextButton(
                onClick = {
                    showReplies = !showReplies
                }
            ) {
                if (showReplies) {
                    Text("Hide replies")
                } else {
                    Text("Show replies")
                }
            }

            if (showReplies) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    for (reply in comment.children) {
                        Column {
                            Text(reply.author,
                                modifier = Modifier.padding(bottom = 4.dp).padding(start = 12.dp),
                                fontWeight = FontWeight.Bold)
                            Text(reply.text, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Dark mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light mode", showBackground = true)
@Composable
fun PlayerPreview() {
    VideoPlayer(null)
}
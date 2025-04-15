package dev.tobo.omneko.activity

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.preference.PreferenceManager
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import dev.tobo.omneko.PREFERENCE_CATEGORY_DOWNLOAD
import dev.tobo.omneko.PREFERENCE_CATEGORY_GENERAL
import dev.tobo.omneko.PREFERENCE_DEFAULT_CUSTOM_DOWNLOAD_QUALITY
import dev.tobo.omneko.PREFERENCE_DEFAULT_DOWNLOAD_QUALITY
import dev.tobo.omneko.PREFERENCE_DEFAULT_MAX_COMMENTS
import dev.tobo.omneko.PREFERENCE_DEFAULT_THEME
import dev.tobo.omneko.PREFERENCE_DEFAULT_USE_ARIA2C
import dev.tobo.omneko.PREFERENCE_DOWNLOAD_QUALITY_BEST
import dev.tobo.omneko.PREFERENCE_DOWNLOAD_QUALITY_CUSTOM
import dev.tobo.omneko.PREFERENCE_DOWNLOAD_QUALITY_WORST
import dev.tobo.omneko.PREFERENCE_FOOTER_CUSTOM_DOWNLOAD_QUALITY
import dev.tobo.omneko.PREFERENCE_KEY_CUSTOM_DOWNLOAD_QUALITY
import dev.tobo.omneko.PREFERENCE_KEY_DOWNLOAD_QUALITY
import dev.tobo.omneko.PREFERENCE_KEY_MAX_COMMENTS
import dev.tobo.omneko.PREFERENCE_KEY_THEME
import dev.tobo.omneko.PREFERENCE_KEY_USE_ARIA2C
import dev.tobo.omneko.PREFERENCE_LIST_MAX_COMMENTS
import dev.tobo.omneko.PREFERENCE_THEME_DARK
import dev.tobo.omneko.PREFERENCE_THEME_LIGHT
import dev.tobo.omneko.PREFERENCE_THEME_SYSTEM
import dev.tobo.omneko.PREFERENCE_VALUES_DOWNLOAD_QUALITY
import dev.tobo.omneko.PREFERENCE_VALUES_THEME
import dev.tobo.omneko.R
import dev.tobo.omneko.ui.theme.OmnekoTheme
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.footerPreference
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import me.zhanghai.compose.preference.textFieldPreference

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SettingsLayout()
        }
    }
}

fun themeSettingString(setting: String): String {
    return when (setting) {
        PREFERENCE_THEME_SYSTEM -> "Follow System"
        PREFERENCE_THEME_DARK -> "Dark mode"
        PREFERENCE_THEME_LIGHT -> "Light mode"
        else -> "null"
    }
}

fun commentsLimitString(setting: Int): String {
    if (setting == 0) {
        return "All (Downloading may take a long time)"
    }

    return setting.toString()
}

fun downloadQualityString(setting: String): String {
    return when (setting) {
        PREFERENCE_DOWNLOAD_QUALITY_BEST -> "High"
        PREFERENCE_DOWNLOAD_QUALITY_WORST -> "Low"
        PREFERENCE_DOWNLOAD_QUALITY_CUSTOM -> "Custom"
        else -> "null"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsLayout(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity

    val preferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
    val flowPreferences = FlowSharedPreferences(preferences)
    val downloadQualityPreference by flowPreferences.getString(
        PREFERENCE_KEY_DOWNLOAD_QUALITY,
        PREFERENCE_DEFAULT_DOWNLOAD_QUALITY
    ).asFlow().collectAsState(PREFERENCE_DEFAULT_DOWNLOAD_QUALITY)

    OmnekoTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = MaterialTheme.colorScheme.inverseSurface
                    ),
                    title = {
                        Text(LocalContext.current.getString(R.string.title_activity_settings))
                    },
                    navigationIcon = {
                        IconButton(onClick = { activity?.finish() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            println(innerPadding)

            ProvidePreferenceLocals {
                LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    preferenceCategory(
                        key = PREFERENCE_CATEGORY_GENERAL,
                        title = { Text("General") }
                    )

                    listPreference(
                        key = PREFERENCE_KEY_THEME,
                        defaultValue = PREFERENCE_DEFAULT_THEME,
                        values = PREFERENCE_VALUES_THEME,
                        title = { Text("Theme") },
                        summary = { Text(themeSettingString(it)) },
                        valueToText = { AnnotatedString(themeSettingString(it)) }
                    )

                    preferenceCategory(
                        key = PREFERENCE_CATEGORY_DOWNLOAD,
                        title = { Text("Download") }
                    )

                    listPreference(
                        key = PREFERENCE_KEY_MAX_COMMENTS,
                        defaultValue = PREFERENCE_DEFAULT_MAX_COMMENTS,
                        values = PREFERENCE_LIST_MAX_COMMENTS,
                        title = { Text("Maximum number of comments to download") },
                        summary = { Text(commentsLimitString(it)) },
                        valueToText = { AnnotatedString(commentsLimitString(it)) }
                    )

                    switchPreference(
                        key = PREFERENCE_KEY_USE_ARIA2C,
                        defaultValue = PREFERENCE_DEFAULT_USE_ARIA2C,
                        title = { Text("Usa Aria2c for downloads") },
                        summary = { Text("Try both and see which gives you faster downloads") }
                    )

                    listPreference(
                        key = PREFERENCE_KEY_DOWNLOAD_QUALITY,
                        defaultValue = PREFERENCE_DEFAULT_DOWNLOAD_QUALITY,
                        values = PREFERENCE_VALUES_DOWNLOAD_QUALITY,
                        title = { Text("Download quality") },
                        summary = { Text(downloadQualityString(it)) },
                        valueToText = { AnnotatedString(downloadQualityString(it)) }
                    )

                    if (downloadQualityPreference == "custom") {
                        textFieldPreference(
                            key = PREFERENCE_KEY_CUSTOM_DOWNLOAD_QUALITY,
                            defaultValue = PREFERENCE_DEFAULT_CUSTOM_DOWNLOAD_QUALITY,
                            title = { Text("Custom download quality") },
                            textToValue = { it },
                            summary = { Text(it) }
                        )

                        footerPreference(
                            key = PREFERENCE_FOOTER_CUSTOM_DOWNLOAD_QUALITY,
                            summary = { Text("The custom download quality field requires a valid string that can be passed to the \"-S\" parameter of yt-dlp, which will then automatically be preceded with the \"ext:mp4\" format selector. Invalid values may cause the download to fail. If you're unsure about this, use one of the predefined quality profiles.") }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsLayoutPreview() {
    SettingsLayout()
}
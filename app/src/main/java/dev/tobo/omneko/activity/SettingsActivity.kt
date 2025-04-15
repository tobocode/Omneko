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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import dev.tobo.omneko.PREFERENCE_DEFAULT_MAX_COMMENTS
import dev.tobo.omneko.PREFERENCE_DEFAULT_THEME
import dev.tobo.omneko.PREFERENCE_DEFAULT_USE_ARIA2C
import dev.tobo.omneko.PREFERENCE_KEY_MAX_COMMENTS
import dev.tobo.omneko.PREFERENCE_KEY_THEME
import dev.tobo.omneko.PREFERENCE_KEY_USE_ARIA2C
import dev.tobo.omneko.PREFERENCE_LIST_MAX_COMMENTS
import dev.tobo.omneko.PREFERENCE_THEME_DARK
import dev.tobo.omneko.PREFERENCE_THEME_LIGHT
import dev.tobo.omneko.PREFERENCE_THEME_SYSTEM
import dev.tobo.omneko.PREFERENCE_VALUES_THEME
import dev.tobo.omneko.R
import dev.tobo.omneko.ui.theme.OmnekoTheme
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsLayout(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity

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
                        key = "category_general",
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
                        key = "category_download",
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
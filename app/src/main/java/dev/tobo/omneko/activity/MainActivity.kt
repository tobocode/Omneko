package dev.tobo.omneko.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import dev.tobo.omneko.MeteredAlertDialog
import dev.tobo.omneko.PREFERENCE_ADDRESS_DONATE_MONERO
import dev.tobo.omneko.PREFERENCE_ADDRESS_DONATE_SOLANA
import dev.tobo.omneko.PREFERENCE_CATEGORY_DONATE
import dev.tobo.omneko.PREFERENCE_CATEGORY_DOWNLOAD
import dev.tobo.omneko.PREFERENCE_CATEGORY_GENERAL
import dev.tobo.omneko.PREFERENCE_CATEGORY_INFO
import dev.tobo.omneko.PREFERENCE_CATEGORY_METERED_WARNING
import dev.tobo.omneko.PREFERENCE_DEFAULT_CUSTOM_DOWNLOAD_QUALITY
import dev.tobo.omneko.PREFERENCE_DEFAULT_DOWNLOAD_QUALITY
import dev.tobo.omneko.PREFERENCE_DEFAULT_FIRST_RUN
import dev.tobo.omneko.PREFERENCE_DEFAULT_MAX_COMMENTS
import dev.tobo.omneko.PREFERENCE_DEFAULT_METERED_WARNING_DOWNLOAD_VIDEO
import dev.tobo.omneko.PREFERENCE_DEFAULT_METERED_WARNING_UPDATE_YOUTUBEDL
import dev.tobo.omneko.PREFERENCE_DEFAULT_THEME
import dev.tobo.omneko.PREFERENCE_DEFAULT_USE_ARIA2C
import dev.tobo.omneko.PREFERENCE_DOWNLOAD_QUALITY_BEST
import dev.tobo.omneko.PREFERENCE_DOWNLOAD_QUALITY_CUSTOM
import dev.tobo.omneko.PREFERENCE_DOWNLOAD_QUALITY_WORST
import dev.tobo.omneko.PREFERENCE_FOOTER_CUSTOM_DOWNLOAD_QUALITY
import dev.tobo.omneko.PREFERENCE_KEY_AUTHOR
import dev.tobo.omneko.PREFERENCE_KEY_CUSTOM_DOWNLOAD_QUALITY
import dev.tobo.omneko.PREFERENCE_KEY_DONATE_MONERO
import dev.tobo.omneko.PREFERENCE_KEY_DONATE_SOLANA
import dev.tobo.omneko.PREFERENCE_KEY_DOWNLOAD_QUALITY
import dev.tobo.omneko.PREFERENCE_KEY_FIRST_RUN
import dev.tobo.omneko.PREFERENCE_KEY_GITHUB
import dev.tobo.omneko.PREFERENCE_KEY_LANGUAGE
import dev.tobo.omneko.PREFERENCE_KEY_LICENSE
import dev.tobo.omneko.PREFERENCE_KEY_LINK_ASSOCIATION
import dev.tobo.omneko.PREFERENCE_KEY_MAX_COMMENTS
import dev.tobo.omneko.PREFERENCE_KEY_METERED_WARNING_DOWNLOAD_VIDEO
import dev.tobo.omneko.PREFERENCE_KEY_METERED_WARNING_UPDATE_YOUTUBEDL
import dev.tobo.omneko.PREFERENCE_KEY_THEME
import dev.tobo.omneko.PREFERENCE_KEY_UPDATE_YOUTUBEDL
import dev.tobo.omneko.PREFERENCE_KEY_USE_ARIA2C
import dev.tobo.omneko.PREFERENCE_KEY_VERSION
import dev.tobo.omneko.PREFERENCE_LIST_MAX_COMMENTS
import dev.tobo.omneko.PREFERENCE_THEME_DARK
import dev.tobo.omneko.PREFERENCE_THEME_LIGHT
import dev.tobo.omneko.PREFERENCE_THEME_SYSTEM
import dev.tobo.omneko.PREFERENCE_VALUES_DOWNLOAD_QUALITY
import dev.tobo.omneko.PREFERENCE_VALUES_THEME
import dev.tobo.omneko.R
import dev.tobo.omneko.isConnectionMetered
import dev.tobo.omneko.ui.theme.OmnekoTheme
import dev.tobo.omneko.viewmodel.MainViewModel
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.footerPreference
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import me.zhanghai.compose.preference.textFieldPreference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainLayout()
        }
    }
}

fun themeSettingString(context: Context, setting: String): String {
    return when (setting) {
        PREFERENCE_THEME_SYSTEM -> context.getString(R.string.theme_follow_system)
        PREFERENCE_THEME_DARK -> context.getString(R.string.theme_dark_mode)
        PREFERENCE_THEME_LIGHT -> context.getString(R.string.theme_light_mode)
        else -> "null"
    }
}

fun commentsLimitString(context: Context, setting: Int): String {
    if (setting == 0) {
        return context.getString(R.string.comment_download_unlimited)
    }

    return setting.toString()
}

fun downloadQualityString(context: Context, setting: String): String {
    return when (setting) {
        PREFERENCE_DOWNLOAD_QUALITY_BEST -> context.getString(R.string.download_quality_best)
        PREFERENCE_DOWNLOAD_QUALITY_WORST -> context.getString(R.string.download_quality_worst)
        PREFERENCE_DOWNLOAD_QUALITY_CUSTOM -> context.getString(R.string.download_quality_custom)
        else -> "null"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(modifier: Modifier = Modifier, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val updating by viewModel.updating.collectAsState()

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    val flowPreferences = FlowSharedPreferences(preferences)

    val downloadQualityPreference by flowPreferences.getString(
        PREFERENCE_KEY_DOWNLOAD_QUALITY,
        PREFERENCE_DEFAULT_DOWNLOAD_QUALITY
    ).asFlow().collectAsState(PREFERENCE_DEFAULT_DOWNLOAD_QUALITY)

    val meteredYoutubeDLAlertEnabled by flowPreferences.getBoolean(
        PREFERENCE_KEY_METERED_WARNING_UPDATE_YOUTUBEDL,
        PREFERENCE_DEFAULT_METERED_WARNING_UPDATE_YOUTUBEDL
    ).asFlow().collectAsState(PREFERENCE_DEFAULT_METERED_WARNING_UPDATE_YOUTUBEDL)

    var showMeteredAlertDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        if (preferences.getBoolean(PREFERENCE_KEY_FIRST_RUN, PREFERENCE_DEFAULT_FIRST_RUN)) {
            val toast = Toast.makeText(context, context.getString(R.string.first_run_update_youtubedl_toast), Toast.LENGTH_SHORT)
            toast.show()

            preferences.edit {
                putBoolean(PREFERENCE_KEY_FIRST_RUN, false)
            }

            if (meteredYoutubeDLAlertEnabled && isConnectionMetered(context)) {
                showMeteredAlertDialog = true
            } else {
                viewModel.updateYoutubeDL(context)
            }
        }
    }

    OmnekoTheme {
        if (showMeteredAlertDialog) {
            MeteredAlertDialog(
                onDismissRequest = { showMeteredAlertDialog = false },
                onConfirmation = {
                    showMeteredAlertDialog = false
                    viewModel.updateYoutubeDL(context)
                },
                stringResource(R.string.metered_alert_dialog_update_youtubedl_text)
            )
        }

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = MaterialTheme.colorScheme.inverseSurface
                    ),
                    title = {
                        Text(context.getString(R.string.app_name))
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            ProvidePreferenceLocals {
                LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    preferenceCategory(
                        key = PREFERENCE_CATEGORY_GENERAL,
                        title = { Text(stringResource(R.string.preference_category_general)) }
                    )

                    preference(
                        key = PREFERENCE_KEY_UPDATE_YOUTUBEDL,
                        title = { Text(stringResource(R.string.update_youtubedl_button)) },
                        summary = { Text(stringResource(R.string.update_youtubedl_button_summary)) },
                        enabled = !updating
                    ) {
                        if (meteredYoutubeDLAlertEnabled && isConnectionMetered(context)) {
                            showMeteredAlertDialog = true
                        } else {
                            viewModel.updateYoutubeDL(context)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        preference(
                            key = PREFERENCE_KEY_LINK_ASSOCIATION,
                            title = { Text(stringResource(R.string.link_association_button)) },
                            summary = { Text(stringResource(R.string.link_association_button_summary)) }
                        ) {
                            val intent = Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS)
                            intent.data = "package:${context.packageName}".toUri()
                            context.startActivity(intent)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        preference(
                            key = PREFERENCE_KEY_LANGUAGE,
                            title = { Text(stringResource(R.string.language_button)) },
                            summary = { Text(stringResource(R.string.language_button_summary)) }
                        ) {
                            val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                            intent.data = "package:${context.packageName}".toUri()
                            context.startActivity(intent)
                        }
                    }

                    listPreference(
                        key = PREFERENCE_KEY_THEME,
                        defaultValue = PREFERENCE_DEFAULT_THEME,
                        values = PREFERENCE_VALUES_THEME,
                        title = { Text(stringResource(R.string.preference_theme_title)) },
                        summary = { Text(themeSettingString(context, it)) },
                        valueToText = { AnnotatedString(themeSettingString(context, it)) }
                    )

                    preferenceCategory(
                        key = PREFERENCE_CATEGORY_DOWNLOAD,
                        title = { Text(stringResource(R.string.preference_category_download)) }
                    )

                    listPreference(
                        key = PREFERENCE_KEY_MAX_COMMENTS,
                        defaultValue = PREFERENCE_DEFAULT_MAX_COMMENTS,
                        values = PREFERENCE_LIST_MAX_COMMENTS,
                        title = { Text(stringResource(R.string.preference_max_comments_title)) },
                        summary = { Text(commentsLimitString(context, it)) },
                        valueToText = { AnnotatedString(commentsLimitString(context, it)) }
                    )

                    switchPreference(
                        key = PREFERENCE_KEY_USE_ARIA2C,
                        defaultValue = PREFERENCE_DEFAULT_USE_ARIA2C,
                        title = { Text(stringResource(R.string.preference_use_aria2c_title)) },
                        summary = { Text(stringResource(R.string.preference_use_aria2c_summary)) }
                    )

                    listPreference(
                        key = PREFERENCE_KEY_DOWNLOAD_QUALITY,
                        defaultValue = PREFERENCE_DEFAULT_DOWNLOAD_QUALITY,
                        values = PREFERENCE_VALUES_DOWNLOAD_QUALITY,
                        title = { Text(stringResource(R.string.preference_download_quality_title)) },
                        summary = { Text(downloadQualityString(context, it)) },
                        valueToText = { AnnotatedString(downloadQualityString(context, it)) }
                    )

                    if (downloadQualityPreference == PREFERENCE_DOWNLOAD_QUALITY_CUSTOM) {
                        textFieldPreference(
                            key = PREFERENCE_KEY_CUSTOM_DOWNLOAD_QUALITY,
                            defaultValue = PREFERENCE_DEFAULT_CUSTOM_DOWNLOAD_QUALITY,
                            title = { Text(stringResource(R.string.preference_custom_download_quality_title)) },
                            textToValue = { it },
                            summary = { Text(it) }
                        )

                        footerPreference(
                            key = PREFERENCE_FOOTER_CUSTOM_DOWNLOAD_QUALITY,
                            summary = { Text(stringResource(R.string.preference_footer_custom_download_quality)) }
                        )
                    }

                    preferenceCategory(
                        key = PREFERENCE_CATEGORY_METERED_WARNING,
                        title = { Text(stringResource(R.string.preference_category_metered_warning)) }
                    )

                    switchPreference(
                        key = PREFERENCE_KEY_METERED_WARNING_UPDATE_YOUTUBEDL,
                        defaultValue = PREFERENCE_DEFAULT_METERED_WARNING_UPDATE_YOUTUBEDL,
                        title = { Text(stringResource(R.string.preference_metered_alert_update_youtubedl_title)) },
                        summary = { Text(stringResource(R.string.preference_metered_alert_update_youtubedl_summary)) }
                    )

                    switchPreference(
                        key = PREFERENCE_KEY_METERED_WARNING_DOWNLOAD_VIDEO,
                        defaultValue = PREFERENCE_DEFAULT_METERED_WARNING_DOWNLOAD_VIDEO,
                        title = { Text(stringResource(R.string.preference_metered_alert_download_video_title)) },
                        summary = { Text(stringResource(R.string.preference_metered_alert_download_video_summary)) }
                    )

                    preferenceCategory(
                        key = PREFERENCE_CATEGORY_INFO,
                        title = { Text(stringResource(R.string.preference_category_info)) }
                    )

                    preference(
                        key = PREFERENCE_KEY_AUTHOR,
                        title = { Text(stringResource(R.string.preference_info_author_title)) },
                        summary = { Text("Tobias Wienkoop") },
                        icon = {
                            Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open in new")
                        }
                    ) {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/tobocode".toUri()
                        )

                        context.startActivity(intent)
                    }

                    preference(
                        key = PREFERENCE_KEY_VERSION,
                        title = { Text(stringResource(R.string.preference_info_version_title)) },
                        summary = {
                            Text(
                                stringResource(
                                    R.string.preference_info_version_summary,
                                    context.packageManager.getPackageInfo(context.packageName, 0).versionName.toString()
                                )
                            )
                        }
                    )

                    preference(
                        key = PREFERENCE_KEY_LICENSE,
                        title = { Text(stringResource(R.string.preference_info_license_title)) },
                        summary = { Text("GPL-3.0") }
                    )

                    preference(
                        key = PREFERENCE_KEY_GITHUB,
                        title = { Text(stringResource(R.string.preference_info_github_title)) },
                        summary = { Text("https://github.com/tobocode/Omneko") },
                        icon = {
                            Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open in new")
                        }
                    ) {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/tobocode/Omneko".toUri()
                        )

                        context.startActivity(intent)
                    }

                    preferenceCategory(
                        key = PREFERENCE_CATEGORY_DONATE,
                        title = { Text(stringResource(R.string.preference_category_donate)) }
                    )

                    preference(
                        key = PREFERENCE_KEY_DONATE_MONERO,
                        title = { Text("Monero") },
                        summary = { Text(PREFERENCE_ADDRESS_DONATE_MONERO) },
                        icon = {
                            Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Monero")
                        }
                    ) {
                        clipboardManager.setText(AnnotatedString(PREFERENCE_ADDRESS_DONATE_MONERO))
                    }

                    preference(
                        key = PREFERENCE_KEY_DONATE_SOLANA,
                        title = { Text("Solana") },
                        summary = { Text(PREFERENCE_ADDRESS_DONATE_SOLANA) },
                        icon = {
                            Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Solana")
                        }
                    ) {
                        clipboardManager.setText(AnnotatedString(PREFERENCE_ADDRESS_DONATE_SOLANA))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainLayoutPreview() {
    MainLayout()
}
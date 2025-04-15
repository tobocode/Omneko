package dev.tobo.omneko.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import dev.tobo.omneko.viewmodel.MainViewModel
import dev.tobo.omneko.R
import dev.tobo.omneko.ui.theme.OmnekoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainLayout()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(modifier: Modifier = Modifier, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val updating by viewModel.updating.collectAsState()

    LaunchedEffect(Unit) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        if (preferences.getBoolean("firstRun", true)) {
            val toast = Toast.makeText(context, "First run, updating YoutubeDL", Toast.LENGTH_SHORT)
            toast.show()

            preferences.edit {
                putBoolean("firstRun", false)
            }

            viewModel.updateYoutubeDL(context)
        }
    }

    OmnekoTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                LargeTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = MaterialTheme.colorScheme.inverseSurface
                    ),
                    title = {
                        Text(LocalContext.current.getString(R.string.app_name))
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val intent = Intent(context, SettingsActivity::class.java)
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = modifier.padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("If you have trouble watching videos, try updating YoutubeDL")

                Button(modifier = Modifier.fillMaxWidth(),
                    enabled = !updating,
                    onClick = {
                        viewModel.updateYoutubeDL(context)
                    }) {

                    Text("Update YoutubeDL")
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Text("Here you can configure what links should automatically be opened")

                    Button(modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS)
                            intent.data = "package:${context.packageName}".toUri()
                            context.startActivity(intent)
                        }) {

                        Text("Configure link handling")
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
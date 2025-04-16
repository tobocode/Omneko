package dev.tobo.omneko

import android.content.Context
import android.net.ConnectivityManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

fun isConnectionMetered(context: Context): Boolean {
    val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    return connectivityManager.isActiveNetworkMetered
}

@Composable
fun MeteredAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogText: String
) {
    AlertDialog(
        icon = { Icon(Icons.Filled.Warning, contentDescription = "Warning") },
        title = { Text(stringResource(R.string.metered_alert_dialog_title)) },
        text = { Text(dialogText) },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(
                onClick = { onConfirmation() }
            ) {
                Text(stringResource(R.string.metered_alert_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismissRequest() }
            ) {
                Text(stringResource(R.string.metered_alert_dialog_dismiss))
            }
        }
    )
}
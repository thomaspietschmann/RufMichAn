package de.pietschie.rufmichan

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.pietschie.rufmichan.alarm.ExactAlarmPermission
import de.pietschie.rufmichan.ui.navigation.RufMichAnNavHost
import de.pietschie.rufmichan.ui.theme.RufMichAnTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val app = application as RufMichAnApp

        setContent {
            RufMichAnTheme {
                CompositionLocalProvider(LocalAppContainer provides app.container) {

                    var showBatteryDialog by remember { mutableStateOf(false) }
                    var showExactAlarmDialog by remember { mutableStateOf(false) }

                    // Request POST_NOTIFICATIONS on Android 13+
                    val notifLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { /* granted or denied — UI will indicate when scheduling */ }

                    LaunchedEffect(Unit) {
                        // Ask for notification permission (Android 13+)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }

                        // Prompt for exact alarm permission (Android 12/12L)
                        if (!ExactAlarmPermission.canScheduleExactAlarms(this@MainActivity)) {
                            showExactAlarmDialog = true
                        }

                        // Suggest battery optimisation exemption (once per install, low-priority)
                        val pm = getSystemService(PowerManager::class.java)
                        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                            showBatteryDialog = true
                        }
                    }

                    if (showExactAlarmDialog) {
                        AlertDialog(
                            onDismissRequest = { showExactAlarmDialog = false },
                            title = { Text(stringResource(R.string.perm_exact_alarm_title)) },
                            text = { Text(stringResource(R.string.perm_exact_alarm_msg)) },
                            confirmButton = {
                                TextButton(onClick = {
                                    showExactAlarmDialog = false
                                    ExactAlarmPermission.requestExactAlarmPermission(this@MainActivity)
                                }) { Text(stringResource(R.string.open_settings)) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showExactAlarmDialog = false }) {
                                    Text(stringResource(R.string.later))
                                }
                            }
                        )
                    }

                    if (showBatteryDialog) {
                        AlertDialog(
                            onDismissRequest = { showBatteryDialog = false },
                            title = { Text(stringResource(R.string.battery_opt_title)) },
                            text = { Text(stringResource(R.string.battery_opt_msg)) },
                            confirmButton = {
                                TextButton(onClick = {
                                    showBatteryDialog = false
                                    @Suppress("BatteryLife")
                                    val intent = Intent(
                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:$packageName")
                                    )
                                    startActivity(intent)
                                }) { Text(stringResource(R.string.open_settings)) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showBatteryDialog = false }) {
                                    Text(stringResource(R.string.later))
                                }
                            }
                        )
                    }

                    RufMichAnNavHost()
                }
            }
        }
    }
}

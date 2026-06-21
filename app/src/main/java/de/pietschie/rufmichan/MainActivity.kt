package de.pietschie.rufmichan

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import de.pietschie.rufmichan.alarm.ExactAlarmPermission
import de.pietschie.rufmichan.ui.navigation.RufMichAnNavHost
import de.pietschie.rufmichan.ui.theme.RufMichAnTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val app = application as RufMichAnApp

        // Restore the persisted language choice before the UI inflates so there's no flicker.
        // runBlocking is acceptable here because DataStore reads from disk at most once (cached).
        val savedTag = runBlocking { app.container.settingsRepository.languageTag.first() }
        if (savedTag.isNotEmpty()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedTag))
        }

        setContent {
            RufMichAnTheme {
                CompositionLocalProvider(LocalAppContainer provides app.container) {

                    var showBatteryDialog by remember { mutableStateOf(false) }
                    var showExactAlarmDialog by remember { mutableStateOf(false) }

                    val notifLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { /* result handled implicitly — scheduling UI shows a hint if denied */ }

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        if (!ExactAlarmPermission.canScheduleExactAlarms(this@MainActivity)) {
                            showExactAlarmDialog = true
                        }
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
                                    startActivity(
                                        Intent(
                                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                            "package:$packageName".toUri()
                                        )
                                    )
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

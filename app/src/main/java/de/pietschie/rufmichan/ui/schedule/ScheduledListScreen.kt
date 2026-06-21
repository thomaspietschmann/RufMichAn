package de.pietschie.rufmichan.ui.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.pietschie.rufmichan.LocalAppContainer
import de.pietschie.rufmichan.R
import de.pietschie.rufmichan.call.ui.ContactAvatar
import de.pietschie.rufmichan.data.call.CallWithContact
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledListScreen(onScheduleNew: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: ScheduledListViewModel = viewModel(
        factory = ScheduledListViewModel.Factory(container.callRepository)
    )
    val calls by vm.activeCalls.collectAsStateWithLifecycle()

    // Ticker to update countdowns every second
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.scheduled)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onScheduleNew) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.schedule_call))
            }
        }
    ) { padding ->
        if (calls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.no_scheduled_calls),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(calls, key = { it.call.id }) { cwc ->
                    ScheduledCallCard(
                        cwc = cwc,
                        now = now,
                        onCancel = { vm.cancel(cwc.call.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduledCallCard(
    cwc: CallWithContact,
    now: Long,
    onCancel: () -> Unit
) {
    val contact = cwc.contact
    val triggerAt = cwc.call.triggerAtEpochMillis
    val remainingMs = (triggerAt - now).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(
                photoPath = contact.photoPath,
                name = contact.name,
                size = 48.dp,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatAbsoluteTime(triggerAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCountdown(remainingMs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Filled.Cancel,
                    contentDescription = stringResource(R.string.cancel_call),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dateTimeFormat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

private fun formatAbsoluteTime(epochMillis: Long): String {
    val cal = java.util.Calendar.getInstance()
    val then = java.util.Calendar.getInstance().apply { timeInMillis = epochMillis }
    return if (cal.get(java.util.Calendar.DAY_OF_YEAR) == then.get(java.util.Calendar.DAY_OF_YEAR)
        && cal.get(java.util.Calendar.YEAR) == then.get(java.util.Calendar.YEAR)
    ) {
        timeFormat.format(Date(epochMillis))
    } else {
        dateTimeFormat.format(Date(epochMillis))
    }
}

private fun formatCountdown(ms: Long): String {
    val totalSeconds = ms / 1_000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "in %dh %02dm %02ds".format(h, m, s)
    else if (m > 0) "in %dm %02ds".format(m, s)
    else "in ${s}s"
}

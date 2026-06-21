package de.pietschie.rufmichan.ui.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.pietschie.rufmichan.LocalAppContainer
import de.pietschie.rufmichan.R
import de.pietschie.rufmichan.call.ui.ContactAvatar
import de.pietschie.rufmichan.data.contact.ContactEntity
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCallScreen(
    preselectedContactId: Long?,
    onScheduled: () -> Unit,
    onCancel: () -> Unit
) {
    val container = LocalAppContainer.current
    val vm: ScheduleCallViewModel = viewModel(
        factory = ScheduleCallViewModel.Factory(
            container.contactRepository,
            container.callRepository,
            preselectedContactId
        )
    )

    val contacts by vm.contacts.collectAsStateWithLifecycle()
    val selected by vm.selectedContact.collectAsStateWithLifecycle()
    val mode by vm.mode.collectAsStateWithLifecycle()
    val countdownMinutes by vm.countdownMinutes.collectAsStateWithLifecycle()
    val targetHour by vm.targetHour.collectAsStateWithLifecycle()
    val targetMinute by vm.targetMinute.collectAsStateWithLifecycle()
    val done by vm.done.collectAsStateWithLifecycle()
    val rolledToTomorrow by vm.rolledToTomorrow.collectAsStateWithLifecycle()

    LaunchedEffect(done) { if (done) onScheduled() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val tomorrowMsg = stringResource(R.string.time_set_to_tomorrow)
    LaunchedEffect(rolledToTomorrow) {
        if (rolledToTomorrow) scope.launch { snackbarHostState.showSnackbar(tomorrowMsg) }
    }

    var contactExpanded by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = targetHour,
        initialMinute = targetMinute,
        is24Hour = true
    )
    // Keep VM in sync with time picker
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        vm.setTargetHour(timePickerState.hour)
        vm.setTargetMinute(timePickerState.minute)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.schedule_call)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Contact selector
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { contactExpanded = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContactAvatar(
                        photoPath = selected?.photoPath,
                        name = selected?.name ?: "",
                        size = 40.dp,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = selected?.name ?: stringResource(R.string.select_contact),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selected == null) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = contactExpanded,
                    onDismissRequest = { contactExpanded = false }
                ) {
                    contacts.forEach { contact ->
                        DropdownMenuItem(
                            text = { Text(contact.name) },
                            onClick = {
                                vm.selectContact(contact)
                                contactExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mode selector: Countdown / Target time
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ScheduleMode.values().forEachIndexed { idx, m ->
                    SegmentedButton(
                        selected = mode == m,
                        onClick = { vm.setMode(m) },
                        shape = SegmentedButtonDefaults.itemShape(idx, ScheduleMode.values().size),
                        label = {
                            Text(
                                if (m == ScheduleMode.COUNTDOWN) stringResource(R.string.countdown)
                                else stringResource(R.string.target_time)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (mode == ScheduleMode.COUNTDOWN) {
                CountdownInput(
                    minutes = countdownMinutes,
                    onMinutesChange = vm::setCountdownMinutes
                )
            } else {
                TimePicker(state = timePickerState)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = vm::schedule,
                enabled = selected != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.schedule))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CountdownInput(minutes: Int, onMinutesChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = minutes.toString(),
            onValueChange = { v -> v.toIntOrNull()?.let { onMinutesChange(it) } },
            label = { Text(stringResource(R.string.countdown)) },
            suffix = { Text(stringResource(R.string.minutes)) },
            singleLine = true,
            modifier = Modifier.width(180.dp)
        )
    }
}

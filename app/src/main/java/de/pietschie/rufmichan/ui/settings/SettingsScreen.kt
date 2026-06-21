package de.pietschie.rufmichan.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.pietschie.rufmichan.LocalAppContainer
import de.pietschie.rufmichan.R
import de.pietschie.rufmichan.call.ui.CallStylePreview
import de.pietschie.rufmichan.data.settings.CallStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val container = LocalAppContainer.current
    val vm: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(container.settingsRepository)
    )

    val callStyle by vm.callStyle.collectAsStateWithLifecycle()
    val languageTag by vm.languageTag.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader(stringResource(R.string.theme))

            // Tappable design previews — see each call screen before choosing it.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CallStyle.entries.forEach { style ->
                    val labelRes = when (style) {
                        CallStyle.SYSTEM -> R.string.theme_system
                        CallStyle.PIXEL -> R.string.theme_pixel
                        CallStyle.SAMSUNG -> R.string.theme_samsung
                        CallStyle.MIUI -> R.string.theme_miui
                        CallStyle.ONEPLUS -> R.string.theme_oneplus
                    }
                    CallStylePreview(
                        style = style,
                        label = stringResource(labelRes),
                        selected = callStyle == style,
                        onClick = { vm.setCallStyle(style) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SectionHeader(stringResource(R.string.language))

            // "System default" entry (empty tag = follow OS locale).
            RadioRow(
                label = stringResource(R.string.system_default),
                selected = languageTag.isEmpty(),
                onClick = {
                    vm.setLanguageTag("")
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                }
            )

            val languages = listOf(
                "en" to R.string.language_en,
                "es" to R.string.language_es,
                "de" to R.string.language_de,
                "fr" to R.string.language_fr,
                "it" to R.string.language_it,
                "tr" to R.string.language_tr,
            )
            languages.forEach { (tag, labelRes) ->
                RadioRow(
                    label = stringResource(labelRes),
                    selected = languageTag == tag,
                    onClick = {
                        vm.setLanguageTag(tag)
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(tag)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

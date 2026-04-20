package off.kys.backtalk.presentation.screen.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.R
import off.kys.backtalk.common.BacktalkPreferences
import off.kys.backtalk.common.Constants
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.util.isSecurityEnabled
import off.kys.backtalk.util.openUrl
import off.kys.backtalk.util.toast
import org.koin.compose.koinInject

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val prefs = koinInject<BacktalkPreferences>()
        val context = LocalContext.current

        var themeMode by remember { mutableStateOf(prefs.themeMode) }
        var dynamicColor by remember { mutableStateOf(prefs.dynamicColorEnabled) }
        var lockEnabled by remember { mutableStateOf(prefs.lockEnabled) }
        var secureScreen by remember { mutableStateOf(prefs.secureScreenEnabled) }

        Scaffold(
            topBar = {
                LargeTopAppBar(title = { Text(stringResource(R.string.settings)) })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsSectionTitle(stringResource(R.string.appearance))

                ThemeSelector(
                    selected = themeMode,
                    onSelected = {
                        prefs.themeMode = it
                        themeMode = it
                    }
                )

                ToggleSetting(
                    label = stringResource(R.string.material_you_dynamic_color),
                    checked = dynamicColor,
                    onCheckedChange = {
                        prefs.dynamicColorEnabled = it
                        dynamicColor = it
                    }
                )

                HorizontalDivider()
                SettingsSectionTitle(stringResource(R.string.privacy_security))

                if (context.isSecurityEnabled()) ToggleSetting(
                    label = stringResource(R.string.enable_app_lock),
                    checked = lockEnabled,
                    requireRestart = true,
                    onCheckedChange = {
                        prefs.lockEnabled = it
                        lockEnabled = it
                    }
                )

                ToggleSetting(
                    label = stringResource(R.string.secure_screen_block_screenshots),
                    checked = secureScreen,
                    onCheckedChange = {
                        prefs.secureScreenEnabled = it
                        secureScreen = it
                    }
                )

                HorizontalDivider()
                SettingsSectionTitle(stringResource(R.string.about))

                InfoRow(
                    label = stringResource(R.string.version),
                    value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                )

                InfoRow(
                    label = stringResource(R.string.developer),
                    value = stringResource(R.string.dev_name),
                    onClick = { context.toast(R.string.dev_click) }
                )

                InfoRow(
                    label = stringResource(R.string.license),
                    value = stringResource(R.string.mit),
                    onClick = { context.openUrl(Constants.BACKTALK_MIT_LICENSE_RAW_URL) }
                )
            }
        }
    }

    @Composable
    private fun SettingsSectionTitle(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    @Composable
    private fun InfoRow(
        label: String,
        value: String,
        onClick: (() -> Unit)? = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }

    @Composable
    private fun ToggleSetting(
        label: String,
        checked: Boolean,
        requireRestart: Boolean = false,
        onCheckedChange: (Boolean) -> Unit
    ) {
        val context = LocalContext.current

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                    if (requireRestart) {
                        context.toast(R.string.restart_app_to_apply_changes)
                    }
                }
            )
        }
    }

    @Composable
    private fun ThemeSelector(selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
        Column(Modifier.selectableGroup()) {
            ThemeMode.entries.forEach { mode ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onSelected(mode) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (mode == selected),
                        onClick = { onSelected(mode) }
                    )
                    Text(
                        text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}
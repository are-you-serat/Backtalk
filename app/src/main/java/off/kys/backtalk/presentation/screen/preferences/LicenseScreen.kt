package off.kys.backtalk.presentation.screen.preferences

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.R

class LicenseScreen : Screen {

    @Composable
    override fun Content() {
        LicenseContent()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun LicenseContent() {
        val navigator = LocalNavigator.currentOrThrow
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        // Exhaustive list based on your TOML file
        val libraries = listOf(
            LibraryInfo("Activity Compose", "1.13.0", "Apache License 2.0"),
            LibraryInfo("Biometric", "1.4.0-alpha07", "Apache License 2.0"),
            LibraryInfo("Concurrent Futures", "1.3.0", "Apache License 2.0"),
            LibraryInfo("Core KTX", "1.18.0", "Apache License 2.0"),
            LibraryInfo("DataStore Preferences", "1.2.1", "Apache License 2.0"),
            LibraryInfo("DocumentFile", "1.1.0", "Apache License 2.0"),
            LibraryInfo("Espresso Core", "3.7.0", "Apache License 2.0"),
            LibraryInfo("Gau", "1.0.0", "MIT License"),
            LibraryInfo("Jetpack Compose BOM", "2026.04.01", "Apache License 2.0"),
            LibraryInfo("JUnit", "4.13.2", "Eclipse Public License 1.0"),
            LibraryInfo("JUnit Extension", "1.3.0", "Apache License 2.0"),
            LibraryInfo("Koin", "4.2.1", "Apache License 2.0"),
            LibraryInfo("Kotlinx Serialization", "1.11.0", "Apache License 2.0"),
            LibraryInfo("Lifecycle Process", "2.10.0", "Apache License 2.0"),
            LibraryInfo("Lifecycle Runtime KTX", "2.10.0", "Apache License 2.0"),
            LibraryInfo("Mockito", "5.23.0", "MIT License"),
            LibraryInfo("Mockito-Kotlin", "6.3.0", "MIT License"),
            LibraryInfo("Room", "2.8.4", "Apache License 2.0"),
            LibraryInfo("Splashscreen", "1.2.0", "Apache License 2.0"),
            LibraryInfo("Voyager", "1.1.0-beta03", "MIT License"),
            LibraryInfo("WorkManager", "2.11.2", "Apache License 2.0")
        ).sortedBy { it.name }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.settings_license),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                painter = painterResource(R.drawable.round_arrow_back_24),
                                contentDescription = stringResource(R.string.common_navigate_up)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                itemsIndexed(libraries) { index, library ->
                    LicenseItem(library)

                    if (index < libraries.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun LicenseItem(library: LibraryInfo) {
        ListItem(
            headlineContent = {
                Text(
                    text = library.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                Text(
                    text = library.license,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Text(
                    text = library.version,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        )
    }

    private data class LibraryInfo(
        val name: String,
        val version: String,
        val license: String
    )
}
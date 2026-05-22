package off.kys.backtalk.presentation.screen.license

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.screen.Screen
import off.kys.backtalk.presentation.components.SplitThemeContainer
import off.kys.backtalk.presentation.screen.license.components.LicenseContent

class LicenseScreen : Screen {

    @Composable
    override fun Content() {
        LicenseContent()
    }

}

@Preview(
    showSystemUi = true,
    device = "id:pixel_10",
)
@Composable
private fun LicenseContentPreview() {
    SplitThemeContainer {
        LicenseContent()
    }
}
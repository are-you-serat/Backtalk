package off.kys.backtalk.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import off.kys.backtalk.presentation.theme.BacktalkTheme

@Composable
fun SplitThemeContainer(
    modifier: Modifier = Modifier,
    splitFraction: Float = 0.5f,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    clipRect(
                        left = 0f,
                        top = 0f,
                        right = size.width * splitFraction,
                        bottom = size.height
                    ) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            BacktalkTheme(darkTheme = false) {
                content()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    clipRect(
                        left = size.width * splitFraction,
                        top = 0f,
                        right = size.width,
                        bottom = size.height
                    ) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            BacktalkTheme(darkTheme = true) {
                content()
            }
        }
    }
}
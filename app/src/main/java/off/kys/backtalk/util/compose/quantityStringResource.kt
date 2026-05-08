package off.kys.backtalk.util.compose

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalResources

@Composable
@ReadOnlyComposable
fun quantityStringResource(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any): String {
    val resources = LocalResources.current
    return resources.getQuantityString(resId, quantity, *formatArgs)
}
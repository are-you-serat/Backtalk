package off.kys.backtalk.util.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember

@Composable
fun rememberScrollToBottomVisibility(listState: LazyListState): Boolean {
    val previousOffset = remember { mutableIntStateOf(0) }
    val previousIndex = remember { mutableIntStateOf(0) }

    val showScrollToBottom by remember {
        derivedStateOf {
            val currentIndex = listState.firstVisibleItemIndex
            val currentOffset = listState.firstVisibleItemScrollOffset
            val isScrollingUp = if (currentIndex == previousIndex.intValue) {
                currentOffset < previousOffset.intValue
            } else {
                currentIndex < previousIndex.intValue
            }

            previousIndex.intValue = currentIndex
            previousOffset.intValue = currentOffset

            currentIndex > 2 && isScrollingUp
        }
    }

    return showScrollToBottom
}
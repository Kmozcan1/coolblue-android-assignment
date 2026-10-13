package com.coolblue.assignment.search.ui.view

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Stable
internal class CollapsingHeaderState {
    var heightPx by mutableIntStateOf(0)

    var offsetPx by mutableFloatStateOf(0f)
        private set

    private val hiddenOffsetPx get() = -heightPx.toFloat()

    private var animation: Job? = null

    val scrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            animation?.cancel()
            val previous = offsetPx
            offsetPx = (offsetPx + available.y).coerceIn(hiddenOffsetPx, 0f)
            val consumedByHeader = offsetPx - previous
            return Offset(0f, consumedByHeader)
        }
    }

    suspend fun snapToShownOrHidden() {
        val isMostlyHidden = offsetPx < hiddenOffsetPx / 2
        animateTo(if (isMostlyHidden) hiddenOffsetPx else 0f)
    }

    suspend fun show() = animateTo(0f)

    private suspend fun animateTo(target: Float) = coroutineScope {
        animation?.cancel()
        if (offsetPx != target) animation = launch { animate(offsetPx, target) { value, _ -> offsetPx = value } }
    }
}

@Composable
internal fun rememberCollapsingHeaderState() = remember { CollapsingHeaderState() }

internal fun Modifier.collapsingHeader(state: CollapsingHeaderState) = clipToBounds().layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    state.heightPx = placeable.height
    val offset = state.offsetPx.roundToInt().coerceAtLeast(-placeable.height)
    val visibleHeight = placeable.height + offset
    layout(placeable.width, visibleHeight) { placeable.place(0, offset) }
}

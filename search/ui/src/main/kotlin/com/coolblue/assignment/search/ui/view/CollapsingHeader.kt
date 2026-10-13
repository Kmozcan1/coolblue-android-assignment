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
import androidx.compose.ui.unit.Velocity
import kotlin.math.roundToInt

/**
 * A header that slides away as the list scrolls down and comes back as soon as it scrolls up, then
 * settles fully shown or fully hidden. The list hands it the scroll through [scrollConnection].
 */
@Stable
internal class CollapsingHeaderState {
    /** The header's full height in pixels, measured by [collapsingHeader]. */
    var height by mutableIntStateOf(0)

    /** 0 when shown, -[height] when hidden. */
    var offset by mutableFloatStateOf(0f)
        private set

    val scrollConnection = object : NestedScrollConnection {
        // Before the list scrolls, the header takes what it needs to move: the content then follows the finger.
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val previous = offset
            offset = (offset + available.y).coerceIn(-height.toFloat(), 0f)
            return Offset(0f, offset - previous)
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            animateTo(if (offset < -height / 2f) -height.toFloat() else 0f)
            return Velocity.Zero
        }
    }

    suspend fun show() = animateTo(0f)

    private suspend fun animateTo(target: Float) {
        if (offset != target) animate(offset, target) { value, _ -> offset = value }
    }
}

@Composable
internal fun rememberCollapsingHeaderState() = remember { CollapsingHeaderState() }

/** Places the header at its offset and takes only the height still showing, so the list below grows. */
internal fun Modifier.collapsingHeader(state: CollapsingHeaderState) = clipToBounds().layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    state.height = placeable.height
    val offset = state.offset.roundToInt().coerceAtLeast(-placeable.height)
    layout(placeable.width, placeable.height + offset) { placeable.place(0, offset) }
}

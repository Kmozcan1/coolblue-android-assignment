package com.coolblue.assignment.search.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.coolblue.assignment.search.ui.R

private val ToggleShape = RoundedCornerShape(12.dp)

/** The current language's flag. Tapped, it opens up to show the others. It fills the height it is given. */
@Composable
internal fun LanguageToggle(
    current: AppLanguage,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(ToggleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Flag(
            language = current,
            description = stringResource(R.string.language_current, current.nativeName),
            state = stringResource(if (expanded) R.string.languages_expanded else R.string.languages_collapsed),
            onClickLabel = stringResource(if (expanded) R.string.languages_hide else R.string.languages_show),
            onClick = { onExpandedChange(!expanded) },
        )
        AppLanguage.entries.filter { it != current }.forEach { language ->
            AnimatedVisibility(visible = expanded, label = "language") {
                Flag(
                    language = language,
                    description = language.nativeName,
                    onClick = {
                        onExpandedChange(false)
                        onSelect(language)
                    },
                )
            }
        }
    }
}

@Composable
private fun Flag(
    language: AppLanguage,
    description: String,
    onClick: () -> Unit,
    state: String? = null,
    onClickLabel: String? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(48.dp)
            .clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = description
                if (state != null) stateDescription = state
            },
        contentAlignment = Alignment.Center,
    ) {
        // Silent, or TalkBack would also read the emoji's own name ("flag: Netherlands").
        Text(language.flag, style = MaterialTheme.typography.titleLarge, modifier = Modifier.clearAndSetSemantics {})
    }
}

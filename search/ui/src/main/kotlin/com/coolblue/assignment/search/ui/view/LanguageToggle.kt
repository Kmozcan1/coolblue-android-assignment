package com.coolblue.assignment.search.ui.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.coolblue.assignment.search.ui.R
import java.util.Locale

/** The languages the app is translated into. Each one names itself, as language pickers do. */
internal enum class AppLanguage(val tag: String, val flag: String, val nativeName: String) {
    English("en", "🇬🇧", "English"),
    Dutch("nl", "🇳🇱", "Nederlands"),
    ;

    val locale: Locale get() = Locale.forLanguageTag(tag)
}

/** The language the strings resolved to: the user's pick, else Dutch on a Dutch phone and English otherwise. */
@Composable
internal fun currentAppLanguage(): AppLanguage {
    val tag = stringResource(R.string.language_tag)
    return AppLanguage.entries.first { it.tag == tag }
}

private val ToggleShape = RoundedCornerShape(12.dp)

/** The current language's flag. Tapped, it opens up to show the other languages' flags. */
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
            .height(TextFieldDefaults.MinHeight)
            .clip(ToggleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Flag(
            language = current,
            description = stringResource(R.string.language_current, current.nativeName),
            onClickLabel = stringResource(if (expanded) R.string.languages_hide else R.string.languages_show),
            onClick = { onExpandedChange(!expanded) },
        )
        if (expanded) {
            AppLanguage.entries.filter { it != current }.forEach { language ->
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
    onClickLabel: String? = null,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        // Silent, or TalkBack would also read the emoji's own name ("flag: Netherlands").
        Text(language.flag, style = MaterialTheme.typography.titleLarge, modifier = Modifier.clearAndSetSemantics {})
    }
}

package com.coolblue.assignment.search.ui.view

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.coolblue.assignment.search.ui.R
import com.coolblue.assignment.search.ui.mapper.formatCount
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

/**
 * A plural in the app's language, count included. pluralStringResource follows the phone's language,
 * which on a French or Russian phone picks the wrong form of the English text ("0 review", "21 review").
 */
@SuppressLint("AppBundleLocaleChanges") // the app module keeps every language in the base APK
@Composable
internal fun appPluralString(@PluralsRes id: Int, count: Int): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = currentAppLanguage().locale
    val resources = remember(context, configuration, locale) {
        context.createConfigurationContext(Configuration(configuration).apply { setLocale(locale) }).resources
    }
    return resources.getQuantityString(id, count, formatCount(count, locale))
}

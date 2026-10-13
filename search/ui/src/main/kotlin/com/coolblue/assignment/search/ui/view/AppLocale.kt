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

/**
 * The language the strings resolved to: Dutch on a Dutch phone, or when picked in Android 13+ settings;
 * English otherwise. The phone's own variant when it speaks that language (nl-BE, en-GB), so TalkBack
 * keeps its voice for the numbers.
 */
@Composable
internal fun appLocale(): Locale {
    val language = stringResource(R.string.language_tag)
    val phone = LocalConfiguration.current.locales[0]
    return if (phone.language == language) phone else Locale.forLanguageTag(language)
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
    val locale = appLocale()
    val resources = remember(context, configuration, locale) {
        context.createConfigurationContext(Configuration(configuration).apply { setLocale(locale) }).resources
    }
    return resources.getQuantityString(id, count, formatCount(count, locale))
}
